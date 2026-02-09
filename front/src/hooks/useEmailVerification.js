import { useState, useEffect, useCallback } from 'react';
import { axiosPublic } from '../services/axiosInstance';

/**
 * 이메일 인증과 관련된 모든 상태와 로직을 관리하는 커스텀 훅
 */
export const useEmailVerification = () => {
  const [email, setEmail] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [timer, setTimer] = useState(180);
  const [isVerified, setIsVerified] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // 모달이 열려있고, 인증이 완료되지 않았을 때만 타이머를 동작
  useEffect(() => {
    let countdown;
    if (isModalOpen && timer > 0 && !isVerified) {
      countdown = setInterval(() => setTimer((prev) => prev - 1), 1000);
    }
    return () => clearInterval(countdown);
  }, [isModalOpen, timer, isVerified]);

  // 인증 코드 발송을 요청하는 함수
  const handleRequestCode = useCallback(async (url, payload) => {
    setLoading(true);
    setError('');
    try {
      await axiosPublic.post(url, payload);

      setIsModalOpen(true);
      setTimer(180);
      setIsVerified(false);
      setVerificationCode('');
    } catch (err) {
      const errorMessage = err.response?.data?.message || '인증번호 요청에 실패했습니다.';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleVerifyCode = useCallback(async (url, payload, validator) => {
    if (!payload.code) {
      setError('인증번호를 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await axiosPublic.post(url, payload);

      let isSuccess = false;

      if (validator && typeof validator === 'function') {
        isSuccess = validator(response.data);
      } else if (response.data?.verified === true) {
        isSuccess = true;
      }

      if (isSuccess) {
        setIsVerified(true);
        setIsModalOpen(false);
      } else {
        throw new Error(response.data?.message || '인증 응답이 올바르지 않습니다.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || '인증에 실패했습니다.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    email,
    setEmail,
    verificationCode,
    setVerificationCode,
    isModalOpen,
    setIsModalOpen,
    timer,
    isVerified,
    setIsVerified,
    error,
    setError,
    loading,
    handleRequestCode,
    handleVerifyCode,
  };
};
