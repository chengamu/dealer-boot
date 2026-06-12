# 产品配置中心基础资料模块重构需求

创建日期：2026-06-11

## Requirement Source

- 用户命令：`/plan 产品配置中心基础资料模块重构，参考 docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`
- 参考文档：`docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`
- 关联背景：当前一期未上线、无客户数据，产品配置中心第一期旧实现允许修改、删除和重建；数据库脚本和开发数据库允许直接调整。

## 目标

重构产品配置中心的基础资料模块，把后续配置器、工程规则、BOM、价格、发布和订单快照依赖的主数据先建稳。

一期必须覆盖：

- 产品分类 / Product Category
- 单位管理 / Unit of Measure
- 面料系列 / Fabric Series
- 面料资料 / Fabric Profile
- 物料 / Material
- 通用物料属性 / Material Attribute
- 组件包 / Component
- 组件明细 / Component Item
- 配置字典 / Base Attribute
- 附件资料 / Media Asset
- 附件绑定 OSS / Media Binding
- OFBiz 测试 seed 来源标识

## 不做范围

- 不实现完整配置规则引擎。
- 不实现 BOM 规则计算。
- 不实现工程尺寸扣减试算。
- 不实现价格试算或发布审批流。
- 不做正式历史数据迁移。
- 不导入客户、地址、联系人、付款等订单外围数据。
- 不新增上传系统，附件复用现有 `sys_oss` / OSS 能力。

## 当前实现判断

代码侧已确认当前基础资料仍是浅 CRUD：

- 前端 `ProductBaseInfoPage.vue` 只有 `category`、`material`、`component` 三个页签。
- 前端 `ProductAssetPage.vue` 只有 `asset`、`binding` 两个页签。
- 后端已有 `ProductCategory`、`ProductMaterial`、`ProductComponent`、`ProductMediaAsset`、`ProductMediaBinding` 等对象，可保留改造。
- 当前缺 `FabricSeries`、`FabricProfile`、`ProductUnit`、`ProductMaterialAttribute`、`ProductComponentItem`、`ProductBaseAttribute`。
- 当前组件仍像单物料，不足以表达组件包包含多个物料。

## 关键业务规则

1. `material_code` 是主业务编号。
2. 不再引入单独 `SKYSPF 编号` 字段；老订单系统编号仅作为 `legacy_source` / `legacy_id` 追溯。
3. `sample_book_no`、`vendor_item_no`、`supplier_code` 是外部关联编号，不作为主身份。
4. 面料的主要规则在 `Fabric Series`：
   - 可用门幅、最小门幅、最大门幅。
   - 厚度单位、默认厚度、最大厚度差、最大组合厚度。
   - 后续系列可约束“两层厚度不能超过多少”等规则。
5. `Fabric Profile` 表示系列下的颜色、材质、纹理、涂层、工厂型号、样册编号、供应商料号、克重、厚度等变体。
6. 电机、遥控器、胶条、安装码、配重条等不单独建 profile 表，统一进入 `ProductMaterial` + `ProductMaterialAttribute`。
7. 允许字段冗余，优先查询性能、试算性能和后续分析：
   - 高频展示字段冗余到主表。
   - 写入、编辑、导入时由 Service 同步冗余字段。
   - 发布包和订单快照后续保留当时值。
8. 附件复用现有 OSS：
   - 文件本体进入 `sys_oss`。
   - `pc_media_asset` 记录 `oss_id`、资料类型、语言、版本、legacy 信息。
   - `pc_media_binding` 记录绑定对象、用途、是否发布必需。

## 数据库影响

优先修改 `sql/postgresql/product_capability.sql`。

保留改造：

- `pc_category` 或重命名/改造为产品分类表。
- `pc_material`
- `pc_component`
- `pc_media_asset`
- `pc_media_binding`

新增或补齐：

- `pc_unit`
- `pc_fabric_series`
- `pc_fabric_profile`
- `pc_material_attribute`
- `pc_base_attribute`
- `pc_component_item`

## 后端影响

预计新增或改造包路径：

- `bocoo-modules-product/src/main/java/com/bocoo/product/controller`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service`
- `bocoo-modules-product/src/main/java/com/bocoo/product/mapper`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/entity`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/bo`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo`

建议对象名：

- `ProductUnit`
- `ProductBaseAttribute`
- `ProductMaterialAttribute`
- `FabricSeries`
- `FabricProfile`
- `ProductComponentItem`

## 前端影响

预计新增或改造：

- `admin-ui/src/pages/product-center/base/ProductBaseInfoPage.vue`
- `admin-ui/src/pages/product-center/assets/ProductAssetPage.vue`
- `admin-ui/src/pages/product-center/fabric/ProductFabricPage.vue`
- `admin-ui/src/api/product-capability/base.ts`
- `admin-ui/src/api/product-capability/fabric.ts`
- `admin-ui/src/api/product-capability/asset.ts`
- `admin-ui/src/i18n/locales/en_US.json`

可采用页签或独立页面，但一期必须让 6 个基础资料模块可维护：

- 面料设置
- 物料设置
- 组件包
- 配置字典
- 附件资料
- 单位管理

## OFBiz 测试数据要求

OFBiz 作为测试 seed 来源，不作为新系统模型。

需要从 `/Users/chengmuxuan/Desktop/cmx/ofbiz` 的代码和其连接的真实数据库继续核对字段来源，优先使用：

- `PRODUCT`
- `PRODUCT_CATEGORY`
- `PRODUCT_CONFIG`
- `PRODUCT_CONFIG_ITEM`
- `PRODUCT_CONFIG_OPTION`
- `PRODUCT_CONFIG_PRODUCT`
- `CONTENT`
- `DATA_RESOURCE`
- `PRODUCT_CONTENT`

至少写入 5 个产品测试样本，覆盖：

- 产品分类 / 系列
- 面料系列与面料资料
- 电机或遥控等控制系统物料
- 胶条、安装码、配重条等通用属性物料
- 组件包和组件明细
- 附件资料和 OSS / legacy 绑定

建议样本：

- `ROLLER_SHADE_BASIC`
- `ROLLER_SHADE_MOTOR`
- `ZEBRA_SHADE_BASIC`
- `OUTDOOR_SHADE`
- `CURTAIN_TRACK_SAMPLE`

## 验收标准

- 基础资料 6 个模块均有清晰入口和 CRUD 能力。
- 面料系列支持厚度、门幅、组合厚度等约束字段。
- 面料资料支持颜色、材质、纹理、涂层、工厂型号、样册编号、供应商料号、克重、厚度。
- 物料只保留一个主编号 `material_code`，外部编号按引用字段维护。
- 电机、遥控、胶条、安装码、配重条通过通用属性维护，不新增每类专表。
- 组件包支持多个组件明细。
- 单位管理具备精度、类型、换算字段，不再只依赖普通字典。
- 附件资料复用 OSS，并可绑定到分类、系列、面料、物料、组件等对象。
- 禁用/删除基础资料前有引用检查。
- 至少 5 个产品测试样本可查询，且能覆盖后续配置中心最小演示链路。
- 遵守项目 i18n、UTC、权限、分页和租户边界。

## 风险

- 数据库表调整较多，需要先完成 contract，再进入并行实现。
- 当前没有覆盖测试，后续 `/do` 必须优先做编译、SQL 初始化、前端类型检查和浏览器 smoke。
- OFBiz 真实库连接可能涉及本地环境和敏感连接串，计划阶段不输出连接信息；执行阶段只记录表结构和统计，不打印凭据。
