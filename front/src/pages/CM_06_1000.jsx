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

  // 장바구니 상태
  const [items, setItems] = useState([]); // CartItemResponse[]
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openCartModal, setOpenCartModal] = useState(false);
  const [cartMessage, setCartMessage] = useState('');
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteTargetId, setDeleteTargetId] = useState(null);
  const [selectedCartItemIds, setSelectedCartItemIds] = useState([]);
  const [bulkDeleteMode, setBulkDeleteMode] = useState(false);

  // 합계 계산
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

  // 이미지 URL helper (리스트 페이지와 동일 스타일)
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
      console.error('헤더 장바구니 수량 동기화 실패:', err?.response?.data || err.message);
    }
  }

  async function fetchCart() {
    setLoading(true);
    setError('');
    try {
      const { data } = await axiosInstance.get('/guest/cart', { params: { userId } });
      if (!data?.result) throw new Error(data?.message || '장바구니 조회 실패');
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
      console.error('장바구니 조회 실패:', err);
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
      if (!data?.result) throw new Error(data?.message || '삭제 실패');
      await fetchCart();
    } catch (err) {
      console.error('선택 상품 삭제 실패:', err);
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

    // 최대 수량 체크
    if (nextQuantity > purchaseLimit) {
      restoreQuantity(target);
      openInfoModal(CMMessage.MSG_LIMIT_001(purchaseLimit));
      return;
    }

    // 재고 체크
    const availableQty =
      typeof target.availableQty === 'number' && target.availableQty >= 0
        ? target.availableQty
        : null;

    if (availableQty !== null && nextQuantity > availableQty) {
      restoreQuantity(target);
      const insufficientMessage =
        availableQty <= 0
          ? CMMessage.MSG_ERR_032
          : CMMessage.MSG_LIMIT_002(availableQty);
      openInfoModal(insufficientMessage);
      return;
    }

    if (availableQty !== null && availableQty <= 0) {
      restoreQuantity(target);
      openInfoModal(CMMessage.MSG_ERR_032);
      return;
    }

    // 서버 PATCH 요청
    try {
      const { data } = await axiosInstance.patch(`/guest/cart/${cartItemId}`, {
        quantity: nextQuantity,
      });

      // 백엔드 ResponseModel 형식: { result: boolean, message: string, ... } 가정
      if (!data?.result) {
        throw new Error(data?.message || '수량 변경 실패');
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
      console.error('수량 변경 실패:', err);
      const message = err?.response?.data?.message || err.message;
      openInfoModal(message || CMMessage.MSG_ERR_007('수량 변경'));

      // 서버 에러 시에도 원래 수량 복원
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
      if (!data?.result) throw new Error(data?.message || '삭제 실패');
      await fetchCart();
    } catch (err) {
      console.error('장바구니 삭제 실패:', err);
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
    // 장바구니 자체가 비어 있는 경우
    if (items.length === 0) {
      openInfoModal(CMMessage.MSG_EMPTY_013);
      return;
    }

    // 구매할 상품을 선택하지 않은 경우
    if (!selectedCartItemIds || selectedCartItemIds.length === 0) {
      openInfoModal(CMMessage.MSG_VAL_011);
      return;
    }

    if (!isLoggedIn || !memberNumber) {
      openInfoModal(CMMessage.MSG_ERR_023);
      navigate('/login', { state: { from: { pathname: '/cart' } } });
      return;
    }

    // 선택된 상품만 대상
    const selectedItems = items.filter((it) => selectedCartItemIds.includes(it.cartItemId));
    const cartItemIds = selectedItems.map((it) => it.cartItemId);

    // 선택된 항목이 필터링 과정에서 0개가 되면 방어적으로 한 번 더 체크
    if (cartItemIds.length === 0) {
      openInfoModal(CMMessage.MSG_VAL_011);
      return;
    }

    try {
      // 백엔드에서 주문 준비(금액/재고 등 최종 검증)를 수행
      const { data } = await axiosInstance.post('/guest/cart/prepare-order', {
        cartItemIds,
      });

      if (!data?.result) {
        throw new Error(data?.message || CMMessage.MSG_ERR_007('주문 준비'));
      }

      const payload = data.data || data.resultList || {};
      const preparedItems =
        Array.isArray(payload.items) && payload.items.length > 0 ? payload.items : selectedItems;

      // 서버에서 내려준 합계를 우선 사용하고, 없으면 프론트에서 재계산
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

      // 주문 생성은 CM_06_1001 화면에서 배송정보/결제수단 선택 후 실행
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
      console.error('주문 준비 실패:', err);
      openInfoModal(
        err?.response?.data?.message || err.message || CMMessage.MSG_ERR_007('주문 준비')
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
          alt={it.productName || '상품 이미지'}
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
          {(it.productPrice || 0).toLocaleString()} 원
        </div>
        <div className="mt-2 d-flex justify-content-between align-items-center w-100">
          <div className="d-flex align-items-center gap-2">
            <label className="m-0">수량</label>
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
              삭제
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
              장바구니
            </h1>
            <div className="d-flex flex-wrap flex-sm-nowrap align-items-sm-end">
              <CM_06_1000_stepIndicator currentStep={0} />
            </div>
          </div>
          <hr />
          <div className="col-12 col-lg-8">
            <div className="d-flex justify-content-between align-items-center pb-2">
              <h5 className="m-0">상품 정보</h5>
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
                      전체 선택
                    </label>
                  </div>
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={openBulkDeleteModal}
                    disabled={!hasSelected}
                  >
                    선택 삭제
                  </button>
                </div>
              )}
            </div>

            {loading && <div className="py-3">불러오는 중...</div>}
            {error && <div className="text-danger py-2">{error}</div>}

            {!loading && items.length === 0 && (
              <div className="py-3 text-muted">{CMMessage.MSG_EMPTY_013}</div>
            )}

            {!loading && normalItems.map((it) => renderCartItem(it))}

            {!loading && reservationItems.length > 0 && (
              <div className="mt-4">
                <h5 className="fw-bold mb-2">예약 상품 정보</h5>
                {reservationItems.map((it) => renderCartItem(it))}
              </div>
            )}
          </div>

          <div className="col-12 col-lg-4 mt-4 mt-5 d-flex flex-column">
            <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
              <span>총 상품 금액</span>
              <strong>{subtotal.toLocaleString()}원</strong>
            </div>
            <div className="d-flex justify-content-between pb-2 border-bottom mb-2">
              <span>배송비</span>
              <strong>{shippingFee.toLocaleString()}원</strong>
            </div>
            <div className="d-flex justify-content-between mt-3 mb-3 flex-column pb-3">
              <span className="fw-bold">총 결제 금액</span>
              <span className="fw-bold fs-5">{total.toLocaleString()}원</span>
            </div>
            <button className="btn btn-primary w-100" onClick={handleCheckout}>
              구매하기
            </button>
            <div className="text-danger mt-3" style={{ fontSize: '0.8em', fontWeight: 'bold' }}>
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
            </div>
          </div>
        </div>
      </div>
      <CM_99_1004 isOpen={openCartModal} onClose={closeInfoModal} Message={cartMessage} />
      <CM_99_1001
        isOpen={deleteModalOpen}
        onClose={closeDeleteModal}
        onConfirm={handleRemoveConfirm}
        Message={
          bulkDeleteMode
            ? CMMessage.MSG_CON_004
            : CMMessage.MSG_CON_005
        }
      />
    </>
  );
}
