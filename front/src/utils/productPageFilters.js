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
  신상품: () => true,
  '예약 상품': isReservationProduct,
  캐릭터: (item) => hasCategoryGroup(item, '캐릭터'),
  장르: (item) => hasCategoryGroup(item, '장르'),
};

export const filterItemsForPage = (items, pageName) => {
  const list = Array.isArray(items) ? items : [];
  const filterFn = PAGE_FILTERS[pageName] ?? (() => true);
  return list.filter((item) => filterFn(item));
};
