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
    1: '通常販売',
    2: '予約販売',
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
    if (!form.productName) newErrors.productName = '商品を選択してください。';
    if (form.productPrice === '' || isNaN(form.productPrice) || Number(form.productPrice) === 0) {
      newErrors.productPrice = '仕入価格を入力してください。';
    }
    if (form.quantity === '' || isNaN(form.quantity) || Number(form.quantity) === 0) {
      newErrors.quantity = '数量を入力してください。';
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
        <h5 className="border-bottom pb-2 mb-3">在庫登録</h5>
        <form>
          {/* 商品名 */}
          <div className="mb-3">
            <label htmlFor="productName" className="form-label">
              商品名
            </label>
            <Select
              id="productName"
              value={productCodeOptions.find((option) => option.value === form.productCode) || null}
              options={productCodeOptions}
              onChange={(selected) => handleProductCodeChange(selected?.value || '')}
              placeholder="商品を検索または選択してください"
              isClearable
              isSearchable
              noOptionsMessage={() => '検索結果がありません'}
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

          {/* 商品コード */}
          <div className="mb-3">
            <label htmlFor="productCode" className="form-label">
              商品コード（自動入力）
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

          {/* 商品区分 */}
          <div className="mb-3">
            <label htmlFor="productCategory" className="form-label">
              商品区分（自動入力）
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

          {/* 商品仕入価格 */}
          <div className="mb-3">
            <label htmlFor="productPrice" className="form-label">
              商品仕入価格
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

          {/* 数量 */}
          <div className="mb-3">
            <label htmlFor="quantity" className="form-label">
              数量
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

          {/* 備考 */}
          <div className="mb-3">
            <label htmlFor="note" className="form-label">
              備考
            </label>
            <input
              type="text"
              className="form-control"
              id="note"
              value={form.note}
              onChange={handleChange('note')}
            />
          </div>

          {/* ボタン */}
          <div className="d-flex justify-content-center">
            <button
              type="button"
              className="btn btn-primary me-2 btn-fixed-width"
              onClick={handleRegisterClick}
            >
              登録
            </button>
            <Link to="/admin/products/stock" className="btn btn-secondary btn-fixed-width">
              戻る
            </Link>
          </div>
        </form>

        {/* 登録確認モーダル */}
        {showModal && (
          <div className="modal fade show custom-modal-overlay" onClick={handleModalClose}>
            <div
              className="modal-dialog modal-dialog-centered"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">登録確認</h5>
                  <button
                    type="button"
                    className="btn-close"
                    onClick={handleModalClose}
                    aria-label="閉じる"
                  />
                </div>
                <div className="modal-body">本当に登録しますか？</div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-primary" onClick={handleSubmit}>
                    保存
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={handleModalClose}>
                    キャンセル
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
