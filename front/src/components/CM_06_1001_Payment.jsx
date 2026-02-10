import { useEffect, useState } from 'react';

function CM_06_1001_Payment({
  paymentMethod = { code: 'CARD', label: 'カード決済' },
  paymentOptions = [],
  onSelectPaymentMethod = () => {},
}) {
  const [pendingMethodCode, setPendingMethodCode] = useState(paymentMethod?.code || 'CARD');
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  useEffect(() => {
    setPendingMethodCode(paymentMethod?.code || 'CARD');
  }, [paymentMethod]);

  const openPaymentModal = () => {
    setPendingMethodCode(paymentMethod.code);
    setShowPaymentModal(true);
  };

  const closePaymentModal = () => {
    setShowPaymentModal(false);
  };

  const confirmPaymentOption = () => {
    const selectedOption =
      paymentOptions.find((option) => option.code === pendingMethodCode) || paymentOptions[0];
    onSelectPaymentMethod(selectedOption);
    setShowPaymentModal(false);
  };

  return (
    <>
      <h5 className="border-bottom pb-2 mt-4 fw-semibold">決済方法入力</h5>
      <div className="py-2 d-flex justify-content-between align-items-center">
        <strong>決済方法</strong>
      </div>
      <div className="border-bottom py-2 d-flex justify-content-between align-items-center">
        <div className="flex-grow-1 d-flex align-items-center gap-2 flex-wrap">
          <span>{paymentMethod.label}</span>
          {paymentMethod.code === 'CARD' && (
            <span className="d-flex align-items-center gap-2">
              <img
                src="https://img.icons8.com/color/48/visa.png"
                alt="Visa"
                style={{ height: '22px' }}
              />
              <img
                src="https://img.icons8.com/color/48/mastercard-logo.png"
                alt="MasterCard"
                style={{ height: '22px' }}
              />
            </span>
          )}
        </div>
        <div>
          <button
            className="btn btn-primary btn-sm"
            style={{ padding: '5px 20px' }}
            onClick={openPaymentModal}
          >
            編集
          </button>
        </div>
      </div>

      {showPaymentModal && (
        <div
          className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
          style={{
            backgroundColor: 'rgba(0, 0, 0, 0.45)',
            backdropFilter: 'blur(4px)',
            zIndex: 1050,
            padding: '1.5rem',
          }}
        >
          <div
            className="bg-white p-4 shadow-lg"
            style={{
              width: '600px',
              boxShadow: '0 28px 60px rgba(15, 23, 42, 0.28)',
            }}
          >
            <div className="d-flex justify-content-between align-items-start mb-4">
              <div>
                <h5 className="m-0 fw-semibold">決済方法を選択</h5>
                <small className="text-muted">ご希望の決済手段を選択してください。</small>
              </div>
              <button type="button" className="btn-close" onClick={closePaymentModal} />
            </div>
            <div className="row g-3">
              {paymentOptions.map((option) => {
                const isSelected = pendingMethodCode === option.code;
                return (
                  <div className="col-12 col-md-6" key={option.code}>
                    <label
                      className={`card h-100 p-3 ${isSelected ? 'border-primary shadow-sm' : 'border-light'}`}
                      style={{ cursor: 'pointer' }}
                    >
                      <input
                        type="radio"
                        className="form-check-input visually-hidden"
                        name="paymentMethod"
                        checked={isSelected}
                        onChange={() => setPendingMethodCode(option.code)}
                      />
                      <div className="fw-semibold mb-2">{option.label}</div>
                      <p className="text-muted small mb-0">
                        {option.code === 'CARD'
                          ? 'クレジットカード／デビットカード決済に対応しています。'
                          : '銀行振込による決済に対応しています。'}
                      </p>
                      {option.code === 'CARD' && (
                        <div className="mt-2 d-flex align-items-center gap-2">
                          <img
                            src="https://img.icons8.com/color/48/visa.png"
                            alt="Visa"
                            style={{ height: '20px' }}
                          />
                          <img
                            src="https://img.icons8.com/color/48/mastercard-logo.png"
                            alt="MasterCard"
                            style={{ height: '20px' }}
                          />
                        </div>
                      )}
                      {isSelected && (
                        <div className="badge bg-primary-subtle text-primary mt-3 px-3 py-2 rounded-pill">
                          選択中
                        </div>
                      )}
                    </label>
                  </div>
                );
              })}
            </div>
            <div className="d-flex justify-content-end gap-2 mt-4">
              <button className="btn btn-outline-secondary" onClick={closePaymentModal}>
                キャンセル
              </button>
              <button className="btn btn-primary" onClick={confirmPaymentOption}>
                適用
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default CM_06_1001_Payment;
