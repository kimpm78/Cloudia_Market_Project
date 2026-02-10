import { useState, useMemo, useEffect, useCallback } from 'react';

import CM_Pagination from '../components/CM_99_1013_pagination';
import CM_01_1005_Filter from '../components/CM_01_1005_OrderSearchFilter';
import CM_01_1005_Table from '../components/CM_01_1005_OrderHistoryTable';
import { getRequest } from '../services/axiosInstance';
import { ORDER_STATUS_CODE } from '../constants/constants';

import '../styles/CM_01_1005.css';

const ALL_OPTION = { value: '', name: 'すべて' };

export default function CM_01_1005() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusOptions, setStatusOptions] = useState([ALL_OPTION]);
  const [paymentOptions, setPaymentOptions] = useState([ALL_OPTION]);
  const [filters, setFilters] = useState({
    year: '',
    month: '',
    orderStatusValue: '',
    paymentMethod: '',
    keyword: '',
  });

  const [activeFilters, setActiveFilters] = useState(filters);
  const pageSize = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const fetchCommonCodes = useCallback(async (group, setOptions) => {
    try {
      const data = await getRequest(`/common-codes?group=${group}`);
      if (Array.isArray(data)) {
        const filteredData =
          group === '008'
            ? data.filter((code) => Number(code.codeValue) !== ORDER_STATUS_CODE.PAYMENT_FAILED)
            : data;
        const options = [
          ALL_OPTION,
          ...filteredData.map((code) => ({
            value: String(code.codeValue),
            name: code.codeValueName,
          })),
        ];
        setOptions(options);
      }
    } catch (error) {
      console.error(`共通コード（${group}）の取得に失敗:`, error);
    }
  }, []);

  useEffect(() => {
    fetchCommonCodes('008', setStatusOptions);
    fetchCommonCodes('011', setPaymentOptions);
  }, [fetchCommonCodes]);

  // 注文履歴の取得
  const fetchOrderHistory = useCallback(async () => {
    setLoading(true);
    setCurrentPage(1);

    const searchParams = Object.entries(activeFilters).reduce((acc, [key, value]) => {
      if (value) acc[key] = value;
      return acc;
    }, {});

    try {
      const data = await getRequest('/user/mypage/purchases', searchParams);
      if (Array.isArray(data)) {
        setOrders(
          data.filter(
            (order) => Number(order.orderStatusValue) !== ORDER_STATUS_CODE.PAYMENT_FAILED
          )
        );
      } else if (data && Array.isArray(data.resultList)) {
        setOrders(
          data.resultList.filter(
            (order) => Number(order.orderStatusValue) !== ORDER_STATUS_CODE.PAYMENT_FAILED
          )
        );
      } else {
        setOrders([]);
      }
    } catch (error) {
      console.error('注文履歴の取得に失敗:', error);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [activeFilters]);

  useEffect(() => {
    fetchOrderHistory();
  }, [fetchOrderHistory]);

  const handleFilterChange = (e) => {
    const { id, value } = e.target;
    setFilters((prev) => ({ ...prev, [id]: value }));
  };

  const handleSearch = () => {
    setActiveFilters(filters);
  };

  const totalPages = useMemo(() => {
    return Math.ceil(orders.length / pageSize) || 1;
  }, [orders.length, pageSize]);

  const paginatedOrders = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return orders.slice(start, start + pageSize);
  }, [currentPage, pageSize, orders]);

  return (
    <div className="container mt-2">
      <div className="d-flex justify-content-between align-items-end pb-4 mb-4 border-bottom">
        <h2 className="fw-bolder mt-3">購入履歴</h2>
      </div>
      <h2 className="fw-bold fs-5 mt-3 mb-4">注文履歴検索</h2>

      <CM_01_1005_Filter
        filters={filters}
        handleFilterChange={handleFilterChange}
        handleSearch={handleSearch}
        loading={loading}
        statusOptions={statusOptions}
        paymentOptions={paymentOptions}
      />

      <CM_01_1005_Table orders={paginatedOrders} loading={loading} />

      <CM_Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </div>
  );
}