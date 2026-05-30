# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 2
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only

## Requirement Source

- `D:\work\base-boot\CODE_REVIEW.md`
- `C:\Users\Administrator\Desktop\需求.txt`
- `D:\work\base-boot\.ai\requirements\20260530-code-review-curtain-intro.md`

## Options

- Option A：修复 5 个真实风险 + 登录页 Curtain Reveal，低风险常量化一并处理。
- Option B：同时做全部复用重构和 XSS 策略调整。
- Recommendation：采用 Option A。`cleanLogininfor` / `cleanOperLog` 泛型抽象和 `XssFilter` GET/DELETE 跳过不进入默认实现，避免扩大安全和架构影响。

## Wave 0: Contract / Scope / File Boundary

### Task C1: 确认风险修复策略和文件边界

Owner: main
AgentRole: orchestrator
Status: pending
Priority: high
RiskLevel: high - 登录锁定、CORS、验证码配额均涉及安全边界，必须先定策略再改代码。
Wave: 0

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\monitor\SysLogininforController.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysTenantApplyService.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-web\src\main\java\com\bocoo\common\web\config\properties\CorsProperties.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysDictDataController.java`
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`

Forbidden:
- `D:\work\base-boot\sql\**`
- `D:\work\base-boot\pom.xml`
- `D:\work\base-boot\admin-ui\package.json`
- `D:\work\base-boot\.ai\archive\**`

DependsOn: none
ParallelGroup: planning
ConflictBoundary: no-business-code-change

Acceptance:
- 明确登录锁定采用 IP 维度主 key 和全局兜底 key。
- 明确本批不改 DB、不升级依赖、不切换认证架构。
- 明确 Curtain Reveal 不新增图片/视频/依赖。

## Barrier 0

Checks:
- 文件边界和 Forbidden 路径已写清。
- 没有未确认的 DB schema、依赖升级或生产域名需求。

## Wave 1: Independent Implementation

### Task B1: 修复后端 5 个真实风险

Owner: java-architect
AgentRole: backend
Status: pending
Priority: high
RiskLevel: high - 覆盖认证锁定、邮箱验证码、CORS 和公开字典限流，均为安全相关改动。
Wave: 1

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\monitor\SysLogininforController.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-core\src\main\java\com\bocoo\common\core\constant\CacheConstants.java`
- `D:\work\base-boot\bocoo-admin\src\main\resources\application.yml`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysTenantApplyService.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-web\src\main\java\com\bocoo\common\web\config\properties\CorsProperties.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysDictDataController.java`

Forbidden:
- `D:\work\base-boot\sql\**`
- `D:\work\base-boot\admin-ui\**`
- `D:\work\base-boot\pom.xml`
- `D:\work\base-boot\.ai\archive\**`

DependsOn:
- C1
ParallelGroup: backend
ConflictBoundary: backend-security-only

Acceptance:
- 登录错误锁定不会 5 次错误就全局锁死账号，全局锁定阈值高于单 IP 阈值。
- 手动解锁能清理新旧登录错误 key。
- `sendEmailCode` 仅对真正可能发送邮件的邮箱消耗每日配额。
- username/email 冲突查询结果具备确定排序。
- CORS 默认来源只包含本地开发来源，不允许 `* + credentials`。
- 字典接口限流提高到共享 IP 场景更合理的阈值，非公开字典仍要求登录。

### Task F1: 新增登录页 Curtain Reveal 组件

Owner: frontend-developer
AgentRole: frontend
Status: pending
Priority: high
RiskLevel: medium - 改动集中在登录页展示，但需要验证遮罩移除、性能和移动端效果。
Wave: 1

Files:
- `D:\work\base-boot\admin-ui\src\components\AuthVisual\CurtainIntro.vue`
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`

Forbidden:
- `D:\work\base-boot\bocoo-**\**`
- `D:\work\base-boot\sql\**`
- `D:\work\base-boot\admin-ui\package.json`
- `D:\work\base-boot\.ai\archive\**`

DependsOn:
- C1
ParallelGroup: frontend
ConflictBoundary: auth-login-visual-only

Acceptance:
- 只使用 CSS + `div`，不加载图片、视频、GIF、Lottie 或新依赖。
- 动画 1.2 到 1.5 秒内结束，点击可跳过。
- 使用 `sessionStorage` 本会话只播放一次。
- 动画结束后通过 `v-if` 移除 DOM。
- `prefers-reduced-motion: reduce` 下不播放。
- 不影响 `loadCaptcha`、登录提交、语言切换和强制改密跳转逻辑。

### Task Q1: 局部低风险质量清理

Owner: main
AgentRole: implementation
Status: pending
Priority: medium
RiskLevel: low - 仅做局部常量化；上传复用如差异较大则停止，不强行抽象。
Wave: 1

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\**\*.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-core\src\main\java\com\bocoo\common\core\constant\*.java`
- `D:\work\base-boot\admin-ui\src\components\Editor\index.vue`
- `D:\work\base-boot\admin-ui\src\components\FileUpload\index.vue`
- `D:\work\base-boot\admin-ui\src\components\ImageUpload\index.vue`
- `D:\work\base-boot\admin-ui\src\pages\system\user\UserPage.vue`

