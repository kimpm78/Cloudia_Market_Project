import { useEffect, useState, useCallback, useMemo, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import TiptapEditor from '../components/editor/EditorContainer';
import CMMessage from '../constants/CMMessage';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import Select from 'react-select';
import '../styles/CM_90_1060.css';
import CM200200 from '../images/common/CM-200200.png';

const INITIAL_FORM_STATE = {
  categoryGroup: [],
  category: [],
  productCode: '',
  productCategory: '',
  productName: '',
  productPrice: 0,
  purchasePrice: '',
  availableQty: 0,
  expectedDeliveryDate: '',
  reservationDeadline: '',
  productFile: '',
  detailImages: [],
  productnote: '',
  weight: 0,
  purchaseLimit: 0,
};

const IMAGE_CONSTRAINTS = {
  MIN_WIDTH: 200,
  MIN_HEIGHT: 200,
  DETAIL_MIN_WIDTH: 150,
  DETAIL_MIN_HEIGHT: 150,
  ALLOWED_TYPES: ['image/jpeg', 'image/png'],
  MAX_DETAIL_IMAGES: 20,
};

const PRODUCT_CATEGORY_LABEL = {
  1: '通常販売',
  2: '予約販売',
};

const formatPrice = (value) => {
  return value !== null && value !== undefined ? Number(value).toLocaleString() : '0';
};

const useModal = () => {
  const [modals, setModals] = useState({
    confirm: false,
    loading: false,
    error: false,
    info: false,
  });
  const [message, setMessage] = useState('');

  const open = useCallback((type, msg = '') => {
    setMessage(msg);
    setModals((prev) => ({ ...prev, [type]: true }));
  }, []);

  const close = useCallback((type) => {
    setModals((prev) => ({ ...prev, [type]: false }));
  }, []);

  return { modals, message, open, close };
};

const useApiHandler = (navigate, openModal, closeModal) => {
  return useCallback(
    async (apiCall, showLoading = true) => {
      if (showLoading) openModal('loading');

      try {
        const result = await apiCall();
        return result;
      } catch (error) {
        openModal('error', CMMessage.MSG_ERR_001);
        return null;
      } finally {
        if (showLoading) closeModal('loading');
      }
    },
    [navigate, openModal, closeModal]
  );
};

const useApiData = (navigate, apiHandler) => {
  const [productOptions, setProductOptions] = useState([]);
  const [categoryGroups, setCategoryGroups] = useState([]);
  const [categories, setCategories] = useState([]);

  const fetchInitialData = useCallback(async () => {
    const [productResult, categoryGroupResult] = await Promise.all([
      apiHandler(() => axiosInstance.get('/admin/product/stockCode')),
      apiHandler(() => axiosInstance.get('/admin/product/categoryGroupCode')),
    ]);

    if (productResult.data.result && categoryGroupResult.data.result) {
      const formattedGroups = categoryGroupResult.data.resultList.map((group) => ({
        value: group.categoryGroupCode,
        label: group.categoryGroupName,
      }));
      setProductOptions(productResult.data.resultList || []);
      setCategoryGroups(formattedGroups || []);
    }
  }, [navigate, apiHandler]);

  const fetchCategories = useCallback(
    async (categoryGroupCodes) => {
      if (categoryGroupCodes.length === 0) {
        setCategories([]);
        return;
      }

      const result = await apiHandler(() =>
        axiosInstance.post('/admin/product/findCategory', categoryGroupCodes)
      );

      if (result.data.result) {
        const formattedCategories = result.data.resultList.map((group) => ({
          value: `${group.categoryGroupCode}-${group.categoryCode}`,
          label: group.categoryName,
        }));
        setCategories(formattedCategories);
      } else {
        setCategories([]);
      }
    },
    [navigate, apiHandler]
  );

  return {
    productOptions,
    categoryGroups,
    categories,
    fetchInitialData,
    fetchCategories,
  };
};

const SelectField = ({
  id,
  label,
  value,
  onChange,
  options = [],
  error,
  placeholder = '選択してください',
  ...props
}) => (
  <div className="mb-3">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <select
      id={id}
      className="form-select"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      {...props}
    >
      <option value="">{placeholder}</option>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const InputField = ({ id, label, value, onChange, error, ...props }) => (
  <div className="mb-3">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <input
      id={id}
      className="form-control"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      {...props}
    />
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const InputGroup = ({ children }) => (
  <div className="mb-3">
    <div className="d-flex gap-4">{children}</div>
  </div>
);

const InputGroupItem = ({ id, label, value, onChange, error, ...props }) => (
  <div className="d-flex flex-column flex-grow-1">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <input
      id={id}
      className="form-control"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      {...props}
    />
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const CategorySelectGroup = ({
  categoryGroup,
  category,
  categoryGroups,
  categories,
  onCategoryGroupChange,
  onCategoryChange,
  error,
}) => (
  <div className="mb-3">
    <label className="form-label">カテゴリ</label>
    <div className="d-flex gap-2">
      <Select
        isMulti
        value={categoryGroup}
        options={categoryGroups}
        className="flex-grow-1 select-unified"
        classNamePrefix="select"
        onChange={onCategoryGroupChange}
        placeholder="選択してください"
      />
      <Select
        isMulti
        value={category}
        options={categories}
        className="flex-grow-1 select-unified"
        classNamePrefix="select"
        onChange={onCategoryChange}
        placeholder="選択してください"
      />
    </div>
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const ImageUploadField = ({ id, label, file, preview, onChange, error }) => (
  <div className="mb-3">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <input
      type="file"
      className="form-control"
      id={id}
      accept="image/png, image/jpeg"
      onChange={onChange}
    />
    <img
      src={preview}
      className="img-thumbnail mt-2"
      alt="商品画像"
      style={{ width: '200px', height: '200px', objectFit: 'cover' }}
    />
    <div>
      ※最小サイズ <b>200 × 200 ピクセル</b>を推奨します。
    </div>
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const DetailImageUpload = ({ images, onChange, error, onError }) => {
  const fileInputRef = useRef(null);
  const dropZoneRef = useRef(null);
  const [isDragging, setIsDragging] = useState(false);
  const MAX_IMAGES = IMAGE_CONSTRAINTS.MAX_DETAIL_IMAGES;

  const validateAndProcessFiles = (files) => {
    if (images.length + files.length > MAX_IMAGES) {
      onError(`画像は最大 ${MAX_IMAGES} 枚までアップロードできます。`);
      return;
    }

    onError('');

    // 全画像をまとめて処理するための Promise 配列
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
                `詳細画像は最小 ${IMAGE_CONSTRAINTS.DETAIL_MIN_WIDTH}×${IMAGE_CONSTRAINTS.DETAIL_MIN_HEIGHT} ピクセル以上である必要があります。 ` +
                  `(現在の画像: ${image.width}×${image.height})`
              );
              return;
            }

            const newImage = {
              id: Date.now() + index,
              file: file,
              name: file.name,
              src: e.target.result,
            };

            resolve(newImage);
          };
          image.onerror = () => reject('画像の読み込みに失敗しました');
          image.src = e.target.result;
        };
        reader.onerror = () => reject('ファイルの読み込みに失敗しました');
        reader.readAsDataURL(file);
      });
    });

    // 全画像の処理後にまとめて状態更新
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

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleUploadClick = () => {
    if (images.length >= MAX_IMAGES) {
      onError(`画像は最大 ${MAX_IMAGES} 枚までアップロードできます。`);
      return;
    }
    onError('');
    fileInputRef.current?.click();
  };

  const handleRemoveImage = (imageId) => {
    onChange(images.filter((img) => img.id !== imageId));
  };

  // ドラッグ＆ドロップのイベントハンドラー
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

    if (images.length >= MAX_IMAGES) {
      onError(`画像は最大 ${MAX_IMAGES} 枚までアップロードできます。`);
      return;
    }

    const files = Array.from(e.dataTransfer.files).filter((file) => file.type.startsWith('image/'));

    if (files.length === 0) {
      onError('画像ファイルのみアップロードできます。');
      return;
    }

    validateAndProcessFiles(files);
  };

  return (
    <div className="mb-3">
      {/* ヘッダー */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <label className="form-label mb-0">
          詳細画像 <span className="text-muted">({images.length}枚)</span>
        </label>
        <button
          type="button"
          className="btn btn-outline-primary d-flex align-items-center gap-2"
          onClick={handleUploadClick}
          disabled={images.length >= MAX_IMAGES}
        >
          <i className="bi bi-upload"></i>
          Upload
        </button>
      </div>

      {/* 非表示のファイル入力 */}
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
        style={{
          minHeight: '100px',
          maxHeight: '400px',
          overflowY: 'auto',
          transition: 'all 0.3s ease',
          position: 'relative',
        }}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
      >
        {/* ドラッグオーバーレイ */}
        {isDragging && (
          <div
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              backgroundColor: 'rgba(13, 110, 253, 0.1)',
              border: '2px dashed #0d6efd',
              borderRadius: '0.375rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 10,
              pointerEvents: 'none',
            }}
          >
            <div className="text-center">
              <i className="bi bi-cloud-upload fs-1 text-primary"></i>
              <p className="mt-2 mb-0 text-primary fw-bold">ここに画像をドロップしてください</p>
            </div>
          </div>
        )}

        {images.length === 0 ? (
          <div className="text-center py-4 text-muted">
            <i className="bi bi-image fs-1 opacity-25"></i>
            <p className="mt-2 mb-0">アップロードされた画像はありません。</p>
            <p className="mb-0">
              <small>ファイルをドラッグするか、Upload ボタンをクリックしてください</small>
            </p>
          </div>
        ) : (
          images.map((image) => (
            <div key={image.id} className="d-flex align-items-center p-2 border-bottom">
              {/* 画像サムネイル */}
              <div className="me-3" style={{ flexShrink: 0 }}>
                <img
                  src={image.src}
                  alt={image.name}
                  className="rounded"
                  style={{
                    width: '60px',
                    height: '60px',
                    objectFit: 'cover',
                    border: '1px solid #dee2e6',
                  }}
                />
              </div>

              {/* ファイル名 */}
              <div className="flex-grow-1">
                <span className="text-primary fw-medium d-block">{image.name}</span>
                <small className="text-muted">{(image.file.size / 1024).toFixed(1)} KB</small>
              </div>

              {/* 削除ボタン */}
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={() => handleRemoveImage(image.id)}
                title="画像を削除"
              >
                <i className="bi bi-trash3"></i>
              </button>
            </div>
          ))
        )}
      </div>

      {/* 進捗表示 */}
      {images.length > 0 && (
        <div className="mt-2">
          <div className="d-flex justify-content-between align-items-center mb-1">
            <small className="text-muted">
              アップロード進捗: {images.length} / {MAX_IMAGES}
            </small>
            <small className="text-muted">{((images.length / MAX_IMAGES) * 100).toFixed(0)}%</small>
          </div>
          <div className="progress" style={{ height: '4px' }}>
            <div
              className="progress-bar"
              role="progressbar"
              style={{ width: `${(images.length / MAX_IMAGES) * 100}%` }}
            ></div>
          </div>
        </div>
      )}
      {/* 画像一覧 */}
      <div>
        ※最小サイズ <b>150 × 150 ピクセル</b>を推奨します。
      </div>
      {error && <div className="text-danger mt-1">{error}</div>}
    </div>
  );
};

