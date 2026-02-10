import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import '../styles/CM_01_1010.css';

const useModal = () => {
  const [modals, setModals] = useState({
    loading: false,
    error: false,
    confirm: false,
    success: false,
  });
  const [message, setMessage] = useState('');
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);

  const toggleCurrentPasswordVisibility = () => setShowCurrentPassword((prev) => !prev);

  const open = useCallback((type, msg = '') => {
    setMessage(msg);
    setModals((prev) => ({ ...prev, [type]: true }));
  }, []);

  const close = useCallback((type) => {
    setModals((prev) => ({ ...prev, [type]: false }));
  }, []);

  return { modals, message, open, close, showCurrentPassword, toggleCurrentPasswordVisibility };
};

// 退会理由一覧
const UNSUBSCRIBE_REASONS = [
  '欲しい商品が見つからないため',
  '配送日程などサービスに不満があるため',
  '商品の価格が高いため',
  'キャラクター関連のショッピングや情報がこれ以上必要ないため',
  'その他',
];

export default function CM_01_1010() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { modals, message, open, close, showCurrentPassword, toggleCurrentPasswordVisibility } =
    useModal();

  const [selectedReasons, setSelectedReasons] = useState([]);
  const [otherReasonText, setOtherReasonText] = useState('');
  const [password, setPassword] = useState('');
  const [isComplete, setIsComplete] = useState(false);

  const handleReasonChange = (reason) => {
    setSelectedReasons((prev) =>
      prev.includes(reason) ? prev.filter((r) => r !== reason) : [...prev, reason]
    );
  };

  const handleConfirmUnsubscribe = async () => {
    close('confirm');
    open('loading');
    try {
      const OTHER_REASON = 'その他';
      const reasonsPayload = selectedReasons.includes(OTHER_REASON)
        ? selectedReasons.filter((r) => r !== OTHER_REASON).concat(otherReasonText)
        : selectedReasons;

      await axiosInstance.post('/user/unsubscribe', {
        userId: user?.userId,
        password: password,
        reasons: reasonsPayload,
      });

      open(
        'success',
        'これまでクラウディアマーケットをご利用いただき、誠にありがとうございました。<br/>退会処理が正常に完了しました。'
      );
    } catch (error) {
      console.error('退会処理に失敗しました:', error);
      const serverMessage = error.response?.data?.message;
      open('error', serverMessage);
    } finally {
      close('loading');
    }
  };

  const handleFinalExit = () => {
    close('success');
    logout();
    navigate('/', { replace: true });
  };

  const handleUnsubscribeClick = () => {
    if (selectedReasons.length === 0 && !otherReasonText.trim()) {
      open('error', '退会理由を選択するか、「その他」の理由を入力してください。');
      return;
    }
    if (selectedReasons.includes('その他') && !otherReasonText.trim()) {
      open('error', '「その他」の理由を入力してください。');
      return;
    }
    if (!password) {
      open('error', 'パスワードを入力してください。');
      return;
    }
    open('confirm');
  };

  return (
    <>
      <div className="container mt-2">
        <h2 className="border-bottom fw-bolder pb-3 mb-4 mt-4">退会</h2>
        <p className="mb-5">
          クラウディア マーケットを退会しますか？
          <br />
          よろしければ、退会理由をご入力ください。
        </p>
        <div className="mb-3">
          <h5 className="border-bottom fw-bolder pb-3 mb-4">退会理由（複数選択可）</h5>
          {UNSUBSCRIBE_REASONS.map((reason, index) => (
            <div className="form-check mb-2" key={index}>
              <input
                className="form-check-input"
                type="checkbox"
                id={`reason-${index}`}
                checked={selectedReasons.includes(reason)}
                onChange={() => handleReasonChange(reason)}
              />
              <label className="form-check-label" htmlFor={`reason-${index}`}>
                {reason}
              </label>
            </div>
          ))}
          {selectedReasons.includes('その他') && (
            <textarea
              className="form-control mt-3"
              rows="4"
              placeholder="その他の理由を入力してください"
              value={otherReasonText}
              onChange={(e) => setOtherReasonText(e.target.value)}
            ></textarea>
          )}
        </div>

        <div className="mb-3">
          <label htmlFor="currentPasswordInput" className="form-label fw-bold">
            <i className="bi bi-asterisk required-asterisk"></i>
            現在のパスワード
          </label>
          <div className="password-input-container">
            <input
              type={showCurrentPassword ? 'text' : 'password'}
              className="form-control"
              id="currentPasswordInput"
              placeholder="パスワードを入力してください"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={toggleCurrentPasswordVisibility}
            >
              <i className={`bi ${showCurrentPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
            </button>
          </div>
        </div>

        <div className="d-flex justify-content-center gap-2 mt-5 mb-3">
          <button className="btn btn-primary px-5" onClick={handleUnsubscribeClick}>
            確認する
          </button>
          <button
            type="button"
            className="btn btn-secondary px-5"
            onClick={() => navigate('/mypage')}
          >
            戻る
          </button>
        </div>
      </div>

      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => close('confirm')}
        onConfirm={handleConfirmUnsubscribe}
        Message="本当に退会しますか？"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleFinalExit} Message={message} />
    </>
  );
}
