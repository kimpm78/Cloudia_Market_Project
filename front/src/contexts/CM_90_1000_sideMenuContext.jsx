import { createContext, useContext, useEffect, useState } from 'react';
import axiosInstance from '../services/axiosInstance';

const MenuContext = createContext();

export const useMenu = () => {
  const context = useContext(MenuContext);
  if (!context) {
    throw new Error('useMenu must be used within a MenuProvider');
  }
  return context;
};

export const MenuProvider = ({ children }) => {
  const [menuData, setMenuData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchMenuData = async () => {
    try {
      setLoading(true);
      const res = await axiosInstance.get(
        `${import.meta.env.VITE_API_BASE_URL}/admin/menu/all`,
        []
      );
      if (res.status === 200) {
        setMenuData(res.data);
        setError(null);
      }
    } catch (err) {
      console.error('메뉴 데이터 불러오기 실패:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMenuData();
  }, []);

  const value = {
    menuData,
    loading,
    error,
    refetch: fetchMenuData,
  };

  return <MenuContext.Provider value={value}>{children}</MenuContext.Provider>;
};
