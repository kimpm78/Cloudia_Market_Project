import { useEffect, useState, useCallback } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, Title } from 'chart.js';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import { Doughnut } from 'react-chartjs-2';
import { useNavigate } from 'react-router-dom';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import axiosInstance from '../services/axiosInstance';
import CMMessage from '../constants/CMMessage';

ChartJS.register(ArcElement, Tooltip, Legend, Title, ChartDataLabels);

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

// API呼び出し
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

export default function CM_90_1031() {
  const [ageDatas, setAgeDatas] = useState([]);
  const [genderDatas, setGenderDatas] = useState([]);
  const { modals, message, open, close } = useModal();
  const navigate = useNavigate();
  const apiHandler = useApiHandler(navigate, open, close);
  const [loading, setLoading] = useState(false);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    let resul = await apiHandler(() => axiosInstance.get('/admin/visits/findAllAges'));
    if (resul.data.result) {
      setAgeDatas(resul.data.resultList);
    }
    resul = await apiHandler(() => axiosInstance.get('/admin/visits/findAllGenders'));
    if (resul.data.result) {
      setGenderDatas(resul.data.resultList);
    }
    setLoading(false);
  }, [apiHandler, navigate]);

  // 初期データロード
  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const ageData = {
    labels: ageDatas.map((item) => item.ageGroup),
    datasets: [
      {
        data: ageDatas.map((item) => item.userCount),
        backgroundColor: ['#87CEEB', '#FFD700', '#90EE90', '#DDA0DD'],
        borderWidth: 0,
        cutout: '50%',
      },
    ],
  };

  const genderData = {
    labels: genderDatas.map((item) => item.genderGroup),
    datasets: [
      {
        data: genderDatas.map((item) => item.userCount),
        backgroundColor: ['#87CEEB', '#FFB6C1', '#DDA0DD'],
        borderWidth: 0,
        cutout: '50%',
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          padding: 20,
          usePointStyle: true,
          boxWidth: 10,
          font: { size: 12 },
        },
      },
      tooltip: {
        callbacks: {
          label: function (context) {
            const value = context.parsed;
            const total = context.dataset.data.reduce((a, b) => a + b, 0);
            const percentage = Math.round((value / total) * 100);
            return `${context.label}: ${percentage}%`;
          },
        },
      },
      datalabels: {
        color: '#000',
        font: { size: 14 },
        formatter: function (value, context) {
          const total = context.dataset.data.reduce((a, b) => a + b, 0);
          const percentage = Math.round((value / total) * 100);
          return percentage > 0 ? `${percentage}%` : '';
        },
        anchor: 'center',
        align: 'center',
      },
    },
    layout: { padding: { top: 20, bottom: 20 } },
  };

  const formatNumber = (value) => {
    if (value == null || isNaN(value)) return '0';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  };

  const ageColumnDefs = [
    { field: 'ageGroup', headerName: '年齢層', flex: 1 },
    {
      field: 'userCount',
      headerName: '登録者',
      flex: 1,
      valueFormatter: (params) => formatNumber(params.value),
    },
    {
      field: 'avgAmountPerUser',
      headerName: '平均単価（年単位）',
      flex: 2,
      valueFormatter: (params) => formatNumber(params.value),
    },
  ];

  const genderColumnDefs = [
    { field: 'genderGroup', headerName: '性別', flex: 1 },
    {
      field: 'userCount',
      headerName: '登録者',
      flex: 1,
      valueFormatter: (params) => formatNumber(params.value),
    },
    {
      field: 'avgAmountPerUser',
      headerName: '平均単価（四半期単位）',
      flex: 2,
      valueFormatter: (params) => formatNumber(params.value),
    },
  ];

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h2 className="border-bottom pb-2 mb-4">性別 / 年齢層分析</h2>

        {/* 主要指標 */}
        <h6 className="mb-3 fw-semibold">
          主要指標
        </h6>

        {/* チャートセクション */}
        <div className="row mb-5">
          {/* 年齢層チャート */}
          <div className="col-md-6">
            <div
              className="card h-100"
              style={{ border: '1px solid #e0e0e0', borderRadius: '8px' }}
            >
              <div className="card-header bg-white" style={{ borderBottom: '1px solid #e0e0e0' }}>
                <h6 className="mb-0" style={{ color: '#007bff', fontWeight: 'bold' }}>
                  年齢層
                </h6>
              </div>
              <div
                className="card-body d-flex align-items-center justify-content-center"
                style={{ height: '350px' }}
              >
                <div style={{ width: '100%', height: '350px', position: 'relative' }}>
                  <Doughnut data={ageData} options={chartOptions} />
                </div>
              </div>
            </div>
          </div>

          {/* 性別チャート */}
          <div className="col-md-6">
            <div
              className="card h-100"
              style={{ border: '1px solid #e0e0e0', borderRadius: '8px' }}
            >
              <div className="card-header bg-white" style={{ borderBottom: '1px solid #e0e0e0' }}>
                <h6 className="mb-0" style={{ color: '#007bff', fontWeight: 'bold' }}>
                  性別
                </h6>
              </div>
              <div
                className="card-body d-flex align-items-center justify-content-center"
                style={{ height: '350px' }}
              >
                <div style={{ width: '100%', height: '350px', position: 'relative' }}>
                  <Doughnut data={genderData} options={chartOptions} />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* テーブルセクション */}
        <h6 className="mb-3" style={{ fontWeight: 'bold' }}>
          性別／年齢層
        </h6>

        <div className="row">
          {/* 年齢層 AG Grid */}
          <div className="col-md-6 mb-4">
            <div className="ag-theme-alpine custom-grid w-100">
              <AgGridReact
                rowData={ageDatas}
                columnDefs={ageColumnDefs}
                pagination={false}
                defaultColDef={{ resizable: false }}
                theme="legacy"
                domLayout="autoHeight"
              />
            </div>
          </div>

          {/* 性別 AG Grid */}
          <div className="col-md-6 mb-4">
            <div className="ag-theme-alpine custom-grid w-100">
              <AgGridReact
                rowData={genderDatas}
                columnDefs={genderColumnDefs}
                pagination={false}
                defaultColDef={{ resizable: false }}
                theme="legacy"
                domLayout="autoHeight"
              />
            </div>
          </div>
        </div>
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
      </div>
    </div>
  );
}
