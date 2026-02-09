import { useEffect, useState } from 'react';

import { Link } from 'react-router-dom';
import axiosInstance from '../services/axiosInstance';

import CM_90_1011_grid from '../components/CM_90_1000_grid';
import '../styles/CM_90_1063.css';

export default function CM_90_1063() {
  const [searchType, setSearchType] = useState('1');
  const [searchTerm, setSearchTerm] = useState('');
  const [rowData, setRowData] = useState([]);
  const columnDefs = [
    {
      headerName: '상품 코드',
      field: 'productCode',
      minWidth: 100,
      maxWidth: 120,
      flex: 1,
    },
    {
      headerName: '상품명',
      field: 'productName',
      minWidth: 300,
      flex: 2,
    },
    {
      headerName: '수량',
      field: 'qty',
      minWidth: 80,
      maxWidth: 100,
      flex: 1,
      cellStyle: (params) => {
        const value = params.value;
        const isNumber = typeof value === 'number' && !isNaN(value);
        if (!isNumber) return {};
        return {
          color: value < 0 ? 'red' : 'blue',
          fontWeight: 'bold',
        };
      },
    },
    {
      headerName: '비고',
      field: 'reason',
      minWidth: 100,
      flex: 1,
    },
    {
      headerName: '재고 등록일',
      field: 'createdAt',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date.getTime())) return '';
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
      },
    },
    {
      headerName: '재고 등록자',
      field: 'createdBy',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
  ];

  useEffect(() => {
    stockAll();
  }, []);

  const stockAll = async () => {
    const result = await axiosInstance.get('/admin/stocks/all');
    setRowData(result.data.resultList);
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      stockAll();
    } else {
      const result = await axiosInstance.get('/admin/stocks/findStocks', {
        params: {
          searchType: searchType,
          searchTerm: searchTerm,
        },
      });
      setRowData(result.data.resultList);
    }
  };

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">재고 목록</h5>

        <div className="row mb-3">
          <div className="col-12">
            <div className="d-flex align-items-center w-100">
              <div className="input-group input-group-custom">
                <select
                  className="form-select select-custom"
                  value={searchType}
                  onChange={(e) => setSearchType(e.target.value)}
                >
                  <option value="1">상품 코드</option>
                  <option value="2">상품 명</option>
                </select>
                <input
                  type="text"
                  className="form-control"
                  placeholder="상품코드 / 상품명을 입력하세요"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                />
              </div>
              <button className="btn btn-primary me-2 btn-min-width" onClick={handleSearch}>
                검색
              </button>
              <div className="spacer-30px"></div>
              <div className="ms-auto">
                <Link to="/admin/products/stock/register" className="btn btn-primary btn-min-width">
                  등록
                </Link>
              </div>
            </div>
          </div>
        </div>
        <CM_90_1011_grid
          itemsPerPage={10}
          pagesPerGroup={5}
          rowData={rowData}
          columnDefs={columnDefs}
        />
      </div>
    </div>
  );
}
