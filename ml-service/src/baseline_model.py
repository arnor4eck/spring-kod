# dataforge/src/baseline_model.py

"""Baseline-модель: EfficientNet-B0, эмбеддинги, вероятности (k-NN + центроиды)."""

import numpy as np
from typing import Tuple, Optional
import torch
import torchvision.models as models
from torch.utils.data import DataLoader, Dataset
from tqdm import tqdm

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
BATCH_SIZE = 64


def get_model():
    """Загружает предобученную EfficientNet-B0."""
    print(f"Загружаем EfficientNet-B0 (устройство: {DEVICE})...")
    model = models.efficientnet_b0(weights=models.EfficientNet_B0_Weights.IMAGENET1K_V1)
    model.to(DEVICE)
    model.eval()
    return model


def get_transforms():
    """Возвращает transforms для EfficientNet-B0."""
    return models.EfficientNet_B0_Weights.IMAGENET1K_V1.transforms()


class TransformedDataset(Dataset):
    """Обёртка для применения transforms."""
    def __init__(self, subset, transform):
        self.subset = subset
        self.transform = transform

    def __getitem__(self, idx):
        img, label = self.subset[idx]
        return self.transform(img), label

    def __len__(self):
        return len(self.subset)


@torch.no_grad()
def get_embeddings(model, dataset, transforms):
    """Извлекает эмбеддинги (выход avgpool) для всего датасета."""
    transformed_dataset = TransformedDataset(dataset, transforms)
    loader = DataLoader(transformed_dataset, batch_size=BATCH_SIZE, shuffle=False)

    embeddings_list = []

    def hook_fn(module, input, output):
        emb = output.squeeze(-1).squeeze(-1).cpu().numpy()
        embeddings_list.append(emb)

    handle = model.avgpool.register_forward_hook(hook_fn)

    print("Извлекаем эмбеддинги...")
    for images, _ in tqdm(loader, desc="  Прогресс"):
        images = images.to(DEVICE)
        model(images)

    handle.remove()
    embeddings = np.vstack(embeddings_list)
    print(f"  Размер матрицы эмбеддингов: {embeddings.shape}")
    return embeddings


def find_optimal_temperature_knn(
    embeddings: np.ndarray,
    targets: np.ndarray,
    n_neighbors: int = 10,
    verbose: bool = True,
) -> float:
    """
    Подбирает оптимальную температуру для k-NN.
    Критерий: максимизация accuracy при сохранении средней уверенности в диапазоне [0.7, 0.95].
    Слишком низкая температура → сверх-уверенность → много ложных ошибок Cleanlab.
    Слишком высокая → низкая точность.
    """
    temperatures = [0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.7, 1.0, 1.5, 2.0]
    
    if verbose:
        print("\nПодбор температуры для k-NN...")
        print(f"  {'Temp':<10} {'Accuracy':<10} {'Avg Conf':<10} {'Score':<10}")
        print(f"  {'-'*40}")
    
    best_temp, best_score = 0.2, -1
    
    for temp in temperatures:
        probs, _ = get_pred_probs_knn(
            embeddings, targets,
            n_neighbors=n_neighbors, temperature=temp,
            verbose=False  # тихо внутри
        )
        preds = probs.argmax(axis=1)
        acc = (preds == targets).mean()
        conf = probs.max(axis=1).mean()
        
        # Штраф за слишком высокую уверенность (>0.95) — Cleanlab флагует ложные ошибки
        overconfidence_penalty = max(0, conf - 0.95) * 2
        # Штраф за слишком низкую уверенность (<0.7) — модель не уверена
        underconfidence_penalty = max(0, 0.7 - conf)
        
        score = acc - overconfidence_penalty - underconfidence_penalty
        
        if verbose:
            print(f"  {temp:<10.3f} {acc:<10.4f} {conf:<10.4f} {score:<10.4f}")
        
        if score > best_score:
            best_score, best_temp = score, temp
    
    if verbose:
        print(f"\n  Оптимальная температура: {best_temp:.3f} (score: {best_score:.4f})")
    
    return best_temp
# =============================================================================
# k-NN с out-of-sample (ОСНОВНОЙ МЕТОД)
# =============================================================================

