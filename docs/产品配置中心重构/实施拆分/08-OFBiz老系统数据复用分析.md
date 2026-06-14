# 08-OFBiz 老系统数据复用分析

生成日期：2026-06-11

本文记录对 `/Users/chengmuxuan/Desktop/cmx/ofbiz` 老订单系统的只读分析结论。

本次分析做了两件事：

1. 从 OFBiz 代码和实体模型看产品、配置、订单、附件的业务结构。
2. 通过 OFBiz 现有数据库连接只读查询真实库的表结构、表行数、类型分布。

未导出客户、联系人、地址、付款、订单明细原文等敏感业务数据。

---

## 1. 总结结论

老系统数据可以拿来做新产品配置中心的测试数据，但不能 1:1 当成新系统正式模型。

适合复用为测试数据的部分：

```text
产品主数据
产品分类
可配置产品
配置问题
配置选项
配置选项绑定的物料 / 子产品
产品图片 / 说明 / 文档类资料的附件索引
报价 / 订单中的产品、数量、尺寸、房间、价格字段
```

需要谨慎或先脱敏的部分：

```text
客户 / 经销商 / 用户 / 地址 / 联系方式
报价和订单备注
订单附件
可能包含客户现场信息的图片、PDF、图纸
```

不建议直接复用为新系统核心模型的部分：

```text
OFBiz 通用实体主键和状态模型
OFBiz Content / DataResource 作为新附件主表
OFBiz ProductAssoc 直接作为新工程配置规则
OFBiz Quote / OrderHeader 直接作为新订单模型
```

正确做法是：把老系统作为“真实样本来源”，抽取成新系统的 seed/test data。

---

## 2. 真实库数据量概览

只读查询结果：

| 表 | 行数 | 复用判断 |
| --- | ---: | --- |
| `PRODUCT` | 886 | 可作为产品、物料、组件、配置成品样本 |
| `PRODUCT_CATEGORY` | 162 | 可作为产品分类、系列、控制系统、罩壳分类参考 |
| `PRODUCT_CATEGORY_MEMBER` | 1534 | 可作为产品和分类关系参考 |
| `PRODUCT_CONFIG` | 854 | 可作为配置模板问题顺序、必填项样本 |
| `PRODUCT_CONFIG_ITEM` | 49 | 可作为配置问题样本 |
| `PRODUCT_CONFIG_OPTION` | 601 | 可作为配置选项样本 |
| `PRODUCT_CONFIG_PRODUCT` | 544 | 可作为选项绑定物料 / 子产品样本 |
| `PRODUCT_CONFIG_OPTION_IACTN` | 0 | 老库没有明显的选项联动数据，联动规则要新建 |
| `PRODUCT_FEATURE` | 19 | 可参考，但不是主要配置来源 |
| `PRODUCT_FEATURE_APPL` | 0 | 不作为主要来源 |
| `PRODUCT_PRICE` | 127 实查类型分布 | 可作为基础价格样本，但不足以覆盖新价格规则 |
| `PRODUCT_ASSOC` | 294 | 主要是配置产品关联，不是完整工程配置 |
| `PRODUCT_CONTENT` | 12 | 产品内容绑定很少，附件更多在 `DATA_RESOURCE` |
| `CONTENT` | 470 | 附件 / 文本 / 内容索引来源 |
| `DATA_RESOURCE` | 459 | 附件资源主来源 |
| `IMAGE_DATA_RESOURCE` | 3 | 只有少量图片 BLOB |
| `OTHER_DATA_RESOURCE` | 0 | 没有通用文件 BLOB |
| `ORDER_HEADER` | 77 | 可做流程样本，涉及客户时要脱敏 |
| `ORDER_ITEM` | 97 | 可做订单行、尺寸、价格、房间样本 |
| `ORDER_ITEM_ATTRIBUTE` | 0 | 真实库没有订单行属性扩展数据 |
| `ORDER_CONTENT` | 0 | 真实库没有订单内容附件绑定 |
| `QUOTE` | 221 | 可作为报价流程样本，涉及客户时要脱敏 |
| `QUOTE_ITEM` | 161 | 可作为报价行、配置产品、价格样本 |
| `QUOTE_ATTRIBUTE` | 0 | 没有报价扩展属性 |
| `CUST_REQUEST` | 16 | 暂不作为一期主要测试数据 |
| `CUST_REQUEST_ITEM` | 13 | 暂不作为一期主要测试数据 |
| `CUST_REQUEST_CONTENT` | 0 | 无客户请求附件绑定 |

---

## 3. 类型分布

产品类型：

| 类型 | 数量 | 说明 |
| --- | ---: | --- |
| `SUBASSEMBLY` | 518 | 更像组件 / 物料 / 子装配 |
| `AGGREGATED_CONF` | 277 | 可配置产品，是重点 |
| `AGGREGATED` | 124 | 聚合产品，可参考 |
| `FINISHED_GOOD` | 9 | 成品样本较少 |

分类类型：

