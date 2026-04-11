# OSS 文件上传接口文档

> 所有接口需在请求头携带 `Authorization: Bearer <token>`

---

## 1. 获取上传凭证

**请求：**
```
GET /api/oss/getOssToken?folder=user-avatar
```

**参数：**

| 参数 | 位置 | 必填 | 说明 |
|---|---|---|---|
| folder | query | 否 | 上传目标文件夹，默认 `user-avatar`，后续可传 `chat-image`、`chat-file` 等 |

**响应示例：**
```json
{
  "code": 200,
  "data": {
    "accessKeyId": "STS.NUxxxxxxxxxx",
    "accessKeySecret": "xxxxxxxxxx",
    "securityToken": "CAISxxxxxxxxxx",
    "expiration": "2026-03-30T16:00:00Z",
    "endpoint": "oss-cn-shenzhen.aliyuncs.com",
    "bucket": "chat-mu",
    "folder": "user-avatar"
  }
}
```

---

## 2. 更新头像

**请求：**
```
POST /api/oss/updateAvatar
Content-Type: application/json
```

**Body：**
```json
{
  "avatarUrl": "https://chat-mu.oss-cn-shenzhen.aliyuncs.com/user-avatar/20260330153000_abc123.jpg"
}
```

**参数：**

| 参数 | 必填 | 说明 |
|---|---|---|
| avatarUrl | 是 | OSS 文件的完整访问地址，前端直传成功后拼接 |

**响应示例：**
```json
{
  "code": 200,
  "data": "头像更新成功"
}
```

---

## 前端对接流程

```
1. 前端选择图片
2. 调 GET /api/oss/getOssToken 获取临时凭证
3. 用阿里云 OSS SDK（浏览器端）+ 临时凭证直传图片到 OSS
   - 文件路径建议: folder + "/" + 时间戳 + UUID + 文件后缀
   - 上传后的完整 URL: https://{bucket}.{endpoint}/{filePath}
4. 调 POST /api/oss/updateAvatar 把 URL 存到数据库
```

## OSS 浏览器 SDK 安装

```bash
npm install ali-oss
```

## 上传示例代码

```js
import OSS from 'ali-oss'

// 1. 获取凭证
const res = await fetch('/api/oss/getOssToken?folder=user-avatar', {
  headers: { 'Authorization': `Bearer ${token}` }
})
const { data } = await res.json()

// 2. 创建 OSS 客户端（用临时凭证）
const client = new OSS({
  accessKeyId: data.accessKeyId,
  accessKeySecret: data.accessKeySecret,
  stsToken: data.securityToken,
  bucket: data.bucket,
  endpoint: data.endpoint
})

// 3. 上传文件
const fileName = `${data.folder}/${Date.now()}_${Math.random().toString(36).slice(2)}.${file.name.split('.').pop()}`
const result = await client.put(fileName, file)

// 4. 拿到 URL，调后端更新头像
const avatarUrl = result.url
await fetch('/api/oss/updateAvatar', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ avatarUrl })
})
```

> **注意：** STS 临时凭证有效期为 1 小时，过期后需重新调用 `getOssToken` 获取。建议每次上传前都获取一次凭证，或判断过期后再重新获取。
