import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import CMMessage from '../constants/CMMessage';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';
import axiosInstance from '../services/axiosInstance.js';
import { CATEGORIES, DISPLAY_OPTIONS } from '../constants/constants.js';

const RadioGroup = ({ name, options = [], value, onChange, label }) => (
  <div className="mb-3">
    <label className="form-label d-block">{label}</label>
    {options &&
      options.length > 0 &&
      options.map((option) => (
        <div key={option.value} className="form-check form-check-inline">
          <input
            className="form-check-input"
            type="radio"
            name={name}
            id={`${name}_${option.value}`}
            value={option.value}
            checked={value === option.value}
            onChange={(e) => onChange(Number(e.target.value))}
          />
          <label className="form-check-label" htmlFor={`${name}_${option.value}`}>
            {option.label}
          </label>
        </div>
      ))}
  </div>
);

const InputField = ({ id, label, value, onChange, error, ...props }) => (
  <div className="mb-3">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <input
      id={id}
      className="form-control"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      {...props}
    />
    {error && <div className="text-danger">{error}</div>}
  </div>
);

const TextAreaField = ({ id, label, value, onChange, error, ...props }) => (
  <div className="mb-3">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <textarea
      id={id}
      className="form-control"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      {...props}
    />
    {error && <div className="text-danger">{error}</div>}
  </div>
);

export default function CM_90_1045() {
  const navigate = useNavigate();
  const [popupType, setPopupType] = useState('');
  const [open1004, setOpen1004] = useState(false);
  const [message, setMessage] = useState('');
  const [errors, setErrors] = useState({});
  const [form, setForm] = useState({
    title: '',
    content: '',
    codeValue: 1,
    isDisplay: 1,
  });

  const updateForm = (field, value) => {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleRegisterClick = async (e) => {
    e.preventDefault();
    if (validateForm()) {
      const result = await axiosInstance.post('/admin/menu/notice/upload', form);
      if (result.data.result) {
        setMessage(CMMessage.MSG_INF_001);
        setPopupType('success');
        setOpen1004(true);
      }
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (form.title.trim().length === 0) newErrors.title = CMMessage.MSG_ERR_002('タイトル名');

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3"> お知らせ 登録</h5>
        <form onSubmit={handleRegisterClick}>
          {/* タイトル */}
          <InputField
            id="title"
            label="タイトル"
            type="text"
            value={form.title}
            onChange={(value) => updateForm('title', value)}
            error={errors.title}
          />

          {/* 分類 */}
          <RadioGroup
            name="codeValue"
            label="分類"
            options={CATEGORIES}
            value={form.codeValue}
            onChange={(value) => updateForm('codeValue', value)}
          />

          {/* 使用 有無 */}
          <RadioGroup
            name="isDisplay"
            label="使用 有無"
            options={DISPLAY_OPTIONS}
            value={form.isDisplay}
            onChange={(value) => updateForm('isDisplay', value)}
          />

          {/* 内容 */}
          <TextAreaField
            id="content"
            label="内容"
            rows="15"
            value={form.content}
            onChange={(value) => updateForm('content', value)}
            error={errors.content}
          />

          {/* ボタン */}
          <div className="d-flex justify-content-center gap-2">
            <button type="submit" className="btn btn-primary btn-fixed-width">
              作成する
            </button>
            <Link to="/admin/menu/notice" className="btn btn-secondary btn-fixed-width">
              戻る
            </Link>
          </div>
        </form>
        {/* お知らせポップアップ */}
        <CM_99_1004
          isOpen={open1004}
          onClose={() => {
            setOpen1004(false);
            if (popupType === 'success') {
              navigate(-1);
            }
          }}
          Message={message}
        />
      </div>
    </div>
  );
}
