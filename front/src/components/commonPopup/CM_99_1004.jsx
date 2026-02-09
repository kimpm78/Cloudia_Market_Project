import ModalBase from './ModalBase.jsx';
import '../../styles/CM_99_popup.css';

const CM_99_1004 = ({ isOpen, onClose, Message }) => {
  if (!isOpen) return null;

  return (
    <ModalBase onClose={onClose}>
      <p
        className="fs-4 fw-semibold mb-4 text-center"
        dangerouslySetInnerHTML={{ __html: Message }}
      ></p>
      <div className="d-flex justify-content-center w-100">
        <button className="btn-action" onClick={onClose}>
          確認
        </button>
      </div>
    </ModalBase>
  );
};

export default CM_99_1004;
