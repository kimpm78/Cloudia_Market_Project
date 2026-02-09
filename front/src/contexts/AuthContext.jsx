import { createContext, useContext, useReducer, useEffect, useState } from 'react';
import { authClient, setAccessToken, axiosPublic } from '../services/axiosInstance';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';

const AuthContext = createContext(null);

const authReducer = (state, action) => {
  switch (action.type) {
    case 'LOGIN':
      return { ...state, isLoggedIn: true, user: action.payload };
    case 'LOGOUT':
      return { isLoggedIn: false, user: null };
    default:
      return state;
  }
};

export const AuthProvider = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, {
    isLoggedIn: false,
    user: null,
  });
  const [loading, setLoading] = useState(true);

  // 앱 로드(새로고침) 시 토큰 복구
  useEffect(() => {
    const initAuth = async () => {
      const wasLoggedIn = localStorage.getItem('isConnected');

      if (!wasLoggedIn) {
        setLoading(false);
        return;
      }

      try {
        // 쿠키에 있는 리프레시 토큰으로 AccessToken 요청
        const response = await authClient.post('/auth/refresh');

        const result = response.data.resultList;

        if (result) {
          setAccessToken(result.accessToken);
          dispatch({ type: 'LOGIN', payload: result.user });
        } else {
          throw new Error('토큰이 없습니다.');
        }
      } catch (error) {
        console.warn('세션 만료됨:', error);
        localStorage.removeItem('isConnected');
        setAccessToken(null);
        dispatch({ type: 'LOGOUT' });
      } finally {
        setLoading(false);
      }
    };
    initAuth();
  }, []);

  // 로그인
  const login = async (loginId, password) => {
    try {
      const response = await axiosPublic.post(
        '/guest/login',
        { loginId, password },
        { withCredentials: true }
      );

      const result = response.data.resultList;

      if (result) {
        setAccessToken(result.accessToken); // 메모리에 저장
        localStorage.setItem('isConnected', '1'); // 로그인 흔적 남기기
        dispatch({ type: 'LOGIN', payload: result.user }); // 상태 업데이트
        return true;
      }

      throw new Error('토큰이 없습니다.');
    } catch (e) {
      console.error('로그인 에러:', e);

      let errorMessage = '아이디 또는 비밀번호를 확인해주세요.';
      if (e.response && e.response.data) {
        const backendError = e.response.data.message || e.response.data.error;
        if (backendError) {
          // 에러 메시지 파싱 로직은 그대로 유지
          if (backendError.startsWith('DORMANT_ACCOUNT:'))
            errorMessage = backendError.substring('DORMANT_ACCOUNT:'.length);
          else if (backendError.startsWith('INACTIVE_ACCOUNT:'))
            errorMessage = backendError.substring('INACTIVE_ACCOUNT:'.length);
          else if (backendError.startsWith('UNAUTHORIZED_STATUS:'))
            errorMessage = backendError.substring('UNAUTHORIZED_STATUS:'.length);
          else errorMessage = backendError;
        }
      }
      throw new Error(errorMessage);
    }
  };

  const logout = async () => {
    try {
      await authClient.post('/auth/logout');
    } catch (e) {}
    localStorage.removeItem('isConnected');
    setAccessToken(null);
    dispatch({ type: 'LOGOUT' });
    window.location.href = '/';
  };

  if (loading) return <CM_99_1002 isOpen={true} onClose={() => {}} />;

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>{children}</AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('AuthContext Error');
  return context;
};
