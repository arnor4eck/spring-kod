# dataforge/src/utility.py

"""Интегральная полезность объекта."""

import numpy as np
from typing import Dict, Optional

WEIGHTS = {
    "entropy": 0.35,
    "novelty": 0.30,
    "deficit": 0.20,
    "label_quality": 0.15,
}


def compute_utility_scores(
    entropy: np.ndarray,
    outlier_scores: np.ndarray,
    deficit_per_obj: np.ndarray,
    label_scores: np.ndarray,
    is_duplicate: Optional[np.ndarray] = None,
    weights: Optional[Dict[str, float]] = None,
) -> np.ndarray:
    """Интегральный скор полезности [0, 1]."""
    if weights is None:
        weights = WEIGHTS

    entropy_norm = _minmax_normalize(entropy)
    novelty_norm = _minmax_normalize(1.0 - outlier_scores)
    deficit_norm = _minmax_normalize(deficit_per_obj)
    label_norm = _minmax_normalize(label_scores)

    utility = (
        weights["entropy"] * entropy_norm
        + weights["novelty"] * novelty_norm
        + weights["deficit"] * deficit_norm
        + weights["label_quality"] * label_norm
    )

    if is_duplicate is not None:
        utility = np.where(is_duplicate, utility * 0.1, utility)

    return _minmax_normalize(utility)


def _minmax_normalize(x: np.ndarray) -> np.ndarray:
    """Min-max нормализация в [0, 1]."""
    x_min, x_max = x.min(), x.max()
    if x_max - x_min < 1e-10:
        return np.zeros_like(x)
    return (x - x_min) / (x_max - x_min)