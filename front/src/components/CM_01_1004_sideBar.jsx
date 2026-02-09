import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import '../styles/CM_01_1004.css';

export default function CM_01_1004_sideBar() {
  const [menuData, setMenuData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchMyPageMenu = async () => {
      try {
        setLoading(true);
        const res = await axiosInstance.get('/user/mypage/menus');
        setMenuData(res.data || []);
        setError(null);
      } catch (err) {
        console.error('마이페이지 메뉴 로딩 실패:', err);
        setError('메뉴를 불러오는 데 실패했습니다.');
        setMenuData([]);
      } finally {
        setLoading(false);
      }
    };
    fetchMyPageMenu();
  }, []);

  if (loading) {
    return (
      <div className="mypage-sidebar">
        <div className="sidebar-loading">메뉴 로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mypage-sidebar">
        <div className="sidebar-error">메뉴 로드 실패</div>
      </div>
    );
  }

  return (
    <div className="sidebar">
      {menuData.map((section) => (
        <div key={section.menuId} className="sidebar-section">
          <div className="sidebar-title">{section.menuName}</div>
          <ul className="sidebar-list">
            {section.children &&
              section.children.map((item) => (
                <li key={item.menuId} className="sidebar-item">
                  <NavLink
                    to={item.url}
                    className={({ isActive }) => `link-body-emphasis ${isActive ? 'fw-bold' : ''}`}
                    style={{ textDecoration: 'none' }}
                  >
                    {item.menuName}
                  </NavLink>
                </li>
              ))}
          </ul>
        </div>
      ))}
    </div>
  );
}
