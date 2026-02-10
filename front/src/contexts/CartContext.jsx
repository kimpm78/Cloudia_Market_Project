import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import axiosInstance from '../services/axiosInstance';

const CART_UPDATED_EVENT = 'cart:updated';
const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
  const { isLoggedIn, user } = useAuth();
  const [cartCount, setCartCount] = useState(0);
  const [cartCreatedAt, setCartCreatedAt] = useState(null);
  const [cartCountLoaded, setCartCountLoaded] = useState(false);

  const updateCartState = useCallback(({ count, createdAtEpoch, loaded = true } = {}) => {
    if (typeof count === 'number' && Number.isFinite(count)) {
      setCartCount(count);
      setCartCountLoaded(loaded);
    }
    if (createdAtEpoch !== undefined) {
      const parsedCreatedAt =
        createdAtEpoch === null || createdAtEpoch === undefined ? null : Number(createdAtEpoch);
      setCartCreatedAt(Number.isFinite(parsedCreatedAt) ? parsedCreatedAt : null);
    }
  }, []);

  const clearCartState = useCallback(() => {
    setCartCount(0);
    setCartCountLoaded(false);
    setCartCreatedAt(null);
  }, []);

  useEffect(() => {
    if (!isLoggedIn) {
      clearCartState();
    }
  }, [isLoggedIn]);

  const fetchCartCreatedAt = async () => {
    const res = await axiosInstance.get('/guest/cart/created-at', {
      params: { userId: user?.userId },
    });
    return (
      res.data?.resultList?.createdAtEpoch ??
      res.data?.data?.createdAtEpoch ??
      res.data?.createdAtEpoch ??
      null
    );
  };

  const fetchCartCount = async () => {
    const res = await axiosInstance.get('/guest/menus/cart/count', {
      params: { userId: user?.userId },
    });
    return res.data;
  };

  const syncCartCount = useCallback(async ({ source } = {}) => {
    if (!isLoggedIn || !user?.userId) {
      updateCartState({ count: 0 });
      return;
    }
    try {
      const createdAtEpoch = await fetchCartCreatedAt();
      const nextCount = await fetchCartCount();
      const parsedCount = Number(nextCount);
      if (Number.isFinite(parsedCount)) {
        updateCartState({
          count: parsedCount,
          createdAtEpoch: createdAtEpoch ? Number(createdAtEpoch) : null,
        });
        if (typeof window !== 'undefined') {
          window.dispatchEvent(
            new CustomEvent(CART_UPDATED_EVENT, { detail: { count: parsedCount, source } })
          );
        }
      }
    } catch (error) {
      console.error('カート数量の同期に失敗しました:', error?.response?.data || error.message);
    }
  }, [isLoggedIn, user?.userId, updateCartState]);

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined;
    }

    const handleCartUpdated = (event) => {
      if (!isLoggedIn) {
        return;
      }
      const next = event?.detail?.count;
      const createdAtEpoch = event?.detail?.createdAtEpoch;
      const parsedNext = Number(next);
      if (Number.isFinite(parsedNext) && parsedNext >= 0) {
        updateCartState({ count: parsedNext, createdAtEpoch });
      }
    };

    window.addEventListener(CART_UPDATED_EVENT, handleCartUpdated);
    return () => {
      window.removeEventListener(CART_UPDATED_EVENT, handleCartUpdated);
    };
  }, [isLoggedIn]);

  const value = useMemo(
    () => ({
      cartCount,
      cartCreatedAt,
      cartCountLoaded,
      updateCartState,
      clearCartState,
      syncCartCount,
    }),
    [cartCount, cartCreatedAt, cartCountLoaded, updateCartState, clearCartState, syncCartCount]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('CartContext エラー');
  }
  return context;
};
