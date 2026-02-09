import { useEffect, useMemo, useState } from 'react';
import axiosInstance from '../services/axiosInstance';
import noImage from '../images/common/CM-NoImage.png';

const PAYMENT_OPTIONS = [
  { code: 'CARD', label: '신용카드 결제 (주문 후 결제)' },
  { code: 'BANK', label: '계좌이체' },
];

// 이미지 URL 보정
const resolveImage = (url) => {
  if (!url) return noImage;
  if (url.startsWith('http')) return url;
  const base = import.meta.env.VITE_API_BASE_IMAGE_URL?.replace(/\/+$/, '') || '';
  return base ? `${base}/${url.replace(/^\/+/, '')}` : noImage;
};

// 숫자 변환
const num = (v, fb = 0) => {
  const n = Number(String(v ?? '').replace(/[^0-9.-]/g, ''));
  return Number.isFinite(n) ? n : fb;
};

// OrderItemInfo or CartItemResponse > front item 모델
const normalizeItems = (raw = []) =>
  raw.map((item, idx) => {
    const quantity = num(item.quantity, 1);
    const unitPrice = num(item.price ?? item.productPrice, 0);
    const lineTotal = num(item.lineTotal, quantity * unitPrice);

    return {
      key: item.orderItemId ?? item.cartItemId ?? idx,
      name: item.productName ?? `상품 ${idx + 1}`,
      quantity,
      unitPrice,
      lineTotal,
      shippingFee: num(item.shippingFee, 0),
      image: resolveImage(item.imageLink),
    };
  });

/** OrderSummary → front orderData 모델 */
const normalizeSummary = (s) =>
  !s
    ? null
    : {
        orderId: s.orderId,
        orderNumber: s.orderNumber,
        totalAmount: num(s.totalAmount),
        shippingFee: num(s.shippingFee),
        ordererName: s.buyerName ?? '',
        ordererEmail: s.buyerEmail ?? '',
        recipientName: s.recipientName ?? '',
        recipientPhone: s.recipientPhone ?? '',
        address: s.address ?? '',
        orderStatus: s.orderStatus,
      };

export function useOrder({ initialOrder, locationState, user, navigate }) {
  const [orderData, setOrderData] = useState(initialOrder || null);
  const [items, setItems] = useState([]);

  const [isOrderLoading, setIsOrderLoading] = useState(false);
  const [orderError, setOrderError] = useState('');

  /** orderId / orderNumber 기준 결정 */
  const orderId = orderData?.orderId ?? initialOrder?.orderId ?? locationState?.orderId ?? null;

  const orderNumber =
    orderData?.orderNumber ?? initialOrder?.orderNumber ?? locationState?.orderNumber ?? null;

  /** 로그인 사용자 memberNumber */
  const memberNumber =
    orderData?.memberNumber ?? locationState?.memberNumber ?? user?.memberNumber ?? null;

  /** 장바구니에서 넘어온 주문 후보(location.state)를 초기 데이터로 사용 */
  useEffect(() => {
    // 이미 orderId가 있는 경우(실제 주문 상세 조회 모드)는 서버에서 조회만 한다.
    if (orderId) return;

    // locationState에 준비된 items가 없으면 아무 것도 하지 않는다.
    if (!locationState || !Array.isArray(locationState.items) || locationState.items.length === 0) {
      return;
    }

    const preparedItems = normalizeItems(locationState.items);
    setItems(preparedItems);

    const totals = locationState.totals || {};
    const shippingFeeFromState = num(totals.shippingFee, 0);
    const totalFromState = num(totals.total, 0);

    // totals 정보가 없을 때를 대비해 프론트에서 한 번 더 계산
    const subtotalFromState =
      totalFromState > 0
        ? totalFromState - shippingFeeFromState
        : preparedItems.reduce((sum, item) => sum + item.lineTotal, 0);

    setOrderData(
      (prev) =>
        prev ?? {
          orderId: null,
          orderNumber: null,
          totalAmount:
            totalFromState > 0 ? totalFromState : subtotalFromState + shippingFeeFromState,
          shippingFee: shippingFeeFromState,
          ordererName: user?.name ?? '',
          ordererEmail: user?.email ?? '',
          recipientName: '',
          recipientPhone: '',
          address: '',
          orderStatus: null,
        }
    );
  }, [orderId, locationState, user]);

  /** 주문 기본 정보 조회 */
  useEffect(() => {
    if (!orderId) return;

    let active = true;
    const controller = new AbortController();

    const load = async () => {
      setIsOrderLoading(true);
      setOrderError('');

      try {
        console.log('[useOrder] request', { orderId, memberNumber });

        const { data } = await axiosInstance.get(`/guest/orders/${orderId}`, {
          params: { memberNumber },
          signal: controller.signal,
        });

        console.log('[useOrder] response', data);

        if (!data?.result) throw new Error(data?.message || '주문 정보를 불러오지 못했습니다.');

        if (!active) return;

        const listSource =
          data.resultList?.orderItems ??
          data.resultList?.items ??
          data.resultList?.orderItemList ??
          [];

        const summary = normalizeSummary(data.resultList);
        const detailItems = normalizeItems(listSource);

        setOrderData(summary);
        setItems(detailItems);
      } catch (err) {
        if (!active) return;

        console.error('[useOrder] error', err);
        setOrderError(
          err?.response?.data?.message || err.message || '주문 정보를 불러오지 못했습니다.'
        );
      } finally {
        active = false;
        setIsOrderLoading(false);
      }
    };

    load();
    return () => {
      active = false;
      controller.abort();
    };
  }, [orderId, memberNumber]);

  /** 수량합 */
  const subtotal = useMemo(() => {
    return items.reduce((s, i) => s + i.lineTotal, 0);
  }, [items]);

  /** 배송비 */
  const shippingFee = orderData?.shippingFee ?? 0;

  /** 총합 */
  const total = subtotal + shippingFee;

  return {
    orderData,
    orderId,
    orderNumber,
    items,
    subtotal,
    shippingFee,
    total,
    memberNumber,
    isOrderLoading,
    orderError,
    paymentOptions: PAYMENT_OPTIONS,
  };
}
