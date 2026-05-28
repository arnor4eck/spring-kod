import React from "react";
import type { Dataset } from "../types/types";
import DatasetCard from "./DatasetCard";
import './StarredDatasetsList.scss';

interface StarredDatasetsListProps {
  datasets: Dataset[];
  onUnstar: (datasetId: number) => void;
}

const StarredDatasetsList: React.FC<StarredDatasetsListProps> = ({
  datasets,
  onUnstar,
}) => {
  if (datasets.length === 0) {
    return (
      <div className="starred-datasets">
        <h3 className="starred-datasets__title">Избранные датазитории</h3>
        <p className="starred-datasets__empty">Нет избранных датазиториев</p>
        <p className="starred-datasets__hint">
          Отмечайте датазитории звёздочкой, и они появятся здесь
        </p>
      </div>
    );
  }

  return (
    <div className="starred-datasets">
      <h3 className="starred-datasets__title">
        Избранные датазитории ({datasets.length})
      </h3>
      <div className="starred-datasets__list">
        {datasets.map((dataset) => (
          <DatasetCard
            key={dataset.id}
            dataset={dataset}
            showCreator={true}
            onStarClick={() => onUnstar(dataset.id)}
            isStarred={true}
          />
        ))}
      </div>
    </div>
  );
};

export default StarredDatasetsList;
