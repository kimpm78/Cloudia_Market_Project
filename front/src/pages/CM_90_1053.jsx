import { useEffect, useState } from 'react';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import axiosInstance from '../services/axiosInstance';

export default function CM_90_1053() {
  const [searchType, setSearchType] = useState('1');
  const [searchTerm, setSearchTerm] = useState('');
  const [rowData, setRowData] = useState([]);
  const columnDefs = [
    {
      headerName: '商品コード',
      field: 'productCode',
      minWidth: 100,
      maxWidth: 120,
      flex: 1,
    },
    {
      headerName: '商品名',
      field: 'productName',
      minWidth: 300,
      flex: 2,
    },
    {
      headerName: '商品区分',
      field: 'productCategory',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (value === 1) return '通常販売';
        else return '予約販売';
      },
    },
    {
      headerName: '在庫',
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
      headerName: 'カート',
      field: 'cartQty',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
    {
      headerName: '不良',
      field: 'defectiveQty',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
    {
      headerName: '利用可能在庫',
      field: 'availableQty',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
    {
      headerName: 'ステータス',
      field: 'saleStatus',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (value === 1) return '売り切れ';
        if (value === 2) return '販売可能';
        else return '販売中';
      },
    },
    {
      headerName: '登録日',
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
      headerName: '登録者',
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
        <h2 className="border-bottom pb-2 mb-3">在庫状態確認</h2>
        <div className="row mb-3">
          <div className="col-12">
            <div className="d-flex align-items-center w-100">
              <div className="input-group input-group-custom">
                <select
                  className="form-select select-custom"
                  value={searchType}
                  onChange={(e) => setSearchType(e.target.value)}
                >
                  <option value="1">商品コード</option>
                  <option value="2">商品名</option>
                </select>
                <input
                  type="text"
                  className="form-control"
                  placeholder="商品コード／商品名を入力してください"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                />
              </div>
              <button className="btn btn-primary me-2 btn-min-width" onClick={handleSearch}>
                検索
              </button>
              <div className="small mt-2 mt-md-0 ms-auto">
                <div>
                  <strong>売り切れ</strong>: 利用可能在庫 0
                </div>
                <div>
                  <strong>販売可能</strong>: 在庫登録済み & 商品未登録
                </div>
                <div>
                  <strong>販売中</strong>: 在庫登録済み & 商品登録済み
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
