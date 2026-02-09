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

// API 호출
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

  // 초기 데이터 로드
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
    { field: 'ageGroup', headerName: '연령대', flex: 1 },
    {
      field: 'userCount',
      headerName: '가입자',
      flex: 1,
      valueFormatter: (params) => formatNumber(params.value),
    },
    {
      field: 'avgAmountPerUser',
      headerName: '평균 단가 (1년 단위)',
      flex: 2,
      valueFormatter: (params) => formatNumber(params.value),
    },
  ];

  const genderColumnDefs = [
    { field: 'genderGroup', headerName: '성별', flex: 1 },
    {
      field: 'userCount',
      headerName: '가입자',
      flex: 1,
      valueFormatter: (params) => formatNumber(params.value),
    },
    {
      field: 'avgAmountPerUser',
      headerName: '평균 단가 (분기 단위)',
      flex: 2,
      valueFormatter: (params) => formatNumber(params.value),
    },
  ];

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-4">성별/연령대 분석</h5>

        {/* 주요 측정항목 */}
        <h6 className="mb-3" style={{ fontWeight: 'bold' }}>
          주요 측정항목
        </h6>

        {/* 차트 섹션 */}
        <div className="row mb-5">
          {/* 연령대 차트 */}
          <div className="col-md-6">
            <div
              className="card h-100"
              style={{ border: '1px solid #e0e0e0', borderRadius: '8px' }}
            >
              <div className="card-header bg-white" style={{ borderBottom: '1px solid #e0e0e0' }}>
                <h6 className="mb-0" style={{ color: '#007bff', fontWeight: 'bold' }}>
                  연령대
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

          {/* 성별 차트 */}
          <div className="col-md-6">
            <div
              className="card h-100"
              style={{ border: '1px solid #e0e0e0', borderRadius: '8px' }}
            >
              <div className="card-header bg-white" style={{ borderBottom: '1px solid #e0e0e0' }}>
                <h6 className="mb-0" style={{ color: '#007bff', fontWeight: 'bold' }}>
                  성별
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

        {/* 테이블 섹션 */}
        <h6 className="mb-3" style={{ fontWeight: 'bold' }}>
          성별 / 연령대
        </h6>

        <div className="row">
          {/* 연령대 AG Grid */}
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

          {/* 성별 AG Grid */}
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
