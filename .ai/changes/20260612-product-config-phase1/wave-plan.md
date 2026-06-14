# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 4
- UseWorktree: true
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only
- Requirement Source: `.ai/requirements/20260612-product-config-phase1.md`
- CodeGraph Sync: 2026-06-12 已执行，结果为 `Already up to date`

## Agent Registry

- `browser-debugger`: 浏览器复现、UI 证据、client-side debugging。
- `code-reviewer`: 静态代码质量、风险、可维护性、正确性审查。
- `java-architect`: Java / Spring / SQL / transaction / permission / tenant / 架构边界。
- `frontend-developer`: Vue / UI / component / 交互实现。
- `typescript-pro`: TypeScript 类型、API typing、前后端 contract 对齐。
- `main`: orchestrator，需求、seed contract、Barrier、无法匹配的任务。

## Options

- Option A：一次性补齐产品与配置第一阶段闭环。覆盖数据库、后端、前端、seed、真实验证，满足目标但风险较高。
- Option B：只修配置模板工作台。改动小但缺销售产品入口和完整求值闭环，不推荐。

## Recommendation

采用 Option A，分 Wave 控制风险。只覆盖销售产品、配置模板、问题组、配置问题、配置答案、配置求值器；配置规则只作为求值器轻量规则支持；标品 SKU 只预留或轻量，不进入价格、BOM、订单快照。

## Trade-offs

- 保留现有 `ProductConfigService` / `ConfigEvaluationEngine` 可降低改动量，但需要补结构化输出和引用来源字段。
- 销售产品使用新 `pc_sales_product` 能让业务名清晰，但需要小心价格、发布、订单快照旧代码中的 `productModelCode` 依赖，避免本阶段跨模块扩散。
- 配置问题 / 配置答案独立菜单可提高录入效率，但模板工作台仍保留聚合编辑入口。

## Pause Points

- 如果发现 `pc_sales_product` 改名导致价格、发布、订单快照编译大面积破坏，暂停并回到 `/plan`，优先采用兼容字段方案。
- 如果 seed 需要真实 OFBiz 数据库凭据或外部附件本体，暂停；本阶段只写无敏感信息的开发 seed。
- 如果浏览器验证发现菜单路径或权限无法通过 SQL 修复，暂停并记录真实错误，不用前端兜底长期绕过。

## Wave 0: Contract / Scope / File Boundary

### Task PC-C1: 定义数据库、菜单和 seed contract

TaskId: `PC-C1`
Title: 定义数据库、菜单和 seed contract
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 PostgreSQL 表、菜单、按钮权限、seed、UTC 和枚举长度，匹配 Java / SQL / database Agent。
AgentRole: contract-database
Status: done
Priority: high
RiskLevel: high，当前 SQL 已 drop 旧产品与配置表，必须先明确新表和兼容边界。
Wave: 0

Files:
- `sql/postgresql/product_capability.sql`
- `.ai/changes/20260612-product-config-phase1/**`

Forbidden:
- `admin-ui/**`
- `bocoo-modules-product/**`
- `pom.xml`
- `bocoo-admin/src/main/resources/application-prod*`
- `.ai/archive/**`

DependsOn: none
ParallelGroup: contract
ConflictBoundary: database-contract-only

Acceptance:
- 明确 `pc_sales_product`、`pc_config_template`、`pc_config_template_version`、`pc_question_group`、`pc_config_question`、`pc_config_option`、`pc_config_rule` 表字段。
- 明确标品 SKU 本阶段预留口径。
- 明确菜单顺序：销售产品、配置模板、问题组模板、配置问题、配置答案、配置求值器、标品 SKU。
- 明确 5 个样本 seed 覆盖卷帘基础款、卷帘户外款、斑马帘双层款、带电机控制、含附件 / 色卡 / 说明资料。
- 明确不修改 migration，不新增 MySQL。

### Task PC-C2: 定义后端 API、DTO/VO 和求值器 contract

