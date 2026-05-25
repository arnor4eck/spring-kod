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
