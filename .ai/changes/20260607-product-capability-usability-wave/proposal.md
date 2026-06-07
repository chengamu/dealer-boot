# Proposal: 共享产品能力中心上线体验收口 Wave

## Summary：摘要

以前一阶段归档为基础继续推进，把共享产品能力中心从“底座可跑”推进到“可上线使用”。本 Wave 不扩大到客户价、AI、ERP/MES 深度同步或独立服务拆分；聚焦 P0 上线体验、真实录入、视觉舒适性、前后端闭环和浏览器证据。

## Requirement Source：需求来源

- `.ai/requirements/20260607-product-capability-usability-wave.md`
- `.ai/archive/20260607-product-capability-phase1.md`
- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`

## Problem：问题

Phase1 完成了菜单、权限、真实库和基础闭环，但仍有明显上线体验缺口：

- UI 精修没有作为独立 P0 呈现，容易误解为后续优化。
- 5 个自定义页只是第一版可跑页面，尚未完成设计参考转 Vue 组件化重构、交互优化和强证据视觉验收。
- 配置模板、价格中心、发布闸门、销售只读等页面仍需要提升录入效率和视觉舒适性。
- 报价预览、审核、缺口、发布包、同步日志、导入中心需要从“能触发”推进到“真实流程可用”。
- 历史页面标准化需要复查，避免旧模板继续污染新功能。

## Options：可选方案

### Option A：小范围修明显 bug

只处理当前肉眼看到的问题和接口错误。

Trade-off：速度快，但仍无法证明可上线使用；`done` 仍可能退化成“局部可跑”。

### Option B：上线体验收口 Wave

以入口文档第 7 节验收门为合同，按 Wave 执行 contract、实现、集成、审计、浏览器验收。

Trade-off：计划和验证更重，但能保住“不是 MVP”的目标。

## Recommendation：推荐

选择 Option B。本 Wave P0 明确为“上线体验收口”，UI 精修和真实录入流程不后延。

## Scope：范围

- 5 个主自定义页按设计文档和静态参考转 Vue 组件化重构、UI 精修和操作优化。
- 普通产品能力 grid/list 页面可用性复查。
- 报价预览 / Quick Quote、审核审批、缺口待办、发布包、同步日志、导入中心关键流程。
- 历史页面和代码生成器标准化复查。
- 真实开发库、内部浏览器、Static Review、Build/Test/i18n/UTC 验收。

## Out of Scope：不做范围

- 客户价、促销价、复杂价格体系。
- AI 智能识别、ConfigAgent、PricingAgent。
- ERP/MES 深度同步和外部回调。
- 独立服务拆分。
- 生产环境配置和生产库变更。

## Risks：风险

- 发布检查批次/去重若涉及结构调整，必须受控 SQL。
- 浏览器截图接口可能再次超时，需保留替代证据。
- 页面体验调整容易扩大成重构，必须保持 minimal diff。

## Done Definition：完成定义

只有通过入口文档第 7 节验收门，并且 Static Review、Runtime Validation、真实开发库和构建检查全部通过，才能把本 Wave 任务标记为 `done`。
