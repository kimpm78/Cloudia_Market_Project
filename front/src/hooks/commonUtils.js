export const formatDate = (dateString) => {
  if (!dateString) return '';
  const dateOnly = dateString.includes('T') ? dateString.split('T')[0] : dateString.slice(0, 10);
  return dateOnly;
};
