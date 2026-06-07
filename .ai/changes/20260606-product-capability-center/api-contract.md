# PCC-02 API Contract：共享产品能力中心 Batch 1

生成日期：2026-06-06  
Owner：java-architect  
范围：仅冻结 API contract 和 Java 分层边界，不实现 Controller，不生成 SQL/migration，不修改业务代码。

## 1. 执行边界

本 contract 覆盖共享产品能力中心第一批后端 API。入口边界是管理端和后续内部消费者调用的 HTTP API，统一前缀：

```text
/product-capability/*
```

当前前端菜单路由可以继续使用 `/product-center/*`，但后端 API 不使用订单私有前缀。第一批 API 只负责工作台只读入口、基础信息、资料、产品模型和销售变体。配置规则求值、价格计算、发布、销售只读和订单快照只保留 Batch 2/3 TODO。

响应形态固定为：

| 场景 | 返回类型 |
| --- | --- |
| 分页列表 | `TableDataInfo<XxxVo>` |
| 单条详情 / 汇总 / 结果 | `R<XxxVo>` 或 `R<XxxResultVo>` |
| 下拉、树、选择器 | `R<List<XxxOptionVo>>` |
| 新增、修改、删除、状态切换、绑定 | `R<Void>` |

Controller 只接收 BO / QueryBo / PathVariable / PageQuery，做权限、校验和 Service 调用；不得返回 Entity，不得直接拼规则、价格、BOM 或快照 JSON。

## 2. Batch 1 API Contract

### 2.1 工作台

工作台是聚合读入口，优先读取 read model、缺口任务和同步事件。Batch 1 可先返回稳定空数据骨架，但字段结构不能后续破坏。

| 方法 | 路径 | 入参 | 返回 | 权限 | 是否分页 | 数据要求 |
| --- | --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/workbench/summary` | `WorkbenchSummaryQueryBo` | `R<WorkbenchSummaryVo>` | `product:center:view` | 否 | 可先空骨架 |
| GET | `/product-capability/workbench/progress/list` | `WorkbenchProgressQueryBo`, `PageQuery` | `TableDataInfo<WorkbenchProgressVo>` | `product:center:view` | 是 | 可先空分页 |
| GET | `/product-capability/workbench/priority/list` | `WorkbenchPriorityQueryBo`, `PageQuery` | `TableDataInfo<WorkbenchPriorityVo>` | `product:center:view` | 是 | 可先空分页 |
| GET | `/product-capability/workbench/sync-events` | `WorkbenchSyncEventQueryBo`, `PageQuery` | `TableDataInfo<WorkbenchSyncEventVo>` | `product:center:view` | 是 | 可先空分页 |

建议字段：

| VO | 关键字段 |
| --- | --- |
| `WorkbenchSummaryVo` | `modelCount`, `draftCount`, `publishedCount`, `blockerCount`, `warningCount`, `pendingSyncCount`, `lastSyncTime` |
| `WorkbenchProgressVo` | `modelId`, `modelCode`, `modelName`, `categoryName`, `templateStatus`, `priceStatus`, `assetStatus`, `publishStatus`, `blockerCount`, `warningCount`, `updatedTime` |
| `WorkbenchPriorityVo` | `taskId`, `taskType`, `severity`, `targetType`, `targetCode`, `targetName`, `ownerName`, `dueTime`, `status` |
| `WorkbenchSyncEventVo` | `eventId`, `eventType`, `targetType`, `targetCode`, `status`, `retryCount`, `lastErrorKey`, `createdTime`, `updatedTime` |

空骨架约束：

- `summary` 返回计数字段 `0`、列表字段空数组、时间字段 `null`。
- 分页接口返回空 `rows` 和正确 `total = 0`。
- 不允许返回临时 `Map` 或前端 mock 专用字段。
- 后续接入真实表时字段名和语义保持兼容。

### 2.2 分类

分类是产品能力基础数据，Batch 1 必须连接真实表或真实 Mapper，不做长期 mock。

| 方法 | 路径 | 入参 | 返回 | 权限 | 日志 |
| --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/categories/tree` | `ProductCategoryQueryBo` | `R<List<ProductCategoryOptionVo>>` | `product:base:list` | 无 |
| GET | `/product-capability/categories/{categoryId}` | Path `categoryId` | `R<ProductCategoryDetailVo>` | `product:base:list` | 无 |
| POST | `/product-capability/categories` | `ProductCategoryBo` | `R<Void>` | `product:base:add` | `INSERT` |
| PUT | `/product-capability/categories` | `ProductCategoryBo` | `R<Void>` | `product:base:edit` | `UPDATE` |
| PUT | `/product-capability/categories/change-status` | `ProductCategoryStatusBo` | `R<Void>` | `product:base:edit` | `UPDATE` |
| DELETE | `/product-capability/categories/{categoryIds}` | Path `categoryIds` | `R<Void>` | `product:base:remove` | `DELETE` |
| GET | `/product-capability/categories/{categoryId}/references` | Path `categoryId` | `R<ReferenceCheckResultVo>` | `product:base:list` | 无 |

