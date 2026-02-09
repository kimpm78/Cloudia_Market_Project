import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';
import { fetchReviews as fetchReviewsService } from '../services/ReviewService.js';
import { buildImageUrl } from '../services/ReviewService.js';
import { CATEGORIES } from '../constants/constants.js';

import noImage from '../images/common/CM-NoImage.png';
import '../styles/CM_02_1000_board.css';

export default function CM_02_1000_board() {
  const [reviewPage, setReviewPage] = useState(1);
  const [noticePage, setNoticePage] = useState(1);

  const [notices, setNotices] = useState([]);

  useEffect(() => {
    axiosInstance
      .get(`${import.meta.env.VITE_API_BASE_URL}/guest/notice`)
      .then((res) => {
        // console.log('공지사항 응답 데이터:', res.data);
        const list = Array.isArray(res.data) ? res.data : res.data?.resultList || [];
        if (Array.isArray(list)) {
          setNotices(list.filter((notice) => notice.isDisplay === 1));
        } else {
          setNotices([]);
        }
      })
      .catch((err) => {
        console.error('공지사항 로드 실패:', err?.response?.data?.message || err.message);
      });
  }, []);

  const noticesPerPage = 7;
  const slicedNotices = notices.slice(0, 14);
  const noticePages =
    slicedNotices.length > 0
      ? [slicedNotices.slice(0, noticesPerPage), slicedNotices.slice(noticesPerPage, 14)]
      : [[]];

  const reviewsPerPage = 6;

  const [allReviews, setAllReviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadReviews = async () => {
      setLoading(true);
      const list = await fetchReviewsService();
      setAllReviews(list);
      setLoading(false);
    };
    loadReviews();
  }, []);

  const sortedReviews = [...allReviews].sort(
    (a, b) => new Date(b.created_at) - new Date(a.created_at)
  );
  const paginatedReviews = [];
  for (let i = 0; i < sortedReviews.length; i += reviewsPerPage) {
    paginatedReviews.push(sortedReviews.slice(i, i + reviewsPerPage));
  }
  const reviewPages = paginatedReviews.length > 0 ? paginatedReviews : [[]];
  const totalReviewPages = reviewPages.filter((p) => p.length > 0).length;

  const totalNoticePages = noticePages.filter((p) => p.length > 0).length;

  useEffect(() => {
    setReviewPage((prev) => {
      if (prev > totalReviewPages) {
        return Math.max(totalReviewPages, 1);
      }
      if (prev < 1) {
        return 1;
      }
      return prev;
    });
  }, [totalReviewPages]);

  useEffect(() => {
    setNoticePage((prev) => {
      if (prev > totalNoticePages) {
        return Math.max(totalNoticePages, 1);
      }
      if (prev < 1) {
        return 1;
      }
      return prev;
    });
  }, [totalNoticePages]);

  const currentReviewIndex =
    totalReviewPages === 0 ? 0 : Math.min(Math.max(reviewPage - 1, 0), totalReviewPages - 1);
  const currentReviewPage = reviewPages[currentReviewIndex] ?? [];

  const getCategoryClass = (value) => {
    switch (value) {
      case 1:
      case 2:
        return 'urgent';
      case 3:
        return 'payment';
      default:
        return 'default';
    }
  };

  return (
    <div className="main-board">
      <div className="notice-review-board d-flex flex-column flex-lg-row border rounded overflow-hidden">
        {/* 공지 영역 */}
        <div className="notice-section w-100 w-lg-50 border-end d-block d-lg-inline-block">
          <div className="header bg-light d-flex justify-content-between align-items-center px-3 py-2">
            <Link to="/notice" className="text-dark text-decoration-none fw-bold">
              공지사항
            </Link>
            <div>
              <span>
                {noticePages.filter((p) => p.length > 0).length === 0
                  ? '0 / 0'
                  : `${noticePage} / ${noticePages.filter((p) => p.length > 0).length}`}
              </span>
              <button
                className="btn btn-sm btn-dark ms-2 me-1"
                onClick={() => setNoticePage(1)}
                disabled={noticePage === 1 || noticePages.filter((p) => p.length > 0).length === 0}
              >
                ◀
              </button>
              <button
                className="btn btn-sm btn-dark"
                onClick={() => setNoticePage(noticePage + 1)}
                disabled={
                  noticePage === noticePages.filter((p) => p.length > 0).length ||
                  noticePages.filter((p) => p.length > 0).length === 0
                }
              >
                ▶
              </button>
            </div>
          </div>
          <ul className="list-unstyled text-center m-0 p-3 lh-lg">
            {noticePages[noticePage - 1]?.length === 0 && (
              <li className="text-muted">공지사항 내용이 없습니다.</li>
            )}
            {(noticePages[noticePage - 1] ?? []).map((notice) => {
              const codeValue = Number(notice.codeValue);
              const category = CATEGORIES.find((cat) => cat.value === codeValue);

              const categoryLabel = category ? category.label : notice.codeValue;
              const badgeClass = getCategoryClass(codeValue);

              return (
                <li key={notice.noticeId} className="mb-2 d-flex align-items-center gap-3">
                  <strong className={`badge-category ${badgeClass}`}>{categoryLabel}</strong>
                  <Link
                    to={`/notice/${notice.noticeId}`}
                    className="fw-semibold text-decoration-none text-dark"
                  >
                    {notice.title}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>
        {/* 리뷰 영역 */}
        <div className="review-section w-100 w-lg-50 d-block d-lg-inline-block">
          <div className="header bg-light d-flex justify-content-between align-items-center px-3 py-2">
            <Link to="/review" className="text-dark text-decoration-none fw-bold">
              리뷰/후기
            </Link>
            <div>
              <span>
                {totalReviewPages === 0
                  ? '0 / 0'
                  : `${Math.min(reviewPage, totalReviewPages)} / ${totalReviewPages}`}
              </span>
              <button
                className="btn btn-sm btn-dark ms-2 me-1"
                onClick={() => setReviewPage(reviewPage - 1)}
                disabled={reviewPage === 1 || totalReviewPages === 0}
              >
                ◀
              </button>
              <button
                className="btn btn-sm btn-dark"
                onClick={() => setReviewPage(reviewPage + 1)}
                disabled={reviewPage === totalReviewPages || totalReviewPages === 0}
              >
                ▶
              </button>
            </div>
          </div>
          <div className="p-3 row row-cols-1 row-cols-md-2 g-3">
            {loading && <div className="text-center w-100">로딩 중...</div>}
            {!loading && currentReviewPage.length === 0 && (
              <div className="text-muted text-center w-100">등록된 리뷰가 없습니다.</div>
            )}
            {!loading &&
              currentReviewPage.map((review, idx) => (
                <Link
                  key={idx}
                  to={`/review/${review.reviewId}`}
                  className="d-flex text-decoration-none text-dark"
                >
                  <div className="review-img-wrapper me-3">
                    <img
                      className="review-img"
                      src={buildImageUrl(review.imageUrl)}
                      alt={`리뷰${idx}`}
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = noImage;
                      }}
                    />
                  </div>
                  <div>
                    <div className="fw-semibold">{review.title}</div>
                    <div className="small text-muted">{review.createdBy}</div>
                  </div>
                </Link>
              ))}
          </div>
        </div>
      </div>
    </div>
  );
}
