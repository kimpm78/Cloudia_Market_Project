import { mergeAttributes } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import TextStyle from '@tiptap/extension-text-style';
import Color from '@tiptap/extension-color';
import TaskList from '@tiptap/extension-task-list';
import TaskItem from '@tiptap/extension-task-item';
import Heading from '@tiptap/extension-heading';
import Table from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import Placeholder from '@tiptap/extension-placeholder';
import Italic from '@tiptap/extension-italic';
import Highlight from '@tiptap/extension-highlight';
import TextAlign from '@tiptap/extension-text-align';
import Image from '@tiptap/extension-image';

export const getExtensions = (placeholderText = '내용을 입력해주세요.') => [
  StarterKit.configure({
    heading: false,
    horizontalRule: false,
    italic: false,
  }),
  Heading.configure({
    levels: [1, 2, 3, 4, 5, 6],
  }),
  Italic,
  Underline,
  Highlight,
  Link.configure({ openOnClick: false }),
  TextStyle,
  Color.configure({ types: [TextStyle.name] }),
  TextAlign.configure({
    types: ['heading', 'paragraph', 'image'],
  }),
  Image.extend({
    addAttributes() {
      return {
        ...this.parent?.(),
        dataSize: {
          default: null,
          parseHTML: (element) => element.closest('figure')?.getAttribute('data-size'),
          renderHTML: () => ({}),
        },
        linkHref: {
          default: null,
          parseHTML: (element) => element.closest('a')?.getAttribute('href'),
          renderHTML: () => ({}),
        },
        linkTarget: {
          default: '_blank',
          parseHTML: (element) => element.closest('a')?.getAttribute('target') || '_blank',
          renderHTML: () => ({}),
        },
        linkRel: {
          default: 'noopener noreferrer',
          parseHTML: (element) =>
            element.closest('a')?.getAttribute('rel') || 'noopener noreferrer',
          renderHTML: () => ({}),
        },
      };
    },
    addOptions() {
      return {
        ...this.parent?.(),
        sizes: ['inline', 'block', 'left', 'right'],
      };
    },
    renderHTML({ HTMLAttributes, node }) {
      const { dataSize, linkHref, linkTarget, linkRel } = node.attrs;
      const { style, ...imgAttrs } = HTMLAttributes;
      const figureAttrs = {};
      if (style) figureAttrs.style = style;
      if (dataSize) figureAttrs['data-size'] = dataSize;

      const image = ['img', mergeAttributes(this.options.HTMLAttributes, imgAttrs)];

      if (linkHref) {
        const linkAttrs = {
          href: linkHref,
          target: linkTarget || '_blank',
          rel: linkRel || 'noopener noreferrer',
        };
        return ['figure', figureAttrs, ['a', linkAttrs, image]];
      }

      return ['figure', figureAttrs, image];
    },
  }),
  TaskList,
  TaskItem.configure({ nested: true }),
  Table.configure({ resizable: true }),
  TableRow,
  TableHeader,
  TableCell,
  Placeholder.configure({
    placeholder: placeholderText,
    showOnlyWhenEditable: true,
    showOnlyCurrent: true,
    includeChildren: false,
    emptyEditorClass: 'is-editor-empty',
  }),
];
