# Memory

## Decisions

- 2026-05-29：漏洞修复按三批推进。第一批修局部可修的真实漏洞；第二批修中风险安全/稳定性/性能；第三批处理依赖升级、数据库结构、Token/CSRF 架构和前端技术债专项。
- 2026-05-29：当前没有正式生产域名；CORS 修复不得硬编码未知域名，第一批采用配置化策略并保留本地开发来源。
- 2026-05-29：上传允许常见图片、PDF、Excel、Word 格式；第一批服务端校验按这些类型做白名单。
- 2026-05-29：`CODE_REVIEW_BUSINESS.md` 作为业务安全补充来源；其中登录限流、RepeatSubmit fallback、商户验证码滥用、邮箱枚举、审批并发、GET XSS 跳过、SMS/Email key 共用进入 Phase 1/2，首次登录强制改密码和 Token 数量上限进入专项。

## Lessons

- 暂无。

## Known Risks

- 暂无。

## 2026-05-30 Security Remediation Lessons

- pnpm v11 不再读取 `package.json#pnpm.overrides`；本项目的前端依赖覆盖规则应放在 `admin-ui/pnpm-workspace.yaml`。
- 后端 OWASP Dependency-Check 依赖 NVD/CISA 外部数据源；如果更新失败且本地无缓存库，必须标记为阻塞，不能当作扫描通过。
- 登录受验证码阻挡时，可用本地 Redis 已有 admin 在线 Token 做 authenticated smoke；验证时不得输出 Token。
- `sys_menu.perms` 运行时按 `distinct + Set` 进入 Sa-Token 权限字符串集合；重复值不必默认修数据，先判断是否是菜单页和按钮共享权限。

## 2026-05-30 Known Risks

- `@vueup/vue-quill -> quill@1.3.7` 仍有 moderate advisory；没有 `quill@1.3.8` 发布版，Quill 2.x 迁移需单独评估。
- Cookie/CSRF 认证架构和生产 CORS 域名收紧等待正式域名与部署拓扑。

## Project Patterns

- 新任务开始前读取 `AGENTS.md` 和 `.ai` 核心文件。
- 平台跨租户查询必须同时依赖平台身份和明确业务边界，并使用受控 bypass；不要把业务确认的 `allList` / 数据权限 bypass 直接当作漏洞移除。
- 公开字典应走白名单或专用公开 API，不因某个公开下拉放开所有字典。
- `TenantContextFilter` 当前已在请求 `finally` 中调用 `TenantContextHolder.clear()`；后续不要把 ThreadLocal 泄漏报告当成未核验漏洞直接改。
- `SysProfileController.updatePwd()` 当前已校验旧密码；后续不要重复安排该项修复。
