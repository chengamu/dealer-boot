# Requirement: 共享产品能力中心落地计划

## Goal：目标

基于 `docs/产品配置中心/配置中心功能拆分清单.md`，把“共享产品能力中心”沉淀成适合当前 `base-boot` 项目的可执行计划。

目标不是重做后台基础设施，而是在现有 Spring Boot + Vue 管理端中落地产品基础信息、配置、价格、发布、快照、销售只读和后续 AI 提效能力。

## Background：背景

当前项目已经具备登录、角色、菜单、按钮权限、租户上下文、审计字段、操作日志、OSS、字典、i18n、UTC、代码生成器、`admin-ui` 管理端布局和 Element Plus 组件体系。

产品配置中心文档已经从“从 0 到 1 新系统”调整为“共享产品能力中心”。它不是订单系统私有能力，而是订单、ERP、MES、报价、售后、客户门户后续都要消费的产品能力 source of truth。

## Scope：范围

本计划覆盖：

- 产品域基础信息：分类、物料、组件、资料资产、资料绑定、单位/字典接入。
- 配置中心：产品模型、销售变体、配置模板、问题组、问题、答案、规则、用量、BOM 预览。
- 价格中心：价格方案、价格版本、价格规则项、矩阵/工艺/固定组件/运费、价格试算、价格快照。
- 发布治理：发布检查、缺口任务、审批记录、发布包、outbox、read model。
- 前端页面：现有 grid/list 页面 + 5 个自定义页面。
- API 与 Java 风格：使用当前项目 `R`、`TableDataInfo`、BO/VO、代码生成器、权限、日志、i18n、UTC 口径。
- 后续批次：销售只读、订单快照、ERP/MES 同步、ConfigAgent、PricingAgent、导入中心预留。

## Out of Scope：不做范围

第一批不做：

- 不直接生成或修改 production migration。
- 不修改生产环境配置。
- 不新增 Redis 作为核心依赖。
- 不做完整订单侧详细设计、ERP/MES 生产领料库存流程。
- 不做完整资料包自动导入。
- 不做 Agent 自动发布、自动改价。
- 不做促销、客户等级、折扣、真实商户专属价。

这些能力必须保留 TODO 或 Deferred，不删除。

## User Stories / Use Cases：用户故事 / 使用场景

- 产品运营要快速创建复杂产品配置草稿，并看到当前缺口和下一步动作。
- 资料负责人要维护产品、组件、颜色、面料、安装说明等资料，并知道哪些发布被阻断。
- 价格负责人要维护价格方案、执行价格试算、发现缺价和异常价。
- 发布负责人要执行联合发布检查，确认 blocker/warning 后发布不可变发布包。
- 销售和领导要通过只读总览查看已发布产品能力，不进入编辑态。
- 订单系统要引用发布包并保存订单配置、价格、BOM、资料快照。

## Business Rules：业务规则

- 共享产品能力中心是 source of truth，订单、ERP、MES 只消费发布版本和快照。
- 草稿可编辑，发布包不可原地修改。
- 快照必须自包含，保存当时 code、name、version、单位、币种、价格明细、BOM、资料引用。
- 前端可以做轻量预览，但配置求值、价格计算、BOM、发布检查、订单快照必须以后端为准。
- BLOCKER 不可发布；WARNING 可发布但必须确认。
- Redis 不保存发布包、审核结果、订单快照、价格快照、BOM 快照的权威数据。

## UX / UI Requirements：交互 / 界面要求

- 页面必须进入现有 `admin-ui` 布局和 `sys_menu` 动态路由体系。
- 菜单最多两级，正式一级菜单是 `基础资料`、`配置与价格`、`发布与应用`；不再使用一个笼统的“产品能力/产品中心”一级菜单承载全部功能。
- 普通维护页优先现有 grid/list + toolbar + table + drawer。
- 自定义页面为：工作台、配置录入工作台、价格编辑与测试、测试发布、销售只读总览。
- H5 原型只作为业务数据和关系参考，不作为正式视觉风格。
- 新增可见文案只写 `i18n/locales/en_US.json`。

