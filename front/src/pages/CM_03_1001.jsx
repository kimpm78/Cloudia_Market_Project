import { useState, useEffect, useRef } from 'react';
import axiosInstance from '../services/axiosInstance';
import { Link } from 'react-router-dom';

import noImage from '../images/common/CM-NoImage.png';
import CM_99_1011_filterSidebar from '../components/CM_99_1011_filterSidebar';
import CMMessage from '../constants/CMMessage';
import '../styles/CM_03_1001.css';
import useCartMeta from '../hooks/useCartMeta';
import useProductPage from '../hooks/useProductPage';
import { hasReservationClosed } from '../utils/productStatus';
import { isReservationProduct } from '../utils/productPageFilters';

const PRODUCT_STATUS_MAP = {
  1: '판매중',
  2: 'SOLD OUT',
  3: '예약중',
  4: '예약 마감',
};

const normalizeStatusCode = (value) => {
  if (value === undefined || value === null) return null;
  const code = `${value}`.trim();
  return PRODUCT_STATUS_MAP[code] ? code : null;
};

const isReservationStatus = (code) => code === '3' || code === '4';
const isSoldOutStatus = (code) => code === '2' || code === '4';

export default function CM_03_1001({
  showFilter = true,
  showPagination = true,
  limit = null,
  onTotalChange = null,
}) {
  const itemsPerPage = 30;
  const [allItems, setAllItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const topRef = useRef(null);
  const cartMeta = useCartMeta();

  const getItemPrimaryKey = (item) => {
    const candidates = [item?.productId, item?.product_id, item?.productCode, item?.product_code]
      .filter((v) => v !== undefined && v !== null)
      .map((v) => `${v}`.trim())
      .filter((v) => v.length > 0);
    return candidates[0] ?? null;
  };

  const getImageUrl = (url) => {
    const PLACEHOLDER = noImage;
    if (!url || typeof url !== 'string') return PLACEHOLDER;

    try {
      if (url.startsWith('http')) {
        return url;
      }

      return `${import.meta.env.VITE_API_BASE_IMAGE_URL}/${url}`;
    } catch (e) {
      return PLACEHOLDER;
    }
  };

  const resolveAvailableQty = (item) => {
    const candidates = [
      item?.availableQty,
      item?.availableqty,
      item?.available_quantity,
      item?.quantity,
      item?.stock,
    ];
    for (const candidate of candidates) {
      if (typeof candidate === 'number' && !Number.isNaN(candidate)) {
        return candidate;
      }
    }
    return null;
  };

  const getCartReservedQty = (item) => {
    if (!item) return 0;

    const rawKeys = [
      item.productCode,
      item.product_code,
      item.productCodeId,
      item.product_code_id,
      item.productId,
      item.product_id,
      item.id,
      item.id_code,
    ];

    const candidateKeys = rawKeys
      .filter((value) => value != null)
      .map((value) => `${value}`.trim())
      .filter((v) => v.length > 0);

    const uniqueKeys = [...new Set(candidateKeys)];

    for (const key of uniqueKeys) {
      const entry = cartMeta?.[key];
      if (entry?.quantity != null && typeof entry.quantity === 'number') {
        return entry.quantity;
      }
    }
    return 0;
  };

  const buildStatusBadge = (status) => {
    if (!status) return null;
    if (status.code === '4') {
      return { text: PRODUCT_STATUS_MAP['4'], className: 'bg-secondary text-white' };
    }
    if (status.code === '3') {
      return { text: PRODUCT_STATUS_MAP['3'], className: 'bg-primary text-white' };
    }
    if (status.code === '2') {
      return { text: PRODUCT_STATUS_MAP['2'], className: 'bg-warning text-dark' };
    }
    return null;
  };

  const detectSoldOut = (item) => {
    if (item?.isSoldOut === true) {
      return true;
    }
    const soldOutFlags = [
      item?.isSoldOut,
      item?.soldOut,
      item?.reservationSoldOut,
      item?.reservationClosed,
      item?.stockStatus === 'SOLD_OUT',
      item?.status === 'SOLD_OUT',
      item?.stock === 0,
      item?.quantity === 0,
      typeof item?.availableQty === 'number' && item.availableQty <= 0,
      item?.availableQty === 0,
    ];
    if (soldOutFlags.some(Boolean)) {
      return true;
    }
    const available = resolveAvailableQty(item);
    const reserved =
      typeof item?.reservedQty === 'number'
        ? item.reservedQty
        : typeof item?.reservationQuantity === 'number'
          ? item.reservationQuantity
          : 0;
    if (available !== null && available - reserved <= 0) {
      return true;
    }
    const userReserved = getCartReservedQty(item);
    if (available !== null && available - userReserved <= 0) {
      return true;
    }
    return false;
  };

  const getProductStatus = (item) => {
    const explicitCode = normalizeStatusCode(item?.codeValue ?? item?.code_value);
    const reservationClosed = hasReservationClosed(item);
    const soldOutByStock = detectSoldOut(item);
    const reservationFlag = isReservationProduct(item);

    if (explicitCode) {
      return {
        code: explicitCode,
        label: PRODUCT_STATUS_MAP[explicitCode],
        isReservation: isReservationStatus(explicitCode),
        reservationClosed: explicitCode === '4',
        // 코드일람(003) 기준으로만 품절/마감 처리 (2=품절, 4=예약 마감)
        soldOut: isSoldOutStatus(explicitCode) || soldOutByStock,
      };
    }

    if (reservationClosed) {
      return {
        code: '4',
        label: PRODUCT_STATUS_MAP['4'],
        isReservation: true,
        reservationClosed: true,
        soldOut: true,
      };
    }

    if (soldOutByStock) {
      return {
        code: '2',
        label: PRODUCT_STATUS_MAP['2'],
        isReservation: reservationFlag,
        reservationClosed: false,
        soldOut: true,
      };
    }

    if (reservationFlag) {
      return {
        code: '3',
        label: PRODUCT_STATUS_MAP['3'],
        isReservation: true,
        reservationClosed: false,
        soldOut: false,
      };
    }

    return {
      code: '1',
      label: PRODUCT_STATUS_MAP['1'],
      isReservation: false,
      reservationClosed: false,
      soldOut: false,
    };
  };

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const [preOrderRes, newProductRes] = await Promise.all([
          axiosInstance.get(`${import.meta.env.VITE_API_BASE_URL}/guest/pre-order`),
          axiosInstance.get(`${import.meta.env.VITE_API_BASE_URL}/guest/new-product`),
        ]);

        const preOrderList = Array.isArray(preOrderRes.data?.resultList)
          ? preOrderRes.data.resultList
          : [];
        const filteredPreOrderList = preOrderList.filter(isReservationProduct);
        const newProductList = Array.isArray(newProductRes.data?.resultList)
          ? newProductRes.data.resultList
          : [];

        const existingKeys = new Set(
          filteredPreOrderList.map((item) => getItemPrimaryKey(item)).filter((v) => v !== null)
        );

        const supplement = newProductList.filter(
          (item) => isReservationProduct(item) && !existingKeys.has(getItemPrimaryKey(item))
        );

        setAllItems([...filteredPreOrderList, ...supplement]);
        setLoading(false);
      } catch (err) {
        console.error('예약상품 불러오기 실패:', err?.response?.data?.message || err.message);
        setError(err);
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const handleReset = () => {
    resetFilters();
  };

  const handleFilterChange = (filter) => {
    setSelectedFilters((prev) => {
      const isActive = prev.includes(filter);
      if (isActive) {
        return prev.filter((f) => f !== filter);
      }
      if (filter === '마감된 상품 보기') {
        return [...prev.filter((f) => f !== '예약종료상품 제외'), filter];
      }
      if (filter === '예약종료상품 제외') {
        return [...prev.filter((f) => f !== '마감된 상품 보기'), filter];
      }
      return [...prev, filter];
    });
  };

  const categoryGroupLabels = ['예약 상품'];
  const {
    currentPage,
    totalPages,
    paginatedItems,
    keyword,
    selectedFilters,
    setSelectedFilters,
    handleKeywordChange,
    handlePageClick,
    resetFilters,
  } = useProductPage({
    items: allItems,
    categoryGroupLabels,
    itemsPerPage,
    showPagination,
    limit,
    onTotalChange,
    topRef,
  });

  if (error) {
    return <div className="text-center mt-5">{CMMessage.MSG_ERR_005('상품')}</div>;
  }

  return (
    <>
      <h1 ref={topRef}>예약상품</h1>
      <div className="container-fluid my-4">
        <div className="row justify-content-center">
          {showFilter && (
            <div className="col-12 col-md-3 mb-3 mb-md-0">
              <CM_99_1011_filterSidebar
                pageName="예약 상품"
                selectedFilters={selectedFilters}
                onFilterChange={handleFilterChange}
                onReset={handleReset}
                keyword={keyword}
                onKeywordChange={handleKeywordChange}
                categoryLabel="출시월"
              />
            </div>
          )}
          <div className="col-12 col-md-9">
            {paginatedItems.length === 0 ? (
              keyword.trim().length > 0 ? (
                <div className="text-center mt-5">
                  <h5 style={{ color: 'red' }}>{CMMessage.MSG_EMPTY_001}</h5>
                  <p className="text-muted">
                    {CMMessage.MSG_EMPTY_002}
                  </p>
                </div>
              ) : (
                <div className="text-center mt-5">
                  <h5 style={{ color: 'red' }}>{CMMessage.MSG_EMPTY_005}</h5>
                  <p className="text-muted">{CMMessage.MSG_EMPTY_006}</p>
                </div>
              )
            ) : (
              <div className="row row-cols-2 row-cols-md-5 g-4">
                {paginatedItems.map((item, i) => {
                  const itemId =
                    item.productId ??
                    item.product_id ??
                    item.id ??
                    item.productCode ??
                    item.product_code;
                  const status = getProductStatus(item);
                  const { soldOut, reservationClosed } = status;
                  const badge = buildStatusBadge(status);
                  const overlayText =
                    status?.label ??
                    (reservationClosed ? PRODUCT_STATUS_MAP['4'] : PRODUCT_STATUS_MAP['2']);
                  const cardContent = (
                    <div className="card h-100 text-start rounded overflow-hidden border-0 d-flex flex-column">
                      <div className="position-relative">
                        <img
                          src={getImageUrl(item.thumbnailUrl || item.image)}
                          onError={(e) => (e.currentTarget.src = noImage)}
                          className="product-card-img rounded-0"
                          alt="상품 이미지"
                        />
                        {soldOut && (
                          <div className="position-absolute top-0 start-0 w-100 h-100 d-flex flex-column justify-content-center align-items-center bg-light bg-opacity-75 rounded">
                            <div
                              className={`fw-bold fs-4 text-dark mb-3 ${
                                reservationClosed ? '' : 'fst-italic text-uppercase'
                              }`}
                            >
                              {overlayText}
                            </div>
                          </div>
                        )}
                        {badge && (
                          <div
                            className={`position-absolute bottom-0 w-100 text-center py-2 item-text ${badge.className}`}
                            style={{ fontWeight: 600 }}
                          >
                            {badge.text}
                          </div>
                        )}
                      </div>
                      <div className="card-body pt-2 p-0 d-flex flex-column">
                        {item.categoryGroupName && (
                          <div className="mb-1 text-muted small" style={{ fontSize: '12px' }}>
                            {item.categoryGroupName}
                          </div>
                        )}
                        <p className="mb-1 text-primary small fw-bold" style={{ fontSize: '12px' }}>
                          [예약구매] <span className="text-danger">*조기 품절 될 수 있습니다.</span>
                        </p>
                        <div className="fw-bold text-secondary" style={{ fontSize: '15px' }}>
                          {item.name}
                        </div>
                        <div className="mt-auto">
                          <div className="fw-bold mt-2 item-price-text fs-5">
                            {(item.productPrice ?? item.price)?.toLocaleString()}원
                          </div>
                          {item.productnote && (
                            <div
                              className="product-note"
                              dangerouslySetInnerHTML={{
                                __html: normalizeImageUrl(item.productnote),
                              }}
                            />
                          )}
                        </div>
                      </div>
                    </div>
                  );
                  // 상세 진입은 상태와 무관하게 허용(품절/예약마감도 상세는 확인 가능)
                  const disableLink =
                    itemId === undefined || itemId === null || `${itemId}`.trim() === '';
                  return (
                    <div className="col d-flex" key={itemId}>
                      {disableLink ? (
                        <div
                          className="text-decoration-none text-dark h-100 d-flex"
                          style={{ cursor: 'not-allowed', pointerEvents: 'none' }}
                          aria-disabled="true"
                        >
                          <div className="w-100 h-100 d-flex flex-column">{cardContent}</div>
                        </div>
                      ) : (
                        <Link
                          to={`/detail/${itemId}`}
                          className="text-decoration-none text-dark h-100 d-flex"
                        >
                          <div className="w-100 h-100 d-flex flex-column">{cardContent}</div>
                        </Link>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
            {showPagination && paginatedItems.length > 0 && (
              <div className="d-flex justify-content-center gap-2 my-4">
                <button
                  className="btn btn-outline-secondary"
                  disabled={currentPage === 1}
                  onClick={() => handlePageClick(currentPage - 1)}
                >
                  &lt;
                </button>
                {Array.from({ length: totalPages }).map((_, idx) => (
                  <button
                    key={idx + 1}
                    className={`btn ${currentPage === idx + 1 ? 'btn-primary text-white' : 'btn-outline-secondary'}`}
                    onClick={() => handlePageClick(idx + 1)}
                  >
                    {idx + 1}
                  </button>
                ))}
                <button
                  className="btn btn-outline-secondary"
                  disabled={currentPage === totalPages}
                  onClick={() => handlePageClick(currentPage + 1)}
                >
                  &gt;
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
