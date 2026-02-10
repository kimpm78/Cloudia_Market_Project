# Cloudia Market

## Dockerの起動

### 1) 環境変数ファイルの準備

```bash
cp .env.example .env
```

必要に応じて .env の値を修正してください。

2) ビルド + 起動

```bash
docker compose up -d --build
```

### 3) 接続先

- Frontend: http://localhost:5173
- Backend API: http://localhost:9090/api
- MailHog UI: http://localhost:8025
- PostgreSQL: localhost:5433
- Redis: localhost:6379

### 4) 停止

```bash
docker compose down
```

データボリュームも削除する場合:

```bash
docker compose down -v
```

### 5) DB パッチ (既存データベース向け)

パスワード履歴の `password` カラム長エラーが発生する場合は、以下を実行してください。

```bash
docker compose exec -T postgres psql -U postgres -d cloudia -f /dev/stdin < backend/db/patches/2026-02-09_alter_password_history_password_to_255.psql
```
# Cloudia_Market_Project
