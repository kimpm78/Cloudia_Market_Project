import { useState, useCallback, useMemo } from 'react';
import CM_90_1011_grid from './CM_90_1000_grid';
import axiosInstance from '../services/axiosInstance';

const CM_90_1052_ReturnRequestModal = ({ show, onHide, onConfirm }) => {
  const [requestNo, setRequestNo] = useState('');
  const [refundNumber, setRefundNumber] = useState('');
  const [orderNumber, setOrderNumber] = useState('');
  const [refundType, setRefundType] = useState('0');
  const [exchangeParts, setExchangeParts] = useState('0');
  const [shippingList, setShippingList] = useState([]);
  const [quantities, setQuantities] = useState({});
  const [shippingFee, setShippingFee] = useState('0');
  const [memo, setMemo] = useState('');
  const [loading, setLoading] = useState(false);
  const [shippingAmount, setShippingAmount] = useState('');

  const handleSearch = useCallback(async () => {
    if (!requestNo.trim()) {
      alert('依頼番号を入力してください。');
      return;
    }

    if (!refundNumber.trim()) {
      alert('会員番号を入力してください。');
      return;
    }
    if (!orderNumber.trim()) {
      alert('購入番号を入力してください。');
      return;
    }

    setLoading(true);
    try {
      const response = await axiosInstance.get('/admin/settlement/refund/orderDetail', {
        params: { requestNo, refundNumber, orderNumber },
      });

      const productData = response.data.resultList || [];

      const mappedData = productData.map((item) => ({
        productNumber: item.productId,
        productName: item.productName,
        unitPrice: parseInt(item.unitPrice) || 0,
        maxQuantity: parseInt(item.quantity) || 0,
      }));

      setShippingList(mappedData);

      const initialQuantities = {};
      mappedData.forEach((item) => {
        initialQuantities[item.productNumber] = 0;
      });
      setQuantities(initialQuantities);
    } catch (error) {
      console.error('商品検索エラー:', error);
      alert('商品検索中にエラーが発生しました。');
      setShippingList([]);
      setQuantities({});
    } finally {
      setLoading(false);
    }
  }, [requestNo, refundNumber, orderNumber]);

  const handleQuantityChange = useCallback((productNumber, value) => {
    setQuantities((prev) => ({
      ...prev,
      [productNumber]: parseInt(value) || 0,
    }));
  }, []);

  const handleRefundTypeChange = useCallback((type) => {
    setRefundType(type);
    if (type === '0') {
      setExchangeParts('0');
    }
  }, []);

  // 商品合計金額の計算
  const calculateProductTotal = useCallback(() => {
    let total = 0;
    shippingList.forEach((item) => {
      const quantity = quantities[item.productNumber] || 0;
      total += (item.unitPrice || 0) * quantity;
    });
    return total;
  }, [shippingList, quantities]);

  // 最終キャンセル金額の計算（商品金額 + 配送料）
  const calculateTotalAmount = useCallback(() => {
    const productTotal = calculateProductTotal();
    const shipping = parseInt(shippingAmount) || 0;
    return productTotal + shipping;
  }, [calculateProductTotal, shippingAmount]);

  const columnDefs = useMemo(
    () => [
      {
        headerName: '商品名',
        field: 'productName',
        flex: 2,
        minWidth: 200,
      },
      {
        headerName: '数量',
        field: 'productNumber',
        flex: 1,
        minWidth: 100,
        cellStyle: { textAlign: 'center' },
        cellRenderer: (params) => {
          const maxQuantity = params.data.maxQuantity || 10;
          return (
            <select
              className="form-select form-select-sm"
              value={quantities[params.value] || 0}
              onChange={(e) => handleQuantityChange(params.value, e.target.value)}
            >
              {[...Array(maxQuantity + 1)].map((_, i) => (
                <option key={i} value={i}>
                  {i}
                </option>
              ))}
            </select>
          );
        },
      },
    ],
    [quantities, handleQuantityChange]
  );

  const handleConfirm = useCallback(() => {
    if (!refundNumber.trim()) {
      alert('会員番号を入力してください。');
      return;
    }

    if (!orderNumber.trim()) {
      alert('購入番号を入力してください。');
      return;
    }

    if (shippingList.length === 0) {
      alert('商品を検索してください。');
      return;
    }

    const selectedProducts = shippingList.filter((item) => {
      const quantity = quantities[item.productNumber] || 0;
      return quantity > 0;
    });

    if (selectedProducts.length === 0) {
      alert('返金する商品の数量を選択してください。');
      return;
    }

    const productTotalAmount = calculateProductTotal();
    const shippingAmountValue = parseInt(shippingAmount) || 0;

    const data = {
      requestNo,
      refundNumber,
      orderNumber,
      refundType,
      exchangeParts: refundType === '1' ? exchangeParts : '0',
      products: selectedProducts.map((item) => ({
        productNumber: item.productNumber,
        productName: item.productName,
        quantity: quantities[item.productNumber] || 0,
        unitPrice: item.unitPrice,
        totalPrice: (item.unitPrice || 0) * (quantities[item.productNumber] || 0),
      })),
      shippingFee,
      shippingAmount: shippingAmountValue,
      productTotalAmount: productTotalAmount,
      totalAmount: productTotalAmount + shippingAmountValue,
      memo,
    };

    onConfirm(data);
  }, [
    requestNo,
    refundNumber,
    orderNumber,
    refundType,
    exchangeParts,
    shippingList,
    quantities,
    shippingFee,
    shippingAmount,
    memo,
    calculateProductTotal,
    onConfirm,
  ]);

  const handleClose = useCallback(() => {
    setRequestNo('');
    setRefundNumber('');
    setOrderNumber('');
    setRefundType('0');
    setExchangeParts('0');
    setShippingList([]);
    setQuantities({});
    setShippingFee('0');
    setMemo('');
    setShippingAmount('');
    onHide();
  }, [onHide]);

  if (!show) {
    return null;
  }

  return (
    <>
      <div
        className={`modal-backdrop fade ${show ? 'show' : ''}`}
        style={{ display: show ? 'block' : 'none' }}
        onClick={handleClose}
      ></div>

      <div
        className={`modal fade ${show ? 'show' : ''}`}
        style={{ display: show ? 'block' : 'none' }}
        tabIndex="-1"
        role="dialog"
      >
        <div className="modal-dialog modal-lg" role="document">
          <div className="modal-content border-0 shadow-lg">
            <div className="modal-header bg-primary text-white">
              <h5 className="modal-title fw-bold">交換 / 返金依頼</h5>
              <button
                type="button"
                className="btn-close btn-close-white"
                onClick={handleClose}
                aria-label="Close"
              ></button>
            </div>

            <div className="modal-body p-4" style={{ backgroundColor: '#f8f9fa' }}>
              {/* 依頼番号 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">依頼番号</label>
                <input
                  type="text"
                  className="form-control"
                  value={requestNo}
                  onChange={(e) => setRequestNo(e.target.value)}
                  placeholder="依頼番号を入力してください"
                />
              </div>
              {/* 会員番号 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">会員番号</label>
                <input
                  type="text"
                  className="form-control"
                  value={refundNumber}
                  onChange={(e) => setRefundNumber(e.target.value)}
                  placeholder="会員番号を入力してください"
                />
              </div>

              {/* 購入番号 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">購入番号</label>
                <input
                  type="text"
                  className="form-control mb-2"
                  value={orderNumber}
                  onChange={(e) => setOrderNumber(e.target.value)}
                  placeholder="購入番号を入力してください"
                />
                <button className="btn btn-primary w-100" onClick={handleSearch} disabled={loading}>
                  {loading ? '検索中...' : '検索'}
                </button>
              </div>

              {/* 区分 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">区分</label>
                <div className="d-flex gap-3">
                  <div className="form-check">
                    <input
                      className="form-check-input"
                      type="radio"
                      name="refundType"
                      id="refundTypeRefund"
                      checked={refundType === '0'}
                      onChange={() => handleRefundTypeChange('0')}
                    />
                    <label className="form-check-label" htmlFor="refundTypeRefund">
                      返金
                    </label>
                  </div>
                  <div className="form-check">
                    <input
                      className="form-check-input"
                      type="radio"
                      name="refundType"
                      id="refundTypeExchange"
                      checked={refundType === '1'}
                      onChange={() => handleRefundTypeChange('1')}
                    />
                    <label className="form-check-label" htmlFor="refundTypeExchange">
                      交換
                    </label>
                  </div>
                </div>
              </div>

              {/* 交換部品（交換選択時のみ表示） */}
              {refundType === '1' && (
                <div className="mb-3">
                  <label className="form-label fw-semibold">交換部品</label>
                  <div className="d-flex gap-3">
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="radio"
                        name="exchangeParts"
                        id="exchangePartsFull"
                        checked={exchangeParts === '0'}
                        onChange={() => setExchangeParts('0')}
                      />
                      <label className="form-check-label" htmlFor="exchangePartsFull">
                        フルパッケージ
                      </label>
                    </div>
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="radio"
                        name="exchangeParts"
                        id="exchangePartsBody"
                        checked={exchangeParts === '1'}
                        onChange={() => setExchangeParts('1')}
                      />
                      <label className="form-check-label" htmlFor="exchangePartsBody">
                        本体
                      </label>
                    </div>
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="radio"
                        name="exchangeParts"
                        id="exchangePartsParts"
                        checked={exchangeParts === '2'}
                        onChange={() => setExchangeParts('2')}
                      />
                      <label className="form-check-label" htmlFor="exchangePartsParts">
                        パーツ
                      </label>
                    </div>
                  </div>
                </div>
              )}

              {/* 商品リスト */}
              <div className="mb-3">
                <label className="form-label fw-semibold">商品リスト</label>
                <div className="card border-0 shadow-sm">
                  <div className="card-body p-0">
                    <div style={{ height: '250px' }}>
                      {loading ? (
                        <div className="d-flex justify-content-center align-items-center h-100">
                          <div className="spinner-border text-primary" role="status">
                            <span className="visually-hidden">Loading...</span>
                          </div>
                        </div>
                      ) : shippingList.length === 0 ? (
                        <div className="d-flex justify-content-center align-items-center h-100 text-muted">
                          購入番号を検索してください
                        </div>
                      ) : (
                        <CM_90_1011_grid
                          itemsPerPage={shippingList.length}
                          pagesPerGroup={1}
                          rowData={shippingList}
                          columnDefs={columnDefs}
                          wrapperClass="h-100"
                          hidePagination={true}
                        />
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* 配送料 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">配送料</label>
                <div className="d-flex gap-3">
                  <div className="form-check">
                    <input
                      className="form-check-input"
                      type="radio"
                      name="shippingFee"
                      id="shippingFeeStore"
                      checked={shippingFee === '0'}
                      onChange={() => setShippingFee('0')}
                    />
                    <label className="form-check-label" htmlFor="shippingFeeStore">
                      クラウディアマーケット負担
                    </label>
                  </div>
                  <div className="form-check">
                    <input
                      className="form-check-input"
                      type="radio"
                      name="shippingFee"
                      id="shippingFeeCustomer"
                      checked={shippingFee === '1'}
                      onChange={() => setShippingFee('1')}
                    />
                    <label className="form-check-label" htmlFor="shippingFeeCustomer">
                      購入者負担
                    </label>
                  </div>
                </div>
              </div>

              {/* 配送料金入力 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">配送料金</label>
                <input
                  type="number"
                  className="form-control"
                  value={shippingAmount}
                  onChange={(e) => setShippingAmount(e.target.value)}
                  placeholder="配送料金を入力してください（例：3000）"
                />
              </div>

              {/* キャンセル金額の自動計算 */}
              <div className="mb-3">
                <label className="form-label fw-semibold">キャンセル金額</label>
                <div className="card bg-light">
                  <div className="card-body">
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">商品合計:</span>
                      <span className="fw-semibold">
                        {calculateProductTotal().toLocaleString()} 円
                      </span>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">配送料:</span>
                      <span className="fw-semibold">
                        {(parseInt(shippingAmount) || 0).toLocaleString()} 円
                      </span>
                    </div>
                    <hr />
                    <div className="d-flex justify-content-between">
                      <span className="fw-bold">キャンセル総額:</span>
                      <span className="text-primary fw-bold" style={{ fontSize: '20px' }}>
                        {calculateTotalAmount().toLocaleString()} 円
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* メモ */}
              <div className="mb-3">
                <label className="form-label fw-semibold">メモ</label>
                <textarea
                  className="form-control"
                  rows="3"
                  value={memo}
                  onChange={(e) => setMemo(e.target.value)}
                  placeholder="内容を入力してください"
                />
              </div>
            </div>

            <div className="modal-footer bg-white border-top">
              <button className="btn btn-primary px-4" onClick={handleConfirm}>
                返金処理
              </button>
              <button className="btn btn-secondary px-4" onClick={handleClose}>
                閉じる
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default CM_90_1052_ReturnRequestModal;