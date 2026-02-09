const DEFAULT_OBJECT_KEYS = [
  'filePath',
  'filepath',
  'path',
  'url',
  'imageUrl',
  'imageURL',
  'image_url',
  'image',
  'thumb',
  'thumbnail',
  'thumbnailUrl',
  'thumbnailURL',
  'full',
  'src',
];

const normalizeBaseUrl = (baseUrl) => {
  if (!baseUrl || typeof baseUrl !== 'string') return '';
  return baseUrl.replace(/\/+$/, '');
};

const resolveImageUrl = (raw, baseUrl) => {
  if (!raw) return null;
  if ((typeof File !== 'undefined' && raw instanceof File) || (typeof Blob !== 'undefined' && raw instanceof Blob)) {
    if (typeof URL !== 'undefined' && typeof URL.createObjectURL === 'function') {
      return URL.createObjectURL(raw);
    }
    return null;
  }
  if (typeof raw !== 'string') return null;
  const trimmed = raw.trim();
  if (!trimmed) return null;
  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed;
  }
  const normalizedBase = normalizeBaseUrl(baseUrl);
  if (!normalizedBase) return trimmed;
  return `${normalizedBase}/${trimmed.replace(/^\/+/, '')}`;
};

const extractFromObject = (obj) => {
  if (!obj || typeof obj !== 'object') return null;
  for (const key of DEFAULT_OBJECT_KEYS) {
    if (typeof obj[key] === 'string' && obj[key].trim()) {
      return obj[key].trim();
    }
  }
  return null;
};

const ensureArray = (value) => {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) return [];
    try {
      const parsed = JSON.parse(trimmed);
      if (Array.isArray(parsed)) {
        return parsed;
      }
    } catch {
      // ignore
    }
    if (trimmed.includes('|')) {
      return trimmed.split('|').map((v) => v.trim()).filter(Boolean);
    }
    if (trimmed.includes(',')) {
      return trimmed.split(',').map((v) => v.trim()).filter(Boolean);
    }
    return [trimmed];
  }
  return [value];
};

export const buildProductImages = (item, options = {}) => {
  if (!item || typeof item !== 'object') {
    return [];
  }
  const baseUrl = normalizeBaseUrl(
    options.baseUrl ?? import.meta?.env?.VITE_API_BASE_IMAGE_URL ?? ''
  );
  const seen = new Set();
  const results = [];

  const pushUrl = (url) => {
    if (!url || typeof url !== 'string') return;
    const normalized = url.trim();
    if (!normalized || seen.has(normalized)) return;
    seen.add(normalized);
    results.push({
      thumb: normalized,
      full: normalized,
    });
  };

  const addCandidate = (candidate) => {
    if (!candidate) return;
    if (typeof candidate === 'string') {
      const resolved = resolveImageUrl(candidate, baseUrl);
      if (resolved) pushUrl(resolved);
      return;
    }
    if (candidate instanceof File || candidate instanceof Blob) {
      const resolved = resolveImageUrl(candidate, baseUrl);
      if (resolved) pushUrl(resolved);
      return;
    }
    if (Array.isArray(candidate)) {
      candidate.forEach(addCandidate);
      return;
    }
    if (typeof candidate === 'object') {
      const fromObject = extractFromObject(candidate);
      if (fromObject) {
        addCandidate(fromObject);
        return;
      }
      Object.values(candidate).forEach(addCandidate);
    }
  };

  const arrayCandidates = [
    item.thumbnails,
    item.thumbnailList,
    item.thumbnailUrls,
    item.thumbnailUrlList,
    item.thumbnail,
    item.thumbs,
    item.images,
    item.imageList,
    item.imageUrls,
    item.detailImages,
    item.detailImageList,
    item.detailImageUrls,
    item.productImages,
    item.productImageList,
    item.productFiles,
    item.productFileList,
    item.attachments,
  ];

  arrayCandidates.forEach((candidate) => {
    ensureArray(candidate).forEach(addCandidate);
  });

  const singleCandidates = [
    item.thumbnailUrl,
    item.thumbnailPath,
    item.thumbnail,
    item.image,
    item.imageUrl,
    item.productFile,
    item.mainImage,
    item.mainImg,
    item.filePath,
    item.path,
    item.url,
  ];

  singleCandidates.forEach(addCandidate);

  return results.slice(0, options.limit ?? 20);
};
