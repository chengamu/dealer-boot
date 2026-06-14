# Requirement: 产品与配置模块第一阶段重构

## Goal：目标

完成产品配置中心“产品与配置模块第一阶段”闭环：销售产品、配置模板、问题组、配置问题、配置答案、配置求值器可维护、可联动、可真实验证。

## Background：背景

基础资料模块已完成并归档，本阶段从“基础资料录入”推进到“客户可下单产品配置”。现有代码已有配置模板、问题组、问题、答案、规则和轻量求值骨架，但 SQL 当前清理了旧 `pc_product_model` / `pc_sales_variant` 和配置表，正式菜单也缺少销售产品、配置问题、配置答案、配置求值器的独立入口，需要重新建立清晰业务边界。

## Scope：范围

- 数据库：补齐 `pc_sales_product`、配置模板、模板版本、问题组、配置问题、配置答案、配置规则所需表、菜单、按钮、字典和 5 个测试样本 seed。
- 后端：补销售产品 CRUD，增强配置模板 / 问题组 / 问题 / 答案 CRUD、引用检查、状态变更和配置求值器。
- 前端：补销售产品、配置模板、问题组、配置问题、配置答案、配置求值器页面或入口，遵守标准 grid、抽屉、i18n、动态路由和权限规范。
- 测试数据：至少 5 个产品样本，每个样本包含销售产品、配置模板、问题组、3 个以上问题、每题至少 2 个答案，并覆盖组件包、基础字典、附件资料引用。
- 验证：SQL 开发库、后端 compile、前端 typecheck/build、真实 dist 同步、浏览器真实菜单和 CRUD/求值冒烟。

## Out of Scope：不做范围

- 不做最终价格计算。
- 不做正式 BOM 计算。
- 不做工程扣减完整试算。
- 不做发布审批流。
- 不做订单快照闭环。
- 不做 Excel 批量导入。
- 不做客户门户前台下单页。
- 不改 migration 文件，不改生产环境配置，不升级依赖。

## User Stories / Use Cases：用户故事 / 使用场景

- 录入人员可以维护销售产品，并为产品关联配置模板。
- 配置人员可以维护问题组、配置问题和配置答案。
- 配置答案可以引用配置字典、物料、组件包和附件资料。
- 配置人员可以输入尺寸和已选答案，调用配置求值器查看可见问题、可选答案、禁用原因、校验结果、自动带出组件和资料。

## Business Rules：业务规则

- 销售产品是客户下单入口，UI 和 API 不再暴露 `Product Model` / `Sales Variant` 这类泛名称。
- 标品 SKU 第一阶段只预留清晰入口或轻量表结构，不阻塞配置闭环。
- 配置规则只放轻量显示 / 禁用 / 必填校验逻辑，不承载价格和正式 BOM。
- `autoComponents` 只返回配置答案带出的组件，不替代正式 BOM。
- `mediaAssets` 必须来自 OSS / `pc_media_asset` / `pc_media_binding`。

## UX / UI Requirements：交互 / 界面要求

- 普通维护页使用 `app-container + 查询区 + toolbar/right-toolbar + el-table + pagination + 右侧 drawer`。
- 表格必须有选择列、序号列、合理列宽、长字段 tooltip、固定右侧图标操作列。
- 双击行打开详情抽屉，新增/编辑/详情区分清楚。
- 抽屉宽度不小于 80%，字段多时两列分组，底部 sticky 操作栏。
- 所有可见文案写 `i18n/locales/en_US.json`，不手改 `admin-ui/public/i18n/*.json`。
- 菜单 SQL、`permission.ts` 动态路由映射、按钮权限、后端权限必须三方一致。

## API Requirements：接口要求

- 普通 CRUD 遵守项目标准接口：`list`、`options`、`get`、`add`、`update`、`delete`、`changeStatus`、必要的 `references`。
- 配置求值器最小输入：`salesProductId`、`templateVersionId`、`width`、`height`、`selectedOptions`。
- 配置求值器最小输出：`visibleQuestions`、`availableOptions`、`disabledOptions`、`validations`、`autoComponents`、`mediaAssets`、`warnings`、`blockers`。
- `disabledOptions` 必须带 `disabledReason`，`warnings` / `blockers` 必须带 `code`、`message`、`targetType`、`targetId`。

## Data Requirements：数据要求

