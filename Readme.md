# 🐳 家計簿アプリ Docker 起動手順書
📦 対象構成
- Spring Boot（Java 17）
- PostgreSQL（DB）
- Docker / Docker Compose
- Gradle（ビルドツール）

📁 プロジェクト構成（例）
``` kakeibo-app/ ├── Dockerfile ├── docker-compose.yml ├── build.gradle ├── src/ │   └── main/ │       ├── java/com/yuuki/householdbook/ │       └── resources/templates/ ```



✅ 1. .jar ファイルを生成
./gradlew clean bootJar


- 成果物は build/libs/app.jar
- build.gradle に以下を追加するとファイル名が固定されて便利：
bootJar {
    archiveFileName = 'app.jar'
}



✅ 2. Dockerfile を作成
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]



✅ 3. docker-compose.yml を作成
version: '3.8'

services:
  db:
    image: postgres:15
    container_name: household_db
    environment:
      POSTGRES_DB: household_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  app:
    build: .
    container_name: household_app
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/household_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SPRING_JPA_SHOW_SQL: true
      SPRING_THYMELEAF_CACHE: false

volumes:
  pgdata:



✅ 4. 起動コマンド
docker-compose up -d --remove-orphans


- -d：バックグラウンド実行
- --remove-orphans：不要なコンテナを削除

✅ 5. 状態確認
docker ps
docker logs household_app


- Tomcat initialized with port 8080 → Webサーバー起動成功
- HikariPool-1 - Start completed → DB接続成功

✅ 6. ブラウザで確認
http://localhost:8080/


- 404の場合は / に対応するコントローラーが未定義
- HomeController を追加して redirect:/login などに設定

✅ 7. 初期管理者登録（AdminInitializer）
admin.setEmail("admin@example.com"); // ← 必須


- email が null のままだと Hibernate が例外を投げて起動失敗します

⚠️ よくあるエラーと対処法
|  |  |  | 
| email=null | AppUser.emailnullable=false | setEmail(...) | 
|  | SPRING_DATASOURCE_URL | jdbc:postgresql://db:5432/household_db | 
| .jar | bootJar | ./gradlew clean bootJar | 
|  | / | HomeController | 



🧼 クリーンアップ
docker-compose down
docker system prune -f



