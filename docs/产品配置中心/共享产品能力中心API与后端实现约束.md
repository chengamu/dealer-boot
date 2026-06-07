# 共享产品能力中心 API 与后端实现约束

生成日期：2026-06-06

关联设计：

- [产品配置中心设计.md](./产品配置中心设计.md)
- [配置中心功能拆分清单.md](./配置中心功能拆分清单.md)
- [共享产品能力中心数据库设计草案.md](./共享产品能力中心数据库设计草案.md)
- [产品能力界面设计稿.md](./产品能力界面设计稿.md)
- [价格中心设计.md](./价格中心设计.md)
- [../项目配置和代码风格/fullstack-code-standards.md](../项目配置和代码风格/fullstack-code-standards.md)

> 本文是 API 契约和后台 Java 落地约束，不是 Java 代码实现，不生成 migration，不修改生产配置。  
> 目标是在数据库设计和前端界面设计之间补齐“接口层”和“后端风格约束”，避免后续直接从页面跳到 Controller。
> 普通 grid 后端分层、代码生成器、导出、权限、i18n、UTC 的项目级规范以 [fullstack-code-standards.md](../项目配置和代码风格/fullstack-code-standards.md) 为准。

## 0. 结论

数据库草案和前端界面稿第一版已经够继续推进，但还缺一层 API 和 Java 风格约束。

缺口主要是：

| 缺口 | 风险 |
| --- | --- |
| API 分组和 URL 未固化 | 前端页面、订单、ERP/MES 后续调用容易各写各的 |
| BO / VO / Query 边界未固化 | 容易把 Entity 直接暴露给前端，或把页面字段和数据库字段绑死 |
| 发布、求值、快照接口未明确权威 | 前端可能复制规则计算，导致报价、订单、发布不一致 |
| 后台 Java 分层约束未写清 | 规则求值、发布检查、快照构建可能散落在 Controller |
| 权限、日志、i18n、UTC、租户处理未成接口级规则 | 后续实现容易绕过现有项目基础能力 |

本文补齐这些约束。

## 1. 当前项目后端风格基线

后续实现必须贴合当前 `base-boot` 风格：

| 能力 | 当前项目做法 | 产品能力中心要求 |
| --- | --- | --- |
| Controller 响应 | 单条/操作返回 `R<T>`；分页返回 `TableDataInfo<T>` | 不自定义新的响应壳 |
| 分页 | Controller 接收 `PageQuery`，Service 返回 `TableDataInfo` | 列表接口统一支持 `pageNum/pageSize/orderByColumn/isAsc` |
| 权限 | `@SaCheckPermission` + 前端 `v-hasPermi` | 每个菜单和按钮动作都有权限码 |
| 操作日志 | `@Log(title = "...", businessType = BusinessType.XXX)` | 新增、修改、删除、发布、审核、导出必须记录 |
| OpenAPI 注解 | `@Tag`、`@Operation`、`@Parameter` | 新 Controller 继续补注解，方便后续接口文档 |
| 校验 | `@Validated`、`@RequestBody BO` | 新增/修改/发布类接口必须校验 BO |
| 时间 | 后端使用 `TimeUtils.utcNow()`，前端使用 `formatUtc()` | 禁止直接 `LocalDateTime.now()` |
| 审计字段 | Entity 继承 `BaseEntity`，自动填充创建/更新时间 | 前端不提交审计字段，后端自动维护 |
| i18n | 后端错误消息走 JSON i18n / message key | 新增错误尽量使用 message key，不散落硬编码 |
| 前端请求 | `request.ts` 自动带 `Content-Language` | 后端 locale 从请求头解析，不新增运行时 AI 翻译 |

## 2. API 总体原则

### 2.1 API 路径

前端菜单路由可以继续使用 `/product-center/*`，API 路径建议使用更明确的共享能力前缀：

```text
/product-capability/*
```

原因：

- 菜单名可以叫“产品能力”，路由可以沿用 `/product-center`，但后端 API 要表达共享能力边界。
- 未来独立服务时，`/product-capability` 更适合作为服务 API 前缀。
- 订单、ERP、MES 消费时，不应误以为这是订单系统私有接口。

### 2.2 响应形态

