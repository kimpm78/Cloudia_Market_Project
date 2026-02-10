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

// モーダルおよびメッセージ
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
        day: '日',
        value: 123123,
      },
      {
        day: '月',
        value: 123123,
      },
      {
        day: '火',
        value: 123123,
      },
      {
        day: '水',
        value: 123123,
      },
      {
        day: '木',
        value: 123123,
      },
      {
        day: '金',
        value: 123123,
      },
      {
        day: '土',
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

  // データ取得
  useEffect(() => {
    const fetchAnalyticsData = async () => {
      try {
        open('loading');

        // ステータス
        const statusResponse = await axiosInstance.get('/admin/main/status');
        // 前日情報
        const previousResponse = await axiosInstance.get('/admin/main/previous');
        // 週間売上
        const weeklySalesResponse = await axiosInstance.get('/admin/main/weekly');
        // 日別訪問者
        const weeklyResponse = await axiosInstance.get('/admin/main/weekly-visitors');
        // 月別訪問者
        const monthlyResponse = await axiosInstance.get('/admin/main/monthly-visitors');

        const dayMapping = {
          Sun: '日',
          Mon: '月',
          Tue: '火',
          Wed: '水',
          Thu: '木',
          Fri: '金',
          Sat: '土',
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
        open('error', 'データの読み込みに失敗しました');
      }
    };

    fetchAnalyticsData();
  }, []);

  // 週間売上チャート設定（縦棒グラフ）
  const weeklyOrdersChartData = useMemo(() => {
    return {
      labels: dashboardData.weeklyOrders.map((item) => item.day),
      datasets: [
        {
          label: '週間売上状況',
          data: dashboardData.weeklyOrders.map((item) => item.value),
          backgroundColor: 'rgba(99, 102, 241, 0.7)',
          borderColor: 'rgba(99, 102, 241, 1)',
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    };
  }, [dashboardData.weeklyOrders]);

  // 日別訪問者チャート設定
  const weeklyVisitorsChartData = useMemo(() => {
    const days = ['日', '月', '火', '水', '木', '金', '土'];

    return {
      labels: days,
      datasets: [
        {
          label: 'PV（ページビュー）',
          data: dashboardData.weeklyVisitors.pv,
          borderColor: 'rgba(99, 102, 241, 1)',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
        },
        {
          label: 'UV（ユニーク訪問者）',
          data: dashboardData.weeklyVisitors.uv,
          borderColor: 'rgba(16, 185, 129, 1)',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.4,
        },
      ],
    };
  }, [dashboardData.weeklyVisitors]);

  // 月別訪問者チャート設定
  const monthlyVisitorsChartData = useMemo(() => {
    const months = [
      '1月',
      '2月',
      '3月',
      '4月',
      '5月',
      '6月',
      '7月',
      '8月',
      '9月',
      '10月',
      '11月',
      '12月',
    ];

    return {
      labels: months,
      datasets: [
        {
          label: 'PV（ページビュー）',
          data: dashboardData.monthlyVisitors.pv,
          borderColor: 'rgba(99, 102, 241, 1)',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
        },
        {
          label: 'UV（ユニーク訪問者）',
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
            return `売上: ${context.parsed.y.toLocaleString()}円`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function (value) {
            return value.toLocaleString() + '円';
          },
        },
      },
    },
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h2 className="border-bottom pb-2 mb-4">管理ページ</h2>

        {/* ステータス管理 */}
        <div className="card mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">ステータス管理</h6>
            <div className="row g-3">
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">新規注文</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.newOrder}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">入金待ち</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.paymentPending}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">購入確定</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.purchaseConfirmed}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">予約確定</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.reserved}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">交換申請</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.exchangeRequested}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">交換対応中</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.exchangeInProgress}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">交換完了</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.exchangeCompleted}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">返金申請</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.refundRequested}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">返金対応中</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.refundInProgress}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">返金完了</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.refundCompleted}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">発送準備中</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.preparingShipment}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">配送中</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.inTransit}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">配送完了</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.delivered}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">キャンセル確定</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.cancelRequested}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">回答待ち</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.answerPending}
                  </span>
                </div>
              </div>
              <div className="col-md-3 col-6">
                <div className="d-flex align-items-center">
                  <span className="me-2">回答完了</span>
                  <span className="badge bg-dark rounded-pill">
                    {dashboardData.statusManagement.answerCompleted}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 売上情報および週間チャート */}
        <div className="row mb-4">
          {/* 売上情報 */}
          <div className="col-lg-5">
            <div className="card h-100">
              <div className="card-body">
                <h6 className="card-title mb-3">売上情報</h6>
                <div className="mb-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-muted">昨日の純売上状況</span>
                  </div>
                  <h3 className="text-primary mb-0">
                    {dashboardData.salesInfo.previousNetSales} 円
                  </h3>
                </div>

                <table className="table table-sm table-borderless">
                  <tbody>
                    <tr>
                      <td className="text-muted">売上金額</td>
                      <td className="text-end">{dashboardData.salesInfo.settlementAmount} 円</td>
                      <td className="text-muted">返金金額</td>
                      <td className="text-end">{dashboardData.salesInfo.refundAmount} 円</td>
                    </tr>
                    <tr>
                      <td className="text-muted">注文件数</td>
                      <td className="text-end">{dashboardData.salesInfo.orderCount} 件</td>
                      <td className="text-muted">返金/返品件数</td>
                      <td className="text-end">{dashboardData.salesInfo.refundCount} 件</td>
                    </tr>
                    <tr>
                      <td className="text-muted">発送処理件数</td>
                      <td className="text-end">
                        {dashboardData.salesInfo.shipmentProcessedCount} 件
                      </td>
                      <td className="text-muted">配送完了件数</td>
                      <td className="text-end">{dashboardData.salesInfo.deliveredCount} 件</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* 週間売上状況 */}
          <div className="col-lg-7">
            <div className="card h-100">
              <div className="card-body">
                <h6 className="card-title mb-3">週間売上状況</h6>
                <div style={{ height: '250px' }}>
                  <Bar data={weeklyOrdersChartData} options={barChartOptions} />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 訪問者数 */}
        <div className="card mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">訪問者数</h6>
            {isDataLoaded ? (
              <div className="row">
                <div className="col-md-6 mb-3 mb-md-0">
                  <h6 className="text-center mb-3">日別訪問者数</h6>
                  <div style={{ height: '300px' }}>
                    <Line data={weeklyVisitorsChartData} options={lineChartOptions} />
                  </div>
                </div>
                <div className="col-md-6">
                  <h6 className="text-center mb-3">累計訪問者数</h6>
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

        {/* モーダルコンポーネント */}
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      </div>
    </div>
  );
}
