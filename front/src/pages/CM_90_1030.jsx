import { useState, useCallback, useMemo } from 'react';
import { Doughnut, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
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
  ArcElement,
  Title,
  Tooltip,
  Legend
);

const useModal = () => {
  const [modals, setModals] = useState({ loading: false, error: false });
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

export default function CM_90_1030() {
  const { modals, message, open, close } = useModal();
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [visitData, setVisitData] = useState({
    totalSessions: 0,
    totalUsers: 0,
    trafficSources: [],
    sessionTimeline: [],
    newVsReturning: { new: 0, returning: 0 },
    deviceBreakdown: { mobile: 0, desktop: 0, tablet: 0 },
  });

  const translateChannel = (channel) => {
    const translationMap = {
      Direct: '直接流入',
      'Organic Search': '自然検索',
      'Paid Search': '有料検索',
      'Organic Social': '自然ソーシャル',
      'Paid Social': '有料ソーシャル',
      Email: 'メール',
      Referral: '参照（リファラ）',
      Display: 'ディスプレイ広告',
      Unassigned: '未分類',
    };
    return translationMap[channel] || channel;
  };

  // チャネル別の色指定関数
  const getChannelColor = (channel) => {
    const colorMap = {
      'Organic Search': '#4285F4',
      'Paid Search': '#34A853',
      Direct: '#FBBC04',
      'Organic Social': '#1877F2',
      'Paid Social': '#E4405F',
      Referral: '#EA4335',
      Email: '#9333EA',
      Display: '#F59E0B',
    };
    return colorMap[channel] || '#6B7280';
  };

  // データ取得関数
  const fetchAnalyticsData = useCallback(async () => {
    if (!startDate || !endDate) {
      open('error', '開始日と終了日を両方選択してください。');
      return;
    }

    try {
      open('loading');

      // 新規/再訪問者
      const newVsReturningResponse = await axiosInstance.get('/admin/visits/newReturning', {
        params: { startDate, endDate },
      });

      // デバイス別セッション
      const deviceResponse = await axiosInstance.get('/admin/visits/sessionsDevice', {
        params: { startDate, endDate },
      });

      // チャネル別セッション
      const channelResponse = await axiosInstance.get('/admin/visits/sessionsChannel', {
        params: { startDate, endDate },
      });

      // ページ別平均滞在時間
      const pageEngagementResponse = await axiosInstance.get('/admin/visits/pageEngagement', {
        params: { startDate, endDate },
      });

      // チャネルデータをtrafficSources形式に変換
      const trafficSources = channelResponse.data.resultList.map((item) => ({
        name: translateChannel(item.channel),
        value: Math.round(item.percentage),
        sessions: item.sessions,
        color: getChannelColor(item.channel),
      }));

      // ページ別データをsessionTimeline形式に変換
      const sessionTimeline = pageEngagementResponse.data.resultList.map((item) => ({
        date: item.pageTitle,
        value: item.avgDuration,
      }));

      setVisitData((prev) => ({
        ...prev,
        newVsReturning: {
          new: newVsReturningResponse.data.resultList.new || 0,
          returning: newVsReturningResponse.data.resultList.returning || 0,
        },
        totalUsers:
          (newVsReturningResponse.data.resultList.new || 0) +
          (newVsReturningResponse.data.resultList.returning || 0),
        deviceBreakdown: {
          mobile: deviceResponse.data.resultList.mobile || 0,
          desktop: deviceResponse.data.resultList.desktop || 0,
          tablet: deviceResponse.data.resultList.tablet || 0,
        },
        totalSessions:
          (deviceResponse.data.resultList.mobile || 0) +
          (deviceResponse.data.resultList.desktop || 0) +
          (deviceResponse.data.resultList.tablet || 0),
        trafficSources: trafficSources,
        sessionTimeline: sessionTimeline,
      }));

      close('loading');
    } catch (error) {
      close('loading');
      open('error', 'Analyticsデータの読み込みに失敗しました。');
    }
  }, [startDate, endDate, open, close, translateChannel, getChannelColor]);

  const pageEngagementChartData = useMemo(
    () => ({
      labels: visitData.sessionTimeline.map((item) => item.date),
      datasets: [
        {
          label: '平均滞在時間（分）',
          data: visitData.sessionTimeline.map((item) => item.value),
          backgroundColor: 'rgba(99, 102, 241, 0.7)',
          borderColor: 'rgba(99, 102, 241, 1)',
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    }),
    [visitData.sessionTimeline]
  );

  const newVsReturningChartData = useMemo(
    () => ({
      labels: ['新規', '再訪問'],
      datasets: [
        {
          data: [visitData.newVsReturning.new, visitData.newVsReturning.returning],
          backgroundColor: ['rgba(59, 130, 246, 1)', 'rgba(234, 179, 8, 1)'],
          borderWidth: 0,
        },
      ],
    }),
    [visitData.newVsReturning]
  );

  const deviceChartData = useMemo(
    () => ({
      labels: ['モバイル', 'デスクトップ', 'タブレット'],
      datasets: [
        {
          data: [
            visitData.deviceBreakdown.mobile,
            visitData.deviceBreakdown.desktop,
            visitData.deviceBreakdown.tablet,
          ],
          backgroundColor: [
            'rgba(59, 130, 246, 1)',
            'rgba(234, 179, 8, 1)',
            'rgba(239, 68, 68, 1)',
          ],
          borderWidth: 0,
        },
      ],
    }),
    [visitData.deviceBreakdown]
  );

  const formatDuration = (minutes) => {
    const h = Math.floor(minutes / 60);
    const m = Math.round(minutes % 60);
    return h > 0 ? `${h}時間 ${m}分` : `${m}分`;
  };

  const pageEngagementBarOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: function (context) {
            const minutes = context.parsed.y;
            return `平均滞在時間: ${formatDuration(minutes)}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        reverse: false,
        title: {
          display: true,
          text: '時間',
        },
        ticks: {
          callback: function (value) {
            return formatDuration(value);
          },
        },
        grid: { display: true, color: 'rgba(0, 0, 0, 0.05)' },
      },
      x: {
        title: {
          display: true,
          text: 'ページ',
        },
        grid: { display: false },
      },
    },
  };

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
      },
    },
    cutout: '70%',
  };

  return (
    <div className="d-flex flex-grow-1 bg-light">
      <div className="content-wrapper p-4 w-100">
        <h2 className="border-bottom pb-2 mb-4">訪問データ</h2>

        {/* 日付範囲選択 */}
        <div className="row mb-4 align-items-center">
          <div className="col-md-6">
            <div className="d-flex align-items-end gap-2">
              <div style={{ width: '180px' }}>
                <label className="form-label small mb-1">開始日</label>
                <input
                  type="date"
                  className="form-control"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                />
              </div>
              <div className="mb-2">
                <span>~</span>
              </div>
              <div style={{ width: '180px' }}>
                <label className="form-label small mb-1">終了日</label>
                <input
                  type="date"
                  className="form-control"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                />
              </div>
              <div>
                <button
                  className="btn btn-primary"
                  style={{ width: '80px' }}
                  onClick={fetchAnalyticsData}
                  disabled={!startDate || !endDate}
                >
                  検索
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 上部カード：サイトセッション + ユニーク訪問者 */}
        <div className="row g-3 mb-3">
          <div className="col-md-6">
            <div className="card shadow-sm h-100 text-center">
              <div className="card-body">
                <h6 className="text-muted mb-2">サイトセッション</h6>
                <h3 className="mb-0">{visitData.totalSessions}</h3>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card shadow-sm h-100 text-center">
              <div className="card-body">
                <h6 className="text-muted mb-2">ユニーク訪問者</h6>
                <h3 className="mb-0">{visitData.totalUsers}</h3>
              </div>
            </div>
          </div>
        </div>

        {/* 中間カード：セッション（チャネル別） */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0">セッション</h6>
              <select className="form-select form-select-sm w-auto">
                <option>基準：流入経路カテゴリ</option>
              </select>
            </div>
            <div className="mt-3">
              {visitData.trafficSources.map((source, index) => (
                <div key={index} className="mb-2">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <div className="d-flex align-items-center">
                      <span className="me-2">{source.name}</span>
                    </div>
                    <span className="text-muted small">{source.sessions} セッション</span>
                  </div>
                  <div className="progress" style={{ height: '8px' }}>
                    <div
                      className="progress-bar"
                      role="progressbar"
                      style={{
                        width: `${source.value}%`,
                        backgroundColor: source.color,
                      }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* ページ別平均滞在時間チャート */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">ページ別平均滞在時間（時/分）</h6>
            <div style={{ height: '400px' }}>
              <Bar data={pageEngagementChartData} options={pageEngagementBarOptions} />
            </div>
          </div>
        </div>

        {/* 新規訪問者 vs 再訪問者、デバイス別 */}
        <div className="row g-3 mb-4">
          <div className="col-md-6">
            <div className="card shadow-sm h-100">
              <div className="card-body d-flex flex-column">
                <h6 className="card-title mb-3">新規訪問者 vs 再訪問者</h6>
                <div className="flex-grow-1 position-relative" style={{ minHeight: '300px' }}>
                  <Doughnut data={newVsReturningChartData} options={doughnutOptions} />
                  <div
                    style={{
                      position: 'absolute',
                      top: '45%',
                      left: '50%',
                      transform: 'translate(-50%, -50%)',
                      textAlign: 'center',
                      pointerEvents: 'none',
                    }}
                  >
                    <div className="fw-bold">ユニーク訪問者</div>
                    <div className="h4 mb-0">{visitData.totalUsers}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card shadow-sm h-100">
              <div className="card-body d-flex flex-column">
                <h6 className="card-title mb-3">デバイス別セッション</h6>
                <div className="flex-grow-1 position-relative" style={{ minHeight: '300px' }}>
                  <Doughnut data={deviceChartData} options={doughnutOptions} />
                  <div
                    style={{
                      position: 'absolute',
                      top: '45%',
                      left: '50%',
                      transform: 'translate(-50%, -50%)',
                      textAlign: 'center',
                      pointerEvents: 'none',
                    }}
                  >
                    <div className="fw-bold">サイトセッション</div>
                    <div className="h4 mb-0">{visitData.totalSessions}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      </div>
    </div>
  );
}
