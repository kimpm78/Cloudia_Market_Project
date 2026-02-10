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

  // 初期月設定
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

  // データ取得関数
  const fetchData = useCallback(async () => {
    if (!startMonth || !endMonth) {
      open('error', '開始月と終了月を両方選択してください。');
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
      open('error', 'データの読み込みに失敗しました。');
    }
  }, [startMonth, endMonth, open, close]);

  // 初期ロード時に一度だけデータ取得
  useEffect(() => {
    if (startMonth && endMonth) {
      fetchData();
    }
  }, []);

  // 1つ目のチャートデータ
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

  // 2つ目のチャートデータ
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
          text: '金額（円）',
        },
        grid: { display: true, color: 'rgba(0, 0, 0, 0.05)' },
      },
      x: {
        title: {
          display: true,
          text: '月',
        },
        grid: { display: false },
      },
    },
  };

  return (
    <div className="d-flex flex-grow-1 bg-light">
      <div className="content-wrapper p-4 w-100">
        <h2 className="border-bottom pb-2 mb-4">売上情報（チャート）</h2>

        {/* 月範囲選択 */}
        <div className="row mb-4 align-items-center">
          <div className="col-md-6">
            <div className="d-flex align-items-end gap-2">
              <div style={{ width: '180px' }}>
                <label className="form-label small mb-1">開始月</label>
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
                <label className="form-label small mb-1">終了月</label>
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
                  検索
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 1つ目の棒グラフ */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">売上</h6>
            <div style={{ height: '400px' }}>
              <Bar data={chart1Data} options={barChartOptions} />
            </div>
          </div>
        </div>

        {/* 2つ目の棒グラフ */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h6 className="card-title mb-3">純利益</h6>
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
