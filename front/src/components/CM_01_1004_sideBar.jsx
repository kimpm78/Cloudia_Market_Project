import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import '../styles/CM_01_1004.css';

export default function CM_01_1004_SideBar() {
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
        console.error('マイページメニューの読み込みに失敗しました:', err);
        setError('メニューの読み込みに失敗しました。');
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
        <div className="sidebar-loading">メニュー読み込み中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mypage-sidebar">
        <div className="sidebar-error">メニューの読み込みに失敗しました</div>
      </div>
    );
  }

  return (
    <div className="sidebar">
      <div className="sidebar-section">
        <NavLink
          to="/mypage"
          end
          className={({ isActive }) =>
            `sidebar-title text-decoration-none mypage-sidebar-link ${isActive ? 'active fw-bold' : ''}`
          }
        >
          マイページ
        </NavLink>
      </div>

      {menuData.map((section) => (
        <div key={section.menuId} className="sidebar-section">
          <div className="sidebar-title">{section.menuName}</div>
          <ul className="sidebar-list">
            {section.children &&
              section.children.map((item) => (
                <li key={item.menuId} className="sidebar-item">
                  <NavLink
                    to={item.url}
                    className={({ isActive }) =>
                      `text-decoration-none mypage-sidebar-link ${isActive ? 'active fw-bold' : ''}`
                    }
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
