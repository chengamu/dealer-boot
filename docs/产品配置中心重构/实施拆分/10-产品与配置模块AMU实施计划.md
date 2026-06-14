# 10-产品与配置模块 AMU 实施计划

生成日期：2026-06-12

本文是给 `amu-workflow` 执行“产品配置中心：产品与配置模块第一阶段”使用的实施输入。

> 2026-06-14 修正：本文记录的是“产品与配置第一阶段”的阶段计划和历史执行口径。根据后续业务梳理，产品配置不能再脱离基础信息和工程配置单独向订单推进。下一阶段必须先补 `基础信息与工程配置`，再回到销售产品发布和订单快照。

建议入口：

```text
/plan 产品配置中心产品与配置模块第一阶段重构，参考 docs/产品配置中心重构/实施拆分/10-产品与配置模块AMU实施计划.md
```

确认计划后执行：

```text
/do all。允许数据库直接执行，允许登录，admin 密码 123456；dev 配置可连接数据库；完成后 build、同步 bocoo-admin/target/dist，并用浏览器真实验证。
```

阶段完成后执行：

```text
/archive。归档本阶段经验、踩坑、菜单和接口边界。
```

本文不替代 `/plan` 产物。AMU 执行时仍需生成 `.ai/requirements`、`.ai/CURRENT.md`、`.ai/TASKS.md` 和 wave-plan。

---

## 1. 阶段目标

本阶段从“基础资料录入”进入“客户可下单产品配置”。该阶段只能完成配置录入骨架，不能作为订单可用闭环。

一期第一阶段目标：

```text
销售产品能维护
标品 SKU 能维护或预留清晰入口
配置模板能维护
问题组 / 配置问题 / 配置答案能维护
答案能引用配置字典、物料、组件包、附件资料
配置求值器能返回可见问题、可选答案、禁用原因、校验结果
```

不在本阶段完成：

```text
最终价格计算
正式 BOM 计算
工程扣减完整试算
发布审批流
订单快照闭环
Excel 批量导入
客户门户前台下单页
```

后续前置修正：

```text
产品配置进入订单前，必须先完成基础信息与工程配置闭环：
基础资料 -> 工程配置 -> 产品配置 -> 发布/订单快照
```

---

## 2. 推荐执行顺序

不要一次性铺满所有菜单。

第一阶段按下面顺序执行：

```text
1. 销售产品 Sales Product
2. 配置模板 Config Template
3. 问题组 Question Group
4. 配置问题 Config Question
5. 配置答案 Config Option
6. 配置规则 Config Rule
7. 配置求值器 Config Evaluator
8. 标品 SKU Standard SKU 先预留或做轻量 CRUD
```

如果工期压力大，第一轮最小闭环为：

```text
销售产品
配置模板
问题组
配置问题
配置答案
配置求值器
```

标品 SKU 可以先预留菜单和表结构，等配置闭环跑通后再补完整 CRUD。

---

## 3. 模块边界

产品与配置模块解决：

```text
客户买什么
客户下单时看到什么
客户能选什么
答案来自哪里
选择答案后触发哪些规则
哪些选择因为尺寸、系列、物料或组件约束被禁用
```

产品与配置模块不解决：

```text
物料主数据维护
面料 SKU 维护
组件包明细维护
附件文件本体存储
工程扣减结果
生产 BOM 明细
最终价格和利润
```

边界规则：

```text
基础资料是可选项来源
产品配置是客户下单表达
工程配置模块是工程方案、构成项、可选范围、能力规则和带出规则
生产 BOM 明细、真实用量和扣减属于后续 ERP / 生产工艺
价格模块是金额计算
发布模块是把配置、工程配置、价格、资料打包成可下单版本
```

---

## 4. 数据库处理口径

项目未上线，没有客户数据。`sql/postgresql/product_capability.sql` 可以直接修改，开发库可以直接执行。

优先表：

```text
pc_sales_product
pc_standard_sku
pc_config_template
pc_config_template_version
pc_question_group
pc_config_question
pc_config_option
pc_config_rule
```

如果当前代码仍有旧表：

```text
pc_product_model
pc_sales_variant
```

处理建议：

```text
优先改成业务清晰的新表名
如果短期风险较高，可先用旧表承载新语义，但页面、接口、菜单、i18n 必须统一叫销售产品 / 标品 SKU
不要在 UI 上继续暴露 Product Model / Sales Variant 这种泛名称
```

