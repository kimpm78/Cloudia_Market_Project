import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';
import useCountries from '../hooks/useCountries';
import '../styles/CM_01_1007.css';

import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

const useModal = () => {
  const [modals, setModals] = useState({
    loading: false,
    error: false,
    confirm: false,
    success: false,
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

export default function CM_01_1007() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { modals, message, open, close } = useModal();
  const { countries } = useCountries();

  const [profile, setProfile] = useState({
    loginId: '',
    name: '',
    nationality: 'KR',
    phoneNumber: '',
    postalCode: '',
    addressMain: '',
    addressDetail1: '',
    addressDetail2: '',
    addressDetail3: '',
    pccc: '',
  });
  const [phoneParts, setPhoneParts] = useState({ code: '', part1: '', part2: '', part3: '' });
  const [initialLoading, setInitialLoading] = useState(true);

  const parsePhoneNumber = useCallback((phoneStr) => {
    if (!phoneStr) return;
    const parts = phoneStr.match(/\(\+(\d+)\)(\d+)-(\d+)-(\d+)/);
    if (parts) {
      setPhoneParts({ code: parts[1], part1: parts[2], part2: parts[3], part3: parts[4] });
    }
  }, []);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await axiosInstance.get('/user/profile');
        setProfile(response.data);
        parsePhoneNumber(response.data.phoneNumber);
      } catch (err) {
        open('error', 'プロフィール情報の取得に失敗しました。');
      } finally {
        setInitialLoading(false);
      }
    };
    if (user) {
      fetchProfile();
    }
  }, [user, parsePhoneNumber, open]);

  useEffect(() => {
    if (!countries.length || !profile.nationality) return;
    const country = countries.find((c) => c.isoCode === profile.nationality);
    if (country) {
      setPhoneParts((prev) => ({ ...prev, code: country.phoneCode }));
    }
  }, [countries, profile.nationality]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile((prev) => {
      if (name === 'nationality') {
        return { ...prev, [name]: value, pccc: value === 'KR' ? prev.pccc : '' };
      }
      return { ...prev, [name]: value };
    });
    if (name === 'nationality') {
      const selectedCountry = countries.find((c) => c.isoCode === value);
      if (selectedCountry) {
        setPhoneParts((prev) => ({ ...prev, code: selectedCountry.phoneCode }));
      }
    }
  };

  const handlePhoneChange = (e) => {
    const { name, value } = e.target;
    if (/^\d*$/.test(value)) {
      setPhoneParts((prev) => ({ ...prev, [name]: value }));
    }
  };

  useEffect(() => {
    const { code, part1, part2, part3 } = phoneParts;
    if (code && part1 && part2 && part3) {
      setProfile((prev) => ({ ...prev, phoneNumber: `(+${code})${part1}-${part2}-${part3}` }));
    }
  }, [phoneParts]);

  const validatePccc = (pcccValue) => {
    if (pcccValue && pcccValue.length > 0) {
      const pcccRegex = /^P\d{12}$/;

      if (!pcccRegex.test(pcccValue)) {
        return '個人通関固有番号（PCCC）は、Pから始まる13桁（P＋数字12桁）である必要があります。';
      }
    }
    return null;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const pcccError = profile.nationality === 'KR' ? validatePccc(profile.pccc) : null;

    if (pcccError) {
      open('error', pcccError);
      return;
    }

    open('confirm');
  };

  const handleConfirmUpdate = async () => {
    close('confirm');
    open('loading');
    try {
      // profile 状態に pccc が含まれているため、そのまま送信
      await axiosInstance.put('/user/profile', profile);
      open('success', '正常に更新されました。<br/>メインページへ移動します。');
    } catch (err) {
      open('error', err.response?.data?.message || '更新に失敗しました。');
    } finally {
      close('loading');
    }
  };

  const handleSuccessAndRedirect = () => {
    close('success');
    navigate('/');
  };

  if (initialLoading) {
    return <div className="p-5 text-center">読み込み中...</div>;
  }

  return (
    <div className="container mt-2">
      <h2 className="border-bottom fw-bolder pb-3 mb-4 mt-4">プロフィール</h2>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="loginId" className="form-label fw-bold">
            ID
          </label>
          <input
            type="text"
            className="form-control profile-input-medium bg-light" // bg-light で無効化の見た目にする
            id="loginId"
            value={profile.loginId || ''}
            readOnly
          />
        </div>

        <div className="mb-3">
          <label htmlFor="name" className="form-label fw-bold">
            氏名
          </label>
          <input
            type="text"
            className="form-control profile-input-medium bg-light"
            id="name"
            value={profile.name || ''}
            readOnly
          />
        </div>

        <div className="mb-3">
          <label htmlFor="nationality" className="form-label fw-bold">
            <i className="bi bi-asterisk required-asterisk"></i>国籍/居住国
          </label>
          <select
            className="form-select profile-input-medium"
            id="nationality"
            name="nationality"
            value={profile.nationality}
            onChange={handleChange}
          >
            {countries.map((c) => (
              <option key={c.isoCode} value={c.isoCode}>
                {c.name.jp}
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

        <div className="mb-3">
          <label htmlFor="postalCode" className="form-label fw-bold">
            <i className="bi bi-asterisk required-asterisk"></i>郵便番号
          </label>
          <input
            type="text"
            className="form-control profile-input-medium"
            id="postalCode"
            name="postalCode"
            value={profile.postalCode || ''}
            onChange={handleChange}
          />
        </div>

        <div className="mb-3">
          <label htmlFor="addressMain" className="form-label fw-bold">
            <i className="bi bi-asterisk required-asterisk"></i>住所
          </label>
          <input
            type="text"
            className="form-control profile-input-medium"
            id="addressMain"
            name="addressMain"
            value={profile.addressMain || ''}
            onChange={handleChange}
          />
        </div>

        <div className="mb-3">
          <label htmlFor="addressDetail1" className="form-label fw-bold">
            <i className="bi bi-asterisk required-asterisk"></i>住所詳細 1
          </label>
          <input
            type="text"
            className="form-control profile-input-medium"
            id="addressDetail1"
            name="addressDetail1"
            value={profile.addressDetail1 || ''}
            onChange={handleChange}
          />
        </div>

        <div className="mb-3">
          <label htmlFor="addressDetail2" className="form-label fw-bold">
            住所詳細 2
          </label>
          <input
            type="text"
            className="form-control profile-input-medium"
            id="addressDetail2"
            name="addressDetail2"
            value={profile.addressDetail2 || ''}
            onChange={handleChange}
          />
        </div>

        <div className="mb-3">
          <label htmlFor="addressDetail3" className="form-label fw-bold">
            住所詳細 3
          </label>
          <input
            type="text"
            className="form-control profile-input-medium"
            id="addressDetail3"
            name="addressDetail3"
            value={profile.addressDetail3 || ''}
            onChange={handleChange}
          />
        </div>

        {profile.nationality === 'KR' && (
          <div className="mb-3">
            <label htmlFor="pccc" className="form-label fw-bold">
              個人通関固有番号（PCCC）
            </label>
            <input
              type="text"
              className="form-control profile-input-medium"
              id="pccc"
              name="pccc"
              value={profile.pccc || ''}
              onChange={handleChange}
              maxLength={13}
              placeholder="Pから始まる13桁"
            />
            <div className="form-text">Pから始まる13桁です。</div>
          </div>
        )}

        <div className="d-flex justify-content-center gap-2 mt-5 mb-3">
          <button type="submit" className="btn btn-primary px-5" disabled={modals.loading}>
            {modals.loading ? '変更中...' : '変更する'}
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
        onClose={() => close('confirm')}
        onConfirm={handleConfirmUpdate}
        Message="プロフィール情報を変更しますか？"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleSuccessAndRedirect} Message={message} />
    </div>
  );
}
