import { useState, useEffect, useMemo } from 'react';
import { useAuth } from '../contexts/AuthContext.jsx';
import CM_99_1000 from '../components/commonPopup/CM_99_1000.jsx';
import { useNavigate, Link } from 'react-router-dom';

import { AgGridReact } from 'ag-grid-react';
import { ModuleRegistry } from 'ag-grid-community';
import {
  PaginationModule,
  ClientSideRowModelModule,
  LocaleModule,
  RowStyleModule,
} from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

import noImage from '../images/common/CM-NoImage.png';
import { fetchReviews } from '../services/ReviewService.js';
import { formatYearMonthDot } from '../utils/DateFormat.js';
import { SEARCH_TYPE } from '../constants/constants.js';
import CM_SearchBar from '../components/CM_99_1012_searchBar.jsx';
import CM_Pagination from '../components/CM_99_1013_pagination.jsx';
import CMMessage from '../constants/CMMessage';
import '@styles/CM_04_1000.css';

// 画像URLを安全に結合する関数
const buildImageUrl = (rawUrl) => {
  const base = (import.meta.env.VITE_API_BASE_IMAGE_URL || '').replace(/\/$/, '');
  if (!rawUrl) return '';
  // 絶対URLならそのまま使用
  if (rawUrl.startsWith('http://') || rawUrl.startsWith('https://')) {
    return rawUrl;
  }
  if (rawUrl.startsWith('/images/') && base.endsWith('/images')) {
    return base.replace(/\/images$/, '') + rawUrl;
  }
  // rawUrl が / で始まる場合は base + rawUrl
  if (rawUrl.startsWith('/')) {
    return base + rawUrl;
  }
  // それ以外
  return `${base}/${rawUrl}`;
};

ModuleRegistry.registerModules([
  ClientSideRowModelModule,
  PaginationModule,
  RowStyleModule,
  LocaleModule,
]);

