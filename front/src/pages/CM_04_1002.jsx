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
  const [open1002, setOpen1002] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [popupMessage, setPopupMessage] = useState('');

  // 注文・商品連携ステータス
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
  // 0＝レビュー、1＝口コミ
  const isReview = useMemo(() => selectedTag === '0', [selectedTag]);

  const tagLabel = useMemo(() => {
    if (selectedTag === '0') return 'レビュー';
    if (selectedTag === '1') return '口コミ';
    return '';
  }, [selectedTag]);

  // 数値型userIdの補正（文字列／undefinedの混入防止）
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
          console.warn('エディタ画像アップロードが失敗しました。');
          return null;
        }
        return buildEditorImageUrl(relativePath);
      } catch (error) {
        console.error('エディタ画像アップロード中にエラーが発生しました:', error);
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
        console.error('レビュー詳細取得失敗:', err);
      });
    return () => {
      isMounted = false;
    };
  }, [isEditMode, writeId]);

  // 注文番号＋商品取得API（GET）
  useEffect(() => {
    if (!user?.memberNumber) return;

    fetchOrdersWithProducts(user.memberNumber)
      .then((res) => {
        const list = Array.isArray(res) ? res : [];
        setOrderList(list);
      })
      .catch((err) => {
        console.error('注文/商品取得に失敗:', err);
      });
  }, [user, navigate]);

  // 注文選択時に商品一覧 / 選択状態をセット
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

  // バリデーション関数
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

  // postData 生成関数
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

  // レビュー作成 API (POST)
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setOpen1002(true);

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
      setOpen1002(false); // ローディングポップアップを閉じる
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
          alert(res?.message || CMMessage.MSG_ERR_007('修正'));
        }
      } else {
        // レビュー作成（バックエンドでメイン画像/エディタ画像を両方処理）
        await createReview(payload);
        setOpen1002(false);
        setPopupMessage(CMMessage.MSG_INF_008);
        setOpen1004(true);
      }
    } catch (err) {
      console.error('レビュー保存失敗:', err);
      setOpen1002(false);
      alert(err?.response?.data?.message || err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <h1 className="review-form-title m-5">
        レビュー/口コミ ({isEditMode ? '修正する' : '作成する'})
      </h1>
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
          Message={popupMessage || (isEditMode ? CMMessage.MSG_INF_007 : CMMessage.MSG_INF_008)}
        />
        <form onSubmit={handleSubmit}>
          <div className="mb-3 w-50 review-title-field" ref={titleRef}>
            <label className="form-label">タイトル</label>
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
            <label className="form-label">オプション</label>
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
                  レビュー
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
                  口コミ
                </label>
              </div>
            </div>
            {errors.tag && <div className="text-danger small mt-1">{errors.tag}</div>}
          </div>

          <div className="mb-3 w-100 d-flex gap-3">
            <div className="w-50" ref={orderRef}>
              <label className="form-label">
                {isReview ? '注文番号 (選択)' : '注文番号 (必須)'}
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
                    ? '購入した注文がありません'
                    : isReview
                      ? '選択 (選択事項)'
                      : '注文番号を選択してください'}
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
              {errors.order && <div className="text-danger small mt-1">{errors.order}</div>}
            </div>

            <div className="w-50" ref={productRef}>
              <label className="form-label">商品{isReview ? ' (注文選択時)' : ' (必須)'}</label>
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
                <option value="">選択してください</option>
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
                <strong>選択した注文</strong> :{' '}
                {selectedOrder.displayOrderNumber || selectedOrder.orderNumber}
                {selectedOrder.displayOrderDate ? ` / ${selectedOrder.displayOrderDate}` : ''}
              </div>
              <div>
                <strong>選択した商品</strong> : {selectedProductName || selectedProductCode}
              </div>
            </div>
          )}

          <div className="mb-3 w-100" ref={imageRef}>
            <label className="form-label">レビューメイン画像</label>
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
                  alt="レビュー画像プレビュー"
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
            <label className="form-label">内容入力</label>
            <TiptapEditor
              field="review"
              content={content}
              onContentChange={handleEditorContentChange}
              onImageAdd={handleEditorImageAdd}
              placeholder="レビューや口コミを自由に入力してください。"
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
                  ? '更新中...'
                  : '作成中...'
                : isEditMode
                  ? '更新完了'
                  : '作成完了'}
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
