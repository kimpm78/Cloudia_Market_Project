export const resolveAvailableQty = (item) => {
  if (!item || typeof item !== 'object') return null;
  const candidates = [
    item.availableQty,
    item.availableqty,
    item.available_quantity,
    item.quantity,
    item.stock,
  ];
  for (const candidate of candidates) {
    if (typeof candidate === 'number' && !Number.isNaN(candidate)) {
      return candidate;
    }
  }
  return null;
};

export const getReservedFromMeta = (item, cartMeta) => {
  if (!item || !cartMeta) return 0;
  const candidateKeys = [
    item.productCode,
    item.product_code,
    item.productCodeId,
    item.product_code_id,
    item.productId,
    item.product_id,
    item.id,
    item.id_code,
  ]
    .filter((value) => value !== undefined && value !== null)
    .map((value) => String(value).trim())
    .filter((value, index, self) => value.length > 0 && self.indexOf(value) === index);

  for (const key of candidateKeys) {
    const entry = cartMeta?.[key];
    if (entry && typeof entry.quantity === 'number') {
      return entry.quantity;
    }
  }
  return 0;
};

export const normalizeCartAwareStock = (items, cartMeta) => {
  const list = Array.isArray(items) ? items : [];
  return list.map((item) => {
    const available = resolveAvailableQty(item);
    const reserved = getReservedFromMeta(item, cartMeta);
    const effective = available === null ? null : Math.max(available - reserved, 0);
    const soldOut = item?.isSoldOut === true || (effective !== null && effective <= 0);
    return {
      ...item,
      availableQty: effective ?? item.availableQty,
      isSoldOut: soldOut,
    };
  });
};

export const isSoldOutBasic = (item) => {
  const soldOutFlags = [
    item?.isSoldOut,
    item?.soldOut,
    item?.reservationSoldOut,
    item?.reservationClosed,
    item?.stockStatus === 'SOLD_OUT',
    item?.status === 'SOLD_OUT',
  ];

  if (soldOutFlags.some(Boolean)) {
    return true;
  }

  const available = resolveAvailableQty(item);
  return available !== null && available <= 0;
};