## API Requirements：接口要求

- API 前缀建议 `/product-capability/*`。
- 前端路由可继续使用 `/product-center/*`，但菜单显示和 `sys_menu` 必须按 `基础资料`、`配置与价格`、`发布与应用` 三组落库。
- 分页返回 `TableDataInfo<XxxVo>`，单条/操作返回 `R<T>`。
- Controller 不返回 Entity，不拼复杂业务 JSON，不写规则求值逻辑。
- 发布、求值、价格、BOM、快照由后端 engine 或 Service 负责。

## Data Requirements：数据要求

- PostgreSQL 是主数据库，新设计不补 MySQL SQL。
- 后续落 SQL 时，每张表和每个字段都必须有中文注释。
- 时间字段使用 `timestamptz`，注释标明 `UTC timestamptz`。
- 建议表前缀 `pc_`，发布包、订单快照、read model 允许宽表和 JSONB。
- 第一批落库前必须逐表确认租户策略和 migration 评审节奏。

## Permission / Tenant Rules：权限 / 租户规则

- 权限复用现有 `@SaCheckPermission`、`v-hasPermi`、系统菜单和角色授权。
- 产品能力核心数据按平台共享主数据处理，不做商户级配置复制。
- 技术上必须选择：加入租户忽略表，或统一写平台租户 ID。
- 不让订单、ERP、MES 直接写共享产品能力核心表。

## i18n / UTC Rules：国际化 / 时间规则

- 前端新增可见文案只写 `i18n/locales/en_US.json`。
- 后端错误消息优先 message key。
- 产品名、答案名、客户说明是业务多语言字段，不是 UI i18n key。
- Java 业务时间使用 `TimeUtils.utcNow()`，前端展示使用 `formatUtc()`，提交绝对时间使用 `toUtcPayload()` 或 UTC range 工具。

## Options：可选方案

### Option A：在订单系统当前代码库内按独立模块/独立包落地

Pros：最快落地，复用现有权限、菜单、i18n、OSS、代码生成器和部署链路。

Cons：需要严格控制边界，避免被订单业务耦合。

### Option B：立即拆独立服务和独立数据库

Pros：边界最干净，天然面向 ERP/MES/订单多消费者。

Cons：第一批落地成本高，需要同步、鉴权、部署、数据一致性、联调全部提前解决。

### Option C：先放进 `bocoo-modules-system`

Pros：最省模块配置。

Cons：会污染系统模块，和共享产品能力规模不匹配，不推荐。

## Recommended Option：推荐方案

推荐 Option A：当前代码库内落地，但新建 `bocoo-modules-product` 或至少独立 `com.bocoo.product` 包，并按未来独立服务边界设计 API、表前缀、发布包、read model、outbox。

## Risks：风险

- 需求范围大，不能一次 `/do` 全量实现。
- 租户策略不确认会导致平台数据被租户拦截或误写。
- migration 未评审前直接建表风险高。
- 前后端 contract 不先冻结，后续接口和 UI 容易反复返工。
- 复杂规则如果先写在前端，会导致发布、报价、订单结果不一致。
- 如果 P1/P2 被删除，第一批完成后容易丢失后续路线。

## Open Questions：待确认问题

1. 已确认后端统一放在 `bocoo-modules-product`，后续方便拆分独立服务。
2. 已确认产品能力核心数据按平台共享能力处理，P0 不考虑商户租户隔离；技术上优先纳入租户忽略表或等价平台共享策略。
3. 已授权 `/do` 阶段可以修改数据库脚本并在真实开发库执行受控 SQL，但不能修改生产环境配置。
4. 第一阶段不是“基础信息 + 骨架”，而是：历史遗留标准化问题修改，新增产品、价格、发布模块完成开发和 UI 复刻，并通过代码审计、内部浏览器真实测试、真实开发库验证。

## Acceptance Criteria：验收标准

