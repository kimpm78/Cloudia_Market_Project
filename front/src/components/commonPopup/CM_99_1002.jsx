import { useEffect } from 'react';
import ModalBase from './ModalBase.jsx';
import '../../styles/CM_99_popup.css';

const CM_99_1002 = ({ isOpen, onClose, duration }) => {
  useEffect(() => {
    if (isOpen && duration && onClose) {
      const timer = setTimeout(() => {
        onClose();
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [isOpen, duration, onClose]);

  if (!isOpen) return null;

  return (
    <ModalBase onClose={onClose} hideClose={true}>
      <div className="cm-loading-popup text-center py-4">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <div className="fs-4 fw-semibold m-4">処理中です。しばらくお待ちください…</div>
      </div>
    </ModalBase>
  );
};

export default CM_99_1002;
