import '../../styles/authLayout.css';

export default function UserAuthLayout({ children }) {
  return (
    <div className="d-flex flex-grow-1 justify-content-center align-items-center vh-100 auth-layout">
      <div className="auth-container">
        <h2 className="fw-bold text-primary mb-4">Cloudia Market</h2>
        {children}
      </div>
    </div>
  );
}
