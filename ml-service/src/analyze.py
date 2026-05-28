"""Cleanlab-анализ: ошибки разметки, выбросы, дубликаты + JSD-комбинации."""

import numpy as np
from typing import Dict, Any, Optional
from collections import defaultdict


def compute_label_issues(
    pred_probs: np.ndarray, 
    targets: np.ndarray,
) -> Dict[str, Any]:
    """Находит объекты с вероятными ошибками разметки (с фильтром confusion pairs)."""
    from cleanlab.rank import get_label_quality_scores
    from cleanlab.filter import find_label_issues
    
    label_scores = get_label_quality_scores(
        labels=targets,
        pred_probs=pred_probs,
    )
    
    label_issues_indices = find_label_issues(
        labels=targets,
        pred_probs=pred_probs,
        return_indices_ranked_by="self_confidence",
    )
    
    predicted_labels = np.argmax(pred_probs, axis=1)
    
    suggested_labels = targets.copy()
    if len(label_issues_indices) > 0:
        suggested_labels[label_issues_indices] = predicted_labels[label_issues_indices]
    
    return {
        "label_scores": label_scores,
        "predicted_labels": predicted_labels,
        "suggested_labels": suggested_labels,
    }


def compute_outliers(
    embeddings: np.ndarray,
) -> Dict[str, np.ndarray]:
    from cleanlab.outlier import OutOfDistribution
    
    ood = OutOfDistribution()
    outlier_scores = ood.fit_score(features=embeddings)
    
    return {"outlier_scores": outlier_scores}


def compute_near_duplicates(
    embeddings: np.ndarray,
    threshold: float = 0.999,
) -> Dict[str, Any]:

    from sklearn.metrics.pairwise import cosine_similarity
    
    n = len(embeddings)
    
    # Матрица сходства
    sim_matrix = cosine_similarity(embeddings)
    np.fill_diagonal(sim_matrix, 0.0)
    
    # Находим пары выше порога
    high_i, high_j = np.where(sim_matrix > threshold)
    
    if len(high_i) == 0:
        print(f"  Дубликатов не найдено (порог: {threshold})")
        return {
            "is_duplicate": np.zeros(n, dtype=bool),
            "duplicate_scores": np.ones(n),
            "near_duplicate_sets": [],
        }
    
    # Строим граф связности
    graph = defaultdict(set)
    for a, b in zip(high_i, high_j):
        a, b = int(a), int(b)
        if a != b:
            graph[a].add(b)
            graph[b].add(a)
    
    # Находим связные компоненты
    visited = set()
    groups = []
    for node in graph:
        if node not in visited:
            stack = [node]
            group = []
            while stack:
                cur = stack.pop()
                if cur not in visited:
                    visited.add(cur)
                    group.append(cur)
                    stack.extend(graph[cur] - visited)
            if len(group) >= 2:
                groups.append(sorted(group))
    
    # Формируем результат
    is_duplicate = np.zeros(n, dtype=bool)
    duplicate_scores = np.ones(n)
    
    for group in groups:
        # Первый объект — primary (не дубликат), остальные — дубликаты
        for idx in group[1:]:
            is_duplicate[idx] = True
            # Чем ближе к primary, тем ниже score
            duplicate_scores[idx] = 1.0 - sim_matrix[idx, group[0]]
    
    for i in range(n):
        if not is_duplicate[i]:
            max_sim = sim_matrix[i].max()
            duplicate_scores[i] = 1.0 - max_sim
    
    n_dup = int(is_duplicate.sum())
    print(f"  Найдено дубликатов: {n_dup} из {n} (групп: {len(groups)}, порог: {threshold})")
    
    if len(groups) > 0:
        print(f"  Первые 5 групп:")
        for g in groups[:5]:
            print(f"    {len(g)} объектов: индексы {g}")
    
    return {
        "is_duplicate": is_duplicate,
        "duplicate_scores": duplicate_scores,
        "near_duplicate_sets": groups,
    }


