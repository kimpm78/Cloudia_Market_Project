const normalizeText = (value) => (typeof value === 'string' ? value.trim() : '');

export const getCategoryGroupNames = (item) => {
  const raw = normalizeText(item?.categoryGroupName ?? '');
  if (!raw) return [];
  return raw
    .split(',')
    .map((value) => value.trim())
    .filter((value) => value.length > 0);
};

export const hasCategoryGroup = (item, groupName) => {
  const target = normalizeText(groupName);
  if (!target) return false;
  return getCategoryGroupNames(item).some((name) => name === target);
};

export const isReservationProduct = (item) => {
  const deadline = item?.reservationDeadline ?? item?.reservation_deadline ?? '';
  return typeof deadline === 'string' && deadline.trim().length > 0;
};

const PAGE_FILTERS = {
  新商品: () => true,
  '予約商品': isReservationProduct,
  キャラクター: (item) => hasCategoryGroup(item, 'キャラクター'),
  ジャンル: (item) => hasCategoryGroup(item, 'ジャンル'),
};

export const filterItemsForPage = (items, pageName) => {
  const list = Array.isArray(items) ? items : [];
  const filterFn = PAGE_FILTERS[pageName] ?? (() => true);
  return list.filter((item) => filterFn(item));
};
