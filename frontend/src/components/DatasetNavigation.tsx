import React from "react";
import { NavLink } from "react-router-dom";
import './DatasetNavigation.scss';

interface DatasetNavigationProps {
  datasetId: number;
}

const DatasetNavigation: React.FC<DatasetNavigationProps> = ({ datasetId }) => {
  const basePath = `/profile/datasets/${datasetId}`;

  return (
    <div className="dataset-navigation">
      <NavLink
        to={`${basePath}/edit`}
        className={({ isActive }) =>
          isActive
            ? "dataset-navigation__link dataset-navigation__link--active"
            : "dataset-navigation__link"
        }
      >
        Датасет
      </NavLink>
      <NavLink
        to={`${basePath}/collaborators`}
        className={({ isActive }) =>
          isActive
            ? "dataset-navigation__link dataset-navigation__link--active"
            : "dataset-navigation__link"
        }
      >
        Соавторы
      </NavLink>
      <NavLink
        to={`${basePath}/controversial`}
        className={({ isActive }) =>
          isActive
            ? "dataset-navigation__link dataset-navigation__link--active"
            : "dataset-navigation__link"
        }
      >
        Спорные метки
      </NavLink>
      <NavLink
        to={`${basePath}/duplicates`}
        className={({ isActive }) =>
          isActive
            ? "dataset-navigation__link dataset-navigation__link--active"
            : "dataset-navigation__link"
        }
      >
        Дубликаты
      </NavLink>
      <NavLink
        to={`${basePath}/poor-quality`}
        className={({ isActive }) =>
          isActive
            ? "dataset-navigation__link dataset-navigation__link--active"
            : "dataset-navigation__link"
        }
      >
        Файлы плохого качества
      </NavLink>
    </div>
  );
};

export default DatasetNavigation;