| 类型 | 数量 | 说明 |
| --- | ---: | --- |
| `SERIES_CATGR` | 99 | 可映射面料系列 / 产品系列参考 |
| `CATALOG_CATEGORY` | 41 | 可映射销售产品分类 |
| `CONTROL_SYSTEM_CATGR` | 16 | 可映射控制系统基础资料 |
| `VALANCE_CATGR` | 8 | 可映射罩壳分类 |

配置问题：

```text
PRODUCT_CONFIG_ITEM.config_item_type_id = SINGLE
数量：48
```

说明老系统主要是一组选项单选题，并不是复杂表达式配置器。

价格：

```text
PRODUCT_PRICE.product_price_type_id = DEFAULT_PRICE
currency_uom_id = USD
数量：127
```

说明老系统能给基础价格样本，但不能直接覆盖新系统的面积价、规则价、附加项价。

订单 / 报价状态：

```text
ORDER_CREATED   62
ORDER_APPROVED  14
ORDER_CANCELLED 4

QUO_CREATED   145
QUO_ORDERED   70
QUO_APPROVED  6
```

---

## 4. 附件机制分析

老 OFBiz 附件不是独立 OSS 表，而是通用内容模型：

```text
ProductContent
  -> Content
    -> DataResource
      -> ImageDataResource / OtherDataResource / ElectronicText / objectInfo
```

关键字段：

```text
PRODUCT_CONTENT.product_id
PRODUCT_CONTENT.content_id
PRODUCT_CONTENT.product_content_type_id

CONTENT.content_id
CONTENT.data_resource_id
CONTENT.content_name
CONTENT.mime_type_id

DATA_RESOURCE.data_resource_id
DATA_RESOURCE.data_resource_type_id
DATA_RESOURCE.data_resource_name
DATA_RESOURCE.mime_type_id
DATA_RESOURCE.object_info
DATA_RESOURCE.is_public

IMAGE_DATA_RESOURCE.image_data
OTHER_DATA_RESOURCE.data_resource_content
```

真实库附件资源类型分布：

| 类型 | 数量 | 判断 |
| --- | ---: | --- |
| `OFBIZ_FILE` | 335 | 最大头，通常表示文件资源，需要看 `object_info` 指向路径 |
| `ELECTRONIC_TEXT` | 68 | 文本内容，可作为说明文案样本 |
| `IMAGE_OBJECT` | 22 | 图片对象索引，但 BLOB 表只有 3 条 |
| `URL_RESOURCE` | 12 | 外链资源 |
| `LOCAL_FILE` | 11 | 本地文件路径资源 |
| `OFBIZ_FILE_BIN` | 5 | 少量二进制文件资源 |
| `SHORT_TEXT` | 4 | 短文本 |
| `CONTEXT_FILE` | 1 | 上下文文件 |

产品内容绑定类型：

```text
IMAGE             8
PRODUCT_NAME      1
DESCRIPTION       1
SPECIFICATION     1
FULFILLMENT_EXTASYNC 1
```

判断：

1. 老系统确实有附件 / 内容资源。
2. 大多数附件可能不是存在数据库 BLOB，而是 `DATA_RESOURCE.object_info` 里记录路径或资源标识。
3. 新系统不应该复刻 OFBiz 内容模型。
4. 新系统应继续复用 base-boot 现有 OSS：

```text
sys_oss
bocoo-common-oss
/system/oss/upload
/system/oss/download/{ossId}
/system/oss/listByIds/{ossIds}
```

迁移为测试数据时建议：

```text
DATA_RESOURCE / CONTENT / PRODUCT_CONTENT
  -> pc_media_asset
  -> pc_media_binding
  -> sys_oss 或 legacy_url / legacy_path 临时引用
```

如果文件本体可访问，再导入 OSS；如果文件本体不可访问，则先保存 legacy metadata，用于页面和流程测试。

---

## 5. 配置数据映射

老系统配置来源：

```text
PRODUCT
PRODUCT_CONFIG
PRODUCT_CONFIG_ITEM
PRODUCT_CONFIG_OPTION
PRODUCT_CONFIG_PRODUCT
PRODUCT_ASSOC
```

建议映射：

| OFBiz | 新系统 |
| --- | --- |
| `PRODUCT` where `product_type_id = AGGREGATED_CONF` | `pc_sales_product` |
| `PRODUCT` where `product_type_id = SUBASSEMBLY` | `pc_item_master` 或 `pc_component` |
| `PRODUCT_CATEGORY` / `PRODUCT_CATEGORY_MEMBER` | `pc_product_category` / 产品分类关系 |
| `PRODUCT_CONFIG` | `pc_config_template_question` 的问题排序和必填设置 |
| `PRODUCT_CONFIG_ITEM` | `pc_config_question` |
| `PRODUCT_CONFIG_OPTION` | `pc_config_option` |
| `PRODUCT_CONFIG_PRODUCT` | 选项关联物料 / 组件 |
| `PRODUCT_ASSOC` where `product_assoc_type_id = PRODUCT_CONF` | 可配置产品和实际销售 / 子产品关联 |

需要注意：

```text
PRODUCT_CONFIG_OPTION_IACTN = 0
```

