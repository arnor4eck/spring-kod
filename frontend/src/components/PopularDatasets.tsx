import React from "react";
import type { Dataset } from "../types/types";
import DatasetCard from "./DatasetCard";
import './PopularDatasets.scss';

interface PopularDatasetsProps {
  datasets: Dataset[];
}

const PopularDatasets: React.FC<PopularDatasetsProps> = ({ datasets }) => {
  if (datasets.length === 0) {
    return (
      <div className="popular-datasets">
        <h3 className="popular-datasets__title">Популярные датазитории</h3>
        <p className="popular-datasets__empty">Нет датазитории для отображения</p>
      </div>
    );
  }

  return (
    <div className="popular-datasets">
      <h3 className="popular-datasets__title">Популярные датазитории</h3>
      <div className="popular-datasets__list">
        {datasets.map((dataset) => (
          <DatasetCard key={dataset.id} dataset={dataset} showCreator={true} />
        ))}
      </div>
    </div>
  );
};

export default PopularDatasets;
