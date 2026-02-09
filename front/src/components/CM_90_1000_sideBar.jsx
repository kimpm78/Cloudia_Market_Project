import { Link } from 'react-router-dom';
import { useMenu } from '../contexts/CM_90_1000_sideMenuContext';
import '../styles/CM_90_1000_sideBar.css';

export default function CM_90_1000_sideBar() {
  const { menuData, loading, error } = useMenu();

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
      {menuData.map((section) => (
        <div key={section.menuId} className="sidebar-section">
          <div className="sidebar-title">{section.menuName}</div>
          <ul className="sidebar-list">
            {Array.isArray(section.children) &&
              section.children.map((item) => (
                <li key={item.menuId} className="sidebar-item">
                  <Link
                    to={item.url}
                    className={`${section.menuName === 'Account' ? 'link-dark' : 'link-body-emphasis'}`}
                    style={{ textDecoration: 'none' }}
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