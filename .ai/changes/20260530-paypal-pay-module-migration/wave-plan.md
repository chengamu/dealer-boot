# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 2
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only

## Requirement Source

- `.ai/requirements/20260530-paypal-pay-module-migration.md`

## Analysis Summary

- 旧 `amu-module-pay` 可作为迁移参考，但不适合整包复制。
- 迁移范围已扩大为全渠道核心能力：PayPal、支付宝、微信、Stripe、Mock、Wallet，以及订单、退款、转账、钱包、通知任务。
- 当前业务模型是商户下单付款，厂家 / 平台方收款；`pay_app` / `pay_channel` 归厂家 / 平台方管理。
- 支付订单需要明确付款方租户与收款方租户，旧模块单一 `tenant_id` 语义不能照搬。
- 旧 PayPal 客户端有下单、查询、退款基础逻辑，但 webhook 验签没有接入主流程。
- 旧 Stripe 客户端已接入 webhook 验签；支付宝、微信也有各自 notify 验签逻辑，迁移时要保留。
- 旧 `PayChannelServiceImpl.parseConfig()` 未按 `paypal` / `stripe` 选择对应 config，迁移时必须修正为全渠道映射。
- `D:\work\amu-boot\sql\postgresql\ruoyi-vue-pro.sql` 未发现 `pay_*` 建表 DDL，只发现支付字典、菜单和状态数据。

## Recommendation

新建 `bocoo-module-pay`，迁移全渠道核心能力，并按当前业务改造成厂家 / 平台统一收款、商户付款。真实支付联调按配置条件分层执行。至少 Mock 主链路必须 smoke，PayPal / 支付宝 / 微信 / Stripe 需要 sandbox 或商户配置才做真实联调。

## Risks

- 旧模块依赖体系差异大，编译问题会集中在 validation、annotation、tenant、json type handler、swagger、job、redis lock。
- 付款方 / 收款方租户边界如果设计错误，会导致商户越权查看渠道配置或支付记录。
- 支付平台官方回调需要公网可访问地址；无域名时只能做 sandbox + mock 或临时隧道验证。
- 微信依赖版本可能需要稳定化；支付宝、PayPal SDK 与当前 BouncyCastle 依赖需要冲突检查。

## Pause Points

- 如果需要修改订单业务表结构，暂停并回到 `/plan` 明确 DB contract。
- 如果任一支付 SDK 依赖需要升级或换版本，暂停说明版本和兼容性理由。
- 如果厂家 / 平台方在当前系统中的租户类型或账号模型不明确，先确认后再落代码。
- 如果没有 sandbox / 商户配置，真实联调降级为 Mock smoke，并记录风险。

## Wave 0: Contract / Scope / File Boundary

### Task C1: 定义支付模块边界与订单 contract

TaskId: C1
Title: 定义支付模块边界与订单 contract
Owner: java-architect
AgentRole: contract
Status: done
Priority: high
RiskLevel: high，原因是支付状态、订单回调、金额和租户边界错误会直接影响交易安全。
Wave: 0

Files:
- `pom.xml`
- `bocoo-admin/pom.xml`
- `bocoo-module-pay/**`
- `bocoo-modules-system/**`
- `D:\work\amu-boot\amu-module-pay/**`

Forbidden:
- `**/src/main/resources/application-prod.yml`
- `**/migration/**`
- `.ai/archive/**`
- 真实支付密钥、证书或 Token

DependsOn: none
ParallelGroup: payment-contract
ConflictBoundary: contract-only

Acceptance:
- 明确第一批迁移全渠道核心能力，但真实联调按配置条件分层。
- 明确商户是付款方，厂家 / 平台方是收款方。
- 明确支付渠道配置只归厂家 / 平台方管理，商户不能管理收款渠道。
- 明确订单业务如何调用支付模块、支付成功如何通知订单业务。
- 明确金额单位、币种字段、状态枚举、付款方租户、收款方租户、权限边界。

### Task C2: 设计 PostgreSQL pay_* DDL

TaskId: C2
Title: 设计 PostgreSQL pay_* DDL
Owner: java-architect
AgentRole: database-contract
Status: done
Priority: high
RiskLevel: high，原因是旧 SQL 未提供 pay 表建表脚本，需要从 DO 和当前项目规则反推。
Wave: 0

Files:
- `D:\work\amu-boot\amu-module-pay\src\main\java\amu\module\pay\dal\dataobject\**`
- `D:\work\amu-boot\sql\postgresql\ruoyi-vue-pro.sql`
- `bocoo-admin/src/main/resources/sql/**`
- `bocoo-module-pay/**`

