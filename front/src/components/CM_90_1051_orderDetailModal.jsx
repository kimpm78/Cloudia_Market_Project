import { useMemo, useState, useCallback } from 'react';
import CM_90_1011_grid from './CM_90_1000_grid.jsx';
import CM_99_1003 from './commonPopup/CM_99_1003.jsx';
import { ORDER_STATUS, PAYMENT_METHODS } from '../constants/constants.js';

const getStatusBadgeClass = (value) => {
  switch (String(value)) {
    case '1':
      return 'bg-warning text-dark';
    case '2':
      return 'bg-info';
    case '3':
      return 'bg-primary';
    case '4':
      return 'bg-secondary';
    case '5':
      return 'bg-success';
    default:
      return 'bg-light text-dark';
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';
  return date.toISOString().slice(0, 10);
};

const CM_90_1051_orderDetailModal = ({ show, onHide, orderData, rowData, addressData }) => {
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [loading] = useState(false);

  const purchaseColumnDefs = useMemo(
    () => [
      {
        headerName: '商品名',
        field: 'productName',
        flex: 2,
        minWidth: 150,
      },
      {
        headerName: '数量',
        field: 'quantity',
        flex: 1,
        minWidth: 80,
        cellStyle: { textAlign: 'center' },
      },
      {
        headerName: '価格',
        field: 'totalPrice',
        flex: 1,
        minWidth: 100,
        cellStyle: { textAlign: 'right' },
        valueFormatter: (params) => {
          return params.value ? `₩${params.value.toLocaleString()}` : '';
        },
      },
      {
        headerName: '単価',
        field: 'unitPrice',
        flex: 1,
        minWidth: 100,
        cellStyle: { textAlign: 'right' },
        valueFormatter: (params) => {
          return params.value ? `₩${params.value.toLocaleString()}` : '';
        },
      },
    ],
    []
  );

  const closeErrorModal = useCallback(() => {
    setShowErrorModal(false);
  }, []);

  if (!show || !orderData) {
    return null;
  }

  const statusOption = ORDER_STATUS.find((s) => s.value === orderData.orderStatusValue);
  const paymentOption = PAYMENT_METHODS.find((p) => p.value === orderData.paymentValue);

  // addressData のデフォルト値設定
  const address = addressData || {};

  return (
    <>
      <div
        className={`modal-backdrop fade ${show ? 'show' : ''}`}
        style={{ display: show ? 'block' : 'none' }}
        onClick={onHide}
      ></div>

      <div
        className={`modal fade ${show ? 'show' : ''}`}
        style={{ display: show ? 'block' : 'none' }}
        tabIndex="-1"
        role="dialog"
      >
        <div className="modal-dialog modal-xl" role="document">
          <div className="modal-content border-0 shadow-lg">
            <div className="modal-header bg-primary text-white">
              <h5 className="modal-title fw-bold">注文詳細情報</h5>
              <button
                type="button"
                className="btn-close btn-close-white"
                onClick={onHide}
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body p-4" style={{ backgroundColor: '#f8f9fa' }}>
              {/* 基本情報・決済情報 */}
              <div className="row g-3 mb-3">
                <div className="col-md-6">
                  <div className="card border-0 shadow-sm h-100">
                    <div className="card-header bg-white border-bottom">
                      <h6 className="mb-0 fw-bold text-secondary">基本情報</h6>
                    </div>
                    <div className="card-body">
                      <table className="table table-borderless mb-0">
                        <tbody>
                          <tr>
                            <td className="text-muted fw-semibold" style={{ width: '40%' }}>
                              注文番号:
                            </td>
                            <td className="fw-medium">{orderData.orderNumber || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">購入者ID:</td>
                            <td className="fw-medium">{orderData.loginId || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">会員番号:</td>
                            <td className="fw-medium">{orderData.memberNumber || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">注文日:</td>
                            <td className="fw-medium">{formatDate(orderData.orderDate)}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>

                <div className="col-md-6">
                  <div className="card border-0 shadow-sm h-100">
                    <div className="card-header bg-white border-bottom">
                      <h6 className="mb-0 fw-bold text-secondary">決済情報</h6>
                    </div>
                    <div className="card-body">
                      <table className="table table-borderless mb-0">
                        <tbody>
                          <tr>
                            <td className="text-muted fw-semibold" style={{ width: '40%' }}>
                              決済金額:
                            </td>
                            <td className="text-primary fw-bold fs-5">
                              ₩{orderData.totalAmount?.toLocaleString() || '0'}
                            </td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">決済方法:</td>
                            <td>
                              <span className="badge bg-secondary fs-6 px-3 py-2">
                                {paymentOption?.label || orderData.paymentValue || '-'}
                              </span>
                            </td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">現在のステータス:</td>
                            <td>
                              <span
                                className={`badge ${getStatusBadgeClass(orderData.orderStatusValue)} fs-6 px-3 py-2`}
                              >
                                {statusOption?.label || orderData.orderStatusValue || '-'}
                              </span>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              {/* 配送先情報 */}
              <div className="card border-0 shadow-sm mb-3">
                <div className="card-header bg-white border-bottom">
                  <h6 className="mb-0 fw-bold text-secondary">配送先情報</h6>
                </div>
                <div className="card-body">
                  <div className="row g-3">
                    <div className="col-md-6">
                      <table className="table table-borderless mb-0">
                        <tbody>
                          <tr>
                            <td className="text-muted fw-semibold" style={{ width: '40%' }}>
                              配送先名称:
                            </td>
                            <td className="fw-medium">{address.addressNickname || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">受取人:</td>
                            <td className="fw-medium">{address.recipientName || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">郵便番号:</td>
                            <td className="fw-medium">{address.postalCode || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">国:</td>
                            <td className="fw-medium">{address.country || '-'}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <div className="col-md-6">
                      <table className="table table-borderless mb-0">
                        <tbody>
                          <tr>
                            <td className="text-muted fw-semibold" style={{ width: '40%' }}>
                              住所:
                            </td>
                            <td className="fw-medium">{address.addressMain || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">住所詳細 1:</td>
                            <td className="fw-medium">{address.addressDetail1 || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">住所詳細 2:</td>
                            <td className="fw-medium">{address.addressDetail2 || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">住所詳細 3:</td>
                            <td className="fw-medium">{address.addressDetail3 || '-'}</td>
                          </tr>
                          <tr>
                            <td className="text-muted fw-semibold">携帯電話番号:</td>
                            <td className="fw-medium">{address.recipientPhone || '-'}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              {/* 購入リスト */}
              <div className="card border-0 shadow-sm">
                <div className="card-header bg-white border-bottom">
                  <h6 className="mb-0 fw-bold text-secondary">購入リスト</h6>
                </div>
                <div className="card-body p-0">
                  <div style={{ height: '280px' }}>
                    {loading ? (
                      <div className="d-flex justify-content-center align-items-center h-100">
                        <div className="spinner-border text-primary" role="status">
                          <span className="visually-hidden">Loading...</span>
                        </div>
                      </div>
                    ) : (
                      <CM_90_1011_grid
                        itemsPerPage={rowData?.length || 10}
                        pagesPerGroup={1}
                        rowData={rowData || []}
                        columnDefs={purchaseColumnDefs}
                        wrapperClass="h-100"
                        hidePagination={true}
                      />
                    )}
                  </div>
                </div>
              </div>
            </div>

            <div className="modal-footer bg-white border-top">
              <button type="button" className="btn btn-secondary px-4" onClick={onHide}>
                閉じる
              </button>
            </div>
          </div>
        </div>
      </div>

      <CM_99_1003 isOpen={showErrorModal} onClose={closeErrorModal} message={errorMessage} />
    </>
  );
};

export default CM_90_1051_orderDetailModal;
