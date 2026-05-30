# Archive: CODE_REVIEW 风险收敛与登录页 Curtain Reveal

## Feature：功能

基于更新后的 `CODE_REVIEW.md` 修复 5 个真实中低风险，并为登录页新增轻量 CSS Curtain Reveal 开场动画。

## Requirement Source：需求来源

- `D:\work\base-boot\CODE_REVIEW.md`
- `C:\Users\Administrator\Desktop\需求.txt`
- `D:\work\base-boot\.ai\requirements\20260530-code-review-curtain-intro.md`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\wave-plan.md`

## Final Status：最终状态

Accepted

## Scope：范围

- 修复登录锁定账号级 DoS 风险。
- 修复商户申请邮箱验证码每日配额被已注册邮箱静默消耗的问题。
- 修复 username/email 冲突时查询顺序不确定。
- 为 CORS 缺省来源提供本地开发 fallback。
- 调整公开字典接口限流阈值。
- 新增登录页 Curtain Reveal 动画组件。
- 做局部低风险质量清理。

## Out of Scope：不做范围

- 不修改数据库结构或 SQL/migration。
- 不升级依赖。
- 不切换 Cookie/CSRF 架构。
- 不处理 Quill 2.x 迁移。
- 不硬编码未知生产域名。
- 不默认重构日志清理泛型工具或 `XssFilter` GET/DELETE 策略。

## Completed：已完成

- `SysLoginService.checkLogin()` 改为 IP 维度主锁定 + 全局错误次数兜底，默认全局阈值 `20`，高于单 IP 阈值 `5`。
- `SysLogininforController.unlock()` 同步清理旧格式、IP 维度和全局错误计数 key。
- `SysTenantApplyService.sendEmailCode()` 将每日配额计数移动到邮箱可用性检查之后，已注册邮箱不会消耗发送额度。
- `loadUserByUsernameOrEmail()` 增加 `orderByAsc(SysUser::getUserId)`，冲突时结果稳定。
- `CorsProperties` 增加 localhost / 127.0.0.1 默认来源，仍不允许 `* + credentials`。
- `SysDictDataController.dictType()` 限流从 `60/min/IP` 调整为 `180/min/IP`，非公开字典仍要求登录。
- 新增 `CurtainIntro.vue`，登录页接入 CSS-only Curtain Reveal，支持点击跳过、sessionStorage 本会话一次、动画结束后移除 DOM、reduced motion 跳过。
- `forcePasswordChange` 使用 `UserConstants.FORCE_PASSWORD_CHANGE_YES/NO` 常量表达。
- 上传 FormData 复用经评估暂不强抽象，避免改变不同组件行为。

## Not Completed：未完成

- 无本批阻塞项。
- 后续仍可单独处理日志清理批量删除复用、`XssFilter` GET/DELETE 性能策略、Quill advisory、Cookie/CSRF 架构和生产 CORS 域名收紧。

## Validation Summary：验证摘要

- Passed: `codegraph sync`
- Passed: `git diff --check`
- Passed: `mvn -DskipTests compile`
- Passed: `admin-ui` `pnpm typecheck`
- Passed: `admin-ui` `pnpm build`
- Passed: Playwright Curtain Reveal browser validation:
  - 首次进入可见。
  - 自动结束后 DOM 移除。
  - `sessionStorage` 写入已看标记。
  - 本会话刷新不重复播放。
  - 点击遮罩可跳过。
  - 390x844 移动端视口正常自动移除。
- Passed: DEV backend runtime/API smoke after Redis was available:
  - `/captchaImage`: HTTP 200, business code 200.
  - unauthenticated `/getInfo`: HTTP 200, business code 401.
  - `/login`: HTTP 200, business code 200.
  - authenticated `/getInfo`: HTTP 200, business code 200.
  - authenticated `/getRouters`: HTTP 200, business code 200.
  - authenticated `/system/dict/data/type/sys_oper_type`: HTTP 200, business code 200.
  - authenticated `/monitor/operlog/list?pageNum=1&pageSize=1`: HTTP 200, business code 200.
- Not Required: i18n validation, because no i18n files changed.

## Remaining Risks：剩余风险

- Backend OWASP Dependency-Check 仍依赖 NVD/CISA 外部数据源，沿用上一归档风险。
- `@vueup/vue-quill -> quill@1.3.7` advisory 仍需后续独立评估。
- Cookie/CSRF 与生产 CORS 域名收紧仍等待正式域名和部署拓扑。
- 运行日志文件因本地验证发生变化，属于工作树中已有日志类噪音，不作为业务修改。

## Lessons from Learn：经验提炼

- Redis 验证码值可能是 JSON 字符串，使用前要反序列化；验证过程中不得输出验证码、口令或 Token。
- 无在线 Token 时，可以通过真实 `/captchaImage` + Redis 验证码完成 DEV 登录 smoke。
- 低风险复用建议要看组件行为差异；不同上传组件的状态、回调和接口参数不一致时，不应为了清报告强行抽象。

## Key Decisions：关键决策

- 登录锁定选择 IP 维度主锁定 + 全局兜底，不回退到纯 IP，也不保留 5 次账号级全局锁。
- CORS 默认来源只给本地开发，生产域名继续通过 YAML 后续配置。
- Curtain Reveal 不新增依赖和静态资源，保持 CSS + div。
- 上传复用、日志清理泛型工具和 `XssFilter` GET/DELETE 策略留作后续，不并入本批。

## Files Modified：修改文件

- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysLoginService.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\monitor\SysLogininforController.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-core\src\main\java\com\bocoo\common\core\constant\CacheConstants.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-core\src\main\java\com\bocoo\common\core\constant\UserConstants.java`
- `D:\work\base-boot\bocoo-admin\src\main\resources\application.yml`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\service\SysTenantApplyService.java`
- `D:\work\base-boot\bocoo-common\bocoo-common-web\src\main\java\com\bocoo\common\web\config\properties\CorsProperties.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysDictDataController.java`
- `D:\work\base-boot\bocoo-modules-system\src\main\java\com\bocoo\system\controller\system\SysLoginController.java`
- `D:\work\base-boot\admin-ui\src\components\AuthVisual\CurtainIntro.vue`
- `D:\work\base-boot\admin-ui\src\pages\auth\LoginPage.vue`
- `D:\work\base-boot\.ai\requirements\20260530-code-review-curtain-intro.md`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\wave-plan.md`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\check.md`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\task-events.jsonl`
- `D:\work\base-boot\.ai\CURRENT.md`
- `D:\work\base-boot\.ai\TASKS.md`
- `D:\work\base-boot\.ai\MEMORY.md`

## Artifacts：产物

- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\wave-plan.md`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\check.md`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\task-events.jsonl`
- `D:\work\base-boot\.ai\changes\20260530-code-review-curtain-intro\logs\`

## Follow-up：后续事项

- Backend Dependency-Check：等待 NVD API Key、内部镜像或外部数据源可用后重跑。
- Rich Text Editor Security：评估 `@vueup/vue-quill` 替换或 Quill 2.x 迁移。
- Cookie / CSRF Architecture：有正式域名和部署拓扑后单独 `/plan`。
- Production CORS Origins：有正式域名后更新 YAML。
- 日志清理批量删除复用：后续有清理窗口时处理。
- `XssFilter` GET/DELETE 性能策略：结合压测或明确安全策略单独评估。
