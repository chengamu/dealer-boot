# Seed Contract: OFBiz 到基础资料测试数据

## Task

- TaskId: `BASE-C3`
- Scope: 只定义 seed 映射，不修改 OFBiz 代码，不连接或输出真实数据库凭据。
- Source: `/Users/chengmuxuan/Desktop/cmx/ofbiz`

## OFBiz 来源判断

本阶段只把 OFBiz 当测试数据来源，不把 OFBiz 通用模型搬进新系统。

重点来源：

| OFBiz 来源 | 新系统用途 |
| --- | --- |
| `PRODUCT` | 物料、组件、配置产品样本来源 |
| `PRODUCT_CATEGORY` | 产品分类、面料系列、控制系统、罩壳分类来源 |
| `PRODUCT_CATEGORY_MEMBER` / `ProductProductCategoryView` | 产品与分类、系列、控制系统关系 |
| `PRODUCT_CONFIG_PRODUCT` | 配置选项带出物料 / 子装配来源 |
| `PRODUCT_CONFIG_OPTION` | 后续配置项选项来源，本阶段只做 seed 参考 |
| `PRODUCT_CONTENT` | 产品附件绑定来源 |
| `CONTENT` | 附件内容索引来源 |
| `DATA_RESOURCE` | 附件资源 metadata 来源 |

已在 OFBiz 代码中确认这些业务分类被使用：

- `SERIES_CATGR`：系列 / 面料系列参考。
- `CONTROL_SYSTEM_CATGR`：控制系统参考。
- `VALANCE_CATGR`：罩壳 / 工艺分类参考。

## 映射原则

1. 不复刻 OFBiz `Content` / `DataResource` 通用内容模型。
2. 文件本体可访问时，先进入现有 `sys_oss`，再写 `pc_media_asset.oss_id`。
3. 文件本体不可访问时，只写 `legacy_source`、`legacy_id`、`legacy_path`、`legacy_url`。
4. `PRODUCT.product_id` 只作为 `legacy_id` 或 seed 参考，不成为新系统主编号。
5. 新系统物料主编号统一使用 `material_code`。
6. 样册编号、供应商料号、供应商编码属于业务引用字段，不作为主键。

## 新系统 seed 落点

| 新系统表 | Seed 用途 |
| --- | --- |
| `pc_product_category` 或现有分类表 | 写产品分类、系列分类、控制系统分类、罩壳分类 |
| `pc_unit` | 写米、平方米、个、套、平方克重等单位 |
| `pc_base_attribute` | 写颜色、材质、纹理、涂层、控制系统、电压、协议、规格、重量等属性定义 |
| `pc_material` | 写面料、电机、遥控、胶条、安装码、配重条等物料主档 |
| `pc_material_attribute` | 写电机/遥控/胶条/安装码/配重条差异属性 |
| `pc_fabric_series` | 写面料系列和厚度/门幅限制 |
| `pc_fabric_profile` | 写面料颜色、材质、纹理、工厂型号、厚度、克重、样册编号等变体 |
| `pc_component` | 写组件包 |
| `pc_component_item` | 写组件包下包含的物料和数量公式 |
| `pc_media_asset` | 写色卡、图纸、规格书、安装说明等资料资产 |
| `pc_media_binding` | 写资料与分类、系列、面料、物料、组件的绑定关系 |

## 5 个产品测试样本

### 1. `ROLLER_SHADE_BASIC`

用途：基础卷帘样本。

至少写入：

- 产品分类：卷帘 / Roller Shade。
- 面料系列：基础涂层布系列，设置门幅和厚度限制。
- 面料资料：白色涂层布，包含颜色、门幅、型号、样册编号、供应商信息。
- 组件包：卷帘基础包。
- 组件明细：PET 胶条、安装码、配重条。
- 附件：色卡或规格书绑定到面料系列 / 面料资料。

### 2. `ROLLER_SHADE_MOTOR`

用途：电动卷帘样本。

至少写入：

- 产品分类：电动卷帘。
- 控制系统属性：无拉、拉珠、奥克电机、ZIGBEE 奥克电机、matter 奥克电机、蓝牙奥克电机等。
- 物料：电机、遥控器。
- 物料属性：电压、协议、接线款、通道数、颜色。
- 组件包：电动控制包。
- 附件：电机规格书或接线说明。

### 3. `ZEBRA_SHADE_BASIC`

用途：柔纱帘 / 斑马帘样本。

至少写入：

- 产品分类：柔纱帘 / Zebra Shade。
- 面料系列：双层面料系列。
- 系列约束：启用厚度组合限制，例如两层厚度最大组合值。
- 面料资料：颜色、材质、纹理、厚度、门幅。
- 组件包：柔纱帘基础组件包。
- 附件：色卡或安装说明。

### 4. `OUTDOOR_SHADE`

用途：户外遮阳样本。

至少写入：

- 产品分类：户外遮阳。
- 面料系列：户外遮阳面料系列。
- 面料资料：高克重、涂层、较大门幅。
- 物料属性：安装码、配重条、绳/拉珠等五金属性。
- 组件包：户外安装包。
- 附件：安装说明、规格图纸。

### 5. `CURTAIN_TRACK_SAMPLE`

用途：轨道 / 窗帘配件样本。

至少写入：

- 产品分类：轨道 / Curtain Track。
- 物料：轨道、电机或遥控器。
- 物料属性：颜色、长度规格、电压、协议。
- 组件包：轨道电机包。
- 组件明细：轨道、电机、遥控器、安装件。
- 附件：图纸或安装说明。

## 最小验收矩阵

| 样本 | 分类 | 面料/系列 | 通用物料属性 | 组件包/明细 | 附件绑定 |
| --- | --- | --- | --- | --- | --- |
| `ROLLER_SHADE_BASIC` | 必须 | 必须 | 必须 | 必须 | 必须 |
| `ROLLER_SHADE_MOTOR` | 必须 | 可选 | 必须 | 必须 | 必须 |
| `ZEBRA_SHADE_BASIC` | 必须 | 必须 | 可选 | 必须 | 必须 |
| `OUTDOOR_SHADE` | 必须 | 必须 | 必须 | 必须 | 必须 |
| `CURTAIN_TRACK_SAMPLE` | 必须 | 可选 | 必须 | 必须 | 必须 |

## 敏感信息规则

- 不把 OFBiz 数据库连接串、账号、密码写入 `.ai`、日志或最终输出。
- 后续 `/do` 需要连接 OFBiz 真实库时，只输出表名、行数、字段摘要和 seed 结果。
- 若附件本体路径不可访问，记录 `legacy_path` / `legacy_url`，不强行复制文件。

## Acceptance

- 已明确 OFBiz 表到新系统基础资料的 seed 映射。
- 已明确至少 5 个产品测试样本名称和覆盖面。
- 已明确无法访问附件本体时写 `legacy_*` 字段。
- 未输出敏感连接信息。
