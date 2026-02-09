import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import CM_06_1000_stepIndicator from '../CM_06_1000_stepIndicator';
import axiosInstance from '../../services/axiosInstance';
import { useAuth } from '../../contexts/AuthContext';

const CART_UPDATED_EVENT = 'cart:updated';

export default function OrderPaymentResult() {
  const { state } = useLocation();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { isLoggedIn, user } = useAuth();
  const queryStatus = params.get('status');
  const queryOrderNumber = params.get('orderNumber') || params.get('orderNo');
  const queryOrderId = params.get('orderId');
  const queryPaymentMethod = params.get('paymentMethod');
  const queryCancelReason = params.get('cancelReason') || params.get('reason');

  const resolvedState = {
    paymentStatus: state?.paymentStatus ?? queryStatus ?? null,
    orderNumber: state?.orderNumber ?? queryOrderNumber ?? null,
    orderId: state?.orderId ?? queryOrderId ?? null,
    paymentMethod: state?.paymentMethod ?? queryPaymentMethod ?? null,
    cancelReason: state?.cancelReason ?? queryCancelReason ?? null,
  };

  const [displayOrderNumber, setDisplayOrderNumber] = useState(
    resolvedState.orderNumber ?? null
  );

  useEffect(() => {
    setDisplayOrderNumber(resolvedState.orderNumber ?? null);
  }, [resolvedState.orderNumber]);

  useEffect(() => {
    if (!isLoggedIn || !user?.userId) {
      return;
    }

    let alive = true;
    const refresh = async () => {
      try {
        const res = await axiosInstance.get('/guest/menus/cart/count', {
          params: { userId: user.userId },
        });
        const count = res.data;
        if (!alive || typeof count !== 'number' || count < 0) {
          return;
        }
        window.dispatchEvent(
          new CustomEvent(CART_UPDATED_EVENT, { detail: { count, source: '주문 요청' } })
        );
      } catch (error) {
        console.warn('[주문 요청] cart count refresh failed', error);
      }
    };

    refresh();
    return () => {
      alive = false;
    };
  }, [isLoggedIn, user?.userId]);

  useEffect(() => {
    if (!isLoggedIn || !resolvedState.orderId) {
      return;
    }

    let alive = true;
    const loadOrderNumber = async () => {
      try {
        const res = await axiosInstance.get(`/guest/orders/${resolvedState.orderId}`);
        const orderNumber = res.data?.resultList?.orderNumber ?? null;
        if (alive && orderNumber) {
          setDisplayOrderNumber(orderNumber);
        }
      } catch (error) {
        console.warn('[주문 결과] order number load failed', error);
      }
    };

    loadOrderNumber();
    return () => {
      alive = false;
    };
  }, [isLoggedIn, resolvedState.orderId]);

  if (!resolvedState || !resolvedState.paymentStatus) {
    return (
      <div className="container py-5">
        <h2 className="fw-bold mb-4">잘못된 접근입니다.</h2>
        <div className="p-4 bg-light rounded">
          <p>결제 결과 정보를 불러올 수 없습니다.</p>
        </div>
        <div className="text-center mt-4">
          <button className="btn btn-primary" onClick={() => navigate('/')}>
            메인 화면으로 이동
          </button>
        </div>
      </div>
    );
  }

  const { paymentStatus, paymentMethod, cancelReason } = resolvedState;

  const stepStatus =
    paymentStatus === 'success' ? 'success' : paymentStatus === 'pending' ? 'pending' : 'fail';

	  const title =
	    paymentStatus === 'success'
	      ? '주문 완료'
	      : paymentStatus === 'pending'
	        ? '결제 대기 상태'
	        : paymentStatus === 'cancel'
	          ? '결제가 취소되었습니다'
	          : paymentStatus === 'error'
	            ? '결제 오류'
	            : '결제 실패';

  return (
    <div className="container py-5">
      <div className="d-flex flex-column flex-sm-row mb-3">
        <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">{title}</h1>
        <div className="d-flex flex-wrap flex-sm-nowrap align-items-sm-end">
          <CM_06_1000_stepIndicator currentStep={2} status={stepStatus} />
        </div>
      </div>

      <hr />

      <div className="p-4 rounded bg-light fs-6 lh-lg">
        {paymentStatus === 'success' && (
          <>
            <p className="mb-1">주문해주셔서 감사합니다.</p>
            <p className="mb-0">
              주문 번호는 <strong className="text-primary">[{displayOrderNumber}]</strong> 입니다.&nbsp;
              {paymentMethod === 'BANK'
                ? '송금할 계좌 이체 번호는 이메일로 보내드리겠습니다.'
                : '주문 확인 내용은 이메일로 보내드리겠습니다.'}
            </p>
          </>
        )}

        {paymentStatus === 'pending' && (
          <>
            <p className="mb-1">계좌이체 결제를 선택하셨습니다.</p>
            <p className="mb-0">
              송금하실 계좌 정보는 이메일로 안내드렸습니다.
              <br />
              주문 번호: <strong className="text-primary">[{displayOrderNumber}]</strong>
            </p>
          </>
        )}

        {paymentStatus === 'cancel' && (
          <p className="mb-0">
            사용자가 결제를 취소했습니다.
            {cancelReason ? ` (${cancelReason})` : ''}
          </p>
        )}

	        {(paymentStatus === 'fail' || paymentStatus === 'error') && (
	          <p className="text-danger mb-0">
	            결제에 {paymentStatus === 'error' ? '오류가 발생했습니다' : '실패했습니다'}. 잠시 후 다시
	            시도해주세요.
	            {cancelReason ? ` (${cancelReason})` : ''}
	          </p>
	        )}
	      </div>

      <div className="text-center mt-5">
        <Link to="/" className="btn btn-primary px-5 py-2">
          메인 화면으로 이동
        </Link>
      </div>
    </div>
  );
}
