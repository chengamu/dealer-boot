# 前后端代码规范

生成日期：2026-06-07

本文记录当前项目的前端、后端、代码生成器和特殊页面落地规范。普通管理功能必须优先遵守本文；工作台、配置编辑器、价格矩阵、发布闸门、销售只读总览等特殊页面可以做自定义布局，但不能脱离现有后台框架、权限、i18n、UTC 和接口体系。

关联文档：

- [frontend-i18n-utc-rules.md](./frontend-i18n-utc-rules.md)
- [frontend-function-alignment-a11y.md](./frontend-function-alignment-a11y.md)
- [产品能力界面设计稿.md](../产品配置中心/产品能力界面设计稿.md)
- [共享产品能力中心API与后端实现约束.md](../产品配置中心/共享产品能力中心API与后端实现约束.md)

## 1. 总原则

- 普通 CRUD/grid 功能优先使用代码生成器风格，不从零手写一套页面和分层。
- 新功能必须接入现有菜单、按钮权限、动态路由、请求封装、i18n、UTC、审计和日志体系。
- 菜单和按钮权限必须先落数据库，再接前端和后端；不能只靠前端路由或临时代码兜底。
- 业务聚合、规则求值、发布、价格、BOM、快照可以手写 Service/Engine，但 Controller 不写复杂业务逻辑。
- 设计稿和效果图只作为内容区布局参考，不重做 Layout、Sidebar、Navbar、TagsView、登录态和权限框架。
- H5 原型和静态 mock 只作为业务数据和复杂度参考，不作为生产视觉和代码结构依据。

## 2. 前端标准页面

普通维护页必须对齐系统管理下 `角色管理`、`菜单管理`、`参数配置` 等标准页面。

标准结构：

```text
app-container
  -> el-form inline 查询区
  -> el-row toolbar + right-toolbar
  -> el-table / tree-table
  -> pagination
  -> el-drawer 表单，短操作可用 el-dialog
```

标准要求：

- 搜索区使用 `el-form`、`inline`、合理 `label-width`，默认不超过两行。
- 工具栏左侧放新增、修改、删除、导出、审核等业务按钮，右侧使用 `right-toolbar`。
- 表格使用 `el-table`，批量操作需要选择列，长文本使用 `show-overflow-tooltip`。
- 分页使用项目现有 `pagination` 组件。
- 新增、编辑、详情、引用检查、复杂预览默认用右侧 `el-drawer`，保留列表上下文。
- 删除确认、启停确认、审核通过/拒绝原因、密码重置、导入选择、图片预览、批量粘贴这类短操作可用 `el-dialog` / `ElMessageBox`。
- 抽屉表单必须有清晰分组和底部 sticky 操作栏，避免提交按钮被长表单滚走。
- 按钮必须使用 `v-hasPermi`，权限码要和 `sys_menu.perms`、后端 `@SaCheckPermission` 一致。
- 时间展示使用 `formatUtc()`；日期范围提交使用 `withUtcDateRange()` / `withUtcDateRangeParams()`。
- 可见文案使用 i18n key，不在 Vue 模板里新增硬编码中文或英文。

不符合标准的常见问题：

- 把高频主功能藏在页面 tabs 里。
- 自己写卡片式 CRUD，缺少标准 toolbar、选择列、分页和 `right-toolbar`。
- 前端有导出/审核按钮，但数据库菜单和后端权限没有同步。
- 页面刷新后依赖前端兜底路由，而不是稳定菜单 `component` 和动态路由映射。

### 2.1 标准 grid 细则

普通 CRUD/grid 页面必须参考 `系统管理 / 角色管理` 的交互密度和表格形态；产品配置中心的基础资料、物料、面料、组件、单位、字典等主数据页也按本节执行。