export default function CM_04_1000() {
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const [searchType, setSearchType] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeKeyword, setActiveKeyword] = useState('');
  const [allReviews, setAllReviews] = useState([]);
  const [open1000, setOpen1000] = useState(false);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [isMobile, setIsMobile] = useState(false);
  const pageSize = 5;

  useEffect(() => {
    fetchReviews()
      .then((list) => setAllReviews(list))
      .catch((err) => {
        console.error('レビューの読み込みに失敗しました:', err?.response?.data?.message || err.message);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth <= 767);
    onResize();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const LoadingMessage = () => (
    <div className="container-fluid px-5" style={{ marginTop: '50px', textAlign: 'center' }}>
    </div>
  );

  const mergedReviews = useMemo(() => allReviews, [allReviews]);

  const filtered = useMemo(() => {
    const keyword = activeKeyword.toLowerCase();
    return mergedReviews.filter((r) => {
      if (searchType === 2) return r.title.toLowerCase().includes(keyword); // タイトル
      if (searchType === 3) return r.content.toLowerCase().includes(keyword); // 内容
      if (searchType === 4) return (r.createdBy ?? '').toLowerCase().includes(keyword); // 作成者
      return r.title.toLowerCase().includes(keyword) || r.content.toLowerCase().includes(keyword); // タイトル+内容
    });
  }, [mergedReviews, activeKeyword, searchType]);

  const totalPages = useMemo(() => Math.ceil(filtered.length / pageSize) || 1, [filtered.length]);
  const paginated = useMemo(
    () => filtered.slice((currentPage - 1) * pageSize, currentPage * pageSize),
    [filtered, currentPage]
  );

  const columnDefs = useMemo(() => {
    const imageWidth = isMobile ? 160 : 220;
    const imageStyle = {
      width: '100%',
      height: '100%',
    };
    const frameStyle = {
      width: isMobile ? '140px' : '190px',
      height: isMobile ? '96px' : '108px',
      borderRadius: '6px',
      overflow: 'hidden',
      backgroundColor: '#f8f9fa',
      border: '1px solid #eceff1',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    };

    const imageColumn = {
      headerName: '',
      field: 'imageUrl',
      width: imageWidth,
      suppressMovable: true,
      cellStyle: {
        padding: 0,
        margin: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      },
      cellRenderer: (params) => {
        const rawUrl = params.data.imageUrl || params.data.image_url;
        const imageUrl = rawUrl ? buildImageUrl(rawUrl) : noImage;

        return (
          <div style={frameStyle}>
            <img
              src={imageUrl}
              alt="サムネイル"
              style={{
                ...imageStyle,
                objectFit: 'contain',
                objectPosition: 'center',
                boxSizing: 'border-box',
                display: 'block',
              }}
              onError={(e) => {
                e.currentTarget.onerror = null;
                e.currentTarget.src = noImage;
              }}
            />
          </div>
        );
      },
    };

    const reviewColumn = {
      headerName: 'レビュー',
      flex: 1,
      suppressMovable: true,
      cellRenderer: (params) => (
        <Link
          to={`/review/${params.data.reviewId}`}
          style={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            height: '100%',
            padding: '8px 0',
            textDecoration: 'none',
            color: 'inherit',
          }}
        >
          <div className="fw-bold lh-sm">
            {params.data.reviewType === 0
              ? '[レビュー] '
              : params.data.reviewType === 1
                ? '[口コミ] '
                : ''}
            {params.data.title}
          </div>
          <div className="text-secondary small">{params.data.createdBy}</div>
          <div className="text-secondary small">
            閲覧数 {(params.data?.viewCount ?? 0).toLocaleString()}
          </div>
        </Link>
      ),
    };

    if (isMobile) {
      return [
        imageColumn,
        {
          headerName: '',
          flex: 1,
          suppressMovable: true,
          cellRenderer: (params) => (
            <Link
              to={`/review/${params.data.reviewId}`}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                justifyContent: 'flex-start',
                gap: '5px',
                height: '100%',
                padding: '4px 0',
                textDecoration: 'none',
                color: 'inherit',
              }}
            >
              <div className="text-secondary small">{formatYearMonthDot(params.data.createdAt)}</div>
              <div className="fw-bold lh-sm">
                {params.data.reviewType === 0
                  ? '[レビュー] '
                  : params.data.reviewType === 1
                    ? '[口コミ] '
                    : ''}
                {params.data.title}
              </div>
              <div className="text-secondary small">{params.data.createdBy}</div>
            </Link>
          ),
        },
      ];
    }

    return [
      imageColumn,
      reviewColumn,
      {
        headerName: '作成日',
        field: 'createdAt',
        width: 100,
        suppressMovable: true,
        cellStyle: {
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        },
        cellRenderer: (params) => (
          <span className="text-secondary small">{formatYearMonthDot(params.value)}</span>
        ),
      },
    ];
  }, [isMobile]);

  const handleSearch = () => {
    setActiveKeyword(searchKeyword);
    setCurrentPage(1);
  };

  if (loading) return <LoadingMessage />;

  return (
    <>
      <div className="container-fluid px-5 review-list-container">
        <div className="d-flex justify-content-between align-items-end my-5 review-list-header">
          <h1 className="m-0">レビュー/口コミ</h1>
          <button
            className="btn btn-primary review-write-button"
            onClick={() => {
              if (!isLoggedIn) {
                setOpen1000(true);
                return;
              }
              navigate('/review/write');
            }}
          >
            作成する
          </button>
        </div>
        <div className="ag-theme-alpine custom-grid w-100">
          <AgGridReact
            rowData={paginated}
            columnDefs={columnDefs}
            headerHeight={0}
            domLayout="autoHeight"
            rowHeight={120}
            theme="legacy"
            rowStyle={{ cursor: 'pointer' }}
            overlayNoRowsTemplate={`<span class='text-muted'>${CMMessage.MSG_EMPTY_008}</span>`}
            onRowClicked={(params) => navigate(`/review/${params.data.reviewId}`)}
          />
        </div>
        <CM_SearchBar
          searchKeyword={searchKeyword}
          setSearchKeyword={setSearchKeyword}
          searchType={searchType}
          setSearchType={setSearchType}
          searchTypes={SEARCH_TYPE}
          onSearch={handleSearch}
        />
        <CM_Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      </div>
      <CM_99_1000 isOpen={open1000} onClose={() => setOpen1000(false)} />
    </>
  );
}
