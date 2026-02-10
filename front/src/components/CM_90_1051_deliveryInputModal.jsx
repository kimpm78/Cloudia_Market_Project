import { useState } from 'react';

const CM_90_1051_deliveryInputModal = ({ show, onHide, onConfirm, orderData }) => {
  const [trackingNumber, setTrackingNumber] = useState('');

  const handleConfirm = () => {
    if (!trackingNumber.trim()) {
      alert('追跡番号を入力してください。');
      return;
    }

    onConfirm({
      carrier: '日本郵便',
      trackingNumber: trackingNumber.trim(),
      orderData: orderData,
    });

    setTrackingNumber('');
    onHide();
  };

  const handleCancel = () => {
    setTrackingNumber('');
    onHide();
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleConfirm();
    }
  };

  if (!show) return null;

  return (
    <>
      <div
        className="modal-backdrop fade show"
        style={{ display: 'block' }}
        onClick={handleCancel}
      ></div>

      <div className="modal fade show" style={{ display: 'block' }} tabIndex="-1" role="dialog">
        <div className="modal-dialog modal-dialog-centered modal-md" role="document">
          <div className="modal-content shadow-sm rounded-3">
            <div className="modal-header border-0 pb-0">
              <h5 className="modal-title fw-bold">配送情報入力</h5>
              <button
                type="button"
                className="btn-close"
                onClick={handleCancel}
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body">
              <div className="mb-4">
                <label className="form-label fw-semibold">配送会社</label>
                <input
                  type="text"
                  className="form-control bg-light fw-bold"
                  value="日本郵便"
                  disabled
                  readOnly
                />
              </div>

              <div>
                <label className="form-label fw-semibold">追跡番号</label>
                <input
                  type="text"
                  className="form-control"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="追跡番号を入力してください"
                  autoFocus
                />
              </div>
            </div>

            <div className="modal-footer border-0">
              <button type="button" className="btn btn-secondary" onClick={handleCancel}>
                閉じる
              </button>
              <button type="button" className="btn btn-primary" onClick={handleConfirm}>
                確認
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default CM_90_1051_deliveryInputModal;
