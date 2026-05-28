# Current

## Status

Check completed. Awaiting workspace cleanup / archive decision.

## Current Goal

完成“跨境订单系统”的多租户 + 商家注册/审核主链路：

- 登录页不允许选择租户，登录后自动识别租户。
- 平台真实租户 ID 固定为 `1`，`tenant_id = 0` 不再作为合法业务租户或 fallback。
- 商家注册页只提交申请资料，不提交/保存密码。
- 平台审核通过后自动创建商家租户、商家资料、管理员账号、角色和菜单权限。
- 登录支持用户名/邮箱 + 密码，并返回 `tenantId / tenantType / merchantId`。
- 平台可受控跨租户管理；商家只能访问自己租户下的数据。
- 新增可见文案走 i18n；绝对时间保持 UTC 语义。

## Scope

已覆盖：

- 后端租户上下文、登录上下文、商家申请审核、`merchant_profile`、API、权限隔离、后端 i18n message。
- PostgreSQL baseline / seed / 索引 / 菜单权限 / i18n seed。
- 前端商家注册页、平台商家审核、平台商家资料查看、商家端资料维护、API 类型、路由映射、i18n、UTC 展示。
- 本地 dealer 数据库作为目标环境直接完成 schema/seed 同步；不保留独立升级/覆盖类脚本。

未覆盖：

- 不做登录页租户选择。
- 不新增重量级邮件或模板系统。
- 不修改旧 MySQL SQL。
- 不做订单、商品、发货等业务表的全量租户改造。
- 不提供正式生产环境迁移脚本；未来如有生产环境需重新制定备份和迁移策略。

## Completed

- 平台租户策略已落地：平台真实租户 ID 为 `1`，平台身份通过 `tenantType = PLATFORM` 判断。
- PostgreSQL baseline 已调整：平台/system seed 迁移到 `tenant_id = 1`，新增 `merchant_profile`，补齐申请字段、索引、菜单权限和 i18n seed。
- 本地 dealer 数据库已直接同步 schema/seed，旧无引用 `tenant_id = 0` 租户记录已删除，校验剩余 `tenantZero=0`。
- Redis 已清理，避免旧 session 缺失 `tenantType/merchantId`。
- 后端已新增/调整商家资料、商家申请、审核通过、登录上下文、权限边界和邮件通知逻辑。
- 前端已完成商家申请页字段收敛、平台审核/商家资料页面、商家端资料维护页、API 类型、路由映射和 locale。
- 邮件发送能力已用 DEV SMTP 真实验证成功；接口不返回明文初始密码。
- `/check` 阶段发现的公开申请审计字段伪造、平台/商家身份边界、邮件 i18n、租户过滤顺序等风险已修复并复验。

## Not Completed

- 未做生产环境迁移方案或备份方案。
- 未实现邮件模板、重发邀请、重置密码等后续增强能力。
- 未验证所有浏览器和移动端响应式视口。
- 未处理工作区中大量无法明确归因到本需求的 dirty / untracked 文件；因此当前不建议直接整体提交或 `/archive`。

## Key Changed Files

后端：

- `bocoo-admin/src/main/resources/application.yml`
- `bocoo-common/bocoo-common-core/.../TenantProperties.java`
- `bocoo-common/bocoo-common-core/.../TenantConstants.java`
- `bocoo-common/bocoo-common-core/.../TenantContextHolder.java`
- `bocoo-common/bocoo-common-core/.../LoginUser.java`
- `bocoo-common/bocoo-common-satoken/.../LoginHelper.java`
- `bocoo-modules-system/pom.xml`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysLoginController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLoginService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/MerchantProfileController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/{bo,entity,vo}/MerchantProfile*`
- `bocoo-modules-system/src/main/java/com/bocoo/system/mapper/MerchantProfileMapper.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/MerchantProfileService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysTenantApplyController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/{bo,entity,vo}/SysTenantApply*`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysTenantApplyService.java`
- `bocoo-admin/src/main/resources/i18n/messages*.properties`

前端：

