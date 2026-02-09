import { useState, useEffect, useLayoutEffect, useCallback, useRef } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useCart } from '../contexts/CartContext';
import axiosInstance from '../services/axiosInstance';
import logo from '../images/logo.png';
import '../styles/CM_02_1000_header.css';

const CART_UPDATED_EVENT = 'cart:updated';
const CART_TTL_MINUTES = 30;

const CM_02_1000_header = () => {
  const [menuOpen, setMenuOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 992);
  const [headerMenu, setHeaderMenu] = useState([]);
  const [iconMenus, setIconMenus] = useState([]);
  const [loading, setLoading] = useState(true);
  const { cartCount, cartCountLoaded, cartCreatedAt, updateCartState, clearCartState, syncCartCount } =
    useCart();
  const [cartNotice, setCartNotice] = useState('');
  const [toastMsg, setToastMsg] = useState('');
  const [toastVisible, setToastVisible] = useState(false);
  const [toastHiding, setToastHiding] = useState(false);
  const [toastKey, setToastKey] = useState(0);
  const timerRef = useRef(null);
  const cartExpiredRef = useRef(false);
  const lastRemainingRef = useRef(null);
  const updateCartNotice = (time) => {
    setCartNotice(`장바구니 안에 상품이 들어있습니다. 카트의 유지 시간은 ${time}분 남았습니다`);
  };

  const resolveRemainingMinutes = () => {
    if (!cartCreatedAt) {
      return CART_TTL_MINUTES;
    }
    const elapsedMs = Date.now() - cartCreatedAt;
    const remainingMs = CART_TTL_MINUTES * 60 * 1000 - elapsedMs;
    const remaining = Math.ceil(remainingMs / 60000);
    return Math.max(0, remaining);
  };

  const stopCartTimer = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
    setCartNotice('');
    lastRemainingRef.current = null;
  };

  const expireCart = async () => {
    if (!isLoggedIn || cartExpiredRef.current) return;
    cartExpiredRef.current = true;
    try {
      await axiosInstance.delete('/guest/cart');
      updateCartState({ count: 0 });
      const detail = { count: 0, source: 'header' };
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent(CART_UPDATED_EVENT, { detail }));
      }
    } catch (err) {
      console.error('장바구니 만료 처리 실패:', err?.response?.data || err.message);
    }
  };

  const startCartTimer = () => {
    if (timerRef.current) return;

    const remaining = resolveRemainingMinutes();
    if (remaining <= 0) {
      setCartNotice('');
      expireCart();
      return;
    }
    cartExpiredRef.current = false;
    lastRemainingRef.current = remaining;
    updateCartNotice(remaining);

    timerRef.current = setInterval(() => {
      const nextRemaining = resolveRemainingMinutes();
      if (nextRemaining <= 0) {
        clearInterval(timerRef.current);
        timerRef.current = null;
        setCartNotice('');
        expireCart();
        return;
      }
      if (nextRemaining !== lastRemainingRef.current) {
        lastRemainingRef.current = nextRemaining;
        updateCartNotice(nextRemaining);
      }
    }, 1000);
  };

  const showToast = useCallback((msg, duration = 3000) => {
    setToastMsg(msg);
    setToastHiding(false);
    setToastVisible(true);
    setToastKey((k) => k + 1);
    window.clearTimeout(showToast._hideTid);
    window.clearTimeout(showToast._closeTid);
    showToast._hideTid = window.setTimeout(() => {
      setToastHiding(true);
      showToast._closeTid = window.setTimeout(() => setToastVisible(false), 350);
    }, duration);
  }, []);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      window.CM_showToast = showToast;
      return () => {
        if (window.CM_showToast === showToast) {
          delete window.CM_showToast;
        }
      };
    }
  }, [showToast]);

  const { isLoggedIn, user, logout } = useAuth();
  const navigate = useNavigate();

  const iconMap = {
    로그인: 'bi-box-arrow-in-right',
    마이페이지: 'bi-person-circle',
    장바구니: 'bi-cart4',
    '관리 페이지': 'bi-gear',
    로그아웃: 'bi-box-arrow-left',
  };

  useLayoutEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 992);
    };
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  useEffect(() => {
    const navbar = document.getElementById('mainNavbar');
    if (!navbar) return;

    if (isMobile) {
      if (menuOpen) {
        navbar.classList.add('slide-active');
        navbar.classList.remove('slide-exit');
      } else {
        navbar.classList.remove('slide-active');
        navbar.classList.add('slide-exit');
      }
    } else {
      navbar.classList.remove('slide-active');
      navbar.classList.remove('slide-exit');
    }
  }, [menuOpen, isMobile]);

  useEffect(() => {
    axiosInstance
      .get(`/guest/menus`)
      .then((res) => {
        const fullMenu = Array.isArray(res.data) ? res.data : res.data?.menus || [];

        const headerOnly = fullMenu
          .filter((item) => typeof item.url === 'string')
          .map((item) => ({
            ...item,
            sortOrder: parseInt(item.sortOrder ?? '0', 10),
          }))
          .sort((a, b) => a.sortOrder - b.sortOrder);
        setHeaderMenu(headerOnly);
        setLoading(false);
      })
      .catch((err) => {
        console.error('헤더 메뉴 로드 실패:', err?.response?.data || err.message);
      });
  }, []);
  // 아이콘
  useEffect(() => {
    const roleIdParam = isLoggedIn && typeof user?.roleId === 'number' ? user.roleId : undefined;

    axiosInstance
      .get(`/guest/menus/icons`, { params: { roleId: roleIdParam } })
      .then((res) => {
        let menus = Array.isArray(res.data) ? res.data : res.data?.menus || [];

        if (isLoggedIn) {
          menus = menus.map((item) =>
            item.menuName === '로그인' ? { ...item, menuName: '로그아웃', url: '/' } : item
          );

          const isAdminOrManager = typeof user?.roleId === 'number' && user.roleId <= 2;
          const hasAdminMenu = menus.some(
            (item) => item.menuName === '관리자 페이지' || item.url === '/admin'
          );

          if (isAdminOrManager && !hasAdminMenu) {
            menus.push({
              url: '/admin',
              menuName: '관리자 페이지',
              icon: 'bi-gear',
            });
          }

          if (!isAdminOrManager) {
            menus = menus.filter(
              (item) => item.menuName !== '관리자 페이지' && item.url !== '/admin'
            );
          }
        }

        setIconMenus(menus);
      })
      .catch((err) => {
        console.error('아이콘 메뉴 로드 실패:', err?.response?.data || err.message);
      });
  }, [isLoggedIn, user]);


  useEffect(() => {
    if (!isLoggedIn) {
      setCartNotice('');
      clearCartState();
      stopCartTimer();
      return;
    }
    if (user?.userId) {
      syncCartCount();
    }
  }, [isLoggedIn, user?.userId, syncCartCount, clearCartState]);

  useEffect(() => {
    if (!cartCountLoaded) {
      return;
    }
    if (cartCount > 0) {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      startCartTimer();
    } else {
      cartExpiredRef.current = false;
      stopCartTimer();
    }
  }, [cartCount, cartCountLoaded, cartCreatedAt]);

  const renderIcon = (iconClass, menuName) => (
    <div style={{ position: 'relative' }}>
      <i className={`bi ${iconClass}`} style={{ fontSize: '25px' }} />
      {menuName === '장바구니' && cartCount > 0 && <span className="badge-cart">{cartCount}</span>}
    </div>
  );

  const handleLogout = async () => {
    try {
      await axiosInstance.delete('/guest/cart');
    } catch (err) {
      console.error('로그아웃 시 장바구니 정리 실패:', err?.response?.data || err.message);
    }

    setCartNotice('');
    updateCartState({ count: 0 });
    stopCartTimer();

    if (typeof window !== 'undefined') {
      window.dispatchEvent(
        new CustomEvent(CART_UPDATED_EVENT, { detail: { count: 0, source: 'logout' } })
      );
    }

    logout();
    navigate('/');
  };

  const closeMobileMenu = () => {
    if (isMobile) {
      setMenuOpen(false);
    }
  };

  const renderIconMenuItem = ({ url, menuName, icon }) => {
    const iconClass = icon || iconMap[menuName] || 'bi-question-circle';

    if (menuName === '로그아웃') {
      return (
        <button
          key={menuName}
          onClick={() => {
            closeMobileMenu();
            handleLogout();
          }}
          className="nav-link p-0 d-flex flex-column align-items-center text-center no-hover-blue bg-transparent border-0"
        >
          {renderIcon(iconClass, menuName)}
          {menuName}
        </button>
      );
    }

    return (
      <NavLink
        key={url}
        to={url}
        className={({ isActive }) =>
          'nav-link p-0 d-flex flex-column align-items-center text-center' +
          (isActive ? ' active' : '')
        }
        onClick={() => {
          closeMobileMenu();
          if (['로그인', '마이페이지', '장바구니', '관리자 페이지'].includes(menuName)) {
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }
        }}
      >
        {renderIcon(iconClass, menuName)}
        {menuName}
      </NavLink>
    );
  };

  return (
    <>
      <nav
        className="navbar navbar-expand-lg bg-white border-bottom fixed-top"
        style={{ height: '80px', borderBottom: '1px solid #dee2e6' }}
      >
        <div className="container-fluid px-4 d-flex justify-content-between align-items-center">
          <NavLink
            to="/"
            className={({ isActive }) => 'navbar-brand' + (isActive ? ' active' : '')}
            onClick={closeMobileMenu}
          >
            <img src={logo} alt="logo" height="50" />
          </NavLink>

          <button
            className="navbar-toggler"
            type="button"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <div
            id="mainNavbar"
            className={`navbar-collapse ${menuOpen ? 'slide-active' : 'slide-exit'}`}
          >
            <div className="d-flex w-100 justify-content-between align-items-center">
              <ul
                className="navbar-nav me-auto mb-2 mb-lg-0 gap-responsive"
                style={{ gap: '20px', fontSize: '16px', display: 'flex' }}
              >
                {headerMenu.map((item) => (
                  <li key={item.menuId} className="nav-item">
                    <NavLink
                      to={item.url}
                      className="nav-link"
                      onClick={() => {
                        if (isMobile) setMenuOpen(false);
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                      }}
                    >
                      {item.menuName}
                    </NavLink>
                  </li>
                ))}
                {(() => {
                  return headerMenu.length === 0 && !loading ? (
                    <li className="nav-item text-muted">메뉴 항목이 없습니다.</li>
                  ) : null;
                })()}
              </ul>
              <div className="d-none d-lg-flex align-items-ceter gap-4">
                {iconMenus.map(renderIconMenuItem)}
              </div>
            </div>
          </div>
        </div>
      </nav>
      {cartNotice && (
        <div className="cart-notice-banner text-center">
          <span role="link" className="cart-notice-text" onClick={() => navigate('/cart')}>
            {cartNotice}
          </span>
        </div>
      )}
      {/* 모바일시용 고정헤더 */}
      <div className="d-block d-lg-none">
        <ul className="navbar-nav d-flex flex-row gap-3 justify-content-center mt-4 mobile-footer-nav">
          {iconMenus.map(({ url, menuName, icon }) => (
            <li key={url} className="nav-item d-flex flex-column align-items-center text-center">
              {renderIconMenuItem({ url, menuName, icon })}
            </li>
          ))}
        </ul>
      </div>
      {/* 토스트 */}
      {toastVisible && (
        <div
          key={toastKey}
          className="position-fixed start-50 translate-middle-x CM-toast"
          style={{ zIndex: 2000 }}
        >
          <div
            className={`toast show align-items-center text-white bg-dark border-0 toast-fade ${toastHiding ? 'toast-exit' : 'toast-enter'}`}
            role="alert"
          >
            <div className="d-flex">
              <div className="toast-body">{toastMsg}</div>
              <button
                type="button"
                className="btn-close btn-close-white me-2 m-auto"
                aria-label="Close"
                onClick={() => {
                  setToastHiding(true);
                  window.setTimeout(() => setToastVisible(false), 350);
                }}
              />
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default CM_02_1000_header;
