# Bugs / Risks

## Active i18n Risks

- Some backend user-visible text remains dynamic or pass-through and needs a parameterized-message strategy before migration.
- Legacy plain-message `ServiceException` constructors remain for compatibility/internal paths; new user-visible code should use message keys.
- `BaseException` caller coverage is not fully migrated; treat remaining literal/pass-through messages as scoped follow-up work.
- Generator Swagger/OpenAPI annotations, `@Log(title)`, `@ExcelProperty`, SQL fallback labels, and some metadata may still contain Chinese literals; classify before changing.
- `SysI18nMessageService` runtime cache invalidation/refresh behavior still needs confirmation if DB i18n messages are edited at runtime.

## Active UTC / API Date Risks

- API date contract is ISO-8601 UTC `Z`; external API docs and frontend type expectations still need to be updated.
- Direct Spring MVC query-object `LocalDateTime` binding expects ISO instant UTC strings with `Z`; legacy no-zone query strings are unsupported.
- `DateUtils` still contains default-timezone legacy/local helpers. They must not be used for persisted business/API time.
- `run.bat` and `docker-compose.yml` still force `Asia/Shanghai`; UTC correctness depends on keeping business paths off system-default timezone APIs.
- Sequence date prefixes remain classified as internal identifier behavior. Revisit only if they become user-visible or business-time semantics.

## Active Generator Risks

- Generated frontend i18n keys are seeded into backend `sys_i18n_message`; runtime loading by frontend generated pages still needs confirmation.
- Generated SQL currently does not emit business table DDL, so generated timestamp column `timestamptz` output is not validated.
- Final generator sample regeneration previously failed due to PowerShell/Maven argument parsing in the helper classpath command.

## Active Verification Risks

- No build/test was run during the 2026-05-25 context compaction.
- Final verification should be rerun only after explicit approval because project rules require pausing before build/test.

## Encoding Risk

- Some PowerShell output can show mojibake for Chinese content. Prefer `Get-Content -Raw -Encoding UTF8` and avoid bulk rewrites unless encoding is confirmed.
