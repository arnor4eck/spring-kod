# dataforge/src/tags.py

"""Присвоение тегов объектам."""

import numpy as np
from typing import List, Optional

THRESHOLDS = {
    "label_error": 0.2,
    "label_suspicious": 0.5,
    "hard_case": 0.8,
    "confident": 0.95,
    "novelty": 0.3,          
    "deficit": 0.3,
    "reliable_conf": 0.9,
    # НОВЫЕ пороги
    "controversial_jsd": 0.1,
    "outlier_entropy": 0.5,
}


def _select_primary_from_duplicate_group(
    group_indices: List[int],
    label_scores: np.ndarray,
    outlier_scores: np.ndarray,
    confidence: np.ndarray,
) -> int:
    """Выбирает главного представителя группы дубликатов."""
    if len(group_indices) == 1:
        return group_indices[0]

    best_score = -1
    best_idx = group_indices[0]

    for idx in group_indices:
        score = label_scores[idx] * 0.5 + outlier_scores[idx] * 0.3 + confidence[idx] * 0.2
        if score > best_score:
            best_score = score
            best_idx = idx

    return best_idx


def compute_tags(
    label_scores: np.ndarray,
    entropy: np.ndarray,
    confidence: np.ndarray,
    outlier_scores: np.ndarray,
    is_duplicate: np.ndarray,
    deficit_per_obj: np.ndarray,
    near_duplicate_sets: List[List[int]],
    # НОВЫЕ параметры
    js_label_scores: Optional[np.ndarray] = None,
    is_outlier_flag: Optional[np.ndarray] = None,
    is_novelty_flag: Optional[np.ndarray] = None,
) -> List[List[str]]:
    """Присваивает теги с учётом приоритетов и взаимоисключений."""
    n = len(label_scores)
    tags = [[] for _ in range(n)]

    primary_indices = set()
    for group in near_duplicate_sets:
        if len(group) > 0:
            primary = _select_primary_from_duplicate_group(
                group, label_scores, outlier_scores, confidence
            )
            primary_indices.add(primary)

    is_duplicate_flagged = is_duplicate.copy()
    for i in primary_indices:
        is_duplicate_flagged[i] = False

    for i in range(n):
        has_label_issue = False
        is_hard = entropy[i] > THRESHOLDS["hard_case"]
        is_confident = confidence[i] > THRESHOLDS["confident"]
        is_novel_candidate = outlier_scores[i] < THRESHOLDS["novelty"]
        is_def_deficit = deficit_per_obj[i] > THRESHOLDS["deficit"]
        is_dup = is_duplicate_flagged[i]

        # --- Ошибки разметки (Cleanlab) ---
        if label_scores[i] < THRESHOLDS["label_error"]:
            tags[i].append("label_error")
            has_label_issue = True
        elif label_scores[i] < THRESHOLDS["label_suspicious"]:
            tags[i].append("label_suspicious")
            has_label_issue = True

        # --- НОВОЕ: Спорный случай (JSD) ---
        if (not has_label_issue and
            js_label_scores is not None and
            js_label_scores[i] > THRESHOLDS["controversial_jsd"]):
            tags[i].append("controversial")

        # --- Дубликат ---
        if is_dup:
            tags[i].append("duplicate")

        # --- Сложный случай ---
        if is_hard:
            tags[i].append("hard_case")

        # --- Дефицитный класс ---
        if is_def_deficit:
            tags[i].append("deficit_class")

        # --- НОВОЕ: Новизна vs Выброс (через флаги из analyze.py) ---
        if is_novel_candidate and not has_label_issue and not is_dup:
            if is_outlier_flag is not None and is_novelty_flag is not None:
                # Используем готовое разделение из separate_outlier_novelty()
                if is_outlier_flag[i]:
                    tags[i].append("outlier")
                elif is_novelty_flag[i]:
                    tags[i].append("novelty")
            else:
                tags[i].append("novelty")

        # --- Уверенное предсказание ---
        if not has_label_issue and not is_hard and is_confident:
            tags[i].append("confident_prediction")

        # --- Надёжный ---
        no_problems = (
            not has_label_issue and not is_dup and not is_hard and
            (js_label_scores is None or js_label_scores[i] <= THRESHOLDS["controversial_jsd"])
        )
        if no_problems and confidence[i] > THRESHOLDS["reliable_conf"]:
            tags[i].append("reliable")

        # --- Если совсем без тегов — присваиваем хотя бы один ---
        if not tags[i]:
            if is_confident:
                tags[i].append("confident_prediction")
            elif is_novel_candidate:
                tags[i].append("novelty")
            else:
                tags[i].append("hard_case")

    return tags