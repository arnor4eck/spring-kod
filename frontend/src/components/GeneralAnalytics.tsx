import React from "react";
import type { Summary, ClassInfo } from "../types/types";
import ClassPieChart from "./ClassPieChart";
import "./GeneralAnalytics.scss";

interface GeneralAnalyticsProps {
  summary: Summary;
}

const GeneralAnalytics: React.FC<GeneralAnalyticsProps> = ({ summary }) => {
  const classData = summary.classes.map((cls: ClassInfo) => ({
    name: cls.name,
    value: cls.percentage,
    count: cls.count,
    deficit: cls.deficit,
  }));

  return (
    <div className="general-analytics">
      <div className="general-analytics__stats">
        <div className="general-analytics__stat">
          <span className="general-analytics__stat-label">
            Уровень готовности
          </span>
          <span className="general-analytics__stat-value">
            {summary.readiness}%
          </span>
        </div>
        <div className="general-analytics__stat">
          <span className="general-analytics__stat-label">
            Количество объектов
          </span>
          <span className="general-analytics__stat-value">
            {summary.n_total}
          </span>
        </div>
        <div className="general-analytics__stat">
          <span className="general-analytics__stat-label">
            Количество классов
          </span>
          <span className="general-analytics__stat-value">
            {summary.n_classes}
          </span>
        </div>
      </div>
      <div className="general-analytics__charts">
        <ClassPieChart data={classData} />
      </div>
    </div>
  );
};

export default GeneralAnalytics;
