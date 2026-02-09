const STORAGE_KEY = 'cartMetaMap';
const EVENT_NAME = 'cart-meta-updated';

const hasWindow = () => typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';

const safeParse = (value) => {
  if (!value) return {};
  try {
    const parsed = JSON.parse(value);
    if (parsed && typeof parsed === 'object') {
      return parsed;
    }
  } catch (err) {
    console.warn('[cartMetaStorage] failed to parse meta store:', err);
  }
  return {};
};

const cloneMeta = (meta) => {
  return meta ? JSON.parse(JSON.stringify(meta)) : {};
};

export const loadCartMeta = () => {
  if (!hasWindow()) return {};
  return safeParse(window.localStorage.getItem(STORAGE_KEY));
};

const dispatchUpdate = (meta) => {
  if (!hasWindow()) return;
  window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: cloneMeta(meta) }));
};

const persist = (meta) => {
  if (!hasWindow()) return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(meta));
  dispatchUpdate(meta);
};

export const upsertCartMeta = (productId, patch = {}) => {
  if (!productId || !hasWindow()) return;
  const current = loadCartMeta();
  const existing = current[productId] || {};
  const next = {
    ...existing,
    ...patch,
    productId,
  };
  const merged = { ...current, [productId]: next };
  persist(merged);
};

export const replaceCartMetaSnapshot = (items = []) => {
  if (!hasWindow()) return;
  const current = loadCartMeta();
  const next = {};
  items.forEach((item) => {
    if (!item?.productId) return;
    const prev = current[item.productId] || {};
    next[item.productId] = {
      ...prev,
      productId: item.productId,
      quantity: typeof item.quantity === 'number' ? item.quantity : prev.quantity ?? 0,
      availableQty:
        typeof item.availableQty === 'number'
          ? item.availableQty
          : typeof prev.availableQty === 'number'
            ? prev.availableQty
            : undefined,
    };
  });
  persist(next);
};

export const removeCartMeta = (productId) => {
  if (!productId || !hasWindow()) return;
  const current = loadCartMeta();
  if (Object.prototype.hasOwnProperty.call(current, productId)) {
    delete current[productId];
    persist(current);
  }
};

export const clearCartMeta = () => {
  if (!hasWindow()) return;
  window.localStorage.removeItem(STORAGE_KEY);
  dispatchUpdate({});
};

export const getReservedQuantity = (productId) => {
  if (!hasWindow()) return 0;
  const current = loadCartMeta();
  const entry = current[productId];
  return entry && typeof entry.quantity === 'number' ? entry.quantity : 0;
};

export const getEventName = () => EVENT_NAME;
