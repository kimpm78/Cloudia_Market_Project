import { useEffect, useMemo, useState } from 'react';
import { filterItemBySelectedFilters } from '../utils/FilterUtils';

export default function useProductPage({
  items,
  categoryGroupLabels,
  itemsPerPage = 30,
  showPagination = true,
  limit = null,
  onTotalChange,
  topRef,
  scrollOffset = 120,
}) {
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedFilters, setSelectedFilters] = useState([]);
  const [keyword, setKeyword] = useState('');

  const effectiveFilters = useMemo(() => {
    const filters = new Set(selectedFilters);

    items.forEach((item) => {
      categoryGroupLabels.forEach((group) => {
        if (selectedFilters.includes(group)) {
          if (item.categoryGroupName === group) {
            filters.add(item.categoryGroupName);
          }
        }
      });
    });

    return Array.from(filters);
  }, [items, categoryGroupLabels, selectedFilters]);

  const filteredItems = useMemo(() => {
    const word = keyword.replace(/\s+/g, '').toLowerCase();
    return items.filter((item) => {
      const name = (item.productName ?? item.name ?? '').replace(/\s+/g, '').toLowerCase();
      const matchKeyword = word === '' || name.includes(word);
      return matchKeyword && filterItemBySelectedFilters(item, effectiveFilters);
    });
  }, [items, keyword, effectiveFilters]);

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

  const resetFilters = () => {
    setSelectedFilters([]);
    setKeyword('');
    setCurrentPage(1);
  };

  const handleKeywordChange = (value) => {
    setKeyword(value ?? '');
    setCurrentPage(1);
  };

  const handlePageClick = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
      setTimeout(() => {
        if (topRef?.current) {
          const offsetTop = topRef.current.getBoundingClientRect().top + window.scrollY - scrollOffset;
          window.scrollTo({ top: offsetTop, behavior: 'smooth' });
        }
      }, 0);
    }
  };

  return {
    currentPage,
    setCurrentPage,
    totalPages,
    paginatedItems,
    filteredItems,
    selectedFilters,
    setSelectedFilters,
    keyword,
    setKeyword,
    resetFilters,
    handleKeywordChange,
    handlePageClick,
  };
}
