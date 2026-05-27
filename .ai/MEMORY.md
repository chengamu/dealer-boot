# Memory

## Decisions

- 项目 AI 上下文收敛为 4 个核心文件：`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- 旧 `.ai/BUGS.md`、`.ai/COMMANDS.md`、`.ai/DECISIONS.md`、`.ai/HANDOFF.md`、`.ai/TASKS.md` 仅保留 deprecated 指引，不再作为活跃上下文入口。
- PostgreSQL 是当前主数据库方向；MySQL 相关内容视为历史兼容，不为新需求补 MySQL SQL。
- 前端可见文案必须使用 `vue-i18n` key，同时维护中文和英文 locale。
- 后端用户可见消息优先使用 MessageSource / `MessageUtils.message(...)` / Bean Validation message key。
- 业务、审计、API 绝对时间按 UTC 语义处理；前端按浏览器本地时区展示。
- API 绝对时间目标契约为 ISO-8601 UTC `Z` 字符串。
- 代码生成模板应保持 i18n 和 UTC 规则，不生成新的本地时区时间处理。
- 多租户平台真实租户 ID 已确认固定为 `1`；`tenant_id = 0` 不再是合法业务租户，也不得作为缺失 tenant fallback，平台身份后续通过 `tenantType = PLATFORM` 判断。

## Lessons

- PowerShell 读取中文文档优先使用 `Get-Content -Raw -Encoding UTF8`，否则可能出现中文乱码。
- codegraph 适合快速定位结构和符号，但最终结论必须读取真实源文件确认。
- 准备运行 build/test/lint 前必须暂停说明，不要把未执行命令写成已通过。
- 前端 `package.json` 指向 pnpm，但仓库同时存在 `package-lock.json`，执行安装前需要确认包管理器，避免锁文件混用。
- 历史生成器样例再生成曾卡在 PowerShell/Maven classpath 参数解析，重试前应先修正命令拼接方式。

## Known Risks

- 动态后端用户可见文本仍需要参数化 message 策略，例如导入摘要、上游消息、文件扩展名列表和遗留 pass-through 异常文本。
- 部分 legacy `ServiceException` / `BaseException` 纯文本路径仍为兼容或内部路径，新用户可见代码不要继续扩散。
- Generator Swagger/OpenAPI 注解、`@Log(title)`、`@ExcelProperty`、SQL fallback label 和部分元数据是否属于用户可见 i18n 债务仍需分类。
- `SysI18nMessageService` 运行时缓存刷新行为在编辑 DB i18n message 时仍需确认。
- Generator 页面前端运行时 DB i18n loader 未实现；仅在做代码生成器或 i18n 编辑器时处理。
- API 时间契约为 ISO-8601 UTC `Z`，外部 API 文档和前端类型期望仍需同步。
- Spring MVC query-object `LocalDateTime` 绑定期望 ISO instant UTC 字符串；legacy no-zone query string 不作为新代码支持目标。
- `DateUtils` 仍有默认时区 legacy/local helper，不应用于持久化业务时间或 API 时间。
- 后端 `run.bat` 和 `docker-compose.yml` 已统一为 UTC 时区配置；后续不要重新引入非 UTC 系统时区、宿主 `/etc/localtime` 挂载或依赖系统默认时区的时间逻辑。

## Project Patterns

- 新任务开始前读取 `AGENTS.md` 和 `.ai` 四个核心文件。
- 涉及跨模块、数据库、依赖、架构、i18n、UTC、时区、日期格式时，先在 `.ai/CURRENT.md` 写影响范围和计划。
- i18n：主链路已完成；生成器 SQL 会写 `sys_i18n_message`，但生成页面前端 `getMessage()` 只读本地 locale，不运行时加载 DB i18n。做代码生成器或 i18n 编辑器时再处理。
- UTC：新 API 时间契约统一 ISO-8601 UTC `Z`；不要使用 legacy no-zone query string；不要用 `DateUtils` 默认时区 helper 做业务/API 时间。
- 环境：后端 `run.bat` 和 `docker-compose.yml` 已统一 UTC；后续不要重新引入非 UTC 系统时区。
- 验证：build/test/lint 未默认运行，`/check` 阶段按需确认后执行。
- 权限：Controller 权限优先检查 Sa-Token 注解和菜单/按钮权限标识。
- 数据权限：Mapper 查询按 `@DataPermission`、`@DataColumn` 和相关处理器现有模式处理。
- 验证命令需要先说明并暂停；静态只读检查可先执行。

## Sub-Agent Notes

- frontend-developer：适合前端页面、组件、交互、样式、接口接入；结论要压缩到 CURRENT.md。
- typescript-pro：适合 TypeScript 类型边界、复杂类型推导、组合式函数和编译类型风险；不要让其扩大为全仓 TS 重写。
- java-architect：适合 Java Controller、Service、Mapper、权限、事务、模块边界和架构影响；数据库结构或 migration 仍需主 Agent 暂停确认。
- code-reviewer：用于 /check 阶段最终审查，只审查不继续改代码；重点检查需求、验收标准、minimal diff、i18n、UTC、权限、API 兼容、未验证项。
- 如果子 Agent 意见冲突，主 Agent 记录冲突点并暂停，不强行猜测。

## Archive

- 2026-05-25：历史 UTC、i18n、generator、phase 日志已归档到 `.ai/archive/`，不再回填到活跃上下文。
- 2026-05-25：AI Context Compaction 完成；当时未修改业务代码，未运行 build/test。
- 2026-05-25：`java-architect` 安装到 `.codex/agents/java-architect.toml`。
- 2026-05-27：`code-reviewer` 安装到 `.codex/agents/code-reviewer.toml`。
- 2026-05-27：初始化轻量 AI 工作流，创建四个核心文件并迁移旧 `.ai` 有效内容。
