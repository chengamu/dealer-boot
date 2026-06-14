# 09-基础资料模块 AMU 实施计划

生成日期：2026-06-11

本文是给 `amu-workflow` 执行“产品配置中心：基础资料模块”使用的实施输入。

建议入口：

```text
/plan 产品配置中心基础资料模块重构，参考 docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md
```

本文不直接替代 `/plan` 产物。AMU 执行时仍需生成 `.ai/requirements`、`.ai/CURRENT.md`、`.ai/TASKS.md` 和 wave-plan。

---

## 1. 模块目标

基础资料模块要先把后续配置器、工程配置、价格、发布、订单快照以及未来 ERP 依赖的主数据打稳。

一期目标：

```text
产品分类
标准单位
面料系列
面料资料（颜色 / 材质 / 纹理 / 工厂型号变体）
物料
组件
组件明细
控制系统 / 颜色 / 工艺类基础属性
资料资产
资料绑定 OSS
OFBiz 测试数据来源标识
```

不在本阶段完成：

```text
配置器完整规则引擎
工程配置规则计算
工程尺寸扣减试算
价格规则试算
发布审批流
正式历史数据迁移
客户 / 地址 / 联系人 / 付款数据导入
```

---

## 2. 现有实现判断

当前 base-boot 已有基础资料浅层 CRUD：

| 现有对象 | 当前文件 | 判断 |
| --- | --- | --- |
| 产品分类 | `ProductCategory` / `ProductCategoryMapper` | 可保留改造 |
| 物料 | `ProductMaterial` / `ProductMaterialMapper` | 可保留改造，但字段不足 |
| 组件 | `ProductComponent` / `ProductComponentMapper` | 可保留改造，但缺组件明细 |
| 资料资产 | `ProductMediaAsset` / `ProductMediaAssetMapper` | 可保留改造，需绑定 OSS 和 legacy 来源 |
| 资料绑定 | `ProductMediaBinding` / `ProductMediaBindingMapper` | 可保留改造，需强化 target 类型和发布必需资料 |
| 基础资料页面 | `ProductBaseInfoPage.vue` | 可保留改造 |
| 资料资产页面 | `ProductAssetPage.vue` | 可保留改造 |

当前明显缺口：

```text
面料系列 / 面料资料
通用物料属性表，避免电机、遥控器、安装码、胶条、配重条等每类都建 profile
组件明细 component_item
单位管理不能只停留在普通字典，需要维护精度和基础换算
配置字典需要明确属性组：颜色、材质、纹理、涂层、控制系统、罩壳分类等
面料系列缺可用门幅、最小/最大门幅等结构化约束
OFBiz legacy_source / legacy_id
资料资产和 sys_oss 的明确绑定字段
资料类型和发布必需资料规则
```

---

## 3. OFBiz 参考来源

老 OFBiz 数据可以作为测试 seed 来源，不作为新系统模型。

| OFBiz 表 / 类型 | 行数 / 特征 | 新系统用途 |
| --- | ---: | --- |
| `PRODUCT` | 886 | 产品、物料、组件、配置成品样本 |
| `PRODUCT` where `product_type_id = SUBASSEMBLY` | 518 | 优先映射物料 / 子装配 |
| `PRODUCT` where `product_type_id = AGGREGATED_CONF` | 277 | 后续映射销售产品，不放在基础资料首批 |
| `PRODUCT_CATEGORY` | 162 | 产品分类、系列、控制系统、罩壳分类 |
| `PRODUCT_CATEGORY` where `product_category_type_id = SERIES_CATGR` | 99 | 面料系列 / 产品系列参考 |
| `PRODUCT_CATEGORY` where `product_category_type_id = CATALOG_CATEGORY` | 41 | 产品分类 |
| `PRODUCT_CATEGORY` where `product_category_type_id = CONTROL_SYSTEM_CATGR` | 16 | 控制系统 |
| `PRODUCT_CATEGORY` where `product_category_type_id = VALANCE_CATGR` | 8 | 罩壳分类 / 工艺属性 |
| `PRODUCT_CONFIG_PRODUCT` | 544 | 选项带出物料 / 子产品样本 |
| `CONTENT` | 470 | 附件 / 文本 / 内容索引 |
| `DATA_RESOURCE` | 459 | 附件资源 metadata |
| `PRODUCT_CONTENT` | 12 | 产品资料绑定样本 |

OFBiz 附件链路：

```text
ProductContent
  -> Content
    -> DataResource
      -> ImageDataResource / OtherDataResource / ElectronicText / objectInfo
```

新系统附件链路：

```text
sys_oss
pc_media_asset
pc_media_binding
```

迁移原则：

```text
能访问文件本体：导入 sys_oss，再写 pc_media_asset.oss_id
不能访问文件本体：保留 legacy_source / legacy_id / legacy_path / legacy_url
不复刻 OFBiz Content / DataResource 通用模型
```

---

## 4. 中英文命名总表

### 4.1 模块名

