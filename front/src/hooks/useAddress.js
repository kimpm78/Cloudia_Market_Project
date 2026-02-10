import { useCallback, useEffect, useMemo, useState, useReducer, useRef } from 'react';
import axiosInstance from '../services/axiosInstance';

export function useAddress({ memberNumber, shippingDefaults, initialShippingOverride = null }) {
  // 配送先一覧および関連状態
  const [deliveryAddresses, setDeliveryAddresses] = useState([]); // 配送先一覧
  const [isAddressLoading, setIsAddressLoading] = useState(false); // 一覧のローディング状態
  const [addressError, setAddressError] = useState(''); // 一覧エラー
  // 配送先選択およびモーダル状態
  const [showAddressModal, setShowAddressModal] = useState(false); // 配送先モーダル表示有無
  const [selectedAddressId, setSelectedAddressId] = useState(null); // 選択中の配送先ID
  const [shippingOverride, setShippingOverride] = useState(initialShippingOverride); // 選択・編集された配送先オーバーライド

  // 配送先フォーム状態
  const [addressFormMode, setAddressFormMode] = useState(null);

  // 初期状態定義
  const initialFormState = {
    addressNickname: '',
    recipientName: '',
    recipientPhone: '',
    postalCode: '',
    addressMain: '',
    addressDetail1: '',
    addressDetail2: '',
    addressDetail3: '',
    isDefault: false,
  };

  // リデューサー定義
  function addressFormReducer(state, action) {
    switch (action.type) {
      case 'SET_FIELD':
        return { ...state, [action.field]: action.value };
      case 'SET_ALL':
        return { ...state, ...action.payload };
      case 'RESET':
        return initialFormState;
      default:
        return state;
    }
  }

  // useReducerでaddressFormDataを管理
  const [addressFormData, dispatchForm] = useReducer(addressFormReducer, initialFormState);

  const [addressFormError, setAddressFormError] = useState('');
  const [addressFormSubmitting, setAddressFormSubmitting] = useState(false);
  const [isSavingDefault, setIsSavingDefault] = useState(false);

  // preferredDefaultIdをuseRefで管理
  const preferredDefaultIdRef = useRef(null);
  useEffect(() => {
    if (shippingOverride?.addressId && normalizeDefaultFlag(shippingOverride?.isDefault)) {
      preferredDefaultIdRef.current = shippingOverride.addressId;
    } else {
      preferredDefaultIdRef.current = null;
    }
  }, [shippingOverride?.addressId, shippingOverride?.isDefault]);

  // adaptAddressListを通常関数に変更
  function adaptAddressList(rawList) {
    if (!Array.isArray(rawList)) {
      return [];
    }
    const mapped = rawList.map((addr) => ({
      addressId: addr.addressId,
      addressNickname: addr.addressNickname || '',
      recipientName: addr.recipientName || '',
      recipientPhone: addr.recipientPhone || '',
      postalCode: addr.postalCode || '',
      addressMain: addr.addressMain || '',
      addressDetail1: addr.addressDetail1 || '',
      addressDetail2: addr.addressDetail2 || '',
      addressDetail3: addr.addressDetail3 || '',
      isDefault: normalizeDefaultFlag(addr.isDefault),
    }));
    let normalizedDefaultId = preferredDefaultIdRef.current;
    if (!normalizedDefaultId) {
      const firstServerDefault = mapped.find((addr) => addr.isDefault);
      if (firstServerDefault) {
        normalizedDefaultId = firstServerDefault.addressId;
      }
    }
    const normalized = mapped.map((addr) => ({
      ...addr,
      isDefault: normalizedDefaultId != null && addr.addressId === normalizedDefaultId,
    }));
    const sorted = [...normalized].sort((a, b) => Number(b.isDefault) - Number(a.isDefault));
    if (sorted.length <= 3) {
      return sorted;
    }
    const top = sorted.slice(0, 3);
    const defaultAddress = sorted.find((addr) => addr.isDefault);
    if (defaultAddress && !top.some((addr) => addr.addressId === defaultAddress.addressId)) {
      return [
        defaultAddress,
        ...top.filter((addr) => addr.addressId !== defaultAddress.addressId),
      ].slice(0, 3);
    }
    return top;
  }

  // adaptAddressList内でrefを使用するため、依存配列は空にします。
  const refreshAddresses = useCallback(async () => {
    setIsAddressLoading(true);
    setAddressError('');
    try {
      const res = await axiosInstance.get('/user/addresses');
      const list = adaptAddressList(res.data);
      setDeliveryAddresses(list);
      return list;
    } catch (error) {
      setAddressError(
        error?.response?.data?.message || '配送先一覧を取得できませんでした。'
      );
      setDeliveryAddresses([]);
      return null;
    } finally {
      setIsAddressLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!memberNumber) {
      setDeliveryAddresses([]);
      return;
    }
    let canceled = false;
    const fetchAddresses = async () => {
      const list = await refreshAddresses();
      if (canceled) return;
      if (list && list.length > 0) {
        const initialSelected = list.find((addr) => addr.isDefault) || list[0] || null;
        setSelectedAddressId(initialSelected ? initialSelected.addressId : null);
      } else {
        setSelectedAddressId(null);
      }
    };
    fetchAddresses();
    return () => {
      canceled = true;
    };
  }, [memberNumber, refreshAddresses]);

  const currentAddressRecord = useMemo(() => {
    if (!selectedAddressId) return null;
    return deliveryAddresses.find((addr) => addr.addressId === selectedAddressId) || null;
  }, [selectedAddressId, deliveryAddresses]);

  const openAddressModal = useCallback(() => {
    const list = deliveryAddresses;
    const preferredId = shippingOverride?.addressId;
    const candidateId =
      (preferredId && list.some((addr) => addr.addressId === preferredId)
        ? preferredId
        : list.find((addr) => addr.isDefault)?.addressId) ??
      list[0]?.addressId ??
      null;
    setSelectedAddressId(candidateId);
    setShowAddressModal(true);
    if (list.length === 0) {
      setTimeout(() => openNewAddressForm(), 0);
    }
  }, [deliveryAddresses, shippingOverride?.addressId]);

  const closeAddressModal = useCallback(() => {
    setShowAddressModal(false);
    resetAddressForm();
  }, []);

  const handleAddressRadioChange = useCallback((addressId) => {
    setSelectedAddressId(addressId);
  }, []);

  const applySelectedAddress = useCallback(() => {
    const selected = deliveryAddresses.find((addr) => addr.addressId === selectedAddressId);
    if (!selected) {
      return null;
    }
    const next = {
      addressId: selected.addressId,
      recipientName: selected.recipientName,
      recipientPhone: selected.recipientPhone,
      postalCode: selected.postalCode,
      addressMain: selected.addressMain,
      addressDetail1: selected.addressDetail1,
      addressDetail2: selected.addressDetail2,
      addressDetail3: selected.addressDetail3,
      addressNickname: selected.addressNickname,
      isDefault: selected.isDefault,
    };
    setShippingOverride(next);
    setShowAddressModal(false);
    return next;
  }, [deliveryAddresses, selectedAddressId]);

  const resetAddressForm = useCallback(() => {
    setAddressFormMode(null);
    setAddressFormError('');
    dispatchForm({ type: 'RESET' });
  }, []);

  const openNewAddressForm = useCallback(
    (defaults = {}) => {
      setAddressFormMode('new');
      setAddressFormError('');
      dispatchForm({
        type: 'SET_ALL',
        payload: {
          addressNickname: defaults.addressNickname || '新しい配送先',
          recipientName: defaults.recipientName || shippingDefaults?.recipientName || '',
          recipientPhone: defaults.recipientPhone || shippingDefaults?.recipientPhone || '',
          postalCode: defaults.postalCode || shippingDefaults?.postalCode || '',
          addressMain: defaults.addressMain || shippingDefaults?.addressMain || '',
          addressDetail1: defaults.addressDetail1 || shippingDefaults?.addressDetail1 || '',
          addressDetail2: defaults.addressDetail2 || '',
          addressDetail3: defaults.addressDetail3 || '',
          isDefault: deliveryAddresses.length === 0,
        },
      });
    },
    [shippingDefaults, deliveryAddresses]
  );

  const openEditAddressForm = useCallback((address) => {
    if (!address) return;
    setAddressFormMode('edit');
    setAddressFormError('');
    dispatchForm({
      type: 'SET_ALL',
      payload: {
        addressId: address.addressId,
        addressNickname: address.addressNickname || '',
        recipientName: address.recipientName || '',
        recipientPhone: address.recipientPhone || '',
        postalCode: address.postalCode || '',
        addressMain: address.addressMain || '',
        addressDetail1: address.addressDetail1 || '',
        addressDetail2: address.addressDetail2 || '',
        addressDetail3: address.addressDetail3 || '',
        isDefault: Boolean(address.isDefault),
      },
    });
  }, []);

  const handleAddressFormChange = useCallback((field, value) => {
    dispatchForm({ type: 'SET_FIELD', field, value });
  }, []);

  const buildAddressPayload = useCallback(
    (source) => {
      const detail1 = source.addressDetail1 || source.addressDetail || '';
      const defaultFlag = normalizeDefaultFlag(source.isDefault);
      return {
        memberNumber: memberNumber || source.memberNumber || undefined,
        addressNickname: source.addressNickname || '',
        recipientName: source.recipientName || '',
        recipientPhone: source.recipientPhone || '',
        postalCode: source.postalCode || '',
        addressMain: source.addressMain || '',
        addressDetail1: detail1,
        addressDetail2: source.addressDetail2 || '',
        addressDetail3: source.addressDetail3 || '',
        isDefault: defaultFlag,
      };
    },
    [memberNumber]
  );

  const buildPayloadFromCurrentAddress = useCallback(
    (isDefault = false) =>
      buildAddressPayload({
        addressNickname:
          shippingOverride?.addressNickname || shippingDefaults?.addressNickname || '注文配送先',
        recipientName: shippingOverride?.recipientName || shippingDefaults?.recipientName || '',
        recipientPhone: shippingOverride?.recipientPhone || shippingDefaults?.recipientPhone || '',
        postalCode: shippingOverride?.postalCode || shippingDefaults?.postalCode || '',
        addressMain: shippingOverride?.addressMain || shippingDefaults?.addressMain || '',
        addressDetail1: shippingOverride?.addressDetail1 || shippingDefaults?.addressDetail1 || '',
        addressDetail2: shippingOverride?.addressDetail2 || '',
        addressDetail3: shippingOverride?.addressDetail3 || '',
        isDefault,
      }),
    [buildAddressPayload, shippingDefaults, shippingOverride]
  );

  const fetchAddressById = useCallback(async (addressId) => {
    try {
      const res = await axiosInstance.get(`/user/addresses/${addressId}`);
      return res.data;
    } catch (error) {
      throw error;
    }
  }, []);

  const updateAddress = useCallback(async (addressId, payload) => {
    try {
      const res = await axiosInstance.put(`/user/addresses/${addressId}`, payload);
      return res.data;
    } catch (error) {
      throw error;
    }
  }, []);

  const deleteAddress = useCallback(async (addressId) => {
    try {
      const res = await axiosInstance.delete(`/user/addresses/${addressId}`);
      return res.data;
    } catch (error) {
      throw error;
    }
  }, []);

  // CRUDハンドラー
  const handleAddressFormSubmit = useCallback(
    async (event) => {
      event?.preventDefault();
      if (!addressFormMode) return;
      if (!addressFormData.recipientName || !addressFormData.addressMain) {
        setAddressFormError('氏名と住所は必須です。');
        return;
      }
      setAddressFormSubmitting(true);
      setAddressFormError('');
      try {
        const payload = buildAddressPayload(addressFormData);
        if (addressFormMode === 'new') {
          await axiosInstance.post('/user/addresses', payload);
        } else if (addressFormMode === 'edit' && addressFormData.addressId) {
          await updateAddress(addressFormData.addressId, payload);
        }
        resetAddressForm();
        await refreshAddresses();
        window.CM_showToast('配送先の保存が完了しました。');
      } catch (error) {
        console.error('配送先の保存に失敗しました:', error);
        setAddressFormError(
          error?.response?.data?.message || '配送先を保存できませんでした。'
        );
        showErrorPopup(error?.response?.data?.message || '配送先を保存できませんでした。');
      } finally {
        setAddressFormSubmitting(false);
      }
    },
    [
      addressFormData,
      addressFormMode,
      buildAddressPayload,
      refreshAddresses,
      resetAddressForm,
      updateAddress,
    ]
  );

  const handleDeleteAddress = useCallback(
    async (addressId) => {
      const confirm = await showConfirmPopup('選択した配送先を削除しますか？');
      if (!confirm) {
        return;
      }
      try {
        await deleteAddress(addressId);
        if (selectedAddressId === addressId) {
          setSelectedAddressId(null);
          setShippingOverride(null);
        }
        await refreshAddresses();
        window.CM_showToast('配送先の削除が完了しました。');
      } catch (error) {
        console.error('配送先の削除に失敗しました:', error);
        showErrorPopup(error?.response?.data?.message || '配送先を削除できませんでした。');
      }
    },
    [refreshAddresses, selectedAddressId, deleteAddress]
  );

  const handleToggleDefaultCheckbox = useCallback(
    async (event, options = {}) => {
      const checked = event.target.checked;
      const hasText = (value) => String(value || '').trim().length > 0;
      const payloadCandidate = currentAddressRecord || shippingOverride || shippingDefaults || {};
      const hasMinimumAddressData =
        hasText(payloadCandidate.recipientName) &&
        hasText(payloadCandidate.addressMain) &&
        hasText(payloadCandidate.recipientPhone);

      if (checked && !hasMinimumAddressData) {
        window.CM_showToast('配送先情報を先に追加してください。');
        return;
      }

      setIsSavingDefault(true);
      try {
        if (checked) {
          if (currentAddressRecord) {
            const payload = buildAddressPayload({ ...currentAddressRecord, isDefault: true });
            await axiosInstance.put(`/user/addresses/${currentAddressRecord.addressId}`, payload);
          } else {
            const payload = buildPayloadFromCurrentAddress(true);
            await axiosInstance.post('/user/addresses', payload);
          }
        } else if (currentAddressRecord) {
          const payload = buildAddressPayload({ ...currentAddressRecord, isDefault: false });
          await axiosInstance.put(`/user/addresses/${currentAddressRecord.addressId}`, payload);
        }
        const targetAddressId =
          currentAddressRecord?.addressId ?? shippingOverride?.addressId ?? null;
        if (targetAddressId) {
          setDeliveryAddresses((prev) =>
            prev.map((addr) => {
              if (addr.addressId === targetAddressId) {
                return { ...addr, isDefault: checked };
              }
              if (checked) {
                return { ...addr, isDefault: false };
              }
              return addr;
            })
          );
        }
        const refreshed = await refreshAddresses();
        const resolvedTarget =
          checked && refreshed
            ? (targetAddressId && refreshed.find((addr) => addr.addressId === targetAddressId)) ||
              refreshed.find((addr) => addr.isDefault)
            : null;

        setShippingOverride((prev) => {
          const seedFromCurrent = () => ({
            addressId: currentAddressRecord?.addressId ?? null,
            addressNickname:
              currentAddressRecord?.addressNickname || shippingDefaults?.addressNickname || '',
            recipientName: currentAddressRecord?.recipientName || shippingDefaults?.recipientName || '',
            recipientPhone:
              currentAddressRecord?.recipientPhone || shippingDefaults?.recipientPhone || '',
            postalCode: currentAddressRecord?.postalCode || shippingDefaults?.postalCode || '',
            addressMain: currentAddressRecord?.addressMain || shippingDefaults?.addressMain || '',
            addressDetail1:
              currentAddressRecord?.addressDetail1 || shippingDefaults?.addressDetail1 || '',
            addressDetail2: currentAddressRecord?.addressDetail2 || '',
            addressDetail3: currentAddressRecord?.addressDetail3 || '',
          });

          const base = prev ? { ...prev } : seedFromCurrent();

          if (resolvedTarget) {
            setSelectedAddressId(resolvedTarget.addressId);
            return {
              ...base,
              addressId: resolvedTarget.addressId,
              recipientName: resolvedTarget.recipientName || base.recipientName,
              recipientPhone: resolvedTarget.recipientPhone || base.recipientPhone,
              postalCode: resolvedTarget.postalCode || base.postalCode,
              addressMain: resolvedTarget.addressMain || base.addressMain,
              addressDetail1: resolvedTarget.addressDetail1 || base.addressDetail1,
              addressDetail2: resolvedTarget.addressDetail2 || base.addressDetail2,
              addressDetail3: resolvedTarget.addressDetail3 || base.addressDetail3,
              addressNickname: resolvedTarget.addressNickname || base.addressNickname,
              isDefault: true,
            };
          }

          return { ...base, isDefault: checked };
        });
        if (checked && !options.suppressToast) {
          window.CM_showToast('デフォルト配送先の設定が完了しました。');
        }
      } catch (error) {
        showErrorPopup(
          error?.response?.data?.message || 'デフォルト配送先の設定に失敗しました。'
        );
      } finally {
        setIsSavingDefault(false);
      }
    },
    [
      currentAddressRecord,
      shippingOverride,
      buildAddressPayload,
      buildPayloadFromCurrentAddress,
      refreshAddresses,
      shippingDefaults,
    ]
  );

  return {
    deliveryAddresses,
    isAddressLoading,
    addressError,
    refreshAddresses,
    showAddressModal,
    openAddressModal,
    closeAddressModal,
    selectedAddressId,
    setSelectedAddressId,
    handleAddressRadioChange,
    applySelectedAddress,
    shippingOverride,
    setShippingOverride,
    currentAddressRecord,
    addressFormMode,
    addressFormData,
    dispatchForm,
    addressFormError,
    addressFormSubmitting,
    isSavingDefault,
    openNewAddressForm,
    openEditAddressForm,
    handleAddressFormChange,
    handleAddressFormSubmit,
    handleDeleteAddress,
    handleToggleDefaultCheckbox,
    resetAddressForm,
    fetchAddressById,
    updateAddress,
    deleteAddress,
  };
}

// さまざまな型のデフォルトフラグをbooleanに変換
const normalizeDefaultFlag = (value) => {
  if (typeof value === 'boolean') return value;
  if (typeof value === 'number') return value === 1;
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    if (['y', 'yes', 'true', '1'].includes(normalized)) return true;
  }
  return false;
};
