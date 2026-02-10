import { Link, useLocation } from 'react-router-dom';
import { useMenu } from '../contexts/CM_90_1000_SideMenuContext';
import '../styles/CM_90_1000_sideBar.css';

export default function CM_90_1000_SideBar() {
  const { menuData, loading, error } = useMenu();
  const location = useLocation();
  const safeMenuData = Array.isArray(menuData) ? menuData : [];
  const normalizePath = (path = '') => {
    const pathnameOnly = path.split('?')[0];
    const trimmed = pathnameOnly.replace(/\/+$/, '');
    return trimmed || '/';
  };
  const currentPath = normalizePath(location.pathname);

  if (loading) {
    return (
      <div className="sidebar">
        <div className="sidebar-loading">メニューを読み込み中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="sidebar">
        <div className="sidebar-error">メニューの読み込みに失敗しました</div>
      </div>
    );
  }

  return (
    <div className="sidebar">
      {safeMenuData.map((section) => (
        <div key={section.menuId} className="sidebar-section">
          <div className="sidebar-title">{section.menuName}</div>
          <ul className="sidebar-list">
            {Array.isArray(section.children) &&
              section.children.map((item) => (
                <li key={item.menuId} className="sidebar-item">
                  <Link
                    to={item.url}
                    className={`text-decoration-none admin-sidebar-link ${
                      currentPath === normalizePath(item.url) ? 'active fw-bold' : ''
                    }`}
                  >
                    {item.menuName}
                  </Link>
                </li>
              ))}
          </ul>
        </div>
      ))}
    </div>
  );
}
