# Requirement: single-source i18n modernization

## Goal：目标

把现有多源 i18n：

- 前端 `admin-ui/src/locales/en_US.ts`
- 前端 `admin-ui/src/locales/zh_CN.ts`
- 后端 `messages_en_US.properties`
- 后端 `messages_zh_CN.properties`
- 数据库 `sys_i18n_message`
- 字典 `i18n_key`

逐步重构为：

- 单一源文件目录：`i18n/locales/en_US.json`、`i18n/locales/zh_CN.json`
- `en_US.json` 作为唯一主语言，人工维护
- `zh_CN.json` 由构建期 AI 补全，人工校对
- 前端和后端运行时只读取静态 JSON 资源
- AI 只作为开发/构建期翻译助理，不参与运行时

## Background：背景

当前系统 i18n 维护成本高：

- 新增一条文案可能同时需要改前端 TS、后端 properties、数据库 i18n seed。
- 菜单、字典有时由后端 `sys_i18n_message` 翻译，有时又由前端本地 locale fallback。
- 国家/币种/语言这类标准资料被混在普通字典翻译模型里，不利于搜索和维护。
- `sys_i18n_message` 带来 seed、缓存、运行时查询、缺失 key 管理等额外复杂度。

## Scope：范围

- 统一 i18n 源文件和 key 命名规范。
- 明确前端 JSON lazy loading 方案。
- 明确后端 JSON i18n 服务方案。
- 明确构建期 AI 翻译、校验、同步脚本。
- 明确字典、国家/币种/语言、业务数据的边界。
- 明确 `sys_i18n_message` 废弃路线。
- 第一波实施包含根目录 `i18n/`、pnpm 脚本、前端 JSON 加载、后端 `MessageUtils.message(...)` 底层 JSON 兼容、字典 JSON 翻译、国家/币种/语言标准数据、相关引用代码同步修改。
- 本需求允许修改 PostgreSQL schema/seed，并允许执行 build/typecheck/test 等验证命令。

## Out of Scope：不做范围

第一版不做：

- 不做后台在线编辑 i18n。
- 不接 Crowdin / Lokalise 等外部 i18n 管理平台。
- 不做数据库存储 i18n。
- 不做运行时 AI 翻译。
- 不做 Redis 翻译缓存。
- 不做多租户文案覆盖。
- 不做复杂 ICU 复数规则。
- 不做运行时 AI 或运行时远程翻译调用。

## Target Directory：目标目录

```text
project-root/
  i18n/
    locales/
      en_US.json
      zh_CN.json
    scripts/
      check.ts
      translate.ts
      validate.ts
      sync.ts

  admin-ui/
    public/
      i18n/
        en_US.json
        zh_CN.json

  bocoo-admin/
    src/main/resources/
      i18n/
        en_US.json
        zh_CN.json
```

规则：

- `i18n/locales/` 是唯一源文件目录。
- `admin-ui/public/i18n/` 和 `bocoo-admin/src/main/resources/i18n/` 是构建/同步产物，不手动维护。
- 未来删除或停止维护前端 TS locale 和后端 properties。

## Language File Rules：语言文件规范

- 主语言：`en_US.json`。
- 目标语言：`zh_CN.json`。
- key 使用扁平结构，避免深层对象带来的合并和 diff 成本。
- 占位符统一使用 `{{variable}}`。
- 中文翻译必须保留英文同名占位符。
- 第一版不启用 ICU；未来有复数、性别、复杂日期金额格式时再评估 ICU MessageFormat。

示例：

```json
{
  "auth.login": "Login",
  "auth.logout": "Logout",
  "order.submit": "Submit Order",
  "order.success": "Order {{orderNo}} submitted successfully.",
  "common.save": "Save",
  "common.cancel": "Cancel"
}
```

## Key Naming Rules：Key 命名规范

统一格式：

```text
module.feature.action
module.feature.field
dict.<dict_type_code>.<value>
```

推荐：

- `auth.login`
- `auth.logout`
- `nav.home`
- `nav.orders`
- `order.list`
- `order.detail`
- `order.submit`
- `order.status.pending`
- `product.name`
- `common.save`
- `common.cancel`
- `dict.order_status.pending`
- `dict.payment_status.paid`