TaskId: `PC-C2`
Title: 定义后端 API、DTO/VO 和求值器 contract
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Controller、Service、BO/VO、Engine、permission 和 transaction 边界，匹配 Java 架构 Agent。
AgentRole: contract-backend
Status: done
Priority: high
RiskLevel: high，现有后端有轻量配置 CRUD，但求值输出不满足结构化禁用原因和 mediaAssets 要求。
Wave: 0

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/mapper/**`
- `.ai/changes/20260612-product-config-phase1/**`

Forbidden:
- `admin-ui/**`
- `sql/postgresql/product_capability.sql`
- `pom.xml`
- `.ai/archive/**`

DependsOn: none
ParallelGroup: contract
ConflictBoundary: backend-contract-only

Acceptance:
- 明确销售产品 CRUD API 和权限码。
- 明确配置模板、问题组、配置问题、配置答案、配置规则 API 是否复用现有 `ProductConfigController`。
- 明确 `ConfigEvaluationBo` 与 `ConfigEvaluationResultVo` 字段，输出包含 `disabledOptions`、`validations`、`autoComponents`、`mediaAssets`、结构化 `warnings` / `blockers`。
- 明确引用检查、状态变更、tenant / permission 边界。

### Task PC-C3: 定义前端页面、API 类型和 i18n contract

TaskId: `PC-C3`
Title: 定义前端页面、API 类型和 i18n contract
Owner: `frontend-developer`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Vue 页面、标准 grid、抽屉、动态路由、i18n 和权限入口，匹配前端 Agent。
AgentRole: contract-frontend
Status: done
Priority: high
RiskLevel: high，本阶段必须规避基础资料阶段的菜单、grid、抽屉、裸 key 和真实 dist 问题。
Wave: 0

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/stores/permission.ts`
- `admin-ui/src/i18n/locales/en_US.json`
- `.ai/changes/20260612-product-config-phase1/**`

Forbidden:
- `bocoo-modules-product/**`
- `sql/postgresql/product_capability.sql`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn: none
ParallelGroup: contract
ConflictBoundary: frontend-contract-only

Acceptance:
- 明确页面形态：销售产品、配置问题、配置答案为标准 grid；配置模板工作台和配置求值器可自定义内容区。
- 明确独立菜单和 `permission.ts` 映射。
- 明确新增 API 类型，避免大量 `ProductRecord` 导致类型漂移。
- 明确 i18n key 只写 `i18n/locales/en_US.json`。

## Barrier 0

Checks:
- 数据库、后端、前端 contract 字段一致。
- 菜单、权限码、动态路由路径一致。
- 标品 SKU 不作为必须闭环任务。
- 价格、BOM、订单快照未进入实现范围。
- Barrier 0 通过前不进入 Wave 1。

## Wave 1: Independent Implementation

### Task PC-D1: 实现数据库表、菜单按钮和开发 seed

TaskId: `PC-D1`
Title: 实现数据库表、菜单按钮和开发 seed
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 PostgreSQL DDL、幂等 seed、菜单按钮和 UTC 字段，匹配 Java / SQL Agent。
AgentRole: database
Status: done
Priority: high
RiskLevel: high，SQL 是后端和浏览器验证的前置，错误会导致运行态 500 或菜单 404。
Wave: 1

Files:
- `sql/postgresql/product_capability.sql`

Forbidden:
- `admin-ui/**`
- `bocoo-modules-product/**`
- `pom.xml`
- `bocoo-admin/src/main/resources/application-prod*`
- `.ai/archive/**`

DependsOn: `PC-C1`
ParallelGroup: database
ConflictBoundary: database-only

Acceptance:
- SQL 可重复执行，包含表、索引、注释、菜单、按钮、管理员授权和 5 个样本 seed。
- `status`、`source_type`、`business_type` 等枚举字段长度足够。
- 菜单不暴露价格、BOM、订单快照新入口。
- Seed 不输出或写入敏感连接信息。

### Task PC-B1: 实现销售产品和配置 CRUD 后端

TaskId: `PC-B1`
Title: 实现销售产品和配置 CRUD 后端
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Controller、Service、Mapper、Entity、BO、VO、transaction、reference check 和 permission，匹配 Java 架构 Agent。
AgentRole: backend
Status: done
Priority: high
RiskLevel: high，需在新增销售产品语义和现有配置服务之间保持最小改动。
Wave: 1

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/mapper/**`

Forbidden:
- `admin-ui/**`
- `sql/postgresql/product_capability.sql`
- `pom.xml`
- `.ai/archive/**`

DependsOn: `PC-C2`
ParallelGroup: backend
ConflictBoundary: backend-only

Acceptance:
- 销售产品、配置模板、问题组、配置问题、配置答案 CRUD 可用。
- 状态变更接口使用真实主键字段。
- 引用检查返回可解释引用摘要。
- 后端权限码与 contract 一致。
- Controller 不拼复杂业务 JSON。

### Task PC-B2: 增强配置求值器

TaskId: `PC-B2`
Title: 增强配置求值器
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 `ConfigEvaluationEngine`、结构化 VO、受控规则解析和资料/组件来源解析，匹配 Java 架构 Agent。
AgentRole: backend-engine
Status: done
Priority: high
RiskLevel: high，求值器是本阶段闭环核心，不能只返回轻量字符串校验。
Wave: 1

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/ConfigEvaluationEngine.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/ProductConfigService.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/bo/ConfigEvaluationBo.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/ConfigEvaluationResultVo.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/ConfigOptionVo.java`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/ConfigQuestionVo.java`

Forbidden:
- `admin-ui/**`
- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/PriceCalculationEngine.java`
- `.ai/archive/**`

DependsOn: `PC-C2`
ParallelGroup: backend
ConflictBoundary: backend-engine-only

Acceptance:
- 输出可见问题、可选答案、禁用答案和禁用原因。
- 输出结构化 validations、warnings、blockers。
- 输出答案带出的组件和资料，不执行价格或正式 BOM。
- 不执行任意动态表达式，只解析受控 JSON 规则。

### Task PC-F1: 实现前端销售产品和配置维护页面

TaskId: `PC-F1`
Title: 实现前端销售产品和配置维护页面
Owner: `frontend-developer`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Vue 页面、标准 grid、抽屉、权限、i18n 和动态路由，匹配前端 Agent。
AgentRole: frontend
Status: done
Priority: high
RiskLevel: high，页面必须规避 tabs 藏主功能、裸 key、布局挤压和刷新 404。
Wave: 1

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/stores/permission.ts`
- `admin-ui/src/i18n/locales/en_US.json`

Forbidden:
- `bocoo-modules-product/**`
- `sql/postgresql/product_capability.sql`
- `admin-ui/public/i18n/**`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn: `PC-C3`
ParallelGroup: frontend
ConflictBoundary: frontend-only

Acceptance:
- 销售产品、配置问题、配置答案具备独立菜单或入口。
- 普通维护页符合标准 grid 和右侧抽屉要求。
- 配置模板工作台保留，但不把高频独立维护对象只藏在 tabs。
- 新增文案写入 `i18n/locales/en_US.json`。
- `permission.ts` 覆盖新增菜单 component。

### Task PC-F2: 实现配置求值器前端体验

TaskId: `PC-F2`
Title: 实现配置求值器前端体验
Owner: `frontend-developer`
OwnerSource: agent-registry
OwnerReason: 该任务涉及配置求值输入、结果展示、禁用原因、校验和资料/组件预览，匹配前端 Agent。
AgentRole: frontend-evaluator
Status: done
Priority: high
RiskLevel: medium，求值器是自定义页面，但仍需遵守后台 Layout、权限、i18n 和请求封装。
Wave: 1

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/config.ts`
- `admin-ui/src/api/product-capability/types.ts`
- `admin-ui/src/i18n/locales/en_US.json`

Forbidden:
- `bocoo-modules-product/**`
- `sql/postgresql/product_capability.sql`
- `admin-ui/public/i18n/**`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn: `PC-C3`
ParallelGroup: frontend
ConflictBoundary: frontend-evaluator-only

Acceptance:
- 可输入销售产品、模板版本、宽高和已选答案。
- 可展示 visibleQuestions、availableOptions、disabledOptions、validations、autoComponents、mediaAssets、warnings、blockers。
- 禁用原因和 blocker 不显示裸 JSON 或裸 i18n key。
- 页面 console 不出现明显运行时错误。

## Barrier 1

Checks:
- Wave 1 任务没有 Files 重叠违规。
- SQL、后端、前端权限码和路径保持一致。
- 未修改 `admin-ui/public/i18n/*.json`。
- 未触碰价格、BOM、订单快照实现。
- 通过后进入 Wave 2 集成对齐。

## Wave 2: Integration Alignment

### Task PC-I1: 对齐前后端 API 类型、字段和 i18n

TaskId: `PC-I1`
Title: 对齐前后端 API 类型、字段和 i18n
Owner: `typescript-pro`
OwnerSource: agent-registry
OwnerReason: 该任务重点是 TypeScript API 类型、字段名、枚举、page response 和 i18n key，匹配 TypeScript Agent。
AgentRole: integration-types
Status: done
Priority: high
RiskLevel: medium，若继续依赖宽泛 `ProductRecord`，浏览器阶段容易暴露字段漂移。
Wave: 2

Files:
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/i18n/locales/en_US.json`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/bo/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/**`

Forbidden:
- `pom.xml`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn: `PC-B1`, `PC-B2`, `PC-F1`, `PC-F2`
ParallelGroup: integration
ConflictBoundary: integration-types-only

Acceptance:
- 前端字段与后端 BO/VO 一致。
- 状态、sourceType、warning/blocker 结构一致。
- i18n key 不裸露。
- page response、options response 与现有 request 封装一致。

### Task PC-I2: 对齐测试样本与业务闭环

TaskId: `PC-I2`
Title: 对齐测试样本与业务闭环
Owner: `main`
OwnerSource: main-fallback
OwnerReason: 该任务需要按业务目标核查 5 个样本覆盖面，并协调 SQL、后端和前端验收路径。
AgentRole: integration-seed
Status: done
Priority: high
RiskLevel: medium，样本不足会导致浏览器验证只能测 CRUD，无法证明配置闭环。
Wave: 2

Files:
- `sql/postgresql/product_capability.sql`
- `.ai/changes/20260612-product-config-phase1/**`
- `docs/产品配置中心重构/实施拆分/10-产品与配置模块AMU实施计划.md`

Forbidden:
- `admin-ui/package.json`
- `pom.xml`
- `.ai/archive/**`

DependsOn: `PC-D1`, `PC-B2`
ParallelGroup: integration
ConflictBoundary: seed-validation-only

Acceptance:
- 5 个样本均有销售产品、模板、问题组、至少 3 个问题、每题至少 2 个答案。
- 每个样本至少覆盖组件包、基础字典、附件资料引用中的 3 类。
- 至少一个样本包含电机控制，至少一个样本包含附件 / 色卡 / 说明资料。

## Barrier 2

Checks:
- 前后端 contract 和 seed 已对齐。
- 求值器可使用 seed 数据形成真实结果。
- 未引入价格、BOM、订单快照闭环。
- 通过后进入 Wave 3 静态审查。

## Wave 3: Static Review / Security Review

### Task PC-R1: 静态审查产品与配置第一阶段风险

TaskId: `PC-R1`
Title: 静态审查产品与配置第一阶段风险
Owner: `code-reviewer`
OwnerSource: agent-registry
OwnerReason: 该任务需要审查权限、SQL、transaction、DTO/VO、tenant、XSS、SQL injection、敏感信息和字段漂移风险，匹配 code review Agent。
AgentRole: static-review
Status: done
Priority: high
RiskLevel: high，跨 SQL、后端、前端和 seed，需要独立静态审查。
Wave: 3

Files:
- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/stores/permission.ts`
- `admin-ui/src/i18n/locales/en_US.json`

Forbidden:
- `.ai/archive/**`

DependsOn: `PC-I1`, `PC-I2`
ParallelGroup: review
ConflictBoundary: static-review-only

Acceptance:
- 找出 P1/P2 级别真实风险并给出文件/行级证据。
- 重点检查权限三方一致、tenant 边界、SQL 幂等、NPE、DTO/VO 边界、XSS、裸 key、敏感数据泄露。
- 不执行浏览器验证，不替代 Runtime Validation Lane。

## Barrier 3

Checks:
- Static Review 完成且 P1/P2 已处理或明确记录为 Pause Point。
- codegraph sync 在代码修改后、Runtime / API / Browser Validation 前重新执行。
- 通过后进入 Wave 4 `/check`。

## Wave 4: Check / Validation

### Task PC-V1: 数据库和后端验证

TaskId: `PC-V1`
Title: 数据库和后端验证
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及开发库 SQL、后端 compile 和 API contract 验证，匹配 Java / backend Agent。
AgentRole: runtime-backend-validation
Status: done
Priority: high
RiskLevel: high，SQL 与后端是浏览器验证前置。
Wave: 4

Files:
- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `bocoo-admin/**`
- `.ai/changes/20260612-product-config-phase1/**`

Forbidden:
- `bocoo-admin/src/main/resources/application-prod*`
- `.ai/archive/**`

DependsOn: `PC-R1`
ParallelGroup: validation-backend
ConflictBoundary: validation-only

Acceptance:
- 开发库执行 `sql/postgresql/product_capability.sql` 成功。
- `mvn -pl bocoo-admin -am -DskipTests compile` 成功。
- 如启动后端，使用 `bocoo-admin/target/dist/bocoo-admin.jar` 或明确记录实际运行方式。
- 记录菜单 SQL 查询摘要，不输出敏感连接信息。

### Task PC-V2: 前端构建、i18n 和真实 dist 同步

TaskId: `PC-V2`
Title: 前端构建、i18n 和真实 dist 同步
Owner: `frontend-developer`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 i18n validate/sync、typecheck、build 和 dist 同步，匹配前端 Agent。
AgentRole: runtime-frontend-validation
Status: done
Priority: high
RiskLevel: high，基础资料阶段已证明只 build 不同步真实 dist 会导致验证偏差。
Wave: 4

Files:
- `admin-ui/**`
- `bocoo-admin/target/dist/**`
- `.ai/changes/20260612-product-config-phase1/**`

Forbidden:
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn: `PC-R1`
ParallelGroup: validation-frontend
ConflictBoundary: validation-only

Acceptance:
- `pnpm --dir admin-ui i18n:validate` 成功。
- `pnpm --dir admin-ui i18n:sync` 成功。
- `pnpm --dir admin-ui typecheck` 成功。
- `pnpm --dir admin-ui build` 成功。
- 前端 dist 已同步到 `bocoo-admin/target/dist`。

### Task PC-V3: 浏览器真实验证

TaskId: `PC-V3`
Title: 浏览器真实验证
Owner: `browser-debugger`
OwnerSource: agent-registry
OwnerReason: 该任务需要真实浏览器打开页面、检查 console/network、CRUD、求值器和布局证据，匹配 browser debugging Agent。
AgentRole: runtime-browser-validation
Status: done
Priority: high
RiskLevel: high，浏览器验证是本阶段最终验收门，不能由 code-reviewer 替代。
Wave: 4

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `.ai/changes/20260612-product-config-phase1/**`

Forbidden:
- `sql/postgresql/product_capability.sql`
- `pom.xml`
- `package.json`
- `.ai/archive/**`

DependsOn: `PC-V1`, `PC-V2`
ParallelGroup: validation-browser
ConflictBoundary: browser-validation-only

Acceptance:
- 真实登录后验证菜单进入和刷新不 404。
- 验证销售产品、配置模板、问题组、配置问题、配置答案的搜索、重置、分页、新增、编辑、详情、删除/停用。
- 验证配置求值器可基于 5 个样本返回真实结果。
- 检查 console error、network error、接口 403/500。
- 截图检查布局挤压、遮挡、字段溢出。

## Barrier 4

Checks:
- `/check` Lane A Static Review 已完成。
- `/check` Lane B Runtime Validation 已完成：SQL、compile、i18n、typecheck、build、dist、browser。
- 所有失败项必须记录真实原因，不能伪造通过。
- 全部通过后 Next Step 设置为 `Ready for /archive`。
