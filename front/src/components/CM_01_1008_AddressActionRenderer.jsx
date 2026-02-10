import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import useCountries from '../hooks/useCountries';

import CM_99_1001 from './commonPopup/CM_99_1001';
import CM_99_1002 from './commonPopup/CM_99_1002';
import CM_99_1003 from './commonPopup/CM_99_1003';
import CM_99_1004 from './commonPopup/CM_99_1004';

const initialFormData = {
  addressNickname: '',
  recipientName: '',
  recipientPhone: '',
  postalCode: '',
  addressMain: '',
  addressDetail1: '',
  addressDetail2: '',
  addressDetail3: '',
  country: 'KR',
};

const initialPhoneParts = { code: '82', part1: '', part2: '', part3: '' };

export default function CM_01_1008_AddressActionRenderer() {
  const [searchParams] = useSearchParams();
  const addressId = searchParams.get('id');
  const navigate = useNavigate();
  const { countries } = useCountries();

  const [formData, setFormData] = useState(initialFormData);
  const [phoneParts, setPhoneParts] = useState(initialPhoneParts);
  const [initialLoading, setInitialLoading] = useState(true);
  const [modals, setModals] = useState({
    loading: false,
    error: false,
    confirm: false,
    success: false,
  });
  const [modalMessage, setModalMessage] = useState('');

  const parsePhoneNumber = useCallback((phoneStr) => {
    if (!phoneStr) return;
    const parts = phoneStr.match(/\(\+(\d+)\)(\d+)-(\d+)-(\d+)/);
    if (parts) {
      const country = countries.find((c) => c.phoneCode === parts[1]);
      setFormData((prev) => ({ ...prev, country: country ? country.isoCode : '' }));
      setPhoneParts({ code: parts[1], part1: parts[2], part2: parts[3], part3: parts[4] });
    }
  }, [countries]);

  useEffect(() => {
    if (addressId) {
      setInitialLoading(true);
      axiosInstance
        .get(`/user/addresses/${addressId}`)
        .then((res) => {
          setFormData((prev) => ({ ...prev, ...res.data }));
          parsePhoneNumber(res.data.recipientPhone);
        })
        .catch((err) => {
          setModalMessage('住所情報の読み込みに失敗しました。');
          setModals((prev) => ({ ...prev, error: true }));
        })
        .finally(() => setInitialLoading(false));
    } else {
      setFormData(initialFormData);
      setPhoneParts(initialPhoneParts);
      setInitialLoading(false);
    }
  }, [addressId, parsePhoneNumber]);

  useEffect(() => {
    const { code, part1, part2, part3 } = phoneParts;
    if (code && part1 && part2 && part3) {
      setFormData((prev) => ({ ...prev, recipientPhone: `(+${code})${part1}-${part2}-${part3}` }));
    }
  }, [phoneParts]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'country') {
      const selectedCountry = countries.find((c) => c.isoCode === value);
      if (selectedCountry) {
        setFormData((prev) => ({ ...prev, country: selectedCountry.isoCode }));
        setPhoneParts((prev) => ({ ...prev, code: selectedCountry.phoneCode }));
      }
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handlePhoneChange = (e) => {
    const { name, value } = e.target;
    if (/^\d*$/.test(value)) {
      setPhoneParts((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (
      !formData.addressNickname ||
      !formData.recipientName ||
      !formData.postalCode ||
      !formData.addressMain ||
      !formData.recipientPhone ||
      !formData.addressDetail1
    ) {
      setModalMessage('必須項目（*）をすべて入力してください。');
      setModals((prev) => ({ ...prev, error: true }));
      return;
    }

    const { part1, part2, part3 } = phoneParts;

    if (!part1 || !part2 || !part3) {
      setModalMessage('携帯電話番号をすべて入力してください。');
      setModals((prev) => ({ ...prev, error: true }));
      return;
    }

    if (part1.length < 3 || part2.length < 4 || part3.length < 4) {
      setModalMessage('携帯電話番号を正しい形式で入力してください。');
      setModals((prev) => ({ ...prev, error: true }));
      return;
    }

    setModalMessage(`この住所を${addressId ? '修正' : '保存'}しますか？`);
    setModals((prev) => ({ ...prev, confirm: true }));
  };

  const handleConfirmSave = async () => {
    setModals({ loading: true, error: false, success: false, confirm: false });
    const { country, ...payload } = formData;
    try {
      if (addressId) {
        await axiosInstance.put(`/user/addresses/${addressId}`, payload);
      } else {
        await axiosInstance.post('/user/addresses', payload);
      }
      setModalMessage('正常に処理しました。');
      setModals((prev) => ({ ...prev, loading: false, success: true }));
    } catch (error) {
      setModalMessage(error.response?.data?.message || '保存に失敗しました。');
      setModals((prev) => ({ ...prev, loading: false, error: true }));
    }
  };

  const handleSuccessAndRedirect = () => {
    setModals((prev) => ({ ...prev, success: false }));
    navigate('/mypage/address-book');
  };

  const handleCancel = () => navigate(-1);

  if (initialLoading) {
    return <CM_99_1002 isOpen={true} />;
  }

  return (
    <>
      <form onSubmit={handleSubmit}>
        <div className="address-form-container">
          <div className="mb-3">
            <label htmlFor="addressNickname" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>配送先名
            </label>
            <input
              type="text"
              className="form-control"
              id="addressNickname"
              name="addressNickname"
              onChange={handleChange}
              value={formData.addressNickname || ''}
              placeholder="配送先名を入力してください"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="recipientName" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>受取人
            </label>
            <input
              type="text"
              className="form-control"
              id="recipientName"
              name="recipientName"
              onChange={handleChange}
              value={formData.recipientName || ''}
              placeholder="受取人の氏名を入力してください"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="postalCode" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>郵便番号
            </label>
            <input
              type="text"
              className="form-control"
              id="postalCode"
              name="postalCode"
              onChange={handleChange}
              value={formData.postalCode || ''}
              placeholder="郵便番号を入力してください"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="addressMain" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>住所
            </label>
            <input
              type="text"
              className="form-control"
              id="addressMain"
              name="addressMain"
              onChange={handleChange}
              value={formData.addressMain || ''}
              placeholder="住所を入力してください"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="addressDetail1" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>住所詳細 1
            </label>
            <input
              type="text"
              className="form-control"
              id="addressDetail1"
              name="addressDetail1"
              onChange={handleChange}
              value={formData.addressDetail1 || ''}
              placeholder="住所詳細を入力してください"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="addressDetail2" className="form-label fw-bold">
              住所詳細 2
            </label>
            <input
              type="text"
              className="form-control"
              id="addressDetail2"
              name="addressDetail2"
              onChange={handleChange}
              value={formData.addressDetail2 || ''}
              placeholder="住所詳細を入力してください"
            />
          </div>
          <div className="mb-3">
            <label htmlFor="addressDetail3" className="form-label fw-bold">
              住所詳細 3
            </label>
            <input
              type="text"
              className="form-control"
              id="addressDetail3"
              name="addressDetail3"
              onChange={handleChange}
              value={formData.addressDetail3 || ''}
              placeholder="住所詳細を入力してください"
            />
          </div>

          <div className="mb-3">
            <label htmlFor="country" className="form-label fw-bold">
              国籍/地域
            </label>
            <select
              id="country"
              name="country"
              className="form-select"
              value={formData.country}
              onChange={handleChange}
            >
              {countries.map((c) => (
                <option key={c.isoCode} value={c.isoCode}>
                  {c.name.jp || c.name.en || c.name.kr}
                </option>
              ))}
            </select>
          </div>
          <div className="mb-3">
            <label className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>携帯電話番号
            </label>
            <div className="d-flex align-items-center">
              <input
                type="text"
                className="form-control me-2 phone-country-code"
                value={`+${phoneParts.code}`}
                readOnly
              />
              <input
                type="tel"
                name="part1"
                className="form-control me-2 phone-part"
                value={phoneParts.part1}
                onChange={handlePhoneChange}
                maxLength="4"
              />
              <span className="me-2">-</span>
              <input
                type="tel"
                name="part2"
                className="form-control me-2 phone-part"
                value={phoneParts.part2}
                onChange={handlePhoneChange}
                maxLength="4"
              />
              <span className="me-2">-</span>
              <input
                type="tel"
                name="part3"
                className="form-control me-2 phone-part"
                value={phoneParts.part3}
                onChange={handlePhoneChange}
                maxLength="4"
              />
            </div>
          </div>
        </div>
        <div className="d-flex justify-content-center mt-5 mb-5 gap-2">
          <button type="submit" className="btn btn-primary btn-lg px-5" disabled={modals.loading}>
            {modals.loading ? '処理中...' : addressId ? '修正する' : '登録する'}
          </button>
          <button
            type="button"
            className="btn btn-secondary px-5"
            onClick={() => navigate('/mypage')}
          >
            戻る
          </button>
        </div>
      </form>

      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => setModals((p) => ({ ...p, confirm: false }))}
        onConfirm={handleConfirmSave}
        Message={modalMessage}
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003
        isOpen={modals.error}
        onClose={() => setModals((p) => ({ ...p, error: false }))}
        message={modalMessage}
      />
      <CM_99_1004
        isOpen={modals.success}
        onClose={handleSuccessAndRedirect}
        Message={modalMessage}
      />
    </>
  );
}
