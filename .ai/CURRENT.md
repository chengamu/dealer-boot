# Current

## Current Goal：当前目标

产品配置中心产品与配置模块第一阶段重构 `/do all`：完成销售产品、配置模板、问题组、配置问题、配置答案、配置求值器第一阶段闭环。

## Requirement Source：需求来源

- `.ai/requirements/20260612-product-config-phase1.md`
- `docs/产品配置中心重构/实施拆分/10-产品与配置模块AMU实施计划.md`
- `docs/产品配置中心重构/实施拆分/03-产品与配置模块.md`
- `docs/产品配置中心重构/实施拆分/07-现有实现对比与改造清单.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`

## Current Phase：当前阶段

`/do all` 已完成。用户手测反馈后，已完成产品配置可用性整改：模块命名、销售产品下拉、配置问题/答案下拉、求值器体验、分类冗余、厘米单位、菜单图标、i18n raw key 和真实浏览器验证均已处理。随后按旧 demo 仅取业务串联、重新设计了生产版“配置录入工作台”，并同步当前单独功能和菜单排序。最新一轮已按运营录入视角继续修正：问题新增/编辑弹窗化，主屏只保留产品索引、问题序列、问题详情、答案表和求值结果。

## Scope：范围

- 销售产品。
- 配置模板和模板版本。
- 问题组。
- 配置问题。
- 配置答案。
- 配置求值器。
- 必要配置规则轻量支持。
- 5 个可用于配置求值的测试产品样本。
- SQL、后端、前端、i18n、真实 dist、浏览器验证。

## Out of Scope：不做范围

- 最终价格计算。
- 正式 BOM 计算。
- 工程扣减完整试算。
- 发布审批流。
- 订单快照闭环。
- Excel 批量导入。
- 客户门户前台下单页。
- 依赖升级、migration、生产配置。

## Key Decisions：关键决策

- 使用新 `pc_sales_product` 表达销售产品，不在 UI 暴露 `Product Model` / `Sales Variant` 泛名称。
- 标品 SKU 本阶段只预留或轻量，不阻塞第一阶段闭环。
- 配置求值器必须返回结构化 `disabledOptions`、`validations`、`warnings`、`blockers`、`autoComponents`、`mediaAssets`。
- 配置答案来源结构化，不只塞 JSON。
- 普通维护页按标准 grid + 右侧 drawer；配置模板工作台和求值器可自定义内容区。
- 销售产品选择产品分类后冗余保存分类编码、中文名、英文名；尺寸单位从 `pc_unit` 下拉，当前库已补 `CM`。
- 配置问题和配置答案页面不再把模板版本 ID、问题组 ID、问题 ID 作为用户手输字段；改为模板版本、问题组、问题业务下拉。
- 配置求值器通过销售产品自动带出当前模板版本和默认尺寸，不再要求用户输入模板版本 ID。
- 配置模板菜单调整为“配置录入工作台”，作为产品配置模块第一个入口；单独功能页仍保留在同模块下：销售产品、问题组模板、配置问题、配置答案、配置求值器。
- 配置录入工作台不是 demo 复刻：采用产品上下文 + 问题序列 + 问题详情 + 答案绑定 + 求值输出的生产工具布局，复用现有单独页面 API 和字段契约；新增/编辑问题和答案均走弹窗，避免主页面成为长表单。
- 新增 i18n 只写 `i18n/locales/en_US.json`，不手改 `admin-ui/public/i18n/*.json`。
- `codegraph sync` 已在 `/plan` 阶段和 `/do` 验证前执行，结果均为 `Already up to date`。
- 子 Agent 启动时出现 `SubAgent unavailable`，本轮按 AMU fallback 由主 Agent 完成实现、静态审查和验证。
- 真实菜单路径为 `/product-config/...`，页面组件仍位于 `admin-ui/src/pages/product-center/template/**`。
- 真实 dist 浏览器验证使用 `bocoo-admin/target/dist/bocoo-admin.jar` 启动到 18081，并用本地静态代理服务 `bocoo-admin/target/dist` 到 18083；验证后两个临时端口均已关闭。

## Active Tasks：当前任务

详见 `.ai/TASKS.md` 与 `.ai/changes/20260612-product-config-phase1/wave-plan.md`。

本阶段任务 `PC-C1` 到 `PC-V3` 均已完成，详见 `.ai/changes/20260612-product-config-phase1/check.md`。

## Risks / Pause Points：风险 / 暂停点

- `git diff --check` 全量受 `logs/*` 生成日志尾随空格影响；代码区已执行 `git diff --check -- . ':!logs/*'` 并通过。
- 当前工作区有本轮之外的文档、日志和 `master-guide` 改动，未在本轮回滚。

## Validation Plan：验证计划

- SQL：开发库已执行 `sql/postgresql/product_capability.sql`；手测整改阶段又执行了非破坏性补丁，补 `pc_sales_product.category_name_cn/category_name_en`、`pc_unit.CM`、菜单名称和图标。
- CodeGraph：已执行 `codegraph sync`。
- Backend：`mvn -pl bocoo-admin -am -DskipTests compile` 和 `mvn -pl bocoo-admin -am -DskipTests package` 通过。
- Frontend：整改后 `pnpm i18n:validate`、`pnpm i18n:sync`、`pnpm --dir admin-ui typecheck`、`pnpm --dir admin-ui build` 通过；工作台设计化重构后再次通过。
- Dist：前端产物已同步到 `bocoo-admin/target/dist`。
- Browser：真实登录后验证 `/product-config/sales-products`、`/product-config/config-questions`、`/product-config/config-options`、`/product-config/config-evaluator`，console error 为 0；求值器运行后返回 `BLOCKER`，禁用原因已翻译，无 `product.config.*` 裸 key。工作台新版已用真实 Chrome 访问 `/product-config/template`，菜单顺序、工作台布局、答案入口和内嵌评估通过；运行评估返回 `ALLOW`，可用选项 5、禁用答案 1、自动带出组件 1。二次修正后已验证销售产品列表可读、主屏不再铺长问题表单，点击 `新增问题` 弹出维护弹窗。

## Next Step：下一步

Ready for `/archive`。
