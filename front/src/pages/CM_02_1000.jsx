import { useEffect, useState, useRef } from 'react';
import axiosInstance from '../services/axiosInstance';
import { useAuth } from '../contexts/AuthContext';
import CM_02_1000_board from '../components/CM_02_1000_board';
import CM_03_1000 from './CM_03_1000';
import CM_03_1001 from './CM_03_1001';
import noImage from '../images/common/CM-NoImage.png';

import '../styles/CM_02_1000.css';

export default function CM_02_1000() {
  const { user } = useAuth();
  const roleId = Number(user?.roleId);
  const loginId =
    roleId === 1 ? '관리자' : roleId === 2 ? '매니저' : user?.loginId || user?.userId || '게스트';
  const [banners, setBanners] = useState([]);
  const [activeIndex, setActiveIndex] = useState(0);
  const [newProductCount, setNewProductCount] = useState(0);
  const [preOrderCount, setPreOrderCount] = useState(0);
  const [newProductLimit, setNewProductLimit] = useState(30);
  const [preOrderLimit, setPreOrderLimit] = useState(5);
  const carouselRef = useRef(null);

  useEffect(() => {
    const fetchBanners = async () => {
      try {
        const res = await axiosInstance.get(
          `${import.meta.env.VITE_API_BASE_URL}/guest/menu/banner/findAll`
        );
        const list = Array.isArray(res.data) ? res.data : res.data?.resultList || [];
        const sorted = list
          .filter((b) => b.isDisplay === 1)
          .sort((a, b) => a.displayOrder - b.displayOrder);
        setBanners(sorted);
      } catch (err) {
        console.error('배너 로드 실패:', err?.response?.data?.message || err.message);
      }
    };

    fetchBanners();
  }, []);

  useEffect(() => {
    const el = carouselRef.current;
    if (!el) return;
    const onSlid = (e) => {
      if (typeof e.to === 'number') setActiveIndex(e.to);
      else {
        const active = el.querySelector('.carousel-indicators .active');
        if (active && active.getAttribute('data-bs-slide-to')) {
          setActiveIndex(parseInt(active.getAttribute('data-bs-slide-to'), 10));
        }
      }
    };
    el.addEventListener('slid.bs.carousel', onSlid);
    return () => el.removeEventListener('slid.bs.carousel', onSlid);
  }, []);

  useEffect(() => {
    const el = carouselRef.current;
    if (!el || banners.length === 0) return;
    const BootstrapCarousel = window?.bootstrap?.Carousel;
    if (!BootstrapCarousel) return;
    const instance = BootstrapCarousel.getOrCreateInstance(el);
    instance.cycle();
  }, [banners.length]);

  const getBannerImageUrl = (banner) => {
    const link = banner?.imageLink || banner?.image_link;
    if (!link) return noImage;
    return `${import.meta.env.VITE_API_BASE_IMAGE_URL}/${link.replace(/^\/+/, '')}`;
  };

  return (
    <>
      <div className="container-fluid d-flex justify-content-center w-100 px-4 px-sm-4">
        <div className="w-100 d-flex justify-content-end py-2 pe-2">
          <span className="text-muted small">
            어서오세요! <strong>{loginId}</strong> 님
          </span>
        </div>
      </div>
      <CM_02_1000_board />
      <div className="container-fluid d-flex justify-content-center max-w-1080 px-0">
        <div style={{ width: '100%' }}>
          <div
            id="carouselExampleIndicators"
            className="carousel slide w-100"
            data-bs-ride="carousel"
            data-bs-wrap="true"
            data-bs-interval="4000"
            ref={carouselRef}
          >
            <div className="carousel-indicators">
              {banners.map((_, index) => (
                <button
                  key={index}
                  type="button"
                  data-bs-target="#carouselExampleIndicators"
                  data-bs-slide-to={index}
                  className={index === 0 ? 'active' : ''}
                  aria-current={index === 0 ? 'true' : undefined}
                  aria-label={`Slide ${index + 1}`}
                ></button>
              ))}
            </div>
            <div className="carousel-inner">
              {banners.map((banner, index) => (
                <div
                  key={banner.banner_id || index}
                  className={`carousel-item ${index === 0 ? 'active' : ''}`}
                >
                  <a
                    href={banner.urlLink || banner.url_link || '#'}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="w-100 h-100 d-flex align-items-center justify-content-center"
                  >
                    <img
                      src={getBannerImageUrl(banner)}
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = noImage;
                      }}
                      className="banner-image"
                      alt={banner.bannerName || `main_${index + 1}`}
                    />
                  </a>
                </div>
              ))}
            </div>
            <button
              className="carousel-control-prev"
              type="button"
              data-bs-target="#carouselExampleIndicators"
              data-bs-slide="prev"
            >
              <span className="carousel-control-prev-icon" aria-hidden="true"></span>
              <span className="visually-hidden">Previous</span>
            </button>
            <button
              className="carousel-control-next"
              type="button"
              data-bs-target="#carouselExampleIndicators"
              data-bs-slide="next"
            >
              <span className="carousel-control-next-icon" aria-hidden="true"></span>
              <span className="visually-hidden">Next</span>
            </button>
          </div>
        </div>
      </div>
      {banners.length > 0 && (
        <div className="text-center mt-3">
          <h5 className="fs-4 fs-md-4 fw-bold text-black">
            {banners[Math.min(activeIndex, banners.length - 1)]?.bannerName}
          </h5>
        </div>
      )}
      <CM_03_1000
        showFilter={false}
        showPagination={false}
        limit={newProductLimit}
        onTotalChange={setNewProductCount}
      />
      {newProductCount > newProductLimit && (
        <div className="text-center my-4">
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setNewProductLimit((prev) => prev + 10)}
          >
            신상품 더보기
          </button>
        </div>
      )}
      <CM_03_1001
        showFilter={false}
        limit={preOrderLimit}
        showPagination={false}
        onTotalChange={setPreOrderCount}
      />
      {preOrderCount > preOrderLimit && (
        <div className="text-center my-4">
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setPreOrderLimit((prev) => prev + 10)}
          >
            예약상품 더보기
          </button>
        </div>
      )}
    </>
  );
}
