import { createContext, useCallback, useContext, useReducer, useEffect, useMemo } from 'react';
import axiosInstance from '../services/axiosInstance';

const initialState = { isLoading: true, roleHierarchy: {}, error: null };
const ConfigContext = createContext(initialState);

const ConfigReducer = (state, action) => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    case 'SET_ROLES':
      return { ...state, roleHierarchy: action.payload, isLoading: false };
    case 'SET_ERROR':
      return { ...state, isLoading: false, error: action.payload };
    default:
      return state;
  }
};

export const ConfigProvider = ({ children }) => {
  const [state, dispatch] = useReducer(ConfigReducer, initialState);

  const fetchRoles = useCallback(async () => {
    const isConnected = localStorage.getItem('isConnected');
    if (!isConnected) {
      dispatch({ type: 'SET_LOADING', payload: false });
      return;
    }

    dispatch({ type: 'SET_LOADING', payload: true });

    try {
      const response = await axiosInstance.get('/roles');
      const roles = response.data.resultList || response.data;

      if (Array.isArray(roles)) {
        const newHierarchy = {};
        roles.forEach((r) => {
          newHierarchy[r.roleType] = r.roleId;
        });
        dispatch({ type: 'SET_ROLES', payload: newHierarchy });
      } else {
        dispatch({ type: 'SET_ERROR', payload: 'Invalid data' });
      }
    } catch (error) {
      // 401 에러가 나면 axiosInstance가 알아서 리프레시 시도함
      console.error('Role Fetch Error:', error);
      dispatch({ type: 'SET_ERROR', payload: error.message });
    }
  }, []);

  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  const contextValue = useMemo(() => ({ ...state, fetchRoles }), [state, fetchRoles]);

  return <ConfigContext.Provider value={contextValue}>{children}</ConfigContext.Provider>;
};

export const useConfig = () => useContext(ConfigContext);