def get_pred_probs_knn(
    embeddings: np.ndarray,
    targets: np.ndarray,
    n_neighbors: int = 10,
    temperature: float = 0.05,
    verbose: bool = True,
) -> Tuple[np.ndarray, float]:
    """
    Вычисляет pred_probs через k-NN с кросс-валидацией (out-of-sample).
    
    - Для каждого объекта ищет k ближайших соседей, исключая сам объект.
    - Считает вероятности классов по голосованию соседей.
    - Применяет temperature scaling.
    
    Это out-of-sample предсказания — корректно для Cleanlab Confident Learning.
    """
    from sklearn.neighbors import NearestNeighbors

    n_samples = len(embeddings)
    n_classes = len(np.unique(targets))

    # One-hot метки соседей для быстрого суммирования
    labels_onehot = np.zeros((n_samples, n_classes))
    labels_onehot[np.arange(n_samples), targets] = 1

    # Ищем k+1 соседей (k реальных + возможно сам объект)
    nn = NearestNeighbors(n_neighbors=n_neighbors + 1, metric='cosine')
    nn.fit(embeddings)
    distances, indices = nn.kneighbors(embeddings)

    # Исключаем сам объект из соседей
    neighbor_indices = np.zeros((n_samples, n_neighbors), dtype=int)
    for i in range(n_samples):
        mask = indices[i] != i
        neighbors = indices[i][mask][:n_neighbors]
        # Если после исключения себя не хватает соседей — добираем
        if len(neighbors) < n_neighbors:
            all_others = indices[i][indices[i] != i]
            neighbors = all_others[:n_neighbors]
        neighbor_indices[i] = neighbors

    # Суммируем one-hot метки соседей → распределение голосов
    neighbor_labels = labels_onehot[neighbor_indices]  # [N, k, K]
    votes = neighbor_labels.sum(axis=1)  # [N, K]

    # Temperature scaling
    votes_scaled = votes / max(temperature, 1e-6)
    votes_stable = votes_scaled - votes_scaled.max(axis=1, keepdims=True)
    exp_votes = np.exp(votes_stable)
    pred_probs = exp_votes / exp_votes.sum(axis=1, keepdims=True)

    if verbose:
        preds = pred_probs.argmax(axis=1)
        acc = (preds == targets).mean()
        conf = pred_probs.max(axis=1).mean()
        print(f"  k-NN (k={n_neighbors}, temp={temperature}): accuracy={acc:.2%}, confidence={conf:.4f}")

    return pred_probs, temperature


# =============================================================================
# Центроидный метод (FALLBACK)
# =============================================================================

def _compute_centroids(embeddings, targets, n_classes):
    """Вычисляет центроиды (средние эмбеддинги) для каждого класса."""
    centroids = []
    for class_idx in range(n_classes):
        class_embeddings = embeddings[targets == class_idx]
        if len(class_embeddings) == 0:
            centroids.append(np.zeros(embeddings.shape[1]))
        else:
            centroids.append(class_embeddings.mean(axis=0))
    return np.array(centroids)


def _cosine_similarity(embeddings, centroids):
    """Вычисляет cosine similarity."""
    emb_norm = embeddings / (np.linalg.norm(embeddings, axis=1, keepdims=True) + 1e-10)
    cen_norm = centroids / (np.linalg.norm(centroids, axis=1, keepdims=True) + 1e-10)
    return emb_norm @ cen_norm.T


def _similarities_to_probs(similarities, temperature):
    """Softmax с температурой."""
    scaled = similarities / max(temperature, 1e-6)
    scaled_stable = scaled - scaled.max(axis=1, keepdims=True)
    exp_scaled = np.exp(scaled_stable)
    return exp_scaled / exp_scaled.sum(axis=1, keepdims=True)


