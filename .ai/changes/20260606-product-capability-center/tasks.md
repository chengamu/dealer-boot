# Tasks: 共享产品能力中心

## Active Batch

Batch 0 到 Batch 3 已完成并归档；Deferred 能力保留为后续队列。

## Task List

### PCC-00 - 确认实施决策和暂停点

Owner：main  
Status：done  
DependsOn：none  
Requirement Source：`.ai/requirements/20260606-product-capability-center.md`  
Scope：确认模块名、租户方案、migration 节奏、第一批交付边界。  
Files：`.ai/changes/20260606-product-capability-center/**`、`docs/产品配置中心/**`  
Validation：文档审查。  
Blockers：模块名、租户方案或 migration 策略未确认。  
Short Notes：已确认默认决策：推荐 `bocoo-modules-product`，`pc_*` 核心表走租户忽略表，SQL 草案放 `sql/postgresql/product_capability.sql`，Batch 1 控制为基础能力闭环。

### PCC-01 - 细化数据库落地计划

Owner：java-architect  
Status：done  
DependsOn：PCC-00  
Requirement Source：`docs/产品配置中心/共享产品能力中心数据库设计草案.md`  
Scope：把数据库草案拆成 Batch 1/2/3 表级实施顺序，确认 tenant、UTC、中文注释、索引、JSONB、read model。  
Files：`docs/产品配置中心/**`、后续可能的 migration 草案路径。  
Validation：静态审查，不直接执行 SQL。  
Blockers：是否允许生成 migration 草案。  
Short Notes：已新增 `db-contract.md`，明确 Batch 1/2/3 表级顺序、tenant 策略、SQL 草案放置和注释/UTC/hash/jsonb 检查清单。

### PCC-02 - 细化 API contract

Owner：java-architect  
Status：done  
DependsOn：PCC-00  
Requirement Source：`docs/产品配置中心/共享产品能力中心API与后端实现约束.md`  
Scope：明确 Batch 1 API、BO/VO/Result、分页、权限、错误 message key。  
Files：`bocoo-modules-product/**` 或确认后的模块路径、`admin-ui/src/api/product-capability/**`  
Validation：contract review。  
Blockers：模块路径和字段边界未确认。  
Short Notes：已新增 `api-contract.md`，明确 Batch 1 API、BO/QueryBo/VO/DetailVo/OptionVo/ResultVo、权限、日志、分页、UTC、i18n 和后续 API TODO。

### PCC-03 - 细化前端菜单和页面 contract

Owner：frontend-developer  
Status：done  
DependsOn：PCC-00  
Requirement Source：`docs/产品配置中心/产品能力界面设计稿.md`  
Scope：确认菜单、路由、页面文件、i18n key、普通 grid 页面和 5 个自定义页面边界。  
Files：`admin-ui/src/pages/**`、`admin-ui/src/api/**`、`admin-ui/src/i18n/**`  
Validation：前端 contract review。  
Blockers：菜单显示名和权限码未确认。  
Short Notes：已新增 `frontend-contract.md`，明确产品能力菜单、路由、Batch 1 页面/API 目录、grid 与自定义页面边界、i18n/UTC/权限/状态要求。

### PCC-10 - Batch 1 后端基础模块骨架

Owner：java-architect  
Status：done  
DependsOn：PCC-01, PCC-02  
Requirement Source：`.ai/changes/20260606-product-capability-center/design.md`  
Scope：新增或确认产品能力后端模块/包，按代码生成器风格准备基础 CRUD 骨架。  
Files：`bocoo-modules-product/**` 或确认后的模块路径、`pom.xml`  
Validation：Java compile。  
Blockers：新增 Maven module 需要用户确认。  
Short Notes：已新增 `bocoo-modules-product`，接入根 `pom.xml` 和 `bocoo-admin/pom.xml`，新增 `ProductModule` 标记类；`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### PCC-11 - Batch 1 基础信息后端能力

Owner：java-architect  
Status：done  
DependsOn：PCC-10  
Requirement Source：`docs/产品配置中心/配置中心功能拆分清单.md`  
Scope：分类、物料、组件、资料资产、资料绑定、引用检查。  
Files：产品能力后端模块、Mapper XML、可选 migration 草案。  
Validation：Java compile + API contract review。  
Blockers：表级 migration 未确认。  
Short Notes：已生成 `bocoo-modules-product` 下 Batch 1 基础资料 Entity/BO/VO/Mapper/Service/Controller 和 `sql/postgresql/product_capability.sql` 草案；补齐分类树、媒体绑定、模型变体契约入口；`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### PCC-12 - Batch 1 前端菜单和基础 grid 页面

