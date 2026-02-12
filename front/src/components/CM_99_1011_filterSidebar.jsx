import { getCategoryGroupsForPage } from '../utils/FilterUtils';
import { useState, useEffect } from 'react';
import axiosInstance from '../services/axiosInstance';

export default function CM_99_1011_filterSideBar({
  pageName,
  selectedFilters,
  onFilterChange,
  onReset,
  keyword,
  onKeywordChange,
  categoryLabel = 'カテゴリー',
  groupSelectionMode = 'include-children',
}) {
  const [visible, setVisible] = useState(true);
  const [localCategoryGroups, setLocalCategoryGroups] = useState([]);
  const [localCategories, setLocalCategories] = useState([]);
  const [categoryOptions, setCategoryOptions] = useState([]);
  const [expandedGroups, setExpandedGroups] = useState({});

  const normalizeFilterLabel = (label) => {
    const value = String(label ?? '').trim();
    const aliasMap = {
      売り切れ商品を表示: '品切れ商品表示',
      売り切れ商品を除外: '品切れ商品除外',
      受付終了商品を表示: '終了商品表示',
      締切済み商品を表示: '終了商品表示',
      予約終了商品を除外: '予約終了商品除外',
      予約終了商品を除く: '予約終了商品除外',
      '10,000円以上': '1万円以上',
      '50,000円以上': '5万円以上',
      '100,000円以上': '10万円以上',
    };
    return aliasMap[value] ?? value;
  };

  const isSelectedFilter = (label) => {
    const target = normalizeFilterLabel(label);
    return (selectedFilters || []).some((f) => normalizeFilterLabel(f) === target);
  };

  useEffect(() => {
    setCategoryOptions(localCategoryGroups.map((g) => g.label));
    setExpandedGroups((prev) => {
      const initial = {};
      localCategoryGroups.forEach((g) => {
        initial[g.label] = true;
      });
      return initial;
    });
  }, [localCategoryGroups]);

  useEffect(() => {
    const fetchCategoryGroupsWithDetails = async () => {
      try {
        const res = await axiosInstance.get('/guest/product/categoryGroupForCheckbox');
        const groups = res.data?.resultList ?? [];

        const mappedGroups = groups.map((g) => ({
          label: g.groupName,
          value: g.groupCode,
        }));

        const mappedDetails = groups.flatMap((g) =>
          (g.categories || []).map((cat) => ({
            group: g.groupName,
            value: `${g.groupCode}-${cat.code}`,
            label: cat.name,
          }))
        );

        setLocalCategoryGroups(mappedGroups);
        setLocalCategories(mappedDetails);
      } catch (err) {
        console.error('[フィルター] カテゴリーの読み込みに失敗しました:', err);
      }
    };

    fetchCategoryGroupsWithDetails();
  }, []);

  const allowedGroups = getCategoryGroupsForPage(pageName);

  return (
    <aside>
      <div className="mb-3 position-relative">
        <i
          className="bi bi-search position-absolute top-50 start-0 translate-middle-y ps-3"
          style={{ fontSize: '1.2rem', color: '#888' }}
        ></i>
        <input
          type="text"
          className="form-control ps-5"
          placeholder="キーワード検索"
          value={keyword || ''}
          onChange={(e) => onKeywordChange(e.target.value)}
          style={{ height: '40px' }}
        />
      </div>
      <div className="border rounded p-3">
        <h5
          className="fw-bold mb-2 d-flex align-items-center gap-2"
          style={{ cursor: 'pointer' }}
          onClick={() => setVisible((prev) => !prev)}
        >
          <i className="bi bi-filter"></i>
          {visible ? 'フィルターを非表示' : 'フィルターを表示'}
        </h5>
        <hr className="border-3 border-dark" />

        {visible && (
          <>
            <div className="d-flex justify-content-between align-items-center mb-2">
              <p className="mb-0">選択中のフィルター</p>
              <button className="btn btn-link p-0 text-danger" onClick={onReset}>
                リセット
              </button>
            </div>

            <div className="mb-3">
              {selectedFilters.map((filter) => {
                const match =
                  localCategories.find((cat) => cat.value === filter) ||
                  localCategoryGroups.find((group) => group.value === filter);
                const displayLabel = match?.label || filter;

                return (
                  <span
                    key={filter}
                    className="badge bg-secondary me-1"
                    style={{ cursor: 'pointer' }}
                    onClick={() => onFilterChange(filter)}
                  >
                    {displayLabel} ×
                  </span>
                );
              })}
            </div>

            <hr />
            <h5>商品情報</h5>
            <hr className="border-3 border-dark" />
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id="outOfStock"
                checked={isSelectedFilter('売り切れ商品を表示')}
                onChange={() => onFilterChange('売り切れ商品を表示')}
              />
              <label className="form-check-label" htmlFor="outOfStock">
                売り切れ商品を表示
              </label>
            </div>
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id="closedReservation"
                checked={isSelectedFilter('受付終了商品を表示')}
                onChange={() => onFilterChange('受付終了商品を表示')}
              />
              <label className="form-check-label" htmlFor="closedReservation">
                受付終了商品を表示
              </label>
            </div>
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id="endOfReservation"
                checked={isSelectedFilter('予約終了商品を除外')}
                onChange={() => onFilterChange('予約終了商品を除外')}
              />
              <label className="form-check-label" htmlFor="endOfReservation">
                予約終了商品を除外
              </label>
            </div>
            <hr />
            <h5>価格帯</h5>
            <hr className="border-3 border-dark" />
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id="priceOver10000"
                checked={isSelectedFilter('10,000円以上')}
                onChange={() => onFilterChange('10,000円以上')}
              />
              <label className="form-check-label" htmlFor="priceOver10000">
                10,000円以上
              </label>
            </div>
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id="priceOver50000"
                checked={isSelectedFilter('50,000円以上')}
                onChange={() => onFilterChange('50,000円以上')}
              />
              <label className="form-check-label" htmlFor="priceOver50000">
                50,000円以上
              </label>
            </div>
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                id="priceOver100000"
                checked={isSelectedFilter('100,000円以上')}
                onChange={() => onFilterChange('100,000円以上')}
              />
              <label className="form-check-label" htmlFor="priceOver100000">
                100,000円以上
              </label>
            </div>

            <hr />
            <h5>{categoryLabel}</h5>
            <hr className="border-3 border-dark" />
            {categoryOptions.map((groupLabel) => {
              if (!allowedGroups.includes(groupLabel)) {
                return null;
              }

              const groupItem = localCategoryGroups.find((g) => g.label === groupLabel);
              const groupCode = groupItem?.value ?? '';
              const childCategories = localCategories.filter((cat) => cat.group === groupLabel);
              const hasChildren = childCategories.length > 0;
              const isExpanded = expandedGroups[groupLabel] ?? false;
              const normalizedSelected = new Set(
                selectedFilters.map((f) => String(f ?? '').trim())
              );
              const childValues = childCategories.map((cat) => String(cat.value ?? '').trim());
              const areAllChildrenSelected =
                childValues.length > 0 && childValues.every((val) => normalizedSelected.has(val));
              const isGroupChecked =
                normalizedSelected.has(groupCode) ||
                (groupSelectionMode === 'include-children' && areAllChildrenSelected);

              return (
                <div key={groupLabel} className="mb-2">
                  <div className="d-flex align-items-center">
                    <span
                      className="me-2"
                      style={{ cursor: 'pointer', fontSize: '1.2rem' }}
                      onClick={() =>
                        setExpandedGroups((prev) => ({
                          ...prev,
                          [groupLabel]: !prev[groupLabel],
                        }))
                      }
                    >
                      <i
                        className={`bi ${isExpanded ? 'bi-caret-down-fill' : 'bi-caret-right-fill'}`}
                      ></i>
                    </span>
                    <div className="form-check m-0">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id={`category-${groupLabel}`}
                        checked={isGroupChecked}
                        onChange={() => {
                          const toggleValue = (value) => onFilterChange(String(value).trim());
                          const shouldSelect = !isGroupChecked;

                          if (shouldSelect) {
                            if (!normalizedSelected.has(groupCode)) {
                              toggleValue(groupCode);
                            }
                            if (groupSelectionMode === 'include-children') {
                              childValues.forEach((val) => {
                                if (!normalizedSelected.has(val)) {
                                  toggleValue(val);
                                }
                              });
                            }
                          } else {
                            if (normalizedSelected.has(groupCode)) {
                              toggleValue(groupCode);
                            }
                            if (groupSelectionMode === 'include-children') {
                              childValues.forEach((val) => {
                                if (normalizedSelected.has(val)) {
                                  toggleValue(val);
                                }
                              });
                            }
                          }
                        }}
                      />
                      <label className="form-check-label ms-1" htmlFor={`category-${groupLabel}`}>
                        {groupLabel}
                      </label>
                    </div>
                  </div>
                  {hasChildren && isExpanded && (
                    <div className="ps-5 mt-1">
                      {childCategories.map(({ value, label }) => {
                        const stringValue = String(value ?? '').trim();
                        const isChecked = normalizedSelected.has(stringValue);

                        return (
                          <div className="form-check" key={`cat-${stringValue}`}>
                            <input
                              className="form-check-input"
                              type="checkbox"
                              id={`category-${stringValue}`}
                              checked={isChecked}
                              onChange={(e) => {
                                e.stopPropagation();
                                onFilterChange(stringValue);
                              }}
                            />
                            <label className="form-check-label" htmlFor={`category-${stringValue}`}>
                              {label}
                            </label>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
            <hr />
          </>
        )}
      </div>
    </aside>
  );
}
