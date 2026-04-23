# 本地 MySQL 连接与项目联调

## 当前环境

- MySQL 版本：`9.6.0`
- 本地监听：`127.0.0.1:3306`
- 项目数据库：`communication_optimizer`
- 当前登录方式：`mysql -u root`

## 常用连接命令

```bash
# 登录 MySQL
mysql -u root

# 直接连接项目数据库
mysql -u root -D communication_optimizer

# 查询版本、端口和主机
mysql -u root -e "SELECT VERSION() AS version, @@port AS port, @@hostname AS hostname;"

# 查看项目数据库中的表
mysql -u root -D communication_optimizer -e "SHOW TABLES;"

# 查看核心业务数据条数
mysql -u root -D communication_optimizer -e "SELECT COUNT(*) AS record_count FROM communication_record; SELECT COUNT(*) AS variant_count FROM communication_variant; SELECT COUNT(*) AS media_count FROM media_asset; SELECT COUNT(*) AS feedback_count FROM user_feedback;"
```

## 服务管理

```bash
brew services start mysql
brew services list | grep mysql
brew services stop mysql
```

## 创建项目数据库

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS communication_optimizer CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

## 导入表结构

```bash
mysql -u root communication_optimizer < /Users/bruce/workspace/communication-optimizer-miniapp/docs/database.sql
```

说明：

- 如果后端以 `localmysql` profile 启动，并且 `app.mysql.auto-init-schema=true`，也可以自动建表。
- 手动导入 `database.sql` 更适合你想明确掌控结构版本的时候。

## 用本地 MySQL 启动项目后端

最简单方式：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=localmysql
```

显式参数方式：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar \
  --app.storage.mode=mysql \
  --app.mysql.url='jdbc:mysql://127.0.0.1:3306/communication_optimizer?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8' \
  --app.mysql.username=root \
  --app.mysql.password= \
  --app.mysql.auto-init-schema=true
```

## 启动后快速验证

```bash
curl -L -sS http://127.0.0.1:8080/api/v1/system/providers
```

预期能看到：

- `storageMode` 为 `mysql`
- `selectedLlm` / `selectedAsr` / `selectedTts` 为当前启用值

## 已验证的 MySQL 持久化对象

- `communication_record`
- `communication_variant`
- `media_asset`
- `user_feedback`