| 场景 | 返回 |
| --- | --- |
| 分页列表 | `TableDataInfo<XxxVo>` |
| 单条详情 | `R<XxxVo>` |
| 新增、修改、删除、状态切换 | `R<Void>` |
| 下拉选择、树结构、小列表 | `R<List<XxxOptionVo>>` |
| 工作台统计、发布检查、求值结果 | `R<XxxResultVo>` |
| 导出 | `void` + `ExcelUtil.exportExcel(...)` |

禁止：

- Controller 直接返回 Entity。
- Controller 返回裸 `Map` 作为主要业务契约，除非是极小范围临时结构。
- 前端为了展示方便要求后端返回数据库表结构原样字段。

### 2.3 请求对象边界

| 对象 | 用途 | 规则 |
| --- | --- | --- |
| `XxxQueryBo` 或 `XxxBo` | 查询、新增、修改入参 | 接收前端请求，不继承 Entity |
| `XxxVo` | 返回前端展示 | 按页面需要组织字段，可包含冗余展示名和状态摘要 |
| `XxxOptionVo` | 下拉、树、选择器 | 只包含 id、code、label、status、disabled 等轻字段 |
| `XxxDetailVo` | 详情、编辑回显 | 可包含子列表和 JSON 摘要 |
| `XxxResultVo` | 求值、发布检查、快照构建结果 | 表达业务结果，不绑定单表 |

转换建议使用现有 `MapstructUtils` 或项目已有转换方式。复杂聚合结果由 Service 显式组装，不在 Controller 里拼。

## 3. API 分组草案

下面是第一版 API 分组，不要求一次全部实现，但命名边界建议先固定。

### 3.1 工作台

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/workbench/summary` | `product:center:view` | 工作台统计、待处理流、同步摘要 |
| GET | `/product-capability/workbench/progress/list` | `product:center:view` | 产品配置进度看板分页 |
| GET | `/product-capability/workbench/priority/list` | `product:center:view` | 优先处理队列 |
| GET | `/product-capability/workbench/sync-events` | `product:center:view` | 最近同步事件 |

读取来源优先 `pc_read_model`、`pc_gap_task`、`pc_product_sync_outbox`，不要实时 join 全部编辑表。

### 3.2 基础信息

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/categories/tree` | `product:base:list` | 产品分类树 |
| GET | `/product-capability/materials/list` | `product:base:list` | 物料分页 |
| POST | `/product-capability/materials` | `product:base:add` | 新增物料 |
| PUT | `/product-capability/materials` | `product:base:edit` | 修改物料 |
| DELETE | `/product-capability/materials/{materialIds}` | `product:base:remove` | 删除物料 |
| GET | `/product-capability/components/list` | `product:base:list` | 组件分页 |
| GET | `/product-capability/media-assets/list` | `product:asset:list` | 资料资产分页 |
| POST | `/product-capability/media-bindings/batch` | `product:asset:bind` | 批量绑定资料 |
| GET | `/product-capability/references/check` | `product:base:list` | 停用/删除前引用检查 |

基础信息页面仍按普通 grid/list 形态实现，接口要给抽屉和引用检查提供轻量详情。

### 3.3 产品模型

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/models/list` | `product:model:list` | 产品模型分页 |
| GET | `/product-capability/models/{modelId}` | `product:model:query` | 产品模型详情 |
| POST | `/product-capability/models` | `product:model:add` | 新增产品模型 |
| PUT | `/product-capability/models` | `product:model:edit` | 修改产品模型 |
| PUT | `/product-capability/models/change-status` | `product:model:edit` | 启停用 |
| GET | `/product-capability/models/{modelId}/variants` | `product:model:list` | 销售变体列表 |
| POST | `/product-capability/models/{modelId}/variants` | `product:model:edit` | 保存销售变体 |

产品模型返回给列表时必须包含当前模板、当前价格方案、发布状态、缺口摘要等冗余展示字段。

### 3.4 配置模板

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/templates/list` | `product:template:list` | 配置模板分页 |
| GET | `/product-capability/templates/{templateId}/versions` | `product:template:list` | 模板版本列表 |
| GET | `/product-capability/template-versions/{versionId}/editor` | `product:template:edit` | 配置录入工作台完整数据 |
| PUT | `/product-capability/template-versions/{versionId}/groups` | `product:template:edit` | 保存问题组排序和结构 |
| PUT | `/product-capability/template-versions/{versionId}/questions` | `product:template:edit` | 保存问题 |
| PUT | `/product-capability/template-versions/{versionId}/options` | `product:template:edit` | 保存答案选项 |
| POST | `/product-capability/template-versions/{versionId}/options/batch-parse` | `product:template:edit` | 批量粘贴答案解析预览 |
| POST | `/product-capability/template-versions/{versionId}/copy-group` | `product:template:edit` | 复制问题组 |
| POST | `/product-capability/template-versions/{versionId}/evaluate` | `product:template:test` | 配置规则求值预览 |