- 高频主模块必须拆成独立菜单和独立路由，例如“面料系列”和“面料资料”不能只靠页面 tab 作为正式入口；页面内 tab 只允许承载局部细节或低频辅助视图。
- 动态路由必须由数据库 `sys_menu.path`、`component` 和前端 `permission.ts` 映射共同保证；刷新目标页面不能 404。
- 标准表格前置列顺序：选择列、序号列、业务列；序号按分页偏移显示 `1, 2, 3...`，不能让用户自己数行。
- `el-table` 默认使用 `border`，业务列设置合理 `min-width`，长文本使用 `show-overflow-tooltip`，允许用户拖动列宽查看完整内容。
- 业务状态字段使用开关或明确状态标签，不在表格里裸显示 `true/false`、`1/0`、`ENABLED/DISABLED`。可直接启停的字段优先用 `el-switch` 调后端 `changeStatus` 接口。
- `Boolean` 业务字段显示为“是/否”，表单里用 `el-switch`；不要用普通输入框或暴露 `true/false` 字符串。
- 新增/编辑表单不默认让用户手工选“启用/停用”；新记录默认启用，列表启停由开关控制。确有审批/草稿流的页面除外，但状态值必须由业务流转控制。
- 操作列固定在右侧，默认使用图标按钮 + `el-tooltip` + `aria-label`，避免“引用检查 / 修改 / 删除 / 管理明细”一整排文字挤压表格。
- 常用行操作顺序建议：引用检查、详情、修改、删除、管理子资料；危险操作必须有确认。
- 查询区默认不超过两行，字段多时优先收敛搜索项，不把所有表格列都塞进查询区。
- 查询字段必须按业务检索习惯收敛。名称类字段默认支持中英文合并检索，不要同时堆多个低价值编号搜索；面料资料这类 SKU 页优先保留主编码、名称、系列、样册编号、状态等高频条件。
- 普通 grid 必须支持双击行打开详情抽屉；如果操作列空间不足，详情入口可以只保留双击和图标按钮，不再增加文字按钮。
- 产品配置中心单位下拉必须以业务单位表 `pc_unit` 为权威，不能使用系统字典 `sys_dict_data.product_unit`；系统字典只用于物料类型、业务类型等枚举分类值。
- 字典和单位下拉必须按业务语义过滤，例如厚度单位只能出现 `MM/CM/IN` 这类厚度/长度单位，不能混入“件、套、平方米”等无关单位。
- 菜单、表头、按钮、placeholder、tooltip、空状态和错误提示都走 i18n key；新增 key 写入 `i18n/locales/en_US.json` 后执行 `pnpm i18n:validate` 和 `pnpm i18n:sync`。
- 本地验证不仅要跑 `pnpm --dir admin-ui build`，还要在真实运行目录 `bocoo-admin/target/dist` 或当前开发服务里刷新页面做浏览器冒烟检查。

### 2.1.1 产品配置中心基础资料页面细则

产品配置中心基础资料是给业务录入人员长期维护的主数据入口，不能把技术中间表直接暴露成一排菜单。

基础资料可见菜单顺序固定为：

```text
录入向导
单位管理
配置字典
物料管理
面料系列
面料资料
组件包
```

默认隐藏的内部能力：

```text
物料属性
组件明细
资料资产
资料绑定
```

隐藏能力的处理口径：

- `物料属性` 是物料的扩展属性能力，默认作为物料详情或高级维护能力，不作为普通录入人员一级入口。
- `组件明细` 从 `组件包` 的行内入口维护，不单独暴露给普通录入人员。
- `录入向导` 是基础资料第一入口，用于提示录入顺序、缺失主数据和快捷跳转，不承载新的业务存储表。
- `资料资产` 和 `资料绑定` 是附件台账和关联台账，不要求业务人员单独维护。
- 页面需要附件时，必须在物料、面料、组件包等业务对象抽屉里直接上传和管理附件，底层复用 OSS、`pc_media_asset`、`pc_media_binding`。

基础资料抽屉要求：

- 抽屉宽度默认不小于页面宽度的 80%，字段多的页面建议 84% 左右。
- 表单按两列排版，长文本、附件、备注、规则 JSON 等字段可以独占整行。
- 数字类字段控件不要占满无意义的大宽度，和单位字段组合展示时要优先降低录入负担。
- 详情、编辑、新增共用字段布局，但详情必须只读，不能让用户误编辑。
- 附件上传区必须能看到文件名、类型、用途、预览或下载入口；上传本体走通用 OSS，不新增产品配置中心专用文件存储。

