# Tasks: 共享产品能力中心上线体验收口 Wave

创建日期：2026-06-07

## Active Queue

| TaskId | Title | Owner | Status | Wave | Priority |
| --- | --- | --- | --- | --- | --- |
| PCC-UW-C01 | 冻结上线体验 P0 contract 和 UI 精修验收口径 | frontend-developer | done | 0 | high |
| PCC-UW-C02 | 冻结报价/审核/缺口/发布/同步/导入 API 和数据 contract | java-architect | done | 0 | high |
| PCC-UW-C03 | 冻结真实浏览器验收矩阵和证据口径 | browser-debugger | done | 0 | high |
| PCC-UW-F01 | 5 个自定义页转 Vue 组件化重构、UI 精修和录入效率优化 | frontend-developer | done | 1 | high |
| PCC-UW-F02 | 普通产品能力 grid/list 与历史页面标准化复查整改 | frontend-developer | done | 1 | high |
| PCC-UW-B01 | 报价/审核/缺口/发布包/同步/导入后端可用性加固 | java-architect | done | 1 | high |
| PCC-UW-F03 | 报价预览、审核审批、缺口、发布包、同步日志、导入中心前端流程收口 | frontend-developer | done | 1 | high |
| PCC-UW-I01 | 前后端字段、权限、i18n、UTC、选择器和错误态集成对齐 | typescript-pro | done | 2 | high |
| PCC-UW-I02 | 真实开发库菜单、按钮、字典、关键数据和 SQL post-check 收口 | java-architect | done | 2 | high |
| PCC-UW-R01 | Static Review：上线体验 Wave 代码审计 | code-reviewer | done | 3 | high |
| PCC-UW-V01 | Runtime Validation：内部浏览器真实录入和视觉验收 | browser-debugger | done | 4 | high |
| PCC-UW-V02 | Build/Test/i18n/codegraph/diff 总验收和任务状态收口 | main | done | 4 | high |

## Completion Rule

- `done` 必须通过 `docs/产品配置中心/共享产品能力中心开发入口.md` 第 7 节验收门。
- UI 实现和验收必须先读 `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`。
- Wave 0 是纯合同阶段，只允许收窄计划、文档和验收口径，禁止修改 `admin-ui/**`、`bocoo-modules-product/**`、`sql/**`。
- UI 精修、操作便捷性、视觉舒适性、真实录入流程、内部浏览器验收均为本 Wave P0。
- 不能只因页面能打开、接口能返回、编译通过或有静态样式就标记 `done`。
- 未完成项必须保留 `pending`、`blocked` 或 `failed`，不能从任务清单删除。
- `PCC-UW-F01` 是 5 个自定义页的设计主线任务，不允许再按页面拆成多个互相覆盖的实现任务。

## Deferred

- 客户价 / 促销价 / 客户等级价 / 复杂价格体系。
- AI 智能识别、ConfigAgent、PricingAgent。
- ERP/MES 真实外部推送、回调确认、失败退避和定时重试。
- 导入字段映射确认后的批量提交正式表、冲突合并和回滚策略。
- 共享产品能力中心独立服务拆分。
- Redis 查询缓存优化；权威快照仍不得放 Redis。

## Run Notes

### 2026-06-07 Wave 1 进展

