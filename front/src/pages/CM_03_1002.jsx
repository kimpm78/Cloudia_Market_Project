import { useEffect, useMemo, useRef, useState } from 'react';
import axiosInstance from '../services/axiosInstance';

import CM_99_1011_filterSidebar from '../components/CM_99_1011_filterSidebar';
import noImage from '../images/common/CM-NoImage.png';
import { getCategoryGroupsForPage } from '../utils/FilterUtils';
import useCartMeta from '../hooks/useCartMeta';
import useProductPage from '../hooks/useProductPage';
import { filterItemsForPage } from '../utils/productPageFilters';
import ProductCardBasic from '../components/CM_03_1000_ProductCardBasic';
import { normalizeCartAwareStock, isSoldOutBasic } from '../utils/productInventory';
import CMMessage from '../constants/CMMessage';
import { normalizeImageUrl } from '../utils/htmlContent';

import '../styles/CM_03_1000.css';

export default function CM_03_1002({ showFilter = true }) {
  const itemsPerPage = 30;
  const [rawItems, setRawItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const topRef = useRef(null);
  const cartMeta = useCartMeta();

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
    axiosInstance
      .get(`${import.meta.env.VITE_API_BASE_URL}/guest/characters`)
      .then((res) => {
        const productList = Array.isArray(res.data?.resultList) ? res.data.resultList : [];
        setRawItems(productList);
        setLoading(false);
      })
      .catch((err) => {
        console.error('신상품 불러오기 실패:', err?.response?.data?.message || err.message);

        const status = err?.response?.status;
        const currentPath = window.location.pathname;
        const navigate = (path) => {
          if (currentPath !== path) {
            window.location.assign(path);
          }
        };

        if (status === 401) navigate('/401');
        else if (status === 403) navigate('/403');
        else if (status === 404) navigate('/404');
        else if (status >= 500) navigate('/500');
      });
  }, []);

  const allItems = useMemo(() => normalizeCartAwareStock(rawItems, cartMeta), [rawItems, cartMeta]);
  const categoryGroupLabels = getCategoryGroupsForPage('キャラクター');

  const sourceItems = filterItemsForPage(allItems, 'キャラクター');
  const {
    currentPage,
    selectedFilters,
    setSelectedFilters,
    keyword,
    resetFilters,
    handleKeywordChange,
    handlePageClick,
    totalPages,
    paginatedItems,
  } = useProductPage({
    items: sourceItems,
    categoryGroupLabels,
    itemsPerPage,
    topRef,
  });

  const handleReset = () => {
    resetFilters();
  };

  const handleFilterChange = (filter) => {
    setSelectedFilters((prev) =>
      prev.includes(filter) ? prev.filter((f) => f !== filter) : [...prev, filter]
    );
  };

  return (
    <>
      <h1 className="m-5" ref={topRef}>キャラクター</h1>
      <div className="container-fluid my-4">
        <div className="row justify-content-center">
          {showFilter && (
            <div className="col-12 col-md-3 mb-3 mb-md-0">
              <CM_99_1011_filterSidebar
                pageName="キャラクター"
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
                  const imageUrl = getImageUrl(item.thumbnailUrl || item.image);
                  const soldOut = isSoldOutBasic(item);
                  const noteHtml = item.productnote
                    ? { __html: normalizeImageUrl(item.productnote) }
                    : null;
                  return (
                    <ProductCardBasic
                      key={item.productId ?? item.id ?? item.productCode}
                      item={item}
                      imageUrl={imageUrl}
                      soldOut={soldOut}
                      noteHtml={noteHtml}
                    />
                  );
                })}
              </div>
            )}

            {paginatedItems.length > 0 && (
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
