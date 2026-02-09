import { useState, useRef, useEffect, useCallback } from 'react';
import {
  DndContext,
  PointerSensor,
  TouchSensor,
  MouseSensor,
  useSensor,
  useSensors,
  closestCenter,
  DragOverlay,
} from '@dnd-kit/core';
import {
  SortableContext,
  useSortable,
  arrayMove,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import '../styles/CM_90_1043.css';
import axiosInstance from '../services/axiosInstance';
import CMMessage from '../constants/CMMessage';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1002 from '../components/commonPopup/CM_99_1002';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

function SortableItem({ id, children }) {
  const { setNodeRef, attributes, listeners, transform, transition, isDragging } = useSortable({
    id,
  });

  return (
    <div
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
        cursor: 'pointer',
        background: isDragging ? '#e6e6e6' : undefined,
      }}
      {...{ ...attributes, ...listeners }}
    >
      {children}
    </div>
  );
}

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

const useApiHandler = (openModal, closeModal) => {
  return useCallback(
    async (apiCall, showLoading = true) => {
      if (showLoading) openModal('loading');

      try {
        const result = await apiCall();
        return result;
      } catch (error) {
        openModal('error', CMMessage.MSG_ERR_001 || '오류가 발생했습니다.');
        return null;
      } finally {
        if (showLoading) closeModal('loading');
      }
    },
    [openModal, closeModal]
  );
};

