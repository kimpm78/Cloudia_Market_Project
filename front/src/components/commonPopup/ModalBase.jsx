const ModalBase = ({ children, onClose, hideClose = false }) => (
  <div className="popup-backdrop" onClick={onClose}>
    <div className="popup-box" onClick={(e) => e.stopPropagation()}>
      {!hideClose && (
        <button className="popup-close" onClick={onClose}>
          ×
        </button>
      )}
      {children}
    </div>
  </div>
);

export default ModalBase;