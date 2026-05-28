import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import DatasetNavigation from "../components/DatasetNavigation";
import { authService } from "../services/authService";
import { datasetAPI, markupAPI } from "../services/api";
import { analyticsAPI } from "../services/analyticsAPI";
import type { Dataset, DuplicateGroup, FileToDelete } from "../types/types";
import type { apiError } from "../types/apiError";
import ExportIcon from "../assets/icons/Export.svg";
import "./DuplicatesPage.scss";

const DuplicatesPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const datasetId = Number(id);

  const [user, setUser] = useState<{ username: string } | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [groups, setGroups] = useState<DuplicateGroup[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isExporting, setIsExporting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedCopies, setSelectedCopies] = useState<
    Map<number, Set<number>>
  >(new Map());

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

    const fetchDuplicatesData = async () => {
      try {
        const analytics = await analyticsAPI.getAnalytics(datasetId);
        setGroups(analytics.groups.duplicates || []);

        const initialSelected = new Map<number, Set<number>>();
        analytics.groups.duplicates?.forEach((_, groupIndex) => {
          initialSelected.set(groupIndex, new Set());
        });
        setSelectedCopies(initialSelected);
      } catch (error) {
        console.error("Ошибка загрузки дубликатов", error);
        setGroups([]);
      }
    };

    fetchUser();
    fetchDataset();
    fetchDuplicatesData();
  }, [navigate, datasetId]);

  const handleSelectCopy = (groupIndex: number, copyIndex: number) => {
    setSelectedCopies((prev) => {
      const newMap = new Map(prev);
      const groupSet = new Set(prev.get(groupIndex));
      if (groupSet.has(copyIndex)) {
        groupSet.delete(copyIndex);
      } else {
        groupSet.add(copyIndex);
      }
      newMap.set(groupIndex, groupSet);
      return newMap;
    });
  };

  const handleSelectAllInGroup = (groupIndex: number) => {
    const currentGroup = groups[groupIndex];
    const allCopyIndices = currentGroup.copies.map((_, idx) => idx);

    setSelectedCopies((prev) => {
      const newMap = new Map(prev);
      const currentSelected = prev.get(groupIndex) || new Set();
      const allSelected = allCopyIndices.every((idx) =>
        currentSelected.has(idx),
      );

      if (allSelected) {
        newMap.set(groupIndex, new Set());
      } else {
        newMap.set(groupIndex, new Set(allCopyIndices));
      }
      return newMap;
    });
  };

  const getSelectedCopiesCount = (groupIndex: number): number => {
    return selectedCopies.get(groupIndex)?.size || 0;
  };

  const getTotalSelectedCount = (): number => {
    let total = 0;
    selectedCopies.forEach((set) => {
      total += set.size;
    });
    return total;
  };

  const handleDeleteSelected = async () => {
    const filesToDelete: FileToDelete[] = [];

    groups.forEach((group, groupIndex) => {
      const selectedIndices = selectedCopies.get(groupIndex);
      if (selectedIndices && selectedIndices.size > 0) {
        selectedIndices.forEach((copyIndex) => {
          filesToDelete.push({ fileName: group.copies[copyIndex].file_name });
        });
      }
    });

    if (filesToDelete.length === 0) {
      toast.error("Выберите объекты для удаления");
      return;
    }

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

      const updatedGroups = groups
        .map((group, groupIndex) => ({
          ...group,
          copies: group.copies.filter(
            (_, copyIndex) => !selectedCopies.get(groupIndex)?.has(copyIndex),
          ),
        }))
        .filter((group) => group.copies.length > 0);

      setGroups(updatedGroups);

      const newSelected = new Map<number, Set<number>>();
      updatedGroups.forEach((_, idx) => {
        newSelected.set(idx, new Set());
      });
      setSelectedCopies(newSelected);

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

  const totalSelected = getTotalSelectedCount();

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
    <div className="duplicates-page">
      <ProfileHeader
        username={user?.username || "Пользователь"}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="duplicates-page__content">
        <div className="duplicates-page__header">
          <button
            className="duplicates-page__back-btn"
            onClick={() => navigate(`/profile/datasets/${datasetId}/analytics`)}
            type="button"
          >
            ← Назад
          </button>
          <div className="duplicates-page__title-wrapper">
            <h1 className="duplicates-page__title">{dataset.name}</h1>
            <span className="duplicates-page__visibility">
              {visibilityText}
            </span>
          </div>
          <div className="duplicates-page__actions">
            <button
              className="duplicates-page__action-btn"
              onClick={() =>
                navigate(`/profile/datasets/${datasetId}/analytics`)
              }
            >
              Аналитика
            </button>
            <button className="duplicates-page__action-btn duplicates-page__action-btn--active">
              Работа с датасетом
            </button>
            <button
              className="duplicates-page__export-btn"
              onClick={handleExport}
              disabled={isExporting}
            >
              <img src={ExportIcon} alt="Export icon" />
            </button>
          </div>
        </div>

        <DatasetNavigation datasetId={datasetId} />

        {totalSelected > 0 && (
          <div className="duplicates-page__global-delete">
            <button
              className="duplicates-page__delete-btn"
              onClick={handleDeleteSelected}
              disabled={isDeleting}
            >
              {isDeleting
                ? "Удаление..."
                : `Удалить выбранные (${totalSelected})`}
            </button>
          </div>
        )}

        <div className="duplicates-page__groups">
          {groups.length === 0 ? (
            <div className="duplicates-page__empty">Нет дубликатов</div>
          ) : (
            groups.map((group, groupIndex) => (
              <div key={group.group_id} className="duplicates-page__group">
                <div className="duplicates-page__group-header">
                  <span className="duplicates-page__group-name">
                    Группа дубликатов #{group.group_id}
                  </span>
                  <div className="duplicates-page__group-actions">
                    <button
                      className="duplicates-page__select-all-btn"
                      onClick={() => handleSelectAllInGroup(groupIndex)}
                      disabled={isDeleting}
                    >
                      {getSelectedCopiesCount(groupIndex) ===
                      group.copies.length
                        ? "Отменить все"
                        : "Выбрать все"}
                    </button>
                  </div>
                </div>
                <div className="duplicates-page__group-content">
                  <div className="duplicates-page__images">
                    <div className="duplicates-page__image-wrapper duplicates-page__image-wrapper--primary">
                      <img
                        src={group.primary.url}
                        alt={`primary-${group.group_id}`}
                        className="duplicates-page__image duplicates-page__image--primary"
                      />
                      <span className="duplicates-page__main-badge">
                        Главный
                      </span>
                    </div>
                    {group.copies.map((copy, copyIndex) => (
                      <div
                        key={copyIndex}
                        className={`duplicates-page__image-wrapper ${selectedCopies.get(groupIndex)?.has(copyIndex) ? "duplicates-page__image-wrapper--selected" : ""}`}
                      >
                        <input
                          type="checkbox"
                          className="duplicates-page__image-checkbox"
                          checked={
                            selectedCopies.get(groupIndex)?.has(copyIndex) ||
                            false
                          }
                          onChange={() =>
                            handleSelectCopy(groupIndex, copyIndex)
                          }
                          disabled={isDeleting}
                        />
                        <img
                          src={copy.url}
                          alt={`copy-${group.group_id}-${copyIndex}`}
                          className="duplicates-page__image"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default DuplicatesPage;