关键字段：

- `ProductCategoryBo`：`categoryId`, `parentId`, `categoryCode`, `categoryNameCn`, `categoryNameEn`, `sortOrder`, `status`, `remark`。
- `ProductCategoryOptionVo`：`id`, `parentId`, `code`, `label`, `labelCn`, `labelEn`, `status`, `disabled`, `children`。
- `ProductCategoryDetailVo`：基础字段 + `modelCount`, `canDisable`, `canRemove`, `referenceSummary`。

### 2.3 物料

物料是配置、价格、BOM、ERP 共同依赖的主数据，Batch 1 必须连接真实表。

| 方法 | 路径 | 入参 | 返回 | 权限 | 日志 |
| --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/materials/list` | `MaterialQueryBo`, `PageQuery` | `TableDataInfo<MaterialVo>` | `product:base:list` | 无 |
| GET | `/product-capability/materials/{materialId}` | Path `materialId` | `R<MaterialDetailVo>` | `product:base:list` | 无 |
| GET | `/product-capability/materials/options` | `MaterialQueryBo` | `R<List<MaterialOptionVo>>` | `product:base:list` | 无 |
| POST | `/product-capability/materials` | `MaterialBo` | `R<Void>` | `product:base:add` | `INSERT` |
| PUT | `/product-capability/materials` | `MaterialBo` | `R<Void>` | `product:base:edit` | `UPDATE` |
| PUT | `/product-capability/materials/change-status` | `MaterialStatusBo` | `R<Void>` | `product:base:edit` | `UPDATE` |
| DELETE | `/product-capability/materials/{materialIds}` | Path `materialIds` | `R<Void>` | `product:base:remove` | `DELETE` |
| GET | `/product-capability/materials/{materialId}/references` | Path `materialId` | `R<ReferenceCheckResultVo>` | `product:base:list` | 无 |

关键字段：

- `MaterialQueryBo`：`materialCode`, `materialName`, `materialType`, `businessType`, `supplierName`, `status`, `referenced`, `beginTime`, `endTime`。
- `MaterialBo`：`materialId`, `materialCode`, `materialNameCn`, `materialNameEn`, `materialType`, `businessType`, `unitCode`, `supplierName`, `status`, `attributeJson`, `remark`。
- `MaterialVo`：列表展示字段 + `unitLabel`, `referenceCount`, `updatedTime`。
- `MaterialDetailVo`：基础字段 + `attributeJson`, `assetSummary`, `componentSummary`, `priceReferenceSummary`, `referenceSummary`。
- `MaterialOptionVo`：`id`, `code`, `label`, `materialType`, `unitCode`, `status`, `disabled`。

### 2.4 组件

组件必须引用物料或标准品，不能把组件名自由文本写在配置答案中。Batch 1 必须连接真实表。

| 方法 | 路径 | 入参 | 返回 | 权限 | 日志 |
| --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/components/list` | `ComponentQueryBo`, `PageQuery` | `TableDataInfo<ComponentVo>` | `product:base:list` | 无 |
| GET | `/product-capability/components/{componentId}` | Path `componentId` | `R<ComponentDetailVo>` | `product:base:list` | 无 |
| GET | `/product-capability/components/options` | `ComponentQueryBo` | `R<List<ComponentOptionVo>>` | `product:base:list` | 无 |
| POST | `/product-capability/components` | `ComponentBo` | `R<Void>` | `product:base:add` | `INSERT` |
| PUT | `/product-capability/components` | `ComponentBo` | `R<Void>` | `product:base:edit` | `UPDATE` |
| PUT | `/product-capability/components/change-status` | `ComponentStatusBo` | `R<Void>` | `product:base:edit` | `UPDATE` |
| DELETE | `/product-capability/components/{componentIds}` | Path `componentIds` | `R<Void>` | `product:base:remove` | `DELETE` |
| GET | `/product-capability/components/{componentId}/references` | Path `componentId` | `R<ReferenceCheckResultVo>` | `product:base:list` | 无 |

关键字段：

