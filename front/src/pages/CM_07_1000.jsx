import { useState } from 'react';

export default function CM_07_1000() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    inquiryType: '',
    title: '',
    content: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const { name, email, inquiryType, title, content } = formData;
    if (!name || !email || !inquiryType || !title || !content) {
      alert('すべての項目を入力してください。');
      return;
    }
    console.log('送信されたお問い合わせ:', formData);
    alert('お問い合わせを送信しました。');
    setFormData({
      name: '',
      email: '',
      inquiryType: '',
      title: '',
      content: '',
    });
  };

  return (
    <div className="container mt-4">
      <div className="contact-page">
        <h1 className="ms-0">お問い合わせ</h1>
        <p>以下から該当する内容を選択してください。</p>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <div className="form-text text-danger">* 印の項目は必須入力です。</div>
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="fw-semibold email" className="form-label">
              <span className="text-danger">*</span> メールアドレス
            </label>
            <input
              type="email"
              id="email"
              name="email"
              className="form-control"
              placeholder="メールアドレスを入力してください"
              value={formData.email}
              onChange={handleChange}
            />
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="inquiryType" className="form-label">
              <span className="text-danger">*</span> お問い合わせ種別
            </label>
            <select
              id="inquiryType"
              name="inquiryType"
              className="form-control"
              value={formData.inquiryType}
              onChange={handleChange}
            >
              <option value="">お問い合わせ種別を選択してください</option>
              <option value="1">商品・特典内容</option>
              <option value="2">商品到着後の不良（返品・交換）</option>
              <option value="3">その他</option>
            </select>
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="title" className="form-label">
              <span className="text-danger">*</span> 件名
            </label>
            <input
              type="text"
              id="title"
              name="title"
              className="form-control"
              placeholder="件名を入力してください"
              value={formData.title}
              onChange={handleChange}
            />
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="content" className="form-label">
              <span className="text-danger">*</span> お問い合わせ内容
            </label>
            <textarea
              id="content"
              name="content"
              className="form-control"
              placeholder="お問い合わせ内容を入力してください"
              value={formData.content}
              onChange={handleChange}
              rows="5"
            />
            <div className="form-text">
              ※ 商品の傷や汚れは、開封後に拭き取ることで解消する場合があります。
              　商品不良に関して画像を添付される場合は、開封済みの状態の写真を添付いただけますよう
              ご協力をお願いいたします。
            </div>
            <div className="form-text">
              ※ お問い合わせ内容はできるだけ詳しくご記入ください。可能な限り迅速に対応いたします。
            </div>
          </div>
          <div className="my-4">
            <button type="submit" className="btn btn-primary fw-semibold px-4">
              送信する
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
