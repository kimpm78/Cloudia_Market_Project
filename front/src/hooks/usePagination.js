import { useState, useEffect } from 'react';

export default function CM_90_1011_usePagination(rowData, itemsPerPage, pagesPerGroup) {
  const [currentPage, setCurrentPage] = useState(0);
  const [pageGroup, setPageGroup] = useState(0);
  const [displayedData, setDisplayedData] = useState([]);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (rowData.length > 0) {
      const total = Math.ceil(rowData.length / itemsPerPage);
      setTotalPages(total);
      setPageGroup(0);
      goToPage(0, rowData, itemsPerPage);
    } else {
      setTotalPages(0);
      setPageGroup(0);
      setCurrentPage(0);
      setDisplayedData([]);
    }
  }, [rowData, itemsPerPage]);

  const goToPage = (page, data = rowData, items = itemsPerPage) => {
    const start = page * items;
    const end = start + items;
    setDisplayedData(data.slice(start, end));
    setCurrentPage(page);
    setPageGroup(Math.floor(page / pagesPerGroup));
  };

  const goToPrevGroup = () => {
    const prevGroup = pageGroup - 1;
    if (prevGroup >= 0) {
      const prevPage = prevGroup * pagesPerGroup;
      setPageGroup(prevGroup);
      goToPage(prevPage);
    }
  };

  const goToNextGroup = () => {
    const nextGroup = pageGroup + 1;
    if (nextGroup <= Math.floor((totalPages - 1) / pagesPerGroup)) {
      const nextPage = nextGroup * pagesPerGroup;
      setPageGroup(nextGroup);
      goToPage(nextPage);
    }
  };

  return {
    currentPage,
    totalPages,
    pageGroup,
    displayedData,
    goToPage,
    goToPrevGroup,
    goToNextGroup,
  };
}
