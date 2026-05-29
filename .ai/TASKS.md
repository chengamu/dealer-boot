# Tasks

## Active Queue

当前处于 `/plan`：已根据 `D:\work\base-boot\CODE_REVIEW.md`、`D:\work\base-boot\CODE_REVIEW_BUSINESS.md` 和 `.ai.workflow-backup` 形成安全漏洞与业务安全改进计划，等待用户确认 `/do`。

## Phase 1：局部可修的真实漏洞

### T001 - 凭据与隐私日志泄露修复

Owner：java-architect
Status：pending
DependsOn：无
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：删除密码/hash 日志；移除 `SysUserVo.password` 的序列化风险；Token 日志脱敏或移除；登录失败 PII 日志降级/脱敏。
Files：`SysUserController.java`、`SysUserVo.java`、`UserActionListener.java`、`BocooWebSocket.java`、`SysLoginService.java`
Validation：Maven 编译；关键日志 grep 检查。
Blockers：无
Short Notes：优先处理真实凭据泄露。

### T002 - 认证、权限和输入校验修复

Owner：java-architect
Status：pending
DependsOn：T001
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：保留业务确认的 `allList` / 平台查询受控 bypass，不盲目移除；复核是否满足登录态 + 平台身份 + 调用场景约束；`thirdLogin` 改 DTO 校验；`updateByKey` 补 `@Validated`；用户名/邮箱登录查询合并；密码错误锁定 key 不再按 IP 绕过。
Files：`SysUserController.java`、`SysLoginController.java`、`SysLoginService.java`、`SysConfigController.java`、新增 `ThirdLoginBody` 类（如需要）。
Validation：Maven 编译；Controller/Service 静态检查。
Blockers：无
Short Notes：保持接口返回结构不变；平台租户全量可见是业务需求。

### T003 - 上传、JWT、CORS、POST body 配置加固

Owner：java-architect
Status：pending
DependsOn：T002
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：服务端上传基础校验，允许常见图片、PDF、Excel、Word；JWT secret 启动校验；CORS 改为配置化安全来源，当前无正式域名时允许开发 localhost/127.0.0.1；限制 Undertow POST body。
Files：`SysOssService.java`、`ResourcesConfig.java`、`application.yml`、可能新增配置属性类。
Validation：Maven 编译；上传路径静态检查。
Blockers：正式生产域名未知，生产 CORS 后续需配置。
Short Notes：不新增重量级依赖，优先 magic bytes 最小实现。

### T004 - 防重、限流和关键写操作一致性

Owner：java-architect
Status：pending
DependsOn：T002
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：关键登录端点添加最小 `@RateLimiter`；`RepeatSubmitAspect` 在 token 为空时使用 IP/请求身份 fallback；敏感公开端点和关键写接口添加 `@RateLimiter` / `@RepeatSubmit`；OSS 状态切换避免并发中间态；`SysDeptService.updateDept` 补事务；租户审批邮件移到事务提交后，并结合现有 Lock4j/状态条件更新增加审批并发保护。
Files：`SysLoginController.java`、系统 Controller、`RepeatSubmitAspect.java`、`SysOssConfigService.java`、`SysDeptService.java`、`SysTenantApplyService.java`
Validation：Maven 编译；重点方法静态检查。
Blockers：限流阈值需确认，未确认时使用保守默认。
Short Notes：不引入数据库锁表或 schema 变更；`TenantContextFilter` 已有 finally clear，只做回归确认不误改。

### T005 - MyBatis 加解密 Map 参数修复

Owner：java-architect
Status：pending
DependsOn：T001
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：Map 参数加密/解密遍历 entry 并写回；保持 List/Object 现有逻辑不变。
Files：`MybatisEncryptInterceptor.java`、`MybatisDecryptInterceptor.java`
Validation：Maven 编译；如有现成测试则补充/运行。
Blockers：无
Short Notes：局部修复，不改变加密算法。

### T006 - 前端局部安全稳定性修复

