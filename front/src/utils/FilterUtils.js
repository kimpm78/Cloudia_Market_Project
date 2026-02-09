import { hasReservationClosed } from './productStatus';
import { isSoldOutBasic } from './productInventory';

// 페이지별로 허용된 카테고리 그룹 설정
export const CATEGORY_PAGE_MAP = [
  {
    page: '신상품',
    groups: ['예약 상품', '캐릭터', '장르'],
  },
  {
    page: '예약 상품',
    groups: ['예약 상품'],
  },
  {
    page: '캐릭터',
    groups: ['캐릭터'],
  },
  {
    page: '장르',
    groups: ['장르'],
  },
];

// 페이지에 해당하는 카테고리 그룹 라벨들을 반환
export const getCategoryGroupsForPage = (page) => {
  const entry = CATEGORY_PAGE_MAP.find((item) => item.page === page);
  return entry ? entry.groups : [];
};

const parseItemCategories = (raw) => {
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
    if (!Array.isArray(parsed)) return [];

    return parsed.flatMap((group) =>
      Object.entries(group).flatMap(([groupCode, codes]) =>
        codes.map((code) => `${groupCode}-${code}`)
      )
    );
  } catch {
    return [];
  }
};

// 카테고리 필터 매칭 (하이픈 구분자 고려)
const isCategoryMatch = (filters, categories) => {
  return filters.some(
    (filter) =>
      typeof filter === 'string' &&
      categories.some(
        (cat) =>
          typeof cat === 'string' &&
          (cat === filter || cat.startsWith(filter + '-') || filter.startsWith(cat + '-'))
      )
  );
};

// 선택된 필터들로 아이템 필터링
export const filterItemBySelectedFilters = (item, selectedFilters) => {
  const filtersArray = Array.isArray(selectedFilters) ? selectedFilters : [];
  const normalized = filtersArray.map((f) => String(f ?? '').trim());

  const statusFilters = ['품절상품 제외', '품절상품 보기', '마감된 상품 보기', '예약종료상품 제외'];
  const priceFilters = ['1만원 이상', '5만원 이상', '10만원 이상'];

  const evalFilter = (filter) => {
    if (filter === '품절상품 제외') return !isSoldOutBasic(item);
    if (filter === '품절상품 보기') return isSoldOutBasic(item);
    if (filter === '마감된 상품 보기') return hasReservationClosed(item);
    if (filter === '예약종료상품 제외') return !hasReservationClosed(item);

    const price = item.productPrice ?? item.price ?? 0;
    if (filter === '1만원 이상') return price >= 10000;
    if (filter === '5만원 이상') return price >= 50000;
    if (filter === '10만원 이상') return price >= 100000;

    return true;
  };

  const statusSelected = normalized.filter((f) => statusFilters.includes(f));
  // OR logic for 상태 필터들
  if (statusSelected.length > 0) {
    const anyStatusMatch = statusSelected.some((f) => evalFilter(f));
    if (!anyStatusMatch) {
      return false;
    }
  }

  // 가격 필터는 모두 AND
  const priceSelected = normalized.filter((f) => priceFilters.includes(f));
  if (!priceSelected.every((f) => evalFilter(f))) {
    return false;
  }

  // 카테고리 필터는 OR (하나라도 매칭되면 통과)
  const categorySelected = normalized.filter(
    (f) => !statusFilters.includes(f) && !priceFilters.includes(f)
  );
  if (categorySelected.length > 0) {
    const itemCategories = parseItemCategories(item.category);
    const categoryGroupName = item.categoryGroupName ?? '';
    const categoryMatched =
      isCategoryMatch(categorySelected, itemCategories) ||
      categorySelected.includes(categoryGroupName);
    if (!categoryMatched) {
      return false;
    }
  }

  return true;
};