禁止：

- `submit`
- `name`
- `title`
- `button1`
- `text001`

## Business Rules：业务规则

分类判断：

```text
代码里的固定文案？ -> JSON
系统枚举字典？ -> dict 表存 value，JSON 存 label
国家/币种/语言/标准基础资料？ -> 独立基础数据表
客户填写的数据？ -> 原样存储
```

## Frontend Requirements：前端需求

- 前端继续使用 `vue-i18n`。
- 从 `/i18n/{locale}.json` lazy load 语言文件。
- 页面统一使用 `t('key')`。
- 参数化文案使用 `t('order.success', { orderNo })`。
- 禁止业务页面硬编码中文或英文。
- 语言切换后应更新菜单、面包屑、标签、按钮、表单、toast 等可见文案。
- 字典显示优先使用接口返回的 `label`，页面不应到处拼接 `dict.` key。

## Backend Requirements：后端需求

- 最终不再使用 `.properties` 作为主维护格式。
- 后端运行时直接读取 `resources/i18n/{locale}.json`。
- 第一阶段先让 `MessageUtils.message(...)` 底层兼容 JSON，减少现有业务代码大面积改动。
- 后续再抽出/扩展统一 `I18nService`，供字典、错误消息、邮件模板等统一使用。
- 提供统一服务，例如：

```java
i18n.get("order.submit", locale)
i18n.get("order.success", locale, Map.of("orderNo", orderNo))
```

- 后端系统级错误可返回 `code + messageKey + message`，过渡期保留 `message` 便于兼容现有前端。
- 表单/页面提示优先由前端翻译。
- 业务状态/枚举统一使用稳定 i18n key。
- 开发环境缺失 key 建议显示 `[missing: key]`；生产环境可兜底为 key 或 value。

## Dictionary Requirements：字典需求

字典也走 JSON 翻译，但字典表不再手动存 `i18n_key`。

自动 key 规则：

```text
dict.{dict_type_code}.{value}
```

示例：

```json
{
  "dict.order_status.pending": "Pending",
  "dict.order_status.confirmed": "Confirmed",
  "dict.payment_status.unpaid": "Unpaid",
  "dict.payment_status.paid": "Paid"
}
```

后端字典服务生成 key：

```java
String key = "dict." + dictType + "." + value;
String label = i18n.get(key, locale);
```

返回给前端：

```json
{
  "value": "pending",
  "label": "Pending",
  "i18nKey": "dict.order_status.pending",
  "color": "warning"
}
```

前端优先显示：

```vue
{{ item.label }}
```

### Dict Table Direction：字典表方向

第一波明确废弃 `sys_dict_data.i18n_key` 运行时依赖，字典翻译直接走 JSON。旧字段可以先通过迁移保留或清理，但新代码不得继续读写作为翻译来源。

目标字典表只保留系统枚举所需字段：

```text
id
dict_type_code
value
color
sort
enabled
remark
created_at
updated_at
```

废弃或停止使用：

```text
label
label_en
label_zh
i18n_key
```

本轮允许修改 PostgreSQL schema/seed；是否物理删除字段在 `/plan` 中按影响范围拆分，但实现目标是停止依赖 `i18n_key`。

## Standard Data Requirements：标准基础数据

国家、币种、语言不是普通系统枚举，不走普通字典。它们并入第一波实施，需要独立标准数据表、后端 API、前端下拉/搜索和 seed 同步。

### Country

建议独立表或标准数据模块：

```text
id
code
iso2
iso3
name_en
name_zh
native_name
phone_code
currency_code
enabled
sort
created_at
updated_at
```

接口按 locale 返回：

```json
{
  "code": "CN",
  "name": "中国",
  "nameEn": "China",
  "nameZh": "中国",
  "nativeName": "中国",
  "iso2": "CN",
  "phoneCode": "+86"
}
```

下拉搜索需要支持 code、当前显示名、英文名、本地名模糊匹配。

### Currency

建议字段：

```text
id
code
name_en
name_zh
symbol
decimal_places
enabled
sort
```

### Language

