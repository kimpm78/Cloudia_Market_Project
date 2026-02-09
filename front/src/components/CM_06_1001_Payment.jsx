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
      <h5 className="border-bottom pb-2 mt-4">주문결제 입력</h5>
      <div className="py-2 d-flex justify-content-between align-items-center">
        <strong>결제 방법</strong>
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
            편집
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
                <h5 className="m-0 fw-semibold">결제 방법 선택</h5>
                <small className="text-muted">원하는 결제 수단을 선택해 주세요.</small>
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
                          ? '신용카드/체크카드 결제를 지원합니다.'
                          : '계좌 이체를 통한 결제를 지원합니다.'}
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
                          선택됨
                        </div>
                      )}
                    </label>
                  </div>
                );
              })}
            </div>
            <div className="d-flex justify-content-end gap-2 mt-4">
              <button className="btn btn-outline-secondary" onClick={closePaymentModal}>
                취소
              </button>
              <button className="btn btn-primary" onClick={confirmPaymentOption}>
                적용
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default CM_06_1001_Payment;
