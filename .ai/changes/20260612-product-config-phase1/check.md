# Check: 产品与配置模块第一阶段重构

## 已执行验证

- `docker exec -i base-boot-postgres psql -U root -d dealer -v ON_ERROR_STOP=1 -f - < sql/postgresql/product_capability.sql`：通过。
- Seed 计数验证通过：
  - `pc_sales_product`: 5
  - `pc_config_template`: 5
  - `pc_config_template_version`: 5
  - `pc_question_group`: 3
  - `pc_config_question`: 15
  - `pc_config_option`: 30
  - `pc_config_rule`: 1
- `mvn -pl bocoo-admin -am -DskipTests compile`：通过。
- `pnpm i18n:validate`：通过，2278 keys valid across 2 locales。
- `pnpm i18n:sync`：通过，已同步到前端 public 和后端 runtime i18n JSON。
- `pnpm --dir admin-ui typecheck`：通过。
- `pnpm --dir admin-ui build`：通过。
- `cp -R admin-ui/dist/. bocoo-admin/target/dist/`：完成真实 dist 同步。
- `codegraph sync`：通过，`Already up to date`。
- `mvn -pl bocoo-admin -am -DskipTests package`：通过，刷新 `bocoo-admin/target/dist/bocoo-admin.jar`。
- `git diff --check -- . ':!logs/*'`：通过。

## 真实运行验证

- 后端使用真实 dist jar 启动：`bocoo-admin/target/dist/bocoo-admin.jar`。
- 因 8081 被用户 IntelliJ 调试进程占用，未杀用户进程；本轮验证 jar 临时运行在 18081。
- 为避免 WebSocket 2831 与现有进程冲突，验证进程使用 2931。
- 为自动化登录，验证进程用启动参数临时关闭验证码；未修改配置文件或数据库。
- 前端使用本地静态代理服务 `bocoo-admin/target/dist`，地址 `http://127.0.0.1:18083`，`/dev-api` 代理到 18081。
- 验证完成后 18081 和 18083 均已关闭。

## API / Browser Smoke

- 登录：通过。
- API 列表：
  - `/product-capability/sales-products/list`: 200 / code 200 / 5 rows
  - `/product-capability/templates/list`: 200 / code 200 / 5 rows
  - `/product-capability/question-groups/list`: 200 / code 200 / 3 rows
  - `/product-capability/questions/list`: 200 / code 200 / 15 rows
  - `/product-capability/options/list`: 200 / code 200 / 30 rows
- 配置求值：
  - `/product-capability/config/evaluate`: 200 / code 200
  - `resultStatus`: `BLOCKER`
  - `visibleQuestions`: 3
  - `availableOptions`: 5
  - `disabledOptions`: 1
- 浏览器页面：
  - `/product-config/sales-products`: 通过，表格 3，新增按钮 1。
  - `/product-config/template`: 通过，表格 6，新增按钮 3，运行评估按钮 2。
  - `/product-config/question-groups`: 通过，表格 3，新增按钮 1。
  - `/product-config/config-questions`: 通过，表格 3，新增按钮 1。
  - `/product-config/config-options`: 通过，表格 3，新增按钮 1。
  - `/product-config/config-evaluator`: 通过，表格 16，运行评估按钮 1。
- Browser console errors: 0。
- Browser bad local responses: 0。

## 静态审查结论

- 产品与配置第一阶段未引入价格、BOM、订单快照闭环。
- 新增 SQL、后端、前端 API 均使用 `/product-capability` contract，真实菜单路径为 `/product-config/...`。
- 前端新增可见文案已进入 `i18n/locales/*.json`，并由 `i18n:sync` 分发。
- 登录失败预检期间确认请求日志中的密码已脱敏。
- 子 Agent 在本轮执行时不可用，已按 AMU fallback 由主 Agent 完成静态审查和验证。

## 已知事项

- 全量 `git diff --check` 会被 `logs/*` 生成日志中的尾随空格拦截；本轮未清理日志文件，代码区 diff check 已通过。
- 工作区存在本轮之外的文档、日志和 `admin-ui/src/pages/product-center/master-guide/` 改动，本轮未回滚。

## 手工测试前清理

- 已确认旧 `pc_product_model`、`pc_sales_variant` 表不存在。
- 已清理开发库残留的旧 `Variant Query/Add/Edit/Delete` 菜单按钮及 `sys_role_menu` 引用：
  - `old_menu_count`: 0
  - `old_role_menu_refs`: 0
- 已将本阶段不测试的 `Pricing`、`Quote Preview` 菜单设为隐藏。
- 当前 `Product Configuration` 下可见菜单仅 6 个：
  - Sales Products
  - Config Template
  - Question Groups
  - Config Questions
  - Config Options
  - Config Evaluator

## 手测反馈整改：产品配置可用性

