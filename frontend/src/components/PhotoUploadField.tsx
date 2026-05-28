import React, { useState, useRef } from "react";
import Upload from "../assets/icons/Upload.svg";
import Cross from "../assets/icons/Cross.svg";
import './PhotoUploadField.scss';

interface PhotoUploadFieldProps {
  label: string;
  required?: boolean;
  onPhotosSelect: (photos: File[]) => void;
}

const PhotoUploadField: React.FC<PhotoUploadFieldProps> = ({
  label,
  required = false,
  onPhotosSelect,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [photos, setPhotos] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validatePhoto = (file: File): boolean => {
    const acceptedTypes = [
      "image/jpeg",
      "image/png",
      "image/jpg",
      "image/webp",
    ];
    if (!acceptedTypes.includes(file.type)) {
      setError(`Неподдерживаемый формат. Разрешены: jpeg, png, jpg, webp`);
      return false;
    }
    setError(null);
    return true;
  };

  const createPreview = (file: File): Promise<string> => {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        resolve(reader.result as string);
      };
      reader.readAsDataURL(file);
    });
  };

  const handleFiles = async (selectedFiles: FileList | null) => {
    if (!selectedFiles) return;

    const newPhotos: File[] = [];
    const newPreviews: string[] = [];

    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i];
      if (validatePhoto(file)) {
        newPhotos.push(file);
        const preview = await createPreview(file);
        newPreviews.push(preview);
      }
    }

    const updatedPhotos = [...photos, ...newPhotos];
    setPhotos(updatedPhotos);
    setPreviews([...previews, ...newPreviews]);
    onPhotosSelect(updatedPhotos);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    handleFiles(e.dataTransfer.files);
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFiles(e.target.files);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleRemovePhoto = (index: number) => {
    const updatedPhotos = photos.filter((_, i) => i !== index);
    const updatedPreviews = previews.filter((_, i) => i !== index);
    setPhotos(updatedPhotos);
    setPreviews(updatedPreviews);
    onPhotosSelect(updatedPhotos);
  };

  return (
    <div className="photo-upload-field">
      <label className="photo-upload-field__label">
        {label}
        {required && <span className="photo-upload-field__required">*</span>}
      </label>

      <div
        className={`photo-upload-field__dropzone ${isDragging ? "photo-upload-field__dropzone--dragging" : ""}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        <div className="photo-upload-field__placeholder">
          <div className="photo-upload-field__icon">{<img src={Upload} alt="Upload icon" />}</div>
          <p className="photo-upload-field__text">
            Перетащите фотографии сюда или нажмите для выбора
          </p>
          <p className="photo-upload-field__hint">
            Можно выбрать несколько файлов
          </p>
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        className="photo-upload-field__input"
        onChange={handleFileInput}
        accept="image/jpeg,image/png,image/jpg,image/webp"
        multiple
        style={{ display: "none" }}
      />

      {error && <div className="photo-upload-field__error">{error}</div>}

      {previews.length > 0 && (
        <div className="photo-upload-field__gallery">
          <div className="photo-upload-field__gallery-title">
            Загружено фотографий: {previews.length}
          </div>
          <div className="photo-upload-field__gallery-grid">
            {previews.map((preview, index) => (
              <div key={index} className="photo-upload-field__photo">
                <img
                  src={preview}
                  alt={`preview-${index}`}
                  className="photo-upload-field__photo-img"
                />
                <button
                  type="button"
                  className="photo-upload-field__photo-remove"
                  onClick={() => handleRemovePhoto(index)}
                >
                  {<img src={Cross} alt="Удалить" />}
                </button>
                <div className="photo-upload-field__photo-name">
                  {photos[index]?.name.substring(0, 20)}
                  {photos[index]?.name.length > 20 ? "..." : ""}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default PhotoUploadField;
