export const CATEGORIES = [
  { value: 1, label: '必読' },
  { value: 2, label: 'お知らせ' },
  { value: 3, label: '注文/決済' },
  { value: 4, label: 'ご案内' },
];

export const DISPLAY_OPTIONS = [
  { value: 1, label: '表示' },
  { value: 2, label: '非表示' },
];

export const SEARCH_TYPE = [
  { value: 1, label: 'タイトル + 内容' },
  { value: 2, label: 'タイトル' },
  { value: 3, label: '内容' },
  { value: 4, label: '作成者' },
];

export const CATEGORIES_2 = [
  { value: '1', label: '販売中' },
  { value: '2', label: '売り切れ' },
  { value: '3', label: '予約中' },
  { value: '4', label: '予約締切' },
];

export const SEARCH_TYPE_2 = [
  { value: 1, label: '商品コード' },
  { value: 2, label: '商品名' },
];

export const ACTION_TYPE = {
  EDIT: 'edit',
  DELETE: 'del',
};

export const ORDER_STATUS = [
  { value: 0, label: 'すべて' },
  { value: 1, label: '入金確認中' },
  { value: 2, label: '購入確定' },
  { value: 3, label: '発送準備中' },
  { value: 4, label: '配送中' },
  { value: 5, label: '配送完了' },
  { value: 6, label: '購入キャンセル（管理者）' },
  { value: 7, label: '購入キャンセル（ユーザー）' },
  { value: 8, label: '決済失敗' },
];

export const PAYMENT_METHODS = [
  { value: 0, label: 'すべて' },
  { value: 1, label: '銀行振込' },
  { value: 2, label: 'クレジットカード' },
];

export const ORDER_STATUS_CODE = {
  TRANSFER_CHECKING: 1,
  PURCHASE_CONFIRMED: 2,
  PREPARING: 3,
  SHIPPING: 4,
  DELIVERED: 5,
  CANCELLED_ADMIN: 6,
  CANCELLED_USER: 7,
  PAYMENT_FAILED: 8,
};

export const PAYMENT_METHOD_CODE = {
  TRANSFER: 1,
  CREDIT_CARD: 2,
};
