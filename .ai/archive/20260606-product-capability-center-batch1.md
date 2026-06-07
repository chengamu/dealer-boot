# Archive: shared product capability center batch 1

## Feature：功能

共享产品能力中心 Batch 1 基础能力。

## Requirement Source：需求来源

- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `.ai/requirements/20260606-product-capability-center.md`
- `.ai/changes/20260606-product-capability-center/wave-plan.md`

## Final Status：最终状态

Accepted with Risks。

## Scope：范围

- Wave 0 contract：模块名、tenant、SQL 草案、API contract、前端菜单与页面 contract。
- Wave 1 Batch 1：后端产品能力模块、基础资料 CRUD、产品模型/销售变体骨架、资料资产/绑定、工作台只读 API、前端菜单映射、基础 grid、工作台页面、i18n 同步。

## Out of Scope：不做范围

- Batch 2 配置模板、规则求值、价格中心、发布包、outbox。
- Batch 3 销售只读总览、订单快照内部消费。
- Redis 权威缓存。
- 生产配置修改。

## Completed：已完成

- 新增 `bocoo-modules-product` 模块并接入根 `pom.xml` 与 `bocoo-admin/pom.xml`。
- 新增 Batch 1 基础资料 Entity / BO / VO / Mapper / Service / Controller。
- 新增产品能力工作台只读 VO 和 API。
- 新增 PostgreSQL SQL 草案 `sql/postgresql/product_capability.sql`，表与字段中文注释全量覆盖，时间字段使用 `timestamptz` 并标注 UTC。
- 新增产品能力前端 API、通用 grid、基础信息页、产品模型页、组件资料页、工作台页。
- 新增动态菜单 component 映射和 Batch 1 `sys_menu` / `sys_role_menu` 草案。
- 新增 i18n 文案并通过同步脚本生成 runtime i18n 文件。

## Not Completed：未完成

- Batch 2 / Batch 3 任务保留在 `.ai/changes/20260606-product-capability-center/tasks.md` 和 `wave-plan.md`。
- 真实数据库执行仍需人工表级评审后进行。

## Validation Summary：验证摘要

- `codegraph sync`：passed。
- `jq empty i18n/locales/en_US.json admin-ui/public/i18n/en_US.json bocoo-admin/src/main/resources/i18n/en_US.json`：passed。
- `node i18n/scripts/sync.ts`：passed。
- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- `./node_modules/.bin/vue-tsc --noEmit`：passed。
- `./node_modules/.bin/vite build`：passed。
- `pnpm`：Not Run，全局命令不存在，已改用本地 Node/Vite/Vue TSC 脚本验证。

## Remaining Risks：剩余风险

- `sql/postgresql/product_capability.sql` 是初始化草案，不是已执行 migration。
- 引用检查当前是稳定 contract 骨架，后续需要接真实引用统计。
- 工作台 progress / priority / sync-events 当前返回稳定空分页，后续需要接 read model / outbox。
- Batch 2 的规则 JSON schema、价格规则项字段和发布包 hash schema 仍需在实现中冻结。

## Lessons from Learn：经验提炼

- 共享产品能力适合独立模块与独立 `pc_*` 表，不应绑定订单域。
- 快照、审核、发布包优先冗余必要字段，便于隔离、追溯、查询和后续 AI 读取。
- Batch 1 先走真实 API 骨架比前端 mock 更利于后续联调。

## Key Decisions：关键决策

- 模块名：`bocoo-modules-product`。
- Java package：`com.bocoo.product`。
- 前端路由：`/product-center/*`。
- 后端 API：`/product-capability/*`。
- Batch 1 核心 `pc_*` 表按平台共享能力处理，不引入业务租户隔离字段。
- Batch 1 不引入 Redis 作为权威缓存。

## Files Modified：修改文件

- `.ai/changes/20260606-product-capability-center/**`
- `.ai/requirements/20260606-product-capability-center.md`
- `pom.xml`
- `bocoo-admin/pom.xml`
- `bocoo-modules-product/**`
- `sql/postgresql/product_capability.sql`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/stores/permission.ts`
- `i18n/locales/en_US.json`
- `admin-ui/public/i18n/en_US.json`
- `bocoo-admin/src/main/resources/i18n/en_US.json`

## Artifacts：产物

- Batch 1 后端模块与 SQL 草案。
- Batch 1 前端页面与菜单映射。
- AMU contract、task、event、archive 文档。

## Follow-up：后续事项

- 继续执行 Wave 2：PCC-20 配置模板和规则求值、PCC-21 配置录入工作台、PCC-22 价格中心、PCC-23 发布检查/发布包/outbox、PCC-24 测试发布前端。
- Wave 2 完成后继续 Batch 3 销售只读和订单快照内部消费。