Forbidden:
- 直接导入整份 `ruoyi-vue-pro.sql`
- MySQL 脚本
- `.ai/archive/**`

DependsOn: none
ParallelGroup: payment-db-contract
ConflictBoundary: sql-design-only

Acceptance:
- 列出必要 `pay_*` 表、字段、索引、tenant_id 策略、UTC 时间字段。
- 明确付款方租户与收款方租户字段设计。
- 明确 `pay_channel.config` 的 `jsonb` 结构和脱敏要求。
- 明确初始化字典和菜单数据如何适配当前 `sys_*` 表。

### Task C3: 定义全渠道依赖与验签 contract

TaskId: C3
Title: 定义全渠道依赖与验签 contract
Owner: java-architect
AgentRole: dependency-contract
Status: done
Priority: high
RiskLevel: high，原因是支付 SDK 版本和回调验签直接影响编译稳定性与支付安全。
Wave: 0

Files:
- `pom.xml`
- `bocoo-module-pay/pom.xml`
- `D:\work\amu-boot\amu-dependencies\pom.xml`
- `D:\work\amu-boot\amu-module-pay\pom.xml`
- `D:\work\amu-boot\amu-module-pay\src\main\java\amu\module\pay\framework\pay\core\client\impl\**`

Forbidden:
- 真实支付密钥、证书或 Token
- 未说明原因的依赖升级
- `.ai/archive/**`

DependsOn: none
ParallelGroup: payment-dependencies
ConflictBoundary: dependency-design-only

Acceptance:
- 明确支付宝、微信、PayPal、Stripe SDK 版本和 exclusions。
- 明确每个渠道的 notify / webhook 验签要求。
- 明确无密钥时的 Mock smoke 策略。

## Barrier 0

Checks:
- 全渠道核心迁移 scope 已确认。
- 商户付款、厂家 / 平台方收款边界已确认。
- DB schema 草案已明确，且未直接导入旧 SQL。
- 所有外部支付回调验签被列为强制验收。

## Wave 1: Module Skeleton / Core Migration

### Task B1: 新建 bocoo-module-pay 模块骨架

TaskId: B1
Title: 新建 bocoo-module-pay 模块骨架
Owner: java-architect
AgentRole: backend
Status: done
Priority: high
RiskLevel: medium，原因是会修改 Maven 多模块和启动依赖。
Wave: 1

Files:
- `pom.xml`
- `bocoo-admin/pom.xml`
- `bocoo-module-pay/pom.xml`
- `bocoo-module-pay/src/main/java/**`

Forbidden:
- `admin-ui/**`
- `**/src/main/resources/application-prod.yml`
- `.ai/archive/**`

DependsOn:
- C1
- C2
- C3

ParallelGroup: backend-module
ConflictBoundary: pay-module-skeleton

Acceptance:
- 模块命名为 `bocoo-module-pay` 或按当前项目复数命名约定最终确认。
- 包名统一为 `com.bocoo.pay` 或经确认的项目命名。
- Maven 编译路径能识别新模块。
- 支付 SDK 依赖进入依赖管理，且说明版本来源。

### Task B2: 迁移支付核心实体、Mapper、Service contract

TaskId: B2
Title: 迁移支付核心实体、Mapper、Service contract
Owner: java-architect
AgentRole: backend
Status: done
Priority: high
RiskLevel: high，原因是涉及支付状态幂等和租户数据边界。
Wave: 1

Files:
- `bocoo-module-pay/src/main/java/**/domain/**`
- `bocoo-module-pay/src/main/java/**/mapper/**`
- `bocoo-module-pay/src/main/java/**/service/**`
- `bocoo-module-pay/src/main/java/**/enums/**`

Forbidden:
- `admin-ui/**`
- `bocoo-modules-system/src/main/java/**`
- `.ai/archive/**`

DependsOn:
- C1
- C2

ParallelGroup: backend-core
ConflictBoundary: pay-core-only

Acceptance:
- 使用当前项目 `BaseEntity`、`BaseMapperPlus`、`TenantContextHolder`。
- 状态更新必须按旧模块思路保留幂等条件更新。
- 不保留旧 `amu.framework.*` import。
- 覆盖订单、退款、转账、钱包、通知任务核心 service contract。
- 商户付款方与厂家 / 平台收款方边界在实体和查询条件中可表达。

### Task B3: 补齐 PostgreSQL DDL 与初始化数据

TaskId: B3
Title: 补齐 PostgreSQL DDL 与初始化数据
Owner: java-architect
AgentRole: database
Status: done
Priority: high
RiskLevel: high，原因是用户已允许 DB/架构修改，但支付表必须可审查、可回滚。
Wave: 1

