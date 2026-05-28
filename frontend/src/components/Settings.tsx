import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { authService } from "../services/authService";
import './Settings.scss';

const Settings: React.FC = () => {
  const navigate = useNavigate();
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);

  const handleChangePassword = () => {
    toast.success("Функция в разработке");
  };

  const handleChangeEmail = () => {
    toast.success("Функция в разработке");
  };

  const handleDeleteAccount = () => {
    if (showConfirmDelete) {
      toast.error("Функция в разработке");
      setShowConfirmDelete(false);
    } else {
      setShowConfirmDelete(true);
    }
  };

  const handleCancelDelete = () => {
    setShowConfirmDelete(false);
  };

  const handleLogout = () => {
    authService.logout();
    toast.success("Вы вышли из системы");
    navigate("/login");
  };

  return (
    <div className="settings">
      <h3 className="settings__title">Настройки</h3>

      <div className="settings__item">
        <button className="settings__btn" onClick={handleChangePassword}>
          Сменить пароль
        </button>
      </div>

      <div className="settings__item">
        <button className="settings__btn" onClick={handleChangeEmail}>
          Сменить email
        </button>
      </div>

      <div className="settings__item">
        <button
          className="settings__btn settings__btn--logout"
          onClick={handleLogout}
        >
          Выйти
        </button>
      </div>

      <div className="settings__item">
        {!showConfirmDelete ? (
          <button
            className="settings__btn settings__btn--danger"
            onClick={handleDeleteAccount}
          >
            Удалить аккаунт
          </button>
        ) : (
          <div className="settings__confirm">
            <p className="settings__confirm-text">
              Вы уверены? Это действие необратимо.
            </p>
            <div className="settings__confirm-buttons">
              <button
                className="settings__btn settings__btn--danger"
                onClick={handleDeleteAccount}
              >
                Да, удалить
              </button>
              <button className="settings__btn" onClick={handleCancelDelete}>
                Отмена
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Settings;
