export default function CM_01_1015_Filter({
  filters,
  handleFilterChange,
  handleSearch,
  statusOptions,
}) {
  // 年度生成（現在の年基準）
  const currentYear = new Date().getFullYear();
  const years = [
    { value: '', name: 'すべて' },
    ...Array.from({ length: 5 }, (_, i) => {
      const year = currentYear - i;
      return { value: String(year), name: `${year}年` };
    }),
  ];

  // 月生成（1〜12月）
  const months = [
    { value: '', name: 'すべて' },
    ...Array.from({ length: 12 }, (_, i) => ({
      value: String(i + 1).padStart(2, '0'),
      name: `${i + 1}月`,
    })),
  ];

  return (
    <div className="bg-light p-4 rounded mb-4">
      <div className="row g-3 align-items-center">
        {/* 1. 注文年 */}
        <div className="col-auto d-flex align-items-center">
          <label htmlFor="year" className="form-label me-2 mb-0 fw-bold">
            注文年
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

        {/* 注文月 */}
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

        {/* 進行状態（返品/交換） */}
        <div className="col-auto d-flex align-items-center ms-3">
          <label htmlFor="status" className="form-label me-2 mb-0 fw-bold">
            進行状態
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

        {/* 商品名検索 */}
        <div className="col-auto d-flex align-items-center ms-3 flex-grow-1">
          <label htmlFor="keyword" className="form-label me-2 mb-0 fw-bold">
            商品名
          </label>
          <input
            type="text"
            id="keyword"
            className="form-control"
            placeholder="商品名検索"
            value={filters.keyword}
            onChange={handleFilterChange}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
        </div>

        {/* 検索ボタン */}
        <div className="col-auto">
          <button className="btn btn-primary px-4" onClick={handleSearch}>
            検索
          </button>
        </div>
      </div>
    </div>
  );
}
