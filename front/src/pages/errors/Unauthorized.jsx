import { Link } from 'react-router-dom';

export default function Unauthorized() {
  return (
    <div className="container py-5">
      <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">401 - 認証が必要です</h1>
      <hr />
      <div className="p-4 rounded bg-light fs-6 lh-lg">
        <p className="mb-0">ログインが必要です。</p>
      </div>
      <div className="text-center mt-5">
        <Link to="/" className="btn btn-primary px-5 py-2">
          メイン画面へ移動
        </Link>
      </div>
    </div>
  );
}
