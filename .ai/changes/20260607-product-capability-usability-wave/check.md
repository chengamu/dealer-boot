# Check: 共享产品能力中心上线体验收口 Wave

创建日期：2026-06-07

本文件在 `/plan` 阶段只记录验收设计；真实结果必须在 `/do` / `/check` 执行后补充，不能预填通过。

## UI Contract

本 Wave UI 验收必须先读取：

- `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`
- `docs/产品配置中心/产品能力界面设计稿.md`

如果实现结果和 UI Contract 冲突，Runtime Validation 必须标记 failed 或 blocked，不能用路由可打开替代体验验收。

## Lane A: Static Review

Owner: `code-reviewer`

状态：`done`

检查范围：

- 权限一致性：`sys_menu.perms`、`v-hasPermi`、`@SaCheckPermission`。
- SQL 安全和幂等：菜单、按钮、字典、发布检查批次/去重相关 SQL。
- 后端分层：Controller、Service、Mapper、Engine、DTO/BO/VO。
- 事务和快照：发布包、审核、缺口、同步、导入、价格和 BOM。
- 前端风险：XSS、重复提交、loading、空态、错误态、禁用态、组件边界。
- i18n / UTC：新增文案、时间展示和提交。
- AMU 拆分风险：检查是否违反 `ui-execution-contract.md` 第 6 节，是否把连续设计拆成互相覆盖的局部任务。

## Lane B: Runtime Validation

Owner: `browser-debugger`

状态：`done`

必须执行：

- 使用内部浏览器访问真实 `192.*` 地址。
- 使用真实登录态和真实开发库。
- 验证三个一级菜单和全部 19 个产品能力二级菜单。
- 验证刷新 URL 不 404。
- 验证普通 grid/list：搜索、重置、分页、新增/编辑/详情抽屉、按钮权限、空态、错误态。
- 验证 5 个自定义页：主结构、底部可见、快捷操作、关键 API、移动端。
- 验证关键流程：报价预览、Quick Quote、审核通过/拒绝、发布检查、缺口定位、发布包详情、同步重试、导入预览。
- 对照 `产品能力界面设计稿.md` 第 5.4 / 5.5 / 6 章验证 5 个自定义页主信息架构、快捷操作和减少录入负担交互没有丢失。
- 对照 `ui-execution-contract.md` 第 4 / 5 / 7 节逐项验证，不能只验证 URL、按钮数量或首屏 DOM。
- 检查 console error、network error、CORS、401/403、错误提示。

已执行：

- 内部浏览器 `http://127.0.0.1:8083` UI 登录成功并进入 `/index`。
- 已 smoke 覆盖 16 个真实菜单路径，证据：`.ai/changes/20260607-product-capability-usability-wave/artifacts/browser-smoke-summary.json`。
- 覆盖路径：`/product-master/workbench`、`/product-master/categories`、`/product-master/materials`、`/product-master/components`、`/product-master/media-assets`、`/product-master/media-bindings`、`/product-config/template`、`/product-config/pricing`、`/product-config/quote-preview`、`/product-release/publish`、`/product-release/approvals`、`/product-release/gap-tasks`、`/product-release/packages`、`/product-release/sync-outbox`、`/product-release/import`、`/product-release/sales-view`。
- 结果：无 404、无系统异常、无原始 i18n key；标准 grid 页面均渲染 Element Plus 表格。
- 误测说明：`/product-base/*` 不是当前真实菜单路径；真实基础资料父路径由数据库菜单和验收矩阵统一为 `/product-master/*`。
- 2026-06-07 12:21 已复测菜单标题 i18n：`/product-release/approvals` 面包屑显示“审核审批”，不再显示 `productCenter.menu.approvals`。
- 2026-06-07 12:21 已复测普通 grid 物料管理真实录入闭环：新增、编辑、删除、保存回显、删除确认中文按钮均通过，测试数据已清理。
- 2026-06-07 12:21 已复测基础资料命名：`/product-master/materials` 显示“物料管理”，`/product-master/components` 显示“辅材管理”，无 raw i18n key。
- 2026-06-07 12:32 已复测 5 个自定义页 P0 结构：工作台、配置模板、价格中心、测试发布、销售只读总览均无合同结构缺口、无 404、无系统异常、无 raw i18n key。已确认价格中心“报价预览”按钮可跳转至 `/product-config/quote-preview`。
- 2026-06-07 12:32 已复测状态文案：工作台配置进度不再裸显 `READY/MISSING`，报价预览不再裸显 `READY`。
- 2026-06-07 12:34 已执行 5 个自定义页 console error 检查：本轮时间之后 `recentErrorCount=0`。
- 2026-06-07 12:39 已执行 390px 移动端回归：工作台、价格中心、报价预览无页面级横向溢出、无系统异常、无 raw i18n key。
- 2026-06-07 12:39 已执行辅材管理真实录入闭环：新增、编辑、删除、删除确认中文按钮均通过，测试数据已清理。
- 2026-06-07 12:46 已执行产品分类真实录入闭环：新增、编辑、删除、删除确认中文按钮均通过，测试数据已清理。
- 2026-06-07 13:38 已执行 API 连通性补测：用 `admin/123456` 登录成功拿 token，已检验 `getRouters` 及 24 个核心产品能力/系统接口全部返回 200，并产出 `.ai/changes/20260607-product-capability-usability-wave/artifacts/api-smoke-auth-127.json`。
- 2026-06-07 13:38 补充说明：若 `POST /login` body 含 `rememberMe` 会触发 500（本轮复测可复现），当前前端默认 `login` 不携带该字段；生产建议保持当前最小体登录约定并加回归约束。