- 大模块名称已从 `配置与价格 / Configuration & Pricing` 改为 `产品配置 / Product Configuration`。
- `Config Evaluator` 菜单图标从不存在的 `test` 改为已有 `calculator`。
- `pc_sales_product` 补 `category_name_cn`、`category_name_en` 快照字段；保存销售产品时按 `category_id` 同步分类编码和中英文名称。
- 开发库已补 `CM / 厘米` 到 `pc_unit`，销售产品尺寸单位可从单位下拉选择。
- 销售产品页面：
  - 查询条件收缩为销售产品编码、中文名称、产品分类、产品类型、销售模式、状态。
  - 产品分类、产品类型、销售模式、尺寸单位、业务状态改为下拉。
  - 配置模板版本改为业务下拉，选择后自动带模板编码和版本号。
- 配置问题页面：
  - 查询条件不再包含模板版本 ID、问题组 ID。
  - 模板版本、问题组、输入类型、必填、客户可见改为业务下拉。
- 配置答案页面：
  - 问题改为业务下拉，选择后自动写入模板版本 ID。
  - 来源类型改为结构化下拉。
- 配置求值器页面：
  - 选择销售产品后自动带出当前模板版本、默认宽高和尺寸单位。
  - 不再让用户输入模板版本 ID。
  - 后端 message key 自动翻译，禁用原因不再展示 `product.config.*` 裸 key。
- 整改后验证：
  - `mvn -pl bocoo-admin -am -DskipTests compile`：通过。
  - `pnpm i18n:validate`：通过，2316 keys valid across 2 locales。
  - `pnpm i18n:sync`：通过。
  - `pnpm --dir admin-ui typecheck`：通过。
  - `pnpm --dir admin-ui build`：通过。
  - `rsync -a --delete admin-ui/dist/ bocoo-admin/target/dist/`：已同步真实 dist。
  - 浏览器验证 `/product-config/sales-products`、`/product-config/config-questions`、`/product-config/config-options`、`/product-config/config-evaluator`：通过，console error 为 0。
  - 求值器运行验证：返回 `BLOCKER`，可用选项 5，禁用答案 1，校验结果 3；禁用原因已翻译为中文。

## 工作台设计化重构与菜单排序

- `配置模板` 入口已改为 `配置录入工作台`，菜单排序调整为：
  - 1 配置录入工作台
  - 2 销售产品
  - 3 问题组模板
  - 4 配置问题
  - 5 配置答案
  - 6 配置求值器
  - 本阶段不测的标准 SKU、Pricing、Quote Preview 保持隐藏。
- 工作台按生产录入动线重构：
  - 左侧销售产品索引。
  - 顶部当前产品上下文：分类、产品类型、模板版本、问题/答案数量、必填数、业务状态。
  - 中部配置问题序列。
  - 右侧问题字段编辑，模板版本和问题组走业务下拉。
  - 当前问题答案表保留答案维护入口。
  - 底部内嵌求值输出，复用真实求值 API。
- 单独功能页保持同步可用：销售产品、问题组模板、配置问题、配置答案、配置求值器仍作为独立菜单存在。
- 验证：
  - `pnpm i18n:validate`：通过，2338 keys valid across 2 locales。
  - `pnpm i18n:sync`：通过。
  - `pnpm --dir admin-ui typecheck`：通过。
  - `pnpm --dir admin-ui build`：通过。
  - `rsync -a --delete admin-ui/dist/ bocoo-admin/target/dist/`：已同步真实 dist。
  - 当前开发库菜单 SQL 已执行并查询确认排序正确。
  - 真实 Chrome 访问 `/product-config/template`：新版工作台渲染正常，未出现裸 `productCenter.*` key。
  - 工作台内点击 `运行评估`：返回 `ALLOW`，可用选项 5，禁用答案 1，自动带出组件 1。

## 工作台可用性二次修正

- 按运营录入视角调整工作台：
  - 主页面只负责选产品、看问题序列、看当前问题详情、看答案表和求值结果。
  - `新增问题` / `编辑问题` 改为弹窗，不再把完整问题字段表单长期铺在主页面。
  - `新增答案` / `编辑答案` 保持弹窗维护。
  - 销售产品卡片改为紧凑两行信息，展示中文名、销售产品编码、英文名、产品类型、版本、单位，不再把长英文名和模板编码挤在窄列里。
  - 问题录入区和问题详情区使用固定最小宽度，避免右侧区域过窄导致控件不可读。
- 验证：
  - `pnpm i18n:validate`：通过，2338 keys valid across 2 locales。
  - `pnpm --dir admin-ui typecheck`：通过。
  - `pnpm --dir admin-ui build`：通过。
  - `rsync -a --delete admin-ui/dist/ bocoo-admin/target/dist/`：已同步真实 dist。
  - 真实 Chrome 访问 `/product-config/template`：销售产品列表、问题详情、答案表新版布局正常；点击 `新增问题` 弹出问题维护弹窗。
