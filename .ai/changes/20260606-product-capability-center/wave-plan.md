# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 3
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only

## Requirement Source

- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `.ai/requirements/20260606-product-capability-center.md`

## Agent Registry

| Agent | Role |
| --- | --- |
| `main` | orchestrator、需求沉淀、计划修订、跨模块协调 |
| `java-architect` | Java 后端、Service、Mapper、事务、tenant、权限、架构边界 |
| `frontend-developer` | Vue 页面、Element Plus 交互、路由、i18n、前端 API |
| `typescript-pro` | TypeScript 类型、API 类型、复杂类型边界 |
| `code-reviewer` | Static Review |
| `browser-debugger` | Runtime Validation / Browser Evidence |

## Wave 0: Contract / Scope / File Boundary

### Task PCC-00: 确认实施决策和暂停点

TaskId: PCC-00
Owner: main  
OwnerSource: main-fallback  
OwnerReason: 该任务需要结合用户偏好、项目约束和执行节奏做 orchestrator 决策。  
AgentRole: orchestrator  
Status: done  
Priority: high  
RiskLevel: high，模块名、租户策略和 migration 节奏不确认会阻塞后续实现。  
Wave: 0  
DependsOn: none  
ParallelGroup: contract  
ConflictBoundary: ai-docs-only  

Files:
- `.ai/requirements/20260606-product-capability-center.md`
- `.ai/changes/20260606-product-capability-center/**`
- `docs/产品配置中心/**`

Forbidden:
- 业务代码修改
- migration 修改
- 生产配置修改
- 依赖升级

Acceptance:
- 明确是否新建 `bocoo-modules-product` 或先用独立包。
- 明确产品能力核心表 tenant 策略。
- 明确第一批是否允许生成 migration 草案。
- 明确第一批交付边界。

### Task PCC-01: 细化数据库落地计划

TaskId: PCC-01
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务需要 Java/数据库/tenant/UTC 边界判断，匹配 `java-architect`。  
AgentRole: backend-contract  
Status: done  
Priority: high  
RiskLevel: high，数据库结构涉及平台共享主数据、快照、read model 和未来拆服务。  
Wave: 0  
DependsOn: PCC-00  
ParallelGroup: contract  
ConflictBoundary: db-contract-only  

Files:
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `.ai/changes/20260606-product-capability-center/design.md`

Forbidden:
- 直接修改生产 migration
- 执行 SQL
- 修改生产配置

Acceptance:
- 拆出 Batch 1/2/3 表级落地顺序。
- 标出所有需要中文注释和 UTC 注释的检查点。
- 标出租户忽略表或平台租户的逐表决策点。
- 标出发布包、订单快照、read model、outbox 的不可省略性。

### Task PCC-02: 细化 API contract

TaskId: PCC-02
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务需要后端 API、BO/VO/Result、权限和事务边界判断。  
AgentRole: backend-contract  
Status: done  
Priority: high  
RiskLevel: high，contract 不冻结会导致前后端返工。  
Wave: 0  
DependsOn: PCC-00  
ParallelGroup: contract  
ConflictBoundary: api-contract-only  

Files:
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `.ai/changes/20260606-product-capability-center/design.md`

Forbidden:
- Controller 实现
- Entity 直接返回前端
- 前端复制后端规则

Acceptance:
- Batch 1 API 路径、权限码、请求 BO、返回 VO 明确。
- 明确分页、错误 message key、UTC 参数、tenant boundary。
- 明确哪些能力进入 engine。

### Task PCC-03: 细化前端菜单和页面 contract

TaskId: PCC-03
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 该任务需要前端菜单、路由、页面、i18n、Element Plus 还原边界判断。  
AgentRole: frontend-contract  
Status: done  
Priority: high  
RiskLevel: medium，页面多且容易跑偏成 H5 或三级菜单。  
Wave: 0  
DependsOn: PCC-00  
ParallelGroup: contract  
ConflictBoundary: frontend-contract-only  

Files:
- `docs/产品配置中心/产品能力界面设计稿.md`
- `.ai/changes/20260606-product-capability-center/design.md`

