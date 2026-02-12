import { Link } from 'react-router-dom';
import { hasReservationClosed } from '../utils/productStatus';
import { isReservationProduct } from '../utils/productPageFilters';
import noImage from '../images/common/CM-NoImage.png';

export default function ProductCardBasic({
  item,
  imageUrl,
  soldOut,
  categoryLabel,
  noteHtml,
  statusCode,
  reservationClosed: reservationClosedProp,
}) {
  const itemId = item.productId ?? item.id ?? item.productCode;
  const categoryText = categoryLabel ?? item.categoryGroupName;
  const rawCodeValue = statusCode ?? item?.codeValue ?? item?.code_value;
  const normalizedStatusCode =
    rawCodeValue !== undefined && rawCodeValue !== null ? String(rawCodeValue).trim() : '';
  const reservationFlag =
    normalizedStatusCode === '3' ||
    normalizedStatusCode === '4' ||
    isReservationProduct(item);
  const reservationClosed =
    reservationClosedProp ??
    (normalizedStatusCode === '4' || hasReservationClosed(item, { useClientTime: true }));
  const soldOutState =
    soldOut || normalizedStatusCode === '2' || normalizedStatusCode === '4' || reservationClosed;
  const showOverlay = soldOutState || reservationClosed;
  const overlayText = reservationClosed ? '予約締切' : 'SOLD OUT';
  const badgeText =
    normalizedStatusCode === '2' ? 'SOLD OUT' : reservationClosed ? '予約締切' : '予約中';
  const badgeClassName =
    normalizedStatusCode === '2'
      ? 'bg-warning text-dark'
      : reservationClosed
        ? 'bg-secondary text-white'
        : 'bg-primary text-white';
  const showBadge = normalizedStatusCode === '2' || reservationFlag;

  return (
    <div className="col d-flex" key={itemId}>
      <Link to={`/detail/${itemId}`} className="text-decoration-none text-dark h-100 d-flex">
        <div className="card border-0 text-start rounded overflow-hidden h-100 d-flex flex-column">
          <div className="position-relative">
            <img
              src={imageUrl}
              onError={(e) => (e.currentTarget.src = noImage)}
              className="card-img-top rounded"
              alt="商品画像"
              style={{ aspectRatio: '1/1', objectFit: 'cover' }}
            />
            {showOverlay && (
              <div className="position-absolute top-0 start-0 w-100 h-100 d-flex flex-column justify-content-center align-items-center bg-light bg-opacity-75 rounded">
                <div
                  className={`fw-bold fs-4 text-dark ${
                    reservationClosed ? '' : 'fst-italic text-uppercase'
                  }`}
                >
                  {overlayText}
                </div>
              </div>
            )}
            {showBadge && (
              <div
                className={`position-absolute bottom-0 w-100 text-center py-2 item-text ${badgeClassName}`}
                style={{ fontWeight: 600 }}
              >
                {badgeText}
              </div>
            )}
          </div>
          <div className="card-body pt-2 p-0 d-flex flex-column">
            <p className="mb-1 text-muted small" style={{ fontSize: '12px' }}>
              {item.brand}
            </p>
            {categoryText && (
              <p className="mb-1 text-muted small" style={{ fontSize: '12px' }}>
                {categoryText}
              </p>
            )}
            {reservationFlag && (
              <p className="mb-1 text-primary small fw-bold" style={{ fontSize: '12px' }}>
                [{reservationClosed ? '予約締切' : '予約購入'}]{' '}
                {!reservationClosed && (
                  <span className="text-danger">※早期に売り切れる場合があります。</span>
                )}
              </p>
            )}
            <div className="fw-bold text-secondary" style={{ fontSize: '0.95rem' }}>
              {item.productName ?? item.name}
            </div>
            <div className="mt-auto">
              <div className="fw-bold mt-2 item-price-text fs-5">
                {item.price.toLocaleString()}円
              </div>
              {noteHtml && <div className="product-note" dangerouslySetInnerHTML={noteHtml} />}
            </div>
          </div>
        </div>
      </Link>
    </div>
  );
}
