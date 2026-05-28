import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import DatasetNavigation from "../components/DatasetNavigation";
import { authService } from "../services/authService";
import { datasetAPI } from "../services/api";
import type { Dataset } from "../types/types";
import type { apiError } from "../types/apiError";
import ExportIcon from "../assets/icons/Export.svg";
import "./CollaboratorsPage.scss";

interface Collaborator {
  id: number;
  email: string;
  username: string;
  role: "admin" | "ml_engineer" | "labeler" | "expert" | "analyst";
}

const CollaboratorsPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const datasetId = Number(id);

  const [user, setUser] = useState<{ username: string } | null>(null);
  const [dataset, setDataset] = useState<Dataset | null>(null);
  const [collaborators, setCollaborators] = useState<Collaborator[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [isExporting, setIsExporting] = useState(false);

  const [newCollaboratorEmail, setNewCollaboratorEmail] = useState("");
  const [newCollaboratorRole, setNewCollaboratorRole] =
    useState<Collaborator["role"]>("labeler");

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

    const fetchCollaborators = async () => {
      setCollaborators([
        { id: 1, email: "owner@example.com", username: "owner", role: "admin" },
        {
          id: 2,
          email: "ml@example.com",
          username: "ml_engineer",
          role: "ml_engineer",
        },
      ]);
    };

    fetchUser();
    fetchDataset();
    fetchCollaborators();
  }, [navigate, datasetId]);

  const handleAddCollaborator = async () => {
    if (!newCollaboratorEmail.trim()) {
      toast.error("Введите email");
      return;
    }

    setIsLoading(true);
    try {
      setCollaborators([
        ...collaborators,
        {
          id: Date.now(),
          email: newCollaboratorEmail,
          username: newCollaboratorEmail.split("@")[0],
          role: newCollaboratorRole,
        },
      ]);
      setNewCollaboratorEmail("");
      toast.success("Соавтор добавлен");
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка добавления соавтора");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemoveCollaborator = async (collaboratorId: number) => {
    setIsLoading(true);
    try {
      setCollaborators(collaborators.filter((c) => c.id !== collaboratorId));
      toast.success("Соавтор удалён");
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка удаления соавтора");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleChange = async (
    collaboratorId: number,
    newRole: Collaborator["role"],
  ) => {
    setIsLoading(true);
    try {
      setCollaborators(
        collaborators.map((c) =>
          c.id === collaboratorId ? { ...c, role: newRole } : c,
        ),
      );
      toast.success("Роль обновлена");
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка обновления роли");
      }
    } finally {
      setIsLoading(false);
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
    <div className="collaborators-page">
      <ProfileHeader
        username={user?.username || "Пользователь"}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="collaborators-page__content">
        <div className="collaborators-page__header">
          <button
            className="collaborators-page__back-btn"
            onClick={() => navigate(`/profile/datasets/${datasetId}/analytics`)}
            type="button"
          >
            ← Назад
          </button>
          <div className="collaborators-page__title-wrapper">
            <h1 className="collaborators-page__title">{dataset.name}</h1>
            <span className="collaborators-page__visibility">
              {visibilityText}
            </span>
          </div>
          <div className="collaborators-page__actions">
            <button
              className="collaborators-page__action-btn"
              onClick={() =>
                navigate(`/profile/datasets/${datasetId}/analytics`)
              }
            >
              Аналитика
            </button>
            <button className="collaborators-page__action-btn collaborators-page__action-btn--active">
              Работа с датасетом
            </button>
            <button
              className="collaborators-page__export-btn"
              onClick={handleExport}
              disabled={isExporting}
            >
              <img src={ExportIcon} alt="Export icon" />
            </button>
          </div>
        </div>

        <DatasetNavigation datasetId={datasetId} />

        <div className="collaborators-page__form">
          <h2 className="collaborators-page__section-title">
            Добавление соавтора
          </h2>

          <div className="collaborators-page__add-form">
            <div className="collaborators-page__field">
              <label className="collaborators-page__label">Email</label>
              <input
                type="email"
                className="collaborators-page__input"
                placeholder="user@example.com"
                value={newCollaboratorEmail}
                onChange={(e) => setNewCollaboratorEmail(e.target.value)}
                disabled={isLoading}
              />
            </div>

            <div className="collaborators-page__field">
              <label className="collaborators-page__label">Роль</label>
              <select
                className="collaborators-page__select"
                value={newCollaboratorRole}
                onChange={(e) =>
                  setNewCollaboratorRole(e.target.value as Collaborator["role"])
                }
                disabled={isLoading}
              >
                <option value="admin">Администратор</option>
                <option value="ml_engineer">ML-инженер</option>
                <option value="labeler">Разметчик</option>
                <option value="expert">Эксперт</option>
                <option value="analyst">Аналитик</option>
              </select>
            </div>

            <button
              className="collaborators-page__add-btn"
              onClick={handleAddCollaborator}
              disabled={isLoading}
            >
              Добавить
            </button>
          </div>
        </div>

        <div className="collaborators-page__list">
          <h2 className="collaborators-page__section-title">
            Список соавторов
          </h2>

          <div className="collaborators-page__table-wrapper">
            <table className="collaborators-page__table">
              <thead>
                <tr>
                  <th>Пользователь</th>
                  <th>Email</th>
                  <th>Роль</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {collaborators.map((collaborator) => (
                  <tr key={collaborator.id}>
                    <td>{collaborator.username}</td>
                    <td>{collaborator.email}</td>
                    <td>
                      <select
                        className="collaborators-page__role-select"
                        value={collaborator.role}
                        onChange={(e) =>
                          handleRoleChange(
                            collaborator.id,
                            e.target.value as Collaborator["role"],
                          )
                        }
                        disabled={isLoading}
                      >
                        <option value="admin">Администратор</option>
                        <option value="ml_engineer">ML-инженер</option>
                        <option value="labeler">Разметчик</option>
                        <option value="expert">Эксперт</option>
                        <option value="analyst">Аналитик</option>
                      </select>
                    </td>
                    <td>
                      <button
                        className="collaborators-page__remove-btn"
                        onClick={() =>
                          handleRemoveCollaborator(collaborator.id)
                        }
                        disabled={isLoading}
                      >
                        Удалить
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CollaboratorsPage;
