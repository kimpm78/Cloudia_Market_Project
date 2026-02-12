import axiosInstance from '../services/axiosInstance';

// レビュー一覧取得
export const fetchReviews = async () => {
  try {
    const res = await axiosInstance.get(`${import.meta.env.VITE_API_BASE_URL}/guest/reviews`);
    return res.data?.resultList || [];
  } catch (error) {
    console.error('レビュー一覧取得中にエラーが発生しました:', error);
    return [];
  }
};

// レビュー詳細取得
export const fetchReviewDetail = async (reviewId) => {
  try {
    const res = await axiosInstance.get(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}`
    );
    return res.data || null;
  } catch (error) {
    console.error(`レビュー詳細取得中にエラーが発生しました (ID: ${reviewId}):`, error);
    return null;
  }
};

// レビュー作成（本文 + メイン画像）
export const createReview = async (body) => {
  try {
    const formData =
      body instanceof FormData
        ? body
        : Object.entries(body || {}).reduce((acc, [key, value]) => {
            if (value !== undefined && value !== null) {
              acc.append(key, value);
            }
            return acc;
          }, new FormData());

    // Content-Type の手動指定を削除（axios が boundary を含めて自動設定）
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/upload`,
      formData
    );

    if (res.data?.result) {
      return {
        reviewId: res.data.resultList ?? null,
        message: res.data.message,
      };
    }

    throw new Error(res.data?.message || 'レビューの保存に失敗しました。');
  } catch (error) {
    console.error('レビュー作成中にエラーが発生しました:', error);
    throw error;
  }
};

// レビュー更新（本文 + 画像）
export const updateReview = async (reviewId, body) => {
  try {
    const formData = new FormData();
    formData.append('reviewId', reviewId);

    for (const key in body) {
      if (body[key] !== undefined && body[key] !== null) {
        formData.append(key, body[key]);
      }
    }

    // ここでも Content-Type の手動指定は不要
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/update`,
      formData
    );

    return res.data || null;
  } catch (error) {
    console.error(`レビュー更新中にエラーが発生しました (ID: ${reviewId}):`, error);
    return null;
  }
};

// レビュー削除
export const deleteReview = async (reviewId) => {
  try {
    const res = await axiosInstance.delete(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}`
    );
    return res.data;
  } catch (err) {
    console.error(`レビュー削除中にエラーが発生しました (ID: ${reviewId}):`, err);
    throw err;
  }
};

// 注文 + 商品一覧取得
export const fetchOrdersWithProducts = async (memberNumber) => {
  try {
    const formatOrderNumber = (value) => {
      if (!value) return '';
      const str = String(value).trim();
      return /^\d+$/.test(str) ? str.padStart(5, '0') : str;
    };

    const formatOrderDate = (value) => {
      if (!value) return '';
      const str = String(value).trim();
      const idx = str.indexOf('T') >= 0 ? str.indexOf('T') : str.indexOf(' ');
      return idx > 0 ? str.slice(0, idx) : str;
    };

    const res = await axiosInstance.get(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/orders`,
      {
        params: { memberNumber },
      }
    );

    const list = Array.isArray(res.data?.resultList) ? res.data.resultList : [];

    return list.map((order) => ({
      orderId: order.orderId,
      memberNumber: order.memberNumber,
      orderNumber: order.orderNumber,
      orderDate: order.orderDate,
      displayOrderNumber: formatOrderNumber(order.orderNumber),
      displayOrderDate: formatOrderDate(order.orderDate),
      products: order.products || [],
    }));
  } catch (error) {
    console.error('注文および商品一覧取得中にエラーが発生しました:', error);
    return [];
  }
};

// 閲覧数を増加（1日1回）
export const increaseReviewView = async (reviewId) => {
  try {
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/view`
    );
    return res.data || null;
  } catch (error) {
    console.error(`レビュー閲覧数増加中にエラーが発生しました (ID: ${reviewId}):`, error);
    return null;
  }
};

// エディター画像アップロード
export const uploadReviewEditorImage = async (file) => {
  try {
    if (!file) {
      console.error('エディター画像アップロード中にエラーが発生しました: file がありません。');
      return null;
    }

    const formData = new FormData();
    formData.append('file', file); // バックエンド @RequestParam("file") と同じキー

    // Content-Type を直接指定しない
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/image/upload`,
      formData
    );

    // サーバーは ResponseModel<String> のため resultList（または result）を使用
    return res.data?.resultList ?? res.data?.result ?? null;
  } catch (error) {
    console.error('エディター画像アップロード中にエラーが発生しました:', error);
    return null;
  }
};

// メイン画像アップロード POST /guest/reviews/{reviewId}/image
export const uploadReviewMainImage = async (reviewId, file) => {
  try {
    if (!reviewId || !file) {
      console.error(
        `メイン画像アップロード中にエラーが発生しました: reviewId または file がありません。 (reviewId: ${reviewId})`
      );
      return null;
    }

    const formData = new FormData();
    formData.append('file', file); // バックエンド @RequestParam("file") と一致

    // Content-Type を直接指定しない
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/image`,
      formData
    );

    return res.data?.resultList ?? res.data?.result ?? null;
  } catch (error) {
    console.error(`メイン画像アップロード中にエラーが発生しました (レビューID: ${reviewId}):`, error);
    return null;
  }
};

// レビュー画像削除（main/editor 共通）
export const deleteReviewImage = async (reviewId, imageId) => {
  try {
    const res = await axiosInstance.delete(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/images/${imageId}`
    );
    return res.data || null;
  } catch (error) {
    console.error(
      `レビュー画像削除中にエラーが発生しました (レビューID: ${reviewId}, 画像ID: ${imageId}):`,
      error
    );
    return null;
  }
};

// 画像URLの安全な結合関数（env.local=/images を維持可能）
export const buildImageUrl = (rawUrl) => {
  const base = import.meta.env.VITE_API_BASE_IMAGE_URL || '';

  if (!rawUrl) return '';

  // 両方が /images の場合は重複を除去
  if (rawUrl.startsWith('/images/') && base.endsWith('/images')) {
    return base.replace(/\/images$/, '') + rawUrl;
  }

  // rawUrl が / で始まる場合は base + rawUrl
  if (rawUrl.startsWith('/')) {
    return base + rawUrl;
  }

  // 通常の結合
  return `${base}/${rawUrl}`;
};