| 中文名 | English Name | 代码建议 |
| --- | --- | --- |
| 基础资料 | Base Information | `base` / `BaseInfo` |
| 产品分类 | Product Category | `ProductCategory` |
| 标准单位 | Unit of Measure | `ProductUnit` / `pc_unit` |
| 单位换算 | Unit Conversion | `ProductUnitConversion` |
| 面料系列 | Fabric Series | `FabricSeries` |
| 面料资料 | Fabric Profile | `FabricProfile` |
| 面料厚度 | Fabric Thickness | `fabricThickness` / `thickness_value` |
| 厚度组合限制 | Thickness Combination Limit | `thicknessLimit` |
| 材质成分 | Material Composition | `materialComposition` / `material_composition` |
| 纹理类型 | Texture Type | `textureType` / `texture_type` |
| 物料 | Item / Material | `ProductMaterial` 或 `ItemMaster` |
| 组件 | Component | `ProductComponent` |
| 组件明细 | Component Item | `ProductComponentItem` |
| 控制系统 | Control System | `ControlSystem` |
| 颜色 | Color | `ColorOption` |
| 工艺 / 表面处理 | Finish / Process | `FinishOption` |
| 资料资产 | Media Asset | `ProductMediaAsset` |
| 资料绑定 | Media Binding | `ProductMediaBinding` |
| 来源系统 | Legacy Source | `legacySource` |
| 来源编号 | Legacy ID | `legacyId` |

### 4.2 业务字段名

| 中文名 | English Name | 字段建议 |
| --- | --- | --- |
| 编码 | Code | `code` / 具体表使用 `category_code` |
| 中文名称 | Chinese Name | `name_cn` |
| 英文名称 | English Name | `name_en` |
| 状态 | Status | `status` |
| 排序 | Sort Order | `sort_order` |
| 备注 | Remark | `remark` |
| 单位编码 | Unit Code | `unit_code` |
| 供应商编码 | Supplier Code | `supplier_code` |
| 供应商名称 | Supplier Name | `supplier_name` |
| 工厂型号 | Factory Model | `factory_model` |
| 物料编号 | Material Code | `material_code` |
| 供应商料号 | Vendor Item No. | `vendor_item_no` |
| 样册编号 | Sample Book No. | `sample_book_no` |
| 门幅值 | Width Value | `width_value` |
| 门幅单位 | Width Unit | `width_unit` |
| 厚度值 | Thickness Value | `thickness_value` |
| 厚度单位 | Thickness Unit | `thickness_unit` |
| 平方克重 | GSM / Gram Weight | `gsm_value` |
| 克重单位 | GSM Unit | `gsm_unit` |
| 最大厚度差 | Max Thickness Difference | `max_thickness_diff` |
| 最大组合厚度 | Max Combined Thickness | `max_combined_thickness` |
| 可用门幅 | Available Widths | `available_widths` |
| 最小门幅 | Minimum Width | `min_width_value` |
| 最大门幅 | Maximum Width | `max_width_value` |
| 颜色编码 | Color Code | `color_code` |
| 颜色名称 | Color Name | `color_name` |
| 材质成分 | Material Composition | `material_composition` |
| 纹理类型 | Texture Type | `texture_type` |
| 涂层 / 表面处理 | Finish / Coating | `finish_type` |
| 采购单位 | Purchase Unit | `purchase_unit_code` |
| 库存单位 | Inventory Unit | `inventory_unit_code` |
| 销售单位 | Sales Unit | `sales_unit_code` |
| 原始属性文本 | Legacy Attribute Text | `legacy_attribute_text` |
| 是否发布必需 | Required for Publish | `required_for_publish` |
| OSS 文件 ID | OSS ID | `oss_id` |
| 旧系统来源 | Legacy Source | `legacy_source` |
| 旧系统 ID | Legacy ID | `legacy_id` |
| 旧系统路径 | Legacy Path | `legacy_path` |
| 旧系统 URL | Legacy URL | `legacy_url` |

### 4.3 状态枚举

| 中文名 | English Name | 建议值 |
| --- | --- | --- |
| 草稿 | Draft | `DRAFT` |
| 启用 | Enabled | `ENABLED` |
| 停用 | Disabled | `DISABLED` |
| 废弃 | Deprecated | `DEPRECATED` |

### 4.4 资料资产类型

| 中文名 | English Name | 建议值 |
| --- | --- | --- |
| 图片 | Image | `IMAGE` |
| 规格书 | Specification | `SPEC` |
| 证书 | Certificate | `CERT` |
| 安装说明 | Installation Guide | `INSTALL_GUIDE` |
| 产品手册 | Manual | `MANUAL` |
| 图纸 | Drawing | `DRAWING` |
| 色卡 | Color Card | `COLOR_CARD` |
| 其他 | Other | `OTHER` |

---

## 5. 数据库表计划

数据库脚本优先修改：

```text
sql/postgresql/product_capability.sql
```

项目当前未上线，可以按首次正式编码重建产品配置中心相关表。

### 5.1 建议保留改造

| 当前表 | 中文名 | 处理 |
| --- | --- | --- |
| `pc_category` | 产品分类 | 改造为 `pc_product_category` 或保留表名但补字段 |
| `pc_material` | 物料 | 改造，补 legacy、供应商、工厂型号、属性字段 |
| `pc_component` | 组件 | 改造，组件不再只等同单物料 |
| `pc_media_asset` | 资料资产 | 改造，补 `oss_id`、legacy 字段 |
| `pc_media_binding` | 资料绑定 | 改造，强化 target 和 publish 字段 |

### 5.2 建议新增

| 表名 | 中文名 | English Name | 说明 |
| --- | --- | --- | --- |
| `pc_fabric_series` | 面料系列 | Fabric Series | 来源可参考 OFBiz `SERIES_CATGR` |
| `pc_unit` | 单位管理 | Unit of Measure | 单位编码、精度、单位类型、基础换算 |
| `pc_material_attribute` | 物料属性值 | Material Attribute Value | 通用属性值表，承载规格、重量、电压、协议等差异属性 |
| `pc_fabric_profile` | 面料资料 | Fabric Profile | 颜色、材质、纹理、工厂型号、宽幅、厚度、克重 |
| `pc_component_item` | 组件明细 | Component Item | 组件包含哪些物料，支持默认数量 |
| `pc_base_attribute` | 基础属性 | Base Attribute | 控制系统、颜色、表面处理等轻量基础属性 |

