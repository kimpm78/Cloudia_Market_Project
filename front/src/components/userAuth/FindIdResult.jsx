import React from 'react';
import { useNavigate } from 'react-router-dom';

export default function FindIdResult({ id }) {
  const navigate = useNavigate();

  const maskedId = id ? id.slice(0, -3) + '***' : '';

  return (
    <>
      <h4 className="fw-bold mb-3">아이디 찾기</h4>
      <p className="text-muted mb-4">회원님의 아이디를 확인했습니다.</p>
      <div className="alert alert-light text-center fs-4 fw-bold">{maskedId}</div>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={() => navigate('/login')}>
          로그인 화면으로 이동
        </button>
      </div>
    </>
  );
}
