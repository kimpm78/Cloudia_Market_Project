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
      <label className="form-label">주문번호</label>
      <select
        className="form-select"
        value={selectedOrderNumber}
        onChange={onOrderChange}
        disabled={orderList.length === 0}
      >
        <option value="">선택해주세요</option>
        {orderList.map((order) => {
          const labelParts = [order.displayOrderNumber || order.orderNumber];
          if (order.displayOrderDate) {
            labelParts.push(`구매일: ${order.displayOrderDate}`);
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
      <label className="form-label">상품</label>
      <select
        className="form-select"
        value={selectedProductCode}
        onChange={onProductChange}
        disabled={availableProducts.length === 0}
      >
        <option value="">선택해주세요</option>
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
      <label className="form-label">상품명</label>
      <select
        className="form-select"
        value={selectedProductCode}
        onChange={onProductChange}
        disabled={productList.length === 0}
      >
        <option value="">상품을 선택해주세요</option>
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
      <label className="form-label">상품번호 (자동 입력)</label>
      <input
        type="text"
        className="form-control"
        value={productNumberText}
        placeholder="상품 선택 시 자동 입력됩니다"
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
  const [selectedTag, setSelectedTag] = useState('비공개');
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
  const [productList, setProductList] = useState([]); // 전체 상품 목록
  const [productNameText, setProductNameText] = useState(''); // 선택된 상품명
  const [productNumberText, setProductNumberText] = useState(''); // 선택된 상품코드 (자동입력)

  const visibilityLabel = useMemo(() => {
    if (selectedTag === '공개') return '공개';
    if (selectedTag === '비공개') return '비공개';
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
        setInquiryCategories([{ value: '', name: '선택해주세요' }, ...formattedCategories]);
      } else {
        setInquiryCategories([{ value: '', name: '선택해주세요' }]);
      }
    } catch (err) {
      console.error('문의 분류 목록 로딩 실패:', err);
      setInquiryCategories([{ value: '', name: '선택해주세요' }]);
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
        console.error('주문 정보 로드 실패:', err);
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
    if (selectedCategoryName !== '상품문의') {
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
        console.error('상품 목록 로드 실패:', err);
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
      setProductNumberText(code); // 상품번호 자동 입력
      setProductNameText(selectedProduct.productName); // 상품명 저장
      setErrors((prev) => ({ ...prev, productNameText: '' }));
    }
  };

  useEffect(() => {
    if (selectedCategoryName === '기타문의') {
      setSelectedTag('비공개');
    }
  }, [selectedCategoryName]);

  const validateForm = () => {
    const nextErrors = {};
    if (!title.trim()) nextErrors.title = '＊제목을 입력해주세요.';
    if (!selectedCategory) nextErrors.category = '＊문의 유형을 선택해주세요.';
    if (!content.trim()) nextErrors.content = '＊내용을 입력해주세요.';

    if (selectedCategoryName !== '기타문의' && !selectedTag) {
      nextErrors.tag = '＊공개 여부를 선택해주세요.';
    }

    switch (selectedCategoryName) {
      case '배송문의':
        if (!selectedOrderNumber) nextErrors.order = '＊주문번호를 선택해주세요.';
        if (!selectedProductCode) nextErrors.product = '＊상품을 선택해주세요.';
        break;
      case '상품문의':
        if (!productNameText.trim()) nextErrors.productNameText = '＊상품을 선택해주세요.';
        break;
      case '기타문의':
      default:
        break;
    }
    return nextErrors;
  };

  const buildPayloadContent = () => {
    const meta = [];

    if (selectedCategoryName) meta.push(`[문의유형] ${selectedCategoryName}`);

    switch (selectedCategoryName) {
      case '배송문의': {
        const displayOrderNo = selectedOrder?.displayOrderNumber || selectedOrderNumber;
        if (displayOrderNo) meta.push(`[주문번호] ${displayOrderNo}`);
        if (selectedProductName) meta.push(`[상품] ${selectedProductName}`);
        if (selectedProductCode) meta.push(`[상품코드] ${selectedProductCode}`);
        if (selectedProductId) meta.push(`[상품ID] ${selectedProductId}`);
        break;
      }
      case '상품문의':
        if (productNameText) meta.push(`[상품명] ${productNameText.trim()}`);
        if (productNumberText) meta.push(`[상품번호] ${productNumberText.trim()}`);
        break;
      case '기타문의':
      default:
        break;
    }

    const metaBlock = meta.length > 0 ? `${meta.join('\n')}\n\n` : '';
    return `${metaBlock}${content.trim()}`;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!isLoggedIn || !numericUserId) {
      setPopupMessage('로그인이 필요합니다.');
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
      setPopupMessage('1:1 문의가 등록되었습니다.');
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
      console.error('1:1 문의 등록 실패:', err);
      setPopupMessage(
        err?.response?.data?.message || err?.message || '1:1 문의 등록에 실패했습니다.'
      );
      setOpenErrorPopup(true);
    } finally {
      setIsLoading(false);
      setOpenLoadingPopup(false);
    }
  };

  const renderDynamicFields = () => {
    switch (selectedCategoryName) {
      case '배송문의':
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

      case '상품문의':
        return (
          <ProductInquiryFields
            productList={productList}
            selectedProductCode={productNumberText}
            onProductChange={handleProductSelectChange}
            productNumberText={productNumberText}
            errors={errors}
          />
        );

      case '기타문의':
      default:
        return null;
    }
  };

  return (
    <>
      <h1>1:1 문의 (작성하기)</h1>
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
            <label className="form-label">제목</label>
            <div className="input-group">
              {visibilityLabel && <span className="input-group-text">{visibilityLabel}</span>}
              <input
                className="form-control"
                value={title}
                onChange={(e) => {
                  setTitle(e.target.value);
                  setErrors((prev) => ({ ...prev, title: '' }));
                }}
                placeholder="제목을 입력해주세요"
              />
            </div>
            {errors.title && <div className="text-danger small mt-1">{errors.title}</div>}
          </div>

          <div className="mb-3 w-50">
            <label className="form-label">문의 유형</label>
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

          {selectedCategoryName !== '기타문의' && (
            <div className="mb-3">
              <label className="form-label">공개 여부</label>
              <div>
                <div className="form-check form-check-inline">
                  <input
                    className="form-check-input"
                    type="radio"
                    name="visibility"
                    id="qna-public"
                    value="공개"
                    checked={selectedTag === '공개'}
                    onChange={(e) => {
                      setSelectedTag(e.target.value);
                      setErrors((prev) => ({ ...prev, tag: '' }));
                    }}
                  />
                  <label className="form-check-label" htmlFor="qna-public">
                    공개
                  </label>
                </div>
                <div className="form-check form-check-inline">
                  <input
                    className="form-check-input"
                    type="radio"
                    name="visibility"
                    id="qna-private"
                    value="비공개"
                    checked={selectedTag === '비공개'}
                    onChange={(e) => {
                      setSelectedTag(e.target.value);
                      setErrors((prev) => ({ ...prev, tag: '' }));
                    }}
                  />
                  <label className="form-check-label" htmlFor="qna-private">
                    비공개
                  </label>
                </div>
              </div>
              {errors.tag && <div className="text-danger small mt-1">{errors.tag}</div>}
            </div>
          )}

          <div className="mb-3">
            <label className="form-label">내용</label>
            <textarea
              className="form-control"
              rows="10"
              value={content}
              onChange={(e) => {
                setContent(e.target.value);
                setErrors((prev) => ({ ...prev, content: '' }));
              }}
              placeholder="궁금하신 내용을 입력해주세요."
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
              목록으로
            </button>
            <button type="submit" className="btn btn-primary" disabled={isLoading}>
              {isLoading ? '등록 중...' : '등록하기'}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
