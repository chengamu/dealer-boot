# Frontend Contract: 产品配置中心基础资料模块重构

创建日期：2026-06-11  
任务：AMU Wave 0 / `BASE-C2`  
边界：只定义前端页面、API 类型、i18n 和权限 contract；不实现业务代码。

## 1. Contract 范围与现状

### 1.1 本次必须覆盖的 6 个基础资料入口

1. 面料设置 `fabric`
2. 物料设置 `material`
3. 组件包 `component`
4. 配置字典 `attribute`
5. 附件资料 `media`
6. 单位管理 `unit`

### 1.2 现有结构结论

- 现有基础资料页为 [ProductBaseInfoPage.vue](/Users/chengmuxuan/Desktop/cmx/base-boot/admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue)。
- 现有附件资料页为 [ProductAssetPage.vue](/Users/chengmuxuan/Desktop/cmx/base-boot/admin-ui/src/pages/product-center/assets/ProductAssetPage.vue)。
- 现有通用 CRUD 容器为 [ProductEntityGridPage.vue](/Users/chengmuxuan/Desktop/cmx/base-boot/admin-ui/src/pages/product-center/components/ProductEntityGridPage.vue)，原生支持：
  - 搜索、分页、通用抽屉表单
  - 引用检查抽屉
  - 行级动作和工具栏动作
- 现有 API 文件：
  - [base.ts](/Users/chengmuxuan/Desktop/cmx/base-boot/admin-ui/src/api/product-capability/base.ts)
  - [asset.ts](/Users/chengmuxuan/Desktop/cmx/base-boot/admin-ui/src/api/product-capability/asset.ts)
  - [types.ts](/Users/chengmuxuan/Desktop/cmx/base-boot/admin-ui/src/api/product-capability/types.ts)
- 人工主 i18n 文件真实路径是 [en_US.json](/Users/chengmuxuan/Desktop/cmx/base-boot/i18n/locales/en_US.json)，不是 `admin-ui/src/i18n/locales/en_US.json`。后续实现只改 `i18n/locales/en_US.json`，`admin-ui/public/i18n/*.json` 继续由同步脚本生成。

### 1.3 非本任务实现范围

- 不改后端、SQL、菜单、路由注册、权限表、字典数据。
- 不落任何 Vue、TS、后端实现代码。
- 不扩展上传系统，附件仍复用现有 OSS / `sys_oss` 能力。

## 2. 页面与文件 Contract

### 2.1 页面入口建议

| 模块 | 用户入口建议 | 页面文件 contract | 交互形态 | 备注 |
| --- | --- | --- | --- | --- |
| 面料设置 | `/product-master/fabrics` | `admin-ui/src/pages/product-center/fabric/ProductFabricPage.vue` | 独立页面，页签 + 明细抽屉 | 同页维护 `Fabric Series` 与 `Fabric Profile` |
| 物料设置 | `/product-master/materials` | `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue` | 继续走通用 grid，增加属性抽屉 | 现有 material tab 扩展 |
| 组件包 | `/product-master/components` | `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue` 或 `admin-ui/src/pages/product-center/component/ProductComponentPage.vue` | 主表 grid + 明细子表抽屉 | 推荐拆专页，避免 base tab 过重 |
| 配置字典 | `/product-master/base-attributes` | `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue` 或 `admin-ui/src/pages/product-center/base/ProductBaseAttributePage.vue` | 继续走通用 grid | 属性定义主档，不编辑物料值 |
| 附件资料 | `/product-master/media-assets`、`/product-master/media-bindings` | `admin-ui/src/pages/product-center/assets/ProductAssetPage.vue` | 资产 grid + 绑定专用抽屉 | 现有页面保留增强 |
| 单位管理 | `/product-master/units` | `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue` 或 `admin-ui/src/pages/product-center/base/ProductUnitPage.vue` | 继续走通用 grid | 单位主档比普通字典更强 |

### 2.2 页面分组建议

建议实现阶段分成 3 组页面，不强制本次落代码：

| 页面 | 承载模块 | 原因 |
| --- | --- | --- |
| `ProductBaseInfoPage.vue` | 物料设置、配置字典、单位管理 | 都是“主档 + 简单抽屉表单” |
| `ProductFabricPage.vue` | 面料系列、面料资料 | 两级实体强关联，需要系列过滤、联动明细 |
| `ProductAssetPage.vue` | 附件资料、附件绑定 | 已有独立页面和权限分组 |
| `ProductComponentPage.vue` | 组件包、组件明细 | 组件主表可 grid，但明细维护明显超出通用抽屉能力 |

