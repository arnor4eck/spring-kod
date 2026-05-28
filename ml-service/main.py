from typing import List, Optional
from fastapi import FastAPI, File, UploadFile, HTTPException
import logging
import sys
import os

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from src.pipeline import run_full_pipeline

"""
python -m venv venv
venv/Scripts/activate
pip install -r requirements.txt
uvicorn main:app --reload
"""

mock = {
    "summary": {
        "readiness": 65,
        "n_total": 50,
        "n_classes": 5,
        "classes": [
            {
                "class_idx": 0,
                "name": "Tennis",
                "count": 12,
                "percentage": 24.0,
                "deficit": 0.0
            },
            {
                "class_idx": 1,
                "name": "Soccer",
                "count": 14,
                "percentage": 28.0,
                "deficit": 0.0
            },
            {
                "class_idx": 2,
                "name": "Basketball",
                "count": 10,
                "percentage": 20.0,
                "deficit": 0.17
            },
            {
                "class_idx": 3,
                "name": "Swimming",
                "count": 8,
                "percentage": 16.0,
                "deficit": 0.33
            },
            {
                "class_idx": 4,
                "name": "Athletics",
                "count": 6,
                "percentage": 12.0,
                "deficit": 0.50
            }
        ]
    },
    "groups": {
        "all_objects": [
            {"file_name": "761887cb9d.jpg", "tags": ["reliable"], "utility_score": 0.08, "entropy": 0.11, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.18},
            {"file_name": "31c1317a56.jpg", "tags": ["boundary"], "utility_score": 0.85, "entropy": 1.28, "confidence": 0.44, "label_score": 0.58, "outlier_score": 0.59},
            {"file_name": "5b6aef6ae3.jpg", "tags": ["label_error"], "utility_score": 0.92, "entropy": 1.52, "confidence": 0.28, "label_score": 0.12, "outlier_score": 0.81},
            {"file_name": "59e194399b.jpg", "tags": ["reliable"], "utility_score": 0.06, "entropy": 0.09, "confidence": 0.98, "label_score": 0.96, "outlier_score": 0.14},
            {"file_name": "bea88acd1f.jpg", "tags": ["novel"], "utility_score": 0.78, "entropy": 0.92, "confidence": 0.67, "label_score": 0.71, "outlier_score": 0.42},
            {"file_name": "958993cd00.jpg", "tags": ["duplicate"], "utility_score": 0.04, "entropy": 0.06, "confidence": 0.99, "label_score": 0.98, "outlier_score": 0.07},
            {"file_name": "caf50c7d4c.jpg", "tags": ["reliable"], "utility_score": 0.10, "entropy": 0.14, "confidence": 0.95, "label_score": 0.93, "outlier_score": 0.24},
            {"file_name": "9816354fd1.jpg", "tags": ["label_suspicious"], "utility_score": 0.72, "entropy": 1.18, "confidence": 0.53, "label_score": 0.48, "outlier_score": 0.61},
            {"file_name": "888c3243d9.jpg", "tags": ["reliable"], "utility_score": 0.05, "entropy": 0.08, "confidence": 0.98, "label_score": 0.97, "outlier_score": 0.12},
            {"file_name": "26af51ae56.jpg", "tags": ["boundary"], "utility_score": 0.83, "entropy": 1.26, "confidence": 0.46, "label_score": 0.56, "outlier_score": 0.54},
            {"file_name": "c0f3db9927.jpg", "tags": ["reliable"], "utility_score": 0.09, "entropy": 0.13, "confidence": 0.96, "label_score": 0.94, "outlier_score": 0.23},
            {"file_name": "8d36d7904e.jpg", "tags": ["label_error"], "utility_score": 0.94, "entropy": 1.48, "confidence": 0.31, "label_score": 0.14, "outlier_score": 0.79},
            {"file_name": "3c64a8efb3.jpg", "tags": ["reliable"], "utility_score": 0.07, "entropy": 0.10, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.16},
            {"file_name": "fba7d09f1f.jpg", "tags": ["novel"], "utility_score": 0.75, "entropy": 0.88, "confidence": 0.69, "label_score": 0.73, "outlier_score": 0.39},
            {"file_name": "5061ad90ec.jpg", "tags": ["duplicate"], "utility_score": 0.03, "entropy": 0.05, "confidence": 0.99, "label_score": 0.98, "outlier_score": 0.06},
            {"file_name": "ff87e7ffb4.jpg", "tags": ["reliable"], "utility_score": 0.11, "entropy": 0.16, "confidence": 0.94, "label_score": 0.92, "outlier_score": 0.26},
            {"file_name": "e142f3a015.jpg", "tags": ["label_suspicious"], "utility_score": 0.68, "entropy": 1.12, "confidence": 0.56, "label_score": 0.45, "outlier_score": 0.58},
            {"file_name": "371e3e7751.jpg", "tags": ["reliable"], "utility_score": 0.04, "entropy": 0.06, "confidence": 0.99, "label_score": 0.98, "outlier_score": 0.07},
            {"file_name": "bf8a602705.jpg", "tags": ["boundary"], "utility_score": 0.86, "entropy": 1.29, "confidence": 0.43, "label_score": 0.59, "outlier_score": 0.56},
            {"file_name": "4ab1750b1b.jpg", "tags": ["reliable"], "utility_score": 0.09, "entropy": 0.12, "confidence": 0.96, "label_score": 0.94, "outlier_score": 0.21},
            {"file_name": "5ca74b57cd.jpg", "tags": ["label_error"], "utility_score": 0.91, "entropy": 1.49, "confidence": 0.29, "label_score": 0.11, "outlier_score": 0.82},
            {"file_name": "6cb3d4d2be.jpg", "tags": ["reliable"], "utility_score": 0.08, "entropy": 0.11, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.18},
            {"file_name": "37e1c25017.jpg", "tags": ["novel"], "utility_score": 0.76, "entropy": 0.90, "confidence": 0.68, "label_score": 0.72, "outlier_score": 0.41},
            {"file_name": "e798d9f5d2.jpg", "tags": ["reliable"], "utility_score": 0.06, "entropy": 0.09, "confidence": 0.98, "label_score": 0.96, "outlier_score": 0.15},
            {"file_name": "8c0534b686.jpg", "tags": ["duplicate"], "utility_score": 0.05, "entropy": 0.07, "confidence": 0.99, "label_score": 0.98, "outlier_score": 0.09},
            {"file_name": "b6387a7cf0.jpg", "tags": ["reliable"], "utility_score": 0.10, "entropy": 0.15, "confidence": 0.95, "label_score": 0.93, "outlier_score": 0.25},
            {"file_name": "b001b4618c.jpg", "tags": ["label_suspicious"], "utility_score": 0.70, "entropy": 1.15, "confidence": 0.54, "label_score": 0.46, "outlier_score": 0.60},
            {"file_name": "5781559f15.jpg", "tags": ["reliable"], "utility_score": 0.05, "entropy": 0.08, "confidence": 0.98, "label_score": 0.97, "outlier_score": 0.13},
            {"file_name": "0cae8e25db.jpg", "tags": ["boundary"], "utility_score": 0.84, "entropy": 1.27, "confidence": 0.45, "label_score": 0.57, "outlier_score": 0.53},
            {"file_name": "1ddf1aea40.jpg", "tags": ["reliable"], "utility_score": 0.09, "entropy": 0.13, "confidence": 0.96, "label_score": 0.94, "outlier_score": 0.22},
            {"file_name": "0e768a2aac.jpg", "tags": ["label_error"], "utility_score": 0.93, "entropy": 1.50, "confidence": 0.30, "label_score": 0.13, "outlier_score": 0.80},
            {"file_name": "9c00311966.jpg", "tags": ["reliable"], "utility_score": 0.07, "entropy": 0.10, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.17},
            {"file_name": "15a10979b8.jpg", "tags": ["novel"], "utility_score": 0.77, "entropy": 0.91, "confidence": 0.68, "label_score": 0.72, "outlier_score": 0.40},
            {"file_name": "c1036.jpg", "tags": ["duplicate"], "utility_score": 0.04, "entropy": 0.06, "confidence": 0.99, "label_score": 0.98, "outlier_score": 0.08},
            {"file_name": "2365efdc20.jpg", "tags": ["reliable"], "utility_score": 0.10, "entropy": 0.15, "confidence": 0.95, "label_score": 0.93, "outlier_score": 0.24},
            {"file_name": "d30c6d94e2.jpg", "tags": ["label_suspicious"], "utility_score": 0.69, "entropy": 1.14, "confidence": 0.55, "label_score": 0.47, "outlier_score": 0.59},
            {"file_name": "36e898dd84.jpg", "tags": ["reliable"], "utility_score": 0.05, "entropy": 0.08, "confidence": 0.98, "label_score": 0.97, "outlier_score": 0.13},
            {"file_name": "6cd440cbaa.jpg", "tags": ["boundary"], "utility_score": 0.82, "entropy": 1.25, "confidence": 0.47, "label_score": 0.55, "outlier_score": 0.52},
            {"file_name": "8dfe9c12f6.jpg", "tags": ["reliable"], "utility_score": 0.08, "entropy": 0.11, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.19},
            {"file_name": "d680b297c8.jpg", "tags": ["label_error"], "utility_score": 0.95, "entropy": 1.45, "confidence": 0.32, "label_score": 0.15, "outlier_score": 0.78},
            {"file_name": "ff8d3faa32.jpg", "tags": ["reliable"], "utility_score": 0.06, "entropy": 0.09, "confidence": 0.98, "label_score": 0.96, "outlier_score": 0.14},
            {"file_name": "572e9eaeb5.jpg", "tags": ["novel"], "utility_score": 0.74, "entropy": 0.89, "confidence": 0.69, "label_score": 0.73, "outlier_score": 0.38},
            {"file_name": "35d5eb0617.jpg", "tags": ["reliable"], "utility_score": 0.09, "entropy": 0.12, "confidence": 0.96, "label_score": 0.94, "outlier_score": 0.22},
            {"file_name": "71ba081db6.jpg", "tags": ["duplicate"], "utility_score": 0.03, "entropy": 0.05, "confidence": 0.99, "label_score": 0.98, "outlier_score": 0.06},
            {"file_name": "9c01ba7033.jpg", "tags": ["reliable"], "utility_score": 0.11, "entropy": 0.16, "confidence": 0.94, "label_score": 0.92, "outlier_score": 0.27},
            {"file_name": "7261ba3343.jpg", "tags": ["label_suspicious"], "utility_score": 0.71, "entropy": 1.16, "confidence": 0.52, "label_score": 0.47, "outlier_score": 0.61},
            {"file_name": "50964901e6.jpg", "tags": ["reliable"], "utility_score": 0.07, "entropy": 0.10, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.16},
            {"file_name": "9e962ffd6c.jpg", "tags": ["boundary"], "utility_score": 0.87, "entropy": 1.30, "confidence": 0.42, "label_score": 0.60, "outlier_score": 0.58},
            {"file_name": "9de67825c6.jpg", "tags": ["reliable"], "utility_score": 0.06, "entropy": 0.09, "confidence": 0.98, "label_score": 0.96, "outlier_score": 0.15},
            {"file_name": "30073e5955.jpg", "tags": ["reliable"], "utility_score": 0.08, "entropy": 0.11, "confidence": 0.97, "label_score": 0.95, "outlier_score": 0.18}
        ],
        "reliable": [
            {"file_name": "761887cb9d.jpg", "tags": ["reliable"], "utility_score": 0.08},
            {"file_name": "59e194399b.jpg", "tags": ["reliable"], "utility_score": 0.06},
            {"file_name": "caf50c7d4c.jpg", "tags": ["reliable"], "utility_score": 0.10},
            {"file_name": "888c3243d9.jpg", "tags": ["reliable"], "utility_score": 0.05},
            {"file_name": "c0f3db9927.jpg", "tags": ["reliable"], "utility_score": 0.09},
            {"file_name": "3c64a8efb3.jpg", "tags": ["reliable"], "utility_score": 0.07},
            {"file_name": "ff87e7ffb4.jpg", "tags": ["reliable"], "utility_score": 0.11},
            {"file_name": "371e3e7751.jpg", "tags": ["reliable"], "utility_score": 0.04},
            {"file_name": "bf8a602705.jpg", "tags": ["reliable"], "utility_score": 0.09},
            {"file_name": "6cb3d4d2be.jpg", "tags": ["reliable"], "utility_score": 0.08},
            {"file_name": "e798d9f5d2.jpg", "tags": ["reliable"], "utility_score": 0.06},
            {"file_name": "b6387a7cf0.jpg", "tags": ["reliable"], "utility_score": 0.10},
            {"file_name": "5781559f15.jpg", "tags": ["reliable"], "utility_score": 0.05},
            {"file_name": "1ddf1aea40.jpg", "tags": ["reliable"], "utility_score": 0.09},
            {"file_name": "9c00311966.jpg", "tags": ["reliable"], "utility_score": 0.07},
            {"file_name": "2365efdc20.jpg", "tags": ["reliable"], "utility_score": 0.10},
            {"file_name": "36e898dd84.jpg", "tags": ["reliable"], "utility_score": 0.05},
            {"file_name": "8dfe9c12f6.jpg", "tags": ["reliable"], "utility_score": 0.08},
            {"file_name": "ff8d3faa32.jpg", "tags": ["reliable"], "utility_score": 0.06},
            {"file_name": "35d5eb0617.jpg", "tags": ["reliable"], "utility_score": 0.09},
            {"file_name": "9c01ba7033.jpg", "tags": ["reliable"], "utility_score": 0.11},
            {"file_name": "50964901e6.jpg", "tags": ["reliable"], "utility_score": 0.07},
            {"file_name": "9de67825c6.jpg", "tags": ["reliable"], "utility_score": 0.06},
            {"file_name": "30073e5955.jpg", "tags": ["reliable"], "utility_score": 0.08}
        ],
        "label_issues": [
            {"file_name": "5b6aef6ae3.jpg", "old_label": 1, "old_label_name": "Soccer", "suggested_label": 2, "suggested_label_name": "Basketball"},
            {"file_name": "8d36d7904e.jpg", "old_label": 3, "old_label_name": "Swimming", "suggested_label": 4, "suggested_label_name": "Athletics"},
            {"file_name": "5ca74b57cd.jpg", "old_label": 4, "old_label_name": "Athletics", "suggested_label": 3, "suggested_label_name": "Swimming"},
            {"file_name": "0e768a2aac.jpg", "old_label": 2, "old_label_name": "Basketball", "suggested_label": 1, "suggested_label_name": "Soccer"},
            {"file_name": "d680b297c8.jpg", "old_label": 0, "old_label_name": "Tennis", "suggested_label": 1, "suggested_label_name": "Soccer"}
        ],
        "duplicates": [
            {
                "group_id": 1,
                "primary": {"file_name": "958993cd00.jpg"},
                "copies": [{"file_name": "5061ad90ec.jpg"}]
            },
            {
                "group_id": 2,
                "primary": {"file_name": "8c0534b686.jpg"},
                "copies": [{"file_name": "71ba081db6.jpg"}]
            },
            {
                "group_id": 3,
                "primary": {"file_name": "c1036.jpg"},
                "copies": [{"file_name": "958993cd00.jpg"}]
            }
        ],
        "quality_issues": []
    },
    "roadmap": [
        {"id": 1, "action": "Проверить 5 объектов с вероятной ошибкой разметки"},
        {"id": 2, "action": "Удалить 3 дубликата"},
        {"id": 3, "action": "Дособрать 9 примеров класса Athletics"},
        {"id": 4, "action": "Дособрать 7 примеров класса Swimming"},
        {"id": 5, "action": "Проверить 8 пограничных объектов"}
    ]
}

app = FastAPI()
logger = logging.getLogger(__name__)

@app.post("/upload/analitics")
async def upload_multiple_files(
    images: List[UploadFile] = File(...),  # несколько изображений
    markup_file: UploadFile = File(...),   # CSV файл с разметкой
    probability: Optional[UploadFile] = File(None),  # опционально
    metadata: Optional[UploadFile] = File(None)      # опционально
):
    return await run_full_pipeline(images, markup_file, probability)
    # return mock