基础资料字段口径：

- 主身份只保留一个业务主编码，例如物料和面料都以 `material_code` / `fabric_code` 这类主编码为准；样册编号、供应商料号、供应商编码属于外部业务关联编号，可以保留但不能替代主编码。
- 面料系列是规则承载层，厚度限制、门幅限制、适用规则优先放在系列上；具体面料是真实 SKU，主要承载颜色、材质、纹理、门幅、厚度、克重、样册编号、供应商信息和附件。
- 面料资料查询区不要同时暴露“面料编码”和“物料编码”造成身份混乱；列表可展示必要冗余字段，但录入逻辑必须明确一个主编码。
- 颜色名称不是高价值搜索条件时不要默认放查询区；供应商料号仅在业务明确需要按供应商反查时保留。
- 面料图片、色卡、规格书等资料必须直接绑定在面料资料上，不能要求用户先去资料资产页面再手动绑定。

### 2.2 弹窗和抽屉分工

所有管理端页面、历史页面和代码生成器新产物必须按下列规则实现；历史页面不豁免，不作为例外模板继续复制：

| 场景 | 推荐形态 | 原因 |
| --- | --- | --- |
| 新增 / 编辑普通主数据 | 右侧抽屉 | 字段可能扩展，用户可保留列表上下文连续处理 |
| 详情 / 审核详情 / 引用检查 / 影响分析 | 右侧抽屉 | 信息密度高，需要对照当前列表 |
| 删除、启停、强退、清空、同步确认 | `ElMessageBox.confirm` | 短确认，不承载复杂表单 |
| 审核通过 / 拒绝原因、密码重置、少量输入 | 小弹窗 / `ElMessageBox.prompt` | 字段少，操作闭环短 |
| 图片预览、导入表选择、批量粘贴预览 | 小弹窗允许 | 属于临时预览或轻量批处理，不承载长表单 |
| 配置编辑器、价格矩阵、发布闸门 | 自定义页面内容区 | 不是普通 CRUD，不塞进弹窗 |

P0 不符合项和处理口径：

| 范围 | 当前问题 | P0 处理 |
| --- | --- | --- |
| 代码生成器 `index.vue.vm` / `index-tree.vue.vm` | 仍固定生成新增/编辑 `el-dialog` | 新生成页面必须改为右侧抽屉，短操作继续弹窗 |
| `admin-ui/src/pages/system/user/UserPage.vue`、`admin-ui/src/pages/merchant/MerchantUserPage.vue` | 用户新增/编辑字段多，仍使用大弹窗 | P0 改为右侧抽屉，导入、密码重置等短操作保留弹窗 |
| `admin-ui/src/pages/system/MenuPage.vue` | 菜单树、权限标识、路由组件等字段多，弹窗承载偏重 | P0 改为右侧抽屉，保留树形列表上下文 |
| `admin-ui/src/pages/system/RolePage.vue` | 角色表单和数据权限树仍在大弹窗中处理 | P0 改为右侧抽屉，数据权限作为抽屉内分组或独立抽屉 |
| `admin-ui/src/pages/system/NoticePage.vue` | 公告内容编辑较长，弹窗容易过高 | P0 改为右侧抽屉或独立编辑内容区 |
| `admin-ui/src/pages/system/OssConfigPage.vue` | OSS 配置字段多，弹窗不利于分组 | P0 改为右侧抽屉 |
| `admin-ui/src/pages/monitor/OperationLogPage.vue` | 详情查看仍是弹窗 | P0 改为详情抽屉 |
| `admin-ui/src/pages/system/DictTypePage.vue`、`DictDataPage.vue`、`ConfigPage.vue`、`PostPage.vue`、`DeptPage.vue` | 基础维护仍使用旧弹窗 | P0 统一改为右侧抽屉，确认类操作继续 `ElMessageBox` |
| 产品配置中心普通维护页 | 第一版如果用生成器旧弹窗，会和录入体验目标冲突 | P0 必须使用抽屉，不允许沿用旧弹窗模板 |

