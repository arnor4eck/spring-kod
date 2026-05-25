# dataforge/src/tags.py

"""Присвоение тегов объектам."""

import numpy as np
from typing import List

THRESHOLDS = {
    "label_error": 0.2,
    "label_suspicious": 0.5,
    "hard_case": 0.8,
    "confident": 0.95,
    "novelty": 0.3,
    "deficit": 0.3,
    "reliable_conf": 0.9,
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
        is_novel = outlier_scores[i] < THRESHOLDS["novelty"]
        is_def_deficit = deficit_per_obj[i] > THRESHOLDS["deficit"]
        is_dup = is_duplicate_flagged[i]

        if label_scores[i] < THRESHOLDS["label_error"]:
            tags[i].append("label_error")
            has_label_issue = True
        elif label_scores[i] < THRESHOLDS["label_suspicious"]:
            tags[i].append("label_suspicious")
            has_label_issue = True

        if is_dup:
            tags[i].append("duplicate")

        if is_hard:
            tags[i].append("hard_case")

        if is_def_deficit:
            tags[i].append("deficit_class")

        if not has_label_issue and not is_dup and is_novel:
            tags[i].append("novelty")

        if not has_label_issue and not is_hard and is_confident:
            tags[i].append("confident_prediction")

        no_problems = not has_label_issue and not is_dup and not is_hard
        if no_problems and confidence[i] > THRESHOLDS["reliable_conf"]:
            tags[i].append("reliable")

        if not tags[i]:
            if is_confident:
                tags[i].append("confident_prediction")
            elif is_novel:
                tags[i].append("novelty")
            else:
                tags[i].append("hard_case")

    return tags