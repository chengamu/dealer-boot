# Decisions

## Scope Assumption

- Project is not yet live.
- No compatibility is required for old production data, Redis cache, API date format, or i18n keys unless the user later says otherwise.
- PostgreSQL is the primary database. MySQL SQL is legacy and should not drive new work.

## Unified i18n Strategy

- Frontend visible text must use `vue-i18n` keys from `admin-ui/src/locales/zh_CN.ts` and `admin-ui/src/locales/en_US.ts`.
- Frontend key naming should stay grouped and semantic, for example `common.search`, `language.zhCN`, `dashboard.activeOrders`, and `permission.missingRoleValue`.
- Language switching must go through the locale store and continue sending `Content-Language`.
- Database-driven menu and dictionary labels should use `i18n_key` plus `sys_i18n_message`.
- Backend API response messages and user-facing errors should resolve through MessageSource keys.
- `ServiceException` supports i18n keys and args; plain string constructors are legacy/internal compatibility paths.
- `BaseException` keeps the `code + args` message model; migrate callers to keys only in scoped follow-up work.
- `GlobalExceptionHandler` should not return raw framework exception text to users.

## Unified UTC Strategy

- Database stores absolute business time as PostgreSQL `timestamptz`.
- Backend business, audit, login, operation log, and API timestamp fields should use `LocalDateTime` with UTC semantics.
- `TimeUtils.utcNow()` is the canonical backend business "now" source.
- `DateUtils` must not be used for persisted business timestamps.
- `DateUtils.datePath()` is retained for upload/OSS file-path grouping and uses explicit UTC day.
- `JacksonConfig` and `RedisConfig` must keep explicit UTC behavior.
- API timestamps use ISO-8601 UTC strings with `Z`.
- Direct Spring MVC query-object `LocalDateTime` binding accepts ISO instant UTC strings with `Z`.
- Frontend parses API timestamps as UTC and displays them in the browser's local timezone.
- Frontend date-range submission should use `toUtcPayload()`, `withUtcDateRange()`, or `withUtcDateRangeParams()`.

## Date Type Decision

- Use `LocalDateTime` for current backend entity/VO/BO/export timestamp fields, with UTC semantics.
- Keep `java.util.Date` only where third-party/protocol APIs require it, such as JavaMail sent date and OSS SDK expiration.
- Keep UI-only frontend `new Date(...)` picker defaults and identifier/cache-busting `Date.now()` usage where they are not business time.
- Keep `Seq` date prefix unchanged unless it becomes a business/API time contract.

## API Date Format Decision

- Preferred and current target is ISO-8601 UTC `Z` for absolute timestamps.
- Legacy timezone-less timestamp strings are not accepted for direct `LocalDateTime` query/form binding.
- Existing frontend compatibility parsing can remain during migration, but new/generated code should use UTC helpers.

## Code Generator Decision

- Java templates should generate `LocalDateTime` for date/time columns.
- Java templates should not generate legacy `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` for date/time fields.
- Vue templates should generate i18n-aware labels, placeholders, buttons, validation, confirmations, and messages.
- Vue templates should use `formatUtc()` for timestamp display and UTC range helpers for date range queries.
- Generated SQL should stay PostgreSQL-oriented and include `sys_i18n_message` rows where backend/database-driven translation is required.
