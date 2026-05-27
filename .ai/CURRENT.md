# Current

## Status

Local Database Sync Completed - No Upgrade Script Kept

## Requirement

用户要求规划“跨境订单系统”的多租户 + 商家注册流程，并先分析现有租户管理相关代码。当前暂停 `/do`，回到 `/plan`。

业务背景：
- 不是通用 SaaS 登录选租户模型；登录页不允许选择租户。
- 平台/厂家是中国运营方，负责商家、订单、商品、发货、权限等。
- 商家/客户是美国商家，通过商家注册申请进入系统，审核后属于自己的商家租户。
- 登录后系统自动识别租户。
- 现有 `tenant_id = 0` 作为显式平台租户，需要改为真实平台租户记录。

目标：
- 商家注册页只提交申请资料，不提交/保存密码。
- 商家申请进入 `PENDING`。
- 平台审核通过后，后端自动创建租户、商家资料、商家管理员账号、角色和菜单权限。
- 初始密码由平台审核通过时生成，并通过已有 `bocoo-common-mail` 发送；邮件不可用时保留 TODO/风险，不新增复杂邮件系统。
- 同一邮箱暂不允许申请多个商家，沿用 `sys_user.email` 全局唯一。
- 登录支持用户名/邮箱 + 密码，并自动带出 `tenantId / tenantType / merchantId`。
- 平台用户可受控跨租户管理；商家用户只能访问自己 `tenant_id` 下的数据。
- 个人中心只维护个人账号资料；新增独立“商家资料”维护商家主体信息。

Scope：
- 后端：租户上下文、登录上下文、商家申请审核、`merchant_profile`、API、权限隔离、i18n message、PostgreSQL baseline/索引/菜单权限数据。
- 前端：商家注册页字段收敛、平台商家审核、平台商家管理/资料查看、商家端商家资料维护、API 类型、路由映射、i18n、UTC 展示。

Out of Scope：
- 不做登录页租户选择。
- 不新增重量级邮件或模板系统。
- 不修改旧 MySQL SQL。
- 不做订单、商品、发货等业务表的全量租户改造，除非后续明确进入对应业务阶段。

## Execution Gate

- 用户未明确确认进入 `/do` 前，不执行任何业务代码修改。
- 平台真实租户 ID 未确认前，不修改 SQL/baseline/tenant seed，不修改平台租户判断逻辑，不开始依赖该 ID 的后端任务。
- 不允许自行选择“看起来不阻塞”的任务绕过 `Next Step` 或绕过本 Gate。
- 需要数据库结构、baseline、索引、菜单 seed、权限 seed 的任务，必须先获得明确确认。
- 准备运行 build/test/lint/typecheck 前必须暂停并说明命令，等待确认。
- 若开放问题影响第一步任务，必须先问用户，不得以假设推进。

## Blocking Confirmations

已由用户确认：
- 平台真实租户 ID 使用固定值 `1`。
- 允许将 PostgreSQL seed 中的平台、admin、部门、菜单、角色、角色菜单、用户角色等从 `tenant_id = 0` 迁移到 `tenant_id = 1`。
- `tenant_id = 0` 不再作为合法业务租户 ID，也不作为缺失 tenant fallback。
- 平台身份以后通过 `tenantType = PLATFORM` 判断，不再通过 `tenant_id = 0` 判断。
- 允许本阶段修改 `sql/postgresql/base.sql`，只修改 PostgreSQL，不修改 MySQL。
- 允许新增 `merchant_profile` 表、必要索引、平台 seed 调整、菜单权限和 i18n seed。
- `merchant_profile` 字段按本计划建议字段落地。
- 邮件未启用或发送失败时，审核通过允许成功；不阻断审核，不在接口响应中暴露明文密码，记录日志/风险/TODO。

## Current Implementation Analysis

