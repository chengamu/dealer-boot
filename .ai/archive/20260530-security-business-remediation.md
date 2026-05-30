# Archive: Security and Business Remediation

## Feature：功能

基于 `CODE_REVIEW.md`、`CODE_REVIEW_BUSINESS.md` 和 `.ai.workflow-backup` 的安全漏洞、业务安全和后续专项修复。

## Requirement Source：需求来源

- `D:\work\base-boot\CODE_REVIEW.md`
- `D:\work\base-boot\CODE_REVIEW_BUSINESS.md`
- `D:\work\base-boot\.ai.workflow-backup`
- 用户补充决策：当前无正式域名；`allList` 等部分全量过滤权限为业务需要；上传只允许常见图片/PDF/Excel/Word；DEV PostgreSQL 为当前唯一数据库；允许依赖升级、PostgreSQL 结构调整和新增审计类型。

## Final Status：最终状态

Accepted with Risks

## Scope：范围

- 修复可本地完成的真实安全问题。
- 保留已确认的业务权限边界，不盲目移除平台租户全量查询。
- 对 PostgreSQL DEV 库直接应用已确认结构和字典变更。
- 对依赖、上传、CORS、JWT、限流、防重、日志脱敏、验证码、审计和首次登录强制改密做分批修复。

## Out of Scope：不做范围

- 不做 Cookie + CSRF 认证架构改造，因正式域名和部署拓扑未确定。
- 不做生产 CORS 域名收紧，等待正式域名。
- 不做商户申请进度自助查询，按用户决策暂缓。
- 不强制升级 Quill 2.x，避免未评估的富文本组件兼容风险。

## Completed：已完成

- 凭据和隐私日志修复：移除 resetPwd 密码/hash 日志；移除 `SysUserVo.password` 序列化风险；Token 日志移除或脱敏；登录失败日志不输出原始账号/邮箱/手机号/openid。
- 认证和输入校验修复：`thirdLogin` 改 DTO 校验；`updateByKey` 补 `@Validated`；用户名/邮箱登录查询合并；密码错误锁定 key 不再按 IP 绕过。
- 上传和请求边界加固：上传白名单限制为常见图片/PDF/Word/Excel，并增加 MIME 与 magic bytes 校验；JWT secret 启动期校验；CORS 改为 YAML 配置；Undertow POST body 限制为 20MB。
- 限流、防重和并发一致性：登录、注册、验证码、商户申请、OSS 上传、用户导入等敏感入口补限流/防重；`RepeatSubmitAspect` token 缺失时回退客户端 IP；OSS 状态切换、租户审批/拒绝增加并发保护；部门更新补事务。
- MyBatis 加解密 Map 参数修复：Map 分支遍历 entry 并写回处理后的 value。
- 前端安全稳定性：上传组件不再在组件内保存 Bearer Token headers；API 分页类型使用 `requestPage<T>()`；用户部门展示使用后端 `deptName`；手机号正则修正；i18n runtime sync 完成。
- XSS 和公开接口边界：`XssFilter` 不再按 GET/DELETE 跳过；请求 wrapper 清理单值 query 参数；公开字典维持白名单边界。
- 业务安全 Phase 2：商户申请邮箱验证码改 6 位；增加每日发送上限、错误次数限制和作废逻辑；公开申请响应收敛枚举型错误；SMS/Email 登录验证码 Redis key 拆分。
- 后端资源保护：OSS 批量查询替代 N+1；在线用户扫描加上限；在线用户缓存增加最大 TTL；日志清理分批执行。
- DB/架构批次：新增 `sys_user.force_password_change`；商户临时密码和管理员重置密码后强制改密；用户自改密码后清除标记；登录/getInfo 暴露标记；前端路由守卫强制进入个人资料页；Sa-Token `max-login-count: 5`。
- PostgreSQL DEV 已应用：`sys_user.force_password_change` 列存在，默认值为 `'0'::bpchar`；`sys_oper_type` 审计字典 10/11 已写入；`uk_sys_menu_tenant_parent_name` 唯一索引已创建。
- 审计增强：新增 `CROSS_TENANT_QUERY` 和 `SENSITIVE_OPERATION`；跨租户查询和敏感管理操作补审计类型。
- 依赖升级：后端安全敏感依赖已升级或覆盖，包括 Spring Boot 3.2.12、Hutool 5.8.46、POI 5.5.1、OkHttp 4.12.0、commons-io 2.22.0、Bouncy Castle 1.84、Redisson 3.52.0、commons-compress 1.28.0、ion-java 1.5.1、Undertow Core 2.3.24.Final。
- 前端依赖审计：`pnpm audit` 从 8 个漏洞降到 1 个，`vite-plugin-svg-icons` 旧传递依赖链通过 pnpm workspace overrides 处理。

## Not Completed：未完成

