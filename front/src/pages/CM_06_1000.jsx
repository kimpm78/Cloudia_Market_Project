import { useEffect, useState, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axiosInstance from '../services/axiosInstance';

import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_06_1000_stepIndicator from '../components/CM_06_1000_stepIndicator';
import { isReservationProduct } from '../utils/productPageFilters';
import CMMessage from '../constants/CMMessage';
const CART_UPDATED_EVENT = 'cart:updated';

export default function CM_06_1000() {
  const navigate = useNavigate();
  const topRef = useRef(null);
  const hasInitializedSelectionRef = useRef(false);
  const { isLoggedIn, user } = useAuth();
  const rawUserId = user?.userId;
  const userId =
    rawUserId !== undefined && rawUserId !== null && rawUserId !== '' ? Number(rawUserId) : null;
  const rawMemberNumber = user?.memberNumber;
  const memberNumber =
    typeof rawMemberNumber === 'string' && rawMemberNumber.trim().length > 0
      ? rawMemberNumber.trim()
      : Number.isFinite(userId)
        ? String(userId)
        : null;

  // カート状態
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openCartModal, setOpenCartModal] = useState(false);
  const [cartMessage, setCartMessage] = useState('');
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteTargetId, setDeleteTargetId] = useState(null);
  const [selectedCartItemIds, setSelectedCartItemIds] = useState([]);
  const [bulkDeleteMode, setBulkDeleteMode] = useState(false);

  // 合計・採算
  const subtotal = useMemo(() => items.reduce((sum, it) => sum + (it.lineTotal || 0), 0), [items]);
  const shippingFee = useMemo(
    () => items.reduce((sum, it) => sum + (it.shippingFee || 0), 0),
    [items]
  );
  const total = subtotal + shippingFee;

  const isAllSelected = useMemo(
    () => items.length > 0 && items.every((it) => selectedCartItemIds.includes(it.cartItemId)),
    [items, selectedCartItemIds]
  );

  const hasSelected = selectedCartItemIds.length > 0;

  // 画像URLヘルパー（一覧ページと同一スタイル）
  const getImageUrl = (url) => {
    const PLACEHOLDER = 'https://placehold.jp/100x100.png';
    if (!url || typeof url !== 'string') return PLACEHOLDER;
    try {
      if (url.startsWith('http')) return url;
      return `${import.meta.env.VITE_API_BASE_IMAGE_URL}/${url}`;
    } catch (e) {
      return PLACEHOLDER;
    }
  };

  useEffect(() => {
    if (!Number.isFinite(userId)) {
      setItems([]);
      setLoading(false);
      return;
    }
    fetchCart();
  }, [userId]);

  useEffect(() => {
    if (!Number.isFinite(userId) || typeof window === 'undefined') {
      return;
    }

    const handleCartUpdated = (e) => {
      const source = e?.detail?.source;
      if (source === 'cart-page') return;
      fetchCart();
    };

    window.addEventListener(CART_UPDATED_EVENT, handleCartUpdated);
    return () => {
      window.removeEventListener(CART_UPDATED_EVENT, handleCartUpdated);
    };
  }, [userId]);

  async function syncCartCount({ source } = {}) {
    try {
      const res = await axiosInstance.get('/guest/menus/cart/count', { params: { userId } });
      if (typeof res.data === 'number' && typeof window !== 'undefined') {
        window.dispatchEvent(
          new CustomEvent(CART_UPDATED_EVENT, {
            detail: { count: res.data, source: source || 'cart-page' },
          })
        );
      }
    } catch (err) {
      console.error(
        'ヘッダーのカート件数の同期に失敗しました:',
        err?.response?.data || err.message
      );
    }
  }

  async function fetchCart() {
    setLoading(true);
    setError('');
    try {
      const { data } = await axiosInstance.get('/guest/cart', { params: { userId } });
      if (!data?.result) throw new Error(data?.message || 'カートの取得に失敗しました');
      const list = Array.isArray(data?.resultList)
        ? data.resultList
        : Array.isArray(data?.data)
          ? data.data
          : [];
      setItems(list);
      if (!hasInitializedSelectionRef.current) {
        hasInitializedSelectionRef.current = true;
        setSelectedCartItemIds(list.map((item) => item.cartItemId));
      } else {
        setSelectedCartItemIds((prev) =>
          prev.filter((id) => list.some((it) => it.cartItemId === id))
        );
      }
      await syncCartCount();
    } catch (err) {
      console.error('カートの取得に失敗しました:', err);
      setError(err?.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }

  function openInfoModal(message) {
    setCartMessage(message);
    setOpenCartModal(true);
  }

  function closeInfoModal() {
    setOpenCartModal(false);
  }

  function openDeleteModal(cartItemId) {
    setBulkDeleteMode(false);
    setDeleteTargetId(cartItemId);
    setDeleteModalOpen(true);
  }

  function closeDeleteModal() {
    setDeleteModalOpen(false);
    setDeleteTargetId(null);
    setBulkDeleteMode(false);
  }

  const handleToggleSelect = (cartItemId) => {
    setSelectedCartItemIds((prev) =>
      prev.includes(cartItemId) ? prev.filter((id) => id !== cartItemId) : [...prev, cartItemId]
    );
  };

  const handleToggleSelectAll = () => {
    if (isAllSelected) {
      setSelectedCartItemIds([]);
    } else {
      setSelectedCartItemIds(items.map((it) => it.cartItemId));
    }
  };

  function openBulkDeleteModal() {
    if (selectedCartItemIds.length === 0) {
      openInfoModal(CMMessage.MSG_VAL_010);
      return;
    }
    setBulkDeleteMode(true);
    setDeleteModalOpen(true);
  }

  async function removeSelectedItems(cartItemIds) {
    try {
      const { data } = await axiosInstance.post('/guest/cart/delete-selected', {
        cartItemIds,
      });
      if (!data?.result) throw new Error(data?.message || '削除に失敗しました');
      await fetchCart();
    } catch (err) {
      console.error('選択商品の削除に失敗しました:', err);
      openInfoModal(err?.response?.data?.message || err.message);
    }
  }

  async function updateQuantity(cartItemId, quantity) {
    const target = items.find((item) => item.cartItemId === cartItemId);
    if (!target) return;

    const purchaseLimit = resolvePurchaseLimit(target);
    const nextQuantity = Number(quantity);
    if (!Number.isFinite(nextQuantity)) return;
    if (nextQuantity === target.quantity) return;

    // 最大数量チェック
    if (nextQuantity > purchaseLimit) {
      restoreQuantity(target);
      openInfoModal(CMMessage.MSG_LIMIT_001(purchaseLimit));
      return;
    }

    // 在庫チェック
    const availableQty =
      typeof target.availableQty === 'number' && target.availableQty >= 0
        ? target.availableQty
        : null;

    if (availableQty !== null && nextQuantity > availableQty) {
      restoreQuantity(target);
      const insufficientMessage =
        availableQty <= 0 ? CMMessage.MSG_ERR_032 : CMMessage.MSG_LIMIT_002(availableQty);
      openInfoModal(insufficientMessage);
      return;
    }

    if (availableQty !== null && availableQty <= 0) {
      restoreQuantity(target);
      openInfoModal(CMMessage.MSG_ERR_032);
      return;
    }

    // サーバーへPATCHリクエスト
    try {
      const { data } = await axiosInstance.patch(`/guest/cart/${cartItemId}`, {
        quantity: nextQuantity,
      });

      // バックエンドのResponseModel形式: { result: boolean, message: string, ... } を想定
      if (!data?.result) {
        throw new Error(data?.message || '数量の変更に失敗しました');
      }

      // 프론트 상태 반영
      setItems((prev) =>
        prev.map((item) => {
          if (item.cartItemId !== cartItemId) return item;
          const productPrice = Number(item.productPrice) || 0;
          return {
            ...item,
            quantity: nextQuantity,
            lineTotal: productPrice * nextQuantity,
          };
        })
      );

      await syncCartCount();
    } catch (err) {
      console.error('数量の変更に失敗しました:', err);
      const message = err?.response?.data?.message || err.message;
      openInfoModal(message || CMMessage.MSG_ERR_007('数量変更'));

      // フロントの状態へ反映
      restoreQuantity(target);
    }

    function restoreQuantity(original) {
      setItems((prev) =>
        prev.map((item) => {
          if (item.cartItemId !== cartItemId) return item;
          const productPrice = Number(item.productPrice) || 0;
          return {
            ...item,
            quantity: original.quantity,
            lineTotal: productPrice * original.quantity,
          };
        })
      );
    }
  }

  async function removeItem(cartItemId) {
    try {
      const { data } = await axiosInstance.delete(`/guest/cart/${cartItemId}`);
      if (!data?.result) throw new Error(data?.message || '削除に失敗しました');
      await fetchCart();
    } catch (err) {
      console.error('カートの削除に失敗しました:', err);
      openInfoModal(err?.response?.data?.message || err.message);
    }
  }

  async function handleRemoveConfirm() {
    try {
      if (bulkDeleteMode) {
        if (selectedCartItemIds.length === 0) {
          closeDeleteModal();
          return;
        }
        await removeSelectedItems(selectedCartItemIds);
        setSelectedCartItemIds([]);
      } else {
        if (deleteTargetId === null) {
          closeDeleteModal();
          return;
        }
        await removeItem(deleteTargetId);
      }
    } finally {
      closeDeleteModal();
    }
  }

  async function handleCheckout() {
    // カート自体が空の場合
    if (items.length === 0) {
      openInfoModal(CMMessage.MSG_EMPTY_013);
      return;
    }

    // 選択された商品のみ対象
    if (!selectedCartItemIds || selectedCartItemIds.length === 0) {
      openInfoModal(CMMessage.MSG_VAL_011);
      return;
    }

    if (!isLoggedIn || !memberNumber) {
      openInfoModal(CMMessage.MSG_ERR_023);
      navigate('/login', { state: { from: { pathname: '/cart' } } });
      return;
    }

    const selectedItems = items.filter((it) => selectedCartItemIds.includes(it.cartItemId));
    const cartItemIds = selectedItems.map((it) => it.cartItemId);

    // フィルタリングの結果、選択項目が0件になった場合は防御的に再チェック
    if (cartItemIds.length === 0) {
      openInfoModal(CMMessage.MSG_VAL_011);
      return;
    }

    try {
      // バックエンドで注文準備（金額/在庫などの最終検証）を実行
      const { data } = await axiosInstance.post('/guest/cart/prepare-order', {
        cartItemIds,
      });

      if (!data?.result) {
        throw new Error(data?.message || CMMessage.MSG_ERR_007('注文準備'));
      }

      const payload = data.data || data.resultList || {};
      const preparedItems =
        Array.isArray(payload.items) && payload.items.length > 0 ? payload.items : selectedItems;

      // サーバーから返却された合計を優先し、無ければフロントで再計算
      const preparedSubtotal =
        typeof payload.subtotal === 'number'
          ? payload.subtotal
          : preparedItems.reduce((sum, it) => sum + (it.lineTotal || 0), 0);

      const preparedShipping =
        typeof payload.shipping === 'number'
          ? payload.shipping
          : preparedItems.reduce((sum, it) => sum + (it.shippingFee || 0), 0);

      const preparedTotal =
        typeof payload.total === 'number' ? payload.total : preparedSubtotal + preparedShipping;

      // 注文作成はCM_06_1001画面で配送情報/決済手段を選択後に実行
      navigate('/order-payment', {
        state: {
          cartItemIds,
          items: preparedItems,
          totals: {
            subtotal: preparedSubtotal,
            shippingFee: preparedShipping,
            total: preparedTotal,
          },
          userId,
          memberNumber,
        },
      });
    } catch (err) {
      console.error('注文準備に失敗しました:', err);
      openInfoModal(
        err?.response?.data?.message || err.message || CMMessage.MSG_ERR_007('注文準備')
      );
    }
  }

  const buildQuantityOptions = (item) => {
    const availableQty =
      typeof item.availableQty === 'number' && item.availableQty >= 0 ? item.availableQty : null;
    const purchaseLimit = resolvePurchaseLimit(item);
    const currentQuantity = Math.min(item.quantity || 1, purchaseLimit);
    if (availableQty !== null && availableQty <= 0) {
      return [currentQuantity];
    }
    let limit = purchaseLimit;
    if (availableQty !== null) {
      limit = Math.min(limit, availableQty);
    }
    limit = Math.max(Math.min(limit, purchaseLimit), currentQuantity, 1);
    const start = Math.min(currentQuantity, limit);
    return Array.from({ length: limit }, (_, idx) => idx + 1);
  };

  const resolvePurchaseLimit = (item) => {
    const raw = item?.purchaseLimit;
    const parsed = Number(raw);
    if (!Number.isFinite(parsed) || parsed <= 0) return 1;
    return Math.max(1, Math.floor(parsed));
  };

  const normalItems = items.filter((item) => !isReservationProduct(item));
  const reservationItems = items.filter((item) => isReservationProduct(item));

  const renderCartItem = (it) => (
    <div key={it.cartItemId} className="d-flex border-bottom py-2 align-items-center">
      <div className="form-check me-2">
        <input
          className="form-check-input"
          type="checkbox"
          checked={selectedCartItemIds.includes(it.cartItemId)}
          onChange={() => handleToggleSelect(it.cartItemId)}
        />
      </div>
      <div style={{ flex: '0 0 100px' }}>
        <img
          src={getImageUrl(it.imageLink)}
          onError={(e) => (e.currentTarget.src = 'https://placehold.jp/100x100.png')}
          alt={it.productName || '商品画像'}
          className="img-fluid"
          style={{ width: '100px', height: '100px', objectFit: 'cover' }}
        />
      </div>
      <div className="ms-3 flex-grow-1">
        <div>
          {it.productName || it.productId}
          {isReservationProduct(it) && formatReservationMonth(it) && (
            <span className="text-muted ms-2">({formatReservationMonth(it)})</span>
          )}
        </div>
        <div className="fw-bold text-primary mt-2">
          {(it.productPrice || 0).toLocaleString()} 円
        </div>
        <div className="mt-2 d-flex justify-content-between align-items-center w-100">
          <div className="d-flex align-items-center gap-2">
            <label className="m-0">数量</label>
            <select
              className="form-select"
              style={{ width: '100px' }}
              value={it.quantity}
              onChange={(e) => updateQuantity(it.cartItemId, parseInt(e.target.value))}
            >
              {buildQuantityOptions(it).map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </div>
          <div>
            <button
              type="button"
              className="btn btn-link text-danger ms-3 p-0"
              onClick={() => openDeleteModal(it.cartItemId)}
            >
              削除
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <>
      <div className="container my-5">
        <div className="row">
          <div className="d-flex flex-column flex-sm-row mb-3">
            <h1 className="fw-bold m-0 me-sm-4 mb-2 mb-sm-0" ref={topRef}>
              カート
            </h1>
            <div className="d-flex flex-wrap flex-sm-nowrap align-items-sm-end">
              <CM_06_1000_stepIndicator currentStep={0} />
            </div>
          </div>
          <hr />
          <div className="col-12 col-lg-8">
            <div className="d-flex justify-content-between align-items-center pb-2">
              <h5 className="m-0 fw-semibold">商品情報</h5>
              {!loading && items.length > 0 && (
                <div className="d-flex align-items-center gap-2">
                  <div className="form-check m-0">
                    <input
                      id="cart-select-all"
                      className="form-check-input"
                      type="checkbox"
                      checked={isAllSelected}
                      onChange={handleToggleSelectAll}
                    />
                    <label className="form-check-label ms-1" htmlFor="cart-select-all">
                      全選択
                    </label>
                  </div>
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={openBulkDeleteModal}
                    disabled={!hasSelected}
                  >
                    選択削除
                  </button>
                </div>
              )}
            </div>

            {loading && <div className="py-3">読み込み中...</div>}
            {error && <div className="text-danger py-2">{error}</div>}

            {!loading && items.length === 0 && (
              <div className="py-3 text-muted">{CMMessage.MSG_EMPTY_013}</div>
            )}

            {!loading && normalItems.map((it) => renderCartItem(it))}

            {!loading && reservationItems.length > 0 && (
              <div className="mt-4">
                <h5 className="fw-bold mb-2">予約商品情報</h5>
                {reservationItems.map((it) => renderCartItem(it))}
              </div>
            )}
          </div>

          <div className="col-12 col-lg-4 mt-4 mt-5 d-flex flex-column">
            <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
              <span>商品合計</span>
              <strong>{subtotal.toLocaleString()}円</strong>
            </div>
            <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
              <span>送料</span>
              <strong>{shippingFee.toLocaleString()}円</strong>
            </div>
            <div className="d-flex justify-content-between mt-3 mb-3 flex-column pb-3">
              <span className="fw-bold">お支払い合計</span>
              <span className="fw-bold fs-5">{total.toLocaleString()}円</span>
            </div>
            <button className="btn btn-primary w-100" onClick={handleCheckout}>
              購入する
            </button>
            <div className="text-danger mt-3" style={{ fontSize: '0.8em', fontWeight: 'bold' }}>
              <div>
                <i className="bi bi-exclamation-circle me-1"></i>
                商品不良の場合を除き、返品はできません。
              </div>
              <div className="mt-2 ms-3">キャンセルはお支払い方法によって異なります。</div>
              <div className="mt-2 ms-3">
                詳細 <i className="bi bi-caret-right-fill mx-1"></i>
                <a
                  href="/terms?type=e-commerce"
                  className="text-danger text-decoration-none"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  [ガイド - 返品・交換・キャンセル]
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
      <CM_99_1004 isOpen={openCartModal} onClose={closeInfoModal} Message={cartMessage} />
      <CM_99_1001
        isOpen={deleteModalOpen}
        onClose={closeDeleteModal}
        onConfirm={handleRemoveConfirm}
        Message={bulkDeleteMode ? CMMessage.MSG_CON_004 : CMMessage.MSG_CON_005}
      />
    </>
  );
}