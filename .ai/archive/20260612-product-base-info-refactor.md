# Archive: 产品配置中心基础资料模块重构

## Feature：功能

重构产品配置中心基础资料模块，覆盖面料设置、物料设置、组件包、配置字典、附件资料、单位管理，为后续配置器、工程、BOM、价格、发布和订单快照提供主数据基础。

## Requirement Source：需求来源

- `.ai/requirements/20260611-product-base-info-refactor.md`
- `docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`
- 用户补充要求：参考 OFBiz、统一 `material_code` / `fabric_code` 口径、保留样册编号/供应商料号、系列承载面料厚度约束、附件复用 OSS。

## Final Status：最终状态

Accepted。

## Scope：范围

- 基础资料 6 模块：面料设置、物料设置、组件包、配置字典、附件资料、单位管理。
- PostgreSQL 初始化脚本和开发库同步。
- 后端 Controller / Service / Mapper / Entity / BO / VO。
- 前端 API、页面、动态路由映射和 i18n。
- 5 个产品样本 seed。
- 旧 workbench/model/sales 基础资料污染入口清理。

## Out of Scope：不做范围

- 配置器正式试算器。
- BOM / 工程规则正式计算。
- OFBiz 全量迁移工具。
- 逐字段业务审批流和发布流。
- 严格按代码生成器拆 `IService` / `ServiceImpl` 的二次架构整理。

## Completed：已完成

- 明确 `material_code` 是统一主业务编号；样册编号、供应商料号、旧系统编号只做业务引用或追溯。
- 面料系列承载厚度、门幅和组合约束；面料资料承载颜色、材质、型号、样册、供应商料号、克重、厚度等字段。
- 电机、遥控、胶条、安装码、配重条等统一进入 `ProductMaterial` + `ProductMaterialAttribute`，不为每类物料建 profile 表。
- 附件资料复用现有 OSS，只新增 `pc_media_asset` 和 `pc_media_binding`。
- 开发库已清理旧 `pc_product_model`、`pc_sales_variant` 和旧菜单/角色菜单。
- 开发库补齐运行态发现的缺列：`pc_unit.sort_order`、`pc_component.legacy_source`、`pc_component.legacy_id`。
- 后端请求日志已对密码、验证码、token、secret 等敏感字段脱敏。

## Not Completed：未完成

- 未做逐字段新增/编辑/删除保存的完整 E2E。
- 未做 OFBiz 真实附件文件搬迁。
- 未做正式单元测试或集成测试套件。

## Validation Summary：验证摘要

- `codegraph sync`：通过。
- `sql/postgresql/product_capability.sql`：已执行到本地开发 PostgreSQL。
- Seed 计数：`pc_unit=7`、`pc_base_attribute=10`、`pc_fabric_series=3`、`pc_fabric_profile=3`、`pc_material_attribute=13`、`pc_component=5`、`pc_component_item=16`、`pc_media_asset=5`、`pc_media_binding=5`。
- `mvn -pl bocoo-modules-product -DskipTests compile`：通过。
- `mvn -pl bocoo-admin -DskipTests clean package`：通过，并用 `bocoo-admin/target/dist/bocoo-admin.jar` 做运行态验证。
- `pnpm --dir admin-ui build`：通过。
- Playwright + 本机 Chrome 真实冒烟：六个页面均打开成功，均有表格、Add 按钮和新增弹窗；本地应用 `consoleErrorCount=0`、`badResponseCount=0`、`apiFailureCount=0`、`failedPageCount=0`。

## Remaining Risks：剩余风险

- 当前验证覆盖页面入口和新增弹窗，不等于全部 CRUD 保存链路验收。
- 后续如强制完全贴合代码生成器风格，需要单独计划 service interface / impl 拆分。
- OFBiz 数据只作为 seed 参考，正式迁移还需字段映射、清洗和附件搬迁策略。

## Lessons from Learn：经验提炼

- 运行态验证应以 `bocoo-admin/target/dist/bocoo-admin.jar` 为准。
- 没有直接暴露 `browser.open` 不代表不能浏览器验证，可用 Playwright + 本机 Chrome。
- SQL DDL 不能只看新建表，必须给已有开发库写 `ALTER TABLE IF EXISTS ... ADD COLUMN IF NOT EXISTS`。
- Entity / BO / VO 与 SQL seed 任一处不一致，都可能在真实页面列表接口暴露 500。

## Key Decisions：关键决策

- `material_code` / `fabric_code` 统一为内部物料编号，不再额外保留 SKYSPF 编号字段。
- 样册编号、供应商料号保留为业务字段。
- 面料约束集中在系列层，面料资料只表达颜色、材质和物料属性。
- 附件不重造上传能力，复用 OSS。

## Files Modified：修改文件

- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `bocoo-common/bocoo-common-web/src/main/java/com/bocoo/common/web/interceptor/PlusWebInvokeTimeInterceptor.java`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/stores/permission.ts`
- `i18n/locales/en_US.json`
- `i18n/locales/zh_CN.json`
- `bocoo-admin/src/main/resources/i18n/en_US.json`
- `bocoo-admin/src/main/resources/i18n/zh_CN.json`
- `docs/产品配置中心重构/**`
- `.ai/changes/20260611-product-base-info-refactor/**`

## Artifacts：产物

- `.ai/changes/20260611-product-base-info-refactor/check.md`
- `.ai/changes/20260611-product-base-info-refactor/db-backend-contract.md`
- `.ai/changes/20260611-product-base-info-refactor/frontend-contract.md`
- `.ai/changes/20260611-product-base-info-refactor/seed-contract.md`

## Follow-up：后续事项

- 新开 `/plan` 做基础资料逐项 CRUD 保存验收。
- 新开 `/plan` 做配置器试算器和系列约束校验。
- 新开 `/plan` 做 OFBiz 数据迁移导入工具和附件搬迁。
