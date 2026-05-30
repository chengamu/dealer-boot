# Archive: bocoo-module-pay 支付模块迁移

## Feature：功能

从 `D:\work\amu-boot\amu-module-pay` 分析并迁移支付模块能力到当前项目，新建 `bocoo-module-pay`，适配当前 PostgreSQL、租户、UTC、权限和 Mock smoke 验证策略。

## Requirement Source：需求来源

- `.ai/requirements/20260530-paypal-pay-module-migration.md`
- `.ai/changes/20260530-paypal-pay-module-migration/wave-plan.md`
- 用户补充：系统业务模型为商户下单付款，厂家 / 平台方收款；真实支付先不做，转 TODO。

## Final Status：最终状态

Accepted with Risks

原因：本地可验证范围已经完成并通过编译、DB、runtime/API smoke；真实 PayPal / Stripe / 支付宝 / 微信 / 真实转账按用户要求延期为 TODO，等待 sandbox 配置、证书和公网 webhook 地址。

## Scope：范围

- 新建 `bocoo-module-pay`。
- 增加支付核心实体、Mapper、枚举、Service contract 和基础实现。
- 增加 PostgreSQL `pay_*` DDL、支付字典、Mock 默认应用和 Mock 默认渠道。
- 增加 Mock dev/local smoke API。
- 增加 Wallet 本地支付扣余额能力和 Transfer 建单骨架。
- 增加订单侧内部 API。
- 增加支付管理后端查询入口，包含渠道、订单、退款、通知任务、钱包查询。
- 增加真实支付 TODO 和安全审查文档。

## Out of Scope：不做范围

- 不导入整份旧 `ruoyi-vue-pro.sql`。
- 不写入真实支付密钥、证书、Token 或生产连接串。
- 不做真实 PayPal / Stripe / 支付宝 / 微信下单、退款、转账或 webhook 联调。
- 不新增支付管理前端页面和菜单 SQL。
- 不修改订单业务表结构。

## Completed：已完成

- 根 `pom.xml` 增加 `bocoo-module-pay` 模块和支付 SDK 依赖管理。
- `bocoo-admin/pom.xml` 引入 `bocoo-module-pay`。
- 新增 `PayApp`、`PayChannel`、`PayOrder`、`PayOrderExtension`、`PayRefund`、`PayNotifyTask`、`PayNotifyLog`、`PayTransfer`、`PayWallet`、`PayWalletTransaction`、`PayWalletRecharge`、`PayWalletRechargePackage`。
- 新增支付状态、退款状态、转账状态、渠道编码、钱包业务类型枚举。
- 新增 `PayOrderService`、`PayChannelService`、`PayRefundService`、`PayWalletService`、`PayTransferService`、`PayAdminService`。
- Mock 支付提交后会推进订单成功；非 Mock / Wallet 渠道不会伪造成成功。
- Wallet 本地支付会扣余额并推进订单成功；余额不足会失败。
- 新增 `PayApi` / `PayApiImpl` / `PayOrderStatusVo`，供订单业务内部调用。
- 新增 `PayAdminController` 查询入口，渠道配置返回前脱敏。
- 新增 `sql/postgresql/pay.sql` 并已在 DEV PostgreSQL 执行校验。

## Not Completed：未完成

- PayPal / Stripe / 支付宝 / 微信真实客户端和 webhook 验签主流程。
- 真实退款和真实转账联调。
- 支付通知任务异步重试调度。
- 支付管理前端页面、菜单和权限初始化 SQL。
- `pay_channel.config` 写入 API 和 JSON schema 校验。

## Validation Summary：验证摘要

- `codegraph sync`：已执行，索引已是最新。
- `git diff --check`：已执行，无空白错误，仅 CRLF 提示。
- `mvn -pl bocoo-module-pay -am -DskipTests compile`：通过。
- `mvn -pl bocoo-admin -am -DskipTests compile`：通过。
- `mvn -pl bocoo-admin -am -DskipTests package`：通过，产物为 `bocoo-admin/target/dist/bocoo-admin.jar`。
- DEV PostgreSQL：已执行 `sql/postgresql/pay.sql`；只读校验 12 张 `pay_*` 表、Mock 默认应用、Mock 默认渠道存在。
- Runtime/API smoke：从 `bocoo-admin/target/dist` 启动 jar，登录后调用 `/pay/mock/smoke`，HTTP 200，业务 code 200，订单状态 `10`。
- Runtime/API smoke 终验：调用 `/pay/admin/channel/list` 返回 HTTP 200、业务 code 200，渠道配置已脱敏。
- 临时服务已停止，`8081` 已释放。

## Remaining Risks：剩余风险

- 真实支付渠道未解冻前，PayPal / Stripe / 支付宝 / 微信均不能用于真实收款。
- webhook 验签、金额一致性、真实退款/转账幂等仍是解冻真实支付前的强制项。
- `pay_channel.config` 当前实体为 `String`，后续写入 API 需要校验 JSON 并保证脱敏。
- 管理查询权限注解已加，但菜单/角色初始化尚未落地。
- 支付 SDK 依赖已纳入构建，但真实渠道客户端未启用，解冻时还需 targeted compatibility smoke。

## Lessons from Learn：经验提炼

- 支付迁移应先固化付款方 / 收款方租户边界，再落表和 service，避免旧 `tenant_id` 语义误用。
- 没有真实支付配置和公网 webhook 地址时，应保留 SDK / 表结构 / TODO，但禁止伪造真实渠道成功。
- 支付配置查询必须默认脱敏；真实写入接口和前端页面应晚于权限与脱敏策略。
- 本项目 dist 产物启动路径是 `bocoo-admin/target/dist/bocoo-admin.jar`，不是 `target/bocoo-admin.jar`。

## Key Decisions：关键决策

- 商户是付款方，厂家 / 平台方是收款方。
- `pay_app` / `pay_channel` 归收款侧管理，商户只能查看自己的付款记录和支付状态。
- 交易表显式使用 `payer_tenant_id` / `payee_tenant_id`。
- `pay_channel.config` 使用 PostgreSQL `jsonb`，接口输出必须脱敏。
- 真实支付先转 TODO；Mock 和 Wallet 本地支付作为当前可验证链路。

## Files Modified：修改文件

- `pom.xml`
- `bocoo-admin/pom.xml`
- `bocoo-module-pay/**`
- `sql/postgresql/pay.sql`
- `.ai/requirements/20260530-paypal-pay-module-migration.md`
- `.ai/changes/20260530-paypal-pay-module-migration/**`
- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/MEMORY.md`

## Artifacts：产物

- `.ai/changes/20260530-paypal-pay-module-migration/design.md`
- `.ai/changes/20260530-paypal-pay-module-migration/wave-plan.md`
- `.ai/changes/20260530-paypal-pay-module-migration/real-payment-todo.md`
- `.ai/changes/20260530-paypal-pay-module-migration/security-review.md`
- `.ai/changes/20260530-paypal-pay-module-migration/task-events.jsonl`
- `sql/postgresql/pay.sql`

## Follow-up：后续事项

- 解冻真实支付：提供 sandbox / 生产商户配置、证书或 webhook secret、公网回调地址。
- 实现 PayPal / Stripe / 支付宝 / 微信真实下单、退款、webhook / notify 验签。
- 实现真实转账提交、状态查询和回调同步。
- 增加支付管理前端页面、菜单和权限 SQL。
- 增加 `pay_channel.config` 写入 API、JSON schema 校验和敏感字段脱敏。
- 根据实际订单业务接入 `PayApi`，并保证订单状态推进幂等。
