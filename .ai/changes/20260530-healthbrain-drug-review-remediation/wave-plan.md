# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 2
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only

## Requirement Source

- `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md`
- `.ai/requirements/20260530-healthbrain-drug-review-remediation.md`

## Wave 0: Contract / Scope / File Boundary

### Task HBD-0: 确认报告问题与执行边界

Owner: main
AgentRole: orchestrator
Status: done
Priority: high
RiskLevel: medium，报告混合了可修复问题、非漏洞项和高风险架构建议。
Wave: 0

Files:
- `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md`
- `D:\work\base-boot\pom.xml`
- `D:\work\base-boot\sql\base.sql`
- `D:\work\base-boot\sql\postgresql\base.sql`
- `D:\work\base-boot\i18n\scripts\sync.ts`
- `D:\work\base-boot\bocoo-common\bocoo-common-mybatis\src\main\java\com\bocoo\common\mybatis\core\domain\BaseEntity.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-mybatis\src\main\java\com\bocoo\common\mybatis\core\service\IBaseService.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-mybatis\src\main\java\com\bocoo\common\mybatis\core\service\impl\BaseServiceImpl.java`

Forbidden:
- 业务代码修改
- 数据库结构修改
- 依赖升级

DependsOn: none
ParallelGroup: planning
ConflictBoundary: ai-docs-only

Acceptance:
- 已确认 CodeGraph 同步成功。
- 已区分可修复问题、非漏洞项、暂缓架构项。

## Barrier 0

Checks:
- Scope 已明确。
- 本轮默认不修 MySQL 旧脚本。
- 本轮执行轻量 BO 契约治理：只处理 `BaseBo`、现有 `*Bo` 继承和代码生成器 BO 模板。
- 本轮不拆分 `IBaseService` / `BaseServiceImpl`，不深拆 `BaseEntity`，不强制 VO 继承。
- Google 登录第一版不自动创建系统用户、不新增绑定表；优先匹配现有启用用户邮箱，未匹配时按申请状态提示下一步。

## Wave 1: Independent Implementation

### Task HBD-1: 修复根 POM 中文注释乱码

Owner: main
AgentRole: backend
Status: pending
Priority: high
RiskLevel: low，只改注释。
Wave: 1

Files:
- `D:\work\base-boot\pom.xml`

Forbidden:
- 依赖版本调整
- 模块增删
- 子模块 POM 修改

DependsOn:
- HBD-0
ParallelGroup: docs-config
ConflictBoundary: root-pom-comments-only

Acceptance:
- 已识别乱码注释修复为可读中文或删除无价值注释。
- XML 结构不变。

### Task HBD-2: 明确 i18n 同步模型并校验三处资源一致

Owner: frontend-developer
AgentRole: frontend-i18n
Status: pending
Priority: medium
RiskLevel: low，当前已有同步脚本。
Wave: 1

Files:
- `D:\work\base-boot\package.json`
- `D:\work\base-boot\i18n\scripts\sync.ts`
- `D:\work\base-boot\i18n\locales\*.json`
- `D:\work\base-boot\admin-ui\public\i18n\*.json`
- `D:\work\base-boot\bocoo-admin\src\main\resources\i18n\*.json`
- `D:\work\base-boot\AGENTS.md`

Forbidden:
- 新增 i18n 运行时机制
- 删除运行时 i18n 资源目录
- 硬编码 UI 文案

DependsOn:
- HBD-0
ParallelGroup: docs-config
ConflictBoundary: i18n-docs-and-validation-only

Acceptance:
- 三处 i18n JSON hash 一致。
- 文档不把运行时资源重复误描述为漏洞。

### Task HBD-3: 记录旧 MySQL 脚本健康大脑字典乱码风险

Owner: java-architect
AgentRole: database-review
Status: pending
Priority: medium
RiskLevel: medium，旧脚本存在真实乱码但不在当前 PostgreSQL 主链路。
Wave: 1

