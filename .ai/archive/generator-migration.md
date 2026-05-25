# Generator Migration Archive

## Purpose

This archive preserves generator migration details removed from active context during AI Context Compaction on 2026-05-25.

## Completed Generator UTC Work

- `VelocityUtils` was updated to use UTC metadata time through `TimeUtils`.
- Java templates now generate `LocalDateTime` for date/time columns instead of `java.util.Date`.
- Generated Java date/time fields no longer emit `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`.
- Vue templates now import and use `formatUtc()` for API datetime display.
- Vue templates now submit generated date range queries through UTC range helpers such as `withUtcDateRangeParams()`.

## Completed Generator i18n Work

- Vue templates generate `t(...)` calls and generated field/message keys for visible labels, placeholders, buttons, dialog titles, validation, confirmations, and success/error messages.
- SQL template remains PostgreSQL-oriented and seeds `sys_i18n_message` rows for generated menu/action/page/field keys.
- Generated source comments in allowed Java/JS/Vue templates were cleaned where safe.
- Stale generated status method comments that referenced `设备分组` were replaced with `${functionName}` wording.

## Sample Generation History

- An isolated sample module was generated under `.ai/generated/sample` for validation without modifying core business modules.
- The sample output included Java controller/service/mapper/domain/BO/VO, mapper XML, Vue API/page, and SQL menu/i18n seed output.
- Static audit of the generated sample found no generated `@JsonFormat`, `java.util.Date`, `parseTime()`, or old `yyyy-MM-dd HH:mm:ss` timestamp contract.
- Static audit confirmed generated output uses `LocalDateTime`, `formatUtc()`, `withUtcDateRangeParams()`, and `sys_i18n_message` seed rows.
- Generated SQL did not include business table DDL, so generated `timestamptz` business columns could not be validated from `sql.vm`.

## Deferred Generator Items

- Confirm how generated frontend page i18n keys seeded into backend `sys_i18n_message` are loaded by the frontend runtime.
- If complete module generation should emit business DDL, add or verify PostgreSQL `timestamptz` output.
- Classify generated Swagger/OpenAPI annotations, `@Log(title)`, `@ExcelProperty`, SQL fallback labels, and source comments as user-visible or developer metadata before further i18n work.
- Retry final generator sample regeneration only after resolving the PowerShell/Maven classpath argument issue and receiving explicit approval to run verification commands.
