import axiosInstance from '../services/axiosInstance';

// コメント一覧取得
export const fetchComments = async (reviewId) => {
  const res = await axiosInstance.get(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments`
  );
  return Array.isArray(res.data?.resultList) ? res.data.resultList : [];
};

// コメント／返信コメント登録
export const createComment = async (reviewId, body) => {
  const res = await axiosInstance.post(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments`,
    body
  );

  // バックエンド応答が配列（resultList）か単一オブジェクトか、成功フラグのみかを判定して処理
  if (Array.isArray(res.data?.resultList)) {
    return res.data.resultList;
  }
  if (res.data?.resultList) {
    return [res.data.resultList];
  }
  if (res.data?.result) {
    return res.data;
  }
  return [];
};

// コメント更新（本人のみ可能）
export const updateComment = async (reviewId, commentId, body) => {
  const res = await axiosInstance.put(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments/${commentId}`,
    body
  );
  return res.data;
};

// コメント削除（本人のみ可能）
export const deleteComment = async (reviewId, commentId, body) => {
  const res = await axiosInstance.delete(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments/${commentId}`,
    { data: body }
  );
  return res.data;
};
