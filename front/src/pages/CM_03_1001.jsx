import { useState, useEffect, useRef } from 'react';
import axiosInstance from '../services/axiosInstance';

import noImage from '../images/common/CM-NoImage.png';
import CM_99_1011_filterSidebar from '../components/CM_99_1011_filterSidebar';
import CMMessage from '../constants/CMMessage';
import '../styles/CM_03_1001.css';
import useCartMeta from '../hooks/useCartMeta';
import useProductPage from '../hooks/useProductPage';
import { hasReservationClosed } from '../utils/productStatus';
import { isReservationProduct } from '../utils/productPageFilters';
import ProductCardBasic from '../components/CM_03_1000_ProductCardBasic';
import { normalizeImageUrl } from '../utils/htmlContent';

const PRODUCT_STATUS_MAP = {
  1: '販売中',
  2: 'SOLD OUT',
  3: '予約中',
  4: '予約終了',
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
          // コード一覧（003）基準でのみ売り切れ / 締切判定（2=売り切れ、4=予約締切）
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
          console.error('予約商品の取得に失敗しました:', err?.response?.data?.message || err.message);
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
        const showClosedLabelJa = '締切済み商品を表示';
        const showClosedLabelKo = '受付終了商品を表示';
        const excludeClosedLabelJa = '予約締切商品を除外';
        const excludeClosedLabelKo = '予約終了商品を除く';

        const normalizeFilterLabel = (label) => {
          if (label === showClosedLabelKo) return showClosedLabelJa;
          if (label === excludeClosedLabelKo) return excludeClosedLabelJa;
          return label;
        };

        const normalized = normalizeFilterLabel(filter);
        const isActive = prev.map(normalizeFilterLabel).includes(normalized);
        if (isActive) {
          return prev.filter((f) => normalizeFilterLabel(f) !== normalized);
        }
        if (filter === showClosedLabelJa || filter === showClosedLabelKo) {
          return [...prev.filter((f) => f !== excludeClosedLabelJa && f !== excludeClosedLabelKo), showClosedLabelJa];
        }
        if (filter === excludeClosedLabelJa || filter === excludeClosedLabelKo) {
          return [...prev.filter((f) => f !== showClosedLabelJa && f !== showClosedLabelKo), excludeClosedLabelJa];
        }
        return [...prev, normalized];
      });
    };

  const categoryGroupLabels = ['予約商品'];
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
    return <div className="text-center mt-5">{CMMessage.MSG_ERR_005('商品')}</div>;
  }

  return (
    <>
      <h1 className="m-5" ref={topRef}>予約商品</h1>
      <div className="container-fluid my-4">
        <div className="row justify-content-center">
          {showFilter && (
            <div className="col-12 col-md-3 mb-3 mb-md-0">
              <CM_99_1011_filterSidebar
                pageName="予約商品"
                selectedFilters={selectedFilters}
                onFilterChange={handleFilterChange}
                onReset={handleReset}
                keyword={keyword}
                onKeywordChange={handleKeywordChange}
                categoryLabel="発売月"
              />
            </div>
          )}
          <div className="col-12 col-md-9">
            {paginatedItems.length === 0 ? (
              keyword.trim().length > 0 ? (
                <div className="text-center mt-5">
                  <h5 style={{ color: 'red' }}>{CMMessage.MSG_EMPTY_001}</h5>
                  <p className="text-muted">{CMMessage.MSG_EMPTY_002}</p>
                </div>
              ) : (
                <div className="text-center mt-5">
                  <h5 style={{ color: 'red' }}>{CMMessage.MSG_EMPTY_005}</h5>
                  <p className="text-muted">{CMMessage.MSG_EMPTY_006}</p>
                </div>
              )
            ) : (
              <div className="row row-cols-2 row-cols-md-5 g-4">
                {paginatedItems.map((item) => {
                  const status = getProductStatus(item);
                  const itemId = item.productId ?? item.id ?? item.productCode;
                  const noteHtml = item.productnote
                    ? { __html: normalizeImageUrl(item.productnote) }
                    : null;

                  return (
                    <ProductCardBasic
                      key={itemId}
                      item={item}
                      imageUrl={getImageUrl(item.thumbnailUrl || item.image)}
                      soldOut={status.soldOut}
                      statusCode={status.code}
                      reservationClosed={status.reservationClosed}
                      noteHtml={noteHtml}
                    />
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
