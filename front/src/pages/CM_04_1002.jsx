import { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import TiptapEditor from '../components/editor/EditorContainer';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import '../styles/CM_04_1002.css';
import { useAuth } from '../contexts/AuthContext';
import {
  fetchOrdersWithProducts,
  createReview,
  fetchReviewDetail,
  updateReview,
  uploadReviewEditorImage,
  buildImageUrl,
} from '../services/ReviewService';
import CMMessage from '../constants/CMMessage';

export default function CM_04_1002() {
  const navigate = useNavigate();
  const { writeId } = useParams();
  const { user } = useAuth();
  const isEditMode = Boolean(writeId);

  const [title, setTitle] = useState('');
  const [imagePreview, setImagePreview] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [content, setContent] = useState('');
  const [selectedTag, setSelectedTag] = useState('');
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [open1002, setOpen1002] = useState(false); // 로딩 팝업
  const [open1004, setOpen1004] = useState(false); // 완료 안내 팝업
  const [popupMessage, setPopupMessage] = useState(''); // 완료 메시지

  // 주문/상품 연동 상태
  const [selectedOrderNumber, setSelectedOrderNumber] = useState('');
  const [availableProducts, setAvailableProducts] = useState([]);
  const [orderList, setOrderList] = useState([]);
  const [selectedProductCode, setSelectedProductCode] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedProductName, setSelectedProductName] = useState('');
  const [currentReviewId, setCurrentReviewId] = useState(null);

  const titleRef = useRef(null);
  const tagRef = useRef(null);
  const orderRef = useRef(null);
  const productRef = useRef(null);
  const imageRef = useRef(null);
  const contentRef = useRef(null);

  const isReview = useMemo(() => selectedTag === '0', [selectedTag]); // 0=리뷰, 1=후기

  const tagLabel = useMemo(() => {
    if (selectedTag === '0') return '리뷰';
    if (selectedTag === '1') return '후기';
    return '';
  }, [selectedTag]);

  // 숫자형 userId 보정 (문자/undefined 방지)
  const numericUserId = useMemo(() => {
    const raw = user?.userId ?? user?.id;
    if (raw === undefined || raw === null || raw === '') return null;
    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : null;
  }, [user?.userId, user?.id]);

  const baseImageUrl = useMemo(() => {
    const raw = import.meta.env.VITE_API_BASE_IMAGE_URL || '';
    return raw.replace(/\/$/, '');
  }, []);

  const buildEditorImageUrl = useCallback(
    (relativePath) => {
      if (!relativePath) return '';
      const normalized = relativePath.replace(/^\//, '');
      if (!normalized) return '';
      return baseImageUrl ? `${baseImageUrl}/${normalized}` : `/${normalized}`;
    },
    [baseImageUrl]
  );

  const handleEditorImageAdd = useCallback(
    async (file) => {
      if (!file) return null;
      try {
        const relativePath = await uploadReviewEditorImage(file);
        if (!relativePath) {
          console.warn('에디터 이미지 업로드가 실패했습니다.');
          return null;
        }
        return buildEditorImageUrl(relativePath);
      } catch (error) {
        console.error('에디터 이미지 업로드 중 오류 발생:', error);
        return null;
      }
    },
    [buildEditorImageUrl]
  );

  const handleEditorContentChange = useCallback((value) => {
    setContent(value);
    setErrors((prev) => ({ ...prev, content: '' }));
  }, []);

  useEffect(() => {
    if (!isEditMode || !writeId) return;
    let isMounted = true;
    fetchReviewDetail(writeId)
      .then((res) => {
        if (!isMounted) return;
        const review = res?.resultList || null;
        if (!review) return;
        setCurrentReviewId(review.reviewId ?? Number(writeId));
        setTitle(review.title ?? '');
        setContent(review.content ?? '');
        setSelectedTag(
          review.reviewType !== undefined && review.reviewType !== null
            ? String(review.reviewType)
            : ''
        );
        setSelectedOrderNumber(review.orderNumber ?? '');
        setSelectedProductCode(review.productCode ?? '');
        setSelectedProductId(review.productId ? String(review.productId) : '');
        setSelectedProductName(review.productName ?? '');
        setImagePreview(review.imageUrl ? buildImageUrl(review.imageUrl) : '');
      })
      .catch((err) => {
        console.error('리뷰 상세 불러오기 실패:', err);
      });
    return () => {
      isMounted = false;
    };
  }, [isEditMode, writeId]);

  // 주문번호 + 상품 조회 API (GET)
  useEffect(() => {
    if (!user?.memberNumber) return;

    fetchOrdersWithProducts(user.memberNumber)
      .then((res) => {
        const list = Array.isArray(res) ? res : [];
        setOrderList(list);
      })
      .catch((err) => {
        console.error('주문/상품 불러오기 실패:', err);
      });
  }, [user, navigate]);

  // 주문 선택 시 상품 목록/선택 상태 세팅
  useEffect(() => {
    if (!selectedOrderNumber) {
      setSelectedOrder(null);
      setAvailableProducts([]);
      setSelectedProductCode('');
      setSelectedProductId('');
      setSelectedProductName('');
      return;
    }

    if (orderList.length === 0) {
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
      const matched = products.find((p) => p.productCode === selectedProductCode) || null;
      if (matched) {
        setSelectedProductId(matched.productId);
        setSelectedProductName(matched.productName || '');
      } else {
        setSelectedProductCode('');
        setSelectedProductId('');
        setSelectedProductName('');
      }
    }
  }, [selectedOrderNumber, orderList, selectedProductCode]);

  // 유효성 검사 함수
  const validateForm = () => {
    const newErrors = {};
    if (!title.trim()) newErrors.title = CMMessage.MSG_VAL_002;
    if (!selectedTag) newErrors.tag = CMMessage.MSG_VAL_003;
    if (!isReview && !selectedOrderNumber) newErrors.order = CMMessage.MSG_VAL_004;
    if (!isReview && !selectedProductCode) newErrors.product = CMMessage.MSG_VAL_005;
    if (!imageFile && !imagePreview) newErrors.image = CMMessage.MSG_VAL_006;
    if (!content.trim()) newErrors.content = CMMessage.MSG_VAL_007;
    return newErrors;
  };

  // postData 생성 함수
  const buildPostData = () => ({
    userId: numericUserId,
    memberNumber: user.memberNumber,
    orderNumber: selectedOrderNumber,
    productId: selectedProductId ? Number(selectedProductId) : null,
    productCode: selectedProductCode || null,
    reviewType: Number(selectedTag),
    title,
    imageUrl: '',
    content,
    createdBy: user.loginId,
    updatedBy: user.loginId,
  });

  // 리뷰 작성 API (POST)
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setOpen1002(true); // 로딩 팝업 표시

    const newErrors = validateForm();
    setErrors(newErrors);

    if (Object.keys(newErrors).length > 0) {
      setIsLoading(false);
      setOpen1002(false);

      if (newErrors.title && titleRef.current) {
        titleRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (newErrors.tag && tagRef.current) {
        tagRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (newErrors.order && orderRef.current) {
        orderRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (newErrors.product && productRef.current) {
        productRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (newErrors.image && imageRef.current) {
        imageRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (newErrors.content && contentRef.current) {
        contentRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }

      return;
    }

    if (!numericUserId || !user?.memberNumber) {
      alert(CMMessage.MSG_ERR_011);
      setIsLoading(false);
      setOpen1002(false); // 로딩 팝업 닫기
      navigate('/login');
      return;
    }

    try {
      const postData = buildPostData();

      const payload = {
        ...postData,
        file: imageFile || undefined,
      };

      if (isEditMode) {
        const targetId = currentReviewId || Number(writeId);
        const res = await updateReview(targetId, payload);
        setOpen1002(false);
        if (res?.result) {
          setPopupMessage(CMMessage.MSG_INF_007);
          setOpen1004(true);
        } else {
          alert(res?.message || CMMessage.MSG_ERR_007('수정'));
        }
      } else {
        // 리뷰 생성 (백엔드에서 메인 이미지/에디터 이미지 모두 처리)
        await createReview(payload);
        setOpen1002(false);
        setPopupMessage(CMMessage.MSG_INF_008);
        setOpen1004(true);
      }
    } catch (err) {
      console.error('리뷰 저장 실패:', err);
      setOpen1002(false);
      alert(err?.response?.data?.message || err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <h1 className="review-form-title">리뷰/후기 ({isEditMode ? '수정하기' : '작성하기'})</h1>
      <div className="pt-0 px-4 pb-5">
        <CM_99_1002
          isOpen={open1002}
          onClose={() => setOpen1002(false)}
          duration={3000}
          hideClose={true}
        />
        <CM_99_1004
          isOpen={open1004}
          onClose={() => {
            setOpen1004(false);
            if (isEditMode && (currentReviewId || writeId)) {
              navigate(`/review/${currentReviewId || writeId}`);
            } else {
              navigate('/review');
            }
          }}
          Message={
            popupMessage || (isEditMode ? CMMessage.MSG_INF_007 : CMMessage.MSG_INF_008)
          }
        />
        <form onSubmit={handleSubmit}>
          <div className="mb-3 w-50 review-title-field" ref={titleRef}>
            <label className="form-label">제목</label>
            <div className="input-group">
              {tagLabel && <span className="input-group-text">{tagLabel}</span>}
              <input
                className="form-control"
                value={title}
                onChange={(e) => {
                  setTitle(e.target.value);
                  setErrors((prev) => ({ ...prev, title: '' }));
                }}
              />
            </div>
            {errors.title && <div className="text-danger small mt-1">{errors.title}</div>}
          </div>

          <div className="mb-3" ref={tagRef}>
            <label className="form-label">옵션</label>
            <div>
              <div className="form-check form-check-inline">
                <input
                  className="form-check-input"
                  type="radio"
                  name="tag"
                  id="tag-review"
                  value="0"
                  checked={selectedTag === '0'}
                  onChange={(e) => {
                    setSelectedTag(e.target.value);
                    setErrors((prev) => ({ ...prev, tag: '' }));
                  }}
                />
                <label className="form-check-label" htmlFor="tag-review">
                  리뷰
                </label>
              </div>
              <div className="form-check form-check-inline">
                <input
                  className="form-check-input"
                  type="radio"
                  name="tag"
                  id="tag-comment"
                  value="1"
                  checked={selectedTag === '1'}
                  onChange={(e) => {
                    setSelectedTag(e.target.value);
                    setErrors((prev) => ({ ...prev, tag: '' }));
                  }}
                />
                <label className="form-check-label" htmlFor="tag-comment">
                  후기
                </label>
              </div>
            </div>
            {errors.tag && <div className="text-danger small mt-1">{errors.tag}</div>}
          </div>

          <div className="mb-3 w-100 d-flex gap-3">
            <div className="w-50" ref={orderRef}>
              <label className="form-label">
                {isReview ? '주문번호 (선택)' : '주문번호 (필수)'}
              </label>
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
                  {orderList.length === 0
                    ? '구매 내역이 없습니다'
                    : isReview
                      ? '선택 (선택 사항)'
                      : '주문번호를 선택해주세요'}
                </option>
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

            <div className="w-50" ref={productRef}>
              <label className="form-label">상품{isReview ? ' (주문 선택 시)' : ' (필수)'}</label>
              <select
                className="form-select"
                value={selectedProductCode}
                onChange={(e) => {
                  const code = e.target.value;
                  setSelectedProductCode(code);
                  const product = availableProducts.find((p) => p.productCode === code);
                  setSelectedProductId(product ? product.productId : '');
                  setSelectedProductName(product?.productName || '');
                  setErrors((prev) => ({ ...prev, product: '' }));
                }}
                disabled={availableProducts.length === 0}
              >
                <option value="">선택해주세요</option>
                {availableProducts.map((product) => (
                  <option key={product.productId} value={product.productCode}>
                    {product.productName}
                  </option>
                ))}
              </select>
              {errors.product && <div className="text-danger small mt-1">{errors.product}</div>}
            </div>
          </div>

          {selectedOrder && selectedProductCode && (
            <div className="alert alert-secondary small">
              <div>
                <strong>선택한 주문</strong> :{' '}
                {selectedOrder.displayOrderNumber || selectedOrder.orderNumber}
                {selectedOrder.displayOrderDate ? ` / ${selectedOrder.displayOrderDate}` : ''}
              </div>
              <div>
                <strong>선택한 상품</strong> : {selectedProductName || selectedProductCode}
              </div>
            </div>
          )}

          <div className="mb-3 w-100" ref={imageRef}>
            <label className="form-label">리뷰 메인 이미지</label>
            <input
              type="file"
              className="form-control"
              accept="image/*"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  setImageFile(file);
                  const reader = new FileReader();
                  reader.onloadend = () => {
                    if (reader.result && typeof reader.result === 'string') {
                      setImagePreview(reader.result);
                      setErrors((prev) => ({ ...prev, image: '' }));
                    }
                  };
                  reader.readAsDataURL(file);
                }
              }}
            />
            {imagePreview && (
              <div className="mt-2">
                <img
                  src={imagePreview}
                  alt="미리보기"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = '/no-image.png';
                  }}
                  style={{
                    width: '200px',
                    height: '200px',
                    objectFit: 'cover',
                    border: '1px solid #ccc',
                  }}
                />
              </div>
            )}
            {errors.image && <div className="text-danger small mt-1">{errors.image}</div>}
          </div>

          <div className="mb-3" ref={contentRef}>
            <label className="form-label">내용 입력</label>
            <TiptapEditor
              field="review"
              content={content}
              onContentChange={handleEditorContentChange}
              onImageAdd={handleEditorImageAdd}
              placeholder="리뷰/후기를 자유롭게 입력해주세요."
              error={errors.content}
            />
          </div>

          <div className="d-grid d-md-flex gap-3 mt-5">
            <button
              type="submit"
              className="btn btn-primary btn-lg w-100 w-md-50"
              disabled={isLoading}
            >
              {isLoading
                ? isEditMode
                  ? '수정 중...'
                  : '작성 중...'
                : isEditMode
                  ? '수정 완료'
                  : '작성 완료'}
            </button>
            <button
              type="button"
              className="btn btn-outline-secondary btn-lg w-100 w-md-50"
              onClick={() => navigate(-1)}
              disabled={isLoading}
            >
              뒤로 가기
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
