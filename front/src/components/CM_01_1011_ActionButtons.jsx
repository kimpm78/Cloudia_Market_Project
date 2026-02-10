import { useNavigate } from 'react-router-dom';
import { ORDER_STATUS_CODE } from '../constants/constants';

export default function CM_01_1011_ActionButtons({ order, onCancel, onAddressChange }) {
  const navigate = useNavigate();

  if (!order) return null;

  const renderButtons = () => {
    const currentStatus = order.orderStatusValue;
    const btnClass = 'btn fw-bold px-4 py-2';

    const checkTimeLimit = () => {
      if (!order.paymentAt) return false;

      const paymentTime = new Date(order.paymentAt).getTime();
      const currentTime = new Date().getTime();
      const diffMs = currentTime - paymentTime;
      const seventyTwoHoursMs = 72 * 60 * 60 * 1000;
      return diffMs > seventyTwoHoursMs;
    };

    const isExpired = checkTimeLimit();

    if (currentStatus === ORDER_STATUS_CODE.TRANSFER_CHECKING) {
      return (
        <div className="d-flex gap-3">
          <button
            type="button"
            className={`${btnClass} btn-outline-primary`}
            onClick={onAddressChange}
          >
            配送先を変更
          </button>
          <button type="button" className={`${btnClass} btn-danger`} onClick={onCancel}>
            注文をキャンセル
          </button>
        </div>
      );
    }

    if (
      currentStatus === ORDER_STATUS_CODE.PURCHASE_CONFIRMED ||
      currentStatus === ORDER_STATUS_CODE.PREPARING
    ) {
      return (
        <div className="d-flex gap-3">
          {currentStatus === ORDER_STATUS_CODE.PURCHASE_CONFIRMED && (
            <button
              type="button"
              className={`${btnClass} btn-outline-primary`}
              onClick={onAddressChange}
            >
              配送先を変更
            </button>
          )}
          {isExpired ? (
            <button
              type="button"
              className={`${btnClass} btn-secondary`}
              disabled
              title="決済後72時間が経過したため、キャンセルできません。"
            >
              キャンセル期限切れ
            </button>
          ) : (
            <button type="button" className={`${btnClass} btn-danger`} onClick={onCancel}>
              注文をキャンセル
            </button>
          )}
        </div>
      );
    }

    if (currentStatus === ORDER_STATUS_CODE.DELIVERED) {
      return (
        <button
          type="button"
          className={`${btnClass} btn-primary`}
          onClick={() => navigate('/review/write')}
        >
          レビューを書く
        </button>
      );
    }

    return null;
  };

  return (
    <div className="d-flex flex-column align-items-center gap-4 mt-5 pt-4 border-top">
      {renderButtons()}
      <button
        type="button"
        className="btn btn-link text-secondary text-decoration-none small"
        onClick={() => navigate('/mypage/purchases')}
      >
        <i className="bi bi-list-ul me-1"></i> 購入履歴一覧に戻る
      </button>
    </div>
  );
}
