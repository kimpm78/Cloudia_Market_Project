import { useEffect, useMemo, useState } from 'react';
import axiosInstance from '../services/axiosInstance';
import noImage from '../images/common/CM-NoImage.png';

const PAYMENT_OPTIONS = [
  { code: 'CARD', label: 'クレジットカード決済（注文後に決済）' },
  { code: 'BANK', label: '銀行振込' },
];

// 画像URLの補正
const resolveImage = (url) => {
  if (!url) return noImage;
  if (url.startsWith('http')) return url;
  const base = import.meta.env.VITE_API_BASE_IMAGE_URL?.replace(/\/+$/, '') || '';
  return base ? `${base}/${url.replace(/^\/+/, '')}` : noImage;
};

// 数値変換
const num = (v, fb = 0) => {
  const n = Number(String(v ?? '').replace(/[^0-9.-]/g, ''));
  return Number.isFinite(n) ? n : fb;
};

// OrderItemInfo / CartItemResponse → フロント用 item モデル
const normalizeItems = (raw = []) =>
  raw.map((item, idx) => {
    const quantity = num(item.quantity, 1);
    const unitPrice = num(item.price ?? item.productPrice, 0);
    const lineTotal = num(item.lineTotal, quantity * unitPrice);

    return {
      key: item.orderItemId ?? item.cartItemId ?? idx,
      name: item.productName ?? `商品 ${idx + 1}`,
      quantity,
      unitPrice,
      lineTotal,
      shippingFee: num(item.shippingFee, 0),
      image: resolveImage(item.imageLink),
    };
  });

/** OrderSummary → フロント用 orderData モデル */
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

  /** orderId / orderNumber の基準決定 */
  const orderId = orderData?.orderId ?? initialOrder?.orderId ?? locationState?.orderId ?? null;

  const orderNumber =
    orderData?.orderNumber ?? initialOrder?.orderNumber ?? locationState?.orderNumber ?? null;

  /** ログインユーザーの memberNumber */
  const memberNumber =
    orderData?.memberNumber ?? locationState?.memberNumber ?? user?.memberNumber ?? null;

  /** カートから渡された注文候補（location.state）を初期データとして使用 */
  useEffect(() => {
    // すでに orderId がある場合（注文詳細の取得モード）はサーバーから取得のみ行う。
    if (orderId) return;

    // locationState に準備済み items がなければ何もしない。
    if (!locationState || !Array.isArray(locationState.items) || locationState.items.length === 0) {
      return;
    }

    const preparedItems = normalizeItems(locationState.items);
    setItems(preparedItems);

    const totals = locationState.totals || {};
    const shippingFeeFromState = num(totals.shippingFee, 0);
    const totalFromState = num(totals.total, 0);

    // totals 情報がない場合に備えてフロント側で再計算
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

  /** 注文基本情報の取得 */
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

        if (!data?.result) throw new Error(data?.message || '注文情報を取得できませんでした。');

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
          err?.response?.data?.message || err.message || '注文情報を取得できませんでした。'
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

  /** 小計 */
  const subtotal = useMemo(() => {
    return items.reduce((s, i) => s + i.lineTotal, 0);
  }, [items]);

  /** 送料 */
  const shippingFee = orderData?.shippingFee ?? 0;

  /** 合計 */
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
