# dataforge/src/utility.py

"""Интегральная полезность объекта."""

import numpy as np
from typing import Dict, Optional


WEIGHTS = {
    "entropy": 0.20,
    "novelty": 0.25,
    "deficit": 0.15,
    "label_quality": 0.25,
    "js_controversial": 0.15,  
}


def compute_utility_scores(
    entropy: np.ndarray,
    outlier_scores: np.ndarray,
    deficit_per_obj: np.ndarray,
    label_scores: np.ndarray,
    is_duplicate: Optional[np.ndarray] = None,
    weights: Optional[Dict[str, float]] = None,
    # НОВЫЕ параметры
    js_label_scores: Optional[np.ndarray] = None,
    is_outlier_flag: Optional[np.ndarray] = None,
    is_novelty_flag: Optional[np.ndarray] = None,
) -> np.ndarray:

    if weights is None:
        weights = WEIGHTS

    # Нормализация базовых метрик
    entropy_norm = _minmax_normalize(entropy)

    # Новизна: 1 - outlier_score для обычных объектов, но 0 для выбросов
    novelty_raw = 1.0 - outlier_scores
    if is_outlier_flag is not None:
        # Выбросы не полезны — обнуляем их novelty
        novelty_raw = np.where(is_outlier_flag, 0.0, novelty_raw)
    novelty_norm = _minmax_normalize(novelty_raw)

    deficit_norm = _minmax_normalize(deficit_per_obj)

    # Качество разметки: объединяем Cleanlab + JSD
    if js_label_scores is not None:
        # Комбинированный скор ошибки: чем выше, тем вероятнее проблема с меткой
        # 1 - label_score: Cleanlab (низкий label_score → высокий скор ошибки)
        # js_label_score: JSD (высокий → нетипичен для класса)
        label_error_score = 0.6 * (1.0 - label_scores) + 0.4 * js_label_scores
    else:
        # Fallback: только Cleanlab
        label_error_score = 1.0 - label_scores
    label_norm = _minmax_normalize(label_error_score)

    # Спорные случаи: JSD от эталона класса
    if js_label_scores is not None:
        js_norm = _minmax_normalize(js_label_scores)
    else:
        js_norm = np.zeros_like(entropy_norm)

    # Интегральный скор
    utility = (
        weights["entropy"] * entropy_norm
        + weights["novelty"] * novelty_norm
        + weights["deficit"] * deficit_norm
        + weights["label_quality"] * label_norm
        + weights["js_controversial"] * js_norm
    )

    # Дубликаты бесполезны для дообучения
    if is_duplicate is not None:
        utility = np.where(is_duplicate, utility * 0.1, utility)

    return _minmax_normalize(utility)


def _minmax_normalize(x: np.ndarray) -> np.ndarray:
    """Min-max нормализация в [0, 1]."""
    x_min, x_max = x.min(), x.max()
    if x_max - x_min < 1e-10:
        return np.zeros_like(x)
    return (x - x_min) / (x_max - x_min)