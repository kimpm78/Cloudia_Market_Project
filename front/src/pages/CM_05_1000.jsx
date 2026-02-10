import { useEffect, useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { AgGridReact } from 'ag-grid-react';
import { ModuleRegistry } from 'ag-grid-community';
import { PaginationModule } from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import '../styles/CM_05_1000.css';

import axiosInstance from '../services/axiosInstance.js';
import { CATEGORIES, SEARCH_TYPE } from '../constants/constants.js';
import { formatYearMonthDot } from '../utils/DateFormat.js';
import CM_SearchBar from '../components/CM_99_1012_searchBar.jsx';
import CM_Pagination from '../components/CM_99_1013_pagination.jsx';
import CMMessage from '../constants/CMMessage';

ModuleRegistry.registerModules([PaginationModule]);

export default function CM_05_1000() {
  const navigate = useNavigate();

  const categoryPriority = CATEGORIES.reduce((acc, cur) => {
    acc[cur.label] = cur.value;
    return acc;
  }, {});

  const [notices, setNotices] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [searchType, setSearchType] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeKeyword, setActiveKeyword] = useState('');
  const pageSize = 15;
  const totalPages = Math.ceil(notices.length / pageSize);

  useEffect(() => {
    axiosInstance
      .get(`${import.meta.env.VITE_API_BASE_URL}/guest/notice`)
      .then((res) => {
        // console.log('공지사항 응답 데이터:', res.data);
        const list = Array.isArray(res.data) ? res.data : res.data?.resultList || [];
        if (Array.isArray(list)) {
          setNotices(list.filter((notice) => notice.isDisplay === 1));
        } else {
          setNotices([]);
        }
      })
      .catch((err) => {
        console.error('공지사항 로드 실패:', err?.response?.data?.message || err.message);
      });
  }, []);

  const filteredNotices = useMemo(() => {
    return notices.filter((notice) => {
      const keyword = activeKeyword.toLowerCase();
      const title = notice.title.toLowerCase();
      const content = notice.content.toLowerCase();
      const userId = String(notice.userId ?? '').toLowerCase();
      const createdBy = String(notice.createdBy ?? '').toLowerCase();

      if (searchType === 2) {
        return title.includes(keyword);
      } else if (searchType === 3) {
        return content.includes(keyword);
      } else if (searchType === 4) {
        return userId.includes(keyword) || createdBy.includes(keyword);
      } else {
        return title.includes(keyword) || content.includes(keyword);
      }
    });
  }, [notices, activeKeyword, searchType]);

  const sortedNotices = useMemo(() => {
    return [...filteredNotices].sort((a, b) => {
      return (categoryPriority[a.category] || 999) - (categoryPriority[b.category] || 999);
    });
  }, [filteredNotices, categoryPriority]);

  const paginatedNotices = useMemo(() => {
    return sortedNotices.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  }, [sortedNotices, currentPage, pageSize]);

  const formatAuthor = (roleId, userId) => {
    if (Number(roleId) === 1) return '管理者';
    if (Number(roleId) === 2) return 'マネージャー';
    const raw = String(userId ?? '').trim();
    if (!raw) return '-';
    return `user${raw}`;
  };

  const renderNoticeLink = (params, value) => (
    <Link to={`/notice/${params.data.noticeId}`} className="text-decoration-none text-black">
      {value}
    </Link>
  );

  const columnDefs = [
    {
      headerName: '区分',
      field: 'codeValue',
      width: 120,
      headerClass: 'fs-5',
      suppressMovable: true,
      cellRenderer: (params) => {
        const value = params.value;
        const category = CATEGORIES.find((cat) => cat.value === value);
        const label = category ? category.label : value;

        let className = 'default';
        if (value === 1 || value === 2) className = 'urgent';
        else if (value === 3) className = 'payment';

        return <span className={`badge-category ${className}`}>{label}</span>;
      },
    },
    {
      headerName: 'タイトル',
      field: 'title',
      flex: 1,
      minWidth: 200,
      headerClass: 'fs-5',
      suppressMovable: true,
      cellRenderer: (params) => (
        <Link to={`/notice/${params.data.noticeId}`} className="text-decoration-none text-black">
          {params.value}
        </Link>
      ),
    },
    {
      headerName: '作成者',
      field: 'userId',
      width: 120,
      headerClass: 'fs-5',
      suppressMovable: true,
      sortable: false,
      cellRenderer: (params) => {
        const displayName = formatAuthor(params.data?.roleId, params.value);
        return (
          <Link to={`/notice/${params.data.noticeId}`} className="text-decoration-none text-black">
            {displayName}
          </Link>
        );
      },
    },
    {
      headerName: '作成日',
      field: 'publishedAt',
      width: 120,
      headerClass: 'fs-5',
      suppressMovable: true,
      sortable: false,
      cellRenderer: (params) => (
        <Link to={`/notice/${params.data.noticeId}`} className="text-decoration-none text-black">
          {formatYearMonthDot(params.value)}
        </Link>
      ),
    },
    {
      headerName: '閲覧数',
      field: 'viewCount',
      width: 100,
      headerClass: 'fs-5',
      suppressMovable: true,
      sortable: false,
      cellRenderer: (params) => renderNoticeLink(params, Number(params.value).toLocaleString()),
    },
  ];

  const handleSearch = () => {
    setActiveKeyword(searchKeyword);
    setCurrentPage(1);
  };

  const handleEditing = (e) => {
    if (e.data) {
      const noticeId = e.data.noticeId;
      navigate(`/notice/${noticeId}`);
    }
  };

  return (
    <>
      <div className="container-fluid px-3 px-md-5">
        <h1 className="m-0 notice-title">お知らせ</h1>
        <div className="ag-theme-alpine custom-grid w-100">
          <AgGridReact
            rowData={paginatedNotices}
            columnDefs={columnDefs}
            pagination={false}
            defaultColDef={{ resizable: false }}
            theme="legacy"
            domLayout="autoHeight"
            onRowClicked={handleEditing}
            overlayNoRowsTemplate={`<span class='text-muted'>${CMMessage.MSG_EMPTY_011}</span>`}
          />
        </div>
        <CM_Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />

        <CM_SearchBar
          searchTypes={SEARCH_TYPE}
          searchType={searchType}
          setSearchType={setSearchType}
          searchKeyword={searchKeyword}
          setSearchKeyword={setSearchKeyword}
          onSearch={handleSearch}
        />
      </div>
    </>
  );
}
