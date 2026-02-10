const CM_Pagination = ({ currentPage, totalPages, onPageChange }) => {
  const handleClick = (page) => {
    if (page >= 1 && page <= totalPages) {
      onPageChange(page);
    }
  };

  return (
    <div className="d-flex justify-content-center gap-2 my-4">
      <button
        className="btn btn-outline-secondary"
        disabled={currentPage === 1}
        onClick={() => handleClick(currentPage - 1)}
      >
        <i className="bi bi-caret-left"></i>
      </button>

      {Array.from({ length: totalPages }).map((_, idx) => {
        const page = idx + 1;
        return (
          <button
            key={page}
            className={`btn ${
              currentPage === page ? 'btn-primary text-white' : 'btn-outline-secondary'
            }`}
            onClick={() => handleClick(page)}
          >
            {page}
          </button>
        );
      })}

      <button
        className="btn btn-outline-secondary"
        disabled={currentPage === totalPages}
        onClick={() => handleClick(currentPage + 1)}
      >
        <i className="bi bi-caret-right"></i>
      </button>
    </div>
  );
};

export default CM_Pagination;