import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import DatasetNavigation from "../components/DatasetNavigation";
import FileUploadField from "../components/FileUploadField";
import PhotoUploadField from "../components/PhotoUploadField";
import BrokenFilesModal from "../components/BrokenFilesModal";
import { datasetAPI, fileAPI } from "../services/api";
import { analyticsAPI } from "../services/analyticsAPI";
import { authService } from "../services/authService";
import type { Dataset } from "../types/types";
import type { apiError } from "../types/apiError";
import type { BrokenFile } from "../types/types";
import ExportIcon from "../assets/icons/Export.svg";
import "./DatasetEditPage.scss";

interface FileUploadState {
  photos: File[];
  labelsFile: File | null;
  probabilitiesFile: File | null;
  metadataFile: File | null;
}

interface FileExistsState {
  hasPhotos: boolean;
  hasLabels: boolean;
  hasProbabilities: boolean;
  hasMetadata: boolean;
}

const DatasetEditPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const datasetId = Number(id);

  const [user, setUser] = useState<{ username: string } | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [isExporting, setIsExporting] = useState(false);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] = useState<"PUBLIC" | "PRIVATE">("PUBLIC");

  const [filesToUpload, setFilesToUpload] = useState<FileUploadState>({
    photos: [],
    labelsFile: null,
    probabilitiesFile: null,
    metadataFile: null,
  });

  const [existingFiles, setExistingFiles] = useState<FileExistsState>({
    hasPhotos: false,
    hasLabels: false,
    hasProbabilities: false,
    hasMetadata: false,
  });

  const [showBrokenModal, setShowBrokenModal] = useState(false);
  const [brokenFiles, setBrokenFiles] = useState<BrokenFile[]>([]);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate("/login");
      return;
    }

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

    const fetchDataset = async () => {
      try {
        const data = await datasetAPI.getDatasetById(datasetId);
        setDataset(data);
        setName(data.name);
        setDescription(data.description);
        setVisibility(data.type === "OPEN" ? "PUBLIC" : "PRIVATE");
      } catch (error) {
        console.error("Ошибка загрузки датасета", error);
      }
    };

    const fetchExistingFilesInfo = async () => {
      try {
        const analytics = await analyticsAPI.getAnalytics(datasetId);

        const hasPhotos =
          analytics.groups.all_objects &&
          analytics.groups.all_objects.length > 0;
        const hasLabels = analytics.groups.label_issues !== undefined;
        const hasProbabilities =
          analytics.groups.all_objects?.some(
            (obj) => obj.confidence !== undefined,
          ) || false;
        const hasMetadata =
          analytics.groups.all_objects?.some(
            (obj) => obj.label_score !== undefined,
          ) || false;

        setExistingFiles({
          hasPhotos,
          hasLabels,
          hasProbabilities,
          hasMetadata,
        });
      } catch (error) {
        console.error("Ошибка получения информации о файлах", error);
      }
    };

    fetchUser();
    fetchDataset();
    fetchExistingFilesInfo();
  }, [navigate, datasetId]);

  const isNameChanged = dataset ? name !== dataset.name : false;
  const isDescriptionChanged = dataset
    ? description !== dataset.description
    : false;
  const isVisibilityChanged = dataset
    ? visibility !== (dataset.type === "OPEN" ? "PUBLIC" : "PRIVATE")
    : false;
  const hasFilesToUpload =
    filesToUpload.photos.length > 0 ||
    filesToUpload.labelsFile !== null ||
    filesToUpload.probabilitiesFile !== null ||
    filesToUpload.metadataFile !== null;

  const currentHasUnsavedChanges =
    isNameChanged ||
    isDescriptionChanged ||
    isVisibilityChanged ||
    hasFilesToUpload;

  if (currentHasUnsavedChanges !== hasUnsavedChanges) {
    setHasUnsavedChanges(currentHasUnsavedChanges);
  }

  const handleSaveAllChanges = async () => {
    setIsLoading(true);

    try {
      if (
        dataset &&
        (name !== dataset.name ||
          description !== dataset.description ||
          visibility !== (dataset.type === "OPEN" ? "PUBLIC" : "PRIVATE"))
      ) {
        await datasetAPI.update(datasetId, {
          name,
          description: description || undefined,
          visibility,
        });
      }

      if (filesToUpload.photos.length > 0 && existingFiles.hasPhotos) {
        await fileAPI.uploadImages(datasetId, filesToUpload.photos);
        setFilesToUpload((prev) => ({ ...prev, photos: [] }));
      }

      if (filesToUpload.labelsFile && existingFiles.hasLabels) {
        await fileAPI.uploadMarkup(datasetId, filesToUpload.labelsFile);
        setFilesToUpload((prev) => ({ ...prev, labelsFile: null }));
      }

      if (filesToUpload.probabilitiesFile && existingFiles.hasProbabilities) {
        await fileAPI.uploadProbability(
          datasetId,
          filesToUpload.probabilitiesFile,
        );
        setFilesToUpload((prev) => ({ ...prev, probabilitiesFile: null }));
      }

      if (filesToUpload.metadataFile && existingFiles.hasMetadata) {
        await fileAPI.uploadMetadata(datasetId, filesToUpload.metadataFile);
        setFilesToUpload((prev) => ({ ...prev, metadataFile: null }));
      }

      toast.success("Все изменения сохранены");
      setHasUnsavedChanges(false);

      const updatedDataset = await datasetAPI.getDatasetById(datasetId);
      setDataset(updatedDataset);
      setName(updatedDataset.name);
      setDescription(updatedDataset.description);
      setVisibility(updatedDataset.type === "OPEN" ? "PUBLIC" : "PRIVATE");

      const analytics = await analyticsAPI.getAnalytics(datasetId);
      const hasPhotos =
        analytics.groups.all_objects && analytics.groups.all_objects.length > 0;
      const hasLabels = analytics.groups.label_issues !== undefined;
      const hasProbabilities =
        analytics.groups.all_objects?.some(
          (obj) => obj.confidence !== undefined,
        ) || false;
      const hasMetadata =
        analytics.groups.all_objects?.some(
          (obj) => obj.label_score !== undefined,
        ) || false;

      setExistingFiles({
        hasPhotos,
        hasLabels,
        hasProbabilities,
        hasMetadata,
      });
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка сохранения изменений");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCheckBrokenFiles = async () => {
    const allNewFiles: File[] = [];

    allNewFiles.push(...filesToUpload.photos);
    if (filesToUpload.labelsFile) allNewFiles.push(filesToUpload.labelsFile);
    if (filesToUpload.probabilitiesFile)
      allNewFiles.push(filesToUpload.probabilitiesFile);
    if (filesToUpload.metadataFile)
      allNewFiles.push(filesToUpload.metadataFile);

    if (allNewFiles.length === 0) {
      toast.error("Нет файлов для загрузки");
      return;
    }

    const broken = await analyticsAPI.checkBrokenFiles(datasetId);

    if (broken.length > 0) {
      setBrokenFiles(broken);
      setShowBrokenModal(true);
      return;
    }

    setHasUnsavedChanges(true);
    toast.success("Файлы готовы к сохранению");
  };

  const handleConfirmUpload = () => {
    setShowBrokenModal(false);
    setHasUnsavedChanges(true);
    toast.success(
      "Файлы будут сохранены при нажатии 'Сохранить все изменения'",
    );
  };

  const handleExport = async () => {
    setIsExporting(true);
    try {
      const blob = await datasetAPI.exportDataset(datasetId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `dataset_${datasetId}.zip`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success("Экспорт датасета начался");
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка экспорта датасета");
      }
    } finally {
      setIsExporting(false);
    }
  };

  if (!dataset) {
    return (
      <div className="loading">
        <div className="loading__container">
          <div className="loading__spinner"></div>
          <p className="loading__message">Загрузка...</p>
        </div>
      </div>
    );
  }

  const visibilityText = dataset.type;

  return (
    <div className="dataset-edit-page">
      <ProfileHeader
        username={user?.username || "Пользователь"}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="dataset-edit-page__content">
        <div className="dataset-edit-page__header">
          <button
            className="dataset-edit-page__back-btn"
            onClick={() => navigate(`/profile/datasets/${datasetId}/analytics`)}
            type="button"
          >
            ← Назад
          </button>
          <div className="dataset-edit-page__title-wrapper">
            <h1 className="dataset-edit-page__title">{dataset.name}</h1>
            <span className="dataset-edit-page__visibility">
              {visibilityText}
            </span>
          </div>
          <div className="dataset-edit-page__actions">
            <button
              className="dataset-edit-page__action-btn"
              onClick={() =>
                navigate(`/profile/datasets/${datasetId}/analytics`)
              }
            >
              Аналитика
            </button>
            <button className="dataset-edit-page__action-btn dataset-edit-page__action-btn--active">
              Работа с датасетом
            </button>
            <button
              className="dataset-edit-page__export-btn"
              onClick={handleExport}
              disabled={isExporting}
            >
              <img src={ExportIcon} alt="Export icon" />
            </button>
          </div>
        </div>

        <DatasetNavigation datasetId={datasetId} />

        <div className="dataset-edit-page__section">
          <h2 className="dataset-edit-page__section-title">
            Редактирование датазитория
          </h2>

          <div className="dataset-edit-page__field">
            <label className="dataset-edit-page__label">
              Название датазитория
            </label>
            <input
              type="text"
              className="dataset-edit-page__input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div className="dataset-edit-page__field">
            <label className="dataset-edit-page__label">
              Описание датазитория
            </label>
            <textarea
              className="dataset-edit-page__textarea"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
              disabled={isLoading}
            />
          </div>

          <div className="dataset-edit-page__field">
            <label className="dataset-edit-page__label">Видимость</label>
            <div className="dataset-edit-page__radio-group">
              <label className="dataset-edit-page__radio">
                <input
                  type="radio"
                  value="PUBLIC"
                  checked={visibility === "PUBLIC"}
                  onChange={(e) => setVisibility(e.target.value as "PUBLIC")}
                  disabled={isLoading}
                />
                <span>Публичный</span>
              </label>
              <label className="dataset-edit-page__radio">
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

          <div className="dataset-edit-page__upload">
            <h3 className="dataset-edit-page__subtitle">Догрузка файлов</h3>
            <p className="dataset-edit-page__hint">
              Вы можете догрузить файлы только тех типов, которые уже были
              загружены ранее
            </p>

            <PhotoUploadField
              label="Фотографии (догрузка)"
              required={false}
              onPhotosSelect={(photos) =>
                setFilesToUpload((prev) => ({ ...prev, photos }))
              }
            />

            <FileUploadField
              label="Файл с метками (догрузка)"
              required={false}
              onFileSelect={(file) =>
                setFilesToUpload((prev) => ({ ...prev, labelsFile: file }))
              }
              acceptedFileTypes=".csv"
            />

            {existingFiles.hasProbabilities && (
              <FileUploadField
                label="Файл с вероятностями модели (догрузка)"
                required={false}
                onFileSelect={(file) =>
                  setFilesToUpload((prev) => ({
                    ...prev,
                    probabilitiesFile: file,
                  }))
                }
                acceptedFileTypes=".json"
              />
            )}
            {!existingFiles.hasProbabilities && (
              <div className="dataset-edit-page__disabled-info">
                Файл вероятностей не был загружен ранее. Догрузка недоступна.
              </div>
            )}

            {existingFiles.hasMetadata && (
              <FileUploadField
                label="Файл с метаданными (догрузка)"
                required={false}
                onFileSelect={(file) =>
                  setFilesToUpload((prev) => ({ ...prev, metadataFile: file }))
                }
                acceptedFileTypes=".csv"
              />
            )}
            {!existingFiles.hasMetadata && (
              <div className="dataset-edit-page__disabled-info">
                Файл метаданных не был загружен ранее. Догрузка недоступна.
              </div>
            )}

            <button
              className="dataset-edit-page__upload-btn"
              onClick={handleCheckBrokenFiles}
              disabled={isLoading || !hasFilesToUpload}
            >
              Подготовить файлы к сохранению
            </button>
          </div>
        </div>

        <div className="dataset-edit-page__save-all">
          <button
            className={`dataset-edit-page__save-all-btn ${hasUnsavedChanges ? "dataset-edit-page__save-all-btn--active" : ""}`}
            onClick={handleSaveAllChanges}
            disabled={isLoading || !hasUnsavedChanges}
          >
            {isLoading ? "Сохранение..." : "Сохранить все изменения"}
          </button>
        </div>
      </div>

      <BrokenFilesModal
        isOpen={showBrokenModal}
        brokenFiles={brokenFiles}
        onClose={() => setShowBrokenModal(false)}
        onConfirm={handleConfirmUpload}
      />
    </div>
  );
};

export default DatasetEditPage;
