# 共享产品能力中心 Batch 8：导入中心基础能力

## 结论

Batch 8 已完成并验证通过。

本批把导入中心从 Deferred 中拆出第一层基础能力：统一放在 `bocoo-modules-product` 和 `pc_*` 共享产品能力表里，不放订单模块。当前只实现导入批次、字段映射/预览 JSON、行级问题和状态流转，为人工导入、历史数据整理、ERP/MES 导入和后续 AI 解析提供统一承接层。

## 完成内容

- 新增 `pc_import_batch` 产品能力导入批次表。
- 新增 `pc_import_row_issue` 产品能力导入行问题表。
- 两张表均补齐表注释、所有字段中文注释、UTC 时间字段说明和查询索引。
- 将 `pc_import_batch`、`pc_import_row_issue` 加入 `bocoo.tenant.ignore-tables`。
- 新增 `ProductImportBatch` / `ProductImportRowIssue` Entity、BO、VO、Mapper。
- 新增 `ProductImportService`，支持批次分页、详情、新增、修改、状态切换、删除和行级问题维护。
- 新增 `/product-capability/import/**` Controller API。
- 新增导入中心按钮权限：`product:import:list/query/add/edit/remove`。
- 更新数据库设计、API 约束、AMU contract、任务清单和 handoff 文档。

## 当前边界

- 导入批次默认 `sourceSystem = MANUAL`、`importStatus = DRAFT`，批次号由后端生成。
- 行级问题默认 `status = 1`，用于后续人工修正、忽略或提交前检查。
- 删除导入批次时同步删除该批次行级问题。
- 当前 API 不直接提交到物料、组件、答案或价格表。
- 完整 Excel/AI 自动解析、字段智能映射、批量提交、冲突合并和目标表回滚策略仍保留 Deferred。

## 验证

- YAML parse：passed。
- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- SQL 结构检查：passed，23 张 `pc_*` 表均覆盖租户忽略、表注释和字段注释。
- 一次性 PostgreSQL 16 空库执行：passed，顺序执行 `base.sql` 和 `product_capability.sql` 成功。
- 临时库结果：23 张 `pc_*` 表、`pc_import_batch` 存在、`pc_import_row_issue` 存在、44 条产品能力菜单、44 条 `role_id = 1` 产品能力授权。
- `git diff --check`：passed。

## 残余风险

- `product_capability.sql` 仍是 PostgreSQL 初始化草案，不是生产 migration。
- 真实开发库执行前仍需按 `docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md` 做目标库 preflight。
- 导入中心当前只保存预览和问题，不做自动解析和目标表提交。
- 后续提交动作必须按目标对象单独设计事务、审计、幂等和冲突策略，不能一个接口粗暴写所有表。

## 下一步

继续 Deferred 队列：

- 完整 Excel/AI 导入解析和提交。
- ERP/MES 深度同步。
- ConfigAgent / PricingAgent。
- 客户价、促销价、客户等级价。
- 独立服务拆分执行方案。
