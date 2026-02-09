import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

export default function CM_01_1015_Table({ returns, loading }) {
  const navigate = useNavigate();

  // 컬럼 정의
  const columnDefs = useMemo(
    () => [
      {
        headerName: '번호',
        valueGetter: 'node.rowIndex + 1',
        width: 70,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '신청일자',
        field: 'requestedAt',
        width: 120,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '주문번호',
        field: 'orderNo',
        flex: 1.2,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '상품정보',
        flex: 2,
        // products 배열의 첫 번째 요소에서 상품명 추출
        valueGetter: (params) => {
          const products = params.data.products;
          return products && products.length > 0 ? products[0].productName : '상품 정보 없음';
        },
        cellStyle: { fontWeight: 'bold' },
      },
      {
        headerName: '수량',
        width: 80,
        cellStyle: { textAlign: 'center' },
        //products 배열의 첫 번째 요소에서 수량 추출
        valueGetter: (params) => {
          const products = params.data.products;
          return products && products.length > 0 ? `${products[0].quantity}개` : '-';
        },
      },
      {
        headerName: '진행상태',
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
    return <div className="text-center p-5">데이터를 불러오는 중입니다...</div>;
  }

  if (!returns || returns.length === 0) {
    return (
      <div className="text-center p-5 border rounded bg-light">
        조회된 교환/반품 내역이 없습니다.
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
        overlayNoRowsTemplate="<span>내역이 없습니다.</span>"
        pagination={false}
      />
    </div>
  );
}
