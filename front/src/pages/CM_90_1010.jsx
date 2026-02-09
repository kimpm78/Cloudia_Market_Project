import { useEffect, useState, useCallback, useMemo } from 'react';
import { Line, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import axiosInstance from '../services/axiosInstance';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend
);

// 모달 및 메시지
const useModal = () => {
  const [modals, setModals] = useState({
    loading: false,
    error: false,
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

export default function CM_90_1000() {
  const { modals, message, open, close } = useModal();

  const [dashboardData, setDashboardData] = useState({
    statusManagement: {
      newOrder: 0,
      cancelRequested: 0,
      reserved: 0,
      paymentPending: 0,
      answerPending: 0,
      answerCompleted: 0,
      exchangeInProgress: 0,
      exchangeCompleted: 0,
      refundInProgress: 0,
      refundCompleted: 0,
      purchaseConfirmed: 0,
      exchangeRequested: 0,
      refundRequested: 0,
      preparingShipment: 0,
      inTransit: 0,
      delivered: 0,
    },
    salesInfo: {
      previousNetSales: 0,
      settlementAmount: 0,
      refundAmount: 0,
      orderCount: 0,
      refundCount: 0,
      shipmentProcessedCount: 0,
      deliveredCount: 0,
    },
    weeklyOrders: [
      {
        day: '일',
        value: 123123,
      },
      {
        day: '월',
        value: 123123,
      },
      {
        day: '화',
        value: 123123,
      },
      {
        day: '수',
        value: 123123,
      },
      {
        day: '목',
        value: 123123,
      },
      {
        day: '금',
        value: 123123,
      },
      {
        day: '토',
        value: 123123,
      },
    ],
    weeklyVisitors: {
      pv: [0, 0, 0, 0, 0, 0, 0],
      uv: [0, 0, 0, 0, 0, 0, 0],
    },
    monthlyVisitors: {
      pv: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      uv: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    },
  });

  const [isDataLoaded, setIsDataLoaded] = useState(false);

  // 데이터 가져오기
  useEffect(() => {
    const fetchAnalyticsData = async () => {
      try {
        open('loading');

        // 상태
        const statusResponse = await axiosInstance.get('/admin/main/status');
        // 전날 정보
        const previousResponse = await axiosInstance.get('/admin/main/previous');
        // 일주일 매출
        const weeklySalesResponse = await axiosInstance.get('/admin/main/weekly');
        // 일별 방문자
        const weeklyResponse = await axiosInstance.get('/admin/main/weekly-visitors');
        // 월별 방문자
        const monthlyResponse = await axiosInstance.get('/admin/main/monthly-visitors');

        const dayMapping = {
          Sun: '일',
          Mon: '월',
          Tue: '화',
          Wed: '수',
          Thu: '목',
          Fri: '금',
          Sat: '토',
        };

        const weeklyOrdersData = weeklySalesResponse.data.resultList.map((item) => ({
          day: dayMapping[item.dayOfWeek],
          value: item.totalAmount,
        }));

        setDashboardData((prev) => ({
          ...prev,
          statusManagement: {
            newOrder: statusResponse.data.resultList.newOrder,
            cancelRequested: statusResponse.data.resultList.cancelRequested,
            reserved: statusResponse.data.resultList.reserved,
            paymentPending: statusResponse.data.resultList.paymentPending,
            answerPending: statusResponse.data.resultList.answerPending,
            answerCompleted: statusResponse.data.resultList.answerCompleted,
            exchangeInProgress: statusResponse.data.resultList.exchangeInProgress,
            exchangeCompleted: statusResponse.data.resultList.exchangeCompleted,
            refundInProgress: statusResponse.data.resultList.refundInProgress,
            refundCompleted: statusResponse.data.resultList.refundCompleted,
            purchaseConfirmed: statusResponse.data.resultList.purchaseConfirmed,
            exchangeRequested: statusResponse.data.resultList.exchangeRequested,
            refundRequested: statusResponse.data.resultList.refundRequested,
            preparingShipment: statusResponse.data.resultList.preparingShipment,
            inTransit: statusResponse.data.resultList.inTransit,
            delivered: statusResponse.data.resultList.delivered,
          },
          salesInfo: {
            previousNetSales: previousResponse.data.resultList.previousNetSales || 0,
            settlementAmount: previousResponse.data.resultList.settlementAmount || 0,
            refundAmount: previousResponse.data.resultList.refundAmount || 0,
            orderCount: previousResponse.data.resultList.orderCount || 0,
            refundCount: previousResponse.data.resultList.refundCount || 0,
            shipmentProcessedCount: previousResponse.data.resultList.shipmentProcessedCount || 0,
            deliveredCount: previousResponse.data.resultList.deliveredCount || 0,
          },
          weeklyOrders: weeklyOrdersData,
          weeklyVisitors: {
            pv: weeklyResponse.data.resultList.pv,
            uv: weeklyResponse.data.resultList.uv,
          },
          monthlyVisitors: {
            pv: monthlyResponse.data.resultList.pv,
            uv: monthlyResponse.data.resultList.uv,
          },
        }));

        setIsDataLoaded(true);
        close('loading');
      } catch (error) {
        setIsDataLoaded(true);
        close('loading');
        open('error', '데이터 로드 실패');
      }
    };

    fetchAnalyticsData();
  }, []);

  // 주간 주문 차트 설정 (세로 막대 그래프)
  const weeklyOrdersChartData = useMemo(() => {
    return {
      labels: dashboardData.weeklyOrders.map((item) => item.day),
      datasets: [
        {
          label: '일주일 매출 현황',
          data: dashboardData.weeklyOrders.map((item) => item.value),
          backgroundColor: 'rgba(99, 102, 241, 0.7)',
          borderColor: 'rgba(99, 102, 241, 1)',
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    };
  }, [dashboardData.weeklyOrders]);

  // 일별 방문자 차트 설정
  const weeklyVisitorsChartData = useMemo(() => {
    const days = ['일', '월', '화', '수', '목', '금', '토'];

    return {
      labels: days,
      datasets: [
        {
          label: 'PV (페이지뷰)',
          data: dashboardData.weeklyVisitors.pv,
          borderColor: 'rgba(99, 102, 241, 1)',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
        },
        {
          label: 'UV (순방문자)',
          data: dashboardData.weeklyVisitors.uv,
          borderColor: 'rgba(16, 185, 129, 1)',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.4,
        },
      ],
    };
  }, [dashboardData.weeklyVisitors]);

  // 월별 방문자 차트 설정
  const monthlyVisitorsChartData = useMemo(() => {
    const months = [
      '1월',
      '2월',
      '3월',
      '4월',
      '5월',
      '6월',
      '7월',
      '8월',
      '9월',
      '10월',
      '11월',
      '12월',
    ];

    return {
      labels: months,
      datasets: [
        {
          label: 'PV (페이지뷰)',
          data: dashboardData.monthlyVisitors.pv,
          borderColor: 'rgba(99, 102, 241, 1)',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
        },
        {
          label: 'UV (순방문자)',
          data: dashboardData.monthlyVisitors.uv,
          borderColor: 'rgba(16, 185, 129, 1)',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.4,
        },
      ],
    };
  }, [dashboardData.monthlyVisitors]);

  const lineChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  };

  const barChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: function (context) {
            return `매출: ${context.parsed.y.toLocaleString()}원`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function (value) {
            return value.toLocaleString() + '원';
          },
        },
      },
    },
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-4">관리 페이지</h5>

        {/* 상태관리 */}
        <div className="card mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">상태관리</h6>
            <div className="row g-3">
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">신규 주문</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.newOrder}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">입금 대기</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.paymentPending}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">구매 확정</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.purchaseConfirmed}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">예약 확정</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.reserved}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">교환 요청</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.exchangeRequested}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">교환 처리중</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.exchangeInProgress}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">교환 완료</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.exchangeCompleted}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">환불 요청</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.refundRequested}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">환불 처리중</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.refundInProgress}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">환불 완료</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.refundCompleted}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">배송 준비중</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.preparingShipment}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">배송중</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.inTransit}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">배송 완료</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.delivered}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">취소 확정</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.cancelRequested}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">답변 대기</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.answerPending}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">답변 완료</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.answerCompleted}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 매출 정보 및 주간 차트 */}
        <div className="row mb-4">
          {/* 매출 정보 */}
          <div className="col-lg-5">
            <div className="card h-100">
              <div className="card-body">
                <h6 className="card-title mb-3">매출 정보</h6>
                <div className="mb-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-muted">어제의 순매출 현황</span>
                  </div>
                  <h3 className="text-primary mb-0">
                    {dashboardData.salesInfo.previousNetSales} 원
                  </h3>
                </div>

                <table className="table table-sm table-borderless">
                  <tbody>
                    <tr>
                      <td className="text-muted">매출 금액</td>
                      <td className="text-end">{dashboardData.salesInfo.settlementAmount} 원</td>
                      <td className="text-muted">환불 금액</td>
                      <td className="text-end">{dashboardData.salesInfo.refundAmount} 원</td>
                    </tr>
                    <tr>
                      <td className="text-muted">주문 건수</td>
                      <td className="text-end">{dashboardData.salesInfo.orderCount} 건</td>
                      <td className="text-muted">환불/반품 건수</td>
                      <td className="text-end">{dashboardData.salesInfo.refundCount} 건</td>
                    </tr>
                    <tr>
                      <td className="text-muted">배송 처리 건수</td>
                      <td className="text-end">
                        {dashboardData.salesInfo.shipmentProcessedCount} 건
                      </td>
                      <td className="text-muted">배송 완료 건수</td>
                      <td className="text-end">{dashboardData.salesInfo.deliveredCount} 건</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* 일주일 매출 현황 */}
          <div className="col-lg-7">
            <div className="card h-100">
              <div className="card-body">
                <h6 className="card-title mb-3">일주일 매출 현황</h6>
                <div style={{ height: '250px' }}>
                  <Bar data={weeklyOrdersChartData} options={barChartOptions} />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 방문자 수 */}
        <div className="card mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">방문자 수</h6>
            {isDataLoaded ? (
              <div className="row">
                <div className="col-md-6 mb-3 mb-md-0">
                  <h6 className="text-center mb-3">일 방문자 수</h6>
                  <div style={{ height: '300px' }}>
                    <Line data={weeklyVisitorsChartData} options={lineChartOptions} />
                  </div>
                </div>
                <div className="col-md-6">
                  <h6 className="text-center mb-3">총 방문자 수</h6>
                  <div style={{ height: '300px' }}>
                    <Line data={monthlyVisitorsChartData} options={lineChartOptions} />
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-5">
                <div className="spinner-border text-primary" role="status">
                  <span className="visually-hidden">Loading...</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 모달 컴포넌트 */}
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      </div>
    </div>
  );
}
