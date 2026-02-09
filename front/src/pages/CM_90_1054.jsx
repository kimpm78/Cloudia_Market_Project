import { useEffect, useState, useCallback, useMemo } from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import axiosInstance from '../services/axiosInstance';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

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

export default function CM_90_1054() {
  const { modals, message, open, close } = useModal();
  const [startMonth, setStartMonth] = useState('');
  const [endMonth, setEndMonth] = useState('');
  const [chartData1, setChartData1] = useState([]);
  const [chartData2, setChartData2] = useState([]);

  // 초기 월 설정
  useEffect(() => {
    const now = new Date();

    const jstNow = new Date(now.getTime() + 9 * 60 * 60 * 1000);

    const end = jstNow.toISOString().slice(0, 7);

    const startDate = new Date(jstNow);
    startDate.setMonth(startDate.getMonth() - 11);
    const start = startDate.toISOString().slice(0, 7);

    setStartMonth(start);
    setEndMonth(end);
  }, []);

  // 데이터 가져오기 함수
  const fetchData = useCallback(async () => {
    if (!startMonth || !endMonth) {
      open('error', '시작 월과 종료 월을 모두 선택해주세요.');
      return;
    }

    try {
      open('loading');

      // 첫 번째 차트 데이터
      const response1 = await axiosInstance.get('/admin/settlement/month-sales/chart1', {
        params: { startMonth, endMonth },
      });

      // 두 번째 차트 데이터
      const response2 = await axiosInstance.get('/admin/settlement/month-sales/chart2', {
        params: { startMonth, endMonth },
      });

      setChartData1(
        response1.data.resultList.map((item) => ({
          label: item.month,
          value: item.totalAmount,
        }))
      );

      setChartData2(
        response2.data.resultList.map((item) => ({
          label: item.month,
          value: item.totalAmount,
        }))
      );

      close('loading');
    } catch (error) {
      close('loading');
      open('error', '데이터를 불러오는데 실패했습니다.');
    }
  }, [startMonth, endMonth, open, close]);

  // 초기 로딩 시 한번만 데이터 가져오기
  useEffect(() => {
    if (startMonth && endMonth) {
      fetchData();
    }
  }, []);

  // 첫 번째 차트 데이터
  const chart1Data = useMemo(
    () => ({
      labels: chartData1.map((item) => item.label),
      datasets: [
        {
          data: chartData1.map((item) => item.value),
          backgroundColor: 'rgba(99, 102, 241, 0.7)',
          borderColor: 'rgba(99, 102, 241, 1)',
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    }),
    [chartData1]
  );

  // 두 번째 차트 데이터
  const chart2Data = useMemo(
    () => ({
      labels: chartData2.map((item) => item.label),
      datasets: [
        {
          data: chartData2.map((item) => item.value),
          backgroundColor: 'rgba(16, 185, 129, 0.7)',
          borderColor: 'rgba(16, 185, 129, 1)',
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    }),
    [chartData2]
  );

  const barChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: function (context) {
            return `${context.dataset.label}: ${context.parsed.y.toLocaleString()}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: '금액(원)',
        },
        grid: { display: true, color: 'rgba(0, 0, 0, 0.05)' },
      },
      x: {
        title: {
          display: true,
          text: '월',
        },
        grid: { display: false },
      },
    },
  };

  return (
    <div className="d-flex flex-grow-1 bg-light">
      <div className="content-wrapper p-4 w-100">
        <h5 className="border-bottom pb-2 mb-4">매출 정보(차트)</h5>

        {/* 월 범위 선택 */}
        <div className="row mb-4 align-items-center">
          <div className="col-md-6">
            <div className="d-flex align-items-end gap-2">
              <div style={{ width: '180px' }}>
                <label className="form-label small mb-1">시작월</label>
                <input
                  type="month"
                  className="form-control"
                  value={startMonth}
                  onChange={(e) => setStartMonth(e.target.value)}
                />
              </div>
              <div className="mb-2">
                <span>~</span>
              </div>
              <div style={{ width: '180px' }}>
                <label className="form-label small mb-1">종료월</label>
                <input
                  type="month"
                  className="form-control"
                  value={endMonth}
                  onChange={(e) => setEndMonth(e.target.value)}
                />
              </div>
              <div>
                <button
                  className="btn btn-primary"
                  style={{ width: '80px' }}
                  onClick={fetchData}
                  disabled={!startMonth || !endMonth}
                >
                  검색
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 첫 번째 막대 그래프 */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">매출</h6>
            <div style={{ height: '400px' }}>
              <Bar data={chart1Data} options={barChartOptions} />
            </div>
          </div>
        </div>

        {/* 두 번째 막대 그래프 */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">순이익</h6>
            <div style={{ height: '400px' }}>
              <Bar data={chart2Data} options={barChartOptions} />
            </div>
          </div>
        </div>

        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      </div>
    </div>
  );
}
