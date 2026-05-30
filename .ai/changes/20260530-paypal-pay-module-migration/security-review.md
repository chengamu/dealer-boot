# 支付模块安全审查

## 已落实

- 金额字段按分存储，未使用浮点金额。
- 订单、退款、通知任务显式区分 `payer_tenant_id` 和 `payee_tenant_id`。
- Mock 支付只作为 dev/local smoke 入口，控制器使用 `@Profile({"dev", "local"})`。
- `submitOrder` 已限制非 `mock` / `wallet` 渠道，真实支付未完成前不会伪造提交成功。
- Mock 成功路径使用等待状态条件更新，重复通知不会重复推进成功状态。
- 管理查询接口按登录租户显式过滤：平台按 `payeeTenantId`，商户按 `payerTenantId`。
- `pay_channel.config` 管理查询返回前脱敏为 `{"masked":true}`。
- 真实支付密钥、证书、Token、连接串未写入代码和 SQL。
- 支付宝与 PayPal 依赖排除了旧 `bcprov-jdk15on`，降低 BouncyCastle 冲突风险。

## 必须在真实支付解冻前完成

- PayPal webhook 必须强制验签，验签失败不得更新订单、退款或转账状态。
- Stripe webhook 必须使用 webhook secret 验签，验签失败不得更新状态。
- 支付宝 / 微信 notify 必须保留官方验签，验签失败不得更新状态。
- 真实渠道支付金额、币种、订单号必须与本地订单一致后才能更新成功。
- 真实退款和真实转账必须实现幂等状态更新，不允许按渠道回调重复记账。
- 渠道配置管理必须只允许平台 / 厂家侧访问，所有密钥、证书、secret 输出必须脱敏。
- webhook 地址需要公网入口；当前无域名时只能先使用 Mock 或临时 sandbox 隧道。

## 可延后

- 支付管理前端页面和菜单初始化，等后端管理 API 稳定后再补。
- 支付通知任务的异步重试调度，当前只保留表结构和查询入口。
- Wallet 充值包管理页面和真实充值流程，当前只补本地 service contract。
- 真实渠道 sandbox smoke，等待商户配置、证书和 webhook 公网地址。

## 当前剩余风险

- `pay_channel.config` 当前实体仍是 `String`，后续写入接口需要严格校验 JSON 结构并脱敏。
- 管理查询权限 `pay:*:list` 已加在控制器，但菜单/角色初始化尚未落 SQL，前端菜单需要后续补齐。
- 真实支付 SDK 虽已纳入依赖管理，但真实客户端未启用，版本兼容性需要在解冻真实支付时单独验证。
