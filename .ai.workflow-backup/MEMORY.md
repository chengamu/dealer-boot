# Memory

## Decisions

- `.ai` 活跃上下文收敛为 4 个核心文件：`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- PostgreSQL 是当前主数据库方向；MySQL 相关内容视为历史兼容，不为新需求补 MySQL SQL。
- 前端可见文案必须使用 i18n key；目标源是 `i18n/locales/en_US.json`，`zh_CN.json` 由构建期 AI 补全并人工校对。
- 后端用户可见消息由 JSON `I18nService` / 兼容后的 `MessageUtils.message(...)` 解析；不再维护 `messages*.properties`。
- 业务、审计、API 绝对时间按 UTC 语义处理；前端按浏览器本地时区展示。
- 多租户平台真实租户 ID 固定为 `1`；`tenant_id = 0` 不再是合法业务租户，也不得作为缺失 tenant fallback。
- 跨境订单系统不是通用 SaaS 登录选租户模型；登录页不提供租户选择，平台/商家身份由登录用户上下文自动识别。
- 商家注册只提交申请资料；平台审核通过后由后端生成初始密码并通过邮件发送。
- 租户过滤主要用于业务数据隔离；系统表由系统权限控制，公共表按共享资源处理，业务表必须按 `tenant_id` 隔离。`sys_tenant` 是租户注册/系统表，`sys_tenant_apply` 是商家入驻申请/系统流程表，二者不靠租户插件过滤。
- i18n 现代化目标改为单源 JSON：`i18n/locales/en_US.json` 为唯一人工主语言，`zh_CN.json` 由构建期 AI 补全并人工校对；前后端运行时读静态 JSON，AI 不进入运行时。
- i18n 第一波确认包含：新增根目录 `i18n/` 和 pnpm 脚本、`MessageUtils.message(...)` JSON 兼容、字典 `i18n_key` 翻译来源废弃、`sys_i18n_message` 彻底废弃并迁移到 JSON、国家/币种/语言标准数据、允许 PostgreSQL 改动和 build/typecheck/test 验证。
- i18n 计划确认：baseline/code 完整移除 `sys_i18n_message` 和 `sys_dict_data.i18n_key` 翻译来源，但 DEV DB drop 必须在 JSON 与运行时验证后执行；`i18n:translate` 使用 DeepSeek `deepseek-v4-pro`，API key 由 `.env` 占位后续补充；国家/币种/语言 seed 使用 ISO/CLDR 静态数据，普通字典中的国家数据删除。
- 后端打包后运行入口优先使用 `bocoo-admin/target/dist` 目录下的发行包产物；不要默认用 `bocoo-admin/target/bocoo-admin.jar` 作为运行入口。

## Lessons

- Browser smoke for this repo should prefer the current checkout on `127.0.0.1:8083` when a shared `192.168.*:8083` target is blocked or may not reflect local files.
- Known frontend dictionary types should prefer i18n keys over raw backend labels; otherwise English seed values such as `Unknown` can leak into zh UI.
- Legal document content should stay on existing `sys_legal_document` admin maintenance/API once present; do not reintroduce TXT/file-based runtime content.
- PostgreSQL seed is the active database path; deprecated MySQL seed files are not repaired unless the user explicitly asks.
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
- 国家下拉当前按返回 label + ISO value 本地搜索；新 i18n 方案要求国家/币种/语言迁为独立标准数据，不再走普通字典或 `sys_i18n_message`。
- 临时验证后端如果用模块 jar 启动，需先确认 jar 是否为最新构建产物；2026-05-29 复核确认重新 `package/install` 后 `bocoo-modules-system`、`bocoo-modules-generator` 模块 jar 以及 `bocoo-admin/target/dist/bocoo-admin.jar` 内嵌模块 jar 均包含 mapper XML，前次缺失判断来自 stale jar。
- 运行后端发行包时优先进入 `bocoo-admin/target/dist`，使用该目录下的发行包/启动脚本验证；`target/bocoo-admin.jar` 可能不是可直接运行的 fat jar。
- `vue-i18n` 开启 `flatJson` 后可能会把部分扁平 key 规范化成嵌套对象；同步兼容 helper（如 `getMessage`）必须同时支持扁平 key 和嵌套 path，否则导航/面包屑会显示原始 key。
- 2026-05-29 验证 `/thirdLogin`：该接口是 appKey/secretKey 简单登录入口，当前按平台租户登录处理；运行时从 `bocoo-admin/target/dist` 启动后，`/thirdLogin` 返回 200，`/getInfo` 返回 `tenantId=1`、`tenantType=PLATFORM`。
- 字典 label 由后端 JSON i18n 翻译后直接返回，不再向页面暴露字典 `i18nKey`；菜单 `i18nKey` 仍保留作为 JSON key/fallback，两者不要混淆。

## Known Risks

- 动态后端用户可见文本仍需要参数化 message 策略。
- Generator Swagger/OpenAPI 注解、`@Log(title)`、`@ExcelProperty`、SQL fallback label 等 i18n 债务仍需分类。
- `SysI18nMessageService` 已退出运行时链路；后续重点不是完善 DB i18n 编辑，而是保持静态 JSON `I18nService` 单源。
- Generator 页面前端运行时 DB i18n loader 不再作为目标方向；生成器应改为输出/引用单源 JSON key。
- API 时间契约为 ISO-8601 UTC `Z`；legacy no-zone query string 不作为新代码支持目标。
- `DateUtils` 仍有默认时区 legacy/local helper，不应用于持久化业务时间或 API 时间。
- 未来若出现正式生产环境，本地 dealer 直同步经验不能替代生产方案；必须重新制定备份、迁移、回滚和演练流程。
- `sys_role_menu` 依赖租户插件补 `tenant_id`；后续扩展角色菜单写入仍建议复核。
- `sys_user`、`sys_role`、`sys_dept` 虽然是 `sys_*` 表，但若未来开放商家管理员维护员工、角色或部门，需要按产品规则重新确认是否作为租户内资源隔离。
- 邮件模板、重发邀请、重置密码能力未实现。
- 国家字典跨语言 alias/search-key 能力未实现；当前仅按当前返回 label 和 ISO code 搜索。
- 后端发布包/模块 jar 的 mapper XML 打包策略已于 2026-05-29 复核通过；后续风险重点是避免用 stale module jar 做运行时验证。
- `/thirdLogin`、`/smsLogin`、`/emailLogin`、`/xcxLogin` 这类 `@SaIgnore` 登录入口若要查询带租户字段的用户表，必须同时纳入租户上下文 ignore 或使用受控登录查询策略；否则会出现登录前 `Tenant id is required`。

## Project Patterns

- 新任务开始前读取 `AGENTS.md` 和 `.ai` 四个核心文件。
- 涉及跨模块、数据库、依赖、架构、i18n、UTC、时区、日期格式时，先在 `.ai/CURRENT.md` 写影响范围和计划。
- 旧 i18n 主链路使用 TS locale、properties 和 `sys_i18n_message`；新方向已改为单源 JSON，旧链路仅作历史背景。
- 新 i18n 需求中，系统文案和字典翻译走单源 JSON；字典 key 自动生成为 `dict.<dictType>.<value>`，国家/币种/语言独立标准数据，业务数据原样存储，`sys_i18n_message` 已废弃。
- UTC：新 API 时间契约统一 ISO-8601 UTC `Z`；不要用 `DateUtils` 默认时区 helper 做业务/API 时间。
- 后端 `run.bat` 和 `docker-compose.yml` 已统一 UTC；后续不要重新引入非 UTC 系统时区。
- Controller 权限优先检查 Sa-Token 注解和菜单/按钮权限标识。
- 数据权限按 `@DataPermission`、`@DataColumn` 和相关处理器现有模式处理。
- 商家主体资料独立于个人中心；商家只能更新非关键字段。
- 平台跨租户查询必须同时依赖平台身份和明确权限，并使用受控 bypass。
- `merchant_profile` 是审核通过后从 `sys_tenant_apply` 沉淀出的正式商家资料表，与 `sys_tenant` 通过 `tenant_id` 一对一关联；商家自助资料展示/维护应以后端登录上下文中的 tenantId/merchantId 为准，不信任前端传入任意 tenantId。

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
- 2026-05-29：归档“mapper XML 发布包/模块 jar 打包复核”。Final Status：Accepted with Risks。确认重新 `package/install` 后模块 jar 和 `bocoo-admin/target/dist/bocoo-admin.jar` 内嵌模块 jar 均包含 mapper XML；无需改 POM。剩余风险：后续不要用 stale module jar 判断发布包内容。
- 2026-05-29：归档“i18n follow-up / thirdLogin / deprecated cleanup”。Final Status：Accepted with Risks。完成 DeepSeek translate 脚本空缺失验证、`bocoo-admin/target/dist` 运行约定、`/thirdLogin` 平台租户登录验证、废弃 i18n 文件/代码保留删除、字典 `i18nKey` 残留清理。剩余风险：DeepSeek 实际 provider 调用需等真实缺失 key 验证；其他 `@SaIgnore` 登录入口需后续复核。
- 2026-05-29：归档“browser comments follow-up”。Final Status：Accepted with Risks。完成法律页路由、商户申请备注布局、消息面板、密码锁定默认值、角色编辑不踢人、OSS 预览开关、平台用户查询 bypass、字典 i18n 优先和 PostgreSQL `简体中文` seed 修复。剩余风险：live DB 若已有坏数据仍需运维清理；本地 browser smoke 使用前端 fallback 法务内容。
