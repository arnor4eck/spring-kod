import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import DatasetNavigation from "../components/DatasetNavigation";
import { authService } from "../services/authService";
import { datasetAPI, markupAPI } from "../services/api";
import { analyticsAPI } from "../services/analyticsAPI";
import type { Dataset, FileToUpdate, LabelIssue } from "../types/types";
import type { apiError } from "../types/apiError";
import ExportIcon from "../assets/icons/Export.svg";
import "./ControversialLabelsPage.scss";

interface ControversialItem {
  id: number;
  imageUrl: string;
  userLabel: string;
  modelLabel: string;
  finalLabel: string;
  fileName: string;
}

const ControversialLabelsPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const datasetId = Number(id);

  const [user, setUser] = useState<{ username: string } | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [items, setItems] = useState<ControversialItem[]>([]);
  const [availableClasses, setAvailableClasses] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isExporting, setIsExporting] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

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

    const fetchClasses = async () => {
      try {
        const analytics = await analyticsAPI.getAnalytics(datasetId);
        const classNames = analytics.summary.classes.map((c) => c.name);
        setAvailableClasses(classNames);
      } catch (error) {
        console.error("Ошибка загрузки классов", error);
        setAvailableClasses([]);
      }
    };

    const fetchControversialData = async () => {
      try {
        const analytics = await analyticsAPI.getAnalytics(datasetId);
        const labelIssues: LabelIssue[] = analytics.groups.label_issues || [];

        const transformedItems: ControversialItem[] = labelIssues.map(
          (issue, index) => ({
            id: index + 1,
            imageUrl: issue.url,
            userLabel: issue.old_label_name,
            modelLabel: issue.suggested_label_name,
            finalLabel: issue.old_label_name,
            fileName: issue.file_name,
          }),
        );

        setItems(transformedItems);
      } catch (error) {
        console.error("Ошибка загрузки спорных меток", error);
        setItems([]);
      }
    };

    fetchUser();
    fetchDataset();
    fetchClasses();
    fetchControversialData();
  }, [navigate, datasetId]);

  const handleKeepAllLabels = () => {
    setItems(items.map((item) => ({ ...item, finalLabel: item.userLabel })));
    toast.success("Все метки заменены на метки пользователя");
  };

  const handleConfirmAllLabels = () => {
    setItems(items.map((item) => ({ ...item, finalLabel: item.modelLabel })));
    toast.success("Все метки заменены на метки модели");
  };

  const handleFinalLabelChange = (itemId: number, newLabel: string) => {
    setItems(
      items.map((item) =>
        item.id === itemId ? { ...item, finalLabel: newLabel } : item,
      ),
    );
  };

  const handleSaveAll = async () => {
    const changedItems = items.filter(
      (item) => item.finalLabel !== item.userLabel,
    );

    if (changedItems.length === 0) {
      toast("Нет изменений для сохранения");
      return;
    }

    const filesToUpdate: FileToUpdate[] = changedItems.map((item) => ({
      fileName: item.fileName,
      newLabel: item.finalLabel,
    }));

    const request = { filesToUpdate };

    setIsSaving(true);
    try {
      await markupAPI.updateMarkupLines(datasetId, request);
      toast.success(`Сохранено ${changedItems.length} изменений`);

      setItems(
        items.map((item) => ({
          ...item,
          userLabel: item.finalLabel,
        })),
      );
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка сохранения изменений");
      }
    } finally {
      setIsSaving(false);
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

  return (
    <div className="controversial-labels-page">
      <ProfileHeader
        username={user?.username || "Пользователь"}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="controversial-labels-page__content">
        <div className="controversial-labels-page__header">
          <button
            className="controversial-labels-page__back-btn"
            onClick={() => navigate(`/profile/datasets/${datasetId}/analytics`)}
            type="button"
          >
            ← Назад
          </button>
          <div className="controversial-labels-page__title-wrapper">
            <h1 className="controversial-labels-page__title">{dataset.name}</h1>
            <span className="controversial-labels-page__visibility">
              {visibilityText}
            </span>
          </div>
          <div className="controversial-labels-page__actions">
            <button
              className="controversial-labels-page__action-btn"
              onClick={() =>
                navigate(`/profile/datasets/${datasetId}/analytics`)
              }
            >
              Аналитика
            </button>
            <button className="controversial-labels-page__action-btn controversial-labels-page__action-btn--active">
              Работа с датасетом
            </button>
            <button
              className="controversial-labels-page__export-btn"
              onClick={handleExport}
              disabled={isExporting}
            >
              <img src={ExportIcon} alt="Export icon" />
            </button>
          </div>
        </div>

        <DatasetNavigation datasetId={datasetId} />

        <div className="controversial-labels-page__controls">
          <button
            className="controversial-labels-page__radio-btn"
            onClick={handleKeepAllLabels}
            disabled={isSaving}
          >
            Оставить все метки (пользовательские)
          </button>
          <button
            className="controversial-labels-page__radio-btn"
            onClick={handleConfirmAllLabels}
            disabled={isSaving}
          >
            Подтвердить все метки (модельные)
          </button>
        </div>

        <div className="controversial-labels-page__table-wrapper">
          <table className="controversial-labels-page__table">
            <thead>
              <tr>
                <th>Объект</th>
                <th>Метка пользователя</th>
                <th>Предполагаемая метка модели</th>
                <th>Итоговая метка</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <tr>
                    <img
                      src={item.imageUrl}
                      alt={`controversial-${item.id}`}
                      className="controversial-labels-page__thumbnail"
                    />
                  </tr>
                  <td>{item.userLabel}</td>
                  <td>{item.modelLabel}</td>
                  <td>
                    <select
                      className="controversial-labels-page__select"
                      value={item.finalLabel}
                      onChange={(e) =>
                        handleFinalLabelChange(item.id, e.target.value)
                      }
                      disabled={isSaving}
                    >
                      {availableClasses.map((className) => (
                        <option key={className} value={className}>
                          {className}
                        </option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="controversial-labels-page__save">
          <button
            className="controversial-labels-page__save-btn"
            onClick={handleSaveAll}
            disabled={isSaving}
          >
            {isSaving ? "Сохранение..." : "Сохранить все изменения"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ControversialLabelsPage;
