import { ORDER_STATUS_CODE } from '../constants/constants';

export default function CM_01_1011_OrderSummary({
  orderDetail,
  totalAmount,
  displayProductName,
}) {
  if (!orderDetail) return null;

  const getBadgeClass = (code) => {
    if (code === ORDER_STATUS_CODE.PURCHASE_CONFIRMED || code === ORDER_STATUS_CODE.DELIVERED)
      return 'bg-success';
    if (
      code === ORDER_STATUS_CODE.CANCELLED_USER ||
      code === ORDER_STATUS_CODE.CANCELLED_ADMIN ||
      code === ORDER_STATUS_CODE.PAYMENT_FAILED
    )
      return 'bg-danger';
    if (code === ORDER_STATUS_CODE.SHIPPING) return 'bg-primary';
    return 'bg-dark';
  };

  const badgeClass = getBadgeClass(orderDetail.orderStatusValue);

  return (
    <div className="card border-0 bg-light mb-5">
      <div className="card-body p-4">
        <div className="row align-items-center">
          {/* 注文詳細情報 */}
          <div className="col-md-9">
            <div className="row g-3">
              <div className="col-6 col-md-4">
                <div className="text-secondary small">注文番号</div>
                <div className="fw-bold text-primary">{orderDetail.orderNo}</div>
              </div>
              <div className="col-6 col-md-4">
                <div className="text-secondary small">注文日</div>
                <div className="fw-semibold">{orderDetail.orderDate}</div>
              </div>
              <div className="col-6 col-md-4">
                <div className="text-secondary small">現在のステータス</div>
                <div className={`badge ${badgeClass} fw-bold`}>{orderDetail.orderStatus}</div>
              </div>
              <div className="col-12 col-md-8">
                <div className="text-secondary small">商品名</div>
                <div className="fw-bold text-dark text-truncate">{displayProductName}</div>
              </div>
              <div className="col-6 col-md-2 ">
                <div className="text-secondary small">合計購入金額</div>
                <div className="fw-bolder fs-5 text-dark">{totalAmount.toLocaleString()}円</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
