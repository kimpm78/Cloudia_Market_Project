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
              '内容を入力してください。\n交換・キャンセル・返品に関するお問い合わせは、1:1お問い合わせよりご相談ください。'
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
