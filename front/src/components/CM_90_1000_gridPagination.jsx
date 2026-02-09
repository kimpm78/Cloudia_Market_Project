import '../styles/CM_90_1000_grid.css';

export default function CM_90_1011_gridPagination({
  currentPage,
  totalPages,
  pageGroup,
  pagesPerGroup,
  goToPage,
  goToPrevGroup,
  goToNextGroup,
}) {
  const startPage = pageGroup * pagesPerGroup;
  const endPage = Math.min(startPage + pagesPerGroup, totalPages);
  const pagesCount = Math.max(0, endPage - startPage);

  return (
    <div className="pagination-container">
      <button
        onClick={() => currentPage > 0 && goToPage(currentPage - 1)}
        className={`nav-button ${currentPage === 0 ? 'disabled' : ''}`}
        disabled={currentPage === 0}
      >
        ◀
      </button>

      <button
        onClick={goToPrevGroup}
        className={`group-button ${pageGroup === 0 ? 'disabled' : ''}`}
        disabled={pageGroup === 0}
      >
        «
      </button>

      {[...Array(pagesCount)].map((_, i) => {
        const pageNumber = startPage + i;
        return (
          <button
            key={pageNumber}
            onClick={() => goToPage(pageNumber)}
            className={`page-button ${currentPage === pageNumber ? 'active' : ''}`}
          >
            {pageNumber + 1}
          </button>
        );
      })}

      <button
        onClick={goToNextGroup}
        className={`group-button ${
          pageGroup >= Math.floor((totalPages - 1) / pagesPerGroup) ? 'disabled' : ''
        }`}
        disabled={pageGroup >= Math.floor((totalPages - 1) / pagesPerGroup)}
      >
        »
      </button>

      <button
        onClick={() => currentPage + 1 < totalPages && goToPage(currentPage + 1)}
        className={`nav-button ${currentPage === totalPages - 1 ? 'disabled' : ''}`}
        disabled={currentPage === totalPages - 1}
      >
        ▶
      </button>
    </div>
  );
}
