import { useEffect, useState } from 'react';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import axiosInstance from '../services/axiosInstance';

export default function CM_90_1053() {
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
      headerName: '상품 분류',
      field: 'productCategory',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (value === 1) return '상시 판매';
        else return '예약 판매';
      },
    },
    {
      headerName: '재고',
      field: 'totalQty',
      minWidth: 80,
      maxWidth: 100,
      flex: 1,
      cellStyle: (params) => {
        const value = params.value;
        const isNumber = typeof value === 'number' && !isNaN(value);
        if (!isNumber) return {};
        return {
          color: value === 0 ? 'red' : 'blue',
          fontWeight: 'bold',
        };
      },
    },
    {
      headerName: '장바구니',
      field: 'cartQty',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
    {
      headerName: '불량',
      field: 'defectiveQty',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
    {
      headerName: '가용 재고',
      field: 'availableQty',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
    {
      headerName: '상태',
      field: 'saleStatus',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (value === 1) return '품절';
        if (value === 2) return '판매가능';
        else return '판매중';
      },
    },
    {
      headerName: '등록일',
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
      headerName: '등록자',
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
    const result = await axiosInstance.get('/admin/stocks/status');
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      stockAll();
    } else {
      const result = await axiosInstance.get('/admin/stocks/findstatus', {
        params: { searchType: searchType, searchTerm: searchTerm },
      });
      if (result.data.result) {
        setRowData(result.data.resultList);
      }
    }
  };

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">재고 상태 확인</h5>
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
              <div className="small mt-2 mt-md-0 ms-auto">
                <div>
                  <strong>품절</strong>: 가용 재고 0
                </div>
                <div>
                  <strong>판매가능</strong>: 재고 등록 & 상품 미등록
                </div>
                <div>
                  <strong>판매중</strong>: 재고 등록 & 상품 등록
                </div>
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
