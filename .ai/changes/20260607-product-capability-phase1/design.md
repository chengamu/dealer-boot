# Design: 共享产品能力中心第一阶段落地

创建日期：2026-06-07

## 1. 核心设计决策

| 决策 | 结论 |
| --- | --- |
| 开发合同 | `docs/产品配置中心/共享产品能力中心开发入口.md` 是第一阶段合同 |
| 菜单形态 | 三个一级菜单：`基础资料`、`配置与价格`、`发布与应用`；侧边栏最多两级 |
| 模块边界 | 统一放在 `bocoo-modules-product`，为后续独立服务拆分保留边界 |
| 数据边界 | 平台共享主数据，P0 不按商户租户隔离；真实库执行时核验租户忽略或等价策略 |
| 页面规范 | 普通页走标准 grid/list + 右侧抽屉；5 个主自定义页可自定义内容区；报价预览是独立轻量试算入口 |
| 权威计算 | 配置求值、价格计算、BOM、发布检查、快照、发布包由后端 Service/Engine 负责 |
| 缓存 | Redis 不承载权威数据；性能靠宽表、read model、JSONB 摘要 |
| 验收 | 代码审计、内部浏览器真实测试、真实开发库验证是完成条件，不是可选项 |

## 2. 当前代码事实

- 已存在 `bocoo-modules-product`，包含 Controller、Service、Mapper、Entity、BO、VO、Engine 基础文件。
- 已存在 `admin-ui/src/pages/product-center/**` 和 `admin-ui/src/api/product-capability/**`。
- `admin-ui/src/stores/permission.ts` 已有 `product-center/workbench/base/model/template/pricing/publish/sales-view/assets` 等组件映射。
- `sql/postgresql/product_capability.sql` 仍有旧 `Product Capability` 一级菜单和组合入口；后续追加了一批二级入口，但还没有完全按三组一级菜单收敛。
- `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue`、`ProductAssetPage.vue`、`ProductModelPage.vue` 仍以 tab/复用页方式承载多个独立功能，正式菜单需要独立入口并注入明确模式。
- `PricingWorkbenchPage.vue`、`PublishGatePage.vue` 仍保留 JSON textarea 和 tab/grid 混合痕迹，需要按 H5/PNG 和便捷录入口径继续复刻。
- 系统管理、商家用户、字典、参数、部门、岗位、OSS、公告、操作日志详情等历史页面仍有 `el-dialog` 长表单。
- `TenantApplicationsPlaceholder.vue` 已有通过/拒绝按钮和抽屉/拒绝弹窗，但仍需要按标准页面风格、真实菜单按钮权限和桌面/移动显示完整性复核。

## 3. Contract

### 3.1 数据库和菜单

P0 必须完成：

- `基础资料` 一级菜单：工作台、产品分类、物料管理、辅材管理、资料资产、资料绑定。
- `配置与价格` 一级菜单：产品模型、销售变体、问题组模板、配置模板、价格中心、报价预览。
- `发布与应用` 一级菜单：测试发布、审核审批、缺口待办、发布包、同步日志、导入中心、销售只读总览。
- 每个二级菜单有稳定 `path`、`component`、`perms`、`i18n_key`、`visible/status`。
- 新增、编辑、删除、导出、上传、绑定、测试、检查、通过、拒绝、发布、重试、报价、下载等按钮必须建 `F` 菜单。
- `sys_menu.perms`、前端 `v-hasPermi`、后端 `@SaCheckPermission` 三方一致。
- 真实开发库执行后，用 SQL 查询核验菜单、按钮、角色授权和组件路径。

### 3.2 后端

P0 必须完成：

- 标准 CRUD 使用当前生成器分层：Controller、Service、ServiceImpl、Mapper、Entity、BO、VO。
- Controller 返回 `R<T>` / `TableDataInfo<T>`，不返回 Entity。
- 复杂能力进入 Service/Engine：配置求值、BOM、价格试算、发布检查、快照构建、发布包、outbox。
- 报价预览必须调用价格引擎，不由前端计算最终价。
- 审核通过/拒绝必须有独立权限和真实接口，不共用泛化权限。
- 发布成功写 outbox；发布包和快照不可变并可追溯。
- 导入中心 P0 完成 XLS/XLSX 解析预览、行级问题和状态流转；字段映射确认、冲突合并、批量提交正式表和回滚策略保留为 TODO，不冒充正式提交闭环。

### 3.3 前端

P0 必须完成：

- 普通页面统一标准结构：查询区、toolbar、right-toolbar、表格、分页、右侧抽屉。
- 长表单新增/编辑/详情默认右侧抽屉；短确认和少量输入保留小弹窗。
- 产品分类、物料管理、辅材管理、资料资产、资料绑定、产品模型、销售变体、问题组模板、审核审批、缺口待办、发布包、同步日志、导入中心必须是独立二级菜单或明确独立路由入口。
- 5 个主自定义页在当前 `admin-ui` 内容区复刻 H5/PNG 主结构：工作台、配置模板、价格中心、测试发布、销售只读总览。
- 报价预览是独立轻量试算入口，复用价格和销售组件，不计入 5 个主自定义页。
- 复杂区块组件化，页面 SFC 只做编排；没有公共组件时沉淀领域组件。
- 所有字典下拉 `filterable`；物料、辅材、产品、用户、角色、问题组、价格方案等对象选择必须支持远程搜索、分页或选择器。
- 新增可见文案写 `i18n/locales/en_US.json`，同步 runtime JSON。
- 时间展示和提交遵守 UTC 工具。

### 3.4 验收

完成必须通过：

- Lane A Static Review：`code-reviewer` 或 main 静态审计，覆盖权限、SQL、事务、DTO/VO、XSS、NPE、重复提交、i18n、UTC、菜单一致性。
- Lane B Runtime Validation：内部浏览器真实地址 `192.*`、真实登录态、真实开发库，验证菜单进入、刷新、接口错误、搜索、录入、保存、底部按钮、5 个自定义页视觉和关键流程。
- Build/Test：至少 Java 产品模块和 admin 编译、Vue 类型检查/构建、i18n 同步校验、SQL 执行/核验。

## 4. 风险和暂停点

| 风险 | 处理 |
| --- | --- |
| 真实开发库已有菜单 ID 或权限冲突 | 先做 preflight SQL，冲突则更新计划，不直接覆盖不明数据 |
| 历史页面整改范围大 | 作为 P0 单独任务；不能因产品能力开发赶进度延期 |
| H5 像素复刻和现有框架冲突 | 以现有 Layout/权限/i18n/UTC 为硬约束，H5 只还原内容区 |
| 真实浏览器需要登录态或 token | 优先用当前浏览器登录态；登录失败按用户授权从 Redis 查 token，但不输出 token |
| 环境无法启动或外部依赖不可用 | 任务标记 `blocked` 并记录恢复条件，不能写 `done` |

## 5. Deferred

- 客户价、促销价、复杂价格体系。
- 真实 ERP/MES 外部推送、回调确认、失败退避和定时重试。
- AI 自动字段识别、ConfigAgent、PricingAgent。
- 导入字段映射确认后的批量提交正式表、冲突合并和回滚策略。
- 产品能力中心独立服务拆分。
