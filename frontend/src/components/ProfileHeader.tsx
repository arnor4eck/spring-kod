import React from "react";
import { NavLink } from "react-router-dom";
import UserIcon from "../assets/icons/Profile.svg";
import './ProfileHeader.scss';

interface ProfileHeaderProps {
  username: string;
  searchQuery: string;
  onSearchChange: (query: string) => void;
}

const ProfileHeader: React.FC<ProfileHeaderProps> = ({
  username,
  searchQuery,
  onSearchChange,
}) => {
  return (
    <header className="profile-header">
      <div className="profile-header__top">
        <div className="profile-header__user">
                  <span className="profile-header__user-icon">{<img src={UserIcon} alt="User icon" />}</span>
          <span className="profile-header__username">{username}</span>
        </div>
        <div className="profile-header__search">
          <input
            type="text"
            className="profile-header__search-input"
            placeholder="Поиск..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
          />
        </div>
      </div>

      <nav className="profile-header__nav">
        <NavLink
          to="/profile"
          end
          className={({ isActive }) =>
            isActive
              ? "profile-header__link profile-header__link--active"
              : "profile-header__link"
          }
        >
          Профиль
        </NavLink>
        <NavLink
          to="/profile/datasets"
          className={({ isActive }) =>
            isActive
              ? "profile-header__link profile-header__link--active"
              : "profile-header__link"
          }
        >
          Датазитории
        </NavLink>
        <NavLink
          to="/profile/stars"
          className={({ isActive }) =>
            isActive
              ? "profile-header__link profile-header__link--active"
              : "profile-header__link"
          }
        >
          Звёзды
        </NavLink>
      </nav>
    </header>
  );
};

export default ProfileHeader;
