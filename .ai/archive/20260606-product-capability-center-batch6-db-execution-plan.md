# Archive: 共享产品能力中心 Batch 6 DB Execution Plan

## Status

Accepted with Risks

## Scope Completed

- PCC-52：真实库受控执行计划。
- 新增 `docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md`。
- 明确 preflight 查询、执行命令模板、post-check 查询、应用侧核验和回退策略。

## Key Files

- `docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md`
- `.ai/changes/20260606-product-capability-center/tasks.md`
- `.ai/TASKS.md`
- `.ai/CURRENT.md`
- `.ai/HANDOFF.md`

## Validation

- 文档审查：passed。
- `codegraph sync`：passed，already up to date。
- `git diff --check`：passed。

## Review Notes

- 计划明确禁止生产库直接手工执行。
- 计划要求先确认目标库是开发库或测试库。
- 计划要求先查 `pc_*` 同名表、`24000-24099` 菜单 ID、`product:%` 权限码、`role_id = 1`。
- 计划给出执行后预期：20 张 `pc_*` 表、36 条产品能力菜单、36 条管理员角色授权。
- 计划给出开发库回退 SQL，生产库回退必须走备份或正式 rollback。

## Residual Risks

- 本批没有连接真实开发库，也没有执行远程数据库。
- 如果真实库已有同名表，`CREATE TABLE IF NOT EXISTS` 不会迁移结构，需要单独 diff。
- 生产发布仍需正式 migration 或发布脚本，不建议直接使用初始化草案。

## Next Queue

- 真实开发库人工确认后执行 `product_capability.sql`。
- 订单系统接入订单快照持久化。
- Deferred：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分。