Owner：frontend-developer
Status：pending
DependsOn：T003
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：i18n 加载失败 fallback；上传组件避免在 Upload `headers` 中保存 Bearer Token，改走统一请求层或局部 `http-request`。
Files：`admin-ui/src/i18n/index.ts`、`admin-ui/src/main.ts`、`FileUpload/index.vue`、`ImageUpload/index.vue`、`Editor/index.vue`
Validation：`pnpm typecheck`；必要时 `pnpm build`。
Blockers：不做 HttpOnly Cookie 架构改造。
Short Notes：只做局部安全修复。

### T007 - 最终验证与报告回写

Owner：code-reviewer
Status：pending
DependsOn：T001,T002,T003,T004,T005,T006
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：检查修复点覆盖、未修复项分类、验证命令结果，更新 `.ai/CURRENT.md`。
Files：`.ai/CURRENT.md`、必要时补充修复摘要。
Validation：后端 Maven 编译；前端 typecheck/build；敏感日志 grep。
Blockers：准备运行 build/test 前需暂停确认。
Short Notes：不声称未执行过的验证通过。

## Phase 2：中风险安全、稳定性和性能

### T101 - 限流与防重覆盖补齐

Owner：java-architect
Status：pending
DependsOn：Phase 1
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：在第一批最小覆盖基础上，为注册、短信/邮箱登录、商家申请邮箱验证码、商家申请提交、上传等敏感端点补 `@RateLimiter` / `@RepeatSubmit`；`RateLimiterAspect` 高频日志降到 DEBUG；`RepeatSubmitAspect` 清理逻辑评估改为 finally 语义。
Files：登录/注册/租户申请/OSS Controller，`RateLimiterAspect.java`、`RepeatSubmitAspect.java`
Validation：Maven 编译；接口注解静态检查。
Blockers：具体阈值可先用保守默认，后续按压测/业务调整。
Short Notes：不做全系统机械加注解，优先敏感端点。

### T102 - 后端中风险性能与资源保护

Owner：java-architect
Status：pending
DependsOn：Phase 1
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：OSS 批量查询替代 N+1；在线用户列表分页/阈值保护；角色清理在线用户扫描加数量保护；日志清理分批；在线用户缓存即使 token 永不过期也给最大 TTL。
Files：`SysOssService.java`、`SysUserOnlineController.java`、`SysRoleService.java`、日志 service、`UserActionListener.java`
Validation：Maven 编译；重点路径静态检查。
Blockers：分页接口是否影响前端展示需确认。
Short Notes：不改 Redis/DB 结构。

### T103 - 前端中风险局部修复

Owner：frontend-developer
Status：pending
DependsOn：Phase 1
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：UserPage 等关键 CRUD 表单提交 loading 防重复；API 层移除高风险 `as unknown as` 分页断言；修正 `SysUser.deptName` 类型；手机号正则；Dropdown/copyText 等低风险稳定性项。
Files：`admin-ui/src/pages/system/*`、`admin-ui/src/api/system/*`、相关 utils/directives
Validation：`pnpm typecheck`；必要时 `pnpm build`。
Blockers：CRUD 页面数量较多，按核心页面优先。
Short Notes：不做 CRUD 抽象，只做局部修复。

### T104 - XSS 与公开接口边界复核

Owner：java-architect
Status：pending
DependsOn：Phase 1
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：扩展现有 `@Xss` 覆盖到公告、字典、配置等自由文本字段；评估并修复 `XssFilter` 对 GET query 直接跳过的问题；公开字典继续白名单；公开法务接口确认只读且只返回发布内容。
Files：BO/entity、`XssFilter.java`、`XssHttpServletRequestWrapper.java`、`XssValidator.java`、公开 Controller/Service
Validation：Maven 编译；公开接口静态检查。
Blockers：是否引入 jsoup/OWASP sanitizer 属于依赖新增，默认不做。
Short Notes：第二批先扩大覆盖和边界检查。

### T105 - 商户入驻业务安全加固

Owner：java-architect
Status：pending
DependsOn：Phase 1
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：商户申请邮箱验证码增加每日发送上限；验证码从 4 位改为 6 位并同步邮件文案；验证码校验增加错误次数限制和作废逻辑；公开申请接口对外收敛邮箱已申请/已注册的枚举型错误；审批/拒绝接口补防重复提交。
Files：`SysTenantApplyController.java`、`SysTenantApplyService.java`、`i18n/locales/en_US.json`、`i18n/locales/zh_CN.json`、`bocoo-admin/src/main/resources/i18n/*.json`
Validation：Maven 编译；i18n sync/validate；商户申请验证码和提交路径静态检查。
Blockers：涉及 i18n 文案和公开接口错误语义，执行前需按项目 Pause Rules 明确影响范围。
Short Notes：不新增数据库字段；内部日志可记录具体拒绝原因，但对外响应保持通用。

