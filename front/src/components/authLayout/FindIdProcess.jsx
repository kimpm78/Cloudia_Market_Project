import { useState } from 'react';
import VerificationModal from '../VerificationModal';
import { useEmailVerification } from '../../hooks/useEmailVerification';

export default function FindIdProcess({ onIdFound }) {
  const requestCodeApiUrl = `${import.meta.env.VITE_API_BASE_URL}/guest/find-id/send-code`;
  const verifyCodeApiUrl = `${import.meta.env.VITE_API_BASE_URL}/guest/find-id/verify`;

  const {
    email,
    setEmail,
    verificationCode,
    setVerificationCode,
    isModalOpen,
    setIsModalOpen,
    timer,
    isVerified,
    error: apiError,
    loading,
    handleRequestCode,
    handleVerifyCode,
  } = useEmailVerification();

  const [errors, setErrors] = useState({});

  const validateEmail = (email) => {
    const errors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
      errors.email = '이메일을 입력해주세요.';
    } else if (!emailRegex.test(email)) {
      errors.email = '유효한 이메일 형식이 아닙니다.';
    }
    return errors;
  };

  const handleEmailChange = (e) => {
    const newEmail = e.target.value;
    setEmail(newEmail);
    if (errors.email) {
      setErrors((prev) => ({ ...prev, email: '' }));
    }
  };

  const handleRequest = () => {
    const validationErrors = validateEmail(email);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setErrors({});
    handleRequestCode(requestCodeApiUrl);
  };

  const onVerificationSuccess = (data) => {
    onIdFound(data.loginId);
  };

  const handleVerify = () => {
    handleVerifyCode(verifyCodeApiUrl, onVerificationSuccess);
  };

  return (
    <>
      <h4 className="fw-bold mb-3">아이디 찾기</h4>
      <p className="text-muted mb-4">가입 시 입력한 이메일 주소를 입력해주세요.</p>

      {apiError && <div className="alert alert-danger p-2">{apiError}</div>}

      <div className="form-group mb-2">
        <input
          type="email"
          className={`form-control ${errors.email ? 'is-invalid' : ''}`}
          placeholder="이메일 주소"
          value={email}
          onChange={handleEmailChange}
        />
        {errors.email && <div className="invalid-feedback d-block text-start">{errors.email}</div>}
      </div>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={handleRequest} disabled={loading}>
          {loading ? '요청 중...' : '인증번호 받기'}
        </button>
      </div>

      {/* VerificationPopup 컴포넌트를 렌더링하고 상태와 함수를 props로 전달 */}
      <VerificationModal
        show={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        timeLeft={timer}
        verificationCode={verificationCode}
        onCodeChange={(e) => setVerificationCode(e.target.value)}
        onVerify={handleVerify}
        onResend={() => handleRequestCode(requestCodeApiUrl)}
        isEmailVerified={isVerified}
      />
    </>
  );
}
