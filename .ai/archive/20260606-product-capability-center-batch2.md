# Archive: 共享产品能力中心 Batch 2

## Status

Accepted with Risks

## Scope Completed

- PCC-20：配置模板、模板版本、问题组、问题、答案、规则后端 CRUD 和 `ConfigEvaluationEngine` MVP。
- PCC-21：配置录入工作台前端、配置 API、二级菜单路由和 i18n。
- PCC-22：价格方案、价格版本、价格规则项后端 CRUD、`PriceCalculationEngine` MVP、价格工作台前端。
- PCC-23：发布检查结果、审批记录、不可变发布包、同步 outbox 后端能力。
- PCC-24：测试发布前端页面。
- PCC-90：静态审查。
- PCC-91：运行 smoke 验证。

## Key Files

- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/ProductConfigController.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/ProductPricingController.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/ProductPublishController.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/ConfigEvaluationEngine.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/PriceCalculationEngine.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/ProductPublishService.java`
- `admin-ui/src/pages/product-center/template/ConfigTemplateWorkbenchPage.vue`
- `admin-ui/src/pages/product-center/pricing/PricingWorkbenchPage.vue`
- `admin-ui/src/pages/product-center/publish/PublishGatePage.vue`
- `admin-ui/src/api/product-capability/config.ts`
- `admin-ui/src/api/product-capability/pricing.ts`
- `admin-ui/src/api/product-capability/publish.ts`
- `sql/postgresql/product_capability.sql`

## Validation

- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- `./node_modules/.bin/vue-tsc --noEmit`：passed。
- `./node_modules/.bin/vite build`：passed。
- `node i18n/scripts/sync.ts`：passed。
- `jq empty i18n/locales/en_US.json admin-ui/public/i18n/en_US.json bocoo-admin/src/main/resources/i18n/en_US.json`：passed。
- `codegraph sync`：passed，already up to date。
- Vite + Playwright smoke：passed with environment limitation。页面 200 并在未登录时重定向登录页；`/captchaImage` 500 来自后端 8081 未启动。

## Review Notes

- 发布包和 outbox 已收敛为 API 只读，写入只能通过发布流程生成。
- Batch 2 没有引入 Redis 权威缓存。
- 产品能力核心表继续按平台共享能力处理，不新增租户字段。
- 新增 SQL 草案使用 PostgreSQL，时间字段为 `timestamptz` 并标注 UTC。
- 新增可见前端文案写入 `i18n/locales/en_US.json`，runtime i18n 由同步脚本生成。

## Residual Risks

- `sql/postgresql/product_capability.sql` 仍是初始化草案，未真实落库执行。
- `ConfigEvaluationEngine` 不执行动态表达式，复杂规则待 schema 冻结后接入。
- `PriceCalculationEngine` 只做基础金额和单位价累计，矩阵价、面积价和公式仍是 MVP。
- 发布包 JSON/hash schema 仍是最小稳定结构，后续需要扩展成完整 read model。
- 运行 smoke 未覆盖登录后的真实菜单、权限按钮和 API 数据交互。

## Next Queue

- PCC-30：销售只读总览，读取发布包/read model，不读草稿。
- PCC-31：订单快照内部消费，订单侧保存配置、价格、配件和发布包版本快照。
- PCC-40：AI、导入中心、ERP/MES 深度同步、客户价/促销价等延期能力。