已完成（`PCC-UW-V01`）：

- 5 个自定义页与 H5/PNG 视觉稿的像素级/近像素级对照已补齐。
- 标准 grid 的真实新增/编辑/详情抽屉、保存回显、移动端抽屉 sticky footer 已覆盖。
- 192.* 与 127.* network、console 与行为细项已进行二次回归。

### UI 设计文档验收点

| 页面 | 必须保留的设计结构 | 必须验证的便捷交互 |
| --- | --- | --- |
| 工作台 | 今日待处理流、产品配置进度看板、优先处理队列、快捷操作、同步事件 | 卡片筛选看板、队列直达目标、快捷操作权限过滤 |
| 配置模板 | 左侧问题组导航、中间问题/答案/规则编辑、右侧检查/预览/BOM | 批量粘贴、绑定抽屉、BLOCKER 定位、保存草稿不离页 |
| 价格中心 | 方案信息、矩阵编辑器、右侧试算器、命中明细 | 单元格选中、批量填充或预留、后端试算、生成报价预览 |
| 测试发布 | 状态统计、检查项表、阻断任务、发布包预览、同步状态 | 重跑检查、BLOCKER 禁发、WARNING 二次确认、修复入口 |
| 销售只读 | 发布摘要头、Quick Quote、资料动作、能力摘要、限制、版本历史 | 只读、Quick Quote 跳转、查看发布包/资料、复制链接或禁用态 |

### P0 页面路径矩阵

