import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import GeneralAnalytics from "../components/GeneralAnalytics";
import ObjectsTable from "../components/ObjectsTable";
import ReliableDataTable from "../components/ReliableDataTable";
import ControversialObjectsTable from "../components/ControversialObjectsTable";
import DuplicatesGroups from "../components/DuplicatesGroups";
import PoorQualityFilesTable from "../components/PoorQualityFilesTable";
import RoadmapBlock from "../components/RoadmapBlock";
import { analyticsAPI } from "../services/analyticsAPI";
import { authService } from "../services/authService";
import { datasetAPI } from "../services/api";
import type { MlAnalyticsResponse, Dataset } from "../types/types";
import type { apiError } from "../types/apiError";
import ExportIcon from "../assets/icons/Export.svg";
import "./DataAnalyticsPage.scss";

const DatasetAnalyticsPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const datasetId = Number(id);

  const [user, setUser] = useState<{ username: string } | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [analytics, setAnalytics] = useState<MlAnalyticsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isExporting, setIsExporting] = useState(false);

  useEffect(() => {
    let isMounted = true;

    if (!authService.isAuthenticated()) {
      navigate("/login");
      return;
    }

    const fetchUser = async () => {
      try {
        const userData = await authService.fetchCurrentUser();
        if (isMounted && userData && userData.id !== -1) {
          setUser(userData);
        }
      } catch (error) {
        console.error("Ошибка загрузки пользователя", error);
      }
    };

    const fetchDataset = async () => {
      try {
        const data = await datasetAPI.getDatasetById(datasetId);
        if (isMounted) {
          setDataset(data);
        }
      } catch (error) {
        console.error("Ошибка загрузки датасета", error);
        if (isMounted) {
          setError("Не удалось загрузить информацию о датасете");
        }
      }
    };

    const fetchAnalytics = async () => {
      try {
        const data = await analyticsAPI.getAnalytics(datasetId);
        if (isMounted) {
          setAnalytics(data);
        }
      } catch (error) {
        const apiError = error as apiError;
        if (isMounted) {
          if (apiError.messages && apiError.messages.length > 0) {
            setError(apiError.messages[0]);
          } else {
            setError("Ошибка загрузки аналитики");
          }
        }
      }
    };

    const loadAllData = async () => {
      setIsLoading(true);
      setError(null);

      await Promise.all([fetchUser(), fetchDataset(), fetchAnalytics()]);

      if (isMounted) {
        setIsLoading(false);
      }
    };

    loadAllData();

    return () => {
      isMounted = false;
    };
  }, [navigate, datasetId]);

  const handleBack = () => {
    navigate("/profile/datasets");
  };

  const handleLogout = () => {
    authService.logout();
    toast.success("Вы вышли из системы");
    navigate("/login");
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

  if (isLoading) {
    return (
      <div className="loading">
        <div className="loading__container">
          <div className="loading__spinner"></div>
          <p className="loading__message">Загрузка...</p>
        </div>
      </div>
    );
  }

  if (error || !analytics || !dataset) {
    return (
      <div className="error">
        <div className="error__container">
          <p className="error__message">
            {error ||
              "Не удалось загрузить данные. Перезагрузите или перезайдите в аккаунт."}
          </p>
          <button className="error__button" onClick={handleLogout}>
            Выйти
          </button>
        </div>
      </div>
    );
  }

  const visibilityText = dataset.type;

  return (
    <div className="dataset-analytics-page">
      <ProfileHeader
        username={user?.username || "Пользователь"}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="dataset-analytics-page__content">
        <div className="dataset-analytics-page__header">
          <button
            className="dataset-analytics-page__back-btn"
            onClick={handleBack}
            type="button"
          >
            ← Назад
          </button>
          <div className="dataset-analytics-page__title-wrapper">
            <h1 className="dataset-analytics-page__title">{dataset.name}</h1>
            <span className="dataset-analytics-page__visibility">
              {visibilityText}
            </span>
          </div>
          <div className="dataset-analytics-page__actions">
            <button className="dataset-analytics-page__action-btn dataset-analytics-page__action-btn--active">
              Аналитика
            </button>
            <button
              className="dataset-analytics-page__action-btn"
              onClick={() => navigate(`/profile/datasets/${datasetId}/edit`)}
            >
              Работа с датасетом
            </button>
            <button
              className="dataset-analytics-page__export-btn"
              onClick={handleExport}
              disabled={isExporting}
            >
              <img src={ExportIcon} alt="Export icon" />
            </button>
          </div>
        </div>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">
            Общая аналитика
          </h2>
          <GeneralAnalytics summary={analytics.summary} />
        </section>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">Объекты</h2>
          <ObjectsTable data={analytics.groups.all_objects} />
        </section>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">
            Надёжные данные
          </h2>
          <ReliableDataTable data={analytics.groups.reliable} />
        </section>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">
            Спорные метки
          </h2>
          <ControversialObjectsTable data={analytics.groups.label_issues} />
        </section>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">Дубликаты</h2>
          <DuplicatesGroups groups={analytics.groups.duplicates} />
        </section>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">
            Файлы плохого качества
          </h2>
          <PoorQualityFilesTable data={analytics.groups.quality_issues} />
        </section>

        <section className="dataset-analytics-page__section">
          <h2 className="dataset-analytics-page__section-title">
            Дорожная карта
          </h2>
          <RoadmapBlock roadmap={analytics.roadmap} />
        </section>
      </div>
    </div>
  );
};

export default DatasetAnalyticsPage;