## 3. 自定义页面例外

下列页面允许自定义内容区布局：

| 页面类型 | 允许自定义原因 | 仍需遵守 |
| --- | --- | --- |
| 工作台 / Dashboard | 聚合任务、缺口、进度、同步状态 | 菜单、权限、请求、i18n、UTC |
| 配置模板录入工作台 | 三栏编辑、规则检查、预览、BOM 同屏 | 不重做后台外壳，保存和求值走后端 |
| 价格中心工作台 | 价格矩阵、规则项、试算器同屏 | 最终价格必须来自后端引擎 |
| 测试发布 / 发布闸门 | PASS/WARNING/BLOCKER 状态矩阵 | 发布动作走权限、日志、后端检查 |
| 销售只读总览 | 面向销售快速理解发布能力 | 只读，不展示内部编辑字段 |

自定义页面约束：

- 只能自定义 `AppMain` 内容区。
- 侧边栏最多两级菜单，效果图中的三级业务树要收敛为页面内面板、tabs、drawer 或标准二级菜单。
- 特殊页面内部可以使用 tabs，但高频主数据、独立搜索对象、独立授权对象必须拆成菜单。
- 状态、价格、发布、快照等权威结果必须来自后端 API，前端只做展示和轻量预览。

## 4. 前端工程规范

- 页面目录优先放在 `admin-ui/src/pages/<domain>/`。
- API 封装放在 `admin-ui/src/api/<domain>/`，不要在页面里直接写 URL。
- 动态路由组件必须补到 `admin-ui/src/stores/permission.ts` 的映射中。
- 图标优先使用项目已有 `SvgIcon` 或 Element Plus 图标，不手写零散 SVG 路径。
- 工作台、发布状态、价格命中等需要图表增强时，可以使用项目已有 `echarts`，但必须按需引入并封装成组件；不得新增重复图表库，不得把前端图表计算结果作为报价、发布、审核等业务权威。
- 页面 SFC 只负责路由级编排、查询条件、数据加载和动作分发；复杂业务区块必须优先复用公共组件，没有现成组件时再按领域沉淀新组件，禁止把整页模板、样式和交互逻辑全部堆在一个 Vue 文件里。
- 通用组件优先放到 `admin-ui/src/components/` 或现有公共组件目录；只服务单一业务域的组件放到 `admin-ui/src/pages/<domain>/components/`，被两个以上页面复用时再上提为公共组件。
- 组件抽取边界以“可独立理解、可独立测试、可复用或明显降低页面复杂度”为准，不为了拆而拆；简单字段和一次性轻量布局可以留在页面内。
- 所有下拉选择默认必须支持模糊过滤；字典项少的使用 `filterable`，物料、辅材、产品、用户、角色、问题组、价格方案等数据量可能增长的选择使用远程搜索、分页或弹出选择器，不能让用户在长下拉里硬找。
- 新增文案先写 `i18n/locales/en_US.json`，再运行 i18n 同步脚本生成 runtime JSON。
- `admin-ui/public/i18n/*.json` 是生成物，不手动维护。
- 新增页面需要至少做一次浏览器验证：菜单可进入、刷新不 404、搜索/重置/分页/按钮权限可见性正常。

## 5. 后端标准分层

普通 grid 后端以 `bocoo-modules-generator/src/main/resources/vm/java/` 为基线，healthbrain 模块可作为历史标准样例，但当前项目的 `BaseBo`、JSON i18n、PostgreSQL、UTC 优化优先级更高。

标准结构：

```text
controller/
service/
service/impl/
mapper/
domain/entity/
domain/bo/
domain/vo/
resources/mapper/
```

标准职责：

