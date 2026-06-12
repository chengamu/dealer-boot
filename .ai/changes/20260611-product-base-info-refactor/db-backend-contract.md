# DB / Backend Contract

日期：2026-06-11

## 0. 边界与结论

- 本文只定义产品配置中心基础资料模块重构的数据库与后端 contract，不实现业务代码。
- 本次 contract 分析边界：
  - `sql/postgresql/product_capability.sql`
  - `bocoo-modules-product/src/main/java/com/bocoo/product/**`
  - `.ai/requirements/20260611-product-base-info-refactor.md`
  - `.ai/changes/20260611-product-base-info-refactor/design.md`
  - `.ai/changes/20260611-product-base-info-refactor/wave-plan.md`
  - `docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`
- 最小安全方案：
  - 保留现有物理表名 `pc_category`、`pc_material`、`pc_component`、`pc_media_asset`、`pc_media_binding`，只做字段补强和语义重定义，不在 Wave 1 引入表重命名。
  - 新增 `pc_unit`、`pc_fabric_series`、`pc_fabric_profile`、`pc_material_attribute`、`pc_base_attribute`、`pc_component_item`。
  - 保持现有 `/product-capability` API 前缀、`R<T>` / `TableDataInfo<T>` 返回壳、`change-status`、`references`、`@SaCheckPermission` 风格。
- 已识别的实现前风险：
  - 当前 `product_capability.sql` 基础资料表没有 `tenant_id`，但项目全局启用了 `TenantLineInnerInterceptor`。Wave 1 DDL 必须显式补齐 `tenant_id`，否则 Mapper 查询存在租户拦截失败风险。

## 1. 统一数据库约束

### 1.1 通用列 contract

基础资料相关表统一包含以下列：

| 列名 | 说明 |
| --- | --- |
| `tenant_id` | 租户 ID，`bigint not null`，唯一约束和查询索引默认带入该列 |
| `status` | 业务状态，建议 `ENABLED` / `DISABLED` / `DRAFT` / `DEPRECATED`，基础资料至少支持 `ENABLED` / `DISABLED` |
| `del_flag` | 逻辑删除标记，沿用项目现状：`0` 存在，`2` 删除 |
| `create_by_id` / `create_by` | 审计字段 |
| `create_time` / `update_time` | `timestamptz`，保存 UTC instant |
| `update_by` | 审计字段 |
| `remark` | 备注 |

### 1.2 UTC 语义

- `create_time`、`update_time` 使用 PostgreSQL `timestamptz`，语义是 UTC 存储。
- Java Entity 继续使用项目现状 `LocalDateTime` + 统一 UTC 约束；手写时间赋值时使用 `TimeUtils.utcNow()`，不直接使用 `LocalDateTime.now()`。
- 基础资料表不新增业务生效时区字段；如后续出现生效窗口，另开 contract。

### 1.3 唯一约束基线

- 所有业务编码唯一约束都按 `tenant_id + business_code + active(del_flag=0)` 定义。
- 不把 `sample_book_no`、`vendor_item_no`、`supplier_code`、`legacy_*` 作为主唯一键。
- 多态绑定表不做数据库级跨表外键，改由 Service 做存在性与引用检查，避免对后续模块造成强耦合。

## 2. 保留改造表与新增表清单

### 2.1 保留改造表

| 表名 | 处理结论 | 原因 |
| --- | --- | --- |
| `pc_category` | 保留并补齐业务字段 | 已有分类 CRUD 和现有 Controller 路由可复用 |
| `pc_material` | 保留并扩展为所有物料主档 | `material_code` 主业务编号必须落在主表 |
| `pc_component` | 保留并改为组件包头表 | 现有表可保留，补 `pc_component_item` 表达多物料明细 |
| `pc_media_asset` | 保留并补 OSS / legacy 字段 | 已有资产 CRUD，可直接扩展 |
| `pc_media_binding` | 保留并强化 target / publish 语义 | 已有绑定 CRUD，可直接扩展 |

### 2.2 新增表

