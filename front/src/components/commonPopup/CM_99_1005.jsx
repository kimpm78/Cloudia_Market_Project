import { useNavigate } from 'react-router-dom';
import ModalBase from './ModalBase.jsx';

const CM_99_1005 = ({ isOpen, onClose }) => {
  const navigate = useNavigate();

  if (!isOpen) {
    return null;
  }

  const handleNavigate = (path) => {
    onClose();
    navigate(path);
  };

  return (
    <ModalBase onClose={onClose}>
      <div className="popup-content-wrapper">
        <h4 className="popup-title">ログインが必要です</h4>
        <p className="popup-message">
          会員のみご利用いただけるサービスです。
          <br />
          ログイン後にご利用ください。
        </p>
        <div className="popup-actions-horizontal">
          <button className="btn-action" onClick={() => handleNavigate('/login')}>
            ログイン
          </button>
        </div>
      </div>
    </ModalBase>
  );
};

export default CM_99_1005;