已确认的现有实现：
- `bocoo-common/.../TenantContextHolder.java`：ThreadLocal 保存 `tenantId` 和 `IGNORE`，已有 `runWithTenant()`，但直接 `setIgnore(true)` 风险较高。
- `bocoo-common/.../TenantContextFilter.java`：命中 ignore URL 时直接 `setIgnore(true)`，否则用 `LoginHelper.getTenantId()` 设置上下文。
- `bocoo-common/.../TenantDatabaseInterceptor.java`：MyBatis-Plus `TenantLineHandler` 注入 `tenant_id`，按 ignore table 或 `TenantContextHolder.isIgnore()` 跳过。
- `bocoo-admin/src/main/resources/application.yml`：`bocoo.tenant.platform-id: 0`；ignore table 包含 `sys_tenant`、`sys_tenant_apply` 等。
- `sql/postgresql/base.sql`：平台租户、平台部门、admin、平台菜单均使用 `tenant_id = 0`；`sys_user.email` 已有全局唯一索引。
- `SysTenantApplyBo` 当前字段只有 `merchantName/contactName/email/country/remark/status/rejectReason`，比前端注册页少很多。
- `SysTenantApplyService.approve()` 已能创建 `SysTenant`、临时密码、商家用户和 `merchant_admin` 角色，但没有 `merchant_profile`、没有邮件发送，审核 API 直接返回临时密码，角色菜单权限未完整处理。
- `LoginUser` 当前只有 `tenantId`，没有 `tenantType/merchantId`；`SysLoginService.buildLoginUser()` 只设置 `tenantId`。
- `SysLoginService.login()` 只按 `user_name` 查用户；已有 `loadUserByEmail()`，但用于邮箱验证码登录，不用于密码登录。
- `SysLoginController.getInfo()` 返回 `user/roles/permissions`，未返回 `tenantType/merchantId`。
- `SysProfileController` 只处理个人资料，方向符合个人中心边界。
- 前端 `MerchantApplyPage.vue` 视觉风格可复用，但字段与后端 BO 不对齐，包含密码/确认密码等应由后台生成的字段。
- 前端 `TenantApplicationsPlaceholder.vue` 是占位空状态，需要替换为真实审核页。
- 已有 `bocoo-common-mail`，`MailConfig` 通过 `mail.enabled=true` 装配 `MailAccount`，`MailUtils.send(...)` 可复用。

参考 yudao-boot-mini：
- 可参考其请求级租户上下文、`TenantUtils.execute/executeIgnore` 的受控执行模式、租户安全过滤和 MyBatis 租户拦截模式。
- 不照搬请求头选租户模型，因为本项目要求登录后自动识别租户。

## Design

推荐方案：
- 平台也是 `sys_tenant` 中 `tenant_type = PLATFORM` 的真实租户记录。
- 平台判断从硬编码 `tenant_id = 0` 迁移为 `LoginUser.tenantType == PLATFORM` 或配置驱动平台租户 ID。
- 引入受控租户执行 helper，限制直接 `TenantContextHolder.setIgnore(true)` 的使用场景。
- 平台跨租户接口必须同时具备平台身份和明确权限；商家端默认使用 MyBatis 租户插件隔离。
- 新增 `merchant_profile` 与 `sys_tenant` 一对一，承载商家主体资料。
- 审核通过事务内创建租户、商家资料、商家管理员账号、角色和菜单权限；初始密码只在审核通过瞬间用于邮件。
- 密码登录支持用户名或邮箱；登录上下文补齐 `tenantType/merchantId`；`getInfo` 同步返回。
- 前端分三类页面：商家申请、平台审核/管理、商家资料维护；所有新增可见文案走 vue-i18n，时间展示用 `formatUtc()`。

## Tasks

### A. 必须先确认，确认前禁止执行

1. Confirm platform tenant migration policy
- Owner：user + main agent
- Files：无业务代码修改
- Depends on：无
- Validation：用户明确回答平台真实租户 ID、是否允许改 PostgreSQL baseline。
- Status：completed

2. Database baseline and seed design
- Owner：java-architect
- Files：`sql/postgresql/base.sql`、`bocoo-admin/src/main/resources/application.yml`
- Depends on：Task A1
- Validation：平台、admin、部门、菜单、角色、角色菜单 seed 使用真实平台租户 ID；新增 `merchant_profile`、必要索引、菜单权限/i18n seed。
- Status：completed

3. Tenant identity foundation
- Owner：java-architect
- Files：`TenantConstants.java`、`TenantProperties.java`、`LoginHelper.java`、`LoginUser.java`、`TenantContextHolder.java`、`TenantContextFilter.java`
- Depends on：Task A1
- Validation：平台判断不再依赖 `tenant_id = 0`；租户 ignore 只能通过受控路径。
- Status：completed

### B. 后端核心链路，A 完成后顺序执行

4. Merchant profile backend
- Owner：java-architect
- Files：新增 merchant profile entity/bo/vo/mapper/service/controller，后端 messages
- Depends on：Task A2
- Validation：平台可查全部；商家只能查/改自己；关键字段商家不可改。
- Status：completed

5. Merchant application backend
- Owner：java-architect
- Files：`SysTenantApplyBo/Entity/Vo`、`SysTenantApplyService`、`SysTenantApplyController`、mapper、messages
- Depends on：Task A2, A3, B4
- Validation：提交进入 PENDING；同邮箱 active 申请和 `sys_user.email` 冲突被拒绝；审核通过创建租户/商家/账号/权限。
- Status：completed

