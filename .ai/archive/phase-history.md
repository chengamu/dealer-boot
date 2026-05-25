# Historical Phase Archive

## Purpose

This archive keeps the high-level chronological record removed from active `.ai` docs during AI Context Compaction on 2026-05-25.

## Completed Phases

- Phase 1: created AI collaboration docs and completed low-risk frontend i18n cleanup for permission directives and weekday labels.
- Phase 2: investigated UTC/timezone risks, including default timezone usage, Java `Date`, PostgreSQL `timestamptz`, deployment timezone settings, and frontend display behavior.
- Phase 3: fixed explicit UTC serialization baseline in `JacksonConfig` and `RedisConfig`.
- Phase 4 Step 1: migrated shared frontend language labels, dashboard visible text, and 404 language labels to i18n keys.
- Phase 4 Step 2A: added backend exception i18n model support for `ServiceException` and common framework/system handlers.
- Phase 4 Step 2B: migrated core user/role/dept `ServiceException` messages to backend i18n keys.
- Phase 4 Step 3A: migrated user login date fields and login recording to UTC-semantic `LocalDateTime`.
- Phase 4 Step 3B: migrated scoped business/API `Date` fields to UTC-semantic `LocalDateTime`.
- Phase 4 Step 4: added UTC helpers in `TimeUtils` and documented `DateUtils` legacy/local boundaries.
- Phase 4 Step 5A: moved API `LocalDateTime` JSON baseline to ISO-8601 UTC `Z`.
- Phase 4 Step 5B: removed scoped field-level legacy `@JsonFormat` overrides and aligned Redis `LocalDateTime` serialization.
- Phase 4 Step 5C: investigated direct `LocalDateTime` query binding and documented API/query date strategy.
- Phase 4 Step 6A: audited frontend UTC display/query usage and moved searched API timestamp displays to `formatUtc()`.
- Phase 4 Step 7A: synchronized generator Java/Vue/SQL templates for UTC and i18n baselines.
- Phase 4 Step 8B: generated isolated sample output and statically validated generator UTC/i18n behavior.
- Phase 4 Step 9A: completed i18n tail cleanup for navbar language labels, validation audit, and low-risk service exception migration/classification.
- Phase 4 Step 9B: completed UTC tail cleanup for direct query-object binding, upload/OSS UTC date paths, and sequence prefix classification.
- Phase 4 Step 9C: completed low-risk cleanup for navbar i18n recheck, unused import cleanup, generator comments, and validation audit.
- Phase 4 Step 10A: migrated user-facing Bean Validation and remaining user-visible system `ServiceException` messages.
- Phase 4 Step 10B: audited and migrated fixed user-facing API response messages to backend i18n keys.

## Last Pre-Compaction Handoff

- Step 11 Final Verification had started and stopped at generator sample regeneration.
- Previously reported before that stop: backend compile, frontend typecheck, and frontend build passed.
- Generator sample regeneration did not complete because the helper Maven classpath command was parsed incorrectly by PowerShell/Maven.
- No automatic retry was attempted after that failure.

## Compaction Result

- Active context was reduced to current rules, current architecture, current stage, current TODO, active risks, and recent handoff only.
- Historical migration detail now lives in `.ai/archive/utc-modernization.md`, `.ai/archive/i18n-modernization.md`, `.ai/archive/generator-migration.md`, and this file.