### 5.3 配置字典和系统字典边界

单位管理不再只走 `product_unit` 普通字典，改为基础资料模块的 `pc_unit`。普通字典只保留不会被复杂计算引用的枚举值。

| 字典类型 | 中文名 | 用途 |
| --- | --- | --- |
| `product_material_type` | 物料类型 | fabric、hardware、motor、accessory |
| `product_component_type` | 组件类型 | rail、motor、cassette、bottom_rail |
| `product_business_type` | 业务类型 | shade、blind、curtain |
| `product_asset_type` | 资料类型 | image、spec、drawing、manual |

单位管理是独立页面。可以先做轻量 `pc_unit`，只维护单位编码、名称、单位类型、精度、基础单位和换算倍率；不做复杂多级换算和财务计量。

---

## 6. 表字段建议

### 6.0 字段冗余与同步策略

本模块以查询性能、试算性能、发布检查性能、后续 BI/分析便利为优先。允许字段冗余，不追求纯范式化。

原则：

```text
1. 主身份只认一个主编号，例如物料使用 material_code
2. 外部关联编号可以保留，例如 sample_book_no、supplier_code、vendor_item_no
3. 高频列表、试算、发布检查、订单快照所需字段允许冗余
4. 冗余字段由 Service 在新增、修改、导入、停用时同步
5. 删除优先软删除或停用，避免冗余快照断链
6. 订单快照和发布包保留当时字段值，不跟随后续基础资料修改
```

建议冗余字段：

| 表 | 冗余字段 | 用途 |
| --- | --- | --- |
| `pc_fabric_profile` | `material_code`、`material_name_cn`、`series_code`、`series_name_cn`、`supplier_name`、`unit_code` | 面料列表、配置器、导入核对、分析报表少联表 |
| `pc_component` | `component_type_name`、`business_type_name`、`default_unit_name` | 组件包列表和工程配置预览 |
| `pc_component_item` | `material_code`、`material_name_cn`、`material_type`、`unit_code`、`unit_name` | 工程配置预览和组件明细列表少联表 |
| `pc_media_binding` | `asset_code`、`asset_name_cn`、`asset_type`、`target_code`、`target_name` | 附件资料列表、发布检查、资料快照 |
| `pc_media_asset` | `oss_file_name`、`oss_url`、`mime_type`、`file_size` | 附件列表、预览、发布包 |
| `pc_material` | `spec_summary`、`primary_spec`、`primary_color`、`primary_weight` | 物料列表、搜索和分析少联属性表 |

同步要求：

```text
ProductFabricService 更新面料系列或物料后，同步 pc_fabric_profile 冗余字段
ProductBaseInfoService 更新物料后，同步 pc_component_item 冗余字段
ProductBaseInfoService 更新 pc_material_attribute 后，同步 pc_material.spec_summary 等高频冗余字段
资料资产更新后，同步 pc_media_binding 中的 asset 快照字段
导入 OFBiz seed 时同时写主字段和冗余字段
批量同步可提供 repair/sync 方法，但业务写入必须同步维护
```

### 6.1 `pc_product_category`

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `category_id` | 分类 ID | 主键 |
| `category_code` | 分类编码 | 唯一 |
| `category_name_cn` | 中文名称 | 必填 |
| `category_name_en` | 英文名称 | 可选 |
| `parent_id` | 父分类 ID | 支持树 |
| `category_type` | 分类类型 | `PRODUCT` / `SERIES` / `CONTROL_SYSTEM` / `VALANCE` |
| `category_level` | 分类层级 | 后端维护 |
| `category_path` | 分类路径 | 后端维护 |
| `status` | 状态 | `DRAFT` / `ENABLED` / `DISABLED` |
| `sort_order` | 排序 | 数字 |
| `legacy_source` | 旧系统来源 | 例如 `OFBIZ` |
| `legacy_id` | 旧系统 ID | 例如 `PRODUCT_CATEGORY_ID` |

### 6.2 `pc_fabric_series`

面料系列是面料物理资料和原始可用范围的承载层。配置、工程配置、发布检查可以引用系列编码、门幅、厚度等资料，但不能把工程方案规则塞进面料系列。厚度组合、可用门幅可以放在系列；适用产品线、适用控制系统、尺寸禁用、构成项带出规则必须进入工程配置模块。

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `series_id` | 系列 ID | 主键 |
| `series_code` | 系列编码 | 唯一 |
| `series_name_cn` | 中文名称 | 必填 |
| `series_name_en` | 英文名称 | 可选 |
| `category_id` | 所属分类 | 可选 |
| `supplier_code` | 供应商编码 | 可选 |
| `supplier_name` | 供应商名称 | 可选 |
| `default_thickness_unit` | 默认厚度单位 | 例如 `mm` |
| `width_rule_enabled` | 是否启用门幅规则 | 布尔 |
| `available_widths` | 可用门幅 | 可先用 JSON 数组，例如 `[200, 300]` |
| `min_width_value` | 最小门幅 | 数字 |
| `max_width_value` | 最大门幅 | 数字 |
| `width_unit` | 门幅单位 | 例如 `cm` |
| `max_thickness_diff` | 最大厚度差 | 用于限制同一系列中两块/两层面料厚度差 |
| `max_combined_thickness` | 最大组合厚度 | 用于限制两块/两层面料总厚度 |
| `thickness_rule_enabled` | 是否启用厚度规则 | 布尔 |
| `usage_hint` | 使用提示 | 仅用于人工备注，不作为工程限制判断 |
| `status` | 状态 | 必填 |
| `legacy_source` | 旧系统来源 | `OFBIZ` |
| `legacy_id` | 旧系统 ID | `PRODUCT_CATEGORY_ID` |

