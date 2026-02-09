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

// 탈퇴 사유 목록
const UNSUBSCRIBE_REASONS = [
  '원하는 상품을 찾을 수 없어서',
  '배송 일정 등 서비스에 불만이 있어서',
  '상품 가격이 비싸서',
  '캐릭터 관련 쇼핑이나 정보가 더 이상 필요 없어서',
  '기타',
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
      const reasonsPayload = selectedReasons.includes('기타')
        ? selectedReasons.filter((r) => r !== '기타').concat(otherReasonText)
        : selectedReasons;

      await axiosInstance.post('/user/unsubscribe', {
        userId: user?.userId,
        password: password,
        reasons: reasonsPayload,
      });

      open(
        'success',
        '그동안 무기와라 장터를 이용해 주셔서 감사합니다.<br/>정상적으로 탈퇴 처리되었습니다.'
      );
    } catch (error) {
      console.error('회원탈퇴 처리 실패:', error);
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
      open('error', '탈퇴 이유를 선택하거나 기타 사유를 입력해주세요.');
      return;
    }
    if (selectedReasons.includes('기타') && !otherReasonText.trim()) {
      open('error', '기타 이유를 입력해주세요.');
      return;
    }
    if (!password) {
      open('error', '비밀번호를 입력해주세요.');
      return;
    }
    open('confirm');
  };

  return (
    <>
      <div className="container-fluid p-0 mt-2">
        <h4 className="border-bottom fw-bolder pb-3 mb-4">회원 탈퇴</h4>
        <p className="mb-5">
          무기와라 장터를 탈퇴하겠습니까?
          <br />
          괜찮으시다면 회원 탈퇴 이유를 입력해주세요.
        </p>
        <div className="mb-3">
          <h5 className="border-bottom fw-bolder pb-3 mb-4">탈퇴 이유 (중복 회답 가능)</h5>
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
          {selectedReasons.includes('기타') && (
            <textarea
              className="form-control mt-3"
              rows="4"
              placeholder="기타 사유를 입력해주세요"
              value={otherReasonText}
              onChange={(e) => setOtherReasonText(e.target.value)}
            ></textarea>
          )}
        </div>

        <div className="mb-3">
          <label htmlFor="currentPasswordInput" className="form-label fw-bold">
            <i className="bi bi-asterisk required-asterisk"></i>
            현재 비밀번호
          </label>
          <div className="password-input-container">
            <input
              type={showCurrentPassword ? 'text' : 'password'}
              className="form-control"
              id="currentPasswordInput"
              placeholder="비밀번호를 입력해주세요"
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
            확인하기
          </button>
          <button
            type="button"
            className="btn btn-secondary px-5"
            onClick={() => navigate('/mypage')}
          >
            뒤로 가기
          </button>
        </div>
      </div>

      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => close('confirm')}
        onConfirm={handleConfirmUnsubscribe}
        Message="정말로 회원을 탈퇴하시겠습니까?"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleFinalExit} Message={message} />
    </>
  );
}
