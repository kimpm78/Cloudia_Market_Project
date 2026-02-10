const normalizeBaseUrl = (baseUrl) => {
  if (!baseUrl || typeof baseUrl !== 'string') return '';
  return baseUrl.replace(/\/+$/, '');
};

export const normalizeImageUrl = (html) => {
  if (typeof html !== 'string' || html.trim().length === 0) return '';

  const baseUrl = normalizeBaseUrl(import.meta.env.VITE_API_BASE_IMAGE_URL || '');
  if (!baseUrl) return html;

  return html.replace(
    /(<img[^>]+src=["'])(?!https?:\/\/|data:|blob:|\/{2})([^"']+)(["'][^>]*>)/gi,
    (_, prefix, src, suffix) => `${prefix}${baseUrl}/${src.replace(/^\/+/, '')}${suffix}`
  );
};