### 6.2.1 `pc_unit`

单位管理是基础资料模块的一等模块，不能只散落在普通字典里。价格、工程配置、采购、库存都会引用单位。

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `unit_id` | 单位 ID | 主键 |
| `unit_code` | 单位编码 | 唯一，例如 `m`、`sqm`、`pcs`、`cm`、`mm`、`g/m2` |
| `unit_name_cn` | 中文名称 | 必填 |
| `unit_name_en` | 英文名称 | 可选 |
| `unit_type` | 单位类型 | `LENGTH` / `AREA` / `COUNT` / `WEIGHT` / `THICKNESS` |
| `base_unit_code` | 基础单位编码 | 同类型换算基础单位 |
| `conversion_factor` | 换算倍率 | 相对基础单位，简单场景使用 |
| `decimal_scale` | 小数位 | 前端输入和后端计算精度 |
| `status` | 状态 | 必填 |

### 6.2.2 `pc_base_attribute`

配置字典用于维护会被配置器、工程配置、发布检查引用的基础属性，不等同普通系统字典。

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `attribute_id` | 属性 ID | 主键 |
| `attribute_group` | 属性组 | `COLOR` / `MATERIAL` / `TEXTURE` / `FINISH` / `CONTROL_SYSTEM` / `VALANCE` / `BOTTOM_RAIL` |
| `attribute_code` | 属性编码 | 组内唯一 |
| `attribute_name_cn` | 中文名称 | 必填 |
| `attribute_name_en` | 英文名称 | 可选 |
| `value_type` | 值类型 | `TEXT` / `NUMBER` / `BOOLEAN` / `OPTION` |
| `unit_code` | 默认单位 | 数值属性可配置，例如 g、mm、V |
| `material_types` | 适用物料类型 | JSON 或逗号编码，例如 `MOTOR,REMOTE,ACCESSORY` |
| `extra_json` | 扩展属性 | 颜色 hex、排序、适用范围等 |
| `status` | 状态 | 必填 |

### 6.2.3 `pc_material_attribute`

物料属性值表用于承载不同物料类型的差异属性。不要为每个物料类型都建独立 profile 表。

适用示例：

```text
PET 胶条：规格 = 1.2
安装码：形状 = 7字
配重条：规格 = 3.5*12，重量g = 50
电机：电压 = 24V，控制协议 = ZIGBEE
遥控器：通道数 = 1，频率 = 433MHz
```

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `material_attr_id` | 物料属性 ID | 主键 |
| `material_id` | 物料 ID | 关联 `pc_material` |
| `material_code` | 物料编号快照 | 冗余字段 |
| `attribute_id` | 属性 ID | 关联 `pc_base_attribute` |
| `attribute_code` | 属性编码快照 | 例如 `SPEC`、`WEIGHT_G`、`VOLTAGE` |
| `attribute_name_cn` | 属性名称快照 | 例如 规格、重量g、电压 |
| `value_text` | 文本值 | 规格、形状、协议等 |
| `value_number` | 数值 | 重量、厚度、电压等 |
| `value_bool` | 布尔值 | 是否静音、是否免安装等 |
| `value_unit_code` | 值单位 | 例如 g、mm、V |
| `sort_order` | 排序 | 数字 |
| `status` | 状态 | 必填 |

高频字段允许同步冗余到 `pc_material`，例如：

```text
primary_spec
primary_color
primary_weight
attribute_summary
```

这些字段用于列表、搜索、导出、分析，不替代 `pc_material_attribute` 明细。

### 6.3 `pc_fabric_profile`

面料资料是系列下的具体变体，主要区分颜色、材质、纹理、工厂型号、供应商料号等。它可以记录厚度等物理属性，但“两个厚度不能超过多少”这类组合规则由系列维护。

截图和 OFBiz 旧数据里，面料属性经常被拼成一段描述，例如名称、颜色、门幅、型号、样册编号、厚度、平方克重、供应商。新系统必须拆成结构化字段，不能继续只塞到备注里。

