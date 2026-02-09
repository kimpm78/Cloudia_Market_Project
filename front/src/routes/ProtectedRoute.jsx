import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useConfig } from '../contexts/ConfigContext';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { user, isLoading: authLoading } = useAuth();
  const { isLoading: configLoading } = useConfig();
  const location = useLocation();

  if (authLoading || configLoading) {
    return <div>권한 확인 중...</div>;
  }

  const isAuthorized = user && user.roleId <= requiredRole;

  if (!user) {
    // 비로그인 상태면 로그인 페이지로
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!isAuthorized) {
    console.warn(`유저권한(${user.roleId})이 요구권한(${requiredRole})보다 낮습니다.`);
    return <Navigate to="/403" replace />;
  }

  return children;
};

export default ProtectedRoute;
