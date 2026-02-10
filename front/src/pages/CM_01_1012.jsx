import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import { fetchOrdersWithProducts } from '../services/ReviewService.js';
import { createInquiry } from '../services/InquiryService.js';
import axiosInstance from '../services/axiosInstance.js';

const ShippingInquiryFields = ({
  orderList,
  selectedOrderNumber,
  onOrderChange,
  availableProducts,
  selectedProductCode,
  onProductChange,
  errors,
}) => (
  <div className="mb-3 w-100 d-flex gap-3 flex-column flex-md-row">
    <div className="w-100 w-md-50">
      <label className="form-label">注文番号</label>
      <select
        className="form-select"
        value={selectedOrderNumber}
        onChange={onOrderChange}
        disabled={orderList.length === 0}
      >
        <option value="">選択してください</option>
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
      {errors.order && <div className="text-danger small mt-1">{errors.order}</div>}
    </div>
    <div className="w-100 w-md-50">
      <label className="form-label">商品</label>
      <select
        className="form-select"
        value={selectedProductCode}
        onChange={onProductChange}
        disabled={availableProducts.length === 0}
      >
        <option value="">選択してください</option>
        {availableProducts.map((product) => (
          <option key={product.productCode} value={product.productCode}>
            {product.productName}
          </option>
        ))}
      </select>
      {errors.product && <div className="text-danger small mt-1">{errors.product}</div>}
    </div>
  </div>
);

const ProductInquiryFields = ({
  productList,
  selectedProductCode,
  onProductChange,
  productNumberText,
  errors,
}) => (
  <div className="mb-3 w-100 d-flex gap-3 flex-column flex-md-row">
    <div className="w-100 w-md-50">
      <label className="form-label">商品名</label>
      <select
        className="form-select"
        value={selectedProductCode}
        onChange={onProductChange}
        disabled={productList.length === 0}
      >
        <option value="">商品を選択してください</option>
        {productList.map((product) => (
          <option key={product.productCode} value={product.productCode}>
            {product.productName}
          </option>
        ))}
      </select>
      {errors.productNameText && (
        <div className="text-danger small mt-1">{errors.productNameText}</div>
      )}
    </div>
    <div className="w-100 w-md-50">
      <label className="form-label">商品番号（自動入力）</label>
      <input
        type="text"
        className="form-control"
        value={productNumberText}
        placeholder="商品を選択すると自動入力されます"
        readOnly
        disabled
      />
    </div>
  </div>
);

