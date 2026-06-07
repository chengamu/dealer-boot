# Proposal: 共享产品能力中心第一阶段落地

创建日期：2026-06-07

## Requirement Source

- `.ai/requirements/20260606-product-capability-center.md`
- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`

## Background

此前 Batch 1 到 Batch 10 已经形成 `bocoo-modules-product`、产品能力 SQL 草案、基础前后端页面、价格/发布/导入/快照/outbox 骨架，并通过若干编译、空库和文档检查。

但 2026-06-06 至 2026-06-07 的真实浏览器和文档复盘确认：这些 `done` 不能代表真实业务体验完成。当前遗留问题包括：

- SQL 和菜单仍残留单一 `Product Capability` 一级菜单口径，和入口文档“三个一级菜单”冲突。
- 部分页面仍是组合 tab 或调试式页面，不符合二级菜单、标准 grid/list、抽屉和 H5 复刻要求。
- 报价、审核审批、缺口待办、发布包、同步日志、导入确认等独立功能不能只藏在发布页 tab 或 Deferred 文案里。
- 历史系统页面和代码生成器仍有旧弹窗长表单，不符合当前统一标准。
- 真实开发库、内部浏览器、真实登录态、真实接口错误、刷新 404、底部按钮可见性等验证没有形成完整闭环。

## Goal

以 `docs/产品配置中心/共享产品能力中心开发入口.md` 为合同，完成共享产品能力中心第一阶段落地：

- 历史遗留标准化问题修改。
- 新增产品、配置、价格、发布模块完成开发和 UI 复刻。
- 数据库菜单按钮、前端权限、后端权限三方一致。
- 真实开发库验证、代码审计、内部浏览器真实测试全部进入完成口径。

## Scope

- 标准化：代码生成器 Vue 模板、系统用户、商家用户、菜单、角色、参数、字典、岗位、部门、OSS、公告、操作日志详情、商家审核等页面。
- 数据库和菜单：`product_capability.sql`、必要的 `base.sql` 菜单/按钮补丁、字典、角色授权、真实库执行核验。
- 后端：`bocoo-modules-product` Controller/Service/Mapper/Engine/BO/VO，商家审核按钮权限相关后端核对。
- 前端：`admin-ui/src/pages/product-center/**`、`admin-ui/src/api/product-capability/**`、`admin-ui/src/pages/system/**`、`admin-ui/src/pages/merchant/**`、`admin-ui/src/stores/permission.ts`、i18n。
- 验证：Java 编译、Vue 类型/构建、静态 code review、内部浏览器、真实开发库 SQL/API 核验。

## Out of Scope

- 不改生产环境配置。
- 不新增 MySQL SQL。
- 不把 Redis 作为发布包、审核结果、订单快照、价格快照、BOM 快照的权威数据。
- 不做真实 ERP/MES 外部推送和回调，只保留 outbox 基础层和重试标记。
- 不做客户价、促销价、复杂价格体系。
- 不做运行时 AI 自动保存、发布或改价。

## Options

### Option A：继续在旧 Batch 计划上追加任务

Pros：不新增 change 目录，历史上下文都在一个地方。

Cons：旧 `done` 语义已经污染，容易再次把骨架、空库、编译通过误当成业务完成。

### Option B：创建第一阶段落地 change，旧 Batch 作为历史参考

Pros：能把入口文档第 7 节验收门写成新计划的完成合同，明确哪些任务必须重新验收。

Cons：需要把旧任务和新计划关系写清楚，避免重复开发。

## Recommendation

采用 Option B。

`.ai/changes/20260607-product-capability-phase1/` 是当前执行 change。旧 `.ai/changes/20260606-product-capability-center/` 保留为历史实现和可复用代码参考，不再作为第一阶段完成证明。

## Done Definition

第一阶段任何任务只有满足对应 Acceptance，并通过入口文档第 7 节相关验收门，才能标记为 `done`。

如果仅完成骨架、静态页、空库试执行、编译通过或接口占位，必须保留 `pending` 或写成更准确的中间状态，不允许冒充真实业务完成。