建议字段：

```text
id
language_code
locale_code
name_en
native_name
enabled
sort
```

## Business Data Rules：业务数据规则

以下数据原样存储，不生成 i18n key：

- 客户公司名
- 联系人姓名
- 地址
- 订单备注
- 项目名称
- 房间名称
- 客户上传说明
- 产品自定义名称

## AI Translation Requirements：AI 翻译需求

AI 只在开发/构建阶段使用：

- 新增 key。
- 发现目标语言缺失。
- 批量初始化翻译。
- 长文案初稿生成。

AI 不在运行时使用：

- 用户登录。
- 用户下单。
- 接口返回错误。
- 页面渲染。

AI 翻译要求：

- 只翻译 value。
- 不修改 key。
- 保留 `{{variable}}`。
- 保留 HTML 标签。
- 保留数字、单位、订单号格式。
- 语气适合 B2B 订单系统。
- 中文使用简体中文。
- 只处理缺失或空值，不覆盖已有人工翻译。

## Script Requirements：脚本需求

### `i18n:check`

检查：

- `zh_CN` 是否缺 key。
- 是否有多余 key。
- 是否有空 value。
- key 是否重复。

### `i18n:translate`

功能：

- 自动补全 `zh_CN` 缺失翻译。
- 只处理缺失或空值。
- 不覆盖已有人工翻译。

### `i18n:validate`

检查：

- 所有语言 key 是否一致。
- 占位符是否一致。
- JSON 是否合法。
- value 是否为空。
- 是否存在明显未翻译内容。
- `dict.*` key 是否覆盖所有启用字典项。

### `i18n:sync`

功能：

- 复制 `i18n/locales/*.json` 到 `admin-ui/public/i18n/`。
- 复制 `i18n/locales/*.json` 到 `bocoo-admin/src/main/resources/i18n/`。

## Development Workflow：开发流程

新增文案：

1. 在 `i18n/locales/en_US.json` 新增英文 key。
2. 执行 `pnpm i18n:translate`。
3. 人工检查 `i18n/locales/zh_CN.json`。
4. 执行 `pnpm i18n:validate`。
5. 执行 `pnpm i18n:sync`。
6. 提交代码。

可合并命令：

```text
pnpm i18n
```

等价于：

```text
pnpm i18n:translate
pnpm i18n:validate
pnpm i18n:sync
```

## CI/CD Requirements：CI/CD 要求

- 提交或构建前执行 `pnpm i18n:validate`。
- 构建前执行 `pnpm i18n:sync`。
- 校验失败禁止构建。
- 失败场景包括缺少 key、占位符不一致、JSON 格式错误、空翻译、字典覆盖缺失。

## Migration Plan：迁移步骤

### Phase 1：建立单源 JSON 和脚本

- 创建 `i18n/locales/en_US.json`、`i18n/locales/zh_CN.json`。
- 创建 check / validate / sync 脚本。
- 创建 pnpm 脚本入口。
- 从现有 TS locale、properties、`sys_i18n_message` seed、字典 label/i18n key 等抽取初始 JSON。
- 保留 AI translate 脚本接口，但运行时和普通构建不得调用 AI。

### Phase 2：前端改为 JSON lazy loading

- `vue-i18n` 从 `/i18n/{locale}.json` 加载。
- 停止维护 `en_US.ts` / `zh_CN.ts`。
- 保证语言切换后系统文案刷新。

### Phase 3：后端改为 JSON I18nService

- 后端读取 `resources/i18n/{locale}.json`。
- 第一阶段先让 `MessageUtils.message(...)` 底层兼容 JSON。
- 后续再抽 `I18nService` 给字典、错误、邮件模板统一使用。
- 最终停止维护 `messages_*.properties`。

### Phase 4：字典翻译迁移

- 后端按 `dict.<dictType>.<value>` 自动生成 key。
- JSON 补齐所有启用字典项。
- 废弃 `sys_dict_data.i18n_key`，相关代码和页面不再把它作为翻译来源。
- 旧 label 字段可作为管理识别/fallback，是否物理删除在 `/plan` 中基于代码引用和 SQL 影响决定。