def _evaluate_temperature(embeddings, centroids, targets, temperature):
    """Оценивает качество температуры: accuracy, confidence, ECE."""
    similarities = _cosine_similarity(embeddings, centroids)
    probs = _similarities_to_probs(similarities, temperature)
    preds = probs.argmax(axis=1)
    confs = probs.max(axis=1)

    accuracy = (preds == targets).mean()
    avg_confidence = confs.mean()

    n_bins = 10
    bin_boundaries = np.linspace(0, 1, n_bins + 1)
    ece = 0.0

    for i in range(n_bins):
        in_bin = (confs > bin_boundaries[i]) & (confs <= bin_boundaries[i + 1])
        if in_bin.sum() > 0:
            bin_accuracy = (preds[in_bin] == targets[in_bin]).mean()
            bin_confidence = confs[in_bin].mean()
            ece += (in_bin.sum() / len(targets)) * abs(bin_accuracy - bin_confidence)

    confidence_bonus = min(avg_confidence / 0.85, 1.0)
    calibration_penalty = max(0, 0.25 - ece) / 0.25
    score = accuracy * 0.4 + confidence_bonus * 0.3 + calibration_penalty * 0.3

    return score, accuracy, avg_confidence, ece


def find_optimal_temperature(embeddings, targets, verbose=True):
    """Подбирает оптимальную температуру перебором (для центроидного метода)."""
    n_classes = len(np.unique(targets))
    centroids = _compute_centroids(embeddings, targets, n_classes)
    temperatures = [0.01, 0.02, 0.03, 0.05, 0.07, 0.10, 0.15, 0.20, 0.30, 0.50]

    if verbose:
        print("\nПодбор температуры (центроиды)...")
        print(f"  {'Temp':<10} {'Accuracy':<10} {'Avg Conf':<10} {'ECE':<10} {'Score':<10}")
        print(f"  {'-'*50}")

    best_temp, best_score = 0.05, -1

    for temp in temperatures:
        score, acc, conf, ece = _evaluate_temperature(embeddings, centroids, targets, temp)
        if verbose:
            print(f"  {temp:<10.3f} {acc:<10.4f} {conf:<10.4f} {ece:<10.4f} {score:<10.4f}")
        if score > best_score:
            best_score, best_temp = score, temp

    if verbose:
        similarities = _cosine_similarity(embeddings, centroids)
        final_probs = _similarities_to_probs(similarities, best_temp)
        final_acc = (final_probs.argmax(axis=1) == targets).mean()
        final_conf = final_probs.max(axis=1).mean()
        print(f"\n  Оптимальная температура: {best_temp:.3f} (score: {best_score:.4f})")
        print(f"  Итоговая точность: {final_acc:.2%}")
        print(f"  Итоговая уверенность: {final_conf:.4f}")

    return best_temp


def get_pred_probs_centroid(embeddings, targets, temperature=None):
    """Вычисляет вероятности через центроиды (старый метод, fallback)."""
    n_classes = len(np.unique(targets))

    if temperature is None:
        temperature = find_optimal_temperature(embeddings, targets, verbose=True)

    centroids = _compute_centroids(embeddings, targets, n_classes)
    similarities = _cosine_similarity(embeddings, centroids)
    pred_probs = _similarities_to_probs(similarities, temperature)

    return pred_probs, temperature


# dataforge/src/baseline_model.py — добавьте эти функции

def get_pred_probs_logreg(
    embeddings: np.ndarray,
    targets: np.ndarray,
    temperature: Optional[float] = None,
    verbose: bool = True,
) -> Tuple[np.ndarray, float]:
    """
    Вычисляет pred_probs через многоклассовую логистическую регрессию
    с out-of-sample предсказаниями (5-fold CV) и temperature scaling.
    """
    from sklearn.model_selection import cross_val_predict
    from sklearn.linear_model import LogisticRegression
    from sklearn.preprocessing import StandardScaler
    
    n_samples = len(embeddings)
    
    # Стандартизация
    scaler = StandardScaler()
    embeddings_scaled = scaler.fit_transform(embeddings)
    
    # Логистическая регрессия — убраны лишние параметры
    clf = LogisticRegression(
        solver='lbfgs',
        max_iter=1000,
        C=1.0,
    )
    
    if verbose:
        print("  Обучение логистической регрессии (5-fold CV, out-of-sample)...")
    
    # Out-of-sample вероятности через кросс-валидацию
    # method='predict_proba' вместо 'decision_function' — работает во всех версиях
    pred_probs_cv = cross_val_predict(
        clf, embeddings_scaled, targets,
        cv=5, method='predict_proba'
    )
    
    # Подбор температуры
    if temperature is None:
        temperature = find_optimal_temperature_logreg(pred_probs_cv, targets, verbose=verbose)
    
    # Temperature scaling к вероятностям
    # Работаем с логами вероятностей для стабильности
    probs_clipped = np.clip(pred_probs_cv, 1e-10, 1.0)
    log_probs = np.log(probs_clipped)
    log_probs_scaled = log_probs / max(temperature, 1e-6)
    log_probs_stable = log_probs_scaled - log_probs_scaled.max(axis=1, keepdims=True)
    exp_log_probs = np.exp(log_probs_stable)
    pred_probs = exp_log_probs / exp_log_probs.sum(axis=1, keepdims=True)
    
    if verbose:
        preds = pred_probs.argmax(axis=1)
        acc = (preds == targets).mean()
        conf = pred_probs.max(axis=1).mean()
        print(f"  LogReg (temp={temperature:.3f}): accuracy={acc:.2%}, confidence={conf:.4f}")
    
    return pred_probs, temperature


