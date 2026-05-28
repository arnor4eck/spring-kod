import React from "react";
import type { RoadmapItem } from "../types/types";
import "./RoadmapBlock.scss";

interface RoadmapBlockProps {
  roadmap: RoadmapItem[];
}

const RoadmapBlock: React.FC<RoadmapBlockProps> = ({ roadmap }) => {
  return (
    <div className="roadmap-block">
      <div className="roadmap-block__icon"></div>
      <div className="roadmap-block__content">
        {roadmap.length === 0 ? (
          <p className="roadmap-block__text">Нет рекомендаций</p>
        ) : (
          <ul className="roadmap-block__list">
            {roadmap.map((item) => (
              <li key={item.id} className="roadmap-block__item">
                {item.action}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default RoadmapBlock;
