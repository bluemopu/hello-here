# Hello Here

現在地の周辺で投稿を共有できる、地域交流向けのWebアプリケーションです。個人開発として、要件整理、画面実装、データベース設計・連携、入力検証、テストまで一貫して取り組みました。

## 主な機能

- ユーザー登録・ログイン・ログアウト
- プロフィールの表示・編集、アバター画像の登録
- 現在地から100m以内の投稿を距離順で表示（最大100件）
- テキストと画像を含む投稿の作成
- 投稿者本人だけが実行できる投稿の論理削除
- CSRF対策、パスワードのArgon2ハッシュ化
- サーバー側の入力値・画像ファイル検証

## 使用技術

- Java 17
- Spring Boot 4.1.1
- Spring MVC / Thymeleaf
- Spring Data JPA / MySQL
- Spring Security（CSRF対策）
- Flyway
- HTML / CSS / JavaScript
- Geolocation API
- Leaflet / OpenStreetMap
- JUnit / Mockito / H2 / Testcontainers
- Maven

## 設計概要

- Controller、Service、Repositoryに責務を分割しています。
- 認証はHTTPセッションとSpring MVCのInterceptorで実装し、保護対象の画面とAPIへの未ログインアクセスを制御しています。
- データベース構造の変更はFlywayのマイグレーションで管理しています。
- 周辺投稿はMySQLの`ST_Distance_Sphere`で距離を計算し、100m以内を近い順に最大100件取得します。

## セットアップ

### 1. 必要な環境

- Java 17
- MySQL
- Docker（Testcontainersを使うテストを実行する場合）

### 2. データベースを作成

MySQLへ管理者ユーザーで接続し、アプリケーション用のデータベースとユーザーを作成します。パスワードは任意の安全な値へ変更してください。

```sql
CREATE DATABASE hello_here CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'hellohere'@'localhost' IDENTIFIED BY 'change-this-password';
GRANT ALL PRIVILEGES ON hello_here.* TO 'hellohere'@'localhost';
```

### 3. 接続情報を環境変数へ設定

Windows（PowerShell）:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/hello_here"
$env:DB_USERNAME = "hellohere"
$env:DB_PASSWORD = "change-this-password"
```

macOS / Linux:

```bash
export DB_URL="jdbc:mysql://localhost:3306/hello_here"
export DB_USERNAME="hellohere"
export DB_PASSWORD="change-this-password"
```

### 4. 起動

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

起動後、`http://localhost:8080/login` を開きます。`http://localhost:8080` からもログイン画面へ移動します。データベースのテーブルはFlywayによって作成・更新されます。

## テスト

Windows（PowerShell）:

```powershell
.\mvnw.cmd test
```

macOS / Linux:

```bash
./mvnw test
```

通常のテストはインメモリH2データベースを使用します。FlywayのマイグレーションテストはTestcontainersでMySQL 8.4を起動するためDockerが必要です。Dockerを利用できない環境では、このテストだけが自動的にスキップされます。

## 実装上の工夫

- 位置情報を利用し、現在地から100m以内の投稿を距離順で最大100件取得します。
- 投稿IDとログインユーザーIDの両方で対象を検索し、本人の投稿だけを論理削除できるようにしています。
- パスワードは平文で保存せず、Argon2でハッシュ化しています。
- CSRF対策に加え、投稿・プロフィール更新時の入力値と画像の実体形式をサーバー側でも検証しています。
- 画像保存後にデータベース更新がロールバックされた場合は、保存済みファイルも削除して不整合を防ぎます。

入力値の詳しい制約は [`docs/input-validation.md`](docs/input-validation.md) を参照してください。

## 補足

このリポジトリは成果物紹介用のスナップショットです。実際のデータベース認証情報や利用者がアップロードした画像は含めていません。
