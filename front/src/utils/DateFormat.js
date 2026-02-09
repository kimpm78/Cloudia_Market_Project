export function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`; // YYYY/MM/DD 통일
}

// YYYY/MM/DD HH:mm 형식으로 변환
export function formatDateTime(dateInput, options = {}) {
  if (!dateInput) return '';
  const date = dateInput instanceof Date ? dateInput : new Date(dateInput);
  if (Number.isNaN(date.valueOf())) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const separator = options.separator || '.';
  return `${year}${separator}${month}${separator}${day} ${hours}:${minutes}`;
}

// YYYY.MM.DD 형식으로 변환 (상세페이지 등에서 사용)
export function formatYearMonthDot(value) {
  if (!value) return '';
  const raw = String(value).trim();
  if (!raw) return '';
  const dateOnly = raw.split(/[ T]/)[0];
  return dateOnly ? dateOnly.replace(/-/g, '.') : raw.replace(/-/g, '.');
}
