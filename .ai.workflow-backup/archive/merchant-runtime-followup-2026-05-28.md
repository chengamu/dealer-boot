# Merchant Runtime Follow-up

## Feature

浏览器反馈和 `/check` 后续修复：商家申请公开页、邮箱验证码、国家下拉、Terms / Privacy、法务文档、商家审核/资料路由、登录态全局控制、公开字典白名单、DEV 运行时验证。

## Final Status

Accepted with Risks.

## Scope

- 修复 `/merchant/apply` 公开访问、邮箱验证码、Terms / Privacy checkbox、国家下拉和法务链接。
- 新增法务文档维护和公开读取能力。
- 修复动态路由 fallback：`/system/tenantApplication`、`/merchantManagement/tenantApplication`、`/system/merchantProfile`、`/monitor/cacheList`。
- 收窄公开字典访问，只允许匿名读取 `sys_country`。
- 同步 PostgreSQL baseline / DEV DB seed：国家、字典、i18n、法务文档、`apply_locale`。
- 修复商家审核通过默认部门、OSS `dept_id` 兼容、登录页 i18n、全局 401 跳登录。
- 修复登录日志异步记录读取已回收 `HttpServletRequest` 的运行时异常。

## Out of Scope

- 不新增正式生产 upgrade / migration 覆盖脚本。
- 不实现跨语言国家 alias 搜索。
- 不实现邮件重发邀请、重置密码完整闭环。
- 不做提交/分支/PR。

## Completed

- `8081` 后端已由 clean 后重新打包的 `bocoo-admin/target/dist/bocoo-admin.jar` 启动。
- `8083` 前端 dev server 保持运行。
- 商家申请页公开访问、邮箱验证码、Terms / Privacy、国家选择可见。
- 登录成功状态访问 `/login` 会跳回 `/index`。
- `/system/tenantApplication` 与 `/system/merchantProfile` 登录后不再 404。
- 匿名国家字典返回 249 条；匿名访问非公开字典返回 401。
- 登录 API 返回 `tenantId=1`、`tenantType=PLATFORM`、`merchantId=null`。
- 错误 captcha 重放后不再出现 `request object has been recycled` / `AsyncConfig` 异步异常。

## Validation Summary

- `codegraph sync`: passed.
- `git diff --check -- . ':!logs/*'`: passed with CRLF warnings only.
- `pnpm build`: passed.
- `mvn test`: passed.
- `mvn -DskipTests package`: passed.
- Runtime: `8081` / `2831` listening on rebuilt jar process.
- API: `sys_country` anonymous 200 / 249, `sys_user_sex` anonymous 401, captcha login 200, `getInfo` platform context, `getRouters` contains MerchantManagement and tenantApplication, email-code 200.
- Browser: public apply page, login page, login redirect, tenant application route, merchant profile route verified by Playwright; console error count 0.

## Artifacts

- `.ai/artifacts/2026-05-28-runtime-api-check-after-fix.json`
- `.ai/artifacts/2026-05-28-browser-check-after-fix.json`
- `.ai/artifacts/2026-05-28-browser-merchant-apply-after-fix.png`
- `.ai/artifacts/2026-05-28-browser-merchant-profile-after-fix.png`
- `.ai/tmp/backend-8081.stdout.log`
- `.ai/tmp/backend-8081.stderr.log`

## Remaining Risks

- 正式生产环境上线时仍需重新制定备份、迁移、回滚和演练方案；本阶段仅按用户要求同步本地 DEV DB 和 PostgreSQL baseline。
- 工作区仍有本地产物/环境文件需要提交前筛选，例如 `.idea/*`、`logs/*.log`、验证截图和 `.ai/artifacts/*`。
- 国家下拉当前支持本地展示文案和 alpha-2 code 搜索；中文界面输入英文全称 alias 不是本阶段能力。
- `/check` 的静态审查由当前 Agent 执行；由于本轮用户未明确要求子 Agent，未使用 `code-reviewer` 子 Agent。

## Follow-up

- 如需生产迁移，另起任务制定 migration / rollback。
- 如需更强国家搜索，设计国家 alias 字段或专用 API。
- 如需完善邮件链路，补重发邀请、重置密码和模板管理。
- 提交前清理或忽略本地日志、IDE 和验证产物。

## Lessons

- Windows PowerShell 启动 Java 时，`-D...` JVM 参数应直接传给 `java.exe`，避免嵌套 PowerShell 解析破坏参数。
- 本地 `8081` 若旧 Java 进程占用端口，`Stop-Process` 不一定完成清理；可按用户授权用 `taskkill /F /T` 清理。
- Redisson 写入的 captcha value 是 JSON 字符串，直接读取 Redis 后要先 JSON parse 再提交。
- 异步事件不要跨线程读取 servlet request 的可变对象；发布事件前应快照 IP/User-Agent 等必要字段。

## Key Decisions

- 公开字典仍走白名单，只放行 `sys_country`。
- 法务文档默认状态保持 `DRAFT`，发布后公开接口读取。
- 平台租户 ID 继续使用固定值 `1`。

## Files Modified

详见 `git diff --stat`。本阶段主要涉及 `admin-ui/src/**`、`bocoo-modules-system/src/main/java/com/bocoo/system/**`、`bocoo-common/**`、`bocoo-admin/src/main/resources/**`、`sql/postgresql/base.sql`、`.ai/**` 和 `README.md`。
