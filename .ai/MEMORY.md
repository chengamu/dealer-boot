# Memory

本文件只记录长期有效的项目约束、架构决策和反复容易踩坑的经验。
不要记录单次任务流水、批次执行过程、临时验证步骤、归档清单或完整变更日志；这些内容应放在 `.ai/CURRENT.md`、`.ai/TASKS.md`、`.ai/HANDOFF.md` 或 `.ai/archive/`。

## Project Decisions

- PostgreSQL 是当前主数据库；新项目不再补 MySQL SQL。
- CORS 不硬编码未知生产域名；本地开发保留本地来源，后期 Docker Compose 部署优先使用 Nginx 同源代理，正式域名收紧作为 TODO。
- 上传服务端白名单只允许常见图片、PDF、Excel、Word 格式。
- 支付业务模型是商户付款给厂家 / 平台方；支付渠道配置归厂家 / 平台方管理，商户只作为付款方查看自己的支付状态和付款记录。
- 支付表和服务必须区分 `payer_tenant_id` 与 `payee_tenant_id`；旧系统单一 `tenant_id` 语义不能直接照搬。
- 真实支付渠道未完成 sandbox / 商户配置 / webhook 验签前，不实现真实下单、退款、转账或 webhook 成功流，也不能把非 Mock / Wallet 渠道伪造成成功。
- `pay_channel.config` 查询默认脱敏；写入 API、前端页面和菜单应晚于权限与脱敏策略。

## Project Patterns

- 新任务开始前读取 `AGENTS.md` 和 `.ai` 核心文件；普通问答不更新 `.ai` 工作流文件。
- 平台跨租户查询必须同时依赖平台身份和明确业务边界，并使用受控 bypass；不要把业务确认的 `allList` / 数据权限 bypass 直接当作漏洞移除。
- 公开字典应走白名单或专用公开 API，不因某个公开下拉放开所有字典。
- `TenantContextFilter` 当前已在请求 `finally` 中调用 `TenantContextHolder.clear()`；后续不要把 ThreadLocal 泄漏报告当成未核验漏洞直接改。
- `SysProfileController.updatePwd()` 当前已校验旧密码；后续不要重复安排该项修复。
- `sys_menu.perms` 运行时按 `distinct + Set` 进入 Sa-Token 权限字符串集合；重复值不必默认修数据，先判断是否是菜单页和按钮共享权限。
- 本项目 Spring Boot 可运行 dist 产物路径是 `bocoo-admin/target/dist/bocoo-admin.jar`。

## Known Risks / TODO

- PayPal / Stripe / 支付宝 / 微信真实支付联调需要 sandbox 或生产商户配置、证书或 webhook secret、以及公网 webhook 地址。
- PayPal webhook 验签是支付迁移强制验收项；旧代码虽有 `verifyWebhookSignature` 方法，但主流程未强制调用，不能原样迁移。
- Cookie/CSRF 认证架构和生产 CORS 域名收紧等待正式域名与部署拓扑。
- `@vueup/vue-quill -> quill@1.3.7` 仍有 moderate advisory；没有 `quill@1.3.8` 发布版，Quill 2.x 迁移需单独评估。
