import { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import CMMessage from '../constants/CMMessage';
import axiosInstance from '../services/axiosInstance';
import CM_90_1052_ReturnRequestModal from '../components/CM_90_1052_ReturnRequestModal';
import CM_90_1052_DeliveryTrackingModal from '../components/CM_90_1052_DeliveryTrackingModal';

const EXCHANGE_STATUS = [
  { value: 2, label: '교환 처리중' },
  { value: 3, label: '교환 완료' },
];
const REFUND_STATUS = [
  { value: 4, label: '환불 처리중' },
  { value: 5, label: '환불 완료' },
];

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

export default function CM_90_1052() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);

  const [rowData, setRowData] = useState([]);
  const [rowDetailData, setRowDetailData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [showDeliveryTrackingModal, setShowDeliveryTrackingModal] = useState(false);
  const [previousStatusValue, setPreviousStatusValue] = useState(null);
  const [showRefundModal, setShowRefundModal] = useState(false);

  const handleOrderClick = useCallback(
    async (orderData) => {
      const searchRequestDto = {
        memberNumber: orderData.memberNumber,
        orderNumber: orderData.orderNumber,
      };

      const resultList = await apiHandler(() =>
        axiosInstance.get('/admin/settlement/status/findDetails', searchRequestDto)
      );
      if (resultList) {
        setRowDetailData(resultList);
        setSelectedOrder(orderData);
        setShowOrderModal(true);
      }
    },
    [apiHandler]
  );

  const handleCloseModal = useCallback(() => {
    setShowOrderModal(false);
    setSelectedOrder(null);
  }, []);

  const handleCloseDeliveryTrackingModal = useCallback(() => {
    if (selectedOrder && previousStatusValue !== null) {
      const updatedRowData = rowData.map((row) =>
        row.returnId === selectedOrder.returnId
          ? { ...row, returnStatusValue: previousStatusValue }
          : row
      );
      setRowData(updatedRowData);
    }

    setShowDeliveryTrackingModal(false);
    setSelectedOrder(null);
    setPreviousStatusValue(null);
  }, [selectedOrder, previousStatusValue, rowData]);

  const handleStatusUpdate = useCallback(
    async (orderData, deliveryInfo = null) => {
      const updateRequestDto = {
        returnId: orderData.returnId,
        returnStatusValue: orderData.returnStatusValue,
      };

      if (deliveryInfo) {
        updateRequestDto.carrier = deliveryInfo.carrier;
        updateRequestDto.trackingNumber = deliveryInfo.trackingNumber;
      }

      // await apiHandler(() =>
      //   axiosInstance.post('/admin/settlement/refund/updateStatus', updateRequestDto)
      // );

      // 성공 시 목록 새로고침
      await fetchAllProducts();
    },
    [apiHandler]
  );

  const handleDeliveryTrackingConfirm = useCallback(
    async (deliveryInfo) => {
      try {
        await handleStatusUpdate(selectedOrder, deliveryInfo);
        open('info', '상태가 업데이트되었습니다.');
        setShowDeliveryTrackingModal(false);
        setSelectedOrder(null);
        setPreviousStatusValue(null);
      } catch (error) {
        if (selectedOrder && previousStatusValue !== null) {
          const updatedRowData = rowData.map((row) =>
            row.returnId === selectedOrder.returnId
              ? { ...row, returnStatusValue: previousStatusValue }
              : row
          );
          setRowData(updatedRowData);
        }
      }
    },
    [selectedOrder, handleStatusUpdate, previousStatusValue, rowData, open]
  );

  const DropdownCellRenderer = (params) => {
    const handleChange = async (e) => {
      const newValue = parseInt(e.target.value);
      const oldValue = params.value;

      // 교환 완료(3) 또는 환불 완료(5)인 경우 배송조회 모달 표시
      if (newValue === 3 || newValue === 5) {
        setPreviousStatusValue(oldValue);
        params.node.setDataValue(params.colDef.field, newValue);
        const updatedOrderData = { ...params.data, returnStatusValue: newValue };
        setSelectedOrder(updatedOrderData);
        setShowDeliveryTrackingModal(true);
        return;
      } else {
        // 그 외의 경우 바로 업데이트
        params.node.setDataValue(params.colDef.field, newValue);
        const updatedOrderData = { ...params.data, returnStatusValue: newValue };
        try {
          await handleStatusUpdate(updatedOrderData);
          open('info', '상태가 업데이트되었습니다.');
        } catch (error) {
          // 에러 시 원래 값으로 복구
          params.node.setDataValue(params.colDef.field, oldValue);
        }
      }
    };

    // returnStatusValue에 따라 표시할 옵션 결정
    const getStatusOptions = (statusValue) => {
      // 교환 상태
      if ([2, 3].includes(statusValue)) {
        return EXCHANGE_STATUS;
      }
      // 환불 상태
      if ([4, 5].includes(statusValue)) {
        return REFUND_STATUS;
      }
      // 기본값 (환불)
      return REFUND_STATUS;
    };

    const statusOptions = getStatusOptions(params.value);

    return (
      <select
        value={params.value || ''}
        onChange={handleChange}
        className="form-select"
        style={{
          width: '100%',
          height: '100%',
          border: '1px solid #ced4da',
          borderRadius: '4px',
          padding: '2px 8px',
          fontSize: '13px',
          backgroundColor: '#fff',
          backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m1 6 7 7 7-7'/%3e%3c/svg%3e")`,
          backgroundRepeat: 'no-repeat',
          backgroundPosition: 'right 8px center',
          backgroundSize: '16px 12px',
          paddingRight: '32px',
          appearance: 'none',
          cursor: 'pointer',
        }}
      >
        {statusOptions.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    );
  };

  const columnDefs = useMemo(
    () => [
      {
        headerName: '요청 번호',
        field: 'orderNo',
        minWidth: 100,
        maxWidth: 120,
        flex: 1,
      },
      {
        headerName: '요청일',
        field: 'requestedAt',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '구매자 ID',
        field: 'customerId',
        minWidth: 100,
        maxWidth: 150,
        flex: 1,
      },
      {
        headerName: '구매 번호',
        field: 'orderNumber',
        minWidth: 100,
        maxWidth: 120,
        flex: 2,
        cellRenderer: (params) => (
          <span
            className="text-primary text-decoration-underline"
            style={{ cursor: 'pointer' }}
            onClick={() => handleOrderClick(params.data)}
          >
            {params.value}
          </span>
        ),
      },
      {
        headerName: '환불 금액',
        field: 'refundAmount',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}` : '';
        },
      },
      {
        headerName: '배송 금액(고객 부담)',
        field: 'shippingFeeCustomerAmount',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}` : '';
        },
      },
      {
        headerName: '배송 금액(매장 부담)',
        field: 'shippingFeeSellerAmount',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}` : '';
        },
      },
      {
        headerName: '환불 총 금액',
        field: 'totalAmount',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}` : '';
        },
      },
      {
        headerName: '환불일',
        field: 'completedAt',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '상태',
        field: 'returnStatusValue',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
        cellRenderer: DropdownCellRenderer,
        cellStyle: {
          padding: '4px',
          display: 'flex',
          alignItems: 'center',
        },
      },
    ],
    [handleOrderClick, DropdownCellRenderer]
  );

  const fetchAllProducts = useCallback(async () => {
    setLoading(true);
    try {
      const resultList = await apiHandler(() =>
        axiosInstance.get('/admin/settlement/refund/findAll')
      );
      if (resultList.data.result) {
        setRowData(resultList.data.resultList);
      }
    } catch (error) {
      console.error('데이터 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  }, [apiHandler]);

  const handleSearch = useCallback(async () => {
    setLoading(true);

    const searchParams = {
      dateFrom,
      dateTo,
      orderStatusValue: selectedStatus,
      paymentMethod: selectedPaymentMethod,
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

    if (!hasDateFrom && !hasDateTo) {
      open('error', CMMessage.MSG_ERR_002('날짜'));
      setLoading(false);
      return;
    }

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
        axiosInstance.get('/admin/settlement/refund/findByPeriod', {
          params: searchParams,
        })
      );
      if (resultList.data.result) {
        setRowData(resultList.data.resultList);
      }
    } catch (error) {
      open('error', CMMessage.MSG_ERR_001);
    } finally {
      setLoading(false);
    }
  }, [dateFrom, dateTo, selectedStatus, selectedPaymentMethod, apiHandler, fetchAllProducts, open]);

  const handleRefundClick = useCallback(() => {
    setShowRefundModal(true);
  }, []);

  const handleRefundModalClose = useCallback(() => {
    setShowRefundModal(false);
  }, []);

  const handleRefundConfirm = useCallback(
    async (refundData) => {
      try {
        open('loading');
        await axiosInstance.post('/admin/settlement/refund/process', refundData);
        close('loading');
        open('info', '환불 처리가 완료되었습니다.');
        setShowRefundModal(false);
        fetchAllProducts();
      } catch (error) {
        close('loading');
        open('error', '환불 처리 중 오류가 발생했습니다.');
      }
    },
    [open, close, fetchAllProducts]
  );

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
    setSelectedStatus('');
    setSelectedPaymentMethod('');
    fetchAllProducts();
  }, [fetchAllProducts]);

  const handlePeriodSelect = (value) => {
    const getJSTDateString = (date) => {
      const formatter = new Intl.DateTimeFormat('ja-JP', {
        timeZone: 'Asia/Tokyo',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });

      const parts = formatter.formatToParts(date);
      const year = parts.find((p) => p.type === 'year').value;
      const month = parts.find((p) => p.type === 'month').value;
      const day = parts.find((p) => p.type === 'day').value;

      return `${year}-${month}-${day}`;
    };

    const today = new Date();
    let fromDate = '';

    if (value === 'week') {
      const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      fromDate = getJSTDateString(weekAgo);
    } else if (value === 'month') {
      const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
      fromDate = getJSTDateString(monthAgo);
    } else if (value === '3months') {
      const threeMonthsAgo = new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000);
      fromDate = getJSTDateString(threeMonthsAgo);
    }

    if (fromDate) {
      setDateFrom(fromDate);
      setDateTo(getJSTDateString(today));
    }
  };
  useEffect(() => {
    fetchAllProducts();
  }, [fetchAllProducts]);

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">환불/교환 관리</h5>

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
                        onClick={handleRefundClick}
                        disabled={loading}
                      >
                        환불 하기
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
        <CM_90_1052_ReturnRequestModal
          show={showRefundModal}
          onHide={handleRefundModalClose}
          onConfirm={handleRefundConfirm}
        />
        <CM_90_1052_DeliveryTrackingModal
          show={showDeliveryTrackingModal}
          onHide={handleCloseDeliveryTrackingModal}
          onConfirm={handleDeliveryTrackingConfirm}
          orderData={selectedOrder}
        />
      </div>
    </div>
  );
}