表设计必须包含：

```text
tenant_id
业务主键 id
业务编码 code
中文名称 name_cn
英文名称 name_en
status
sort_order
remark
legacy_source / legacy_id 按需保留
create_by / create_time / update_by / update_time / del_flag
```

字段长度不要再用 `varchar(1)` 承载业务枚举。`status`、`source_type`、`business_type` 等枚举字段必须给足长度。

---

## 5. 历史菜单建议

本阶段只记录产品与配置内部页面的历史顺序。当前业务推进顺序必须以 `基础资料 -> 工程配置 -> 产品与配置 -> 价格发布` 为准。

```text
销售产品
配置模板
问题组模板
配置问题
配置答案
配置求值器
标品 SKU
```

可隐藏或后置：

```text
配置规则
模板版本
答案引用明细
```

隐藏规则：

```text
低频技术中间表不要直接暴露给普通录入人员
能从配置模板行内进入的子资料，不单独做一级菜单
能通过详情抽屉展示的引用关系，不做孤立维护入口
```

---

## 6. 后端包和分层

参考现有代码生成器和项目规范，不写 600 行大 Service。

建议包：

```text
com.bocoo.product.controller
com.bocoo.product.service
com.bocoo.product.service.impl
com.bocoo.product.mapper
com.bocoo.product.domain.entity
com.bocoo.product.domain.bo
com.bocoo.product.domain.vo
com.bocoo.product.engine
```

建议类：

```text
ProductConfigController
SalesProductController
ConfigTemplateController
ConfigQuestionController
ConfigOptionController
ConfigEvaluationController

SalesProductService
ConfigTemplateService
ConfigQuestionService
ConfigOptionService
ConfigEvaluationService

ConfigEvaluationEngine
ProductOptionSourceResolver
ProductConfigReferenceCheckService
```

拆分原则：

```text
普通 CRUD 一个 Service 不超过单一业务职责
规则求值放 Engine，不塞 Controller
引用检查独立 Service
附件绑定复用基础资料的 OSS / media 能力
状态变更独立 changeStatus 接口
```

---

## 7. 前端页面规范

普通维护页必须按系统 `角色管理` 这类标准 grid 做：

```text
app-container
查询区
toolbar + right-toolbar
el-table
pagination
右侧 drawer 新增 / 编辑 / 详情
双击行打开详情
```

必须遵守：

```text
选择列 + 序号列 + 业务列
操作列固定右侧
操作使用图标按钮 + tooltip
状态用 switch 或 tag，不裸显示 true/false/ENABLED
新增默认启用，启停在列表控制
查询条件不要堆满所有字段
名称类默认支持中英文合并检索
抽屉宽度不小于 80%，字段多时两列布局
字段多的页面分组展示，底部 sticky 操作栏
所有文案走 i18n key
```

特殊页面：

```text
配置模板工作台
配置求值器
```

可以自定义内容区，但仍不能重做后台 Layout、Sidebar、Navbar、TagsView。

---

## 8. 基础资料阶段踩坑清单

AMU 执行本阶段必须先读并遵守：

```text
docs/项目配置和代码风格/fullstack-code-standards.md
docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md
```

基础资料刚踩过的坑，产品与配置阶段不得重复：

### 8.1 菜单和页面

```text
高频功能必须拆独立菜单，不能藏在 tabs 里
菜单 SQL、数据库执行、permission.ts 动态路由映射必须同步
菜单图标缺失要补，不允许空图标
真实运行目录是 bocoo-admin/target/dist，build 后必须同步
```

### 8.2 Grid

```text
表格必须有序号列
长字段必须 show-overflow-tooltip
列宽要合理，允许拖动查看
操作列不能堆一排文字按钮
双击行打开详情
引用检查必须说明引用了什么，不能只给一个数字
```

### 8.3 表单和抽屉

```text
抽屉不能太窄
字段多用两列和分组
数字字段不需要无限宽
单位和数值组合展示
详情、编辑、新增要区分清楚
```

### 8.4 编码和字段

```text
业务主身份只保留一个主编码
外部编号可以保留，但不能和主编码混淆
不要同时暴露多个含义相近的编码搜索项
字段长度按真实枚举和值设计，不要 varchar(1)
允许必要冗余字段，Service 负责同步
```

### 8.5 字典和单位

