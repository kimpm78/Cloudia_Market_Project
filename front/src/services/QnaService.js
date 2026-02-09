import axiosInstance from './axiosInstance';

const buildError = (response) => {
  if (response?.message) {
    return new Error(response.message);
  }
  return new Error('요청 처리 중 오류가 발생했습니다.');
};

export const fetchQnaList = async ({ page = 1, size = 25, searchType = 1, keyword = '', signal } = {}) => {
  const params = { page, size };
  if (searchType) params.searchType = searchType;
  if (keyword && keyword.trim()) params.searchKeyword = keyword.trim();

  const config = signal ? { params, signal } : { params };
  const { data } = await axiosInstance.get('/guest/qna', config);
  if (!data?.result) {
    throw buildError(data);
  }
  return data.resultList;
};

export const fetchQnaDetail = async (qnaId, { signal } = {}) => {
  if (!qnaId) throw new Error('qnaId가 필요합니다.');
  const config = signal ? { signal } : undefined;
  const { data } = await axiosInstance.get(`/guest/qna/${qnaId}`, config);
  if (!data?.result) {
    throw buildError(data);
  }
  return data.resultList;
};

export const createQna = async (payload) => {
  const { data } = await axiosInstance.post('/user/qna', payload);
  if (!data?.result) {
    throw buildError(data);
  }
  return data.resultList;
};

export const answerQna = async (qnaId, answerContent) => {
  if (!qnaId) throw new Error('qnaId가 필요합니다.');
  const { data } = await axiosInstance.post(`/admin/qna/${qnaId}/answer`, {
    answerContent,
  });
  if (!data?.result) {
    throw buildError(data);
  }
  return data;
};

export const fetchRecentQna = async ({ size = 5, productId } = {}) => {
  const params = { size };
  if (productId) params.productId = productId;
  const { data } = await axiosInstance.get('/guest/qna/recent', { params });
  if (!data?.result) {
    throw buildError(data);
  }
  return data.resultList || [];
};

export const deleteQna = async (qnaId) => {
  if (!qnaId) throw new Error('qnaId가 필요합니다.');
  const { data } = await axiosInstance.delete(`/user/qna/${qnaId}`);
  if (!data?.result) {
    throw buildError(data);
  }
  return data;
};
