import axios from 'axios';

let accessToken = null;

// トークン更新関数（ログイン成功時、またはリフレッシュ成功時に呼び出す）
export const setAccessToken = (token) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // Cookie（Refresh Token）送信用
});

export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

export const axiosPublic = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// リクエストインターセプター
axiosInstance.interceptors.request.use(
  (config) => {
    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// レスポンスインターセプター（トークン失効時にCookieで再発行）
let isRefreshing = false;
let failedQueue = [];

// 待機中リクエストの処理関数
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

    // 401（トークン失効）/403 が発生 && まだ再試行していないリクエスト
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
        // リフレッシュリクエスト
        const { data } = await authClient.post('/auth/refresh');

        const newToken = data.resultList?.accessToken;

        if (!newToken) throw new Error('トークン更新失敗: トークンがありません。');

        // メモリ変数に保存
        setAccessToken(newToken);

        // キューに待機中のリクエストを処理
        processQueue(null, newToken);

        // 元のリクエストを再試行
        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
        return axiosInstance(originalRequest);
      } catch (err) {
        processQueue(err, null);
        setAccessToken(null);
        localStorage.removeItem('isConnected'); // ログイン状態情報を削除
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
    console.error(`GET リクエスト失敗:`, ('url', url), ('error', error));
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
    console.error(`POST リクエスト失敗:`, ('url', url), ('error', error));
    throw error;
  }
};

export const putRequest = async (url, data) => {
  try {
    const res = await axiosInstance.put(url, data);
    return handleResponse(res);
  } catch (error) {
    console.error(`PUT リクエスト失敗:`, ('url', url), ('error', error));
    throw error;
  }
};
export const deleteRequest = async (url, data) => {
  try {
    const res = await axiosInstance.delete(url, { data });
    return handleResponse(res);
  } catch (error) {
    console.error(`DELETE リクエスト失敗:`, ('url', url), ('error', error));
    throw error;
  }
};

export default axiosInstance;
