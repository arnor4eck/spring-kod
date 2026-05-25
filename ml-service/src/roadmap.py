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
    if class_names is None:
        class_names = [str(i) for i in range(n_classes)]

    actions = []
    n_label_errors = len(groups["label_issues"])
    n_duplicates = sum(len(g["copies"]) for g in groups["duplicates"])
    n_quality_issues = len(groups["quality_issues"])

    class_counts = np.bincount(targets, minlength=n_classes)
    median_count = np.median(class_counts[class_counts > 0]) if np.any(class_counts > 0) else 0

    if n_label_errors > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Check {n_label_errors} objects with high probability of label errors",
        })

    for i in range(n_classes):
        count = class_counts[i]
        if median_count > 0 and count < median_count:
            deficit = (median_count - count) / median_count
            needed = int(median_count - count)
            if needed >= 3 or deficit > 0.1:
                actions.append({
                    "id": len(actions) + 1,
                    "action": f"Add {needed} examples of class '{class_names[i]}' (deficit: {deficit:.0%})",
                })

    if n_duplicates > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Remove {n_duplicates} duplicates",
        })

    n_suspicious = _count_suspicious(groups["all_objects"])
    if n_suspicious > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Check {n_suspicious} objects with suspicious labels",
        })

    if n_quality_issues > 0:
        actions.append({
            "id": len(actions) + 1,
            "action": f"Check {n_quality_issues} low quality objects",
        })

    return actions


def _count_suspicious(all_objects: List[Dict[str, Any]]) -> int:
    """Считает объекты с тегом ошибки или сомнительной метки."""
    count = 0
    for obj in all_objects:
        tags = obj.get("tags", [])
        if "label_error" in tags or "label_suspicious" in tags:
            count += 1
    return count