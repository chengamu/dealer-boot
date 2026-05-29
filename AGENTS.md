# Project Agent Instructions

## Language

- 默认使用中文回答说明。
- 代码、变量名、方法名保持英文。
- 不输出密钥、密码、Token、连接串等敏感信息。

## Workflow

1. 先理解需求和现有实现，再修改代码。
2. 涉及跨模块、数据库结构、依赖升级、架构调整、i18n、UTC、时区、日期格式时，必须先分析影响范围并给出计划。
3. 默认允许连续完成同一子任务，不需要每一步都等待确认。
4. 保持 minimal diff，只做完成任务所需的最小修改。
5. 修改前确认文件存在，修改后优先做静态检查或编译检查；没有执行过的命令不能声称执行过。

## Pause Rules

以下情况必须暂停，并在暂停前更新 `.ai/CURRENT.md`，必要时同步 `.ai/MEMORY.md`：

- 功能阶段完成。
- 准备运行 build/test。
- 准备大规模修改。
- 准备跨模块重构。
- 涉及 i18n / UTC / 时区改动但影响范围不明确。
- 遇到不确定方案。

## Context Maintenance

- 开始新任务前先查看 `AGENTS.md`、`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- 做出关键技术判断时更新 `.ai/MEMORY.md` 的 Decisions 或 Project Patterns。
- 发现风险、遗留问题、不确定项时更新 `.ai/CURRENT.md`；长期有效的风险同步到 `.ai/MEMORY.md`。
- 交接或暂停前更新 `.ai/CURRENT.md`，写清楚已做、未做、验证情况和下一步。

## Forbidden

- 不修改数据库结构。
- 不修改 migration 文件。
- 不升级依赖，除非用户明确要求且已说明原因。
- 不做无关重构。
- 不删除用户代码或已有注释，除非该段代码确实无用且本任务要求删除。
- 不猜测不存在的框架能力或 API。
- 不在业务代码里写新的硬编码中文/英文 UI 文案。
- 不绕过统一 i18n、UTC、日期时间工具。
- 不随意使用系统本地时区。

## Frontend i18n Rules

- 前端主 i18n 使用 `vue-i18n`，入口仍以 `admin-ui/src/i18n/index.ts` 为运行入口。
- i18n 目标模型是单源 JSON：`i18n/locales/en_US.json` 是唯一人工主语言，`i18n/locales/zh_CN.json` 由构建期 AI 补全并人工校对。
- `admin-ui/public/i18n/*.json` 是由 `i18n:sync` 生成的前端运行时资源，不手动维护。
- 新增可见文案默认只写 `i18n/locales/en_US.json`；中文由 `i18n:translate` 补全，人工修正后提交。
- 页面、组件、弹窗、表格列、按钮、placeholder、toast、空状态、错误提示、aria-label/title 都应使用 i18n key。
- 语言切换必须通过 `admin-ui/src/stores/locale.ts` 的 `setLocale` / `setLanguage`，不要刷新页面来切换语言。
- 请求层必须继续发送 `Content-Language` 请求头，后端依赖它解析 locale。
- Element Plus locale 由 `admin-ui/src/App.vue` 的 `el-config-provider` 统一控制。
- 字典翻译使用自动 key：`dict.<dictType>.<value>`，存放在单源 JSON；页面优先使用字典 API 返回的 `label`，不要在页面局部拼接或硬编码翻译。
- 国家、币种、语言属于独立标准数据，不走普通字典 i18n。

## Backend i18n Rules

- 后端目标模型是运行时读取 `bocoo-admin/src/main/resources/i18n/*.json`，这些文件由 `i18n:sync` 从 `i18n/locales/*.json` 同步。
- 不再维护 `messages*.properties`；后端运行时资源来自同步后的 JSON。
- 后端 locale 通过 `bocoo-common/bocoo-common-web/.../I18nLocaleResolver.java` 从 `content-language` 请求头解析。
- 后端错误消息应走统一 JSON `I18nService`；`MessageUtils.message(...)` 底层必须兼容 JSON，减少业务代码改动。
- 菜单和字典不依赖 `sys_i18n_message`；字典 key 由规则自动生成，菜单保留 `i18n_key` 作为 JSON key/fallback。
- `sys_i18n_message` 已废弃，不再作为运行时或新功能翻译源。
- AI 翻译只允许在开发/构建期脚本中使用，运行时不得调用 AI。

## UTC / Timezone Rules

- PostgreSQL 是主数据库；MySQL 已放弃，不再为 MySQL 补新增 SQL。
- PGSQL 初始化脚本使用 `timestamptz`，注释标明 UTC 存储。
- DEV PGSQL Hikari 连接使用 `SET TIME ZONE 'UTC'`，避免 `timestamptz` 与 `LocalDateTime` 转换偏移。
- 后端审计时间使用 `TimeUtils.utcNow()`，不要直接用 `LocalDateTime.now()`。
- MyBatis `LocalDateTime` 使用 `UtcLocalDateTimeTypeHandler`。
- 前端展示后端时间使用 `formatUtc()` 或旧兼容 `parseTime()`；提交绝对时间使用 `toUtcPayload()` / `withUtcDateRange()` / `withUtcDateRangeParams()`。
- 中国用户和美国用户都按浏览器本地时区展示；后端存储和传输保持 UTC 语义。
- scheduler / cron 改动必须先确认是否有显式时区；当前项目主要是 XXL-JOB 外部入口，未发现本地 `@Scheduled` 任务。

## Output Style

- 默认中文，简洁说明关键事实、风险、验证结果。
- 文件引用使用绝对路径。
- 不输出完整日志，只摘要关键结果。
- 没有把握的结论写 TODO，不要猜。
