import axiosInstance from '../services/axiosInstance';

// 댓글 목록 조회
export const fetchComments = async (reviewId) => {
  const res = await axiosInstance.get(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments`
  );
  return Array.isArray(res.data?.resultList) ? res.data.resultList : [];
};

// 댓글/대댓글 등록
export const createComment = async (reviewId, body) => {
  const res = await axiosInstance.post(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments`,
    body
  );

  // 백엔드 응답이 배열(resultList)인지, 단일 객체인지, 성공 여부만 주는지 구분 처리
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

// 댓글 수정 (본인만 가능)
export const updateComment = async (reviewId, commentId, body) => {
  const res = await axiosInstance.put(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments/${commentId}`,
    body
  );
  return res.data;
};

// 댓글 삭제 (본인만 가능)
export const deleteComment = async (reviewId, commentId, body) => {
  const res = await axiosInstance.delete(
    `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/comments/${commentId}`,
    { data: body }
  );
  return res.data;
};