- `ComponentQueryBo`：`componentCode`, `componentName`, `componentType`, `materialCode`, `categoryId`, `modelId`, `status`, `beginTime`, `endTime`。
- `ComponentBo`：`componentId`, `componentCode`, `componentNameCn`, `componentNameEn`, `componentType`, `materialId`, `defaultQty`, `qtyMode`, `unitCode`, `status`, `scopeJson`, `remark`。
- `ComponentVo`：列表展示字段 + `materialCode`, `materialName`, `defaultQty`, `unitLabel`, `priceMode`, `referenceCount`。
- `ComponentDetailVo`：基础字段 + `scopeJson`, `assetSummary`, `priceReferenceSummary`, `referenceSummary`。
- `ComponentOptionVo`：`id`, `code`, `label`, `componentType`, `materialId`, `status`, `disabled`。

### 2.5 资料资产

文件本体复用现有 OSS/文件管理，资料资产只维护产品能力域的业务元数据、版本、语言、用途和绑定关系。Batch 1 必须连接真实资料资产表；文件上传能力复用现有服务。

| 方法 | 路径 | 入参 | 返回 | 权限 | 日志 |
| --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/media-assets/list` | `MediaAssetQueryBo`, `PageQuery` | `TableDataInfo<MediaAssetVo>` | `product:asset:list` | 无 |
| GET | `/product-capability/media-assets/{assetId}` | Path `assetId` | `R<MediaAssetDetailVo>` | `product:asset:list` | 无 |
| GET | `/product-capability/media-assets/options` | `MediaAssetQueryBo` | `R<List<MediaAssetOptionVo>>` | `product:asset:list` | 无 |
| POST | `/product-capability/media-assets` | `MediaAssetBo` | `R<Void>` | `product:asset:upload` | `INSERT` |
| PUT | `/product-capability/media-assets` | `MediaAssetBo` | `R<Void>` | `product:asset:upload` | `UPDATE` |
| PUT | `/product-capability/media-assets/change-status` | `MediaAssetStatusBo` | `R<Void>` | `product:asset:upload` | `UPDATE` |
| DELETE | `/product-capability/media-assets/{assetIds}` | Path `assetIds` | `R<Void>` | `product:asset:upload` | `DELETE` |

关键字段：

- `MediaAssetQueryBo`：`assetCode`, `assetName`, `assetType`, `usageType`, `languageCode`, `visibility`, `status`, `targetType`, `targetCode`, `beginTime`, `endTime`。
- `MediaAssetBo`：`assetId`, `assetCode`, `assetNameCn`, `assetNameEn`, `assetType`, `usageType`, `languageCode`, `visibility`, `ossId`, `url`, `altText`, `versionNo`, `status`, `remark`。
- `MediaAssetVo`：列表展示字段 + `fileName`, `fileSize`, `previewUrl`, `bindingCount`, `updatedTime`。
- `MediaAssetDetailVo`：基础字段 + `bindingSummary`, `versionSummary`, `referenceSummary`。
- `MediaAssetOptionVo`：`id`, `code`, `label`, `assetType`, `usageType`, `languageCode`, `visibility`, `status`, `disabled`。

### 2.6 资料绑定

资料绑定是资料资产和产品、问题、答案、组件、规则、标准品等对象之间的关系。Batch 1 必须连接真实绑定表；引用目标不存在时要返回明确 message key。

| 方法 | 路径 | 入参 | 返回 | 权限 | 日志 |
| --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/media-bindings/list` | `MediaBindingQueryBo`, `PageQuery` | `TableDataInfo<MediaBindingVo>` | `product:asset:list` | 无 |
| POST | `/product-capability/media-bindings/batch` | `MediaBindingBatchBo` | `R<Void>` | `product:asset:bind` | `INSERT` |
| PUT | `/product-capability/media-bindings/sort` | `MediaBindingSortBo` | `R<Void>` | `product:asset:bind` | `UPDATE` |
| DELETE | `/product-capability/media-bindings/{bindingIds}` | Path `bindingIds` | `R<Void>` | `product:asset:bind` | `DELETE` |

关键字段：

- `MediaBindingQueryBo`：`assetId`, `targetType`, `targetId`, `targetCode`, `usageType`, `visibility`, `languageCode`, `status`。
- `MediaBindingBatchBo`：`targetType`, `targetId`, `targetCode`, `assetIds`, `usageType`, `visibility`, `sortOrder`, `requiredForPublish`。
- `MediaBindingVo`：`bindingId`, `assetId`, `assetCode`, `assetName`, `targetType`, `targetId`, `targetCode`, `usageType`, `visibility`, `languageCode`, `requiredForPublish`, `sortOrder`, `status`。

### 2.7 产品模型 / 销售变体

产品模型是配置中心入口，销售变体是面向市场、控制方式、包装、等级等维度的销售入口。Batch 1 必须连接真实表，列表允许部分摘要字段先为空。

