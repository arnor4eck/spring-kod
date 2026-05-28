import React from "react";
import type { BrokenFile } from "../types/types";
import './BrokenFilesModal.scss';

interface BrokenFilesModalProps {
  isOpen: boolean;
  brokenFiles: BrokenFile[];
  onClose: () => void;
  onConfirm: () => void;
}

const BrokenFilesModal: React.FC<BrokenFilesModalProps> = ({
  isOpen,
  brokenFiles,
  onClose,
  onConfirm,
}) => {
  if (!isOpen) return null;

  return (
    <div className="broken-files-modal">
      <div className="broken-files-modal__overlay">
        <div className="broken-files-modal__content">
          <div className="broken-files-modal__icon"></div>
          <h3 className="broken-files-modal__title">Обнаружены битые файлы</h3>
          <p className="broken-files-modal__message">
            Следующие файлы повреждены и не будут использоваться в обработке:
          </p>
          <div className="broken-files-modal__list">
            {brokenFiles.map((file, index) => (
              <div key={index} className="broken-files-modal__item">
                <span className="broken-files-modal__filename">
                  {file.name}
                </span>
                <span className="broken-files-modal__reason">
                  {file.reason}
                </span>
              </div>
            ))}
          </div>
          <div className="broken-files-modal__actions">
            <button
              className="broken-files-modal__cancel-btn"
              onClick={onClose}
            >
              Отмена
            </button>
            <button
              className="broken-files-modal__confirm-btn"
              onClick={onConfirm}
            >
              Продолжить
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BrokenFilesModal;