| 表名 | 用途 |
| --- | --- |
| `pc_unit` | 单位管理，承接精度、换算、单位类型 |
| `pc_fabric_series` | 面料系列及系列级规则 |
| `pc_fabric_profile` | 面料资料扩展表，承接颜色 / 纹理 / 厚度 / 门幅等 |
| `pc_material_attribute` | 通用物料属性值表，避免电机、遥控器、胶条等专表化 |
| `pc_base_attribute` | 配置字典与属性定义表 |
| `pc_component_item` | 组件包明细表 |

## 3. 表级 contract

### 3.1 `pc_category`

- 主键：`category_id`
- 关键字段：
  - `parent_id`
  - `category_code`
  - `category_name_cn`
  - `category_name_en`
  - `business_type`
  - `category_level`
  - `category_path`
  - `sort_order`
  - `status`
- 唯一约束：
  - `uk_pc_category_code_active (tenant_id, category_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_category_parent_sort (tenant_id, parent_id, sort_order)`
  - `idx_pc_category_business_status (tenant_id, business_type, status)`
- UTC 字段语义：
  - 仅审计时间，记录分类最后编辑时间

### 3.2 `pc_unit`

- 主键：`unit_id`
- 关键字段：
  - `unit_code`
  - `unit_name_cn`
  - `unit_name_en`
  - `unit_type`
  - `precision_scale`
  - `rounding_mode`
  - `base_unit_code`
  - `conversion_rate`
  - `status`
- 唯一约束：
  - `uk_pc_unit_code_active (tenant_id, unit_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_unit_type_status (tenant_id, unit_type, status)`
- UTC 字段语义：
  - 仅审计时间

### 3.3 `pc_base_attribute`

- 主键：`attribute_id`
- 关键字段：
  - `attribute_group`
  - `attribute_code`
  - `attribute_name_cn`
  - `attribute_name_en`
  - `value_type`
  - `unit_code`
  - `material_types`
  - `extra_json`
  - `sort_order`
  - `status`
- 唯一约束：
  - `uk_pc_base_attribute_code_active (tenant_id, attribute_group, attribute_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_base_attribute_group_status (tenant_id, attribute_group, status)`
- UTC 字段语义：
  - 仅审计时间

### 3.4 `pc_material`

- 主键：`material_id`
- 关键字段：
  - `material_code`
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
  - `status`
- 唯一约束：
  - `uk_pc_material_code_active (tenant_id, material_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_material_type_status (tenant_id, material_type, status)`
  - `idx_pc_material_supplier_code (tenant_id, supplier_code)`
  - `idx_pc_material_vendor_item_no (tenant_id, vendor_item_no)`
  - `idx_pc_material_sample_book_no (tenant_id, sample_book_no)`
- UTC 字段语义：
  - 仅审计时间
- 兼容说明：
  - 现有 `attribute_json` 不再作为属性事实来源；Wave 1 如保留该列，仅作为低频兼容缓存或调试快照。

### 3.5 `pc_fabric_series`

- 主键：`series_id`
- 关键字段：
  - `series_code`
  - `series_name_cn`
  - `series_name_en`
  - `material_type`
  - `default_thickness_unit`
  - `default_thickness_value`
  - `thickness_rule_enabled`
  - `max_thickness_diff`
  - `max_combined_thickness`
  - `width_rule_enabled`
  - `available_widths`
  - `min_width_value`
  - `max_width_value`
  - `width_unit`
  - `extra_rule_json`
  - `status`
- 唯一约束：
  - `uk_pc_fabric_series_code_active (tenant_id, series_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_fabric_series_type_status (tenant_id, material_type, status)`
- UTC 字段语义：
  - 仅审计时间

### 3.6 `pc_fabric_profile`

- 主键：`profile_id`
- 关键字段：
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
  - `factory_model`
  - `purchase_unit_code`
  - `inventory_unit_code`
  - `sales_unit_code`
  - `legacy_attribute_text`
  - `legacy_source`
  - `legacy_id`
  - `status`