- `admin-ui/src/api/auth.ts`
- `admin-ui/src/api/merchant/application.ts`
- `admin-ui/src/api/merchant/profile.ts`
- `admin-ui/src/locales/zh_CN.ts`
- `admin-ui/src/locales/en_US.ts`
- `admin-ui/src/pages/auth/MerchantApplyPage.vue`
- `admin-ui/src/pages/merchant/MerchantProfileDescriptions.vue`
- `admin-ui/src/pages/merchant/MerchantProfilePage.vue`
- `admin-ui/src/pages/system/MerchantProfilePage.vue`
- `admin-ui/src/pages/system/TenantApplicationsPlaceholder.vue`
- `admin-ui/src/router/index.ts`
- `admin-ui/src/stores/permission.ts`
- `admin-ui/src/stores/user.ts`
- `admin-ui/src/types/api.ts`

SQL：

- `sql/postgresql/base.sql`

## Validation

Static Validation：

- 已调用 `code-reviewer` 做只读复核。
- 复核发现的问题已处理：公开申请审计字段伪造、平台/商家身份边界、邮件 i18n、租户上下文过滤顺序、前端状态/i18n 展示。

Runtime Validation：

- `mvn -pl bocoo-admin -am -DskipTests package`：成功，测试按命令跳过。
- `mvn -DskipTests compile`：成功。
- `pnpm build`：成功，包含 `vue-tsc --noEmit && vite build`。
- `pnpm typecheck`：成功。
- 后端可执行 jar 已启动验证，`GET /actuator/health` 返回 `UP`。
- 前端 Vite dev server 已启动验证，验证结束后已停止本轮 8081 / 8083 服务。
- `git diff --check`：本需求相关改动无 whitespace error；仅运行日志曾出现无关 trailing whitespace，未清理无关日志改动。

Database / Redis Validation：

- 已同步本地 dealer 数据库 schema/seed。
- 校验结果包括：平台租户存在、`tenant_id=0` 剩余为 0、admin 归属租户 1、`merchant_profile` 存在、申请目标列齐全、平台菜单和角色菜单就位。
- 已执行 Redis `FLUSHDB` 清理旧 session/cache。

API Validation：

- 真实 API E2E 已通过，结果 `API_E2E_OK`。
- 已验证公开商家申请、平台登录、`getInfo/getRouters`、平台申请列表/详情/审核通过、商家资料列表/详情、商家邮箱密码登录、商家当前资料查询/更新。
- 已验证负向权限：商家 token 不能访问平台申请列表；平台 token 不能访问商家自助资料接口。
- 邮件关闭时审核通过不阻断且不返回明文密码；DEV SMTP 开启后真实测试邮件发送成功。

Browser Validation：

- 当时 Codex Browser 插件未暴露可调用 browser tool，按 fallback 使用本地 Playwright 做真实浏览器验证。
- 已验证页面：`/merchant/apply`、`/login`、`/system/tenant/applications`、`/system/merchant/profile`、`/merchant/profile`。
- 已验证交互：商家申请提交、平台登录、申请审核页渲染、平台商家资料页渲染、商家资料修改保存。
- console relevant errors：0。
- failed requests：仅 1 个登录页导航切换期间的 `GET /dev-api/captchaImage net::ERR_ABORTED`，未阻断业务验证。

Regression Validation：

- 已验证登录页无租户选择。
- 已验证平台/商家登录态自动识别租户。
- 已验证平台跨租户接口需要平台身份和权限，商家自助接口强制 `tenantType=MERCHANT`。
- 已验证新增页面可构建、可渲染，基本浏览器交互可用。

## Remaining Risks

- 当前没有正式生产环境；本地 dealer 数据库已作为目标环境完成同步。未来若出现生产环境，必须重新制定备份、迁移和回滚策略。
- 当前工作区存在大量与本需求无法明确归因的 dirty / untracked 文件；提交或归档前必须分拣。
- `sys_role_menu` 实体当前没有 `tenantId` 字段，依赖租户插件自动补 `tenant_id`；真实数据库联调已通过主链路，但后续扩展角色菜单写入时仍建议复核。
- `sys_tenant`、`sys_tenant_apply` 仍在 ignore tables 中；后续新增平台管理查询时需继续明确哪些表 ignore、哪些通过服务层受控过滤。
- 邮件模板、重发邀请、重置密码能力仍是后续增强项。
- 未覆盖所有浏览器和移动端响应式视口。

## Blockers

- 无新的技术 blocker。
- 流程 blocker：工作区无关改动未分拣，当前不建议直接 `/archive` 或整体提交。

## Next Step

先分拣工作区 dirty / untracked 文件，确认哪些属于本需求、哪些是历史或无关改动。分拣完成后，如果用户认可当前验证结果，再执行 `/archive` 形成 Final Status 和归档摘要。