- 表字段包含 `tenant_id`、业务主键、业务编码、`name_cn`、`name_en`、`status`、`sort_order`、`remark`、`legacy_source` / `legacy_id`、审计字段和 `del_flag`。
- 枚举字段不使用 `varchar(1)` 承载业务枚举。
- 配置答案来源结构化：`source_type`、`source_ref_id`、`source_code`、`source_name`、`display_name_cn`、`display_name_en`、`value_code`、`rule_json`。
- 单位以 `pc_unit` 为权威，普通字典不承载业务单位。

## Permission / Tenant Rules：权限 / 租户规则

- 新增菜单、按钮、前端 `v-hasPermi`、后端 `@SaCheckPermission` 必须一致。
- 后端查询和写入保持现有 tenant / data permission 体系，不新增绕过。
- 管理员角色 seed 需要授权本阶段菜单和按钮。

## i18n / UTC Rules：国际化 / 时间规则

- 前端新增可见文案只写 `i18n/locales/en_US.json`，随后在 `/do` 验证阶段执行 i18n validate / sync。
- 后端错误消息优先使用 JSON i18n key。
- SQL 时间字段使用 `timestamptz`，后端业务时间不直接使用 `LocalDateTime.now()`。
- 前端展示后端时间使用 `formatUtc()`。

## Options：可选方案

### Option A：一次性补齐产品与配置第一阶段闭环

Pros：销售产品、配置模板、问题、答案和求值器同步形成可验收链路。
Cons：涉及 SQL、后端、前端、seed、浏览器验证，任务量较大，需要严格 Wave / Barrier 控制。

### Option B：只修配置模板工作台，不补销售产品独立入口

Pros：改动更小。
Cons：无法完成“客户买什么”的入口，仍停留在配置资料维护，不满足本阶段目标。

## Recommended Option：推荐方案

选择 Option A，但严格限制范围：只做销售产品、配置模板、问题组、配置问题、配置答案、配置求值器和必要 seed；标品 SKU 仅预留或轻量，不展开价格、BOM、发布、订单快照。

## Risks：风险

- 当前 SQL 已 drop 旧产品和配置表，`/do` 需要先恢复清晰表结构，否则后端和前端运行态会 500。
- 现有后端仍有价格、发布、订单快照代码引用 `productModelCode`，本阶段不能无脑改名扩大到价格/发布/快照，需通过兼容字段或隔离边界避免跨模块爆炸。
- 配置求值器如输出裸字符串 `warnings` / `blockers`，不能满足 10 号文档的结构化要求，需要重设 VO。
- 真实 dist 路径是 `bocoo-admin/target/dist/bocoo-admin.jar`，前端 build 后必须同步 dist 再浏览器验证。

## Open Questions：待确认问题

- 标品 SKU 本阶段默认只预留菜单 / 表结构或轻量 CRUD，不纳入必须验收闭环；如用户确认要完整 CRUD，需要回到 `/plan` 修订。
- 配置规则表达式只支持受控 JSON 规则，不执行任意动态表达式。

## Acceptance Criteria：验收标准

- 数据库脚本可重复执行到开发库。
- 菜单能进入，刷新页面不 404。
- 销售产品、配置模板、问题组、配置问题、配置答案 CRUD 真实读写数据库。
- 状态启停和引用检查可用，引用检查能说明引用对象。
- 配置求值器返回真实后端计算结果，包含可见问题、可选答案、禁用原因、校验、组件和资料。
- 至少 5 个测试产品能完成配置求值。
- `mvn -pl bocoo-admin -am -DskipTests compile` 通过。
- `pnpm --dir admin-ui typecheck`、`pnpm --dir admin-ui build` 通过。
- 前端 dist 已同步 `bocoo-admin/target/dist`。
- 浏览器真实验证菜单、搜索、重置、分页、新增、编辑、详情、删除/停用、求值器、console 和 network。

## Related Decisions：相关决策

- Requirement source: `docs/产品配置中心重构/实施拆分/10-产品与配置模块AMU实施计划.md`
- 参考：`docs/产品配置中心重构/实施拆分/03-产品与配置模块.md`
- 参考：`docs/产品配置中心重构/实施拆分/07-现有实现对比与改造清单.md`
- 参考：`docs/项目配置和代码风格/fullstack-code-standards.md`
