import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';
import { deriveShippingInfo } from '../utils/shippingInfo';
import { useOrder } from '../hooks/useOrder';
import { useAddress } from '../hooks/useAddress';
import noImage from '../images/common/CM-NoImage.png';

import CM_06_1000_stepIndicator from '../components/CM_06_1000_stepIndicator';
import CM_06_1001_Address from '../components/CM_06_1001_Address';
import CM_06_1001_Payment from '../components/CM_06_1001_Payment';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CMMessage from '../constants/CMMessage';

import '../styles/CM_06_1001.css';

const PG_LAST_TID_KEY = 'PG_LAST_TID';

const safeJsonParse = (raw) => {
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
};

const readCachedTidForOrder = (orderId, orderNumber) => {
  const parsed = safeJsonParse(localStorage.getItem(PG_LAST_TID_KEY));
  if (!parsed?.tid) return null;

  const matchOrderId =
    parsed?.orderId != null && orderId != null && String(parsed.orderId) === String(orderId);
  const matchOrderNumber =
    parsed?.orderNumber != null &&
    orderNumber != null &&
    String(parsed.orderNumber) === String(orderNumber);

  if (!matchOrderId && !matchOrderNumber) return null;
  return String(parsed.tid);
};

const isTrustedCookiepayHtml = (htmlText) => {
  const text = typeof htmlText === 'string' ? htmlText : String(htmlText ?? '');
  return (
    text.includes('cookiepayments.com') ||
    text.includes('payverseglobal.com') ||
    text.includes('cookiepayments-')
  );
};

