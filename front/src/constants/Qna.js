export const QNA_STATUS = {
  PENDING: {
    value: 1,
    label: '回答待ち',
    badgeClass: 'bg-secondary text-white',
  },
  ANSWERED: {
    value: 2,
    label: '回答済み',
    badgeClass: 'bg-success text-white',
  },
};

export const resolveQnaStatus = (statusCode, statusValue) => {
  if (statusCode && QNA_STATUS[statusCode]) {
    return QNA_STATUS[statusCode];
  }
  if (statusValue === 2) {
    return QNA_STATUS.ANSWERED;
  }
  return QNA_STATUS.PENDING;
};