老库没有明显的选项联动规则数据，所以新系统的可见性、互斥、禁用、依赖规则需要重新建模，不要期待从老库完整迁出来。

---

## 6. 工程配置数据判断

OFBiz 标准制造模块会用：

```text
PRODUCT_ASSOC.product_assoc_type_id = MANUF_COMPONENT
PRODUCT_ASSOC.product_assoc_type_id = ENGINEER_COMPONENT
```

代码里 `FindProductBom.groovy` 也是按这两个类型查旧系统的组件关系。

但真实库 `PRODUCT_ASSOC` 类型分布是：

```text
PRODUCT_CONF 277
ALSO_BOUGHT  27
```

结论：

1. 老库没有可直接复用的标准制造 BOM 数据，也没有新系统需要的完整工程配置数据。
2. `PRODUCT_CONFIG_PRODUCT` 可作为“选项带出物料 / 子产品”的参考。
3. 新系统一期要新建工程方案、构成项、可选范围、能力规则、带出规则、工程配置预览和缺失检查。
4. 老数据可以帮助我们生成工程配置测试样本，但不能替代新系统的工程配置维护和发布检查。

---

## 7. 订单 / 报价数据判断

老系统订单行字段里已经有一些对我们有用的信息：

```text
ORDER_ITEM.product_id
ORDER_ITEM.quantity
ORDER_ITEM.unit_price
ORDER_ITEM.item_description
ORDER_ITEM.comments
ORDER_ITEM.room
ORDER_ITEM.quote_id
ORDER_ITEM.quote_item_seq_id
```

报价行也有类似字段：

```text
QUOTE_ITEM.product_id
QUOTE_ITEM.quantity
QUOTE_ITEM.quote_unit_price
QUOTE_ITEM.client_quote_unit_price
QUOTE_ITEM.config_id
QUOTE_ITEM.room
```

代码中的订单导出逻辑还体现了业务字段：

```text
Mount Position
成品宽度
成品长度
控制系统
控制 / 电机方向
电机配置
面料型号 / 工厂型号
罩壳工艺
底杆型号
罩壳 / 底杆颜色
卷帘面料方向
拉珠长度
太阳能板
遮光条
伸缩杆
梦幻帘轨道
```

但真实库 `ORDER_ITEM_ATTRIBUTE = 0`，所以这些字段不是简单地存在订单属性表里。

判断：

1. 可以用订单 / 报价行生成“用户已下单过的配置场景”。
2. 订单客户、地址、联系人、备注必须脱敏或不导入。
3. 订单配置明细要从 `config_id` 和 `ProductConfigWorker` 相关逻辑反推，不要只看 `ORDER_ITEM_ATTRIBUTE`。

---

## 8. 推荐复用策略

第一阶段只做测试种子，不做完整迁移：

```text
1. 抽取 1 条产品线，例如卷帘
2. 抽取对应 PRODUCT / CATEGORY / CONFIG / OPTION / CONFIG_PRODUCT
3. 生成新系统基础资料、销售产品、配置模板
4. 抽取 DATA_RESOURCE / CONTENT / PRODUCT_CONTENT 作为资料资产 metadata
5. 能访问文件本体的导入 OSS；不能访问的保留 legacy_path
6. 抽取少量 QUOTE_ITEM / ORDER_ITEM 生成试算器回归样本
7. 人工补齐新系统工程配置规则和价格规则
```

第二阶段再考虑导入更多历史样本：

```text
更多产品线
更多报价行
更多订单行
更多附件资源
```

但仍然只作为开发 / 测试数据，不作为正式业务迁移。

---

## 9. 一期落地建议

优先拿老库做这些测试数据：

| 优先级 | 数据 | 用途 |
| --- | --- | --- |
| P0 | `AGGREGATED_CONF` 产品 + 配置题 + 选项 | 配置模板和配置试算器 |
| P0 | `PRODUCT_CONFIG_PRODUCT` | 选项带出组件 / 物料 |
| P0 | `PRODUCT_CATEGORY` 分类类型 | 产品分类、系列、控制系统、罩壳分类基础资料 |
| P1 | `DATA_RESOURCE` / `CONTENT` | 资料资产、附件绑定、OSS 导入测试 |
| P1 | `PRODUCT_PRICE` | 基础价格试算样本 |
| P1 | `QUOTE_ITEM` | 报价快照和价格回归样本 |
| P2 | `ORDER_ITEM` | 订单快照样本，必须脱敏 |

暂不优先：

```text
客户主数据
联系人 / 地址
付款 / 发票
订单内容附件
客户请求
完整 OFBiz 状态流
```

---

## 10. 对当前重构文档的影响

本分析不改变前面 01-07 的重构方向，只补充一个执行输入：

```text
新系统按我们的目标模型重建
老 OFBiz 数据用于生成真实感测试样本
附件仍然走 base-boot OSS
工程配置规则不能指望从老库直接迁出
配置题、选项、产品、分类可以作为第一批导入样本
```

后续如果开始写导入脚本，建议新增独立目录：

```text
tools/ofbiz-seed/
```

里面只放开发期一次性抽取脚本，不进入运行时业务链路。