补充：

- 产品分类 `category` 继续保留现有入口，不计入本次 6 个必需入口，但媒体绑定和引用检查需要支持 `category` 目标类型。
- 若实现阶段坚持保留单页多 tab，也必须保证路由可直达 6 个入口，不要求用户先进入总 tab 页再二次切换。

## 3. API 文件与类型 Contract

### 3.1 API 文件分布建议

| 文件 | 负责对象 | 说明 |
| --- | --- | --- |
| `admin-ui/src/api/product-capability/base.ts` | `ProductMaterial`、`ProductUnit`、`ProductBaseAttribute`、保留 `ProductCategory` | 继续承载简单基础主档 |
| `admin-ui/src/api/product-capability/fabric.ts` | `FabricSeries`、`FabricProfile` | 新增文件，避免把系列/资料混进 base.ts |
| `admin-ui/src/api/product-capability/component.ts` | `ProductComponent`、`ProductComponentItem` | 推荐新增文件，单独承载组件明细接口 |
| `admin-ui/src/api/product-capability/asset.ts` | `ProductMediaAsset`、`ProductMediaBinding` | 现有文件继续增强 |
| `admin-ui/src/api/product-capability/types.ts` | 通用查询、引用检查、基础 VO 类型别名 | 保留公共类型 |
| `admin-ui/src/api/product-capability/base-info-types.ts` | 六模块具体 VO / Query / Form 类型 | 推荐新增，避免 `ProductRecord` 长期失控 |

### 3.2 API 对象命名建议

| 模块 | API 对象名 |
| --- | --- |
| 单位管理 | `productUnitApi` |
| 配置字典 | `productBaseAttributeApi` |
| 面料系列 | `fabricSeriesApi` |
| 面料资料 | `fabricProfileApi` |
| 物料属性值 | `productMaterialAttributeApi` |
| 组件包 | `productComponentApi` |
| 组件明细 | `productComponentItemApi` |
| 附件资料 | `productMediaAssetApi` |
| 附件绑定 | `productMediaBindingApi` |

### 3.3 API 能力 Contract

#### 通用 grid 型对象

适用于：`ProductUnit`、`ProductBaseAttribute`、`ProductMaterial`、`FabricSeries`、`ProductMediaAsset`

标准能力：

- `list(query)`
- `options(query)`，仅用于下拉
- `get(id)`
- `add(data)`
- `update(data)`
- `remove(ids)`
- `changeStatus(id, status)`
- `references(id)`

#### 需要扩展明细能力的对象

适用于：`FabricProfile`、`ProductMaterialAttribute`、`ProductComponentItem`、`ProductMediaBinding`

除标准能力外，建议补充：

- `listByParent(parentId | parentCode)`
- `batchSave(data[])`
- `sort(data[])`
- `copyFrom(sourceId)`，仅组件明细可选

#### 绑定类特例

`ProductMediaBinding` 额外建议：

- `batchBind(data)`
- `listTargets(query)`，用于 target 选择器
- `validateRequired(query)`，仅发布校验联动时使用，Wave 1 再决定是否落接口

### 3.4 TypeScript 类型建议

建议在实现阶段补具体类型，避免六个模块继续使用裸 `ProductRecord`：

| 类型 | 用途 |
| --- | --- |
| `ProductUnitVO` / `ProductUnitForm` / `ProductUnitQuery` | 单位管理 |
| `ProductBaseAttributeVO` / `Form` / `Query` | 配置字典 |
| `FabricSeriesVO` / `Form` / `Query` | 面料系列 |
| `FabricProfileVO` / `Form` / `Query` | 面料资料 |
| `ProductMaterialVO` / `Form` / `Query` | 物料主档 |
| `ProductMaterialAttributeVO` / `Form` / `Query` | 物料属性值 |
| `ProductComponentVO` / `Form` / `Query` | 组件包 |
| `ProductComponentItemVO` / `Form` / `Query` | 组件明细 |
| `ProductMediaAssetVO` / `Form` / `Query` | 附件资料 |
| `ProductMediaBindingVO` / `Form` / `Query` | 附件绑定 |

## 4. ProductEntityGridPage 复用边界

### 4.1 可继续直接复用 `ProductEntityGridPage`

