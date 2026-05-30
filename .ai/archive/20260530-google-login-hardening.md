# Archive: Google Login Hardening

## Feature：功能

Google 登录加固后续批次。

## Requirement Source：需求来源

- `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md`
- `.ai/requirements/20260530-google-login-hardening.md`

## Final Status：最终状态

Accepted。

## Scope：范围

- 提取 Google ID token 校验、公钥缓存和 certs 拉取逻辑。
- 配置化 Google certs URL。
- 补充 `GoogleLoginBody` 显式无参构造。
- 迁移登录页 Google 全局类型声明。

## Out of Scope：不做范围

- 未修改数据库结构或 migration。
- 未新增 Google 账号绑定表。
- 未改变现有 Google 邮箱匹配登录策略。
- 未新增第三方登录提供商。
- 未修改 i18n 同步模型。
- 未处理已移除的 `HealthbrainDrug Mapper XML`。
- 未升级依赖。

## Completed：已完成

- 新增 `GoogleAuthService`，集中处理 Google JWT 解析、签名校验、公钥缓存、certs 拉取和 claim 校验。
- `SysLoginService` 只保留 Google 登录编排：已验证邮箱、用户匹配、Sa-Token 登录、申请状态分流。
- `auth.google.certs-url` 增加配置入口，默认仍使用 Google 官方 certs URL。
- `GoogleLoginBody` 增加 `@NoArgsConstructor`。
- 新增 `admin-ui/src/types/google.d.ts`，`LoginPage.vue` 不再内联 Google 全局类型声明。
- `.ai/CURRENT.md`、`.ai/TASKS.md` 和 wave plan 已更新任务完成状态。

## Not Completed：未完成

- 无本批阻塞项。
- `SysLoginService` 直调 `SysTenantApplyMapper` 保持现状；当前只基于已验证 Google 邮箱查询本人申请状态，后续申请查询逻辑复杂化时再评估迁移。
- 静态 `HttpClient` 保持现状；如公司网络需要统一代理或 SSL 策略，再改为 Spring 管理的 HTTP client。

## Validation Summary：验证摘要

- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- `pnpm --dir admin-ui typecheck`：passed。
- `pnpm --dir admin-ui build`：passed。
- `codegraph sync`：passed，already up to date。
- `git diff --check`：passed，仅有 CRLF warning。

## Remaining Risks：剩余风险

- Google 生产 `Authorized JavaScript origins` 仍等待正式域名或 Nginx 同源代理拓扑确认。
- Cookie/CSRF 与生产 CORS 收紧仍等待部署拓扑确认。
- 如后续需要 Google 多账号绑定能力，应单独设计绑定表。

## Lessons from Learn：经验提炼

- 第三方登录 token 校验应和登录编排分离：校验服务只返回已验证身份，登录服务再做用户匹配、租户、角色和权限构建。
- 外部认证的公钥 / certs URL 应配置化，但默认值必须保持官方地址，避免改变现有登录行为。

## Key Decisions：关键决策

- M1/L2/L3/L4 已处理。
- M2、M3、L1、L5 不进入本批实现。
- M4 已由用户确认不适用，因为 `HealthbrainDrug Mapper XML` 已移除。

## Files Modified：修改文件

- `bocoo-modules-system/src/main/java/com/bocoo/system/service/GoogleAuthService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLoginService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/bo/GoogleLoginBody.java`
- `bocoo-admin/src/main/resources/application.yml`
- `admin-ui/src/pages/auth/LoginPage.vue`
- `admin-ui/src/types/google.d.ts`
- `.ai/requirements/20260530-google-login-hardening.md`
- `.ai/changes/20260530-google-login-hardening/wave-plan.md`
- `.ai/CURRENT.md`
- `.ai/TASKS.md`

## Artifacts：产物

- `.ai/requirements/20260530-google-login-hardening.md`
- `.ai/changes/20260530-google-login-hardening/wave-plan.md`

## Follow-up：后续事项

- 等正式域名或同源代理拓扑确认后配置 Google production origin。
- 后续如接入新的第三方登录，优先按 `GoogleAuthService` 的边界设计独立 provider auth service。
