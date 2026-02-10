import { useState, useEffect, useCallback } from 'react';
import { axiosPublic } from '../services/axiosInstance';

/**
 * メール認証に関する状態とロジックを管理するカスタムフック
 */
export const useEmailVerification = () => {
  const [email, setEmail] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [timer, setTimer] = useState(180);
  const [isVerified, setIsVerified] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let countdown;
    if (isModalOpen && timer > 0 && !isVerified) {
      countdown = setInterval(() => setTimer((prev) => prev - 1), 1000);
    }
    return () => clearInterval(countdown);
  }, [isModalOpen, timer, isVerified]);

  // 認証コード送信をリクエストする関数
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
      const errorMessage = err.response?.data?.message || '認証コードのリクエストに失敗しました。';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleVerifyCode = useCallback(async (url, payload, validator) => {
    if (!payload.code) {
      setError('認証コードを入力してください。');
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
        throw new Error(response.data?.message || '認証レスポンスが正しくありません。');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || '認証に失敗しました。';
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
