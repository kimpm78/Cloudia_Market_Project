import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';

import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

const BANK_OPTIONS = [
  // 日本の主な銀行リスト
  { code: 'JP_POST', name: 'ゆうちょ銀行' },
  { code: 'MUFG', name: '三菱UFJ銀行' },
  { code: 'SMBC', name: '三井住友銀行' },
  { code: 'MIZUHO', name: 'みずほ銀行' },
  { code: 'RESONA', name: 'りそな銀行' },
  { code: 'SBI', name: '住信SBIネット銀行' },
  { code: 'RAKUTEN', name: '楽天銀行' },
  { code: 'AEON', name: 'イオン銀行' },
  // 韓国の主な銀行リスト
  { code: 'KB', name: 'KB国民銀行' },
  { code: 'SHINHAN', name: '新韓銀行' },
  { code: 'WOORI', name: 'ウリィ銀行' },
  { code: 'HANA', name: 'ハナ銀行' },
  { code: 'NH', name: 'NH農協銀行' },
  { code: 'IBK', name: 'IBK企業銀行' },
  { code: 'KAKAO', name: 'カカオバンク' },
  { code: 'TOSS', name: 'トスバンク' },
  { code: 'SC', name: 'SC第一銀行' },
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

  // 口座情報の取得
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
        console.error('口座情報の取得中にエラーが発生しました:', err);
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
    // 口座番号は数字のみ入力可
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
      open('error', 'すべての項目を入力してください。');
      return;
    }
    open('confirm');
  };

  const handleConfirmUpdate = async () => {
    close('confirm');
    open('loading');
    try {
      await axiosInstance.put('/user/account', account);
      open('success', '口座情報を保存しました。');
    } catch (err) {
      open('error', err.response?.data?.message || '保存に失敗しました。');
    } finally {
      close('loading');
    }
  };

  const handleSuccessAndRedirect = () => {
    close('success');
  };

  if (initialLoading) {
    return <div className="p-5 text-center">読み込み中...</div>;
  }

  return (
    <>
      <div className="container mt-2">
        <h2 className="border-bottom fw-bolder pb-3 mb-4 mt-4">
          返金口座の登録</h2>
        <div className="alert alert-light border mb-4" role="alert">
          <i className="bi bi-info-circle me-2"></i>
          注文キャンセルまたは返金が発生した場合に振込先となる口座情報を入力してください。
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="refundAccountBank" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>銀行名
            </label>
            <select
              className="form-select"
              style={{ maxWidth: '400px' }}
              id="refundAccountBank"
              name="refundAccountBank"
              value={account.refundAccountBank}
              onChange={handleChange}
            >
              <option value="">銀行を選択してください</option>
              {BANK_OPTIONS.map((bank) => (
                <option key={bank.code} value={bank.name}>
                  {bank.name}
                </option>
              ))}
            </select>
          </div>

          {/* 口座番号 */}
          <div className="mb-3">
            <label htmlFor="refundAccountNumber" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>口座番号
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

          {/* 口座名義 */}
          <div className="mb-3">
            <label htmlFor="refundAccountHolder" className="form-label fw-bold">
              <i className="bi bi-asterisk required-asterisk"></i>口座名義
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
              {modals.loading ? '保存中...' : '保存する'}
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
      </div>

      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => close('confirm')}
        onConfirm={handleConfirmUpdate}
        Message="口座情報を保存しますか？"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleSuccessAndRedirect} Message={message} />
    </>
  );
}
