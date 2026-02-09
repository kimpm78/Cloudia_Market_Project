export default function CM_01_1015_Filter({
  filters,
  handleFilterChange,
  handleSearch,
  statusOptions,
}) {
  // 연도 생성 (현재 연도 기준)
  const currentYear = new Date().getFullYear();
  const years = [
    { value: '', name: '전부' },
    ...Array.from({ length: 5 }, (_, i) => {
      const year = currentYear - i;
      return { value: String(year), name: `${year}년` };
    }),
  ];

  // 월 생성 (1~12월)
  const months = [
    { value: '', name: '전부' },
    ...Array.from({ length: 12 }, (_, i) => ({
      value: String(i + 1).padStart(2, '0'),
      name: `${i + 1}월`,
    })),
  ];

  return (
    <div className="bg-light p-4 rounded mb-4">
      <div className="row g-3 align-items-center">
        {/* 1. 주문 연도 */}
        <div className="col-auto d-flex align-items-center">
          <label htmlFor="year" className="form-label me-2 mb-0 fw-bold">
            주문 연도
          </label>
          <select
            id="year"
            className="form-select"
            value={filters.year}
            onChange={handleFilterChange}
            style={{ minWidth: '100px' }}
          >
            {years.map((y) => (
              <option key={y.value} value={y.value}>
                {y.name}
              </option>
            ))}
          </select>
        </div>

        {/* 주문 월 */}
        <div className="col-auto d-flex align-items-center">
          <select
            id="month"
            className="form-select"
            value={filters.month}
            onChange={handleFilterChange}
            style={{ minWidth: '90px' }}
          >
            {months.map((m) => (
              <option key={m.value} value={m.value}>
                {m.name}
              </option>
            ))}
          </select>
        </div>

        {/* 진행 상태 (반품/교환) */}
        <div className="col-auto d-flex align-items-center ms-3">
          <label htmlFor="status" className="form-label me-2 mb-0 fw-bold">
            진행 상태
          </label>
          <select
            id="status"
            className="form-select"
            value={filters.status}
            onChange={handleFilterChange}
            style={{ minWidth: '120px' }}
          >
            {statusOptions &&
              statusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.name}
                </option>
              ))}
          </select>
        </div>

        {/* 상품명 검색 */}
        <div className="col-auto d-flex align-items-center ms-3 flex-grow-1">
          <label htmlFor="keyword" className="form-label me-2 mb-0 fw-bold">
            상품명
          </label>
          <input
            type="text"
            id="keyword"
            className="form-control"
            placeholder="상품명 검색"
            value={filters.keyword}
            onChange={handleFilterChange}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
        </div>

        {/* 검색 버튼 */}
        <div className="col-auto">
          <button className="btn btn-primary px-4" onClick={handleSearch}>
            검색
          </button>
        </div>
      </div>
    </div>
  );
}
