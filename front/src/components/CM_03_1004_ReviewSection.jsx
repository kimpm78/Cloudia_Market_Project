import { Link } from 'react-router-dom';
import noImage from '../images/common/CM-NoImage.png';
import { buildImageUrl } from '../services/ReviewService';
import { formatYearMonthDot } from '../utils/DateFormat';

export default function CM_03_1004_ReviewSection({ reviews = [], reviewRef }) {
  const items = Array.isArray(reviews) ? reviews : [];

  return (
    <div id="review-section" className="product-review-section mb-5">
      <h5 className="fw-bold mb-3" ref={reviewRef}>
        レビュー/口コミ
      </h5>
      <div className="d-flex flex-column gap-3">
        {items.length > 0 ? (
          items.map((review) => (
            <Link
              key={review.reviewId}
              to={`/review/${review.reviewId}`}
              className="d-flex border rounded p-3 gap-3 align-items-center"
              style={{ textDecoration: 'none', color: 'inherit', cursor: 'pointer' }}
              onClick={() => {
                setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 0);
              }}
            >
              <img
                src={buildImageUrl(review.imageUrl) || noImage}
                alt="리뷰 썸네일"
                style={{ width: 120, height: 80, objectFit: 'cover' }}
              />
              <div className="flex-grow-1 d-flex align-items-center justify-content-between gap-3">
                <div className="flex-grow-1">
                  <p className="mb-1 fw-semibold review-title-clamp">{review.title}</p>
                  <div className="text-muted small">{review.createdBy}</div>
                </div>
                <span className="text-muted small text-nowrap">
                  {formatYearMonthDot(review.createdAt)}
                </span>
              </div>
            </Link>
          ))
        ) : (
          <p className="text-muted">レビュー/口コミ内容がありません。</p>
        )}
      </div>
    </div>
  );
}
