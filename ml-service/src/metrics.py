"""Метрики: энтропия, уверенность, дефицит, баланс, JSD, KL."""

import numpy as np
from scipy.stats import entropy as scipy_entropy


def compute_entropy(pred_probs: np.ndarray) -> np.ndarray:
    """Энтропия Шеннона для каждого объекта."""
    return np.array(scipy_entropy(pred_probs, base=np.e, axis=1))


def compute_confidence(pred_probs: np.ndarray) -> np.ndarray:
    """Уверенность — максимальная вероятность среди классов."""
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
    """Дефицит класса [0, 1] относительно медианы или максимума."""
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


def _clip_safe(probs: np.ndarray, eps: float = 1e-10) -> np.ndarray:
    return np.clip(probs, eps, 1.0 - eps)


def kl_divergence(p: np.ndarray, q: np.ndarray, eps: float = 1e-10) -> np.ndarray:
    """D_KL(P || Q) = Σ P(i) * log(P(i) / Q(i))."""
    p_safe = _clip_safe(p, eps)
    q_safe = _clip_safe(q, eps)
    return np.sum(p_safe * np.log(p_safe / q_safe), axis=-1)


def js_divergence(p: np.ndarray, q: np.ndarray, eps: float = 1e-10) -> np.ndarray:
    """JSD(P || Q) — симметричная, [0, 1]."""
    p_safe = _clip_safe(p, eps)
    q_safe = _clip_safe(q, eps)
    m = 0.5 * (p_safe + q_safe)
    return 0.5 * kl_divergence(p_safe, m) + 0.5 * kl_divergence(q_safe, m)


def compute_class_prototypes(
    pred_probs: np.ndarray, targets: np.ndarray, n_classes: int,
    confident_only: bool = True, confidence_threshold: float = 0.5
) -> np.ndarray:
    """Эталонные распределения pred_probs для каждого класса [n_classes, K]."""
    prototypes = np.zeros((n_classes, pred_probs.shape[1]))
    for k in range(n_classes):
        mask = targets == k
        if confident_only:
            pred_labels = np.argmax(pred_probs, axis=1)
            conf = np.max(pred_probs, axis=1)
            mask = mask & (pred_labels == k) & (conf > confidence_threshold)
        prototypes[k] = pred_probs[mask].mean(axis=0) if mask.sum() > 0 else pred_probs[targets == k].mean(axis=0)
    return prototypes


def compute_js_label_score(
    pred_probs: np.ndarray, targets: np.ndarray, n_classes: int,
    class_prototypes: np.ndarray | None = None, confident_only: bool = True
) -> np.ndarray:
    """JSD от эталона класса для каждого объекта [N]. Высокий JSD → спорный случай."""
    if class_prototypes is None:
        class_prototypes = compute_class_prototypes(pred_probs, targets, n_classes, confident_only)
    scores = np.zeros(len(pred_probs))
    for k in range(n_classes):
        mask = targets == k
        if mask.sum() == 0:
            continue
        prototype = class_prototypes[k]
        for idx in np.where(mask)[0]:
            scores[idx] = js_divergence(pred_probs[idx], prototype)
    return scores


def compute_balance_coefficient(targets: np.ndarray, n_classes: int) -> float:
    """Коэффициент баланса [0, 1] через KL. 1 = идеальный баланс."""
    counts = compute_class_distribution(targets, n_classes)
    total = counts.sum()
    if total == 0:
        return 0.0
    p_emp = counts / total
    p_uniform = np.ones(n_classes) / n_classes
    kl = np.sum(p_emp * np.log(p_emp / p_uniform))
    kl_max = np.log(n_classes)
    return 1.0 - kl / kl_max if kl_max > 0 else 1.0


def compute_class_kl_contributions(targets: np.ndarray, n_classes: int) -> np.ndarray:
    """Вклад каждого класса в KL-дисбаланс. + перепредставлен, − недопредставлен."""
    counts = compute_class_distribution(targets, n_classes)
    total = counts.sum()
    if total == 0:
        return np.zeros(n_classes)
    p_emp = counts / total
    return p_emp * np.log(p_emp * n_classes)


def compute_deficit_from_kl(targets: np.ndarray, n_classes: int) -> np.ndarray:
    """Дефицит через KL-вклад [0, 1]. 0 = норма, 1 = класса нет."""
    counts = np.bincount(targets, minlength=n_classes)
    total = counts.sum()
    target_count = total / n_classes
    if target_count == 0:
        return np.zeros(n_classes)
    return np.maximum(0.0, 1.0 - counts / target_count)