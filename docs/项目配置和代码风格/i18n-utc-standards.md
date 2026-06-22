# i18n 和 UTC 规范

## i18n

- 前端主 i18n 使用 `vue-i18n`，入口为 `admin-ui/src/i18n/index.ts`。
- 人工维护源文件写 `i18n/locales/en_US.json`。
- `i18n/locales/zh_CN.json` 由开发或构建期 AI 补全后人工校对。
- `admin-ui/public/i18n/*.json` 和 `bocoo-admin/src/main/resources/i18n/*.json` 是同步产物。
- 新增可见文案必须使用 i18n key，包括页面、组件、弹窗、表格列、按钮、placeholder、toast、空状态、错误提示、aria-label/title。
- 语言切换通过 `admin-ui/src/stores/locale.ts` 的 `setLocale` / `setLanguage`。
- 请求层发送 `Content-Language` 请求头。

## 字典和菜单

- 字典翻译使用自动 key：`dict.<dictType>.<value>`。
- 页面优先使用字典 API 返回的 `label`。
- 菜单和按钮建议写 `i18n_key`，显示名由 JSON i18n 提供。
- 国家、币种、语言属于独立标准数据，不走普通字典 i18n。

## 后端消息

- 后端运行时读取 `bocoo-admin/src/main/resources/i18n/*.json`。
- 后端 locale 通过 `I18nLocaleResolver` 从 `Content-Language` 请求头解析。
- 后端错误消息走统一 JSON `I18nService`。
- AI 翻译只允许在开发或构建期脚本中使用，运行时不得调用 AI。

## UTC

- PostgreSQL 是主数据库，时间字段按 UTC 语义存储和传输。
- PGSQL 初始化脚本使用 `timestamptz`，注释标明 UTC 存储。
- DEV PGSQL Hikari 连接使用 `SET TIME ZONE 'UTC'`。
- 后端审计时间使用 `TimeUtils.utcNow()`。
- MyBatis `LocalDateTime` 使用 `UtcLocalDateTimeTypeHandler`。
- 前端展示后端时间使用 `formatUtc()`。
- 前端提交绝对时间使用 `toUtcPayload()` / `withUtcDateRange()` / `withUtcDateRangeParams()`。
- 中国用户和美国用户都按浏览器本地时区展示；后端存储和传输保持 UTC 语义。
- scheduler / cron 改动必须先确认是否有显式时区。

## 验证

- 新 key 已写入 `i18n/locales/en_US.json`。
- 已运行项目可用的 i18n validate/sync 脚本。
- 页面没有明显 i18n key 泄漏。
- 后端错误消息在 `Content-Language` 下能正确解析。
- 涉及时间展示时，用非中国时区抽查显示。
