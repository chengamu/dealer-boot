# Tasks

## Current Unfinished Tasks

- [ ] Decide a parameterized backend message strategy for dynamic user-visible text:
  import summaries, upstream/external messages, usernames/object names, file-extension arrays, and legacy pass-through exception text.
- [ ] Migrate remaining user-visible `ServiceException` / `BaseException` paths after the dynamic-message strategy is settled.
- [ ] Confirm whether generator Swagger/OpenAPI annotations, `@Log(title)`, `@ExcelProperty`, SQL fallback labels, and Java comments are user-visible i18n debt or developer metadata.
- [ ] Confirm how generated frontend page i18n keys seeded into `sys_i18n_message` are loaded by the frontend runtime, or add a generator/frontend locale-loading follow-up.
- [ ] If generator output must include complete business table DDL, add or verify PostgreSQL `timestamptz` output for generated timestamp columns.
- [ ] Update external API docs and frontend type expectations for ISO-8601 UTC `Z` timestamp payloads.
- [ ] Resolve the PowerShell/Maven classpath command parsing issue from the previous Step 11 generator sample regeneration attempt.
- [ ] After explicit approval, rerun final verification:
  backend compile, frontend typecheck/build, generator sample regeneration, and targeted static searches.

## Constraints For Next Work

- Do not modify business code unless the user explicitly asks for implementation work.
- Do not run build/test without pausing first and getting explicit approval.
- Keep changes scoped; do not re-expand archived historical logs back into active context.