def compute_label_issues_combined(
    pred_probs: np.ndarray,
    targets: np.ndarray,
    n_classes: int,
    js_label_scores: Optional[np.ndarray] = None,
    label_scores: Optional[np.ndarray] = None,
    js_threshold: float = 0.1,
    cleanlab_threshold: float = 0.4,
) -> Dict[str, Any]:
    from .metrics import compute_js_label_score, compute_class_prototypes
    
    # Считаем JSD, если не передали
    if js_label_scores is None:
        class_prototypes = compute_class_prototypes(pred_probs, targets, n_classes)
        _js_scores: np.ndarray = compute_js_label_score(
            pred_probs, targets, n_classes, class_prototypes=class_prototypes
        )
    else:
        _js_scores = js_label_scores
    
    # Считаем label_scores, если не передали
    if label_scores is None:
        label_results = compute_label_issues(pred_probs, targets)
        
        _label_scores: np.ndarray = label_results["label_scores"]
    else:
        _label_scores = label_scores
    
    n = len(pred_probs)
    is_label_error = np.zeros(n, dtype=bool)
    is_controversial = np.zeros(n, dtype=bool)
    
    for i in range(n):
        if _label_scores[i] < cleanlab_threshold:
            is_label_error[i] = True
        elif _js_scores[i] > js_threshold and _label_scores[i] >= cleanlab_threshold:
            is_controversial[i] = True
    
    combined_score = 0.6 * (1.0 - _label_scores) + 0.4 * _js_scores
    
    return {
        "is_label_error": is_label_error,
        "is_controversial": is_controversial,
        "combined_score": combined_score,
        "js_label_scores": _js_scores,
    }


def compute_duplicates_combined(
    embeddings: np.ndarray,
    pred_probs: np.ndarray,
    duplicate_results: Optional[Dict[str, Any]] = None,
    js_threshold: float = 0.01,
) -> Dict[str, Any]:

    from .metrics import js_divergence
    
    if duplicate_results is None:
        duplicate_results = compute_near_duplicates(embeddings)
    
    n = len(embeddings)
    is_duplicate_cleanlab = duplicate_results.get("is_duplicate", np.zeros(n, dtype=bool))
    near_duplicate_sets = duplicate_results.get("near_duplicate_sets", [])
    duplicate_scores = duplicate_results.get("duplicate_scores", np.ones(n))
    
    if near_duplicate_sets is None:
        near_duplicate_sets = []
    if is_duplicate_cleanlab is None:
        is_duplicate_cleanlab = np.zeros(n, dtype=bool)
    if duplicate_scores is None:
        duplicate_scores = np.ones(n)
    
    is_duplicate = np.zeros(n, dtype=bool)
    is_visual_only = np.zeros(n, dtype=bool)
    
    for group in near_duplicate_sets:
        if not isinstance(group, (list, np.ndarray)) or len(group) < 2:
            continue
        group_list = list(group)
        for i in range(len(group_list)):
            for j in range(i + 1, len(group_list)):
                idx_i = int(group_list[i])
                idx_j = int(group_list[j])
                jsd = float(js_divergence(pred_probs[idx_i], pred_probs[idx_j]))
                
                if jsd < js_threshold:
                    is_duplicate[idx_i] = True
                    is_duplicate[idx_j] = True
                else:
                    is_visual_only[idx_i] = True
                    is_visual_only[idx_j] = True
    
    n_cleanlab = int(np.sum(is_duplicate_cleanlab))
    n_confirmed = int(is_duplicate.sum())
    n_rejected = int(is_visual_only.sum())
    
    print(f"  Cosine нашёл: {n_cleanlab}, JSD подтвердил: {n_confirmed}, "
          f"JSD отверг (false positives): {n_rejected}")
    
    return {
        "is_duplicate": is_duplicate,
        "is_visual_only": is_visual_only,
        "duplicate_scores": duplicate_scores,
        "near_duplicate_sets": near_duplicate_sets,
    }


def separate_outlier_novelty(
    outlier_scores: np.ndarray,
    entropy: np.ndarray,
    outlier_threshold: float = 0.3,
    entropy_threshold: float = 0.5,
) -> Dict[str, np.ndarray]:

    n = len(outlier_scores)
    is_outlier = np.zeros(n, dtype=bool)
    is_novelty = np.zeros(n, dtype=bool)
    
    for i in range(n):
        if outlier_scores[i] < outlier_threshold:
            if entropy[i] > entropy_threshold:
                is_outlier[i] = True
            else:
                is_novelty[i] = True
    
    n_outliers = int(is_outlier.sum())
    n_novelties = int(is_novelty.sum())
    print(f"  Выбросов: {n_outliers}, Новых объектов: {n_novelties}")
    
    return {
        "is_outlier": is_outlier,
        "is_novelty": is_novelty,
    }