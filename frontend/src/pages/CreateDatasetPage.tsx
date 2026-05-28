import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import PhotoUploadField from "../components/PhotoUploadField";
import FileUploadField from "../components/FileUploadField";
import { datasetAPI, fileAPI } from "../services/api";
import { authService } from "../services/authService";
import type { User } from "../types/types";
import type { apiError } from "../types/apiError";
import './CreateDatasetPage.scss';

const CreateDatasetPage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] = useState<"PUBLIC" | "PRIVATE">("PUBLIC");

  const [photos, setPhotos] = useState<File[]>([]);
  const [labelsFile, setLabelsFile] = useState<File | null>(null);
  const [probabilitiesFile, setProbabilitiesFile] = useState<File | null>(null);
  const [metadataFile, setMetadataFile] = useState<File | null>(null);

  const [errors, setErrors] = useState<{
    name?: string;
    photos?: string;
    labelsFile?: string;
  }>({});

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const userData = await authService.fetchCurrentUser();
        if (userData && userData.id !== -1) {
          setUser(userData);
        }
      } catch (error) {
        console.error("Ошибка загрузки пользователя", error);
      }
    };
    fetchUser();
  }, []);

  const validateForm = (): boolean => {
    const newErrors: typeof errors = {};

    if (!name.trim()) {
      newErrors.name = "Название датазитория обязательно";
    }

    if (photos.length === 0) {
      newErrors.photos = "Добавьте хотя бы одну фотографию";
    }

    if (!labelsFile) {
      newErrors.labelsFile = "Файл с метками обязателен";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error("Пожалуйста, заполните все обязательные поля");
      return;
    }

    setIsLoading(true);

    try {
      const newDataset = await datasetAPI.create({
        name,
        description: description || undefined,
        visibility,
        creatorId: user!.id,
      });

      const datasetId = newDataset.id;

      if (photos.length > 0) {
        await fileAPI.uploadImages(datasetId, photos);
      }

      if (labelsFile) {
        await fileAPI.uploadMarkup(datasetId, labelsFile);
      }

      if (probabilitiesFile) {
        await fileAPI.uploadProbability(datasetId, probabilitiesFile);
      }

      if (metadataFile) {
        await fileAPI.uploadMetadata(datasetId, metadataFile);
      }

      toast.success("Датазиторий успешно создан");
      navigate("/profile/datasets");
    } catch (error) {
      const apiError = error as apiError;

      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка создания датазитория");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/profile/datasets");
  };

  if (!user) {
    return (
      <div className="loading">
        <div className="loading__container">
          <div className="loading__spinner"></div>
          <p className="loading__message">Загрузка...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="create-dataset-page">
      <ProfileHeader
        username={user.username}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="create-dataset-page__content">
        <div className="create-dataset-page__header">
          <button
            className="create-dataset-page__back-btn"
            onClick={handleBack}
            type="button"
          >
            ← Назад
          </button>
          <h1 className="create-dataset-page__title">Загрузка датасета</h1>
        </div>

        <form className="create-dataset-form" onSubmit={handleSubmit}>
          <div className="create-dataset-form__field">
            <label className="create-dataset-form__label">
              Название датазитория{" "}
              <span className="create-dataset-form__required">*</span>
            </label>
            <input
              type="text"
              className={`create-dataset-form__input ${errors.name ? "create-dataset-form__input--error" : ""}`}
              placeholder="Введите название датазитория"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={isLoading}
            />
            {errors.name && (
              <div className="create-dataset-form__error">{errors.name}</div>
            )}
          </div>

          <div className="create-dataset-form__field">
            <label className="create-dataset-form__label">
              Описание датазитория
            </label>
            <textarea
              className="create-dataset-form__textarea"
              placeholder="Введите описание датазитория"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
              disabled={isLoading}
            />
          </div>

          {/* Видимость */}
          <div className="create-dataset-form__field">
            <label className="create-dataset-form__label">
              Видимость <span className="create-dataset-form__required">*</span>
            </label>
            <div className="create-dataset-form__radio-group">
              <label className="create-dataset-form__radio">
                <input
                  type="radio"
                  value="PUBLIC"
                  checked={visibility === "PUBLIC"}
                  onChange={(e) => setVisibility(e.target.value as "PUBLIC")}
                  disabled={isLoading}
                />
                <span>Публичный</span>
              </label>
              <label className="create-dataset-form__radio">
                <input
                  type="radio"
                  value="PRIVATE"
                  checked={visibility === "PRIVATE"}
                  onChange={(e) => setVisibility(e.target.value as "PRIVATE")}
                  disabled={isLoading}
                />
                <span>Приватный</span>
              </label>
            </div>
          </div>

          {/* Фотографии */}
          <PhotoUploadField
            label="Фотографии"
            required={true}
            onPhotosSelect={setPhotos}
          />
          {errors.photos && (
            <div className="create-dataset-form__error">{errors.photos}</div>
          )}

          {/* Файл с метками (разметка) - CSV */}
          <FileUploadField
            label="Файл с метками"
            required={true}
            onFileSelect={setLabelsFile}
            acceptedFileTypes=".csv"
          />
          {errors.labelsFile && (
            <div className="create-dataset-form__error">
              {errors.labelsFile}
            </div>
          )}

          {/* Файл с вероятностями модели - JSON */}
          <FileUploadField
            label="Файл с вероятностями модели"
            required={false}
            onFileSelect={setProbabilitiesFile}
            acceptedFileTypes=".json"
          />

          {/* Файл с метаданными - CSV */}
          <FileUploadField
            label="Файл с метаданными"
            required={false}
            onFileSelect={setMetadataFile}
            acceptedFileTypes=".csv"
          />

          {/* Кнопка сохранения */}
          <div className="create-dataset-form__actions">
            <button
              type="submit"
              className="create-dataset-form__submit-btn"
              disabled={isLoading}
            >
              {isLoading ? "Сохранение..." : "Сохранить"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateDatasetPage;
