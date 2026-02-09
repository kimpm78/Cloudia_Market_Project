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
      Direct: '직접 유입',
      'Organic Search': '자연 검색',
      'Paid Search': '유료 검색',
      'Organic Social': '자연 소셜',
      'Paid Social': '유료 소셜',
      Email: '이메일',
      Referral: '추천',
      Display: '디스플레이 광고',
      Unassigned: '미분류',
    };
    return translationMap[channel] || channel;
  };

  // 채널별 색상 지정 함수
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

  // 데이터 가져오기 함수
  const fetchAnalyticsData = useCallback(async () => {
    if (!startDate || !endDate) {
      open('error', '시작일과 종료일을 모두 선택해주세요.');
      return;
    }

    try {
      open('loading');

      // 신규/재방문자
      const newVsReturningResponse = await axiosInstance.get('/admin/visits/newReturning', {
        params: { startDate, endDate },
      });

      // 기기별 세션
      const deviceResponse = await axiosInstance.get('/admin/visits/sessionsDevice', {
        params: { startDate, endDate },
      });

      // 채널별 세션
      const channelResponse = await axiosInstance.get('/admin/visits/sessionsChannel', {
        params: { startDate, endDate },
      });

      // 페이지별 평균 머문 시간
      const pageEngagementResponse = await axiosInstance.get('/admin/visits/pageEngagement', {
        params: { startDate, endDate },
      });

      // 채널 데이터를 trafficSources 형식으로 변환
      const trafficSources = channelResponse.data.resultList.map((item) => ({
        name: translateChannel(item.channel),
        value: Math.round(item.percentage),
        sessions: item.sessions,
        color: getChannelColor(item.channel),
      }));

      // 페이지별 데이터를 sessionTimeline 형식으로 변환
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
      open('error', 'Analytics 데이터를 불러오는데 실패했습니다.');
    }
  }, [startDate, endDate, open, close, translateChannel, getChannelColor]);

  const pageEngagementChartData = useMemo(
    () => ({
      labels: visitData.sessionTimeline.map((item) => item.date),
      datasets: [
        {
          label: '평균 머문 시간 (분)',
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
      labels: ['신규', '재방문'],
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
      labels: ['모바일', '데스크톱', '태블릿'],
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
    return h > 0 ? `${h}시간 ${m}분` : `${m}분`;
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
            return `평균 머문 시간: ${formatDuration(minutes)}`;
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
          text: '시간',
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
          text: '페이지',
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
        <h5 className="border-bottom pb-2 mb-4">방문 데이터</h5>

        {/* 날짜 범위 선택 */}
        <div className="row mb-4 align-items-center">
          <div className="col-md-6">
            <div className="d-flex align-items-end gap-2">
              <div style={{ width: '180px' }}>
                <label className="form-label small mb-1">시작일</label>
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
                <label className="form-label small mb-1">종료일</label>
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
                  검색
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 상단 카드: 사이트 세션 + 고유 방문자 */}
        <div className="row g-3 mb-3">
          <div className="col-md-6">
            <div className="card shadow-sm h-100 text-center">
              <div className="card-body">
                <h6 className="text-muted mb-2">사이트 세션</h6>
                <h3 className="mb-0">{visitData.totalSessions}</h3>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card shadow-sm h-100 text-center">
              <div className="card-body">
                <h6 className="text-muted mb-2">고유 방문자</h6>
                <h3 className="mb-0">{visitData.totalUsers}</h3>
              </div>
            </div>
          </div>
        </div>

        {/* 중간 카드: 세션(채널별) */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0">세션</h6>
              <select className="form-select form-select-sm w-auto">
                <option>기준: 유입 경로 카테고리</option>
              </select>
            </div>
            <div className="mt-3">
              {visitData.trafficSources.map((source, index) => (
                <div key={index} className="mb-2">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <div className="d-flex align-items-center">
                      <span className="me-2">{source.name}</span>
                    </div>
                    <span className="text-muted small">{source.sessions} 세션</span>
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

        {/* 페이지별 평균 머문 시간 차트 */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">페이지별 평균 머문 시간 (시/분)</h6>
            <div style={{ height: '400px' }}>
              <Bar data={pageEngagementChartData} options={pageEngagementBarOptions} />
            </div>
          </div>
        </div>

        {/* 신규 방문자 vs 재방문자, 기기별 */}
        <div className="row g-3 mb-4">
          <div className="col-md-6">
            <div className="card shadow-sm h-100">
              <div className="card-body d-flex flex-column">
                <h6 className="card-title mb-3">신규 방문자 vs 재방문자</h6>
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
                    <div className="fw-bold">고유 방문자</div>
                    <div className="h4 mb-0">{visitData.totalUsers}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card shadow-sm h-100">
              <div className="card-body d-flex flex-column">
                <h6 className="card-title mb-3">기기별 세션</h6>
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
                    <div className="fw-bold">사이트 세션</div>
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
