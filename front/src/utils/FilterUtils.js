import { hasReservationClosed } from './productStatus';
import { isSoldOutBasic } from './productInventory';

// ページ別カテゴリーグループマッピング 定義
export const CATEGORY_PAGE_MAP = [
  {
    page: '新商品',
    groups: ['予約商品', 'キャラクター', 'ジャンル'],
  },
  {
    page: '予約商品',
    groups: ['予約商品'],
  },
  {
    page: 'キャラクター',
    groups: ['キャラクター'],
  },
  {
    page: 'ジャンル',
    groups: ['ジャンル'],
  },
];

// ページに対応するカテゴリグループラベルを返す
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

// カテゴリフィルターのマッチング（ハイフン区切りを考慮）
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

// 選択されたフィルターでアイテムを絞り込む
export const filterItemBySelectedFilters = (item, selectedFilters) => {
  const filtersArray = Array.isArray(selectedFilters) ? selectedFilters : [];
  const normalizeFilterLabel = (label) => {
    const value = String(label ?? '').trim();

    const aliasMap = {
      // 商品情報
      売り切れ商品を表示: '品切れ商品表示',
      売り切れ商品を除外: '品切れ商品除外',
      受付終了商品を表示: '終了商品表示',
      予約終了商品を除外: '予約終了商品除外',
      予約終了商品を除く: '予約終了商品除外',
      締切済み商品を表示: '終了商品表示',
      // 価格帯
      '10,000円以上': '1万円以上',
      '50,000円以上': '5万円以上',
      '100,000円以上': '10万円以上',
    };

    return aliasMap[value] ?? value;
  };

  const normalized = filtersArray.map((f) => normalizeFilterLabel(f));

  const statusFilters = ['品切れ商品除外', '品切れ商品表示', '終了商品表示', '予約終了商品除外'];
  const priceFilters = ['1万円以上', '5万円以上', '10万円以上'];

  const evalFilter = (filter) => {
    if (filter === '品切れ商品除外') return !isSoldOutBasic(item);
    if (filter === '品切れ商品表示') return isSoldOutBasic(item);
    if (filter === '終了商品表示') return hasReservationClosed(item);
    if (filter === '予約終了商品除外') return !hasReservationClosed(item);

    const price = item.productPrice ?? item.price ?? 0;
    if (filter === '1万円以上') return price >= 10000;
    if (filter === '5万円以上') return price >= 50000;
    if (filter === '10万円以上') return price >= 100000;

    return true;
  };

  const statusSelected = normalized.filter((f) => statusFilters.includes(f));
  // ステータスフィルターは OR 条件
  if (statusSelected.length > 0) {
    const anyStatusMatch = statusSelected.some((f) => evalFilter(f));
    if (!anyStatusMatch) {
      return false;
    }
  }

  // 価格フィルターはすべて AND 条件
  const priceSelected = normalized.filter((f) => priceFilters.includes(f));
  if (!priceSelected.every((f) => evalFilter(f))) {
    return false;
  }

  // カテゴリフィルターは OR（いずれかが一致すれば通過）
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
