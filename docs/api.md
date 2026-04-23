# 后端 API 设计

## 通用返回

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "xxx"
}
```

## 接口列表

### 1. 微信登录

- `POST /api/v1/auth/wx/login`

请求：

```json
{
  "code": "wx-login-code"
}
```

### 2. 媒体上传

- `POST /api/v1/media/upload`
- `POST /api/v1/media/upload-file`

请求：

```json
{
  "fileName": "voice-001.mp3",
  "source": "voice",
  "durationMs": 4200
}
```

说明：

- `/upload` 适合当前 mock 或服务端直传占位
- `/upload-file` 适合微信小程序通过 `wx.uploadFile` 直接上传录音文件

### 3. 语音转文字

- `POST /api/v1/speech/asr`

请求：

```json
{
  "mediaId": 2001
}
```

### 4. 核心优化

- `POST /api/v1/communications/optimize`

请求：

```json
{
  "sourceType": "text",
  "text": "你这周到底能不能把方案给我？别再拖了。",
  "scene": "workplace",
  "relation": "colleague",
  "goal": "remind",
  "needTts": false
}
```

说明：

- `sourceType` 支持 `text` 和 `voice`
- `scene`、`relation`、`goal` 都可以由前端手动指定，也可以留空让模型自动识别

### 5. 历史列表

- `GET /api/v1/communications`

### 6. 历史详情

- `GET /api/v1/communications/{recordId}`

### 7. 结果转语音

- `POST /api/v1/communications/{recordId}/tts`

请求：

```json
{
  "text": "麻烦今天内把方案发我一下；如果当前进度有问题，也请同步一下原因和预计时间。"
}
```

### 8. 收藏记录

- `PATCH /api/v1/communications/{recordId}/favorite`

### 9. 删除记录

- `DELETE /api/v1/communications/{recordId}`

### 10. 用户反馈

- `POST /api/v1/feedback`

请求示例：

```json
{
  "recordId": 1001,
  "actionType": "copy",
  "variantType": "recommended"
}
```

### 11. 当前 Provider 信息

- `GET /api/v1/system/providers`

返回示例：

```json
{
  "storageMode": "in-memory",
  "selectedLlm": "mock",
  "selectedAsr": "mock",
  "selectedTts": "mock",
  "availableLlms": ["mock", "openai", "glm", "local"],
  "availableAsrs": ["mock", "openai", "tencent", "local"],
  "availableTts": ["mock", "tencent", "azure", "edge"]
}
```