规则求值返回 `EvaluationResultVo`，至少包含：

- 当前可见问题和禁用问题。
- 校验错误和 warning。
- 自动带出的组件、资料、BOM 摘要。
- 可发布 blocker 摘要。
- 快照预览摘要。

### 3.5 价格中心

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/price-plans/list` | `product:price:list` | 价格方案分页 |
| GET | `/product-capability/price-plan-versions/{versionId}/editor` | `product:price:edit` | 价格编辑器完整数据 |
| PUT | `/product-capability/price-plan-versions/{versionId}` | `product:price:edit` | 保存价格方案版本元数据 |
| PUT | `/product-capability/price-rule-items` | `product:price:edit` | 保存价格规则项 |
| POST | `/product-capability/price-rule-items/import-preview` | `product:price:edit` | 导入价格预览 |
| POST | `/product-capability/price-plan-versions/{versionId}/test` | `product:price:test` | 价格试算 |
| GET | `/product-capability/price-plan-versions/{versionId}/impact` | `product:price:publish` | 发布影响分析 |

价格试算必须调用后端价格引擎。前端只能展示命中过程，不能复制价格规则自行计算最终价。

### 3.6 发布和审核

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/product-capability/publish/check` | `product:publish:check` | 执行发布检查 |
| GET | `/product-capability/publish/checks/{checkId}` | `product:publish:check` | 发布检查详情 |
| POST | `/product-capability/publish/submit` | `product:publish:approve` | 提交发布审核 |
| POST | `/product-capability/publish/approve` | `product:publish:approve` | 审核通过 |
| POST | `/product-capability/publish/reject` | `product:publish:approve` | 审核驳回 |
| POST | `/product-capability/publish/release` | `product:publish:publish` | 发布不可变发布包 |
| GET | `/product-capability/published-packages/list` | `product:publish:list` | 发布包分页 |
| GET | `/product-capability/published-packages/{packageId}` | `product:publish:list` | 发布包详情 |

发布接口必须保证：

- 存在 BLOCKER 时不可发布。
- WARNING 可发布，但需要明确确认。
- 发布包生成后不可原地修改。
- 发布成功写 `pc_product_sync_outbox`。
- 发布包摘要、hash、生效时间、配置版本、价格版本必须可追溯。

### 3.7 销售只读和订单消费

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/sales/products/list` | `product:sales-view:list` | 销售只读产品列表 |
| GET | `/product-capability/sales/products/{productCode}` | `product:sales-view:list` | 销售只读总览详情 |
| POST | `/product-capability/sales/products/{productCode}/quote-preview` | `product:sales-view:quote` | 快捷报价预览 |
| GET | `/product-capability/sales/products/{productCode}/documents` | `product:sales-view:download` | 客户可见资料 |
| POST | `/product-capability/order-snapshots/build` | `product:order-snapshot:build` | 只构建产品能力快照，不落库 |
| POST | `/product-capability/snapshot-instances/build` | `product:snapshot-instance:build` | 构建并保存产品能力快照实例 |
| GET | `/product-capability/snapshot-instances/list` | `product:snapshot-instance:list` | 查询产品能力快照实例 |
| GET | `/product-capability/snapshot-instances/{snapshotId}` | `product:snapshot-instance:query` | 查询产品能力快照实例详情 |

订单、报价、ERP、MES 保存业务单据前必须调用后端 `snapshot-instances/build` 或等价 Service。快照实例统一放在产品能力中心，通过 `source_system/source_biz_type/source_biz_no/source_biz_line_no` 标识来源，业务系统侧只保存 `snapshot_id/snapshot_hash` 和必要展示冗余字段。

### 3.8 缺口和同步

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/gap-tasks/list` | `product:center:view` | 缺口任务分页 |
| PUT | `/product-capability/gap-tasks/assign` | `product:center:handle` | 分配负责人 |
| PUT | `/product-capability/gap-tasks/resolve` | `product:center:handle` | 标记处理完成 |
| GET | `/product-capability/sync-outbox/list` | `product:publish:list` | 产品能力同步事件分页 |
| GET | `/product-capability/sync-outbox/{id}` | `product:publish:list` | 产品能力同步事件详情 |
| POST | `/product-capability/sync-outbox/{id}/retry` | `product:publish:retrySync` | 将同步事件重新标记为待同步 |

