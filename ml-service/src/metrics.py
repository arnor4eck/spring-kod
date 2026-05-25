# dataforge/src/metrics.py

"""Метрики: энтропия, уверенность, дефицит класса, индекс дисбаланса."""

import numpy as np
from scipy.stats import entropy as scipy_entropy


def compute_entropy(pred_probs: np.ndarray) -> np.ndarray:
    """Энтропия Шеннона для каждого объекта."""
    return np.array(scipy_entropy(pred_probs, base=np.e, axis=1))


def compute_confidence(pred_probs: np.ndarray) -> np.ndarray:
    """Уверенность модели — максимальная вероятность среди классов."""
    return np.max(pred_probs, axis=1)


def compute_predicted_labels(pred_probs: np.ndarray) -> np.ndarray:
    """Предсказанный класс для каждого объекта."""
    return np.argmax(pred_probs, axis=1)


def compute_mismatch(targets: np.ndarray, pred_probs: np.ndarray) -> np.ndarray:
    """Флаг расхождения: предсказание != истинная метка."""
    return compute_predicted_labels(pred_probs) != targets


def compute_class_distribution(targets: np.ndarray, n_classes: int) -> np.ndarray:
    """Количество объектов в каждом классе."""
    return np.bincount(targets, minlength=n_classes)


def compute_class_deficit(targets: np.ndarray, n_classes: int, target_mode: str = "median") -> np.ndarray:
    """Показатель дефицита для каждого класса [0, 1]."""
    counts = compute_class_distribution(targets, n_classes)
    N_target = np.median(counts) if target_mode == "median" else np.max(counts)
    if N_target == 0:
        return np.zeros(n_classes)
    return np.maximum(0.0, (N_target - counts) / N_target)


def compute_deficit_per_object(targets: np.ndarray, n_classes: int, target_mode: str = "median") -> np.ndarray:
    """Дефицит класса для каждого объекта."""
    deficit = compute_class_deficit(targets, n_classes, target_mode)
    return deficit[targets]


def compute_imbalance_index(targets: np.ndarray, n_classes: int) -> float:
    """Индекс дисбаланса датасета [0, 1]."""
    counts = compute_class_distribution(targets, n_classes)
    p = counts / counts.sum()
    H = scipy_entropy(p, base=np.e)
    H_max = np.log(n_classes)
    if H_max == 0:
        return 0.0
    return 1.0 - (H / H_max)