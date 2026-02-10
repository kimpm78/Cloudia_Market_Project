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
          new CustomEvent(CART_UPDATED_EVENT, { detail: { count, source: '注文リクエスト' } })
        );
      } catch (error) {
        console.warn('[注文リクエスト] cart count refresh failed', error);
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
        console.warn('[注文結果] order number load failed', error);
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
        <h2 className="fw-bold mb-4">不正なアクセスです。</h2>
        <div className="p-4 bg-light rounded">
          <p>決済結果情報を取得できません。</p>
        </div>
        <div className="text-center mt-4">
          <button className="btn btn-primary" onClick={() => navigate('/')}>
            メイン画面へ移動
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
	      ? '注文完了'
	      : paymentStatus === 'pending'
	        ? '決済待ち'
	        : paymentStatus === 'cancel'
	          ? '決済がキャンセルされました'
	          : paymentStatus === 'error'
	            ? '決済エラー'
	            : '決済失敗';

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
            <p className="mb-1">ご注文ありがとうございます。</p>
            <p className="mb-0">
              注文番号は <strong className="text-primary">[{displayOrderNumber}]</strong> です。&nbsp;
              {paymentMethod === 'BANK'
                ? 'お振込み先の口座情報はメールでご案内いたします。'
                : 'ご注文内容の確認メールをお送りいたします。'}
            </p>
          </>
        )}

        {paymentStatus === 'pending' && (
          <>
            <p className="mb-1">銀行振込でのお支払いを選択しました。</p>
            <p className="mb-0">
              お振込み先の口座情報はメールでご案内しました。
              <br />
              注文番号: <strong className="text-primary">[{displayOrderNumber}]</strong>
            </p>
          </>
        )}

        {paymentStatus === 'cancel' && (
          <p className="mb-0">
            お客様が決済をキャンセルしました。
            {cancelReason ? ` (${cancelReason})` : ''}
          </p>
        )}

	        {(paymentStatus === 'fail' || paymentStatus === 'error') && (
	          <p className="text-danger mb-0">
	            決済に {paymentStatus === 'error' ? 'エラーが発生しました' : '失敗しました'}. しばらくしてから再度お試しください。
	            {cancelReason ? ` (${cancelReason})` : ''}
	          </p>
	        )}
	      </div>

      <div className="text-center mt-5">
        <Link to="/" className="btn btn-primary px-5 py-2">
          メイン画面へ移動
        </Link>
      </div>
    </div>
  );
}