6. Login context
- Owner：java-architect
- Files：`SysLoginService.java`、`SysLoginController.java`、tenant/profile mapper
- Depends on：Task A3, B4
- Validation：用户名/邮箱密码登录均可；`LoginUser/getInfo` 返回 `tenantType/merchantId`。
- Status：completed

7. Mail notification
- Owner：java-architect
- Files：`SysTenantApplyService.java` 或轻量通知 helper
- Depends on：Task B5
- Validation：`mail.enabled=true` 时发送初始账号邮件；未启用或失败时不泄露密码到响应，并记录 TODO/失败原因。
- Status：completed

### C. 前端任务，后端 API/字段定稿后执行

8. Frontend application page
- Owner：frontend-developer
- Files：`MerchantApplyPage.vue`、`admin-ui/src/api/merchant/application.ts`、locale
- Depends on：Task B5 的申请字段定稿
- Validation：字段与后端申请 BO 对齐；密码/确认密码/邀请码/role/memberId 等后台生成字段移除；无硬编码可见文案。
- Status：completed

9. Frontend platform merchant audit and management
- Owner：frontend-developer
- Files：替换 `TenantApplicationsPlaceholder.vue`，新增商家管理/详情页、API、路由映射、locale
- Depends on：Task B4, B5
- Validation：申请列表、详情、通过、拒绝、商家资料查看可用；时间用 `formatUtc()`。
- Status：completed

10. Frontend merchant profile page
- Owner：frontend-developer
- Files：新增商家端资料维护页、API、路由映射、locale
- Depends on：Task B4, B6
- Validation：关键字段只读；非关键字段可提交；商家用户自动关联自己的 `merchantId`。
- Status：completed

### D. 可并行但不能绕过 Gate

11. Static review of existing code only
- Owner：main agent
- Files：只读
- Depends on：用户明确要求继续分析，且不进入 `/do`
- Validation：只能读取和总结，不修改业务代码。

12. Check and verification
- Owner：code-reviewer
- Files：本阶段全部改动
- Depends on：对应实现任务完成；运行命令前需再次确认
- Validation：后端 compile、前端 typecheck/build 按用户确认执行；审查 i18n、UTC、权限、minimal diff。

## Acceptance Criteria

- 平台真实租户不再依赖 `tenant_id = 0` 硬编码。
- 商家提交注册申请成功，后端保存 PENDING。
- 平台用户能查看申请详情并审核通过/拒绝。
- 审核通过后创建真实 `sys_tenant`、`merchant_profile`、商家管理员 `sys_user`、商家角色及菜单权限。
- 同一邮箱不能重复申请 active 商家，也不能绕过 `sys_user.email` 全局唯一。
- 初始密码不由注册页提交，不持久化明文；邮件能力启用时发送给客户。
- 登录页不出现租户选择；用户名或邮箱 + 密码可登录。
- 商家登录后 `LoginUser/getInfo` 带 `tenantId/tenantType/merchantId`。
- 商家访问数据默认只看到自己 `tenant_id`；平台跨租户接口必须有平台身份和明确权限。
- 个人中心只维护个人账号资料；商家资料有独立页面。
- 新增/改动可见文案走 vue-i18n；时间展示用 `formatUtc()`。

## Changed Files

