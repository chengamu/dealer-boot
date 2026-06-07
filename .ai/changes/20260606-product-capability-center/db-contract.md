# PCC-01 数据库落地 Contract

## 1. 执行边界和设计缺口

本文件只细化共享产品能力中心数据库落地计划，不生成 SQL、不修改 migration、不执行数据库命令。

分析边界：

- 需求源：`docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- 功能边界：`docs/产品配置中心/配置中心功能拆分清单.md`
- 项目约束：`.ai/requirements/20260606-product-capability-center.md`
- 现有 SQL 组织：`sql/postgresql/base.sql`、`sql/postgresql/pay.sql`

当前设计缺口不是“表不够”，而是草案已经列出表和字段，但还缺少进入实现前必须冻结的三件事：

- 表级分批顺序，避免第一批一次性落完整配置、价格、发布、订单快照。
- tenant 策略逐表决策，避免平台共享主数据被租户拦截器误过滤。
- PostgreSQL SQL 草案放置和审查清单，避免在没有 migration 框架的情况下直接改初始化脚本或执行 SQL。

## 2. Batch 1/2/3 表级落地顺序

### Batch 1：基础信息和产品入口

目标：先建立产品能力中心的基础主数据、资料绑定、产品模型和销售入口，使后端 CRUD、引用检查、工作台/列表骨架有稳定表结构。

落地顺序：

| 顺序 | 表名 | 必要性 | 说明 |
| --- | --- | --- | --- |
| 1 | `pc_category` | 必须 | 产品分类树，后续产品模型、搜索、权限预留都依赖它。 |
| 2 | `pc_material` | 必须 | 面料、颜色、电机、型材、服务项等基础口径，组件和 BOM 依赖它。 |
| 3 | `pc_component` | 必须 | 配置答案和 BOM 可带出的组件库，引用 `pc_material` 的 ID/code 快照。 |
| 4 | `pc_media_asset` | 必须 | 资料业务版本和客户可见性；文件本体仍复用现有 OSS。 |
| 5 | `pc_media_binding` | 必须 | 将资料绑定到产品、问题、答案、组件、物料等对象。 |
| 6 | `pc_product_model` | 必须 | 可配置产品模型，是配置模板、价格、发布包的上游入口。 |
| 7 | `pc_sales_variant` | 必须 | 销售/下单可见入口，后续绑定默认配置模板和价格方案。 |
| 8 | `pc_read_model` | 建议 Batch 1 建表 | 工作台和列表避免实时 join；Batch 1 可只刷新基础信息摘要，Batch 2 发布后再扩展发布摘要。 |

Batch 1 暂不落：

- `pc_config_template*`、问题、答案、规则、用量、BOM：需要规则 schema 和 engine contract，放 Batch 2。
- `pc_price_*`：价格计算和试算 contract 未冻结，放 Batch 2。
- `pc_published_package`、`pc_publish_check`、`pc_approval_record`、`pc_product_sync_outbox`：发布闭环依赖配置和价格，放 Batch 2。
- `pc_product_snapshot_instance`：通用产品能力快照实例表，放 Batch 7，统一支持 ORDER、ERP、MES 等来源。

### Batch 2：配置、价格、发布闭环

目标：形成“配置模板 + 价格方案 + 发布检查 + 不可变发布包 + outbox”的后端权威闭环。

落地顺序：

| 顺序 | 表名 | 必要性 | 说明 |
| --- | --- | --- | --- |
| 1 | `pc_config_template` | 必须 | 配置模板主记录，管理当前版本和已发布版本指针。 |
| 2 | `pc_config_template_version` | 必须 | 草稿、提交、发布版本载体，`schema_json` 和 `draft_hash` 在此冻结。 |
| 3 | `pc_question_group` | 必须 | 可复用问题组，供模板版本组织问题。 |
| 4 | `pc_config_question` | 必须 | 配置问题定义，含校验 JSON 和展示规则摘要。 |
| 5 | `pc_config_option` | 必须 | 答案选项，含组件、资料、价格影响摘要 JSON。 |
| 6 | `pc_config_rule` | 必须 | 可见性、禁用、校验、自动带出等规则，后端求值权威依赖它。 |
| 7 | `pc_usage_model` | 必须 | 面积、周长、长度、固定数量等用量算法。 |
| 8 | `pc_bom_rule` | 必须 | 配置结果到组件/BOM 的带出规则。 |
| 9 | `pc_price_plan` | 必须 | 价格方案主记录。 |
| 10 | `pc_price_plan_version` | 必须 | 价格草稿、提交、发布版本，含价格摘要和内容 hash。 |
| 11 | `pc_price_rule_item` | 必须 | 第一版统一承载矩阵价、公式价、固定组件价、运费等价格项。 |
| 12 | `pc_publish_check` | 必须 | 发布检查结果明细，不能只存通过/失败。 |
| 13 | `pc_approval_record` | 必须 | 提交、审核、发布、驳回、归档的可追溯记录。 |
| 14 | `pc_gap_task` | 必须 | 缺资料、缺价格、缺组件、测试失败等待办。 |
| 15 | `pc_published_package` | 必须 | 发布后不可变宽表；报价、订单、ERP、MES、销售只读优先消费它。 |
| 16 | `pc_product_sync_outbox` | 必须 | 发布包写入成功后同事务写 outbox，供后续同步追踪。Batch 9 已扩展为 ORDER/ERP/MES 多目标同步事件。 |
| 17 | `pc_read_model` 扩展刷新 | 必须 | Batch 1 已建表时，本批补齐发布状态、价格状态、资料状态、AI 上下文。 |

### Batch 3：消费、快照和后续扩展

目标：订单和销售只读消费发布包，历史订单不依赖当前产品中心源表。

落地顺序：

| 顺序 | 表名 | 必要性 | 说明 |
| --- | --- | --- | --- |
| 1 | `pc_product_snapshot_instance` | 必须 | 下单、报价、ERP、MES 消费时由产品能力中心保存完全自包含快照实例，不强 FK 到来源业务表。 |
| 2 | `pc_change_impact` | TODO | 第一版可由发布检查 JSON 或实时计算替代，稳定后再落表。 |
| 3 | `pc_import_batch` | 已在 Batch 8 落基础表 | 第一阶段保存导入批次、预览和状态；完整 Excel/AI 自动识别后置。 |
| 4 | `pc_import_row_issue` | 已在 Batch 8 落基础表 | 保存行级错误、警告、原始行和修正行。 |
| 5 | `pc_agent_task` | TODO | ConfigAgent/PricingAgent 第一版不自动发布、不自动改价，可先用操作日志或轻量记录。 |
| 6 | `pc_legacy_mapping` | TODO | 老库适配稳定后再沉淀映射。 |

## 3. tenant 策略建议

### 3.1 总体建议

推荐方案：`pc_*` 核心表按平台共享主数据处理，加入 `bocoo.tenant.ignore-tables`，业务查询不按商户 tenant 过滤。原因：

- 产品能力中心是订单、报价、ERP、MES、售后共用的 source of truth，不是商户私有配置。
- 发布包、read model、outbox 是跨消费者读取面，按商户复制会导致发布一致性和回溯成本升高。
- 来源系统已经通过 `pc_product_snapshot_instance` 保存当时消费结果，不需要回写或拥有 `pc_*` 源数据。

备选方案：如果实现阶段必须复用带 `tenant_id` 的 BaseEntity 或代码生成器模板，可以保留 `tenant_id bigint NOT NULL CHECK (tenant_id <> 0)`，并固定平台数据 `tenant_id = 1`。此时仍建议将 `pc_*` 表加入租户忽略表，避免运行时按当前商户过滤；`tenant_id` 只作为审计和兼容字段，不作为业务分区。

禁止方案：

- 不使用 `tenant_id = 0` 作为平台数据。
- 不让订单、ERP、MES 直接写 `pc_*` 核心表。
- 不在 Batch 1 引入商户专属产品配置复制。

### 3.2 逐表 tenant 策略

| 表名 | 推荐 tenant 策略 | 平台租户备选 | 说明 |
| --- | --- | --- | --- |
| `pc_category` | 忽略表 | 可保留 `tenant_id = 1` | 平台分类树。 |
| `pc_material` | 忽略表 | 可保留 `tenant_id = 1` | 平台物料口径，后续 ERP/MES 消费。 |
| `pc_component` | 忽略表 | 可保留 `tenant_id = 1` | 平台组件库。 |
| `pc_media_asset` | 忽略表 | 可保留 `tenant_id = 1` | 资料业务版本；文件权限仍复用 OSS/文件能力。 |
| `pc_media_binding` | 忽略表 | 可保留 `tenant_id = 1` | 资料绑定关系是平台产品能力的一部分。 |
| `pc_product_model` | 忽略表 | 可保留 `tenant_id = 1` | 产品模型是共享 source of truth。 |
| `pc_sales_variant` | 忽略表 | 可保留 `tenant_id = 1` | 销售入口共享，是否客户可见由发布包/API 控制。 |
| `pc_config_template` | 忽略表 | 可保留 `tenant_id = 1` | 模板主记录共享。 |
| `pc_config_template_version` | 忽略表 | 可保留 `tenant_id = 1` | 模板版本共享，发布后不可原地改。 |
| `pc_question_group` | 忽略表 | 可保留 `tenant_id = 1` | 可复用问题组。 |
| `pc_config_question` | 忽略表 | 可保留 `tenant_id = 1` | 模板版本内问题定义。 |
| `pc_config_option` | 忽略表 | 可保留 `tenant_id = 1` | 模板版本内答案定义。 |
| `pc_config_rule` | 忽略表 | 可保留 `tenant_id = 1` | 后端求值规则，不按商户隔离。 |
| `pc_usage_model` | 忽略表 | 可保留 `tenant_id = 1` | 用量算法共享。 |
| `pc_bom_rule` | 忽略表 | 可保留 `tenant_id = 1` | BOM 带出规则共享。 |
| `pc_price_plan` | 忽略表 | 可保留 `tenant_id = 1` | 第一版平台价；未来商户专属价用 `price_scope` 或独立 scope 字段扩展。 |
| `pc_price_plan_version` | 忽略表 | 可保留 `tenant_id = 1` | 价格版本共享。 |
| `pc_price_rule_item` | 忽略表 | 可保留 `tenant_id = 1` | 价格规则项共享；常查 scope 字段不要只放 JSONB。 |
| `pc_publish_check` | 忽略表 | 可保留 `tenant_id = 1` | 发布检查属于平台治理。 |
| `pc_approval_record` | 忽略表 | 可保留 `tenant_id = 1` | 审核记录属于平台治理。 |
| `pc_gap_task` | 忽略表 | 可保留 `tenant_id = 1` | 缺口任务属于产品运营治理，不是商户任务。 |
| `pc_published_package` | 忽略表 | 可保留 `tenant_id = 1` | 发布包跨订单、报价、ERP、MES 消费。 |
| `pc_product_snapshot_instance` | 忽略表 | 可保留 `tenant_id = 1` | 通用产品能力快照实例，用 `source_system/source_biz_*` 区分订单、ERP、MES 等来源。 |
| `pc_read_model` | 忽略表 | 可保留 `tenant_id = 1` | 工作台、搜索、AI 读取宽表。 |
| `pc_product_sync_outbox` | 忽略表 | 可保留 `tenant_id = 1` | 发布同步事件，当前覆盖 ORDER/ERP/MES 目标系统。 |

### 3.3 `pc_product_snapshot_instance` 通用快照实例归属

用户已确认快照持久化统一放在产品能力中心，便于后期把 `bocoo-modules-product` 拆成独立共享服务。因此不再新建订单私有 `order_product_snapshot` 表，改为产品中心通用表 `pc_product_snapshot_instance`：

- 表名使用 `pc_` 前缀，加入 `bocoo.tenant.ignore-tables`。
- 不保存订单专属状态，不反向依赖订单表。
- 通过 `source_system`、`source_biz_type`、`source_biz_no`、`source_biz_line_no` 标识调用来源。
- 保存 `package_id/package_code/package_hash`、模型编码、变体编码、模板版本、价格版本、用户输入、完整 `snapshot_json` 和 `snapshot_hash`。
- 订单、ERP、MES 侧只引用 `snapshot_id/snapshot_hash` 或按需冗余少量展示字段。
- 历史展示只读快照，不实时读取当前 `pc_*` 源表和当前价格。

## 4. PostgreSQL SQL 草案放置建议

当前项目没有 migration 框架，已有 PostgreSQL 初始化脚本集中在：

- `sql/postgresql/base.sql`
- `sql/postgresql/pay.sql`

建议后续 SQL 草案放置：

| 文件 | 用途 | 建议 |
| --- | --- | --- |
| `sql/postgresql/product_capability.sql` | `pc_*` 产品能力中心表草案 | 后续经人工评审后新建；不要在 PCC-01 中创建或执行。 |
| `sql/postgresql/base.sql` | 平台基础表 | 不建议塞入 `pc_*` 大量业务表。 |
| `sql/postgresql/pay.sql` | 支付模块表 | 不修改。 |
| `sql/base.sql` | 旧 MySQL 风格脚本 | 不补产品能力中心 MySQL SQL。 |

SQL 草案风格应对齐现有 PG 脚本：

- 使用 `CREATE TABLE IF NOT EXISTS`。
- 使用 `CREATE INDEX IF NOT EXISTS` / `CREATE UNIQUE INDEX IF NOT EXISTS`。
- 主键使用 `bigint PRIMARY KEY`，由 MyBatis-Plus 雪花 ID 生成。
- 时间字段使用 `timestamptz`，注释明确 `UTC timestamptz`。
- 所有表和字段都补 `COMMENT ON TABLE` / `COMMENT ON COLUMN` 中文注释。
- 平台租户备选方案下，`tenant_id` 必须 `CHECK (tenant_id <> 0)`，平台固定 `tenant_id = 1`。

## 5. 全表全字段检查清单

### 5.1 中文注释

- 每张表必须有 `COMMENT ON TABLE xxx IS '中文表注释';`。
- 每个字段必须有 `COMMENT ON COLUMN xxx.field IS '中文字段注释';`。
- 状态字段必须写出枚举含义，例如 `状态：1正常，0停用`。
- JSONB 字段必须写明保存内容，不只写 `JSON`。
- hash 字段必须写明 hash 来源对象。
- 审计字段中文注释按项目口径统一：`创建者ID`、`创建者`、`创建时间，UTC timestamptz`、`更新者`、`更新时间，UTC timestamptz`。

### 5.2 UTC 和时间字段

- 所有业务时间字段使用 `timestamptz`，不使用 `timestamp without time zone`。
- 字段注释必须包含 `UTC timestamptz`。
- Java 写入时间后续使用 `TimeUtils.utcNow()`，不直接 `LocalDateTime.now()`。
- `effective_from` / `effective_to` / `published_time` / `checked_time` / `operate_time` / `due_time` / `refresh_time` / `next_retry_time` / `created_time` / `processed_time` 都必须纳入 UTC 检查。
- scheduler / outbox retry 后续实现时如涉及 cron，必须单独确认时区。

### 5.3 JSONB

- 仅将结构多变、发布后不可变、快照自包含或读取摘要类内容放入 `jsonb`。
- JSONB 内容必须包含 `schemaVersion`。
- JSONB 中的关键业务对象必须同时保存 `id/code/nameCn/nameEn/versionNo`，不能只保存 ID。
- 常用筛选、权限判断、状态、编码、版本、发布时间不得只放 JSONB，必须拉成普通列。
- 发布包、订单快照、事件 payload 不允许保存密钥、连接串、Token。
- 初期不默认创建 JSONB GIN 索引；只有查询模式稳定并经过 explain 后再加。

### 5.4 hash

- `content_hash`、`draft_hash`、`before_hash`、`after_hash`、`package_hash`、`snapshot_hash` 等字段统一 `varchar(100)`。
- 推荐后续实现使用稳定序列化后的 SHA-256 hex；如果更换算法，必须在字段注释或 payload 中带算法版本。
- 发布包 hash 来源：配置结构、规则、用量、BOM、资料、价格摘要、有效期等发布内容。
- 价格版本 hash 来源：价格规则项、币种、有效期、价格摘要。
- 订单快照 hash 来源：发布包引用、用户选择、尺寸、配置、价格、BOM、资料快照。
- 审核记录必须保存操作前后 hash，便于追踪发布差异。

### 5.5 索引

- 所有业务 code 应有唯一或普通索引，软删除表优先部分唯一索引：`WHERE del_flag = '0'`。
- 列表常查条件建立组合索引：`status`、`del_flag`、`biz_status`、`version_status`、`publish_status`、`sort`、`create_time DESC`。
- 版本表必须有 `(主表ID, version_no)` 唯一索引。
- 发布包必须有 `(package_code, package_version_no)` 唯一索引和 `content_hash` 索引。
- outbox 必须有 `sync_status, next_retry_time, created_time` 索引。
- 订单快照必须有订单行索引、发布包引用索引、`snapshot_hash` 索引。
- 不强制跨系统外键；核心引用通过 ID + code + name 快照 + hash 保证可追溯。

## 6. 第一批必须做和 TODO

### 第一批必须做

第一批如果进入 SQL 草案和 Java 实现，必须至少覆盖：

- `pc_category`
- `pc_material`
- `pc_component`
- `pc_media_asset`
- `pc_media_binding`
- `pc_product_model`
- `pc_sales_variant`

如果 Batch 1 工作台/列表要读取真实数据库而不是空数据，建议同时落：

- `pc_read_model`

第一批必须同步完成的非 SQL 决策：

- 确认 `pc_*` 是否全部加入 `bocoo.tenant.ignore-tables`。
- 若保留 `tenant_id`，确认固定平台租户 `tenant_id = 1`，并避免 `tenant_id = 0`。
- 冻结第一批 code 唯一索引、软删除策略、中文注释和 UTC 字段注释。
- 明确资料资产只维护业务版本和绑定，文件本体继续复用现有 OSS。

### 保留 TODO / Deferred

保留 TODO，不在第一批实现：

- 配置模板、问题、答案、规则、用量、BOM：`pc_config_template`、`pc_config_template_version`、`pc_question_group`、`pc_config_question`、`pc_config_option`、`pc_config_rule`、`pc_usage_model`、`pc_bom_rule`。
- 价格中心：`pc_price_plan`、`pc_price_plan_version`、`pc_price_rule_item`。
- 发布治理：`pc_publish_check`、`pc_approval_record`、`pc_gap_task`、`pc_published_package`、`pc_product_sync_outbox`。
- 快照实例：`pc_product_snapshot_instance`，归产品能力中心 Batch 7。
- 后置增强：`pc_change_impact`、`pc_agent_task`、`pc_legacy_mapping`。
- 导入中心增强：`pc_import_batch` 和 `pc_import_row_issue` 已有基础表，完整 Excel/AI 自动解析后置。
- 商户专属价、促销、客户等级折扣、真实 ERP/MES 推送和回调、完整 ERP/MES 生产领料库存、向量数据库、Agent 自动发布/自动改价。

## 7. 验证方式和剩余风险

PCC-01 已做的验证只限静态阅读和 contract 收敛：

- 对照需求源确认 Batch 1/2/3 表级顺序。
- 对照现有 `sql/postgresql/base.sql`、`sql/postgresql/pay.sql` 确认项目 PG 脚本组织和注释风格。
- 未执行 SQL，未修改 SQL，未运行 Java 编译。

后续仍需环境级验证：

- 产品能力表加入 `bocoo.tenant.ignore-tables` 后，MyBatis 租户拦截器是否确实不追加 tenant 条件。
- 如果保留 `tenant_id = 1`，代码生成器、BaseEntity、查询条件和数据权限是否会把平台数据误过滤。
- `product_capability.sql` 草案生成后，需要人工审查全表全字段注释、UTC、jsonb、hash、索引，并在开发库单独验证。
- 来源系统业务表仍需确认如何保存 `snapshot_id/snapshot_hash` 引用；产品能力中心不反向依赖订单、ERP、MES 表。
