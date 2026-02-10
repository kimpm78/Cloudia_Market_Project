import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import { ModuleRegistry } from 'ag-grid-community';
import { PaginationModule } from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

import CM_SearchBar from '../components/CM_99_1012_searchBar.jsx';
import CM_Pagination from '../components/CM_99_1013_pagination.jsx';
import { fetchQnaList } from '../services/QnaService.js';
import { SEARCH_TYPE } from '../constants/constants.js';
import { resolveQnaStatus } from '../constants/Qna.js';
import { useAuth } from '../contexts/AuthContext.jsx';
import CMMessage from '../constants/CMMessage';
import '../styles/CM_04_1003.css';

ModuleRegistry.registerModules([PaginationModule]);

const pageSize = 25;

export default function CM_04_1003() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAdminOrManager = user?.roleId === 1 || user?.roleId === 2;

  const [rows, setRows] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchType, setSearchType] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeKeyword, setActiveKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadData = useCallback(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');

    fetchQnaList({
      page: currentPage,
      size: pageSize,
      searchType,
      keyword: activeKeyword,
      signal: controller.signal,
    })
      .then((result) => {
        const items = Array.isArray(result?.items) ? result.items : [];
        const count = Number(result?.totalCount ?? 0);
        setRows(items);
        setTotalPages(Math.max(Math.ceil(count / pageSize) || 1, 1));
      })
      .catch((err) => {
        if (err.name === 'CanceledError') return;
        console.error('Q&A リスト取得失敗:', err?.message || err);
        setRows([]);
        setTotalPages(1);
        setError(err?.message || CMMessage.MSG_ERR_005('Q&A リスト'));
      })
      .finally(() => {
        setLoading(false);
      });

    return () => controller.abort();
  }, [activeKeyword, currentPage, searchType]);

  useEffect(() => {
    const abort = loadData();
    return abort;
  }, [loadData]);

  const handleSearch = () => {
    setActiveKeyword(searchKeyword.trim());
    setCurrentPage(1);
  };

  const showNoPermissionToast = useCallback(() => {
    if (typeof window !== 'undefined' && typeof window.CM_showToast === 'function') {
      window.CM_showToast(CMMessage.MSG_ERR_014);
    }
  }, []);

  const canAccessPrivate = useCallback(
    (row) => {
      if (!row || Number(row.isPrivate) !== 1) {
        return true;
      }
      if (!user) {
        return false;
      }
      const roleId = user?.roleId;
      if (roleId === 1 || roleId === 2) {
        return true;
      }
      return Number(row.userId) === Number(user.userId);
    },
    [user]
  );

  const columnDefs = useMemo(
    () => [
      {
        headerName: '区分',
        field: 'statusLabel',
        width: 140,
        headerClass: 'fs-5',
        suppressMovable: true,
        cellRenderer: (params) => {
          const status = resolveQnaStatus(params.data?.statusCode, params.data?.statusValue);
          return <span className={`badge ${status.badgeClass} fw-semibold`}>{status.label}</span>;
        },
      },
      {
        headerName: 'タイトル',
        field: 'title',
        flex: 1,
        minWidth: 220,
        headerClass: 'fs-5',
        suppressMovable: true,
        cellRenderer: (params) => {
          const isPrivate = Number(params.data?.isPrivate ?? 0) === 1;
          const handleClick = (event) => {
            if (isPrivate && !canAccessPrivate(params.data)) {
              event.preventDefault();
              showNoPermissionToast();
            }
          };
          return (
            <Link
              to={`/qna/${params.data.qnaId}`}
              className={`text-decoration-none ${isPrivate && !canAccessPrivate(params.data) ? 'text-muted' : 'text-black'}`}
              onClick={handleClick}
            >
              {isPrivate && <i className="bi bi-file-lock2 me-1"></i>}
              {params.value}
            </Link>
          );
        },
      },
      {
        headerName: '作成者',
        field: 'loginId',
        width: 140,
        headerClass: 'fs-5',
        suppressMovable: true,
        sortable: false,
        cellRenderer: (params) => {
          const handleClick = (event) => {
            if (!canAccessPrivate(params.data)) {
              event.preventDefault();
              showNoPermissionToast();
            }
          };
          return (
            <Link
              to={`/qna/${params.data.qnaId}`}
              className="text-decoration-none text-black"
              onClick={handleClick}
            >
              {params.value || params.data?.writerName || `user-${params.data?.userId}`}
            </Link>
          );
        },
      },
      {
        headerName: '作成日',
        field: 'createdAt',
        width: 140,
        headerClass: 'fs-5',
        suppressMovable: true,
        sortable: false,
        cellRenderer: (params) => {
          const value = params.value ?? '';
          const dateOnly = value.split(' ')[0]?.replace(/-/g, '.');
          const handleClick = (event) => {
            if (!canAccessPrivate(params.data)) {
              event.preventDefault();
              showNoPermissionToast();
            }
          };
          return (
            <Link
              to={`/qna/${params.data.qnaId}`}
              className="text-decoration-none text-black"
              onClick={handleClick}
            >
              {dateOnly || value}
            </Link>
          );
        },
      },
    ],
    [canAccessPrivate, showNoPermissionToast]
  );

  const handleRowClick = (event) => {
    if (!event?.data) {
      return;
    }
    if (!canAccessPrivate(event.data)) {
      showNoPermissionToast();
      return;
    }
    if (event.data.qnaId) {
      navigate(`/qna/${event.data.qnaId}`);
    }
  };

  return (
    <>
      <div className="container-fluid px-5 qna-list-container">
        <div className="d-flex justify-content-between align-items-end my-5 qna-list-header">
          <h1>Q & A</h1>
          {!isAdminOrManager && (
            <button className="btn btn-primary" onClick={() => navigate('/qna/edit/new')}>
              作成する
            </button>
          )}
        </div>
        {loading && null}
        {error && !loading && <div className="py-3 text-danger">{error}</div>}
        <div className="ag-theme-alpine custom-grid w-100 qna-list-grid">
          <AgGridReact
            rowData={rows}
            columnDefs={columnDefs}
            pagination={false}
            defaultColDef={{ resizable: false }}
            theme="legacy"
            domLayout="autoHeight"
            onRowClicked={handleRowClick}
            overlayNoRowsTemplate={`<span class='text-muted'>${CMMessage.MSG_EMPTY_007}</span>`}
          />
        </div>
        <div className="d-flex justify-content-center align-items-center mt-3">
          <CM_Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </div>

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
