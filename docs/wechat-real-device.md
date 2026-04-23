# 小程序真机联调说明

## 当前局域网接口地址

- 后端地址：`http://192.168.31.17:8080/api/v1`
- 当前小程序默认环境：`lan`

对应配置文件：

- `/Users/bruce/workspace/communication-optimizer-miniapp/miniprogram/config/env.js`

## 环境切换

```js
const CURRENT_ENV = "lan";
```

可选值：

- `local`：本机模拟器走 `127.0.0.1`
- `lan`：手机和电脑同网段时走局域网 IP

## 启动后端

```bash
cd /Users/bruce/workspace/communication-optimizer-miniapp/backend
/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -jar target/communication-optimizer-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=localmysql
```

## 真机联调步骤

1. 手机和电脑连接同一个 Wi-Fi
2. 在微信开发者工具中打开：
   `/Users/bruce/workspace/communication-optimizer-miniapp/miniprogram`
3. 确认 `miniprogram/config/env.js` 当前为 `lan`
4. 在微信开发者工具中预览或真机调试

## 当前项目已验证的接口链路

- 文本优化
- 历史列表
- 详情页
- 收藏
- 反馈
- 语音上传
- ASR 转写
- TTS 返回
- 删除记录

## 注意事项

- `127.0.0.1` 只能给电脑本机访问，手机访问不到
- 如果电脑切换 Wi-Fi，局域网 IP 可能变化，需要同步更新 `env.js`
- 开发联调阶段可以先用局域网 HTTP
- 如果后续要做更稳定的真机体验，建议换成 HTTPS 域名并配置小程序后台合法域名
