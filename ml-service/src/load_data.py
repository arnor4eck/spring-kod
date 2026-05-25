# dataforge/src/load_data.py

"""Загрузка датасета из изображений и CSV."""

import os
import io
import numpy as np
import pandas as pd
from PIL import Image
from typing import Tuple, List
from torch.utils.data import Dataset
from fastapi import UploadFile


class DatasetFromImages(Dataset):
    """Датасет из списка PIL.Image и меток."""
    
    def __init__(
        self,
        images: List[Image.Image],
        filenames: List[str],
        labels: np.ndarray,
        class_names: List[str],
        transform=None,
    ):
        self.images = images
        self.filenames = filenames
        self.labels = labels
        self.class_names = class_names
        self.transform = transform
        self.filename_to_idx = {fname: i for i, fname in enumerate(filenames)}
    
    def __getitem__(self, idx):
        img = self.images[idx].copy()
        label = int(self.labels[idx])
        if self.transform:
            img = self.transform(img)
        return img, label
    
    def __len__(self):
        return len(self.images)
    
    def get_targets(self) -> np.ndarray:
        return np.array([int(l) for l in self.labels])
    
    def get_class_names(self) -> List[str]:
        return self.class_names
    
    def get_filename(self, idx: int) -> str:
        return self.filenames[idx]


# src/load_data.py

async def load_dataset(
        images: List[UploadFile],
        markup_file: UploadFile,
        image_col: str = "image_name",
        label_col: str = "label",
):
    """Загружает датасет из UploadFile."""
    print(f"Чтение CSV из файла: {markup_file.filename}")

    csv_content = await markup_file.read()
    csv_text = csv_content.decode('utf-8')

    # Читаем CSV с разделителем ;
    df = pd.read_csv(io.StringIO(csv_text), sep=';')

    # Очищаем названия колонок
    df.columns = df.columns.str.strip()

    print(f"Колонки: {list(df.columns)}")
    print(f"Уникальные классы: {df[label_col].unique().tolist()}")

    if image_col not in df.columns:
        raise ValueError(f"Колонка '{image_col}' не найдена. Доступные: {list(df.columns)}")
    if label_col not in df.columns:
        raise ValueError(f"Колонка '{label_col}' не найдена. Доступные: {list(df.columns)}")

    csv_filenames = df[image_col].astype(str).tolist()
    csv_labels = df[label_col].tolist()

    # ✅ Создаем маппинг строковых меток -> индексы
    unique_labels = sorted(set(csv_labels))
    label_to_idx = {label: idx for idx, label in enumerate(unique_labels)}
    class_names = [str(l) for l in unique_labels]

    print(f"Маппинг классов: {label_to_idx}")
    print(f"Классов: {len(class_names)}")

    # Создаем маппинг имя_файла -> UploadFile
    file_by_name: dict[str, UploadFile] = {}
    for img in images:
        fname = img.filename
        if fname:
            file_by_name[fname] = img

    # Проверяем, что все файлы из CSV найдены
    missing = [f for f in csv_filenames if f not in file_by_name]
    if missing:
        raise ValueError(f"{len(missing)} файлов из CSV не найдены. Примеры: {missing[:5]}")

    loaded_images: List[Image.Image] = []
    matched_labels: List[int] = []  # ✅ Теперь храним индексы (int)
    matched_filenames: List[str] = []

    for fname, label in zip(csv_filenames, csv_labels):
        try:
            upload_file = file_by_name[fname]
            content = await upload_file.read()
            img = Image.open(io.BytesIO(content)).convert("RGB")

            loaded_images.append(img)
            matched_labels.append(label_to_idx[label])  # ✅ Преобразуем в индекс
            matched_filenames.append(fname)

            # Возвращаем указатель в начало для возможности повторного чтения
            await upload_file.seek(0)

        except Exception as e:
            print(f"Ошибка загрузки {fname}: {e}, пропускаем")

    print(f"Загружено изображений: {len(loaded_images)}")

    labels_array = np.array(matched_labels)

    dataset = DatasetFromImages(
        images=loaded_images,
        filenames=matched_filenames,
        labels=labels_array,
        class_names=class_names,
        transform=None,
    )

    print(dataset)

    return dataset, dataset.get_targets(), class_names