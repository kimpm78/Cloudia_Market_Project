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
      <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">403 - 접근이 제한되었습니다</h1>
      <hr />
      <div className="p-4 rounded bg-light fs-6 lh-lg">
        <p className="mb-0">
          요청하신 페이지에 접근할 권한이 없습니다. 필요한 경우 관리자에게 권한을 요청해 주세요.
        </p>
      </div>
      <div className="text-center mt-5">
        <div className="d-flex flex-column flex-sm-row justify-content-center gap-2 gap-sm-3">
          <button type="button" className="btn btn-primary px-5 py-2" onClick={handleGoBack}>
            뒤로 가기
          </button>
          <button type="button" className="btn btn-outline-primary px-5 py-2" onClick={handleGoHome}>
            홈으로 이동
          </button>
        </div>
      </div>
    </div>
  );
}