本轮 `/do` 已修改：
- `.ai/CURRENT.md`
- `.ai/MEMORY.md`
- `bocoo-admin/src/main/resources/application.yml`
- `bocoo-common/bocoo-common-core/src/main/java/com/bocoo/common/core/config/properties/TenantProperties.java`
- `bocoo-common/bocoo-common-core/src/main/java/com/bocoo/common/core/constant/TenantConstants.java`
- `bocoo-common/bocoo-common-core/src/main/java/com/bocoo/common/core/context/TenantContextHolder.java`
- `bocoo-common/bocoo-common-core/src/main/java/com/bocoo/common/core/domain/bo/LoginUser.java`
- `bocoo-common/bocoo-common-satoken/src/main/java/com/bocoo/common/satoken/utils/LoginHelper.java`
- `bocoo-modules-system/pom.xml`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysLoginController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLoginService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/MerchantProfileController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/bo/MerchantProfileBo.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/entity/MerchantProfile.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/vo/MerchantProfileVo.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/mapper/MerchantProfileMapper.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/MerchantProfileService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysTenantApplyController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/bo/SysTenantApplyBo.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/entity/SysTenantApply.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/vo/SysTenantApplyVo.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysTenantApplyService.java`
- `bocoo-admin/src/main/resources/i18n/messages.properties`
- `bocoo-admin/src/main/resources/i18n/messages_zh_CN.properties`
- `bocoo-admin/src/main/resources/i18n/messages_en_US.properties`
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
- `sql/postgresql/base.sql`

已按用户要求还原此前误改的业务文件：
- `admin-ui/src/pages/auth/MerchantApplyPage.vue`
- `admin-ui/src/api/merchant/application.ts`
- `admin-ui/src/locales/zh_CN.ts`
- `admin-ui/src/locales/en_US.ts`

## Validation

### 2026-05-27 Re-check After Permission/i18n Fixes And Dev DB Sync

Static Validation：
- 已调用 `code-reviewer` 做只读静态复核；新增发现的高风险“公开申请接口可伪造审计字段”已修复：`SysTenantApplyService.submit()` 入库前清理 `applyId/tenantId/audit/reject/base audit` 字段。
- 已修复邮件用户可见文案绕过 i18n：审核通过邮件 subject/content 改为后端 MessageSource key。
- 已修复真实运行中发现的租户上下文问题：`getInfo/getRouters` 在登录用户租户下读取用户/菜单；`TenantContextFilter` 调整到 Sa-Token 认证过滤之后执行，保证普通受保护接口有请求级 tenant context。
- code-reviewer 剩余静态风险已按本地开发库即目标环境的前提处理：schema/seed 已直接同步到本地 dealer 数据库，旧 Redis session/cache 已清理；工作区仍有与本需求无关的既有 dirty/untracked 文件，归档/提交前需人工分拣。

Runtime Validation：
- 已执行 dev 数据库 schema/seed 幂等同步：补齐 `sys_tenant_apply` 目标列，创建 `merchant_profile` 表和索引，平台租户 `tenant_id=1/tenantType=PLATFORM`，admin 归属租户 1，平台商家菜单 seed 5 个就位。
- 已执行 Redis `FLUSHDB` 清理旧缓存。
- 已执行 `mvn -pl bocoo-admin -am -DskipTests package`：成功；测试按命令跳过。Maven 仍有既有 warning（注解处理、deprecated、资源过滤编码提示），无构建失败。
- 已执行 `pnpm build`：成功，包含 `vue-tsc --noEmit && vite build`。
- 已执行 `pnpm typecheck`：成功。
- 已启动后端 `bocoo-admin/target/dist/bocoo-admin.jar`，关闭 captcha/mail 做验证；`GET /actuator/health` 返回 `UP`。
- 已启动前端 Vite dev server `127.0.0.1:8083`；验证结束后已停止 8081/8083 本轮服务。

API Validation：
- 已执行真实 API E2E，结果 `API_E2E_OK`。
- 已验证公开 `POST /merchant/applications`：提交申请成功进入 `PENDING`，且伪造的 `createBy/rejectReason` 未落库。
- 已验证平台用户名密码登录、`GET /getInfo` 返回 `tenantId=1/tenantType=PLATFORM`，`GET /getRouters` 返回平台商家审核路由。
- 已验证平台申请列表、申请详情、审核通过接口。
- 已验证审核通过后创建：商家 `sys_tenant(tenantType=MERCHANT)`、`merchant_profile`、商家管理员 `sys_user`、商家角色、4 条商家角色菜单权限。
- 已验证平台商家资料列表和详情。
- 已验证商家邮箱+密码登录，`getInfo` 返回商家 `tenantId/tenantType=MERCHANT/merchantId`。
- 已验证商家当前资料查询和维护；公司名称、主邮箱等关键字段未被商家自助更新覆盖，办公电话等非关键字段可更新。
- 已验证负向权限：商家 token 不能访问平台申请列表；平台 token 不能访问商家自助资料接口。
- 已验证邮件关闭时审核通过不阻断，后端记录跳过邮件日志，接口不返回明文密码。

Browser Validation：
- Browser 插件本轮没有可调用 browser tool 暴露；按 frontend-testing-debugging fallback 使用本地 Playwright 1.60.0 做真实浏览器验证。
- 已打开 `http://127.0.0.1:8083/merchant/apply`，填写申请表并提交，页面跳转登录页；截图：`.ai/runtime-validation/browser-merchant-apply-filled.png`。
- 已用平台账号登录，打开 `/system/tenant/applications`，验证商家审核页真实渲染、表格展示待审核/已通过申请、时间使用页面格式化展示；截图：`.ai/runtime-validation/browser-platform-audit.png`。
- 已打开 `/system/merchant/profile`，验证平台商家资料页真实渲染；截图：`.ai/runtime-validation/browser-platform-merchant-profile.png`。
- 已用商家账号登录，打开 `/merchant/profile`，修改可编辑字段并保存；截图：`.ai/runtime-validation/browser-merchant-profile-updated.png`。
- Browser console relevant errors：0。
- Browser failed requests：1 个 `GET /dev-api/captchaImage net::ERR_ABORTED`，发生在登录页导航切换期间；未阻断登录、页面或业务 API 验证。