| 方法 | 路径 | 入参 | 返回 | 权限 | 日志 |
| --- | --- | --- | --- | --- | --- |
| GET | `/product-capability/models/list` | `ProductModelQueryBo`, `PageQuery` | `TableDataInfo<ProductModelVo>` | `product:model:list` | 无 |
| GET | `/product-capability/models/{modelId}` | Path `modelId` | `R<ProductModelDetailVo>` | `product:model:query` | 无 |
| GET | `/product-capability/models/options` | `ProductModelQueryBo` | `R<List<ProductModelOptionVo>>` | `product:model:list` | 无 |
| POST | `/product-capability/models` | `ProductModelBo` | `R<Void>` | `product:model:add` | `INSERT` |
| PUT | `/product-capability/models` | `ProductModelBo` | `R<Void>` | `product:model:edit` | `UPDATE` |
| PUT | `/product-capability/models/change-status` | `ProductModelStatusBo` | `R<Void>` | `product:model:edit` | `UPDATE` |
| DELETE | `/product-capability/models/{modelIds}` | Path `modelIds` | `R<Void>` | `product:model:remove` | `DELETE` |
| GET | `/product-capability/models/{modelId}/variants` | Path `modelId`, `SalesVariantQueryBo` | `R<List<SalesVariantVo>>` | `product:model:list` | 无 |
| POST | `/product-capability/models/{modelId}/variants` | `SalesVariantBatchBo` | `R<Void>` | `product:model:edit` | `UPDATE` |
| GET | `/product-capability/models/{modelId}/references` | Path `modelId` | `R<ReferenceCheckResultVo>` | `product:model:query` | 无 |

关键字段：

- `ProductModelQueryBo`：`modelCode`, `modelName`, `categoryId`, `productFamily`, `salesMode`, `productNature`, `status`, `publishStatus`, `beginTime`, `endTime`。
- `ProductModelBo`：`modelId`, `modelCode`, `modelNameCn`, `modelNameEn`, `categoryId`, `productFamily`, `structureType`, `salesMode`, `productNature`, `unitCode`, `status`, `ownerUserId`, `assetOwnerUserId`, `priceOwnerUserId`, `remark`。
- `ProductModelVo`：基础展示字段 + `categoryName`, `currentTemplateVersion`, `currentPricePlan`, `publishStatus`, `questionCount`, `answerCount`, `componentBindingCount`, `assetGapCount`, `blockerCount`, `warningCount`。
- `ProductModelDetailVo`：基础字段 + `variants`, `templateSummary`, `priceSummary`, `assetSummary`, `publishSummary`, `referenceSummary`。
- `ProductModelOptionVo`：`id`, `code`, `label`, `categoryId`, `status`, `disabled`。
- `SalesVariantBatchBo`：`modelId`, `variants`，其中 variant 包含 `variantId`, `variantCode`, `variantNameCn`, `variantNameEn`, `dimensionJson`, `marketCode`, `controlMethod`, `grade`, `packageType`, `status`, `sortOrder`。
- `SalesVariantVo`：variant 基础字段 + `dimensionLabel`, `publishStatus`, `disabledReasonKey`。

## 3. BO / QueryBo / VO / DetailVo / OptionVo / ResultVo 命名边界

命名必须表达请求和响应职责，不以表结构作为前端 contract。

| 类型 | 用途 | 规则 |
| --- | --- | --- |
| `XxxBo` | 新增、修改、状态切换、批量操作请求 | 可继承项目现有 `BaseBo<Entity>` 风格；包含校验注解；不接受 `tenantId`、`createBy`、`createTime`、`updateBy`、`updateTime` |
| `XxxQueryBo` | 列表、树、选择器查询条件 | 只放筛选字段；时间范围字段使用 UTC 语义；分页字段放 `PageQuery` |
| `XxxVo` | 列表或普通详情展示 | 可冗余展示名、状态摘要、引用计数；不得暴露 Entity 内部审计和租户写入字段 |
| `XxxDetailVo` | 编辑回显和详情页 | 可包含子集合、摘要、引用检查结果；大 JSONB 只在详情读取 |
| `XxxOptionVo` | 下拉、树、选择器 | 固定轻量字段：`id`, `code`, `label`, `status`, `disabled`，树结构可加 `children` |
| `XxxResultVo` | 检查、求值、试算、快照构建结果 | 表达业务结果和错误明细，不绑定单表，不作为持久化 Entity |

跨模块共用的结果对象建议先固定：

