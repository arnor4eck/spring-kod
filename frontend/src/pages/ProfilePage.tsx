import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { userAPI } from "../services/api";
import { authService } from "../services/authService";
import ProfileHeader from "../components/ProfileHeader";
import ProfileInfo from "../components/ProfileInfo";
import PopularDatasets from "../components/PopularDatasets";
import Settings from "../components/Settings";
import type { User, Dataset } from "../types/types";
import type { apiError } from "../types/apiError";
import './ProfilePage.scss';

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [datasets, setDatasets] = useState<Dataset[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate("/login");
      return;
    }

    const fetchData = async () => {
      setIsLoading(true);
      try {
        const [userData, datasetsData] = await Promise.all([
          userAPI.getMe(),
          userAPI.getMyDatasets(),
        ]);
        setUser(userData);
        setDatasets(datasetsData);
      } catch (error) {
        const apiError = error as apiError;
        if (apiError.messages) {
          apiError.messages.forEach((msg) => toast.error(msg));
        } else {
          toast.error("Ошибка загрузки профиля");
        }
        if (apiError.code === 401) {
          authService.logout();
          navigate("/login");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [navigate]);

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString("ru-RU", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const filteredDatasets = datasets.filter(
    (dataset) =>
      dataset.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      dataset.description.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const popularDatasets = filteredDatasets.slice(0, 3);

  if (isLoading) {
    return (
      <div className="loading">
        <div className="loading__container">
          <div className="loading__spinner"></div>
          <p className="loading__message">Загрузка...</p>
        </div>
      </div>
    );
  }

  const handleLogout = () => {
    authService.logout();
    toast.success("Вы вышли из системы");
    navigate("/login");
  };

  if (!user) {
    return (
      <div className="error">
        <div className="error__container">
          <p className="error__message">
            Не удалось загрузить данные. Перезагрузите или перезайдите в аккаунт.
          </p>
          <button className="error__button" onClick={handleLogout}>
            Выйти
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <ProfileHeader
        username={user.username}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="profile-page__content">
        <div className="profile-page__main">
          <ProfileInfo
            username={user.username}
            email={user.email}
            createdAt={formatDate(user.createdAt)}
          />

          <PopularDatasets datasets={popularDatasets} />
        </div>

        <div className="profile-page__sidebar">
          <Settings />
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
