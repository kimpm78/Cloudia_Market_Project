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
      newErrors.password = '새 비밀번호를 입력해주세요.';
    } else if (!passwordRegex.test(password)) {
      newErrors.password = '비밀번호는 8~20자의 영문, 숫자, 특수문자 조합이어야 합니다.';
    }

    if (!passwordConfirm) {
      newErrors.passwordConfirm = '새 비밀번호 확인을 입력해주세요.';
    } else if (password !== passwordConfirm) {
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
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
      setErrors({ api: err.response?.data?.message || '비밀번호 변경에 실패했습니다.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h4 className="fw-bold mb-3">비밀번호 재설정</h4>
      <p className="text-muted mb-4">새로운 비밀번호를 입력해주세요.</p>

      <div className="form-group mb-2">
        <div className="password-input-container">
          <input
            type={showPassword ? 'text' : 'password'}
            name="password"
            placeholder="새 비밀번호"
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
            placeholder="새 비밀번호 확인"
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
          {loading ? '변경 중...' : '비밀번호 변경'}
        </button>
      </div>
      {errors.api && <div className="invalid-feedback d-block text-start">{errors.api}</div>}
    </>
  );
}
