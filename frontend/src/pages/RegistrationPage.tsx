import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import toast from "react-hot-toast";
import { registrationService } from "../services/registrationService";
import type { apiError } from "../types/apiError";
import EyeOpen from "../assets/icons/EyeOpen.svg";
import EyeClose from "../assets/icons/EyeClose.svg";
import "./RegistrationPage.scss";

const RegistrationPage: React.FC = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await registrationService.registration({ username, email, password });

      toast.success("Регистрация успешна! Теперь войдите в систему");
      navigate("/login");
    } catch (error) {
      const apiError = error as apiError;

      if (apiError.messages && apiError.messages.length > 0) {
        apiError.messages.forEach((msg: string) => toast.error(msg));
      } else {
        toast.error(`Ошибка ${apiError.code || "регистрации"}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="registration-page">
      <div className="registration-page__container">
        <h2 className="registration-page__title">Регистрация</h2>

        <form className="registration-form" onSubmit={handleSubmit}>
          <div className="registration-form__field">
            <label htmlFor="username" className="registration-form__label">
              Имя пользователя
            </label>
            <input
              id="username"
              type="text"
              className="registration-form__input"
              placeholder="Введите имя пользователя"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>

          <div className="registration-form__field">
            <label htmlFor="email" className="registration-form__label">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="registration-form__input"
              placeholder="Введите email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>

          <div className="registration-form__field">
            <label htmlFor="password" className="registration-form__label">
              Пароль
            </label>
            <div className="registration-form__password-wrapper">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                className="registration-form__input registration-form__input--with-icon"
                placeholder="Введите пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={isLoading}
              />
              <button
                type="button"
                className="registration-form__password-toggle"
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
          </div>

          <button
            type="submit"
            className="registration-form__button"
            disabled={isLoading}
          >
            {isLoading ? "Регистрация..." : "Зарегистрироваться"}
          </button>
        </form>
        <p className="registration-page__subtitle">
          Уже есть аккаунт?{" "}
          <Link to="/login" className="registration-page__link">
            Войти
          </Link>
        </p>
      </div>
    </div>
  );
};

export default RegistrationPage;
