# Memory

## Decisions

- `.ai` 活跃上下文收敛为 4 个核心文件：`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- PostgreSQL 是当前主数据库方向；MySQL 相关内容视为历史兼容，不为新需求补 MySQL SQL。
- 前端可见文案必须使用 `vue-i18n` key，同时维护中文和英文 locale。
- 后端用户可见消息优先使用 MessageSource / `MessageUtils.message(...)` / Bean Validation message key。
- 业务、审计、API 绝对时间按 UTC 语义处理；前端按浏览器本地时区展示。
- 多租户平台真实租户 ID 固定为 `1`；`tenant_id = 0` 不再是合法业务租户，也不得作为缺失 tenant fallback。
- 跨境订单系统不是通用 SaaS 登录选租户模型；登录页不提供租户选择，平台/商家身份由登录用户上下文自动识别。
- 商家注册只提交申请资料；平台审核通过后由后端生成初始密码并通过邮件发送。

## Lessons

- PowerShell 读取中文文档优先使用 `Get-Content -Raw -Encoding UTF8`。
- codegraph 适合快速定位结构和符号，但最终结论必须读取真实源文件确认。
- 准备运行 build/test/lint 前必须说明并按 `.ai/rules/do.md` 的授权规则执行。
- 执行真实 DB/SMTP 验证时不要输出连接串、密码、授权码；临时执行器放在系统临时目录，验证结束后删除。
- 对本地目标库做 schema/seed 同步后，要同步清 Redis session/cache。
- 如果 IDE 后端 `8081` 未重启，API 结果可能仍是旧代码/旧缓存；最终验收需说明真实 dev 栈是否已切回新代码。
- `SysDictDataController` 公开字典应走白名单或专用公开 API，不要因国家下拉放开所有字典。
- 申请人面向邮件应按申请提交时持久化的 `applyLocale` 渲染。
- 法务文档默认应为 `DRAFT`，发布时再补 `publishedTime/version`。
- Maven / MapStruct clean 后首次完整 `mvn test` 偶发 generated-source read error；不要隐藏第一次失败，应记录 transient 风险。
- 国家字典同步后必须清 Redis `sys_dict` 缓存。

## Known Risks

- 动态后端用户可见文本仍需要参数化 message 策略。
- Generator Swagger/OpenAPI 注解、`@Log(title)`、`@ExcelProperty`、SQL fallback label 等 i18n 债务仍需分类。
- `SysI18nMessageService` 运行时缓存刷新行为在编辑 DB i18n message 时仍需确认。
- Generator 页面前端运行时 DB i18n loader 未实现；仅在做代码生成器或 i18n 编辑器时处理。
- API 时间契约为 ISO-8601 UTC `Z`；legacy no-zone query string 不作为新代码支持目标。
- `DateUtils` 仍有默认时区 legacy/local helper，不应用于持久化业务时间或 API 时间。
- 未来若出现正式生产环境，本地 dealer 直同步经验不能替代生产方案；必须重新制定备份、迁移、回滚和演练流程。
- `sys_role_menu` 依赖租户插件补 `tenant_id`；后续扩展角色菜单写入仍建议复核。
- `sys_tenant`、`sys_tenant_apply` 仍在 ignore tables 中；新增平台管理查询时需明确哪些表 ignore，哪些由服务层受控过滤。
- 邮件模板、重发邀请、重置密码能力未实现。

## Project Patterns

- 新任务开始前读取 `AGENTS.md` 和 `.ai` 四个核心文件。
- 涉及跨模块、数据库、依赖、架构、i18n、UTC、时区、日期格式时，先在 `.ai/CURRENT.md` 写影响范围和计划。
- i18n 主链路已完成；生成器 SQL 会写 `sys_i18n_message`，但生成页面前端 `getMessage()` 只读本地 locale。
- UTC：新 API 时间契约统一 ISO-8601 UTC `Z`；不要用 `DateUtils` 默认时区 helper 做业务/API 时间。
- 后端 `run.bat` 和 `docker-compose.yml` 已统一 UTC；后续不要重新引入非 UTC 系统时区。
- Controller 权限优先检查 Sa-Token 注解和菜单/按钮权限标识。
- 数据权限按 `@DataPermission`、`@DataColumn` 和相关处理器现有模式处理。
- 商家主体资料独立于个人中心；商家只能更新非关键字段。
- 平台跨租户查询必须同时依赖平台身份和明确权限，并使用受控 bypass。

## Sub-Agent Notes

- frontend-developer：前端页面、组件、交互、样式、接口接入。
- typescript-pro：TypeScript 类型边界、复杂类型推导、组合式函数和编译类型风险。
- java-architect：Java Controller、Service、Mapper、权限、事务、模块边界和架构影响。
- code-reviewer：`/check` 阶段最终审查，只审查不继续改代码。

## Archive

- 2026-05-25：历史 UTC、i18n、generator、phase 日志已归档到 `.ai/archive/`。
- 2026-05-27：初始化轻量 AI 工作流，创建四个核心文件并迁移旧 `.ai` 有效内容。
- 2026-05-28：归档“多租户 + 商家注册/审核主链路”。Final Status：Accepted with Risks。完成平台租户 ID=1、`tenant_id=0` 清零、`merchant_profile`、商家申请审核、账号/角色/菜单权限、登录上下文 `tenantType/merchantId`、用户名/邮箱登录、平台/商家权限边界、前端注册/审核/商家资料页面、i18n/UTC、DEV DB/Redis 同步、API/Browser/Runtime 验证和 DEV SMTP 真实发送。剩余风险：未来正式生产环境需重新制定备份/迁移/回滚；邮件模板/重发邀请/重置密码为后续增强；未覆盖所有浏览器和移动端响应式视口。
- 2026-05-28：归档“商家注册/审核运行时 follow-up”。Final Status：Accepted with Risks。完成公开申请页邮箱验证码、Terms/Privacy、国家下拉、法务文档维护、商家管理路由、公开字典白名单、登录态跳转、DEV `8081/8083` Runtime/API/Browser 验证，并修复异步登录日志读取已回收 request 的异常。剩余风险：正式生产仍需单独迁移/回滚方案；提交前需过滤本地日志、IDE 与验证产物；国家跨语言 alias 搜索为后续增强。