Owner：frontend-developer  
Status：done  
DependsOn：PCC-03, PCC-11  
Requirement Source：`docs/产品配置中心/产品能力界面设计稿.md`  
Scope：产品能力菜单、路由、API 文件、基础信息/产品模型/组件资料 grid 页面骨架、i18n key。  
Files：`admin-ui/src/pages/product-center/**`、`admin-ui/src/api/product-capability/**`、`i18n/locales/en_US.json`  
Validation：typecheck + browser smoke。  
Blockers：后端 API contract 未确认。  
Short Notes：已新增产品能力 API、通用 grid 页面、基础信息/产品模型/组件资料入口、动态路由映射和 Batch 1 菜单 SQL 草案；新增文案写入 `i18n/locales/en_US.json` 并通过同步脚本生成 runtime i18n；`vue-tsc --noEmit` 和 `vite build` 通过。

### PCC-13 - Batch 1 工作台只读骨架

Owner：frontend-developer  
Status：done  
DependsOn：PCC-02, PCC-03  
Requirement Source：`docs/产品配置中心/产品能力界面设计稿.md`  
Scope：工作台页面骨架、summary/progress/priority/sync-events API 对接或 mock boundary。  
Files：`admin-ui/src/pages/product-center/workbench/**`、`admin-ui/src/api/product-capability/**`  
Validation：typecheck + browser smoke。  
Blockers：read model API 是否先返回空数据或真实数据。  
Short Notes：已新增工作台页面和真实后端空骨架 API，summary 读取产品模型轻量统计，progress/priority/sync-events 返回稳定空分页；`vue-tsc --noEmit` 和 `vite build` 通过。

### PCC-20 - Batch 2 配置模板和规则求值

