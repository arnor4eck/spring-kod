import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { userAPI } from "../services/api";
import { authService } from "../services/authService";
import ProfileHeader from "../components/ProfileHeader";
import MyDatasetsList from "../components/MyDatasetsList";
import type { User, Dataset } from "../types/types";
import type { apiError } from "../types/apiError";
import './MyDatasetsPage.scss'

const MyDatasetsPage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [datasets, setDatasets] = useState<Dataset[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      if (!isMounted) return;

      try {
        const [userData, datasetsData] = await Promise.all([
          userAPI.getMe(),
          userAPI.getMyDatasets(),
        ]);

        if (isMounted) {
          setUser(userData);
          setDatasets(datasetsData);
          setIsLoading(false);
        }
      } catch (error) {
        if (!isMounted) return;

        const apiError = error as apiError;
        if (apiError.messages) {
          apiError.messages.forEach((msg) => toast.error(msg));
        } else {
          toast.error("Ошибка загрузки");
        }
        if (apiError.code === 401) {
          authService.logout();
          navigate("/login");
        }
        setIsLoading(false);
      }
    };

    if (!authService.isAuthenticated()) {
      navigate("/login");
      return;
    }

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [navigate]);

  const handleDatasetDelete = (deletedId: number) => {
    setDatasets((prevDatasets) =>
      prevDatasets.filter((dataset) => dataset.id !== deletedId),
    );
  };

  const filteredDatasets = datasets.filter(
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
    <div className="my-datasets-page">
      <ProfileHeader
        username={user.username}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      <div className="my-datasets-page__content">
        <MyDatasetsList
          datasets={filteredDatasets}
          searchQuery={searchQuery}
          onDatasetDelete={handleDatasetDelete}
          
        />
      </div>
    </div>
  );
};

export default MyDatasetsPage;