| 模块 | 结论 | 原因 |
| --- | --- | --- |
| 单位管理 | 可以 | 单表主档，字段稳定，表单不含子表 |
| 配置字典 | 可以 | 属性定义主档，无复杂明细编辑 |
| 面料系列 | 可以 | 单表主档，限制字段虽多，但仍可抽屉表单 |
| 附件资料 | 可以 | 主体是资产元数据，OSS 选择可做单字段或扩展 slot |

### 4.2 需要“grid + 扩展抽屉/子表”

| 模块 | 结论 | 原因 |
| --- | --- | --- |
| 物料设置 | 需要扩展抽屉 | 主档仍可 grid，但 `ProductMaterialAttribute` 是一对多 |
| 附件绑定 | 需要扩展抽屉 | `targetType` + `targetCode` 不能只靠纯文本输入，至少要目标选择器 |
| 面料资料 | 需要扩展抽屉 | 需要受 `seriesId/seriesCode` 过滤，部分字段联动系列默认值 |

### 4.3 需要专页或复杂抽屉

| 模块 | 结论 | 原因 |
| --- | --- | --- |
| 组件包 | 推荐专页 | `ProductComponent` 主档 + `ProductComponentItem` 子表是核心编辑面，不适合单通用抽屉 |
| 组件明细 | 子表/内嵌表格 | 至少支持多行新增、排序、物料选择、默认数量 |

## 5. 页面字段与 VO 字段映射

说明：

- 前端字段统一用 camelCase。
- 后端 VO 字段建议与前端同名 camelCase。
- 表内字段来源说明里保留需求中的 snake_case 业务名，避免 contract 歧义。

### 5.1 单位管理 `ProductUnit`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `unitId` | `unitId` | `id` | 主键 |
| `unitCode` | `unitCode` | `unit_code` | 主业务编码 |
| `unitNameCn` | `unitNameCn` | `unit_name_cn` | 中文名 |
| `unitNameEn` | `unitNameEn` | `unit_name_en` | 英文名 |
| `unitType` | `unitType` | `unit_type` | 长度/面积/重量/数量等 |
| `precisionScale` | `precisionScale` | `precision_scale` | 小数精度 |
| `roundingMode` | `roundingMode` | `rounding_mode` | 舍入方式 |
| `baseUnitCode` | `baseUnitCode` | `base_unit_code` | 基准单位 |
| `conversionRate` | `conversionRate` | `conversion_rate` | 换算系数 |
| `status` | `status` | `status` | 状态 |
| `sortOrder` | `sortOrder` | `sort_order` | 排序 |
| `remark` | `remark` | `remark` | 备注 |

### 5.2 面料系列 `FabricSeries`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `seriesId` | `seriesId` | `id` | 主键 |
| `seriesCode` | `seriesCode` | `series_code` | 系列编码 |
| `seriesNameCn` | `seriesNameCn` | `series_name_cn` | 中文名 |
| `seriesNameEn` | `seriesNameEn` | `series_name_en` | 英文名 |
| `materialType` | `materialType` | `material_type` | 面料业务类型 |
| `defaultThicknessUnit` | `defaultThicknessUnit` | `default_thickness_unit` | 默认厚度单位 |
| `thicknessRuleEnabled` | `thicknessRuleEnabled` | `thickness_rule_enabled` | 厚度规则开关 |
| `maxThicknessDiff` | `maxThicknessDiff` | `max_thickness_diff` | 最大厚度差 |
| `maxCombinedThickness` | `maxCombinedThickness` | `max_combined_thickness` | 最大组合厚度 |
| `widthRuleEnabled` | `widthRuleEnabled` | `width_rule_enabled` | 门幅规则开关 |
| `availableWidths` | `availableWidths` | `available_widths` | 可用门幅列表 |
| `minWidthValue` | `minWidthValue` | `min_width_value` | 最小门幅 |
| `maxWidthValue` | `maxWidthValue` | `max_width_value` | 最大门幅 |
| `widthUnit` | `widthUnit` | `width_unit` | 门幅单位 |
| `status` | `status` | `status` | 状态 |

