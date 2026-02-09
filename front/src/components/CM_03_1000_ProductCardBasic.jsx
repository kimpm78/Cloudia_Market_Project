import { Link } from 'react-router-dom';
import { hasReservationClosed } from '../utils/productStatus';
import { isReservationProduct } from '../utils/productPageFilters';
import noImage from '../images/common/CM-NoImage.png';

export default function ProductCardBasic({ item, imageUrl, soldOut, categoryLabel, noteHtml }) {
  const itemId = item.productId ?? item.id ?? item.productCode;
  const categoryText = categoryLabel ?? item.categoryGroupName;
  const codeValue = item?.codeValue ?? item?.code_value;
  const statusCode = codeValue !== undefined && codeValue !== null ? String(codeValue).trim() : '';
  const reservationFlag = statusCode === '3' || statusCode === '4' || isReservationProduct(item);
  const reservationClosed = hasReservationClosed(item, { useClientTime: true });
  const showOverlay = soldOut || reservationClosed;
  const overlayText = reservationClosed ? '예약 마감' : 'SOLD OUT';
  const badgeText = reservationClosed ? '예약 마감' : '예약중';
  const badgeClassName = reservationClosed ? 'bg-secondary text-white' : 'bg-primary text-white';

  return (
    <div className="col d-flex" key={itemId}>
      <Link to={`/detail/${itemId}`} className="text-decoration-none text-dark h-100 d-flex">
        <div className="card border-0 text-start rounded overflow-hidden h-100 d-flex flex-column">
          <div className="position-relative">
            <img
              src={imageUrl}
              onError={(e) => (e.currentTarget.src = noImage)}
              className="card-img-top rounded"
              alt="상품 이미지"
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
            {reservationFlag && (
              <div
                className={`position-absolute bottom-0 w-100 text-white text-center py-2 item-text ${badgeClassName}`}
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
                [{reservationClosed ? '예약마감' : '예약구매'}]{' '}
                {!reservationClosed && (
                  <span className="text-danger">*조기 품절 될 수 있습니다.</span>
                )}
              </p>
            )}
            <div className="fw-bold text-secondary" style={{ fontSize: '0.95rem' }}>
              {item.productName ?? item.name}
            </div>
            <div className="mt-auto">
              <div className="fw-bold mt-2 item-price-text fs-5">
                {item.price.toLocaleString()}원
              </div>
              {noteHtml && <div className="product-note" dangerouslySetInnerHTML={noteHtml} />}
            </div>
          </div>
        </div>
      </Link>
    </div>
  );
}
