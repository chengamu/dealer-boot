# 决策

## 2026-05-29 - i18n single-source JSON target

Context：当前 i18n 同时维护前端 TS locale、后端 properties、DB `sys_i18n_message`、字典 `i18n_key`，新增文案和语言切换维护成本高。
Decision：目标架构改为单源 JSON：`i18n/locales/en_US.json` 为唯一人工主语言，`zh_CN.json` 由构建期 AI 补全并人工校对；前端和后端运行时读取同步后的静态 JSON；AI 不参与运行时。
Reason：减少重复维护，保证前后端文案一致，降低 DB i18n seed/cache 复杂度。
Impact：后续需要分阶段迁移前端 locale、后端 MessageSource/properties、字典 label、菜单标题和 `sys_i18n_message` 依赖。
Related Requirement：`.ai/requirements/20260529-i18n-modernization.md`

## 2026-05-29 - i18n plan confirmations

Context：`/plan` identified three remaining implementation decisions for destructive DB cleanup, AI translate provider, and standard data seed source.
Decision：Use recommended baseline/code full-removal path for `sys_i18n_message` and `sys_dict_data.i18n_key`, but only execute DEV DB drop after JSON extraction/runtime validation. `i18n:translate` integrates DeepSeek `deepseek-v4-pro` and reads the API key from a new `.env` placeholder. Country/currency/language seeds use ISO/CLDR static data; existing country dictionary data is removed from ordinary dict seed.
Reason：Keeps the target clean while reducing data-loss risk during DEV sync; avoids runtime AI; separates standard data from dictionary translation.
Impact：`/do` must include `.env` handling without committing real secrets, ISO/CLDR seed generation/curation, and a staged DB cleanup gate.
Related Requirement：`.ai/requirements/20260529-i18n-modernization.md`

## 2026-05-29 - i18n first-wave scope

Context：用户确认 i18n 第一波需要一次性覆盖单源 JSON、后端兼容、字典翻译、国家/币种/语言标准数据，以及 DB i18n 废弃。
Decision：允许新增根目录 `i18n/` 和 pnpm 脚本；后端先让 `MessageUtils.message(...)` 底层兼容 JSON，再抽统一 `I18nService`；废弃字典表 `i18n_key` 翻译来源；`sys_i18n_message` 彻底废弃且现有编码迁移到 JSON；国家/币种/语言并入第一波；允许修改 PostgreSQL 并运行 build/typecheck/test 验证。
Reason：减少 TS/properties/DB 多源维护，统一前后端运行时资源，避免字典和标准数据继续混用。
Impact：这是架构级跨模块改造，`/plan` 必须先做引用清单、数据库变更顺序、脚本策略和回滚风险拆解。
Related Requirement：`.ai/requirements/20260529-i18n-modernization.md`