### Phase 5：国家/币种/语言独立标准数据

- 国家、币种、语言迁出普通字典。
- 建立独立 API 和下拉搜索能力。
- 不生成 `dict.country.*` 这类 key。
- 本阶段并入第一波，不再后置。

### Phase 6：废弃 `sys_i18n_message`

- 系统文案迁移到 JSON。
- 字典翻译迁移到 `dict.*` JSON key。
- `sys_i18n_message` 中现有编码和内容迁移到 JSON。
- 业务数据不迁移。
- 相关运行时读取、seed 和管理入口同步修改。
- `sys_i18n_message` 彻底废弃；是否物理删表/移出 ignore tables/清 SQL seed 在 `/plan` 中按引用分析和数据库变更顺序执行。

## Risks：风险

- 这是跨前端、后端、字典、菜单、数据库 seed、构建流程的架构级改造，需要分阶段实施。
- 前端菜单、面包屑、标签页如果当前依赖后端返回已翻译 title，需要改成响应 locale 的客户端翻译或重新加载策略。
- 后端从 properties 切 JSON 会影响所有 `MessageUtils.message(...)` 调用。
- 字典 label 由后端按 JSON 解析后返回，会影响字典缓存 key、公开字典接口、字典管理页。
- `sys_i18n_message` 彻底废弃牵涉 SQL baseline、实体/Mapper/Service、菜单/字典翻译、生成器 SQL seed 和缓存逻辑，必须先做引用清单和迁移顺序。
- 国家/币种/语言并入第一波会扩大数据库、API、前端下拉和 seed 范围，需要作为独立子任务验收。
- AI 翻译脚本需要安全处理 API key，不得提交密钥；运行时不得依赖 AI。

## Confirmed Decisions：已确认决策

- 允许新增根目录 `i18n/` 和 pnpm 脚本。
- 后端第一阶段先让 `MessageUtils.message(...)` 底层兼容 JSON，再抽 `I18nService` 给字典、错误、邮件模板统一使用。
- 废弃字典表 `i18n_key` 翻译来源，字典直接走 JSON 翻译。
- `sys_i18n_message` 彻底废弃，现有编码迁移到 JSON。
- 国家、币种、语言并入第一波。
- 涉及到的具体功能和引用代码都需要同步修改。
- 允许修改数据库。
- 验证阶段允许执行 build/typecheck/test 等命令。
- baseline/code 完整移除 `sys_i18n_message` 和 `sys_dict_data.i18n_key` 翻译来源；DEV DB 实际 drop 必须在 JSON 抽取和运行时验证后执行。
- `i18n:translate` 接入 DeepSeek `deepseek-v4-pro`，API key 用新建 `.env` 占位保存，用户后续补充。
- 国家、币种、语言 seed 来源使用 ISO/CLDR 静态数据；普通字典中的现有国家数据删除。

## Open Questions：待确认问题

- No open business questions. `/plan` has resolved destructive cleanup staging, AI provider, and standard data seed source.

## Acceptance Criteria：验收标准

- 新增文案只需要写 `i18n/locales/en_US.json`。
- `zh_CN.json` 可由 AI 补全，且人工翻译不会被覆盖。
- `i18n:validate` 能发现缺失 key、空 value、占位符不一致、字典覆盖缺失。
- `i18n:sync` 能同步前端和后端静态 JSON。
- 前端运行时从 JSON 加载语言包，不再依赖 TS locale。
- 后端运行时从 JSON 解析参数化消息，不再依赖手写 properties。
- 字典返回 label 来自 JSON `dict.*` key，表内不再手写 `i18n_key`。
- 国家/币种/语言不再作为普通字典翻译项处理。
- `sys_i18n_message` 现有 key/value 已迁移到 JSON，运行时不再依赖该表。
- 国家/币种/语言第一波具备标准数据表/API/前端搜索下拉能力。

## Related Decisions：相关决策

- 前端仍使用 `vue-i18n`。
- AI 只用于构建/开发期翻译补全，不参与运行时。
- `sys_i18n_message` 彻底废弃，现有内容迁移到 JSON。
- 业务数据原样存储，不迁移到 i18n。
