import ModalBase from './ModalBase.jsx';
import '../../styles/CM_99_popup.css';

const CM_99_1003 = ({
  isOpen,
  onClose,
  message = 'エラーが発生しました。',
  errorMessage,
}) => {
  if (!isOpen) return null;
  const displayMessage = errorMessage || message;

  return (
    <ModalBase onClose={onClose}>
      <p className="fs-4 fw-semibold mb-4">{displayMessage}</p>
      <div className="d-flex flex-column align-items-center gap-2 w-100">
        <button className="btn-error" onClick={onClose}>
          閉じる
        </button>
      </div>
    </ModalBase>
  );
};

export default CM_99_1003;
