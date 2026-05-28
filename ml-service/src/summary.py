# dataforge/src/summary.py

"""Общая статистика датасета."""

import numpy as np
from typing import Dict, Any, List, Optional


def generate_summary(
    targets: np.ndarray,
    n_classes: int,
    class_names: Optional[List[str]] = None,
    imbalance_index: float = 0.0,
    n_label_errors: int = 0,
    n_duplicates: int = 0,
    n_quality_issues: int = 0,
    balance_coefficient: Optional[float] = None,
    n_controversial: int = 0,
    n_outliers: int = 0,
) -> Dict[str, Any]:
    """Генерирует общую статистику датасета."""
    if class_names is None:
        class_names = [str(i) for i in range(n_classes)]

    n_total = len(targets)
    class_counts = np.bincount(targets, minlength=n_classes)
    median_count = np.median(class_counts[class_counts > 0]) if np.any(class_counts > 0) else 0

    bal = balance_coefficient if balance_coefficient is not None else (1.0 - imbalance_index)

    readiness = _calculate_readiness(
        n_total=n_total,
        n_label_errors=n_label_errors,
        n_duplicates=n_duplicates,
        n_quality_issues=n_quality_issues + n_controversial + n_outliers,
        balance_coefficient=bal,
    )

    classes_info = []
    for i in range(n_classes):
        count = int(class_counts[i])
        pct = count / n_total * 100 if n_total > 0 else 0
        # Старый метод: дефицит через медиану
        deficit = max(0.0, (median_count - count) / median_count) if median_count > 0 else 0.0
        classes_info.append({
            "class_idx": i,
            "name": class_names[i],
            "count": count,
            "percentage": round(pct, 1),
            "deficit": round(deficit, 4),
        })

    return {
        "readiness": readiness,
        "n_total": n_total,
        "n_classes": n_classes,
        "classes": classes_info,
    }


def _calculate_readiness(
    n_total: int,
    n_label_errors: int,
    n_duplicates: int,
    n_quality_issues: int,
    balance_coefficient: float,
) -> int:
    """Готовность датасета в процентах [0, 100]."""
    if n_total == 0:
        return 0
    score = 100.0
    score -= min(n_label_errors / n_total * 100, 30)
    score -= min(n_duplicates / n_total * 100, 20)
    score -= (1.0 - balance_coefficient) * 20  # дисбаланс
    score -= min(n_quality_issues / n_total * 100, 30)
    return max(0, min(100, int(score)))