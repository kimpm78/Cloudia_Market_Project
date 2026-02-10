import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { postRequest, getRequest } from '../services/axiosInstance';

import TiptapEditor from '../components/editor/EditorContainer';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

const INITIAL_FORM_STATE = {
  title: '',
  selectedTag: '0', // 0: 返品, 1: 交換
  orderNo: '',
  detailImages: [],
  content: '',
  selectedProducts: [],
};

const IMAGE_CONSTRAINTS = {
  MIN_WIDTH: 200,
  MIN_HEIGHT: 200,
  DETAIL_MIN_WIDTH: 150,
  DETAIL_MIN_HEIGHT: 150,
  ALLOWED_TYPES: ['image/jpeg', 'image/png'],
  MAX_DETAIL_IMAGES: 20,
};

const DetailImageUpload = ({ images, onChange, error, onError }) => {
  const fileInputRef = useRef(null);
  const dropZoneRef = useRef(null);
  const [isDragging, setIsDragging] = useState(false);
  const MAX_IMAGES = IMAGE_CONSTRAINTS.MAX_DETAIL_IMAGES;

  const validateAndProcessFiles = (files) => {
    if (images.length + files.length > MAX_IMAGES) {
      onError(`画像は最大${MAX_IMAGES}枚までアップロードできます。`);
      return;
    }

    onError('');

    const imagePromises = files.map((file, index) => {
      return new Promise((resolve, reject) => {
        if (!file.type.startsWith('image/')) {
          reject('画像ファイルのみアップロードできます。');
          return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
          const image = new Image();
          image.onload = () => {
            if (
              image.width < IMAGE_CONSTRAINTS.DETAIL_MIN_WIDTH ||
              image.height < IMAGE_CONSTRAINTS.DETAIL_MIN_HEIGHT
            ) {
              reject(
                `詳細画像は最低${IMAGE_CONSTRAINTS.DETAIL_MIN_WIDTH}x${IMAGE_CONSTRAINTS.DETAIL_MIN_HEIGHT}ピクセル以上である必要があります。` +
                  `(現在の画像: ${image.width}x${image.height})`
              );
              return;
            }

            const newImage = {
              id: Date.now() + index,
              file: file,
              name: file.name,
              src: e.target.result,
              isNew: true,
            };

            resolve(newImage);
          };
          image.onerror = () => reject('画像の読み込みに失敗しました。');
          image.src = e.target.result;
        };
        reader.onerror = () => reject('ファイルの読み込みに失敗しました。');
        reader.readAsDataURL(file);
      });
    });

    Promise.all(imagePromises)
      .then((newImages) => {
        onChange([...images, ...newImages]);
      })
      .catch((error) => {
        onError(error);
      });
  };

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    validateAndProcessFiles(files);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleRemoveImage = (imageId) => {
    onChange(images.filter((img) => img.id !== imageId));
  };

  const handleDragEnter = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };
  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.currentTarget === dropZoneRef.current && !e.currentTarget.contains(e.relatedTarget)) {
      setIsDragging(false);
    }
  };
  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };
  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    const files = Array.from(e.dataTransfer.files).filter((file) => file.type.startsWith('image/'));
    validateAndProcessFiles(files);
  };

  return (
    <div className="mb-3">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <label className="form-label mb-0 fw-bold">
          画像添付{' '}
          <span className="text-muted">
            ({images.length}/{MAX_IMAGES})
          </span>
        </label>
        <button
          type="button"
          className="btn btn-outline-primary btn-sm"
          onClick={() => fileInputRef.current?.click()}
        >
          <i className="bi bi-upload me-1"></i>アップロード
        </button>
      </div>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />

      <div
        ref={dropZoneRef}
        className={`border rounded p-3 ${isDragging ? 'border-primary bg-light' : ''}`}
        style={{ minHeight: '100px', maxHeight: '300px', overflowY: 'auto', position: 'relative' }}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
      >
        {images.length === 0 ? (
          <div className="text-center py-4 text-muted small">
            画像をドラッグするか、「アップロード」ボタンをクリックしてください。
          </div>
        ) : (
          images.map((image) => (
            <div key={image.id} className="d-flex align-items-center p-2 border-bottom">
              <img
                src={image.src}
                alt={image.name}
                className="rounded me-3"
                style={{ width: '50px', height: '50px', objectFit: 'cover' }}
              />
              <div className="flex-grow-1 small text-truncate">{image.name}</div>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm p-1"
                onClick={() => handleRemoveImage(image.id)}
              >
                <i className="bi bi-trash"></i>
              </button>
            </div>
          ))
        )}
      </div>
      {error && <div className="text-danger small mt-1">{error}</div>}
    </div>
  );
};

