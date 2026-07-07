# i18n 和 UTC 规范

## i18n

- 前端主 i18n 使用 `vue-i18n`，入口为 `admin-ui/src/i18n/index.ts`。
- 人工维护源文件写 `i18n/source/<module>/{en_US,zh_CN}.json`。
- `i18n/locales/*.json` 由 `pnpm i18n:build` 从模块源文件生成，不手工修改。
- `admin-ui/public/i18n/*.json` 和 `bocoo-admin/src/main/resources/i18n/*.json` 是 `pnpm i18n:sync` 同步产物，不手工修改。
- `admin-ui/public/i18n/*.json` 只是前端本地和浏览器运行时读取的静态文件，不是维护源；AI 或开发者都不能为了“让页面先显示”直接改它。
- `admin-ui/src/types/i18n-keys.d.ts` 由 `pnpm i18n:types` 从 source 生成，不手工修改。
- `pnpm i18n:translate` 只作为开发期 AI 补译草稿命令使用，不进入默认 `pnpm i18n` 流程；补译后必须人工校对并运行校验。
- 新增可见文案必须使用 i18n key，包括页面、组件、弹窗、表格列、按钮、placeholder、toast、空状态、错误提示、aria-label/title。
- 静态 `t('...')`、`getMessage('...')`、`i18n.global.t('...')`、`$t('...')` 引用必须能通过 `pnpm i18n:keys` 校验。
- 后端 Java 静态 key 引用必须能通过 `pnpm i18n:java-keys` 校验。
- 动态 i18n key 必须收敛在脚本白名单中；新增动态模式时先确认 key 空间，不允许随意拼接未知前缀。
- `pnpm i18n:hardcoded` 禁止新增前端硬编码文案；历史候选保存在 `i18n/hardcoded-baseline.json`，只有确认属于 DSL 或历史例外时才运行 `pnpm i18n:hardcoded:update` 更新。
- `pnpm i18n:types` 从 source 生成 `admin-ui/src/types/i18n-keys.d.ts`，给前端 `t('...')` 提供 key 类型提示。
- 语言切换通过 `admin-ui/src/stores/locale.ts` 的 `setLocale` / `setLanguage`。
- 请求层发送 `Content-Language` 请求头。

## i18n 修改流程

1. 先判断 key 属于哪个模块，写入 `i18n/source/<module>/en_US.json` 和 `i18n/source/<module>/zh_CN.json`。
2. 前端代码只引用 source 中存在的 key，不临时写死中文或英文。
3. 运行 `pnpm i18n`，由脚本生成 `i18n/locales/*.json`、`admin-ui/public/i18n/*.json`、`bocoo-admin/src/main/resources/i18n/*.json` 和 `admin-ui/src/types/i18n-keys.d.ts`。
4. 如果 `pnpm i18n:keys` 或 `pnpm i18n:drift` 失败，回到 source 修正；不要直接改生成产物来让检查通过。

禁止手工修改的生成产物：

- `i18n/locales/en_US.json`
- `i18n/locales/zh_CN.json`
- `admin-ui/public/i18n/en_US.json`
- `admin-ui/public/i18n/zh_CN.json`
- `bocoo-admin/src/main/resources/i18n/en_US.json`
- `bocoo-admin/src/main/resources/i18n/zh_CN.json`
- `admin-ui/src/types/i18n-keys.d.ts`

## i18n 模块归属

- `common/`：通用按钮、标签、上传、编辑器、日期等跨域文案。
- `auth/`：登录、注册、商家申请、验证码、第三方登录。
- `shell/`：框架壳、导航栏、标签页、权限提示、语言入口。
- `system/`：系统管理、用户、角色、菜单、租户、部门、岗位、OSS 配置。
- `customer/`：客户资料和客户管理。
- `merchant/`：商家资料、商家等级、折扣、商家用户。
- `product-center/`：产品中心基础资料、物料、属性、模板、资产、销售配置。
- `product-formula/`：配方档案、配方设置、模拟、审核、草稿。
- `ai/`：AI Runtime、模型、渠道、密钥、额度、AI 助手。
- `dict/`：普通字典值翻译。
- `validation/`：表单和参数校验文案。
- `error/`：请求、错误码和通用异常消息。
- `monitor/`：缓存、日志、在线用户等监控页面。
- `tool-gen/`：代码生成器。
- `demo/`：演示模块后端校验和示例页面文案。
- `dashboard/`、`dealer-portal/`、`legal/`、`legacy/`：对应独立页面域或历史兼容文案。

## 字典和菜单

- 字典翻译使用自动 key：`dict.<dictType>.<value>`。
- 页面优先使用字典 API 返回的 `label`。
- 菜单和按钮建议写 `i18n_key`，显示名由 JSON i18n 提供。
- 菜单和按钮新增 key 同样写入 `i18n/source/<module>/{en_US,zh_CN}.json`，不要直接改生成后的 flat locale 或运行时 JSON。
- 国家、币种、语言属于独立标准数据，不走普通字典 i18n。

## 后端消息

- 后端运行时读取 `bocoo-admin/src/main/resources/i18n/*.json`。
- 后端 locale 通过 `I18nLocaleResolver` 从 `Content-Language` 请求头解析。
- 后端错误消息走统一 JSON `I18nService`。
- `MessageUtils.message("...")`、`ServiceException.ofMessageKey("...")`、Bean Validation `message = "{...}"` 等静态 key 必须存在于 source。
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

- 新 key 已写入对应 `i18n/source/<module>/en_US.json` 和 `zh_CN.json`。
- 已运行 `pnpm i18n`；该命令包含 source 校验、前端 key 扫描、后端 Java key 扫描、硬编码 baseline 检查、TS 类型生成、sync 和 drift。
- `i18n/locales/*.json`、`admin-ui/public/i18n/*.json`、`bocoo-admin/src/main/resources/i18n/*.json` 必须由脚本生成且保持一致。
- 页面没有明显 i18n key 泄漏。
- 后端错误消息在 `Content-Language` 下能正确解析。
- 涉及时间展示时，用非中国时区抽查显示。
