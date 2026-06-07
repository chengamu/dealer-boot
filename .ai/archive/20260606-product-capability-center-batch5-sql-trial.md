# Archive: 共享产品能力中心 Batch 5 SQL Trial

## Status

Accepted with Risks

## Scope Completed

- PCC-51：临时 PostgreSQL 16 空库试执行。
- 顺序执行 `sql/postgresql/base.sql` 和 `sql/postgresql/product_capability.sql`。
- 发现并修复 Batch 2 / Batch 3 菜单 SQL 中 `tenant_id` 缺失问题。
- 更新 `docs/产品配置中心/共享产品能力中心数据库落库评审.md`。

## Key Files

- `sql/postgresql/product_capability.sql`
- `docs/产品配置中心/共享产品能力中心数据库落库评审.md`
- `.ai/changes/20260606-product-capability-center/tasks.md`
- `.ai/TASKS.md`
- `.ai/CURRENT.md`
- `.ai/HANDOFF.md`

## Findings Fixed

- `sys_menu.tenant_id` 非空，Batch 2 / Batch 3 后追加菜单 SQL 漏写 `tenant_id` 会导致执行失败。
- `sys_role_menu.tenant_id` 非空，最终角色授权 SQL 漏写 `tenant_id` 会导致执行失败。
- 已统一为产品能力菜单和授权写入 `tenant_id = 1`，与现有系统初始化 SQL 口径一致。

## Validation

- Docker 临时 PostgreSQL 16 容器：passed，已停止并删除。
- `base.sql` 空库执行：passed。
- `product_capability.sql` 空库执行：passed。
- 结果查询：
  - `pc_*` 表：20 张。
  - 产品能力菜单：36 条。
  - `role_id = 1` 产品能力授权：36 条。
- SQL 结构检查脚本：passed，0 issues。
- `codegraph sync`：passed，already up to date。
- `git diff --check`：passed。

## Residual Risks

- 本批验证的是一次性空库，不是共享开发库或生产库。
- 真实库执行前仍需确认菜单 ID `24000-24099` 是否被占用。
- 真实库执行前仍需确认权限码是否与已有业务重复。
- 如果真实库已有部分 `pc_*` 表，`CREATE TABLE IF NOT EXISTS` 不会对已有表做结构迁移，需要单独 diff。

## Next Queue

- 真实开发库受控执行计划：先备份、检查冲突、执行、核对菜单/授权/表结构。
- 订单系统接入订单快照持久化。
- Deferred：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分。
