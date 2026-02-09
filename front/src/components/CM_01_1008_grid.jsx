import { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { AgGridReact } from 'ag-grid-react';
import axiosInstance from '../services/axiosInstance';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-balham.css';
import CM_99_1001 from './commonPopup/CM_99_1001';

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
      <br />
      <span>{fullAddress}</span>
      <br />
      <span className="text-muted small">TEL: {data.recipientPhone}</span>

      <div className="mt-1">
        {data.isDefault ? (
          <button className="badge bg-primary text-white ms-2" disabled>
            <i className="bi bi-check-circle me-1"></i>기본 배송지
          </button>
        ) : (
          <button
            className="btn btn-sm btn-outline-secondary ms-2"
            style={{ fontSize: '0.6rem' }}
            onClick={() => onSetDefault(data.addressId)}
          >
            기본으로 설정
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
        편집
      </button>
      <button
        className={`btn btn-sm ${isDefault ? 'btn-outline-secondary' : 'btn-secondary'}`}
        onClick={() => params.onDelete(params.data.addressId)}
        disabled={isDefault}
        title={isDefault ? '기본 배송지는 삭제할 수 없습니다.' : '삭제'}
      >
        삭제
      </button>
    </div>
  );
};

export default function CM_01_1008_grid() {
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
      console.error('주소록 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAddresses();
  }, [fetchAddresses]);

  // [추가됨] 기본 배송지 설정 핸들러
  const handleSetDefaultAddress = useCallback(
    async (addressId) => {
      try {
        // 위에서 만든 백엔드 PATCH API 호출
        await axiosInstance.patch(`/user/addresses/${addressId}/default`);
        // 성공 시 목록 새로고침하여 UI 반영
        fetchAddresses();
      } catch (error) {
        console.error('기본 배송지 설정 실패:', error);
        alert('기본 배송지 설정에 실패했습니다.');
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
      console.error('주소 삭제에 실패했습니다.', error);
      alert('삭제에 실패했습니다.');
    }
  }, [deletingAddressId, fetchAddresses]);

  const columnDefs = useMemo(
    () => [
      {
        headerName: '배송지 명칭',
        field: 'addressNickname',
        flex: 1,
        cellRenderer: NicknameCellRenderer,
        cellStyle: { display: 'flex', alignItems: 'center' },
      },
      {
        headerName: '배송 주소',
        field: 'addressMain',
        flex: 3,
        cellRenderer: AddressCellRenderer,
        cellRendererParams: {
          onSetDefault: handleSetDefaultAddress,
        },
        autoHeight: true,
        cellStyle: { whiteSpace: 'normal', padding: '10px' },
      },
      {
        headerName: '관리',
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

  if (loading) return <p className="text-center p-5">로딩 중...</p>;

  return (
    <div>
      <h5 className="fw-bold border-bottom pb-2 mb-3">배송 정보</h5>
      <div className="ag-theme-balham address-grid-cards">
        <AgGridReact
          rowData={addresses}
          columnDefs={columnDefs}
          domLayout="autoHeight"
          headerHeight={40}
          rowHeight={100}
          rowClass="address-row-card"
          overlayNoRowsTemplate={
            '<div class="no-rows-overlay p-4">등록된 배송지 정보가 없습니다.<br/>아래 버튼을 눌러 새 주소를 등록해주세요.</div>'
          }
        />
      </div>
      <div className="d-flex justify-content-center mt-4 mb-5">
        <button
          className="btn btn-primary btn-lg px-5"
          onClick={handleGoToNewAddressPage}
          disabled={addresses.length >= 3}
        >
          새로운 주소 등록하기
        </button>
      </div>
      <CM_99_1001
        isOpen={isConfirmModalOpen}
        onClose={() => setIsConfirmModalOpen(false)}
        onConfirm={handleDeleteAddress}
        Message="정말로 이 주소를 삭제하시겠습니까?"
      />
    </div>
  );
}
