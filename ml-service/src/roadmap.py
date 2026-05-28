# dataforge/src/roadmap.py

"""Дорожная карта улучшения датасета."""

import numpy as np
from typing import Dict, Any, List, Optional


def generate_roadmap(
    groups: Dict[str, Any],
    targets: np.ndarray,
    n_classes: int,
    class_names: Optional[List[str]] = None,
) -> List[Dict[str, Any]]:
    """Генерирует упорядоченный список рекомендаций."""
    from .metrics import compute_class_kl_contributions

    if class_names is None:
        class_names = [str(i) for i in range(n_classes)]

    actions = []
    n_label_errors = len(groups["label_issues"])
    n_duplicates = sum(len(g["copies"]) for g in groups["duplicates"])
    n_quality_issues = len(groups["quality_issues"])

    # Подсчёт по тегам из quality_issues
    n_controversial = _count_tag_in_group(groups["quality_issues"], "controversial")
    n_outliers = _count_tag_in_group(groups["quality_issues"], "outlier")
    n_low_quality = n_quality_issues - n_controversial - n_outliers

    # Дефицит через KL-вклад
    kl_contributions = compute_class_kl_contributions(targets, n_classes)
    min_contrib = np.min(kl_contributions)

    if n_label_errors > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Переразметить {n_label_errors} объектов с высокой вероятностью ошибки разметки",
        })

    if n_controversial > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Экспертно проверить {n_controversial} спорных случаев (объекты нетипичны для своего класса)",
        })

    if min_contrib < 0:
        # Сортируем классы по дефициту: самые отрицательные вклады → самые дефицитные
        deficit_order = np.argsort(kl_contributions)  # от самого отрицательного к положительному
        for i in deficit_order:
            if kl_contributions[i] < 0:
                deficit = kl_contributions[i] / min_contrib  # нормализация [0, 1]
                count = int(np.bincount(targets, minlength=n_classes)[i])
                # Целевое количество для равномерного распределения
                target_count = len(targets) // n_classes
                needed = max(1, target_count - count)
                actions.append({
                    "id": len(actions) + 1,
                    "action": f"Добрать {needed} примеров класса '{class_names[i]}'",
                })

    if n_duplicates > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Удалить {n_duplicates} дубликатов",
        })

    n_suspicious = _count_suspicious(groups["all_objects"])
    if n_suspicious > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Проверить {n_suspicious} объектов с сомнительными метками",
        })

    if n_outliers > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Проверить {n_outliers} выбросов — потенциально некачественные или ошибочные данные",
        })


    if n_low_quality > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Проверить {n_low_quality} объектов низкого технического качества",
        })

    return actions


def _count_tag_in_group(group: List[Dict[str, Any]], tag: str) -> int:
    """Считает объекты с указанным тегом в группе."""
    count = 0
    for obj in group:
        if tag in obj.get("tags", []):
            count += 1
    return count


def _count_suspicious(all_objects: List[Dict[str, Any]]) -> int:
    """Считает объекты с тегом ошибки или сомнительной метки."""
    count = 0
    for obj in all_objects:
        tags = obj.get("tags", [])
        if "label_error" in tags or "label_suspicious" in tags:
            count += 1
    return count