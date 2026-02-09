import { useState, useEffect } from 'react';
import CM_99_1004 from './commonPopup/CM_99_1004';

export default function CM_90_1065_RegisterModal({ show, onHide, onConfirm }) {
  const [productCode, setProductCode] = useState('');
  const [productName, setProductName] = useState('');
  const [productCategory, setProductCategory] = useState('1');
  const [open1004, setOpen1004] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!show) {
      setProductCode('');
      setProductName('');
      setProductCategory('1');
    }
  }, [show]);

  const handleConfirm = () => {
    if (!productCode.trim() || !productName.trim()) {
      setMessage('모든 정보를 입력해주세요.');
      setOpen1004(true);
      return;
    }
    if (productCode.trim().length != 6) {
      setMessage('상품 코드는 6글자입니다.');
      setOpen1004(true);
      return;
    }
    onConfirm({
      productCode: productCode.trim().toUpperCase(),
      productName: productName.trim(),
      productCategory,
    });
  };

  if (!show) return null;

  return (
    <>
      <div className="modal-backdrop fade show" onClick={onHide} style={{ zIndex: 1040 }}></div>
      <div className="modal fade show" style={{ display: 'block', zIndex: 1050 }} tabIndex="-1">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content shadow-sm rounded-3">
            <div className="modal-header border-0 pb-0">
              <h5 className="modal-title fw-bold">상품 코드 등록</h5>
              <button type="button" className="btn-close" onClick={onHide}></button>
            </div>

            <div className="modal-body">
              <div className="mb-3">
                <label className="form-label fw-bold">상품 코드</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="상품 코드를 입력하세요"
                  value={productCode}
                  onChange={(e) => setProductCode(e.target.value)}
                />
              </div>

              <div className="mb-3">
                <label className="form-label fw-bold">상품 명</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="상품 명을 입력하세요"
                  value={productName}
                  onChange={(e) => setProductName(e.target.value)}
                />
              </div>

              <div className="mb-3">
                <label className="form-label fw-bold">상품 분류</label>
                <select
                  className="form-select"
                  value={productCategory}
                  onChange={(e) => setProductCategory(e.target.value)}
                >
                  <option value="1">상시 판매</option>
                  <option value="2">예약 판매</option>
                </select>
              </div>
            </div>

            <div className="modal-footer border-0">
              <button type="button" className="btn btn-secondary" onClick={onHide}>
                취소
              </button>
              <button type="button" className="btn btn-primary" onClick={handleConfirm}>
                등록
              </button>
            </div>
          </div>
        </div>
        <CM_99_1004 isOpen={open1004} onClose={() => setOpen1004(false)} Message={message} />
      </div>
    </>
  );
}
