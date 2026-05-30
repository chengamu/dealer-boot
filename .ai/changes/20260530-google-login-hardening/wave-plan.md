# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 1
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only

## Requirement Source

- `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md`
- `.ai/requirements/20260530-google-login-hardening.md`

## Wave 0: Report Triage / Boundary

### Task GLH-0: 核验更新报告与执行边界

Owner: main
AgentRole: orchestrator
Status: done
Priority: high
RiskLevel: medium，报告混有可修复项、架构债和缺证据项。
Wave: 0

Files:
- `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md`
- `.ai/requirements/20260530-google-login-hardening.md`

Forbidden:
- 业务代码修改
- 数据库结构修改
- 依赖升级

DependsOn: none
ParallelGroup: planning
ConflictBoundary: ai-docs-only

Acceptance:
- 已执行 `codegraph sync`。
- 已核验 M1/L2/L3/L4 为本批可修项。
- 已确认 M3 是既定 i18n 分发模型，M4 对应 `HealthbrainDrug Mapper XML` 已由用户确认移除，L5 字段仍被服务层使用。

## Barrier 0

Checks:
- 本批不处理数据库、依赖升级、账号绑定表和第三方登录扩展。
- 本批只做 Google 登录校验逻辑解耦和低风险整理。

## Wave 1: Backend Hardening

### Task GLH-1: 提取 `GoogleAuthService`

Owner: main
AgentRole: java-architect
Status: done
Priority: high
RiskLevel: medium，登录鉴权边界需要保持行为不变。
Wave: 1

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\GoogleAuthService.java`
- `D:\work\base-boot\bocoo-admin\src\main\resources\application.yml`

Forbidden:
- 自动创建用户
- 新增 Google 账号绑定表
- 改变登录成功响应结构
- 改变申请状态分流
- 依赖升级

DependsOn:
- GLH-0
ParallelGroup: backend-auth
ConflictBoundary: google-token-validation-only

Acceptance:
- Google JWT 解析、签名校验、公钥缓存、certs 拉取和 claim 校验从 `SysLoginService` 移出。
- `SysLoginService` 只调用 `GoogleAuthService.verifyCredential(...)` 或等价方法获取已验证邮箱。
- 新增 `auth.google.certs-url`，默认值保持 Google 官方 URL。
- Google 登录流程行为保持不变。

### Task GLH-2: 补充 `GoogleLoginBody` 显式无参构造

Owner: main
AgentRole: java-architect
Status: done
Priority: low
RiskLevel: low，DTO 注解补充。
Wave: 1

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\domain\bo\GoogleLoginBody.java`

Forbidden:
- 改字段名
- 改校验 key

DependsOn:
- GLH-0
ParallelGroup: backend-dto
ConflictBoundary: google-login-body-only

Acceptance:
- `GoogleLoginBody` 显式添加 `@NoArgsConstructor`。

## Barrier 1

Checks:
- 后端 Google 登录业务语义没有变化。
- 没有新增依赖或数据库变更。

## Wave 2: Frontend Type Cleanup

### Task GLH-3: 迁移 Google 全局类型声明

Owner: main
AgentRole: typescript-pro
Status: done
Priority: medium
RiskLevel: low，只移动类型声明。
Wave: 2

Files:
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`
- `D:\work\base-boot\admin-ui\src\types\google.d.ts`

Forbidden:
- 登录页视觉重设计
- 改 Google 登录流程
- 硬编码 Google Client ID

DependsOn:
- GLH-0
ParallelGroup: frontend-types
ConflictBoundary: google-types-only

Acceptance:
- `LoginPage.vue` 不再内联 `GoogleCredentialResponse`、`GoogleAccountsId`、`Window.google` 全局声明。
- 前端 typecheck 能识别 `window.google`。

## Barrier 2

Checks:
- 前后端改动范围符合计划。
- M2/M3/L1/L5 均只记录不实施；M4 已确认不适用。

## Wave 3: Validation

### Task GLH-4: targeted checks

Owner: main
AgentRole: orchestrator
Status: done
Priority: high
RiskLevel: medium，登录相关改动需要编译和类型检查兜底。
Wave: 3

Files:
- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/changes/20260530-google-login-hardening/wave-plan.md`

Forbidden:
- 未运行却标记 passed

DependsOn:
- GLH-1
- GLH-2
- GLH-3
ParallelGroup: validation
ConflictBoundary: validation-record-only

Acceptance:
- 执行 `codegraph sync`。
- 执行 `mvn -pl bocoo-admin -am -DskipTests compile`。
- 执行 `pnpm --dir admin-ui typecheck`。
- 视改动情况执行 `pnpm --dir admin-ui build` 或记录 Not Run。