def find_optimal_temperature_logreg(
    logits: np.ndarray,
    targets: np.ndarray,
    verbose: bool = True,
) -> float:
    """
    Подбирает оптимальную температуру для логитов логистической регрессии.
    Критерий: максимизация accuracy с контролем уверенности.
    """
    temperatures = [0.1, 0.2, 0.3, 0.5, 0.7, 1.0, 1.5, 2.0, 3.0, 5.0]
    
    if verbose:
        print("\n  Подбор температуры для LogReg...")
        print(f"  {'Temp':<10} {'Accuracy':<10} {'Avg Conf':<10} {'Score':<10}")
        print(f"  {'-'*40}")
    
    best_temp, best_score = 1.0, -1
    
    for temp in temperatures:
        logits_scaled = logits / max(temp, 1e-6)
        logits_stable = logits_scaled - logits_scaled.max(axis=1, keepdims=True)
        exp_logits = np.exp(logits_stable)
        probs = exp_logits / exp_logits.sum(axis=1, keepdims=True)
        
        preds = probs.argmax(axis=1)
        acc = (preds == targets).mean()
        conf = probs.max(axis=1).mean()
        
        # Штраф за сверхуверенность (>0.9 — Cleanlab флагует ложные ошибки)
        overconfidence_penalty = max(0, conf - 0.9) * 2
        # Штраф за недоуверенность (<0.6 — модель слишком неуверена)
        underconfidence_penalty = max(0, 0.6 - conf)
        
        score = acc - overconfidence_penalty - underconfidence_penalty
        
        if verbose:
            print(f"  {temp:<10.3f} {acc:<10.4f} {conf:<10.4f} {score:<10.4f}")
        
        if score > best_score:
            best_score, best_temp = score, temp
    
    if verbose:
        print(f"\n  Оптимальная температура: {best_temp:.3f} (score: {best_score:.4f})")
    
    return best_temp

# =============================================================================
# Главная функция
# =============================================================================

def get_pred_probs(
    embeddings: np.ndarray,
    targets: np.ndarray,
    temperature: Optional[float] = None,
    method: str = 'logreg',  # ← ИЗМЕНИЛИ ДЕФОЛТ
    n_neighbors: int = 10,
) -> Tuple[np.ndarray, float]:
    """
    Вычисляет pred_probs для датасета.
    
    Args:
        embeddings: [N, D] матрица эмбеддингов
        targets: [N] метки классов
        temperature: температура (авто при None)
        method: 'logreg' (рекомендуется), 'knn', 'centroid'
        n_neighbors: число соседей для k-NN
    """
    if method == 'logreg':
        return get_pred_probs_logreg(embeddings, targets, temperature, verbose=True)
    elif method == 'knn':
        if temperature is None:
            temperature = find_optimal_temperature_knn(embeddings, targets, n_neighbors, verbose=True)
        return get_pred_probs_knn(embeddings, targets, n_neighbors, temperature, verbose=True)
    else:
        return get_pred_probs_centroid(embeddings, targets, temperature)


def compute_baseline(dataset, targets, temperature=None, method='knn'):
    """Загружает модель, извлекает эмбеддинги и вероятности."""
    model = get_model()
    transforms = get_transforms()
    embeddings = get_embeddings(model, dataset, transforms)
    pred_probs, temperature = get_pred_probs(embeddings, targets, temperature, method)
    
    return embeddings, pred_probs, temperature