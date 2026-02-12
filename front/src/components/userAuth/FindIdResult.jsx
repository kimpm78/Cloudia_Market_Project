import React from 'react';
import { useNavigate } from 'react-router-dom';

export default function FindIdResult({ id }) {
  const navigate = useNavigate();

  const maskedId = id ? id.slice(0, -3) + '***' : '';

  return (
    <>
      <h4 className="fw-bold mb-3">ID検索</h4>
      <p className="text-muted mb-4">お客様のIDを確認しました。</p>
      <div className="alert alert-light text-center fs-4 fw-bold">{maskedId}</div>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={() => navigate('/login')}>
          ログイン画面へ移動
        </button>
      </div>
    </>
  );
}
