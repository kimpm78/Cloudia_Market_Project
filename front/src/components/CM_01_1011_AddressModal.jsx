import { useState, useEffect } from 'react';
import { getRequest, putRequest } from '../services/axiosInstance';

export default function CM_01_1011_AddressModal({
  isOpen,
  onClose,
  orderNo,
  currentAddressId,
  onSuccess,
}) {
  const [addressList, setAddressList] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(currentAddressId);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      fetchAddresses();
      setSelectedAddressId(currentAddressId);
    }
  }, [isOpen, currentAddressId]);

  const fetchAddresses = async () => {
    try {
      const data = await getRequest('/user/mypage/delivery-addresses');
      setAddressList(data || []);
    } catch (error) {
      console.error('배송지 목록 조회 실패:', error);
    }
  };

  const handleSave = async () => {
    if (!selectedAddressId) {
      alert('변경할 주소를 선택해주세요.');
      return;
    }
    const selectedAddr = addressList.find((addr) => addr.addressId === selectedAddressId);
    if (!selectedAddr) {
      alert('선택된 주소 정보를 찾을 수 없습니다.');
      return;
    }
    setLoading(true);
    try {
      await putRequest(`/user/mypage/purchases/${orderNo}/shipping`, {
        orderNo: orderNo,
        addressId: selectedAddressId,
        recipientName: selectedAddr.recipientName,
        recipientPhone: selectedAddr.recipientPhone,
        shippingAddressId: selectedAddr.addressId,
      });

      alert('배송지가 변경되었습니다.');
      onSuccess();
      onClose();
    } catch (error) {
      console.error('배송지 변경 실패:', error);
      alert('배송지 변경에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="modal-backdrop show" style={{ zIndex: 1050 }}></div>
      <div className="modal show d-block" tabIndex="-1" style={{ zIndex: 1060 }}>
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 shadow-lg">
            <div className="modal-header bg-light">
              <h5 className="modal-title fw-bold">
                <i className="bi bi-geo-alt-fill me-2 text-primary"></i>배송지 변경
              </h5>
              <button
                type="button"
                className="btn-close"
                onClick={onClose}
                disabled={loading}
              ></button>
            </div>

            <div className="modal-body p-4" style={{ maxHeight: '400px', overflowY: 'auto' }}>
              <p className="small text-muted mb-3">변경하실 주소를 선택해주세요.</p>

              {addressList.length > 0 ? (
                <div className="list-group">
                  {addressList.map((addr) => (
                    <label
                      key={addr.addressId}
                      className={`list-group-item list-group-item-action p-3 ${selectedAddressId === addr.addressId ? 'bg-primary-subtle border-primary' : ''}`}
                      style={{ cursor: 'pointer' }}
                    >
                      <div className="d-flex w-100 justify-content-between align-items-start">
                        <div className="flex-grow-1">
                          <div className="d-flex align-items-center mb-1">
                            <input
                              className="form-check-input me-3"
                              type="radio"
                              name="addressRadio"
                              checked={selectedAddressId === addr.addressId}
                              onChange={() => setSelectedAddressId(addr.addressId)}
                            />
                            <span className="fw-bold">{addr.recipientName || '수령인 미지정'}</span>
                            {addr.addressName && (
                              <span className="badge bg-secondary ms-2">{addr.addressName}</span>
                            )}
                          </div>
                          <div className="ms-4 ps-1">
                            <div className="small text-dark">
                              {addr.addressMain} {addr.addressDetail1}
                            </div>
                            <div className="small text-secondary">{addr.recipientPhone}</div>
                          </div>
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
              ) : (
                <div className="text-center py-4">
                  <p className="text-muted">등록된 주소가 없습니다.</p>
                </div>
              )}
            </div>

            <div className="modal-footer bg-light border-top-0">
              <button
                type="button"
                className="btn btn-secondary px-4"
                onClick={onClose}
                disabled={loading}
              >
                취소
              </button>
              <button
                type="button"
                className="btn btn-primary px-4 fw-bold"
                onClick={handleSave}
                disabled={loading || addressList.length === 0}
              >
                {loading ? <span className="spinner-border spinner-border-sm me-2"></span> : null}
                변경하기
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
