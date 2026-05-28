import React from "react";
import { useNavigate } from "react-router-dom";
import type { Dataset } from "../types/types";
import "./DatasetCard.scss";

interface DatasetCardProps {
  dataset: Dataset;
  showCreator?: boolean;
  onStarClick?: () => void;
  isStarred?: boolean;
  onDeleteClick?: () => void;
  showAnalyticsButton?: boolean;
}

const DatasetCard: React.FC<DatasetCardProps> = ({
  dataset,
  showCreator = true,
  onStarClick,
  isStarred,
  onDeleteClick,
  showAnalyticsButton = true,
}) => {
  const navigate = useNavigate();

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString("ru-RU", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const handleGoToAnalytics = () => {
    navigate(`/profile/datasets/${dataset.id}/analytics`);
  };

  return (
    <div className="dataset-card">
      <div className="dataset-card__name">{dataset.name}</div>
      <div className="dataset-card__description">{dataset.description}</div>
      <div className="dataset-card__meta">
        <span className="dataset-card__type">{dataset.type}</span>
        <span className="dataset-card__date">
          {formatDate(dataset.createdAt)}
        </span>
        <span className="dataset-card__date">
          {formatDate(dataset.updatedAt)}
        </span>
      </div>
      {showCreator && (
        <div className="dataset-card__creator">{dataset.creator.username}</div>
      )}
      <div className="dataset-card__actions">
        {showAnalyticsButton && (
          <button
            className="dataset-card__analytics-btn"
            onClick={handleGoToAnalytics}
          >
            Перейти в датазиторий
          </button>
        )}
        {onStarClick && (
          <button
            className={`dataset-card__star-btn ${isStarred ? "dataset-card__star-btn--active" : ""}`}
            onClick={onStarClick}
          >
            {isStarred ? "★ Удалить из звёзд" : "☆ В избранное"}
          </button>
        )}
        {onDeleteClick && (
          <button className="dataset-card__delete-btn" onClick={onDeleteClick}>
            Удалить датазиторий
          </button>
        )}
      </div>
    </div>
  );
};

export default DatasetCard;
