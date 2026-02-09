import { useState } from 'react';
import { useEmailVerification } from '../../hooks/useEmailVerification';
import VerificationModal from '../../components/verificationModal';
import { useNavigate } from 'react-router';

const requestCodeApiUrl = `${import.meta.env.VITE_API_BASE_URL}/guest/find-id/send-code`;
const verifyCodeApiUrl = `${import.meta.env.VITE_API_BASE_URL}/guest/find-id/verify`;

export default function FindIdProcess({ onIdFound }) {
  const {
    email,
    setEmail,
    verificationCode,
    setVerificationCode,
    isModalOpen,
    setIsModalOpen,
    timer,
    isVerified,
    error,
    setError,
    loading,
    handleRequestCode,
    handleVerifyCode,
  } = useEmailVerification();

  const [formError, setFormError] = useState('');

  const navigate = useNavigate();

  const handleEmailChange = (e) => {
    setEmail(e.target.value);
    if (formError) setFormError('');
    if (error) setError('');
  };

  const handleRequest = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email || !emailRegex.test(email)) {
      setFormError('유효한 이메일 형식이 아닙니다.');
      return;
    }
    setFormError('');
    handleRequestCode(requestCodeApiUrl, { email });
  };

  const onVerificationSuccess = (data) => {
    const foundId = data?.resultList?.loginId;
    onIdFound(foundId);
  };

  const handleVerify = () => {
    const payload = { email, code: verificationCode };
    const isSuccessValidator = (data) => data && data.loginId;
    handleVerifyCode(verifyCodeApiUrl, payload, onVerificationSuccess, isSuccessValidator);
  };

  return (
    <>
      <h4 className="fw-bold mb-3">아이디 찾기</h4>
      <p className="text-muted mb-4">가입 시 입력한 이메일 주소를 입력해주세요.</p>

      <div className="form-group mb-2">
        <input
          type="email"
          className={`form-control ${formError || error ? 'is-invalid' : ''}`}
          placeholder="이메일 주소"
          value={email}
          onChange={handleEmailChange}
        />
        {(formError || error) && (
          <div className="invalid-feedback d-block text-start">{formError || error}</div>
        )}
      </div>

      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={handleRequest} disabled={loading}>
          {loading ? '요청 중...' : '확인'}
        </button>
      </div>

      <div className="d-grid mt-2">
        <button className="btn btn-outline-secondary" onClick={() => navigate(-1)}>
          이전으로 돌아가기
        </button>
      </div>

      <VerificationModal
        show={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        timeLeft={timer}
        verificationCode={verificationCode}
        onCodeChange={(e) => setVerificationCode(e.target.value)}
        onVerify={handleVerify}
        onResend={handleRequest}
        isEmailVerified={isVerified}
        error={error}
      />
    </>
  );
}
