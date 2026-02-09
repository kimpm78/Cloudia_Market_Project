import ModalBase from './ModalBase.jsx';
import '../../styles/CM_99_popup.css';

const CM_99_1001 = ({ isOpen, onClose, onConfirm, Message }) => {
  if (!isOpen) return null;

  return (
    <ModalBase onClose={onClose}>
      <div className="fs-4 fw-semibold mb-5">{Message}</div>
      <div className="d-flex flex-column flex-sm-row gap-2">
        <button className="popup-secondary popup-btn w-100 w-sm-auto" onClick={onClose}>
          キャンセル
        </button>
        <button className="btn-action popup-btn w-100 w-sm-auto" onClick={onConfirm}>
          確認
        </button>
      </div>
    </ModalBase>
  );
};

export default CM_99_1001;
