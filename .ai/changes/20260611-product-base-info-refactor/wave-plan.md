# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 3
- UseWorktree: true
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only
- RequirementSource: `.ai/requirements/20260611-product-base-info-refactor.md`
- ChangeId: `20260611-product-base-info-refactor`

## Agent Registry

| Agent | Source | Role |
| --- | --- | --- |
| `java-architect` | `.codex/agents/java-architect.toml` | Java / Spring / backend / SQL / transaction / permission / tenant |
| `frontend-developer` | `.codex/agents/frontend-developer.toml` | Vue / frontend / UI / component |
| `typescript-pro` | `.codex/agents/typescript-pro.toml` | TypeScript / API type / frontend type alignment |
| `code-reviewer` | `.codex/agents/code-reviewer.toml` | Static review / security / quality |
| `browser-debugger` | `.codex/agents/browser-debugger.toml` | Browser / e2e / runtime validation |
| `main` | orchestrator | AMU 编排、合并、Barrier、用户沟通 |

## Wave 0: Contract / Scope / File Boundary

### Task BASE-C1: 定义数据库与后端 contract

TaskId: `BASE-C1`
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 PostgreSQL 表结构、Java Entity/BO/VO、Controller API、transaction、permission 和 reference check 边界，匹配后端架构 Agent。
AgentRole: contract-backend
Status: pending
Priority: high
RiskLevel: high
Wave: 0

Files:
- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `.ai/changes/20260611-product-base-info-refactor/**`

Forbidden:
- `admin-ui/**`
- `pom.xml`
- `bocoo-admin/src/main/resources/application-prod*`
- `.ai/archive/**`

DependsOn: none
ParallelGroup: contract
ConflictBoundary: contract-only，先输出字段、表、API、权限、引用检查边界，不写实现代码。

Acceptance:
- 明确保留改造表和新增表。
- 明确 `material_code` 是唯一主业务编号。
- 明确样册编号、供应商料号、旧系统编号的字段边界。
- 明确面料系列和面料资料字段。
- 明确通用物料属性字段，避免每类物料新建 profile 表。
- 明确 OSS 复用字段。

### Task BASE-C2: 定义前端页面、API 类型和 i18n contract

