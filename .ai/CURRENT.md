# Current AI Context

## Current Technical Rules

- Default response language is Chinese; code, variable names, and method names stay English.
- Keep minimal diff. Do not refactor, upgrade dependencies, modify database structure, or touch migration files unless explicitly requested.
- Do not output secrets, tokens, passwords, connection strings, or other sensitive data.
- PostgreSQL is the active database. MySQL SQL is legacy and should not receive new work.
- Frontend visible text must use `vue-i18n` keys from `admin-ui/src/locales/zh_CN.ts` and `admin-ui/src/locales/en_US.ts`.
- Frontend language switching must use `admin-ui/src/stores/locale.ts`; request code must keep sending `Content-Language`.
- Backend user-visible messages must use MessageSource through `MessageUtils.message(...)`, Bean Validation message keys, or supported exception message keys.
- Menu and dictionary labels should use backend `i18n_key` / `sys_i18n_message` when they are database-driven.
- Business/audit/API time is UTC-semantic. Use `TimeUtils.utcNow()` for backend business "now"; do not use `LocalDateTime.now()` for persisted business time.
- MyBatis `LocalDateTime` fields should use `UtcLocalDateTimeTypeHandler` for PostgreSQL `timestamptz`.
- API absolute timestamps use ISO-8601 UTC `Z` strings. Direct Spring MVC `LocalDateTime` query/form binding expects ISO instant UTC strings.
- Frontend backend-time display should use `formatUtc()`; absolute time payloads should use `toUtcPayload()`, `withUtcDateRange()`, or `withUtcDateRangeParams()`.
- `DateUtils` remaining default-timezone helpers are legacy/local helpers only, not for persisted business/API time.
- Generator templates should emit UTC-aware Java/Vue date handling and i18n-aware Vue labels/messages.
- Scheduler/cron changes must first confirm explicit timezone behavior. Current project notes identify XXL-JOB as the main external entry and no local `@Scheduled` jobs.

## Current Architecture State

- Backend: Java 17, Spring Boot 3.2.9, Maven multi-module.
- Frontend: Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router, Axios, `vue-i18n`, dayjs.
- Backend i18n files: `bocoo-admin/src/main/resources/i18n/messages*.properties`.
- Backend locale resolver: `bocoo-common/bocoo-common-web/.../I18nLocaleResolver.java`, reading `content-language`.
- Frontend i18n entry: `admin-ui/src/i18n/index.ts`.
- Frontend locale files: `admin-ui/src/locales/zh_CN.ts` and `admin-ui/src/locales/en_US.ts`.
- `ServiceException` supports `messageKey + args` while preserving legacy plain message constructors.
- `GlobalExceptionHandler` returns stable i18n messages for common framework/system exceptions.
- `R.ok()` / `R.fail()` default response messages resolve through backend message keys.
- PostgreSQL schema uses `timestamptz` for time columns with UTC semantics.
- `TimeUtils`, `UtcLocalDateTimeTypeHandler`, and `InjectionMetaObjectHandler` form the backend UTC timestamp baseline.
- `JacksonConfig` and `RedisConfig` use explicit UTC for `LocalDateTime` serialization/deserialization.
- Global Spring MVC formatting for direct `LocalDateTime` query/form fields is registered in `ResourcesConfig`.
- Current searched frontend timestamp display points use `formatUtc()`; legacy `parseTime()` remains as compatibility utility.
- Generator templates have been updated to avoid generated `@JsonFormat`, `java.util.Date`, `parseTime()`, and raw timestamp display for generated date fields.

## Current Development Stage

- UTC modernization, i18n modernization, and generator template migration have completed their main implementation phases.
- Historical phase logs have been archived under `.ai/archive/`.
- The current stage is follow-up hardening and verification, not broad feature work.
- Per the 2026-05-25 context compaction request, no business code was modified and no build/test was run during compaction.

## Current Remaining TODO

- Decide a parameterized-message strategy for dynamic backend response/error messages, import summaries, upstream messages, and file-extension lists.
- Migrate remaining user-visible `ServiceException` / `BaseException` pass-through paths only after the dynamic-message strategy is settled.
- Classify generator Swagger/OpenAPI, `@Log(title)`, `@ExcelProperty`, and SQL fallback labels as user-visible or developer metadata before further i18n changes.
- Confirm how generated frontend page i18n keys seeded into `sys_i18n_message` are loaded by the frontend runtime, or add a loader/template follow-up.
- If complete module generation is expected to include business table DDL, add/verify PostgreSQL `timestamptz` output in generator SQL.
- Update external API docs and frontend type expectations for the ISO-8601 UTC `Z` timestamp contract.
- With explicit approval, rerun final verification and generator sample regeneration.
- Resolve the previous PowerShell/Maven classpath command parsing issue before retrying generator sample regeneration.
