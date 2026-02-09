import { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import CM_90_1011_grid from '../components/CM_90_1000_grid.jsx';
import CM_99_1001 from '../components/commonPopup/CM_99_1001.jsx';
import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';
import CMMessage from '../constants/CMMessage';
import OrderDetailModal from '../components/CM_90_1051_orderDetailModal.jsx';
import DeliveryInputModal from '../components/CM_90_1051_deliveryInputModal.jsx';
import axiosInstance from '../services/axiosInstance.js';
import { ORDER_STATUS, PAYMENT_METHODS } from '../constants/constants.js';
import { formatDate } from '../hooks/commonUtils.js';

const useModal = () => {
  const [modals, setModals] = useState({
    confirm: false,
    loading: false,
    error: false,
    info: false,
    yesno: false,
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

export default function CM_90_1051() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);

  const [rowData, setRowData] = useState([]);
  const [rowDetailData, setRowDetailData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [selectedStatus, setSelectedStatus] = useState(0);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(0);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [addressData, setAddressData] = useState([]);
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [showDeliveryModal, setShowDeliveryModal] = useState(false);
  const [previousStatusValue, setPreviousStatusValue] = useState(null);

  const handleOrderClick = useCallback(
    async (orderData) => {
      const searchRequestDto = {
        memberNumber: orderData.memberNumber,
        orderNumber: orderData.orderNumber,
      };
      const orderDetails = await apiHandler(() =>
        axiosInstance.get('/admin/settlement/status/findDetails', {
          params: searchRequestDto,
        })
      );
      const addressInfo = await apiHandler(() =>
        axiosInstance.get('/admin/settlement/status/address', {
          params: searchRequestDto,
        })
      );

      if (orderDetails.data.result && addressInfo.data.result) {
        setRowDetailData(orderDetails.data.resultList);
        setSelectedOrder(orderData);
        setAddressData(addressInfo.data.resultList);
        setShowOrderModal(true);
      } else {
        open('error', CMMessage.MSG_ERR_001);
      }
    },
    [apiHandler, navigate]
  );

  const handleCloseModal = useCallback(() => {
    setShowOrderModal(false);
    setSelectedOrder(null);
  }, []);

  const handleCloseDeliveryModal = useCallback(() => {
    if (selectedOrder && previousStatusValue !== null) {
      const updatedRowData = rowData.map((row) =>
        row.orderNumber === selectedOrder.orderNumber &&
        row.memberNumber === selectedOrder.memberNumber
          ? { ...row, orderStatusValue: previousStatusValue }
          : row
      );
      setRowData(updatedRowData);
    }

    setShowDeliveryModal(false);
    setSelectedOrder(null);
    setPreviousStatusValue(null);
  }, [selectedOrder, previousStatusValue, rowData]);

  const handleDelivery = useCallback(
    async (orderData, deliveryInfo = null) => {
      const searchRequestDto = {
        memberNumber: orderData.memberNumber,
        orderNumber: orderData.orderNumber,
        orderStatusValue: orderData.orderStatusValue,
        dateFrom: orderData.orderDate,
        dateTo: orderData.orderDate,
      };
      if (deliveryInfo) {
        searchRequestDto.carrier = deliveryInfo.carrier;
        searchRequestDto.trackingNumber = deliveryInfo.trackingNumber;
      }

      const result = await apiHandler(() =>
        axiosInstance.post('/admin/settlement/status/uptStatus', searchRequestDto)
      );

      return result;
    },
    [apiHandler]
  );

  const handleDeliveryConfirm = useCallback(
    async (deliveryInfo) => {
      try {
        await handleDelivery(selectedOrder, deliveryInfo);

        setShowDeliveryModal(false);
        setSelectedOrder(null);
        setPreviousStatusValue(null);

        await fetchAllProducts();
      } catch (error) {
        if (selectedOrder && previousStatusValue !== null) {
          const updatedRowData = rowData.map((row) =>
            row.orderNumber === selectedOrder.orderNumber &&
            row.memberNumber === selectedOrder.memberNumber
              ? { ...row, orderStatusValue: previousStatusValue }
              : row
          );
          setRowData(updatedRowData);
        }
        open('error', CMMessage.MSG_ERR_001);
      }
    },
    [selectedOrder, handleDelivery, previousStatusValue, rowData, open]
  );

  const fetchAllProducts = useCallback(async () => {
    setLoading(true);
    try {
      const result = await apiHandler(() => axiosInstance.get('/admin/settlement/status/findAll'));
      if (result.data.result) {
        setRowData(result.data.resultList);
      }
    } finally {
      setLoading(false);
    }
  }, [apiHandler, navigate]);

  const DropdownCellRenderer = useCallback(
    (params) => {
      const handleChange = (e) => {
        const newValue = parseInt(e.target.value);
        const oldValue = params.value;
        try {
          if (newValue === 4) {
            setPreviousStatusValue(oldValue);
            params.node.setDataValue(params.colDef.field, newValue);
            const updatedOrderData = { ...params.data, orderStatusValue: newValue };
            setSelectedOrder(updatedOrderData);
            setShowDeliveryModal(true);
            return;
          } else if (newValue === 6) {
            setSelectedOrder({
              ...params.data,
              orderStatusValue: newValue,
              oldValue: oldValue,
            });
            open('yesno', '정말로 구매 취소를 하시겠습니까?');
            return;
          } else {
            params.node.setDataValue(params.colDef.field, newValue);
            const updatedOrderData = { ...params.data, orderStatusValue: newValue };
            handleDelivery(updatedOrderData)
              .then((result) => {
                if (!result.data.result) {
                  setTimeout(() => {
                    open('error', result.data.message);
                  }, 0);
                }

                fetchAllProducts();
              })
              .catch(() => {
                params.node.setDataValue(params.colDef.field, oldValue);
              });
          }
        } catch (error) {
          open('error', CMMessage.MSG_ERR_001);
        }
      };

      return (
        <select
          value={params.value || ''}
          onChange={handleChange}
          className="form-select"
          disabled={params.value === 6}
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
          {ORDER_STATUS.filter((option) => option.value !== 0).map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      );
    },
    [handleDelivery, fetchAllProducts, open]
  );

  const handleConfirmSave = useCallback(async () => {
    if (!selectedOrder) return;

    const updatedOrderData = {
      ...selectedOrder,
      orderStatusValue: 6,
    };

    try {
      const result = await handleDelivery(updatedOrderData);

      if (!result.data.result) {
        open('error', result.data.message);
      }

      await fetchAllProducts();
    } catch (error) {
      if (selectedOrder.oldValue !== undefined) {
        const updatedRowData = rowData.map((row) =>
          row.orderNumber === selectedOrder.orderNumber &&
          row.memberNumber === selectedOrder.memberNumber
            ? { ...row, orderStatusValue: selectedOrder.oldValue }
            : row
        );
        setRowData(updatedRowData);
      }
    } finally {
      close('yesno');
      setSelectedOrder(null);
    }
  }, [selectedOrder, handleDelivery, fetchAllProducts, open, close, rowData]);

  const columnDefs = useMemo(
    () => [
      {
        headerName: '사원 번호',
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
        headerName: '구매자 성함',
        field: 'name',
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
        headerName: '결제 금액',
        field: 'totalAmount',
        minWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}` : '';
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
        headerName: '결제 방식',
        field: 'paymentValue',
        minWidth: 80,
        maxWidth: 150,
        flex: 1,
        cellRenderer: (params) => {
          const category = PAYMENT_METHODS.find((cat) => cat.value === params.value);
          const label = category?.label || params.value;
          return <span>{label}</span>;
        },
      },
      {
        headerName: '배송 예정일',
        field: 'deliveryDate',
        minWidth: 130,
        maxWidth: 180,
        flex: 1,
      },
      {
        headerName: '배송 추적 번호',
        field: 'trackingNumber',
        minWidth: 130,
        maxWidth: 180,
        flex: 1,
      },
      {
        headerName: '발송일',
        field: 'shippingDate',
        minWidth: 130,
        maxWidth: 180,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '상태',
        field: 'orderStatusValue',
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
      await fetchAllProducts();
      return;
    }

    try {
      const result = await apiHandler(() =>
        axiosInstance.get('/admin/settlement/status/findOrder', {
          params: searchParams,
        })
      );
      if (result.data.result) {
        setRowData(result.data.resultList);
      }
    } catch (error) {
      open('error', CMMessage.MSG_ERR_001);
    } finally {
      setLoading(false);
    }
  }, [
    dateFrom,
    dateTo,
    selectedStatus,
    selectedPaymentMethod,
    apiHandler,
    navigate,
    fetchAllProducts,
    open,
  ]);

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
    setSelectedStatus(0);
    setSelectedPaymentMethod(0);
    fetchAllProducts();
  }, [fetchAllProducts]);

  const handlePeriodSelect = useCallback((value) => {
    const now = new Date(
      new Intl.DateTimeFormat('en-US', {
        timeZone: 'Asia/Tokyo',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      })
        .format(new Date())
        .replace(/(\d+)\/(\d+)\/(\d+), (\d+):(\d+):(\d+)/, '$3-$1-$2T$4:$5:$6')
    );

    const toDate = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Tokyo',
    }).format(now);

    let fromDate = '';

    if (value === 'week') {
      const d = new Date(now);
      d.setDate(d.getDate() - 7);
      fromDate = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'Asia/Tokyo',
      }).format(d);
    }

    if (value === 'month') {
      const d = new Date(now);
      d.setDate(d.getDate() - 30);
      fromDate = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'Asia/Tokyo',
      }).format(d);
    }

    if (value === '3months') {
      const d = new Date(now);
      d.setDate(d.getDate() - 90);
      fromDate = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'Asia/Tokyo',
      }).format(d);
    }

    if (fromDate) {
      setDateFrom(fromDate);
      setDateTo(toDate);
    }
  }, []);

  useEffect(() => {
    fetchAllProducts();
  }, [fetchAllProducts]);

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">정산 상태 확인</h5>

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

                  <div className="col-6 col-md-3">
                    <label className="form-label fw-semibold">결제방식</label>
                    <select
                      className="form-select"
                      value={selectedPaymentMethod}
                      onChange={(e) => setSelectedPaymentMethod(e.target.value)}
                      onKeyDown={handleKeyDown}
                    >
                      {PAYMENT_METHODS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="col-6 col-md-3">
                    <label className="form-label fw-semibold">상태</label>
                    <select
                      className="form-select"
                      value={selectedStatus}
                      onChange={(e) => setSelectedStatus(e.target.value)}
                      onKeyDown={handleKeyDown}
                    >
                      {ORDER_STATUS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
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
                        className="btn btn-outline-secondary flex-fill"
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

        <OrderDetailModal
          show={showOrderModal}
          onHide={handleCloseModal}
          orderData={selectedOrder}
          addressData={addressData}
          rowData={rowDetailData}
        />

        <DeliveryInputModal
          show={showDeliveryModal}
          onHide={handleCloseDeliveryModal}
          onConfirm={handleDeliveryConfirm}
          orderData={selectedOrder}
        />
        <CM_99_1001
          isOpen={modals.yesno}
          onClose={() => close('yesno')}
          Message={message}
          onConfirm={handleConfirmSave}
        />
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
        <CM_99_1004 isOpen={modals.info} onClose={() => close('info')} Message={message} />
      </div>
    </div>
  );
}
