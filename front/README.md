# React + Vite
このテンプレートは、Vite 上で React を HMR（ホットリロード）と一部の ESLint ルール付きで動かすための最小構成を提供します。
現在、公式プラグインは 2 つあります。

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh

# 参考書

- [Bootstrap](https://getbootstrap.kr/)
- [Bootstrap 5 CheatSheet](https://bootstrap-utilities-cheatsheet.vercel.app/)
- [React Router v7（日本語/英語）](https://react-router-docs-ja.techtalk.jp/)

## 💡 命名規則（ネーミング）

コンポーネント：PascalCase（例：CM_04_1000、NoticeTable）
ページ/画面 ID：CM_XX_XXXX 形式を維持
変数/関数：camelCase（例：searchKeyword、handleSearch）
定数：UPPER_SNAKE_CASE（例：SEARCH_TYPE、CATEGORIES）

## 📦 ディレクトリ構成

src/
├── components/ # 共通 UI コンポーネント（ボタン、モーダル、検索バーなど）
├── constants/  # カテゴリ、検索タイプ、ラベルなど共通定数
├── contexts/   # React Context API のグローバル状態
├── data/       # ダミーデータ、静的 JSON
├── hooks/      # カスタムフック（useXxx）
├── images/     # 静的画像ファイル
├── pages/      # 機能/画面ごとのページ（CM_XX_XXXX）
├── routes/     # react-router-dom のルート定義
├── services/   # API 呼び出しロジック（axios インスタンス、エラーハンドラなど）
├── styles/     # グローバルスタイル
├── utils/      # 日付フォーマット、文字列変換などの共通関数
├── App.jsx     # 最上位 App コンポーネント
└── main.jsx    # React エントリーファイル

## 🎨 コーディング方針

- React Hooks の並び：useState → useEffect → useMemo/useCallback の順で整理
- 検索・フィルタ・ソートなどは useMemo を活用
- JSX は可読性のため、適切に改行・コメントを入れる
- コメントは必要な箇所だけを明確に記載

## 🔧 API

- axios インスタンス（axiosInstance）を使用
- 共通エラー処理：handleHttpError ユーティリティを使用
- try/catch ブロックでは必ずエラー処理を呼び出す

## ページネーションコンポーネント使用例

```jsx
<CM_Pagination
  currentPage={currentPage} // 現在のページ番号（state）
  totalPages={totalPages} // 総ページ数
  onPageChange={setCurrentPage} // ページ変更時に呼ばれる関数
/>
```

## 検索バーの使用例

```jsx
import { CATEGORIES, SEARCH_TYPE } from '../constants/constants.js';

<CM_SearchBar
  searchTypes={SEARCH_TYPE} // 検索タイプ（例：タイトル+内容、タイトル、内容）
  searchType={searchType} // 選択中の検索タイプ（state）
  setSearchType={setSearchType} // 検索タイプ変更ハンドラ
  searchKeyword={searchKeyword} // 検索キーワード（state）
  setSearchKeyword={setSearchKeyword} // キーワード入力ハンドラ
  onSearch={handleSearch} // 検索実行関数
/>;
```

# Tiptap の使い方

Tiptap はカスタマイズ可能なリッチテキストエディタです。

## フォルダ構成

```
- front/
  └── src/
      └── components/
          └── editor/
              ├── EditorContainer.jsx // エディタの構成・描画
              ├── extensions.js       // Tiptap 拡張の定義
              ├── MenuBar.jsx         // ツールバー UI
              ├── toolbar.css         // テーブル等の拡張スタイル
              └── styles.css          // エディタ・ボタンのスタイル
```

## 構成要素

- EditorContainer.jsx：Tiptap エディタ全体をラップするコンポーネント
- extensions.js：エディタで使う拡張機能（h1、bold など）を定義
- MenuBar.jsx：エディタ上部のボタンツールバー UI
- styles.css：エディタとツールバーのスタイル
- toolbar.css：テーブル等の追加要素のスタイル（任意）

## 使用方法

### 1. 親コンポーネント例

```jsx
import React, { useState } from 'react';
import EditorContainer from '../components/editor/EditorContainer'; // 必須
import '../components/editor/toolbar.css'; // 必須

const WritePost = () => {
  const [content, setContent] = useState('');

  const handleContentChange = (html) => {
    setContent(html);
  };

  const handleSubmit = () => {
    console.log('最終内容:', content);
    // サーバへ保存する処理（例：fetch、axios など）
  };

  return (
    <div>
      <EditorContainer
        onContentChange={handleContentChange}
        placeholder="商品レビューを自由に入力してください。"
      />
      <button onClick={handleSubmit}>保存</button>
    </div>
  );
};

export default WritePost;
```

### 2. 保存時は HTML 形式で content が渡されます

- `dangerouslySetInnerHTML` を使って詳細ページでレンダリング可能です。

# ポップアップ（Modal）の使い方

ポップアップは共通 `ModalBase` を基盤として実装され、以下の構成で利用します。

## 폴더 구조

```
- front/
  └── src/
      └── components/
          └── popup/
              ├── ModalBase.jsx        // すべてのポップアップ共通ベース
              ├── CM_99_1000.jsx     // 通知ポップアップ（OK のみ）
              ├── CM_99_1001.jsx     // 確認/キャンセル
              ├── CM_99_1002.jsx     // ローディング（ボタンなし）
              ├── CM_99_1003.jsx     // エラー
              └── CM_99_popup.css    // ポップアップ共通スタイル
```

## 1. 기본 구조

공통 팝업은 아래처럼 구성됩니다.

```jsx
import CM_99_1001 from '@/components/popup/CM_99_1001';

const [isOpen, setIsOpen] = useState(false);

const handleOpen = () => setIsOpen(true);
const handleClose = () => setIsOpen(false);
const handleConfirm = () => {
  // 確認時の処理
  setIsOpen(false);
};

return (
  <>
    <button onClick={handleOpen}>ポップアップを開く</button>
    <CM_99_1001 isOpen={isOpen} onClose={handleClose} onConfirm={handleConfirm}>
      ここに内容が入ります。
    </CM_99_1001>
  </>
);
```

## 2. ポップアップ 種類

- `CM_99_1000`: 通知（OK のみ）
- `CM_99_1001`: 確認/キャンセル
- `CM_99_1002`: ローディング（ボタンなし）
- `CM_99_1003`: エラー

各ポップアップは `src/components/popup/` 配下に定義され、共通で `isOpen`、`onClose` などの `props` を使用します。

# テストアカウント

### 一般

ID:user1
PW:test123123!

### 管理者

ID:admin01
PW:test123123!

## 閲覧数カウントが +2 になる問題について

`React.StrictMode` が原因です。

`React 18` 以降、開発モードで StrictMode が有効の場合、useEffect が
useEffect → cleanup → useEffect の順にもう一度実行される挙動になります。

ビルド（production）環境では二重呼び出しは発生せず、正常に 1 回だけ呼ばれるため問題ありません。
