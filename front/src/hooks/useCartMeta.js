import { useEffect, useState } from 'react';
import { loadCartMeta, getEventName } from '../utils/cartMetaStorage';

export default function useCartMeta() {
  const [cartMeta, setCartMeta] = useState(() => loadCartMeta());

  useEffect(() => {
    if (typeof window === 'undefined') {
      return () => {};
    }
    const eventName = getEventName();
    const handleMetaUpdate = (e) => {
      if (e?.detail && typeof e.detail === 'object') {
        setCartMeta(e.detail);
      } else {
        setCartMeta(loadCartMeta());
      }
    };
    window.addEventListener(eventName, handleMetaUpdate);
    return () => window.removeEventListener(eventName, handleMetaUpdate);
  }, []);

  return cartMeta;
}
