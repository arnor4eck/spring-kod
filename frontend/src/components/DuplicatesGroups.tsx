import React from "react";
import type { DuplicateGroup } from "../types/types";
import "./DublicatesGroups.scss";

interface DuplicatesGroupsProps {
  groups: DuplicateGroup[];
}

const DuplicatesGroups: React.FC<DuplicatesGroupsProps> = ({ groups }) => {
  return (
    <div className="duplicates-groups">
      <div className="duplicates-groups__scroll-wrapper">
        {groups.map((group) => (
          <div key={group.group_id} className="duplicates-groups__group">
            <div className="duplicates-groups__group-header">
              <span className="duplicates-groups__group-name">
                Группа дубликатов #{group.group_id}
              </span>
              <span className="duplicates-groups__group-count">
                ({group.copies.length + 1} фото)
              </span>
            </div>
            <div className="duplicates-groups__group-content">
              <div className="duplicates-groups__images">
                <div className="duplicates-groups__image-wrapper duplicates-groups__image-wrapper--primary">
                  <img
                    src={group.primary.url}
                    alt={`primary-${group.group_id}`}
                    className="duplicates-groups__thumbnail duplicates-groups__thumbnail--primary"
                  />
                  <span className="duplicates-groups__primary-badge">
                    Главный
                  </span>
                </div>
                {group.copies.map((copy, idx) => (
                  <img
                    key={idx}
                    src={copy.url}
                    alt={`copy-${group.group_id}-${idx}`}
                    className="duplicates-groups__thumbnail"
                  />
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DuplicatesGroups;
