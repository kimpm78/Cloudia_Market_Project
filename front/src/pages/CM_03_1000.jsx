import { useState, useEffect, useRef } from 'react';
import axiosInstance from '../services/axiosInstance';
import { Link } from 'react-router-dom';

import noImage from '../images/common/CM-NoImage.png';
import CM_99_1011_filterSidebar from '../components/CM_99_1011_filterSidebar';
import { filterItemBySelectedFilters, getCategoryGroupsForPage } from '../utils/FilterUtils';
import { hasReservationClosed } from '../utils/productStatus';
import CMMessage from '../constants/CMMessage';
import '../styles/CM_03_1000.css';
import { loadCartMeta, getEventName } from '../utils/cartMetaStorage';

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

export default function CM_03_1000({
  showFilter = true,
  showPagination = true,
  limit = null,
  onTotalChange = null,
}) {
  const itemsPerPage = 30;
  const [allItems, setAllItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedFilters, setSelectedFilters] = useState([]);
  const [keyword, setKeyword] = useState('');
  const topRef = useRef(null);
  const [cartMeta, setCartMeta] = useState(() => loadCartMeta());

  const hasReservationFlag = (item) => {
    if (!item || typeof item !== 'object') return false;
    const codeValue = normalizeStatusCode(item.codeValue ?? item.code_value);
    if (codeValue) {
      return isReservationStatus(codeValue);
    }
    const deadline = item.reservationDeadline ?? item.reservation_deadline ?? '';
    if (typeof deadline === 'string' && deadline.trim().length > 0) {
      return true;
    }
    const groupNames = (item.categoryGroupName ?? '')
      .split(',')
      .map((v) => v.trim())
      .filter((v) => v.length > 0);

    return Boolean(
      item.isReservation || item.reservationOnly || groupNames.some((name) => name.includes('예약'))
    );
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

  useEffect(() => {
    if (typeof window === 'undefined') {
      return () => {};
    }
    const eventName = getEventName();
    const handleMetaUpdate = (event) => {
      if (event?.detail && typeof event.detail === 'object') {
        setCartMeta(event.detail);
      } else {
        setCartMeta(loadCartMeta());
      }
    };
    window.addEventListener(eventName, handleMetaUpdate);
    return () => window.removeEventListener(eventName, handleMetaUpdate);
  }, []);

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        const res = await axiosInstance.get(
          `${import.meta.env.VITE_API_BASE_URL}/guest/new-product`
        );
        const productList = Array.isArray(res.data?.resultList) ? res.data.resultList : [];
        const normalized = productList.map((item) => {
          const available = resolveAvailableQty(item);
          const reserved = getReservedFromMeta(item);
          const effective = available === null ? null : Math.max(available - reserved, 0);
          const soldOut = item?.isSoldOut === true || (effective !== null && effective <= 0);
          return {
            ...item,
            availableQty: effective ?? item.availableQty,
            isSoldOut: soldOut,
          };
        });
        setAllItems(normalized);
      } catch (err) {
        console.error('신상품 불러오기 실패:', err?.response?.data?.message || err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const handleReset = () => {
    setSelectedFilters([]);
    setKeyword('');
    setCurrentPage(1);
  };

  const handleKeywordChange = (value) => {
    setKeyword(value ?? '');
    setCurrentPage(1);
  };

  const handleFilterChange = (filter) => {
    setSelectedFilters((prev) =>
      prev.includes(filter) ? prev.filter((f) => f !== filter) : [...prev, filter]
    );
  };
  const handlePageClick = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
      setTimeout(() => {
        if (topRef.current) {
          const offsetTop = topRef.current.getBoundingClientRect().top + window.scrollY - 120;
          window.scrollTo({ top: offsetTop, behavior: 'smooth' });
        }
      }, 0);
    }
  };

  const getEffectiveFilters = (selectedFilters, categoryGroups, allItems) => {
    const filters = new Set(selectedFilters);

    allItems.forEach((item) => {
      categoryGroups.forEach((group) => {
        if (selectedFilters.includes(group)) {
          if (item.categoryGroupName === group) {
            filters.add(item.categoryGroupName);
          }
        }
      });
    });

    return Array.from(filters);
  };

  const categoryGroupLabels = getCategoryGroupsForPage('신상품');

  const getFilteredItems = () => {
    const effectiveFilters = getEffectiveFilters(selectedFilters, categoryGroupLabels, allItems);
    return allItems.filter((item) => {
      const name = (item.productName ?? item.name ?? '').replace(/\s+/g, '').toLowerCase();
      const word = keyword.replace(/\s+/g, '').toLowerCase();
      const matchKeyword = word === '' || name.includes(word);
      return matchKeyword && filterItemBySelectedFilters(item, effectiveFilters);
    });
  };

  const filteredItems = getFilteredItems();
  const limitValue = Number.isFinite(limit) ? Math.max(0, limit) : null;

  const totalPages = Math.ceil(filteredItems.length / itemsPerPage);

  const paginatedItems = showPagination
    ? filteredItems.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)
    : limitValue === null
      ? filteredItems
      : filteredItems.slice(0, limitValue);

  useEffect(() => {
    if (typeof onTotalChange === 'function') {
      onTotalChange(filteredItems.length);
    }
  }, [filteredItems.length, onTotalChange]);

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

  const resolveAvailableQty = (item) => {
    if (!item || typeof item !== 'object') return null;
    const candidates = [
      item.availableQty,
      item.availableqty,
      item.available_quantity,
      item.quantity,
      item.stock,
    ];
    for (const candidate of candidates) {
      if (typeof candidate === 'number' && !Number.isNaN(candidate)) {
        return candidate;
      }
    }
    return null;
  };

  const getReservedFromMeta = (item) => {
    if (!item || !cartMeta) return 0;
    const candidateKeys = [
      item.productCode,
      item.product_code,
      item.productCodeId,
      item.product_code_id,
      item.productId,
      item.product_id,
      item.id,
      item.id_code,
    ]
      .filter((value) => value !== undefined && value !== null)
      .map((value) => `${value}`.trim())
      .filter((value, index, self) => value.length > 0 && self.indexOf(value) === index);

    for (const key of candidateKeys) {
      const entry = cartMeta?.[key];
      if (entry && typeof entry.quantity === 'number') {
        return entry.quantity;
      }
    }
    return 0;
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

    const userReserved = getReservedFromMeta(item);
    if (available !== null && available - userReserved <= 0) {
      return true;
    }

    return false;
  };

  const getProductStatus = (item) => {
    const explicitCode = normalizeStatusCode(item?.codeValue ?? item?.code_value);
    const reservationClosed = hasReservationClosed(item);
    const soldOutByStock = detectSoldOut(item);
    const reservationFlag = hasReservationFlag(item);

    if (explicitCode) {
      return {
        code: explicitCode,
        label: PRODUCT_STATUS_MAP[explicitCode],
        isReservation: isReservationStatus(explicitCode),
        reservationClosed: explicitCode === '4',
        // 코드일람 기준으로만 품절/마감 처리 (2=품절, 4=예약 마감)
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

  return (
    <>
      <h1 ref={topRef}>신상품</h1>
      <div className="container-fluid my-4">
        <div className="row justify-content-center">
          {showFilter && (
            <div className="col-12 col-md-3 mb-3 mb-md-0">
              <CM_99_1011_filterSidebar
                pageName="신상품"
                selectedFilters={selectedFilters}
                onFilterChange={handleFilterChange}
                onReset={handleReset}
                keyword={keyword}
                onKeywordChange={handleKeywordChange}
              />
            </div>
          )}
          <div className="col-12 col-md-9">
            {paginatedItems.length === 0 ? (
              <div className="text-center mt-5">
                <h5 style={{ color: 'red' }}>{CMMessage.MSG_EMPTY_001}</h5>
                <p className="text-muted">
                  {CMMessage.MSG_EMPTY_002}
                </p>
              </div>
            ) : (
              <div className="row row-cols-2 row-cols-md-5 g-4">
                {paginatedItems.map((item) => {
                  const itemId =
                    item.productId ??
                    item.product_id ??
                    item.id ??
                    item.productCode ??
                    item.product_code;
                  const status = getProductStatus(item);
                  const reservationClosed = status.reservationClosed;
                  const isReservationProduct = status.isReservation;
                  const soldOut = status.soldOut;

                  if (!isReservationProduct) {
                    const overlayText = soldOut
                      ? PRODUCT_STATUS_MAP['2']
                      : status?.label ?? PRODUCT_STATUS_MAP['1'];
                    const card = (
                      <div className="card border-0 text-start rounded overflow-hidden h-100 d-flex flex-column">
                        <div className="position-relative">
                          <img
                            src={getImageUrl(item.thumbnailUrl || item.image)}
                            onError={(e) => (e.currentTarget.src = noImage)}
                            className="product-card-img rounded"
                            alt="상품 이미지"
                            style={{ aspectRatio: '1/1', objectFit: 'cover' }}
                          />
                          {soldOut && (
                            <div className="position-absolute top-0 start-0 w-100 h-100 d-flex flex-column justify-content-center align-items-center bg-light bg-opacity-75 rounded">
                              <div className="fw-bold fs-4 text-dark fst-italic text-uppercase">
                                {overlayText}
                              </div>
                            </div>
                          )}
                        </div>
                        <div className="card-body pt-2 p-0 d-flex flex-column">
                          <p className="mb-1 text-muted small" style={{ fontSize: '12px' }}>
                            {item.brand}
                          </p>
                          {item.categoryGroupName && (
                            <p className="mb-1 text-muted small" style={{ fontSize: '12px' }}>
                              {item.categoryGroupName}
                            </p>
                          )}
                          <div className="fw-bold text-secondary" style={{ fontSize: '0.95rem' }}>
                            {item.productName ?? item.name}
                          </div>
                          <div className="mt-auto">
                            <div className="fw-bold mt-2 item-price-text fs-5">
                              {item.price.toLocaleString()}원
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
                    return (
                      <div className="col d-flex" key={itemId}>
                        <Link
                          to={`/detail/${itemId}`}
                          className="text-decoration-none text-dark h-100 d-flex"
                        >
                          {card}
                        </Link>
                      </div>
                    );
                  }
                  // 예약상품
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
                          className="product-card-img rounded"
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
                            className={`position-absolute bottom-0 w-100 text-white text-center py-2 item-text ${badge.className}`}
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
                          {cardContent}
                        </div>
                      ) : (
                        <Link
                          to={`/detail/${itemId}`}
                          className="text-decoration-none text-dark h-100 d-flex"
                        >
                          {cardContent}
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
