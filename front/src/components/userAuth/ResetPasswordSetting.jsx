import { useState } from 'react';
import '../../styles/CM_01_1001.css';
import { axiosPublic } from '../../services/axiosInstance';

export default function ResetPasswordSetting({ email, onSuccess }) {
  const [formData, setFormData] = useState({ password: '', passwordConfirm: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword((prev) => !prev);
  };

  const togglePasswordConfirmVisibility = () => {
    setShowPasswordConfirm((prev) => !prev);
  };

  const validate = () => {
    const newErrors = {};
    const { password, passwordConfirm } = formData;
    const passwordRegex =
      /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,20}$/;

    if (!password) {
      newErrors.password = '新しいパスワードを入力してください。';
    } else if (!passwordRegex.test(password)) {
      newErrors.password = 'パスワードは8〜20文字の英字・数字・記号の組み合わせで入力してください。';
    }

    if (!passwordConfirm) {
      newErrors.passwordConfirm = '新しいパスワード（確認）を入力してください。';
    } else if (password !== passwordConfirm) {
      newErrors.passwordConfirm = 'パスワードが一致しません。';
    }
    return newErrors;
  };

  const handleSubmit = async () => {
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors({});
    setLoading(true);

    try {
      await axiosPublic.post('/guest/reset-password', {
        email,
        newPassword: formData.password,
      });
      onSuccess();
    } catch (err) {
      setErrors({ api: err.response?.data?.message || 'パスワードの変更に失敗しました。' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h4 className="fw-bold mb-3">パスワード再設定</h4>
      <p className="text-muted mb-4">新しいパスワードを入力してください。</p>

      <div className="form-group mb-2">
        <div className="password-input-container">
          <input
            type={showPassword ? 'text' : 'password'}
            name="password"
            placeholder="新しいパスワード"
            className={`form-control ${errors.password ? 'is-invalid' : ''}`}
            value={formData.password}
            onChange={handleChange}
          />
          <button
            type="button"
            className="password-toggle-button"
            onClick={togglePasswordVisibility}
          >
            <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
          </button>
        </div>
        {errors.password && (
          <div className="invalid-feedback d-block text-start">{errors.password}</div>
        )}
      </div>

      <div className="form-group mb-2">
        <div className="password-input-container">
          <input
            type={showPasswordConfirm ? 'text' : 'password'}
            name="passwordConfirm"
            placeholder="新しいパスワード（確認）"
            className={`form-control ${errors.passwordConfirm ? 'is-invalid' : ''}`}
            value={formData.passwordConfirm}
            onChange={handleChange}
          />
          <button
            type="button"
            className="password-toggle-button"
            onClick={togglePasswordConfirmVisibility}
          >
            <i className={`bi ${showPasswordConfirm ? 'bi-eye-slash' : 'bi-eye'}`}></i>
          </button>
        </div>
        {errors.passwordConfirm && (
          <div className="invalid-feedback d-block text-start">{errors.passwordConfirm}</div>
        )}
      </div>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
          {loading ? '変更中...' : 'パスワードを変更'}
        </button>
      </div>
      {errors.api && <div className="invalid-feedback d-block text-start">{errors.api}</div>}
    </>
  );
}