export default function CM_06_1001() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [isPaymentLoading, setIsPaymentLoading] = useState(false);
  const paymentPopupRef = useRef(null);
  const popupCloseWatcherRef = useRef(null);
  const paymentResultReceivedRef = useRef(false);
  const paymentContextRef = useRef(null);
  const autoCardSubmitHandledRef = useRef(false);
  const [isNoAddressPopupOpen, setIsNoAddressPopupOpen] = useState(false);

  const [paymentMethod, setPaymentMethod] = useState({
    code: 'CARD',
    label: 'カード決済',
  });

  const locationState =
    location.state && typeof location.state === 'object' ? location.state : null;

  const initialOrder = locationState?.order || null;

  // 注文データ読み込みフック
  const {
    orderData,
    items,
    subtotal,
    shippingFee,
    total,
    memberNumber,
    isOrderLoading,
    orderError,
    paymentOptions,
  } = useOrder({ initialOrder, locationState, user, navigate });
  const memberNumberReady = Boolean(memberNumber);
  const authRedirectedRef = useRef(false);
  const clearPopupWatcher = () => {
    if (popupCloseWatcherRef.current) {
      window.clearInterval(popupCloseWatcherRef.current);
      popupCloseWatcherRef.current = null;
    }
  };

  const closePopup = () => {
    const popup = paymentPopupRef.current;
    if (popup && !popup.closed) {
      popup.close();
    }
    paymentPopupRef.current = null;
  };

  useEffect(() => {
    if (memberNumberReady || authRedirectedRef.current) {
      return;
    }
    authRedirectedRef.current = true;
    navigate('/login', {
      replace: true,
      state: { from: { pathname: location.pathname, state: location.state } },
    });
  }, [memberNumberReady, navigate, location.pathname, location.state]);

  // 注文者情報：基本はログインユーザー情報、バックエンドの要約情報は補助として使用
  const defaultOrdererName =
    user?.name ||
    orderData?.ordererName ||
    orderData?.buyerName ||
    user?.loginId ||
    (user?.userId ? `${user.userId}` : '');

  const ordererName = defaultOrdererName || '-';

  const ordererEmail = user?.email || orderData?.ordererEmail || orderData?.buyerEmail || '-';

  const defaultReceiverName =
    orderData?.recipientName || orderData?.receiverName || orderData?.shippingName || '';

  const defaultReceiverPhone =
    orderData?.recipientPhone ||
    orderData?.receiverPhone ||
    orderData?.shippingPhone ||
    orderData?.deliveryPhone ||
    '';

  const shippingDefaults = useMemo(
    () => ({
      recipientName: defaultReceiverName,
      recipientPhone: defaultReceiverPhone,
      postalCode: orderData?.shipping?.postalCode ?? '',
      addressMain: orderData?.shipping?.addressMain ?? '',
    }),
    [defaultReceiverName, defaultReceiverPhone, orderData]
  );

  const initialShippingOverride = null;

  const {
    deliveryAddresses,
    isAddressLoading,
    addressError,
    showAddressModal,
    openAddressModal,
    closeAddressModal,
    selectedAddressId,
    handleAddressRadioChange,
    applySelectedAddress,
    shippingOverride,
    currentAddressRecord,
    isSavingDefault,
    handleToggleDefaultCheckbox,
  } = useAddress({
    memberNumber,
    shippingDefaults,
    initialShippingOverride,
  });

  const shippingInfo = useMemo(
    () =>
      deriveShippingInfo({
        orderData,
        shippingOverride,
        selectedAddress: currentAddressRecord,
        userDefaults: shippingDefaults,
      }),
    [orderData, shippingOverride, currentAddressRecord, shippingDefaults]
  );

  useEffect(() => {
    const handlePaymentMessage = (event) => {
      if (!event || !event.data) return;
      if (event.origin !== window.location.origin) return;
      const data = event.data;

      // payment-return.html から送られる形式: { type: "PG_PAYMENT_RESULT", payload: {...} }
      let paymentData;
      if (data.type === 'PG_PAYMENT_RESULT' && data.payload) {
        paymentData = data.payload;
      } else if (data.paymentStatus) {
        // paymentStatus が直接ある場合（下位互換）
        paymentData = data;
      } else {
        return;
      }

      const { paymentStatus, ...rest } = paymentData;
      if (!paymentStatus) return;

      const ctx = paymentContextRef.current || {};
      const merged = {
        ...ctx,
        ...rest,
        paymentStatus,
      };

      paymentResultReceivedRef.current = true;
      clearPopupWatcher();
      paymentPopupRef.current = null;

      navigate('/order-payment/result', { replace: true, state: merged });
    };

    window.addEventListener('message', handlePaymentMessage);
    return () => {
      window.removeEventListener('message', handlePaymentMessage);
      clearPopupWatcher();
    };
  }, [navigate]);

  if (!memberNumberReady) {
    return (
      <div className="container my-5 py-5 text-center">
        <div className="spinner-border text-primary mb-3" role="status" aria-hidden="true" />
      </div>
    );
  }

  const currentReceiverName = shippingInfo.recipientName || '';
  const currentReceiverPhone = shippingInfo.recipientPhone || '';
  const currentAddressNickname = shippingInfo.addressNickname || null;
  const receiverAddress = shippingInfo.displayAddress || '';
  const isPaymentDisabled =
    isOrderLoading || isPaymentLoading || items.length === 0 || Boolean(orderError);
  const isLocalCardMockEnabled =
    typeof window !== 'undefined' &&
    (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

  const navigateToAddressBook = () => {
    navigate('/mypage/address-book');
  };

  const handlePayment = async (localCardInfo = null) => {
    if (isPaymentDisabled) {
      return;
    }

    if (!isAddressLoading && !addressError) {
      const hasAddress = Array.isArray(deliveryAddresses) && deliveryAddresses.length > 0;
      if (!hasAddress) {
        setIsNoAddressPopupOpen(true);
        return;
      }
    }

    if (!memberNumber) {
      alert(CMMessage.MSG_ERR_011);
      navigate('/login', { replace: true });
      return;
    }

    const cartItemIdsFromState =
      locationState && Array.isArray(locationState.cartItemIds) ? locationState.cartItemIds : [];

    if (cartItemIdsFromState.length === 0) {
      alert(CMMessage.MSG_ERR_026);
      navigate('/cart', { replace: true });
      return;
    }

    if (isLocalCardMockEnabled && paymentMethod.code === 'CARD' && !localCardInfo) {
      navigate('/order-payment/card-input', {
        state: locationState || {},
      });
      return;
    }

    setIsPaymentLoading(true);

    try {
      // バックエンドで使用する決済方法コードのマッピング（"1": 銀行振込, "2": カード）
      const paymentMethodValue = paymentMethod.code === 'BANK' ? '1' : '2';

      // 最小限の配送情報 payload 構成（addressId 含む）
      const shippingPayload = {
        recipientName: currentReceiverName,
        recipientPhone: currentReceiverPhone,
        postalCode: shippingInfo.postalCode || '',
        addressMain: shippingInfo.addressMain || receiverAddress,
        memo: shippingInfo.memo || '',
      };
      if (shippingInfo.addressId != null) {
        shippingPayload.addressId = shippingInfo.addressId;
      }
      if (shippingInfo.addressNickname) {
        shippingPayload.addressNickname = shippingInfo.addressNickname;
      }
      if (shippingInfo.addressDetail1) {
        shippingPayload.addressDetail1 = shippingInfo.addressDetail1;
      }
      if (shippingInfo.addressDetail2) {
        shippingPayload.addressDetail2 = shippingInfo.addressDetail2;
      }
      if (shippingInfo.addressDetail3) {
        shippingPayload.addressDetail3 = shippingInfo.addressDetail3;
      }

      // 注文作成リクエスト payload
      const orderCreatePayload = {
        memberNumber,
        cartItemIds: cartItemIdsFromState,
        shippingFee,
        totalAmount: total,
        paymentMethod: paymentMethodValue,
        shipping: shippingPayload,
      };

      const orderRes = await axiosInstance.post('/guest/order/create', orderCreatePayload);
      const createdOrder = orderRes.data?.resultList;

      const createdOrderId = createdOrder?.orderId ?? createdOrder?.orderID;
      const createdOrderNumber = createdOrder?.orderNumber;

      if (!createdOrderId || !createdOrderNumber) {
        throw new Error(CMMessage.MSG_ERR_027);
      }

      paymentContextRef.current = {
        orderId: createdOrderId,
        orderNumber: createdOrderNumber,
        paymentMethod: paymentMethod.code,
        pgType: 'COOKIEPAY',
        memberNumber,
        from: 'checkout',
        ts: Date.now(),
      };

      // 銀行振込：PG なしで完了／案内ページへ遷移（決済状態はサーバ側で「準備」として管理）
      if (paymentMethod.code === 'BANK') {
        navigate('/order-payment/result', {
          replace: true,
          state: {
            paymentStatus: 'success',
            paymentMethod: 'BANK',
            orderNumber: createdOrderNumber,
            orderId: createdOrderId,
          },
        });
        return;
      }

      // ローカル開発環境：カード決済はモック完了として処理
      if (isLocalCardMockEnabled && paymentMethod.code === 'CARD') {
        navigate('/order-payment/result', {
          replace: true,
          state: {
            paymentStatus: 'success',
            paymentMethod: 'CARD',
            pgType: 'LOCAL_MOCK',
            orderNumber: createdOrderNumber,
            orderId: createdOrderId,
            tid: `LOCAL-${Date.now()}`,
            cardLast4: localCardInfo?.cardNumber?.slice(-4) || '',
          },
        });
        return;
      }

      // カード決済：PG 連携
      paymentResultReceivedRef.current = false;
      clearPopupWatcher();

      const popup = window.open('', '_blank', 'width=680,height=720');
      if (!popup) {
        alert(CMMessage.MSG_ERR_028);
        return;
      }
      paymentPopupRef.current = popup;

      // ポップアップのクローズ監視（ユーザーキャンセル検知）
      popupCloseWatcherRef.current = window.setInterval(() => {
        const p = paymentPopupRef.current;
        if (!p) {
          return;
        }
        if (paymentResultReceivedRef.current) {
          return;
        }
        if (p.closed) {
          const cachedTid = readCachedTidForOrder(createdOrderId, createdOrderNumber);

          paymentResultReceivedRef.current = true;
          clearPopupWatcher();
          paymentPopupRef.current = null;

          const goToResult = (tidValue) => {
            navigate('/order-payment/result', {
              replace: true,
              state: {
                pgType: 'COOKIEPAY',
                paymentStatus: 'cancel',
                orderId: createdOrderId,
                orderNumber: createdOrderNumber,
                paymentMethod: paymentMethod.code,
                tid: tidValue,
                cancelReason: 'USER_CLOSED_WINDOW',
                from: 'checkout',
                ts: Date.now(),
              },
            });
          };

          if (cachedTid) {
            goToResult(cachedTid);
            return;
          }

          axiosInstance
            .get('/pay/tid', {
              params: {
                orderId: createdOrderId,
                orderNumber: createdOrderNumber,
              },
            })
            .then((response) => {
              const tidValue = response?.data?.resultList?.tid || null;
              goToResult(tidValue);
            })
            .catch(() => {
              goToResult(null);
            });
        }
      }, 800);

      const payload = {
        orderId: createdOrderId,
        orderNumber: createdOrderNumber,
        pgType: 'COOKIEPAY',
        amount: total,
        productName: items[0]?.name || '商品',
        buyerName: ordererName,
        buyerEmail: ordererEmail,
        homeUrl: window.location.origin,
        payMethod: paymentMethod.code,
        quota: '01',
        payType: '7',
        cardList: 'CCXX:CCUF',
      };

      const payReady = await axiosInstance.post('/pay/ready', payload);
      try {
        const orderContext = {
          orderId: createdOrderId,
          orderNumber: createdOrderNumber,
          pgOrderNo: payReady.data?.resultList?.pgOrderNo || null,
          paymentMethod: paymentMethod.code,
          amount: total,
          apiBase: axiosInstance.defaults.baseURL || '',
        };
        localStorage.setItem('PG_ORDER_CONTEXT', JSON.stringify(orderContext));
        sessionStorage.setItem('PG_ORDER_CONTEXT', JSON.stringify(orderContext));
      } catch (error) {
        // ignore
      }
      const redirectUrl = payReady.data?.resultList?.redirectUrl || payReady.data?.resultList?.url;

      if (redirectUrl) {
        popup.location.href = redirectUrl;
        return;
      }

      const html = payReady.data?.resultList?.html;
      if (!html) {
        closePopup();
        alert(CMMessage.MSG_ERR_005('決済画面情報'));
        return;
      }

      const htmlText = typeof html === 'string' ? html : String(html);
      if (!isTrustedCookiepayHtml(htmlText)) {
        closePopup();
        console.warn('[ORDER/PAY READY] Untrusted HTML payload blocked.');
        alert(CMMessage.MSG_ERR_030);
        return;
      }

      popup.document.open();
      popup.document.write(htmlText);
      popup.document.close();
    } catch (error) {
      console.error('[ORDER/PAY READY ERROR]', error);
      alert(error?.response?.data?.message || CMMessage.MSG_ERR_031);
    } finally {
      setIsPaymentLoading(false);
    }
  };

  useEffect(() => {
    if (!isLocalCardMockEnabled) return;
    if (paymentMethod.code !== 'CARD') return;
    if (!locationState?.autoSubmitCard || !locationState?.localCardInfo) return;
    if (autoCardSubmitHandledRef.current) return;

    autoCardSubmitHandledRef.current = true;
    const { autoSubmitCard, localCardInfo, ...cleanedState } = locationState;
    navigate('/order-payment', { replace: true, state: cleanedState });
    handlePayment(localCardInfo);
  }, [isLocalCardMockEnabled, paymentMethod.code, locationState, navigate]);

  return (
    <>
      <div className="container my-5">
        <div className="row">
          <div className="col-12 col-lg-8">
            <div className="d-flex flex-column flex-sm-row mb-3">
              <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">カート</h1>
              <div className="d-flex flex-wrap flex-sm-nowrap align-items-sm-end">
                <CM_06_1000_stepIndicator currentStep={1} />
              </div>
            </div>
            <hr />
            {isOrderLoading && <div className="alert alert-info py-2" role="alert"></div>}
            {orderError && (
              <div className="alert alert-danger py-2" role="alert">
                {orderError}
              </div>
            )}

            <h5 className="pb-2">商品情報</h5>
            {!isOrderLoading && items.length === 0 ? (
              <div className="py-3 text-muted">{CMMessage.MSG_EMPTY_014}</div>
            ) : (
              items.map((item) => (
                <div key={item.key} className="d-flex border-bottom py-3 align-items-center">
                  <div
                    className="cm-06-1001_thumb"
                  >
                    <img
                      src={item.image || noImage}
                      alt={item.name}
                      className="img-fluid cm-06-1001_product-image"
                      onError={(event) => {
                        if (event.currentTarget.src !== noImage) {
                          event.currentTarget.src = noImage;
                        }
                      }}
                    />
                  </div>
                  <div className="ms-3 flex-grow-1">
                    <div>{item.name}</div>
                    {item.option && <div className="text-muted small mt-1">{item.option}</div>}
                    <div className="fw-bold text-primary mt-2">
                      {item.unitPrice.toLocaleString()}円
                    </div>
                    <div className="mt-2 d-flex justify-content-between align-items-center w-100">
                      <div className="text-muted">数量 {item.quantity}</div>
                      <div className="fw-semibold">{item.lineTotal.toLocaleString()}円</div>
                    </div>
                  </div>
                </div>
              ))
            )}

            <CM_06_1001_Payment
              paymentMethod={paymentMethod}
              paymentOptions={paymentOptions}
              onSelectPaymentMethod={setPaymentMethod}
              onRequestPayment={handlePayment}
            />
            <h5 className="border-bottom pb-2 mt-4 fw-semibold">注文者情報</h5>
            <div className="py-2">
              <span className="fw-semibold pe-4">氏名</span>
              {ordererName}
            </div>
            <div className="border-bottom py-2">
              <span className="fw-semibold pe-3">メール</span>
              {ordererEmail}
            </div>
            {/* 配送先情報 */}
            <CM_06_1001_Address
              currentAddressNickname={currentAddressNickname}
              currentReceiverName={currentReceiverName}
              currentReceiverPhone={currentReceiverPhone}
              receiverAddress={receiverAddress}
              deliveryAddresses={deliveryAddresses}
              isAddressLoading={isAddressLoading}
              addressError={addressError}
              showAddressModal={showAddressModal}
              openAddressModal={openAddressModal}
              closeAddressModal={closeAddressModal}
              selectedAddressId={selectedAddressId}
              handleAddressRadioChange={handleAddressRadioChange}
              applySelectedAddress={applySelectedAddress}
              navigateToAddressBook={navigateToAddressBook}
              shippingOverride={shippingOverride}
              currentAddressRecord={currentAddressRecord}
              isSavingDefault={isSavingDefault}
              handleToggleDefaultCheckbox={handleToggleDefaultCheckbox}
            />
          </div>

          <div className="col-12 col-lg-4 d-flex flex-column align-items-end mt-4 mt-lg-0 cm-06-1001_summary">
            <div className="cm-06-1001_summary-card">
              <hr className="mb-3" />
              <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
                <span>商品合計</span>
                <strong>{subtotal.toLocaleString()}円</strong>
              </div>
              <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
                <span>送料</span>
                <strong>{shippingFee.toLocaleString()}円</strong>
              </div>
              <div className="d-flex justify-content-between mt-3 mb-2">
                <span className="fw-bold">お支払い合計</span>
                <span className="fw-bold fs-5">{total.toLocaleString()}円</span>
              </div>
              <button
                className="btn btn-primary w-100 mb-2 mt-3"
                onClick={handlePayment}
                disabled={isPaymentDisabled}
              >
                {isPaymentLoading ? '決済準備中...' : '決済する'}
              </button>
              <button className="btn btn-secondary w-100" onClick={() => navigate('/cart')}>
                カートへ戻る
              </button>

              <div className="text-danger mt-3 cm-06-1001_summary-warning">
                <div>
                  <i className="bi bi-exclamation-circle me-1"></i>
                  商品不良の場合を除き、返品はできません。
                </div>
                <div className="mt-2 ms-3">キャンセルは決済手段により異なります。</div>
                <div className="mt-2 ms-3">
                  詳細はこちら <i className="bi bi-caret-right-fill mx-1"></i>
                  <a
                    href="/terms?type=e-commerce"
                    className="text-danger text-decoration-none"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    [ガイド - 返品・交換・キャンセル]
                  </a>
                </div>
                <div className="mt-2">
                  <i className="bi bi-exclamation-circle me-1"></i>
                  未成年の方は、保護者など法定代理人の同意を得たうえでご利用ください。
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <CM_99_1001
        isOpen={isNoAddressPopupOpen}
        onClose={() => setIsNoAddressPopupOpen(false)}
        onConfirm={() => {
          setIsNoAddressPopupOpen(false);
          navigateToAddressBook();
        }}
        Message={CMMessage.MSG_VAL_012}
      />
    </>
  );
}