// 메인 컴포넌트
export default function CM_90_1043() {
  // State 선언
  const [categoryTree, setCategoryTree] = useState({
    title: '카테고리 전체보기',
    isOpen: true,
    children: [],
  });
  const [newChildTitles, setNewChildTitles] = useState({});
  const [editingId, setEditingId] = useState(null);
  const [editedTitle, setEditedTitle] = useState('');
  const [prevTitle, setPrevTitle] = useState('');
  const [activeId, setActiveId] = useState(null);
  const [originalTree, setOriginalTree] = useState(() => JSON.parse(JSON.stringify(categoryTree)));
  const [pendingDeleteId, setPendingDeleteId] = useState(null);
  const [maxUpdatedAt, setMaxUpdatedAt] = useState(null);

  const scrollRef = useRef(null);
  const backupRef = useRef(JSON.parse(JSON.stringify(categoryTree)));

  const { modals, message, open, close } = useModal();
  const apiHandler = useApiHandler(open, close);

  // 드래그 센서 설정 (마우스, 터치 지원)
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(MouseSensor, { activationConstraint: { distance: 5 } }),
    useSensor(TouchSensor, { activationConstraint: { delay: 250, tolerance: 5 } })
  );

  // 초기 데이터 로드
  useEffect(() => {
    categoryAll();
  }, []);

  // 카테고리 데이터 조회
  const categoryAll = async () => {
    const result = await apiHandler(
      () => axiosInstance.get('/admin/category/findByAllCategory'),
      true
    );

    if (result.data.result) {
      const payload = result.data.resultList;
      if (payload) {
        const normalized = normalizeTree(payload);
        setCategoryTree(normalized);
        backupRef.current = JSON.parse(JSON.stringify(normalized));
        setOriginalTree(JSON.parse(JSON.stringify(normalized)));
        setMaxUpdatedAt(payload.maxUpdatedAt);
      }
    }
  };

  // 트리 구조로 변환
  const normalizeTree = (tree) => {
    if (!tree) {
      return { title: '카테고리 전체보기', isOpen: true, children: [] };
    }

    const root = {
      title: tree.title ?? '카테고리 전체보기',
      isOpen: typeof tree.isOpen === 'boolean' ? tree.isOpen : true,
      children: Array.isArray(tree.children) ? tree.children : [],
    };

    root.children = root.children.map((g, gi) => {
      const groupOriginalId = String(
        g.id ?? g.categoryGroupCode ?? `grp-${Date.now()}-${gi}-${Math.random()}`
      );
      const children = Array.isArray(g.children) ? g.children : [];

      const normalizedChildren = children.map((c, ci) => {
        const categoryOriginalId = String(
          c.id ?? c.categoryCode ?? `c-${Date.now()}-${gi}-${ci}-${Math.random()}`
        );
        const compositeId = `${groupOriginalId}-${categoryOriginalId}`;

        return {
          id: compositeId,
          originalId: categoryOriginalId,
          groupId: groupOriginalId,
          title: c.title ?? c.categoryName ?? '무명',
          order: typeof c.order === 'number' ? c.order : ci + 1,
          flag: typeof c.flag === 'number' ? c.flag : 0,
          children: Array.isArray(c.children) ? c.children : [],
        };
      });

      return {
        id: groupOriginalId,
        originalId: groupOriginalId,
        title: g.title ?? g.categoryGroupName ?? '무명 그룹',
        isOpen: typeof g.isOpen === 'boolean' ? g.isOpen : true,
        children: normalizedChildren,
        order: typeof g.order === 'number' ? g.order : gi + 1,
        flag: typeof g.flag === 'number' ? g.flag : 0,
      };
    });

    return root;
  };

  // ID로 항목 위치 찾기
  const findLocation = (id) => {
    const groups = categoryTree?.children ?? [];
    for (let i = 0; i < groups.length; i++) {
      const group = groups[i];
      if (group.id === id) return { type: 'group', groupIdx: i };

      const children = Array.isArray(group.children) ? group.children : [];
      const cIdx = children.findIndex((c) => c.id === id);
      if (cIdx !== -1) return { type: 'child', groupIdx: i, childIdx: cIdx };
    }
    return null;
  };

  // 순서 재정렬 및 flag 업데이트
  const updateOrder = (children) => {
    if (!Array.isArray(children)) return [];

    return children.map((child, idx) => ({
      ...child,
      order: idx + 1,
      flag: typeof child.flag === 'number' && child.flag !== 0 ? child.flag : 1,
      children: Array.isArray(child.children) ? updateOrder(child.children) : [],
    }));
  };

  // 전체보기 토글
  const toggleRoot = () => {
    setCategoryTree((prev) => ({
      ...prev,
      isOpen: !prev.isOpen,
      children: prev.children.map((g) => ({ ...g, isOpen: !prev.isOpen })),
    }));
  };

  // 특정 그룹 토글
  const toggleGroup = (idx) => {
    setCategoryTree((prev) => ({
      ...prev,
      children: prev.children.map((g, i) => (i === idx ? { ...g, isOpen: !g.isOpen } : g)),
    }));
  };

  // 하위 카테고리 추가
  const addChild = (groupIdx) => {
    const value = (newChildTitles[groupIdx] || '').trim();
    if (!value) return;

    if (titleExists(value, categoryTree)) {
      open('info', '이미 같은 이름의 카테고리가 존재합니다.');
      return;
    }

    setCategoryTree((prev) => {
      const arr = prev.children.map((g) => ({
        ...g,
        children: Array.isArray(g.children) ? [...g.children] : [],
      }));
      const group = arr[groupIdx];

      const tempId = `temp-${Date.now()}`;
      const compositeId = `${group.originalId || group.id}-${tempId}`;

      const newChild = {
        id: compositeId,
        originalId: tempId,
        groupId: group.originalId || group.id,
        title: value,
        order: (group.children?.length ?? 0) + 1,
        flag: 1,
      };

      group.children.push(newChild);
      return { ...prev, children: arr };
    });

    setNewChildTitles((prev) => ({ ...prev, [groupIdx]: '' }));
  };

  // 드래그 시작
  const handleDragStart = (event) => {
    setActiveId(event.active.id);
  };

  // 드래그 종료
  const handleDragEnd = (event) => {
    const { active, over } = event;
    setActiveId(null);
    if (!over) return;

    const from = findLocation(active.id);
    const to = findLocation(over.id);
    if (!from || !to) return;

    // 그룹 간 순서 변경
    if (from.type === 'group' && to.type === 'group') {
      setCategoryTree((prev) => {
        const arr = prev.children.map((g) => ({
          ...g,
          children: Array.isArray(g.children) ? [...g.children] : [],
        }));
        const moved = arrayMove(arr, from.groupIdx, to.groupIdx);
        const updatedChildren = updateOrder(moved);
        return { ...prev, children: updatedChildren };
      });
      return;
    }

    // 동일 그룹 내 카테고리 순서 변경
    if (from.type === 'child' && to.type === 'child' && from.groupIdx === to.groupIdx) {
      setCategoryTree((prev) => {
        const arr = prev.children.map((g) => ({
          ...g,
          children: Array.isArray(g.children) ? [...g.children] : [],
        }));
        arr[from.groupIdx].children = arrayMove(
          arr[from.groupIdx].children,
          from.childIdx,
          to.childIdx
        );
        arr[from.groupIdx].children = updateOrder(arr[from.groupIdx].children);
        return { ...prev, children: arr };
      });
      return;
    }
  };

  // 드래그 오버레이 렌더링
  const renderOverlayItem = () => {
    if (!activeId) return null;
    const loc = findLocation(activeId);
    if (!loc) return null;

    if (loc.type === 'group') {
      const grp = categoryTree.children[loc.groupIdx];
      return (
        <div className="bg-white border px-3 py-2 rounded shadow">
          <span style={{ marginRight: 8 }}>⠿</span>
          <span>{grp.title}</span>
        </div>
      );
    }

    if (loc.type === 'child') {
      const child = (categoryTree.children[loc.groupIdx].children || [])[loc.childIdx];
      return (
        <div className="bg-white border px-3 py-2 rounded shadow">
          <span style={{ marginRight: 8 }}>⠿</span>
          <span>{child?.title}</span>
        </div>
      );
    }
    return null;
  };

  // 편집 시작
  const handleEdit = (id, title) => {
    setEditingId(id);
    setPrevTitle(title);
    setEditedTitle(title);
  };

  // 편집 저장
  const saveEdit = () => {
    if (!editedTitle.trim()) return;

    if (titleExists(editedTitle, categoryTree, editingId)) {
      open('info', '이미 같은 이름의 카테고리가 존재합니다.');
      return;
    }

    setCategoryTree((prev) => {
      const updated = { ...prev, children: Array.isArray(prev.children) ? [...prev.children] : [] };
      for (let g of updated.children) {
        const c = (g.children || []).find((ch) => ch.id === editingId);
        if (c) {
          c.title = editedTitle.trim();
          c.flag = 1;
          break;
        }
      }
      return updated;
    });

    setEditingId(null);
    setEditedTitle('');
  };

  // 편집 취소
  const cancelEdit = () => {
    setEditingId(null);
    setEditedTitle('');
  };

  // 카테고리 삭제 - 확인 모달 열기
  const handleDelete = (id) => {
    setPendingDeleteId(id);
    open('confirm', '정말 삭제하시겠습니까?');
  };

  // 삭제 확인 처리
  const handleDeleteConfirm = () => {
    if (!pendingDeleteId) return;

    setCategoryTree((prev) => {
      const updated = { ...prev, children: Array.isArray(prev.children) ? [...prev.children] : [] };
      updated.children = updated.children.map((g) => {
        const newChildren = (g.children || []).map((c) =>
          c.id === pendingDeleteId ? { ...c, flag: 2 } : c
        );
        return { ...g, children: newChildren };
      });
      return updated;
    });

    setPendingDeleteId(null);
    close('confirm');
  };

  // 변경사항 저장
  const handleSave = async () => {
    try {
      // 하위 카테고리가 없는 그룹 검증
      const emptyGroups = categoryTree.children
        .filter((g) => g.flag !== 2)
        .filter((g) => {
          const activeChildren = (g.children || []).filter((c) => c.flag !== 2);
          return activeChildren.length === 0;
        });

      if (emptyGroups.length > 0) {
        const groupNames = emptyGroups.map((g) => `"${g.title}"`).join(', ');
        open(
          'info',
          `다음 그룹에 하위 카테고리가 없습니다:\n${groupNames}\n\n` +
            `각 그룹에는 최소 1개 이상의 하위 카테고리가 필요합니다.`
        );
        return;
      }

      // 변경사항 수집
      const changes = collectChanges(categoryTree);

      if (
        changes.created.length === 0 &&
        changes.updated.length === 0 &&
        changes.deleted.length === 0
      ) {
        open('info', '변경된 내용이 없습니다.');
        return;
      }

      const requestData = {
        ...changes,
        maxUpdatedAt: maxUpdatedAt,
      };

      // 백엔드 전송
      const result = await apiHandler(() =>
        axiosInstance.post('/admin/category/save', requestData)
      );

      if (result.data.result) {
        await categoryAll();
        open('info', CMMessage.MSG_INF_001);
      } else {
        open('error', result.data.message || '저장에 실패했습니다.');
      }
    } catch (err) {
      open('error', `저장 중 오류가 발생했습니다: ${err.response?.data?.message || err.message}`);
    }
  };

  // 변경사항 수집 (created, updated, deleted 분류)
  const collectChanges = (tree) => {
    const created = [];
    const updated = [];
    const deleted = [];
    const originalParentMap = new Map();

    // 원본 트리에서 부모 관계 매핑
    const buildOriginalMap = (node, parentId = null) => {
      if (!node) return;
      originalParentMap.set(node.id, parentId);

      if (Array.isArray(node.children)) {
        node.children.forEach((child) => buildOriginalMap(child, node.id));
      }
    };

    if (originalTree.children && Array.isArray(originalTree.children)) {
      originalTree.children.forEach((group) => buildOriginalMap(group, null));
    }

    // 트리 순회하며 변경사항 수집
    const processNode = (node, currentParentId = null, depth = 0) => {
      if (!node) return;

      const isGroup = depth === 0;
      const compositeKey = node.id;
      const actualCategoryCode = node.originalId;

      // 임시 ID 체크 (타임스탬프 포함된 ID)
      const isTemporaryId =
        typeof actualCategoryCode === 'string' &&
        (actualCategoryCode.startsWith('grp-') ||
          actualCategoryCode.startsWith('temp-') ||
          (actualCategoryCode.startsWith('c-') && actualCategoryCode.includes('-')));

      // 삭제된 항목
      if (node.flag === 2) {
        if (!isTemporaryId) {
          const originalParentId = originalParentMap.get(compositeKey);
          deleted.push({
            id: actualCategoryCode,
            parentId: isGroup ? null : originalParentId,
            type: isGroup ? 'group' : 'category',
          });
        }
      }
      // 변경된 항목
      else if (node.flag === 1) {
        const originalParentId = originalParentMap.get(compositeKey);
        const currentGroupId = node.groupId || currentParentId;
        const isNewItem = originalParentId === undefined;

        // 신규 생성
        if (isTemporaryId || isNewItem) {
          created.push({
            title: node.title,
            order: node.order,
            parentId: currentGroupId,
            type: isGroup ? 'group' : 'category',
          });
        }
        // 기존 항목 수정
        else {
          updated.push({
            id: actualCategoryCode,
            title: node.title,
            order: node.order,
            parentId: originalParentId,
            newParentId: currentGroupId,
            type: isGroup ? 'group' : 'category',
          });
        }
      }

      // 자식 노드 처리
      if (Array.isArray(node.children) && node.children.length > 0) {
        node.children.forEach((child) => {
          processNode(child, compositeKey, depth + 1);
        });
      }
    };

    if (tree.children && Array.isArray(tree.children)) {
      tree.children.forEach((group) => {
        processNode(group, null, 0);
      });
    }

    return { created, updated, deleted };
  };

  // 초기화
  const handleCancel = async () => {
    await categoryAll();
    setEditingId(null);
    setEditedTitle('');
  };

  // 모달 닫기 핸들러
  const handleModalClose = useCallback(() => {
    close('info');
  }, [close]);

  // 카테고리명 중복 체크
  const titleExists = (title, tree, ignoreId = null) => {
    const stack = [tree];
    const trimmed = title.trim();

    while (stack.length) {
      const node = stack.pop();
      if (!node) continue;

      if (node.id !== ignoreId && node.flag !== 2 && node.title?.trim() === trimmed) {
        return true;
      }

      if (Array.isArray(node.children) && node.children.length) {
        stack.push(...node.children);
      }
    }
    return false;
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-4" style={{ width: '100%' }}>
        <h5 className="border-bottom pb-2 ps-4 mb-4">카테고리 관리</h5>
        <h6 className="fw-semibold ps-4 mb-1">
          카테고리 순서를 변경하고 주제 연결을 설정할 수 있습니다.
        </h6>
        <p className="text-muted ps-4 mb-4">
          드래그 앤 드롭으로 카테고리 순서를 변경할 수 있습니다.
        </p>

        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onDragCancel={() => setActiveId(null)}
        >
          <div className="category-container">
            <div className="category-scroll" ref={scrollRef}>
              {/* 루트 */}
              <div
                className="d-flex align-items-center bg-white py-2 px-3 border mb-1"
                style={{ cursor: 'pointer', userSelect: 'none' }}
                onClick={toggleRoot}
              >
                <span className="me-2">⠿</span>
                <strong>{categoryTree.title}</strong>
              </div>

              {/* 그룹 + 하위 카테고리 */}
              <div className={`ps-3 fade-slide ${categoryTree.isOpen ? 'open' : ''}`}>
                <SortableContext
                  items={(categoryTree.children || []).filter((g) => g.flag !== 2).map((g) => g.id)}
                  strategy={verticalListSortingStrategy}
                >
                  {(categoryTree.children || [])
                    .filter((grp) => grp.flag !== 2)
                    .map((grp, grpIdx) => (
                      <div key={grp.id} className="mb-2">
                        {/* 그룹 */}
                        <SortableItem id={grp.id}>
                          <div
                            className="item-content d-flex align-items-center bg-white py-2 px-2 border"
                            style={{ cursor: 'pointer', userSelect: 'none' }}
                            onClick={(e) => {
                              e.stopPropagation();
                              toggleGroup(grpIdx);
                            }}
                          >
                            <span className="me-2">⠿</span>
                            <span style={{ flex: 1 }}>{grp.title}</span>
                          </div>
                        </SortableItem>

                        {/* 하위 카테고리 */}
                        {grp.isOpen && (
                          <div className="ps-4 fade-slide open">
                            <SortableContext
                              items={(grp.children || [])
                                .filter((c) => c.flag !== 2)
                                .map((c) => c.id)}
                              strategy={verticalListSortingStrategy}
                            >
                              {(grp.children || [])
                                .filter((child) => child.flag !== 2)
                                .map((child) => (
                                  <SortableItem key={child.id} id={child.id}>
                                    <div
                                      className="item-content d-flex align-items-center bg-white rounded p-2 mb-1 mt-1"
                                      style={{ cursor: 'pointer', userSelect: 'none' }}
                                    >
                                      <span className="me-2">⠿</span>

                                      {/* 편집 모드 */}
                                      {editingId === child.id ? (
                                        <div className="input-group flex-grow-1">
                                          <input
                                            className="form-control"
                                            value={editedTitle}
                                            onChange={(e) => setEditedTitle(e.target.value)}
                                            onKeyDown={(e) => e.key === 'Enter' && saveEdit()}
                                            autoFocus
                                          />
                                          <button
                                            className="btn btn-sm btn-primary me-1"
                                            style={{ width: 100 }}
                                            onClick={saveEdit}
                                          >
                                            저장
                                          </button>
                                          <button
                                            className="btn btn-sm btn-secondary"
                                            style={{ width: 100 }}
                                            onClick={cancelEdit}
                                          >
                                            취소
                                          </button>
                                        </div>
                                      ) : (
                                        <span>{child.title}</span>
                                      )}

                                      {/* 편집/삭제 아이콘 */}
                                      {editingId !== child.id && (
                                        <div className="hover-icons ms-3" style={{ opacity: 0.5 }}>
                                          <i
                                            className="bi bi-pencil-square me-2"
                                            title="편집"
                                            onClick={() => handleEdit(child.id, child.title)}
                                            onMouseEnter={(e) =>
                                              (e.currentTarget.style.color = '#0d6efd')
                                            }
                                            onMouseLeave={(e) =>
                                              (e.currentTarget.style.color = '#6c757d')
                                            }
                                          />
                                          <i
                                            className="bi bi-trash"
                                            title="삭제"
                                            onClick={() => handleDelete(child.id)}
                                            onMouseEnter={(e) =>
                                              (e.currentTarget.style.color = '#dc3545')
                                            }
                                            onMouseLeave={(e) =>
                                              (e.currentTarget.style.color = '#6c757d')
                                            }
                                          />
                                        </div>
                                      )}
                                    </div>
                                  </SortableItem>
                                ))}
                            </SortableContext>

                            {/* 카테고리 추가 입력 */}
                            <div className="d-flex align-items-center my-2">
                              <div className="input-with-button-wrapper">
                                <button
                                  className="input-inside-btn-left"
                                  type="button"
                                  onClick={() => addChild(grpIdx)}
                                >
                                  <i className="bi bi-plus" />
                                </button>
                                <input
                                  type="text"
                                  className="form-control"
                                  placeholder="하위 카테고리 추가"
                                  value={newChildTitles[grpIdx] || ''}
                                  onChange={(e) =>
                                    setNewChildTitles((prev) => ({
                                      ...prev,
                                      [grpIdx]: e.target.value,
                                    }))
                                  }
                                  onKeyDown={(e) => {
                                    if (e.key === 'Enter') addChild(grpIdx);
                                  }}
                                />
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                </SortableContext>
              </div>
            </div>
          </div>

          <DragOverlay>{renderOverlayItem()}</DragOverlay>

          {/* 저장/초기화 버튼 */}
          <div className="mt-4 d-flex gap-2 justify-content-center">
            <button className="btn btn-primary px-4" onClick={handleSave}>
              저장
            </button>
            <button className="btn btn-secondary px-4" onClick={handleCancel}>
              초기화
            </button>
          </div>
        </DndContext>

        {/* 모달 컴포넌트들 */}
        <CM_99_1001
          isOpen={modals.confirm}
          onClose={() => {
            setPendingDeleteId(null);
            close('confirm');
          }}
          onConfirm={handleDeleteConfirm}
          Message={message}
        />
        <CM_99_1002 isOpen={modals.loading} onClose={() => close('loading')} />
        <CM_99_1003 isOpen={modals.error} onClose={() => close('error')} message={message} />
        <CM_99_1004 isOpen={modals.info} onClose={handleModalClose} Message={message} />
      </div>
    </div>
  );
}