TaskId: `BASE-C2`
Owner: `frontend-developer`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Vue 页面结构、Element Plus 表单/表格、i18n key 和权限入口，匹配前端 Agent。
AgentRole: contract-frontend
Status: pending
Priority: high
RiskLevel: medium
Wave: 0

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/i18n/locales/en_US.json`
- `.ai/changes/20260611-product-base-info-refactor/**`

Forbidden:
- `bocoo-modules-product/**`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn: none
ParallelGroup: contract
ConflictBoundary: contract-only，先定义页面入口、字段显示、API 类型和 i18n key，不写实现代码。

Acceptance:
- 明确 6 个基础资料模块入口：面料设置、物料设置、组件包、配置字典、附件资料、单位管理。
- 明确哪些继续用现有通用 grid，哪些需要抽屉或明细子表。
- 明确前端字段和后端 VO 字段映射。
- 明确权限 code 和 i18n key 方案。

### Task BASE-C3: 定义 OFBiz seed 映射与 5 个样本 contract

TaskId: `BASE-C3`
Owner: `main`
OwnerSource: main-fallback
OwnerReason: 该任务需要结合用户业务口径、老系统数据来源和 AMU 计划文档，先由 orchestrator 固化 seed contract，再交给实现任务执行。
AgentRole: contract-seed
Status: pending
Priority: high
RiskLevel: medium
Wave: 0

Files:
- `.ai/changes/20260611-product-base-info-refactor/**`
- `docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`
- `/Users/chengmuxuan/Desktop/cmx/ofbiz/**`

Forbidden:
- `admin-ui/**`
- `bocoo-modules-product/**`
- 任何包含明文连接串、密码、Token 的输出
- `.ai/archive/**`

DependsOn: none
ParallelGroup: contract
ConflictBoundary: seed-contract-only，不修改 OFBiz 代码和数据库。

Acceptance:
- 明确 OFBiz 表到新系统基础资料的 seed 映射。
- 明确至少 5 个产品测试样本名称和覆盖面。
- 明确无法访问附件本体时如何写 `legacy_*` 字段。
- 不输出敏感连接信息。

## Barrier 0

- `BASE-C1`、`BASE-C2`、`BASE-C3` 均为 `done`。
- DB / 后端 / 前端 / seed contract 不互相矛盾。
- 如果字段或权限命名冲突，停在 `/plan` 修订，不进入实现。

## Wave 1: Independent Implementation

### Task BASE-D1: 实现数据库脚本和基础 seed 字典

TaskId: `BASE-D1`
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 PostgreSQL DDL、索引、注释、UTC 字段和基础字典 seed，匹配后端架构 Agent。
AgentRole: database
Status: pending
Priority: high
RiskLevel: high
Wave: 1

Files:
- `sql/postgresql/product_capability.sql`

Forbidden:
- `admin-ui/**`
- `pom.xml`
- `bocoo-admin/src/main/resources/application-prod*`
- `.ai/archive/**`

DependsOn:
- `BASE-C1`
- `BASE-C3`
ParallelGroup: backend-db
ConflictBoundary: database-only

Acceptance:
- 新增或改造基础资料相关表。
- 表注释和字段注释清楚，时间字段保持 UTC 语义。
- 建立必要唯一约束和查询索引。
- 包含至少 5 个产品测试样本的基础 seed 或为 seed 工具预留稳定插入点。

### Task BASE-B1: 实现后端基础资料 API 与服务

TaskId: `BASE-B1`
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Controller、Service、Mapper、Entity、BO、VO、transaction、reference check，匹配后端架构 Agent。
AgentRole: backend
Status: pending
Priority: high
RiskLevel: high
Wave: 1

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/controller/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/service/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/mapper/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/**`

Forbidden:
- `admin-ui/**`
- `sql/postgresql/product_capability.sql`
- `pom.xml`
- `.ai/archive/**`

DependsOn:
- `BASE-C1`
ParallelGroup: backend-db
ConflictBoundary: backend-only

Acceptance:
- 6 个基础资料模块的后端 CRUD 可用。
- 写入物料、面料、组件时同步必要冗余字段。
- 禁用/删除时具备引用检查。
- 附件绑定复用 `oss_id` 和现有 OSS。
- 不引入每类物料 profile 表。

### Task BASE-F1: 实现前端基础资料页面和 API 类型

TaskId: `BASE-F1`
Owner: `frontend-developer`
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Vue 页面、API 调用、表单、表格、i18n 和权限，匹配前端 Agent。
AgentRole: frontend
Status: pending
Priority: high
RiskLevel: high
Wave: 1

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/i18n/locales/en_US.json`

Forbidden:
- `bocoo-modules-product/**`
- `sql/postgresql/product_capability.sql`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn:
- `BASE-C2`
ParallelGroup: frontend
ConflictBoundary: frontend-only

Acceptance:
- 面料设置、物料设置、组件包、配置字典、附件资料、单位管理均有入口。
- 物料属性和组件明细可以维护，不只是展示主表。
- 可见文案进入 `en_US.json`，不新增硬编码 UI 文案。
- 不新增重量级依赖。

### Task BASE-S1: 实现 OFBiz 开发 seed 工具或 SQL

TaskId: `BASE-S1`
Owner: `java-architect`
OwnerSource: agent-registry
OwnerReason: 该任务涉及老系统数据读取、字段映射、开发 seed 写入和敏感信息处理，匹配后端架构 Agent。
AgentRole: seed-backend
Status: pending
Priority: medium
RiskLevel: medium
Wave: 1

Files:
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `sql/postgresql/product_capability.sql`
- `.ai/changes/20260611-product-base-info-refactor/**`

Forbidden:
- `admin-ui/**`
- `/Users/chengmuxuan/Desktop/cmx/ofbiz/**` 的代码修改
- 任何包含明文连接串、密码、Token 的输出
- `.ai/archive/**`

DependsOn:
- `BASE-C3`
ParallelGroup: seed
ConflictBoundary: seed-only，允许读取 OFBiz 结构，不修改 OFBiz 项目。

Acceptance:
- 至少 5 个产品测试样本可写入新系统。
- 样本覆盖分类、面料、物料属性、组件明细、附件绑定。
- 无法导入文件本体时正确记录 `legacy_*`。
- 不输出敏感连接信息。

## Barrier 1

- 数据库、后端、前端、seed 任务无 Forbidden 违规。
- 前后端字段 contract 仍一致。
- 如果 `BASE-D1` 与 `BASE-B1` 同改 SQL，需要由 `main` 合并处理。

## Wave 2: Integration Alignment

### Task BASE-I1: 对齐前后端 API、VO、类型和权限

TaskId: `BASE-I1`
Owner: `typescript-pro`
OwnerSource: agent-registry
OwnerReason: 该任务重点是 TypeScript API 类型、字段名、枚举和前后端 contract 对齐，匹配 TypeScript Agent。
AgentRole: integration-types
Status: pending
Priority: high
RiskLevel: medium
Wave: 2

Files:
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/vo/**`
- `bocoo-modules-product/src/main/java/com/bocoo/product/domain/bo/**`
- `.ai/changes/20260611-product-base-info-refactor/**`

Forbidden:
- `pom.xml`
- `package.json`
- `pnpm-lock.yaml`
- `.ai/archive/**`

DependsOn:
- `BASE-B1`
- `BASE-F1`
ParallelGroup: integration
ConflictBoundary: integration-only

Acceptance:
- API request / response 字段名一致。
- 枚举、状态、权限 code、分页结构一致。
- 前端无明显类型逃逸或重复字段。

### Task BASE-I2: 对齐 seed 数据和业务验收样本

TaskId: `BASE-I2`
Owner: `main`
OwnerSource: main-fallback
OwnerReason: 该任务需要按用户业务目标检查 5 个样本覆盖面，并协调 SQL、后端和前端数据入口。
AgentRole: integration-seed
Status: pending
Priority: high
RiskLevel: medium
Wave: 2

Files:
- `sql/postgresql/product_capability.sql`
- `.ai/changes/20260611-product-base-info-refactor/**`
- `docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`

Forbidden:
- `admin-ui/package.json`
- `pom.xml`
- `.ai/archive/**`

DependsOn:
- `BASE-D1`
- `BASE-S1`
ParallelGroup: integration
ConflictBoundary: seed-validation-only

Acceptance:
- 5 个样本均可映射到基础资料对象。
- 每个样本至少覆盖分类、面料/物料、组件包、附件中的 3 类。
- 记录 seed 覆盖矩阵，供 `/do` 验证。

## Barrier 2

- 前后端字段、权限、seed 样本无明显冲突。
- 若需要修改 contract，回到 `/plan` 修订。

## Wave 3: Static Review / Security Review

### Task BASE-R1: 静态审查基础资料重构风险

TaskId: `BASE-R1`
Owner: `code-reviewer`
OwnerSource: agent-registry
OwnerReason: 该任务需要审查权限、SQL、transaction、NPE、DTO/VO 边界、XSS、敏感信息和数据泄露风险，匹配 code review Agent。
AgentRole: static-review
Status: pending
Priority: high
RiskLevel: high
Wave: 3

Files:
- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/src/main/java/com/bocoo/product/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/i18n/locales/en_US.json`

Forbidden:
- `.ai/archive/**`

DependsOn:
- `BASE-I1`
- `BASE-I2`
ParallelGroup: review
ConflictBoundary: review-only，不直接改代码，先输出 findings。

Acceptance:
- findings 按严重程度排序。
- 覆盖权限、SQL 注入、事务、软删除、租户、分页、i18n、XSS、敏感信息。
- 如果无阻塞问题，明确剩余风险。

## Wave 4: Check / Validation

### Task BASE-V1: 后端与 SQL 验证

TaskId: `BASE-V1`
Owner: `main`
OwnerSource: main-fallback
OwnerReason: 验证命令需要结合本地 Docker、Maven 和开发数据库状态，由 orchestrator 执行并记录真实结果。
AgentRole: runtime-backend
Status: pending
Priority: high
RiskLevel: high
Wave: 4

Files:
- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/**`
- `.ai/changes/20260611-product-base-info-refactor/**`

Forbidden:
- `bocoo-admin/src/main/resources/application-prod*`
- `.ai/archive/**`

DependsOn:
- `BASE-R1`
ParallelGroup: validation
ConflictBoundary: validation-only

Acceptance:
- `codegraph sync` 成功。
- PostgreSQL 初始化脚本在开发库执行成功。
- 后端模块编译或最小可行检查通过。
- 记录未能执行的检查和原因。

### Task BASE-V2: 前端和浏览器验证

TaskId: `BASE-V2`
Owner: `browser-debugger`
OwnerSource: agent-registry
OwnerReason: 该任务需要打开页面、检查交互、console、network 和视觉入口，匹配 Browser / runtime validation Agent。
AgentRole: runtime-browser
Status: pending
Priority: high
RiskLevel: medium
Wave: 4

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/i18n/locales/en_US.json`
- `.ai/changes/20260611-product-base-info-refactor/**`

Forbidden:
- `bocoo-modules-product/**`
- `sql/postgresql/product_capability.sql`
- `.ai/archive/**`

DependsOn:
- `BASE-R1`
ParallelGroup: validation
ConflictBoundary: browser-validation-only

Acceptance:
- 前端 typecheck / build 真实执行并记录结果。
- 浏览器打开基础资料 6 个模块入口。
- 新增/编辑弹窗或抽屉基本可打开。
- 无明显 console error、network error、文本溢出或入口缺失。

## Final Barrier

- 所有任务为 `done`，或明确记录 `blocked` / `failed` 原因。
- 验收标准对应需求文件逐条核对。
- 更新 `.ai/CURRENT.md` Next Step 为 `Ready for /archive`。