发布包生成后必须同事务写入 ORDER、ERP、MES 三个目标系统的 `pc_product_sync_outbox` 事件。当前只落同步事件和重试标记，不直接调用 ERP/MES 外部接口；真实推送、回调确认、失败退避和定时重试后续单独设计。

### 3.9 导入中心

导入中心属于共享产品能力，不是订单私有能力。第一版只落导入批次、预览 JSON 和行级问题，作为人工导入、历史数据整理、ERP/MES 导入和后续 AI 解析的统一承接层。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/product-capability/import/batches/list` | `product:import:list` | 导入批次分页 |
| GET | `/product-capability/import/batches/{batchId}` | `product:import:query` | 导入批次详情 |
| POST | `/product-capability/import/batches/parse-excel` | `product:import:add` | 上传并解析 XLS/XLSX，生成预览和行级问题 |
| POST | `/product-capability/import/batches` | `product:import:add` | 新增导入批次 |
| PUT | `/product-capability/import/batches` | `product:import:edit` | 修改导入批次、映射和预览 |
| PUT | `/product-capability/import/batches/change-status/{batchId}/{status}` | `product:import:edit` | 修改导入批次状态 |
| DELETE | `/product-capability/import/batches/{ids}` | `product:import:remove` | 删除导入批次并清理行级问题 |
| GET | `/product-capability/import/issues/list` | `product:import:list` | 导入行级问题分页 |
| POST | `/product-capability/import/issues` | `product:import:edit` | 新增导入行级问题 |
| PUT | `/product-capability/import/issues` | `product:import:edit` | 修改导入行级问题 |
| DELETE | `/product-capability/import/issues/{ids}` | `product:import:remove` | 删除导入行级问题 |

实现边界：

- `pc_import_batch` 保存来源系统、原始文件、目标对象、字段映射、预览 JSON、错误汇总和行数统计。
- `pc_import_row_issue` 保存行号、字段、问题级别、原始行 JSON、修正后 JSON 和处理状态。
- XLS/XLSX 解析只参考旧文件字段口径，不复制旧库结构；组件、物料、产品模型、价格方案必须优先通过共享主数据编码匹配。
- 当 Excel 中出现组件编码、物料编码、产品模型编码或价格方案编码但共享主数据不存在时，写入 `ERROR` 行级问题，不自动创建重复基础数据。
- 解析结果先写入 `mapping_json`、`preview_json`、`error_summary_json`，只做预览和问题清单；后续提交正式表必须另做目标对象事务和冲突合并策略。
- 第一版不自动提交到物料、组件、答案或价格表；提交动作后续必须逐目标对象单独设计事务和回滚策略。
- 完整 Excel/AI 自动解析、字段智能映射、批量提交和冲突合并仍是后续增强，不在当前基础能力里假装完成。

## 4. 后端 Java 分层约束

后端 Java 分层优先参考当前代码生成器模板，不重新发明一套分层。

代码生成器模板位置：

```text
bocoo-modules-generator/src/main/resources/vm/java/
  -> controller.java.vm
  -> service.java.vm
  -> serviceImpl.java.vm
  -> mapper.java.vm
  -> domain.java.vm
  -> bo.java.vm
  -> vo.java.vm
  -> xml/mapper.xml.vm
