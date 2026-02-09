import { useEffect, useState, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import Select from 'react-select';
import '../styles/CM_90_1064.css';

export default function CM_90_1064() {
  const navigate = useNavigate();
  const [productOptions, setProductOptions] = useState([]);
  const [form, setForm] = useState({
    productCode: '',
    productCategory: '',
    productName: '',
    productPrice: '',
    quantity: '',
    note: '',
  });
  const [errors, setErrors] = useState({});
  const [showModal, setShowModal] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [message, setMessage] = useState('');
  const [popupType, setPopupType] = useState('');
  const PRODUCT_CATEGORY_LABEL = {
    1: '상시 판매',
    2: '예약 판매',
  };

  useEffect(() => {
    const fetchData = async () => {
      const result = await axiosInstance.get('/admin/productCode/all');
      if (result.data.result) {
        setProductOptions(result.data.resultList);
      }
    };
    fetchData();
  }, []);

  const productCodeOptions = useMemo(
    () =>
      productOptions.map((option) => ({
        value: option.productCode,
        label: option.productName,
      })),
    [productOptions]
  );

  const handleChange = (field) => (e) => {
    const value = e.target.value.replace(/,/g, '');

    if (field === 'productPrice') {
      if (!/^\d*$/.test(value)) return;
    }
    if (field === 'quantity') {
      if (!/^-?\d*$/.test(value)) return;
    }

    setForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleProductCodeChange = async (code) => {
    const selected = productOptions.find((p) => p.productCode === code);

    setForm((prev) => ({
      ...prev,
      productCode: code,
      productName: selected?.productName || '',
      productPrice: '',
    }));

    if (code) {
      const result = await axiosInstance.get(`/admin/productCode/${code}`);
      const { price = 0 } = result.data.resultList || {};
      const categoryCode = result.data.resultList.productCategory;

      setForm((prev) => ({
        ...prev,
        productPrice: price,
        productCategory: PRODUCT_CATEGORY_LABEL[categoryCode] || '',
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!form.productName) newErrors.productName = '상품을 선택하세요.';
    if (form.productPrice === '' || isNaN(form.productPrice) || Number(form.productPrice) === 0) {
      newErrors.productPrice = '사입가을 입력하세요.';
    }
    if (form.quantity === '' || isNaN(form.quantity) || Number(form.quantity) === 0) {
      newErrors.quantity = '수량을 입력하세요.';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegisterClick = () => {
    if (validateForm()) {
      setShowModal(true);
    }
  };

  const handleSubmit = async () => {
    const result = await axiosInstance.put('/admin/stocks', form);
    setShowModal(false);
    if (result.data.resultList > 0) {
      setPopupType('success');
    } else {
      setPopupType('error');
    }
    setMessage(result.data.message);
    setOpen1004(true);
  };

  const handleModalClose = () => setShowModal(false);

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">재고 등록</h5>
        <form>
          {/* 상품명 */}
          <div className="mb-3">
            <label htmlFor="productName" className="form-label">
              상품명
            </label>
            <Select
              id="productName"
              value={productCodeOptions.find((option) => option.value === form.productCode) || null}
              options={productCodeOptions}
              onChange={(selected) => handleProductCodeChange(selected?.value || '')}
              placeholder="상품을 검색하거나 선택하세요"
              isClearable
              isSearchable
              noOptionsMessage={() => '검색 결과가 없습니다'}
              styles={{
                control: (base) => ({
                  ...base,
                  minHeight: '38px',
                  borderRadius: '0.375rem',
                  borderColor: '#ced4da',
                }),
                valueContainer: (base) => ({
                  ...base,
                  padding: '0.375rem 0.75rem',
                }),
                input: (base) => ({
                  ...base,
                  margin: 0,
                  padding: 0,
                }),
              }}
            />
            {errors.productName && <div className="text-danger">{errors.productName}</div>}
          </div>

          {/* 상품코드 */}
          <div className="mb-3">
            <label htmlFor="productCode" className="form-label">
              상품코드 (자동 입력)
            </label>
            <input
              type="text"
              className="form-control"
              id="productCode"
              value={form.productCode}
              readOnly
              disabled
            />
          </div>

          {/* 상품분류 */}
          <div className="mb-3">
            <label htmlFor="productCategory" className="form-label">
              상품분류 (자동 입력)
            </label>
            <input
              type="text"
              className="form-control"
              id="productCategory"
              value={form.productCategory}
              readOnly
              disabled
            />
          </div>

          {/* 상품 사입가 */}
          <div className="mb-3">
            <label htmlFor="productPrice" className="form-label">
              상품 사입가
            </label>
            <input
              type="text"
              className="form-control"
              id="productPrice"
              value={Number(form.productPrice).toLocaleString()}
              onChange={handleChange('productPrice')}
            />
            {errors.productPrice && <div className="text-danger">{errors.productPrice}</div>}
          </div>

          {/* 수량 */}
          <div className="mb-3">
            <label htmlFor="quantity" className="form-label">
              수량
            </label>
            <input
              type="text"
              className="form-control"
              id="quantity"
              value={form.quantity}
              onChange={handleChange('quantity')}
            />
            {errors.quantity && <div className="text-danger">{errors.quantity}</div>}
          </div>

          {/* 비고 */}
          <div className="mb-3">
            <label htmlFor="note" className="form-label">
              비고
            </label>
            <input
              type="text"
              className="form-control"
              id="note"
              value={form.note}
              onChange={handleChange('note')}
            />
          </div>

          {/* 버튼 */}
          <div className="d-flex justify-content-center">
            <button
              type="button"
              className="btn btn-primary me-2 btn-fixed-width"
              onClick={handleRegisterClick}
            >
              등록
            </button>
            <Link to="/admin/products/stock" className="btn btn-secondary btn-fixed-width">
              뒤로 가기
            </Link>
          </div>
        </form>

        {/* 등록 확인 모달 */}
        {showModal && (
          <div className="modal fade show custom-modal-overlay" onClick={handleModalClose}>
            <div
              className="modal-dialog modal-dialog-centered"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">등록 확인</h5>
                  <button
                    type="button"
                    className="btn-close"
                    onClick={handleModalClose}
                    aria-label="닫기"
                  />
                </div>
                <div className="modal-body">정말로 등록하시겠습니까?</div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-primary" onClick={handleSubmit}>
                    저장
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={handleModalClose}>
                    취소
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
        <CM_99_1004
          isOpen={open1004}
          onClose={() => {
            setOpen1004(false);
            if (popupType === 'success') {
              navigate('/admin/products/stock');
            }
          }}
          Message={message}
        />
      </div>
    </div>
  );
}
