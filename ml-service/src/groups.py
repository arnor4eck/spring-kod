# dataforge/src/groups.py

"""Разбиение объектов на 5 групп для интерфейса."""

import numpy as np
from typing import List, Dict, Any, Optional


def split_into_groups(
    tags: List[List[str]],
    utility_scores: np.ndarray,
    label_scores: np.ndarray,
    entropy: np.ndarray,
    confidence: np.ndarray,
    outlier_scores: np.ndarray,
    targets: np.ndarray,
    suggested_labels: np.ndarray,
    predicted_labels: np.ndarray,
    near_duplicate_sets: List[List[int]],
    is_duplicate_flagged: np.ndarray,
    filenames: Optional[List[str]] = None,
    class_names: Optional[List[str]] = None,
    quality_scores: Optional[np.ndarray] = None,
    quality_issues: Optional[np.ndarray] = None,
) -> Dict[str, Any]:
    """Разбивает объекты на 5 групп."""
    n = len(tags)
    indices = np.arange(n)

    if filenames is None:
        filenames = [str(i) for i in range(n)]
    if class_names is None:
        class_names = [str(i) for i in range(len(set(targets)))]

    reliable_mask = _get_reliable_mask(tags, is_duplicate_flagged, quality_issues)
    reliable_indices = indices[reliable_mask]
    reliable_order = reliable_indices[np.argsort(utility_scores[reliable_mask])[::-1]]

    label_issue_mask = _get_label_issue_mask(tags, targets, suggested_labels)
    label_issue_indices = indices[label_issue_mask]
    label_issue_order = label_issue_indices[np.argsort(label_scores[label_issue_mask])]

    duplicate_groups = _build_duplicate_groups(near_duplicate_sets, is_duplicate_flagged, filenames)

    quality_issue_indices = np.array([], dtype=int)
    if quality_issues is not None:
        quality_issue_indices = indices[quality_issues]

    all_objects = _build_all_objects_list(
        indices, tags, utility_scores, entropy, confidence,
        label_scores, outlier_scores, filenames
    )

    return {
        "all_objects": all_objects,
        "reliable": _build_reliable_list(reliable_order, tags, utility_scores, filenames),
        "label_issues": _build_label_issues_list(
            label_issue_order, targets, suggested_labels, filenames, class_names
        ),
        "duplicates": duplicate_groups,
        "quality_issues": _build_quality_issues_list(
            quality_issue_indices, tags, filenames, quality_scores
        ),
    }


def _get_reliable_mask(tags, is_duplicate_flagged, quality_issues):
    """Объекты без критических проблем."""
    n = len(tags)
    mask = np.ones(n, dtype=bool)
    for i in range(n):
        if any(t in tags[i] for t in ["label_error", "label_suspicious", "duplicate"]):
            mask[i] = False
        if is_duplicate_flagged[i]:
            mask[i] = False
        if quality_issues is not None and quality_issues[i]:
            mask[i] = False
    return mask


def _get_label_issue_mask(tags, targets, suggested_labels):
    """Объекты где suggested_label отличается от targets."""
    n = len(tags)
    mask = np.zeros(n, dtype=bool)
    for i in range(n):
        has_tag = "label_error" in tags[i] or "label_suspicious" in tags[i]
        if has_tag and suggested_labels[i] != targets[i]:
            mask[i] = True
    return mask


def _build_all_objects_list(indices, tags, utility_scores, entropy, confidence, label_scores, outlier_scores, filenames):
    """Группа 1: Все объекты с метриками."""
    objects = []
    for i in indices:
        objects.append({
            "file_name": filenames[i],
            "tags": tags[i],
            "utility_score": float(utility_scores[i]),
            "entropy": float(entropy[i]),
            "confidence": float(confidence[i]),
            "label_score": float(label_scores[i]),
            "outlier_score": float(outlier_scores[i]),
        })
    objects.sort(key=lambda x: (
        0 if "label_error" in x["tags"] else
        1 if "label_suspicious" in x["tags"] else 2
    ))
    return objects


def _build_reliable_list(indices, tags, utility_scores, filenames):
    """Группа 2: Надёжные данные."""
    return [
        {"file_name": filenames[i], "tags": tags[i], "utility_score": float(utility_scores[i])}
        for i in indices
    ]


def _build_label_issues_list(indices, targets, suggested_labels, filenames, class_names):
    """Группа 3: Ошибки разметки."""
    objects = []
    for i in indices:
        old_label = int(targets[i])
        suggested_label = int(suggested_labels[i])
        objects.append({
            "file_name": filenames[i],
            "old_label": old_label,
            "old_label_name": class_names[old_label] if old_label < len(class_names) else str(old_label),
            "suggested_label": suggested_label,
            "suggested_label_name": class_names[suggested_label] if suggested_label < len(class_names) else str(suggested_label),
        })
    return objects


def _build_duplicate_groups(near_duplicate_sets, is_duplicate_flagged, filenames):
    """Группа 4: Группы дубликатов."""
    groups = []
    for group_idx, group in enumerate(near_duplicate_sets):
        if len(group) == 0:
            continue
        primary = None
        copies = []
        for idx in group:
            if is_duplicate_flagged[idx]:
                copies.append(idx)
            else:
                primary = idx
        if primary is not None:
            groups.append({
                "group_id": group_idx,
                "primary": {"file_name": filenames[primary]},
                "copies": [{"file_name": filenames[idx]} for idx in copies],
            })
    return groups


def _build_quality_issues_list(indices, tags, filenames, quality_scores):
    """Группа 5: Файлы плохого качества."""
    objects = []
    for i in indices:
        obj = {"file_name": filenames[i], "tags": tags[i]}
        if quality_scores is not None:
            obj["quality_score"] = float(quality_scores[i])
        objects.append(obj)
    return objects