import axiosInstance from './axiosInstance'; // axiosインスタンスのパス

/**
 * 1:1問い合わせ一覧取得
 */
export const fetchInquiryList = async () => {
  try {
    const { data } = await axiosInstance.get(`/user/mypage/inquiries`);
    return data;
  } catch (error) {
    console.error(`[InquiryService] fetchInquiryList 失敗:`, error);
    throw error;
  }
};

/**
 * 1:1問い合わせ詳細取得
 * @param {object} payload
 */
export const fetchInquiryDetail = async (payload) => {
  const { inquiryId } = payload;
  if (!inquiryId) throw new Error('Inquiry ID is required');

  try {
    const { data } = await axiosInstance.get(`/user/mypage/inquiries/${inquiryId}`);

    if (!data) {
      throw new Error('サーバーからの応答がありません。');
    }
    return data;
  } catch (error) {
    console.error(`[InquiryService] fetchInquiryDetail 失敗:`, error);
    throw error;
  }
};

/**
 * 1:1問い合わせ回答登録（管理者）
 * @param {object} payload
 */
export const answerInquiry = async (payload) => {
  const { inquiryId, answerContent } = payload;
  if (!inquiryId || !answerContent) {
    throw new Error('Inquiry ID と回答内容は必須です。');
  }

  try {
    const { data } = await axiosInstance.post(`/admin/mypage/inquiries/${inquiryId}/answer`, {
      answerContent,
    });
    return data ?? null;
  } catch (error) {
    console.error(`[InquiryService] answerInquiry 失敗:`, error);
    throw error;
  }
};

/**
 * 1:1問い合わせ作成
 * @param {object} payload
 */
export const createInquiry = async (payload) => {
  if (!payload) throw new Error('Payload is required');
  try {
    // payloadをリクエスト本文としてそのまま使用
    const { data } = await axiosInstance.post('/user/mypage/inquiries', payload);
    return data;
  } catch (error) {
    console.error('[InquiryService] createInquiry 失敗:', error);
    throw error;
  }
};

/**
 * 問い合わせ削除API呼び出し
 * @param {object} payload
 */
export const deleteInquiry = async (payload) => {
  const { inquiryId } = payload;
  if (!inquiryId) throw new Error('Inquiry ID is required for deletion');
  try {
    const response = await axiosInstance.delete(`/user/mypage/inquiries/${inquiryId}`);
    return response.data;
  } catch (error) {
    console.error(`[InquiryService] deleteInquiry 失敗:`, error);
    throw error;
  }
};