### 5.3 面料资料 `FabricProfile`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `fabricId` | `fabricId` | `id` | 主键 |
| `fabricCode` | `fabricCode` | `fabric_code` | 面料编号，前端展示名可提示与 `material_code` 一致 |
| `materialId` | `materialId` | `material_id` | 关联物料主键 |
| `materialCode` | `materialCode` | `material_code` | 主业务编号 |
| `seriesId` | `seriesId` | `series_id` | 关联系列 |
| `seriesCode` | `seriesCode` | `series_code` | 系列编码冗余 |
| `seriesNameCn` | `seriesNameCn` | `series_name_cn` | 系列中文名冗余 |
| `colorCode` | `colorCode` | `color_code` | 颜色编码 |
| `colorName` | `colorName` | `color_name` | 颜色名 |
| `materialComposition` | `materialComposition` | `material_composition` | 材质成分 |
| `textureType` | `textureType` | `texture_type` | 纹理类型 |
| `finishType` | `finishType` | `finish_type` | 涂层/表面处理 |
| `factoryModel` | `factoryModel` | `factory_model` | 工厂型号 |
| `sampleBookNo` | `sampleBookNo` | `sample_book_no` | 样册编号 |
| `vendorItemNo` | `vendorItemNo` | `vendor_item_no` | 供应商料号 |
| `supplierCode` | `supplierCode` | `supplier_code` | 供应商编码 |
| `supplierName` | `supplierName` | `supplier_name` | 供应商名 |
| `widthValue` | `widthValue` | `width_value` | 门幅值 |
| `widthUnit` | `widthUnit` | `width_unit` | 门幅单位 |
| `thicknessValue` | `thicknessValue` | `thickness_value` | 厚度值 |
| `thicknessUnit` | `thicknessUnit` | `thickness_unit` | 厚度单位 |
| `gsmValue` | `gsmValue` | `gsm_value` | 克重 |
| `purchaseUnitCode` | `purchaseUnitCode` | `purchase_unit_code` | 采购单位 |
| `inventoryUnitCode` | `inventoryUnitCode` | `inventory_unit_code` | 库存单位 |
| `salesUnitCode` | `salesUnitCode` | `sales_unit_code` | 销售单位 |
| `legacyAttributeText` | `legacyAttributeText` | `legacy_attribute_text` | 老系统属性原文 |
| `status` | `status` | `status` | 状态 |

### 5.4 物料主档 `ProductMaterial`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `materialId` | `materialId` | `id` | 主键 |
| `materialCode` | `materialCode` | `material_code` | 主业务编号，唯一主键口径 |
| `materialNameCn` | `materialNameCn` | `material_name_cn` | 中文名 |
| `materialNameEn` | `materialNameEn` | `material_name_en` | 英文名 |
| `materialType` | `materialType` | `material_type` | 物料类型 |
| `businessType` | `businessType` | `business_type` | 业务线 |
| `unitCode` | `unitCode` | `unit_code` | 主单位 |
| `supplierCode` | `supplierCode` | `supplier_code` | 供应商编码 |
| `supplierName` | `supplierName` | `supplier_name` | 供应商名称 |
| `factoryModel` | `factoryModel` | `factory_model` | 工厂型号 |
| `sampleBookNo` | `sampleBookNo` | `sample_book_no` | 样册编号 |
| `vendorItemNo` | `vendorItemNo` | `vendor_item_no` | 供应商料号 |
| `primarySpec` | `primarySpec` | `primary_spec` | 主规格冗余 |
| `primaryColor` | `primaryColor` | `primary_color` | 主颜色冗余 |
| `primaryWeight` | `primaryWeight` | `primary_weight` | 主重量冗余 |
| `attributeSummary` | `attributeSummary` | `attribute_summary` | 属性摘要冗余 |
| `legacySource` | `legacySource` | `legacy_source` | 老系统来源 |
| `legacyId` | `legacyId` | `legacy_id` | 老系统编号 |
| `status` | `status` | `status` | 状态 |

### 5.5 物料属性值 `ProductMaterialAttribute`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `attributeValueId` | `attributeValueId` | `id` | 主键 |
| `materialId` | `materialId` | `material_id` | 物料主键 |
| `materialCode` | `materialCode` | `material_code` | 物料编码冗余 |
| `attributeId` | `attributeId` | `attribute_id` | 属性定义主键 |
| `attributeCode` | `attributeCode` | `attribute_code` | 属性编码 |
| `attributeNameCn` | `attributeNameCn` | `attribute_name_cn` | 属性中文名冗余 |
| `valueText` | `valueText` | `value_text` | 文本值 |
| `valueNumber` | `valueNumber` | `value_number` | 数值 |
| `valueBool` | `valueBool` | `value_bool` | 布尔值 |
| `valueUnitCode` | `valueUnitCode` | `value_unit_code` | 数值单位 |
| `sortOrder` | `sortOrder` | `sort_order` | 排序 |
| `status` | `status` | `status` | 状态 |

