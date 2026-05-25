# dataforge/src/baseline_model.py

"""Baseline-модель: EfficientNet-B0, эмбеддинги, вероятности, автоподбор температуры."""

import numpy as np
from typing import Tuple, Optional
import torch
import torchvision.models as models
from torch.utils.data import DataLoader, Dataset
from tqdm import tqdm
from scipy.stats import entropy as scipy_entropy

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
    scaled = similarities / temperature
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
    """Подбирает оптимальную температуру перебором."""
    n_classes = len(np.unique(targets))
    centroids = _compute_centroids(embeddings, targets, n_classes)
    temperatures = [0.01, 0.02, 0.03, 0.05, 0.07, 0.10, 0.15, 0.20, 0.30, 0.50]

    if verbose:
        print("\nПодбор температуры...")
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


def get_pred_probs(embeddings, targets, temperature=None):
    """Вычисляет вероятности через центроиды. Если температура не указана — авто."""
    n_classes = len(np.unique(targets))

    if temperature is None:
        temperature = find_optimal_temperature(embeddings, targets, verbose=True)

    centroids = _compute_centroids(embeddings, targets, n_classes)
    similarities = _cosine_similarity(embeddings, centroids)
    pred_probs = _similarities_to_probs(similarities, temperature)

    return pred_probs, temperature


def compute_baseline(dataset, targets, temperature=None):
    """Загружает модель, извлекает эмбеддинги и вероятности."""
    model = get_model()
    transforms = get_transforms()
    embeddings = get_embeddings(model, dataset, transforms)
    pred_probs, temperature = get_pred_probs(embeddings, targets, temperature)
    return embeddings, pred_probs, temperature