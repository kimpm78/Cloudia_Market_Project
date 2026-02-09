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
    if (typeof handleToggleDefaultCheckbox === 'function') {
      handleToggleDefaultCheckbox(e, { suppressToast: !checked });
    }
  };

  return (
    <>
      {/* 상단 배송지 정보 헤더 + 편집 버튼 */}
      <div className="d-flex align-items-center justify-content-between mt-4">
        <h5 className="mb-0">배송지 정보</h5>
        <button className="btn btn-primary btn-sm cm-address-edit-btn" onClick={openAddressModal}>
          편집
        </button>
      </div>

      {/* 현재 선택된 배송지 표시 카드 */}
      <div className="p-3 border mt-2">
        {currentAddressNickname && (
          <div className="mb-2">
            <span className="badge bg-primary-subtle text-primary me-2">선택 배송지</span>
            <span className="text-muted">{currentAddressNickname}</span>
          </div>
        )}
        <div className="d-flex justify-content-between py-1">
          <strong>이름</strong>
          <span>{currentReceiverName}</span>
        </div>
        <div className="d-flex justify-content-between py-1">
          <strong>주소</strong>
          <span>{receiverAddress}</span>
        </div>
        <div className="d-flex justify-content-between py-1">
          <strong>전화번호</strong>
          <span>{currentReceiverPhone}</span>
        </div>
        <div className="cm-address-note">※배송전화는 최대 7자까지 입력됩니다.</div>
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
            다음에도 사용할게요
          </label>
        </div>
        <div className="text-end mt-2">
          <button className="btn btn-link p-0" onClick={handleNavigateToAddressBook}>
            주소록 관리 바로가기
          </button>
        </div>
      </div>

      {/* 배송지 변경 모달 (원본 디자인 그대로) */}
      {showAddressModal && (
        <>
          <div className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center cm-address-modal-backdrop">
            <div className="bg-white p-4 shadow-lg cm-address-modal" role="document">
              <div className="modal-content border-0">
                <div className="d-flex justify-content-between align-items-start mb-4">
                  <div>
                    <h5 className="modal-title fw-semibold">배송지 변경</h5>
                    <small className="text-muted">
                      등록된 배송지 정보를 선택해 주세요.
                      <br />
                      <span className="text-primary">
                        ※ 배송지 등록과 자택 정보는 마이페이지에서 변경하실 수 있습니다.
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
                      등록된 배송지가 없습니다.{` `}
                      <button
                        className="btn btn-link p-0 align-baseline"
                        onClick={handleNavigateToAddressBook}
                      >
                        마이페이지에서 추가하기
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
                                  {addr.addressNickname || `배송지 ${addr.addressId}`}
                                </span>
                                {addr.isDefault && (
                                  <span className="badge bg-primary-subtle text-primary">기본</span>
                                )}
                              </div>
                              <div className="small text-muted mb-1">이름</div>
                              <div className="fw-semibold">{addr.recipientName || '-'}</div>
                              <div className="small text-muted mt-3 mb-1">주소</div>
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
                              <div className="small text-muted mt-3 mb-1">전화번호</div>
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
                    취소
                  </button>
                  <button
                    className="btn btn-primary"
                    onClick={applySelectedAddress}
                    disabled={!selectedAddressId || deliveryAddresses.length === 0}
                  >
                    적용
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