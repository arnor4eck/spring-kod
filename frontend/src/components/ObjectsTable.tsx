import React, { useState, useMemo } from "react";
import type { AllObject } from "../types/types";
import "./ObjectsTable.scss";

interface ObjectsTableProps {
  data: AllObject[];
}

const ObjectsTable: React.FC<ObjectsTableProps> = ({ data }) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [sortField, setSortField] = useState<keyof AllObject | null>(null);
  const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const handleSort = (field: keyof AllObject) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  const filteredData = useMemo(() => {
    if (!searchQuery) return data;
    return data.filter((item) =>
      item.tags?.some((tag) =>
        tag.toLowerCase().includes(searchQuery.toLowerCase()),
      ),
    );
  }, [data, searchQuery]);

  const sortedData = useMemo(() => {
    if (!sortField) return filteredData;
    return [...filteredData].sort((a, b) => {
      const aVal = a[sortField];
      const bVal = b[sortField];
      if (typeof aVal === "number" && typeof bVal === "number") {
        return sortDirection === "asc" ? aVal - bVal : bVal - aVal;
      }
      const aString = String(aVal);
      const bString = String(bVal);
      return sortDirection === "asc"
        ? aString.localeCompare(bString)
        : bString.localeCompare(aString);
    });
  }, [filteredData, sortField, sortDirection]);

  const totalPages = Math.ceil(sortedData.length / itemsPerPage);
  const paginatedData = sortedData.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage,
  );

  return (
    <div className="objects-table">
      <div className="objects-table__toolbar">
        <div className="objects-table__search-icon"></div>
        <input
          type="text"
          className="objects-table__search"
          placeholder="Поиск по тегам..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>
      <div className="objects-table__scroll-wrapper">
        <table className="objects-table__table">
          <thead>
            <tr>
              <th onClick={() => handleSort("url")}>
                Объект
                {sortField === "url" && (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
              <th onClick={() => handleSort("tags")}>
                Теги
                {sortField === "tags" &&
                  (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
              <th onClick={() => handleSort("entropy")}>
                Энтропия
                {sortField === "entropy" &&
                  (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
              <th onClick={() => handleSort("confidence")}>
                Уверенность модели
                {sortField === "confidence" &&
                  (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
              <th onClick={() => handleSort("label_score")}>
                Вероятность ошибки разметки
                {sortField === "label_score" &&
                  (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
              <th onClick={() => handleSort("outlier_score")}>
                Новизна
                {sortField === "outlier_score" &&
                  (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
              <th onClick={() => handleSort("utility_score")}>
                Полезность для дообучения
                {sortField === "utility_score" &&
                  (sortDirection === "asc" ? " ↑" : " ↓")}
              </th>
            </tr>
          </thead>
          <tbody>
            {paginatedData.map((item, index) => (
              <tr key={index}>
                <td>
                  <img
                    src={item.url}
                    alt={`object-${index}`}
                    className="objects-table__thumbnail"
                  />
                </td>
                <td>{item.tags?.join(", ")}</td>
                <td>{item.entropy}</td>
                <td>{item.confidence}</td>
                <td>{item.label_score}</td>
                <td>{item.outlier_score}</td>
                <td>{item.utility_score}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <div className="objects-table__pagination">
          <button
            className="objects-table__pagination-prev"
            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            disabled={currentPage === 1}
          >
            Назад
          </button>
          <span className="objects-table__pagination-info">
            Страница {currentPage} из {totalPages}
          </span>
          <button
            className="objects-table__pagination-next"
            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            disabled={currentPage === totalPages}
          >
            Вперёд
          </button>
        </div>
      )}
    </div>
  );
};

export default ObjectsTable;
