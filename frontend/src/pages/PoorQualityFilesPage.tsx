import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import DatasetNavigation from "../components/DatasetNavigation";
import { authService } from "../services/authService";
import { datasetAPI, markupAPI } from "../services/api";
import { analyticsAPI } from "../services/analyticsAPI";
import type { Dataset, QualityIssue, FileToDelete } from "../types/types";
import type { apiError } from "../types/apiError";
import ExportIcon from "../assets/icons/Export.svg";
import "./PoorQualityFilesPage.scss";

const PoorQualityFilesPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const datasetId = Number(id);

  const [user, setUser] = useState<{ username: string } | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [items, setItems] = useState<QualityIssue[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isExporting, setIsExporting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedIndices, setSelectedIndices] = useState<Set<number>>(
    new Set(),
  );

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
      } catch (error) {
        console.error("Ошибка загрузки датасета", error);
      }
    };

    const fetchQualityData = async () => {
      try {
        const analytics = await analyticsAPI.getAnalytics(datasetId);
        setItems(analytics.groups.quality_issues || []);
      } catch (error) {
        console.error("Ошибка загрузки файлов плохого качества", error);
        setItems([]);
      }
    };

    fetchUser();
    fetchDataset();
    fetchQualityData();
  }, [navigate, datasetId]);

  const handleSelectItem = (index: number) => {
    setSelectedIndices((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(index)) {
        newSet.delete(index);
      } else {
        newSet.add(index);
      }
      return newSet;
    });
  };

  const handleSelectAll = () => {
    if (selectedIndices.size === items.length) {
      setSelectedIndices(new Set());
    } else {
      setSelectedIndices(new Set(items.map((_, index) => index)));
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedIndices.size === 0) {
      toast.error("Выберите объекты для удаления");
      return;
    }

    const filesToDelete: FileToDelete[] = Array.from(selectedIndices).map(
      (idx) => ({
        fileName: items[idx].file_name,
      }),
    );

    if (
      !window.confirm(
        `Вы уверены, что хотите удалить ${filesToDelete.length} файлов? Это действие необратимо.`,
      )
    ) {
      return;
    }

    setIsDeleting(true);
    try {
      await markupAPI.deleteMarkupLines(datasetId, { filesToDelete });

      const newItems = items.filter((_, idx) => !selectedIndices.has(idx));
      setItems(newItems);
      setSelectedIndices(new Set());

      toast.success(`Удалено ${filesToDelete.length} файлов`);
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка удаления файлов");
      }
    } finally {
      setIsDeleting(false);
    }
  };

  const handleDeleteAll = async () => {
    if (items.length === 0) {
      toast.error("Нет файлов для удаления");
      return;
    }

    if (
      !window.confirm(
        `Вы уверены, что хотите удалить все ${items.length} файлов? Это действие необратимо.`,
      )
    ) {
      return;
    }

    const filesToDelete: FileToDelete[] = items.map((item) => ({
      fileName: item.file_name,
    }));

    setIsDeleting(true);
    try {
      await markupAPI.deleteMarkupLines(datasetId, { filesToDelete });
      setItems([]);
      setSelectedIndices(new Set());
      toast.success(`Удалено ${filesToDelete.length} файлов`);
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка удаления файлов");
      }
    } finally {
      setIsDeleting(false);
    }
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

  const filteredItems = items.filter((item) =>
    item.tags?.some((tag) =>
      tag.toLowerCase().includes(searchQuery.toLowerCase()),
    ),
  );

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

  const visibilityText = dataset.type === "OPEN" ? "Публичный" : "Приватный";
  const hasSelectedItems = selectedIndices.size > 0;

  return (
    <div className="poor-quality-files-page">
      <ProfileHeader
        username={user?.username || "Пользователь"}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="poor-quality-files-page__content">
        <div className="poor-quality-files-page__header">
          <button
            className="poor-quality-files-page__back-btn"
            onClick={() => navigate(`/profile/datasets/${datasetId}/analytics`)}
            type="button"
          >
            ← Назад
          </button>
          <div className="poor-quality-files-page__title-wrapper">
            <h1 className="poor-quality-files-page__title">{dataset.name}</h1>
            <span className="poor-quality-files-page__visibility">
              {visibilityText}
            </span>
          </div>
          <div className="poor-quality-files-page__actions">
            <button
              className="poor-quality-files-page__action-btn"
              onClick={() =>
                navigate(`/profile/datasets/${datasetId}/analytics`)
              }
            >
              Аналитика
            </button>
            <button className="poor-quality-files-page__action-btn poor-quality-files-page__action-btn--active">
              Работа с датасетом
            </button>
            <button
              className="poor-quality-files-page__export-btn"
              onClick={handleExport}
              disabled={isExporting}
            >
              <img src={ExportIcon} alt="Export icon" />
            </button>
          </div>
        </div>

        <DatasetNavigation datasetId={datasetId} />

        <div className="poor-quality-files-page__controls">
          <button
            className="poor-quality-files-page__delete-all-btn"
            onClick={handleDeleteAll}
            disabled={isDeleting || items.length === 0}
          >
            Удалить всё
          </button>
          {hasSelectedItems && (
            <button
              className="poor-quality-files-page__delete-selected-btn"
              onClick={handleDeleteSelected}
              disabled={isDeleting}
            >
              {isDeleting
                ? "Удаление..."
                : `Удалить выбранные (${selectedIndices.size})`}
            </button>
          )}
        </div>

        <div className="poor-quality-files-page__table-wrapper">
          <table className="poor-quality-files-page__table">
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    checked={
                      selectedIndices.size === filteredItems.length &&
                      filteredItems.length > 0
                    }
                    onChange={handleSelectAll}
                    disabled={isDeleting || filteredItems.length === 0}
                  />
                </th>
                <th>Объект</th>
                <th>Теги</th>
                <th>Оценка</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.map((item) => {
                const originalIndex = items.findIndex(
                  (i) => i.file_name === item.file_name,
                );
                return (
                  <tr key={item.file_name}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedIndices.has(originalIndex)}
                        onChange={() => handleSelectItem(originalIndex)}
                        disabled={isDeleting}
                      />
                    </td>
                    <td>
                      <img
                        src={item.url}
                        alt={`poor-quality-${item.file_name}`}
                        className="poor-quality-files-page__thumbnail"
                      />
                    </td>
                    <td>{item.tags?.join(", ")}</td>
                    <td>{item.quality_score}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default PoorQualityFilesPage;
