import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import { PAYMENT_METHOD_CODE, ORDER_STATUS_CODE } from '../constants/constants';

const PAYMENT_LABEL = {
  [PAYMENT_METHOD_CODE.TRANSFER]: '銀行振込',
  [PAYMENT_METHOD_CODE.CREDIT_CARD]: 'クレジットカード',
};

export default function CM_01_1005_OrderHistoryTable({ orders, loading }) {
  const navigate = useNavigate();
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

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

  const columnDefs = useMemo(
    () => [
      {
        headerName: '注文日/注文番号',
        field: 'orderNo',
        width: isMobile ? 130 : 180,
        cellRenderer: (params) => (
          <div style={{ lineHeight: '1.4', paddingTop: '4px' }}>
            <div className="text-dark small">{params.data.orderDate}</div>
            <div className="text-primary fw-bold text-truncate">{params.value}</div>
          </div>
        ),
      },
      {
        headerName: '商品名',
        field: 'productName',
        flex: 1,
        minWidth: 120,
        cellStyle: { fontWeight: '500', display: 'flex', alignItems: 'center' },
        cellRenderer: (params) => {
          const name = params.value || '';
          const count = params.data.orderItemCount;
          const displayText = count > 1 ? `${name} 他 ${count - 1}点` : name;

          return (
            <div className="text-truncate" style={{ width: '100%' }} title={displayText}>
              {displayText}
            </div>
          );
        },
      },
      {
        headerName: '発送予定',
        field: 'deliveryDate',
        width: 130,
        hide: isMobile,
      },
      {
        headerName: isMobile ? '金額' : '注文金額／支払い方法',
        field: 'totalPrice',
        width: isMobile ? 90 : 220,
        cellRenderer: (params) => {
          const price = params.value || 0;
          const method = PAYMENT_LABEL[params.data.paymentValue] || '支払い方法';

          if (isMobile) {
            return (
              <div className="d-flex align-items-center h-100 justify-content-end">
                <span className="fw-bold">{price.toLocaleString()}円</span>
              </div>
            );
          }

          return (
            <div className="d-flex align-items-center h-100">
              <span className="fw-bold">{price.toLocaleString()}円</span>
              <span className="text-secondary ms-2 small">/ {method}</span>
            </div>
          );
        },
      },
      {
        headerName: 'ステータス',
        field: 'orderStatus',
        width: 130,
        hide: isMobile,
        cellRenderer: (params) => {
          const statusName = params.value || '';
          const statusCode = params.data.orderStatusValue;
          let badgeClass = 'bg-dark';

          if (
            statusCode === ORDER_STATUS_CODE.PURCHASE_CONFIRMED ||
            statusCode === ORDER_STATUS_CODE.DELIVERED
          ) {
            badgeClass = 'bg-success';
          } else if (
            statusCode === ORDER_STATUS_CODE.CANCELLED_USER ||
            statusCode === ORDER_STATUS_CODE.CANCELLED_ADMIN ||
            statusCode === ORDER_STATUS_CODE.PAYMENT_FAILED
          ) {
            badgeClass = 'bg-danger';
          } else if (statusCode === ORDER_STATUS_CODE.SHIPPING) {
            badgeClass = 'bg-primary';
          }

          return (
            <div className="d-flex align-items-center h-100">
              <span className={`badge ${badgeClass} fw-bold`}>{statusName}</span>
            </div>
          );
        },
      },
    ],
    [isMobile, navigate]
  );

  const onRowClicked = (event) => {
    navigate(`/mypage/purchases/${encodeURIComponent(event.data.orderNo)}`);
  };

  return (
    <div className="ag-theme-alpine w-100 border-top border-bottom" style={{ height: '550px' }}>
      <AgGridReact
        rowData={orders}
        columnDefs={columnDefs}
        onRowClicked={onRowClicked}
        headerHeight={48}
        rowHeight={65}
        rowClass="border-bottom-light"
        overlayLoadingTemplate={
          '<span class="loading-message">データを読み込み中です...</span>'
        }
        overlayNoRowsTemplate={'<span class="no-rows">購入履歴がありません。</span>'}
        theme="legacy"
        loading={loading}
      />
    </div>
  );
}