export default function CM_01_1016() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [form, setForm] = useState(INITIAL_FORM_STATE);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [orderList, setOrderList] = useState([]);
  const [availableProducts, setAvailableProducts] = useState([]);
  const [open1002, setOpen1002] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [popupMessage, setPopupMessage] = useState('');

  const typeLabel = form.selectedTag === '0' ? '返品' : '交換';

  const updateForm = useCallback(
    (field, value) => {
      setForm((prev) => ({ ...prev, [field]: value }));
      if (errors[field]) setErrors((prev) => ({ ...prev, [field]: '' }));
    },
    [errors]
  );

  const applyTemplate = useCallback(() => {
    const selectedOrder = orderList.find((o) => o.orderNumber === form.orderNo);
    const displayDate =
      selectedOrder?.orderDate || selectedOrder?.displayOrderDate || '(注文番号を選択してください)';

    const displayProduct =
      form.selectedProducts.length > 0
        ? form.selectedProducts.map((p) => `${p.productName} (${p.returnQty}個)`).join(', ')
        : '(商品を選択してください)';

    const formatHtml = `
      <p><strong>[ ${typeLabel} 申請書 ]</strong></p>
      <p><strong>交換/返品:</strong> ${typeLabel}</p> 
      <p><strong>注文番号:</strong> ${form.orderNo || '(注文番号を選択してください)'}</p>
      <p><strong>注文日:</strong> ${displayDate}</p>
      <p><strong>申請商品:</strong> ${displayProduct}</p>
      <p><strong>詳細内容:</strong> </p>
      <p>&nbsp;</p>
    `;
    updateForm('content', formatHtml);
  }, [form.selectedTag, form.orderNo, form.selectedProducts, orderList, updateForm]);

  useEffect(() => {
    if (user?.loginId) {
      getRequest('/user/returns/returnable')
        .then((res) => {
          if (Array.isArray(res)) {
            setOrderList(res);
          }
        })
        .catch((err) => {
          console.error('API呼び出しエラー:', err);
        });
    }
  }, [user?.loginId]);
  useEffect(() => {
    if (!form.content || form.content.includes('신청서 ]')) {
      applyTemplate();
    }
  }, [typeLabel, form.orderNo, form.selectedProducts]);

  const handleOrderChange = async (e) => {
    const orderNo = e.target.value;
    updateForm('orderNo', orderNo);
    updateForm('selectedProducts', []);

    if (orderNo) {
      try {
        const response = await getRequest(`/user/returns/order-products?orderNo=${orderNo}`);
        setAvailableProducts(response || []);
      } catch (err) {
        console.error('商品情報の読み込みに失敗しました:', err);
        setAvailableProducts([]);
      }
    } else {
      setAvailableProducts([]);
    }
  };

  const handleProductToggle = (product) => {
    const current = [...form.selectedProducts];
    const index = current.findIndex((p) => p.productCode === product.productCode);
    if (index > -1) {
      current.splice(index, 1);
    } else {
      current.push({
        productCode: product.productCode,
        productName: product.productName,
        orderQty: product.quantity,
        returnQty: product.quantity,
      });
    }
    updateForm('selectedProducts', current);
  };

  const handleQtyChange = (code, val) => {
    const current = [...form.selectedProducts];
    const item = current.find((p) => p.productCode === code);
    if (item) {
      let num = parseInt(val) || 0;

      // 注文数量を超過していないか確認
      if (num > item.orderQty) {
        num = item.orderQty;
      }

      if (num < 1) num = 1;
      item.returnQty = num;
      updateForm('selectedProducts', current);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!form.title.trim()) newErrors.title = 'タイトルを入力してください。';
    if (!form.orderNo) newErrors.orderNo = '注文番号をを選択してください。';
    if (form.selectedProducts.length === 0) newErrors.product = '商品を一つ以上選択してください。';
    if (form.content.length < 20) newErrors.content = '理由を詳細に入力してください。';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    setOpen1002(true);

    try {
      const formData = new FormData();
      formData.append('title', form.title);
      formData.append('type', form.selectedTag);
      formData.append('orderNo', form.orderNo);
      formData.append('content', form.content);

      const productCode = form.selectedProducts
        .map((p) => `${p.productCode}:${p.returnQty}`)
        .join(',');
      formData.append('productCode', productCode);

      form.detailImages.forEach((img) => {
        if (img.file) formData.append('files', img.file);
      });

      const response = await postRequest('/user/returns', formData);
      if (response) {
        setOpen1002(false);
        setPopupMessage(response);
        setOpen1004(true);
      }
    } catch (err) {
      setOpen1002(false);
      alert(err.message || 'エラーが発生しました。');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="return-request-container p-4">
      <h3 className="fw-bold mb-4 border-bottom pb-3">交換/返品申請</h3>
      <CM_99_1002 isOpen={open1002} onClose={() => setOpen1002(false)} />
      <CM_99_1004
        isOpen={open1004}
        onClose={() => navigate('/mypage/returns')}
        Message={popupMessage}
      />

      <form onSubmit={handleSubmit}>
        <div className="row g-4">
          <div className="col-md-7">
            <div className="mb-3">
              <label className="form-label fw-bold">タイトル</label>
              <input
                className="form-control"
                value={form.title}
                onChange={(e) => updateForm('title', e.target.value)}
                placeholder="交換または返品申請のタイトルを入力してください"
              />
              {errors.title && <small className="text-danger">{errors.title}</small>}
            </div>

            <div className="row mb-3">
              <div className="col-6">
                <label className="form-label fw-bold">区分</label>
                <select
                  className="form-select"
                  value={form.selectedTag}
                  onChange={(e) => updateForm('selectedTag', e.target.value)}
                >
                  <option value="0">返品</option>
                  <option value="1">交換</option>
                </select>
              </div>
              <div className="col-6">
                <label className="form-label fw-bold">注文番号</label>
                <select className="form-select" value={form.orderNo} onChange={handleOrderChange}>
                  <option value="">注文を選択</option>
                  {orderList.map((o, index) => {
                    return (
                      <option key={o.orderNo} value={o.orderNo}>
                        {o.orderNo}
                      </option>
                    );
                  })}
                </select>
                {errors.orderNo && <small className="text-danger">{errors.orderNo}</small>}
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label fw-bold">対象商品（数量選択）</label>
              <div
                className="border rounded p-3 bg-light"
                style={{ maxHeight: '200px', overflowY: 'auto' }}
              >
                {availableProducts.length > 0 ? (
                  availableProducts.map((p) => {
                    const sel = form.selectedProducts.find((s) => s.productCode === p.productCode);
                    return (
                      <div
                        key={p.productCode}
                        className="d-flex align-items-center justify-content-between mb-2 p-2 bg-white rounded border"
                      >
                        <div className="form-check m-0">
                          <input
                            className="form-check-input"
                            type="checkbox"
                            checked={!!sel}
                            onChange={() => handleProductToggle(p)}
                            id={p.productCode}
                          />
                          <label className="form-check-label small" htmlFor={p.productCode}>
                            {p.productName}
                          </label>
                          <div className="text-muted" style={{ fontSize: '0.8rem' }}>
                            注文: {p.quantity}個
                          </div>
                        </div>
                        {sel && (
                          <div className="d-flex align-items-center gap-1">
                            <span className="small text-primary fw-bold">{typeLabel}:</span>
                            <input
                              type="number"
                              className="form-control form-control-sm text-center"
                              style={{ width: '60px' }}
                              value={sel.returnQty}
                              min="1"
                              max={sel.orderQty}
                              onChange={(e) => handleQtyChange(p.productCode, e.target.value)}
                            />
                            <span className="small">個</span>
                          </div>
                        )}
                      </div>
                    );
                  })
                ) : (
                  <div className="text-center text-muted py-3 small">
                    先に注文番号を選択してください。
                  </div>
                )}
              </div>
              {errors.product && <small className="text-danger">{errors.product}</small>}
            </div>

            <DetailImageUpload
              images={form.detailImages}
              onChange={(imgs) => updateForm('detailImages', imgs)}
              error={errors.detailImages}
              onError={(msg) => updateForm('detailImagesError', msg)}
            />
          </div>
        </div>

        <div className="mt-4">
          <label className="form-label fw-bold">詳細内容</label>
          <TiptapEditor
            content={form.content}
            onContentChange={(val) => updateForm('content', val)}
          />
          {errors.content && <small className="text-danger">{errors.content}</small>}
        </div>

        <div className="d-flex gap-2 mt-5">
          <button
            type="submit"
            className="btn btn-primary flex-grow-1 fw-bold"
            disabled={isLoading}
          >
            {isLoading ? '処理中...' : '申請する'}
          </button>
          <button type="button" className="btn btn-light" onClick={() => navigate(-1)}>
            キャンセル
          </button>
        </div>
      </form>
    </div>
  );
}
