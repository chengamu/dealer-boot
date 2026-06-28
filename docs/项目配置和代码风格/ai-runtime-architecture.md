# AI Runtime 架构约定

本文定义 AI Runtime 的长期边界，避免把临时代码混进 Java 业务系统。

## 调用链路

```text
Browser / Page Agent
  -> Nginx /ai-runtime/**
  -> Go ai-runtime
  -> Java /internal/ai/**
  -> Java 业务接口 / 工具
```

- Go ai-runtime 是公司级 AI 入口，负责 channel、session、provider 调用、tool 调度和 runtime 日志。
- Go ai-runtime 独立在 `chengamu/ai-runtime` 仓库；`base-boot` 只保留 Java 内部接口、AI 管理页面、Nginx/部署对接和业务库 SQL。
- Java 业务系统负责 AI 管理界面、用户权限、租户开关、额度、provider 配置、service key、审计汇总和业务工具授权。
- Java 不作为模型代理主路径；浏览器里的 Page Agent 不直接调用 provider。

## 数据库边界

同一个 PostgreSQL 实例，固定拆两个 database：

| database | 归属 | 内容 |
| --- | --- | --- |
| 业务库，例如 `dealer` | Java | AI 权限、额度、provider 配置、service credential、用量和审计汇总 |
| `ai_runtime` | Go | session、message、provider call、tool run、approval、runtime event |

不要先用同库 schema 过渡，后续扩订单、ERP、MES 时继续沿用 database 隔离。

## 密钥边界

- Service key 由 Java AI 设置页面生成，明文只返回一次。
- Java 数据库只保存 AES-GCM 密文、指纹、key version 和状态。
- `BOCOO_AI_SECRET_ENCRYPTION_KEY` 是密钥加密主密钥，只放部署环境变量。
- Go 调 Java 内部接口使用 HMAC 签名头：
  - `X-AI-Service`
  - `X-AI-Key-Version`
  - `X-AI-Timestamp`
  - `X-AI-Nonce`
  - `X-AI-Body-SHA256`
  - `X-AI-Signature`
- Page Agent 使用 Java bootstrap 签发的短期 channel token，不使用 provider key。

## 部署要求

- 外部只暴露 Nginx。
- `/dev-api/**` 代理到 Java。
- `/ai-runtime/**` 代理到 Go，并关闭 proxy buffering 以支持流式响应。
- `/internal/ai/**` 不通过 Nginx 暴露给浏览器，只允许 Go 通过内网服务名调用。
