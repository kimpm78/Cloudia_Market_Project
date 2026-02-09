import '../styles/CM_99_1012_searchBar.css';

export default function CM_99_1012_searchBar({
  searchTypes,
  searchType,
  setSearchType,
  searchKeyword,
  setSearchKeyword,
  onSearch = () => {},
}) {
  return (
    <div className="d-flex justify-content-center gap-2 mb-5 mt-5 align-items-center cm-searchbar">
      <select
        className="form-select w-auto cm-search-select"
        value={searchType}
        onChange={(e) => setSearchType(Number(e.target.value))}
      >
        {searchTypes.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      <input
        className="form-control w-25 cm-search-input"
        placeholder="検索を入力してください"
        value={searchKeyword}
        onChange={(e) => setSearchKeyword(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter') {
            onSearch();
          }
        }}
      />
      <button className="btn btn-primary cm-search-button" onClick={onSearch}>
        검색
      </button>
    </div>
  );
}
