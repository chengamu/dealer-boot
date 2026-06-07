# Archive: 共享产品能力中心 Batch 4 DB Readiness

## Status

Accepted with Risks

## Scope Completed

- PCC-50：数据库落库前表级评审和加固。
- 修正 `sql/postgresql/product_capability.sql` 中两个会导致执行失败的索引字段。
- 将 20 张 `pc_*` 平台共享业务表加入 `bocoo.tenant.ignore-tables`。
- 新增用户可见评审文档：`docs/产品配置中心/共享产品能力中心数据库落库评审.md`。

## Key Files

- `sql/postgresql/product_capability.sql`
- `bocoo-admin/src/main/resources/application.yml`
- `docs/产品配置中心/共享产品能力中心数据库落库评审.md`
- `.ai/changes/20260606-product-capability-center/tasks.md`
- `.ai/TASKS.md`
- `.ai/CURRENT.md`
- `.ai/HANDOFF.md`

## Fixes

- `idx_pc_config_template_version_status` 从 `status` 改为 `version_status`。
- `idx_pc_price_plan_version_status` 从 `status` 改为 `version_status`。
- `pc_category` 到 `pc_product_sync_outbox` 共 20 张表加入租户忽略配置，匹配“产品能力核心表不加 tenant_id”的数据库方案。

## Validation

- YAML 解析：passed，`bocoo-admin/src/main/resources/application.yml` 可解析。
- SQL 结构检查脚本：passed，20 张表、20 张 `pc_*` 表、0 issues。
- 检查项包括：索引引用表/字段存在、表注释存在、字段注释存在、`pc_*` 表不含 `tenant_id`、`pc_*` 表全部进入 ignore-tables、时间字段注释包含 UTC 或 `timestamptz`。
- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- `codegraph sync`：passed，already up to date。
- `git diff --check`：passed。

## Review Notes

- 产品能力核心表继续按平台共享能力处理，不新增 `tenant_id`。
- 系统菜单和角色菜单仍按现有系统表结构使用 `tenant_id = 1`。
- 本批没有执行 SQL，也没有连接真实数据库。
- 本批没有引入外部依赖。

## Residual Risks

- `sql/postgresql/product_capability.sql` 仍是初始化草案，真实执行前需要确认目标数据库是否已有同名表、菜单 ID、权限码和角色授权冲突。
- `jsonb` 字段暂未增加 GIN 索引，需要基于真实查询压力决定。
- 发布包 read model、价格公式、配置规则 schema 仍是 MVP。
- 后续如果产品能力服务拆出去，需要把租户忽略表配置迁移到新服务配置中。

## Next Queue

- 控制环境试执行 `sql/postgresql/product_capability.sql`，记录实际 PostgreSQL 兼容性和菜单授权结果。
- 订单系统接入订单快照持久化。
- Deferred：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分。