- 唯一约束：
  - `uk_pc_fabric_profile_material_active (tenant_id, material_id) where del_flag='0'`
  - `uk_pc_fabric_profile_material_code_active (tenant_id, material_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_fabric_profile_series_status (tenant_id, series_id, status)`
  - `idx_pc_fabric_profile_sample_book_no (tenant_id, sample_book_no)`
  - `idx_pc_fabric_profile_vendor_item_no (tenant_id, vendor_item_no)`
  - `idx_pc_fabric_profile_supplier_code (tenant_id, supplier_code)`
- UTC 字段语义：
  - 仅审计时间
- 关键约束：
  - `pc_fabric_profile` 不是独立物料主档，必须 1:1 关联 `pc_material(material_id)`。
  - 不单独引入第二套 `fabric_code` 主编号，使用 `material_code` 作为唯一主业务编号。

### 3.7 `pc_material_attribute`

- 主键：`material_attribute_id`
- 关键字段：
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
- 唯一约束：
  - `uk_pc_material_attr_active (tenant_id, material_id, attribute_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_material_attr_material (tenant_id, material_id, status)`
  - `idx_pc_material_attr_attribute (tenant_id, attribute_id, status)`
- UTC 字段语义：
  - 仅审计时间

### 3.8 `pc_component`

- 主键：`component_id`
- 关键字段：
  - `component_code`
  - `component_name_cn`
  - `component_name_en`
  - `component_type`
  - `business_type`
  - `default_qty`
  - `qty_mode`
  - `unit_code`
  - `scope_json`
  - `item_count`
  - `status`
- 唯一约束：
  - `uk_pc_component_code_active (tenant_id, component_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_component_type_status (tenant_id, component_type, status)`
- UTC 字段语义：
  - 仅审计时间
- 兼容说明：
  - 现有 `material_id`、`material_code`、`material_name_*` 如继续保留，只能作为默认展示物料快照，不能再作为组件唯一组成依据。

### 3.9 `pc_component_item`

- 主键：`component_item_id`
- 关键字段：
  - `component_id`
  - `component_code`
  - `material_id`
  - `material_code`
  - `material_name_cn`
  - `item_role`
  - `qty_formula`
  - `default_qty`
  - `unit_code`
  - `sort_order`
  - `required_flag`
  - `status`
- 唯一约束：
  - `uk_pc_component_item_sort_active (tenant_id, component_id, sort_order) where del_flag='0'`
- 索引建议：
  - `idx_pc_component_item_component (tenant_id, component_id, status)`
  - `idx_pc_component_item_material (tenant_id, material_id, status)`
- UTC 字段语义：
  - 仅审计时间

### 3.10 `pc_media_asset`

- 主键：`asset_id`
- 关键字段：
  - `asset_code`
  - `asset_name_cn`
  - `asset_name_en`
  - `asset_type`
  - `usage_type`
  - `language_code`
  - `visibility`
  - `oss_id`
  - `version_no`
  - `legacy_source`
  - `legacy_id`
  - `legacy_path`
  - `legacy_url`
  - `status`
- 唯一约束：
  - `uk_pc_media_asset_code_active (tenant_id, asset_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_media_asset_oss (tenant_id, oss_id)`
  - `idx_pc_media_asset_type_lang (tenant_id, asset_type, language_code, status)`
- UTC 字段语义：
  - 仅审计时间

### 3.11 `pc_media_binding`

- 主键：`binding_id`
- 关键字段：
  - `asset_id`
  - `asset_code`
  - `target_type`
  - `target_id`
  - `target_code`
  - `usage_type`
  - `visibility`
  - `language_code`
  - `required_for_publish`
  - `sort_order`
  - `status`
- 唯一约束：
  - `uk_pc_media_binding_target_active (tenant_id, asset_id, target_type, target_id, usage_type, language_code) where del_flag='0'`
- 索引建议：
  - `idx_pc_media_binding_target (tenant_id, target_type, target_id, status)`
  - `idx_pc_media_binding_asset (tenant_id, asset_id, status)`
- UTC 字段语义：
  - 仅审计时间

## 4. 编号与边界规则

### 4.1 `material_code`

