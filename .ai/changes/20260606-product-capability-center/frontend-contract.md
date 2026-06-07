# Frontend Contract: 共享产品能力中心 PCC-03

## 1. 菜单和路由 contract

前端正式实现必须进入现有 `admin-ui` 布局、`sys_menu` 动态路由、按钮权限、TagsView、面包屑和请求体系，不生成 H5，不改 `layout`、`sidebar`、`topbar`。

菜单显示名建议：

```text
产品能力
  工作台
  基础信息
  产品模型
  配置模板
  组件 / 资料
  价格中心
  测试发布
  销售只读总览
```

约束：

- 菜单最多两级。一级固定为“产品能力”，二级直接放业务入口。
- 路由统一使用 `/product-center/*`，这是前端技术路径，不等于 API 前缀。
- 不新增侧边栏三级菜单。价格规则、发布历史、问题组树、资料详情、版本历史等细分入口全部放页面内 tabs、panel、drawer、dialog 或局部导航。
- 后端 `sys_menu.component` 后续需要映射到 `admin-ui/src/stores/permission.ts` 的 `migratedViewModules`，但本任务不修改该文件。

建议菜单、路由、页面和权限：

| 二级菜单 | 路由 | 页面类型 | 建议组件 | 建议基础权限 |
| --- | --- | --- | --- | --- |
| 工作台 | `/product-center/workbench` | 自定义只读 dashboard | `workbench/ProductCenterWorkbenchPage.vue` | `product:center:view` |
| 基础信息 | `/product-center/base` | 普通 grid/list + drawer | `base/ProductBaseInfoPage.vue` | `product:base:list` |
| 产品模型 | `/product-center/model` | 普通 grid/list + drawer | `model/ProductModelPage.vue` | `product:model:list` |
| 配置模板 | `/product-center/template` | 自定义配置录入工作台 | `template/ConfigTemplateWorkbenchPage.vue` | `product:template:list` |
| 组件 / 资料 | `/product-center/assets` | 普通 grid/list + 图片预览 + 绑定抽屉 | `assets/ProductAssetPage.vue` | `product:asset:list` |
| 价格中心 | `/product-center/pricing` | 列表 + 自定义价格编辑与测试区域 | `pricing/PriceCenterPage.vue` | `product:price:list` |
| 测试发布 | `/product-center/publish` | 自定义发布闸门 | `publish/ProductPublishPage.vue` | `product:publish:check` |
| 销售只读总览 | `/product-center/sales-view` | 自定义只读总览 | `sales-view/ProductSalesViewPage.vue` | `product:sales:view` |

按钮权限建议继续细分到动作级：

- 基础信息：`product:base:add`、`product:base:edit`、`product:base:remove`、`product:base:export`、`product:base:reference`
- 产品模型：`product:model:add`、`product:model:edit`、`product:model:publishCheck`、`product:model:clone`
- 配置模板：`product:template:edit`、`product:template:rule`、`product:template:test`、`product:template:submit`
- 组件 / 资料：`product:asset:upload`、`product:asset:bind`、`product:asset:preview`、`product:asset:reference`
- 价格中心：`product:price:edit`、`product:price:test`、`product:price:publish`、`product:price:import`
- 测试发布：`product:publish:approve`、`product:publish:publish`、`product:publish:retrySync`
- 销售只读总览：原则上只有 view、quote、download、copy link，不出现维护权限按钮。

## 2. Batch 1 页面目录和组件建议

Batch 1 只做基础闭环前端：菜单、工作台只读骨架、基础信息、产品模型、组件资料。不进入配置录入、价格编辑测试、正式发布和销售只读实现。

建议页面目录：

```text
admin-ui/src/pages/product-center/
  workbench/
    ProductCenterWorkbenchPage.vue
    components/
      WorkbenchHeader.vue
      TaskFlowCard.vue
      ProductProgressTable.vue
      PriorityQueuePanel.vue
      QuickActionGrid.vue
      RecentSyncEvents.vue
  base/
    ProductBaseInfoPage.vue
    components/
      ProductCategoryTreePanel.vue
      MaterialGrid.vue
      MaterialDetailDrawer.vue
      ReferenceCheckDrawer.vue
  model/
    ProductModelPage.vue
    components/
      ProductModelGrid.vue
      ProductModelDrawer.vue
      ProductVariantTable.vue
      ProductModelStatusBadges.vue
  assets/
    ProductAssetPage.vue
    components/
      AssetGrid.vue
      AssetPreviewDrawer.vue
      AssetBindingDrawer.vue
      BatchAssetBindingDialog.vue
  components/
    ProductStatusTag.vue
    ProductCompletenessBar.vue
    ProductPermissionButton.vue
    ProductEmptyState.vue
```

