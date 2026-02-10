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

// ユーティリティ
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';

  return date.toISOString().slice(0, 10);
};

// モーダルおよびメッセージ
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

export default function CM_90_1060() {
  const navigate = useNavigate();
  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(navigate, open, close);

  // 状態管理
  const [searchType, setSearchType] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRows, setSelectedRows] = useState([]);
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);

  // カラム定義
  const columnDefs = useMemo(
    () => [
      {
        headerName: '商品コード',
        field: 'productCode',
        minWidth: 100,
        maxWidth: 120,
        flex: 1,
      },
      {
        headerName: '商品名',
        field: 'name',
        minWidth: 500,
        maxWidth: 500,
        flex: 1,
      },
      {
        headerName: 'カテゴリ',
        field: 'categoryGroupName',
        minWidth: 200,
        flex: 2,
      },
      {
        headerName: '予約締切月',
        field: 'reservationDeadline',
        minWidth: 100,
        flex: 2,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '販売価格',
        field: 'price',
        minWidth: 80,
        maxWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? `${params.value.toLocaleString()}円` : '';
        },
      },
      {
        headerName: '区分',
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
        headerName: '在庫',
        field: 'availableQty',
        minWidth: 80,
        maxWidth: 100,
        flex: 1,
        valueFormatter: (params) => {
          return params.value ? params.value.toLocaleString() : '0';
        },
      },
      {
        headerName: '最初登録日',
        field: 'createdAt',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '在庫登録者',
        field: 'createdBy',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
      },
      {
        headerName: '修正日',
        field: 'updatedAt',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
        valueFormatter: (params) => formatDate(params.value),
      },
      {
        headerName: '更新者',
        field: 'updatedBy',
        minWidth: 120,
        maxWidth: 180,
        flex: 1,
      },
    ],
    []
  );

  // 全商品取得
  const fetchAllProducts = useCallback(async () => {
    setLoading(true);
    const result = await apiHandler(() => axiosInstance.get('/admin/product/findAll'));
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
    setLoading(false);
  }, [apiHandler, navigate]);

  // 商品検索
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

  // 商品削除
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

  // 商品修正
  const handleEdit = useCallback(() => {
    const productId = selectedRows[0]?.productId;

    if (!productId) {
      open('info', CMMessage.MSG_INF_003);
      return;
    }

    navigate(`/admin/products/edit/${productId}`);
  }, [selectedRows, navigate, open]);

  // アクション処理
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

  // キーボードイベント処理
  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter' && !loading) {
        handleSearch();
      }
    },
    [handleSearch, loading]
  );

  // グリッド選択変更処理
  const onSelectionChanged = useCallback((e) => {
    if (e.api) {
      const selectedNodes = e.api.getSelectedNodes();
      const selectedData = selectedNodes.map((node) => node.data);
      setSelectedRows(selectedData);
    }
  }, []);

  // 初期データロード
  useEffect(() => {
    fetchAllProducts();
  }, [fetchAllProducts]);

  return (
    <div className="d-flex">
      <div className="content-wrapper p-3">
        <h2 className="border-bottom pb-2 mb-3">商品一覧</h2>

        {/* 検索およびアクションボタン */}
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
                    placeholder="商品コード／商品名を入力してください"
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
                  検索
                </button>
              </div>

              <div className="ms-auto d-flex align-items-center">
                <button
                  className="btn btn-primary me-2 btn-min-width"
                  onClick={() => handleAction(ACTION_TYPE.DELETE)}
                  disabled={loading || selectedRows.length === 0}
                >
                  削除
                </button>
                <button
                  className="btn btn-primary me-2 btn-min-width"
                  onClick={() => handleAction(ACTION_TYPE.EDIT)}
                  disabled={loading || selectedRows.length === 0}
                >
                  修正
                </button>
                <Link to="/admin/products/register" className="btn btn-primary btn-min-width">
                  登録
                </Link>
              </div>
            </div>
          </div>
        </div>

        {/* データグリッド */}
        <CM_90_1011_grid
          itemsPerPage={10}
          pagesPerGroup={5}
          rowData={rowData}
          columnDefs={columnDefs}
          rowSelection="multiRow"
          onSelectionChanged={onSelectionChanged}
        />

        {/* モーダルコンポーネント */}
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
