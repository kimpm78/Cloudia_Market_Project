import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

export default function CM_01_1015_Table({ returns, loading }) {
  const navigate = useNavigate();

  // カラム定義
  const columnDefs = useMemo(
    () => [
      {
        headerName: '番号',
        valueGetter: 'node.rowIndex + 1',
        width: 70,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '申請日付',
        field: 'requestedAt',
        width: 120,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '注文番号',
        field: 'orderNo',
        flex: 1.2,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '商品情報',
        flex: 2,
        // products配列の先頭要素から商品名を取得
        valueGetter: (params) => {
          const products = params.data.products;
          return products && products.length > 0 ? products[0].productName : '商品情報なし';
        },
        cellStyle: { fontWeight: 'bold' },
      },
      {
        headerName: '数量',
        width: 80,
        cellStyle: { textAlign: 'center' },
        // products配列の先頭要素から数量を取得
        valueGetter: (params) => {
          const products = params.data.products;
          return products && products.length > 0 ? `${products[0].quantity}個` : '-';
        },
      },
      {
        headerName: '進行状態',
        field: 'returnStatusName',
        width: 120,
        cellStyle: { textAlign: 'center' },
        cellRenderer: (params) => (
          <span className="badge bg-secondary text-white" style={{ fontSize: '0.85rem' }}>
            {params.value}
          </span>
        ),
      },
    ],
    []
  );

  //  행 클릭 시 상세 페이지 이동
  const onRowClicked = (event) => {
    const returnId = event.data.returnId;
    if (returnId) {
      navigate(`/mypage/returns/${returnId}`);
    }
  };

  if (loading) {
    return <div className="text-center p-5">データを読み込んでいます...</div>;
  }

  if (!returns || returns.length === 0) {
    return (
      <div className="text-center p-5 border rounded bg-light">
        選択された交換・返品履歴がありません。
      </div>
    );
  }

  return (
    <div className="ag-theme-alpine w-100" style={{ height: '500px' }}>
      <AgGridReact
        rowData={returns}
        columnDefs={columnDefs}
        defaultColDef={{
          sortable: false,
          resizable: true,
          suppressMovable: true,
        }}
        rowSelection="single"
        onRowClicked={onRowClicked}
        headerHeight={40}
        rowHeight={50}
        overlayNoRowsTemplate="<span>選択された履歴がありません。</span>"
        pagination={false}
      />
    </div>
  );
}