- `material_code` 是基础资料域内唯一主业务编号。
- 所有面料资料、组件明细、后续配置选项、BOM、发布包、订单快照引用物料时，优先引用 `material_id`，同时保留 `material_code` 快照。
- `pc_fabric_profile` 不再新增独立 `fabric_code`；面料主编号和物料主编号统一。

### 4.2 `sample_book_no`

- 定义：样册编号，外部参考字段。
- 用途：搜索、展示、seed 映射、导入匹配辅助。
- 约束：不唯一，不参与对象主身份，不作为 upsert 主键。

### 4.3 `vendor_item_no`

- 定义：供应商料号 / 工厂料号外部编码。
- 用途：供应链追溯、导入映射、搜索。
- 约束：不唯一，不作为主身份；允许同一供应商在不同面料系列或颜色变体下重复。

### 4.4 `supplier_code`

- 定义：供应商主数据外部编号快照。
- 用途：过滤、报表、legacy 映射。
- 约束：不作为当前基础资料表的唯一主键；若未来引入正式供应商主档，再补外键 contract。

### 4.5 `legacy_*`

- 允许字段：
  - `legacy_source`
  - `legacy_id`
  - `legacy_path`
  - `legacy_url`
  - `legacy_attribute_text`
- 用途：仅用于 OFBiz 或其他旧系统追溯，不作为当前系统运行态主身份。
- 约束：
  - 不参与日常 CRUD 唯一校验。
  - 不作为编辑时默认 upsert key。
  - 只有 seed / 导入工具可把它作为匹配辅助字段。

## 5. 重点字段 contract

### 5.1 面料系列字段

`pc_fabric_series` 必须覆盖：

- `series_code`
- `series_name_cn`
- `series_name_en`
- `default_thickness_unit`
- `default_thickness_value`
- `thickness_rule_enabled`
- `max_thickness_diff`
- `max_combined_thickness`
- `width_rule_enabled`
- `available_widths`
- `min_width_value`
- `max_width_value`
- `width_unit`
- `extra_rule_json`

### 5.2 面料资料字段

`pc_fabric_profile` 必须覆盖：

- `material_id`
- `material_code`
- `series_id`
- `series_code`
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
- `factory_model`
- `purchase_unit_code`
- `inventory_unit_code`
- `sales_unit_code`
- `legacy_attribute_text`

### 5.3 通用物料属性字段

`pc_material_attribute` 必须覆盖：

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

说明：

- 电机、遥控器、胶条、安装码、配重条等都走 `pc_material + pc_material_attribute`。
- 不为控制系统、配重条、电机等再新增 profile 专表。

### 5.4 组件明细字段

`pc_component_item` 必须覆盖：

- `component_id`
- `component_code`
- `material_id`
- `material_code`
- `material_name_cn`
- `item_role`
- `qty_formula`
- `default_qty`
- `unit_code`
- `sort_order`
- `required_flag`

### 5.5 OSS 复用字段

- `pc_media_asset.oss_id` 指向现有 `sys_oss` 主键。
- 文件本体只存在 `sys_oss`，基础资料模块不复制文件元数据表。
- 文件拿不到本体时，允许仅写：
  - `legacy_source`
  - `legacy_id`
  - `legacy_path`
  - `legacy_url`

## 6. Java 命名与分层 contract

### 6.1 命名建议

| 层 | 建议命名 |
| --- | --- |
| Entity | `ProductCategory`、`ProductUnit`、`ProductBaseAttribute`、`ProductMaterial`、`ProductMaterialAttribute`、`FabricSeries`、`FabricProfile`、`ProductComponent`、`ProductComponentItem`、`ProductMediaAsset`、`ProductMediaBinding` |
| BO | `ProductCategoryBo`、`ProductUnitBo`、`ProductBaseAttributeBo`、`ProductMaterialBo`、`ProductMaterialAttributeBo`、`FabricSeriesBo`、`FabricProfileBo`、`ProductComponentBo`、`ProductComponentItemBo`、`ProductMediaAssetBo`、`ProductMediaBindingBo` |
| VO | `ProductCategoryVo`、`ProductUnitVo`、`ProductBaseAttributeVo`、`ProductMaterialVo`、`ProductMaterialDetailVo`、`FabricSeriesVo`、`FabricProfileVo`、`FabricProfileDetailVo`、`ProductComponentVo`、`ProductComponentDetailVo`、`ProductMediaAssetVo`、`ProductMediaBindingVo`、`ReferenceCheckResultVo` |
| Mapper | `ProductCategoryMapper`、`ProductUnitMapper`、`ProductBaseAttributeMapper`、`ProductMaterialMapper`、`ProductMaterialAttributeMapper`、`FabricSeriesMapper`、`FabricProfileMapper`、`ProductComponentMapper`、`ProductComponentItemMapper`、`ProductMediaAssetMapper`、`ProductMediaBindingMapper` |

