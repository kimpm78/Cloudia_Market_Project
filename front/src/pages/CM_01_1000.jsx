import { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/CM_01_1001.css';

export default function CM_01_1000() {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  const togglePasswordVisibility = () => {
    setShowPassword((prev) => !prev);
  };

  const handleLogin = async (e) => {
    e.preventDefault();

    setErrors({});
    let currentErrors = {};

    if (!loginId) {
      currentErrors.loginId = 'IDを入力してください。';
    }
    if (!password) {
      currentErrors.password = 'パスワードを入力してください。';
    }
    if (Object.keys(currentErrors).length > 0) {
      setErrors(currentErrors);
      return;
    }

    try {
      const isSuccess = await login(loginId, password);

      if (isSuccess) {
        // ログイン成功時にページ遷移
        navigate(from, { replace: true });
      }
    } catch (error) {
      console.error('ログイン失敗:', error.message);
      setErrors({ general: error.message });
    }
  };

  return (
    <div className="login-page-container d-flex flex-grow-1 justify-content-center align-items-center">
      <div className="text-center login-form-wrapper">
        <h2 className="fw-bold text-primary">Cloudia Market</h2>
        <p className="mb-4">Cloudia Marketへようこそ！</p>
        <h4 className="fw-bold mb-3">ログイン</h4>
        <form onSubmit={handleLogin} noValidate>
          <div className="form-group mb-2">
            <input
              type="text"
              className={`form-control ${errors.loginId ? 'is-invalid' : ''}`}
              placeholder="ID"
              value={loginId}
              onChange={(e) => {
                setLoginId(e.target.value);
                setErrors((prev) => ({ ...prev, loginId: '' }));
              }}
              required
            />
            {errors.loginId && (
              <div className="invalid-feedback d-block text-start">{errors.loginId}</div>
            )}
          </div>
          <div className="form-group mb-2 password-input-container">
            <input
              type={showPassword ? 'text' : 'password'}
              className={`form-control ${errors.password ? 'is-invalid' : ''}`}
              placeholder="パスワード"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                setErrors((prev) => ({ ...prev, password: '' }));
              }}
              required
            />
            <button
              type="button"
              className="password-toggle-button"
              onClick={togglePasswordVisibility}
            >
              <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>{' '}
            </button>
            {errors.password && (
              <div className="invalid-feedback d-block text-start">{errors.password}</div>
            )}
          </div>

          <div className="text-end mb-3 login-links">
            <Link to="/find-id">IDをお忘れの方</Link>
            <span className="mx-1">|</span>
            <Link to="/reset-password">パスワードをお忘れの方</Link>
          </div>

          {errors.general && (
            <div className="alert alert-danger text-start" role="alert">
              {errors.general}
            </div>
          )}
          <div className="d-grid mb-2">
            <button type="submit" className="btn btn-primary">
              ログイン
            </button>
          </div>
        </form>
        <div className="d-grid">
          <button className="btn btn-outline-primary" onClick={() => navigate('/register')}>
            会員登録
          </button>
        </div>
      </div>
    </div>
  );
}
