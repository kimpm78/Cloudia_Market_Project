import { useState, useEffect } from 'react';
import { EditorProvider, useCurrentEditor } from '@tiptap/react';
import MenuBar from './MenuBar';
import { getExtensions } from './extensions';

import './toolbar.css';

const EditorUpdater = ({ content }) => {
  const { editor } = useCurrentEditor();

  useEffect(() => {
    if (editor && content !== undefined) {
      const currentContent = editor.getHTML();
      if (currentContent !== content) {
        editor.commands.setContent(content, false);
      }
    }
  }, [editor, content]);

  return null;
};

const EditorContainer = ({
  onContentChange,
  onImageAdd,
  error,
  field,
  placeholder,
  content = '',
}) => {
  const [isEmpty, setIsEmpty] = useState(true);

  return (
    <div>
      <>
        <EditorProvider
          extensions={getExtensions(
            placeholder ||
              '내용을 입력해주세요.\n교환 및 취소 및 반품 문의는 1:1 문의로 상담부탁드립니다.'
          )}
          content={content}
          slotBefore={<MenuBar field={field} onImageAdd={onImageAdd} />}
          editorProps={{
            attributes: {
              class:
                'tiptap tiptap-wrapper bg-white border rounded py-2 px-3 overflow-y-auto max-w-screen-md mx-auto',
            },
          }}
          onUpdate={({ editor }) => {
            const html = editor.getHTML();
            const text = editor.getText().trim();
            setIsEmpty(text.length === 0);
            if (onContentChange) {
              onContentChange(html);
            }
          }}
        >
          <EditorUpdater content={content} />
        </EditorProvider>
      </>
      {error && <div className="text-danger small mt-1">{error}</div>}
    </div>
  );
};

export default EditorContainer;
