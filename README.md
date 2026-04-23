# Communication Optimizer Miniapp

一个“人际沟通优化小程序”的项目骨架，包含：

- `backend/`：Spring Boot 后端骨架
- `miniprogram/`：微信小程序页面骨架
- `docs/`：架构、接口、数据库、Prompt 文档

## 目录结构

```text
communication-optimizer-miniapp/
├── backend/
├── docs/
└── miniprogram/
```

## 当前状态

- 已搭建后端基础目录和核心接口骨架
- 已搭建微信小程序页面与基础交互壳
- 已补充产品设计文档、API 设计、数据库 SQL、Prompt 模板
- 已验证后端可测试、可打包、可本地启动
- 已完成核心 API 联调验证

## 本地启动

### 后端

首次构建建议使用项目内 Maven 仓库，避免依赖本机 `~/.m2` 状态：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
mkdir -p /Users/bruce/workspace/communication-optimizer-miniapp/.m2repo
mvn -Dmaven.repo.local=/Users/bruce/workspace/communication-optimizer-miniapp/.m2repo test
mvn -Dmaven.repo.local=/Users/bruce/workspace/communication-optimizer-miniapp/.m2repo -DskipTests package
```

启动 jar：

```bash
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar
```

默认地址：

- 后端 API：`http://127.0.0.1:8080`

### 切换到 MySQL

默认存储模式是 `in-memory`。如果要改成 MySQL：

1. 创建数据库并执行：

```bash
mysql -u root -p your_db < /Users/bruce/workspace/communication-optimizer-miniapp/docs/database.sql
```

2. 修改 `backend/src/main/resources/application.yml`：

```yaml
app:
  storage:
    mode: mysql
  mysql:
    url: jdbc:mysql://127.0.0.1:3306/your_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: your_user
    password: your_password
```

3. 重新打包并启动：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
mvn -Dmaven.repo.local=/Users/bruce/workspace/communication-optimizer-miniapp/.m2repo -DskipTests package
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar
```

当前 MySQL 模式已支持的落库对象：

- 沟通记录 `communication_record`
- 三档改写结果 `communication_variant`
- 媒体记录 `media_asset`
- 用户反馈 `user_feedback`

### 配置大模型 Key

建议通过环境变量配置，不要把 key 直接写进仓库。

OpenAI：

```bash
export OPENAI_API_KEY=你的OpenAIKey
export OPENAI_MODEL=gpt-5.2
```

GLM：

```bash
export GLM_API_KEY=你的GLMKey
export GLM_MODEL=glm-4.7
```

切换 provider：

修改 `backend/src/main/resources/application.yml` 中的：

```yaml
app:
  providers:
    llm: openai
```

或：

```yaml
app:
  providers:
    llm: glm
```

### 当前机器上的本地 MySQL

已安装并验证：

- MySQL 版本：`9.6.0`
- 本地地址：`127.0.0.1:3306`
- 数据库：`communication_optimizer`
- 当前 root 可直接本地登录：`mysql -u root`

常用连接方式：

```bash
# 直接进入 MySQL
mysql -u root

# 直接连到项目库
mysql -u root -D communication_optimizer

# 执行一条 SQL 验证连接
mysql -u root -e "SELECT VERSION(), @@port, @@hostname;"

# 查看项目库中的核心表
mysql -u root -D communication_optimizer -e "SHOW TABLES;"
```

服务管理：

```bash
brew services start mysql
brew services list | grep mysql
brew services stop mysql
```

如果你想直接用这台机器上的本地 MySQL 跑后端，最简单方式是使用已经准备好的 profile：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=localmysql
```

如果你想显式覆盖参数，也可以这样启动：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar \
  --app.storage.mode=mysql \
  --app.mysql.url='jdbc:mysql://127.0.0.1:3306/communication_optimizer?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8' \
  --app.mysql.username=root \
  --app.mysql.password= \
  --app.mysql.auto-init-schema=true
```

### 小程序

在微信开发者工具中打开：

- `/Users/bruce/workspace/communication-optimizer-miniapp/miniprogram`

本地接口地址配置在：

- `miniprogram/app.js`
- `miniprogram/config/env.js`

线上 API 域名建议：

- `https://api.mizio.cn/api/v1`

### 真机联调

当前这台机器可用于局域网联调的地址：

- `192.168.31.17`

当前小程序开发环境默认已切到：

- `http://192.168.31.17:8080/api/v1`

真机联调最短路径：

1. 电脑启动后端：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=localmysql
```

2. 确保手机和电脑连接同一个 Wi-Fi
3. 在微信开发者工具中导入：
   `/Users/bruce/workspace/communication-optimizer-miniapp/miniprogram`
4. 用开发者工具预览/真机调试

说明：

- `miniprogram/config/env.js` 里现在默认是 `lan` 环境
- 如果你只想在本机模拟器调试，可以改回 `local`
- `project.config.json` 已添加 `urlCheck: false`，方便开发联调
- 真机开发/预览时，局域网 HTTP 在开发场景通常可用；如果后续要更稳定地给体验版或正式版使用，建议切到 HTTPS 域名并配置小程序后台合法域名

### mizio.cn 域名方案

推荐后端子域名：

- `api.mizio.cn`

对应材料：

- Nginx 配置模板：`deploy/nginx/api.mizio.cn.conf`
- 接入步骤文档：[api-mizio-domain.md](/Users/bruce/workspace/communication-optimizer-miniapp/docs/api-mizio-domain.md)

## 已验证接口

- `GET /api/v1/system/providers`
- `POST /api/v1/communications/optimize`
- `GET /api/v1/communications`
- `GET /api/v1/communications/{recordId}`
- `PATCH /api/v1/communications/{recordId}/favorite`
- `POST /api/v1/feedback`
- `POST /api/v1/media/upload`
- `POST /api/v1/speech/asr`
- `POST /api/v1/communications/{recordId}/tts`

## 建议下一步

1. 接入真实微信登录与用户体系
2. 接入真实 ASR / LLM / TTS 供应商
3. 将 mock 结果替换为数据库持久化与模型调用
4. 在微信开发者工具中联调小程序页面