| 层 | 职责 |
| --- | --- |
| Controller | 接收入参、权限、日志、校验、调用 Service、返回 `R` / `TableDataInfo` / Excel |
| Service 接口 | 定义 `queryPageList`、`queryList`、`insertByBo`、`updateByBo`、`deleteWithValidByIds` |
| ServiceImpl | 事务、查询条件、转换、保存校验、状态变更、调用 Engine |
| Mapper | `BaseMapperPlus<Entity, Vo>`，复杂查询可补 XML |
| Entity | 单表映射，继承 `BaseEntity` |
| BO | 请求对象，继承 `BaseBo`，包含校验注解和查询参数 |
| VO | 返回对象，允许冗余展示字段和 Excel 注解 |

Controller 标准接口：

```text
GET    /xxx/list       -> TableDataInfo<Vo>
POST   /xxx/export     -> ExcelUtil.exportExcel(...)
GET    /xxx/{id}       -> R<Vo>
POST   /xxx            -> R<Void>
PUT    /xxx            -> R<Void>
DELETE /xxx/{ids}      -> R<Void>
PUT    /xxx/changeStatus/{id}/{status} -> R<Void>
```

## 6. 后端约束

- Controller 不直接操作 Mapper，不拼快照 JSON，不写规则求值、价格计算、发布检查。
- 新增、修改、删除、导出、审核、发布、状态切换必须有 `@SaCheckPermission`；重要操作补 `@Log`。
- 前端按钮权限、后端权限注解、`sys_menu.perms` 必须一致。
- 后端错误消息优先使用 message key / JSON i18n，不新增 `messages*.properties`。
- 创建业务时间使用 `TimeUtils.utcNow()`，禁止直接 `LocalDateTime.now()`。
- PostgreSQL 是主数据库；新增 SQL 使用 PG 语法和 `timestamptz`，不补 MySQL 新脚本。
- 导出接口使用 `ExcelUtil.exportExcel(...)`，前端导出按钮必须有对应后端接口和菜单按钮权限。
- 状态变更必须按真实主键字段生成或手写，不允许硬编码 `getId`。

## 7. 数据库菜单和按钮权限规范

菜单、按钮、路由、权限必须以数据库 `sys_menu` 为权威，不允许靠前端硬编码兜底长期运行。

### 7.1 sys_menu 类型

| 类型 | `menu_type` | 用途 | 要求 |
| --- | --- | --- | --- |
| 目录 | `M` | 一级菜单分组 | `component` 通常为空，`path` 是分组路径 |
| 菜单 | `C` | 可进入的二级页面 | 必须有稳定 `path`、`component`、`perms` |
| 按钮 | `F` | 页面内操作权限 | `path='#'`，`component=''`，必须挂父菜单 |

普通页面必须通过 `C` 菜单进入，不能只写前端静态路由。页面内新增、修改、删除、导出、审核、发布、重试等动作必须建 `F` 按钮权限。

### 7.2 权限码命名

普通 grid 权限建议：

```text
<module>:<business>:list
<module>:<business>:query
<module>:<business>:add
<module>:<business>:edit
<module>:<business>:remove
<module>:<business>:export
```

特殊动作单独命名：

```text
<module>:<business>:approve
<module>:<business>:reject
<module>:<business>:publish
<module>:<business>:check
<module>:<business>:retrySync
<module>:<business>:test
<module>:<business>:bind
<module>:<business>:upload
```

不要用一个泛化 `audit` 同时代表通过和拒绝；如果页面上是两个独立按钮，数据库也要拆成两个按钮权限，后端接口也要拆对应权限。

### 7.3 三方一致

每个按钮必须同时满足：

| 位置 | 示例 |
| --- | --- |
| 数据库 | `sys_menu.perms = 'system:tenant:application:approve'` |
| 前端 | `v-hasPermi="['system:tenant:application:approve']"` |
| 后端 | `@SaCheckPermission("system:tenant:application:approve")` |

缺一不可：

- 数据库没有按钮：角色无法授权，非超级管理员看不到按钮。
- 前端权限没写：按钮可能越权显示。
- 后端权限没写：接口可能被越权调用。
- 三方权限码不一致：页面显示、接口调用和角色授权会互相打架。

