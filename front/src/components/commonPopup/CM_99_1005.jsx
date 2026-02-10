import { useNavigate } from 'react-router-dom';
import ModalBase from './ModalBase.jsx';
import '../../styles/CM_99_popup.css';

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
      <p className="fs-4 fw-semibold mb-4 text-center">ログインが必要です</p>
      <p className="fs-5 mb-4 text-center">
        会員のみご利用いただけるサービスです。
        <br />
        ログイン後にご利用ください。
      </p>
      <div className="d-flex justify-content-center w-100">
        <button className="btn-action" onClick={() => handleNavigate('/login')}>
          ログイン
        </button>
      </div>
    </ModalBase>
  );
};

export default CM_99_1005;
