import { useState, useEffect } from 'react';
import axiosInstance from '../services/axiosInstance';

export default function CM_90_1052_DeliveryTrackingModal({ show, onHide, onConfirm, orderData }) {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [stockType, setStockType] = useState('재고');
  const [customerInfo, setCustomerInfo] = useState(null);
  const [loading, setLoading] = useState(false);

  // 고객 환불 계좌 정보 조회
  useEffect(() => {
    const fetchCustomerRefundInfo = async () => {
      if (!show || !orderData) return;

      setLoading(true);
      try {
        const response = await axiosInstance.get('/admin/settlement/refund/getCustomerInfo', {
          params: {
            customerId: orderData.customerId,
          },
        });

        if (response.data.result) {
          setCustomerInfo(response.data.data);
        }
      } catch (error) {
        console.error('고객 정보 조회 실패:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCustomerRefundInfo();
  }, [show, orderData]);

  // 모달 닫힐 때 초기화
  useEffect(() => {
    if (!show) {
      setTrackingNumber('');
      setStockType('재고');
      setCustomerInfo(null);
    }
  }, [show]);

  const handleConfirm = () => {
    if (!trackingNumber.trim()) {
      alert('배송조회 번호를 입력해주세요.');
      return;
    }

    onConfirm({
      carrier: '재팬 포스트',
      trackingNumber: trackingNumber.trim(),
      stockType,
    });

    setTrackingNumber('');
    setStockType('재고');
  };

  const handleCancel = () => {
    setTrackingNumber('');
    setStockType('재고');
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
      {/* 백드롭 */}
      <div
        className="modal-backdrop fade show"
        onClick={handleCancel}
        style={{ zIndex: 1040 }}
      ></div>

      {/* 모달 */}
      <div
        className="modal fade show"
        style={{ display: 'block', zIndex: 1050 }}
        tabIndex="-1"
        role="dialog"
      >
        <div className="modal-dialog modal-dialog-centered" role="document">
          <div className="modal-content shadow-sm rounded-3">
            <div className="modal-header border-0 pb-0">
              <h5 className="modal-title fw-bold">교환/환불 정보</h5>
              <button
                type="button"
                className="btn-close"
                onClick={handleCancel}
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body">
              {loading ? (
                <div className="text-center py-4">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              ) : (
                <>
                  {/* 환불 계좌 정보 */}
                  <div className="mb-4">
                    <h6 className="mb-3 fw-bold">환불 계좌 정보</h6>
                    <div className="ps-3">
                      <div className="mb-2">
                        <strong>고객명:</strong> {customerInfo?.customerName}
                      </div>
                      <div className="mb-2">
                        <strong>은행명:</strong> {customerInfo?.bankName}
                      </div>
                      <div className="mb-2">
                        <strong>계좌:</strong> {customerInfo?.accountNumber}
                      </div>
                    </div>
                  </div>

                  {/* 택배사 (고정) */}
                  <div className="mb-4">
                    <label className="form-label fw-bold">택배사</label>
                    <input
                      type="text"
                      className="form-control bg-light fw-bold"
                      value="재팬 포스트"
                      disabled
                      readOnly
                    />
                  </div>

                  {/* 배송조회 번호 */}
                  <div className="mb-4">
                    <label className="form-label fw-bold">배송조회 번호</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="배송조회 번호를 입력하세요"
                      value={trackingNumber}
                      onChange={(e) => setTrackingNumber(e.target.value)}
                      onKeyDown={handleKeyDown}
                      autoFocus
                    />
                  </div>

                  {/* 재고 분류 */}
                  <div className="mb-3">
                    <h6 className="mb-3 fw-bold">재고 분류</h6>
                    <div className="d-flex gap-3">
                      <div className="form-check">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="stockType"
                          id="stock-restock"
                          value="재고"
                          checked={stockType === '재고'}
                          onChange={(e) => setStockType(e.target.value)}
                        />
                        <label className="form-check-label" htmlFor="stock-restock">
                          재고
                        </label>
                      </div>
                      <div className="form-check">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="stockType"
                          id="stock-defect"
                          value="불량"
                          checked={stockType === '불량'}
                          onChange={(e) => setStockType(e.target.value)}
                        />
                        <label className="form-check-label" htmlFor="stock-defect">
                          불량
                        </label>
                      </div>
                      <div className="form-check">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="stockType"
                          id="stock-damaged"
                          value="파츠"
                          checked={stockType === '파츠'}
                          onChange={(e) => setStockType(e.target.value)}
                        />
                        <label className="form-check-label" htmlFor="stock-damaged">
                          파츠
                        </label>
                      </div>
                    </div>
                  </div>
                </>
              )}
            </div>

            <div className="modal-footer border-0">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleCancel}
                disabled={loading}
              >
                닫기
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleConfirm}
                disabled={loading}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}