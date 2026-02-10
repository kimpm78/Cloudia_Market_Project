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
      open('error', '現在のパスワードを入力してください。');
      return;
    }
    if (!newPassword) {
      open('error', '新しいパスワードを入力してください。');
      return;
    }
    if (newPassword.length < 8) {
      open('error', 'パスワードは8文字以上で入力してください。');
      return;
    }
    if (!passwordRegex.test(newPassword)) {
      open('error', '英字・数字・記号をすべて含めてください。');
      return;
    }
    if (!confirmNewPassword) {
      open('error', '新しいパスワードをもう一度入力してください。');
      return;
    }
    if (newPassword !== confirmNewPassword) {
      open('error', '新しいパスワードが一致しません。');
      return;
    }

    open('confirm');
  };

  const handleConfirmSubmit = async () => {
    close('confirm');
    open('loading');

    try {
      await axiosInstance.post('/user/change-password', passwords);
      open('success', 'パスワードが変更されました。<br/>変更後のパスワードで再ログインしてください。');
    } catch (error) {
      const errorMessage = error.response?.data?.message || '不明なエラーが発生しました。';
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
    <div className="container mt-2">
      <h2 className="border-bottom fw-bolder pb-3 mb-4 mt-4">パスワード変更</h2>
      <p>現在のパスワードと新しいパスワードを入力して、パスワードを変更できます。</p>

      <form onSubmit={handleSubmit} noValidate>
        {/* 現在のパスワード */}
        <div className="mb-3">
          <label htmlFor="currentPassword" className="form-label fw-bold">
            <span className="text-danger me-1">*</span>現在のパスワード
          </label>
          <div className="password-input-container">
            <input
              type={showCurrentPassword ? 'text' : 'password'}
              className="form-control"
              id="currentPassword"
              name="currentPassword"
              value={passwords.currentPassword}
              onChange={handleChange}
              placeholder="パスワードを入力してください"
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={toggleCurrentPasswordVisibility}
            >
              <i className={`bi ${showCurrentPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
            </button>
          </div>
          <div className="form-text">英字＋数字＋記号の組み合わせ（8文字以上）</div>
        </div>
        <div className="mb-3">
          <label htmlFor="newPassword" className="form-label fw-bold">
            <span className="text-danger me-1">*</span>新しいパスワード
          </label>
          <div className="password-input-container">
            <input
              type={showNewPassword ? 'text' : 'password'}
              className="form-control"
              id="newPassword"
              name="newPassword"
              value={passwords.newPassword}
              onChange={handleChange}
              placeholder="パスワードを入力してください"
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={toggleNewPasswordVisibility}
            >
              <i className={`bi ${showNewPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
            </button>
          </div>
          <div className="form-text">英字＋数字＋記号の組み合わせ（8文字以上）</div>
        </div>
        <div className="mb-4">
          <label htmlFor="confirmNewPassword" className="form-label fw-bold">
            <span className="text-danger me-1">*</span>新しいパスワード（再入力）
          </label>
          <div className="password-input-container">
            <input
              type={showConfirmNewPassword ? 'text' : 'password'}
              className="form-control"
              id="confirmNewPassword"
              name="confirmNewPassword"
              value={passwords.confirmNewPassword}
              onChange={handleChange}
              placeholder="パスワードを入力してください"
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
            誕生日や名前など推測されやすいパスワードは避けてください。
          </p>
          <p className="mb-1">
            英字と数字の組み合わせなど、できるだけ複雑なパスワードの設定を推奨します。
          </p>
          <p className="mb-0">
            上記内容に同意いただける場合は <span className="text-primary fw-bold">[変更する]</span>{' '}
            ボタンを 押してください。
          </p>
        </div>
        <div className="d-flex justify-content-center gap-2 mt-5 mb-3">
          <button type="submit" className="btn btn-primary px-5" disabled={modals.loading}>
            {modals.loading ? '変更中...' : '変更する'}
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

      {/* --- 共通ポップアップコンポーネント --- */}
      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => close('confirm')}
        onConfirm={handleConfirmSubmit}
        Message="パスワードを変更しますか？"
      />
      <CM_99_1002 isOpen={modals.loading} />
      <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      <CM_99_1004 isOpen={modals.success} onClose={handleSuccessAndRedirect} Message={message} />
    </div>
  );
}