Regression Validation：
- 已验证公开申请页和登录页无租户选择。
- 已验证平台/商家登录态自动识别租户，不依赖登录页选择租户。
- 已验证平台跨租户查询通过平台身份和权限，商家自助接口强制 `tenantType=MERCHANT`。
- 已验证新增前端页面构建产物可生成，新增页面基本浏览器交互可用。
- 已执行 `git diff --check`：仅 `logs/sys-console.log`、`logs/sys-error.log` 运行日志存在 trailing whitespace；本轮不清理无关日志改动。

未验证项：
- 真实 SMTP 发送已在 DEV 配置启用后补做验证；测试邮件发送成功，Message-ID `<226170135.0.1779866791711@CH-202507160856>`。
- 当前没有线上正式生产环境；按用户确认，本地 dealer 数据库作为目标环境，已直接执行 schema/seed 同步，不保留独立升级/覆盖类脚本。
- 未验证所有浏览器/移动端响应式视口；本轮 Playwright 使用桌面 viewport。
- 未验证旧 Redis session 在不清缓存情况下的兼容；本轮按要求清理 Redis 后验证。

Remaining Risks：
- 当前没有线上正式生产环境；本地 dealer 数据库已作为目标环境完成 schema/seed 同步。
- 旧 token/旧 Redis session 不带 `tenantType/merchantId`；本轮已清理本地 Redis cache/session。
- 当前工作区有大量与本需求无关的 dirty/untracked 文件，提交/归档前必须分拣；当前不建议直接 `/archive` 或整体提交。
- 邮件模板和重发邀请/重置密码能力仍是后续风险；真实 SMTP 发送已补做验证。

### 2026-05-27 Runtime/API/Browser Check

Static Validation：
- 已调用 `code-reviewer` 做只读复核；发现 4 个需要处理的验收风险：
  - 高：商家申请列表/详情只有权限串校验，服务层未像审核通过/拒绝一样二次校验 `tenantType = PLATFORM`；在 `sys_tenant_apply` 为 ignore table 的前提下，若商家误配权限会读到全量申请。
  - 中：商家自助资料 `current/updateCurrent` 只依赖权限和 `tenantId`，服务层未强制 `tenantType = MERCHANT`；平台账号若误配 `merchant:profile:*` 会进入商家自助接口。
  - 中：前端仍有少量可见文案未走 i18n；申请页品牌副标题存在硬编码，商家资料描述直接展示原始 `auditStatus`。
  - 中：PostgreSQL baseline 只适合初始化/seed 修正，不能替代已有环境从 `tenant_id = 0` 到 `1` 的完整迁移；该历史风险已在后续按“本地 dealer 库即目标环境”的前提直接同步数据库处理。

Runtime Validation：
- 已执行 `mvn -pl bocoo-admin -am -DskipTests package`：成功，生成可执行产物 `bocoo-admin/target/dist/bocoo-admin.jar`；测试按命令显式跳过。Maven 有编译期 warning 和资源过滤编码提示，无构建失败。
- 已验证 `bocoo-admin/target/bocoo-admin.jar` 不是可执行 Spring Boot jar；真实可执行 jar 位于 `bocoo-admin/target/dist/bocoo-admin.jar`。
- 已启动 `bocoo-admin/target/dist/bocoo-admin.jar`，并用本地覆盖关闭 captcha/mail 以支持验证且避免外部邮件发送；后端实际监听 `127.0.0.1:8081`，`/actuator/health` 返回 `UP`。
- 已确认本机 Redis `127.0.0.1:6379` 可达，后端启动时连接 PostgreSQL 和 Redis 成功。
- 已执行 `mvn -DskipTests compile`：成功。
- 已执行 `pnpm build`：成功，脚本包含 `vue-tsc --noEmit && vite build`。
- 已启动前端 Vite dev server，`127.0.0.1:8083` 可达；验证完成后已停止本轮启动的后端和前端进程。

API Validation：
- 已调用 `GET /actuator/health`：HTTP/API 可达，返回 `UP`。
- 已调用 `GET /captchaImage`：返回成功，且本轮验证配置中 `captchaEnabled=false`。
- 已调用 `POST /merchant/applications` 提交唯一邮箱商家申请：HTTP 请求到达后端，但响应业务 `code=500`。后端日志显示当前真实 dev 数据库的 `sys_tenant_apply` 缺少新字段 `company_name`，schema 未同步本阶段 PostgreSQL baseline。
- 已调用 `POST /login` 使用平台账号登录：响应业务 `code=500`。后端日志显示当前 dev Redis 缓存中存在旧格式时间字符串，UTC 反序列化失败；SQL 日志还显示当前真实 dev 数据库中的 admin 仍查询到 `sys_tenant.tenant_id = 0`，与本阶段平台租户 ID `1` 决策未同步。
- 因平台登录失败，未能继续真实调用申请列表、详情、审核通过、商家资料列表、商家登录、`getInfo` 中 `tenantType/merchantId` 等受保护接口。

