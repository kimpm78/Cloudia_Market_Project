import ModalBase from './ModalBase.jsx';
import '../../styles/CM_99_popup.css';

const CM_99_1003 = ({ isOpen, onClose, message = '에러가 발생했습니다.' }) => {
  if (!isOpen) return null;

  return (
    <ModalBase onClose={onClose}>
      <p className="fs-4 fw-semibold mb-4">{message}</p>
      <div className="d-flex flex-column align-items-center gap-2 w-100">
        <button className="btn-error" onClick={onClose}>
          閉じる
        </button>
      </div>
    </ModalBase>
  )``;
};

export default CM_99_1003;
