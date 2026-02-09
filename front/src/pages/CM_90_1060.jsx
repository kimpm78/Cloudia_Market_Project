import { useEffect, useState, useCallback, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import CM_90_1011_grid from '../components/CM_90_1000_grid.jsx';
import axiosInstance from '../services/axiosInstance.js';
import CM_99_1001 from '../components/commonPopup/CM_99_1001.jsx';
import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';
import CMMessage from '../constants/CMMessage';
import { CATEGORIES_2, SEARCH_TYPE_2, ACTION_TYPE } from '../constants/constants.js';

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
    confirm: false,
    loading: false,
    error: false,
    info: false,
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

export default function CM_90_1060() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);

  // 상태 관리
  const [searchType, setSearchType] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRows, setSelectedRows] = useState([]);
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);

  // 컬럼 정의
  const columnDefs = useMemo(
    () => [
      {
        headerName: '상품 코드',
        field: 'productCode',
        minWidth: 100,
        maxWidth: 120,
        flex: 1,
      },
      {
        headerName: '상품명',
        field: 'name',
        minWidth: 500,
        maxWidth: 500,
        flex: 1,
      },
      {
        headerName: '카테고리',
        field: 'categoryGroupName',
        minWidth: 200,
        flex: 2,
      },
      {
        headerName: '예약 마감월',
        field: 'reservationDeadline',
        minWidth: 100,
        flex: 2,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '판매가',
        field: 'price',
        minWidth: 80,
        maxWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}원` : '';
        },
      },
      {
        headerName: '상태',
        field: 'codeValue',
        minWidth: 100,
        flex: 1,
        cellRenderer: (params) => {
          const category = CATEGORIES_2.find((cat) => cat.value === params.value);
          const label = category?.label || params.value;
          return <span>{label}</span>;
        },
      },
      {
        headerName: '재고',
        field: 'availableQty',
        minWidth: 80,
        maxWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? params.value.toLocaleString() : '0';
        },
      },
      {
        headerName: '최초 등록일',
        field: 'createdAt',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '재고 등록자',
        field: 'createdBy',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
      },
      {
        headerName: '수정일',
        field: 'updatedAt',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '수정자',
        field: 'updatedBy',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
      },
    ],
    []
  );

  // 모든 상품 조회
  const fetchAllProducts = useCallback(async () => {
    setLoading(true);
    const result = await apiHandler(() => axiosInstance.get('/admin/product/findAll'));
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
    setLoading(false);
  }, [apiHandler, navigate]);

  // 상품 검색
  const handleSearch = useCallback(async () => {
    const trimmedSearchTerm = searchTerm.trim();

    if (!trimmedSearchTerm) {
      fetchAllProducts();
      return;
    }

    setLoading(true);
    const result = await apiHandler(() =>
      axiosInstance.get('/admin/product/findProduct', {
        params: { searchType: searchType, searchTerm: trimmedSearchTerm },
      })
    );
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
    setLoading(false);
  }, [searchType, searchTerm, apiHandler, navigate, fetchAllProducts]);

  // 상품 삭제
  const handleDelete = useCallback(async () => {
    close('confirm');
    setLoading(true);

    const productIds = selectedRows.map((item) => item.productId);
    const result = await apiHandler(() =>
      axiosInstance.delete('/admin/product/del', { data: productIds })
    );

    if (result.data.result) {
      fetchAllProducts();
      setSelectedRows([]);
      open('info', CMMessage.MSG_INF_005);
    }
    setLoading(false);
  }, [selectedRows, apiHandler, navigate, fetchAllProducts, open, close]);

  // 상품 수정
  const handleEdit = useCallback(() => {
    const productId = selectedRows[0]?.productId;

    if (!productId) {
      open('info', CMMessage.MSG_INF_003);
      return;
    }

    navigate(`/admin/products/edit/${productId}`);
  }, [selectedRows, navigate, open]);

  // 액션 처리
  const handleAction = useCallback(
    (actionType) => {
      if (selectedRows.length === 0) {
        open('info', CMMessage.MSG_INF_003);
        return;
      }

      if (selectedRows.length > 1 && actionType === ACTION_TYPE.EDIT) {
        open('info', CMMessage.MSG_INF_004);
        return;
      }

      if (actionType === ACTION_TYPE.EDIT) {
        handleEdit();
      } else if (actionType === ACTION_TYPE.DELETE) {
        open('confirm', CMMessage.MSG_CON_002(`${selectedRows.length}`));
      }
    },
    [selectedRows, open, handleEdit]
  );

  // 키보드 이벤트 처리
  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter' && !loading) {
        handleSearch();
      }
    },
    [handleSearch, loading]
  );

  // 그리드 선택 변경 처리
  const onSelectionChanged = useCallback((e) => {
    if (e.api) {
      const selectedNodes = e.api.getSelectedNodes();
      const selectedData = selectedNodes.map((node) => node.data);
      setSelectedRows(selectedData);
    }
  }, []);

  // 초기 데이터 로드
  useEffect(() => {
    fetchAllProducts();
  }, [fetchAllProducts]);

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">상품 목록</h5>

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
                    {SEARCH_TYPE_2.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="상품코드 / 상품명을 입력하세요"
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

              <div className="ms-auto d-flex align-items-center">
                <button
                  className="btn btn-primary me-2 btn-min-width"
                  onClick={() => handleAction(ACTION_TYPE.DELETE)}
                  disabled={loading || selectedRows.length === 0}
                >
                  삭제
                </button>
                <button
                  className="btn btn-primary me-2 btn-min-width"
                  onClick={() => handleAction(ACTION_TYPE.EDIT)}
                  disabled={loading || selectedRows.length === 0}
                >
                  수정
                </button>
                <Link to="/admin/products/register" className="btn btn-primary btn-min-width">
                  등록
                </Link>
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
          rowSelection="multiRow"
          onSelectionChanged={onSelectionChanged}
        />

        {/* 모달 컴포넌트 */}
        <CM_99_1001
          isOpen={modals.confirm}
          onClose={() => close('confirm')}
          onConfirm={handleDelete}
          Message={message}
        />
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
        <CM_99_1004 isOpen={modals.info} onClose={() => close('info')} Message={message} />
      </div>
    </div>
  );
}
