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
  const [isNoAddressPopupOpen, setIsNoAddressPopupOpen] = useState(false);

  const [paymentMethod, setPaymentMethod] = useState({
    code: 'CARD',
    label: '카드 결제',
  });

  const locationState =
    location.state && typeof location.state === 'object' ? location.state : null;

  const initialOrder = locationState?.order || null;

  // 주문 데이터 로딩 훅
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

  // 주문자 정보: 기본은 로그인 사용자 정보, 백엔드 요약 정보는 보조로 사용
  const defaultOrdererName =
    user?.name ||
    orderData?.ordererName ||
    orderData?.buyerName ||
    user?.loginId ||
    (user?.userId ? `${user.userId}` : '');

  const ordererName = defaultOrdererName || '-';

  const ordererEmail = user?.email || orderData?.ordererEmail || orderData?.buyerEmail || '-';

  const defaultReceiverPhone =
    orderData?.recipientPhone ||
    orderData?.receiverPhone ||
    orderData?.shippingPhone ||
    orderData?.contactNumber ||
    orderData?.deliveryPhone ||
    user?.phone ||
    '';

  const shippingDefaults = useMemo(
    () => ({
      recipientName: ordererName,
      recipientPhone: defaultReceiverPhone,
      postalCode:
        orderData?.shipping?.postalCode ?? orderData?.zipCode ?? orderData?.postalCode ?? '',
      addressMain: orderData?.shipping?.addressMain ?? orderData?.address ?? '',
    }),
    [ordererName, defaultReceiverPhone, orderData]
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

      // 🔧 type과 payload 구조 처리
      const data = event.data;

      // payment-return.html에서 보내는 구조: { type: "PG_PAYMENT_RESULT", payload: {...} }
      let paymentData;
      if (data.type === 'PG_PAYMENT_RESULT' && data.payload) {
        paymentData = data.payload;
      } else if (data.paymentStatus) {
        // 직접 paymentStatus가 있는 경우 (하위 호환)
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

  const navigateToAddressBook = () => {
    navigate('/mypage/address-book');
  };

  const handlePayment = async () => {
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

    setIsPaymentLoading(true);

    try {
      // 백엔드에서 사용하는 결제방법 코드 매핑 ("1": 계좌이체, "2": 카드)
      const paymentMethodValue = paymentMethod.code === 'BANK' ? '1' : '2';

      // 최소한의 배송 정보 payload 구성 (addressId 포함)
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

      // 주문 생성 요청 payload
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

      // 계좌이체: PG 없이 완료/안내 페이지로 이동 (결제 상태는 서버에서 "준비"로 관리)
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

      // 카드 결제: PG 연동
      paymentResultReceivedRef.current = false;
      clearPopupWatcher();

      const popup = window.open('', '_blank', 'width=680,height=720');
      if (!popup) {
        alert(CMMessage.MSG_ERR_028);
        return;
      }
      paymentPopupRef.current = popup;

      // 팝업 닫힘 감시 (사용자 취소 감지)
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
        productName: items[0]?.name || '상품',
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
        alert(CMMessage.MSG_ERR_005('결제창 정보'));
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

  return (
    <>
      <div className="container my-5">
        <div className="row">
        <div className="col-12 col-lg-8">
          <div className="d-flex flex-column flex-sm-row mb-3">
            <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0">장바구니</h1>
            <div className="d-flex flex-wrap flex-sm-nowrap align-items-sm-end">
              <CM_06_1000_stepIndicator currentStep={1} />
            </div>
          </div>
          <hr />
          {isOrderLoading && (
            <div className="alert alert-info py-2" role="alert">
            </div>
          )}
          {orderError && (
            <div className="alert alert-danger py-2" role="alert">
              {orderError}
            </div>
          )}

          <h5 className="pb-2">상품 정보</h5>
          {!isOrderLoading && items.length === 0 ? (
            <div className="py-3 text-muted">{CMMessage.MSG_EMPTY_014}</div>
          ) : (
            items.map((item) => (
              <div key={item.key} className="d-flex border-bottom py-3 align-items-center">
                <div className="CM-06-1001__thumb">
                  <img
                    src={item.image || noImage}
                    alt={item.name}
                    className="img-fluid CM-06-1001__product-image"
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
                    {item.unitPrice.toLocaleString()} 원
                  </div>
                  <div className="mt-2 d-flex justify-content-between align-items-center w-100">
                    <div className="text-muted">수량 {item.quantity}</div>
                    <div className="fw-semibold">{item.lineTotal.toLocaleString()} 원</div>
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
          <h5 className="border-bottom pb-2 mt-4">주문자 정보</h5>
          <div className="py-2">
            <span className="fw-semibold pe-4">이름</span>
            {ordererName}
          </div>
          <div className="border-bottom py-2">
            <span className="fw-semibold pe-3">이메일</span>
            {ordererEmail}
          </div>
          {/* 배송지 정보 */}
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

        <div className="col-12 col-lg-4 d-flex flex-column align-items-end mt-4 mt-lg-0 CM-06-1001__summary">
          <div className="CM-06-1001__summary-card">
            <hr className="mb-3" />
            <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
              <span>총 상품 금액</span>
              <strong>{subtotal.toLocaleString()}원</strong>
            </div>
            <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
              <span>배송비</span>
              <strong>{shippingFee.toLocaleString()}원</strong>
            </div>
            <div className="d-flex justify-content-between mt-3 mb-2">
              <span className="fw-bold">총 결제 금액</span>
              <span className="fw-bold fs-5">{total.toLocaleString()}원</span>
            </div>
            <button
              className="btn btn-primary w-100 mb-2 mt-3"
              onClick={handlePayment}
              disabled={isPaymentDisabled}
            >
              {isPaymentLoading ? '결제 준비 중...' : '결제하기'}
            </button>
            <button className="btn btn-secondary w-100" onClick={() => navigate('/cart')}>
              장바구니로 이동
            </button>

            <div className="text-danger mt-3 CM-06-1001__summary-warning">
              <div>
                <i className="bi bi-exclamation-circle me-1"></i>
                상품 불량의 경우를 제외하고는, 반품은 불가능합니다.
              </div>
              <div className="mt-2 ms-3">취소는 결제 수단에 따라 다릅니다.</div>
              <div className="mt-2 ms-3">
                자세한 내용 <i className="bi bi-caret-right-fill mx-1"></i>
                <a
                  href="/terms?type=e-commerce"
                  className="text-danger text-decoration-none"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  [가이드 - 반품・교환・취소]
                </a>
              </div>
              <div className="mt-2">
                <i className="bi bi-exclamation-circle me-1"></i>
                미성년자의 경우, 부모 등 법정대리인의 동의를 받은 후 이용해 주시기 바랍니다.
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
