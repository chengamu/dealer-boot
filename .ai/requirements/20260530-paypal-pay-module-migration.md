# Requirement: 全渠道支付模块迁移评估与落地计划

## Goal：目标

后期订单业务需要集成支付，为避免后续重复迁移成本，将 `D:\work\amu-boot\amu-module-pay` 作为来源迁移为当前项目的 `bocoo-module-pay`。本系统的业务模型是：商户下单并付款，收款方是厂家 / 平台方；支付渠道配置只归厂家 / 平台方管理。迁移范围纳入 PayPal、支付宝、微信、Stripe、Mock、Wallet，以及支付订单、退款、转账、钱包、通知任务等旧模块核心能力；真实联调按密钥、商户资质和公网回调条件分层验收。

## Background：背景

旧项目存在完整支付模块，包含支付应用、支付渠道、订单、订单扩展单、退款、转账、钱包、通知任务、定时同步，以及 PayPal、支付宝、微信、Stripe、Mock、Wallet 等客户端。当前项目是 `base-boot`，包名、模块命名、公共组件、租户上下文、MyBatis 基类、时间规则、i18n 和 PostgreSQL 约束都不同，不能简单复制目录后直接编译。

已核验：

- 旧模块路径：`D:\work\amu-boot\amu-module-pay`
- 旧 SQL 路径：`D:\work\amu-boot\sql\postgresql`
- 旧 SQL 中找到支付字典和菜单插入，但未找到 `pay_*` 建表 DDL。
- 旧 PayPal 客户端有下单、订单查询、退款、退款查询基础实现。
- 旧 Stripe 客户端有下单、webhook 验签、订单查询、退款基础实现。
- 旧支付宝、微信客户端覆盖多种渠道，整体比 PayPal 更接近完整形态。
- 旧 PayPal webhook 验签方法存在，但主流程中是 TODO，未真正强制执行。
- 旧 `PayChannelServiceImpl.parseConfig()` 只识别支付宝、微信、None，未接入 PayPal / Stripe 配置解析，虽然 `PayChannelDO` type handler 兼容了 PayPal / Stripe。
- 旧依赖版本来源包括：`alipay-sdk-java 4.40.771.ALL`、`weixin-java-pay 4.8.2-20260501.180637`、`stripe-java 31.0.0`、`checkout-sdk 1.0.5`。

## Scope：范围

- 新建 `bocoo-module-pay` 模块并接入当前 Maven 多模块。
- 迁移支付核心模型：`pay_app`、`pay_channel`、`pay_order`、`pay_order_extension`、`pay_refund`、`pay_transfer`、`pay_notify_task`、`pay_notify_log`、`pay_wallet`、`pay_wallet_transaction`、`pay_wallet_recharge`、`pay_wallet_recharge_package`。
- 迁移全渠道客户端：PayPal、支付宝、微信、Stripe、Mock、Wallet。
- 迁移订单、退款、转账、钱包、通知任务、定时同步的后端主链路。
- 按当前业务改造支付归属：厂家 / 平台方配置收款渠道，商户只能作为付款方发起支付和查看自己的付款记录。
- 从旧 SQL 中拆出支付字典、菜单和必要初始化数据，并补齐 PostgreSQL 建表脚本。
- 对齐当前项目包名、BaseEntity、BaseMapperPlus、TenantContextHolder、UTC 时间、i18n 和权限风格。

## Out of Scope：不做范围

- 不把旧 demo 案例页面作为业务功能验收；demo 代码只可作为 smoke 或开发辅助参考。
- 不直接导入整份 `ruoyi-vue-pro.sql`。
- 不写入真实 PayPal、支付宝、微信、Stripe 密钥、证书、Token 或生产连接串。
- 不绕过 PayPal、Stripe、支付宝、微信 webhook / notify 验签。
- 不改订单业务表结构，除非后续订单模块 contract 明确需要支付字段。

## Business Rules：业务规则

- 商户是付款方，厂家 / 平台方是收款方。
- `pay_app` / `pay_channel` 默认归厂家 / 平台方租户管理；商户无权新增、修改、查看完整渠道密钥。
- 商户下单时，订单业务调用支付模块创建支付单，支付模块使用厂家 / 平台方的启用渠道发起收款。
- 支付成功后，支付模块维护支付状态，并通过内部 Service 或受控通知推进订单状态。
- 所有外部支付回调必须校验签名；缺少必要配置或 header 时不能按支付成功处理。
- 支付金额统一以最小货币单位存储，展示或调用渠道 SDK 时再转换为对应渠道格式。
- 支付状态更新必须幂等，重复 webhook / notify 或主动查询不能重复扣款、重复通知订单业务。
- 退款原则上由厂家 / 平台方发起；商户如需申请退款，应进入业务审批或售后流程，不能直接调用渠道退款。

## API Requirements：接口要求

- 厂家 / 平台管理端需要支付应用、支付渠道、支付订单、退款、通知记录查询接口。
- 商户侧需要创建支付单、提交支付、查询自己付款状态的接口。
- 外部支付回调接口必须允许支付平台服务器匿名访问，但只在验签通过后处理。
- 支付模块对订单模块暴露内部 API，避免订单模块直接操作 `pay_*` 表。
- 渠道配置接口返回时必须脱敏，不返回完整 secret、私钥、证书内容。

## Data Requirements：数据要求

- PostgreSQL 是唯一目标数据库。
- 需要补齐 `pay_*` 建表脚本，使用项目现有 UTC 规则和 `timestamptz` 约束。
- `pay_channel.config` 建议使用 `jsonb`，但反序列化不能依赖旧包名 `@class`；需要明确 channel code 到 config class 的映射。
- 支付订单建议区分付款方与收款方，例如 `payer_tenant_id`、`payee_tenant_id` 或等价字段；最终字段名在 Wave 0 结合当前订单模型确认。
- 需要设计索引：支付单号、商户订单号、渠道订单号、付款方租户、收款方租户、通知任务状态/下次执行时间。

