import { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getRequest } from '../services/axiosInstance';
import '../styles/CM_01_9999.css';
import noImage from '../images/common/CM-NoImage.png';
import { ORDER_STATUS, ORDER_STATUS_CODE } from '../constants/constants';

export default function CM_01_9999() {
  const [inquiries, setInquiries] = useState([]);
  const [loadingInquiries, setLoadingInquiries] = useState(true);
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(true);

  const navigate = useNavigate();

  const scrollRef = useRef(null);
  const isDown = useRef(false);
  const startX = useRef(0);
  const velX = useRef(0);
  const momentumID = useRef(null);
  const isDragging = useRef(false);

  const buildImageUrl = (rawUrl) => {
    const base = import.meta.env.VITE_API_BASE_IMAGE_URL || '';
    if (!rawUrl) return noImage;
    if (rawUrl.startsWith('http')) return rawUrl;
    if (rawUrl.startsWith('/images/') && base.endsWith('/images')) {
      return base.replace(/\/images$/, '') + rawUrl;
    }
    if (rawUrl.startsWith('/')) return base + rawUrl;
    return `${base}/${rawUrl}`;
  };

  const formatDate = (date) => {
    if (!date) return '';
    const d = new Date(date);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');
    return `${yyyy}.${mm}.${dd} ${hh}:${min}`;
  };

  const getStatusInfo = (statusCode) => {
    const code = statusCode;

    const statusObj = ORDER_STATUS.find((s) => s.value === code);
    const label = statusObj ? statusObj.label : '状態未定';

    let badgeClass = 'bg-secondary';

    if (code === ORDER_STATUS_CODE.PURCHASE_CONFIRMED || code === ORDER_STATUS_CODE.DELIVERED) {
      badgeClass = 'bg-success';
    } else if (
      code === ORDER_STATUS_CODE.CANCELLED_USER ||
      code === ORDER_STATUS_CODE.CANCELLED_ADMIN ||
      code === ORDER_STATUS_CODE.PAYMENT_FAILED
    ) {
      badgeClass = 'bg-danger';
    } else if (code === ORDER_STATUS_CODE.SHIPPING) {
      badgeClass = 'bg-primary';
    } else {
      badgeClass = 'bg-dark';
    }

    return { label, badgeClass };
  };

  const beginMomentum = () => {
    cancelMomentum();
    const loop = () => {
      if (!scrollRef.current) return;
      scrollRef.current.scrollLeft -= velX.current;
      velX.current *= 0.95;
      if (Math.abs(velX.current) > 0.1) {
        momentumID.current = requestAnimationFrame(loop);
      } else {
        velX.current = 0;
      }
    };
    momentumID.current = requestAnimationFrame(loop);
  };

  const cancelMomentum = () => {
    cancelAnimationFrame(momentumID.current);
  };

  const onMouseDown = (e) => {
    isDown.current = true;
    isDragging.current = false;
    startX.current = e.pageX;
    cancelMomentum();
    velX.current = 0;
  };

  const onMouseLeave = () => {
    if (isDown.current) {
      isDown.current = false;
      beginMomentum();
    }
  };

  const onMouseUp = () => {
    isDown.current = false;
    beginMomentum();
    setTimeout(() => {
      isDragging.current = false;
    }, 0);
  };

  const onMouseMove = (e) => {
    if (!isDown.current) return;
    e.preventDefault();
    const x = e.pageX;
    const walk = (x - startX.current) * 1.0;
    if (scrollRef.current) {
      scrollRef.current.scrollLeft -= walk;
    }
    velX.current = walk;
    startX.current = x;
    if (Math.abs(walk) > 0) {
      isDragging.current = true;
    }
  };

  const handleLinkClick = (e) => {
    if (isDragging.current) {
      e.preventDefault();
      e.stopPropagation();
    }
  };

  const fetchInquiries = useCallback(async () => {
    setLoadingInquiries(true);
    try {
      const data = await getRequest('/user/mypage/inquiries');
      if (Array.isArray(data)) {
        const sortedData = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setInquiries(sortedData.slice(0, 3));
      } else {
        setInquiries([]);
      }
    } catch (error) {
      setInquiries([]);
    } finally {
      setLoadingInquiries(false);
    }
  }, []);

  const fetchOrders = useCallback(async () => {
    setLoadingOrders(true);
    try {
      const data = await getRequest('/user/mypage/purchases');
      if (Array.isArray(data)) {
        const sortedData = data.sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate));
        setOrders(sortedData);
      } else {
        setOrders([]);
      }
    } catch (error) {
      setOrders([]);
    } finally {
      setLoadingOrders(false);
    }
  }, []);

  useEffect(() => {
    fetchInquiries();
    fetchOrders();
  }, [fetchInquiries, fetchOrders]);

  // メニューおよびリンクデータ
  const menuCards = [
    { to: '/mypage/purchases', icon: 'bi-box-seam', label: '購入履歴' },
    { to: '/mypage/inquiries', icon: 'bi-chat-dots', label: 'お問い合わせ履歴' },
    { to: '/mypage/returns', icon: 'bi-arrow-left-right', label: '交換／返品' },
    { to: '/mypage/address-book', icon: 'bi-geo-alt', label: '住所録管理' },
    { to: '/mypage/profile', icon: 'bi-person', label: 'プロフィール' },
    { to: '/mypage/change-password', icon: 'bi-key', label: 'パスワード変更' },
  ];
  const personalLinks = [
    {
      to: '/mypage/profile',
      title: 'プロフィール',
      desc: '氏名、登録情報、電話番号などの確認・変更',
      color: 'text-primary',
    },
    {
      to: '/mypage/address-book',
      title: '住所録管理',
      desc: '配送先の登録・変更',
      color: 'text-primary',
    },
    {
      to: '/mypage/account',
      title: '返金口座の登録',
      desc: '返金を受け取る口座情報の登録・管理',
      color: 'text-primary',
    },
    {
      to: '/mypage/change-password',
      title: 'パスワード変更',
      desc: 'パスワードの変更',
      color: 'text-primary',
    },
    {
      to: '/mypage/unsubscribe',
      title: '退会',
      desc: 'クラウディアーケットの退会手続き',
      color: 'text-dark',
    },
  ];

  const visibleOrders = orders.filter(
    (item) => item.orderStatusValue !== ORDER_STATUS_CODE.PAYMENT_FAILED
  );

  return (
    <div className="mypage-wrapper mt-4">
      <h2 className="border-bottom fw-bolder pb-3 mb-4">マイページ</h2>
      {/* メニューカード */}
      <div className="row row-cols-3 row-cols-md-3 row-cols-lg-6 g-3 text-center mb-5">
        {menuCards.map((item, idx) => (
          <div className="col" key={idx}>
            <Link to={item.to} className="text-decoration-none text-dark d-block">
              <div className="card h-100 shadow-sm border-0 menu-square-card">
                <i className={`bi ${item.icon} fs-2 mb-2`}></i>
                <span className="fw-semibold small">{item.label}</span>
              </div>
            </Link>
          </div>
        ))}
      </div>

      {/* 購入履歴 */}
      <div className="mb-5">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="fw-bold m-0">購入履歴</h4>
          <Link to="/mypage/purchases" className="text-secondary text-decoration-none">
            もっと見る &gt;
          </Link>
        </div>

        <div
          ref={scrollRef}
          className="drag-scroll-container d-flex pb-3 mypage-order-scroll"
          onMouseDown={onMouseDown}
          onMouseLeave={onMouseLeave}
          onMouseUp={onMouseUp}
          onMouseMove={onMouseMove}
        >
          {loadingOrders ? (
            <div className="w-100 text-center text-secondary py-5 bg-light rounded">
              読み込み中...
            </div>
          ) : visibleOrders.length > 0 ? (
            <>
              {visibleOrders.slice(0, 5).map((item) => {
                const { label, badgeClass } = getStatusInfo(item.orderStatusValue);
                const displayTitle =
                  item.orderItemCount > 1
                    ? `${item.productName} 外 ${item.orderItemCount - 1}件`
                    : item.productName;
                return (
                  <Link
                    key={item.orderNo}
                    to={`/mypage/purchases/${encodeURIComponent(item.orderNo)}`}
                    className="text-decoration-none text-dark mypage-order-card-link"
                    onClick={handleLinkClick}
                    draggable="false"
                  >
                    <div className="card h-100 border-0 shadow-sm">
                      <div className="position-relative">
                        <img
                          src={buildImageUrl(item.productImageUrl)}
                          className="card-img-top mypage-order-image"
                          alt={item.productName}
                          onError={(e) => {
                            e.target.src = noImage;
                          }}
                          draggable="false"
                        />
                        {item.deliveryDate && (
                          <span className="position-absolute bottom-0 start-0 bg-white bg-opacity-90 px-2 py-1 small fw-bold m-2 rounded shadow-sm">
                            {item.deliveryDate.includes('-')
                              ? item.deliveryDate
                              : `${item.deliveryDate} 発送`}
                          </span>
                        )}
                      </div>
                      <div className="card-body p-3 d-flex flex-column justify-content-between">
                        <h6
                          className="card-title fw-bold text-dark mb-3 mypage-order-title"
                          title={displayTitle}
                        >
                          {displayTitle}
                        </h6>

                        <div className="d-flex flex-column gap-2">
                          <span className="fw-bolder fs-5 text-dark">
                            {Number(item.totalPrice || 0).toLocaleString()}円
                          </span>

                          <span className={`badge small align-self-start px-2 py-1 ${badgeClass}`}>
                            {label}
                          </span>
                        </div>
                      </div>
                    </div>
                  </Link>
                );
              })}

              <Link
                to="/mypage/purchases"
                className="see-more-card shadow-sm"
                onClick={handleLinkClick}
                draggable="false"
              >
                <div className="see-more-icon-wrapper">
                  <i className="bi bi-chevron-right fs-4"></i>
                </div>
                <span className="fw-bold">もっと見る</span>
              </Link>
            </>
          ) : (
            <div className="w-100 text-center text-secondary py-5 bg-light rounded">
              購入履歴がありません。
            </div>
          )}
        </div>
      </div>

      {/* お問い合わせ履歴 */}
      <div className="mb-5">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="fw-bold m-0">お問い合わせ履歴</h4>
          <Link to="/mypage/inquiries" className="text-secondary text-decoration-none">
            もっと見る &gt;
          </Link>
        </div>
        <table className="table table-hover align-middle">
          <tbody>
            {loadingInquiries ? (
              <tr>
                <td colSpan="3" className="text-center text-secondary py-3 small">
                  読み込み中...
                </td>
              </tr>
            ) : inquiries.length > 0 ? (
              inquiries.map((item) => (
                <tr key={item.inquiryId}>
                  <td className="text-start mypage-inquiry-status mypage-inquiry-status-col">
                    <span
                      className={`fw-bold ${item.status === '回答完了' ? 'text-primary' : 'text-danger'}`}
                    >
                      {item.status || '回答待ち'}
                    </span>
                  </td>

                  <td>
                    <Link
                      to={`/mypage/inquiries/${item.inquiryId}`}
                      className="text-decoration-none text-dark d-block text-truncate mypage-inquiry-title"
                    >
                      {item.isPrivate && <i className="bi bi-lock-fill me-2 text-secondary"></i>}
                      {item.title}
                    </Link>
                  </td>
                  <td className="text-secondary text-end small mypage-inquiry-date mypage-inquiry-date-col">
                    {formatDate(item.createdAt)}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3" className="text-center text-secondary py-3">
                  お問い合わせ履歴がありません。
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* 個人情報変更セクション */}
      <div className="border rounded p-4 mb-5 shadow-sm bg-white">
        <h5 className="fw-bold mb-3">個人情報の変更</h5>
        <div className="row g-4">
          {personalLinks.map((link, idx) => (
            <div className="col-md-6" key={idx}>
              <Link to={link.to} className={`fw-bold text-decoration-none ${link.color}`}>
                {link.title}
              </Link>
              <p className="small text-secondary mb-0">
                <i className="bi bi-dot me-1"></i>
                {link.desc}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