```text
单位以 pc_unit 为权威，不使用系统字典 product_unit
普通字典只用于枚举分类，不承载业务单位
下拉必须按业务语义过滤
不要在厚度单位里出现件、套、平方米
不要在面料系列里搜索其他物料类型
```

### 8.6 附件和 OSS

```text
附件本体复用通用 OSS
业务页面直接上传和查看附件
不要要求用户先去资料资产再手工绑定
资料资产 / 资料绑定是底层台账，不默认暴露给普通录入人员
配置答案如果要展示图片、色卡、说明书，必须通过 media binding 取资料
```

### 8.7 i18n

```text
新增可见文案先写 i18n/locales/en_US.json
同步 zh_CN 后执行 pnpm i18n:validate
再执行 pnpm i18n:sync
不要手改 admin-ui/public/i18n/*.json
不要让页面显示 productCenter.xxx 这种裸 key
```

### 8.8 浏览器验证

```text
不能只 build
必须浏览器真实打开页面
检查菜单进入、刷新不 404、搜索、重置、分页、新增、编辑、详情、删除/停用
检查 console error 和接口错误
截图看布局是否挤压、遮挡、字段溢出
```

---

## 9. 配置答案来源设计

配置答案的来源必须结构化，不要只塞 JSON。

建议 `source_type`：

```text
MANUAL
BASE_ATTRIBUTE
MATERIAL
FABRIC_PROFILE
FABRIC_SERIES
COMPONENT
MEDIA_ASSET
```

建议字段：

```text
source_type
source_ref_id
source_code
source_name
display_name_cn
display_name_en
value_code
sort_order
is_default
status
rule_json
```

说明：

```text
source_ref_id 用于真实关联
source_code / source_name 允许冗余，便于列表、试算、发布快照
display_name_cn / display_name_en 是客户看到的答案名称
rule_json 只放轻量显示 / 禁用规则，不放工程配置和价格主逻辑
```

---

## 10. 配置求值器最小闭环

最小输入：

```text
salesProductId
templateVersionId
width
height
selectedOptions
```

最小输出：

```text
visibleQuestions
availableOptions
disabledOptions
validations
autoComponents
mediaAssets
warnings
blockers
```

返回要求：

```text
每个 disabledOption 必须带 disabledReason
每个 warning / blocker 必须带 code、message、targetType、targetId
mediaAssets 必须来自 OSS / pc_media_asset / pc_media_binding
autoComponents 只返回配置答案带出的轻量提示，不替代工程配置
```

---

## 11. 测试数据要求

本阶段至少配置出 5 个可用于后续试算的产品样本：

```text
1 个卷帘基础款
1 个卷帘户外款
1 个斑马帘双层款
1 个带电机控制的产品
1 个含附件 / 色卡 / 说明资料的产品
```

每个样本至少包含：

```text
销售产品
配置模板
问题组
3 个以上配置问题
每个问题至少 2 个答案
至少 1 个答案引用组件包
至少 1 个答案引用基础字典
至少 1 个答案引用附件资料
```

---

## 12. 验收标准

开发完成后必须满足：

```text
数据库脚本可重复执行到开发库
菜单能进入
刷新页面不 404
CRUD 真实读写数据库
状态启停可用
详情抽屉可用
引用检查可解释
配置求值器返回后端真实计算结果
至少 5 个测试产品能完成配置求值
pnpm --dir admin-ui build 通过
前端 dist 已同步 bocoo-admin/target/dist
浏览器真实验证通过
```

---

## 13. 历史 AMU 口令

下面口令只作为历史记录，不建议继续原样执行。下一阶段请使用 `11-基础信息与工程配置改进计划.md`。

```text
/plan 产品配置中心产品与配置模块第一阶段重构。

参考文档：
1. docs/产品配置中心重构/实施拆分/10-产品与配置模块AMU实施计划.md
2. docs/产品配置中心重构/实施拆分/03-产品与配置模块.md
3. docs/产品配置中心重构/实施拆分/07-现有实现对比与改造清单.md
4. docs/项目配置和代码风格/fullstack-code-standards.md

目标：
完成销售产品、配置模板、问题组、配置问题、配置答案、配置求值器的第一阶段闭环。基础资料阶段踩过的菜单、grid、抽屉、字典、单位、OSS、i18n、真实 dist、浏览器验证问题全部按 10 号文档规避。

要求：
先出数据库、后端、前端、测试数据、浏览器验证的 Wave 计划。本历史口令已被 11 号基础信息与工程配置改进计划取代，不建议继续原样执行。
```