### 6.2 Service / Controller 建议

- Controller：
  - `ProductBaseInfoController`
  - `ProductFabricController`
- Service：
  - `ProductBaseInfoService`
  - `ProductFabricService`
  - `ProductReferenceCheckService`

### 6.3 分层取舍

- 当前 `bocoo-modules-product` 已采用 `service/*.java` 具体服务类风格，而不是 `service + service/impl` 双层接口实现。
- Wave 1 为保持 minimal diff，建议继续沿用现有模块风格，不为了基础资料重构单独引入 `impl` 目录。
- Mapper 继续使用 `BaseMapperPlus<Entity, Vo>`；复杂列表查询或明细聚合再补 XML，不在 Controller 拼装。

## 7. API contract

### 7.1 通用分页与返回壳

- API 前缀：`/product-capability`
- 分页参数：`pageNum`、`pageSize`、`orderByColumn`、`isAsc`
- 返回壳：
  - 列表：`TableDataInfo<XxxVo>`
  - 详情：`R<XxxVo>` / `R<XxxDetailVo>`
  - 新增 / 编辑 / 删除 / 禁用：`R<Void>`
  - 下拉：`R<List<XxxVo>>`
  - 引用检查：`R<ReferenceCheckResultVo>`

### 7.2 分类

- `GET /categories/list`
  - 查询字段：`categoryCode`、`categoryNameCn`、`categoryNameEn`、`businessType`、`parentId`、`status`
- `GET /categories/options`
- `GET /categories/tree`
- `GET /categories/{id}`
- `POST /categories`
- `PUT /categories`
- `PUT /categories/change-status/{id}/{status}`
- `DELETE /categories/{ids}`
- `GET /categories/{id}/references`

### 7.3 单位

- `GET /units/list`
  - 查询字段：`unitCode`、`unitNameCn`、`unitType`、`status`
- `GET /units/options`
- `GET /units/{id}`
- `POST /units`
- `PUT /units`
- `PUT /units/change-status/{id}/{status}`
- `DELETE /units/{ids}`
- `GET /units/{id}/references`

### 7.4 配置字典 / 属性定义

- `GET /base-attributes/list`
  - 查询字段：`attributeGroup`、`attributeCode`、`attributeNameCn`、`materialType`、`status`
- `GET /base-attributes/options`
- `GET /base-attributes/{id}`
- `POST /base-attributes`
- `PUT /base-attributes`
- `PUT /base-attributes/change-status/{id}/{status}`
- `DELETE /base-attributes/{ids}`
- `GET /base-attributes/{id}/references`

### 7.5 物料

- `GET /materials/list`
  - 查询字段：`materialCode`、`materialNameCn`、`materialType`、`businessType`、`supplierCode`、`sampleBookNo`、`vendorItemNo`、`status`
- `GET /materials/options`
- `GET /materials/{id}`
  - 返回：`ProductMaterialDetailVo`
  - 必带：主表字段 + `attributeList`
- `POST /materials`
  - 入参：`ProductMaterialBo`
  - 允许内嵌：`attributeList`
- `PUT /materials`
  - 入参：`ProductMaterialBo`
  - 允许内嵌：`attributeList`
- `PUT /materials/change-status/{id}/{status}`
- `DELETE /materials/{ids}`
- `GET /materials/{id}/references`

