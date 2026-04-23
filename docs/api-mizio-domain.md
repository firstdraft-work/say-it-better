# api.mizio.cn 接入步骤

## 1. DNS 解析

在域名控制台中为 `mizio.cn` 添加：

- 主机记录：`api`
- 记录类型：`A`
- 记录值：你的阿里云轻量服务器公网 IP

最终目标：

- `api.mizio.cn -> 你的服务器公网 IP`

## 2. 服务器 Nginx 配置

使用文件：

- `deploy/nginx/api.mizio.cn.conf`

放到服务器：

```bash
sudo cp /srv/communication-optimizer/backend/api.mizio.cn.conf /etc/nginx/conf.d/api.mizio.cn.conf
```

检查并重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 3. HTTPS 证书

推荐任意一种：

- 阿里云证书服务
- Certbot / Let's Encrypt

配置完成后，证书路径对应：

- `/etc/nginx/ssl/api.mizio.cn/fullchain.pem`
- `/etc/nginx/ssl/api.mizio.cn/privkey.pem`

## 4. 后端访问验证

```bash
curl -L -sS https://api.mizio.cn/api/v1/system/providers
```

## 5. 小程序配置

把 `miniprogram/config/env.js` 切到：

```js
const CURRENT_ENV = "prod";
```

线上接口地址：

- `https://api.mizio.cn/api/v1`

## 6. 微信后台合法域名

至少填写：

- `request 合法域名`：`https://api.mizio.cn`
- `uploadFile 合法域名`：`https://api.mizio.cn`
- `downloadFile 合法域名`：`https://api.mizio.cn`

## 7. 真机验证

验证这些接口链路：

- 文本优化
- 语音上传
- 语音转文字
- 文字转语音
- 历史记录
