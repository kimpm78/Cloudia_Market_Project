import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="container py-5">
      <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">404 - 페이지를 찾을 수 없습니다</h1>
      <hr />
      <div className="p-4 rounded bg-light fs-6 lh-lg">
        <p className="mb-0">요청하신 페이지가 존재하지 않습니다.</p>
      </div>
      <div className="text-center mt-5">
        <Link to="/" className="btn btn-primary px-5 py-2">
          메인 화면으로 이동
        </Link>
      </div>
    </div>
  );
}
