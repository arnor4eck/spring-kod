# dataforge/src/analyze.py

"""Cleanlab-анализ: ошибки разметки, выбросы, дубликаты."""

import numpy as np
from typing import Dict, Any


def compute_label_issues(
    pred_probs: np.ndarray, 
    targets: np.ndarray,
) -> Dict[str, Any]:
    """Находит объекты с вероятными ошибками разметки."""
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
    """Находит выбросы через OutOfDistribution."""
    from cleanlab.outlier import OutOfDistribution
    
    ood = OutOfDistribution()
    outlier_scores = ood.fit_score(features=embeddings)
    
    return {"outlier_scores": outlier_scores}


def compute_near_duplicates(
    embeddings: np.ndarray,
) -> Dict[str, Any]:
    """Находит дубликаты через Datalab."""
    try:
        import pandas as pd
        from cleanlab import Datalab
    except ImportError:
        print("Datalab не установлен. Установите: pip install 'cleanlab[datalab]'")
        return {
            "is_duplicate": np.zeros(len(embeddings), dtype=bool),
            "duplicate_scores": np.ones(len(embeddings)),
            "near_duplicate_sets": [],
        }
    
    n = len(embeddings)
    df = pd.DataFrame({"id": range(n)})
    lab = Datalab(data=df, label_name=None)
    
    lab.find_issues(features=embeddings, issue_types={"near_duplicate": {}})
    
    try:
        issues = lab.get_issues("near_duplicate")
        
        if issues is not None and len(issues) > 0:
            is_duplicate = np.array(issues["is_near_duplicate_issue"].values, dtype=bool)
            duplicate_scores = np.array(issues["near_duplicate_score"].values)
            
            near_duplicate_sets = lab.get_info("near_duplicate_sets")
            if near_duplicate_sets is None:
                near_duplicate_sets = []
            
            print(f"  Найдено дубликатов: {is_duplicate.sum()} из {n}")
        else:
            print("  Дубликаты не обнаружены")
            is_duplicate = np.zeros(n, dtype=bool)
            duplicate_scores = np.ones(n)
            near_duplicate_sets = []
            
    except Exception as e:
        print(f"  Ошибка при поиске дубликатов: {e}")
        is_duplicate = np.zeros(n, dtype=bool)
        duplicate_scores = np.ones(n)
        near_duplicate_sets = []
    
    return {
        "is_duplicate": is_duplicate,
        "duplicate_scores": duplicate_scores,
        "near_duplicate_sets": near_duplicate_sets,
    }