Browser Validation：
- 已使用内置 Browser 打开 `http://127.0.0.1:8083/merchant/apply`。
- 已验证商家申请页真实渲染，保留现有视觉风格，字段包含名、姓、商家名称、公司名称、办公电话、手机、邮箱、备注、国家、州/省、城市、地址、邮编；页面未出现租户选择。
- 已在浏览器中逐项填写申请表并点击“提交申请”；表单交互成功，提交后显示系统异常，和 API Validation 中 `sys_tenant_apply.company_name` 缺失的后端 500 一致。
- 已打开 `http://127.0.0.1:8083/login`，验证登录页渲染成功、存在商家申请入口、未出现租户选择。
- 因后端登录链路 500，未能在浏览器中验证平台商家审核页、平台商家管理页、商家资料维护页的登录后真实路由、权限菜单、列表/弹窗/提交交互。
- Browser console 未捕获到前端 JS error；业务失败来自后端 API 500。

Regression Validation：
- 已验证后端可编译、后端可打包、后端真实启动、前端可构建、前端 dev server 可启动。
- 已验证公开商家申请页和登录页不出现租户选择，符合“不允许登录页选择租户”。
- 未验证真实审核通过后的租户、商家资料、用户、角色、角色菜单写入结果；当前 dev 数据库 schema/seed 未同步导致链路中断。
- 未验证邮件真实发送；本轮运行时明确关闭邮件，避免向外部发送。
- 历史记录：当时未验证已有数据迁移；后续已按“本地 dealer 库即目标环境”的前提直接同步数据库，并确认 `tenant_id=0` 已清零。

