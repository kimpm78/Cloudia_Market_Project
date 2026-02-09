import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';

import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

const BANK_OPTIONS = [
  { code: 'KB', name: 'KB국민은행' },
  { code: 'SHINHAN', name: '신한은행' },
  { code: 'WOORI', name: '우리은행' },
  { code: 'HANA', name: '하나은행' },
  { code: 'NH', name: 'NH농협은행' },
  { code: 'IBK', name: 'IBK기업은행' },
  { code: 'KAKAO', name: '카카오뱅크' },
  { code: 'TOSS', name: '토스뱅크' },
  { code: 'SC', name: 'SC제일은행' },
];

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

export default function CM_01_1014() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { modals, message, open, close } = useModal();

  const [account, setAccount] = useState({
    refundAccountBank: '',
    refundAccountNumber: '',
    refundAccountHolder: '',
  });

  const [initialLoading, setInitialLoading] = useState(true);

  // 계좌 정보 불러오기
  useEffect(() => {
    const fetchAccount = async () => {
      try {
        const response = await axiosInstance.get('/user/account');

        const data = response.data.resultList;

        if (data) {
          setAccount({
            refundAccountBank: data.refundAccountBank || '',
            refundAccountNumber: data.refundAccountNumber || '',
            refundAccountHolder: data.refundAccountHolder || '',
          });
        }
      } catch (err) {
        console.error('계좌 조회 중 오류 발생:', err);
      } finally {
        setInitialLoading(false);
      }
    };

    if (user) {
      fetchAccount();
    }
  }, [user]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    // 계좌번호는 숫자만 입력받도록 처리
    if (name === 'refundAccountNumber') {
      const numberOnly = value.replace(/[^0-9]/g, '');
      setAccount((prev) => ({ ...prev, [name]: numberOnly }));
      return;
    }
    setAccount((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (
      !account.refundAccountBank ||
      !account.refundAccountNumber ||
      !account.refundAccountHolder
    ) {
      open('error', '모든 항목을 입력해주세요.');
      return;
    }
    open('confirm');
  };

  const handleConfirmUpdate = async () => {
    close('confirm');
    open('loading');
    try {
      await axiosInstance.put('/user/account', account);
      open('success', '계좌 정보가 성공적으로 저장되었습니다.');
    } catch (err) {
      open('error', err.response?.data?.message || '저장에 실패했습니다.');
    } finally {
      close('loading');
    }
  };

  const handleSuccessAndRedirect = () => {
    close('success');
  };

  if (initialLoading) {
    return <div className="p-5 text-center">로딩 중...</div>;
  }

  return (
    <>
      <div className="mt-2">
        <h4 className="border-bottom fw-bolder pb-3 mb-4">환불 계좌번호 등록</h4>
        <div className="alert alert-light border mb-4" role="alert">
          <i className="bi bi-info-circle me-2"></i>
          주문 취소 및 환불 발생 시 입금 받으실 계좌 정보를 입력해주세요.
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="refundAccountBank" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>은행명
            </label>
            <select
              className="form-select"
              style={{ maxWidth: '400px' }}
              id="refundAccountBank"
              name="refundAccountBank"
              value={account.refundAccountBank}
              onChange={handleChange}
            >
              <option value="">은행을 선택해주세요</option>
              {BANK_OPTIONS.map((bank) => (
                <option key={bank.code} value={bank.name}>
                  {bank.name}
                </option>
              ))}
            </select>
          </div>

          {/* 계좌 번호 */}
          <div className="mb-3">
            <label htmlFor="refundAccountNumber" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>계좌번호
            </label>
            <input
              type="text"
              className="form-control"
              style={{ maxWidth: '400px' }}
              id="refundAccountNumber"
              name="refundAccountNumber"
              value={account.refundAccountNumber}
              onChange={handleChange}
            />
          </div>

          {/* 예금주 */}
          <div className="mb-3">
            <label htmlFor="refundAccountHolder" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>예금주
            </label>
            <input
              type="text"
              className="form-control"
              style={{ maxWidth: '400px' }}
              id="refundAccountHolder"
              name="refundAccountHolder"
              value={account.refundAccountHolder}
              onChange={handleChange}
            />
          </div>

          <div className="d-flex justify-content-center gap-2 mt-5 mb-3">
            <button type="submit" className="btn btn-primary px-5" disabled={modals.loading}>
              {modals.loading ? '저장 중...' : '저장 하기'}
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
      </div>

      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => close('confirm')}
        onConfirm={handleConfirmUpdate}
        Message="계좌 정보를 저장하시겠습니까?"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleSuccessAndRedirect} Message={message} />
    </>
  );
}