Files:
- `bocoo-admin/src/main/resources/sql/**`
- `bocoo-module-pay/**`
- `.ai/changes/20260530-paypal-pay-module-migration/**`

Forbidden:
- MySQL 脚本
- 直接覆盖旧 SQL
- `.ai/archive/**`

DependsOn:
- C2

ParallelGroup: database
ConflictBoundary: pay-sql-only

Acceptance:
- 独立生成 PostgreSQL `pay_*` DDL。
- 支付字典、菜单、权限初始化适配当前 `sys_dict_*` / `sys_menu` 结构。
- 覆盖全渠道字典：PayPal、支付宝、微信、Stripe、Mock、Wallet。
- 不写入真实支付渠道配置。

## Barrier 1

Checks:
- Maven 模块、核心代码、DDL 文件边界不冲突。
- 付款方 / 收款方租户边界可表达。
- 未引入真实敏感配置。

## Wave 2: Channel Integration / Security

### Task P1: 迁移并修复 PayPal / Stripe 客户端

TaskId: P1
Title: 迁移并修复 PayPal / Stripe 客户端
Owner: java-architect
AgentRole: backend
Status: pending
Priority: high
RiskLevel: high，原因是 webhook 验签和状态映射直接影响支付安全。
Wave: 2

Files:
- `bocoo-module-pay/src/main/java/**/framework/pay/core/client/**`
- `bocoo-module-pay/src/main/java/**/controller/**/notify/**`
- `bocoo-module-pay/src/main/java/**/service/**`

Forbidden:
- `admin-ui/**`
- 真实支付密钥
- `.ai/archive/**`

DependsOn:
- B1
- B2
- C3

ParallelGroup: overseas-channels
ConflictBoundary: overseas-client-only

Acceptance:
- PayPal 下单返回 approve URL。
- PayPal webhook 强制验签，验签失败不更新支付成功。
- Stripe 保留 webhook 验签和退款基础能力。
- `parseConfig()` 正确识别 `paypal` / `stripe` 并使用对应 config。
- `LocalDateTime.now()` 替换为项目 UTC 工具或明确兼容处理。

### Task P2: 迁移并修复支付宝 / 微信客户端

TaskId: P2
Title: 迁移并修复支付宝 / 微信客户端
Owner: java-architect
AgentRole: backend
Status: pending
Priority: high
RiskLevel: high，原因是国内支付渠道回调验签、证书配置、退款和转账链路复杂。
Wave: 2

Files:
- `bocoo-module-pay/src/main/java/**/framework/pay/core/client/impl/alipay/**`
- `bocoo-module-pay/src/main/java/**/framework/pay/core/client/impl/weixin/**`
- `bocoo-module-pay/src/main/java/**/framework/pay/core/client/**`
- `bocoo-module-pay/src/main/java/**/service/**`

Forbidden:
- `admin-ui/**`
- 真实支付宝/微信密钥或证书
- `.ai/archive/**`

DependsOn:
- B1
- B2
- C3

ParallelGroup: domestic-channels
ConflictBoundary: domestic-client-only

Acceptance:
- 支付宝 PC/Wap/App/扫码/条码客户端可编译并保留 notify 验签。
- 微信 JSAPI/小程序/App/Native/Wap/条码客户端可编译并保留 notify 验签。
- 支付宝/微信退款基础链路迁移。
- 没有真实商户配置时不伪造联调通过。

### Task P3: 迁移 Mock / Wallet / Transfer 能力

TaskId: P3
Title: 迁移 Mock / Wallet / Transfer 能力
Owner: java-architect
AgentRole: backend
Status: pending
Priority: medium
RiskLevel: medium，原因是钱包和转账会增加业务面，但能避免后续再次迁移旧模块。
Wave: 2

Files:
- `bocoo-module-pay/src/main/java/**/framework/pay/core/client/impl/mock/**`
- `bocoo-module-pay/src/main/java/**/framework/pay/core/client/impl/wallet/**`
- `bocoo-module-pay/src/main/java/**/service/wallet/**`
- `bocoo-module-pay/src/main/java/**/service/transfer/**`
- `bocoo-module-pay/src/main/java/**/controller/**/wallet/**`
- `bocoo-module-pay/src/main/java/**/controller/**/transfer/**`

Forbidden:
- `admin-ui/**`
- `.ai/archive/**`

DependsOn:
- B1
- B2

ParallelGroup: wallet-transfer
ConflictBoundary: wallet-transfer-only

