# Requirement: 共享产品能力中心上线体验收口 Wave

## Goal：目标

基于已归档的 `20260607-product-capability-phase1` 继续推进，不重开炉灶，把共享产品能力中心从“底座和闭环可跑”推进到“运营人员可以上线使用”。

本 Wave 的核心不是新增骨架，而是完成 P0 上线体验收口：UI 精修、操作便捷性、视觉舒适性、真实录入流程、代码审计、内部浏览器真实验收和真实开发库验证。

## Background：背景

Phase1 已完成菜单、权限、真实库 SQL、产品能力模块基础 API、普通页面和 5 个自定义页第一版、关键交互抽查。但归档明确保留风险：

- 5 个主自定义页尚未完成“设计参考转 Vue 组件化重构和体验优化”的强证据验收。
- 发布检查结果表尚未加入批次号或去重策略。
- 部分页面仍是“能跑第一版”，未达到长期录入效率和视觉舒适性要求。

用户已明确：UI 精修和上线体验不是 P1/P2，而是本 Wave P0；`done` 必须通过入口文档第 7 节验收门。

## Scope：范围

- 5 个主自定义页：工作台、配置录入工作台、价格编辑与测试、测试发布、销售只读总览。
- 普通 grid/list 页面：产品分类、物料管理、辅材管理、资料资产、资料绑定、产品模型、销售变体、问题组模板、审核审批、缺口待办、发布包、同步日志、导入中心。
- 报价预览 / Quick Quote。
- 审核审批、缺口待办、发布包、同步日志、导入中心的可用交互和真实数据状态。
- 历史页面标准化复查，确保系统管理、商家管理、监控日志等旧页面继续符合标准 grid/list + 右侧抽屉。
- 内部浏览器真实验收、真实开发库验证、Static Review、Build/Test/i18n/UTC 检查。

## Out of Scope：不做范围

- 客户价、促销价、客户等级价、复杂价格体系。
- AI 智能识别、ConfigAgent、PricingAgent 自动生成和自动改价。
- ERP/MES 真实外部推送、回调确认、失败退避和定时重试。
- 共享产品能力中心独立服务拆分。
- Redis 作为发布包、审核、订单快照、价格快照、BOM 快照的权威存储。
- 生产环境配置和生产库变更。

## User Stories / Use Cases：用户故事 / 使用场景

- 产品运营可以从工作台看到待处理缺口、最近产品、发布风险和快捷入口，并快速跳到修复页面。
- 配置维护人员可以在配置模板工作台完成问题、答案、规则、BOM 和预览检查，不需要在多个隐藏 tab 中来回找。
- 价格维护人员可以编辑矩阵和规则后立即试算，看到命中明细、缺价原因和导入预览。
- 发布负责人可以在测试发布页看到 PASS / WARNING / BLOCKER 的视觉层级、修复入口和发布包预览。
- 销售或负责人可以在销售只读总览查看发布产品状态，并通过 Quick Quote 进入报价预览。
- 管理员可以在审核、缺口、发布包、同步日志、导入中心按真实数据执行查询、查看、处理、重试或预览。

## Business Rules：业务规则

- 本 Wave P0 不以“页面能打开”为完成；必须证明真实录入、错误提示、保存回显、刷新 URL、权限按钮和关键流程可用。
- UI 精修属于 P0，上线体验未通过不得标记为 `done`。
- 高频功能不得长期藏在一个页面 tab 内；已有 tab 若承载独立高频流程，必须拆为菜单或明确在本 Wave 计划中处理。
- 权威结果仍来自后端：配置求值、价格计算、BOM、发布检查、快照、审核不能由前端临时推导。
- 复杂区块必须组件化，页面 SFC 只做编排。

## UX / UI Requirements：交互 / 界面要求

- 普通页符合项目标准 grid/list + right-toolbar + drawer。
- 复杂页以设计文档、H5 静态参考和 PNG 效果图为输入，转成当前 `admin-ui` 框架内的 Vue 组件化实现；不复制独立 H5 壳层、Tailwind CDN、外链图片或静态演示代码。
- 必须按 `产品能力界面设计稿.md` 第 2.3 / 2.4 / 5 章执行：先锁定现有 `admin-ui` 风格，再拆解页面元素、状态、布局比例、组件边界和资产计划，最后落 Vue / Element Plus / SCSS 实现。
- 5 个自定义页必须遵守设计文档定义的信息架构：工作台聚合、配置三栏、价格矩阵 + 试算、发布闸门、销售只读总览。
- 设计技能链路必须保留：`visual-replica` 锁风格，`image2-ui-skill` 拆结构和资产计划，`frontend-design` 实现 Vue / Element Plus 页面，`design-taste-frontend` 做反模板化和视觉细节审查。
- 底部操作可见，长表单抽屉有 sticky footer。
- 所有字典下拉必须 `filterable`；业务对象选择必须支持模糊过滤、远程搜索或选择器。
- 空态、loading、错误态、无权限态、禁用态必须明确。
- 390px 移动宽度不横向溢出，不遮挡关键按钮。
- 本 Wave 必须产出浏览器证据；如果截图接口失败，必须记录原因并用 DOM、布局尺寸、关键按钮和交互证据替代，但不能宣称视觉和体验验收已完整通过。