| 类型 | 用途 |
| --- | --- |
| `ReferenceCheckResultVo` | 停用、删除、变更前引用检查 |
| `PublishBlockerVo` | 发布检查 blocker / warning 明细，Batch 2 使用 |
| `EvaluationResultVo` | 配置求值结果，Batch 2 使用 |
| `PriceTestResultVo` | 价格试算结果，Batch 2 使用 |
| `SnapshotBuildResultVo` | 发布包和订单快照构建结果，Batch 2/3 使用 |

## 4. 权限、日志、分页、UTC、i18n、tenant boundary

### 4.1 权限码

Batch 1 使用以下权限码，前端 `v-hasPermi` 和后端 `@SaCheckPermission` 必须一致：

```text
product:center:view
product:center:handle
product:base:list
product:base:add
product:base:edit
product:base:remove
product:base:export
product:asset:list
product:asset:upload
product:asset:bind
product:model:list
product:model:query
product:model:add
product:model:edit
product:model:remove
```

Batch 2/3 保留：

```text
product:template:list
product:template:edit
product:template:rule
product:template:test
product:price:list
product:price:edit
product:price:test
product:price:publish
product:publish:check
product:publish:approve
product:publish:publish
product:sales:view
```

### 4.2 操作日志

新增、修改、删除、状态切换、批量绑定必须加 `@Log`：

| 操作 | `BusinessType` |
| --- | --- |
| 新增 | `INSERT` |
| 修改、状态切换、排序、批量绑定 | `UPDATE` |
| 删除 | `DELETE` |
| 导出 | `EXPORT` |
| 发布、审核、生成快照、重试同步 | `SENSITIVE_OPERATION` |

只读列表、详情、options 和 references 默认不记录操作日志，除非后续有审计要求。

### 4.3 分页和排序

- 分页接口统一接收 `PageQuery`，响应 `TableDataInfo<XxxVo>`。
- 查询 BO 不内嵌分页字段。
- 默认支持 `pageNum`, `pageSize`, `orderByColumn`, `isAsc`。
- `orderByColumn` 必须走白名单映射，不能把前端字段直接拼进 SQL。
- 列表不默认读取大 JSON 字段；大字段放详情接口。

### 4.4 UTC

- `beginTime` / `endTime` / `createdTime` / `updatedTime` / `lastSyncTime` 等绝对时间字段按 UTC 语义处理。
- 后端业务时间使用 `TimeUtils.utcNow()`，不使用 `LocalDateTime.now()`。
- 前端提交绝对时间使用 `toUtcPayload()` / `withUtcDateRange()` / `withUtcDateRangeParams()`。
- 前端展示后端时间使用 `formatUtc()` 或旧兼容 `parseTime()`。
- API 不新增本地时区字段；确需展示时区由前端按浏览器本地时区处理。

### 4.5 i18n message key

后端异常和业务校验优先返回 message key，避免硬编码中文或英文错误。

建议 key 前缀：

```text
productCapability.category.*
productCapability.material.*
productCapability.component.*
productCapability.asset.*
productCapability.binding.*
productCapability.model.*
productCapability.variant.*
productCapability.reference.*
productCapability.publish.*
productCapability.price.*
productCapability.snapshot.*
```

Batch 1 最小 key 清单：

| 场景 | message key |
| --- | --- |
| 编码重复 | `productCapability.common.codeDuplicated` |
| 记录不存在 | `productCapability.common.notFound` |
| 状态不允许 | `productCapability.common.invalidStatus` |
| 存在引用不可删除 | `productCapability.reference.deleteBlocked` |
| 存在引用不可停用 | `productCapability.reference.disableBlocked` |
| 资料资产不存在 | `productCapability.asset.notFound` |
| 绑定目标不存在 | `productCapability.binding.targetNotFound` |
| 产品模型不存在 | `productCapability.model.notFound` |
| 销售变体编码重复 | `productCapability.variant.codeDuplicated` |

产品名、答案名、客户说明、alt 文本是业务多语言字段，不是 UI i18n key。发布包和快照后续必须保存当时的中英文业务值。

### 4.6 tenant boundary

产品能力核心数据按平台共享主数据处理，API contract 固定以下规则：

- 客户端不得提交 `tenantId`。
- VO 默认不暴露 `tenantId`。
- Service 层统一决定租户策略，不在 Controller 中拼租户条件。
- 订单、ERP、MES 后续只能消费发布版本、read model 或快照，不直接写共享产品能力核心表。
- 技术落地必须在 Batch 1 实现前二选一：核心表加入租户忽略配置，或统一写平台租户 ID。

在租户策略未最终确认前，API contract 不允许引入商户级产品能力复制字段，避免把平台主数据误设计成租户私有配置。

## 5. 空数据骨架与真实表要求