- `.ai/requirements/20260606-product-capability-center.md` 以 `docs/产品配置中心/共享产品能力中心开发入口.md` 为合同口径，旧 Batch/MVP 只作为历史背景。
- `.ai/changes/20260607-product-capability-phase1/wave-plan.md` 覆盖第一阶段全部 P0：历史界面和生成器标准化、数据库菜单按钮、产品/配置/价格/发布/报价/审核/缺口/发布包/同步/导入 UI 与后端闭环。
- `.ai/TASKS.md` 能看到当前 active queue、每个任务的真实 `pending/done/blocked` 状态，以及未完成项不能消失。
- `.ai/CURRENT.md` 指向当前需求源、入口文档和下一步 `/do all`。
- 真正 `done` 必须通过入口文档第 7 节验收门：菜单门、权限门、API 门、数据门、UI 门、组件门、选择器门、i18n 门、UTC 门、快照门、浏览器门。
- `/do all` 完成后必须包含代码审计、内部浏览器真实测试、真实开发库验证；未通过的项保留 `pending` 或 `blocked`，不能标记为功能完成。

## Related Decisions：相关决策

- PostgreSQL 是主数据库，不补 MySQL SQL。
- 产品能力核心数据按平台共享主数据处理。
- Redis 不作为快照或发布权威存储。
- 普通 CRUD 优先代码生成器风格，自定义复杂页面再手工增强。
- 菜单最多两级，复杂功能放页面内 tabs、panel、抽屉和快捷操作。

## Plan Revision：2026-06-07 第一阶段落地口径

Requirement Source：

- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`

本次 `/plan` 修订原因：

- 旧 Batch 1 到 Batch 10 的 `done` 含义混入了“骨架/MVP/草案/空库验证”，不能代表真实业务体验完成。
- 当前入口文档已经明确第一阶段目标：历史遗留标准化问题修改，新增产品、价格、发布模块完成开发和 UI 复刻、代码审计、内部浏览器真实测试、真实开发库验证。
- 真实代码和 SQL 中仍存在旧“Product Capability”一级菜单和组合页/tab 口径，需要按入口文档收敛为三个一级菜单和独立二级入口。

本次第一阶段 Scope：

- P0 历史标准化：代码生成器 Vue 模板、系统管理和商家审核等历史页面，长表单统一右侧抽屉；通过/拒绝等独立按钮补齐数据库菜单、前端权限和后端权限。
- P0 数据库菜单：`sys_menu` 按 `基础资料`、`配置与价格`、`发布与应用` 三个一级菜单落库，二级菜单和按钮权限完整；真实开发库执行并核验，不靠前端 alias 长期兜底。
- P0 后端能力：`bocoo-modules-product` 内补齐产品基础资料、配置、价格、发布、报价、审核、缺口、发布包、同步日志、导入中心所需 API 和 Service/Engine 闭环。
- P0 前端能力：普通页面按标准 grid/list + drawer；5 个自定义页按 H5/PNG 主结构在现有 `admin-ui` 中复刻，复杂区块组件化。
- P0 验证闭环：静态代码审计、Java/Vue 构建检查、真实开发库 SQL/API 核验、内部浏览器用真实地址和真实登录态验证菜单刷新、接口、视觉、录入便捷性和关键流程。

本次第一阶段 Out of Scope：

- 不把 Redis 作为发布包、审核、快照、价格和 BOM 的权威存储。
- 不接真实 ERP/MES 外部推送回调，只完成 outbox、重试标记和同步日志 UI/API 基础闭环。
- 不做客户价/促销价/复杂价格体系；这些保留后续 P1/P2。
- 不做运行时 AI 自动发布或自动改价；AI 只作为后续建议能力。

第一阶段 `done` 定义：

- 任何任务如果只完成骨架、空库试执行、编译或静态页面，状态不得写 `done`。
- 只有通过该任务 Acceptance，并且不违反入口文档第 7 节验收门，才能写 `done`。
- 由于环境、数据或权限导致无法完成真实验证时，任务必须写 `blocked` 或保留 `pending`，并写清楚恢复条件。
