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
      errors.email = 'メールアドレスを入力してください。';
    } else if (!emailRegex.test(email)) {
      errors.email = '有効なメールアドレス形式ではありません。';
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
      <h4 className="fw-bold mb-3">IDを探す</h4>
      <p className="text-muted mb-4">登録時に入力したメールアドレスを入力してください。</p>

      {apiError && <div className="alert alert-danger p-2">{apiError}</div>}

      <div className="form-group mb-2">
        <input
          type="email"
          className={`form-control ${errors.email ? 'is-invalid' : ''}`}
          placeholder="メールアドレス"
          value={email}
          onChange={handleEmailChange}
        />
        {errors.email && <div className="invalid-feedback d-block text-start">{errors.email}</div>}
      </div>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={handleRequest} disabled={loading}>
          {loading ? '送信中...' : '認証コードを受け取る'}
        </button>
      </div>

      {/* VerificationModal を表示し、状態と関数を props として渡す */}
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
