# Implementation Decisions: 共享产品能力中心 Batch 0

## 执行授权

用户已在本轮授权进入 `/do`，并允许：

- 修改数据库相关设计和必要 SQL 草案。
- 添加或升级必要依赖。
- 联网查询资料、下载素材。
- 运行 build、typecheck、lint、test、browser validation 等验证。
- 验证完成后自动 `/archive`，并继续执行后续 `/do` 任务。

约束：

- 工具层如遇到沙箱、联网、GUI 或写出 workspace 限制，仍按 Codex 审批机制执行。
- 新增依赖必须有明确必要性；能复用现有依赖时不新增。
- 生产环境配置不作为第一批默认修改对象。
- SQL 草案可以生成，但真正执行数据库变更必须以项目落库流程为准。

## PCC-00 默认决策

| 决策项 | 默认结论 | 理由 |
| --- | --- | --- |
| 后端模块名 | 推荐新建 `bocoo-modules-product` | 产品能力不是 system 能力，也不是 dealer/pay 私有能力；新模块最利于后续拆服务 |
| 包名 | `com.bocoo.product` | 和模块语义一致，避免污染 `com.bocoo.system` |
| 前端菜单名 | `产品能力` | 当前框架二级菜单体验最好，不继续叫“产品中心” |
| 前端路由 | `/product-center/*` | 保持文档既定技术路径，不影响菜单显示名 |
| 后端 API 前缀 | `/product-capability/*` | 表达共享能力边界，便于后续独立服务 |
| 第一批 tenant 策略 | 推荐 `pc_*` 核心表加入租户忽略表 | 用户已确认该能力按平台共享主数据处理，避免平台数据被业务租户误拦截 |
| 产品能力快照实例 tenant | 跟随产品能力中心共享表策略 | 用户确认快照实例统一放 `bocoo-modules-product`，用 `source_system/source_biz_*` 承接订单、ERP、MES 来源，后期随产品能力中心独立拆服务 |
| SQL 放置 | 先生成 PostgreSQL 草案到 `sql/postgresql/product_capability.sql` | 项目当前没有 migration 框架，已有 SQL 位于 `sql/postgresql/base.sql` 和 `pay.sql` |
| Redis | Batch 1 不使用 Redis | 快照、发布包、审核、订单依据必须落 PostgreSQL，缓存后续按性能证据引入 |
| 第一批交付 | 基础信息 + 产品模型骨架 + 工作台骨架 + 菜单/API/i18n 基线 | 控制范围，避免一次性吞掉所有 P0 |

## Batch 1 边界

Batch 1 只做可稳定落地的基础闭环：

- 后端模块/包骨架。
- 分类、物料、组件、资料资产、资料绑定、引用检查。
- 产品模型、销售变体基础骨架和列表摘要。
- 工作台只读 API 和前端骨架，可先支持空数据/基础统计。
- 产品能力菜单、路由、前端 API 文件和 i18n key。

Batch 1 不做：

- 规则求值完整引擎。
- 价格计算完整引擎。
- 发布包正式 release。
- 订单快照接入订单保存流程。
- Redis 缓存。
- AI 助手、导入中心、ERP/MES 深度同步。

## Batch 7 边界调整：快照实例归产品能力中心

用户在 Batch 7 明确希望“统一放在 product 里面，后期好拆分”。因此后续快照持久化不再按 `order_product_snapshot` 订单私有表推进，而改为产品能力中心通用表：

- 表名：`pc_product_snapshot_instance`。
- 所属模块：`bocoo-modules-product`。
- 业务语义：产品能力快照实例，不是订单表。
- 来源字段：`source_system`、`source_biz_type`、`source_biz_no`、`source_biz_line_no`。
- 来源范围：ORDER、ERP、MES、QUOTE 等系统都可复用。
- 订单系统职责：保存或引用 `snapshot_id/snapshot_hash`，不拥有产品能力快照生成逻辑。
- 拆服务策略：后期 `bocoo-modules-product` 可整体拆出为共享产品能力服务，订单/ERP/MES 通过 API 或同步事件调用。

## Pause Points

虽然用户已给总体授权，以下情况仍必须记录并谨慎处理：

- 新增 `bocoo-modules-product` 导致 Maven reactor 或 admin 依赖调整失败。
- SQL 草案和文档设计出现字段冲突。
- 租户忽略表配置位置需要修改生产配置。
- 需要新增第三方依赖，但现有项目已有等价能力。
- 规则 JSON schema、价格规则 schema、发布包 hash schema 需要业务确认。

## PCC-00 Acceptance

- 模块名：推荐 `bocoo-modules-product`。
- tenant：推荐 `pc_*` 核心表和通用快照实例表租户忽略，来源系统侧自行保存 `snapshot_id/snapshot_hash` 引用。
- migration/SQL：可生成 PostgreSQL 草案，不直接执行 SQL。
- 第一批：基础能力闭环，不把所有 P0 一次做完。