说明：电机、遥控器、胶条、安装码、配重条等属性全部走 `ProductMaterialAttribute`，不再约定独立 profile 页面。

### 5.6 组件包与组件明细

#### `ProductComponent`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `componentId` | `componentId` | `id` | 主键 |
| `componentCode` | `componentCode` | `component_code` | 组件包编码 |
| `componentNameCn` | `componentNameCn` | `component_name_cn` | 中文名 |
| `componentNameEn` | `componentNameEn` | `component_name_en` | 英文名 |
| `componentType` | `componentType` | `component_type` | 组件类型 |
| `businessType` | `businessType` | `business_type` | 业务线 |
| `defaultQty` | `defaultQty` | `default_qty` | 默认数量 |
| `qtyMode` | `qtyMode` | `qty_mode` | 数量模式 |
| `unitCode` | `unitCode` | `unit_code` | 单位 |
| `status` | `status` | `status` | 状态 |

#### `ProductComponentItem`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `componentItemId` | `componentItemId` | `id` | 主键 |
| `componentId` | `componentId` | `component_id` | 组件主键 |
| `componentCode` | `componentCode` | `component_code` | 组件编码冗余 |
| `materialId` | `materialId` | `material_id` | 物料主键 |
| `materialCode` | `materialCode` | `material_code` | 物料编码 |
| `materialNameCn` | `materialNameCn` | `material_name_cn` | 物料中文名冗余 |
| `qtyFormula` | `qtyFormula` | `qty_formula` | 数量公式 |
| `defaultQty` | `defaultQty` | `default_qty` | 默认数量 |
| `unitCode` | `unitCode` | `unit_code` | 单位 |
| `requiredFlag` | `requiredFlag` | `required_flag` | 是否必选 |
| `sortOrder` | `sortOrder` | `sort_order` | 排序 |
| `status` | `status` | `status` | 状态 |

### 5.7 配置字典 `ProductBaseAttribute`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `attributeId` | `attributeId` | `id` | 主键 |
| `attributeGroup` | `attributeGroup` | `attribute_group` | 属性组 |
| `attributeCode` | `attributeCode` | `attribute_code` | 属性编码 |
| `attributeNameCn` | `attributeNameCn` | `attribute_name_cn` | 中文名 |
| `attributeNameEn` | `attributeNameEn` | `attribute_name_en` | 英文名 |
| `valueType` | `valueType` | `value_type` | 文本/数值/布尔/枚举 |
| `unitCode` | `unitCode` | `unit_code` | 默认单位 |
| `materialTypes` | `materialTypes` | `material_types` | 适用物料类型 |
| `extraJson` | `extraJson` | `extra_json` | 扩展配置 |
| `status` | `status` | `status` | 状态 |

### 5.8 附件资料与绑定

#### `ProductMediaAsset`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `assetId` | `assetId` | `id` | 主键 |
| `ossId` | `ossId` | `oss_id` | OSS 文件 ID |
| `assetCode` | `assetCode` | `asset_code` | 资产编码 |
| `assetNameCn` | `assetNameCn` | `asset_name_cn` | 中文名 |
| `assetNameEn` | `assetNameEn` | `asset_name_en` | 英文名 |
| `assetType` | `assetType` | `asset_type` | 资料类型 |
| `usageType` | `usageType` | `usage_type` | 用途 |
| `languageCode` | `languageCode` | `language_code` | 语言 |
| `visibility` | `visibility` | `visibility` | 可见性 |
| `versionNo` | `versionNo` | `version_no` | 版本号 |
| `legacySource` | `legacySource` | `legacy_source` | 老系统来源 |
| `legacyId` | `legacyId` | `legacy_id` | 老系统 ID |
| `legacyPath` | `legacyPath` | `legacy_path` | 老系统路径 |
| `legacyUrl` | `legacyUrl` | `legacy_url` | 老系统 URL |
| `status` | `status` | `status` | 状态 |

#### `ProductMediaBinding`

