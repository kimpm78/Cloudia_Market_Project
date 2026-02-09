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
          {/* 주문 상세 정보 */}
          <div className="col-md-9">
            <div className="row g-3">
              <div className="col-6 col-md-4">
                <div className="text-secondary small">주문 번호</div>
                <div className="fw-bold text-primary">{orderDetail.orderNo}</div>
              </div>
              <div className="col-6 col-md-4">
                <div className="text-secondary small">주문일</div>
                <div className="fw-semibold">{orderDetail.orderDate}</div>
              </div>
              <div className="col-6 col-md-4">
                <div className="text-secondary small">현재 상태</div>
                <div className={`badge ${badgeClass} fw-bold`}>{orderDetail.orderStatus}</div>
              </div>
              <div className="col-12 col-md-8">
                <div className="text-secondary small">상품명</div>
                <div className="fw-bold text-dark text-truncate">{displayProductName}</div>
              </div>
              <div className="col-6 col-md-2 ">
                <div className="text-secondary small">총 구매 금액</div>
                <div className="fw-bolder fs-5 text-dark">{totalAmount.toLocaleString()}원</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
