import { useState, useEffect } from 'react';
import axiosInstance from '../services/axiosInstance';

export default function CM_90_1052_DeliveryTrackingModal({ show, onHide, onConfirm, orderData }) {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [stockType, setStockType] = useState('在庫');
  const [customerInfo, setCustomerInfo] = useState(null);
  const [loading, setLoading] = useState(false);

  // 顧客の返金口座情報を取得
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
        console.error('顧客情報の取得に失敗しました:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCustomerRefundInfo();
  }, [show, orderData]);

  // モーダルを閉じる際に初期化
  useEffect(() => {
    if (!show) {
      setTrackingNumber('');
      setStockType('在庫');
      setCustomerInfo(null);
    }
  }, [show]);

  const handleConfirm = () => {
    if (!trackingNumber.trim()) {
      alert('追跡番号を入力してください。');
      return;
    }

    onConfirm({
      carrier: '日本郵便',
      trackingNumber: trackingNumber.trim(),
      stockType,
    });

    setTrackingNumber('');
    setStockType('在庫');
  };

  const handleCancel = () => {
    setTrackingNumber('');
    setStockType('在庫');
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

      {/* モーダル */}
      <div
        className="modal fade show"
        style={{ display: 'block', zIndex: 1050 }}
        tabIndex="-1"
        role="dialog"
      >
        <div className="modal-dialog modal-dialog-centered" role="document">
          <div className="modal-content shadow-sm rounded-3">
            <div className="modal-header border-0 pb-0">
              <h5 className="modal-title fw-bold">交換・返金情報</h5>
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
                  {/* 返金口座情報 */}
                  <div className="mb-4">
                    <h6 className="mb-3 fw-bold">返金口座情報</h6>
                    <div className="ps-3">
                      <div className="mb-2">
                        <strong>顧客名:</strong> {customerInfo?.customerName}
                      </div>
                      <div className="mb-2">
                        <strong>銀行名:</strong> {customerInfo?.bankName}
                      </div>
                      <div className="mb-2">
                        <strong>口座:</strong> {customerInfo?.accountNumber}
                      </div>
                    </div>
                  </div>

                  {/* 配送会社（固定） */}
                  <div className="mb-4">
                    <label className="form-label fw-bold">配送会社</label>
                    <input
                      type="text"
                      className="form-control bg-light fw-bold"
                      value="日本郵便"
                      disabled
                      readOnly
                    />
                  </div>

                  {/* 追跡番号 */}
                  <div className="mb-4">
                    <label className="form-label fw-bold">追跡番号</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="追跡番号を入力してください"
                      value={trackingNumber}
                      onChange={(e) => setTrackingNumber(e.target.value)}
                      onKeyDown={handleKeyDown}
                      autoFocus
                    />
                  </div>

                  {/* 在庫区分 */}
                  <div className="mb-3">
                    <h6 className="mb-3 fw-bold">在庫区分</h6>
                    <div className="d-flex gap-3">
                      <div className="form-check">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="stockType"
                          id="stock-restock"
                          value="在庫"
                          checked={stockType === '在庫'}
                          onChange={(e) => setStockType(e.target.value)}
                        />
                        <label className="form-check-label" htmlFor="stock-restock">
                          在庫
                        </label>
                      </div>
                      <div className="form-check">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="stockType"
                          id="stock-defect"
                          value="不良"
                          checked={stockType === '不良'}
                          onChange={(e) => setStockType(e.target.value)}
                        />
                        <label className="form-check-label" htmlFor="stock-defect">
                          不良
                        </label>
                      </div>
                      <div className="form-check">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="stockType"
                          id="stock-damaged"
                          value="パーツ"
                          checked={stockType === 'パーツ'}
                          onChange={(e) => setStockType(e.target.value)}
                        />
                        <label className="form-check-label" htmlFor="stock-damaged">
                          パーツ
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
                閉じる
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleConfirm}
                disabled={loading}
              >
                確認
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}