export default function CM_01_1012() {
  const navigate = useNavigate();
  const { isLoggedIn, user } = useAuth();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedTag, setSelectedTag] = useState('非公開');
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [openLoadingPopup, setOpenLoadingPopup] = useState(false);
  const [openErrorPopup, setOpenErrorPopup] = useState(false);
  const [openSuccessPopup, setOpenSuccessPopup] = useState(false);
  const [popupMessage, setPopupMessage] = useState('');
  const [inquiryCategories, setInquiryCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedOrderNumber, setSelectedOrderNumber] = useState('');
  const [orderList, setOrderList] = useState([]);
  const [availableProducts, setAvailableProducts] = useState([]);
  const [selectedProductCode, setSelectedProductCode] = useState('');
  const [selectedProductName, setSelectedProductName] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [productList, setProductList] = useState([]); // 商品一覧
  const [productNameText, setProductNameText] = useState(''); // 選択された商品名
  const [productNumberText, setProductNumberText] = useState(''); // 選択された商品コード (自動入力)

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

  const selectedCategoryName = useMemo(() => {
    return inquiryCategories.find((c) => c.value === selectedCategory)?.name || '';
  }, [selectedCategory, inquiryCategories]);

  const fetchInquiryCategories = useCallback(async () => {
    setLoadingCategories(true);
    try {
      const response = await axiosInstance.get('/common-codes?group=012');
      const dataList = response.data?.resultList ?? response.data ?? [];

      if (Array.isArray(dataList)) {
        const formattedCategories = dataList.map((code) => ({
          value: String(code.codeValue),
          name: code.codeValueName,
        }));
        setInquiryCategories([{ value: '', name: '選択してください' }, ...formattedCategories]);
      } else {
        setInquiryCategories([{ value: '', name: '選択してください' }]);
      }
    } catch (err) {
      console.error('お問い合わせ分類一覧の読み込みに失敗しました:', err);
      setInquiryCategories([{ value: '', name: '選択してください' }]);
    } finally {
      setLoadingCategories(false);
    }
  }, []);

  useEffect(() => {
    fetchInquiryCategories();
  }, [fetchInquiryCategories]);

  useEffect(() => {
    if (selectedCategoryName !== '배송문의' || !isLoggedIn || !user?.memberNumber) {
      setOrderList([]);
      setAvailableProducts([]);
      return;
    }

    fetchOrdersWithProducts(user.memberNumber)
      .then((list) => {
        setOrderList(Array.isArray(list) ? list : []);
      })
      .catch((err) => {
        console.error('注文情報の読み込みに失敗しました:', err);
      });
  }, [isLoggedIn, user, selectedCategoryName]);

  useEffect(() => {
    if (!selectedOrderNumber) {
      setSelectedOrder(null);
      setAvailableProducts([]);
      setSelectedProductCode('');
      setSelectedProductId('');
      setSelectedProductName('');
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
    } else {
      setSelectedProductCode('');
      setSelectedProductId('');
      setSelectedProductName('');
    }
  }, [selectedOrderNumber, orderList]);

  useEffect(() => {
    if (selectedCategoryName !== '商品お問い合わせ') {
      setProductList([]);
      return;
    }

    axiosInstance
      .get('/products/list')
      .then((res) => {
        const list = res.data;
        if (Array.isArray(list)) {
          setProductList(list);
        }
      })
      .catch((err) => {
        console.error('商品一覧の読み込みに失敗しました:', err);
      });
  }, [selectedCategoryName]);

  // 상품 선택 핸들러
  const handleProductSelectChange = (e) => {
    const code = e.target.value;

    if (!code) {
      setProductNumberText('');
      setProductNameText('');
      return;
    }

    // 선택된 코드로 상품 정보 찾기
    const selectedProduct = productList.find((p) => p.productCode === code);

    if (selectedProduct) {
      setProductNumberText(code); //商品コード 格納
      setProductNameText(selectedProduct.productName); // 商品名 格納
      setErrors((prev) => ({ ...prev, productNameText: '' }));
    }
  };

  useEffect(() => {
    if (selectedCategoryName === 'その他お問い合わせ') {
      setSelectedTag('非公開');
    }
  }, [selectedCategoryName]);

  const validateForm = () => {
    const nextErrors = {};
    if (!title.trim()) nextErrors.title = '＊件名を入力してください。';
    if (!selectedCategory) nextErrors.category = '＊お問い合わせ種別を選択してください。';
    if (!content.trim()) nextErrors.content = '＊内容を入力してください。';

    if (selectedCategoryName !== 'その他お問い合わせ' && !selectedTag) {
      nextErrors.tag = '＊公開/非公開を選択してください。';
    }

    switch (selectedCategoryName) {
      case '配送お問い合わせ':
        if (!selectedOrderNumber) nextErrors.order = '＊注文番号を選択してください。';
        if (!selectedProductCode) nextErrors.product = '＊商品を選択してください。';
        break;
      case '商品お問い合わせ':
        if (!productNameText.trim()) nextErrors.productNameText = '＊商品を選択してください。';
        break;
      case 'その他お問い合わせ':
      default:
        break;
    }
    return nextErrors;
  };

  const buildPayloadContent = () => {
    const meta = [];

    if (selectedCategoryName) meta.push(`[お問い合わせ種別] ${selectedCategoryName}`);

    switch (selectedCategoryName) {
      case '配送お問い合わせ': {
        const displayOrderNo = selectedOrder?.displayOrderNumber || selectedOrderNumber;
        if (displayOrderNo) meta.push(`[注文番号] ${displayOrderNo}`);
        if (selectedProductName) meta.push(`[商品] ${selectedProductName}`);
        if (selectedProductCode) meta.push(`[商品コード] ${selectedProductCode}`);
        if (selectedProductId) meta.push(`[商品ID] ${selectedProductId}`);
        break;
      }
      case '商品お問い合わせ':
        if (productNameText) meta.push(`[商品名] ${productNameText.trim()}`);
        if (productNumberText) meta.push(`[商品番号] ${productNumberText.trim()}`);
        break;
      case 'その他お問い合わせ':
      default:
        break;
    }

    const metaBlock = meta.length > 0 ? `${meta.join('\n')}\n\n` : '';
    return `${metaBlock}${content.trim()}`;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!isLoggedIn || !numericUserId) {
      setPopupMessage('ログインが必要です。');
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
        title: title.trim(),
        content: buildPayloadContent(),
        isPrivate: selectedTag === '비공개' ? 1 : 0,
        categoryCode: selectedCategory,
      };

      const result = await createInquiry(payload);
      setPopupMessage('1:1お問い合わせを登録しました。');
      setOpenSuccessPopup(true);
      setTimeout(() => {
        setOpenSuccessPopup(false);
        if (result?.inquiryId) {
          navigate(`/mypage/inquiries/${result.inquiryId}`);
        } else {
          navigate('/mypage/inquiries');
        }
      }, 1200);
    } catch (err) {
      console.error('1:1お問い合わせの登録に失敗しました:', err);
      setPopupMessage(
        err?.response?.data?.message || err?.message || '1:1お問い合わせの登録に失敗しました。'
      );
      setOpenErrorPopup(true);
    } finally {
      setIsLoading(false);
      setOpenLoadingPopup(false);
    }
  };

  const renderDynamicFields = () => {
    switch (selectedCategoryName) {
      case '配送お問い合わせ':
        return (
          <ShippingInquiryFields
            orderList={orderList}
            selectedOrderNumber={selectedOrderNumber}
            onOrderChange={(e) => {
              setSelectedOrderNumber(e.target.value);
              setErrors((prev) => ({ ...prev, order: '' }));
            }}
            availableProducts={availableProducts}
            selectedProductCode={selectedProductCode}
            onProductChange={(e) => {
              const code = e.target.value;
              setSelectedProductCode(code);
              const product = availableProducts.find((item) => item.productCode === code);
              setSelectedProductId(product ? product.productId : '');
              setSelectedProductName(product?.productName || '');
              setErrors((prev) => ({ ...prev, product: '' }));
            }}
            errors={errors}
          />
        );

      case '商品お問い合わせ':
        return (
          <ProductInquiryFields
            productList={productList}
            selectedProductCode={productNumberText}
            onProductChange={handleProductSelectChange}
            productNumberText={productNumberText}
            errors={errors}
          />
        );

      case 'その他お問い合わせ':
      default:
        return null;
    }
  };

  return (
    <>
      <h1>1:1 お問い合わせ（作成）</h1>
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
            <label className="form-label">件名</label>
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
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setErrors((prev) => ({ ...prev, category: '' }));
              }}
              disabled={loadingCategories}
            >
              {inquiryCategories.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.name}
                </option>
              ))}
            </select>
            {errors.category && <div className="text-danger small mt-1">{errors.category}</div>}
          </div>

          {renderDynamicFields()}

          {selectedCategoryName !== 'その他お問い合わせ' && (
            <div className="mb-3">
              <label className="form-label">公開/非公開</label>
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
                    value="비공개"
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
          )}

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

          <div className="d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/mypage/inquiries')}
              disabled={isLoading}
            >
              一覧へ
            </button>
            <button type="submit" className="btn btn-primary" disabled={isLoading}>
              {isLoading ? '登録中...' : '登録する'}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
