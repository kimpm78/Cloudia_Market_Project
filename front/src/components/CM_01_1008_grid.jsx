import { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import { ModuleRegistry, RowAutoHeightModule } from 'ag-grid-community';
import axiosInstance from '../services/axiosInstance';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-balham.css';
import CM_99_1001 from './commonPopup/CM_99_1001';

ModuleRegistry.registerModules([RowAutoHeightModule]);

const NicknameCellRenderer = (params) => <div className="cell-content fw-bold">{params.value}</div>;

const AddressCellRenderer = (params) => {
  const { data, onSetDefault } = params;
  const addressParts = [
    data.addressMain,
    data.addressDetail1,
    data.addressDetail2,
    data.addressDetail3,
  ];
  const fullAddress = addressParts.filter((part) => part).join(' ');

  return (
    <div className="cell-content" style={{ lineHeight: '1.5' }}>
      <span>[{data.postalCode}]</span>
      <span>{fullAddress}</span>
      <span className="text-muted small">TEL: {data.recipientPhone}</span>

      <div className="mt-1">
        {data.isDefault ? (
          <button className="badge bg-primary text-white ms-2" disabled>
            <i className="bi bi-check-circle me-1"></i>デフォルト配送先
          </button>
        ) : (
          <button
            className="btn btn-sm btn-outline-secondary ms-2"
            style={{ fontSize: '0.6rem' }}
            onClick={() => onSetDefault(data.addressId)}
          >
            デフォルトに設定
          </button>
        )}
      </div>
    </div>
  );
};

const ActionsCellRenderer = (params) => {
  const isDefault = params.data.isDefault;

  return (
    <div className="cell-content actions">
      <button className="btn btn-primary btn-sm me-1" onClick={() => params.onEdit(params.data)}>
        編集
      </button>
      <button
        className={`btn btn-sm ${isDefault ? 'btn-outline-secondary' : 'btn-secondary'}`}
        onClick={() => params.onDelete(params.data.addressId)}
        disabled={isDefault}
        title={isDefault ? 'デフォルト配送先は削除できません。' : '削除'}
      >
        削除
      </button>
    </div>
  );
};

export default function CM_01_1008_Grid() {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [deletingAddressId, setDeletingAddressId] = useState(null);
  const navigate = useNavigate();

  const fetchAddresses = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axiosInstance.get('/user/addresses');
      setAddresses(response.data);
    } catch (error) {
      console.error('住所録の読み込みに失敗しました:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAddresses();
  }, [fetchAddresses]);

  // [追加] デフォルト配送先設定ハンドラー
  const handleSetDefaultAddress = useCallback(
    async (addressId) => {
      try {
        await axiosInstance.patch(`/user/addresses/${addressId}/default`);
        fetchAddresses();
      } catch (error) {
        console.error('デフォルト配送先の設定に失敗しました:', error);
        alert('デフォルト配送先の設定に失敗しました。');
      }
    },
    [fetchAddresses]
  );

  const handleGoToEditAddressPage = useCallback(
    (address) => {
      navigate(`?mode=edit&id=${address.addressId}`);
    },
    [navigate]
  );

  const handleGoToNewAddressPage = useCallback(() => {
    navigate('?mode=new');
  }, [navigate]);

  const handleOpenDeleteConfirm = useCallback((id) => {
    setDeletingAddressId(id);
    setIsConfirmModalOpen(true);
  }, []);

  const handleDeleteAddress = useCallback(async () => {
    if (!deletingAddressId) return;
    try {
      await axiosInstance.delete(`/user/addresses/${deletingAddressId}`);
      setIsConfirmModalOpen(false);
      setDeletingAddressId(null);
      fetchAddresses();
    } catch (error) {
      console.error('住所の削除に失敗しました。', error);
      alert('削除に失敗しました。');
    }
  }, [deletingAddressId, fetchAddresses]);

  const columnDefs = useMemo(
    () => [
      {
        headerName: '配送先名',
        field: 'addressNickname',
        flex: 1,
        cellRenderer: NicknameCellRenderer,
        cellStyle: { display: 'flex', alignItems: 'center' },
      },
      {
        headerName: '配送先住所',
        field: 'addressMain',
        flex: 3,
        cellRenderer: AddressCellRenderer,
        cellRendererParams: {
          onSetDefault: handleSetDefaultAddress,
        },
        cellStyle: { whiteSpace: 'normal', padding: '10px' },
      },
      {
        headerName: '管理',
        field: 'actions',
        width: 150,
        cellRenderer: ActionsCellRenderer,
        cellRendererParams: {
          onEdit: handleGoToEditAddressPage,
          onDelete: handleOpenDeleteConfirm,
        },
        cellStyle: { display: 'flex', alignItems: 'center', justifyContent: 'center' },
      },
    ],
    [handleGoToEditAddressPage, handleOpenDeleteConfirm, handleSetDefaultAddress]
  );

  const defaultColDef = useMemo(
    () => ({
      sortable: false,
      resizable: false,
      suppressMovable: true,
    }),
    []
  );

  if (loading) return <p className="text-center p-5">読み込み中...</p>;

  return (
    <div>
      <h5 className="fw-bold border-bottom pb-2 mb-3">配送情報</h5>
      <div className="ag-theme-balham address-grid-cards">
        <AgGridReact
          theme="legacy"
          rowData={addresses}
          columnDefs={columnDefs}
          defaultColDef={defaultColDef}
          domLayout="autoHeight"
          headerHeight={40}
          rowHeight={132}
          rowClass="address-row-card"
          overlayNoRowsTemplate={
            '<div class="no-rows-overlay p-4">登録された配送先情報がありません。<br/>下のボタンから新しい住所を登録してください。</div>'
          }
        />
      </div>
      <div className="d-flex justify-content-center mt-4 mb-5">
        <button
          className="btn btn-primary btn-lg px-5"
          onClick={handleGoToNewAddressPage}
          disabled={addresses.length >= 3}
        >
          新しい住所を登録
        </button>
      </div>
      <CM_99_1001
        isOpen={isConfirmModalOpen}
        onClose={() => setIsConfirmModalOpen(false)}
        onConfirm={handleDeleteAddress}
        Message="この住所を削除してもよろしいですか？"
      />
    </div>
  );
}