编号口径必须分级：面料作为一种物料，唯一业务主编号只使用 `material_code`。样册编号、供应商编码、供应商料号是外部关联编号，可以保留为字段用于采购、样册、供应商对账，但不能替代 `material_code`，也不参与新系统主身份判断。旧订单系统编号只放在 `legacy_source = OFBIZ` 和 `legacy_id` 中，用于导入追溯和去重。

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `fabric_id` | 面料 ID | 主键 |
| `material_id` | 物料 ID | 关联 `pc_material`，面料资料必须归属一个物料 |
| `material_code` | 物料编号 | 唯一业务编号，来自 `pc_material.material_code` |
| `material_name_cn` | 物料中文名快照 | 冗余字段，减少列表联表 |
| `fabric_name_cn` | 中文名称 | 必填 |
| `fabric_name_en` | 英文名称 | 可选 |
| `series_id` | 面料系列 ID | 关联 `pc_fabric_series` |
| `series_code` | 系列编码快照 | 冗余字段 |
| `series_name_cn` | 系列中文名快照 | 冗余字段 |
| `display_name` | 展示名称 | 例如 `200CM 白色涂层布150D*300DTC150*300白` |
| `fabric_short_name` | 面料简称 | 例如 `涂层布` |
| `factory_model` | 工厂型号 / 型号 | 例如 `150D*300D` |
| `sample_book_no` | 样册编号 | 例如 `TC150*300白`，外部关联编号 |
| `vendor_item_no` | 供应商料号 | 供应商侧编号，外部关联编号 |
| `thickness_value` | 面料厚度 | 数字 |
| `thickness_unit` | 厚度单位 | 默认继承系列，例如 `mm` |
| `width_value` | 门幅值 | 数字 |
| `width_unit` | 门幅单位 | 例如 `cm`，截图里有 200 / 300 |
| `gsm_value` | 平方克重 | 例如 440 |
| `gsm_unit` | 克重单位 | 默认 `g/m2` |
| `color_code` | 颜色编码 | 可选 |
| `color_name_cn` | 颜色中文名 | 可选 |
| `color_name_en` | 颜色英文名 | 可选 |
| `material_composition` | 材质成分 | 例如 polyester、linen blend 等 |
| `texture_type` | 纹理类型 | 例如 plain、woven、jacquard |
| `finish_type` | 涂层 / 表面处理 | 例如 coated、涂层 |
| `blackout_level` | 遮光等级 | 可选 |
| `purchase_unit_code` | 采购单位 | 例如 `m` / `sqm` |
| `inventory_unit_code` | 库存单位 | 例如 `m` / `sqm` |
| `sales_unit_code` | 销售单位 | 例如 `m` / `sqm` |
| `unit_code` | 默认单位 | 兼容字段，后续可由三类单位替代 |
| `unit_name` | 默认单位名称快照 | 冗余字段 |
| `supplier_code` | 供应商编码 | 外部关联编号，用于供应商对账 |
| `supplier_name` | 供应商名称 | 例如截图中的供应商公司名 |
| `purchase_enabled` | 是否可采购 | 布尔 |
| `sales_enabled` | 是否可销售 | 布尔 |
| `config_enabled` | 是否可用于配置 | 布尔 |
| `legacy_attribute_text` | 原始属性文本 | 保存旧系统竖向描述，便于核对导入 |
| `status` | 状态 | 必填 |
| `legacy_source` | 旧系统来源 | `OFBIZ` |
| `legacy_id` | 旧系统 ID | 可对应 `PRODUCT_ID` |

### 6.4 `pc_material`

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `material_id` | 物料 ID | 主键 |
| `material_code` | 物料编码 | 唯一 |
| `material_name_cn` | 中文名称 | 必填 |
| `material_name_en` | 英文名称 | 可选 |
| `material_type` | 物料类型 | 面料、五金、电机、辅料 |
| `business_type` | 业务类型 | 卷帘、斑马帘等 |
| `unit_code` | 单位 | 字典 |
| `supplier_code` | 供应商编码 | 可选 |
| `supplier_name` | 供应商名称 | 可选 |
| `factory_model` | 工厂型号 | 可选 |
| `vendor_item_no` | 厂商料号 | 可选 |
| `primary_spec` | 主规格 | 冗余字段，例如 `1.2`、`3.5*12` |
| `primary_color` | 主颜色 | 冗余字段 |
| `primary_weight` | 主重量 | 冗余字段 |
| `attribute_summary` | 属性摘要 | 冗余字段，例如 `规格:1.2; 重量g:50` |
| `status` | 状态 | 必填 |
| `legacy_source` | 旧系统来源 | `OFBIZ` |
| `legacy_id` | 旧系统 ID | `PRODUCT_ID` |

### 6.5 `pc_component`

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `component_id` | 组件 ID | 主键 |
| `component_code` | 组件编码 | 唯一 |
| `component_name_cn` | 中文名称 | 必填 |
| `component_name_en` | 英文名称 | 可选 |
| `component_type` | 组件类型 | 电机、罩壳、底杆、轨道 |
| `business_type` | 业务类型 | 卷帘、斑马帘等 |
| `default_qty` | 默认数量 | 可选 |
| `qty_mode` | 数量模式 | `FIXED` / `FORMULA` |
| `unit_code` | 单位 | 字典 |
| `status` | 状态 | 必填 |
| `legacy_source` | 旧系统来源 | `OFBIZ` |
| `legacy_id` | 旧系统 ID | 可对应 `PRODUCT_ID` |

### 6.6 `pc_component_item`

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `component_item_id` | 组件明细 ID | 主键 |
| `component_id` | 组件 ID | 关联组件 |
| `material_id` | 物料 ID | 关联物料 |
| `material_code` | 物料编码快照 | 方便展示 |
| `default_qty` | 默认用量 | 数字 |
| `qty_formula` | 用量公式 | 一期可预留，不先做复杂解析 |
| `unit_code` | 单位 | 字典 |
| `required_flag` | 是否必需 | 布尔 |
| `sort_order` | 排序 | 数字 |
| `status` | 状态 | 必填 |

### 6.7 `pc_media_asset`

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `asset_id` | 资料资产 ID | 主键 |
| `asset_code` | 资料编码 | 唯一 |
| `asset_name_cn` | 中文名称 | 必填 |
| `asset_name_en` | 英文名称 | 可选 |
| `asset_type` | 资料类型 | 图片、规格书、证书、图纸 |
| `usage_type` | 使用场景 | `PRODUCT` / `FABRIC` / `COMPONENT` / `PUBLISH` |
| `oss_id` | OSS 文件 ID | 关联 `sys_oss.oss_id` |
| `url` | 访问地址 | 可由 OSS 解析，也可 legacy 临时保留 |
| `mime_type` | MIME 类型 | 可选 |
| `language_code` | 语言 | `en_US` / `zh_CN` |
| `visibility` | 可见性 | `INTERNAL` / `CUSTOMER` |
| `version_no` | 版本号 | 默认 1 |
| `status` | 状态 | 必填 |
| `legacy_source` | 旧系统来源 | `OFBIZ` |
| `legacy_id` | 旧系统 ID | `DATA_RESOURCE_ID` / `CONTENT_ID` |
| `legacy_path` | 旧系统路径 | `object_info` |
| `legacy_url` | 旧系统 URL | URL 资源 |

