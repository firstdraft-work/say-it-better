# 阿里云轻量服务器部署清单

## 推荐架构

- `Nginx`：对外提供 HTTPS 域名
- `Spring Boot Jar`：跑在 `127.0.0.1:8080`
- `MySQL`：本机或云数据库
- `systemd`：托管后端进程

## 目录建议

```text
/srv/communication-optimizer/backend/
├── communication-optimizer-backend-0.0.1-SNAPSHOT.jar
└── .env
```

## 服务器准备

```bash
sudo apt update
sudo apt install -y openjdk-17-jre nginx
```

如果你用的是 Alibaba Cloud Linux，可按对应包管理器改成：

```bash
sudo dnf install -y java-17-openjdk nginx
```

## 上传后端 jar

本地打包：

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
mvn -Dmaven.repo.local=/Users/bruce/workspace/communication-optimizer-miniapp/.m2repo -DskipTests package
```

上传到服务器：

```bash
scp target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar user@your-server:/srv/communication-optimizer/backend/
```

## 生产环境变量

参考文件：

- `deploy/backend.env.example`

服务器上复制：

```bash
cp /srv/communication-optimizer/backend/backend.env.example /srv/communication-optimizer/backend/.env
```

重点改这些值：

- `APP_MYSQL_URL`
- `APP_MYSQL_USERNAME`
- `APP_MYSQL_PASSWORD`
- `GLM_API_KEY`
- `APP_ASR_TENCENT_SECRET_ID`
- `APP_ASR_TENCENT_SECRET_KEY`
- `APP_TTS_TENCENT_SECRET_ID`
- `APP_TTS_TENCENT_SECRET_KEY`

## systemd

把 `deploy/systemd/communication-optimizer.service` 放到：

```bash
/etc/systemd/system/communication-optimizer.service
```

然后执行：

```bash
sudo systemctl daemon-reload
sudo systemctl enable communication-optimizer
sudo systemctl start communication-optimizer
sudo systemctl status communication-optimizer
```

## Nginx

把 `deploy/nginx/api.example.com.conf` 放到：

```bash
/etc/nginx/conf.d/api.example.com.conf
```

检查并重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## HTTPS

你需要一个公网 HTTPS 域名，例如：

- `https://api.example.com`

你可以用：

- Let's Encrypt / Certbot
- 阿里云证书服务

## 验证

后端：

```bash
curl -L -sS https://api.example.com/api/v1/system/providers
```

TTS 文件下载：

```bash
curl -L -I https://api.example.com/api/v1/media/audio/<file-name>.mp3
```

## 小程序环境改动

本地开发：

- `http://127.0.0.1:8080/api/v1`
- `http://192.168.x.x:8080/api/v1`

上线前：

- `https://api.example.com/api/v1`

把 `miniprogram/config/env.js` 里的线上环境地址改成最终域名。
