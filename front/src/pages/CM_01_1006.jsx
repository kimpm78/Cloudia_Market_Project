import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';

import { getRequest } from '../services/axiosInstance';
import CM_SearchBar from '../components/CM_99_1012_searchBar';
import CM_Pagination from '../components/CM_99_1013_pagination';

const SEARCH_OPTIONS = [
  { value: 0, label: 'すべて' },
  { value: 1, label: 'タイトル' },
];

const maskId = (id) => {
  if (id.length <= 2) return '**';
  return id.slice(0, -2) + '**';
};

const dateFormatter = (params) => {
  const date = params.value;
  if (!date) return '';
  if (Array.isArray(date)) {
    const [year, month, day] = date;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }
  return String(date).split('T')[0];
};

export default function CM_01_1006() {
  const navigate = useNavigate();

  const [inquiries, setInquiries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [searchType, setSearchType] = useState(0);
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

  const fetchInquiries = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getRequest('/user/mypage/inquiries');
      if (Array.isArray(data)) {
        setInquiries(data);
      } else {
        setInquiries([]);
      }
    } catch (error) {
      console.error('お問い合わせ履歴の取得に失敗:', error);
      setInquiries([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchInquiries();
  }, [fetchInquiries]);

  useEffect(() => {
    if (gridApi) {
      if (loading) {
        gridApi.showLoadingOverlay(); // 読み込み中表示
      } else {
        if (inquiries.length === 0) {
          gridApi.showNoRowsOverlay(); // データなし表示
        } else {
          gridApi.hideOverlay(); // データありならオーバーレイ非表示
        }
      }
    }
  }, [loading, inquiries, gridApi]);

  const filteredInquiries = inquiries.filter((inquiry) => {
    if (!activeKeyword) return true;
    const keyword = activeKeyword.toLowerCase();
    const title = (inquiry.title || '').toLowerCase();
    const loginId = (inquiry.loginId || '').toLowerCase();
    const writerName = (inquiry.writerName || '').toLowerCase();

    if (searchType === 1) {
      return title.includes(keyword);
    }
    return title.includes(keyword) || loginId.includes(keyword) || writerName.includes(keyword);
  });

  const totalPages = Math.ceil(filteredInquiries.length / pageSize);
  const paginatedInquiries = filteredInquiries.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  // AG Grid 設定
  const onGridReady = (params) => {
    setGridApi(params.api);
  };

  const columnDefs = [
    {
      headerName: '区分',
      field: 'statusLabel',
      width: 120,
      cellRenderer: (params) => {
        const isAnswered = params.data.statusValue === 2 || params.data.statusCode === 'ANSWERED';
        return (
          <span className={`fw-bold ${isAnswered ? 'text-primary' : 'text-danger'}`}>
            {params.value || '回答待ち'}
          </span>
        );
      },
    },
    {
      headerName: 'タイトル',
      field: 'title',
      flex: 1,
      cellRenderer: (params) => (
        <Link
          to={`/mypage/inquiries/${params.data.inquiryId}`}
          className="text-decoration-none text-dark"
        >
          {Number(params.data.isPrivate) === 1 && <i className="bi bi-file-lock-fill me-2"></i>}
          {params.value}
        </Link>
      ),
    },
    {
      headerName: '作成者',
      field: 'loginId',
      width: 150,
      valueFormatter: (params) => maskId(params.value),
    },
    {
      headerName: '作成日',
      field: 'createdAt',
      width: 150,
      valueFormatter: dateFormatter,
    },
  ];

  const handleSearch = () => {
    setActiveKeyword(searchKeyword);
    setCurrentPage(1);
  };

  const onRowClicked = (event) => {
    navigate(`/mypage/inquiries/${event.data.inquiryId}`);
  };

  return (
    <div className="inquiry-container mt-2">
      <div className="d-flex justify-content-between align-items-end pb-4 mb-4 border-bottom">
        <h3 className="fw-bolder m-0">お問い合わせ履歴</h3>
        <button className="btn btn-primary " onClick={() => navigate('/mypage/inquiries/write')}>
          <i className="bi bi-pencil-square me-1"></i>
          お問い合わせ
        </button>
      </div>

      <div className="ag-theme-alpine w-100" style={{ height: '526px' }}>
        <AgGridReact
          rowData={paginatedInquiries}
          columnDefs={columnDefs}
          onGridReady={onGridReady}
          onRowClicked={onRowClicked}
          headerHeight={45}
          rowHeight={48}
          overlayLoadingTemplate={'<span class="loading-message">読み込み中...</span>'}
          overlayNoRowsTemplate={'<span class="no-rows">お問い合わせ履歴がありません。</span>'}
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