- Token/Cookie/CSRF 架构改造：Deferred，等待正式域名和部署拓扑。
- 生产 CORS origin 收紧：Deferred，等待正式域名。
- 商户申请进度自助查询：Deferred，按用户决策暂缓。
- Quill advisory：Remaining risk，`@vueup/vue-quill` 仍依赖 `quill@1.3.7`；公告要求 `>=1.3.8`，但 npm 未发布 `quill@1.3.8`，Quill 2.x 需要单独兼容评估。
- 后端 OWASP Dependency-Check：Blocked，外部 CISA/NVD 漏洞数据源不可用且本地无缓存库。

## Validation Summary：验证摘要

- `pnpm i18n:validate`：Passed。
- `pnpm i18n:sync`：Passed。
- `mvn -DskipTests compile`：Passed。
- `mvn -pl bocoo-admin -am -DskipTests package`：Passed。
- admin-ui `pnpm typecheck`：Passed。
- admin-ui `pnpm build`：Passed。
- DEV backend smoke：Passed。应用通过 Maven exec 在 8081 启动，Redis 与 DEV PostgreSQL 初始化成功，`/captchaImage` 业务成功，未登录 `/getInfo` 返回业务 401。
- Authenticated smoke：Passed。使用本地 Redis 里已有 admin Token 验证 `/getInfo`、`/getRouters`、`/system/dict/data/type/sys_oper_type`、`/monitor/operlog/list?pageNum=1&pageSize=1`，均 HTTP 200 且业务码 200。未输出 Token。
- `sys_menu.perms` duplicate assessment：Passed。DEV PG 有 8 组重复；运行时权限加载使用 `distinct + Set`，Sa-Token 按权限字符串判断，当前无运行时重复授权影响，因此未修改数据。
- Backend OWASP Dependency-Check：Not Run to completion。第一次失败于 CISA KEV HTTPS handshake；第二次关闭 KEV 后失败于 NVD API 多次重试，且无本地漏洞库；未生成报告。
- admin-ui `pnpm audit --registry=https://registry.npmjs.org/`：Completed with 1 remaining moderate advisory in `quill@1.3.7`。

## Remaining Risks：剩余风险

- 后端依赖扫描结果缺失，需要配置 NVD API Key、内部漏洞库镜像或在 NVD 可访问时重跑。
- 富文本编辑器链路存在 `quill@1.3.7` advisory，需要单独评估替换编辑器、升级到 Quill 2.x 或迁移到仍维护的 Vue wrapper。
- Bearer Token 继续使用，Cookie/CSRF 需等正式域名和部署拓扑明确后做架构专项。
- CORS 生产域名需上线前配置。

## Lessons from Learn：经验提炼

- pnpm v11 不再读取 `package.json#pnpm.overrides`；本项目应把 overrides 放在 `admin-ui/pnpm-workspace.yaml`。
- 旧前端构建插件的传递依赖漏洞可优先用 pnpm overrides 降风险，但运行时库漏洞不能盲目 override 大版本。
- 后端依赖扫描依赖 NVD/CISA 外部数据源；无本地缓存时失败不能视为通过，应记录为阻塞。
- 运行时 smoke 若登录受验证码影响，可以读取本地 Redis 既有 admin Token 做 authenticated smoke，但不得输出 Token。

## Key Decisions：关键决策

- PostgreSQL 是唯一当前数据库目标；MySQL 不再补新增 SQL。
- `allList` / 平台用户跨租户查询是业务需求，只加受控边界和审计，不直接移除。
- `sys_menu.perms` 重复当前无运行时影响，不做数据重写或唯一约束。
- Cookie/CSRF、生产 CORS 域名、商户申请进度查询均按用户决策暂缓。

## Files Modified：修改文件

- Workflow: `.ai/CURRENT.md`, `.ai/TASKS.md`, `.ai/MEMORY.md`, `.ai/HANDOFF.md`, `.ai/archive/20260530-security-business-remediation.md`
- Backend config / dependencies / SQL: `pom.xml`, `bocoo-admin/src/main/resources/application.yml`, `sql/postgresql/base.sql`
- Backend common modules: `bocoo-common/**`
- Backend system module: `bocoo-modules-system/**`
- Frontend admin-ui: `admin-ui/src/**`, `admin-ui/public/i18n/*.json`, `admin-ui/package.json`, `admin-ui/pnpm-lock.yaml`, `admin-ui/pnpm-workspace.yaml`
- Shared i18n source: `i18n/locales/en_US.json`, `i18n/locales/zh_CN.json`

## Artifacts：产物

- DEV PostgreSQL 已直接应用确认过的列、字典和索引变更。
- Backend smoke logs retained under `.ai/tmp/`.
- No OWASP dependency-check report generated.

## Follow-up：后续事项

- 重跑 OWASP Dependency-Check：需要 NVD API Key、内部镜像或外网数据源恢复。
- 单独评估 `@vueup/vue-quill` / `quill` 迁移。
- 有正式域名后执行 CORS 生产配置收紧和 Cookie/CSRF 架构设计。
- 若业务需要，再启动商户申请进度自助查询专项。