Forbidden:
- `D:\work\base-boot\sql\**`
- `D:\work\base-boot\pom.xml`
- `D:\work\base-boot\admin-ui\package.json`
- `D:\work\base-boot\.ai\archive\**`

DependsOn:
- C1
ParallelGroup: quality
ConflictBoundary: low-risk-local-cleanup

Acceptance:
- `forcePasswordChange` 的 `"1"` / `"0"` 判断优先用常量表达。
- 上传 FormData 复用只有在不改变组件行为且差异很小时才实施。
- 不新增重量级抽象，不做无关格式化。

## Barrier 1

Checks:
- 后端任务和前端任务没有越界修改 Forbidden 路径。
- 登录页动画不引入新依赖和静态资源。
- 安全修复没有放宽已有认证、租户和权限边界。

## Wave 2: Integration Alignment

### Task I1: 集成对齐与回归点补齐

Owner: main
AgentRole: orchestrator
Status: pending
Priority: high
RiskLevel: medium - 需要确认登录锁定、解锁、验证码、CORS、字典和登录页之间没有行为冲突。
Wave: 2

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\monitor\SysLogininforController.java`
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`
- `D:\work\base-boot\admin-ui\src\components\AuthVisual\CurtainIntro.vue`

Forbidden:
- `D:\work\base-boot\sql\**`
- `D:\work\base-boot\.ai\archive\**`

DependsOn:
- B1
- F1
- Q1
ParallelGroup: integration
ConflictBoundary: integration-only

Acceptance:
- 解锁逻辑能覆盖新旧 Redis key pattern。
- CORS YAML 与默认属性语义一致。
- Curtain Reveal 不遮挡 Element Plus overlay 清理逻辑，不改变登录页表单状态。

## Wave 3: Code Review / Security Review

### Task R1: 真实风险复审

Owner: code-reviewer
AgentRole: code-reviewer
Status: pending
Priority: high
RiskLevel: medium - 需要复查是否引入新的认证、CORS、前端遮罩或限流回归。
Wave: 3

Files:
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysTenantApplyService.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-web\src\main\java\com\bocoo\common\web\config\properties\CorsProperties.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysDictDataController.java`
- `D:\work\base-boot\admin-ui\src\components\AuthVisual\CurtainIntro.vue`
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`

Forbidden:
- none

DependsOn:
- I1
ParallelGroup: review
ConflictBoundary: review-only

Acceptance:
- 复审 `CODE_REVIEW.md` 风险 1 到 5 是否已收敛。
- 复审前端动画是否符合性能和可跳过要求。
- 标记未处理的低优先级建议和原因。

## Wave 4: Check / Validation

### Task V1: 执行验证

Owner: main
AgentRole: check
Status: pending
Priority: high
RiskLevel: medium - 后端和前端均有改动，需要编译、类型和浏览器验证。
Wave: 4

Files:
- `D:\work\base-boot\**`

Forbidden:
- `D:\work\base-boot\.ai\archive\**`

DependsOn:
- R1
ParallelGroup: check
ConflictBoundary: validation-only

Acceptance:
- `mvn -DskipTests compile` 真实执行并记录结果。
- `admin-ui` 执行 `pnpm typecheck` 和 `pnpm build` 并记录结果。
- 若 i18n 文件发生变化，执行 `pnpm i18n:validate` / `pnpm i18n:sync`。
- 通过浏览器验证登录页动画首播、跳过、会话不重复和移动端基本布局。
- 如可复用 Redis admin Token，则做登录相关 authenticated smoke；不输出 Token。

## Pause Points

- 准备运行 build/typecheck/browser validation 前暂停或更新状态。
- 如果登录全局错误阈值需要业务配置值而不是默认 20，回到 `/plan`。
- 如果上传复用差异明显，停止 Q1 的上传抽象并记录为后续任务。
