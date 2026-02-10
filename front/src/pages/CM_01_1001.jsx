// React
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

import axios from 'axios';

import { useEmailVerification } from '../hooks/useEmailVerification';

import VerificationModal from '../components/verificationModal';
import SignupSuccessLayout from '../components/layouts/SignupSuccessLayout';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';

import useCountries from '../hooks/useCountries';

import '../styles/CM_01_1001.css';

const signupSchema = z
  .object({
    loginId: z
      .string()
      .regex(
        /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z0-9]{6,20}$/,
        'IDは6〜20文字の英字と数字を含めてください。'
      )
      .or(z.literal('')),
    password: z
      .string()
      .regex(
        /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,20}$/,
        'パスワードは8〜20文字の英字・数字・記号の組み合わせにしてください。'
      )
      .or(z.literal('')),
    passwordConfirm: z.string().or(z.literal('')),
    email: z.string().email('有効なメール形式ではありません。').or(z.literal('')),
    name: z.string().or(z.literal('')),
    gender: z.string().or(z.literal('')),
    nationality: z.string().min(1, '国籍を選択してください。'),
    phone1: z
      .string()
      .regex(/^\d{2,3}$/, '2〜3桁の数字')
      .or(z.literal('')),
    phone2: z
      .string()
      .regex(/^\d{3,4}$/, '3〜4桁の数字')
      .or(z.literal('')),
    phone3: z
      .string()
      .regex(/^\d{4}$/, '4桁の数字')
      .or(z.literal('')),
    birthDate: z.date().nullable(),
    postalCode: z.string().or(z.literal('')),
    addressMain: z.string().or(z.literal('')),
    addressDetail1: z.string().or(z.literal('')),
    addressDetail2: z.string().optional(),
    addressDetail3: z.string().optional(),
    pccc: z.string().optional().or(z.literal('')),
    termsAgreed: z.boolean(),
  })
  .refine((data) => data.password === data.passwordConfirm, {
    message: 'パスワードが一致しません。',
    path: ['passwordConfirm'],
  })
  .superRefine((data, ctx) => {
    const checkRequired = (field, msg) => {
      if (!data[field] || data[field] === '') {
        ctx.addIssue({ code: z.ZodIssueCode.custom, message: msg, path: [field] });
      }
    };
    checkRequired('loginId', 'IDを入力してください。');
    checkRequired('password', 'パスワードを入力してください。');
    checkRequired('passwordConfirm', 'パスワード（確認）を入力してください。');
    checkRequired('email', 'メールアドレスを入力してください。');
    checkRequired('name', '氏名を入力してください。');
    checkRequired('gender', '性別を選択してください。');
    checkRequired('phone1', '番号を入力してください。');
    checkRequired('phone2', '番号を入力してください。');
    checkRequired('phone3', '番号を入力してください。');
    checkRequired('postalCode', '郵便番号を入力してください。');
    checkRequired('addressMain', '住所を入力してください。');
    checkRequired('addressDetail1', '詳細住所を入力してください。');
    if (!data.birthDate) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: '生年月日を選択してください。',
        path: ['birthDate'],
      });
    } else {
      const today = new Date();
      const minAllowedBirthDate = new Date(today.getFullYear() - 12, today.getMonth(), today.getDate());
      if (data.birthDate > minAllowedBirthDate) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: '満12歳以上のみ登録できます。',
          path: ['birthDate'],
        });
      }
    }

    if (data.nationality === 'KR') {
      const pcccValue = data.pccc?.trim();
      if (!pcccValue) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: '個人通関固有番号を入力してください。',
          path: ['pccc'],
        });
      } else if (!/^P\d{12}$/.test(pcccValue)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Pから始まる13桁の番号を入力してください。',
          path: ['pccc'],
        });
      }
    }
  });

const defaultGenderOptions = [
  { codeValue: 1, codeValueName: '男性' },
  { codeValue: 2, codeValueName: '女性' },
];