Files:
- `D:\work\base-boot\sql\base.sql`
- `D:\work\base-boot\sql\postgresql\base.sql`
- `.ai/requirements/20260530-healthbrain-drug-review-remediation.md`

Forbidden:
- 修改数据库结构
- 修改 PostgreSQL 表结构
- 新增 MySQL 维护承诺

DependsOn:
- HBD-0
ParallelGroup: db-docs
ConflictBoundary: sql-risk-record-only

Acceptance:
- 已记录 `sql/base.sql` 风险。
- 未误改 PostgreSQL 主初始化结构。

## Barrier 1

Checks:
- Wave 1 文件边界无冲突。
- 未修改依赖版本、数据库结构或业务逻辑。

## Wave 2: Integration Alignment

### Task HBD-4: 新增轻量 BO 基类并迁移现有 BO

Owner: java-architect
AgentRole: architecture
Status: pending
Priority: high
RiskLevel: medium，涉及系统 BO 和健康大脑参考 BO，但不改数据库结构、不改 Service 体系。
Wave: 2

Files:
- `D:\work\base-boot\bocoo-common\bocoo-common-mybatis\src\main\java\com\bocoo\common\mybatis\core\domain\BaseBo.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\domain\bo\*.java`
- `D:\work\base-boot\bocoo-modules-dealer\src\main\java\com\bocoo\healthbrain\domain\bo\*.java`

Forbidden:
- 拆分 `IBaseService`
- 修改 `BaseServiceImpl`
- 修改 Entity 审计继承关系
- 修改数据库结构

DependsOn:
- HBD-1
- HBD-2
- HBD-3
ParallelGroup: backend-architecture
ConflictBoundary: bo-contract-only

Acceptance:
- 新增轻量 `BaseBo`，包含 `searchValue` 和 `params`，不包含审计字段。
- 现有 `*Bo extends BaseEntity` 迁移为继承 `BaseBo`。
- 查询逻辑中的 `bo.getParams()` 继续可用。

### Task HBD-4B: 更新代码生成器 BO 模板

Owner: java-architect
AgentRole: generator
Status: pending
Priority: high
RiskLevel: medium，代码生成器是未来业务扩散源头，必须和 BO 基类同步。
Wave: 2

Files:
- `D:\work\base-boot\bocoo-modules-generator\src\main\resources\vm\java\bo.java.vm`

Forbidden:
- 增加新业务生成文件数量
- 拆分 Service 接口体系
- 修改数据库结构

DependsOn:
- HBD-4
ParallelGroup: generator-template
ConflictBoundary: generator-bo-template-only

Acceptance:
- 新生成 BO 默认继承轻量 `BaseBo`。
- 新功能生成文件数量不增加。
- 生成模板不再扩散 `Bo extends BaseEntity`。

### Task HBD-G1: 实现 Google 登录后端校验入口

Owner: java-architect
AgentRole: backend-auth
Status: pending
Priority: high
RiskLevel: high，登录入口属于安全边界，必须校验 Google ID token 的签名、audience、issuer、expiry 和邮箱可信度。
Wave: 2

