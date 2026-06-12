# Design: 产品配置中心基础资料模块重构

## Existing Code Baseline

已通过 codegraph 核对：

- `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue` 当前只有分类、物料、组件页签。
- `admin-ui/src/pages/product-center/assets/ProductAssetPage.vue` 当前只有资料资产和资料绑定页签。
- 后端已有 `ProductCategory`、`ProductMaterial`、`ProductComponent`、`ProductMediaAsset`、`ProductMediaBinding`，可保留改造。
- 当前缺少面料、单位、通用属性、组件明细等主数据对象。

## Data Model

### ProductCategory

用途：产品分类、系列入口、可配置对象分类。

关键字段：

- `category_code`
- `category_name_cn`
- `category_name_en`
- `parent_id`
- `category_path`
- `category_level`
- `business_type`
- `status`

### ProductUnit

用途：单位管理，替代仅靠普通字典的 `product_unit`。

关键字段：

- `unit_code`
- `unit_name_cn`
- `unit_name_en`
- `unit_type`
- `precision_scale`
- `rounding_mode`
- `base_unit_code`
- `conversion_rate`
- `status`

### ProductMaterial

用途：所有物料主档。

主编号：`material_code`。

高频冗余字段：

- `material_name_cn`
- `material_name_en`
- `material_type`
- `business_type`
- `unit_code`
- `supplier_code`
- `supplier_name`
- `factory_model`
- `sample_book_no`
- `vendor_item_no`
- `primary_spec`
- `primary_color`
- `primary_weight`
- `attribute_summary`
- `legacy_source`
- `legacy_id`

### ProductMaterialAttribute

用途：非面料物料的差异属性，也可给面料补充低频属性。

示例：

- `H0004141` 透明 PET 胶条：`spec=1.2`
- `H0008007` 配重条：`weight_g=50`、`spec=3.5*12`
- 电机：`voltage=24V`、`protocol=ZIGBEE`、`wire_type=接线款`
- 遥控：`channel_count=15`、`color=white`

关键字段：

- `material_id`
- `material_code`
- `attribute_id`
- `attribute_code`
- `attribute_name_cn`
- `value_text`
- `value_number`
- `value_bool`
- `value_unit_code`
- `sort_order`
- `status`

### ProductBaseAttribute

用途：配置字典和属性定义，不承载每个物料的值。

关键字段：

- `attribute_group`
- `attribute_code`
- `attribute_name_cn`
- `attribute_name_en`
- `value_type`
- `unit_code`
- `material_types`
- `extra_json`
- `status`

### FabricSeries

用途：面料系列和系列级约束。

关键字段：

- `series_code`
- `series_name_cn`
- `series_name_en`
- `material_type`
- `default_thickness_unit`
- `thickness_rule_enabled`
- `max_thickness_diff`
- `max_combined_thickness`
- `width_rule_enabled`
- `available_widths`
- `min_width_value`
- `max_width_value`
- `width_unit`
- `status`

### FabricProfile

用途：面料系列下具体颜色、材质、纹理、涂层、工厂型号变体。

关键字段：

- `fabric_code`
- `material_id`
- `material_code`
- `series_id`
- `series_code`
- `series_name_cn`
- `color_code`
- `color_name`
- `material_composition`
- `texture_type`
- `finish_type`
- `width_value`
- `width_unit`
- `thickness_value`
- `thickness_unit`
- `gsm_value`
- `sample_book_no`
- `vendor_item_no`
- `supplier_code`
- `supplier_name`
- `purchase_unit_code`
- `inventory_unit_code`
- `sales_unit_code`
- `legacy_attribute_text`
- `status`

说明：`fabric_code` 与 `material_code` 统一，不再新增第二套主编号。

### ProductComponent / ProductComponentItem

用途：组件包及其明细。

`ProductComponent` 关键字段：

- `component_code`
- `component_name_cn`
- `component_name_en`
- `component_type`
- `business_type`
- `default_qty`
- `qty_mode`
- `unit_code`
- `status`

`ProductComponentItem` 关键字段：

- `component_id`
- `component_code`
- `material_id`
- `material_code`
- `material_name_cn`
- `qty_formula`
- `default_qty`
- `unit_code`
- `sort_order`
- `required_flag`
- `status`

### MediaAsset / MediaBinding

复用现有 `sys_oss`。

`MediaAsset` 需要支持：

- `oss_id`
- `asset_code`
- `asset_name_cn`
- `asset_name_en`
- `asset_type`
- `usage_type`
- `language_code`
- `visibility`
- `version_no`
- `legacy_source`
- `legacy_id`
- `legacy_path`
- `legacy_url`

`MediaBinding` 需要支持：

- `asset_id`
- `asset_code`
- `target_type`
- `target_id`
- `target_code`
- `usage_type`
- `required_for_publish`
- `sort_order`

## API / Service Boundary

建议 Controller：

- `ProductBaseInfoController`
- `ProductFabricController`
- `ProductComponentController`，若现有 base controller 可清晰承载则不强制拆。
- `ProductMediaController`，若现有 asset API 可清晰承载则不强制拆。

建议 Service：

- `ProductBaseInfoService`
- `ProductFabricService`
- `ProductMaterialAttributeService`
- `ProductReferenceCheckService`
- `ProductMediaAssetService`
- `OfbizSeedImportService`，仅用于开发 seed，不作为正式迁移接口。

## Frontend Boundary

基础资料模块必须让这些入口可维护：

- 面料设置
- 物料设置
- 组件包
- 配置字典
- 附件资料
- 单位管理

页面可基于现有 `ProductEntityGridPage` 继续演进；复杂明细如物料属性、组件明细可用抽屉或子表，但不能只保留单行字段。

## Seed Boundary

OFBiz 数据只做测试 seed：

- 可写开发 SQL 或受控 dev-only seed service。
- 不打印数据库连接串、账号、密码。
- 不能把 OFBiz `Content` / `DataResource` 模型照搬进新系统。

## Validation Plan

`/do` 完成后至少验证：

- `codegraph sync`
- 后端编译或模块级检查。
- PostgreSQL 初始化脚本执行到开发库。
- 前端 typecheck / build。
- 浏览器 smoke：基础资料 6 个模块入口可打开，列表和新增编辑弹窗无明显错误。
- 5 个 seed 产品样本可查询。