| API 范围 | Batch 1 是否允许空数据骨架 | 要求 |
| --- | --- | --- |
| 工作台 summary | 允许 | 返回稳定 `WorkbenchSummaryVo`，计数为 0，不能返回 `null` 壳 |
| 工作台 progress list | 允许 | 返回空分页，后续优先接 read model |
| 工作台 priority list | 允许 | 返回空分页，后续优先接 gap task |
| 工作台 sync-events | 允许 | 返回空分页，后续优先接 event outbox |
| 分类 tree/detail/options/CRUD | 不允许长期空骨架 | 必须连接真实分类表或真实 Mapper |
| 分类 references | 允许先返回可停用/可删除 | 必须保留 `ReferenceCheckResultVo` 结构；接真实引用前不能阻断正常维护 |
| 物料 list/detail/options/CRUD | 不允许 | 必须连接真实物料表 |
| 物料 references | 允许先返回可删除骨架 | 接配置、组件、价格引用后必须改为真实检查 |
| 组件 list/detail/options/CRUD | 不允许 | 必须连接真实组件表，并校验物料引用 |
| 组件 references | 允许先返回可删除骨架 | 接答案、BOM、价格后必须改为真实检查 |
| 资料资产 list/detail/options/CRUD | 不允许 | 必须连接真实资料资产表，文件本体复用 OSS |
| 资料绑定 list/batch/sort/delete | 不允许 | 必须连接真实绑定表并校验 `targetType` |
| 产品模型 list/detail/options/CRUD | 不允许 | 必须连接真实产品模型表；列表摘要字段可为空 |
| 销售变体 list/save | 不允许 | 必须连接真实销售变体表，并校验 `modelId` |
| 产品模型 references | 允许先返回可删除骨架 | 接模板、价格、发布包后必须改为真实检查 |

原则：维护型接口必须真实持久化；聚合看板和引用摘要可以先骨架，但必须使用最终 VO。

## 6. Batch 2/3 API TODO

### 6.1 Batch 2：配置 editor / evaluate

TODO API：

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| GET | `/product-capability/templates/list` | `TemplateQueryBo`, `PageQuery` | `TableDataInfo<TemplateVo>` | `product:template:list` |
| GET | `/product-capability/template-versions/{versionId}/editor` | Path `versionId` | `R<TemplateEditorDetailVo>` | `product:template:edit` |
| PUT | `/product-capability/template-versions/{versionId}/groups` | `QuestionGroupBatchBo` | `R<Void>` | `product:template:edit` |
| PUT | `/product-capability/template-versions/{versionId}/questions` | `QuestionBatchBo` | `R<Void>` | `product:template:edit` |
| PUT | `/product-capability/template-versions/{versionId}/options` | `QuestionOptionBatchBo` | `R<Void>` | `product:template:edit` |
| POST | `/product-capability/template-versions/{versionId}/options/batch-parse` | `OptionBatchParseBo` | `R<OptionBatchParseResultVo>` | `product:template:edit` |
| POST | `/product-capability/template-versions/{versionId}/evaluate` | `EvaluationRequestBo` | `R<EvaluationResultVo>` | `product:template:test` |

实现边界：

- `evaluate` 必须调用 `ConfigEvaluationEngine`。
- 前端只展示求值结果，不能复制发布、报价、订单所需的规则计算。
- `EvaluationResultVo` 至少包含可见问题、禁用问题、校验错误、warning、自动带出的组件、资料、BOM 摘要和 blocker 摘要。

### 6.2 Batch 2：价格 test

TODO API：

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| GET | `/product-capability/price-plans/list` | `PricePlanQueryBo`, `PageQuery` | `TableDataInfo<PricePlanVo>` | `product:price:list` |
| GET | `/product-capability/price-plan-versions/{versionId}/editor` | Path `versionId` | `R<PricePlanEditorDetailVo>` | `product:price:edit` |
| PUT | `/product-capability/price-plan-versions/{versionId}` | `PricePlanVersionBo` | `R<Void>` | `product:price:edit` |
| PUT | `/product-capability/price-rule-items` | `PriceRuleItemBatchBo` | `R<Void>` | `product:price:edit` |
| POST | `/product-capability/price-rule-items/import-preview` | `PriceRuleImportPreviewBo` | `R<PriceRuleImportPreviewResultVo>` | `product:price:edit` |
| POST | `/product-capability/price-plan-versions/{versionId}/test` | `PriceTestRequestBo` | `R<PriceTestResultVo>` | `product:price:test` |
| GET | `/product-capability/price-plan-versions/{versionId}/impact` | Path `versionId` | `R<PriceImpactResultVo>` | `product:price:publish` |

