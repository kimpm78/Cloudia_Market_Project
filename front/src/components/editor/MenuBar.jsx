import { useState, useRef, useEffect } from 'react';
import { useCurrentEditor } from '@tiptap/react';
import axiosInstance from '../../services/axiosInstance';
import './toolbar.css';

import { HexColorPicker } from 'react-colorful';

const MenuBar = ({ field, onImageAdd }) => {
  const { editor } = useCurrentEditor();
  const [color, setColor] = useState('#958DF1');

  // テーブルグリッド選択の状態および参照(ref)
  const [showTableGrid, setShowTableGrid] = useState(false);
  const [tableSize, setTableSize] = useState({ rows: 0, cols: 0 });
  const gridRef = useRef(null);

  // 事前定義されたカラーパレット
  const presetColors = [
    '#000000',
    '#FF0000',
    '#00FF00',
    '#0000FF',
    '#FFFF00',
    '#FFFFFF',
    '#958DF1',
  ];

  // カラーパレット
  const [showColorPalette, setShowColorPalette] = useState(false);
  const paletteRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (paletteRef.current && !paletteRef.current.contains(event.target)) {
        setShowColorPalette(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // テーブル選択グリッド領域の外をクリックするとグリッドを閉じる
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (gridRef.current && !gridRef.current.contains(event.target)) {
        setShowTableGrid(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // リンク
  const [showLinkModal, setShowLinkModal] = useState(false);
  const [linkUrl, setLinkUrl] = useState('');
  const [linkText, setLinkText] = useState('');
  const [linkError, setLinkError] = useState('');
  const [linkTargetType, setLinkTargetType] = useState('text');
  const modalRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showLinkModal && modalRef.current && !modalRef.current.contains(event.target)) {
        setShowLinkModal(false);
        setLinkUrl('');
        setLinkText('');
        setLinkError('');
        setLinkTargetType('text');
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showLinkModal]);

  const fileInputRef = useRef();

  const insertImage = (src) => {
    if (!src) return;
    editor
      .chain()
      .focus()
      .insertContent([{ type: 'image', attrs: { src } }, { type: 'paragraph' }])
      .run();
  };

  const handleImageUpload = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    const inputEl = e.target;
    const file = inputEl.files?.[0];
    if (!file) {
      if (inputEl) inputEl.value = '';
      return;
    }

    try {
      if (field === 'product') {
        const formData = new FormData();
        formData.append('file', file);
        const result = await axiosInstance.post('/admin/product/image/upload', formData);
        if (result.data.result) {
          const absoluteUrl = `${import.meta.env.VITE_API_BASE_IMAGE_URL}${result.data.resultList}`;
          if (onImageAdd) {
            onImageAdd(result);
          }
          insertImage(absoluteUrl);
        }
      } else {
        let uploadedUrl = null;
        if (onImageAdd) {
          uploadedUrl = await onImageAdd(file);
        }
        if (uploadedUrl) {
          insertImage(uploadedUrl);
        } else {
          const reader = new FileReader();
          reader.onload = () => {
            if (typeof reader.result === 'string') {
              insertImage(reader.result);
            }
          };
          reader.readAsDataURL(file);
        }
      }
    } finally {
      if (inputEl) inputEl.value = '';
    }
  };

  if (!editor) {
    return null;
  }

  const undoRedo = [
    {
      label: 'Undo',
      icon: '<i class="bi bi-arrow-counterclockwise"></i>',
      command: () => editor.chain().focus().undo().run(),
      disabled: () => !editor.can().chain().focus().undo().run(),
    },
    {
      label: 'Redo',
      icon: '<i class="bi bi-arrow-clockwise"></i>',
      command: () => editor.chain().focus().redo().run(),
      disabled: () => !editor.can().chain().focus().redo().run(),
    },
    { divider: true },
  ];

  // 書式オプショングループ
  const formattingOptions = [
    {
      label: 'Bold',
      icon: '<i class="bi bi-type-bold"></i>',
      command: () => editor.chain().focus().toggleBold().run(),
      isActive: () => editor.isActive('bold'),
      disabled: () => !editor.can().chain().focus().toggleBold().run(),
    },
    {
      label: 'Italic',
      icon: '<i class="bi bi-type-italic"></i>',
      command: () => editor.chain().focus().toggleItalic().run(),
      isActive: () => editor.isActive('italic'),
      disabled: () => !editor.can().chain().focus().toggleItalic().run(),
    },
    {
      label: 'Underline',
      icon: '<i class="bi bi-type-underline"></i>',
      command: () => editor.chain().focus().toggleUnderline().run(),
      isActive: () => editor.isActive('underline'),
      disabled: () => !editor.can().chain().focus().toggleUnderline().run(),
    },
    {
      label: 'Strike',
      icon: '<i class="bi bi-type-strikethrough"></i>',
      command: () => editor.chain().focus().toggleStrike().run(),
      isActive: () => editor.isActive('strike'),
      disabled: () => !editor.can().chain().focus().toggleStrike().run(),
    },
  ];

  const Options = [
    ...formattingOptions,
    { divider: true },
    {
      label: 'Highlight',
      icon: '<i class="bi bi-highlighter"></i>',
      command: () => editor.chain().focus().toggleHighlight().run(),
      isActive: () => editor.isActive('highlight'),
      disabled: () => !editor.can().chain().focus().toggleHighlight().run(),
    },
    {
      label: 'Code',
      icon: '<i class="bi bi-code-slash"></i>',
      command: () => editor.chain().focus().toggleCode().run(),
      isActive: () => editor.isActive('code'),
      disabled: () => !editor.can().chain().focus().toggleCode().run(),
    },
    {
      label: 'Code block',
      icon: '<i class="bi bi-file-code"></i>',
      command: () => editor.chain().focus().toggleCodeBlock().run(),
      isActive: () => editor.isActive('codeBlock'),
      disabled: () => !editor.can().chain().focus().toggleCodeBlock().run(),
    },
    { divider: true },
    {
      label: 'Bullet list',
      icon: '<i class="bi bi-list-ul"></i>',
      command: () => editor.chain().focus().toggleBulletList().run(),
      isActive: () => editor.isActive('bulletList'),
    },
    {
      label: 'Ordered list',
      icon: '<i class="bi bi-list-ol"></i>',
      command: () => editor.chain().focus().toggleOrderedList().run(),
      isActive: () => editor.isActive('orderedList'),
    },
    {
      label: 'Task',
      icon: '<i class="bi-ui-checks"></i>',
      command: () => editor.chain().focus().toggleTaskList().run(),
      isActive: () => editor.isActive('taskList'),
    },
    {
      label: 'Blockquote',
      icon: '<i class="bi bi-blockquote-left"></i>',
      command: () => editor.chain().focus().toggleBlockquote().run(),
      isActive: () => editor.isActive('blockquote'),
    },
    // 整列機能
    {
      label: 'Align Left',
      icon: '<i class="bi bi-text-left"></i>',
      command: () => editor.chain().focus().setTextAlign('left').run(),
      isActive: () =>
        editor.isActive({ textAlign: 'left' }) ||
        (editor.isActive('image') &&
          editor.getAttributes('image').style?.includes('text-align: left')),
    },
    {
      label: 'Align Center',
      icon: '<i class="bi bi-text-center"></i>',
      command: () => editor.chain().focus().setTextAlign('center').run(),
      isActive: () =>
        editor.isActive({ textAlign: 'center' }) ||
        (editor.isActive('image') &&
          editor.getAttributes('image').style?.includes('text-align: center')),
    },
    {
      label: 'Align Right',
      icon: '<i class="bi bi-text-right"></i>',
      command: () => editor.chain().focus().setTextAlign('right').run(),
      isActive: () =>
        editor.isActive({ textAlign: 'right' }) ||
        (editor.isActive('image') &&
          editor.getAttributes('image').style?.includes('text-align: right')),
    },
    { divider: true },
    {
      label: 'Table',
      icon: '<i class="bi bi-table"></i>',
      command: () => setShowTableGrid((prev) => !prev),
    },
    {
      label: 'Image',
      icon: '<i class="bi bi-image"></i>',
      command: () => fileInputRef.current?.click(),
    },
    {
      label: 'Link',
      icon: '<i class="bi bi-link-45deg"></i>',
      command: () => {
        if (!editor) return;
        if (editor.isActive('image')) {
          const imageAttrs = editor.getAttributes('image');
          setLinkUrl(imageAttrs.linkHref || '');
          setLinkText('');
          setLinkTargetType('image');
        } else {
          const { from, to } = editor.state.selection;
          if (from !== to) {
            const selectedText = editor.state.doc.textBetween(from, to, ' ');
            setLinkText(selectedText);
          } else {
            setLinkText('');
          }
          setLinkUrl('');
          setLinkTargetType('text');
        }
        setLinkError('');
        setShowLinkModal(true);
      },
    },
  ];

  const primaryLabels = new Set([
    'Bold',
    'Italic',
    'Underline',
    'Strike',
    'Bullet list',
    'Ordered list',
    'Align Left',
    'Align Center',
    'Align Right',
    'Image',
    'Link',
  ]);

  const compactDividers = (items) => {
    const result = [];
    for (const item of items) {
      if (item.divider) {
        if (result.length === 0) continue;
        if (result[result.length - 1].divider) continue;
      }
      result.push(item);
    }
    while (result.length && result[result.length - 1].divider) {
      result.pop();
    }
    return result;
  };

  const primaryOptions = compactDividers(
    Options.filter((item) => item.divider || primaryLabels.has(item.label))
  );

  const overflowOptions = compactDividers(
    Options.filter((item) => item.divider || !primaryLabels.has(item.label))
  );

  return (
    <>
      <div className="control-group border rounded p-1 mb-1 bg-white d-flex align-items-start gap-2">
        {/* 元に戻す/やり直しボタン */}
        {undoRedo.map(({ label, command, isActive, disabled, icon, divider }, index) =>
          divider ? (
            <div key={`divider-undo-${index}`} className="vr mx-1"></div>
          ) : (
            <div key={label} className="tooltip-wrapper">
              <button
                type="button"
                onClick={command}
                className={`btn btn-outline-dark ${isActive?.() ? 'active' : ''}`}
                disabled={disabled?.()}
              >
                <span dangerouslySetInnerHTML={{ __html: icon }} />
              </button>
              <div className="custom-tooltip">{label}</div>
            </div>
          )
        )}
        {/* 見出しドロップダウン */}
        <div className="tooltip-wrapper dropdown">
          <button
            type="button"
            className="btn btn-outline-dark dropdown-toggle"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <span
              dangerouslySetInnerHTML={{
                __html: editor.isActive('heading', { level: 1 })
                  ? '<i class="bi bi-type-h1"></i>'
                  : editor.isActive('heading', { level: 2 })
                    ? '<i class="bi bi-type-h2"></i>'
                    : editor.isActive('heading', { level: 3 })
                      ? '<i class="bi bi-type-h3"></i>'
                      : editor.isActive('heading', { level: 4 })
                        ? '<i class="bi bi-type-h4"></i>'
                        : editor.isActive('heading', { level: 5 })
                          ? '<i class="bi bi-type-h5"></i>'
                          : editor.isActive('heading', { level: 6 })
                            ? '<i class="bi bi-type-h6"></i>'
                            : '<i class="bi bi-type-h1"></i>',
              }}
            />
          </button>
          <div className="custom-tooltip">Heading</div>
          <ul className="dropdown-menu p-0">
            {[1, 2, 3, 4, 5, 6].map((level) => (
              <li key={level}>
                <button
                  type="button"
                  className={`dropdown-item d-flex align-items-center ${
                    editor.isActive('heading', { level }) ? 'active' : ''
                  }`}
                  onClick={() => editor.chain().focus().toggleHeading({ level }).run()}
                >
                  <i className={`bi bi-type-h${level} me-2`}></i> Heading {level}
                </button>
              </li>
            ))}
          </ul>
        </div>

        <input
          type="file"
          accept="image/*"
          ref={fileInputRef}
          onChange={(e) => {
            handleImageUpload(e);
            e.target.value = null;
          }}
          style={{ display: 'none' }}
        />

        <div className="position-relative me-1">
          <div className="tooltip-wrapper">
            <span>
              <button
                type="button"
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  if (e.nativeEvent?.stopImmediatePropagation) {
                    e.nativeEvent.stopImmediatePropagation();
                  }
                  setShowColorPalette((prev) => !prev);
                  return false;
                }}
                className="btn btn-outline-dark"
                title="Color Picker"
              >
                <i className="bi bi-palette"></i>
              </button>
            </span>
            <div className="custom-tooltip">Color Picker</div>
          </div>
          {showColorPalette && (
            <div
              ref={paletteRef}
              className="position-absolute start-50 translate-middle-x mt-2 bg-white p-2 rounded shadow-sm"
              style={{ zIndex: 10 }}
            >
              <HexColorPicker
                color={color}
                onChange={(updatedColor) => {
                  setColor(updatedColor);
                  editor.chain().focus().setColor(updatedColor).run();
                }}
              />
              <div className="d-flex flex-wrap gap-1 mt-2">
                {presetColors.map((preset) => (
                  <button
                    key={preset}
                    style={{
                      backgroundColor: preset,
                      width: '24px',
                      height: '24px',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                    }}
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      if (e.nativeEvent?.stopImmediatePropagation) {
                        e.nativeEvent.stopImmediatePropagation();
                      }
                      setColor(preset);
                      editor.chain().focus().setColor(preset).run();
                    }}
                    title={preset}
                  />
                ))}
              </div>
            </div>
          )}
        </div>

        {/* 定義された順序で残りのツールバーボタンをレンダリング */}
        {primaryOptions.map(({ label, command, isActive, disabled, icon, divider }, index) =>
          divider ? (
            <div key={`divider-${index}`} className="vr mx-1"></div>
          ) : (
            <div key={label} className="tooltip-wrapper">
              <button
                type="button"
                onClick={command}
                className={`btn btn-outline-dark ${isActive?.() ? 'active' : ''}`}
                disabled={disabled?.()}
              >
                <span dangerouslySetInnerHTML={{ __html: icon }} />
              </button>
              <div className="custom-tooltip">{label}</div>
            </div>
          )
        )}
        {overflowOptions.length > 0 && (
          <div className="tooltip-wrapper dropdown">
            <button
              type="button"
              className="btn btn-outline-dark dropdown-toggle"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              <i className="bi bi-three-dots"></i>
            </button>
            <div className="custom-tooltip">More</div>
            <ul className="dropdown-menu p-0">
              {overflowOptions.map(({ label, command, isActive, disabled, icon, divider }, index) =>
                divider ? (
                  <li key={`more-divider-${index}`}>
                    <hr className="dropdown-divider m-0" />
                  </li>
                ) : (
                  <li key={`more-${label}`}>
                    <button
                      type="button"
                      className={`dropdown-item d-flex align-items-center gap-2 ${
                        isActive?.() ? 'active' : ''
                      }`}
                      onClick={command}
                      disabled={disabled?.()}
                    >
                      <span dangerouslySetInnerHTML={{ __html: icon }} />
                      {label}
                    </button>
                  </li>
                )
              )}
            </ul>
          </div>
        )}
      </div>
      {showTableGrid && (
        <div
          ref={gridRef}
          className="position-absolute bg-white border p-2 shadow"
          style={{ zIndex: 20, left: '49%' }}
        >
          {[...Array(10)].map((_, row) => (
            <div className="d-flex" key={row}>
              {[...Array(10)].map((_, col) => (
                <div
                  key={col}
                  className="border"
                  style={{
                    width: '20px',
                    height: '20px',
                    backgroundColor:
                      row <= tableSize.rows && col <= tableSize.cols ? '#cce5ff' : '#fff',
                  }}
                  onMouseEnter={() => setTableSize({ rows: row, cols: col })}
                  onClick={() => {
                    editor
                      .chain()
                      .focus()
                      .insertTable({
                        rows: tableSize.rows + 1,
                        cols: tableSize.cols + 1,
                        withHeaderRow: true,
                      })
                      .run();
                    setShowTableGrid(false);
                  }}
                />
              ))}
            </div>
          ))}
          <div className="mt-2 text-center text-muted">
            {tableSize.cols + 1} x {tableSize.rows + 1}
          </div>
        </div>
      )}
      {/* リンク挿入用モーダル */}
      {showLinkModal && (
        <div className="modal d-block" tabIndex="-1">
          <div className="modal-dialog">
            <div className="modal-content" ref={modalRef}>
              <div className="modal-header">
                <h5 className="modal-title">Insert Link</h5>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">URL</label>
                  <input
                    type="text"
                    className="form-control"
                    value={linkUrl}
                    onChange={(e) => {
                      setLinkUrl(e.target.value);
                      if (linkError) setLinkError('');
                    }}
                    placeholder="https://example.com"
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">テキスト</label>
                  <input
                    type="text"
                    className="form-control"
                    value={linkText}
                    disabled={linkTargetType === 'image'}
                    onChange={(e) => {
                      setLinkText(e.target.value);
                      if (linkError) setLinkError('');
                    }}
                    placeholder={linkTargetType === 'image' ? '画像を選択しています' : 'Click here'}
                  />
                  {linkError && <div className="text-danger mt-1">{linkError}</div>}
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    setShowLinkModal(false);
                    setLinkUrl('');
                    setLinkText('');
                    setLinkError('');
                    setLinkTargetType('text');
                  }}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => {
                    if (!linkUrl) {
                      setLinkError('URLも入力してください');
                      return;
                    }
                    if (linkTargetType !== 'image' && !linkText.trim()) {
                      setLinkError('テキストを入力してください');
                      return;
                    }
                    const linkAttrs = {
                      linkHref: linkUrl,
                      linkTarget: '_blank',
                      linkRel: 'noopener noreferrer',
                    };

                    if (linkTargetType === 'image') {
                      editor.chain().focus().updateAttributes('image', linkAttrs).run();
                    } else {
                      const label = linkText?.trim();
                      const markAttrs = {
                        href: linkUrl,
                        target: '_blank',
                        rel: 'noopener noreferrer',
                      };

                      if (editor.state.selection.empty) {
                        editor
                          .chain()
                          .focus()
                          .insertContent({
                            type: 'text',
                            text: label || linkUrl,
                            marks: [{ type: 'link', attrs: markAttrs }],
                          })
                          .run();
                      } else if (label) {
                        editor
                          .chain()
                          .focus()
                          .insertContent({
                            type: 'text',
                            text: label,
                            marks: [{ type: 'link', attrs: markAttrs }],
                          })
                          .run();
                      } else {
                        editor.chain().focus().extendMarkRange('link').setLink(markAttrs).run();
                      }
                    }
                    setShowLinkModal(false);
                    setLinkUrl('');
                    setLinkText('');
                    setLinkTargetType('text');
                    setLinkError('');
                  }}
                >
                  OK
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default MenuBar;
