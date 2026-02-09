import { useNavigate } from 'react-router-dom';

export default function ResetPasswordResult() {
  const navigate = useNavigate();

  return (
    <>
      <h4 className="fw-bold mb-3">비밀번호 재설정 완료</h4>
      <p className="text-muted mb-4">비밀번호가 성공적으로 변경되었습니다.</p>
      <div className="d-grid mt-4">
        <button className="btn btn-primary" onClick={() => navigate('/login')}>
          로그인 화면으로 이동
        </button>
      </div>
    </>
  );
}