实现边界：

- 价格试算必须调用 `PriceCalculationEngine`。
- `PriceTestResultVo` 要表达命中规则、币种、单位、明细、warning 和最终价。
- 前端不能本地计算最终价格。

### 6.3 Batch 2：发布 check / release

TODO API：

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| POST | `/product-capability/publish/check` | `PublishCheckBo` | `R<PublishCheckResultVo>` | `product:publish:check` |
| GET | `/product-capability/publish/checks/{checkId}` | Path `checkId` | `R<PublishCheckDetailVo>` | `product:publish:check` |
| POST | `/product-capability/publish/submit` | `PublishSubmitBo` | `R<Void>` | `product:publish:approve` |
| POST | `/product-capability/publish/approve` | `PublishApproveBo` | `R<Void>` | `product:publish:approve` |
| POST | `/product-capability/publish/reject` | `PublishRejectBo` | `R<Void>` | `product:publish:approve` |
| POST | `/product-capability/publish/release` | `PublishReleaseBo` | `R<PublishReleaseResultVo>` | `product:publish:publish` |
| GET | `/product-capability/published-packages/list` | `PublishedPackageQueryBo`, `PageQuery` | `TableDataInfo<PublishedPackageVo>` | `product:publish:check` |
| GET | `/product-capability/published-packages/{packageId}` | Path `packageId` | `R<PublishedPackageDetailVo>` | `product:publish:check` |

实现边界：

- `check` 必须调用 `PublishCheckEngine`。
- `release` 必须调用 `SnapshotBuildEngine` 或发布 Service 生成不可变发布包。
- BLOCKER 不可发布；WARNING 可发布但必须明确确认。
- 发布成功后写 `pc_product_sync_outbox`，同步失败不能回退发布包。

### 6.4 Batch 3：销售只读

TODO API：

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| GET | `/product-capability/sales/products/list` | `SalesProductQueryBo`, `PageQuery` | `TableDataInfo<SalesProductVo>` | `product:sales:view` |
| GET | `/product-capability/sales/products/{productCode}` | Path `productCode` | `R<SalesProductDetailVo>` | `product:sales:view` |
| POST | `/product-capability/sales/products/{productCode}/quote-preview` | `QuotePreviewBo` | `R<QuotePreviewResultVo>` | `product:sales:view` |
| GET | `/product-capability/sales/products/{productCode}/documents` | Path `productCode`, `SalesDocumentQueryBo` | `R<List<SalesDocumentVo>>` | `product:sales:view` |

实现边界：

- 只读接口读取发布包或 read model，不读取草稿编辑表。
- 销售不可修改资料、配置模板、价格规则。
- 报价预览必须以后端配置求值和价格试算结果为准。

### 6.5 Batch 3 / Batch 7：snapshot instances

已调整为产品能力中心通用快照实例能力，订单、ERP、MES 等来源系统通过 `source_system/source_biz_*` 字段区分来源。

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| POST | `/product-capability/order-snapshots/build` | `OrderSnapshotBuildBo` | `R<OrderProductSnapshotVo>` | `product:order-snapshot:build` |
| POST | `/product-capability/snapshot-instances/build` | `OrderSnapshotBuildBo` | `R<OrderProductSnapshotVo>` | `product:snapshot-instance:build` |
| GET | `/product-capability/snapshot-instances/list` | `ProductSnapshotInstance + PageQuery` | `TableDataInfo<ProductSnapshotInstanceVo>` | `product:snapshot-instance:list` |
| GET | `/product-capability/snapshot-instances/{snapshotId}` | path `snapshotId` | `R<ProductSnapshotInstanceVo>` | `product:snapshot-instance:query` |

实现边界：

- 订单、ERP、MES 保存业务单据前必须调用 `ProductOrderSnapshotService` 或等价 API。
- 快照必须自包含：来源业务号、发布包、模型、销售变体、模板版本、价格版本、用户输入和 `snapshot_hash`。
- 产品能力快照实例是历史追溯权威数据，不只保存产品能力中心 ID。
- 来源系统侧可保存 `snapshot_id/snapshot_hash` 和少量展示冗余字段，但不复制产品能力快照生成逻辑。

### 6.6 Batch 8：import center foundation