Acceptance:
- Mock 支付可作为本地 smoke 渠道。
- Wallet 支付、钱包流水、充值包核心后端链路可编译。
- Transfer 基础 service 和状态同步迁移；真实转账联调按渠道资质另行执行。

### Task I1: 对接订单侧内部 API contract

TaskId: I1
Title: 对接订单侧内部 API contract
Owner: java-architect
AgentRole: backend
Status: pending
Priority: medium
RiskLevel: high，原因是订单支付成功后的业务状态推进必须幂等。
Wave: 2

Files:
- `bocoo-module-pay/src/main/java/**/api/**`
- `bocoo-module-pay/src/main/java/**/service/**`
- `bocoo-modules-system/src/main/java/**`

Forbidden:
- 未经确认修改订单业务表结构
- `admin-ui/**`
- `.ai/archive/**`

DependsOn:
- C1
- B2
- P1
- P2
- P3

ParallelGroup: order-contract
ConflictBoundary: payment-order-contract

Acceptance:
- 支付模块提供创建支付单、提交支付、查询支付状态的内部接口。
- 订单业务不直接操作 `pay_*` 表。
- 支付成功通知订单业务必须幂等。
- 订单侧以商户为付款方、厂家 / 平台方为收款方。

## Barrier 2

Checks:
- PayPal / Stripe / 支付宝 / 微信 webhook 或 notify 验签路径完整。
- 支付状态更新具备幂等保护。
- 订单 contract 未越权修改 DB。

## Wave 3: Admin / Review

### Task F1: 规划或迁移最小管理端能力

TaskId: F1
Title: 规划或迁移最小管理端能力
Owner: frontend-developer
AgentRole: frontend
Status: pending
Priority: medium
RiskLevel: medium，原因是支付配置涉及敏感字段脱敏和权限控制。
Wave: 3

Files:
- `admin-ui/src/api/**`
- `admin-ui/src/pages/**`
- `i18n/locales/en_US.json`
- `i18n/locales/zh_CN.json`

Forbidden:
- 直接写真实支付密钥
- 绕过 i18n 规则新增可见文案
- `.ai/archive/**`

DependsOn:
- P1
- P2
- P3

ParallelGroup: frontend
ConflictBoundary: pay-admin-ui

Acceptance:
- 至少具备厂家 / 平台方渠道配置、支付订单查询、退款查询、通知记录查询、钱包基础查询的最小管理入口，或明确第一批后端优先、前端延后。
- 商户侧只能看到自己的付款记录和支付状态。
- 敏感配置字段展示脱敏。

### Task R1: 支付安全与迁移审查

TaskId: R1
Title: 支付安全与迁移审查
Owner: code-reviewer
AgentRole: code-reviewer
Status: pending
Priority: high
RiskLevel: high，原因是支付模块必须重点审查伪造回调、重复通知、越权访问和敏感信息泄露。
Wave: 3

Files:
- `bocoo-module-pay/**`
- `bocoo-admin/src/main/resources/sql/**`
- `admin-ui/src/**`

Forbidden:
- `.ai/archive/**`

DependsOn:
- P1
- P2
- P3
- I1

ParallelGroup: review
ConflictBoundary: review-only

Acceptance:
- 审查 webhook / notify 验签、金额单位、幂等更新、付款方/收款方租户隔离、权限、敏感字段脱敏、日志脱敏。
- 审查 SDK 依赖版本、BouncyCastle 冲突、微信快照版本可解析性。
- 列出必须修复项和可延后项。

## Barrier 3

Checks:
- code-reviewer 不和 implementation tasks 同 Wave。
- 高风险支付安全项已完成审查。

## Wave 4: Check / Validation

### Task V1: 编译与支付 smoke 验证

TaskId: V1
Title: 编译与支付 smoke 验证
Owner: main
AgentRole: check
Status: pending
Priority: high
RiskLevel: high，原因是支付集成必须至少通过编译和主链路验证。
Wave: 4

Files:
- `pom.xml`
- `bocoo-admin/pom.xml`
- `bocoo-module-pay/**`
- `admin-ui/**`

Forbidden:
- 输出支付密钥、Token、真实用户支付信息
- `.ai/archive/**`

DependsOn:
- R1

ParallelGroup: validation
ConflictBoundary: validation-only

Acceptance:
- 执行 `codegraph sync`。
- 执行 `git diff --check`。
- 执行 `mvn -DskipTests compile`。
- 如前端有改动，执行 `admin-ui` typecheck/build。
- 执行 Mock 支付主链路 smoke。
- 如具备 PayPal / 支付宝 / 微信 / Stripe sandbox 或商户配置，执行对应 targeted smoke；否则记录未联调原因。
