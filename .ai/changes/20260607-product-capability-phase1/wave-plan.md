# Wave Plan

ChangeId: `20260607-product-capability-phase1`

Requirement Source:

- `.ai/requirements/20260606-product-capability-center.md`
- `docs/产品配置中心/共享产品能力中心开发入口.md`

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 3
- UseWorktree: true
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only
- DonePolicy: 只有通过入口文档第 7 节相关验收门，任务才能标记为 `done`
- RuntimeValidation: 必须使用内部浏览器真实地址、真实登录态、真实开发库

## Agent Registry

| Agent | Role Match | Source |
| --- | --- | --- |
| `java-architect` | Java、Spring、SQL、database、backend、permission、transaction | `.codex/agents/java-architect.toml` |
| `frontend-developer` | Vue、UI、component、CSS、frontend implementation | `.codex/agents/frontend-developer.toml` |
| `typescript-pro` | TypeScript、API typing、前端类型边界 | `.codex/agents/typescript-pro.toml` |
| `code-reviewer` | Static Review、quality、security、maintainability | `.codex/agents/code-reviewer.toml` |
| `browser-debugger` | Browser、runtime validation、UI evidence、network/console | `.codex/agents/browser-debugger.toml` |
| `main` | orchestrator、fallback、总验收和状态收口 | 当前 Codex 主 Agent |

## CodeGraph Sync

- `/plan` 已在项目根目录执行 `codegraph sync`。
- 结果：`Already up to date`，`Done`。
- 说明：这只刷新索引，不等同于 build/test/lint/browser 验收。

## Wave 0: Contract / Scope / File Boundary

### Task PCC-P1-C01: 冻结第一阶段 contract 与文件边界

TaskId: PCC-P1-C01  
Title: 冻结第一阶段 contract 与文件边界  
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务需要把入口文档、后端模块、数据库、API 和真实完成口径收敛成 contract，匹配 Java/架构类 Agent。  
AgentRole: contract  
Status: pending  
Priority: high  
RiskLevel: high，因为旧 Batch 的 `done` 语义已经污染，必须先收敛完成定义。  
Wave: 0  

Files:

- `.ai/requirements/20260606-product-capability-center.md`
- `.ai/changes/20260607-product-capability-phase1/**`
- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`

Forbidden:

- `bocoo-modules-product/src/**`
- `admin-ui/src/**`
- `sql/postgresql/*.sql`
- `pom.xml`
- `package.json`
- `.ai/archive/**`

DependsOn: none  
ParallelGroup: contract  
ConflictBoundary: ai-docs-only  

Acceptance:

- 明确第一阶段 Scope 和 Out of Scope。
- 明确旧 Batch 只作为历史参考，不作为第一阶段完成证明。
- 明确 `done` 必须通过入口文档第 7 节验收门。
- 明确 `/do all` 中未完成项必须保留 `pending` / `blocked` / `failed`。

### Task PCC-P1-C02: 三组菜单、按钮、权限和真实库 contract

TaskId: PCC-P1-C02  
Title: 三组菜单、按钮、权限和真实库 contract  
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及 `sys_menu`、按钮权限、SQL 幂等和真实库校验，匹配 Java/SQL Agent。  
AgentRole: database-contract  
Status: pending  
Priority: high  
RiskLevel: high，因为菜单路径和按钮权限错误会导致刷新 404、按钮缺失或越权。  
Wave: 0  

Files:

- `sql/postgresql/product_capability.sql`
- `sql/postgresql/base.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/**`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysTenantApplyController.java`
- `admin-ui/src/stores/permission.ts`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/pages/system/TenantApplicationsPlaceholder.vue`
- `i18n/locales/en_US.json`

Forbidden:

- 生产环境配置
- MySQL SQL
- `.ai/archive/**`

DependsOn: none  
ParallelGroup: contract  
ConflictBoundary: db-menu-permission-contract  

Acceptance:

- 三个一级菜单、全部二级菜单和按钮权限清单冻结。
- 每个页面的 `sys_menu.perms`、前端 `v-hasPermi`、后端 `@SaCheckPermission` 对应关系冻结。
- 正式路径和兼容旧路径边界明确；不能依赖临时 alias 长期兜底。
- 真实开发库 preflight、执行、post-check SQL 清单明确。

### Task PCC-P1-C03: UI 复刻、组件拆分和标准页面 contract

TaskId: PCC-P1-C03  
Title: UI 复刻、组件拆分和标准页面 contract  
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及 Vue 页面、组件拆分、H5/PNG 复刻和历史页面标准化，匹配前端 Agent。  
AgentRole: frontend-contract  
Status: pending  
Priority: high  
RiskLevel: high，因为 UI 如果仍停留在骨架或调试页，会直接违背用户体验目标。  
Wave: 0  

Files:

- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/system/**`
- `admin-ui/src/pages/merchant/**`
- `admin-ui/src/components/**`
- `docs/产品配置中心/product-ability-ui-mockups/**`
- `i18n/locales/en_US.json`

Forbidden:

- `bocoo-modules-product/src/**`
- `sql/postgresql/*.sql`
- `admin-ui/public/i18n/*.json` 手工编辑
- `.ai/archive/**`

DependsOn: none  
ParallelGroup: contract  
ConflictBoundary: frontend-ui-contract  

Acceptance:

- 普通 grid/list 页面和 5 个自定义页面边界明确。
- 需要新建/复用的公共组件和领域组件清单明确。
- H5/PNG 哪些区域用 Element Plus/代码渲染、哪些区域用资料资产或 image2 临时资产明确。
- 所有下拉和业务对象选择器的模糊过滤/远程搜索口径明确。

### Task PCC-P1-C04: 验收脚本、浏览器路径和真实库核验 contract

TaskId: PCC-P1-C04  
Title: 验收脚本、浏览器路径和真实库核验 contract  
Owner: browser-debugger  
OwnerSource: agent-registry  
OwnerReason: 该任务需要设计内部浏览器真实路径、console/network/API 和视觉证据采集，匹配浏览器验证 Agent。  
AgentRole: runtime-contract  
Status: pending  
Priority: high  
RiskLevel: high，因为第一阶段完成口径必须包含真实浏览器和真实库，不允许只靠编译。  
Wave: 0  

Files:

- `.ai/changes/20260607-product-capability-phase1/check.md`
- `.ai/changes/20260607-product-capability-phase1/artifacts/**`
- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/pages/system/**`
- `admin-ui/src/pages/merchant/**`

Forbidden:

- 业务代码实现文件，除非后续 Wave 明确授权
- 生产环境配置
- `.ai/archive/**`

DependsOn: none  
ParallelGroup: contract  
ConflictBoundary: validation-contract  

Acceptance:

- 浏览器验收路径覆盖三个一级菜单、所有 P0 二级菜单、5 个自定义页和历史标准化页面。
- 明确用 `192.*` 地址、真实登录态、真实开发库验证。
- 明确每个页面要检查刷新、搜索、重置、分页、抽屉、保存、错误提示、底部按钮可见性、console error、network error。
- 明确截图和日志摘要保存位置。

## Barrier 0

Checks:

- 第一阶段 Scope 与入口文档一致。
- `done` 口径不再等同骨架/MVP/空库/编译通过。
- 菜单、权限、API、UI、验证合同无冲突。
- 不进入 Wave 1，直到 Contract 完成。

## Wave 1: Independent Implementation

### Task PCC-P1-D01: 修正数据库菜单、按钮、字典和真实库执行脚本

TaskId: PCC-P1-D01  
Title: 修正数据库菜单、按钮、字典和真实库执行脚本  
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及 PostgreSQL SQL、菜单按钮权限、字典和真实库执行，匹配 Java/SQL Agent。  
AgentRole: database  
Status: pending  
Priority: high  
RiskLevel: high，因为真实菜单不全会让前后端无法完整验收。  
Wave: 1  

Files:

- `sql/postgresql/product_capability.sql`
- `sql/postgresql/base.sql`
- `docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md`
- `docs/产品配置中心/共享产品能力中心数据库落库评审.md`
- `i18n/locales/en_US.json`

Forbidden:

- 生产环境配置
- MySQL SQL
- `admin-ui/src/**`，除非只同步菜单 i18n key 清单
- `.ai/archive/**`

DependsOn: PCC-P1-C02  
ParallelGroup: backend-db  
ConflictBoundary: sql-and-db-docs  

Acceptance:

- `product_capability.sql` 从旧单一 `Product Capability` 一级菜单收敛到 `基础资料`、`配置与价格`、`发布与应用`。
- 全部 P0 二级菜单和按钮权限落库，管理员角色授权可查。
- `product_material_type`、`product_component_type`、`product_business_type`、`product_unit` 字典完整。
- SQL 幂等，已有菜单冲突有明确 upsert 或迁移策略。
- 真实开发库执行前后 SQL 核验脚本完整。

### Task PCC-P1-B01: 补齐产品/配置/价格/发布/报价/审核/缺口/同步/导入后端闭环

TaskId: PCC-P1-B01  
Title: 补齐产品/配置/价格/发布/报价/审核/缺口/同步/导入后端闭环  
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及 `bocoo-modules-product` Controller/Service/Mapper/Engine、权限和事务边界，匹配 Java Agent。  
AgentRole: backend  
Status: pending  
Priority: high  
RiskLevel: high，因为前端不能用假按钮或本地计算替代真实业务闭环。  
Wave: 1  

Files:

- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/mapper/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/**`
- `bocoo-modules-product/src/main/resources/mapper/**`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysTenantApplyController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysTenantApplyService.java`

Forbidden:

- `admin-ui/**`
- 生产环境配置
- MySQL SQL
- `.ai/archive/**`

DependsOn: PCC-P1-C01, PCC-P1-C02  
ParallelGroup: backend-db  
ConflictBoundary: backend-only  

Acceptance:

- Controller 不返回 Entity，不直接操作 Mapper，不写复杂规则计算。
- 产品基础资料、配置模板、价格中心、报价预览、发布检查、审核审批、缺口待办、发布包、同步日志、导入中心均有真实 API 或明确禁用态。
- 价格试算和报价预览调用后端价格引擎。
- 审核通过/拒绝权限拆分，后端注解和数据库按钮一致。
- 发布包、快照、BOM、价格明细权威数据落 PostgreSQL，可追溯。
- outbox 写入、重试标记和同步日志查询可用。
- Java 时间和错误消息遵守 UTC/i18n 规则。

### Task PCC-P1-F01: 历史页面和代码生成器标准化为 grid/list + drawer

TaskId: PCC-P1-F01  
Title: 历史页面和代码生成器标准化为 grid/list + drawer  
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及 Vue 生成器模板和历史页面 UI 改造，匹配前端 Agent。  
AgentRole: frontend  
Status: pending  
Priority: high  
RiskLevel: high，因为历史页面是标准样例，不能继续生成旧弹窗长表单。  
Wave: 1  

Files:

- `bocoo-modules-generator/src/main/resources/vm/vue/**`
- `admin-ui/src/pages/system/user/UserPage.vue`
- `admin-ui/src/pages/merchant/MerchantUserPage.vue`
- `admin-ui/src/pages/system/MenuPage.vue`
- `admin-ui/src/pages/system/RolePage.vue`
- `admin-ui/src/pages/system/DictTypePage.vue`
- `admin-ui/src/pages/system/DictDataPage.vue`
- `admin-ui/src/pages/system/ConfigPage.vue`
- `admin-ui/src/pages/system/PostPage.vue`
- `admin-ui/src/pages/system/DeptPage.vue`
- `admin-ui/src/pages/system/OssPage.vue`
- `admin-ui/src/pages/system/OssConfigPage.vue`
- `admin-ui/src/pages/system/NoticePage.vue`
- `admin-ui/src/pages/monitor/OperationLogPage.vue`
- `admin-ui/src/pages/system/TenantApplicationsPlaceholder.vue`
- `i18n/locales/en_US.json`

Forbidden:

- `bocoo-modules-product/src/**`
- `sql/postgresql/product_capability.sql`
- `admin-ui/public/i18n/*.json` 手工编辑
- `.ai/archive/**`

DependsOn: PCC-P1-C03  
ParallelGroup: frontend-standardization  
ConflictBoundary: historical-ui-and-generator  

Acceptance:

- 代码生成器新产物默认长表单使用右侧抽屉。
- 历史长表单页面改为右侧抽屉；短确认、图片预览、密码重置、导入选择可保留小弹窗。
- 商家审核通过/拒绝按钮在桌面和移动端都符合标准页面结构，权限和数据库按钮一致。
- 抽屉底部 sticky 操作区可见。
- 新增文案走 i18n，不手改 runtime JSON。

### Task PCC-P1-F02: 普通产品能力页面独立菜单化和标准 grid/list 改造

TaskId: PCC-P1-F02  
Title: 普通产品能力页面独立菜单化和标准 grid/list 改造  
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及产品能力普通页面、动态路由、权限和标准 grid/list 交互，匹配前端 Agent。  
AgentRole: frontend  
Status: pending  
Priority: high  
RiskLevel: high，因为高频主数据不能继续藏在 tab 里。  
Wave: 1  

Files:

- `admin-ui/src/pages/product-center/base/**`
- `admin-ui/src/pages/product-center/assets/**`
- `admin-ui/src/pages/product-center/model/**`
- `admin-ui/src/pages/product-center/components/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/stores/permission.ts`
- `i18n/locales/en_US.json`

Forbidden:

- `bocoo-modules-product/src/**`
- `sql/postgresql/*.sql`
- `admin-ui/public/i18n/*.json` 手工编辑
- `.ai/archive/**`

DependsOn: PCC-P1-C02, PCC-P1-C03  
ParallelGroup: frontend-product-standard  
ConflictBoundary: product-standard-pages  

Acceptance:

- 产品分类、物料管理、辅材管理、资料资产、资料绑定、产品模型、销售变体、问题组模板、审核审批、缺口待办、发布包、同步日志、导入中心是独立入口。
- 普通页面符合查询区、toolbar、right-toolbar、table、pagination、drawer 结构。
- 字典下拉 `filterable`；业务对象选择支持远程搜索、分页或选择器。
- 页面刷新不 404；动态组件映射覆盖正式 `component`。
- 所有按钮有真实 API 或明确禁用态，不出现假按钮。

### Task PCC-P1-F03: 五个自定义页组件化和 H5/PNG 复刻实现

TaskId: PCC-P1-F03  
Title: 五个自定义页组件化和 H5/PNG 复刻实现  
Owner: frontend-developer  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及复杂 Vue UI、组件拆分、H5/PNG 像素级参考和交互体验，匹配前端 Agent。  
AgentRole: frontend-ui  
Status: pending  
Priority: high  
RiskLevel: high，因为当前自定义页仍有调试式 JSON textarea 和骨架感。  
Wave: 1  

Files:

- `admin-ui/src/pages/product-center/workbench/**`
- `admin-ui/src/pages/product-center/template/**`
- `admin-ui/src/pages/product-center/pricing/**`
- `admin-ui/src/pages/product-center/publish/**`
- `admin-ui/src/pages/product-center/sales-view/**`
- `admin-ui/src/pages/product-center/components/**`
- `admin-ui/src/api/product-capability/**`
- `docs/产品配置中心/product-ability-ui-mockups/**`
- `i18n/locales/en_US.json`

Forbidden:

- `bocoo-modules-product/src/**`
- `sql/postgresql/*.sql`
- 外部 CDN 进入生产代码
- `admin-ui/public/i18n/*.json` 手工编辑
- `.ai/archive/**`

DependsOn: PCC-P1-C03  
ParallelGroup: frontend-custom-ui  
ConflictBoundary: product-custom-pages  

Acceptance:

- 工作台、配置模板、价格中心、测试发布、销售只读总览在现有 `admin-ui` 内容区复刻 H5/PNG 主结构。
- 页面 SFC 只做编排，复杂区块抽成公共组件或领域组件。
- 配置模板页不再以裸 JSON/大 textarea 为主输入，保留三栏、Live checks、Dealer preview、BOM preview。
- 价格中心保留矩阵/规则编辑和右侧试算闭环。
- 测试发布保留 PASS/WARNING/BLOCKER/PENDING、阻断任务、发布包、同步状态。
- 销售只读总览保留 Quick Quote、发布包、文档、规格、版本历史。
- 底部操作区和关键按钮在 1366/1440/1920 可视区可见。

## Barrier 1

Checks:

- Wave 1 任务无 Forbidden path violation。
- 数据库菜单和前端路由不冲突。
- 后端 API contract 能支撑前端页面。
- 历史标准化和产品能力页面没有互相覆盖同一文件未协调。
- 通过 Barrier 1 前不进入集成对齐。

## Wave 2: Integration Alignment

### Task PCC-P1-I01: 前后端 API、权限、i18n、UTC、字典选择器集成对齐

TaskId: PCC-P1-I01  
Title: 前后端 API、权限、i18n、UTC、字典选择器集成对齐  
Owner: typescript-pro  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及前端 API 类型、权限码、i18n key、UTC 工具和字典/选择器类型边界，匹配 TypeScript Agent。  
AgentRole: integration  
Status: pending  
Priority: high  
RiskLevel: high，因为接口字段、权限码或类型漂移会导致真实浏览器流程失败。  
Wave: 2  

Files:

- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/pages/system/**`
- `admin-ui/src/pages/merchant/**`
- `admin-ui/src/stores/permission.ts`
- `i18n/locales/en_US.json`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/bo/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/**`

Forbidden:

- 数据库结构重写
- 生产配置
- `admin-ui/public/i18n/*.json` 手工编辑
- `.ai/archive/**`

DependsOn: PCC-P1-B01, PCC-P1-F01, PCC-P1-F02, PCC-P1-F03  
ParallelGroup: integration  
ConflictBoundary: api-types-i18n-permission  

Acceptance:

- 前端 API request/response 字段和后端 BO/VO 对齐。
- `v-hasPermi`、`@SaCheckPermission`、`sys_menu.perms` 三方一致。
- 新增 i18n key 存在且同步 runtime JSON。
- 时间字段展示/提交使用 UTC 工具。
- 字典和业务对象选择器符合模糊过滤/远程搜索要求。

### Task PCC-P1-I02: 真实开发库执行和菜单/API 数据核验

TaskId: PCC-P1-I02  
Title: 真实开发库执行和菜单/API 数据核验  
Owner: java-architect  
OwnerSource: agent-registry  
OwnerReason: 该任务涉及真实开发库 SQL 执行、菜单按钮和数据核验，匹配 Java/SQL Agent。  
AgentRole: db-validation  
Status: pending  
Priority: high  
RiskLevel: high，因为不打真实库就无法验证菜单、按钮和权限完整性。  
Wave: 2  

Files:

- `sql/postgresql/product_capability.sql`
- `sql/postgresql/base.sql`
- `.ai/changes/20260607-product-capability-phase1/logs/**`
- `.ai/changes/20260607-product-capability-phase1/check.md`

Forbidden:

- 生产环境配置
- MySQL SQL
- 无 preflight 的真实库破坏性操作
- `.ai/archive/**`

DependsOn: PCC-P1-D01, PCC-P1-B01  
ParallelGroup: integration  
ConflictBoundary: real-db-validation  

Acceptance:

- 真实开发库执行前完成菜单 ID、权限码、同名表、字典冲突 preflight。
- SQL 执行成功或失败原因记录清楚；失败不得伪造通过。
- `sys_menu`、`sys_role_menu`、字典、产品能力表、关键索引核验通过。
- 可以用查询结果证明三个一级菜单和全部 P0 二级菜单/按钮存在。
- 为浏览器真实测试准备好菜单和权限数据。

## Barrier 2

Checks:

- 前后端 API 类型、权限、i18n、UTC 对齐。
- 真实开发库菜单、按钮、字典和基础表核验完成。
- 仍未完成的功能必须保留 `pending` 或 `blocked`，不能从计划消失。
- 通过 Barrier 2 前不进入 Static Review。

## Wave 3: Static Review / Security Review

### Task PCC-P1-R01: Static Review：第一阶段代码审计

TaskId: PCC-P1-R01  
Title: Static Review：第一阶段代码审计  
Owner: code-reviewer  
OwnerSource: agent-registry  
OwnerReason: 该任务需要静态审计权限、SQL、事务、DTO/VO、前端类型、安全和可维护性，匹配 code-reviewer。  
AgentRole: static-review  
Status: pending  
Priority: high  
RiskLevel: high，因为本阶段涉及跨前后端和数据库，审计不能省略。  
Wave: 3  

Files:

- `bocoo-modules-product/src/**`
- `bocoo-modules-system/src/main/java/com/bocoo/system/**`
- `admin-ui/src/**`
- `sql/postgresql/*.sql`
- `i18n/locales/en_US.json`
- `.ai/changes/20260607-product-capability-phase1/review.md`

Forbidden:

- 运行浏览器自动化替代静态审计
- 生产配置
- `.ai/archive/**`

DependsOn: PCC-P1-I01, PCC-P1-I02  
ParallelGroup: review  
ConflictBoundary: static-review-only  

Acceptance:

- Findings 优先列出 bug、风险、权限漏洞、SQL/事务问题、前后端 contract mismatch、缺失测试。
- 每个发现有文件/行号和用户影响。
- Static Review 不替代 Runtime Validation。
- 审计结果写入 `.ai/changes/20260607-product-capability-phase1/review.md`。
- P0/P1 问题修复后才能进入最终验收；无法修复的写明 residual risk。

## Barrier 3

Checks:

- Static Review 完成且 P0 问题已修复或明确 blocked。
- 不把 code-review 结果当作浏览器验收。
- 通过 Barrier 3 前不进入 Runtime Validation。

## Wave 4: Check / Runtime Validation

### Task PCC-P1-V01: Runtime Validation：内部浏览器真实流程和视觉验收

TaskId: PCC-P1-V01  
Title: Runtime Validation：内部浏览器真实流程和视觉验收  
Owner: browser-debugger  
OwnerSource: agent-registry  
OwnerReason: 该任务需要内部浏览器、network/console、视觉证据和真实用户路径，匹配 browser-debugger。  
AgentRole: runtime-validation  
Status: pending  
Priority: high  
RiskLevel: high，因为用户明确要求真实浏览器测试新增界面完整性、视觉复刻、录入便捷性和前后端流程。  
Wave: 4  

Files:

- `.ai/changes/20260607-product-capability-phase1/check.md`
- `.ai/changes/20260607-product-capability-phase1/artifacts/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/pages/system/**`
- `admin-ui/src/pages/merchant/**`

Forbidden:

- Static Review 替代 Runtime Validation
- 伪造截图或伪造浏览器结果
- 输出 token 或敏感连接串
- `.ai/archive/**`

DependsOn: PCC-P1-R01  
ParallelGroup: validation  
ConflictBoundary: browser-validation  

Acceptance:

- 用内部浏览器访问真实 `192.*` 地址和真实登录态。
- 覆盖三个一级菜单和全部 P0 二级菜单；刷新 URL 不 404。
- 覆盖 5 个自定义页视觉复刻、底部按钮可见性、快捷操作和关键流程。
- 覆盖普通 grid/list 页搜索、重置、分页、新增/编辑/详情抽屉、按钮权限。
- 检查 console error、network error、接口响应、CORS、401/403、空状态和错误提示。
- 验收证据写入 `check.md`，截图/日志摘要放 `artifacts/` 或 `logs/`。

### Task PCC-P1-V02: Build/Test/SQL/i18n 总验收和任务状态收口

TaskId: PCC-P1-V02  
Title: Build/Test/SQL/i18n 总验收和任务状态收口  
Owner: main  
OwnerSource: main-fallback  
OwnerReason: 该任务需要主 Agent 汇总 Java/Vue/SQL/i18n/browser/check 结果并更新 `.ai` 状态。  
AgentRole: orchestrator-check  
Status: pending  
Priority: high  
RiskLevel: high，因为最终状态如果写错，会再次出现“骨架 done”误解。  
Wave: 4  

Files:

- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/changes/20260607-product-capability-phase1/check.md`
- `.ai/changes/20260607-product-capability-phase1/task-events.jsonl`
- `bocoo-modules-product/**`
- `bocoo-admin/**`
- `admin-ui/**`
- `sql/postgresql/**`
- `i18n/locales/**`

Forbidden:

- `.ai/archive/**`，除非用户进入 `/archive` 或已明确授权本次完成后归档
- 生产配置
- 伪造未执行的 build/test/browser 结果

DependsOn: PCC-P1-V01  
ParallelGroup: validation  
ConflictBoundary: final-status-and-check  

Acceptance:

- `codegraph sync` 在代码修改后重新执行并记录结果。
- Java 编译、Vue 类型检查/构建、i18n 同步/JSON 校验、SQL 真实库核验、浏览器验收结果明确记录。
- `.ai/TASKS.md` 中所有任务状态准确；未完成项保留 `pending` / `blocked` / `failed`。
- `.ai/CURRENT.md` 更新当前阶段和下一步。
- 如果所有验收门通过，Next Step 设置为 `Ready for /archive`；否则写明具体阻塞点。

## Barrier 4

Checks:

- Build/Test/SQL/i18n/Browser 真实执行结果有记录。
- Static Review 和 Runtime Validation 双 Lane 都完成。
- 入口文档第 7 节验收门逐项核对。
- 未完成项没有从计划消失。
- 全部通过后才允许标记第一阶段完成。
