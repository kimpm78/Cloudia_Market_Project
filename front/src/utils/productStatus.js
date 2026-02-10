const normalizeDeadlineString = (input) => {
  if (!input) return null;
  if (input instanceof Date && !Number.isNaN(input.valueOf())) {
    return input.toISOString();
  }
  if (typeof input !== 'string') return null;

  let value = input.trim();
  if (!value) return null;

  value = value
    .replace(/\//g, '-')
    .replace(/\./g, '-')
    .replace(/年/g, '-')
    .replace(/月/g, '-')
    .replace(/日/g, '')
    .replace(/\s+/g, ' ')
    .trim();

  value = value.replace(/-+$/, '');

  const compactMatch = value.match(/^(\d{4})(\d{2})(\d{2})(?:\s+(\d{2})(\d{2})(\d{2})?)?$/);
  if (compactMatch) {
    const [, y, m, d, hh = '23', mm = '59', ss = '59'] = compactMatch;
    return `${y}-${m}-${d}T${hh}:${mm}:${ss}`;
  }

  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return `${value}T23:59:59`;
  }

  if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}$/.test(value)) {
    return `${value.replace(' ', 'T')}:00`;
  }
  if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/.test(value)) {
    return value.replace(' ', 'T');
  }

  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) {
    return `${value}:00`;
  }

  return value;
};

export const parseReservationDeadline = (input) => {
  if (!input) return null;
  if (input instanceof Date && !Number.isNaN(input.valueOf())) {
    return new Date(input.getTime());
  }

  const normalized = normalizeDeadlineString(input);
  if (!normalized) return null;

  const timeValue = Date.parse(normalized);
  if (Number.isNaN(timeValue)) {
    return null;
  }
  return new Date(timeValue);
};

const toBoolean = (value) => {
  if (typeof value === 'boolean') return value;
  if (typeof value === 'number') return value !== 0;
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    return ['1', 'y', 'yes', 'true', 't'].includes(normalized);
  }
  return false;
};

const hasClosedStatusKeyword = (value) => {
  if (typeof value !== 'string') return false;
  const normalized = value.trim().toLowerCase();
  return ['closed', 'close', 'ended', 'end', 'finished', 'complete', 'completed'].includes(
    normalized
  );
};

export const hasReservationClosed = (item, options = {}) => {
  if (!item || typeof item !== 'object') return false;
  const codeValue = item.codeValue ?? item.reservationCodeValue ?? item.code_value;
  if (item.isReservationClosed === true) return true;
  if (codeValue !== undefined && codeValue !== null && String(codeValue).trim() === '4') {
    return true;
  }

  const directFlags = [
    item.reservationClosed,
    item.reservationSoldOut,
    item.reservationEnd,
    item.reservationEnded,
  ];
  if (directFlags.some((flag) => toBoolean(flag))) {
    return true;
  }

  const statusCandidates = [
    item.reservationStatus,
    item.reservation_state,
    item.status,
    item.codeName,
  ];
  if (statusCandidates.some(hasClosedStatusKeyword)) {
    return true;
  }

  if (options.useClientTime !== true) {
    return false;
  }

  const now = options.now instanceof Date && !Number.isNaN(options.now.valueOf())
    ? options.now
    : new Date();

  const deadlineCandidates = [
    item.reservationDeadline,
    item.reservation_deadline,
    item.reservationEndDate,
    item.reservation_end_date,
    item.deadline,
  ];

  for (const candidate of deadlineCandidates) {
    const parsed = parseReservationDeadline(candidate);
    if (parsed && parsed <= now) {
      return true;
    }
  }

  return false;
};

export const getReservationDeadlineDate = (item) => {
  if (!item || typeof item !== 'object') return null;
  const deadlineCandidates = [
    item.reservationDeadline,
    item.reservation_deadline,
    item.reservationEndDate,
    item.reservation_end_date,
    item.deadline,
  ];

  for (const candidate of deadlineCandidates) {
    const parsed = parseReservationDeadline(candidate);
    if (parsed) {
      return parsed;
    }
  }
  return null;
};