Files:
- `D:\work\base-boot\pom.xml`
- `D:\work\base-boot\bocoo-admin\src\main\resources\application*.yml`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysLoginController.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\domain\bo\GoogleLoginBody.java`
- `D:\work\base-boot\i18n\locales\*.json`

Forbidden:
- 自动创建用户
- 新增 Google 账号绑定表
- 绕过用户状态、租户、角色、权限加载
- 输出或提交 Google Client Secret

DependsOn:
- HBD-0
ParallelGroup: google-auth-backend
ConflictBoundary: backend-auth-only

Acceptance:
- 新增 `/googleLogin` 或等价 Sa-Token 登录入口，返回结构与 `/login` 一致。
- 后端只接受配置的 Google Web Client ID 对应的 ID token。
- 只允许匹配现有启用用户邮箱；登录成功后复用现有 `LoginUser`、tenant、role、permission 构建流程。
- 如果 Google 邮箱未匹配现有用户，按 `sys_tenant_apply.email` 查询并返回结构化状态：`APPLY_REQUIRED`、`APPLY_PENDING`、`APPLY_REJECTED` 或 `APPLY_APPROVED_BUT_USER_MISSING`。
- 申请状态查询只能基于已验证 Google token 中的邮箱，不能接受前端单独传入邮箱来查状态。
- `APPLY_REJECTED` 允许用户重新申请；旧 `REJECTED` 记录保留，新申请走现有商户申请流程插入新的 `PENDING` 记录。
- 未配置 Google Client ID 时接口明确失败，不做假登录。

### Task HBD-G2: 接入登录页 Google Identity Services 按钮

Owner: frontend-developer
AgentRole: frontend-auth
Status: pending
Priority: high
RiskLevel: medium，登录页是用户入口，按钮必须符合 Google 品牌规范并处理未配置状态。
Wave: 2

Files:
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`
- `D:\work\base-boot\admin-ui\src\api\auth.ts`
- `D:\work\base-boot\admin-ui\src\stores\user.ts`
- `D:\work\base-boot\admin-ui\src\utils\config.ts`
- `D:\work\base-boot\i18n\locales\*.json`

Forbidden:
- 使用单色字母 `G` 伪 Logo
- 前端伪造登录成功
- 硬编码 Google Client ID
- 新增无关 UI 重设计

DependsOn:
- HBD-G1
ParallelGroup: google-auth-frontend
ConflictBoundary: login-page-and-auth-api-only

Acceptance:
- 登录页 Google 按钮使用 Google Identity Services 官方渲染，或使用符合官方品牌规范的彩色 `G` 和按钮样式。
- 前端从配置读取 `VITE_GOOGLE_CLIENT_ID`；未配置时不展示不可点击假按钮。
- Google 登录成功后复用现有 token 存储、`loadUser()` 和 redirect 逻辑。
- Google 邮箱未匹配现有用户时，前端按后端状态展示：无申请则引导商户申请并预填邮箱；待审核则提示等待审核；已拒绝则提示重新申请或联系平台；已通过但缺用户则提示联系平台。
- 已拒绝后点击重新申请时跳转商户申请页并预填 Google 邮箱，不覆盖旧申请记录。
- i18n 文案使用 `i18n/locales` 单源并同步到运行时资源。

## Barrier 2

Checks:
- 没有把轻量 BO 治理扩大成 Service / Entity / VO 全局重构。
- Google 登录没有扩大成自动创建系统用户、账号绑定表或额外 Google API scopes。
- 没有扩大 Scope。

## Wave 3: Code Review / Security Review

### Task HBD-5: 最终复核

Owner: code-reviewer
AgentRole: code-reviewer
Status: pending
Priority: medium
RiskLevel: medium，需要确认 Scope 未扩大。
Wave: 3

Files:
- `D:\work\base-boot\pom.xml`
- `.ai/requirements/20260530-healthbrain-drug-review-remediation.md`
- `.ai/TASKS.md`
- `.ai/CURRENT.md`
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysLoginController.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`

Forbidden:
- 业务实现修改
- 数据库结构修改
- 依赖升级

DependsOn:
- HBD-4B
- HBD-G2
ParallelGroup: review
ConflictBoundary: review-only

Acceptance:
- 复核所有变更符合 Scope。
- 复核 Google 登录无前端信任、无自动创建系统用户、无申请信息泄漏、无敏感配置泄漏。
- 未执行的 build/test 不声称通过。

## Wave 4: Check / Validation

Checks:
- `pom.xml` 乱码注释文本检查。
- i18n 三处 JSON hash 检查。
- Java 基础类、系统 BO、生成器模板变更后执行 targeted Maven compile。
- Google 登录前端变更后执行 admin-ui typecheck/build 或等价检查，并用浏览器验证登录页按钮状态。