### 7.6 面料系列

- `GET /fabric-series/list`
  - 查询字段：`seriesCode`、`seriesNameCn`、`materialType`、`status`
- `GET /fabric-series/options`
- `GET /fabric-series/{id}`
- `POST /fabric-series`
- `PUT /fabric-series`
- `PUT /fabric-series/change-status/{id}/{status}`
- `DELETE /fabric-series/{ids}`
- `GET /fabric-series/{id}/references`

### 7.7 面料资料

- `GET /fabric-profiles/list`
  - 查询字段：`materialCode`、`seriesId`、`seriesCode`、`colorCode`、`colorName`、`sampleBookNo`、`vendorItemNo`、`supplierCode`、`status`
- `GET /fabric-profiles/options`
- `GET /fabric-profiles/{id}`
  - 返回：`FabricProfileDetailVo`
  - 必带：profile 字段 + 关联 material 摘要
- `POST /fabric-profiles`
- `PUT /fabric-profiles`
- `PUT /fabric-profiles/change-status/{id}/{status}`
- `DELETE /fabric-profiles/{ids}`
- `GET /fabric-profiles/{id}/references`

### 7.8 组件包

- `GET /components/list`
  - 查询字段：`componentCode`、`componentNameCn`、`componentType`、`businessType`、`status`
- `GET /components/options`
- `GET /components/{id}`
  - 返回：`ProductComponentDetailVo`
  - 必带：主表字段 + `itemList`
- `POST /components`
  - 入参：`ProductComponentBo`
  - 允许内嵌：`itemList`
- `PUT /components`
  - 入参：`ProductComponentBo`
  - 允许内嵌：`itemList`
- `PUT /components/change-status/{id}/{status}`
- `DELETE /components/{ids}`
- `GET /components/{id}/references`

### 7.9 资料资产

- `GET /media-assets/list`
  - 查询字段：`assetCode`、`assetNameCn`、`assetType`、`usageType`、`languageCode`、`status`
- `GET /media-assets/options`
- `GET /media-assets/{id}`
- `POST /media-assets`
- `PUT /media-assets`
- `PUT /media-assets/change-status/{id}/{status}`
- `DELETE /media-assets/{ids}`
- `GET /media-assets/{id}/references`

### 7.10 资料绑定

- `GET /media-bindings/list`
  - 查询字段：`assetCode`、`targetType`、`targetCode`、`usageType`、`languageCode`、`requiredForPublish`、`status`
- `GET /media-bindings/options`
- `GET /media-bindings/{id}`
- `POST /media-bindings`
- `PUT /media-bindings`
- `POST /media-bindings/batch`
- `PUT /media-bindings/change-status/{id}/{status}`
- `DELETE /media-bindings/{ids}`
- `GET /media-bindings/{id}/references`

### 7.11 导出接口

为对齐项目 CRUD 标准，以上资源建议统一补充：

- `POST /<resource>/export`

对应权限码 `*:export`，实现使用 `ExcelUtil.exportExcel(...)`。

## 8. 引用检查与冗余同步 contract

### 8.1 引用检查

- 统一返回 `ReferenceCheckResultVo`：
  - `allowed`
  - `referenceCount`
  - `blockerReasonKey`
  - `referenceSummaries`
- 删除和禁用前都先执行引用检查。
- 第一阶段至少检查：
  - `pc_unit` 被 `pc_material`、`pc_fabric_profile`、`pc_component_item`、`pc_base_attribute` 引用
  - `pc_base_attribute` 被 `pc_material_attribute` 引用
  - `pc_material` 被 `pc_fabric_profile`、`pc_component_item`、`pc_component` 兼容字段引用
  - `pc_fabric_series` 被 `pc_fabric_profile` 引用
  - `pc_media_asset` 被 `pc_media_binding` 引用
  - `pc_category` 被后续模型 / 系列 / 物料分类字段引用
- 扩展边界：
  - Wave 1 需为配置、发布、快照等下游模块预留 provider 式引用扩展点，不把检查逻辑硬编码在 Controller。

### 8.2 冗余字段同步

