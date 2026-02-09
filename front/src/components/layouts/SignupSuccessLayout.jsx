import { useNavigate } from 'react-router-dom';
import '../../styles/CM_01_1001.css';
import { useEffect } from 'react';
export default function SignupSuccessLayout() {
  const navigate = useNavigate();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  const handleGoToLogin = () => {
    navigate('/login');
  };

  return (
    <div className="container py-5 signup-success-page">
      <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">登録完了</h1>
      <hr />
      <div className="p-4 rounded bg-light fs-6 lh-lg">
        <p className="mb-1">会員登録が正常に完了しました。</p>
        <p className="mb-0">
          これからさまざまなフィギュアをご覧いただき、お気に入りの商品をカートに入れてご購入いただけます。
        </p>
      </div>
      <div className="text-center mt-5">
        <button className="btn btn-primary px-5 py-2" onClick={handleGoToLogin}>
          ログイン画面へ移動
        </button>
      </div>
    </div>
  );
}
