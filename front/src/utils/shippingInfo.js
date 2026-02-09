const STRING_FIELDS = [
  'addressNickname',
  'recipientName',
  'recipientPhone',
  'postalCode',
  'addressMain',
  'addressDetail1',
  'addressDetail2',
  'addressDetail3',
  'memo',
  'fullAddress',
];

const NUMERIC_FIELDS = ['addressId'];
const BOOLEAN_FIELDS = ['isDefault'];

const FIELD_MAP = {
  addressId: ['addressId', 'shippingAddressId'],
  addressNickname: ['addressNickname', 'nickname', 'shippingNickname'],
  recipientName: ['recipientName', 'receiverName', 'shippingName', 'deliveryName'],
  recipientPhone: [
    'recipientPhone',
    'receiverPhone',
    'shippingPhone',
    'contactNumber',
    'deliveryPhone',
  ],
  postalCode: ['postalCode', 'zipCode', 'zipcode', 'shippingZipcode', 'zip'],
  addressMain: [
    'addressMain',
    'address1',
    'addressLine1',
    'address',
    'shippingAddress',
    'shippingAddress1',
    'street',
    'roadAddress',
  ],
  addressDetail1: [
    'addressDetail1',
    'addressDetail',
    'address2',
    'addressLine2',
    'shippingAddress2',
    'detailAddress',
  ],
  addressDetail2: ['addressDetail2', 'shippingAddressDetail2', 'addressDetailExtra'],
  addressDetail3: ['addressDetail3', 'shippingAddressDetail3'],
  memo: ['memo', 'deliveryMemo', 'shippingMemo', 'orderMemo'],
  fullAddress: ['fullAddress', 'addressFull'],
  isDefault: ['isDefault', 'default', 'defaultYn'],
};

const AUXILIARY_ADDRESS_KEYS = [
  'address',
  'shippingAddress',
  'shippingAddress1',
  'receiverAddress',
  'orderAddress',
];

const hasText = (value) => typeof value === 'string' && value.trim().length > 0;
const isNumber = (value) => typeof value === 'number' && Number.isFinite(value);

const parseBoolean = (value) => {
  if (typeof value === 'boolean') return value;
  if (typeof value === 'number') return value === 1;
  if (!hasText(value)) return null;
  const normalized = value.trim().toLowerCase();
  if (['y', 'yes', 'true', '1'].includes(normalized)) return true;
  if (['n', 'no', 'false', '0'].includes(normalized)) return false;
  return null;
};

const pickValue = (source, paths) => {
  if (!source || typeof source !== 'object') return null;
  for (const path of paths) {
    const value = getNestedValue(source, path);
    if (value === null || value === undefined) continue;
    if (typeof value === 'string') {
      if (hasText(value)) return value.trim();
    } else if (typeof value === 'number') {
      return value;
    } else if (typeof value === 'boolean') {
      return value;
    }
  }
  return null;
};

const getNestedValue = (source, path) => {
  if (!path.includes('.')) {
    return source[path];
  }
  return path.split('.').reduce((acc, segment) => {
    if (acc && typeof acc === 'object') {
      return acc[segment];
    }
    return undefined;
  }, source);
};

const toCandidate = (value) => {
  if (!value || typeof value !== 'object') return null;
  const candidate = {};

  Object.entries(FIELD_MAP).forEach(([field, keys]) => {
    const raw = pickValue(value, keys);
    if (raw === null || raw === undefined) {
      candidate[field] = null;
      return;
    }
    if (STRING_FIELDS.includes(field) && typeof raw === 'string') {
      candidate[field] = raw.trim();
      return;
    }
    if (NUMERIC_FIELDS.includes(field)) {
      if (isNumber(raw)) {
        candidate[field] = raw;
        return;
      }
      if (typeof raw === 'string') {
        const numeric = Number(raw.replace(/[^0-9.-]/g, ''));
        candidate[field] = Number.isFinite(numeric) ? numeric : null;
        return;
      }
    }
    if (BOOLEAN_FIELDS.includes(field)) {
      candidate[field] = parseBoolean(raw);
      return;
    }
    candidate[field] = raw;
  });

  const auxiliaryAddress = pickValue(value, AUXILIARY_ADDRESS_KEYS);
  if (!hasText(candidate.addressMain) && hasText(auxiliaryAddress)) {
    candidate.addressMain = auxiliaryAddress;
  }
  if (!hasText(candidate.fullAddress) && hasText(auxiliaryAddress)) {
    candidate.fullAddress = auxiliaryAddress;
  }
  return candidate;
};

export const normalizeShippingCandidate = (source) => toCandidate(source);

const applyFallbacks = (target, defaults = {}) => {
  const result = { ...target };
  Object.entries(defaults || {}).forEach(([key, value]) => {
    if (
      result[key] === undefined ||
      result[key] === null ||
      (typeof result[key] === 'string' && !result[key].trim().length)
    ) {
      result[key] = value;
    }
  });
  return result;
};

export const mergeShippingCandidates = (candidates = [], defaults = {}) => {
  const result = {};

  candidates.forEach((candidate) => {
    if (!candidate) return;

    Object.entries(candidate).forEach(([key, value]) => {
      if (value === undefined || value === null) return;
      if (typeof value === 'string' && !value.trim().length) return;
      result[key] = value;
    });
  });

  return applyFallbacks(result, defaults);
};

export const buildAddressDetail = ({ addressDetail1, addressDetail2, addressDetail3 }) => {
  return [addressDetail1, addressDetail2, addressDetail3]
    .filter((part) => hasText(part))
    .map((part) => part.trim())
    .join(' ');
};

export const buildFullAddress = ({
  postalCode,
  addressMain,
  addressDetail1,
  addressDetail2,
  addressDetail3,
  fullAddress,
}) => {
  if (hasText(fullAddress)) {
    return fullAddress.trim();
  }
  const detailLine = buildAddressDetail({ addressDetail1, addressDetail2, addressDetail3 });
  const segments = [addressMain, detailLine]
    .filter((part) => hasText(part))
    .map((part) => part.trim());
  if (segments.length === 0) {
    return '';
  }
  const base = segments.join(' ');
  if (hasText(postalCode)) {
    return `(${postalCode.trim()}) ${base}`.trim();
  }
  return base.trim();
};

export const deriveShippingInfo = ({
  orderData,
  locationState,
  shippingOverride,
  selectedAddress,
  userDefaults = {},
}) => {
  const normalizedDefaults = normalizeShippingCandidate(userDefaults) || {};
  const normalizedCandidates = [
    normalizeShippingCandidate(orderData),
    normalizeShippingCandidate(locationState),
    normalizeShippingCandidate(orderData?.shipping),
    normalizeShippingCandidate(locationState?.shipping),
    normalizeShippingCandidate(selectedAddress),
    normalizeShippingCandidate(shippingOverride),
  ].filter(Boolean);

  const merged = mergeShippingCandidates(normalizedCandidates, normalizedDefaults);
  const addressDetail = buildAddressDetail(merged);
  const fullAddress = buildFullAddress(merged);
  const hasData =
    hasText(merged.recipientName) ||
    hasText(merged.recipientPhone) ||
    hasText(merged.addressMain) ||
    hasText(addressDetail);

  return {
    ...merged,
    addressDetail,
    fullAddress,
    displayAddress: hasText(fullAddress) ? fullAddress : '',
    hasData,
  };
};