### 7.4 菜单路径和刷新

- 后端 `sys_menu.path`、`component` 必须和前端动态路由映射一致。
- 前端 `permission.ts` 的组件映射必须覆盖后端 `component`。
- 刷新页面不能 404；不能依赖临时 alias 或前端兜底长期解决菜单路径错误。
- 菜单迁移时要同步历史路径兼容方案，但正式入口必须以数据库菜单为准。

### 7.5 i18n 和排序

- 菜单和按钮建议写 `i18n_key`，显示名由 i18n JSON 提供。
- 新增菜单 i18n key 要进入 `i18n/locales/en_US.json`，再同步到运行时 JSON。
- 同一父菜单下 `order_num` 要稳定，按钮顺序通常为 query、add、edit、remove、export，再放业务动作。
- 侧边栏层级最多两级；高频主功能独立成二级菜单，不藏在页面 tabs。

### 7.6 角色授权和落库校验

菜单 SQL 或数据补丁必须处理：

- `sys_menu` 目录、菜单、按钮。
- 管理员或目标角色的 `sys_role_menu` 授权。
- 已存在菜单的 `ON CONFLICT` / upsert 或等价幂等更新。
- `tenant_id`、`visible`、`status`、`is_frame`、`is_cache` 等字段符合当前项目口径。

落库后至少检查：

```sql
select menu_id, menu_name, parent_id, order_num, menu_type, path, component, perms
from sys_menu
where perms like '<module>:<business>:%'
order by parent_id, order_num;
```

还要在页面验证：

- 普通用户无按钮权限时不显示按钮。
- 有按钮权限时按钮显示，接口不 403。
- 页面刷新不 404。
- 菜单管理、角色授权里能看到对应按钮。

## 8. 代码生成器使用规则

适合先生成再局部增强：

- 基础资料、字典式主数据、普通列表、导入批次、行级问题、同步日志、发布包列表、缺口待办。

不适合只靠生成器：

- 工作台、配置编辑器、价格矩阵、价格试算、发布检查、发布包生成、快照构建、销售只读聚合。

生成后必须检查：

- 前端列表是否是标准 `app-container + 查询区 + toolbar + table + pagination`。
- 新增、编辑、详情是否按本文 2.1 使用右侧抽屉；不要继续使用旧模板里的大弹窗承载长表单。
- 后端是否生成 `/export` 且权限和前端按钮一致。
- `changeStatus` 是否使用真实主键字段。
- 菜单 SQL 是否包含 list/query/add/edit/remove/export 对应按钮。
- 新增文案是否补 i18n。

## 9. 设计文档冲突扫描结论

截至 2026-06-07，产品配置中心相关文档大方向和当前标准不冲突，已明确：

- 普通维护页优先标准 grid/list。
- 只有 5 类复杂页面允许自定义内容区。
- 正式菜单按三个一级菜单和二级入口组织，不把高频功能藏在 tabs。
- 后端普通 CRUD 参考代码生成器，复杂能力进入 Service/Engine。
- 发布、价格、配置求值、快照以后端为权威。

后续评审重点：

- 新增设计稿如果出现独立侧边栏、独立顶栏、三级菜单，需要收敛回现有框架。
- 新功能如果前端有按钮，必须同步数据库菜单和后端权限。
- 新 API 如果返回 Entity、裸 Map 或由 Controller 拼复杂 JSON，需要退回重设分层。
- 自定义页面可以突破 grid 视觉，但不能突破权限、i18n、UTC、请求和菜单机制。

## 10. 验证清单

前端：

- `npm run typecheck`
- 浏览器验证菜单进入、刷新、搜索、重置、分页、按钮权限、控制台错误。

后端：

- `mvn -pl <module> -am -DskipTests compile`
- 权限码三方一致：Vue `v-hasPermi`、Java `@SaCheckPermission`、`sys_menu.perms`。

文档：

- 设计文档引用本文。
- 特殊页面必须注明为什么不走标准 grid，以及仍遵守哪些项目级约束。
