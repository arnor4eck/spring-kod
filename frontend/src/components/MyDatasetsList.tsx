import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import type { Dataset } from "../types/types";
import DatasetCard from "./DatasetCard";
import { datasetAPI, favoriteAPI } from "../services/api";
import type { apiError } from "../types/apiError";
import "./MyDatasetsList.scss";

interface MyDatasetsListProps {
  datasets: Dataset[];
  searchQuery: string;
  onDatasetDelete?: (datasetId: number) => void;
  onFavoritesChange?: () => void;
}

const MyDatasetsList: React.FC<MyDatasetsListProps> = ({
  datasets,
  searchQuery,
  onDatasetDelete,
  onFavoritesChange,
}) => {
  const navigate = useNavigate();
  const [favoritesIds, setFavoritesIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    let isMounted = true;

    const fetchFavorites = async () => {
      try {
        const favorites = await favoriteAPI.getMyFavorites();
        if (isMounted) {
          if (Array.isArray(favorites)) {
            const ids = new Set(favorites.map((fav) => fav.id));
            console.log("Загружены избранные ID:", Array.from(ids));
            setFavoritesIds(ids);
          } else {
            console.error("API вернул не массив:", favorites);
            setFavoritesIds(new Set());
          }
        }
      } catch (error) {
        console.error("Ошибка загрузки избранного", error);
        if (isMounted) {
          setFavoritesIds(new Set());
        }
      }
    };

    fetchFavorites();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleCreateDataset = () => {
    navigate("/profile/datasets/create");
  };

  const handleDeleteDataset = async (datasetId: number) => {
    if (
      window.confirm(
        "Вы уверены, что хотите удалить этот датазиторий? Это действие необратимо.",
      )
    ) {
      try {
        await datasetAPI.delete(datasetId);
        toast.success("Датазиторий успешно удалён");
        if (onDatasetDelete) {
          onDatasetDelete(datasetId);
        }
      } catch (error) {
        const apiError = error as apiError;
        if (apiError.messages && apiError.messages.length > 0) {
          apiError.messages.forEach((msg: string) => toast.error(msg));
        } else {
          toast.error("Ошибка удаления датазитория");
        }
      }
    }
  };

  const handleStarClick = async (datasetId: number, isStarred: boolean) => {
    console.log("handleStarClick:", {
      datasetId,
      isStarred,
      currentFavorites: Array.from(favoritesIds),
    });

    try {
      if (isStarred) {
        console.log("Удаляем из избранного:", datasetId);
        await favoriteAPI.removeFromFavorites(datasetId);
        setFavoritesIds((prev) => {
          const newSet = new Set(prev);
          newSet.delete(datasetId);
          console.log(
            "Новый список избранных ID после удаления:",
            Array.from(newSet),
          );
          return newSet;
        });
        toast.success("Датазиторий удалён из избранного");
      } else {
        console.log("Добавляем в избранное:", datasetId);
        await favoriteAPI.addToFavorites(datasetId);
        setFavoritesIds((prev) => {
          const newSet = new Set([...prev, datasetId]);
          console.log(
            "Новый список избранных ID после добавления:",
            Array.from(newSet),
          );
          return newSet;
        });
        toast.success("Датазиторий добавлен в избранное");
      }
      if (onFavoritesChange) {
        onFavoritesChange();
      }
    } catch (error) {
      console.error("Ошибка при изменении избранного:", error);
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка");
      }
    }
  };

  if (datasets.length === 0) {
    return (
      <div className="my-datasets">
        <div className="my-datasets__header">
          <h3 className="my-datasets__title">Мои датазитории</h3>
          <button
            className="my-datasets__create-btn"
            onClick={handleCreateDataset}
          >
            Создать датазиторий
          </button>
        </div>
        {searchQuery ? (
          <p className="my-datasets__empty">
            Ничего не найдено по запросу "{searchQuery}"
          </p>
        ) : (
          <p className="my-datasets__empty">У вас пока нет датазиториев</p>
        )}
      </div>
    );
  }

  return (
    <div className="my-datasets">
      <div className="my-datasets__header">
        <h3 className="my-datasets__title">
          Мои датазитории ({datasets.length})
        </h3>
        <button
          className="my-datasets__create-btn"
          onClick={handleCreateDataset}
        >
          Создать датазиторий
        </button>
      </div>
      <div className="my-datasets__list">
        {datasets.map((dataset) => {
          const isStarred = favoritesIds.has(dataset.id);
          return (
            <DatasetCard
              key={dataset.id}
              dataset={dataset}
              showCreator={true}
              onDeleteClick={() => handleDeleteDataset(dataset.id)}
              showAnalyticsButton={true}
              onStarClick={() => handleStarClick(dataset.id, isStarred)}
              isStarred={isStarred}
            />
          );
        })}
      </div>
    </div>
  );
};

export default MyDatasetsList;
