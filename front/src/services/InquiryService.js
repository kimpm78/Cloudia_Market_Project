import axiosInstance from './axiosInstance'; // axios 인스턴스 경로

/**
 * 1:1 문의 목록 조회
 */
export const fetchInquiryList = async () => {
  try {
    const { data } = await axiosInstance.get(`/user/mypage/inquiries`);
    return data;
  } catch (error) {
    console.error(`[InquiryService] fetchInquiryList 실패:`, error);
    throw error;
  }
};

/**
 * 1:1 문의 상세 정보 조회
 * @param {object} payload
 */
export const fetchInquiryDetail = async (payload) => {
  const { inquiryId } = payload;
  if (!inquiryId) throw new Error('Inquiry ID is required');

  try {
    const { data } = await axiosInstance.get(`/user/mypage/inquiries/${inquiryId}`);

    if (!data) {
      throw new Error('서버로부터 응답이 없습니다.');
    }
    return data;
  } catch (error) {
    console.error(`[InquiryService] fetchInquiryDetail 실패:`, error);
    throw error;
  }
};

/**
 * 1:1 문의 답변 등록 (관리자)
 * @param {object} payload
 */
export const answerInquiry = async (payload) => {
  const { inquiryId, answerContent } = payload;
  if (!inquiryId || !answerContent) {
    throw new Error('Inquiry ID와 답변 내용은 필수입니다.');
  }

  try {
    const { data } = await axiosInstance.post(`/admin/mypage/inquiries/${inquiryId}/answer`, {
      answerContent,
    });
    return data ?? null;
  } catch (error) {
    console.error(`[InquiryService] answerInquiry 실패:`, error);
    throw error;
  }
};

/**
 * 1:1 문의 생성
 * @param {object} payload
 */
export const createInquiry = async (payload) => {
  if (!payload) throw new Error('Payload is required');
  try {
    // payload를 요청 본문으로 그대로 사용
    const { data } = await axiosInstance.post('/user/mypage/inquiries', payload);
    return data;
  } catch (error) {
    console.error('[InquiryService] createInquiry 실패:', error);
    throw error;
  }
};

/**
 * 문의 삭제 API 호출
 * @param {object} payload
 */
export const deleteInquiry = async (payload) => {
  const { inquiryId } = payload;
  if (!inquiryId) throw new Error('Inquiry ID is required for deletion');
  try {
    const response = await axiosInstance.delete(`/user/mypage/inquiries/${inquiryId}`);
    return response.data;
  } catch (error) {
    console.error(`[InquiryService] deleteInquiry 실패:`, error);
    throw error;
  }
};
