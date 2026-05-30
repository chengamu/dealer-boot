# Project Agent Instructions

## Language

- 默认使用中文回答说明。
- 代码、变量名、方法名保持英文。
- 不输出密钥、密码、Token、连接串等敏感信息。

## Policy

- 如果用户输入 `自主模式`，则不执行本文件约束。
- 先理解需求和现有实现，再修改代码。
- 保持 minimal diff，只做完成任务所需的最小修改。
- 修改前确认文件存在；修改后优先做静态检查、编译检查或更小范围的可验证检查。
- 没有执行过的命令，不能声称已经执行或通过。

## Skills

- 详细工作流由 Skills 承担，不在 `AGENTS.md` 重复维护。
- 用户明确输入 `/plan`、`/do`、`/archive` 或 `使用 amu-workflow` 时，使用 `amu-workflow` Skill。
- 普通聊天、方案讨论、临时问答不得更新 `.ai/CURRENT.md`、`.ai/TASKS.md`、`.ai/HANDOFF.md` 或 `.ai/archive/`。

## Change Boundaries

- 涉及跨模块、数据库结构、依赖升级、架构调整、i18n、UTC、时区、日期格式时，必须先分析影响范围并给出计划。
- 不修改数据库结构。
- 不修改 migration 文件。
- 不修改生产环境配置。
- 不升级依赖，除非用户明确要求且已说明原因。
- 不做无关重构。
- 不删除用户代码或已有注释，除非该段代码确实无用且本任务要求删除。
- 不猜测不存在的框架能力或 API。
- 不绕过统一 i18n、UTC、日期时间工具；不新增硬编码 UI 文案。

## Frontend i18n

- 前端主 i18n 使用 `vue-i18n`，入口为 `admin-ui/src/i18n/index.ts`。
- `i18n/locales/en_US.json` 是唯一人工主语言；`i18n/locales/zh_CN.json` 由构建期 AI 补全并人工校对。
- `admin-ui/public/i18n/*.json` 由 `i18n:sync` 生成，不手动维护。
- 新增可见文案默认只写 `i18n/locales/en_US.json`，页面、组件、弹窗、表格列、按钮、placeholder、toast、空状态、错误提示、aria-label/title 都应使用 i18n key。
- 语言切换通过 `admin-ui/src/stores/locale.ts` 的 `setLocale` / `setLanguage`，不要刷新页面。
- 请求层必须继续发送 `Content-Language` 请求头。
- 字典翻译使用自动 key：`dict.<dictType>.<value>`；页面优先使用字典 API 返回的 `label`。
- 国家、币种、语言属于独立标准数据，不走普通字典 i18n。

## Backend i18n

- 后端运行时读取 `bocoo-admin/src/main/resources/i18n/*.json`，这些文件由 `i18n:sync` 从 `i18n/locales/*.json` 同步。
- 不再维护 `messages*.properties`；后端错误消息走统一 JSON `I18nService`。
- 后端 locale 通过 `I18nLocaleResolver` 从 `Content-Language` 请求头解析。
- 菜单和字典不依赖 `sys_i18n_message`；`sys_i18n_message` 已废弃。
- AI 翻译只允许在开发或构建期脚本中使用，运行时不得调用 AI。

## UTC / Timezone

- PostgreSQL 是主数据库；MySQL 已放弃，不再为 MySQL 补新增 SQL。
- PGSQL 初始化脚本使用 `timestamptz`，注释标明 UTC 存储。
- DEV PGSQL Hikari 连接使用 `SET TIME ZONE 'UTC'`。
- 后端审计时间使用 `TimeUtils.utcNow()`，不要直接用 `LocalDateTime.now()`。
- MyBatis `LocalDateTime` 使用 `UtcLocalDateTimeTypeHandler`。
- 前端展示后端时间使用 `formatUtc()` 或旧兼容 `parseTime()`；提交绝对时间使用 `toUtcPayload()` / `withUtcDateRange()` / `withUtcDateRangeParams()`。
- 中国用户和美国用户都按浏览器本地时区展示；后端存储和传输保持 UTC 语义。
- scheduler / cron 改动必须先确认是否有显式时区。

## Output Style

- 默认中文，简洁说明关键事实、风险、验证结果。
- 文件引用使用绝对路径。
- 不输出完整日志，只摘要关键结果。
- 没有把握的结论写 TODO，不要猜。
