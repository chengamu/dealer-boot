# i18n Inventory - 2026-05-29

## Source Counts

- Frontend TS locale:
  - `admin-ui/src/locales/en_US.ts`: 989 flattened keys.
  - `admin-ui/src/locales/zh_CN.ts`: 989 flattened keys.
  - Key parity: no missing/extra keys found.
- Backend properties:
  - `bocoo-admin/src/main/resources/i18n/messages_en_US.properties`: 263 keys.
  - `bocoo-admin/src/main/resources/i18n/messages_zh_CN.properties`: 263 keys.
  - Key parity: no missing/extra keys found.
- PostgreSQL baseline:
  - `sys_i18n_message` seed contains about 297 unique message keys.
  - `sys_dict_data` seed scan found 259 relevant rows.
  - `sys_country` ordinary dictionary rows: 249; these move to standard country seed, not `dict.*` JSON.

## Main Consumers

- `MessageUtils.message(...)`: used across 21 backend files in common/system modules.
- DB i18n runtime:
  - `SysI18nMessage`
  - `SysI18nMessageMapper`
  - `SysI18nMessageService`
  - `SysMenuService`
  - `SysDictDataService`
- `i18nKey` / `i18n_key` references:
  - admin APIs/types/pages/helpers for menu and dict.
  - system BO/entity/VO for menu and dict.
  - `SysTenantApplyService` menu creation.
  - `SysMenuMapper.xml`.
  - generator SQL template.
  - PostgreSQL baseline.
- Country dictionary usage:
  - `admin-ui/src/pages/auth/MerchantApplyPage.vue`
  - `SysDictDataController` public `sys_country` whitelist.
  - `sql/postgresql/base.sql` country dict seed.

## Extraction Order

1. Frontend TS locale -> `i18n/locales/*.json`.
2. Backend properties -> same JSON, preserving Java placeholders such as `{0}` and Bean Validation placeholders such as `{min}`.
3. `sys_i18n_message` seed -> JSON.
4. `sys.dict.*` keys -> convert to `dict.<dictType>.<value>` where they represent ordinary system dictionaries.
5. `sys_country` dictionary rows -> standard country seed, not dictionary JSON.
6. Generator template i18n keys -> JSON/generator extraction strategy.

## Implementation Guard

- Do not remove old DB structures until JSON extraction and runtime message/dict/menu paths are validated.
- `sys_menu.i18n_key` can remain as the menu JSON key field; the removed dependency is the DB message table.
- `sys_dict_data.i18n_key` is removed as a translation source; `dict_label` may remain as admin/fallback text unless SQL cleanup removes it later.