| 前端字段 | 后端 VO 字段 | 业务来源字段 | 用途 |
| --- | --- | --- | --- |
| `bindingId` | `bindingId` | `id` | 主键 |
| `assetId` | `assetId` | `asset_id` | 资产主键 |
| `assetCode` | `assetCode` | `asset_code` | 资产编码冗余 |
| `targetType` | `targetType` | `target_type` | 绑定目标类型 |
| `targetId` | `targetId` | `target_id` | 目标主键 |
| `targetCode` | `targetCode` | `target_code` | 目标编码 |
| `usageType` | `usageType` | `usage_type` | 使用场景 |
| `requiredForPublish` | `requiredForPublish` | `required_for_publish` | 是否发布必需 |
| `sortOrder` | `sortOrder` | `sort_order` | 排序 |
| `status` | `status` | `status` | 状态 |

目标类型建议至少支持：

- `CATEGORY`
- `FABRIC_SERIES`
- `FABRIC_PROFILE`
- `MATERIAL`
- `COMPONENT`

## 6. 列表、表单、搜索与交互验收 Contract

### 6.1 模块级最低搜索字段

| 模块 | 搜索字段 |
| --- | --- |
| 单位管理 | `unitCode`、`unitNameCn`、`unitType`、`status` |
| 面料系列 | `seriesCode`、`seriesNameCn`、`materialType`、`status` |
| 面料资料 | `materialCode`、`fabricCode`、`seriesCode`、`colorCode`、`colorName`、`supplierCode`、`status` |
| 物料设置 | `materialCode`、`materialNameCn`、`materialType`、`supplierCode`、`status` |
| 配置字典 | `attributeGroup`、`attributeCode`、`attributeNameCn`、`valueType`、`status` |
| 组件包 | `componentCode`、`componentNameCn`、`componentType`、`status` |
| 附件资料 | `assetCode`、`assetNameCn`、`assetType`、`usageType`、`languageCode`、`status` |
| 附件绑定 | `assetCode`、`targetType`、`targetCode`、`usageType`、`status` |

### 6.2 模块级最低列表字段

| 模块 | 列表字段 |
| --- | --- |
| 单位管理 | `unitCode`、`unitNameCn`、`unitType`、`precisionScale`、`baseUnitCode`、`conversionRate`、`status` |
| 面料系列 | `seriesCode`、`seriesNameCn`、`materialType`、`availableWidths`、`maxThicknessDiff`、`maxCombinedThickness`、`status` |
| 面料资料 | `materialCode`、`seriesCode`、`colorCode`、`colorName`、`materialComposition`、`widthValue`、`thicknessValue`、`gsmValue`、`status` |
| 物料设置 | `materialCode`、`materialNameCn`、`materialType`、`unitCode`、`supplierName`、`factoryModel`、`attributeSummary`、`status` |
| 配置字典 | `attributeGroup`、`attributeCode`、`attributeNameCn`、`valueType`、`unitCode`、`materialTypes`、`status` |
| 组件包 | `componentCode`、`componentNameCn`、`componentType`、`defaultQty`、`qtyMode`、`unitCode`、`status` |
| 附件资料 | `assetCode`、`assetNameCn`、`assetType`、`usageType`、`languageCode`、`ossId`、`versionNo`、`status` |
| 附件绑定 | `assetCode`、`targetType`、`targetCode`、`usageType`、`requiredForPublish`、`sortOrder`、`status` |

### 6.3 模块级最低表单字段

| 模块 | 表单字段 |
| --- | --- |
| 单位管理 | `unitCode`、`unitNameCn`、`unitNameEn`、`unitType`、`precisionScale`、`roundingMode`、`baseUnitCode`、`conversionRate`、`status`、`remark` |
| 面料系列 | `seriesCode`、`seriesNameCn`、`seriesNameEn`、`materialType`、`defaultThicknessUnit`、`thicknessRuleEnabled`、`maxThicknessDiff`、`maxCombinedThickness`、`widthRuleEnabled`、`availableWidths`、`minWidthValue`、`maxWidthValue`、`widthUnit`、`status`、`remark` |
| 面料资料 | `materialCode`、`seriesId/seriesCode`、`colorCode`、`colorName`、`materialComposition`、`textureType`、`finishType`、`factoryModel`、`sampleBookNo`、`vendorItemNo`、`supplierCode`、`supplierName`、`widthValue`、`widthUnit`、`thicknessValue`、`thicknessUnit`、`gsmValue`、`purchaseUnitCode`、`inventoryUnitCode`、`salesUnitCode`、`legacyAttributeText`、`status`、`remark` |
| 物料设置 | `materialCode`、`materialNameCn`、`materialNameEn`、`materialType`、`businessType`、`unitCode`、`supplierCode`、`supplierName`、`factoryModel`、`sampleBookNo`、`vendorItemNo`、`primarySpec`、`primaryColor`、`primaryWeight`、`legacySource`、`legacyId`、`status`、`remark` |
| 配置字典 | `attributeGroup`、`attributeCode`、`attributeNameCn`、`attributeNameEn`、`valueType`、`unitCode`、`materialTypes`、`extraJson`、`status`、`remark` |
| 组件包 | `componentCode`、`componentNameCn`、`componentNameEn`、`componentType`、`businessType`、`defaultQty`、`qtyMode`、`unitCode`、`status`、`remark` |
| 附件资料 | `ossId`、`assetCode`、`assetNameCn`、`assetNameEn`、`assetType`、`usageType`、`languageCode`、`visibility`、`versionNo`、`legacySource`、`legacyId`、`legacyPath`、`legacyUrl`、`status`、`remark` |
| 附件绑定 | `assetId/assetCode`、`targetType`、`targetId/targetCode`、`usageType`、`requiredForPublish`、`sortOrder`、`status`、`remark` |

