import { useNavigate } from 'react-router-dom';

export default function ResetPasswordResult() {
  const navigate = useNavigate();

  return (
    <>
      <h4 className="fw-bold mb-3">パスワード再設定完了</h4>
      <p className="text-muted mb-4">パスワードが正常に変更されました。</p>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={() => navigate('/login')}>
          ログイン画面へ移動
        </button>
      </div>
    </>
  );
}
