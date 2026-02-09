import { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import CMMessage from '../constants/CMMessage';
import axiosInstance from '../services/axiosInstance';

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';
  return date.toISOString().slice(0, 10);
};

const useModal = () => {
  const [modals, setModals] = useState({
    confirm: false,
    loading: false,
    error: false,
    info: false,
  });
  const [message, setMessage] = useState('');

  const open = useCallback((type, msg = '') => {
    setMessage(msg);
    setModals((prev) => ({ ...prev, [type]: true }));
  }, []);

  const close = useCallback((type) => {
    setModals((prev) => ({ ...prev, [type]: false }));
  }, []);

  return { modals, message, open, close };
};

const useApiHandler = (navigate, openModal, closeModal) => {
  return useCallback(
    async (apiCall, showLoading = true) => {
      if (showLoading) openModal('loading');
      try {
        return await apiCall();
      } catch (error) {
        openModal('error', CMMessage.MSG_ERR_001);
        throw error;
      } finally {
        if (showLoading) closeModal('loading');
      }
    },
    [navigate, openModal, closeModal]
  );
};

export default function CM_90_1050() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);

  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const columnDefs = useMemo(() => [
    {
      headerName: '회원 번호',
      field: 'memberNumber',
      minWidth: 100,
      maxWidth: 120,
      flex: 1,
    },
    {
      headerName: '구매자 ID',
      field: 'loginId',
      minWidth: 100,
      maxWidth: 150,
      flex: 1,
    },
    {
      headerName: '상품명',
      field: 'productName',
      minWidth: 100,
      maxWidth: 120,
      flex: 2,
    },
    {
      headerName: '총 정산금액(a+b-c-d)',
      field: 'totalAmount',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        return params.value ? `${params.value.toLocaleString()}` : '0';
      },
    },
    {
      headerName: '판매 단가(a)',
      field: 'unitPrice',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        return params.value ? `${params.value.toLocaleString()}` : '0';
      },
    },
    {
      headerName: '배송비(b)',
      field: 'shippingCost',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        return params.value ? `${params.value.toLocaleString()}` : '0';
      },
    },
    {
      headerName: '상품 사입가(c)',
      field: 'price',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        return params.value ? `${params.value.toLocaleString()}` : '0';
      },
    },
    {
      headerName: '할인가(d)',
      field: 'discountAmount',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        return params.value ? `${params.value.toLocaleString()}` : '0';
      },
    },
    {
      headerName: '구매일',
      field: 'orderDate',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => formatDate(params.value),
    },
    {
      headerName: '판품일',
      field: 'orderDates',
      minWidth: 100,
      flex: 1,
      valueFormatter: (params) => formatDate(params.value),
    },
  ]);

  const fetchAllProducts = useCallback(async () => {
    setLoading(true);
    const resultList = await apiHandler(() =>
      axiosInstance.get('/admin/settlement/sales/findAll', undefined)
    );
    if (resultList) {
      setRowData(resultList);
    }
    setLoading(false);
  }, [apiHandler, navigate]);

  const handleSearch = useCallback(async () => {
    setLoading(true);

    const searchParams = {
      dateFrom,
      dateTo,
    };

    Object.keys(searchParams).forEach((key) => {
      if (
        searchParams[key] === '' ||
        searchParams[key] === null ||
        searchParams[key] === undefined
      ) {
        delete searchParams[key];
      }
    });

    const hasDateFrom = dateFrom && dateFrom.trim() !== '';
    const hasDateTo = dateTo && dateTo.trim() !== '';

    if (hasDateFrom && !hasDateTo) {
      open('error', CMMessage.MSG_ERR_002('시작 날짜'));
      setLoading(false);
      return;
    } else if (!hasDateFrom && hasDateTo) {
      open('error', CMMessage.MSG_ERR_002('종료 날짜'));
      setLoading(false);
      return;
    } else if (hasDateFrom && hasDateTo) {
      const fromDate = new Date(dateFrom);
      const toDate = new Date(dateTo);

      if (fromDate > toDate) {
        open('error', CMMessage.MSG_ERR_004);
        setLoading(false);
        return;
      }
    }

    if (Object.keys(searchParams).length === 0) {
      fetchAllProducts();
      setLoading(false);
      return;
    }

    try {
      const resultList = await apiHandler(() =>
        axiosInstance.get('/admin/settlement/sales/findSales', searchParams)
      );
      if (resultList) {
        setRowData(resultList);
      }
    } catch (error) {
      open('error', CMMessage.MSG_ERR_001);
    }

    setLoading(false);
  }, [dateFrom, dateTo, apiHandler, navigate, fetchAllProducts, open]);

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter' && !loading) {
        handleSearch();
      }
    },
    [handleSearch, loading]
  );

  const handleReset = useCallback(() => {
    setDateFrom('');
    setDateTo('');
    fetchAllProducts();
  }, [fetchAllProducts]);

  const handlePeriodSelect = (value) => {
    const today = new Date();
    let fromDate = '';

    if (value === 'week') {
      const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      fromDate = weekAgo.toISOString().split('T')[0];
    } else if (value === 'month') {
      const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
      fromDate = monthAgo.toISOString().split('T')[0];
    } else if (value === '3months') {
      const threeMonthsAgo = new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000);
      fromDate = threeMonthsAgo.toISOString().split('T')[0];
    }

    if (fromDate) {
      setDateFrom(fromDate);
      setDateTo(today.toISOString().split('T')[0]);
    }
  };

  useEffect(() => {
    fetchAllProducts();
  }, [fetchAllProducts]);

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">매출 정보(표)</h5>

        <div className="row mb-3">
          <div className="col-12">
            <div className="card shadow-sm">
              <div className="card-body">
                <div className="row g-3 align-items-end">
                  <div className="col-12 col-md-4">
                    <label className="form-label fw-semibold">조회기간</label>
                    <div className="d-flex flex-wrap align-items-center">
                      <select
                        className="form-select me-2 mb-2 mb-md-0"
                        style={{ width: '120px' }}
                        onChange={(e) => handlePeriodSelect(e.target.value)}
                      >
                        <option value="">기간 선택</option>
                        <option value="week">최근 1주</option>
                        <option value="month">최근 1개월</option>
                        <option value="3months">최근 3개월</option>
                      </select>
                      <input
                        type="date"
                        className="form-control me-2 mb-2 mb-md-0"
                        style={{ width: '150px' }}
                        value={dateFrom}
                        onChange={(e) => setDateFrom(e.target.value)}
                        onKeyDown={handleKeyDown}
                      />
                      <span className="me-2">~</span>
                      <input
                        type="date"
                        className="form-control"
                        style={{ width: '150px' }}
                        value={dateTo}
                        onChange={(e) => setDateTo(e.target.value)}
                        onKeyDown={handleKeyDown}
                      />
                    </div>
                  </div>

                  <div className="col-12 col-md-2 d-grid">
                    <div className="d-flex gap-2">
                      <button
                        className="btn btn-primary flex-fill"
                        onClick={handleSearch}
                        disabled={loading}
                      >
                        검색
                      </button>
                      <button
                        className="btn btn-primary flex-fill"
                        onClick={handleReset}
                        disabled={loading}
                      >
                        초기화
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <CM_90_1011_grid
          itemsPerPage={10}
          pagesPerGroup={5}
          rowData={rowData}
          columnDefs={columnDefs}
        />

        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
        <CM_99_1004 isOpen={modals.info} onClose={() => close('info')} Message={message} />
      </div>
    </div>
  );
}