### 6.8 `pc_media_binding`

| 字段 | 中文名 | 说明 |
| --- | --- | --- |
| `binding_id` | 绑定 ID | 主键 |
| `asset_id` | 资料资产 ID | 关联 `pc_media_asset` |
| `target_type` | 绑定目标类型 | `CATEGORY` / `FABRIC_SERIES` / `FABRIC` / `MATERIAL` / `COMPONENT` / `PRODUCT` |
| `target_id` | 绑定目标 ID | 数字 ID |
| `target_code` | 绑定目标编码 | 展示和导入兜底 |
| `usage_type` | 使用场景 | 图片、发布资料、内部工程资料 |
| `required_for_publish` | 发布是否必需 | 布尔 |
| `sort_order` | 排序 | 数字 |
| `status` | 状态 | 必填 |

---

## 7. 后端实施计划

当前后端包：

```text
bocoo-modules-product/src/main/java/com/bocoo/product
```

### 7.1 建议新增 / 改造 Controller

| 类名 | 中文名 | 处理 |
| --- | --- | --- |
| `ProductBaseInfoController` | 基础资料 Controller | 保留，继续承载分类、物料、组件、资料资产 |
| `ProductFabricController` | 面料资料 Controller | 新增，承载面料系列、面料资料 |
| `ProductComponentController` | 组件资料 Controller | 可选。如果组件明细复杂，单独拆出；否则先放 `ProductBaseInfoController` |
| `ProductMediaController` | 资料资产 Controller | 可选。若资产功能变重，从 `ProductBaseInfoController` 拆出 |
| `ProductOfbizSeedController` | OFBiz 测试数据导入 Controller | 不建议一期开放正式 Controller，先做 dev-only service 或命令脚本 |

### 7.2 建议新增 / 改造 Service

| 类名 | 中文名 | 处理 |
| --- | --- | --- |
| `ProductBaseInfoService` | 基础资料 Service | 保留改造 |
| `ProductFabricService` | 面料资料 Service | 新增 |
| `ProductMaterialAttributeService` | 物料属性 Service | 新增或并入 `ProductBaseInfoService`，必须支持通用属性值维护和冗余同步 |
| `ProductMediaAssetService` | 资料资产 Service | 可从 `ProductBaseInfoService` 拆出，视当前代码复杂度 |
| `ProductReferenceCheckService` | 引用检查 Service | 新增或预留，用于停用/删除前检查引用 |
| `OfbizSeedImportService` | OFBiz 测试数据导入 Service | 新增，开发期使用 |

### 7.3 建议新增 / 改造 Mapper

| Mapper | 中文名 | 处理 |
| --- | --- | --- |
| `ProductCategoryMapper` | 产品分类 Mapper | 保留改造 |
| `ProductMaterialMapper` | 物料 Mapper | 保留改造 |
| `ProductMaterialAttributeMapper` | 物料属性值 Mapper | 新增 |
| `ProductComponentMapper` | 组件 Mapper | 保留改造 |
| `ProductComponentItemMapper` | 组件明细 Mapper | 新增 |
| `ProductFabricSeriesMapper` | 面料系列 Mapper | 新增 |
| `ProductFabricProfileMapper` | 面料资料 Mapper | 新增 |
| `ProductUnitMapper` | 单位 Mapper | 新增 |
| `ProductBaseAttributeMapper` | 基础属性 Mapper | 新增 |
| `ProductMediaAssetMapper` | 资料资产 Mapper | 保留改造 |
| `ProductMediaBindingMapper` | 资料绑定 Mapper | 保留改造 |

### 7.4 Domain 命名

每张表按现有风格生成：

```text
domain/entity/*Entity
domain/bo/*Bo
domain/vo/*Vo
mapper/*Mapper
```

建议实体名：

```text
ProductCategory
ProductMaterial
ProductMaterialAttribute
ProductComponent
ProductComponentItem
ProductFabricSeries
ProductFabricProfile
ProductUnit
ProductBaseAttribute
ProductMediaAsset
ProductMediaBinding
```

---

## 8. 前端实施计划

当前前端目录：

```text
admin-ui/src/pages/product-center
admin-ui/src/api/product-capability
```

### 8.1 页面

| 页面 | 中文名 | 处理 |
| --- | --- | --- |
| `master-guide/ProductMasterGuidePage.vue` | 录入向导 | 独立菜单，基础资料第一入口 |
| `base/ProductUnitPage.vue` | 单位管理 | 独立菜单，基础资料第一入口 |
| `base/ProductBaseAttributePage.vue` | 配置字典 | 独立菜单，维护业务属性枚举 |
| `base/ProductBaseInfoPage.vue` | 物料管理 / 组件包 | 保留改造，拆成清晰业务入口 |
| `base/ProductFabricPage.vue` | 面料系列 / 面料资料 | 独立菜单，不再用同页 tab 作为正式入口 |
| `base/ProductMaterialAttributePage.vue` | 物料属性 | 默认隐藏；作为物料详情或高级维护能力 |
| `base/ProductComponentItemPage.vue` | 组件明细 | 默认隐藏；从组件包行内入口维护 |
| `assets/ProductAssetPage.vue` | 资料资产 / 资料绑定 | 默认隐藏；附件台账，不要求录入人员单独维护 |

基础资料菜单顺序必须保持：

```text
录入向导 -> 单位管理 -> 配置字典 -> 物料管理 -> 面料系列 -> 面料资料 -> 组件包
```

