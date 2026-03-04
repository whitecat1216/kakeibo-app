# 家計簿アプリ（kakeibo-app）

Spring Boot + PostgreSQL で作成した家計簿アプリです。  
モバイル寄りの UI（ハンバーガーメニュー）で、明細管理・レポート・年間収支を利用できます。

## 技術構成
- Java 17
- Spring Boot 3.2.x
- Thymeleaf
- PostgreSQL 15
- Docker / Docker Compose
- Gradle 8.x

## 主な機能
- ダッシュボード（最小サマリー）
- 明細リスト（検索・期間フィルタ・編集・複製・一括削除）
- レポート（前月比、前年同月比、上位支出カテゴリ、異常増加カテゴリ）
- 年間収支（月別集計、累計推移グラフ）
- CSVエクスポート / CSVインポート（重複スキップ対応）
- カテゴリ管理（色・並び順・タイプ）
- 支払方法管理（口座/財布/カード等）
- 定期収支（毎月自動作成）
- 管理者画面（ユーザー一覧、ユーザー家計簿参照）

## 画面ルート
- `/` -> `/login` にリダイレクト
- `/accounts` ダッシュボード
- `/accounts/list` 明細リスト
- `/accounts/report` レポート
- `/accounts/yearly` 年間収支
- `/accounts/new` 収支登録
- `/accounts/import` CSVインポート
- `/categories` カテゴリ管理
- `/sources` 支払方法管理
- `/recurring` 定期収支
- `/admin/users` 管理者メニュー

## 起動方法（Docker）
1. コンテナ起動

```bash
docker compose up -d
```

2. 起動確認

```bash
docker compose ps
docker logs household_app --tail 100
```

3. ブラウザ
- [http://localhost:8080/login](http://localhost:8080/login)

## 起動方法（ローカル bootRun）
DB は Docker の PostgreSQL を利用する想定です。

1. DBのみ起動

```bash
docker compose up -d db
```

2. アプリ起動

```bash
./gradlew bootRun
```

3. ブラウザ
- [http://localhost:8080/login](http://localhost:8080/login)

### ローカルDB接続設定
`application.properties` は次の順で接続先を解決します。

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/household_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
```

- Docker DB のホスト公開ポートは `5433`（`docker-compose.yml`）
- コンテナ内アプリは `db:5432` を使用

## よくあるエラー

### `bind: address already in use`（8080）
`8080` を別プロセスが使用しています。

```bash
lsof -i :8080
kill <PID>
docker compose up -d
```

### `localhost:5433 への接続が拒絶されました`
`household_db` が起動していないか、ポート不一致です。

```bash
docker compose ps
docker compose up -d db
docker logs household_db --tail 100
```

### `Task 'boot' is ambiguous`
`./gradlew boot` ではなく完全なタスク名を指定してください。

```bash
./gradlew bootRun
./gradlew bootJar
```

## 開発用コマンド

```bash
./gradlew compileJava
./gradlew test
./gradlew bootRun
```

## クリーンアップ

```bash
docker compose down
docker system prune -f
```
