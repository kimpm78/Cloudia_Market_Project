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
        openModal('error', CMMessage.MSG_ERR_001 || 'エラーが発生しました。');
        return null;
      } finally {
        if (showLoading) closeModal('loading');
      }
    },
    [openModal, closeModal]
  );
};

// メインコンポーネント
export default function CM_90_1043() {
  // State 宣言
  const [categoryTree, setCategoryTree] = useState({
    title: 'カテゴリ全体表示',
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

  // ドラッグセンサー設定（マウス、タッチ対応）
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(MouseSensor, { activationConstraint: { distance: 5 } }),
    useSensor(TouchSensor, { activationConstraint: { delay: 250, tolerance: 5 } })
  );

  // 初期データロード
  useEffect(() => {
    categoryAll();
  }, []);

  // カテゴリデータ取得
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

  // ツリー構造へ変換
  const normalizeTree = (tree) => {
    if (!tree) {
      return { title: 'カテゴリ全体表示', isOpen: true, children: [] };
    }

    const root = {
      title: tree.title ?? 'カテゴリ全体表示',
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
          title: c.title ?? c.categoryName ?? '名称未設定',
          order: typeof c.order === 'number' ? c.order : ci + 1,
          flag: typeof c.flag === 'number' ? c.flag : 0,
          children: Array.isArray(c.children) ? c.children : [],
        };
      });

      return {
        id: groupOriginalId,
        originalId: groupOriginalId,
        title: g.title ?? g.categoryGroupName ?? '名称未設定グループ',
        isOpen: typeof g.isOpen === 'boolean' ? g.isOpen : true,
        children: normalizedChildren,
        order: typeof g.order === 'number' ? g.order : gi + 1,
        flag: typeof g.flag === 'number' ? g.flag : 0,
      };
    });

    return root;
  };

  // IDで項目の位置を検索
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

  // 順序の再採番およびflag更新
  const updateOrder = (children) => {
    if (!Array.isArray(children)) return [];

    return children.map((child, idx) => ({
      ...child,
      order: idx + 1,
      flag: typeof child.flag === 'number' && child.flag !== 0 ? child.flag : 1,
      children: Array.isArray(child.children) ? updateOrder(child.children) : [],
    }));
  };

  // 全体表示のトグル
  const toggleRoot = () => {
    setCategoryTree((prev) => ({
      ...prev,
      isOpen: !prev.isOpen,
      children: prev.children.map((g) => ({ ...g, isOpen: !prev.isOpen })),
    }));
  };

  // 特定グループのトグル
  const toggleGroup = (idx) => {
    setCategoryTree((prev) => ({
      ...prev,
      children: prev.children.map((g, i) => (i === idx ? { ...g, isOpen: !g.isOpen } : g)),
    }));
  };

  // 下位カテゴリ追加
  const addChild = (groupIdx) => {
    const value = (newChildTitles[groupIdx] || '').trim();
    if (!value) return;

    if (titleExists(value, categoryTree)) {
      open('info', 'すでに同じ名前のカテゴリが存在します。');
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

  // ドラッグ開始
  const handleDragStart = (event) => {
    setActiveId(event.active.id);
  };

  // ドラッグ終了
  const handleDragEnd = (event) => {
    const { active, over } = event;
    setActiveId(null);
    if (!over) return;

    const from = findLocation(active.id);
    const to = findLocation(over.id);
    if (!from || !to) return;

    // グループ間の順序変更
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

    // 同一グループ内のカテゴリ順序変更
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

  // ドラッグオーバーレイ描画
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

  // 編集開始
  const handleEdit = (id, title) => {
    setEditingId(id);
    setPrevTitle(title);
    setEditedTitle(title);
  };

  // 編集保存
  const saveEdit = () => {
    if (!editedTitle.trim()) return;

    if (titleExists(editedTitle, categoryTree, editingId)) {
      open('info', 'すでに同じ名前のカテゴリが存在します。');
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

  // 編集キャンセル
  const cancelEdit = () => {
    setEditingId(null);
    setEditedTitle('');
  };

  // カテゴリ削除 - 確認モーダルを開く
  const handleDelete = (id) => {
    setPendingDeleteId(id);
    open('confirm', '本当に削除しますか？');
  };

  // 削除確認処理
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

  // 変更内容保存
  const handleSave = async () => {
    try {
      // 下位カテゴリがないグループの検証
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
          `次のグループには下位カテゴリがありません:\n${groupNames}\n\n` +
            `各グループには最低1件以上の下位カテゴリが必要です。`
        );
        return;
      }

      // 変更内容収集
      const changes = collectChanges(categoryTree);

      if (
        changes.created.length === 0 &&
        changes.updated.length === 0 &&
        changes.deleted.length === 0
      ) {
        open('info', '変更された内容がありません。');
        return;
      }

      const requestData = {
        ...changes,
        maxUpdatedAt: maxUpdatedAt,
      };

      // バックエンド送信
      const result = await apiHandler(() =>
        axiosInstance.post('/admin/category/save', requestData)
      );

      if (result.data.result) {
        await categoryAll();
        open('info', CMMessage.MSG_INF_001);
      } else {
        open('error', result.data.message || '保存に失敗しました。');
      }
    } catch (err) {
      open('error', `保存中にエラーが発生しました: ${err.response?.data?.message || err.message}`);
    }
  };

  // 変更内容収集（created, updated, deleted の分類）
  const collectChanges = (tree) => {
    const created = [];
    const updated = [];
    const deleted = [];
    const originalParentMap = new Map();

    // 元ツリーから親子関係をマッピング
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

    // ツリーを走査して変更内容を収集
    const processNode = (node, currentParentId = null, depth = 0) => {
      if (!node) return;

      const isGroup = depth === 0;
      const compositeKey = node.id;
      const actualCategoryCode = node.originalId;

      // 一時IDチェック（タイムスタンプを含むID）
      const isTemporaryId =
        typeof actualCategoryCode === 'string' &&
        (actualCategoryCode.startsWith('grp-') ||
          actualCategoryCode.startsWith('temp-') ||
          (actualCategoryCode.startsWith('c-') && actualCategoryCode.includes('-')));

      // 削除された項目
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
      // 変更された項目
      else if (node.flag === 1) {
        const originalParentId = originalParentMap.get(compositeKey);
        const currentGroupId = node.groupId || currentParentId;
        const isNewItem = originalParentId === undefined;

        // 新規作成
        if (isTemporaryId || isNewItem) {
          created.push({
            title: node.title,
            order: node.order,
            parentId: currentGroupId,
            type: isGroup ? 'group' : 'category',
          });
        }
        // 既存項目修正
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

      // 子ノード処理
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

  // 初期化
  const handleCancel = async () => {
    await categoryAll();
    setEditingId(null);
    setEditedTitle('');
  };

  // モーダルを閉じるハンドラー
  const handleModalClose = useCallback(() => {
    close('info');
  }, [close]);

  // カテゴリ名の重複チェック
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
        <h2 className="border-bottom pb-2 ps-4 mb-4">カテゴリ管理</h2>
        <h6 className="fw-semibold ps-4 mb-1">
          カテゴリの順序変更や、テーマ連携の設定ができます。
        </h6>
        <p className="text-muted ps-4 mb-4">
          ドラッグ＆ドロップでカテゴリの順序を変更できます。
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
              {/* ルート */}
              <div
                className="d-flex align-items-center bg-white py-2 px-3 border mb-1"
                style={{ cursor: 'pointer', userSelect: 'none' }}
                onClick={toggleRoot}
              >
                <span className="me-2">⠿</span>
                <strong>{categoryTree.title}</strong>
              </div>

              {/* グループ + 下位カテゴリ */}
              <div className={`ps-3 fade-slide ${categoryTree.isOpen ? 'open' : ''}`}>
                <SortableContext
                  items={(categoryTree.children || []).filter((g) => g.flag !== 2).map((g) => g.id)}
                  strategy={verticalListSortingStrategy}
                >
                  {(categoryTree.children || [])
                    .filter((grp) => grp.flag !== 2)
                    .map((grp, grpIdx) => (
                      <div key={grp.id} className="mb-2">
                        {/* グループ */}
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

                        {/* 下位カテゴリ */}
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

                                      {/* 編集モード */}
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
                                            保存
                                          </button>
                                          <button
                                            className="btn btn-sm btn-secondary"
                                            style={{ width: 100 }}
                                            onClick={cancelEdit}
                                          >
                                            キャンセル
                                          </button>
                                        </div>
                                      ) : (
                                        <span>{child.title}</span>
                                      )}

                                      {/* 編集/削除アイコン */}
                                      {editingId !== child.id && (
                                        <div className="hover-icons ms-3" style={{ opacity: 0.5 }}>
                                          <i
                                            className="bi bi-pencil-square me-2"
                                            title="編集"
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
                                            title="削除"
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

                            {/* カテゴリ追加入力 */}
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
                                  placeholder="下位カテゴリを追加"
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

          {/* 保存/初期化ボタン */}
          <div className="mt-4 d-flex gap-2 justify-content-center">
            <button className="btn btn-primary px-4" onClick={handleSave}>
              保存
            </button>
            <button className="btn btn-secondary px-4" onClick={handleCancel}>
              初期化
            </button>
          </div>
        </DndContext>

        {/* モーダルコンポーネント */}
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
