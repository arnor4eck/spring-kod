import React from "react";
import './ProfileInfo.scss'

interface ProfileInfoProps {
  username: string;
  email: string;
  createdAt: string;
}

const ProfileInfo: React.FC<ProfileInfoProps> = ({
  username,
  email,
  createdAt,
}) => {
  return (
    <div className="profile-info">
    <div className="profile-info__content">
        <div className="profile-info__label">Имя пользователя</div>
        <div className="profile-info__value">{username}</div>
    </div>

    <div className="profile-info__content">
        <div className="profile-info__label">Email</div>
        <div className="profile-info__value">{email}</div>
    </div>

    <div className="profile-info__content">
        <div className="profile-info__label">Зарегистрирован</div>
        <div className="profile-info__value">{createdAt}</div>
    </div>
    </div>
  );
};

export default ProfileInfo;