- `PCC-UW-F01`：5 个自定义页已按页面编排 + 领域组件方式继续收口，新增工作台 ECharts 趋势组件；ECharts 使用项目已有依赖，按需引入并组件封装，不新增图表库。
- `PCC-UW-F02`：普通产品能力 grid 支持路由 query 回填查询条件，导入中心解析后刷新批次 grid；商家审核和商家资料补齐 right-toolbar / drawer footer / 审核按钮等标准化缺口。
- `PCC-UW-F03`：工作台卡片、进度行、优先任务、同步事件、测试发布 BLOCKER 已补直达目标页面或列表筛选；导入中心解析后刷新批次列表。
- `PCC-UW-B01`：后端 dev profile 可启动并连接 PostgreSQL / Redis；与 `PCC-UW-V01` 的真实流程验收联动完成后进入最终验收。
- 验证已完成：`pnpm --dir admin-ui typecheck`、`pnpm --dir admin-ui build`、`git diff --check`、`codegraph sync`。
- 内部浏览器已用真实 `192.*` 地址和 UI 登录态验证 19 个产品能力入口 + 商家审核/商家资料：未出现刷新 404、登录回跳或系统异常。
- 已按 dev 127/localhost 策略收口：`admin-ui` dev proxy 指向 `127.0.0.1:8081`，后端 dev PostgreSQL 指向 `localhost:5432`，Redis 保持 `127.0.0.1`。
- 内部浏览器已补充 `127.0.0.1:8083` 验证：缺口待办、审核审批、发布闸门不再暴露通用新增/修改/删除，保留处理阻断、审批/拒绝、详情等业务动作；390px 抽查核心产品能力页面无页面级横向溢出、无 404、无系统异常、无原始 i18n key。
- 通用 `ProductEntityGridPage` 业务行按钮已补 loading 和取消确认框处理，避免审批/拒绝/处理阻断等动作重复点击或取消时产生前端噪音；动作成功后仍统一刷新列表。
- 2026-06-07 11:50 已执行 `pnpm i18n:sync`，同步 `i18n/locales/*.json` 到 `admin-ui/public/i18n/*.json` 和 `bocoo-admin/src/main/resources/i18n/*.json`。
- 2026-06-07 11:50 已执行 `mvn -pl bocoo-modules-product,bocoo-admin -am -DskipTests clean compile`：通过，`bocoo-modules-product` 重新编译 120 个 source。
- 2026-06-07 11:50 已执行 `pnpm --dir admin-ui typecheck`、`pnpm --dir admin-ui build`：通过。
- 2026-06-07 12:00 已执行真实接口 E2E，证据：`.ai/changes/20260607-product-capability-usability-wave/artifacts/api-e2e-summary.json`。结果：配置评估可带出 1 条 BOM/组件摘要，必填缺失返回 `BLOCKER`；价格试算命中 2 条规则、合计 `110.00000000`，不命中返回 `product.price.rule.noMatch`；发布重复执行返回同一个发布包，Outbox 保持 3 条；销售只读列表可查到发布包。
- 2026-06-07 12:00 已执行内部浏览器 smoke，证据：`.ai/changes/20260607-product-capability-usability-wave/artifacts/browser-smoke-summary.json`。覆盖 16 个真实菜单路径，均无 404、无系统异常、无原始 i18n key。基础资料真实父路径为 `/product-master/*`，不是 `/product-base/*`。
- 已修复本轮 Static Review P0：销售只读改为独立 `product:sales-view:list` 后端接口；发布重复执行幂等复用发布包和 Outbox；价格引擎按 `matchJson` / `selectedOptions` / `inputValues` 受控命中；配置评估补必填校验和选中项组件带出；发布按钮补 loading/重复点击保护；导入中心 `columnName` i18n key 补齐。
- 2026-06-07 12:21 已重启后端并验证菜单标题链路：`/getRouters` 返回产品能力路由 `meta.title=productCenter.menu.*`，英文标题数量为 0；内部浏览器 `/product-release/approvals` 面包屑显示“审核审批”，不再显示 `productCenter.menu.approvals` raw key。
- 2026-06-07 12:21 已执行真实开发库菜单/权限 post-check：可见菜单为 3 个父菜单 + 19 个二级入口；后端 `@SaCheckPermission` 39 个、SQL 权限 39 个、前端实际使用权限 32 个，三侧无缺失；旧版 `product-center` 树 52 条 legacy 行均为禁用/隐藏，不进入路由和授权，本轮不做删除。
- 2026-06-07 12:21 已用内部浏览器验证普通 grid 物料管理新增、编辑、删除闭环：抽屉可打开，保存后表格即时回显，删除确认可用，测试数据已清理，无 404、无系统异常、无 raw i18n key。
- 2026-06-07 12:21 已修复产品能力 grid 删除确认按钮从 Element Plus 默认 `Cancel/OK` 变成“取消/确定”，并通过浏览器复测。
- 2026-06-07 12:21 已统一基础资料命名：`productCenter.menu.materials` / `productCenter.material.title` 为“物料管理”，`productCenter.menu.components` / `productCenter.component.title` 为“辅材管理”，浏览器确认面包屑和页面标题一致。
- 2026-06-07 12:32 已补工作台 P0 结构：新增“今日待处理流”区块标题和直达说明；工作台配置进度状态从 `READY/MISSING` 裸码改为“就绪/缺失”等业务文案。
- 2026-06-07 12:32 已补价格中心连续体验：价格试算器新增“报价预览”入口，点击进入 `/product-config/quote-preview` 并携带当前币种/数量等查询参数；报价预览状态从 `READY` 裸码改为“待试算”等业务文案。
- 2026-06-07 12:32 已执行 5 个自定义页结构验收：工作台、配置模板、价格中心、测试发布、销售只读总览均包含合同要求的核心区块/动作，无 404、无系统异常、无 raw i18n key。该结果只代表 P0 主结构和连续体验通过，不代表像素级/UI 终验完成。
- 2026-06-07 12:39 已执行移动端 390px 回归：工作台、价格中心、报价预览无页面级横向溢出、无系统异常、无 raw i18n key。
- 2026-06-07 12:39 已执行辅材管理普通 grid 真实录入闭环：新增、编辑、删除、删除确认中文按钮均通过，测试数据和中途失败遗留数据已清理。
- 2026-06-07 12:46 已执行产品分类普通 grid 真实录入闭环：新增、编辑、删除、删除确认中文按钮均通过，测试数据已清理。
- 2026-06-07 13:05 已按“无 AI 翻译 key”策略手工补齐本轮缺失中英文 i18n：`api.error`、`common.normal`、`common.disabled`、`dict.product_unit.*`，并保留已补的 `product_business_type`、`product_component_type`、`product_material_type` 双语字典 key。
- 2026-06-07 13:05 已执行全量前端静态 i18n 扫描：覆盖 `admin-ui/src` 下 207 个 Vue/TS 文件、1328 个静态 key，`i18n/locales/en_US.json` 与 `i18n/locales/zh_CN.json` 均无缺失；`pnpm i18n:sync` 和 `pnpm --dir admin-ui typecheck` 通过。
- 2026-06-07 13:05 已用内部浏览器验证产品能力字典下拉：辅材类型、物料类型、单位下拉均为可过滤 select；可见“面料 / 电机 / 遥控 / 下杆”和“件 / 套 / 厘米 / 平方米”，无 console error。
- `PCC-UW-V01` 已执行 runtime 验收：像素级/近像素级对照、标准 grid 真实新增编辑详情抽屉与保存回显、移动端 sticky footer、`192.*` 回归与 `console/network` 关键项补齐，任务可收口。