## Permission / Tenant Rules：权限 / 租户规则

- 支付配置属于敏感配置，仅厂家 / 平台方具备管理权限。
- 商户只能访问自己作为付款方产生的支付记录和状态，不得访问其他商户记录或渠道密钥。
- 厂家 / 平台方可查看作为收款方的支付订单、退款、通知记录。
- PayPal / Stripe / 支付宝 / 微信回调接口可 `@SaIgnore`，但必须验签。
- `pay_*` 表是否全部启用 tenant interceptor 需要逐表确认；对同时包含付款方和收款方的表，不能简单照搬旧 `TenantBaseDO`。

## i18n / UTC Rules：国际化 / 时间规则

- 新增可见前端文案走项目现有 i18n 规则。
- 后端错误消息遵守当前 JSON i18n 路线，避免新增硬编码用户提示。
- 支付成功时间、退款时间、通知时间走 UTC 语义，避免 `LocalDateTime.now()` 直接落库。

## Options：可选方案

### Option A：整包迁移旧模块

Pros：功能覆盖多，包含国内支付、钱包、转账、demo、通知任务。
Cons：包名、公共 starter、租户、BaseDO、校验包、SQL、Swagger、Job、权限全部不匹配；风险大，且旧模型未直接表达“商户付款给厂家 / 平台”。

### Option B：新建 `bocoo-module-pay`，迁移全渠道核心能力

Pros：一次完成支付核心框架和主要渠道迁移，后续不会因为支付宝、微信、Stripe、Wallet 再重复大规模迁移；可按当前业务重塑付款方 / 收款方边界。
Cons：第一批改动范围更大，需要依赖管理、DB DDL、权限、前端管理页和多渠道验签一起审查。

### Option C：只写单渠道轻量 PayPal Service

Pros：最快接入 PayPal。
Cons：后续退款、通知记录、渠道配置、订单幂等、管理端审计、国内支付都要重新补，容易形成一次性代码。

## Recommended Option：推荐方案

推荐 Option B：新建 `bocoo-module-pay`，迁移旧模块的全渠道核心能力，并按当前业务改造成厂家 / 平台统一收款、商户付款。编译和静态安全验收覆盖所有迁移渠道；真实联调按可用配置分层执行，至少 Mock 主链路必须通过，PayPal / 支付宝 / 微信 / Stripe 需要对应 sandbox 或商户配置才做真实联调。

## Risks：风险

- 旧 SQL 中未发现 `pay_*` 建表 DDL，需要从 DO 和当前业务模型反推并人工生成 PostgreSQL 脚本。
- PayPal webhook 验签在旧代码中未启用，迁移时必须补上，否则存在伪造支付成功风险。
- 支付归属模型变为“商户付款、厂家 / 平台收款”，旧 `tenant_id` 语义不能照搬。
- 微信依赖版本是快照风格时间戳版本，需要确认仓库可解析；如果不可解析，必须选稳定版本并说明差异。
- 支付宝 SDK 和 PayPal SDK 排除了旧 `bcprov-jdk15on`，当前项目已有 `bcprov-jdk15to18`，需要避免 BouncyCastle 版本冲突。
- 旧模块大量使用 `javax.validation` / `javax.annotation`，当前 Spring Boot 3 项目需要迁移到 `jakarta.*` 或当前项目已兼容方式。
- 旧模块依赖 `amu.framework.*` 大量工具类，不能照搬 import，需要替换为 `com.bocoo.common.*`。

## Open Questions：待确认问题

- 厂家 / 平台方在当前系统里具体对应哪类租户或账号：平台租户、厂家租户，还是固定平台配置。
- 第一批是否需要完整管理端前端页面一起迁移，还是先做后端 API + 最小配置页。
- 订单业务模块的表和 API 尚未明确，支付成功后是通过内部 Service 回调，还是先预留 `notifyUrl`。
- PayPal / Stripe / 支付宝 / 微信回调公网地址需要域名或可公网访问地址；在没有正式域名前只能做 sandbox / 本地隧道 / mock 验证。

## Acceptance Criteria：验收标准

- `bocoo-module-pay` 能加入当前 Maven 多模块并编译通过。
- PayPal、支付宝、微信、Stripe、Mock、Wallet 渠道配置可保存，敏感字段返回脱敏。
- 渠道配置仅厂家 / 平台方可管理，商户不可读取完整密钥或修改收款渠道。
- 商户可创建支付单并按渠道提交支付；没有真实渠道配置时至少 Mock 支付链路可 smoke。
- PayPal、Stripe、支付宝、微信回调必须验签后才更新支付状态。
- 重复回调 / 主动查询幂等，不重复通知订单业务。
- PostgreSQL `pay_*` DDL 独立、可审查，不直接导入整份旧 SQL。
- 通过 `mvn -DskipTests compile`，并完成至少 Mock 主链路 smoke；有 sandbox 配置的渠道执行对应 targeted smoke。

## Related Decisions：相关决策

- 2026-05-30：支付模块迁移扩大为全渠道核心能力迁移，PayPal、支付宝、微信、Stripe、Mock、Wallet 均纳入模块；真实联调按配置条件分层验收。
- 2026-05-30：支付业务模型是商户付款给厂家 / 平台方，渠道配置归厂家 / 平台方管理，商户不管理收款渠道。
- 2026-05-30：旧 SQL 只能作为参考来源，不能整份导入当前项目。
