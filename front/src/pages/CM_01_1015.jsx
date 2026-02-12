import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';

import { getRequest } from '../services/axiosInstance';
import CM_SearchBar from '../components/CM_99_1012_searchBar';
import CM_Pagination from '../components/CM_99_1013_pagination';

const SEARCH_OPTIONS = [
  { value: 'all', name: '全体' },
  { value: 'orderNo', name: '注文番号' },
  { value: 'productName', name: '商品名' },
];

const dateFormatter = (params) => {
  const date = params.value;
  if (!date) return '-';
  return String(date).split('T')[0];
};

export default function CM_01_1015() {
  const navigate = useNavigate();

  const [returns, setReturns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [searchType, setSearchType] = useState('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeKeyword, setActiveKeyword] = useState('');
  const [gridApi, setGridApi] = useState(null);
  const pageSize = 10;

  useEffect(() => {
    const gridCss = document.createElement('link');
    gridCss.rel = 'stylesheet';
    gridCss.href = 'https://cdn.jsdelivr.net/npm/ag-grid-community/styles/ag-grid.css';
    gridCss.id = 'ag-grid-css-global';
    document.head.appendChild(gridCss);

    const themeCss = document.createElement('link');
    themeCss.rel = 'stylesheet';
    themeCss.href = 'https://cdn.jsdelivr.net/npm/ag-grid-community/styles/ag-theme-alpine.css';
    themeCss.id = 'ag-theme-css-global';
    document.head.appendChild(themeCss);

    return () => {
      const gridCssLink = document.getElementById('ag-grid-css-global');
      const themeCssLink = document.getElementById('ag-theme-css-global');
      if (gridCssLink) document.head.removeChild(gridCssLink);
      if (themeCssLink) document.head.removeChild(themeCssLink);
    };
  }, []);

  const fetchReturns = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getRequest('/user/returns');
      console.log('サーバー応答データ:', data);
      setReturns(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('取得失敗:', error);
      setReturns([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchReturns();
  }, [fetchReturns]);

  // 検索およびページングロジック ---
  const filteredReturns = returns.filter((item) => {
    if (!activeKeyword) return true;
    const keyword = activeKeyword.toLowerCase();
    const orderNo = item.orderNo?.toLowerCase() || '';
    const productName = item.productName?.toLowerCase() || '';
    if (searchType === 'orderNo') return orderNo.includes(keyword);
    if (searchType === 'productName') return productName.includes(keyword);
    return orderNo.includes(keyword) || productName.includes(keyword);
  });

  const totalPages = Math.ceil(filteredReturns.length / pageSize);
  const paginatedReturns = filteredReturns.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  const onGridReady = (params) => {
    setGridApi(params.api);
  };

  const columnDefs = [
    {
      headerName: '処理状態',
      field: 'returnStatusName',
      width: 120,
      cellRenderer: (params) => {
        const isComplete = params.value?.includes('完了') || params.value?.includes('완료');
        return (
          <span className={`fw-bold ${isComplete ? 'text-primary' : 'text-danger'}`}>
            {params.value || '処理中'}
          </span>
        );
      },
    },
    {
      headerName: '注文番号',
      field: 'orderNo',
      width: 160,
    },
    {
      headerName: '商品名',
      field: 'productName',
      flex: 1,
    },
    {
      headerName: '申請日',
      field: 'requestedAt',
      width: 150,
      valueFormatter: dateFormatter,
    },
    {
      headerName: '完了日',
      field: 'completedAt',
      width: 150,
      valueFormatter: dateFormatter,
    },
  ];

  const handleSearch = () => {
    setActiveKeyword(searchKeyword);
    setCurrentPage(1);
  };

  return (
    <div className="container mt-2">
      <div className="d-flex justify-content-between align-items-end pb-4 mb-4 border-bottom">
        <h2 className="fw-bolder mt-3">交換・返品履歴</h2>
        <button className="btn btn-primary" onClick={() => navigate('/mypage/returns/write')}>
          <i className="bi bi-pencil-square me-1"></i>
          申請する
        </button>
      </div>

      <div className="ag-theme-alpine w-100" style={{ height: '526px' }}>
        <AgGridReact
          rowData={paginatedReturns}
          columnDefs={columnDefs}
          onGridReady={onGridReady}
          onRowClicked={(e) => navigate(`/mypage/returns/${e.data.returnId}`)}
          headerHeight={45}
          rowHeight={48}
          overlayLoadingTemplate={'<span class="loading-message">読み込み中...</span>'}
          overlayNoRowsTemplate={'<span class="no-rows">履歴がありません。</span>'}
          theme="legacy"
        />
      </div>

      <CM_Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      <CM_SearchBar
        searchTypes={SEARCH_OPTIONS}
        searchType={searchType}
        setSearchType={setSearchType}
        searchKeyword={searchKeyword}
        setSearchKeyword={setSearchKeyword}
        onSearch={handleSearch}
      />
    </div>
  );
}