## API Requirements：接口要求

- 页面按钮必须有真实 API 或明确禁用态。
- 报价预览、发布检查、审核审批、缺口定位、发布包详情、同步重试、导入预览必须有前后端闭环。
- 发布检查结果需要收敛历史噪音；优先补批次号、去重或按最新运行批次展示。
- 前后端 DTO / BO / VO、分页结构、枚举、权限码必须一致。

## Data Requirements：数据要求

- 以 PostgreSQL 真实开发库为验证目标。
- 如需补充表字段、索引、菜单、按钮或字典，只能通过受控 PG SQL，并保留 preflight / execute / post-check。
- 不新增 MySQL SQL，不修改生产配置。
- 发布包、配置快照、价格快照、BOM 快照、资料版本保持可追溯。

## Permission / Tenant Rules：权限 / 租户规则

- Vue `v-hasPermi`、Java `@SaCheckPermission`、`sys_menu.perms` 三方一致。
- 本能力是平台共享能力，不因订单租户逻辑扩大复杂度；已有平台权限和菜单体系必须一致。

## i18n / UTC Rules：国际化 / 时间规则

- 新增可见文案只写 `i18n/locales/en_US.json`，再通过脚本同步 runtime JSON。
- 后端新增错误消息走统一 JSON i18n。
- 后端存储和传输保持 UTC 语义，前端展示使用现有 UTC 工具。

## Options：可选方案

### Option A：只补个别明显问题

Pros：改动小，短期快。

Cons：仍会留下“看起来能跑，但不好用”的风险；无法解释 UI 精修 P0；不满足用户对上线使用的要求。

### Option B：以页面组为单位做上线体验收口

Pros：聚焦现有底座，不扩大业务范围；能把 5 个自定义页、普通页、报价/审核/发布/导入关键流程一次性拉到可验收。

Cons：涉及前端、后端、SQL、i18n、浏览器验收多边界，需要严格 Wave 和 Barrier 控制。

## Recommended Option：推荐方案

采用 Option B。先冻结上线体验 contract，再按前端体验、后端闭环、集成对齐、审计、浏览器真实验收推进。可延期的 P1/P2 只保留 TODO，不进入本 Wave 实现。

## Risks：风险

- 真实业务数据不足时，部分可视化和空态需要用开发库种子或现有数据验证。
- 如果发布检查要补批次号，可能涉及数据库结构调整，必须先评估并通过受控 SQL 执行。
- 截图 diff 依赖 in-app browser 稳定性；若再次超时，需保留替代证据和风险说明。
- 历史页面标准化范围容易扩大，必须限制在已列页面和生成器标准，不做无关重构。

## Open Questions：待确认问题

- 无阻塞性问题。若执行中发现数据库结构必须调整，将在 `/do` 中按受控 SQL 和验收门处理。

## Acceptance Criteria：验收标准

- `.ai/changes/20260607-product-capability-usability-wave/tasks.md` 全部 P0 任务完成，未完成项保留 `pending`、`blocked` 或 `failed`。
- 5 个主自定义页通过真实浏览器验证：主结构、底部可见、快捷入口、关键 API、错误态、移动端。
- 5 个主自定义页有明确组件化实现证据：页面 SFC 负责编排，高密度区域拆成 `TaskFlowCard`、`LiveCheckPanel`、`PriceMatrixEditor`、`PriceTesterPanel`、`PublishPackagePreview`、`QuickQuotePanel` 等同类业务组件或等价组件。
- 普通 grid/list 和历史页面标准化复查通过：搜索、重置、分页、抽屉、权限、空态、错误态。
- 报价预览、审核审批、缺口待办、发布包、同步日志、导入中心至少通过关键真实流程。
- Java/Vue/i18n/codegraph/diff 检查通过。
- 真实开发库 post-check 通过。
- Static Review 和 Runtime Validation 双 Lane 都完成，并写入证据。

## Related Decisions：相关决策

- `.ai/archive/20260607-product-capability-phase1.md`
- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`