export default function CM_01_1001() {
  const [isIdChecked, setIsIdChecked] = useState(false);
  const [isSignupSuccess, setIsSignupSuccess] = useState(false);
  const [genderOptions, setGenderOptions] = useState([]);

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [signupError, setSignupError] = useState('');
  const [birthParts, setBirthParts] = useState({ year: '', month: '', day: '' });
  const monthRef = useRef(null);
  const dayRef = useRef(null);
  const { countries } = useCountries();
  const {
    register,
    handleSubmit,
    watch,
    setError,
    setValue,
    clearErrors,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(signupSchema),
    mode: 'onTouched',
    defaultValues: {
      nationality: 'JP',
      termsAgreed: false,
      gender: '',
      birthDate: null,
      pccc: '',
      phone1: '',
      phone2: '',
      phone3: '',
      loginId: '',
      password: '',
      passwordConfirm: '',
      email: '',
      name: '',
      postalCode: '',
      addressMain: '',
      addressDetail1: '',
      addressDetail2: '',
      addressDetail3: '',
    },
  });

  useEffect(() => {
    const subscription = watch((value, { name }) => {
      if (name) {
        clearErrors(name);
      }
    });
    return () => subscription.unsubscribe();
  }, [watch, clearErrors]);

  const watchNationality = watch('nationality');
  const watchLoginId = watch('loginId');
  const watchEmail = watch('email');

  const handleBirthPartChange = (part, rawValue) => {
    const maxLen = part === 'year' ? 4 : 2;
    const nextValue = rawValue.replace(/\D/g, '').slice(0, maxLen);
    setBirthParts((prev) => ({ ...prev, [part]: nextValue }));
    clearErrors('birthDate');

    if (part === 'year' && nextValue.length === 4) {
      monthRef.current?.focus();
    } else if (part === 'month' && nextValue.length === 2) {
      dayRef.current?.focus();
    }
  };

  useEffect(() => {
    const { year, month, day } = birthParts;
    if (!year && !month && !day) {
      setValue('birthDate', null, { shouldValidate: false });
      return;
    }

    if (year.length !== 4 || month.length !== 2 || day.length !== 2) {
      setValue('birthDate', null, { shouldValidate: false });
      return;
    }

    const y = Number(year);
    const m = Number(month);
    const d = Number(day);
    const date = new Date(y, m - 1, d);
    const isValidDate =
      Number.isInteger(y) &&
      Number.isInteger(m) &&
      Number.isInteger(d) &&
      m >= 1 &&
      m <= 12 &&
      d >= 1 &&
      d <= 31 &&
      date.getFullYear() === y &&
      date.getMonth() === m - 1 &&
      date.getDate() === d;

    setValue('birthDate', isValidDate ? date : null, { shouldValidate: false });
  }, [birthParts, setValue]);
  const {
    verificationCode,
    setVerificationCode,
    isModalOpen,
    setIsModalOpen,
    timer,
    isVerified,
    handleRequestCode,
    handleVerifyCode,
    loading: verificationLoading,
    error: verificationError,
  } = useEmailVerification();
  const navigate = useNavigate();

  useEffect(() => {
    axios
      .get(`${import.meta.env.VITE_API_BASE_URL}/common-codes?group=010`)
      .then((res) => {
        const fetched = Array.isArray(res.data) ? res.data : res.data?.resultList || [];
        setGenderOptions(fetched.length > 0 ? fetched : defaultGenderOptions);
      })
      .catch((err) => {
        console.error(err);
        setGenderOptions(defaultGenderOptions);
      });
  }, []);

  const handleCheckId = async () => {
    if (!watchLoginId) {
      setError('loginId', { message: 'IDを入力してください。' });
      return;
    }
    try {
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/guest/check-id?loginId=${watchLoginId}`
      );
      if (response.data === 0) {
        setIsIdChecked(true);
        clearErrors('loginId');
      } else {
        setError('loginId', { message: 'すでに使用されているIDです。' });
        setIsIdChecked(false);
      }
    } catch (e) {
      ('エラーが発生しました');
    }
  };

  useEffect(() => {
    if (isIdChecked) {
      setIsIdChecked(false);
    }
  }, [watchLoginId]);

  const handleVerify = () => {
    const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/guest/verify-email`;
    handleVerifyCode(
      apiUrl,
      { email: watchEmail, code: verificationCode },
      (data) => data?.resultList?.verified === true
    );
  };

  const handleOpenTermsPage = (policyType) => {
    const termsBaseUrl = '/terms';
    if (policyType === 'terms-of-service') {
      window.open(`${termsBaseUrl}?type=service`, '_blank');
    } else if (policyType === 'privacy-policy') {
      window.open(`${termsBaseUrl}?type=privacy-policy`, '_blank');
    }
  };

  const onSubmit = async (data) => {
    if (!isIdChecked) {
      setError('loginId', { message: 'IDの重複チェックを完了してください。' });
      return;
    }
    if (!isVerified) {
      setError('email', { type: 'manual', message: 'メール認証を完了してください。' });
      return;
    }

    const selectedCountry = countries.find((c) => c.isoCode === data.nationality);
    const phoneCode = selectedCountry?.phoneCode || '81';
    const birthStr = data.birthDate
      ? `${data.birthDate.getFullYear()}-${String(data.birthDate.getMonth() + 1).padStart(2, '0')}-${String(data.birthDate.getDate()).padStart(2, '0')}`
      : '';
    const submissionData = {
      user: {
        ...data,
        genderValue: parseInt(data.gender),
        birthDate: birthStr,
        phoneNumber: `(+${phoneCode})${data.phone1}-${data.phone2}-${data.phone3}`,
      },
      address: {
        addressNickname: '基本住所',
        postalCode: data.postalCode,
        addressMain: data.addressMain,
        addressDetail1: data.addressDetail1,
        addressDetail2: data.addressDetail2 || '',
        addressDetail3: data.addressDetail3 || '',
        isDefault: true,
      },
    };

    try {
      await axios.post(`${import.meta.env.VITE_API_BASE_URL}/guest/signup`, submissionData);
      setIsSignupSuccess(true);
    } catch (error) {
      setSignupError(error.response?.data?.message);
    }
  };

  return (
    <div className="vh-100-container">
      {isSignupSuccess ? (
        <SignupSuccessLayout />
      ) : (
        <div className="signup-container">
          <form className="signup-form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <fieldset className="mb-4">
              <legend className="form-legend">ログイン情報</legend>
              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> ID
                </label>
                <div className="d-flex align-items-center">
                  <input
                    {...register('loginId')}
                    className={`form-control ${errors.loginId ? 'is-invalid' : ''}`}
                    placeholder="IDを入力してください"
                  />
                  <button
                    type="button"
                    className="btn btn-primary fixed-btn-size ms-2 dup-check-btn"
                    onClick={handleCheckId}
                  >
                    重複チェック
                  </button>
                </div>
                {errors.loginId && (
                  <div className="invalid-feedback d-block">{errors.loginId.message}</div>
                )}
                {!errors.loginId && isIdChecked && (
                  <div className="text-success mt-1" style={{ fontSize: '0.875em' }}>
                    使用可能なIDです。
                  </div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> パスワード
                </label>
                <div className="password-input-container">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    {...register('password')}
                    className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                    placeholder="パスワードを入力してください"
                  />
                  <button
                    type="button"
                    className="password-toggle-button"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                  </button>
                </div>
                {errors.password && (
                  <div className="invalid-feedback d-block">{errors.password.message}</div>
                )}
                <small className="form-text text-muted">
                  英字+数字+記号の組み合わせ（8〜20文字）
                </small>
              </div>

              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> パスワード（確認）
                </label>
                <div className="password-input-container">
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    {...register('passwordConfirm')}
                    className={`form-control ${errors.passwordConfirm ? 'is-invalid' : ''}`}
                    placeholder="パスワードを再入力してください"
                  />
                  <button
                    type="button"
                    className="password-toggle-button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    <i className={`bi ${showConfirmPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                  </button>
                </div>
                {errors.passwordConfirm && (
                  <div className="invalid-feedback d-block">{errors.passwordConfirm.message}</div>
                )}
              </div>
            </fieldset>

            <fieldset className="mb-4">
              <legend className="form-legend">会員情報</legend>
              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> メールアドレス
                </label>
                <div className="d-flex align-items-center">
                  <input
                    {...register('email')}
                    className={`form-control  ${errors.email ? 'is-invalid' : ''}`}
                    placeholder="example@email.com"
                    disabled={isVerified}
                  />
                  <button
                    type="button"
                    className="btn btn-primary fixed-btn-size ms-2"
                    style={{ width: '100px' }}
                    onClick={() =>
                      handleRequestCode(
                        `${import.meta.env.VITE_API_BASE_URL}/guest/send-verification-email`,
                        { email: watchEmail }
                      )
                    }
                    disabled={isVerified}
                  >
                    {isVerified ? '認証完了' : '認証'}
                  </button>
                </div>
                {errors.email && (
                  <div className="invalid-feedback d-block">{errors.email.message}</div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> 氏名
                </label>
                <input
                  {...register('name')}
                  className={`form-control ${errors.name ? 'is-invalid' : ''}`}
                  placeholder="氏名を入力してください"
                />
                {errors.name && (
                  <div className="invalid-feedback d-block">{errors.name.message}</div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label d-block font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> 性別
                </label>
                {genderOptions.map((opt) => (
                  <div className="form-check form-check-inline" key={opt.codeValue}>
                    <input
                      {...register('gender')}
                      className="form-check-input"
                      type="radio"
                      value={String(opt.codeValue)}
                      id={`gen-${opt.codeValue}`}
                    />
                    <label className="form-check-label" htmlFor={`gen-${opt.codeValue}`}>
                      {opt.codeValueName}
                    </label>
                  </div>
                ))}
                {errors.gender && (
                  <div className="invalid-feedback d-block">{errors.gender.message}</div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> 国籍 / 居住国
                </label>
                <select {...register('nationality')} className="form-select">
                  {countries.map((c) => (
                    <option key={c.isoCode} value={c.isoCode}>
                      {c.name.jp}
                    </option>
                  ))}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> 携帯電話番号
                </label>
                <div className="d-flex align-items-center mb-1">
                  <input
                    type="text"
                    className="form-control me-2"
                    style={{ width: '85px', backgroundColor: '#f8f9fa' }}
                    value={`+${countries.find((c) => c.isoCode === watchNationality)?.phoneCode || '81'}`}
                    readOnly
                  />
                  <input
                    {...register('phone1')}
                    className={`form-control me-2 ${errors.phone1 ? 'is-invalid' : ''}`}
                    maxLength="3"
                  />
                  <span className="me-2">-</span>
                  <input
                    {...register('phone2')}
                    className={`form-control me-2 ${errors.phone2 ? 'is-invalid' : ''}`}
                    maxLength="4"
                  />
                  <span className="me-2">-</span>
                  <input
                    {...register('phone3')}
                    className={`form-control ${errors.phone3 ? 'is-invalid' : ''}`}
                    maxLength="4"
                  />
                </div>
                {(errors.phone1 || errors.phone2 || errors.phone3) && (
                  <div className="invalid-feedback d-block">
                    {errors.phone1?.message || errors.phone2?.message || errors.phone3?.message}
                  </div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label d-block font-weight">
                  <i className="bi bi-asterisk required-asterisk"></i> 生年月日
                </label>
                <input type="hidden" {...register('birthDate')} />
                <div className="d-flex align-items-center gap-2">
                  <input
                    type="text"
                    inputMode="numeric"
                    className={`form-control ${errors.birthDate ? 'is-invalid' : ''}`}
                    style={{ maxWidth: '120px' }}
                    placeholder="YYYY"
                    maxLength={4}
                    value={birthParts.year}
                    onChange={(e) => handleBirthPartChange('year', e.target.value)}
                  />
                  <span>年</span>
                  <input
                    ref={monthRef}
                    type="text"
                    inputMode="numeric"
                    className={`form-control ${errors.birthDate ? 'is-invalid' : ''}`}
                    style={{ maxWidth: '90px' }}
                    placeholder="MM"
                    maxLength={2}
                    value={birthParts.month}
                    onChange={(e) => handleBirthPartChange('month', e.target.value)}
                  />
                  <span>月</span>
                  <input
                    ref={dayRef}
                    type="text"
                    inputMode="numeric"
                    className={`form-control ${errors.birthDate ? 'is-invalid' : ''}`}
                    style={{ maxWidth: '90px' }}
                    placeholder="DD"
                    maxLength={2}
                    value={birthParts.day}
                    onChange={(e) => handleBirthPartChange('day', e.target.value)}
                  />
                  <span>日</span>
                </div>
                <small className="form-text text-muted">例: 1998年 03月 21日</small>
                {errors.birthDate && (
                  <div className="invalid-feedback d-block">{errors.birthDate.message}</div>
                )}
              </div>

              <div className="mt-4">
                <div className="mb-3">
                  <label className="form-label font-weight">
                    <i className="bi bi-asterisk required-asterisk"></i> 郵便番号
                  </label>
                  <input
                    {...register('postalCode')}
                    className={`form-control ${errors.postalCode ? 'is-invalid' : ''}`}
                    placeholder="郵便番号を入力してください"
                  />
                  {errors.postalCode && (
                    <div className="invalid-feedback d-block">{errors.postalCode.message}</div>
                  )}
                </div>
                <div className="mb-3">
                  <label className="form-label font-weight">
                    <i className="bi bi-asterisk required-asterisk"></i> 住所（基本）
                  </label>
                  <input
                    {...register('addressMain')}
                    className={`form-control ${errors.addressMain ? 'is-invalid' : ''}`}
                    placeholder="住所を入力してください"
                  />
                  {errors.addressMain && (
                    <div className="invalid-feedback d-block">{errors.addressMain.message}</div>
                  )}
                </div>
                <div className="mb-3">
                  <label className="form-label font-weight">
                    <i className="bi bi-asterisk required-asterisk"></i> 詳細住所 1
                  </label>
                  <input
                    {...register('addressDetail1')}
                    className={`form-control ${errors.addressDetail1 ? 'is-invalid' : ''}`}
                    placeholder="詳細住所を入力してください"
                  />
                  {errors.addressDetail1 && (
                    <div className="invalid-feedback d-block">{errors.addressDetail1.message}</div>
                  )}
                </div>
                <div className="mb-3">
                  <label className="form-label font-weight">詳細住所 2</label>
                  <input
                    {...register('addressDetail2')}
                    className="form-control"
                    placeholder="詳細住所を入力してください"
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label font-weight">詳細住所 3</label>
                  <input
                    {...register('addressDetail3')}
                    className="form-control"
                    placeholder="詳細住所を入力してください"
                  />
                </div>
              </div>

              {watchNationality === 'KR' && (
                <div className="mt-4">
                  <label className="form-label font-weight">
                    <i className="bi bi-asterisk required-asterisk"></i> 個人通関固有番号
                  </label>
                  <input
                    {...register('pccc')}
                    className={`form-control ${errors.pccc ? 'is-invalid' : ''}`}
                    placeholder="Pから始まる13桁"
                  />
                  {errors.pccc && (
                    <div className="invalid-feedback d-block">{errors.pccc.message}</div>
                  )}
                </div>
              )}
            </fieldset>

            <div className="form-check mb-4 mt-5 terms-agree-check">
              <input
                type="checkbox"
                {...register('termsAgreed')}
                className="form-check-input terms-agree-input"
                id="termsAgreed"
              />
              <label className="form-check-label" htmlFor="termsAgreed">
                <small>
                  <a
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      handleOpenTermsPage('terms-of-service');
                    }}
                    className="agreement-link"
                  >
                    利用規約
                  </a>{' '}
                  および{' '}
                  <a
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      handleOpenTermsPage('privacy-policy');
                    }}
                    className="agreement-link"
                  >
                    プライバシーポリシー
                  </a>
                  に同意します。
                </small>
              </label>
              {errors.termsAgreed && (
                <div className="invalid-feedback d-block">{errors.termsAgreed.message}</div>
              )}
            </div>

            <button type="submit" className="btn btn-primary w-100 btn-lg">
              登録する
            </button>
            <button
              type="button"
              className="btn btn-secondary w-100 btn-lg mt-3"
              onClick={() => navigate('/login')}
            >
              戻る
            </button>
            {signupError && (
              <div
                className="text-danger text-center mt-3"
                style={{ fontSize: '14px', fontWeight: '500' }}
              >
                {signupError}
              </div>
            )}
          </form>

          <VerificationModal
            show={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            timeLeft={timer}
            verificationCode={verificationCode}
            onCodeChange={(e) => setVerificationCode(e.target.value)}
            onVerify={handleVerify}
            onResend={() => {}}
            isEmailVerified={isVerified}
            error={verificationError}
            isLoading={verificationLoading}
          />
          <CM_99_1002 isOpen={verificationLoading} />
        </div>
      )}
    </div>
  );
}