### 6.4 最低交互验收

1. 六个入口都可以通过独立路由直达，不依赖先点击父 tab。
2. 所有列表页都支持：
   - 搜索
   - 分页
   - 新增 / 编辑 / 删除
   - 引用检查
3. 物料设置支持在主档编辑页维护 `ProductMaterialAttribute` 子表。
4. 面料资料只能在已选择或已过滤 `FabricSeries` 的上下文中新增，避免孤立 profile。
5. 组件包支持在同一编辑上下文维护多个 `ProductComponentItem`。
6. 附件绑定不能只允许手输 `targetCode`；至少要能按 `targetType` 拉取目标候选。
7. 删除或停用前，前端必须调用 `references(id)` 并展示引用结果。
8. 列表中的日期时间字段统一按 `formatUtc()` 展示；本模块若无日期列，不额外制造本地时区字段。

## 7. i18n Contract

### 7.1 key 命名方案

继续沿用 `productCenter.*` 前缀，建议新增分组：

- `productCenter.fabric.*`
- `productCenter.fabricSeries.*`
- `productCenter.fabricProfile.*`
- `productCenter.unit.*`
- `productCenter.baseAttribute.*`
- `productCenter.materialAttribute.*`
- `productCenter.componentItem.*`

### 7.2 最低 i18n key 集合

| 分组 | 最低 key |
| --- | --- |
| `productCenter.fabric` | `title`、`description` |
| `productCenter.fabricSeries` | `title`、`description`、`code`、`nameCn`、`nameEn`、`materialType`、`defaultThicknessUnit`、`thicknessRuleEnabled`、`maxThicknessDiff`、`maxCombinedThickness`、`widthRuleEnabled`、`availableWidths`、`minWidthValue`、`maxWidthValue`、`widthUnit` |
| `productCenter.fabricProfile` | `title`、`description`、`fabricCode`、`materialCode`、`seriesCode`、`colorCode`、`colorName`、`materialComposition`、`textureType`、`finishType`、`factoryModel`、`sampleBookNo`、`vendorItemNo`、`supplierCode`、`supplierName`、`widthValue`、`widthUnit`、`thicknessValue`、`thicknessUnit`、`gsmValue`、`purchaseUnitCode`、`inventoryUnitCode`、`salesUnitCode`、`legacyAttributeText` |
| `productCenter.unit` | `title`、`description`、`code`、`nameCn`、`nameEn`、`type`、`precisionScale`、`roundingMode`、`baseUnitCode`、`conversionRate` |
| `productCenter.baseAttribute` | `title`、`description`、`group`、`code`、`nameCn`、`nameEn`、`valueType`、`unitCode`、`materialTypes`、`extraJson` |
| `productCenter.materialAttribute` | `title`、`description`、`attributeCode`、`attributeNameCn`、`valueText`、`valueNumber`、`valueBool`、`valueUnitCode` |
| `productCenter.componentItem` | `title`、`description`、`materialCode`、`materialNameCn`、`qtyFormula`、`defaultQty`、`unitCode`、`requiredFlag` |
| `productCenter.asset` | 增补 `ossId`、`legacySource`、`legacyId`、`legacyPath`、`legacyUrl` |
| `productCenter.binding` | 增补 `assetId`、`targetSelectorHint`、`targetType`、`targetCode`、`requiredForPublish` |

### 7.3 i18n 约束

