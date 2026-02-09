import { useEffect, useState, useCallback, useMemo, useRef } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
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
  1: '상시 판매',
  2: '예약 판매',
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
      if (!categoryGroupCodes || categoryGroupCodes.length === 0) {
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
    <label className="form-label">카테고리</label>
    <div className="d-flex gap-2">
      <Select
        isMulti
        value={categoryGroup}
        options={categoryGroups}
        className="flex-grow-1 select-unified"
        classNamePrefix="select"
        onChange={onCategoryGroupChange}
        placeholder="카테고리 그룹 선택"
      />
      <Select
        isMulti
        value={category}
        options={categories}
        className="flex-grow-1 select-unified"
        classNamePrefix="select"
        onChange={onCategoryChange}
        placeholder="카테고리 선택"
      />
    </div>
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const ImageUploadField = ({ id, label, file, preview, onChange, error, currentImageUrl }) => (
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
      src={preview || currentImageUrl}
      className="img-thumbnail mt-2"
      alt="상품 이미지"
      style={{ width: '200px', height: '200px', objectFit: 'cover' }}
    />
    <div className="text-muted small mt-1">
      ※최소사이즈 <b>200 X 200 픽셀</b>의 크기를 권장합니다.
    </div>
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const DetailImageUpload = ({ images, onChange, error, onError, isEdit = false }) => {
  const fileInputRef = useRef(null);
  const dropZoneRef = useRef(null);
  const [isDragging, setIsDragging] = useState(false);
  const MAX_IMAGES = IMAGE_CONSTRAINTS.MAX_DETAIL_IMAGES;

  const validateAndProcessFiles = (files) => {
    if (images.length + files.length > MAX_IMAGES) {
      onError(`이미지는 최대 ${MAX_IMAGES}개까지만 업로드할 수 있습니다.`);
      return;
    }

    onError('');

    // 모든 이미지를 한 번에 처리하기 위한 Promise 배열
    const imagePromises = files.map((file, index) => {
      return new Promise((resolve, reject) => {
        if (!file.type.startsWith('image/')) {
          reject('이미지 파일만 업로드 가능합니다.');
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
                `상세 이미지는 최소 ${IMAGE_CONSTRAINTS.DETAIL_MIN_WIDTH}x${IMAGE_CONSTRAINTS.DETAIL_MIN_HEIGHT} 픽셀 이상이어야 합니다. ` +
                  `(현재 이미지: ${image.width}x${image.height})`
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
          image.onerror = () => reject('이미지 로드 실패');
          image.src = e.target.result;
        };
        reader.onerror = () => reject('파일 읽기 실패');
        reader.readAsDataURL(file);
      });
    });

    // 모든 이미지 처리 후 한 번에 상태 업데이트
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
      onError(`이미지는 최대 ${MAX_IMAGES}개까지만 업로드할 수 있습니다.`);
      return;
    }
    onError('');
    fileInputRef.current?.click();
  };

  const handleRemoveImage = (imageId) => {
    onChange(images.filter((img) => img.id !== imageId));
  };

  // 드래그 앤 드롭 이벤트 핸들러
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
      onError(`이미지는 최대 ${MAX_IMAGES}개까지만 업로드할 수 있습니다.`);
      return;
    }

    const files = Array.from(e.dataTransfer.files).filter((file) => file.type.startsWith('image/'));

    if (files.length === 0) {
      onError('이미지 파일만 업로드 가능합니다.');
      return;
    }

    validateAndProcessFiles(files);
  };

  return (
    <div className="mb-3">
      {/* 헤더 */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <label className="form-label mb-0">
          상세 이미지 <span className="text-muted">({images.length}개)</span>
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

      {/* 숨겨진 파일 입력 */}
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
        {/* 드래그 오버레이 */}
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
              <p className="mt-2 mb-0 text-primary fw-bold">이미지를 여기에 드롭하세요</p>
            </div>
          </div>
        )}

        {images.length === 0 ? (
          <div className="text-center py-4 text-muted">
            <i className="bi bi-image fs-1 opacity-25"></i>
            <p className="mt-2 mb-0">업로드된 이미지가 없습니다.</p>
            <p className="mb-0">
              <small>파일을 드래그하거나 Upload 버튼을 클릭하세요</small>
            </p>
          </div>
        ) : (
          images.map((image) => (
            <div key={image.id} className="d-flex align-items-center p-2 border-bottom">
              {/* 이미지 썸네일 */}
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

              {/* 파일명 */}
              <div className="flex-grow-1">
                <span className="text-primary fw-medium d-block">{image.name}</span>
                <small className="text-muted">
                  {image.file ? `${(image.file.size / 1024).toFixed(1)} KB` : '기존 이미지'}
                  {image.isNew && <span className="badge bg-success ms-2">새 이미지</span>}
                </small>
              </div>

              {/* 삭제 버튼 */}
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={() => handleRemoveImage(image.id)}
                title="이미지 삭제"
              >
                <i className="bi bi-trash3"></i>
              </button>
            </div>
          ))
        )}
      </div>

      {/* 진행률 표시 */}
      {images.length > 0 && (
        <div className="mt-2">
          <div className="d-flex justify-content-between align-items-center mb-1">
            <small className="text-muted">
              업로드 진행률: {images.length} / {MAX_IMAGES}
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
      {/* 이미지 목록 */}
      <div>
        ※최소사이즈 <b>150 X 150 픽셀</b>의 크기를 권장합니다.
      </div>
      {error && <div className="text-danger mt-1">{error}</div>}
    </div>
  );
};