已新增导入中心基础能力。当前边界只覆盖导入批次、字段映射/预览 JSON、行级问题和状态流转，完整 Excel/AI 自动解析、批量提交、冲突合并和 ERP/MES 专用导入仍保留 Deferred。

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| GET | `/product-capability/import/batches/list` | `ProductImportBatchBo`, `PageQuery` | `TableDataInfo<ProductImportBatchVo>` | `product:import:list` |
| GET | `/product-capability/import/batches/{batchId}` | path `batchId` | `R<ProductImportBatchVo>` | `product:import:query` |
| POST | `/product-capability/import/batches/parse-excel` | multipart `file` + `ProductImportBatchBo` form fields | `R<ProductImportBatchVo>` | `product:import:add` |
| POST | `/product-capability/import/batches` | `ProductImportBatchBo` | `R<Void>` | `product:import:add` |
| PUT | `/product-capability/import/batches` | `ProductImportBatchBo` | `R<Void>` | `product:import:edit` |
| PUT | `/product-capability/import/batches/change-status/{batchId}/{status}` | path `batchId/status` | `R<Void>` | `product:import:edit` |
| DELETE | `/product-capability/import/batches/{ids}` | path `ids` | `R<Void>` | `product:import:remove` |
| GET | `/product-capability/import/issues/list` | `ProductImportRowIssueBo`, `PageQuery` | `TableDataInfo<ProductImportRowIssueVo>` | `product:import:list` |
| POST | `/product-capability/import/issues` | `ProductImportRowIssueBo` | `R<Void>` | `product:import:edit` |
| PUT | `/product-capability/import/issues` | `ProductImportRowIssueBo` | `R<Void>` | `product:import:edit` |
| DELETE | `/product-capability/import/issues/{ids}` | path `ids` | `R<Void>` | `product:import:remove` |

实现边界：

- 导入批次默认 `sourceSystem = MANUAL`、`importStatus = DRAFT`，批次号由后端生成。
- 行级问题默认 `status = 1`，用于人工修正、忽略或后续提交前检查。
- 导入中心表加入租户忽略表，按平台共享产品能力处理。
- 删除批次时同步删除本批次行级问题。
- `parse-excel` 只接收 `xls/xlsx`，复用现有 `bocoo-common-excel`，不新增导入依赖。
- 解析旧 XLS/XLSX 时只参考字段口径，不复刻旧表结构；组件、物料、产品模型、价格方案统一通过共享主数据编码匹配。
- 共享主数据编码缺失时写入 `pc_import_row_issue`，问题级别为 `ERROR`，不会自动创建重复组件或物料。
- 解析预览写入 `pc_import_batch.mapping_json / preview_json / error_summary_json`，批次状态为 `PARSED`。
- 当前 API 不直接写物料、组件、答案、价格等目标表；后续提交动作需要按目标对象单独设计事务、审计和回滚策略。

### 6.7 Batch 9：ERP / MES sync outbox foundation

已将发布成功后的同步 outbox 从单一 ORDER 目标扩展为 ORDER、ERP、MES 三个目标系统。当前只生成同步事件和提供重试标记，不直接调用 ERP/MES 外部接口，不实现定时调度。

| 方法 | 路径 | 入参 | 返回 | 权限 |
| --- | --- | --- | --- | --- |
| GET | `/product-capability/sync-outbox/list` | `ProductSyncOutboxBo`, `PageQuery` | `TableDataInfo<ProductSyncOutboxVo>` | `product:publish:list` |
| GET | `/product-capability/sync-outbox/{id}` | path `id` | `R<ProductSyncOutboxVo>` | `product:publish:list` |
| POST | `/product-capability/sync-outbox/{id}/retry` | path `id` | `R<Void>` | `product:publish:retrySync` |
| POST | `/product-capability/publish/packages/create` | `PublishCheckBo` | `R<PublishExecutionResultVo>` | `product:publish:publish` |

实现边界：

- `publish/packages/create` 生成发布包后，同事务写入 ORDER、ERP、MES 三条 `pc_product_sync_outbox`。
- `PublishExecutionResultVo.outbox` 保留第一条 outbox 兼容旧前端；`outboxes` 返回完整目标系统列表。
- `sync-outbox/{id}/retry` 只把事件重新标记为 `PENDING` 并递增 `retryCount`，不在接口内执行外部同步。
- 真实 ERP/MES 推送、失败退避、定时扫描、回调确认、幂等消费和死信处理后续单独设计。

## 7. 验证要求

PCC-02 本轮只做文档 contract，验证口径为静态审查：

- Batch 1 API 路径、权限码、请求 BO、返回 VO 已明确。
- 分页、错误 message key、UTC、tenant boundary 已明确。
- 维护型接口和可空骨架接口边界已明确。
- Batch 2/3 的 editor/evaluate、price test、publish、sales readonly、orders snapshot 已保留 TODO。

后续进入实现前仍需环境级确认：

- 后端模块名和包路径。
- 产品能力核心表 tenant 策略。
- migration 评审节奏和表级字段。
- 内部服务权限如何表达。
