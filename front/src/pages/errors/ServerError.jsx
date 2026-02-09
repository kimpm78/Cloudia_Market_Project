import { Link } from 'react-router-dom';

export default function ServerError() {
  return (
    <div className="container py-5">
      <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">500 - 서버 오류</h1>
      <hr />
      <div className="p-4 rounded bg-light fs-6 lh-lg">
        <p className="mb-0">서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.</p>
      </div>
      <div className="text-center mt-5">
        <Link to="/" className="btn btn-primary px-5 py-2">
          메인 화면으로 이동
        </Link>
      </div>
    </div>
  );
}
