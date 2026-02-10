const ALL_OPTION = { value: '', name: 'すべて' };
const currentYear = new Date().getFullYear();
export const YEAR_OPTIONS = [
  ALL_OPTION,
  ...Array.from({ length: 5 }, (_, i) => ({
    value: String(currentYear - i),
    name: `${currentYear - i}年`,
  })),
];
export const MONTH_OPTIONS = [
  ALL_OPTION,
  ...Array.from({ length: 12 }, (_, i) => ({
    value: String(i + 1).padStart(2, '0'),
  })),
];

export default function CM_01_1005_OrderSearchFilter({
  filters,
  handleFilterChange,
  handleSearch,
  loading,
  statusOptions,
  paymentOptions,
}) {
  const onKeyDown = (e) => {
    if (e.key === 'Enter') handleSearch();
  };

  return (
    <div className="border p-4 mb-4 bg-light rounded shadow-sm">
      <div className="row g-3 align-items-center mb-3">
        <div className="col-auto">
          <label className="fw-bold mb-0" htmlFor="year">
            注文期間
          </label>
        </div>
        <div className="col-auto" style={{ width: '130px' }}>
          <select
            className="form-select"
            id="year"
            value={filters.year}
            onChange={handleFilterChange}
          >
            {YEAR_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.name}
              </option>
            ))}
          </select>
        </div>
        <div className="col-auto" style={{ width: '110px' }}>
          <select
            className="form-select"
            id="month"
            value={filters.month}
            onChange={handleFilterChange}
          >
            {MONTH_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.name}
              </option>
            ))}
          </select>
        </div>

        <div className="col-auto ms-4">
          <label className="fw-bold mb-0" htmlFor="status">
            現在のステータス
          </label>
        </div>
        <div className="col-auto" style={{ width: '150px' }}>
          <select
            className="form-select"
            id="orderStatusValue"
            value={filters.orderStatusValue}
            onChange={handleFilterChange}
          >
            {statusOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="row g-3 align-items-center">
        {/* 支払い方法 */}
        <div className="col-auto">
          <label className="fw-bold mb-0" htmlFor="paymentMethod">
            支払い方法
          </label>
        </div>
        <div className="col-auto" style={{ width: '130px' }}>
          <select
            className="form-select"
            id="paymentMethod"
            value={filters.paymentMethod}
            onChange={handleFilterChange}
          >
            {paymentOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.name}
              </option>
            ))}
          </select>
        </div>

        <div className="col-auto ms-4">
          <label className="fw-bold mb-0" htmlFor="keyword">
            商品名
          </label>
        </div>
        <div className="col" style={{ maxWidth: '400px' }}>
          <div className="input-group">
            <input
              type="text"
              className="form-control"
              id="keyword"
              placeholder="注文した商品名を入力してください"
              value={filters.keyword}
              onChange={handleFilterChange}
              onKeyDown={onKeyDown}
            />
            <button
              className="btn btn-primary px-4 fw-bold"
              type="button"
              onClick={handleSearch}
              disabled={loading}
            >
              {loading ? (
                <span className="spinner-border spinner-border-sm me-2"></span>
              ) : (
                <i className="bi bi-search me-2"></i>
              )}
              検索
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
