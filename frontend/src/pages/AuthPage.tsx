import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import toast from "react-hot-toast";
import { authService } from "../services/authService";
import { useAuthStore } from "../store/authStore";
import type { apiError } from "../types/apiError";
import EyeOpen from "../assets/icons/EyeOpen.svg";
import EyeClose from "../assets/icons/EyeClose.svg";
import './AuthPage.scss';

const AuthPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await authService.login({ email, password });
      toast.success("Вход выполнен успешно!");
      login();
      navigate("/profile");
    } catch (error) {
      const apiError = error as apiError;

      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error("Ошибка входа");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-page__container">
        <h2 className="auth-page__title">Вход</h2>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-form__field">
            <label htmlFor="email" className="auth-form__label">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="auth-form__input"
              placeholder="Введите email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>

          <div className="auth-form__password-wrapper">
            <label htmlFor="password" className="auth-form__label">
              Пароль
            </label>
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              className="auth-form__input auth-form__input--with-icon"
              placeholder="Введите пароль"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={isLoading}
            />
            <button
              type="button"
              className="auth-form__password-toggle"
              onClick={togglePasswordVisibility}
              tabIndex={-1}
            >
              {showPassword ? (
                <img src={EyeOpen} alt="Скрыть пароль" />
              ) : (
                <img src={EyeClose} alt="Показать пароль" />
              )}
            </button>
          </div>

          <button
            type="submit"
            className="auth-form__button"
            disabled={isLoading}
          >
            {isLoading ? "Вход..." : "Войти"}
          </button>
        </form>
        <p className="auth-page__subtitle">
          Ещё нет аккаунта?{" "}
          <Link to="/register" className="auth-page__link">
            Зарегистрируйтесь
          </Link>
        </p>
      </div>
    </div>
  );
};

export default AuthPage;