```

生成器已经沉淀了当前项目的默认风格：

| 层 | 生成器风格 |
| --- | --- |
| Controller | `@RestController`、`@RequestMapping`、`@SaCheckPermission`、`@Log`、`R`、`TableDataInfo` |
| Service 接口 | `IBaseService<Entity>`，方法包含 `queryPageList`、`queryList`、`insertByBo`、`updateByBo` |
| ServiceImpl | `BaseServiceImpl<Mapper, Entity, Vo>`，使用 `MapstructUtils.convert`、`LambdaQueryWrapper`、`TableDataInfo.build` |
| Mapper | `BaseMapperPlus<Entity, Vo>` |
| Entity | `domain/entity`，继承 `BaseEntity` |
| BO | `domain/bo`，继承 `BaseBo` |
| VO | `domain/vo`，按前端展示返回 |

因此第一版普通 CRUD 能用生成器生成的，优先用生成器生成后再局部调整；不要手写一套和生成器不一致的 Controller/Service/Mapper 风格。

建议新增独立业务模块：

```text
bocoo-modules-product
  src/main/java/com/bocoo/product/
    controller/
    service/
    service/impl/
    mapper/
    domain/entity/
    domain/bo/
    domain/vo/
    domain/dto/
    domain/result/
    engine/
```

如果第一期暂不新增 Maven module，也至少保持独立包名，不放进 `bocoo-modules-system`。

### 4.1 生成器优先的范围

这些页面和表更适合先走代码生成器骨架：

| 范围 | 说明 |
| --- | --- |
| 产品分类 | tree/list、增删改查、状态切换、引用检查局部增强 |
| 物料管理 | 标准分页、详情抽屉、导出 |
| 组件库 | 标准分页、组件和物料引用关系局部增强 |
| 资料资产 | 标准分页、OSS 文件引用、预览字段局部增强 |
| 资料绑定 | 标准分页、批量绑定接口局部增强 |
| 产品模型 | 标准分页、销售变体子表局部增强 |
| 价格方案列表 | 标准分页、版本状态展示局部增强 |
| 价格规则项列表 | 标准分页、规则详情抽屉局部增强 |
| 缺口任务 | 标准分页、分配/处理动作局部增强 |
| 同步日志 | 标准分页、重试动作局部增强 |
| 发布包列表 | 标准分页、详情只读 |
| 导入批次 | 标准分页、详情抽屉、状态切换 |
| 导入行级问题 | 标准分页、问题修正和处理状态局部增强 |

这些页面不要从零写普通 CRUD。先按生成器产出：

```text
Controller
  -> Service
  -> ServiceImpl
  -> Mapper
  -> Entity / BO / VO
  -> Mapper XML
  -> admin-ui API 文件和 grid/list 页面
```

然后再按产品能力业务补充：

- 引用检查。
- 批量绑定。
- 状态流转。
- 宽读模型字段。
- 发布/快照摘要字段。
- i18n key 和 UTC 处理。

### 4.2 需要手工增强的范围

下面这些不能只靠生成器：

| 范围 | 原因 | 增强方式 |
| --- | --- | --- |
| 工作台 | 聚合 read model、缺口、同步事件 | 自定义 Service 查询和 VO |
| 配置录入工作台 | 三栏编辑、问题/答案/规则聚合 | 自定义 editor VO 和保存接口 |
| 配置求值 | 涉及规则、校验、自动带出 | `ConfigEvaluationEngine` |
| BOM 构建 | 涉及用量、组件、答案组合 | `BomBuildEngine` |
| 价格试算 | 涉及矩阵、公式、加价、折扣 | `PriceCalculationEngine` |
| 发布检查 | 涉及配置、价格、资料、BOM、快照 | `PublishCheckEngine` |
| 发布包生成 | 需要不可变宽表和 hash | `SnapshotBuildEngine` + 发布 Service |
| 快照实例 | 订单、ERP、MES 历史必须自包含 | `ProductOrderSnapshotService` + 通用快照实例表 |
| 销售只读总览 | 面向销售的聚合读模型 | 自定义只读 VO |
| 完整导入解析 | 涉及 Excel/AI、字段映射、冲突合并 | 后续 `ProductImportService` 扩展，不放 Controller |

原则：

- 普通维护能力贴生成器。
- 聚合查询用自定义 VO。
- 复杂计算进 engine。
- 发布、快照、价格、BOM 不放在 Controller。

### 4.2.1 前端菜单和页面风格约束

菜单不是一个“产品能力 / 产品中心”大入口。正式 `sys_menu` 建议按三个一级菜单组织，侧边栏仍保持两级：

| 一级菜单 | 二级入口 |
| --- | --- |
| 基础资料 | 工作台、产品分类、物料管理、辅材管理、资料资产、资料绑定 |
| 配置与价格 | 产品模型、销售变体、问题组模板、配置模板、价格中心、报价预览 |
| 发布与应用 | 测试发布、审核审批、缺口待办、发布包、同步日志、导入中心、销售只读总览 |

标准维护页必须贴近系统管理下 `角色管理`、`菜单管理` 这类页面风格：

```text
app-container
  -> el-form inline 查询区
  -> el-row toolbar + right-toolbar
  -> el-table / tree-table
  -> pagination
  -> el-drawer 表单，短操作可用 el-dialog / ElMessageBox
  -> v-hasPermi 控制按钮
