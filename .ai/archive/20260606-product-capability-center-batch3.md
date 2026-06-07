# Archive: 共享产品能力中心 Batch 3

## Status

Accepted with Risks

## Scope Completed

- PCC-30：销售只读总览页面、路由、菜单 SQL 和只读 grid 模式。
- PCC-31：订单快照构建 API、Service、BO/VO 和按钮权限草案。
- PCC-40：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分继续保留 Deferred。

## Key Files

- `admin-ui/src/pages/product-center/sales-view/SalesReadOnlyPage.vue`
- `admin-ui/src/pages/product-center/components/ProductEntityGridPage.vue`
- `admin-ui/src/stores/permission.ts`
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/ProductOrderSnapshotController.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/ProductOrderSnapshotService.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/bo/OrderSnapshotBuildBo.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/OrderProductSnapshotVo.java`
- `bocoo-modules-product/pom.xml`
- `sql/postgresql/product_capability.sql`

## Validation

- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- `./node_modules/.bin/vue-tsc --noEmit`：passed。
- `./node_modules/.bin/vite build`：passed。
- `node i18n/scripts/sync.ts`：passed。
- `jq empty i18n/locales/en_US.json admin-ui/public/i18n/en_US.json bocoo-admin/src/main/resources/i18n/en_US.json`：passed。
- `codegraph sync`：passed，already up to date。
- 轻量静态扫查：未发现产品业务表新增租户字段、Redis 权威缓存或直接 `LocalDateTime.now()`；`tenant_id` 仅出现在 `sys_menu/sys_role_menu` 菜单授权 SQL。

## Review Notes

- 销售只读视图只读取发布包列表，不读取配置/价格草稿。
- 通用 grid 页面增加 `readonly` 模式，销售视图隐藏新增、编辑、删除入口。
- 订单快照由产品能力中心构建，但由订单系统持久化；本批不在产品能力中心新增订单快照表。
- 订单快照输出包含发布包 ID、发布包编码、发布包 hash、产品模型、销售变体、模板版本、价格版本、用户选择、输入值、`snapshotJson` 和 `snapshotHash`。
- `snapshotJson` 使用项目已有 `JsonUtils.toJsonString` 结构化生成，避免手工 JSON 拼接。
- 本批没有引入外部依赖；产品模块仅显式依赖已有 `bocoo-common-json`。

## Residual Risks

- `sql/postgresql/product_capability.sql` 仍是 PostgreSQL 初始化草案，未真实落库执行。
- 发布包 JSON/hash schema 仍是 MVP，后续需要冻结完整 read model schema。
- 订单系统还需要单独接入持久化落点，把返回的订单产品快照保存到订单业务表或订单快照表。
- 销售只读视图当前基于发布包列表展示，问题答案、资料和价格状态的完整展开依赖后续发布包 read model 结构扩展。
- 运行 smoke 未覆盖登录后的真实菜单权限按钮和真实 API 数据交互。

## Next Queue

- 真实落库前表级评审：确认所有 `pc_*` 表字段、索引、中文注释、UTC 注释和 ignore tenant 配置。
- Deferred：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分。
- 订单系统接入：定义订单侧快照持久化字段、生成时机、重试策略和历史查询页面。
