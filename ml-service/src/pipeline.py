# dataforge/src/pipeline.py

"""Главный пайплайн анализа датасета."""

import numpy as np
from typing import Dict, Any, Optional, List
import json
from fastapi import UploadFile
import numpy as np

def convert_numpy_to_python(obj):
    """Рекурсивно преобразует numpy типы в Python типы."""
    if isinstance(obj, np.ndarray):
        return obj.tolist()
    if isinstance(obj, (np.int64, np.int32, np.int16, np.int8)):
        return int(obj)
    if isinstance(obj, (np.float64, np.float32, np.float16)):
        return float(obj)
    if isinstance(obj, np.bool_):
        return bool(obj)
    if isinstance(obj, dict):
        return {k: convert_numpy_to_python(v) for k, v in obj.items()}
    if isinstance(obj, (list, tuple)):
        return [convert_numpy_to_python(item) for item in obj]
    return obj

async def run_full_pipeline(
    images: List[UploadFile],
    markup_file: UploadFile,
    pred_probs_file: Optional[UploadFile] = None,
    image_col: str = "image_name",
    label_col: str = "label",
    temperature: Optional[float] = None,
) -> Dict[str, Any]:
    """Запускает полный анализ датасета."""
    """Запускает полный анализ датасета."""
    from .load_data import load_dataset
    from .baseline_model import get_model, get_transforms, get_embeddings, get_pred_probs
    from .metrics import (
        compute_entropy, compute_confidence, compute_deficit_per_object,
        compute_imbalance_index, compute_balance_coefficient  # ✅ добавить
    )
    from .analyze import (
        compute_label_issues, compute_outliers, compute_near_duplicates,
        compute_label_issues_combined,      # ✅ добавить
        compute_duplicates_combined         # ✅ добавить
    )
    from .tags import compute_tags, _select_primary_from_duplicate_group
    from .utility import compute_utility_scores
    from .groups import split_into_groups
    from .roadmap import generate_roadmap
    from .summary import generate_summary
    from .analyze import separate_outlier_novelty

    print("Загрузка данных...")
    dataset, targets, class_names = await load_dataset(images, markup_file, image_col, label_col)
    n_total, n_classes = len(targets), len(class_names)

    print("[1/6] Извлечение эмбеддингов...")
    model = get_model()
    transforms = get_transforms()
    embeddings = get_embeddings(model, dataset, transforms)

    if pred_probs_file is not None:
            print(f"Загрузка пользовательских вероятностей из JSON: {pred_probs_file.filename}")

            # Проверяем расширение
            if not pred_probs_file.filename.endswith('.json'):
                raise ValueError(f"Файл вероятностей должен быть JSON, получен: {pred_probs_file.filename}")

            # Читаем содержимое UploadFile
            content = await pred_probs_file.read()

            # Парсим JSON
            try:
                pred_probs = np.array(json.loads(content.decode('utf-8')))
            except json.JSONDecodeError as e:
                raise ValueError(f"Ошибка парсинга JSON: {e}")

            # Валидация
            if len(pred_probs) != n_total:
                raise ValueError(
                    f"Размер pred_probs ({len(pred_probs)}) не совпадает с объектами ({n_total})"
                )
            if pred_probs.shape[1] != n_classes:
                raise ValueError(
                    f"Число классов ({pred_probs.shape[1]}) не совпадает с датасетом ({n_classes})"
                )
            temp = None  # температура не используется при загрузке готовых вероятностей

            print(f"✅ Загружены вероятности shape: {pred_probs.shape}")
    else:
        print("[1.1/6] Обучение baseline модели и получение вероятностей...")
        pred_probs, temp = get_pred_probs(embeddings, targets, temperature)

    print("[2/6] Базовые метрики...")
    entropy = compute_entropy(pred_probs)
    confidence = compute_confidence(pred_probs)
    deficit_per_obj = compute_deficit_per_object(targets, n_classes)
    imbalance_index = compute_imbalance_index(targets, n_classes)
    balance_coeff = compute_balance_coefficient(targets, n_classes)

    print("[3/6] Cleanlab + JSD анализ...")
    label_results = compute_label_issues(pred_probs, targets)
    outlier_results = compute_outliers(embeddings)
    dup_results_raw = compute_near_duplicates(embeddings)

    combined_label = compute_label_issues_combined(
        pred_probs=pred_probs, targets=targets, n_classes=n_classes,
        label_scores=label_results["label_scores"],
    )
    outlier_sep = separate_outlier_novelty(outlier_results["outlier_scores"], entropy)
    dup_results = compute_duplicates_combined(embeddings, pred_probs, dup_results_raw)

    print("[4/6] Дубликаты и полезность...")
    primary_indices = set()
    near_dup_sets = dup_results.get("near_duplicate_sets", []) or []
    for group in near_dup_sets:
        if len(group) > 0:
            primary = _select_primary_from_duplicate_group(
                group, label_results["label_scores"], outlier_results["outlier_scores"], confidence
            )
            primary_indices.add(primary)

    is_duplicate_flagged = dup_results["is_duplicate"].copy()
    for i in primary_indices:
        is_duplicate_flagged[i] = False
    n_duplicates = int(is_duplicate_flagged.sum())

    utility_scores = compute_utility_scores(
        entropy=entropy, outlier_scores=outlier_results["outlier_scores"],
        deficit_per_obj=deficit_per_obj, label_scores=label_results["label_scores"],
        is_duplicate=is_duplicate_flagged,
        js_label_scores=combined_label["js_label_scores"],
        is_outlier_flag=outlier_sep["is_outlier"],
        is_novelty_flag=outlier_sep["is_novelty"],
    )

    print("[5/6] Теги и группы...")
    tags = compute_tags(
        label_scores=label_results["label_scores"], entropy=entropy,
        confidence=confidence, outlier_scores=outlier_results["outlier_scores"],
        is_duplicate=dup_results["is_duplicate"], deficit_per_obj=deficit_per_obj,
        near_duplicate_sets=near_dup_sets,
        js_label_scores=combined_label["js_label_scores"],
        is_outlier_flag=outlier_sep["is_outlier"],
        is_novelty_flag=outlier_sep["is_novelty"],
    )

    label_issue_indices = [
        i for i in range(n_total)
        if ("label_error" in tags[i] or "label_suspicious" in tags[i])
           and label_results["suggested_labels"][i] != targets[i]
    ]

    groups = split_into_groups(
        tags=tags, utility_scores=utility_scores, label_scores=label_results["label_scores"],
        entropy=entropy, confidence=confidence, outlier_scores=outlier_results["outlier_scores"],
        targets=targets, suggested_labels=label_results["suggested_labels"],
        predicted_labels=label_results["predicted_labels"],
        near_duplicate_sets=near_dup_sets, is_duplicate_flagged=is_duplicate_flagged,
        filenames=dataset.filenames, class_names=class_names,
        js_label_scores=combined_label["js_label_scores"],
    )

    print("[6/6] Дорожная карта и статистика...")
    roadmap = generate_roadmap(groups, targets, n_classes, class_names)
    summary = generate_summary(
        targets=targets, n_classes=n_classes, class_names=class_names,
        imbalance_index=imbalance_index, n_label_errors=len(label_issue_indices),
        n_duplicates=n_duplicates, n_quality_issues=0,
        balance_coefficient=balance_coeff,
        n_controversial=int(combined_label["is_controversial"].sum()),
        n_outliers=int(outlier_sep["is_outlier"].sum()),
    )

    results = {
        "summary": summary, "groups": groups, "roadmap": roadmap,
        "embeddings": embeddings.tolist(), "pred_probs": pred_probs.tolist(), "temperature": temp,
    }

    print(f"Анализ завершён. Готовность: {summary['readiness']}%")
    return results



def _round_dict(obj, decimals=4):
    """Рекурсивно округляет float до указанного числа знаков."""
    if isinstance(obj, dict):
        return {k: _round_dict(v, decimals) for k, v in obj.items()}
    if isinstance(obj, list):
        return [_round_dict(item, decimals) for item in obj]
    if isinstance(obj, (float, np.floating)):
        return round(float(obj), decimals)
    return obj


def pipeline_to_json(results: Dict[str, Any], indent: int = 2) -> str:
    """Преобразует результаты в JSON."""
    json_results = {
        "summary": _round_dict(results["summary"]),
        "groups": _round_dict(results["groups"]),
        "roadmap": _round_dict(results["roadmap"]),
        "temperature": round(float(results["temperature"]), 4),
    }
    return json.dumps(json_results, ensure_ascii=False, indent=indent)