```

弹窗和抽屉标准：

- 新增、编辑、详情、审核详情、引用检查、影响分析默认使用右侧抽屉。
- 删除确认、启停确认、审核通过/拒绝原因、密码重置、导入选择、图片预览、批量粘贴预览等短操作可以用小弹窗或 `ElMessageBox`。
- 历史页面不豁免：系统用户、商家用户、菜单、角色、参数、字典、岗位、部门、OSS 配置、公告、操作日志详情等旧弹窗页面都列入 P0 标准化改造。
- 代码生成器 Vue 模板必须先对齐抽屉规则，否则产品能力普通维护页生成后必须立即替换为抽屉。

高频主功能不允许藏在 tabs 里：

| 当前组合页 / tab | 正式菜单处理 |
| --- | --- |
| 基础信息页内的分类、物料、组件 | 拆为基础资料下的产品分类、物料管理、辅材管理 |
| 组件资料页内的资料资产、资料绑定 | 拆为基础资料下的资料资产、资料绑定 |
| 产品模型页内的模型、销售变体 | 拆为配置与价格下的产品模型、销售变体 |
| 发布页内的审批、发布包、同步日志 | 拆为发布与应用下的审核审批、发布包、同步日志 |
| 销售只读页里的报价动作 | 拆为配置与价格下的报价预览 |

只有 5 个页面允许明显自定义内容区：工作台、配置模板、价格中心、测试发布、销售只读总览。即便是这 5 个页面，也只能自定义内容区，不重做 Layout、Sidebar、Navbar、TagsView、权限和请求体系。

`商家审核`、商家资料等页面带有业务审核表单和资料展示需求，视觉和结构可以作为例外参考，但不能作为普通 grid/list 页面模板。产品能力的普通维护页应优先对齐 `角色管理`、`菜单管理` 的代码结构和交互节奏。

### 4.3 Controller

Controller 只做：

- 接收 BO / PathVariable / PageQuery。
- 权限注解。
- 参数校验。
- 调用 Service。
- 返回 `R` 或 `TableDataInfo`。

Controller 禁止：

- 写规则求值逻辑。
- 拼 JSON 快照。
- 直接操作 Mapper。
- 直接返回 Entity。
- 处理复杂事务。

普通 CRUD Controller 结构应贴近生成器：

```text
list(Bo bo, PageQuery pageQuery) -> TableDataInfo<Vo>
export(Bo bo, HttpServletResponse response) -> ExcelUtil.exportExcel(...)
getInfo(id) -> R<Vo>
add(@Validated @RequestBody Bo bo) -> R<Void>
edit(@Validated @RequestBody Bo bo) -> R<Void>
remove(ids) -> R<Void>
```

### 4.4 Service

Service 分两类。

普通 CRUD Service 贴近生成器：

```text
queryPageList(Bo bo, PageQuery pageQuery)
queryList(Bo bo)
insertByBo(Bo bo)
updateByBo(Bo bo)
validEntityBeforeSave(Entity entity)
buildQueryWrapper(Bo bo)
```

业务编排 Service 负责：

- 校验业务唯一性、状态流转、引用关系。
- 调用 engine 执行配置求值、价格试算、BOM 构建、发布检查。
- 组装 VO 和 read model。
- 管理事务边界。

需要事务的操作：

- 新增/修改核心主数据及其子表。
- 保存配置模板版本和问题/答案结构。
- 保存价格方案版本和规则项。
- 发布检查结果落库。
- 发布包生成、审核记录、outbox 写入。
- 订单快照构建和保存。

### 4.5 Engine

复杂业务能力必须进入 engine，不散在 Service 方法里：

| Engine | 职责 |
| --- | --- |
| `ConfigEvaluationEngine` | 配置可见性、禁用、校验、自动带出、答案组合 |
| `BomBuildEngine` | 根据配置结果生成 BOM 摘要 |
| `PriceCalculationEngine` | 价格命中、矩阵价、公式价、加价、折扣、最终价 |
| `PublishCheckEngine` | 配置、价格、资料、组件、BOM、快照、同步检查 |
| `SnapshotBuildEngine` | 构建发布包快照和订单产品能力快照 |

engine 输入输出使用 DTO / Result，不直接依赖 Controller BO。

### 4.6 Mapper

Mapper 负责数据访问：

- 简单 CRUD 使用生成器风格的 `BaseMapperPlus<Entity, Vo>`。
- 复杂列表和 read model 查询可写自定义 SQL。
- 大 JSONB 字段只在详情、发布、快照接口读取，列表不要默认全量查。
- 发布包、快照、read model 读取优先走宽表，不在 Mapper 中 join 十几张编辑表。

### 4.7 Entity / BO / VO

| 类型 | 约束 |
| --- | --- |
| Entity | 映射单表，继承 `BaseEntity`，不承载页面聚合逻辑 |
| BO | 请求入参，优先按生成器继承 `BaseBo`，含校验注解，不暴露审计字段给前端写 |
| QueryBo | 列表筛选，包含状态、编码、名称、时间范围等查询字段 |
| VO | 返回前端，允许冗余展示字段，例如分类名、版本号、状态摘要 |
| DetailVo | 编辑回显和详情，允许包含子集合 |
| ResultVo | 求值、试算、发布检查、快照构建结果 |

## 5. 权限和日志约束

权限码要和前端按钮保持一致：

```text
product:center:view
product:center:handle
product:base:list/add/edit/remove/export
product:model:list/query/add/edit/remove/publishCheck
product:template:list/edit/rule/test
product:asset:list/upload/bind
product:price:list/edit/test/publish
product:publish:list/check/approve/publish
product:publish:retrySync
product:sales-view:list/quote/download
product:import:list/query/add/edit/remove
```

数据库菜单和按钮权限必须同时满足：

- `sys_menu` 中存在一级目录、二级菜单和按钮权限。
- 前端 `v-hasPermi`、后端 `@SaCheckPermission`、`sys_menu.perms` 三方一致。
- 页面刷新不 404，后端 `component` 能被前端动态路由映射。
- 通过、拒绝、发布、重试、测试、绑定、上传等独立动作不能共用一个泛化权限。
- 角色授权页能看到按钮权限，非授权角色不显示对应按钮。

详细规范见 [fullstack-code-standards.md](../项目配置和代码风格/fullstack-code-standards.md) 的“数据库菜单和按钮权限规范”。

日志要求：

| 操作 | `BusinessType` |
| --- | --- |
| 新增 | `INSERT` |
| 修改、状态切换、分配、处理 | `UPDATE` 或 `SENSITIVE_OPERATION` |
| 删除 | `DELETE` |
| 导出 | `EXPORT` |
| 发布、审核、生成快照 | `SENSITIVE_OPERATION` |

## 6. UTC、i18n 和租户约束

### 6.1 UTC

- 新增时间字段落库按 PostgreSQL `timestamptz` 语义设计。
- Java 创建业务时间使用 `TimeUtils.utcNow()`。
- 禁止直接使用 `LocalDateTime.now()`。
- 前端提交绝对时间使用 `toUtcPayload()` / `withUtcDateRange()`。
- 前端展示使用 `formatUtc()`。

### 6.2 i18n

- Controller / Service 错误消息优先使用 message key。
- 新增前端可见文案只写 `i18n/locales/en_US.json`。
- 后端运行时读取同步后的 JSON i18n。
- 产品名、答案名、客户资料说明属于业务数据字段，不走 UI i18n key，但发布包和快照要保存当时的中英文值。

### 6.3 租户

当前产品能力中心按平台共享主数据设计，业务上不按商户租户拆分。

技术落地必须二选一：

| 方案 | 要求 |
| --- | --- |
| 租户忽略表 | 产品能力核心表加入租户忽略配置，查询不被租户拦截 |
| 平台租户 | 表保留 `tenant_id`，统一写平台租户 ID |

无论选择哪种，都必须在技术实施计划逐表确认，不能让租户拦截器误伤平台数据。

## 7. 发布、求值、快照权威边界

下面这些能力必须以后端为准：

| 能力 | 权威来源 |
| --- | --- |
| 配置可见性和校验 | `ConfigEvaluationEngine` |
| BOM 生成 | `BomBuildEngine` |
| 价格计算 | `PriceCalculationEngine` |
| 发布检查 | `PublishCheckEngine` |
| 发布包生成 | `SnapshotBuildEngine` / 发布 Service |
| 快照实例 | `ProductOrderSnapshotService` / `pc_product_snapshot_instance` |

前端可以做轻量交互预览，但不能把本地计算结果作为发布、报价、订单保存依据。

## 8. 缓存策略

第一版不把 Redis 作为共享产品能力中心的核心依赖。快照、发布包、审核结果、订单保存依据必须落 PostgreSQL，不允许只存在 Redis。

当前性能优先靠三件事解决：

| 策略 | 说明 |
| --- | --- |
| 宽表和冗余字段 | 发布包、订单快照、销售只读读模型保存当时的 code、name、version、币种、单位、状态摘要、资料版本等冗余字段 |
| read model | 工作台、销售只读、订单消费优先读 `pc_read_model`、发布包和订单快照，不实时 join 编辑态明细表 |
| JSONB 摘要 | 配置结果、价格明细、BOM 摘要、资料清单按快照语义保存，读详情时直接取快照 |

Redis 后续可以作为 cache-aside 派生缓存，但只能缓存“可从数据库重建”的结果：

| 可缓存对象 | 建议 key | 失效方式 |
| --- | --- | --- |
| 已发布包详情 | `pc:published-package:{packageId}:{contentHash}` | 发布包不可变，hash 变化即新 key |
| 销售只读详情 | `pc:sales-product:{productCode}:{publishedVersion}` | 发布新版本后通过 outbox 事件失效 |
| 价格试算结果 | `pc:price-test:{priceVersionId}:{inputHash}` | 短 TTL，仅用于频繁试算提速 |
| 工作台统计 | `pc:workbench:summary` | 30-60 秒短 TTL，允许轻微延迟 |

以下数据不要放进 Redis 作为权威数据：

- 配置草稿、价格草稿、问题答案编辑态。
- 发布检查 blocker / warning 的最终结果。
- 审核记录、发布记录、发布包 hash。
- 缺口任务处理状态。
- `pc_product_sync_outbox` 同步状态。
- 订单产品能力快照、价格快照、BOM 快照。

如果后续引入 Redis，必须遵守：

- 数据库提交成功后再写缓存或发送失效事件，不能先写缓存。
- 缓存 miss 必须回源数据库，不能让页面或订单流程依赖缓存命中。
- 版本化 key 优先于手工删除 key，发布包这类不可变对象优先用 `version/contentHash` 区分。
- 同步事件失败时，缓存只能回退到旧发布版本，不能自动读取未发布草稿。
- 没有真实性能证据前，P0 不写 Redis 缓存代码。

## 9. 第一版 API 实施顺序

建议按页面闭环推进：

1. 工作台只读 API：summary、progress、priority、sync-events。
2. 基础信息和产品模型 CRUD API。
3. 配置模板 editor API 和 evaluate API。
4. 价格方案 editor API 和 price test API。
5. 发布检查和发布包 API。
6. 销售只读 API。
7. 订单 build-snapshot 内部 API / Service。
8. 导入中心批次和行级问题基础 API。
9. 完整 Excel/AI 导入解析、ERP/MES 同步、AI Agent 和价格增强。

每一步完成后都要检查：

- API 是否有权限码。
- 返回是否使用 `R` / `TableDataInfo`。
- 是否没有返回 Entity。
- 时间是否 UTC。
- 错误消息是否走 i18n。
- 是否没有让前端复制后端规则。
- 是否保留未来独立服务的 API 边界。