export default function CM_90_1062() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);
  const apiData = useApiData(navigate, apiHandler);

  const [form, setForm] = useState(INITIAL_FORM_STATE);
  const [preview, setPreview] = useState(null);
  const [errors, setErrors] = useState({});
  const [productInfo, setProductInfo] = useState(null);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [currentImageUrl, setCurrentImageUrl] = useState('');
  const [isInitializing, setIsInitializing] = useState(false);
  const [originalDetailImages, setOriginalDetailImages] = useState([]);

  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      if (isInitializing) return;

      setIsInitializing(true);

      try {
        await apiData.fetchInitialData();
        if (productId) {
          const productData = await apiHandler(() =>
            axiosInstance.get('/admin/product/findByProductCode', {
              params: {
                productId: productId,
              },
            })
          );
          if (productData.data.result && isMounted) {
            setProductInfo(productData.data.resultList);
          }
        }
      } catch (error) {
        if (isMounted) {
          open('error', '데이터를 불러오는 중 오류가 발생했습니다.');
        }
      } finally {
        if (isMounted) {
          setIsDataLoaded(true);
          setIsInitializing(false);
        }
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [productId]);

  useEffect(() => {
    if (!productInfo || !isDataLoaded || apiData.categoryGroups.length === 0) return;

    let isMounted = true;

    const setupFormData = async () => {
      try {
        let categoryGroupCodes = [];

        if (productInfo.category) {
          const categoryData = JSON.parse(productInfo.category);
          if (Array.isArray(categoryData)) {
            categoryData.forEach((item) => {
              Object.keys(item).forEach((groupCode) => {
                if (!categoryGroupCodes.includes(groupCode)) {
                  categoryGroupCodes.push(groupCode);
                }
              });
            });
          }
        }

        if (categoryGroupCodes.length > 0) {
          await apiData.fetchCategories(categoryGroupCodes);
        }

        if (!isMounted) return;

        const imageUrl = productInfo.productFile
          ? `${import.meta.env.VITE_API_BASE_IMAGE_URL}` + '/' + `${productInfo.productFile}`
          : CM200200;
        setCurrentImageUrl(imageUrl);

        let detailImagesData = [];
        if (productInfo.detailImages && Array.isArray(productInfo.detailImages)) {
          detailImagesData = productInfo.detailImages.map((imagePath, index) => {
            const fileName = imagePath.split('/').pop() || `detail_image_${index + 1}.jpg`;
            const imageUrl = `${import.meta.env.VITE_API_BASE_IMAGE_URL}/${imagePath}`;

            return {
              id: `existing_${index}`,
              name: fileName,
              src: imageUrl,
              path: imagePath,
              isNew: false,
            };
          });
        }

        setOriginalDetailImages(detailImagesData);
        const categoryCode = productInfo?.productCategory;
        setForm((prev) => ({
          ...prev,
          productId: productId,
          productCode: productInfo.productCode || '',
          productName: productInfo.productName || '',
          productCategory: PRODUCT_CATEGORY_LABEL[categoryCode] || '',
          productPrice: productInfo.productPrice || 0,
          purchasePrice: productInfo.purchasePrice || '',
          availableQty: productInfo.availableQty || 0,
          expectedDeliveryDate: productInfo.expectedDeliveryDate || '',
          reservationDeadline: productInfo.reservationDeadline || '',
          productnote: productInfo.productnote || '',
          weight: productInfo.weight || 0,
          purchaseLimit: productInfo.purchaseLimit || 0,
          productFile: '',
          detailImages: detailImagesData,
        }));
      } catch (error) {
        console.error('Form setup error:', error);
      }
    };

    setupFormData();

    return () => {
      isMounted = false;
    };
  }, [productInfo, isDataLoaded, apiData.categoryGroups.length]);

  useEffect(() => {
    if (!productInfo || !productInfo.category || apiData.categories.length === 0) return;

    try {
      const categoryData = JSON.parse(productInfo.category);
      let parsedCategoryGroup = [];
      let parsedCategory = [];

      if (Array.isArray(categoryData)) {
        categoryData.forEach((item) => {
          Object.entries(item).forEach(([groupCode, categoryCodes]) => {
            const foundGroup = apiData.categoryGroups.find((g) => g.value === groupCode);
            if (foundGroup && !parsedCategoryGroup.some((pg) => pg.value === groupCode)) {
              parsedCategoryGroup.push(foundGroup);
            }

            if (Array.isArray(categoryCodes)) {
              categoryCodes.forEach((categoryCode) => {
                const foundCategory = apiData.categories.find(
                  (c) => c.value === `${groupCode}-${categoryCode}`
                );
                if (
                  foundCategory &&
                  !parsedCategory.some((pc) => pc.value === foundCategory.value)
                ) {
                  parsedCategory.push(foundCategory);
                }
              });
            }
          });
        });
      }

      setForm((prev) => ({
        ...prev,
        categoryGroup: parsedCategoryGroup,
        category: parsedCategory,
      }));
    } catch (error) {
      open('error', CMMessage.MSG_ERR_001);
    }
  }, [productInfo?.category, apiData.categories.length, apiData.categoryGroups]);

  const updateForm = useCallback(
    (field, value) => {
      setForm((prev) => ({ ...prev, [field]: value }));
      if (errors[field]) {
        setErrors((prev) => ({ ...prev, [field]: '' }));
      }
    },
    [errors]
  );

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

  const handleCategoryGroupChange = useCallback(
    async (selectedCategoryGroups) => {
      updateForm('categoryGroup', selectedCategoryGroups || []);
      updateForm('category', []);

      if (selectedCategoryGroups && selectedCategoryGroups.length > 0) {
        const categoryGroupCodes = selectedCategoryGroups.map((item) => item.value);
        await apiData.fetchCategories(categoryGroupCodes);
      } else {
        await apiData.fetchCategories([]);
      }
    },
    [updateForm, apiData]
  );

  const handleCategoryChange = useCallback(
    (selectedCategories) => {
      updateForm('category', selectedCategories || []);
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
      newErrors.categoryGroup = '카테고리 그룹을 선택하세요.';
    }

    if (!form.category || form.category.length === 0) {
      newErrors.category = '카테고리를 선택하세요.';
    }

    if (!form.productName) {
      newErrors.productName = '상품명을 입력하세요.';
    }

    if (PRODUCT_CATEGORY_LABEL[1] === form.productCategory) {
      if (form.expectedDeliveryDate) {
        newErrors.expectedDeliveryDate = '출고일을 비워주세요.';
      }
      if (form.reservationDeadline) {
        newErrors.reservationDeadline = '예약일을 비워주세요.';
      }
    } else if (PRODUCT_CATEGORY_LABEL[2] === form.productCategory) {
      if (!form.expectedDeliveryDate) {
        newErrors.expectedDeliveryDate = '출고일을 입력해주세요.';
      }
      if (!form.reservationDeadline) {
        newErrors.reservationDeadline = '예약일을 입력해주세요.';
      }
    }

    if (!form.productPrice || isNaN(form.productPrice) || Number(form.productPrice) <= 0) {
      newErrors.productPrice = '판매가를 입력하세요.';
    } else if (form.productPrice > MAX_INT) {
      newErrors.productPrice = '최대 판매 가격을 넘었습니다.';
    }

    if (!form.purchaseLimit) {
      newErrors.purchaseLimit = '구매 수량을 입력해주세요.';
    } else if (form.purchaseLimit > MAX_INT) {
      newErrors.purchaseLimit = '구매 최대 수량을 넘었습니다.';
    }
    if (!form.weight) {
      newErrors.weight = '중량을 입력해주세요.';
    } else if (form.weight > MAX_INT) {
      newErrors.weight = '최대 중량을 넘었습니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [form]);

  const handleRegister = useCallback(
    async (e) => {
      e.preventDefault();

      if (!validateForm()) {
        open('info', '필수 항목을 확인해주세요.');
        return;
      }
      if (form.productPrice <= form.purchasePrice) {
        open('confirm', '사입가 보다 금액이 같거나 낮습니다.');
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

      formData.append('productId', productId);
      formData.append('productCode', form.productCode);
      formData.append('productName', form.productName);
      formData.append('productPrice', form.productPrice);
      formData.append('purchasePrice', form.purchasePrice || '');
      formData.append('expectedDeliveryDate', form.expectedDeliveryDate);
      formData.append('reservationDeadline', form.reservationDeadline || '');
      formData.append('productnote', form.productnote || '');
      formData.append('weight', form.weight || 0);
      formData.append('purchaseLimit', form.purchaseLimit || 0);
      if (form.productFile && form.productFile instanceof File) {
        formData.append('productFile', form.productFile);
      }

      if (form.detailImages && form.detailImages.length > 0) {
        form.detailImages.forEach((image) => {
          if (image.isNew && image.file) {
            formData.append('detailImages', image.file);
          }
        });

        const remainingExistingImages = form.detailImages
          .filter((image) => !image.isNew && image.path)
          .map((image) => image.path);

        remainingExistingImages.forEach((imagePath) => {
          formData.append('existingDetailImages', imagePath);
        });

        const deletedImages = originalDetailImages
          .filter(
            (originalImage) =>
              !form.detailImages.some(
                (currentImage) => !currentImage.isNew && currentImage.path === originalImage.path
              )
          )
          .map((image) => image.path);

        deletedImages.forEach((imagePath) => {
          formData.append('deletedDetailImages', imagePath);
        });
      } else {
        if (originalDetailImages.length > 0) {
          originalDetailImages.forEach((image) => {
            formData.append('deletedDetailImages', image.path);
          });
        }
      }

      if (form.category && form.category.length > 0) {
        form.category.forEach(({ value }) => {
          const [group, code] = value.split('-');
          if (!grouped[group]) {
            grouped[group] = [];
          }
          grouped[group].push(code);
        });

        const category = Object.entries(grouped).map(([key, values]) => ({
          [key]: values,
        }));
        formData.append('categoryGroup', JSON.stringify(category));
      }

      const result = await apiHandler(
        () => axiosInstance.post('/admin/product/update', formData),
        true
      );

      if (result.data.result) {
        open('info', CMMessage.MSG_INF_001);
      } else {
        open('error', result.data.message);
      }
    } catch (error) {
      open('error', '상품 수정 중 오류가 발생했습니다.');
    }
  };

  const handleModalClose = useCallback(() => {
    if (message === CMMessage.MSG_INF_001) {
      navigate(-1);
    }
    close('info');
  }, [message, close, navigate]);

  if (!isDataLoaded || isInitializing) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">상품 수정</h5>

        <form onSubmit={handleRegister}>
          {/* 카테고리 선택 */}
          <CategorySelectGroup
            categoryGroup={form.categoryGroup}
            category={form.category}
            categoryGroups={apiData.categoryGroups}
            categories={apiData.categories}
            onCategoryGroupChange={handleCategoryGroupChange}
            onCategoryChange={handleCategoryChange}
            error={errors.categoryGroup || errors.category}
          />

          {/* 상품명 */}
          <InputField
            id="productName"
            label="상품명"
            type="text"
            value={form.productName}
            error={errors.productName}
            onChange={() => {}}
            readOnly
            disabled
          />

          <InputGroup>
            {/* 상품코드 (자동 입력)  */}
            <InputGroupItem
              id="productCode"
              label="상품코드 (자동 입력)"
              value={form.productCode}
              onChange={() => {}}
              readOnly
              disabled
            />
            {/* 상품분류 (자동 입력)  */}
            <InputGroupItem
              id="productCategory"
              label="상품분류 (자동 입력)"
              value={form.productCategory}
              onChange={() => {}}
              readOnly
              disabled
            />
          </InputGroup>

          {/* 상품 정보 그룹 */}
          <InputGroup>
            <InputGroupItem
              id="availableQty"
              label="상품 재고(자동으로 입력)"
              type="text"
              value={form.availableQty}
              onChange={() => {}}
              readOnly
              disabled
            />
            <InputGroupItem
              id="purchasePrice"
              label="상품 사입가(자동으로 입력)"
              type="text"
              value={form.purchasePrice}
              onChange={() => {}}
              readOnly
              disabled
            />
          </InputGroup>

          {/* 가격 정보 그룹 */}
          <InputGroup>
            <InputGroupItem
              id="productPrice"
              label="판매 단가"
              type="text"
              value={formatPrice(form.productPrice)}
              onChange={handlePriceChange('productPrice')}
              error={errors.productPrice}
            />
            {/* 중량 */}
            <InputGroupItem
              id="weight"
              label="중량 (g)"
              type="text"
              value={formatPrice(form.weight)}
              onChange={handlePriceChange('weight')}
              error={errors.weight}
            />
          </InputGroup>

          {/* 구매 수량 */}
          <InputField
            id="purchaseLimit"
            label="최대 구매 수량"
            type="text"
            value={form.purchaseLimit}
            onChange={handlePriceChange('purchaseLimit')}
            error={errors.purchaseLimit}
          />

          {/* 출고 예정일 */}
          <InputField
            id="expectedDeliveryDate"
            label="출고 예정일"
            type="month"
            value={form.expectedDeliveryDate}
            onChange={(value) => updateForm('expectedDeliveryDate', value)}
            error={errors.expectedDeliveryDate}
          />

          {/* 예약 마감일 */}
          <InputField
            id="reservationDeadline"
            label="예약 마감일"
            type="date"
            value={form.reservationDeadline}
            onChange={(value) => updateForm('reservationDeadline', value)}
          />

          {/* 상품 이미지 */}
          <ImageUploadField
            id="productFile"
            label="상품 이미지"
            file={form.productFile}
            preview={preview}
            currentImageUrl={currentImageUrl}
            onChange={handleFileChange}
            error={errors.productFile}
          />

          {/* 상세 이미지 업로드 */}
          <DetailImageUpload
            images={form.detailImages}
            onChange={handleDetailImagesChange}
            error={errors.detailImages}
            onError={handleDetailImageError}
            isEdit={true}
          />

          {/* 상품 설명 */}
          <div className="mb-3">
            <label htmlFor="productnote" className="form-label">
              상품 설명
            </label>
            <TiptapEditor
              key={`editor-${productInfo?.productCode || 'new'}-${isDataLoaded ? 'loaded' : 'loading'}`}
              field="product"
              content={form.productnote}
              onContentChange={handleEditorContentChange}
              error={errors.productnote}
            />
          </div>

          {/* 버튼 그룹 */}
          <div className="d-flex justify-content-center gap-2">
            <button type="submit" className="btn btn-primary btn-fixed-width">
              수정
            </button>
            <Link to="/admin/products" className="btn btn-secondary btn-fixed-width">
              뒤로 가기
            </Link>
          </div>
        </form>

        {/* 모달 컴포넌트들 */}
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
