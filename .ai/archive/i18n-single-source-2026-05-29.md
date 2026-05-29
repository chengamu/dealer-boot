# Archive: Single-source i18n modernization

## Feature

First-wave single-source i18n modernization.

## Requirement Source

- `.ai/requirements/20260529-i18n-modernization.md`
- User confirmations:
  - Add root `i18n/` and pnpm scripts.
  - Make `MessageUtils.message(...)` JSON-compatible first.
  - Deprecate dictionary `i18n_key` translation source.
  - Fully deprecate `sys_i18n_message`.
  - Include country/currency/language standard data in the first wave.
  - Use DeepSeek `deepseek-v4-pro` for build-time translation script, with `.env` key placeholder.
  - Use ISO/CLDR static data for standard country/currency/language seed.
  - PostgreSQL baseline/code removal is allowed; DEV DB destructive cleanup only after JSON/runtime validation.

## Final Status

Accepted with Risks.

## Scope

- Added single-source JSON locale files under `i18n/locales/`.
- Added i18n scripts for validate/sync/check/translate.
- Synced frontend runtime JSON to `admin-ui/public/i18n/`.
- Synced backend runtime JSON to `bocoo-admin/src/main/resources/i18n/`.
- Added backend JSON `I18nService` / `JsonMessageSource` compatibility.
- Migrated dictionary label resolution to generated JSON keys: `dict.<dictType>.<value>`.
- Removed runtime dependency on `sys_i18n_message`.
- Added standard country/currency/language tables, seed, backend APIs, and merchant apply country consumer.
- Updated generator SQL template to stop emitting `sys_i18n_message`.
- Updated PostgreSQL baseline and active docs/MD i18n principles.

## Out of Scope

- Runtime AI translation.
- Online i18n management UI.
- External SaaS i18n platform.
- Redis translation cache.
- Tenant-specific copy overrides.
- Broad unrelated UI redesign.
- MySQL SQL updates.

## Completed

- T001 through T011 are done.
- DEV DB was synced after JSON/runtime validation:
  - removed `sys_i18n_message`;
  - removed `sys_dict_data.i18n_key`;
  - removed ordinary country dict data;
  - seeded `sys_country`, `sys_currency`, `sys_language`.
- Fixed validation failure where frontend `getMessage()` returned raw keys after `vue-i18n flatJson` normalized part of the message tree.

## Not Completed

- Real DeepSeek translation call was not executed because `DEEPSEEK_API_KEY` is not configured.
- Direct `java -jar bocoo-admin/target/bocoo-admin.jar` runtime was not validated because the jar has no main manifest.
- `/thirdLogin` tenant-ignore issue was not fixed because it is outside this i18n modernization scope.

## Validation Summary

Detailed validation is in `.ai/artifacts/i18n-validation-20260529.md`.

Passed:

- `pnpm i18n:validate`
- `pnpm i18n:sync`
- `cd admin-ui; pnpm typecheck`
- `cd admin-ui; pnpm build`
- `mvn -DskipTests compile`
- `mvn -pl bocoo-admin -am -DskipTests package`
- `mvn -DskipTests install`
- `codegraph sync`
- Runtime/API: `/system/standard/countries`, `/login`, `/getInfo`, `/getRouters`
- Browser: public merchant apply, authenticated `/index`, authenticated `/system/post`, `en_US` and `zh_CN` navigation labels

## Remaining Risks

- `i18n:translate` DeepSeek path is only statically implemented and gated; it still needs a configured `DEEPSEEK_API_KEY` for real provider validation.
- Maven `spring-boot:run` works after `mvn install`; direct module jar execution with `java -jar bocoo-admin/target/bocoo-admin.jar` does not work because the jar lacks a main manifest.
- `/thirdLogin` is `@SaIgnore` but not in tenant ignore URLs, so it fails with `Tenant id is required`; normal `/login` was verified.
- Existing unrelated working tree changes and generated runtime logs remain outside this archive.

## Lessons from Learn

- When `vue-i18n` uses `flatJson`, local helper lookups must support both flat keys and nested path traversal.
- Runtime validation from Maven modules may require `mvn install` first to avoid stale local artifacts.
- Auth endpoints and tenant ignore rules are separate concerns: `@SaIgnore` alone does not prevent tenant plugin failures.

## Key Decisions

- `i18n/locales/en_US.json` and `zh_CN.json` are the maintained source.
- Frontend and backend runtime JSON files are synced artifacts, not independent sources.
- System copy and dict labels live in JSON.
- Country/currency/language are standard data tables, not ordinary dictionaries.
- `sys_i18n_message` is deprecated and removed from active runtime/baseline.

## Files Modified

See git status for full list. Key areas:

- `i18n/**`
- `package.json`
- `.env.example`
- `admin-ui/src/i18n/index.ts`
- `admin-ui/src/locales/index.ts`
- `admin-ui/public/i18n/*.json`
- `admin-ui/src/api/system/standard.ts`
- `admin-ui/src/pages/auth/MerchantApplyPage.vue`
- `bocoo-common/bocoo-common-core/**/MessageUtils.java`
- `bocoo-common/bocoo-common-web/**/i18n/*`
- `bocoo-modules-system/**/SysDictData*`
- `bocoo-modules-system/**/SysMenuService*`
- `bocoo-modules-system/**/SysCountry*`, `SysCurrency*`, `SysLanguage*`
- `bocoo-modules-generator/src/main/resources/vm/sql/sql.vm`
- `sql/postgresql/base.sql`
- active `.ai` docs and project docs for i18n principles

## Artifacts

- `.ai/artifacts/i18n-inventory-20260529.md`
- `.ai/artifacts/i18n-validation-20260529.md`
- runtime logs under `.ai/artifacts/runtime-*20260529*.log`

## Follow-up

- Configure `.env` with `DEEPSEEK_API_KEY` and run real `pnpm i18n:translate` provider validation.
- Decide whether to make `bocoo-admin/target/bocoo-admin.jar` directly executable or standardize on the generated distribution jar / Maven runtime path.
- Fix tenant context behavior for `/thirdLogin` and related non-password login endpoints in a dedicated auth/tenant task.
