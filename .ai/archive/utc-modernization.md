# UTC Modernization Archive

## Purpose

This archive preserves the migration history that was removed from active context during AI Context Compaction on 2026-05-25.

## Completed Modernization Summary

- PostgreSQL was confirmed as the active database direction; MySQL SQL is legacy and no longer a target for new UTC work.
- PostgreSQL initialization uses `timestamptz` for business time columns and documents UTC storage semantics.
- DEV PostgreSQL connections use `SET TIME ZONE 'UTC'`.
- Backend business/audit timestamps standardized on UTC-semantic `LocalDateTime`.
- `TimeUtils.utcNow()` became the canonical backend business "now" source.
- MyBatis `LocalDateTime` timestamp conversion uses `UtcLocalDateTimeTypeHandler`.
- `InjectionMetaObjectHandler` writes audit `createTime` / `updateTime` with UTC semantics.
- `JacksonConfig` and `RedisConfig` were changed away from system default timezone and now serialize/deserialize `LocalDateTime` using UTC ISO instant helpers.
- Field-level `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` overrides were removed from scoped current API timestamp fields.
- Spring MVC direct `LocalDateTime` query/form binding gained a global ISO instant UTC formatter in `ResourcesConfig`.
- Frontend timestamp display/query paths were audited; searched API timestamp display points now use `formatUtc()`, and date range submission uses UTC helpers.
- Upload/OSS folder date generation now uses explicit UTC day through `DateUtils.datePath()`.

## Key Field Migrations

- `SysUser.loginDate`, `SysUserVo.loginDate`, `SysUserBo.loginDate`, and `SysUserExportVo.loginDate` were migrated from `java.util.Date` to `LocalDateTime`.
- `SysLoginService.recordLoginInfo()` now writes `TimeUtils.utcNow()`.
- `SysRoleVo.createTime` and `SysOssVo.createTime` were migrated to `LocalDateTime`.
- `OperLogEvent.operTime` was retained and migrated to `LocalDateTime`.
- `SysUserMapper.xml`, `SysRoleMapper.xml`, and `SysOssMapper.xml` result mappings use `UtcLocalDateTimeTypeHandler` for relevant timestamp fields.

## Caller Classification

- `DateUtils` default-timezone methods remain as documented legacy/local helpers and must not be used for persisted business/API time.
- `Seq.dateTimeNow()` remains unchanged because it is classified as an internal sequence/file-name uniqueness prefix, not a business date contract.
- JavaMail sent date and third-party OSS expiration calls retain `java.util.Date` because those APIs require `Date`.
- Frontend `new Date(...)` in picker defaults and `Date.now()` for cache-busting/export filenames remain UI/identifier usage, not business time.

## Contract Decisions

- API absolute timestamps use ISO-8601 UTC strings with `Z`, for example `2026-05-25T08:30:00Z`.
- Query date ranges should submit UTC `Z` strings through `params[beginTime]` / `params[endTime]` or equivalent range helpers.
- Direct query-object `LocalDateTime` fields are supported only with ISO instant UTC strings with `Z`; legacy no-zone query strings are not supported by the formatter.
- Frontend display converts backend UTC timestamps to the browser's local timezone.

## Historical Verification Notes

- Static audits were run across backend and frontend timestamp usage during the modernization phases.
- Earlier final verification reported backend compile, frontend typecheck, and frontend build passing before a later generator sample regeneration command failed.
- The failed generator helper command appeared to be a PowerShell/Maven argument parsing issue around `-Dmdep.outputFile=.ai\generator-classpath.txt`, not a Java compile or frontend build failure.

## Residual Items Moved To Active Context

- API documentation/type expectations still need to be updated for ISO-8601 UTC `Z`.
- Final verification and generator sample regeneration still need explicit approval before running.
- The Maven classpath command parsing issue must be resolved before retrying sample regeneration.
