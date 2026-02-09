import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ModalBase from './ModalBase.jsx';

import '../../styles/CM_99_popup.css';

const CM_99_1000 = ({ isOpen, onClose }) => {
  const navigate = useNavigate();

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);
  if (!isOpen) return null;

  return (
    <ModalBase onClose={onClose}>
      <div className="text-center d-flex flex-column">
        <p className="fs-4 fw-semibold mb-4">
          会員のみご利用いただけます。
          <br />
          会員登録またはログイン後にご利用ください。
        </p>
        <div className="d-flex flex-column align-items-center gap-2 w-100">
          <button className="popup-secondary" onClick={() => navigate('/login')}>
            ログイン
          </button>
          <button className="btn-action" onClick={() => navigate('/register')}>
            会員登録
          </button>
        </div>
      </div>
    </ModalBase>
  );
};

export default CM_99_1000;
