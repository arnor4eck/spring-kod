import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import ProfileHeader from "../components/ProfileHeader";
import DatasetCard from "../components/DatasetCard";
import { favoriteAPI } from "../services/api";
import { authService } from "../services/authService";
import type { Dataset, User } from "../types/types";
import type { apiError } from "../types/apiError";
import './StarsPage.scss'

const StarsPage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [favorites, setFavorites] = useState<Dataset[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  const fetchFavorites = async () => {
    try {
      const data = await favoriteAPI.getMyFavorites();
      setFavorites(data);
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка загрузки избранного");
      }
    }
  };

  useEffect(() => {
    let isMounted = true;

    if (!authService.isAuthenticated()) {
      navigate("/login");
      return;
    }

    const fetchUser = async () => {
      try {
        const userData = await authService.fetchCurrentUser();
        if (isMounted && userData && userData.id !== -1) {
          setUser(userData);
        }
      } catch (error) {
        console.error("Ошибка загрузки пользователя", error);
      }
    };

    const loadData = async () => {
      setIsLoading(true);
      await Promise.all([fetchUser(), fetchFavorites()]);
      if (isMounted) {
        setIsLoading(false);
      }
    };

    loadData();

    return () => {
      isMounted = false;
    };
  }, [navigate]);

  const handleRemoveFromFavorites = async (datasetId: number) => {
    try {
      await favoriteAPI.removeFromFavorites(datasetId);
      setFavorites((prev) =>
        prev.filter((dataset) => dataset.id !== datasetId),
      );
      toast.success("Датазиторий удалён из избранного");
    } catch (error) {
      const apiError = error as apiError;
      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка");
      }
    }
  };

  const filteredFavorites = favorites.filter(
    (dataset) =>
      dataset.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      dataset.description.toLowerCase().includes(searchQuery.toLowerCase()),
  );

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
    <div className="stars-page">
      <ProfileHeader
        username={user.username}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="stars-page__content">
        <div className="stars-page__header">
          <h1 className="stars-page__title">Избранные датазитории</h1>
        </div>

        {filteredFavorites.length === 0 ? (
          <p className="stars-page__empty">
            {searchQuery
              ? `Ничего не найдено по запросу "${searchQuery}"`
              : "У вас пока нет избранных датазиториев"}
          </p>
        ) : (
          <div className="stars-page__list">
            {filteredFavorites.map((dataset) => (
              <DatasetCard
                key={dataset.id}
                dataset={dataset}
                showCreator={true}
                onStarClick={() => handleRemoveFromFavorites(dataset.id)}
                isStarred={true}
                showAnalyticsButton={true}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default StarsPage;
