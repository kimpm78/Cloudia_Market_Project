import { Link, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import CM_90_1011_grid from '../components/CM_90_1000_grid.jsx';
import axiosInstance from '../services/axiosInstance.js';
import { CATEGORIES, DISPLAY_OPTIONS, SEARCH_TYPE } from '../constants/constants.js';

export default function CM_90_1044() {
  const navigate = useNavigate();
  const [rowData, setRowData] = useState([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchType, setSearchType] = useState(1);

  const columnDefs = [
    {
      headerName: '구분',
      field: 'codeValue',
      width: 120,
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
      width: 200,
      suppressMovable: true,
    },
    {
      headerName: '作成者',
      field: 'userId',
      width: 120,
      suppressMovable: true,
      sortable: false,
    },
    {
      headerName: '作成日',
      field: 'publishedAt',
      width: 140,
      suppressMovable: true,
      sortable: false,
    },
    {
      headerName: '閲覧数',
      field: 'viewCount',
      width: 140,
      suppressMovable: true,
      sortable: false,
      valueFormatter: (params) => Number(params.value).toLocaleString(),
    },
    {
      headerName: '状態',
      field: 'isDisplay',
      width: 80,
      cellRenderer: (params) => {
        const value = params.value;
        const isDisplay = DISPLAY_OPTIONS.find((cat) => cat.value === value);
        const label = isDisplay ? isDisplay.label : value;
        let className = 'default';
        if (value === 2) className = 'urgent';
        return <span className={`badge-category ${className}`}>{label}</span>;
      },
    },
  ];

  useEffect(() => {
    noticeAll();
  }, []);

  const noticeAll = async () => {
    const result = await axiosInstance.get('/admin/menu/notice/findAll');
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
  };

  const handleEditing = (e) => {
    if (e.data) {
      const noticeId = e.data.noticeId;
      navigate(`/notice/${noticeId}`);
    }
  };

  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      noticeAll();
    } else {
      const result = await axiosInstance.get('/admin/menu/notice/findNotice', {
        searchKeyword: searchKeyword,
        searchType: searchType,
      });
      if (result.data.result) {
        setRowData(result.data.resultList);
      }
    }
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h2 className="border-bottom pb-2 mb-3">お知らせ 管理</h2>
        <div className="row mb-3">
          <div className="ms-auto d-flex justify-content-end">
            <Link to="/admin/menu/notice/register" className="btn btn-primary btn-min-width">
              作成する
            </Link>
          </div>
        </div>
        <CM_90_1011_grid
          itemsPerPage={15}
          pagesPerGroup={5}
          rowData={rowData}
          columnDefs={columnDefs}
          onRowClicked={(e) => {
            handleEditing(e);
          }}
          wrapperClass="ag-cursor-pointer"
        />
        <div className="d-flex justify-content-center gap-2 mb-5 mt-5 align-items-center">
          <select
            className="form-select w-auto"
            value={searchType}
            onChange={(e) => setSearchType(e.target.value)}
          >
            {SEARCH_TYPE.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <input
            className="form-control w-25"
            placeholder="검색어를 입력하세요"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                handleSearch();
              }
            }}
          />
          <button className="btn btn-primary" onClick={handleSearch}>
            検索
          </button>
        </div>
      </div>
    </div>
  );
}