- 所有可见文案必须走 i18n，包括：
  - 页面标题
  - tab 标题
  - 搜索 placeholder
  - 抽屉标题
  - 表格列名
  - 按钮文案
  - 空状态
  - 引用检查提示
  - 错误提示 key
  - `aria-label` / `title`
- 新 key 先写 `i18n/locales/en_US.json`，`zh_CN.json` 由既有流程补齐。
- 不新增硬编码中文或英文 UI 文案。

## 8. 权限 Contract

### 8.1 权限 code 建议

为避免继续把全部基础资料塞进 `product:base:*`，建议按模块拆分：

| 模块 | 查询/入口 | 新增 | 编辑 | 删除 | 引用检查 / 明细 |
| --- | --- | --- | --- | --- | --- |
| 面料设置 | `product:fabric:list` | `product:fabric:add` | `product:fabric:edit` | `product:fabric:remove` | `product:fabric:reference` |
| 物料设置 | `product:material:list` | `product:material:add` | `product:material:edit` | `product:material:remove` | `product:material:reference` |
| 物料属性值 | `product:material-attribute:list` | `product:material-attribute:add` | `product:material-attribute:edit` | `product:material-attribute:remove` | `product:material-attribute:reference` |
| 组件包 | `product:component:list` | `product:component:add` | `product:component:edit` | `product:component:remove` | `product:component:reference` |
| 组件明细 | `product:component-item:list` | `product:component-item:add` | `product:component-item:edit` | `product:component-item:remove` | `product:component-item:reference` |
| 配置字典 | `product:base-attribute:list` | `product:base-attribute:add` | `product:base-attribute:edit` | `product:base-attribute:remove` | `product:base-attribute:reference` |
| 单位管理 | `product:unit:list` | `product:unit:add` | `product:unit:edit` | `product:unit:remove` | `product:unit:reference` |
| 附件资料 | `product:asset:list` | `product:asset:upload` | `product:asset:upload` | `product:asset:upload` | `product:asset:reference` |
| 附件绑定 | `product:asset-bind:list` | `product:asset-bind:add` | `product:asset-bind:edit` | `product:asset-bind:remove` | `product:asset-bind:reference` |

### 8.2 兼容建议

- Wave 1 如果后端权限暂未拆细，前端可以短期兼容旧权限：
  - `product:base:add`
  - `product:base:edit`
  - `product:base:remove`
  - `product:base:reference`
- 但 contract 层面以模块化权限为准，避免后续继续扩散共享权限。

## 9. UTC、错误提示与边界约束

### 9.1 UTC / 日期

- 本模块主数据以文本、数值、状态字段为主，不新增前端本地时间业务字段。
- 若后端返回 `createdTime`、`updatedTime`、`lastSyncTime` 等审计字段：
  - 列表展示统一使用 `formatUtc()`
  - 查询参数若涉及时间范围，统一使用 `withUtcDateRange()` 或 `withUtcDateRangeParams()`
- 不新增自定义日期格式化逻辑，不直接手写 `new Date()` 展示业务时间。

### 9.2 错误提示边界

- 表单校验错误优先本地 i18n key：
  - `productCenter.common.required`
  - 未来可补 `productCenter.common.invalidNumber`、`productCenter.common.invalidJson`
- 后端业务错误必须返回 message key，前端不写死业务报错文案。
- 引用检查失败、删除失败、状态切换失败均以统一错误 key 渲染，不在页面层拼接硬编码字符串。

### 9.3 前端边界

- 不在前端猜测不存在的字典或接口返回结构。
- 不新增重量级依赖。
- 不修改 `admin-ui/public/i18n/*.json`。
- 不把单位管理继续当作普通字典处理。
- 不为电机、遥控器、胶条、配重条等扩出专属前端 profile 页面。

## 10. 实施阶段验收清单

1. 六个模块入口均可通过独立路由访问。
2. 简单主档模块继续复用 `ProductEntityGridPage`，未引入无必要重构。
3. 物料属性、组件明细、附件绑定目标选择均有明确扩展交互，不退化为纯文本字段。
4. `materialCode` 是物料与面料资料主业务编号口径，前后端命名一致。
5. `FabricSeries` / `FabricProfile`、`ProductMaterialAttribute`、`ProductComponentItem`、`ProductMediaBinding` 的前端类型已独立定义。
6. 新增文案仅进入 `i18n/locales/en_US.json`。
7. 权限 code 方案不再依赖单一 `product:base:*` 覆盖全部基础资料。
8. 日期和错误处理遵守现有 UTC / i18n 规范。
