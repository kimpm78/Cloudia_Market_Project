import { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import axiosInstance from '../services/axiosInstance';
import CMMessage from '../constants/CMMessage';

const CATEGORIES = [
  { value: 1, label: '활성화' },
  { value: 2, label: '비활성화' },
  { value: 3, label: '탈퇴' },
];

const AUTHORITY_OPTIONS = [
  { value: 1, label: 'Admin' },
  { value: 2, label: 'Manager' },
  { value: 3, label: 'User' },
];

const SEARCH_TYPE = [
  { value: 1, label: '회원 번호' },
  { value: 2, label: 'ID' },
];

// 유틸리티
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';

  return date.toISOString().slice(0, 10);
};

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

export default function CM_90_1020() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);

  const [searchType, setSearchType] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);

  // 컬럼 정의
  const columnDefs = useMemo(
    () => [
      {
        headerName: '회원 번호',
        field: 'memberNumber',
        minWidth: 150,
        flex: 1,
      },
      {
        headerName: '이름',
        field: 'name',
        minWidth: 200,
        flex: 1,
      },
      {
        headerName: 'ID',
        field: 'loginId',
        minWidth: 200,
        flex: 1,
      },
      {
        headerName: '이메일',
        field: 'email',
        minWidth: 300,
        flex: 2,
      },
      {
        headerName: '회원 등급',
        field: 'roleId',
        minWidth: 130,
        flex: 1,
        cellRenderer: (params) => {
          const category = AUTHORITY_OPTIONS.find((cat) => cat.value === params.value);
          const label = category?.label || params.value;
          return <span>{label}</span>;
        },
      },
      {
        headerName: '가입일',
        field: 'createdAt',
        minWidth: 130,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '상태',
        field: 'userStatusValue',
        minWidth: 130,
        flex: 1,
        cellRenderer: (params) => {
          const category = CATEGORIES.find((cat) => cat.value === params.value);
          const label = category?.label || params.value;
          return <span>{label}</span>;
        },
      },
      {
        headerName: '비고',
        field: 'note',
        minWidth: 300,
        flex: 1,
      },
      {
        headerName: '설정',
        field: 'icon',
        minWidth: 180,
        flex: 1,
        cellRenderer: (params) => {
          const handleClick = () => {
            var memberId = params.data.memberNumber;
            navigate(`/admin/users/${memberId}`);
          };
          return (
            <i className="bi bi-gear fs-5" style={{ cursor: 'pointer' }} onClick={handleClick} />
          );
        },
      },
    ],
    []
  );

  // 모든 유저 조회
  const fetchAllUsers = useCallback(async () => {
    setLoading(true);
    const result = await apiHandler(() => axiosInstance.get('/admin/users/findAll'));
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
    setLoading(false);
  }, [apiHandler, navigate]);

  // 유저 검색
  const handleSearch = useCallback(async () => {
    const trimmedSearchTerm = searchTerm.trim();

    if (!trimmedSearchTerm) {
      fetchAllUsers();
      return;
    }

    setLoading(true);
    const result = await apiHandler(() =>
      axiosInstance.get('/admin/users/findUsers', {
        params: { searchType: searchType, searchTerm: trimmedSearchTerm },
      })
    );
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
    setLoading(false);
  }, [searchType, searchTerm, apiHandler, navigate, fetchAllUsers]);

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter' && !loading) {
        handleSearch();
      }
    },
    [handleSearch, loading]
  );

  // 초기 데이터 로드
  useEffect(() => {
    fetchAllUsers();
  }, [fetchAllUsers]);

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">유저 목록</h5>
        {/* 검색 및 액션 버튼 */}
        <div className="row mb-3">
          <div className="col-12">
            <div className="d-flex align-items-center w-100">
              <div className="d-flex align-items-center">
                <div className="input-group input-group-custom">
                  <select
                    className="form-select select-custom"
                    value={searchType}
                    onChange={(e) => setSearchType(Number(e.target.value))}
                    disabled={loading}
                  >
                    {SEARCH_TYPE.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="회원번호 / ID을 입력하세요"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={handleKeyDown}
                    disabled={loading}
                  />
                </div>
                <button
                  className="btn btn-primary ms-2 btn-min-width"
                  onClick={handleSearch}
                  disabled={loading}
                >
                  검색
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 데이터 그리드 */}
        <CM_90_1011_grid
          itemsPerPage={10}
          pagesPerGroup={5}
          rowData={rowData}
          columnDefs={columnDefs}
        />

        {/* 모달 컴포넌트 */}
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
      </div>
    </div>
  );
}
