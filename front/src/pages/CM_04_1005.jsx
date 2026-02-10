import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import { fetchOrdersWithProducts } from '../services/ReviewService.js';
import { createQna } from '../services/QnaService.js';
import CMMessage from '../constants/CMMessage';

const CATEGORY_OPTIONS = [
  { value: '', label: '選択してください' },
  { value: '1', label: '配送お問い合わせ' },
  { value: '2', label: '商品お問い合わせ' },
  { value: '3', label: 'その他お問い合わせ' },
];

export default function CM_04_1005() {
  const navigate = useNavigate();
  const { isLoggedIn, user } = useAuth();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedTag, setSelectedTag] = useState('');
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [openLoadingPopup, setOpenLoadingPopup] = useState(false);
  const [openErrorPopup, setOpenErrorPopup] = useState(false);
  const [openSuccessPopup, setOpenSuccessPopup] = useState(false);
  const [popupMessage, setPopupMessage] = useState('');

  const [selectedOrderNumber, setSelectedOrderNumber] = useState('');
  const [orderList, setOrderList] = useState([]);
  const [availableProducts, setAvailableProducts] = useState([]);
  const [selectedProductCode, setSelectedProductCode] = useState('');
  const [selectedProductName, setSelectedProductName] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('');
  const visibilityLabel = useMemo(() => {
    if (selectedTag === '公開') return '公開';
    if (selectedTag === '非公開') return '非公開';
    return '';
  }, [selectedTag]);

  const numericUserId = useMemo(() => {
    const raw = user?.userId ?? user?.id;
    if (raw === undefined || raw === null || raw === '') return null;
    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : null;
  }, [user?.userId, user?.id]);
  const isAdminOrManager = user?.roleId === 1 || user?.roleId === 2;

  useEffect(() => {
    if (!isLoggedIn) return;
    if (!isAdminOrManager) return;
    navigate('/qna', { replace: true });
  }, [isLoggedIn, isAdminOrManager, navigate]);

  useEffect(() => {
    if (!isLoggedIn || !user?.memberNumber) {
      setOrderList([]);
      setAvailableProducts([]);
      setSelectedOrder(null);
      setSelectedProductCode('');
      setSelectedProductId('');
      setSelectedProductName('');
      return;
    }

    fetchOrdersWithProducts(user.memberNumber)
      .then((list) => {
        const safeList = Array.isArray(list) ? list : [];
        setOrderList(safeList);
      })
      .catch((err) => {
        console.error('注文情報の読み込みに失敗しました:', err);
      });
  }, [isLoggedIn, user]);

  useEffect(() => {
    if (!selectedOrderNumber) {
      setSelectedOrder(null);
      setAvailableProducts([]);
      setSelectedProductCode('');
      setSelectedProductId('');
      setSelectedProductName('');
      setErrors((prev) => ({ ...prev, order: '', product: '' }));
      return;
    }

    const order = orderList.find((o) => o.orderNumber === selectedOrderNumber) || null;
    setSelectedOrder(order);

    const products = order?.products ?? [];
    setAvailableProducts(products);

    if (products.length === 1) {
      const [onlyProduct] = products;
      setSelectedProductCode(onlyProduct.productCode);
      setSelectedProductId(onlyProduct.productId);
      setSelectedProductName(onlyProduct.productName || '');
      setErrors((prev) => ({ ...prev, product: '' }));
    } else {
      setSelectedProductCode('');
      setSelectedProductId('');
      setSelectedProductName('');
      setErrors((prev) => ({ ...prev, product: '' }));
    }
  }, [selectedOrderNumber, orderList]);

  const validateForm = () => {
    const nextErrors = {};
    if (!title.trim()) nextErrors.title = CMMessage.MSG_VAL_002;
    if (!selectedTag) nextErrors.tag = CMMessage.MSG_VAL_008;
    if (!content.trim()) nextErrors.content = CMMessage.MSG_VAL_007;
    return nextErrors;
  };

  const buildPayloadContent = () => content.trim();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isAdminOrManager) {
      setPopupMessage('管理者/マネージャーはQ&Aを作成できません。');
      setOpenErrorPopup(true);
      return;
    }

    if (!isLoggedIn || !numericUserId) {
      setPopupMessage(CMMessage.MSG_ERR_023);
      setOpenErrorPopup(true);
      return;
    }

    setIsLoading(true);
    setOpenLoadingPopup(true);

    const validationErrors = validateForm();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      setIsLoading(false);
      setOpenLoadingPopup(false);
      return;
    }

    try {
      const payload = {
        userId: numericUserId,
        memberNumber: user?.memberNumber || '',
        title: title.trim(),
        content: buildPayloadContent(),
        isPrivate: selectedTag === '非公開' ? 1 : 0,
        inquiriesCodeType: '012',
        inquiriesCodeValue: selectedCategory ? Number(selectedCategory) : null,
        orderNumber: selectedOrder?.orderNumber || '',
        productName: selectedProductName || '',
        productId: selectedProductId || '',
      };

      const result = await createQna(payload);
      setPopupMessage(CMMessage.MSG_INF_009);
      setOpenSuccessPopup(true);
      setTimeout(() => {
        setOpenSuccessPopup(false);
        if (result?.qnaId) {
          navigate(`/qna/${result.qnaId}`);
        } else {
          navigate('/qna');
        }
      }, 1200);
    } catch (err) {
      console.error('Q&Aの登録に失敗しました:', err);
      setPopupMessage(err?.message || CMMessage.MSG_ERR_007('Q&A 登録'));
      setOpenErrorPopup(true);
    } finally {
      setIsLoading(false);
      setOpenLoadingPopup(false);
    }
  };

  return (
    <>
      <h1 className='m-5'>Q & A 作成する</h1>
      <div className="pt-0 px-5 pb-5">
        <CM_99_1002
          isOpen={openLoadingPopup}
          onClose={() => setOpenLoadingPopup(false)}
          duration={3000}
          hideClose={true}
        />
        <CM_99_1003
          isOpen={openErrorPopup}
          onClose={() => setOpenErrorPopup(false)}
          errorMessage={popupMessage}
        />
        <CM_99_1004
          isOpen={openSuccessPopup}
          onClose={() => setOpenSuccessPopup(false)}
          Message={popupMessage}
        />

        <form onSubmit={handleSubmit}>
          <div className="mb-3 w-50">
            <label className="form-label">タイトル</label>
            <div className="input-group">
              {visibilityLabel && <span className="input-group-text">{visibilityLabel}</span>}
              <input
                className="form-control"
                value={title}
                onChange={(e) => {
                  setTitle(e.target.value);
                  setErrors((prev) => ({ ...prev, title: '' }));
                }}
                placeholder="件名を入力してください"
              />
            </div>
            {errors.title && <div className="text-danger small mt-1">{errors.title}</div>}
          </div>

          <div className="mb-3 w-50">
            <label className="form-label">お問い合わせ種別</label>
            <select
              className="form-select"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              {CATEGORY_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3 w-100 d-flex gap-3 flex-column flex-md-row">
            <div className="w-100 w-md-50">
              <label className="form-label">注文番号（任意）</label>
              <select
                className="form-select"
                value={selectedOrderNumber}
                onChange={(e) => {
                  setSelectedOrderNumber(e.target.value);
                  setErrors((prev) => ({ ...prev, order: '' }));
                }}
                disabled={orderList.length === 0}
              >
                <option value="">
                  {orderList.length === 0 ? '注文履歴がありません' : '選択してください'}
                </option>
                {orderList.map((order) => {
                  const labelParts = [order.displayOrderNumber || order.orderNumber];
                  if (order.displayOrderDate) {
                    labelParts.push(`購入日: ${order.displayOrderDate}`);
                  }
                  return (
                    <option key={order.orderId} value={order.orderNumber}>
                      {labelParts.join(' / ')}
                    </option>
                  );
                })}
              </select>
              {orderList.length === 0 && (
                <div className="text-muted small mt-1">注文履歴がありません。</div>
              )}
            </div>
            <div className="w-100 w-md-50">
              <label className="form-label">商品（任意）</label>
              <select
                className="form-select"
                value={selectedProductCode}
                onChange={(e) => {
                  const code = e.target.value;
                  setSelectedProductCode(code);
                  const product = availableProducts.find((item) => item.productCode === code);
                  setSelectedProductId(product ? product.productId : '');
                  setSelectedProductName(product?.productName || '');
                  setErrors((prev) => ({ ...prev, product: '' }));
                }}
                disabled={availableProducts.length === 0}
              >
                <option value="">選択してください</option>
                {availableProducts.map((product) => (
                  <option key={product.productCode} value={product.productCode}>
                    {product.productName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {(selectedOrder || selectedProductCode) && (
            <div className="alert alert-secondary small">
              <div>
                <strong>選択した注文</strong> :{' '}
                {selectedOrder
                  ? `${selectedOrder.displayOrderNumber || selectedOrder.orderNumber}${
                      selectedOrder.displayOrderDate ? ` / ${selectedOrder.displayOrderDate}` : ''
                    }`
                  : '未選択'}
              </div>
              <div>
                <strong>選択した商品</strong> : {selectedProductName || selectedProductCode || '未選択'}
              </div>
            </div>
          )}

          <div className="mb-3">
            <label className="form-label">公開 / 非公開</label>
            <div>
              <div className="form-check form-check-inline">
                <input
                  className="form-check-input"
                  type="radio"
                  name="visibility"
                  id="qna-public"
                  value="公開"
                  checked={selectedTag === '公開'}
                  onChange={(e) => {
                    setSelectedTag(e.target.value);
                    setErrors((prev) => ({ ...prev, tag: '' }));
                  }}
                />
                <label className="form-check-label" htmlFor="qna-public">
                  公開
                </label>
              </div>
              <div className="form-check form-check-inline">
                <input
                  className="form-check-input"
                  type="radio"
                  name="visibility"
                  id="qna-private"
                  value="非公開"
                  checked={selectedTag === '非公開'}
                  onChange={(e) => {
                    setSelectedTag(e.target.value);
                    setErrors((prev) => ({ ...prev, tag: '' }));
                  }}
                />
                <label className="form-check-label" htmlFor="qna-private">
                  非公開
                </label>
              </div>
            </div>
            {errors.tag && <div className="text-danger small mt-1">{errors.tag}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label">内容</label>
            <textarea
              className="form-control"
              rows="10"
              value={content}
              onChange={(e) => {
                setContent(e.target.value);
                setErrors((prev) => ({ ...prev, content: '' }));
              }}
              placeholder="お問い合わせ内容を入力してください。"
            />
            {errors.content && <div className="text-danger small mt-1">{errors.content}</div>}
          </div>

          <div className="d-grid d-md-flex gap-3 mt-5">
            <button
              type="submit"
              className="btn btn-primary btn-lg w-100 w-md-50"
              disabled={isLoading}
            >
              {isLoading ? '作成中...' : '作成完了'}
            </button>
            <button
              type="button"
              className="btn btn-outline-secondary btn-lg w-100 w-md-50"
              onClick={() => navigate(-1)}
              disabled={isLoading}
            >
              戻る
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
