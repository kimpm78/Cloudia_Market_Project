import '../styles/CM_06_1001_Address.css';

function CM_06_1001_Address({
  currentAddressNickname,
  currentReceiverName,
  currentReceiverPhone,
  receiverAddress,
  deliveryAddresses,
  isAddressLoading,
  addressError,
  showAddressModal,
  openAddressModal,
  closeAddressModal,
  selectedAddressId,
  handleAddressRadioChange,
  applySelectedAddress,
  navigateToAddressBook,
  shippingOverride,
  currentAddressRecord,
  isSavingDefault,
  handleToggleDefaultCheckbox,
}) {
  const handleNavigateToAddressBook = () => {
    if (typeof navigateToAddressBook === 'function') {
      navigateToAddressBook();
    }
  };

  const isCurrentAddressDefault = Boolean(
    (shippingOverride && typeof shippingOverride.isDefault === 'boolean'
      ? shippingOverride.isDefault
      : undefined) ??
      (currentAddressRecord && typeof currentAddressRecord.isDefault === 'boolean'
        ? currentAddressRecord.isDefault
        : undefined)
  );

  const handleDefaultCheckboxChange = (e) => {
    const checked = e.target.checked;
    const hasDeliveryInfo =
      String(currentReceiverName || '').trim().length > 0 &&
      String(receiverAddress || '').trim().length > 0 &&
      String(currentReceiverPhone || '').trim().length > 0;

    if (checked && !hasDeliveryInfo) {
      window.CM_showToast('配送先情報を先に追加してください。');
      return;
    }

    if (typeof handleToggleDefaultCheckbox === 'function') {
      handleToggleDefaultCheckbox(e, { suppressToast: !checked });
    }
  };

  return (
    <>
      {/* 上部：配送先情報ヘッダー + 編集ボタン */}
      <div className="d-flex align-items-center justify-content-between mt-4">
        <h5 className="mb-0 fw-semibold">配送先情報</h5>
        <button className="btn btn-primary btn-sm cm-address-edit-btn" onClick={openAddressModal}>
          編集
        </button>
      </div>

      {/* 現在選択中の配送先表示カード */}
      <div className="p-3 border mt-2">
        {currentAddressNickname && (
          <div className="mb-2">
            <span className="badge bg-primary-subtle text-primary me-2">選択中の配送先</span>
            <span className="text-muted">{currentAddressNickname}</span>
          </div>
        )}
        <div className="d-flex justify-content-between py-1">
          <strong>氏名</strong>
          <span>{currentReceiverName}</span>
        </div>
        <div className="d-flex justify-content-between py-1">
          <strong>住所</strong>
          <span>{receiverAddress}</span>
        </div>
        <div className="d-flex justify-content-between py-1">
          <strong>電話番号</strong>
          <span>{currentReceiverPhone}</span>
        </div>
        <div className="cm-address-note">※配送用電話番号は最大7文字まで入力できます。</div>
        <div className="form-check mt-3">
          <input
            className="form-check-input"
            type="checkbox"
            id="saveDefaultAddress"
            checked={isCurrentAddressDefault}
            onChange={handleDefaultCheckboxChange}
            disabled={isSavingDefault}
          />
          <label className="form-check-label" htmlFor="saveDefaultAddress">
            次回も使用する
          </label>
        </div>
        <div className="text-end mt-2">
          <button className="btn btn-link p-0" onClick={handleNavigateToAddressBook}>
            住所録管理へ
          </button>
        </div>
      </div>

      {/* 配送先変更モーダル（元デザインのまま） */}
      {showAddressModal && (
        <>
          <div className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center cm-address-modal-backdrop">
            <div className="bg-white p-4 shadow-lg cm-address-modal" role="document">
              <div className="modal-content border-0">
                <div className="d-flex justify-content-between align-items-start mb-4">
                  <div>
                    <h5 className="modal-title fw-semibold">配送先の変更</h5>
                    <small className="text-muted">
                      登録済みの配送先情報を選択してください。
                      <br />
                      <span className="text-primary">
                        ※ 配送先登録および自宅情報はマイページで変更できます。
                      </span>
                    </small>
                  </div>
                  <button type="button" className="btn-close" onClick={closeAddressModal} />
                </div>
                <div className="modal-body pt-0">
                  {isAddressLoading ? (
                    <div className="text-center py-4">
                      <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                      </div>
                    </div>
                  ) : addressError ? (
                    <div className="alert alert-danger mb-0" role="alert">
                      {addressError}
                    </div>
                  ) : deliveryAddresses.length === 0 ? (
                    <div className="py-4 text-center text-muted">
                      登録された配送先がありません。{` `}
                      <button
                        className="btn btn-link p-0 align-baseline"
                        onClick={handleNavigateToAddressBook}
                      >
                        マイページで追加する
                      </button>
                    </div>
                  ) : (
                    <div className="row g-3 mb-4 pb-3 border-bottom">
                      {deliveryAddresses.map((addr) => {
                        const isSelected = selectedAddressId === addr.addressId;
                        return (
                          <div className="col-12 col-md-4" key={addr.addressId}>
                            <label
                              className={`card h-100 p-3 cm-address-card ${isSelected ? 'cm-address-card--active' : ''}`}
                            >
                              <input
                                type="radio"
                                className="form-check-input visually-hidden"
                                name="deliveryAddress"
                                checked={isSelected}
                                onChange={() => handleAddressRadioChange(addr.addressId)}
                              />
                              <div className="d-flex justify-content-between align-items-center mb-2">
                                <span className="fw-semibold">
                                  {addr.addressNickname || `配送先 ${addr.addressId}`}
                                </span>
                                {addr.isDefault && (
                                  <span className="badge bg-primary-subtle text-primary">
                                    デフォルト
                                  </span>
                                )}
                              </div>
                              <div className="small text-muted mb-1">氏名</div>
                              <div className="fw-semibold">{addr.recipientName || '-'}</div>
                              <div className="small text-muted mt-3 mb-1">住所</div>
                              <div className="text-break">
                                {[addr.postalCode ? `(${addr.postalCode})` : null, addr.addressMain]
                                  .filter(Boolean)
                                  .join(' ')}
                              </div>
                              {[addr.addressDetail1, addr.addressDetail2, addr.addressDetail3]
                                .filter((part) => part && part.trim().length > 0)
                                .map((detail, index) => (
                                  <div key={index} className="text-break">
                                    {detail}
                                  </div>
                                ))}
                              <div className="small text-muted mt-3 mb-1">電話番号</div>
                              <div>{addr.recipientPhone || '-'}</div>
                            </label>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
                <div className="d-flex justify-content-end gap-2">
                  <button className="btn btn-outline-secondary" onClick={closeAddressModal}>
                    キャンセル
                  </button>
                  <button
                    className="btn btn-primary"
                    onClick={applySelectedAddress}
                    disabled={!selectedAddressId || deliveryAddresses.length === 0}
                  >
                    適用
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop fade show" style={{ zIndex: 1050 }} />
        </>
      )}
    </>
  );
}

export default CM_06_1001_Address;
