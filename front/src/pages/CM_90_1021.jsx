import { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { countries } from '../data/countries';
import axiosInstance from '../services/axiosInstance';
import CMMessage from '../constants/CMMessage';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

const INITIAL_FORM_STATE = {
  memberNumber: '',
  loginId: '',
  name: '',
  nationality: '',
  password: '',
  passwordConfirm: '',
  phoneCountryCode: '+82',
  phoneNumber1: '',
  phoneNumber2: '',
  phoneNumber3: '',
  email: '',
  postalCode: '',
  addressMain: '',
  addressDetail1: '',
  addressDetail2: '',
  addressDetail3: '',
  genderValue: '1',
  birthYear: '',
  birthMonth: '',
  birthDay: '',
  note: '',
  userStatusValue: 1,
  roleId: 1,
  refundAccountHolder: '',
  refundAccountNumber: '',
  refundAccountBank: '',
  pccc: '',
};

const PHONE_COUNTRY_CODES = countries
  .reduce((acc, country) => {
    const phoneCode = `+${country.phoneCode}`;
    if (!acc.find((item) => item.value === phoneCode)) {
      acc.push({
        value: phoneCode,
        label: phoneCode,
      });
    }
    return acc;
  }, [])
  .sort((a, b) => parseInt(a.value.slice(1)) - parseInt(b.value.slice(1)));

const AUTHORITY_OPTIONS = [
  { value: 2, label: 'Manager' },
  { value: 3, label: 'User' },
];

const STATUS_OPTIONS = [
  { value: 1, label: '활성화' },
  { value: 2, label: '비활성화' },
  { value: 3, label: '탈퇴' },
];

const GENDER_OPTIONS = [
  { value: '1', label: '남성' },
  { value: '2', label: '여성' },
  { value: '3', label: '선택안함' },
];

const useModal = () => {
  const [modals, setModals] = useState({
    confirm: false,
    loading: false,
    error: false,
    info: false,
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

const useApiHandler = (navigate, openModal, closeModal) => {
  return useCallback(
    async (apiCall, showLoading = true) => {
      if (showLoading) openModal('loading');

      try {
        const result = await apiCall();
        return result;
      } catch (error) {
        openModal('error', CMMessage.MSG_ERR_001);
        return null;
      } finally {
        if (showLoading) closeModal('loading');
      }
    },
    [navigate, openModal, closeModal]
  );
};

const SelectField = ({
  id,
  label,
  value,
  onChange,
  options = [],
  error,
  placeholder = '선택하세요',
  required = false,
  ...props
}) => (
  <div className="mb-3">
    <label htmlFor={id} className="form-label">
      {label}
    </label>
    <select
      id={id}
      className="form-select"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      {...props}
    >
      <option value="">{placeholder}</option>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
    {error && <div className="text-danger mt-1">{error}</div>}
  </div>
);

const InputField = ({
  id,
  label,
  value,
  onChange,
  error,
  required = false,
  as = 'input',
  ...props
}) => {
  const Component = as === 'textarea' ? 'textarea' : 'input';
  return (
    <div className="mb-3">
      <label htmlFor={id} className="form-label">
        {label}
      </label>
      <Component
        id={id}
        className="form-control"
        value={value ?? ''}
        onChange={(e) => onChange(e.target.value)}
        {...props}
      />
      {error && <div className="text-danger mt-1">{error}</div>}
    </div>
  );
};

const PhoneNumberField = ({
  countryCode,
  phoneNumber1,
  phoneNumber2,
  phoneNumber3,
  onCountryCodeChange,
  onPhoneNumber1Change,
  onPhoneNumber2Change,
  onPhoneNumber3Change,
  error,
  required = false,
}) => (
  <div className="mb-3">
    <label className="form-label">휴대폰 번호</label>
    <div className="d-flex gap-2">
      <select
        className="form-select"
        style={{ width: '100px', flexShrink: 0 }}
        value={countryCode}
        onChange={(e) => onCountryCodeChange(e.target.value)}
      >
        {PHONE_COUNTRY_CODES.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      <input
        type="text"
        className="form-control"
        style={{ width: '80px', flexShrink: 0 }}
        value={phoneNumber1}
        onChange={(e) => onPhoneNumber1Change(e.target.value)}
        maxLength={4}
      />
      <span style={{ alignSelf: 'center' }}>-</span>

      <input
        type="text"
        className="form-control"
        style={{ width: '80px', flexShrink: 0 }}
        value={phoneNumber2}
        onChange={(e) => onPhoneNumber2Change(e.target.value)}
        maxLength={4}
      />
      <span style={{ alignSelf: 'center' }}>-</span>
      <input
        type="text"
        className="form-control"
        style={{ width: '80px', flexShrink: 0 }}
        value={phoneNumber3}
        onChange={(e) => onPhoneNumber3Change(e.target.value)}
        maxLength={4}
      />
    </div>
    {error && <div className="text-danger mt-1">{error}</div>}
  </div>
);

const RadioButtonGroup = ({ label, name, value, onChange, options, error, required = false }) => (
  <div className="mb-3">
    <label className="form-label">{label}</label>
    <div className="d-flex gap-3">
      {options.map((option) => (
        <div key={option.value} className="form-check">
          <input
            className="form-check-input"
            type="radio"
            name={name}
            id={`${name}-${option.value}`}
            value={option.value}
            checked={value === option.value}
            onChange={(e) => onChange(e.target.value)}
          />
          <label className="form-check-label" htmlFor={`${name}-${option.value}`}>
            {option.label}
          </label>
        </div>
      ))}
    </div>
    {error && <div className="text-danger mt-1">{error}</div>}
  </div>
);

const BirthDateField = ({
  year,
  month,
  day,
  onYearChange,
  onMonthChange,
  onDayChange,
  error,
  required = false,
}) => {
  const yearOptions = useMemo(() => {
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let i = currentYear; i >= 1950; i--) {
      years.push({ value: i.toString(), label: i.toString() });
    }
    return years;
  }, []);

  const monthOptions = useMemo(() => {
    const months = [];
    for (let i = 1; i <= 12; i++) {
      const monthStr = i.toString().padStart(2, '0');
      months.push({ value: monthStr, label: monthStr });
    }
    return months;
  }, []);

  const dayOptions = useMemo(() => {
    if (!year || !month) {
      const days = [];
      for (let i = 1; i <= 31; i++) {
        const dayStr = i.toString().padStart(2, '0');
        days.push({ value: dayStr, label: dayStr });
      }
      return days;
    }

    const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate();

    const days = [];
    for (let i = 1; i <= daysInMonth; i++) {
      const dayStr = i.toString().padStart(2, '0');
      days.push({ value: dayStr, label: dayStr });
    }
    return days;
  }, [year, month]);

  useEffect(() => {
    if (year && month && day) {
      const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate();
      const selectedDay = parseInt(day);

      if (selectedDay > daysInMonth) {
        onDayChange(daysInMonth.toString().padStart(2, '0'));
      }
    }
  }, [year, month, day, onDayChange]);

  return (
    <div className="mb-3">
      <label className="form-label">생년월일</label>
      <div className="d-flex gap-2">
        <select className="form-select" value={year} onChange={(e) => onYearChange(e.target.value)}>
          <option value="">년</option>
          {yearOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <select
          className="form-select"
          value={month}
          onChange={(e) => onMonthChange(e.target.value)}
        >
          <option value="">월</option>
          {monthOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <select className="form-select" value={day} onChange={(e) => onDayChange(e.target.value)}>
          <option value="">일</option>
          {dayOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      {error && <div className="text-danger mt-1">{error}</div>}
    </div>
  );
};

export default function CM_90_1021() {
  const { memberId } = useParams();
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);
  const [form, setForm] = useState(INITIAL_FORM_STATE);
  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);
  const [loading, setLoading] = useState(false);

  // 특정 유저 조회
  const fetchByUser = useCallback(async () => {
    setLoading(true);
    const result = await apiHandler(() =>
      axiosInstance.get('/admin/users/findUser', {
        params: { memberId: memberId },
      })
    );
    if (result.data.result) {
      const mappedData = mapServerDataToForm(result.data.resultList);
      setForm(mappedData);
    }
    setLoading(false);
  }, [apiHandler, navigate]);

  useEffect(() => {
    fetchByUser();
  }, [fetchByUser]);

  const mapServerDataToForm = (resultData) => {
    const parsePhoneNumber = (phoneNumber) => {
      if (!phoneNumber) return { countryCode: '+82', number1: '010', number2: '', number3: '' };

      const match = phoneNumber.match(/^\(\+(\d+)\)(.+)$/);
      if (!match) return { countryCode: '+82', number1: '', number2: '', number3: '' };

      const countryCode = `+${match[1]}`;
      const numbersWithDash = match[2];

      const numberParts = numbersWithDash.split('-');

      return {
        countryCode,
        number1: numberParts[0] || '',
        number2: numberParts[1] || '',
        number3: numberParts[2] || '',
      };
    };

    const parseBirthDate = (birthDate) => {
      if (!birthDate) return { year: '', month: '', day: '' };

      const parts = birthDate.split('-');
      return {
        year: parts[0] || '',
        month: parts[1] || '',
        day: parts[2] || '',
      };
    };

    const phoneInfo = parsePhoneNumber(resultData.phoneNumber);
    const birthInfo = parseBirthDate(resultData.birthDate);

    const findCountryByPhone = (phoneCode) => {
      const code = phoneCode.replace('+', '');
      const country = countries.find((c) => c.phoneCode === code);
      return country ? country.name.en : 'South Korea';
    };

    return {
      memberNumber: resultData.memberNumber,
      loginId: resultData.loginId || '',
      name: resultData.name || '',
      nationality: findCountryByPhone(phoneInfo.countryCode),
      password: '',
      passwordConfirm: '',
      phoneCountryCode: phoneInfo.countryCode,
      phoneNumber1: phoneInfo.number1,
      phoneNumber2: phoneInfo.number2,
      phoneNumber3: phoneInfo.number3,
      email: resultData.email || '',
      postalCode: resultData.postalCode,
      addressMain: resultData.addressMain,
      addressDetail1: resultData.addressDetail1 || '',
      addressDetail2: resultData.addressDetail2 || '',
      addressDetail3: resultData.addressDetail3 || '',
      genderValue: resultData.genderValue.toString() || '1',
      birthYear: birthInfo.year,
      birthMonth: birthInfo.month,
      birthDay: birthInfo.day,
      note: resultData.note || '',
      userStatusValue: resultData.userStatusValue || 1,
      roleId: resultData.roleId || 1,
      refundAccountHolder: resultData.refundAccountHolder || '',
      refundAccountNumber: resultData.refundAccountNumber || '',
      refundAccountBank: resultData.refundAccountBank || '',
      pccc: resultData.pccc || '',
    };
  };

  const togglePasswordVisibility = useCallback(() => {
    setShowPassword((prev) => !prev);
  }, []);

  const togglePasswordConfirmVisibility = useCallback(() => {
    setShowPasswordConfirm((prev) => !prev);
  }, []);

  const updateForm = useCallback((field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const nationalityOptions = useMemo(() => {
    return countries.map((country) => ({
      value: country.name.en,
      label: country.name.kr,
    }));
  }, []);

  const handleNationalityChange = useCallback(
    (nationality) => {
      updateForm('nationality', nationality);
      const selectedCountry = countries.find((country) => country.name.en === nationality);
      if (selectedCountry) {
        const phoneCode = `+${selectedCountry.phoneCode}`;
        updateForm('phoneCountryCode', phoneCode);
      }
    },
    [updateForm]
  );

  const validateForm = useCallback(() => {
    const newErrors = {};

    if (!form.name) newErrors.name = '이름을 입력하세요.';
    if (!form.nationality) newErrors.nationality = '국적/거주국가를 선택하세요.';
    if (form.password) {
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
      if (!passwordRegex.test(form.password)) {
        newErrors.password = '영문+숫자+특수문자 조합으로 최소 8자 이상 입력하세요.';
      }
    }
    if (form.password !== form.passwordConfirm)
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
    if (!form.phoneNumber2 || !form.phoneNumber3)
      newErrors.phoneNumber = '휴대폰 번호를 입력하세요.';
    if (!form.email) newErrors.email = '이메일 주소를 입력하세요.';
    if (!form.postalCode) newErrors.postalCode = '우편번호를 입력하세요.';
    if (!form.addressMain) newErrors.addressMain = '주소를 입력하세요.';
    if (!form.addressDetail1) newErrors.addressDetail1 = '상세 주소를 입력하세요.';
    if (!form.genderValue) newErrors.genderValue = '성별을 선택하세요.';
    if (!form.birthYear || !form.birthMonth || !form.birthDay)
      newErrors.birthDate = '생년월일을 입력하세요.';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [form]);

  const handleSubmit = useCallback(
    async (e) => {
      e.preventDefault();

      if (!validateForm()) {
        console.log('?');
        return;
      }
      const userData = {
        memberNumber: form.memberNumber,
        loginId: form.loginId,
        name: form.name,
        nationality: form.nationality,
        password: form.password,
        phoneNumber: `(${form.phoneCountryCode})${form.phoneNumber1}-${form.phoneNumber2}-${form.phoneNumber3}`,
        email: form.email,
        postalCode: form.postalCode,
        addressMain: form.addressMain,
        addressDetail1: form.addressDetail1,
        addressDetail2: form.addressDetail2,
        addressDetail3: form.addressDetail3,
        genderValue: form.genderValue.toString(),
        birthDate: `${form.birthYear}-${form.birthMonth}-${form.birthDay}`,
        userStatusValue: form.userStatusValue,
        roleId: form.roleId,
        note: form.note,
        refundAccountHolder: form.refundAccountHolder,
        refundAccountNumber: form.refundAccountNumber,
        refundAccountBank: form.refundAccountBank,
        pccc: form.pccc,
      };

      const result = await apiHandler(() => axiosInstance.post('/admin/users/update', userData));

      if (result.data.result) {
        open('info', result.data.message);
      } else {
        open('error', result.data.message);
      }
    },
    [form, validateForm, navigate, apiHandler, open]
  );

  const handleModalClose = useCallback(() => {
    close('info');
    navigate(-1);
  }, [message, close, navigate]);

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">{memberId} 조회/수정</h5>

        <form onSubmit={handleSubmit}>
          <div className="row">
            <InputField
              id="loginId"
              label="ID"
              value={form.loginId}
              onChange={(value) => updateForm('loginId', value)}
              error={errors.loginId}
              required
              disabled
            />

            <div className="mb-3">
              <label htmlFor="password" className="form-label">
                비밀번호
              </label>
              <div className="mb-1">
                <small className="text-muted">• 영문+숫자+특수문자 조합 (최소 8자 이상)</small>
              </div>
              <div className="position-relative">
                <input
                  id="password"
                  className="form-control"
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                  onChange={(e) => updateForm('password', e.target.value)}
                  placeholder="비밀번호를 입력해주세요"
                />
                <button
                  type="button"
                  className="btn position-absolute top-0 end-0 h-100"
                  style={{ zIndex: 10 }}
                  onClick={togglePasswordVisibility}
                >
                  <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                </button>
              </div>
              {errors.password && <div className="text-danger mt-1">{errors.password}</div>}
            </div>

            <div className="mb-3">
              <label htmlFor="passwordConfirm" className="form-label">
                비밀번호 확인
              </label>
              <div className="position-relative">
                <input
                  id="passwordConfirm"
                  className="form-control"
                  type={showPasswordConfirm ? 'text' : 'password'}
                  value={form.passwordConfirm}
                  onChange={(e) => updateForm('passwordConfirm', e.target.value)}
                  placeholder="비밀번호를 입력해주세요"
                />
                <button
                  type="button"
                  className="btn position-absolute top-0 end-0 h-100"
                  style={{ zIndex: 10 }}
                  onClick={togglePasswordConfirmVisibility}
                >
                  <i className={`bi ${showPasswordConfirm ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                </button>
              </div>
              {errors.passwordConfirm && (
                <div className="text-danger mt-1">{errors.passwordConfirm}</div>
              )}
            </div>

            <InputField
              id="name"
              label="이름"
              value={form.name}
              onChange={(value) => updateForm('name', value)}
              error={errors.name}
              required
            />

            <InputField
              id="email"
              label="이메일 주소"
              type="email"
              value={form.email}
              onChange={(value) => updateForm('email', value)}
              error={errors.email}
              required
              disabled
            />

            <RadioButtonGroup
              label="성별"
              name="genderValue"
              value={form.genderValue}
              onChange={(value) => updateForm('genderValue', value)}
              options={GENDER_OPTIONS}
              error={errors.genderValue}
              required
            />

            <BirthDateField
              year={form.birthYear}
              month={form.birthMonth}
              day={form.birthDay}
              onYearChange={(value) => updateForm('birthYear', value)}
              onMonthChange={(value) => updateForm('birthMonth', value)}
              onDayChange={(value) => updateForm('birthDay', value)}
              error={errors.birthDate}
              required
            />

            <SelectField
              id="nationality"
              label="국적 / 거주국가"
              value={form.nationality}
              onChange={handleNationalityChange}
              options={nationalityOptions}
              error={errors.nationality}
              required
            />

            <PhoneNumberField
              countryCode={form.phoneCountryCode}
              phoneNumber1={form.phoneNumber1}
              phoneNumber2={form.phoneNumber2}
              phoneNumber3={form.phoneNumber3}
              onCountryCodeChange={(value) => updateForm('phoneCountryCode', value)}
              onPhoneNumber1Change={(value) => updateForm('phoneNumber1', value)}
              onPhoneNumber2Change={(value) => updateForm('phoneNumber2', value)}
              onPhoneNumber3Change={(value) => updateForm('phoneNumber3', value)}
              error={errors.phoneNumber}
              required
            />

            <InputField
              id="postalCode"
              label="우편번호"
              value={form.postalCode}
              onChange={(value) => updateForm('postalCode', value)}
              error={errors.postalCode}
              required
            />

            <InputField
              id="addressMain"
              label="주소"
              value={form.addressMain}
              onChange={(value) => updateForm('addressMain', value)}
              error={errors.addressMain}
              required
            />

            <InputField
              id="addressDetail1"
              label="상세 주소 1"
              value={form.addressDetail1}
              onChange={(value) => updateForm('addressDetail1', value)}
              error={errors.addressMain}
              required
            />

            <InputField
              id="addressDetail2"
              label="상세 주소 2"
              value={form.addressDetail2}
              onChange={(value) => updateForm('addressDetail2', value)}
            />

            <InputField
              id="addressDetail3"
              label="상세 주소 3"
              value={form.addressDetail3}
              onChange={(value) => updateForm('addressDetail3', value)}
            />

            <InputField
              id="refundAccountHolder"
              label="환불 예금주"
              value={form.refundAccountHolder}
              onChange={(value) => updateForm('refundAccountHolder', value)}
            />
            <InputField
              id="refundAccountNumber"
              label="환불 계좌번호"
              value={form.refundAccountNumber}
              onChange={(value) => updateForm('refundAccountNumber', value)}
            />
            <InputField
              id="refundAccountBank"
              label="환불 은행"
              value={form.refundAccountBank}
              onChange={(value) => updateForm('refundAccountBank', value)}
            />
            <InputField
              id="pccc"
              label="통관 번호"
              value={form.pccc}
              onChange={(value) => updateForm('pccc', value)}
            />
            <InputField
              id="note"
              label="비고"
              value={form.note}
              onChange={(value) => updateForm('note', value)}
            />
            <SelectField
              id="roleId"
              label="권한"
              value={form.roleId}
              onChange={(value) => updateForm('roleId', value)}
              options={AUTHORITY_OPTIONS}
              error={errors.roleId}
            />

            <SelectField
              id="userStatusValue"
              label="상태"
              value={form.userStatusValue}
              onChange={(value) => updateForm('userStatusValue', value)}
              options={STATUS_OPTIONS}
              error={errors.userStatusValue}
            />

            <InputField
              id="note"
              label="비고"
              value={form.note}
              onChange={(value) => updateForm('note', value)}
            />
          </div>

          <div className="d-flex justify-content-center gap-2 mt-4">
            <button type="submit" className="btn btn-primary btn-fixed-width">
              변경 하기
            </button>
          </div>
        </form>

        <CM_99_1001
          isOpen={modals.confirm}
          onClose={() => close('confirm')}
          onConfirm={() => {}}
          Message={message}
        />
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
        <CM_99_1004 isOpen={modals.info} onClose={handleModalClose} Message={message} />
      </div>
    </div>
  );
}