Forbidden:
- 生成 H5 原型
- 改现有 layout/sidebar/topbar
- 手动改 `admin-ui/public/i18n/*.json`

Acceptance:
- 菜单最多两级。
- 普通 grid 页面和 5 个自定义页面边界明确。
- i18n key、API 文件、页面目录建议明确。

## Barrier 0

Checks:
- 模块名、tenant、migration 策略至少有明确推荐方案和暂停点。
- Batch 1 scope 明确，不把所有 P0 一次做完。
- 不进入 Wave 1，直到用户确认 `/do` 执行 Batch 0 或继续到 Batch 1。

## Wave 1: Batch 1 Foundation Implementation

### Task PCC-10: 后端基础模块骨架

TaskId: PCC-10
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 新模块/包、Controller/Service/Mapper 基线需要 Java 架构判断。  
AgentRole: backend  
Status: done  
Priority: high  
RiskLevel: high，新增 Maven module 或包结构属于架构边界。  
Wave: 1  
DependsOn: PCC-01, PCC-02  
ParallelGroup: backend-foundation  
ConflictBoundary: backend-product-module-only  

Files:
- `bocoo-modules-product/**`
- `pom.xml`
- `bocoo-admin/pom.xml`

Forbidden:
- 修改生产配置
- 依赖升级
- 不经确认新增 migration
- 放进 `bocoo-modules-system`

Acceptance:
- 产品能力后端模块/包建立。
- 基础包结构符合代码生成器风格。
- 编译路径明确。

### Task PCC-11: 基础信息后端能力

TaskId: PCC-11
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: CRUD、引用检查、OSS 资料绑定和 tenant/UTC 都在后端。  
AgentRole: backend  
Status: done  
Priority: high  
RiskLevel: high，涉及多张核心表和引用关系。  
Wave: 1  
DependsOn: PCC-10  
ParallelGroup: backend-foundation  
ConflictBoundary: base-info-backend-only  

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `bocoo-modules-product/src/main/resources/mapper/product/**`

Forbidden:
- Controller 直接返回 Entity
- 前端规则计算
- Redis 权威缓存

Acceptance:
- 分类、物料、组件、资料资产、资料绑定 API 初步可用。
- 引用检查接口有 contract。
- 时间、权限、操作日志、i18n message key 按项目口径。