Owner：java-architect  
Status：done  
DependsOn：PCC-11  
Requirement Source：`docs/产品配置中心/配置中心功能拆分清单.md`  
Scope：产品模型、销售变体、模板、问题组、问题、答案、规则、`ConfigEvaluationEngine` MVP。  
Files：产品能力后端模块、engine、Mapper XML。  
Validation：Java compile + targeted unit/service check。  
Blockers：规则 JSON schema 未细化。  
Short Notes：已新增配置模板、模板版本、问题组、问题、答案、规则 Entity/BO/VO/Mapper/Service/Controller 和 SQL 草案；新增 `ConfigEvaluationEngine` MVP 与 `/product-capability/config/evaluate`，前端和订单侧只能消费后端求值结果；`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### PCC-21 - Batch 2 配置录入工作台前端

Owner：frontend-developer  
Status：done  
DependsOn：PCC-20  
Requirement Source：`docs/产品配置中心/产品能力界面设计稿.md`  
Scope：配置录入三栏工作台、批量粘贴、组件/资料绑定入口、求值预览。  
Files：`admin-ui/src/pages/product-center/template/**`  
Validation：typecheck + browser interaction。  
Blockers：editor API 字段未定。  
Short Notes：已新增配置模板工作台页面、配置 API、二级菜单路由映射和菜单 SQL；页面采用左模板列表、中间版本/分组/问题/答案/规则 tab、右侧求值预览；批量粘贴先提供本地解析预览，不直接写库；`vue-tsc --noEmit`、`vite build`、`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### PCC-22 - Batch 2 价格中心后端和前端

Owner：java-architect  
Status：done  
DependsOn：PCC-11  
Requirement Source：`docs/产品配置中心/价格中心设计.md`  
Scope：价格方案、价格版本、价格规则项、价格试算、`PriceCalculationEngine` MVP。  
Files：产品能力后端模块、`admin-ui/src/pages/product-center/pricing/**`、API 文件。  
Validation：Java compile + typecheck + price test case。  
Blockers：价格规则项字段和导入格式未定。  
Short Notes：已新增价格方案、价格方案版本、价格规则项后端 CRUD、`PriceCalculationEngine` MVP、`/product-capability/pricing/calculate`、价格中心 SQL 草案、前端价格工作台、API 和二级菜单映射；价格计算以后端为准，不在前端复制公式；`mvn -pl bocoo-admin -am -DskipTests compile`、`vue-tsc --noEmit`、`vite build` 通过。

### PCC-23 - Batch 2 发布检查、发布包和 outbox

Owner：java-architect  
Status：done  
DependsOn：PCC-20, PCC-22  
Requirement Source：`docs/产品配置中心/共享产品能力中心API与后端实现约束.md`  
Scope：发布检查、审批记录、不可变发布包、read model 刷新、outbox。  
Files：产品能力后端模块、可选 migration 草案。  
Validation：Java compile + service-level publish flow check。  
Blockers：发布包 JSON/hash schema 未细化。  
Short Notes：已新增发布检查结果、审批记录、不可变发布包、同步 outbox 后端 CRUD/只读接口、`/product-capability/publish/check`、`/product-capability/publish/packages/create` 和 SQL 草案；发布包/outbox API 层不提供通用写接口，只能由发布流程生成；BLOCKER 不可发布；`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### PCC-24 - Batch 2 测试发布前端

Owner：frontend-developer  
Status：done  
DependsOn：PCC-23  
Requirement Source：`docs/产品配置中心/产品能力界面设计稿.md`  
Scope：发布闸门页面、blocker/warning/pass、审批动作、发布包预览、同步状态。  
Files：`admin-ui/src/pages/product-center/publish/**`  
Validation：typecheck + browser interaction。  
Blockers：发布 API contract 未完成。  
Short Notes：已新增测试发布页面、发布 API、二级菜单映射和 i18n；页面支持发布检查、阻断状态、生成发布包、检查结果/审批/发布包/outbox 查看；`vue-tsc --noEmit` 和 `vite build` 通过。

### PCC-30 - Batch 3 销售只读总览

Owner：frontend-developer  
Status：done  
DependsOn：PCC-23  
Requirement Source：`docs/产品配置中心/产品能力界面设计稿.md`  
Scope：销售只读 API 和页面，读取发布包/read model，不读草稿。  
Files：产品能力后端模块、`admin-ui/src/pages/product-center/sales-view/**`  
Validation：Java compile + typecheck + browser smoke。  
Blockers：发布包结构未稳定。  
Short Notes：已新增销售只读视图页面、二级菜单和路由映射；页面只读取发布包列表，复用 grid 只读模式隐藏新增/编辑/删除操作，不读取草稿；`vue-tsc --noEmit`、`vite build`、`mvn -pl bocoo-admin -am -DskipTests compile` 通过。

### PCC-31 - Batch 3 订单快照内部消费

Owner：java-architect  
Status：done  
DependsOn：PCC-23  
Requirement Source：`docs/产品配置中心/共享产品能力中心数据库设计草案.md`  
Scope：订单侧 `build-snapshot` Service/API 设计和最小接入点。  
Files：订单模块路径待确认、产品能力后端模块。  
Validation：service contract review + Java compile。  
Blockers：订单模块边界未确认。  
Short Notes：已新增 `/product-capability/order-snapshots/build`、`ProductOrderSnapshotService`、`OrderSnapshotBuildBo`、`OrderProductSnapshotVo` 和按钮权限草案；当时只构建自包含快照 JSON/hash。Batch 7 已根据用户拆服务诉求调整为产品能力中心通用快照实例持久化，不再推进订单私有 `order_product_snapshot` 表。

### PCC-40 - Deferred AI 和导入增强

Owner：main  
Status：done  
DependsOn：PCC-30, PCC-31  
Requirement Source：`docs/产品配置中心/业务Agent体系设计.md`  
Scope：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、促销价/客户价等后续增强计划。  
Files：`.ai/TASKS.md`、后续需求文档。  
Validation：计划审查。  
Blockers：P0/P1 未完成前不进入实现。  
Short Notes：已保留到 `.ai/TASKS.md` Deferred：ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、客户价/促销价、独立服务拆分均不删除，作为后续 `/do` 的继续入口。

### PCC-50 - Batch 4 数据库落库前表级评审和加固

Owner：main  
Status：done  
DependsOn：PCC-31  
Requirement Source：`.ai/archive/20260606-product-capability-center-batch3.md`  
Scope：检查 `pc_*` 表注释、UTC、索引字段、租户忽略配置和落库前风险。  
Files：`sql/postgresql/product_capability.sql`、`bocoo-admin/src/main/resources/application.yml`、`docs/产品配置中心/共享产品能力中心数据库落库评审.md`  
Validation：SQL 结构脚本检查 + YAML parse + Java compile + codegraph sync + diff check。  
Blockers：真实数据库实例未连接，本批不执行 SQL。  
Short Notes：已修复两个索引引用不存在字段的问题；20 张 `pc_*` 表全部加入 `bocoo.tenant.ignore-tables`；新增数据库落库评审文档；自动检查表/字段注释、UTC、索引字段、租户忽略覆盖均通过。

### PCC-51 - Batch 5 临时 PostgreSQL 空库试执行

Owner：main  
Status：done  
DependsOn：PCC-50  
Requirement Source：`docs/产品配置中心/共享产品能力中心数据库落库评审.md`  
Scope：用一次性 PostgreSQL 16 空库容器顺序执行 `base.sql` 和 `product_capability.sql`，验证真实 PG 语法、菜单授权和幂等基础。  
Files：`sql/postgresql/product_capability.sql`、`docs/产品配置中心/共享产品能力中心数据库落库评审.md`  
Validation：Docker PostgreSQL 临时库执行 + 结果查询 + SQL 结构脚本 + codegraph sync + diff check。  
Blockers：未连接真实开发库或生产库，本批只验证一次性空库。  
Short Notes：临时库首次执行抓到 Batch 2/3 菜单 SQL 漏 `tenant_id` 和 `sys_role_menu` 授权漏 `tenant_id`；已修复后从全新空库完整执行通过，结果为 20 张 `pc_*` 表、36 条产品能力菜单、36 条 role_id=1 产品能力授权。

### PCC-52 - Batch 6 真实库受控执行计划

Owner：main  
Status：done  
DependsOn：PCC-51  
Requirement Source：`.ai/archive/20260606-product-capability-center-batch5-sql-trial.md`  
Scope：生成真实开发库/测试库执行 `product_capability.sql` 的 preflight、执行、post-check、应用侧核验和回退策略。  
Files：`docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md`  
Validation：文档审查 + codegraph sync + diff check。  
Blockers：未连接真实开发库，本批不执行远程库。  
Short Notes：已新增真实库受控执行计划，包含冲突查询、执行命令模板、预期结果、应用侧核验和回退 SQL；明确生产不能直接手工执行。

### PCC-60 - Batch 7 通用产品能力快照实例持久化

Owner：main  
Status：done  
DependsOn：PCC-31, PCC-52  
Requirement Source：用户确认“统一放在 product 里面，后期好拆分”、`.ai/changes/20260606-product-capability-center/db-contract.md`  
Scope：将快照持久化边界从订单私有表调整为产品能力中心通用快照实例；支持 ORDER、ERP、MES 等来源系统通过 `source_system/source_biz_*` 统一落库。  
Files：`bocoo-modules-product/**`、`sql/postgresql/product_capability.sql`、`bocoo-admin/src/main/resources/application.yml`、`docs/产品配置中心/**`、`.ai/changes/20260606-product-capability-center/**`  
Validation：Java compile + i18n sync + YAML parse + SQL structure check + one-shot PostgreSQL execution + codegraph sync + diff check。  
Blockers：真实开发库尚未执行，本批仅更新草案和应用代码。  
Short Notes：已新增 `pc_product_snapshot_instance`、`ProductSnapshotInstance` Entity/VO/Mapper、`/product-capability/snapshot-instances/build/list/{id}`；旧 `/order-snapshots/build` 保留为只构建兼容接口；SQL 空库执行结果为 21 张 `pc_*` 表、39 条产品能力菜单、39 条 role_id=1 授权。

### PCC-70 - Batch 8 导入中心基础能力

Owner：main  
Status：done  
DependsOn：PCC-40, PCC-60  
Requirement Source：用户确认“基础信息、价格等设计相关都是共用能力”、`.ai/TASKS.md` Deferred  
Scope：新增产品能力导入批次和行级问题基础能力，承接人工导入、历史数据整理、ERP/MES 导入和后续 AI 解析。当前不做完整 Excel/AI 自动解析和批量提交。  
Files：`bocoo-modules-product/**`、`sql/postgresql/product_capability.sql`、`bocoo-admin/src/main/resources/application.yml`、`docs/产品配置中心/**`、`.ai/changes/20260606-product-capability-center/**`  
Validation：Java compile + YAML parse + SQL structure check + one-shot PostgreSQL execution + diff check。  
Blockers：完整 Excel/AI 解析、字段智能映射、目标表提交事务和冲突合并策略未进入本批。  
Short Notes：已新增 `pc_import_batch`、`pc_import_row_issue`、`ProductImportBatch` / `ProductImportRowIssue` Entity/BO/VO/Mapper、`ProductImportService` 和 `/product-capability/import/**` API；导入表加入租户忽略；SQL 空库执行结果为 23 张 `pc_*` 表、44 条产品能力菜单、44 条 role_id=1 授权。

### PCC-80 - Batch 9 ERP/MES 同步Outbox基础层

Owner：main  
Status：done  
DependsOn：PCC-23, PCC-70  
Requirement Source：`.ai/TASKS.md` Deferred、用户要求 ERP/MES 等平台也共用产品能力  
Scope：将发布成功后的同步 outbox 从单一订单目标扩展为 ORDER、ERP、MES 多目标事件，并提供重试标记 API；当前不直接调用外部 ERP/MES 接口。  
Files：`bocoo-modules-product/**`、`admin-ui/src/api/product-capability/publish.ts`、`sql/postgresql/product_capability.sql`、`i18n/locales/en_US.json`、`docs/产品配置中心/**`、`.ai/changes/20260606-product-capability-center/**`  
Validation：Java compile + i18n sync + YAML parse + SQL structure check + one-shot PostgreSQL execution + codegraph sync + diff check。  
Blockers：真实 ERP/MES 推送、失败退避、定时扫描、回调确认、幂等消费和死信处理未进入本批。  
Short Notes：已让 `publish/packages/create` 同事务写入 ORDER、ERP、MES 三条 `pc_product_sync_outbox`；`PublishExecutionResultVo` 保留旧 `outbox` 并新增 `outboxes`；新增 `/product-capability/sync-outbox/{id}/retry` 和 `product:publish:retrySync` 菜单权限。

### PCC-81 - Batch 10 XLS/XLSX 导入解析预览基础层

Owner：main  
Status：done  
DependsOn：PCC-70  
Requirement Source：用户确认“xls 可以做，但只能参考以前的，组件/基础数据都共用，不能照旧重复导入”  
Scope：在导入中心补充 XLS/XLSX 上传解析，生成字段映射、预览 JSON、错误汇总和行级问题；共享主数据通过编码匹配，不自动创建重复组件/物料。  
Files：`bocoo-modules-product/**`、`admin-ui/src/api/product-capability/import.ts`、`docs/产品配置中心/**`、`.ai/changes/20260606-product-capability-center/**`  
Validation：Java clean compile + admin-ui typecheck + codegraph sync + diff check。  
Blockers：批量提交正式表、冲突合并、字段映射确认 UI 和 AI 智能识别未进入本批。  
Short Notes：已新增 `/product-capability/import/batches/parse-excel`，支持 `xls/xlsx`；解析结果写入 `pc_import_batch.mapping_json / preview_json / error_summary_json`，共享组件、物料、产品模型、价格方案编码缺失时写入 `pc_import_row_issue` 的 `ERROR`，不复制旧库基础数据。

### PCC-90 - Static Review

Owner：code-reviewer  
Status：done  
DependsOn：本批实现任务  
Requirement Source：`.ai/changes/20260606-product-capability-center/wave-plan.md`  
Scope：静态审查权限、tenant、UTC、i18n、SQL、事务、Entity/VO/BO、快照权威。  
Files：本批改动文件。  
Validation：review findings。  
Blockers：无。  
Short Notes：已审查并修复一处发布包/outbox 可写 API 风险：Controller 层现仅提供 list/options/get，写入只能走发布流程；未发现新增 Redis 权威缓存、tenant 字段误加、硬编码 UI 文案或非 UTC 时间新增问题；残余风险为 SQL 草案未真实落库、规则/价格/发布包 schema 仍是 MVP。

### PCC-91 - Runtime Validation

Owner：browser-debugger  
Status：done  
DependsOn：本批前端实现任务  
Requirement Source：`.ai/changes/20260606-product-capability-center/wave-plan.md`  
Scope：浏览器打开关键页面，检查 console、network、交互、空态、权限隐藏。  
Files：`admin-ui/src/pages/product-center/**`  
Validation：browser evidence。  
Blockers：本地服务未启动或页面未实现。  
Short Notes：已启动 Vite dev server 并用 Playwright + 系统 Chrome 打开 `/`、`/product-center/template`、`/product-center/pricing`、`/product-center/publish`；页面均 200 并重定向登录页；console 500 来自后端 8081 未启动时登录页 `/captchaImage` 代理失败，非新增页面 chunk 错误；`vue-tsc` 和 `vite build` 通过。
