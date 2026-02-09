import axiosInstance from '../services/axiosInstance';

// 리뷰 목록 조회
export const fetchReviews = async () => {
  try {
    const res = await axiosInstance.get(`${import.meta.env.VITE_API_BASE_URL}/guest/reviews`);
    return res.data?.resultList || [];
  } catch (error) {
    console.error('리뷰 목록 조회 중 오류 발생:', error);
    return [];
  }
};

// 리뷰 상세 조회
export const fetchReviewDetail = async (reviewId) => {
  try {
    const res = await axiosInstance.get(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}`
    );
    return res.data || null;
  } catch (error) {
    console.error(`리뷰 상세 조회 중 오류 발생 (ID: ${reviewId}):`, error);
    return null;
  }
};

// 리뷰 작성 (본문 + 메인 이미지)
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

    // Content-Type 수동 지정 제거 (axios가 boundary 포함해서 자동 설정)
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/upload`,
      formData
    );

    if (res.data?.result) {
      return {
        reviewId: res.data.resultList ?? null, // 서버는 resultList에 reviewId 반환
        message: res.data.message,
      };
    }

    throw new Error(res.data?.message || '리뷰 저장에 실패했습니다.');
  } catch (error) {
    console.error('리뷰 작성 중 오류 발생:', error);
    throw error;
  }
};

// 리뷰 수정 (본문 + 이미지)
export const updateReview = async (reviewId, body) => {
  try {
    const formData = new FormData();
    formData.append('reviewId', reviewId);

    for (const key in body) {
      if (body[key] !== undefined && body[key] !== null) {
        formData.append(key, body[key]);
      }
    }

    // 여기서도 Content-Type 수동 지정 제거
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/update`,
      formData
    );

    return res.data || null;
  } catch (error) {
    console.error(`리뷰 수정 중 오류 발생 (ID: ${reviewId}):`, error);
    return null;
  }
};

// 리뷰 삭제
export const deleteReview = async (reviewId) => {
  try {
    const res = await axiosInstance.delete(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}`
    );
    return res.data;
  } catch (err) {
    console.error(`리뷰 삭제 중 오류 발생 (ID: ${reviewId}):`, err);
    throw err;
  }
};

// 주문 + 상품 목록 조회
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
    console.error('주문 및 상품 목록 조회 중 오류 발생:', error);
    return [];
  }
};

// 조회수 증가 (하루 1회)
export const increaseReviewView = async (reviewId) => {
  try {
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/view`
    );
    return res.data || null;
  } catch (error) {
    console.error(`리뷰 조회수 증가 중 오류 발생 (ID: ${reviewId}):`, error);
    return null;
  }
};

// 에디터 이미지 업로드
export const uploadReviewEditorImage = async (file) => {
  try {
    if (!file) {
      console.error('에디터 이미지 업로드 중 오류 발생: file이 없습니다.');
      return null;
    }

    const formData = new FormData();
    formData.append('file', file); // 백엔드 @RequestParam("file") 과 동일 키

    // Content-Type 직접 지정 X
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/image/upload`,
      formData
    );

    // 서버는 ResponseModel<String> 이라서 resultList(또는 result) 사용
    return res.data?.resultList ?? res.data?.result ?? null;
  } catch (error) {
    console.error('에디터 이미지 업로드 중 오류 발생:', error);
    return null;
  }
};

// 메인 이미지 업로드 POST /guest/reviews/{reviewId}/image
export const uploadReviewMainImage = async (reviewId, file) => {
  try {
    if (!reviewId || !file) {
      console.error(
        `메인 이미지 업로드 중 오류 발생: reviewId 또는 file이 없습니다. (reviewId: ${reviewId})`
      );
      return null;
    }

    const formData = new FormData();
    formData.append('file', file); // 백엔드 @RequestParam("file") 와 매칭

    // Content-Type 직접 지정 X
    const res = await axiosInstance.post(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/image`,
      formData
    );

    return res.data?.resultList ?? res.data?.result ?? null;
  } catch (error) {
    console.error(`메인 이미지 업로드 중 오류 발생 (리뷰 ID: ${reviewId}):`, error);
    return null;
  }
};

// 리뷰 이미지 삭제 (main/editor 공통)
export const deleteReviewImage = async (reviewId, imageId) => {
  try {
    const res = await axiosInstance.delete(
      `${import.meta.env.VITE_API_BASE_URL}/guest/reviews/${reviewId}/images/${imageId}`
    );
    return res.data || null;
  } catch (error) {
    console.error(
      `리뷰 이미지 삭제 중 오류 발생 (리뷰 ID: ${reviewId}, 이미지 ID: ${imageId}):`,
      error
    );
    return null;
  }
};

// 이미지 URL 안전 병합 함수 (env.local=/images 유지 가능)
export const buildImageUrl = (rawUrl) => {
  const base = import.meta.env.VITE_API_BASE_IMAGE_URL || '';

  if (!rawUrl) return '';

  // 둘 다 /images 인 경우 중복 제거
  if (rawUrl.startsWith('/images/') && base.endsWith('/images')) {
    return base.replace(/\/images$/, '') + rawUrl;
  }

  // rawUrl 이 / 로 시작하면 그냥 base + rawUrl
  if (rawUrl.startsWith('/')) {
    return base + rawUrl;
  }

  // 일반 조합
  return `${base}/${rawUrl}`;
};
