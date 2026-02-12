import { useNavigate } from 'react-router-dom';

export default function Forbidden() {
  const navigate = useNavigate();

  const handleGoBack = () => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/');
    }
  };

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <div className="container py-5">
      <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">403 - アクセスが制限されています</h1>
      <hr />
      <div className="p-4 rounded bg-light fs-6 lh-lg">
        <p className="mb-0">
          リクエストされたページにアクセスする権限がありません。必要に応じて管理者へ権限を申請してください。
        </p>
      </div>
      <div className="text-center mt-5">
        <div className="d-flex flex-column flex-sm-row justify-content-center gap-2 gap-sm-3">
          <button type="button" className="btn btn-primary px-5 py-2" onClick={handleGoBack}>
            戻る
          </button>
          <button type="button" className="btn btn-outline-primary px-5 py-2" onClick={handleGoHome}>
            ホームへ移動
          </button>
        </div>
      </div>
    </div>
  );
}