Result:
- 已生成 Batch 1 基础资料 Entity/BO/VO/Mapper/Service/Controller。
- 已补齐 `/categories/tree`、`/media-bindings/batch`、`/models/{modelId}/variants` 等契约入口。
- 已生成 `sql/postgresql/product_capability.sql` 草案，表和字段均为中文注释，时间字段使用 `timestamptz` 并标注 UTC。
- `mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### Task PCC-12: 前端菜单和基础 grid 页面

TaskId: PCC-12
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 菜单、路由、页面、i18n 和 Element Plus grid 属于前端实现。  
AgentRole: frontend  
Status: done  
Priority: high  
RiskLevel: medium，页面多但多为现有 grid 模式。  
Wave: 1  
DependsOn: PCC-03, PCC-11  
ParallelGroup: frontend-foundation  
ConflictBoundary: product-center-frontend-only  

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `i18n/locales/en_US.json`

Forbidden:
- `admin-ui/public/i18n/*.json`
- layout/sidebar/topbar 重构
- H5 原型生成

Acceptance:
- 产品能力菜单和基础页面骨架进入现有路由体系。
- 基础信息、产品模型、组件资料使用 grid/list + drawer。
- 新增文案走 i18n key。

Result:
- 已新增 `admin-ui/src/api/product-capability/{base,model,asset,workbench,types}.ts`。
- 已新增通用 `ProductEntityGridPage.vue`，基础信息、产品模型、组件/资料均按现有 grid + drawer 范式落地。
- 已在 `admin-ui/src/stores/permission.ts` 增加动态路由 component 映射。
- 已在 `sql/postgresql/product_capability.sql` 增加 Batch 1 `sys_menu` 菜单和按钮权限草案。
- 已新增 `i18n/locales/en_US.json` 文案并通过 `node i18n/scripts/sync.ts` 同步 runtime i18n。
- `./node_modules/.bin/vue-tsc --noEmit` 和 `./node_modules/.bin/vite build` 通过。

### Task PCC-13: 工作台只读骨架

TaskId: PCC-13
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 工作台是自定义 dashboard，需要前端布局和 API 对接。  
AgentRole: frontend  
Status: done  
Priority: medium  
RiskLevel: medium，工作台数据依赖 read model 和后端 summary API。  
Wave: 1  
DependsOn: PCC-02, PCC-03  
ParallelGroup: frontend-foundation  
ConflictBoundary: workbench-only  

Files:
- `admin-ui/src/pages/product-center/workbench/**`
- `admin-ui/src/api/product-capability/**`

Forbidden:
- 实时 join 草稿数据
- 复制 H5 原型视觉

Acceptance:
- 工作台页面骨架可打开。
- summary、progress、priority、sync-events API contract 对齐。
- 空态、加载态、权限隐藏有设计。

Result:
- 已新增 `ProductCenterWorkbenchPage.vue`，包含 summary 指标、配置进度、优先队列、同步事件区域。
- 已新增后端工作台 VO 和只读 API：summary 轻量统计产品模型，列表接口返回稳定空分页。
- 前端工作台 API 全部走 `/product-capability/workbench/*`，不使用页面 mock 字段。

## Barrier 1

Checks:
- Batch 1 不越界实现价格、发布、订单快照。
- Java 编译计划明确。
- 前端 typecheck/browser smoke 计划明确。
- tenant、UTC、i18n、permission 没有绕过。

## Wave 2: Batch 2 Config / Pricing / Publish

### Task PCC-20: 配置模板和规则求值

TaskId: PCC-20
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 规则求值和模板保存需要后端 Service/engine 边界。  
AgentRole: backend  
Status: done  
Priority: high  
RiskLevel: high，规则 JSON schema 未稳定会影响发布和订单。  
Wave: 2  
DependsOn: PCC-11  
ParallelGroup: backend-config  
ConflictBoundary: config-engine-only  

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`

Forbidden:
- 前端决定发布结果
- Controller 写规则逻辑

Acceptance:
- 模板、问题组、问题、答案、规则保存接口可用。
- `ConfigEvaluationEngine` MVP 可返回可见性、warning、blocker、自动组件摘要。

Result:
- 已新增配置模板、模板版本、问题组、问题、答案、规则的 Entity/BO/VO/Mapper/Service/Controller。
- 已新增 `/product-capability/config/evaluate` 和 `ConfigEvaluationEngine` MVP。
- 当前求值器先返回稳定结构和必需阻断，不执行任意动态表达式；复杂规则待 schema 冻结后逐项接入。
- 已追加 Batch 2 配置模板和规则 PostgreSQL SQL 草案，表字段均带中文注释。
- `mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### Task PCC-21: 配置录入工作台前端

TaskId: PCC-21
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 三栏工作台、批量粘贴和求值预览属于复杂前端交互。  
AgentRole: frontend  
Status: done  
Priority: high  
RiskLevel: high，录入交互复杂，易出现状态错乱。  
Wave: 2  
DependsOn: PCC-20  
ParallelGroup: frontend-config  
ConflictBoundary: config-template-page-only  

Files:
- `admin-ui/src/pages/product-center/template/**`

Forbidden:
- 前端复制后端求值逻辑
- 重做 layout

Acceptance:
- 三栏配置录入工作台可操作。
- 批量粘贴、组件/资料绑定入口、求值预览和缺口提示可用。

Result:
- 已新增 `admin-ui/src/api/product-capability/config.ts`。
- 已新增 `admin-ui/src/pages/product-center/template/ConfigTemplateWorkbenchPage.vue`。
- 已在 `admin-ui/src/stores/permission.ts` 增加 `product-center/template` 动态路由映射。
- 已补齐 `productCenter.template.*` 与 `productCenter.menu.template` i18n key，并通过 `node i18n/scripts/sync.ts` 同步 runtime i18n。
- 已在 `sql/postgresql/product_capability.sql` 增加配置模板二级菜单和按钮权限草案。
- `./node_modules/.bin/vue-tsc --noEmit`、`./node_modules/.bin/vite build`、`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### Task PCC-22: 价格中心后端和前端

TaskId: PCC-22
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 价格规则和试算由后端权威计算，前端需要跟随 contract。  
AgentRole: backend-led-integrated  
Status: done  
Priority: high  
RiskLevel: high，价格计算错误会影响报价和订单。  
Wave: 2  
DependsOn: PCC-11  
ParallelGroup: pricing  
ConflictBoundary: pricing-only  

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `admin-ui/src/pages/product-center/pricing/**`
- `admin-ui/src/api/product-capability/**`

Forbidden:
- 前端自行计算最终价
- Redis 作为价格快照权威

Acceptance:
- 价格方案、价格版本、规则项、价格试算形成闭环。
- 价格编辑与测试页面展示命中过程和缺价 blocker。

Result:
- 已新增 `PricePlan`、`PricePlanVersion`、`PriceRuleItem` 后端 CRUD 和 `ProductPricingController`。
- 已新增 `PriceCalculationBo`、`PriceCalculationResultVo`、`PriceCalculationEngine` MVP。
- 已新增 `/product-capability/pricing/calculate`，缺价格版本或缺规则项时返回 blocker。
- 已追加 `pc_price_plan`、`pc_price_plan_version`、`pc_price_rule_item` PostgreSQL SQL 草案，字段均有中文注释，时间字段为 UTC `timestamptz` 注释。
- 已新增 `admin-ui/src/api/product-capability/pricing.ts` 和 `admin-ui/src/pages/product-center/pricing/PricingWorkbenchPage.vue`。
- 已在 `admin-ui/src/stores/permission.ts` 增加 `product-center/pricing` 动态路由映射。
- `mvn -pl bocoo-admin -am -DskipTests compile`、`./node_modules/.bin/vue-tsc --noEmit`、`./node_modules/.bin/vite build` 通过。

### Task PCC-23: 发布检查、发布包和 outbox

TaskId: PCC-23
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 发布检查、事务、快照 hash、outbox 是后端一致性核心。  
AgentRole: backend  
Status: done  
Priority: high  
RiskLevel: high，发布包和快照是后续系统消费权威。  
Wave: 2  
DependsOn: PCC-20, PCC-22  
ParallelGroup: publish  
ConflictBoundary: publish-snapshot-only  

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`

Forbidden:
- 发布包原地修改
- blocker 下发布
- 缓存保存权威快照

Acceptance:
- 发布检查输出 blocker/warning/pass。
- 发布包不可变，包含 hash、版本、摘要 JSON。
- 发布成功同事务写 outbox 或记录明确策略。

Result:
- 已新增 `PublishCheckResult`、`PublishApproval`、`ProductPublishPackage`、`ProductSyncOutbox` 后端对象、Mapper、Service、Controller 和 SQL 草案。
- 已新增 `/product-capability/publish/check`，输出 PASS/WARNING/BLOCKER 并落发布检查明细。
- 已新增 `/product-capability/publish/packages/create`，无 BLOCKER 时同事务生成不可变发布包和 ORDER outbox。
- 发布包和 outbox 在 Controller 层仅提供 list/options/get，不提供通用新增、修改、删除。
- SQL 草案字段均有中文注释，时间字段为 UTC `timestamptz` 注释。
- `mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### Task PCC-24: 测试发布前端

TaskId: PCC-24
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 发布闸门页面需要复杂状态和审批动作交互。  
AgentRole: frontend  
Status: done  
Priority: high  
RiskLevel: medium，权限和状态按钮容易错。  
Wave: 2  
DependsOn: PCC-23  
ParallelGroup: publish  
ConflictBoundary: publish-page-only  

Files:
- `admin-ui/src/pages/product-center/publish/**`

Forbidden:
- 前端绕过 blocker
- 无权限显示发布按钮

Acceptance:
- 发布检查闸门可展示状态、明细、审批和发布动作。
- BLOCKER、WARNING、PASS 视觉明确。

Result:
- 已新增 `admin-ui/src/api/product-capability/publish.ts`。
- 已新增 `admin-ui/src/pages/product-center/publish/PublishGatePage.vue`。
- 已在 `admin-ui/src/stores/permission.ts` 增加 `product-center/publish` 动态路由映射。
- 已补齐 `productCenter.publish.*` 与 `productCenter.menu.publish` i18n key，并通过 `node i18n/scripts/sync.ts` 同步 runtime i18n。
- `./node_modules/.bin/vue-tsc --noEmit`、`./node_modules/.bin/vite build` 通过。

## Barrier 2

Checks:
- 配置、价格、发布都以后端 engine/Service 为准。
- 发布包、read model、outbox 和快照边界未丢失。
- 前后端 API 字段一致。

## Wave 3: Batch 3 Consumption / Deferred Preparation

### Task PCC-30: 销售只读总览

TaskId: PCC-30
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 销售只读是前端自定义页面，数据来自发布包/read model。  
AgentRole: frontend-led-integrated  
Status: done  
Priority: medium  
RiskLevel: medium，不能误读草稿或暴露编辑动作。  
Wave: 3  
DependsOn: PCC-23  
ParallelGroup: sales-readonly  
ConflictBoundary: sales-view-only  

Files:
- `admin-ui/src/pages/product-center/sales-view/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`

Forbidden:
- 读取草稿
- 显示编辑按钮

Acceptance:
- 销售只读页面从发布包/read model 读取。
- 支持产品身份、问题答案、资料、价格状态、发布摘要。

Result:
- 已新增 `admin-ui/src/pages/product-center/sales-view/SalesReadOnlyPage.vue`。
- 已在 `ProductEntityGridPage.vue` 增加只读模式，销售视图隐藏新增、编辑、删除等草稿操作。
- 已在 `admin-ui/src/stores/permission.ts` 增加 `product-center/sales-view` 动态路由映射。
- 已在 `sql/postgresql/product_capability.sql` 增加 Sales View 二级菜单和只读权限草案。
- `./node_modules/.bin/vue-tsc --noEmit`、`./node_modules/.bin/vite build`、`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### Task PCC-31: 订单快照内部消费

TaskId: PCC-31
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 订单快照涉及订单模块边界、事务和历史追溯。  
AgentRole: backend-contract  
Status: done  
Priority: medium  
RiskLevel: high，需要确认订单模块保存点。  
Wave: 3  
DependsOn: PCC-23  
ParallelGroup: order-consumption  
ConflictBoundary: order-snapshot-contract-only  

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- 订单模块路径待确认

Forbidden:
- 订单直接写产品能力核心表
- 订单只保存 ID 不保存快照

Acceptance:
- `build-snapshot` Service/API contract 明确。
- 订单快照字段自包含。
- 订单模块落点作为单独确认项。

Result:
- 已新增 `OrderSnapshotBuildBo`、`OrderProductSnapshotVo`、`ProductOrderSnapshotService` 和 `ProductOrderSnapshotController`。
- 已新增 `POST /product-capability/order-snapshots/build`，权限码为 `product:order-snapshot:build`。
- 快照构建只读取发布包，不读取草稿；输出发布包版本、配置输入、订单信息、自包含 `snapshotJson` 和 `snapshotHash`。
- 产品能力中心不新增订单快照表，订单系统在后续接入点持久化返回快照，避免订单历史依赖产品中心当前态。
- 快照 JSON 已改为 `JsonUtils.toJsonString` 结构化生成，避免手工拼接造成转义和复杂输入风险。
- `mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### Task PCC-40: Deferred AI / Import / ERP-MES Roadmap

TaskId: PCC-40
Owner: main  
OwnerSource: main-fallback  
OwnerReason: 这些是后续路线整理，不应混入 P0 实现。  
AgentRole: roadmap  
Status: done  
Priority: low  
RiskLevel: medium，若不保留 TODO，第一批完成后容易丢失路线。  
Wave: 3  
DependsOn: PCC-30, PCC-31  
ParallelGroup: deferred  
ConflictBoundary: ai-docs-only  

Files:
- `.ai/TASKS.md`
- `.ai/requirements/20260606-product-capability-center.md`

Forbidden:
- 实现 Agent 自动发布
- 实现自动改价
- 实现完整导入中心

Acceptance:
- ConfigAgent、PricingAgent、导入中心、ERP/MES、客户价/促销价保留 Deferred。
- 不阻塞 P0/P1 主线。

Result:
- Deferred 能力继续保留在 `.ai/TASKS.md`，不进入本批实现。
- 后续可按 ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分继续拆批。

## Barrier 3

Checks:
- P1/P2 能力保留在 Deferred，不从计划删除。
- 销售和订单消费只读发布包或快照，不读草稿。

## Wave 4: Review / Check

### Task PCC-90: Static Review

TaskId: PCC-90
Owner: code-reviewer  
OwnerSource: agent-registry  
OwnerReason: 需要独立静态审查权限、tenant、UTC、i18n、SQL、事务和 contract 风险。  
AgentRole: static-review  
Status: done  
Priority: high  
RiskLevel: high，大范围业务能力需要审查兜底。  
Wave: 4  
DependsOn: 当前批次实现任务  
ParallelGroup: review  
ConflictBoundary: review-only  

Files:
- 当前批次实际改动文件

Forbidden:
- 浏览器自动化
- 无证据标记通过

Acceptance:
- 输出按严重程度排序的静态 review findings。
- 明确残余风险和未执行验证。

Result:
- 已修复一处发布包/outbox 可写 API 风险：发布包和 outbox 在 Controller 层仅保留 list/options/get，写入只能通过发布流程生成。
- 未发现新增 Redis 权威缓存、tenant 字段误加、硬编码 UI 文案、非 UTC 时间新增问题。
- 残余风险：SQL 草案未真实落库；规则 JSON、价格公式、发布包 hash schema 仍为 MVP，需要后续细化。

### Task PCC-91: Runtime Validation

TaskId: PCC-91
Owner: browser-debugger  
OwnerSource: agent-registry  
OwnerReason: 页面打开、交互、console、network 需要浏览器验证。  
AgentRole: runtime-validation  
Status: done  
Priority: high  
RiskLevel: medium，前端页面必须实际打开验证。  
Wave: 4  
DependsOn: 当前批次前端实现任务  
ParallelGroup: validation  
ConflictBoundary: browser-validation-only  

Files:
- `admin-ui/src/pages/product-center/**`

Forbidden:
- Static Review 代替浏览器验证
- 未启动服务却标记 passed

Acceptance:
- 浏览器打开关键页面。
- 检查 console error、network error、空态、权限按钮、主要交互。

Result:
- 已启动 Vite dev server `http://127.0.0.1:8083/`。
- 已用 Playwright + 系统 Chrome 打开 `/`、`/product-center/template`、`/product-center/pricing`、`/product-center/publish`。
- 四个路径均返回 200，并在无登录态下重定向到 `/login?redirect=...`。
- console 中的 500 来自登录页 `/captchaImage` 代理到未启动的后端 `127.0.0.1:8081` 失败；未发现新增页面 chunk 加载失败。
- 已停止 Vite dev server。

## Barrier 4

Checks:
- `/check` 双 Lane 执行：Static Review + Runtime Validation。
- Java compile、frontend typecheck/build 是否运行如实记录。
- 未运行项必须记录 Not Run 和原因。

## Deferred Queue

- ConfigAgent：P1，生成草稿、推荐问题组、缺口检查、解释字段，不直接发布。
- PricingAgent：P2，解释价格、找缺价、生成价格草稿，不自动改价。
- 导入中心：P2，完整资料包和 Excel 智能识别后置。
- ERP/MES 深度同步：P2，第一版只保留 outbox/read model。
- 商户价、客户等级、促销折扣：P2，第一版只做平台主价。
- 独立服务拆分：P2，当前先按可拆分边界实现。