`物料属性`、`组件明细`、`资料资产`、`资料绑定` 是内部或高级能力，默认不在基础资料一级菜单展示。需要维护时从对应业务对象入口进入，例如物料属性从物料详情进入，组件明细从组件包进入，附件从物料/面料/组件包抽屉直接上传并绑定 OSS。

### 8.2 API 文件

| 文件 | 处理 |
| --- | --- |
| `admin-ui/src/api/product-capability/base.ts` | 保留改造，补组件明细、单位、基础属性、物料属性 API |
| `admin-ui/src/api/product-capability/asset.ts` | 保留改造，补 OSS 绑定字段 |
| `admin-ui/src/api/product-capability/fabric.ts` | 新增，面料系列/面料资料 API |
| `admin-ui/src/api/product-capability/ofbizSeed.ts` | 不建议做正式页面 API，开发期脚本优先 |

### 8.3 i18n Key 建议

新增可见文案只写：

```text
admin-ui/src/i18n/locales/en_US.json
```

建议 key：

```text
productCenter.fabric.series.title
productCenter.fabric.series.code
productCenter.fabric.series.nameCn
productCenter.fabric.series.nameEn
productCenter.fabric.profile.title
productCenter.fabric.profile.code
productCenter.fabric.profile.factoryModel
productCenter.componentItem.title
productCenter.materialAttribute.title
productCenter.materialAttribute.attributeCode
productCenter.materialAttribute.valueText
productCenter.materialAttribute.valueNumber
productCenter.unit.title
productCenter.baseAttribute.title
productCenter.asset.ossId
productCenter.asset.legacySource
productCenter.asset.legacyId
productCenter.common.legacySource
productCenter.common.legacyId
```

---

## 9. AMU Wave 任务建议

### Wave 0：确认边界和现状

| TaskId | Title | Owner | Files | Acceptance |
| --- | --- | --- | --- | --- |
| `BASE-00-01` | 读取基础资料现有实现和文档 | `main` | `docs/产品配置中心重构/**`、`bocoo-modules-product/**`、`admin-ui/src/pages/product-center/**` | 明确现有表、页面、Controller、Service、Mapper，列出保留/新增/删除 |
| `BASE-00-02` | 确认数据库脚本改造范围 | `main` | `sql/postgresql/product_capability.sql` | 给出本次要改的表和不改的表 |

Barrier 0：

```text
确认不碰生产配置
确认不导入客户/地址/付款
确认附件复用 OSS
确认 OFBiz 只作为测试数据来源
```

### Wave 1：数据库和领域模型

| TaskId | Title | Owner | Files | Acceptance |
| --- | --- | --- | --- | --- |
| `BASE-01-01` | 改造基础资料数据库表 | `main` | `sql/postgresql/product_capability.sql` | 表字段覆盖分类、面料、物料、物料属性、组件、组件明细、资料资产、资料绑定 |
| `BASE-01-01A` | 补齐单位和配置字典表 | `main` | `sql/postgresql/product_capability.sql` | `pc_unit`、`pc_base_attribute`、`pc_material_attribute` 可支撑单位管理、配置字典和通用物料属性 |
| `BASE-01-02` | 生成/调整后端 Entity/Bo/Vo/Mapper | `main` | `bocoo-modules-product/src/main/java/com/bocoo/product/domain/**`、`mapper/**` | Java 字段和 SQL 字段一致，Mapper 可编译 |
| `BASE-01-03` | 补充字典或基础属性枚举 | `main` | SQL 字典数据、前端字典使用处 | 单位、物料类型、组件类型、资料类型可被页面选择 |

Barrier 1：

```text
SQL 可执行
实体字段无明显遗漏，特别是 `pc_material_attribute`、`pc_unit`、`pc_base_attribute`
legacy_source / legacy_id 已覆盖需要导入的表
资料资产表已有 oss_id
```

### Wave 2：后端 CRUD 和引用检查

| TaskId | Title | Owner | Files | Acceptance |
| --- | --- | --- | --- | --- |
| `BASE-02-01` | 改造 ProductBaseInfoService | `main` | `ProductBaseInfoService.java` | 分类、物料、物料属性、组件、组件明细 CRUD 可用 |
| `BASE-02-02` | 新增 ProductFabricService | `main` | `ProductFabricService.java`、`ProductFabricController.java` | 面料系列、面料资料 CRUD 可用 |
| `BASE-02-03` | 改造资料资产和绑定服务 | `main` | `ProductBaseInfoService.java` 或新 service | 可保存 oss_id、legacy 字段、发布必需字段 |
| `BASE-02-04` | 增加删除/停用引用检查 | `main` | `ProductReferenceCheckService.java` | 被引用资料不能直接物理删除 |

Barrier 2：

```text
基础资料接口分页、详情、新增、修改、删除/停用可用
物料属性维护可用，能为不同物料类型维护不同属性值
修改物料属性后，`pc_material.attribute_summary` 等冗余字段同步
删除策略明确，不误删后续引用数据
接口权限沿用 product:base / product:asset，不新增混乱权限
```

### Wave 3：前端页面

| TaskId | Title | Owner | Files | Acceptance |
| --- | --- | --- | --- | --- |
| `BASE-03-01` | 改造基础资料页面 | `main` | `ProductBaseInfoPage.vue`、`base.ts` | 分类、物料、物料属性、单位、配置字典、组件字段完整 |
| `BASE-03-02` | 新增面料资料页面 | `main` | `ProductFabricPage.vue`、`fabric.ts` | 面料系列、面料资料可维护 |
| `BASE-03-03` | 改造资料资产页面 | `main` | `ProductAssetPage.vue`、`asset.ts` | OSS、legacy、资料类型、发布必需字段可维护 |
| `BASE-03-04` | 补齐 i18n | `main` | `admin-ui/src/i18n/locales/en_US.json` | 页面无新增硬编码文案 |

