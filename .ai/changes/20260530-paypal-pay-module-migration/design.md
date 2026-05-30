# 支付模块迁移设计契约

## 业务边界

- 当前项目中厂家 / 平台方暂按 `TenantType.PLATFORM` 和 `TenantConstants.PLATFORM_TENANT_ID = 1L` 处理；如果后续新增“厂家”租户类型，需要回到 `/plan` 补充租户模型。
- 商户是付款方，厂家 / 平台方是收款方。支付模块的收款配置、支付应用和支付渠道不开放给商户维护。
- `pay_app` / `pay_channel` 是收款侧配置，归平台 / 厂家管理；商户侧只能创建支付单、查看自己订单相关支付状态，不能读取渠道密钥、证书或原始 config。
- 订单业务只通过支付模块内部 API 创建支付单、提交支付、查询支付结果；订单业务不直接操作 `pay_*` 表。
- 支付成功后由支付模块通过内部回调 / 事件推进订单状态，订单侧处理必须幂等，不能仅依赖前端跳转结果。

## 租户与权限模型

- `pay_order`、`pay_refund`、`pay_transfer` 等交易表需要同时表达付款方和收款方：
  - `payer_tenant_id`：付款商户租户。
  - `payee_tenant_id`：收款平台 / 厂家租户。
  - `tenant_id` 如保留，应作为当前项目租户拦截兼容字段，语义必须在 DDL 注释中说明，不能替代上面两个业务字段。
- 平台 / 厂家可管理 `pay_app`、`pay_channel`、通知任务和收款侧交易查询。
- 商户只能查询 `payer_tenant_id` 属于自己的支付单、退款单和订单侧状态；默认不能访问 `pay_channel.config`。
- 外部支付平台回调接口可以使用匿名入口，但必须做渠道验签、金额校验、订单归属校验和幂等状态更新。

## PostgreSQL DDL 契约

- 第一批独立生成 PostgreSQL `pay_*` DDL，不直接导入旧 `ruoyi-vue-pro.sql`。
- 时间字段使用 `timestamptz` 并按项目约定保持 UTC 语义。
- 金额字段沿用旧模块“分”为单位的整数模型，建议使用 `bigint`；币种字段使用 ISO 4217 字符串，例如 `CNY`、`USD`。
- `pay_channel.config` 使用 `jsonb`，接口和日志输出必须脱敏；证书、密钥、Token 不写入文档、不写入示例真实值。
- 必要核心表：
  - `pay_app`：支付应用，含通知地址与状态。
  - `pay_channel`：渠道配置，含 `code`、`app_id`、`fee_rate`、`config jsonb`。
  - `pay_order`：支付单，含业务订单号、渠道、金额、币种、状态、付款方 / 收款方租户。
  - `pay_order_extension`：渠道提交记录和渠道响应。
  - `pay_refund`：退款单，含业务退款号、退款金额、渠道退款号、状态。
  - `pay_notify_task` / `pay_notify_log`：支付、退款、转账通知任务和日志。
  - `pay_transfer`：转账单，保留基础能力；真实转账按渠道资质另行联调。
  - `pay_wallet`、`pay_wallet_transaction`、`pay_wallet_recharge`、`pay_wallet_recharge_package`：钱包能力保留，但使用前需要明确是否作为平台预付余额或商户余额。
- 交易表需要按 `merchant_order_id`、`no`、`channel_order_no`、`payer_tenant_id`、`payee_tenant_id`、`status`、`create_time` 建立必要索引。
- 支付字典、菜单和 job 初始化可以参考旧 SQL 中的 `pay_channel_code`、`pay_order_status`、`pay_notify_status`、`pay_refund_status`、`pay_transfer_status`、`pay_notify_type`，但必须适配当前 `sys_*` 表结构。

## 渠道与依赖契约

- 支付宝、微信、PayPal、Stripe、Mock、Wallet 都纳入迁移范围；真实联调按配置条件分层执行。
- 旧依赖版本来源：
  - `alipay-sdk-java 4.40.771.ALL`
  - `weixin-java-pay 4.8.2-20260501.180637`
  - `stripe-java 31.0.0`
  - `checkout-sdk 1.0.5`
  - `httpclient5 5.5.2`
  - `httpcore5 5.3.6`
- 支付宝和 PayPal 依赖需要继续排除旧 `bcprov-jdk15on`，避免与当前项目安全依赖冲突。
- 微信依赖是快照时间戳版本，进入实现前需要确认 Maven 源可解析；如不可解析，记录原因并选择可解析的稳定版本。
- `PayChannelServiceImpl.parseConfig()` 迁移时必须补齐全渠道 config 映射，至少覆盖 `paypal`、`stripe`、支付宝、微信、Mock、Wallet。

## 回调验签与安全契约

- PayPal webhook 必须强制调用签名验证；旧模块已有验证方法但主流程未强制调用，迁移时必须修复。
- Stripe webhook 保留 `Webhook.constructEvent` 签名验证；验签失败不得更新支付成功。
- 支付宝 notify 保留 `AlipaySignature.rsaCheckV1` 验签，并校验金额、应用、业务单号和状态。
- 微信 notify 保留 WxJava V2/V3 的回调解析与验签能力，并校验金额、商户单号和状态。
- Mock 渠道仅用于本地 smoke，不代表真实渠道联调通过。
- 所有渠道通知必须做幂等更新：只有符合当前状态流转的记录才能更新成功，重复通知只记录日志。

## 验收策略

- Wave 1 进入实现前，允许修改 Maven 模块、依赖管理、PG DDL 和 `bocoo-module-pay` 代码，但不写入真实支付密钥。
- 第一轮验证至少包含 `git diff --check`、Maven 编译和 Mock 支付主链路 smoke。
- 无正式域名不阻塞本地开发；真实支付平台回调联调需要公网可访问地址或 sandbox 临时方案，当前先记录为联调前置条件。
