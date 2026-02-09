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
      alert('모든 필드를 입력해 주세요.');
      return;
    }
    console.log('제출된 문의:', formData);
    alert('문의가 제출되었습니다.');
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
        <h1 className="ms-0">문의하기</h1>
        <p>아래에서 해당되는 문제를 선택해 주세요</p>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <div className="form-text text-danger">* 표시된 항목은 필수 입력 항목입니다.</div>
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="fw-semibold email" className="form-label">
              <span className="text-danger">*</span> 이메일 주소
            </label>
            <input
              type="email"
              id="email"
              name="email"
              className="form-control"
              placeholder="이메일을 입력해 주세요"
              value={formData.email}
              onChange={handleChange}
            />
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="inquiryType" className="form-label">
              <span className="text-danger">*</span> 문의 유형
            </label>
            <select
              id="inquiryType"
              name="inquiryType"
              className="form-control"
              value={formData.inquiryType}
              onChange={handleChange}
            >
              <option value="">문의 유형을 선택해 주세요</option>
              <option value="1">상품・특전 내용</option>
              <option value="2">상품 후의 상품불량(반품・교환)</option>
              <option value="3">기타</option>
            </select>
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="title" className="form-label">
              <span className="text-danger">*</span> 제목
            </label>
            <input
              type="text"
              id="title"
              name="title"
              className="form-control"
              placeholder="제목을 입력해 주세요"
              value={formData.title}
              onChange={handleChange}
            />
          </div>
          <div className="fw-semibold mb-3">
            <label htmlFor="content" className="form-label">
              <span className="text-danger">*</span> 문의 내역
            </label>
            <textarea
              id="content"
              name="content"
              className="form-control"
              placeholder="문의 내용을 입력해 주세요"
              value={formData.content}
              onChange={handleChange}
              rows="5"
            />
            <div className="form-text">
              ※ 상품의 흠집, 더러움은 상품을 개봉한 후 닦아냄으로써, 해소하는 경우가 있습니다.
              　상품 불량에 관하여 이미지를 첨부하실 때에는, 상품을 개봉한 상태의 사진을 첨부하실 수
              있도록 협조 부탁드립니다.
            </div>
            <div className="form-text">
              ※ 문의 내용을 상세히 입력해 주세요. 최대한 신속히 대응해 드리겠습니다.
            </div>
          </div>
          <div className="my-4">
            <button type="submit" className="btn btn-primary fw-semibold px-4">
              제출하기
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