已执行：
- 读取默认上下文：`AGENTS.md`、`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- 使用 CodeGraph 和真实源文件分析租户、登录、商家申请相关入口和影响范围。
- 查询 yudao-boot-mini 租户上下文、TenantUtils、租户安全过滤和租户拦截参考实现。
- 根据用户要求还原本轮误改的 4 个前端业务文件。
- PostgreSQL baseline 已将 `bocoo.tenant.platform-id` 调整为 `1`。
- `sql/postgresql/base.sql` 已新增 `merchant_profile` 表、索引和字段注释。
- `sql/postgresql/base.sql` 已将 tenant-scoped 表的 `tenant_id` 移除 `DEFAULT 0`，并在 baseline 中加入 `tenant_id <> 0` check。
- 平台 seed 已改为 `tenant_id = 1`，并补充平台 seed 重跑时的受限更新语句。
- 已执行只读静态检查：确认无 `tenant_id bigint NOT NULL DEFAULT 0`、无平台 seed `SELECT 1, menu_id, 0`、无 `platform-id: 0` 残留；仅保留说明性注释和迁移兼容 `WHERE tenant_id = 0`。
- 已执行 `git diff --check -- sql/postgresql/base.sql bocoo-admin/src/main/resources/application.yml`，无 whitespace error；Git 提示工作区文件下次触碰时可能 LF 转 CRLF。
- Task A3 已完成静态检查：`TenantConstants` / `TenantProperties` 默认平台租户 ID 改为 `1`；`LoginHelper.getTenantId()` 不再在缺失登录态或异常时 fallback 到平台租户；`LoginHelper.isPlatformTenant()` 改为按 `LoginUser.tenantType == PLATFORM` 判断；`LoginUser` 已补充 `tenantType/merchantId`；`TenantContextHolder` 已补充受控 `runWithIgnore()` helper。
- Task B4 已完成静态检查：新增 `merchant_profile` entity/bo/vo/mapper/service/controller；平台列表/详情接口需要平台身份和明确权限并通过 `callWithIgnore()` 受控跨租户查询；商家当前资料查询/更新不 bypass，按当前 `tenant_id` 定位；商家自助更新只写入联系人、电话、地址、备注等非关键字段；后端 message 已补充中英文和默认文件。
- Task B5 已完成静态检查：`SysTenantApplyBo/Entity/Vo` 已补齐精简注册字段并要求 country；提交申请会校验 active 申请和全局 `sys_user.email`；审核通过改为 `R<Void>`，不再返回明文临时密码；审核通过会创建 `sys_tenant`、`merchant_profile`、商家管理员账号、`merchant_admin` 角色和商家资料菜单/按钮权限；PostgreSQL baseline 已同步申请字段、平台商家资料菜单权限和菜单 i18n seed。
- Task B6 已完成静态检查：密码登录支持用户名或邮箱；`buildLoginUser()` 会根据 `sys_tenant.tenant_type` 填充 `tenantType`，商家账号会按 `tenant_id` 查出 `merchantId`；登录期间权限/角色/部门加载包在 `TenantContextHolder.runWithTenant()` 内，避免 `/login` ignore URL 导致跨租户权限污染；`getInfo` 已返回 `tenantId/tenantType/merchantId`。
- Task B7 已完成静态检查：审核通过后若 `mail.enabled=true` 则调用现有 `MailUtils.sendText()` 发送初始账号邮件；若邮件未启用或发送失败，仅记录 warn 日志且不阻断审核；接口响应仍为 `R<Void>`，不会暴露明文初始密码。
- Task C8-C10 已完成静态检查：商家申请页已移除密码、确认密码、邀请码、role、memberId、验证码等后台生成/未实现字段；申请字段与 `SysTenantApplyBo` 对齐；平台商家审核页替换占位页并支持列表、详情、通过、拒绝；新增平台商家资料查看页和商家端资料维护页；所有新增页面可见文案走 `vue-i18n`，时间展示使用 `formatUtc()`；动态路由组件映射已补齐。
- Task D12 已完成：首次运行 `mvn -DskipTests compile` 发现 `bocoo-modules-system` 缺少现有内部模块 `bocoo-common-mail` 依赖；已补充 `bocoo-modules-system/pom.xml` 后重新运行通过。
- 已执行 `mvn -DskipTests compile`，后端 reactor 全部 `BUILD SUCCESS`。
- 已执行 `pnpm typecheck`，前端 `vue-tsc --noEmit` 通过。
- 已执行 `pnpm build`，前端 `vue-tsc --noEmit && vite build` 通过。
- 已再次执行 `mvn -DskipTests compile`，确认检查阶段调整 `MerchantProfileController` 路由顺序后后端仍编译通过。
- 已执行 `git diff --check` 覆盖本阶段改动文件，无 whitespace error；Git 仅提示部分工作区文件未来可能 LF 转 CRLF。
- 已执行新增前端页面中文硬编码检查，未发现新增可见中文绕过 `vue-i18n`。
- 已执行租户风险关键字检查：未发现 `platform-id: 0`、`tenant_id bigint NOT NULL DEFAULT 0`、平台菜单 seed `SELECT 1, menu_id, 0` 残留；`tenant_id = 0` 仅保留说明注释和受限迁移兼容语句，非 tenant 字段的 `DEFAULT 0` 保持原样。

未验证项：
- 当前真实 dev 数据库未应用本阶段 PostgreSQL baseline 变更，无法完成“申请 -> 审核通过 -> 创建租户/商家资料/账号/权限 -> 商家登录 -> 商家资料维护”的完整端到端闭环。
- 当前真实 dev Redis 存在旧格式时间缓存，导致平台登录链路失败；未验证清理缓存后的登录和 `getInfo` 上下文字段。
- 未验证受保护的商家审核、商家管理、商家资料维护页面。
- 未验证邮件真实发送能力；本轮明确关闭邮件。
- 历史记录：当时未验证已有数据从 `tenant_id = 0` 迁移到 `tenant_id = 1`；后续已直接同步本地 dealer 目标库，不保留独立升级/覆盖类脚本。

### Remaining Risks

- dev 数据库 schema/seed 已同步并完成端到端验收；按用户确认，当前本地 dev 数据库即目标环境，不保留独立升级/覆盖类脚本。
- dev Redis 已清理并重新完成登录链路验收。
- 商家申请列表/详情、商家自助资料接口的身份边界风险已通过服务层 `tenantType` 校验和真实 API negative case 验证处理。
- 前端可见文案/i18n 和状态展示风险已修复并通过 build/typecheck/浏览器主链路验证。
- 真实 SMTP 发送已补做验证；邮件模板和重发邀请/重置密码能力仍是后续增强项。

### 2026-05-27 Local Database Direct Sync

已处理：
- 按用户确认，当前没有线上正式生产环境，本地 dealer 数据库作为目标环境处理。
- 已删除本轮新增的 `sql/postgresql/upgrade_20260527_tenant_merchant.sql`，不保留独立升级/覆盖类脚本。
- 已通过临时 Java/JDBC 执行器直接同步目标数据库 schema/seed：平台租户固定 `tenant_id = 1`、移除 tenant-scoped 表 `tenant_id` 默认值、迁移旧 `tenant_id = 0` 平台/系统 seed、补齐 `sys_tenant_apply` 新字段、创建 `merchant_profile`、补齐索引、平台商家审核/商家资料菜单权限和 i18n seed。
- 首次同步后发现 `sys_tenant` 仍残留 1 条 `tenant_id=0` 且无任何已知 tenant-scoped 表引用，已删除该旧租户记录；二次校验 `tenantZero=0`。
- 已清理本地 Redis `FLUSHDB`，避免旧 `LoginUser` session 缺失 `tenantType/merchantId` 影响登录验证。
- 已清理本轮真实运行验证产生的 tracked 日志脏改：`logs/sys-console.log`、`logs/sys-error.log`、`logs/sys-info.log`。

未伪造验证：
- 用户提供的 `pg-mcp` Docker 入口当前不可用：没有运行中的 `pg-mcp` 容器，Docker Desktop Linux engine 后续也不可连接；因此改用本地 Maven 缓存中的 PostgreSQL JDBC 驱动直连执行。
- 真实邮件发送仍未验证；当前 dev runtime 明确关闭 `mail.enabled=false`，且没有可用 SMTP 配置。已验证的范围仍是“邮件未启用或发送失败不阻断审核，接口不暴露明文密码”。
- 工作区仍存在大量与本需求无法明确归因的 dirty/untracked 文件，本轮未擅自还原或删除。

Database Verification：
- `DB_SYNC_OK platform=1 tenantZero=1 applyTargetColumns=10 merchantProfileTable=1 platformMenus=5 i18nSeed=6`。
- `TENANT_ZERO_AUDIT refs=0 details=none deletedTenantRows=1 remainingTenantZero=0`。
- `REDIS_FLUSHDB_OK`。
- `DB_VERIFY_OK platform=1 tenantZero=0 adminTenant=1 merchantProfileTable=1 applyTargetColumns=10 platformMenus=5 platformRoleMenus=5`。

Remaining Risks：
- 真实 SMTP 邮件发送已在 DEV 配置启用后补做验证。
- `/archive` 仍不建议立即执行，除非用户确认工作区无关改动的处理方式。

### 2026-05-27 DEV SMTP Validation

已处理：
- DEV `mail.enabled=true` 配置已确认。
- 使用临时 Java/Jakarta Mail 执行器读取 DEV 邮件配置并发送真实测试邮件；未向仓库写入临时源码或输出 SMTP 密码/授权码。
- 因用户未指定收件人，本轮使用 DEV 配置中的发件地址作为测试收件人。

Validation：
- `SMTP_SEND_OK messageId=<226170135.0.1779866791711@CH-202507160856>`。

Remaining Risks：
- 真实 SMTP 连通性已验证。
- 邮件模板、重发邀请、重置密码能力仍是后续增强项，不属于本阶段复杂邮件系统范围。

## Risks

- 平台租户 ID 从 0 迁移到真实记录会影响已有数据库；本轮已按用户确认直接同步本地 dealer 目标库，并删除无引用的旧 `tenant_id=0` 租户记录。
- 若未来出现正式生产环境，需要重新制定对应环境的备份和迁移策略；当前阶段不保留独立升级/覆盖类脚本。
- `sys_role_menu` 实体当前没有 `tenantId` 字段，依赖租户插件自动补 `tenant_id`；后端编译已通过，但仍建议在真实数据库联调时验证新增角色菜单插入后的 tenant_id。
- `sys_tenant`、`sys_tenant_apply` 当前在 ignore tables 中，平台管理查询需要明确哪些表继续 ignore，哪些通过服务层受控过滤。
- 邮件不可用时初始密码传递方式存在业务风险；当前已验证 DEV SMTP 可发送，平台后续仍可补重发邀请或重置密码能力。
- 当前已完成编译、类型检查和构建；剩余主要风险是未做真实 PostgreSQL 环境的完整注册、审核、登录、资料维护端到端验收。
- 真实 Runtime/API/Browser 验证已执行，但被当前 dev 数据库 schema/seed、旧 Redis 缓存和静态权限/i18n 风险阻断；当前不满足归档条件。

## Pause Points

- 无新的 Mandatory Pause Condition。

## Next Step

本轮已按用户确认把本地 dealer 数据库作为目标环境，直接完成 schema/seed 同步，删除无引用的旧 `tenant_id=0` 租户记录，并清理 Redis cache/session；不保留独立升级/覆盖类脚本。DEV SMTP 真实发送已补做验证并成功。当前仍不建议 `/archive` 的唯一主要原因是工作区包含大量与本需求无法明确归因的 dirty/untracked 文件，下一步建议先分拣工作区无关改动。
