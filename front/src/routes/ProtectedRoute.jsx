import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useConfig } from '../contexts/ConfigContext';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { user, isLoading: authLoading } = useAuth();
  const { isLoading: configLoading } = useConfig();
  const location = useLocation();

  if (authLoading || configLoading) {
    return <div>権限を確認中...</div>;
  }

  const isAuthorized = user && user.roleId <= requiredRole;

  if (!user) {
    // 未ログインの場合はログインページへ
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!isAuthorized) {
    console.warn(`ユーザー権限（${user.roleId}）が必要権限（${requiredRole}）より低いです。`);
    return <Navigate to="/403" replace />;
  }

  return children;
};

export default ProtectedRoute;
