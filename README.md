# Cloudia Market

このREADMEは、初めてプロジェクトを開く人でも同じ手順で起動できるように、Docker起動からアプリ確認までの順序をまとめたものです。

## 1. 事前準備

- Docker Desktop を起動してください。
- 環境設定ファイルは、共有ドライブに置かれている `CM.zip` を使用します。
- `CM.zip` を解凍し、プロジェクトルートに必要ファイルを配置してください。

例:

- `.env`
- `backend/src/main/resources/application-local.properties`
- `backend/src/main/resources/application-docker.properties`

注意:

- Gitには環境設定ファイルを含めません。
- 不足ファイルがあると、起動時に接続エラーやBean生成エラーが発生します。

## 2. Docker起動

プロジェクトルートで実行:

```bash
docker compose up -d --build
```

## 3. コンテナ状態確認

```bash
docker compose ps
```

`postgres`, `redis`, `backend`, `frontend` が `Up` になっていることを確認してください。

## 4. アプリ接続先

- Frontend: http://localhost:5173
- Backend API: http://localhost:9090/api
- MailHog UI: http://localhost:8025
- PostgreSQL: localhost:5433
- Redis: localhost:6379

## 5. ログ確認（問題発生時）

```bash
docker compose logs -f backend
```

必要に応じて:

```bash
docker compose logs -f frontend
```

## 6. 停止

```bash
docker compose down
```

データボリュームも削除する場合:

```bash
docker compose down -v
```

## 7. DBパッチ（既存データベース向け）

パスワード履歴の `password` カラム長エラーが発生する場合のみ、以下を実行してください。

```bash
docker compose exec -T postgres psql -U postgres -d cloudia -f /dev/stdin < backend/db/patches/2026-02-09_alter_password_history_password_to_255.psql
```
