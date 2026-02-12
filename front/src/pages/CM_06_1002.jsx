import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

export default function CM_06_1002() {
  const location = useLocation();
  const navigate = useNavigate();
  const sourceState =
    location.state && typeof location.state === 'object' ? location.state : {};

  const [form, setForm] = useState({
    cardNumber: '',
    expiry: '',
    cvc: '',
    holderName: '',
  });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const amountText = useMemo(() => {
    const total = Number(sourceState?.order?.total ?? sourceState?.total ?? 0);
    if (!Number.isFinite(total) || total <= 0) return '-';
    return `${total.toLocaleString()}円`;
  }, [sourceState]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    if (name === 'cardNumber') {
      const digits = value.replace(/\D/g, '').slice(0, 16);
      setForm((prev) => ({ ...prev, cardNumber: digits }));
      return;
    }
    if (name === 'cvc') {
      const digits = value.replace(/\D/g, '').slice(0, 4);
      setForm((prev) => ({ ...prev, cvc: digits }));
      return;
    }
    if (name === 'expiry') {
      const digits = value.replace(/\D/g, '').slice(0, 4);
      const formatted = digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
      setForm((prev) => ({ ...prev, expiry: formatted }));
      return;
    }
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    const cardNumber = form.cardNumber.trim();
    const expiry = form.expiry.trim();
    const cvc = form.cvc.trim();
    const holderName = form.holderName.trim();

    if (!/^\d{13,16}$/.test(cardNumber)) return 'カード番号を正しく入力してください。';
    if (!/^\d{2}\/\d{2}$/.test(expiry)) return '有効期限は MM/YY 形式で入力してください。';
    if (!/^\d{3,4}$/.test(cvc)) return 'セキュリティコードを正しく入力してください。';
    if (!holderName) return 'カード名義人を入力してください。';
    return '';
  };

  const handleSubmit = () => {
    if (isSubmitting) return;
    const message = validate();
    if (message) {
      setError(message);
      return;
    }
    setError('');
    setIsSubmitting(true);

    navigate('/order-payment', {
      replace: true,
      state: {
        ...sourceState,
        autoSubmitCard: true,
        localCardInfo: {
          cardNumber: form.cardNumber,
          expiry: form.expiry,
          cvc: form.cvc,
          holderName: form.holderName.trim(),
        },
      },
    });
  };

  return (
    <div className="container py-4" style={{ maxWidth: '680px' }}>
      <div className="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
        <h4 className="m-0 fw-bold">カード情報入力</h4>
        <span className="badge bg-light text-dark border">お支払い金額: {amountText}</span>
      </div>

      <div className="card shadow-sm">
        <div className="card-body p-4">
          <div className="mb-3">
            <label className="form-label fw-semibold">カード番号</label>
            <input
              type="text"
              name="cardNumber"
              className="form-control"
              placeholder="4242424242424242"
              value={form.cardNumber}
              onChange={handleChange}
            />
          </div>
          <div className="row g-3">
            <div className="col-6">
              <label className="form-label fw-semibold">有効期限</label>
              <input
                type="text"
                name="expiry"
                className="form-control"
                placeholder="MM/YY"
                value={form.expiry}
                onChange={handleChange}
              />
            </div>
            <div className="col-6">
              <label className="form-label fw-semibold">CVC</label>
              <input
                type="text"
                name="cvc"
                className="form-control"
                placeholder="123"
                value={form.cvc}
                onChange={handleChange}
              />
            </div>
          </div>
          <div className="mt-3">
            <label className="form-label fw-semibold">カード名義人</label>
            <input
              type="text"
              name="holderName"
              className="form-control"
              placeholder="TARO YAMADA"
              value={form.holderName}
              onChange={handleChange}
            />
          </div>
          {error && <div className="alert alert-danger mt-3 mb-0">{error}</div>}
        </div>
      </div>

      <div className="d-flex justify-content-end gap-2 mt-4">
        <button
          className="btn btn-outline-secondary"
          disabled={isSubmitting}
          onClick={() => navigate('/order-payment', { replace: true, state: sourceState })}
        >
          戻る
        </button>
        <button className="btn btn-primary" disabled={isSubmitting} onClick={handleSubmit}>
          {isSubmitting ? '確定中...' : '確定'}
        </button>
      </div>
    </div>
  );
}
