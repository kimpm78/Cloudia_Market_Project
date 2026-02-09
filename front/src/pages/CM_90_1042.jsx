import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import CM1050560 from '../images/common/CM-1050560.png';

export default function CM_90_1042() {
  const { bannerId } = useParams();
  const navigate = useNavigate();
  const [preview, setPreview] = useState(null);
  const [errors, setErrors] = useState({});
  const [availableOrders, setAvailableOrders] = useState([]);
  const [open1004, setOpen1004] = useState(false);
  const [message, setMessage] = useState('');
  const [popupType, setPopupType] = useState('');
  const [form, setForm] = useState({
    bannerId: '',
    bannerName: '',
    urlLink: '',
    imageFile: '',
    isDisplay: '1',
    displayOrder: 0,
    updatedAt: '',
  });

  useEffect(() => {
    if (form.isDisplay === '2' && form.displayOrder !== 0) {
      setForm((prev) => ({ ...prev, displayOrder: 0 }));
    }
  }, [form.isDisplay]);

  useEffect(() => {
    const fetchData = async () => {
      var order;
      var result = await axiosInstance.get('/admin/menu/banner/findIdBanner', {
        params: {
          bannerId: bannerId,
        },
      });

      if (result.data.result) {
        setForm({
          bannerId: result.data.resultList.bannerId || '',
          bannerName: result.data.resultList.bannerName || '',
          urlLink: result.data.resultList.urlLink || '',
          imageFile: result.data.resultList.imageLink || '',
          isDisplay: result.data.resultList.isDisplay?.toString() || '1',
          displayOrder: result.data.resultList.displayOrder ?? 0,
          updatedAt: result.data.resultList.updatedAt || '',
        });
        order = result.data.resultList.displayOrder;
        if (result.data.resultList.imageLink !== null) {
          setPreview(import.meta.env.VITE_API_BASE_IMAGE_URL + result.data.resultList.imageLink);
        } else {
          setPreview(CM1050560);
        }
      }
      result = await axiosInstance.get('/admin/menu/banner/availableDisplayOrders');
      if (result.data.result) {
        setAvailableOrders([...result.data.resultList, order].sort((a, b) => a - b));
      } else {
        setAvailableOrders([0]);
      }
    };

    if (bannerId) {
      fetchData();
    }
  }, [bannerId, navigate]);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (file.type !== 'image/jpeg' && file.type !== 'image/png' && file.type !== 'image/jpg') {
      setMessage('JPG 또는 PNG 형식의 이미지만 업로드할 수 있습니다.');
      setPopupType('typeError');
      setOpen1004(true);
      return;
    }
    const reader = new FileReader();
    reader.onload = (event) => {
      const image = new Image();
      image.onload = () => {
        if (image.width < 1050 || image.height < 560) {
          setMessage(
            `이미지 크기는 1050x560 이하는 허용되지않습니다. 현재: ${image.width}x${image.height}`
          );
          setForm((prev) => ({
            ...prev,
            imageFile: '',
          }));
          setPreview(CM1050560);
          setOpen1004(true);
        } else if (file.size > 20 * 1024 * 1024) {
          setMessage(`이미지 사이즈는 20MB 이상은 허용되지않습니다. 현재: ${file.size}`);
          setPopupType('sizeError');
          setForm((prev) => ({
            ...prev,
            imageUrl: '',
          }));
          setPreview(CM1050560);
          setOpen1004(true);
        } else {
          setForm((prev) => ({
            ...prev,
            imageFile: file,
          }));
          setPreview(event.target.result);
        }
      };
      image.src = event.target.result;
    };
    reader.readAsDataURL(file);
  };

  const validateForm = () => {
    const newErrors = {};

    if (form.bannerName.trim().length === 0) newErrors.bannerName = '배너명을 입력하세요.';
    if (form.urlLink.trim().length === 0) newErrors.urlLink = '배너링크를 입력하세요.';
    if (!form.imageFile) newErrors.imageFile = '이미지를 선택해주세요.';
    if (form.isDisplay === '1') {
      if (form.displayOrder === 0) newErrors.displayOrder = '배너 순서를 입력하세요.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegisterClick = async (e) => {
    if (validateForm()) {
      const formData = new FormData();
      formData.append('bannerId', form.bannerId);
      formData.append('bannerName', form.bannerName);
      formData.append('urlLink', form.urlLink);
      formData.append('displayOrder', Number(form.displayOrder));
      formData.append('isDisplay', Number(form.isDisplay));
      formData.append('updatedAt', form.updatedAt);

      if (form.imageFile && form.imageFile instanceof File) {
        formData.append('imageFile', form.imageFile);
      } else {
        const emptyBlob = new Blob([''], { type: 'application/octet-stream' });
        formData.append(
          'imageFile',
          new File([emptyBlob], '', { type: 'application/octet-stream' })
        );
      }

      const result = await axiosInstance.post('/admin/menu/banner/update', formData);
      if (result.data.result) {
        setMessage(result.data.message);
        setPopupType('bannerSuccess');
        setOpen1004(true);
      }
    }
  };

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">배너 수정</h5>
        <form>
          {/* 배너명 */}
          <div className="mb-3">
            <label htmlFor="bannerName" className="form-label">
              배너명
            </label>
            <input
              type="text"
              className="form-control"
              id="bannerName"
              value={form.bannerName}
              onChange={(e) =>
                setForm((prev) => ({
                  ...prev,
                  bannerName: e.target.value,
                }))
              }
            />
            {errors.bannerName && <div className="text-danger">{errors.bannerName}</div>}
          </div>

          {/* 링크 */}
          <div className="mb-3">
            <label htmlFor="urlLink" className="form-label">
              배너 링크(URL**/주소입력**)
            </label>
            <input
              type="text"
              className="form-control"
              id="urlLink"
              value={form.urlLink}
              onChange={(e) =>
                setForm((prev) => ({
                  ...prev,
                  urlLink: e.target.value,
                }))
              }
            />
            {errors.urlLink && <div className="text-danger">{errors.urlLink}</div>}
          </div>

          {/* 사용 여부 */}
          <div className="mb-3">
            <label className="form-label d-block">사용 여부</label>
            <div className="form-check form-check-inline">
              <input
                className="form-check-input"
                type="radio"
                name="isDisplay"
                id="isDisplayDisplay"
                value="1"
                checked={form.isDisplay === '1'}
                onChange={(e) =>
                  setForm((prev) => ({
                    ...prev,
                    isDisplay: e.target.value,
                  }))
                }
              />
              <label className="form-check-label" htmlFor="isDisplayDisplay">
                표시
              </label>
            </div>
            <div className="form-check form-check-inline">
              <input
                className="form-check-input"
                type="radio"
                name="isDisplay"
                id="isDisplayHide"
                value="2"
                checked={form.isDisplay === '2'}
                onChange={(e) =>
                  setForm((prev) => ({
                    ...prev,
                    isDisplay: e.target.value,
                  }))
                }
              />
              <label className="form-check-label" htmlFor="isDisplayHide">
                미표시
              </label>
            </div>
          </div>

          {/* 배너 순서 */}
          <div className="mb-3">
            <label htmlFor="displayOrder" className="form-label">
              배너 순서
            </label>
            <select
              className="form-select"
              id="displayOrder"
              value={form.isDisplay === '2' ? '0' : form.displayOrder}
              onChange={(e) =>
                setForm((prev) => ({
                  ...prev,
                  displayOrder: e.target.value,
                }))
              }
              disabled={form.isDisplay === '2'}
            >
              {form.isDisplay === '2' ? (
                <option value="0">0</option>
              ) : (
                availableOrders.map((num) => (
                  <option key={num} value={num}>
                    {num}
                  </option>
                ))
              )}
            </select>
            {errors.displayOrder && <div className="text-danger">{errors.displayOrder}</div>}
          </div>

          {/* 배너 이미지 */}
          <div className="mb-3">
            <label htmlFor="imageFile" className="form-label">
              배너 이미지
            </label>
            <input
              type="file"
              className="form-control"
              id="imageFile"
              accept="image/png, image/jpeg, image/jpg"
              onChange={handleFileChange}
            />
            {errors.imageFile && <div className="text-danger">{errors.imageFile}</div>}
            <img
              src={preview}
              className="img-thumbnail mt-2"
              alt="상품 이미지"
              style={{ width: '200px', height: '200px', objectFit: 'fill' }}
            />
            <div>
              ※최소사이즈 <b>1050 X 560 픽셀</b>의 크기를 권장합니다.
            </div>
          </div>

          {/* 버튼 */}
          <div className="d-flex justify-content-center">
            <button
              type="button"
              className="btn btn-primary me-2 btn-fixed-width"
              onClick={handleRegisterClick}
            >
              업데이트
            </button>
            <Link to="/admin/menu/banner" className="btn btn-secondary btn-fixed-width">
              뒤로 가기
            </Link>
          </div>
        </form>
        <CM_99_1004
          isOpen={open1004}
          onClose={() => {
            setOpen1004(false);
            if (popupType === 'bannerSuccess') {
              navigate(-1);
            }
          }}
          Message={message}
        />
      </div>
    </div>
  );
}
