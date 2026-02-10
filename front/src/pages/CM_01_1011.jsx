import { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import { ModuleRegistry, ClientSideRowModelModule, PinnedRowModule } from 'ag-grid-community';
import { ORDER_STATUS_CODE } from '../constants/constants';
import { getRequest, postRequest } from '../services/axiosInstance';
import CM_01_1011_OrderSummary from '../components/CM_01_1011_OrderSummary';
import CM_01_1011_ActionButtons from '../components/CM_01_1011_ActionButtons';
import CM_01_1011_AddressModal from '../components/CM_01_1011_AddressModal';
import CM_01_1011_RefundModal from '../components/CM_01_1011_RefundModal';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import noImage from '../images/common/CM-NoImage.png';

ModuleRegistry.registerModules([ClientSideRowModelModule, PinnedRowModule]);

const parsePrice = (val) => parseInt(String(val || '0').replace(/[^0-9]/g, ''), 10);

export const buildImageUrl = (rawUrl) => {
  const base = import.meta.env.VITE_API_BASE_IMAGE_URL || '';

  if (!rawUrl) return '';

  if (rawUrl.startsWith('/images/') && base.endsWith('/images')) {
    return base.replace(/\/images$/, '') + rawUrl;
  }

  if (rawUrl.startsWith('/')) {
    return base + rawUrl;
  }

  return `${base}/${rawUrl}`;
};

const ProductCellRenderer = (params) => {
  if (params.node.rowPinned) return <span className="fw-bold text-secondary">{params.value}</span>;
  return (
    <div className="d-flex align-items-center py-2 h-100">
      <img
        src={buildImageUrl(params.data.imageUrl)}
        alt="商品"
        style={{ width: '80px', height: '80px', objectFit: 'contain' }}
        className="me-3 border bg-white rounded"
        onError={(e) => {
          e.currentTarget.onerror = null;
          e.currentTarget.src = noImage;
        }}
      />
      <div className="text-wrap lh-sm small fw-bold">{params.value}</div>
    </div>
  );
};

export default function CM_01_1011() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [orderDetail, setOrderDetail] = useState(null);
  const [paymentRowData, setPaymentRowData] = useState([]);
  const [deliveryRowData, setDeliveryRowData] = useState([]);
  const [depositRowData, setDepositRowData] = useState([]);
  const [modals, setModals] = useState({ refund: false, address: false, confirm: false });
  const [resultPopup, setResultPopup] = useState({ open: false, message: '' });

  const calc = useMemo(() => {
    const itemsSum = paymentRowData.reduce((sum, row) => sum + parsePrice(row.total), 0);
    const shipping = Number(orderDetail?.shippingCost || 0);
    const totalPrice = itemsSum + shipping;
    const totalQty = paymentRowData.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
    const pName =
      orderDetail?.orderItemCount > 1
        ? `${orderDetail.productName} 他 ${orderDetail.orderItemCount - 1}点`
        : orderDetail?.productName || '';
    return { itemsSum, shipping, totalPrice, totalQty, pName };
  }, [paymentRowData, orderDetail]);

  // --- [API呼び出し] ---
  const fetchOrderDetail = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await getRequest(`/user/mypage/purchases/${id}`);
      if (res) {
        setOrderDetail({
          ...res.orderSummary,
          shippingCost: res.orderSummary.shippingCost || 0,
          pointUsed: res.orderSummary.pointUsed || 0,
        });
        setPaymentRowData(res.paymentDetails || []);
        setDeliveryRowData(res.shippingInfo ? [res.shippingInfo] : []);
      }
    } catch (err) {
      console.error('取得に失敗しました:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchOrderDetail();
  }, [fetchOrderDetail]);

  // 振込情報設定
  useEffect(() => {
    if (
      orderDetail?.orderStatusValue === ORDER_STATUS_CODE.TRANSFER_CHECKING &&
      paymentRowData.length > 0
    ) {
      setDepositRowData([
        {
          amount: `${calc.itemsSum.toLocaleString()}円`,
          accountNumber: '[三井住友銀行] 436-074236',
          accountHolder: 'クラウディアーケット',
          dueDate: '注文後72時間以内',
        },
      ]);
    }
  }, [orderDetail, paymentRowData, calc.itemsSum]);

  const paymentColumnDefs = [
    { headerName: '商品名', field: 'product', flex: 2, cellRenderer: ProductCellRenderer },
    { headerName: '数量', field: 'quantity', width: 80, cellStyle: { textAlign: 'center' } },
    {
      headerName: '小計',
      field: 'total',
      width: 180,
      cellStyle: { textAlign: 'right' },
      cellClassRules: {
        'text-danger fw-bold fs-5': (p) =>
          p.node.rowPinned === 'bottom' && p.data.product === '注文合計金額',
      },
    },
  ];

  const paymentDefaultColDef = useMemo(
    () => ({
      suppressMovable: true,
      sortable: false,
      resizable: false,
    }),
    []
  );

  const deliveryColumnDefs = [
    { headerName: '配送先住所', field: 'address', flex: 2 },
    { headerName: '受取人', field: 'receiver', width: 120 },
    { headerName: '電話番号', field: 'phone', width: 150 },
    {
      headerName: '配送追跡',
      field: 'tracking',
      width: 120,
      cellRenderer: (p) =>
        p.value && p.value !== '未登録' ? (
          <a
            className="btn btn-sm btn-outline-primary py-0"
            href={`https://www.post.japanpost.jp/receive/tracking/result.php?code=${p.value}`}
            target="_blank"
            rel="noreferrer"
          >
            照会
          </a>
        ) : (
          <span className="text-muted">未登録</span>
        ),
    },
  ];

  const pinnedBottomRowData = [
    { product: '送料', total: `${calc.shipping.toLocaleString()}円` },
    { product: '注文合計金額', total: `${calc.totalPrice.toLocaleString()}円` },
  ];

  const toggleModal = (name, isOpen) => setModals((prev) => ({ ...prev, [name]: isOpen }));
  const openResultPopup = (message) => setResultPopup({ open: true, message });

  if (loading)
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary"></div>
      </div>
    );

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-end pb-3 mb-4 border-bottom">
        <h3 className="fw-bolder m-0">購入詳細</h3>
        <span className="badge bg-light text-dark border">注文番号: {orderDetail.orderNo}</span>
      </div>

      <CM_01_1011_OrderSummary
        orderDetail={orderDetail}
        totalQuantity={calc.totalQty}
        totalAmount={calc.totalPrice}
        displayProductName={calc.pName}
      />

      {/* 注文商品情報 */}
      <section className="mb-5">
        <h5 className="fw-bold mb-3">
          <i className="bi bi-cart-check me-2"></i>注文商品情報
        </h5>
        <div className="ag-theme-alpine w-100 overflow-hidden">
          <AgGridReact
            rowData={paymentRowData}
            columnDefs={paymentColumnDefs}
            defaultColDef={paymentDefaultColDef}
            pinnedBottomRowData={pinnedBottomRowData}
            domLayout="autoHeight"
            getRowHeight={(p) => (p.node.rowPinned === 'bottom' ? 45 : 100)}
            theme="legacy"
          />
        </div>
      </section>

      {/* お振込のご案内 */}
      {orderDetail?.orderStatusValue === ORDER_STATUS_CODE.TRANSFER_CHECKING && (
        <section className="mb-5">
          <h5 className="fw-bold mb-3 text-primary">
            <i className="bi bi-bank me-2"></i>お振込のご案内
          </h5>
          <div className="ag-theme-alpine w-100 overflow-hidden" style={{ height: '97px' }}>
            <AgGridReact
              rowData={depositRowData}
              columnDefs={[
                { headerName: '振込金額', field: 'amount', width: 150 },
                { headerName: '口座番号', field: 'accountNumber', flex: 1 },
                { headerName: '口座名義', field: 'accountHolder', width: 120 },
                { headerName: '振込期限', field: 'dueDate', width: 180 },
              ]}
              headerHeight={45}
              rowHeight={50}
              theme="legacy"
            />
          </div>
        </section>
      )}

      {/* 配送情報 */}
      <section className="mb-5">
        <h5 className="fw-bold mb-3">
          <i className="bi bi-truck me-2"></i>配送情報
        </h5>
        <div
          className="ag-theme-alpine w-100 overflow-hidden"
          style={{ height: deliveryRowData.length > 0 ? '97px' : '150px' }}
        >
          <AgGridReact
            rowData={deliveryRowData}
            columnDefs={deliveryColumnDefs}
            headerHeight={45}
            rowHeight={50}
            overlayNoRowsTemplate="<span>登録された配送情報がありません。</span>"
            theme="legacy"
          />
        </div>
      </section>

      <CM_01_1011_ActionButtons
        order={orderDetail}
        onCancel={() =>
          orderDetail?.orderStatusValue === ORDER_STATUS_CODE.TRANSFER_CHECKING
            ? toggleModal('confirm', true)
            : toggleModal('refund', true)
        }
        onAddressChange={() => toggleModal('address', true)}
      />

      {/* モーダルコンポーネント */}
      <CM_01_1011_RefundModal
        isOpen={modals.refund}
        onClose={() => toggleModal('refund', false)}
        order={orderDetail}
        onSuccess={fetchOrderDetail}
      />
      <CM_01_1011_AddressModal
        isOpen={modals.address}
        onClose={() => toggleModal('address', false)}
        orderNo={orderDetail.orderNo}
        currentAddressId={deliveryRowData[0]?.addressId}
        onSuccess={fetchOrderDetail}
      />
      <CM_99_1001
        isOpen={modals.confirm}
        onClose={() => toggleModal('confirm', false)}
        Message="注文をキャンセルしますか？"
        onConfirm={async () => {
          try {
            await postRequest(`/user/mypage/purchases/${id}/cancel`, {});
            openResultPopup('キャンセルしました。');
            fetchOrderDetail();
          } catch {
            openResultPopup('失敗しました。');
          }
          toggleModal('confirm', false);
        }}
      />
      <CM_99_1004
        isOpen={resultPopup.open}
        onClose={() => setResultPopup({ open: false, message: '' })}
        Message={resultPopup.message}
      />
    </div>
  );
}
