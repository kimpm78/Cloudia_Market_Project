import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';

import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import '../styles/CM_01_1009.css';

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

export default function CM_01_1009() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { modals, message, open, close } = useModal();
  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmNewPassword, setShowConfirmNewPassword] = useState(false);

  const toggleCurrentPasswordVisibility = () => setShowCurrentPassword((prev) => !prev);
  const toggleNewPasswordVisibility = () => setShowNewPassword((prev) => !prev);
  const toggleConfirmNewPasswordVisibility = () => setShowConfirmNewPassword((prev) => !prev);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setPasswords((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const { currentPassword, newPassword, confirmNewPassword } = passwords;
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).*$/;

    if (!currentPassword) {
      open('error', '현재 비밀번호를 입력해주세요.');
      return;
    }
    if (!newPassword) {
      open('error', '새로운 비밀번호를 입력해주세요.');
      return;
    }
    if (newPassword.length < 8) {
      open('error', '비밀번호는 최소 8자 이상이어야 합니다.');
      return;
    }
    if (!passwordRegex.test(newPassword)) {
      open('error', '영문, 숫자, 특수문자를 모두 포함해야 합니다.');
      return;
    }
    if (!confirmNewPassword) {
      open('error', '새로운 비밀번호를 다시 입력해주세요.');
      return;
    }
    if (newPassword !== confirmNewPassword) {
      open('error', '새로운 비밀번호가 일치하지 않습니다.');
      return;
    }

    open('confirm');
  };

  const handleConfirmSubmit = async () => {
    close('confirm');
    open('loading');

    try {
      await axiosInstance.post('/user/change-password', passwords);
      open('success', '비밀번호가 변경되었습니다.<br/>변경하신 비밀번호로 다시 로그인해주세요.');
    } catch (error) {
      const errorMessage = error.response?.data?.message || '알 수 없는 오류가 발생했습니다.';
      open('error', errorMessage);
    } finally {
      close('loading');
    }
  };

  const handleSuccessAndRedirect = () => {
    close('success');
    logout();
    navigate('/login');
  };

  return (
    <>
      <h4 className="border-bottom fw-bolder pb-3 mb-4 mt-2">비밀번호 변경</h4>
      <p>현재 비밀번호와 새 비밀번호를 입력하여 비밀번호를 변경할 수 있습니다.</p>

      <form onSubmit={handleSubmit} noValidate>
        {/* 현재 비밀번호 */}
        <div className="mb-3">
          <label htmlFor="currentPassword" className="form-label fw-bold">
            <span className="text-danger me-1">*</span>현재 비밀번호
          </label>
          <div className="password-input-container">
            <input
              type={showCurrentPassword ? 'text' : 'password'}
              className="form-control"
              id="currentPassword"
              name="currentPassword"
              value={passwords.currentPassword}
              onChange={handleChange}
              placeholder="비밀번호를 입력해주세요"
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={toggleCurrentPasswordVisibility}
            >
              <i className={`bi ${showCurrentPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
            </button>
          </div>
          <div className="form-text">영문+숫자+특수문자 조합 (최소 8자 이상)</div>
        </div>
        <div className="mb-3">
          <label htmlFor="newPassword" className="form-label fw-bold">
            <span className="text-danger me-1">*</span>새로운 비밀번호
          </label>
          <div className="password-input-container">
            <input
              type={showNewPassword ? 'text' : 'password'}
              className="form-control"
              id="newPassword"
              name="newPassword"
              value={passwords.newPassword}
              onChange={handleChange}
              placeholder="비밀번호를 입력해주세요"
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={toggleNewPasswordVisibility}
            >
              <i className={`bi ${showNewPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
            </button>
          </div>
          <div className="form-text">영문+숫자+특수문자 조합 (최소 8자 이상)</div>
        </div>
        <div className="mb-4">
          <label htmlFor="confirmNewPassword" className="form-label fw-bold">
            <span className="text-danger me-1">*</span>새로운 비밀번호 (재입력)
          </label>
          <div className="password-input-container">
            <input
              type={showConfirmNewPassword ? 'text' : 'password'}
              className="form-control"
              id="confirmNewPassword"
              name="confirmNewPassword"
              value={passwords.confirmNewPassword}
              onChange={handleChange}
              placeholder="비밀번호를 입력해주세요"
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={toggleConfirmNewPasswordVisibility}
            >
              <i className={`bi ${showConfirmNewPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
            </button>
          </div>
        </div>
        <div className="mt-4 small">
          <p className="text-danger mb-1">
            생일이나 이름 등 쉽게 추측할 수 있는 비밀번호는 피해주세요.
          </p>
          <p className="mb-1">
            영문자와 숫자의 조합 등 가능한 한 복잡한 비밀번호 설정을 권장드립니다.
          </p>
          <p className="mb-0">
            위 내용에 동의하신다면 <span className="text-primary fw-bold">[변경하기]</span> 버튼을
            눌러주세요.
          </p>
        </div>
        <div className="d-flex justify-content-center gap-2 mt-5 mb-3">
          <button type="submit" className="btn btn-primary px-5" disabled={modals.loading}>
            {modals.loading ? '변경 중...' : '변경 하기'}
          </button>
          <button
            type="button"
            className="btn btn-secondary px-5"
            onClick={() => navigate('/mypage')}
          >
            뒤로 가기
          </button>
        </div>
      </form>

      {/* --- 공통 팝업 컴포넌트들 --- */}
      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => close('confirm')}
        onConfirm={handleConfirmSubmit}
        Message="비밀번호를 변경하시겠습니까?"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleSuccessAndRedirect} Message={message} />
    </>
  );
}