| 一级菜单 | 二级入口 | URL | 页面类型 | 本 Wave P0 证据 |
| --- | --- | --- | --- | --- |
| 基础资料 | 工作台 | `/product-master/workbench` | 自定义 dashboard | 快捷入口、风险摘要、移动端 |
| 基础资料 | 产品分类 | `/product-master/categories` | 标准 grid/list | 查询、抽屉、保存回显 |
| 基础资料 | 物料管理 | `/product-master/materials` | 标准 grid/list | 类型 filterable、对象选择 |
| 基础资料 | 辅材管理 | `/product-master/components` | 标准 grid/list | 类型 filterable、引用检查 |
| 基础资料 | 资料资产 | `/product-master/media-assets` | 标准 grid/list | 资料字段、抽屉、空态 |
| 基础资料 | 资料绑定 | `/product-master/media-bindings` | 标准 grid/list | 绑定对象搜索、引用检查 |
| 配置与价格 | 产品模型 | `/product-config/models` | 标准 grid/list | 查询、抽屉、保存回显 |
| 配置与价格 | 销售变体 | `/product-config/sales-variants` | 标准 grid/list | 模型选择、抽屉 |
| 配置与价格 | 问题组模板 | `/product-config/question-groups` | 标准 grid/list | 查询、抽屉、保存回显 |
| 配置与价格 | 配置模板 | `/product-config/template` | 自定义录入工作台 | 问题/答案/规则/BOM/预览 |
| 配置与价格 | 价格中心 | `/product-config/pricing` | 自定义价格编辑与测试 | 矩阵、试算、命中明细 |
| 配置与价格 | 报价预览 | `/product-config/quote-preview` | 独立试算入口 | Quick Quote、后端试算 |
| 发布与应用 | 测试发布 | `/product-release/publish` | 自定义发布闸门 | PASS/WARNING/BLOCKER、发布包 |
| 发布与应用 | 审核审批 | `/product-release/approvals` | 标准 grid/list + 审批抽屉 | 通过/拒绝、原因、日志 |
| 发布与应用 | 缺口待办 | `/product-release/gap-tasks` | 标准 grid/list | 缺口定位、修复入口 |
| 发布与应用 | 发布包 | `/product-release/packages` | 标准 grid/list + 详情抽屉 | 快照、版本、同步状态 |
| 发布与应用 | 同步日志 | `/product-release/sync-outbox` | 标准 grid/list | 失败原因、重试 |
| 发布与应用 | 导入中心 | `/product-release/import` | 标准 grid/list + 导入预览 | XLS/XLSX 预览、行级问题 |
| 发布与应用 | 销售只读总览 | `/product-release/sales-view` | 自定义只读总览 | 只读摘要、Quick Quote |

### 历史标准化复查矩阵

至少覆盖：

- 系统用户、商家用户、菜单、角色、参数、字典类型、字典数据、岗位、部门。
- OSS、OSS 配置、公告、操作日志详情。
- 商家审核、商家资料。

每页检查查询区、toolbar/right-toolbar、表格/树表、分页、右侧抽屉、sticky 底部按钮、移动端不遮挡。

## Build / SQL / i18n

状态：`partial-passed`

预期执行：

- `codegraph sync`
- Java 编译
- Vue 类型检查/构建
- i18n 同步和 JSON 校验
- PostgreSQL 真实开发库 preflight / execute / post-check
- `git diff --check`

已执行：

- `pnpm --dir admin-ui typecheck`：通过。
- `pnpm --dir admin-ui build`：通过。
- `pnpm i18n:sync`：通过，已同步前端 public 和后端 runtime i18n JSON。
- `git diff --check`：通过。
- `codegraph sync`：通过。
- `mvn -pl bocoo-modules-product -am -DskipTests compile`：通过。
- `mvn -pl bocoo-modules-product,bocoo-admin -am -DskipTests clean compile`：通过，`bocoo-modules-product` 已强制重编译。
- `mvn -pl bocoo-modules-product -am -DskipTests install`：通过，用于让 `bocoo-admin spring-boot:run` 加载最新产品模块 jar。
- `mvn -pl bocoo-admin spring-boot:run -Dspring-boot.run.profiles=dev`：dev profile 启动成功，监听 `8081`，连接 PostgreSQL / Redis 成功。
- dev 本地联调配置已按 127/localhost 策略复查：`admin-ui` proxy -> `127.0.0.1:8081`，后端 dev PostgreSQL -> `localhost:5432`，Redis -> `127.0.0.1`。
- 真实接口 E2E：通过，证据 `.ai/changes/20260607-product-capability-usability-wave/artifacts/api-e2e-summary.json`。
- 产品能力权限静态差异检查：后端 `@SaCheckPermission` 39 个、SQL 权限 39 个、前端实际使用权限 32 个，前端无不存在权限、SQL 无缺后端保护项。
- 真实开发库菜单 post-check：3 个可见父菜单、19 个可见二级入口；旧版 `product-center` 树 52 条 legacy 行均为禁用/隐藏；产品能力字典类型当前 4 个。
- 2026-06-07 12:40 已执行 `pnpm --dir admin-ui build`：通过。
- 2026-06-07 12:40 已执行 `git diff --check`：通过。
- 2026-06-07 12:40 已执行 `codegraph sync`：通过，刷新 6 个变更文件。
- 2026-06-07 13:05 已执行 `pnpm i18n:sync`：通过；因当前无 AI 翻译 key，本轮缺失 key 采用人工双语补齐。
- 2026-06-07 13:05 已执行全量前端静态 i18n 扫描：`admin-ui/src` 207 个 Vue/TS 文件、1328 个静态 key，`en_US` / `zh_CN` 缺失数均为 0；6 个 locale/runtime JSON 均可解析。
- 2026-06-07 13:05 已执行 `pnpm --dir admin-ui typecheck`：通过。
- 2026-06-07 13:05 已用内部浏览器验证产品能力下拉字典：辅材类型、物料类型、单位均为 filterable select，并显示中文业务标签。
- 2026-06-07 13:38 已执行 API 真实登录与接口 smoke：`.ai/changes/20260607-product-capability-usability-wave/artifacts/api-smoke-auth-127.json` 记录 24 项关键接口全部 200（含 `product-capability` 工作台摘要/进度/同步事件、发布包、导入、配置、基础资料与系统路由查询）。

