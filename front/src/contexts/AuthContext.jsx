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

  // アプリ起動（リロード）時にトークンを復元
  useEffect(() => {
    const initAuth = async () => {
      const wasLoggedIn = localStorage.getItem('isConnected');

      if (!wasLoggedIn) {
        setLoading(false);
        return;
      }

      try {
        // Cookie内のリフレッシュトークンでAccessTokenを再発行
        const response = await authClient.post('/auth/refresh');

        const result = response.data.resultList;

        if (result) {
          setAccessToken(result.accessToken);
          dispatch({ type: 'LOGIN', payload: result.user });
        } else {
          throw new Error('トークンがありません。');
        }
      } catch (error) {
        console.warn('セッションが期限切れ:', error);
        localStorage.removeItem('isConnected');
        setAccessToken(null);
        dispatch({ type: 'LOGOUT' });
      } finally {
        setLoading(false);
      }
    };
    initAuth();
  }, []);

  // ログイン
  const login = async (loginId, password) => {
    try {
      const response = await axiosPublic.post(
        '/guest/login',
        { loginId, password },
        { withCredentials: true }
      );

      const result = response.data.resultList;

      if (result) {
        setAccessToken(result.accessToken); // メモリに保存
        localStorage.setItem('isConnected', '1'); // ログイン状態を保持
        dispatch({ type: 'LOGIN', payload: result.user }); // 状態を更新
        return true;
      }

      throw new Error('トークンがありません。');
    } catch (e) {
      console.error('ログインエラー:', e);

      let errorMessage = 'ログインIDまたはパスワードをご確認ください。';
      if (e.response && e.response.data) {
        const backendError = e.response.data.message || e.response.data.error;
        if (backendError) {
          // エラーメッセージのパースロジックは従来どおり
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
  if (!context) throw new Error('AuthContext エラー');
  return context;
};