### T106 - 登录验证码命名空间与业务误报复核

Owner：java-architect
Status：pending
DependsOn：Phase 1
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：SMS 与 Email 登录验证码拆分 Redis key 前缀，避免共用 `CAPTCHA_CODE_KEY`；保留图片验证码“读取即删除”的安全实践；复核 `TenantContextFilter` finally clear 和 `SysProfileController.updatePwd` 旧密码校验，不安排无效修复。
Files：`CacheConstants.java`、`SysLoginService.java`、`TenantContextFilter.java`、`SysProfileController.java`
Validation：Maven 编译；验证码 key grep 检查。
Blockers：拆分验证码 key 会让切换期间尚未消费的旧 SMS/Email 验证码失效，需作为发布说明。
Short Notes：只改命名空间，不改变验证码业务有效期。

## Phase 3：专项大改和技术债

### T201 - 依赖安全升级专项

Owner：java-architect
Status：pending
DependsOn：Phase 2
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：评估 Spring Boot、Hutool、OkHttp、Redisson、commons-compress、ion-java、undertow-core 等安全升级；决定是否启用 dependency-check。
Files：`pom.xml`、各模块 POM
Validation：完整 Maven test/package；必要时运行后端 smoke。
Blockers：依赖升级必须单独确认。
Short Notes：涉及依赖升级，不与第一批混做。

### T202 - Token / CSRF 架构专项

Owner：java-architect
Status：pending
DependsOn：Phase 2
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：评估 HttpOnly Cookie、SameSite、CSRF Token、BFF 或内存 Token 方案；设计前后端登录、刷新、登出和上传联动。
Files：前后端认证、请求层、Sa-Token 配置
Validation：专项方案后再定。
Blockers：属于架构调整，需要单独 `/plan`。
Short Notes：当前第一批只降低 Token 暴露面，不改认证架构。

### T203 - 数据库结构和查询模型专项

Owner：java-architect
Status：pending
DependsOn：Phase 2
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：菜单唯一约束、乐观锁、导出分页/游标、部门 ancestors 查询模型、必要索引等。
Files：SQL/migration、Mapper/Service
Validation：数据库迁移演练、回滚方案、Maven 编译。
Blockers：项目规则禁止未经确认修改 DB 结构和 migration。
Short Notes：必须另开计划。

### T204 - 前端结构和性能技术债

Owner：frontend-developer
Status：pending
DependsOn：Phase 2
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：DealerPortal 静态页拆组件；CRUD composable/组件抽象；FileUpload/ImageUpload composable；Element Plus 按需导入；路由懒加载；静态 mock 数据优化。
Files：`admin-ui/src/pages/**`、`admin-ui/src/components/**`、`vite.config.ts`、路由
Validation：`pnpm typecheck`、`pnpm build`、关键页面浏览器验证。
Blockers：变更面大，需分子任务。
Short Notes：不是第一批漏洞修复。

### T205 - 业务流程安全专项

Owner：java-architect
Status：pending
DependsOn：Phase 2
Requirement Source：`.ai/requirements/20260529-security-vulnerability-remediation.md`
Scope：首次登录强制修改审批临时密码；单用户最大并发 Token 数量或旧 Token 淘汰策略；超级管理员跨租户查询审计；商户申请进度自助查询；敏感操作二次验证和异地登录提醒的产品方案评估。
Files：用户表/实体/登录服务/前端登录守卫/审计日志/商户申请接口，具体文件待专项 `/plan` 确认。
Validation：专项方案后再定，需覆盖数据库迁移、前端拦截、登录兼容和回滚。
Blockers：首次登录强制改密码大概率涉及数据库结构和登录主流程；Token 上限涉及 Sa-Token 配置和多端登录业务预期，必须单独确认。
Short Notes：这是业务流程专项，不与当前漏洞修复补丁混做。
