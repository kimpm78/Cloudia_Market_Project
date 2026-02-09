import { useState, useEffect } from 'react';
import { postRequest } from '../services/axiosInstance';
import { useNavigate } from 'react-router-dom';

export default function CM_01_1011_RefundModal({ isOpen, onClose, order, onSuccess }) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [refundInfo, setRefundInfo] = useState({ reason: '' });

  const [isAccountReady, setIsAccountReady] = useState(false);

  useEffect(() => {
    if (isOpen && order) {
      setRefundInfo({ reason: '' });

      if (order.paymentValue === 1) {
        const userInfo = order.userRefundInfo;
        if (userInfo?.bankName && userInfo?.accountNumber && userInfo?.accountHolder) {
          setIsAccountReady(true);
        } else {
          setIsAccountReady(false);
        }
      } else {
        setIsAccountReady(true);
      }
    }
  }, [isOpen, order]);

  if (!isOpen || !order) return null;

  const isBankTransfer = order.paymentValue === 1;

  const handleSubmit = async () => {
    if (!window.confirm('정말로 주문을 취소하시겠습니까?')) return;

    setLoading(true);
    try {
      await postRequest(`/user/mypage/purchases/${order.orderNo}/cancel`, {
        orderNo: order.orderNo,
        reason: refundInfo.reason,
        bankName: isBankTransfer ? order.userRefundInfo.bankName : null,
        accountNumber: isBankTransfer ? order.userRefundInfo.accountNumber : null,
        accountHolder: isBankTransfer ? order.userRefundInfo.accountHolder : null,
      });
      alert('취소가 완료되었습니다.');
      onSuccess();
      onClose();
    } catch (error) {
      alert('취소 처리 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="modal-backdrop show" style={{ zIndex: 1050 }}></div>
      <div className="modal show d-block" tabIndex="-1" style={{ zIndex: 1060 }}>
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow-lg">
            <div className="modal-header bg-light">
              <h5 className="modal-title fw-bold">주문 취소 신청</h5>
              <button
                type="button"
                className="btn-close"
                onClick={onClose}
                disabled={loading}
              ></button>
            </div>

            <div className="modal-body p-4">
              {isBankTransfer && !isAccountReady ? (
                <div className="text-center py-4">
                  <div className="mb-3">
                    <i
                      className="bi bi-exclamation-circle text-warning"
                      style={{ fontSize: '3.5rem' }}
                    ></i>
                  </div>
                  <h5 className="fw-bold mb-3">등록된 환불 계좌가 없습니다.</h5>
                  <p className="text-muted mb-4">
                    무통장 입금 취소는 계좌 정보가 필수입니다.
                    <br />
                    계좌를 먼저 등록하신 후 다시 시도해주세요.
                  </p>
                </div>
              ) : (
                <>
                  <div className="alert alert-secondary border-0 mb-4">
                    <div className="small text-secondary mb-1">
                      <strong>주문번호:</strong> {order.orderNo}
                    </div>
                    <div className="small text-secondary">
                      <strong>상품명:</strong> {order.productName}
                    </div>
                  </div>

                  {isBankTransfer && isAccountReady && (
                    <div
                      className="mb-4 p-3 rounded-3"
                      style={{ backgroundColor: '#f8f9fa', border: '1px solid #dee2e6' }}
                    >
                      <h6 className="fw-bold mb-3 small text-primary">
                        <i className="bi bi-check2-circle me-1"></i> 환불 예정 계좌 정보
                      </h6>
                      <div className="d-flex flex-column gap-2">
                        <div className="d-flex justify-content-between border-bottom pb-1">
                          <span className="text-muted small">은행명</span>
                          <span className="fw-bold small">{order.userRefundInfo.bankName}</span>
                        </div>
                        <div className="d-flex justify-content-between border-bottom pb-1">
                          <span className="text-muted small">환불계좌</span>
                          <span className="fw-bold small">
                            {order.userRefundInfo.accountNumber}
                          </span>
                        </div>
                        <div className="d-flex justify-content-between">
                          <span className="text-muted small">계좌명</span>
                          <span className="fw-bold small">
                            {order.userRefundInfo.accountHolder}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}

                  <div className="mb-0">
                    <label className="form-label fw-bold small">취소 사유 (선택)</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="사유를 입력해주세요."
                      value={refundInfo.reason}
                      onChange={(e) => setRefundInfo({ reason: e.target.value })}
                    />
                  </div>
                </>
              )}
            </div>

            <div className="modal-footer bg-light border-0">
              {isBankTransfer && !isAccountReady ? (
                <>
                  <button className="btn btn-secondary px-4" onClick={onClose}>
                    닫기
                  </button>
                  <button
                    className="btn btn-primary px-4 fw-bold"
                    onClick={() => navigate('/mypage/account')}
                  >
                    계좌 등록하러 가기
                  </button>
                </>
              ) : (
                <>
                  <button className="btn btn-secondary px-4" onClick={onClose} disabled={loading}>
                    닫기
                  </button>
                  <button
                    className="btn btn-danger px-4 fw-bold"
                    onClick={handleSubmit}
                    disabled={loading}
                  >
                    주문 취소하기
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
