# i18n Validation - 2026-05-29

## Static / Build

- `pnpm i18n:validate`: passed, 1296 keys across `en_US` / `zh_CN`, 10 dict keys covered.
- `pnpm i18n:sync`: passed earlier in T011 and synced runtime JSON resources.
- `cd admin-ui; pnpm typecheck`: passed.
- `cd admin-ui; pnpm build`: passed.
- `mvn -DskipTests compile`: passed earlier in T011.
- `mvn -pl bocoo-admin -am -DskipTests package`: passed earlier in T011.
- `mvn -DskipTests install`: passed earlier in T011 and was needed before module `spring-boot:run`.
- `codegraph sync`: passed earlier in T011.

## Runtime / API

- Backend started from `bocoo-admin` with `mvn spring-boot:run -DskipTests` after local Maven install.
- Startup log confirmed JSON i18n locale loading for `en_US` and `zh_CN`.
- DEV PostgreSQL schema/seed synced after JSON/runtime validation:
  - `sys_i18n_message` dropped.
  - `sys_dict_data.i18n_key` dropped.
  - ordinary `sys_country` dict data removed.
  - `sys_country` count: 249.
  - `sys_currency` count: 162.
  - `sys_language` count: 2.
- `/system/standard/countries?keyword=China` with `Content-Language=en_US`: HTTP/API 200, returned localized country rows.
- `/system/standard/countries?keyword=中国` with `Content-Language=zh_CN`: HTTP/API 200, returned localized country rows.
- `/login` with real captcha value from Redis: API 200.
- `/getInfo` with returned token: API 200, `tenantId=1`, `tenantType=PLATFORM`.
- `/getRouters` with returned token: API 200.

## Browser

- Public merchant apply page loaded through `http://127.0.0.1:8083/merchant/apply`.
- Browser requested `/i18n/en_US.json`, `/i18n/zh_CN.json`, and `/system/standard/countries` successfully.
- Country select fuzzy search with `China` displayed matching country options.
- Authenticated browser validation used a real login token stored as `Admin-Token-MES` cookie, without printing the token.
- `/index` loaded for both `en_US` and `zh_CN`.
- `/system/post` loaded for both `en_US` and `zh_CN`.
- After fixing `getMessage()` nested lookup, nav/breadcrumb/search labels rendered as:
  - `en_US`: `Base`, `Search`, `English`.
  - `zh_CN`: `基础`, `搜索`, `中文`.
- No browser console errors or failed requests were observed in the Playwright validation run.

## Fixed During Check

- `getMessage()` originally returned raw keys such as `dashboard.base`, `common.search`, `language.enUS` because `vue-i18n` `flatJson` normalized part of the JSON message tree into nested objects. `admin-ui/src/locales/index.ts` now resolves both flat keys and nested paths.

## Remaining Risks / Not Verified

- `i18n:translate` DeepSeek call was not executed because `DEEPSEEK_API_KEY` is intentionally not configured yet; script gating is present.
- Running `java -jar bocoo-admin/target/bocoo-admin.jar` failed with no main manifest; runtime validation used Maven `spring-boot:run` after `mvn install`.
- `/thirdLogin` is `@SaIgnore` but not in tenant ignore URLs, so it currently fails with `Tenant id is required` when used as an auth shortcut. Normal `/login` was verified successfully. This is outside the i18n modernization scope but should be handled in the auth/tenant follow-up.