Barrier 3：

```text
页面能打开
列表能查询
表单能新增/编辑
字典下拉能正常展示
无明显文字溢出和字段缺失
```

### Wave 4：OFBiz 测试数据 seed

| TaskId | Title | Owner | Files | Acceptance |
| --- | --- | --- | --- | --- |
| `BASE-04-01` | 设计 OFBiz seed 抽取映射 | `main` | `tools/ofbiz-seed/**` 或文档 | 明确 PRODUCT/CATEGORY/DATA_RESOURCE 映射 |
| `BASE-04-02` | 实现开发期 seed 脚本 | `main` | `tools/ofbiz-seed/**` | 只导基础资料，不导客户、地址、付款 |
| `BASE-04-03` | 导入至少 5 个产品测试样本 | `main` | dev DB | 能生成 5 个可用于后续配置器、工程配置、价格验证的产品基础资料样本 |
| `BASE-04-04` | 校验 seed 覆盖度 | `main` | dev DB / 验证脚本 | 5 个样本覆盖面料、控制系统、电机/遥控、通用物料属性、组件包、附件资料 |

Barrier 4：

```text
seed 可重复执行或有去重策略
legacy_source / legacy_id 可追踪
敏感数据未导入
附件文件不可访问时保留 legacy metadata
至少 5 个产品测试样本可在基础资料页面查询到
每个产品样本至少有关联分类、面料或物料、组件包、资料资产中的三类数据
```

5 个产品测试样本建议：

| 样本 | 覆盖重点 | 必须包含 |
| --- | --- | --- |
| `ROLLER_SHADE_BASIC` | 卷帘基础款 | 面料系列、面料资料、拉珠/无拉控制、底杆或配重条、色卡图 |
| `ROLLER_SHADE_MOTOR` | 电动卷帘 | 电机、遥控器或控制协议、控制系统字典、组件包 |
| `ZEBRA_SHADE_BASIC` | 斑马帘基础款 | 面料系列、门幅规则、安装码、底杆、附件资料 |
| `OUTDOOR_SHADE` | 户外/阳光面料款 | 厚度/克重、PET 胶条、配重条、规格属性 |
| `CURTAIN_TRACK_SAMPLE` | 轨道/梦幻帘样本 | 轨道物料、遥控/电机可选控制、组件包 |

说明：

```text
这里的“产品样本”不是完整销售产品发布包，而是能支撑后续配置器、工程配置、价格模块继续开发的基础资料样本。
如果 OFBiz 里某个样本数据不完整，可以用手工 seed 补齐，但必须保留 legacy_source / legacy_id 或 seed_source 便于追踪。
```

### Wave 5：检查

| TaskId | Title | Owner | Files | Acceptance |
| --- | --- | --- | --- | --- |
| `BASE-05-01` | 后端编译检查 | `main` | Maven 模块 | 编译通过，或记录明确失败原因 |
| `BASE-05-02` | 前端类型/构建检查 | `main` | `admin-ui` | 类型和构建通过，或记录明确失败原因 |
| `BASE-05-03` | 浏览器验证 | `main` | 前端页面 | 基础资料页面、面料页面、资料资产页面可操作 |
| `BASE-05-04` | 静态审查 | `main` | 改动文件 | 检查权限、i18n、UTC、OSS、删除策略 |

---

## 10. 验收口径

基础资料模块完成，不是指 CRUD 页面能打开，而是要满足：

```text
1. 录入向导、单位管理、配置字典、物料管理、面料系列、面料资料、组件包都有清晰业务入口
2. 组件可以包含多个物料
3. 物料、面料、组件包可以直接上传和查看附件，底层复用 OSS、资料资产和资料绑定台账
4. 面料有系列、颜色、材质、纹理、工厂型号、门幅、单位
5. 面料资料可记录厚度；面料系列可以维护厚度组合限制，例如最大厚度差、最大组合厚度
6. 面料系列可以维护可用门幅、最小/最大门幅
7. 单位管理可以维护单位编码、单位类型、小数精度和简单换算
8. 配置字典可以维护颜色、材质、纹理、涂层、控制系统、罩壳分类等属性
9. 通用物料属性可以维护 PET 胶条、安装码、配重条、电机、遥控器等不同属性，不新增类型专属 profile 表
10. 物料和组件有供应商、单位、业务类型、状态
11. 停用/删除前有引用检查或明确软删除策略
12. 前端文案走 i18n
13. 后端时间继续遵守 UTC 约定
14. 不新增独立附件上传系统
15. 至少写入 5 个产品测试样本，覆盖面料、物料属性、组件包、控制系统、附件绑定
16. OFBiz 数据只作为开发测试 seed，不作为正式迁移
```

---

## 11. 给 AMU 的注意事项

执行 `/plan` 时必须先看：

```text
docs/产品配置中心重构/实施拆分/02-基础资料模块.md
docs/产品配置中心重构/实施拆分/07-现有实现对比与改造清单.md
docs/产品配置中心重构/实施拆分/08-OFBiz老系统数据复用分析.md
docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md
```

执行 `/do` 时不要一次铺满所有后续模块：

```text
不做配置器规则引擎
不做工程配置试算
不做价格试算
不做发布审批
不导客户数据
不重做 OSS
```

如果发现当前 SQL 和 Java 实体差异很大，优先以本计划目标模型为准，因为当前系统未上线，可以按首次正式编码前重构处理。