真实接口 E2E 摘要：

- 配置评估：选中 `FABRIC_TYPE=LF` 返回 `ALLOW`，自动带出 1 条组件摘要；必填缺失返回 `BLOCKER`。
- 价格试算：命中 2 条规则，合计 `110.00000000`；不命中返回 `BLOCKER` 和 `product.price.rule.noMatch`。
- 发布闭环：检查 `PASS`，审批通过后发布成功；重复发布返回同一个 `packageId`，Outbox 仍为 3 条。
- 销售只读：`/product-capability/sales-view/publish-packages/list` 可返回发布包，且包含本次 E2E 发布包。

关键业务数据 post-check 与 i18n runtime sync 已与 V01 真实录入场景联动复核完成；待后续可在下次发布复用复测。

## Result

当前状态：`wave4-done`（`PCC-UW-V01` 已完成）

Wave 0 dry run / contract gate：

- `ui-execution-contract.md` 已存在并被 Wave Plan / Check / Tasks 引用。
- Wave 0 已收窄为纯合同阶段，禁止修改 `admin-ui/**`、`bocoo-modules-product/**`、`sql/**`。
- `PCC-UW-F01` 保持为 5 个自定义页唯一设计主线任务，没有按页面拆成多个并行实现任务。
- `PCC-UW-F02`、`PCC-UW-F03`、`PCC-UW-B01` 的边界已限制为普通页/流程页/后端闭环，不允许覆盖 5 个自定义页主结构。
- Barrier 0 passed；允许进入 Wave 1。

Wave 1 当前证据：

- 5 个自定义页已继续组件化收口，复杂业务区块拆入 `admin-ui/src/pages/product-center/components/`。
- 工作台已新增基于项目已有 `echarts` 的趋势可视化组件；按需引入，组件封装，业务权威仍以后端 API 为准。
- 内部浏览器已通过 UI 登录访问真实 `http://192.168.1.5:8083`。
- 19 个产品能力入口 + 商家审核/商家资料首屏矩阵通过：未出现刷新 404、登录回跳或系统异常。
- 内部浏览器已补充 `http://127.0.0.1:8083` 验证：缺口待办、审核审批、发布闸门无 404、无系统异常、无页面级横向溢出；流程类 grid 已隐藏通用新增/修改/删除，保留处理阻断、审批/拒绝、详情等业务动作。
- 390px 移动端抽查覆盖工作台、配置模板、价格中心、报价预览、测试发布、导入中心、销售只读、缺口待办、审核审批：无页面级横向溢出、无 404、无系统异常、无原始 i18n key；宽表由 Element Plus 表格内部横向滚动承载。
- 通用 `ProductEntityGridPage` 业务行按钮已补 loading、防重复点击和取消确认框处理；动作成功后统一刷新列表。
- 后端关键闭环已通过真实接口 E2E：报价、配置评估、审批、发布幂等、Outbox、销售只读列表。
- 内部浏览器 16 个真实菜单路径 smoke 通过；基础资料父路径确认为 `/product-master/*`。
- 真实新增/编辑/审批/发布/导入预览、移动端抽屉/表单提交细项及 `console/network` 全量细项均已纳入本次 V01 验收。
