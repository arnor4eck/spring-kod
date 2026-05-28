import React, { useState, useRef } from "react";
import Upload from "../assets/icons/Upload.svg";
import Cross from "../assets/icons/Cross.svg";
import './FileUploadField.scss';

interface FileUploadFieldProps {
  label: string;
  required?: boolean;
  onFileSelect: (file: File | null) => void;
  acceptedFileTypes?: string;
}

const FileUploadField: React.FC<FileUploadFieldProps> = ({
  label,
  required = false,
  onFileSelect,
  acceptedFileTypes = "*/*",
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = (selectedFile: File): boolean => {
    if (acceptedFileTypes !== "*/*") {
      const acceptedTypes = acceptedFileTypes.split(",");
      const fileExtension = `.${selectedFile.name.split(".").pop()}`;

      const isValid = acceptedTypes.some((type) => {
        if (type.startsWith(".")) {
          return fileExtension === type;
        }
        return selectedFile.type === type;
      });

      if (!isValid) {
        setError(`Неподдерживаемый формат. Разрешены: ${acceptedFileTypes}`);
        return false;
      }
    }

    setError(null);
    return true;
  };

  const handleFile = (selectedFile: File | null) => {
    if (!selectedFile) {
      setFile(null);
      onFileSelect(null);
      return;
    }

    if (validateFile(selectedFile)) {
      setFile(selectedFile);
      onFileSelect(selectedFile);
    }
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

    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile) {
      handleFile(droppedFile);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    handleFile(selectedFile);
  };

  const handleRemoveFile = () => {
    setFile(null);
    onFileSelect(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  return (
    <div className="file-upload-field">
      <label className="file-upload-field__label">
        {label}
        {required && <span className="file-upload-field__required">*</span>}
      </label>

      <div
        className={`file-upload-field__dropzone ${isDragging ? "file-upload-field__dropzone--dragging" : ""} ${file ? "file-upload-field__dropzone--has-file" : ""}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        {!file ? (
          <div className="file-upload-field__placeholder">
            <div className="file-upload-field__icon">{<img src={Upload} alt="Upload icon" />}</div>
            <p className="file-upload-field__text">
              Перетащите файл сюда или нажмите для выбора
            </p>
          </div>
        ) : (
          <div className="file-upload-field__file-info">
            <div className="file-upload-field__file-icon">
              {<img src={Upload} alt="Upload icon" />}
            </div>
            <div className="file-upload-field__file-details">
              <div className="file-upload-field__file-name">{file.name}</div>
              <div className="file-upload-field__file-size">
                {formatFileSize(file.size)}
              </div>
            </div>
            <button
              type="button"
              className="file-upload-field__remove-btn"
              onClick={(e) => {
                e.stopPropagation();
                handleRemoveFile();
              }}
            >
              {<img src={Cross} alt="Удалить" />}
            </button>
          </div>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        className="file-upload-field__input"
        onChange={handleFileInput}
        accept={acceptedFileTypes}
        style={{ display: "none" }}
      />

      {error && <div className="file-upload-field__error">{error}</div>}
    </div>
  );
};

export default FileUploadField;
