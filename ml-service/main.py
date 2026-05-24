from typing import List, Optional
from fastapi import FastAPI, File, UploadFile, HTTPException
import logging

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
    """
    Загрузка нескольких файлов разных типов
    """
    results = []

    # 1. Обрабатываем изображения
    for image in images:
        result = await process_file(image, "image")
        results.append(result)

    markup_result = await process_file(markup_file, "markup")
    results.append(markup_result)

    # 3. Опционально: probability файл
    if probability:
        prob_result = await process_file(probability, "probability")
        results.append(prob_result)

    # 4. Опционально: metadata файл
    if metadata:
        meta_result = await process_file(metadata, "metadata")
        results.append(meta_result)

    return {
        "files": results,
        "count": len(results),
        "images_count": len(images),
        "has_markup": markup_file is not None
    }


async def process_file(file: UploadFile, file_type: str) -> dict:
    try:
        content = await file.read()

        return {
            "filename": file.filename,
            "file_type": file_type,
            "size": len(content),
            "content_type": file.content_type,
            "status": "success"
        }
    except Exception as e:
        logger.error(f"Error processing file {file.filename}: {e}")
        return {
            "filename": file.filename,
            "file_type": file_type,
            "error": str(e),
            "status": "failed"
        }