export default function CM_90_1061() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);
  const apiData = useApiData(navigate, apiHandler);
  const [form, setForm] = useState(INITIAL_FORM_STATE);
  const [preview, setPreview] = useState(null);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    setPreview(CM200200);
    apiData.fetchInitialData();
  }, [apiData.fetchInitialData]);

  const updateForm = useCallback((field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const handlePriceChange = useCallback(
    (field) => (value) => {
      const cleanValue = value.replace(/,/g, '');

      if (cleanValue === '') {
        updateForm(field, 0);
        return;
      }

      if (/^\d*$/.test(cleanValue)) {
        updateForm(field, Number(cleanValue));
      }
    },
    [updateForm]
  );

  const handleFileChange = useCallback(
    (e) => {
      const file = e.target.files[0];
      if (!file) return;

      if (!IMAGE_CONSTRAINTS.ALLOWED_TYPES.includes(file.type)) {
        open('info', CMMessage.MSG_ALERT_002);
        return;
      }

      const reader = new FileReader();
      reader.onload = (event) => {
        const image = new Image();
        image.onload = () => {
          if (
            image.width < IMAGE_CONSTRAINTS.MIN_WIDTH ||
            image.height < IMAGE_CONSTRAINTS.MIN_HEIGHT
          ) {
            open(
              'info',
              CMMessage.MSG_ALERT_003(
                IMAGE_CONSTRAINTS.MIN_WIDTH,
                IMAGE_CONSTRAINTS.MIN_HEIGHT,
                image.width,
                image.height
              )
            );
            updateForm('productFile', '');
            setPreview(null);
          } else {
            updateForm('productFile', file);
            setPreview(event.target.result);
          }
        };
        image.src = event.target.result;
      };
      reader.readAsDataURL(file);
    },
    [updateForm, open]
  );

  const handleDetailImagesChange = useCallback(
    (newImages) => {
      updateForm('detailImages', newImages);
    },
    [updateForm]
  );

  const handleDetailImageError = useCallback((errorMessage) => {
    setErrors((prev) => ({
      ...prev,
      detailImages: errorMessage,
    }));
  }, []);

  const handleProductCodeChange = useCallback(
    (code) => {
      const selected = apiData.productOptions.find((p) => p.productCode === code);
      const categoryCode = selected?.productCategory;
      setForm((prev) => ({
        ...prev,
        productCode: code,
        productName: selected?.productName || '',
        purchasePrice: selected?.price || '',
        availableQty: selected?.availableQty || '',
        productCategory: PRODUCT_CATEGORY_LABEL[categoryCode] || '',
      }));
    },
    [apiData.productOptions]
  );

  const handleCategoryGroupChange = useCallback(
    async (categoryGroupCode) => {
      updateForm('categoryGroup', categoryGroupCode);
      updateForm('category', []);

      const categoryGroupCodes = categoryGroupCode.map((item) => item.value);
      await apiData.fetchCategories(categoryGroupCodes);
    },
    [updateForm, apiData.fetchCategories]
  );

  const handleCategoryChange = useCallback(
    (categoryCode) => {
      updateForm('category', categoryCode);
    },
    [updateForm]
  );

  const handleEditorContentChange = useCallback(
    (content) => {
      updateForm('productnote', content);
    },
    [updateForm]
  );

  const validateForm = useCallback(() => {
    const newErrors = {};
    const MAX_INT = 2147483647;
    if (!form.categoryGroup || form.categoryGroup.length === 0) {
      newErrors.categoryGroup = 'カテゴリグループを選択してください。';
    }

    if (!form.category || form.category.length === 0) {
      newErrors.category = 'カテゴリを選択してください。';
    }

    if (!form.productName) {
      newErrors.productName = '商品を選択してください。';
    }

    if (PRODUCT_CATEGORY_LABEL[1] === form.productCategory) {
      if (form.expectedDeliveryDate) {
        newErrors.expectedDeliveryDate = '出荷予定月は空にしてください。';
      }
      if (form.reservationDeadline) {
        newErrors.reservationDeadline = '予約締切日は空にしてください。';
      }
    } else if (PRODUCT_CATEGORY_LABEL[2] === form.productCategory) {
      if (!form.expectedDeliveryDate) {
        newErrors.expectedDeliveryDate = '出荷予定月を入力してください。';
      }
      if (!form.reservationDeadline) {
        newErrors.reservationDeadline = '予約締切日を入力してください。';
      }
    }

    if (!form.productPrice || isNaN(form.productPrice) || Number(form.productPrice) <= 0) {
      newErrors.productPrice = '販売単価を入力してください。';
    } else if (form.productPrice > MAX_INT) {
      newErrors.productPrice = '販売単価が上限を超えています。';
    }

    if (!form.productFile) {
      newErrors.productFile = '画像を選択してください。';
    }

    if (!form.purchaseLimit) {
      newErrors.purchaseLimit = '最大購入数量を入力してください。';
    } else if (form.purchaseLimit > MAX_INT) {
      newErrors.purchaseLimit = '最大購入数量が上限を超えています。';
    }

    if (!form.weight) {
      newErrors.weight = '重量を入力してください。';
    } else if (form.weight > MAX_INT) {
      newErrors.weight = '重量が上限を超えています。';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [form]);

  const handleRegister = useCallback(
    async (e) => {
      e.preventDefault();

      if (!validateForm()) {
        open('info', '必須項目を確認してください。');
        return;
      }

      if (form.productPrice <= form.purchasePrice) {
        open('confirm', '仕入価格より金額が同じ、または低いです。');
        return;
      }

      await submitProduct();
    },
    [form, validateForm]
  );

  const submitProduct = async () => {
    try {
      const formData = new FormData();
      const grouped = {};
      formData.append('productCode', form.productCode);
      formData.append('productName', form.productName);
      formData.append('productPrice', form.productPrice);
      formData.append('purchasePrice', form.purchasePrice);
      formData.append('expectedDeliveryDate', form.expectedDeliveryDate);
      formData.append('reservationDeadline', form.reservationDeadline || '');
      formData.append('productnote', form.productnote);
      formData.append('weight', form.weight);
      formData.append('purchaseLimit', form.purchaseLimit);

      if (form.productFile) {
        formData.append('productFile', form.productFile);
      }

      form.detailImages.forEach((image, index) => {
        formData.append(`detailImages`, image.file);
      });

      form.category.forEach(({ value }) => {
        const [group, code] = value.split('-').map(String);
        if (!grouped[group]) {
          grouped[group] = [];
        }
        grouped[group].push(code);
      });

      const category = Object.entries(grouped).map(([key, values]) => ({
        [key]: values,
      }));
      formData.append('categoryGroup', JSON.stringify(category));
      const result = await apiHandler(() => axiosInstance.post('/admin/product/upload', formData));
      if (result.data.result) {
        open('info', CMMessage.MSG_INF_001);
      } else {
        open('error', result.data.message);
      }
    } catch (error) {
      open('error', '商品登録中にエラーが発生しました。');
    }
  };

  const handleModalClose = useCallback(() => {
    if (message === CMMessage.MSG_INF_001) {
      navigate(-1);
    }
    close('info');
  }, [message, close, navigate]);

  const productCodeOptions = useMemo(
    () =>
      apiData.productOptions.map((option) => ({
        value: option.productCode,
        label: option.productName,
      })),
    [apiData.productOptions]
  );

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">商品登録</h5>

        <form onSubmit={handleRegister}>
          {/* カテゴリ選択 */}
          <CategorySelectGroup
            categoryGroup={form.categoryGroup}
            category={form.category}
            categoryGroups={apiData.categoryGroups}
            categories={apiData.categories}
            onCategoryGroupChange={handleCategoryGroupChange}
            onCategoryChange={handleCategoryChange}
            error={errors.categoryGroup || errors.category}
          />
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
          <InputGroup>
            {/* 商品コード（自動入力） */}
            <InputGroupItem
              id="productCode"
              label="商品コード（自動入力）"
              value={form.productCode}
              onChange={() => {}}
              readOnly
              disabled
            />
            {/* 商品区分（自動入力） */}
            <InputGroupItem
              id="productCategory"
              label="商品区分（自動入力）"
              value={form.productCategory}
              onChange={() => {}}
              readOnly
              disabled
            />
          </InputGroup>
          {/* 商品情報グループ */}
          <InputGroup>
            <InputGroupItem
              id="availableQty"
              label="在庫数（自動入力）"
              type="text"
              value={form.availableQty}
              onChange={() => {}}
              readOnly
              disabled
            />
            <InputGroupItem
              id="purchasePrice"
              label="仕入価格（自動入力）"
              type="text"
              value={form.purchasePrice}
              onChange={() => {}}
              readOnly
              disabled
            />
          </InputGroup>

          {/* 価格情報グループ */}
          <InputGroup>
            <InputGroupItem
              id="productPrice"
              label="販売単価"
              type="text"
              value={formatPrice(form.productPrice)}
              onChange={handlePriceChange('productPrice')}
              error={errors.productPrice}
            />

            {/* 重量 */}
            <InputGroupItem
              id="weight"
              label="重量（g）"
              type="text"
              value={form.weight}
              onChange={handlePriceChange('weight')}
              error={errors.weight}
            />
          </InputGroup>

          {/* 購入数量 */}
          <InputField
            id="purchaseLimit"
            label="最大購入数量"
            type="text"
            value={form.purchaseLimit}
            onChange={handlePriceChange('purchaseLimit')}
            error={errors.purchaseLimit}
          />

          {/* 出荷予定月 */}
          <InputField
            id="expectedDeliveryDate"
            label="出荷予定月"
            type="month"
            value={form.expectedDeliveryDate}
            onChange={(value) => updateForm('expectedDeliveryDate', value)}
            error={errors.expectedDeliveryDate}
          />

          {/* 予約締切日 */}
          <InputField
            id="reservationDeadline"
            label="予約締切日"
            type="date"
            value={form.reservationDeadline}
            onChange={(value) => updateForm('reservationDeadline', value)}
            error={errors.reservationDeadline}
          />

          {/* 商品画像 */}
          <ImageUploadField
            id="productFile"
            label="商品画像"
            file={form.productFile}
            preview={preview}
            onChange={handleFileChange}
            error={errors.productFile}
          />

          {/* 詳細画像アップロード */}
          <DetailImageUpload
            images={form.detailImages}
            onChange={handleDetailImagesChange}
            error={errors.detailImages}
            onError={handleDetailImageError}
          />

          {/* 商品説明 */}
          <div className="mb-3">
            <label htmlFor="productnote" className="form-label">
              商品説明
            </label>
            <TiptapEditor
              field="product"
              onContentChange={handleEditorContentChange}
              error={errors.content}
            />
          </div>

          {/* ボタングループ */}
          <div className="d-flex justify-content-center gap-2">
            <button type="submit" className="btn btn-primary btn-fixed-width">
              登録
            </button>
            <Link to="/admin/products" className="btn btn-secondary btn-fixed-width">
              戻る
            </Link>
          </div>
        </form>

        {/* モーダルコンポーネント */}
        <CM_99_1001
          isOpen={modals.confirm}
          onClose={() => close('confirm')}
          onConfirm={async () => {
            close('confirm');
            await submitProduct();
          }}
          Message={message}
        />
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
        <CM_99_1004 isOpen={modals.info} onClose={handleModalClose} Message={message} />
      </div>
    </div>
  );
}