建议 API 文件路径：

```text
admin-ui/src/api/product-capability/
  workbench.ts
  base.ts
  model.ts
  asset.ts
  types.ts
```

API 调用边界：

- 前端 route 使用 `/product-center/*`。
- 后端 API 前缀建议继续使用需求中的 `/product-capability/*`。
- 分页列表按后端 `TableDataInfo<XxxVo>` contract 对接；单条、保存、操作按 `R<T>` 对接。
- 前端 API 类型只表达请求/响应 contract，不复制后端 Entity，不把规则求值、价格计算、发布检查逻辑写到前端。

Batch 1 页面职责：

| 页面 | Batch 1 范围 | 不做 |
| --- | --- | --- |
| 工作台 | 只读 summary、缺口队列、配置进度、同步事件骨架；可先接空数据或 summary API | 不实时 join 多张编辑表，不做配置编辑 |
| 基础信息 | 分类、物料、组件、资料资产的维护型入口；搜索、表格、抽屉、引用检查入口 | 不做复杂配置求值，不在基础页承载完整产品表单 |
| 产品模型 | 产品模型、销售变体、状态、引用摘要；详情抽屉 | 不做问题/答案录入，不做发布包生成 |
| 组件 / 资料 | 资料资产列表、图片预览、绑定对象、批量绑定入口 | 不替代 OSS 文件管理，不直接展示内部资料给销售 |

## 3. 普通 grid 页面和 5 个自定义页面边界

普通 grid/list 页面优先使用现有后台范式：

```text
app-container
  -> el-form inline search
  -> toolbar + v-hasPermi buttons
  -> el-table / tree-table / image column
  -> Pagination
  -> el-drawer / el-dialog
```

普通 grid/list 页面包括：

- 基础信息：产品分类、物料、组件库、资料资产、资料绑定、引用检查。
- 产品模型：模型列表、销售变体、状态和引用摘要。
- 组件 / 资料：资料列表、图片预览、绑定抽屉、批量绑定 dialog。
- 价格中心的方案列表、规则项列表、发布包列表、缺口待办、同步日志等维护型子区域。

5 个自定义页面固定为：

| 自定义页面 | 路由 | 自定义原因 | 权威数据来源 |
| --- | --- | --- | --- |
| 工作台 | `/product-center/workbench` | 聚合缺口、草稿、发布状态、同步状态 | read model、summary API、outbox 摘要 |
| 配置录入工作台 | `/product-center/template` | 三栏编辑、批量粘贴、组件/资料绑定、求值预览 | 后端 `evaluate/buildSnapshot` 结果 |
| 价格编辑与测试 | `/product-center/pricing` | 价格矩阵、规则项、试算器、命中明细 | 后端 `PriceCalculationEngine` |
| 测试发布 | `/product-center/publish` | PASS/WARNING/BLOCKER/PENDING 闸门、发布包预览、审批动作 | 后端发布检查和发布包 |
| 销售只读总览 | `/product-center/sales-view` | 面向销售的只读摘要、资料、限制、版本历史 | `pc_published_package` 或 read model |

边界要求：

- 自定义页面只自定义内容区，不重做顶部导航、侧栏、TagsView。
- 能用 Element Plus 的表单、表格、标签、抽屉、对话框、分页、图片预览就优先复用。
- 自定义组件只放业务密度高的区域，例如 `PriceMatrixEditor`、`LiveCheckPanel`、`PublishPackagePreview`。
- 配置、价格、发布都以后端 engine / Service 为准，前端只展示、收集输入和发起动作。

## 4. i18n key 命名建议

新增可见文案只写 `i18n/locales/en_US.json`。不要手动修改 `admin-ui/public/i18n/*.json`，这些文件由 `i18n:sync` 生成。

建议使用 `productCapability` 作为命名空间：

```json
{
  "productCapability": {
    "menu": {
      "root": "Product capability",
      "workbench": "Workbench",
      "base": "Base information",
      "model": "Product models",
      "template": "Configuration templates",
      "assets": "Components / assets",
      "pricing": "Pricing center",
      "publish": "Test and publish",
      "salesView": "Sales read-only view"
    },
    "common": {
      "status": "Status",
      "owner": "Owner",
      "updatedAt": "Updated at",
      "emptyTitle": "No product capability data yet",
      "loading": "Loading product capability data",
      "retry": "Retry"
    },
    "workbench": {},
    "base": {},
    "model": {},
    "asset": {},
    "template": {},
    "pricing": {},
    "publish": {},
    "salesView": {}
  }
}
```

命名规则：