源数据与冗余快照边界：

- `pc_material` 是 `material_code`、名称、供应商等主事实来源。
- `pc_fabric_profile` 存 `series_code`、`series_name_cn`、`supplier_name` 等展示快照。
- `pc_component_item` 存 `material_code`、`material_name_cn` 快照。
- `pc_media_binding` 存 `asset_code`、`target_code` 快照。

同步规则：

- 保存 `pc_material` 后，同事务刷新依赖它的 `pc_fabric_profile`、`pc_component_item`、`pc_component` 兼容快照字段。
- 保存 `pc_fabric_series` 后，同事务刷新 `pc_fabric_profile` 内系列快照字段。
- 保存 `pc_component` 后，同事务刷新 `pc_component_item.component_code`。
- 保存 `pc_media_asset` 后，同事务刷新 `pc_media_binding.asset_code`。
- 冗余字段只服务查询性能和快照展示，不替代主事实表。

## 9. 权限、租户、数据权限 contract

### 9.1 权限码

- 分类 / 单位 / 属性定义 / 物料 / 组件：
  - `product:base:list`
  - `product:base:query`
  - `product:base:add`
  - `product:base:edit`
  - `product:base:remove`
  - `product:base:export`
  - `product:base:reference`
- 面料系列 / 面料资料：
  - `product:fabric:list`
  - `product:fabric:query`
  - `product:fabric:add`
  - `product:fabric:edit`
  - `product:fabric:remove`
  - `product:fabric:export`
  - `product:fabric:reference`
- 资料资产 / 资料绑定：
  - `product:asset:list`
  - `product:asset:query`
  - `product:asset:upload`
  - `product:asset:bind`
  - `product:asset:remove`
  - `product:asset:export`
  - `product:asset:reference`

### 9.2 多租户

- 所有基础资料表必须包含 `tenant_id`。
- 所有唯一键和业务查询默认带 `tenant_id`。
- 正常 CRUD 不允许使用 `TenantContextHolder.callWithIgnore(...)` 绕过租户。
- 不允许跨租户引用基础资料。

### 9.3 数据权限

- 当前 product 模块未显式使用 `@DataPermission`，Wave 1 可以先依赖租户隔离完成最小闭环。
- 如后续需要平台租户查看商家租户基础资料，应在 Mapper 层补数据权限或显式租户切换，不在 Controller 手工拼 where 条件。

## 10. 事务边界

- `saveProductCategory` / `saveProductUnit` / `saveProductBaseAttribute`：
  - 单表事务
- `saveProductMaterial`：
  - `pc_material` + `pc_material_attribute` 明细整体事务
  - 同事务完成冗余快照刷新
- `saveFabricSeries`：
  - `pc_fabric_series` + `pc_fabric_profile` 系列快照刷新事务
- `saveFabricProfile`：
  - `pc_material` 主档 + `pc_fabric_profile` 扩展表事务
  - 新增面料资料时，必须先确保对应 `pc_material.material_type='FABRIC'`
- `saveProductComponent`：
  - `pc_component` + `pc_component_item` 头明细事务
- `saveProductMediaBinding` / `batchSaveProductMediaBinding`：
  - 绑定表写入事务
  - 不在该事务内处理 OSS 文件上传，仅校验 `oss_id` 已存在
- `changeStatus` / `delete`：
  - 先引用检查，再状态变更或逻辑删除，同事务完成

## 11. Wave 1 直接验证点

- DDL 必验：
  - 基础资料表已补 `tenant_id`
  - 所有业务唯一索引都按 `(tenant_id, code)` 落地
  - `create_time` / `update_time` 为 `timestamptz`
- 后端必验：
  - `/product-capability` 基础资料新接口继续返回 `R<T>` / `TableDataInfo<T>`
  - 保存物料、面料、组件的事务能跨至少一个子表
  - 删除 / 禁用前引用检查结果明确且可读
- 环境级待验证：
  - 真实租户登录态下产品模块 CRUD 是否被租户插件正确过滤
  - 现有 `sys_oss` 数据模型是否已满足 `oss_id` 直接复用
