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
    const label = statusObj ? statusObj.label : '상태 미정';

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

  // 메뉴 및 링크 데이터
  const menuCards = [
    { to: '/mypage/purchases', icon: 'bi-box-seam', label: '구매 내역' },
    { to: '/mypage/inquiries', icon: 'bi-chat-dots', label: '문의 내역' },
    { to: '/mypage/returns', icon: 'bi-arrow-left-right', label: '교환/반품' },
    { to: '/mypage/address-book', icon: 'bi-geo-alt', label: '주소록 관리' },
    { to: '/mypage/profile', icon: 'bi-person', label: '프로필' },
    { to: '/mypage/change-password', icon: 'bi-key', label: '비밀번호 변경' },
  ];
  const personalLinks = [
    {
      to: '/mypage/profile',
      title: '프로필',
      desc: '이름, 등록정보, 전화번호 등 확인과 변경',
      color: 'text-primary',
    },
    {
      to: '/mypage/address-book',
      title: '주소록 관리',
      desc: '배송지 등록과 변경',
      color: 'text-primary',
    },
    {
      to: '/mypage/account',
      title: '환불 계좌번호 등록',
      desc: '환불받으실 계좌 정보 등록 및 관리',
      color: 'text-primary',
    },
    {
      to: '/mypage/change-password',
      title: '비밀번호 변경',
      desc: '비밀번호 변경',
      color: 'text-primary',
    },
    {
      to: '/mypage/unsubscribe',
      title: '회원 탈퇴',
      desc: '무기와라 장터의 회원 탈퇴 수속',
      color: 'text-dark',
    },
  ];

  const visibleOrders = orders.filter(
    (item) => item.orderStatusValue !== ORDER_STATUS_CODE.PAYMENT_FAILED
  );

  return (
    <div className="mypage-wrapper">
      <h4 className="border-bottom fw-bolder pb-3 mb-4">마이 페이지</h4>
      {/* 메뉴 카드 */}
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

      {/* 구매 내역 */}
      <div className="mb-5">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="fw-bold m-0">구매 내역</h4>
          <Link to="/mypage/purchases" className="text-secondary text-decoration-none">
            더보기 &gt;
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
              불러오는 중...
            </div>
          ) : visibleOrders.length > 0 ? (
            <>
              {visibleOrders.slice(0, 5).map((item) => {
                const { label, badgeClass } = getStatusInfo(item.orderStatusValue);
                const displayTitle =
                  item.orderItemCount > 1
                    ? `${item.productName} 외 ${item.orderItemCount - 1}건`
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
                              : `${item.deliveryDate} 발송`}
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
                            {Number(item.totalPrice || 0).toLocaleString()}원
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
                <span className="fw-bold">더보기</span>
              </Link>
            </>
          ) : (
            <div className="w-100 text-center text-secondary py-5 bg-light rounded">
              구매 내역이 없습니다.
            </div>
          )}
        </div>
      </div>

      {/* 문의 내역 */}
      <div className="mb-5">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="fw-bold m-0">문의 내역</h4>
          <Link to="/mypage/inquiries" className="text-secondary text-decoration-none">
            더보기 &gt;
          </Link>
        </div>
        <table className="table table-hover align-middle">
          <tbody>
            {loadingInquiries ? (
              <tr>
                <td colSpan="3" className="text-center text-secondary py-3 small">
                  불러오는 중...
                </td>
              </tr>
            ) : inquiries.length > 0 ? (
              inquiries.map((item) => (
                <tr key={item.inquiryId}>
                  <td className="text-start mypage-inquiry-status mypage-inquiry-status-col">
                    <span
                      className={`fw-bold ${item.status === '답변완료' ? 'text-primary' : 'text-danger'}`}
                    >
                      {item.status || '답변 대기'}
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
                  문의 내역이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* 개인정보 변경 섹션 */}
      <div className="border rounded p-4 mb-5 shadow-sm bg-white">
        <h5 className="fw-bold mb-3">개인정보 변경</h5>
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