- 菜单和路由 meta title 使用 `productCapability.menu.*`。
- 页面通用字段放 `productCapability.common.*`。
- 页面内按钮、placeholder、toast、empty、error、aria-label、title 分别放到对应页面命名空间。
- 状态、类型、单位优先使用字典或标准数据；不要为了 `DRAFT`、`PUBLISHED` 等业务状态硬编码 UI 文案。
- 产品名、答案名、客户说明、销售展示名是业务多语言字段，不属于 UI i18n key。

## 5. UTC、权限和状态要求

UTC：

- 所有后端 UTC 时间展示使用 `formatUtc()` 或项目已有兼容工具。
- 日期范围查询使用 `withUtcDateRange()` / `withUtcDateRangeParams()`；提交绝对时间使用 `toUtcPayload()`。
- 页面文案不要写死 `UTC+8`。中国用户和美国用户都按浏览器本地时区展示，传输保持 UTC 语义。
- `createdAt`、`updatedAt`、`effectiveAt`、`publishedAt`、`lastSyncAt`、`checkedAt` 等字段必须统一格式化。

权限：

- 所有新增、编辑、删除、导出、上传、绑定、测试、审批、发布、重试同步等按钮都必须使用 `v-hasPermi`。
- 快捷操作、行内操作、dropdown item 也要挂权限；不能只在页面入口做权限。
- 发布按钮在存在 BLOCKER 时禁用；存在 WARNING 时必须二次确认；无权限时不展示或禁用并给出一致提示。
- 销售只读页不能出现保存草稿、新增、编辑、删除、上传、发布等维护动作。

加载态、空态、错误态：

- 列表页：初始加载显示 Element Plus loading；空数据使用统一空态文案和主动作；接口错误显示可重试提示。
- 工作台：每个数据区块独立 loading / empty / error，避免一个 summary 失败导致整页不可用。
- 抽屉和 dialog：打开后按对象 ID 拉取详情，加载期间禁用提交按钮；关闭时清理当前对象状态。
- 异步请求要避免旧响应覆盖新查询结果；高风险页面建议使用 request id 或取消策略处理快速筛选切换。
- 表格批量操作后必须刷新列表或局部更新，并清空不再有效的 selection。

## 6. Batch 2/3 前端 TODO

Batch 2：配置录入工作台。

- 实现 `/product-center/template` 三栏工作区：左侧产品/模板/问题组导航，中间问题和答案表格编辑，右侧检查、预览、BOM、AI 建议面板。
- 批量粘贴必须先解析预览，再确认写入；失败行要展示原因。
- 组件绑定和资料绑定使用 drawer，不跳新页面。
- 求值预览、BOM 预览和快照预览必须调用后端，前端不得复制规则求值逻辑。
- 保存只保存草稿；发布必须走测试发布页面和后端发布检查。

Batch 2：价格编辑测试。

- 实现 `/product-center/pricing` 的价格方案列表、价格版本、矩阵价格、规则项、价格测试器。
- `PriceMatrixEditor` 要预留横向滚动、分段加载或虚拟滚动策略，避免大矩阵一次渲染过多 DOM。
- 价格测试器固定在右侧或明确区域，所有最终价、命中规则、缺价 blocker 都以后端价格引擎返回为准。
- 导入/批量填充需要预览差异和失败原因，不直接覆盖。
- 发布价格前展示影响产品、销售变体、配置模板和版本差异。

Batch 2：测试发布。

- 实现 `/product-center/publish` 发布闸门，展示 PASS、WARNING、BLOCKER、PENDING 统计和明细。
- 发布包预览必须展示版本、hash、生效时间、发布范围、包含内容。
- BLOCKER 禁止发布并提供修复入口；WARNING 可发布但必须二次确认。
- 发布成功后展示 outbox 写入和下游同步状态；同步重试按钮必须挂权限。

Batch 3：销售只读。

- 实现 `/product-center/sales-view`，只读读取发布包或 read model，不读草稿和编辑态表。
- 展示产品身份、可销售状态、价格状态、资料状态、限制信息、版本历史、客户可见资料。
- 对销售可见字段由后端控制；前端不得把内部字段直接暴露给销售角色。
- 只读页只能提供 quick quote、下载规格书、复制链接、查看详情等动作，不提供维护动作。

## 7. 验收检查点

- Contract 中菜单只有两级，一级为“产品能力”，路由均为 `/product-center/*`。
- Batch 1 页面目录覆盖工作台、基础信息、产品模型、组件资料，API 文件路径为 `admin-ui/src/api/product-capability/**`。
- 普通 grid/list 页面和 5 个自定义页面边界明确。
- i18n 只要求写 `i18n/locales/en_US.json`，明确不改 `admin-ui/public/i18n/*.json`。
- UTC、`v-hasPermi`、loading / empty / error 状态要求明确。
- Batch 2/3 TODO 保留配置录入、价格编辑测试、测试发布、销售只读。
