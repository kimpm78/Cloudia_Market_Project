import axios from 'axios';

let accessToken = null;

// 토큰 갱신 함수 (로그인 성공 시, 혹은 리프레시 성공 시 호출)
export const setAccessToken = (token) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // 쿠키(Refresh Token) 전송용
});

export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

export const axiosPublic = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// 요청 인터셉터
axiosInstance.interceptors.request.use(
  (config) => {
    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터 (토큰 만료 시 쿠키로 재발급)
let isRefreshing = false;
let failedQueue = [];

// 대기 중인 요청 처리 함수
const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const response = error.response;

    if (!response) return Promise.reject(error);

    // 401(토큰 만료) 발생 && 아직 재시도 안 한 요청
    if ((response.status === 401 || response.status === 403) && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // 리프레시 요청
        const { data } = await authClient.post('/auth/refresh');

        const newToken = data.resultList?.accessToken;

        if (!newToken) throw new Error('토큰 갱신 실패: 토큰이 없습니다.');

        // 메모리 변수에 저장
        setAccessToken(newToken);

        // 큐에 대기중인 요청 처리
        processQueue(null, newToken);

        // 원래 요청 재시도
        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
        return axiosInstance(originalRequest);
      } catch (err) {
        processQueue(err, null);
        setAccessToken(null);
        localStorage.removeItem('isConnected'); // 로그인 상태 정보 삭제
        window.location.href = '/';
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

const handleResponse = (res) => {
  if (!res || !res.data) return null;
  if (Array.isArray(res.data)) return res.data;
  if (res.data.resultList) return res.data.resultList;
  return res.data.message || res.data;
};

export const getRequest = async (url, params) => {
  try {
    const res = await axiosInstance.get(url, { params });
    return handleResponse(res);
  } catch (error) {
    console.error(`GET 요청 실패:`, ('url', url), ('error', error));
    throw error;
  }
};

export const postRequest = async (url, data) => {
  try {
    const isFormData = data instanceof FormData;
    const config = isFormData ? { headers: { 'Content-Type': 'multipart/form-data' } } : {};
    const res = await axiosInstance.post(url, data, config);
    return handleResponse(res);
  } catch (error) {
    console.error(`POST 요청 실패:`, ('url', url), ('error', error));
    throw error;
  }
};

export const putRequest = async (url, data) => {
  try {
    const res = await axiosInstance.put(url, data);
    return handleResponse(res);
  } catch (error) {
    console.error(`PUT 요청 실패:`, ('url', url), ('error', error));
    throw error;
  }
};
export const deleteRequest = async (url, data) => {
  try {
    const res = await axiosInstance.delete(url, { data });
    return handleResponse(res);
  } catch (error) {
    console.error(`DELETE 요청 실패:`, ('url', url), ('error', error));
    throw error;
  }
};

export default axiosInstance;
