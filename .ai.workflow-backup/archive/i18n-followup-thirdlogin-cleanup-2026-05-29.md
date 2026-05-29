# Archive: i18n follow-up, thirdLogin, deprecated cleanup

## Feature：功能

Single-source i18n follow-up validation, `/thirdLogin` platform tenant handling, and removal of deprecated i18n leftovers.

## Requirement Source：需求来源

- User confirmed `.env` already contains `DEEPSEEK_API_KEY`.
- User clarified backend runtime should use `bocoo-admin/target/dist`.
- User requested keeping `/thirdLogin` but adapting it to tenant login rules as a platform-level login path.
- User requested cleaning deprecated code and files after the local changes.

## Final Status：最终状态

Accepted with Risks.

## Scope：范围

- Validate `i18n:translate` after the DeepSeek key was configured, without printing secrets.
- Record and use the distribution runtime convention: `bocoo-admin/target/dist`.
- Keep `/thirdLogin`, but make it a controlled platform-tenant login path.
- Remove deprecated dictionary `i18nKey` residue and keep old i18n replacement files deleted.
- Keep menu `i18nKey` because it is the current JSON key/fallback design.

## Out of Scope：不做范围

- Full OAuth/SSO redesign for `/thirdLogin`.
- Refactor of `/smsLogin`, `/emailLogin`, or `/xcxLogin`.
- Real DeepSeek provider call with an artificial missing key.
- Broad cleanup of unrelated working tree changes, logs, IDE files, or local runtime artifacts.

## Completed：已完成

- `pnpm.cmd i18n:translate` executed successfully; no missing `zh_CN` keys existed, so DeepSeek provider call was skipped.
- `bocoo-admin/target/dist/bocoo-admin.jar` rebuilt and used for runtime validation.
- `/thirdLogin` added to tenant ignore URLs.
- `SysLoginService.thirdLogin(...)` now runs user lookup, password validation, login user build, and login info update under configured platform tenant id.
- Runtime API `/thirdLogin` returned `code=200` with token present.
- Runtime API `/getInfo` with the returned token reported `tenantId=1` and `tenantType=PLATFORM`.
- Removed dictionary API/frontend `i18nKey` residue:
  - `SysDictDataVo` no longer exposes dict `i18nKey`.
  - `SysDictDataService` still builds JSON key internally but does not return it.
  - frontend dict API/type/helper no longer carries dict `i18nKey`.
- Removed unused `legacy.i18nKey` JSON entries and synced runtime JSON.
- Confirmed old frontend TS locale, backend properties, and `SysI18nMessage` entity/mapper/service remain deleted.

## Not Completed：未完成

- DeepSeek provider HTTP request was not exercised because there were no missing translations.
- Other ignored login endpoints were not changed in this scope.

## Validation Summary：验证摘要

- `pnpm.cmd i18n:translate`: passed; no missing `zh_CN` keys.
- `mvn -pl bocoo-admin -am -DskipTests "-Dmaven.compiler.useIncrementalCompilation=false" package`: passed.
- Runtime from `D:\work\base-boot\bocoo-admin\target\dist`: started on port `8081`.
- `/thirdLogin`: returned `code=200`, token present.
- `/getInfo`: returned `tenantId=1`, `tenantType=PLATFORM`.
- `pnpm.cmd i18n:sync`: passed.
- `pnpm.cmd i18n:validate`: passed, 1294 keys valid.
- `pnpm.cmd --dir admin-ui typecheck`: passed.
- `mvn -pl bocoo-modules-system -am -DskipTests compile`: passed.

## Remaining Risks：剩余风险

- DeepSeek provider request path still needs validation on a real missing-key change.
- `/smsLogin`, `/emailLogin`, and `/xcxLogin` can hit the same tenant-context problem if they query tenant-scoped user data before login tenant context exists.
- `/thirdLogin` is still an appKey/secretKey password-style login endpoint, not a full OAuth/SSO protocol.
- Remaining `sys_i18n_message` mentions are documentation warnings, not runtime dependencies.

## Lessons from Learn：经验提炼

- For PowerShell Maven system properties with dots, wrap the `-D...` argument in quotes, for example `"-Dmaven.compiler.useIncrementalCompilation=false"`.
- Use `bocoo-admin/target/dist` as the backend runtime validation directory after packaging.
- Dict labels should be translated server-side from JSON and returned as labels; do not keep exposing dictionary i18n keys to page code.
- Menu `i18nKey` is intentionally retained as a JSON key/fallback and should not be confused with deprecated dictionary `i18n_key`.

## Key Decisions：关键决策

- `/thirdLogin` is retained and treated as platform-level login by default.
- Dictionary runtime API does not expose `i18nKey`; menu APIs still expose `i18nKey`.
- Old TS locale, old properties, and `SysI18nMessage*` files are deprecated and should remain deleted.

## Files Modified：修改文件

- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/MEMORY.md`
- `.ai/HANDOFF.md`
- `bocoo-admin/src/main/resources/application.yml`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLoginService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysDictDataService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/domain/vo/SysDictDataVo.java`
- `admin-ui/src/api/system/dict/data.ts`
- `admin-ui/src/types/api.ts`
- `admin-ui/src/utils/dict.ts`
- `i18n/locales/en_US.json`
- `i18n/locales/zh_CN.json`
- `admin-ui/public/i18n/en_US.json`
- `admin-ui/public/i18n/zh_CN.json`
- `bocoo-admin/src/main/resources/i18n/en_US.json`
- `bocoo-admin/src/main/resources/i18n/zh_CN.json`
- Deleted legacy files remain deleted:
  - `admin-ui/src/locales/en_US.ts`
  - `admin-ui/src/locales/zh_CN.ts`
  - `bocoo-admin/src/main/resources/i18n/messages*.properties`
  - `bocoo-modules-system/src/main/java/com/bocoo/system/domain/entity/SysI18nMessage.java`
  - `bocoo-modules-system/src/main/java/com/bocoo/system/mapper/SysI18nMessageMapper.java`
  - `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysI18nMessageService.java`

## Artifacts：产物

- Runtime jar: `bocoo-admin/target/dist/bocoo-admin.jar`
- Prior validation report: `.ai/artifacts/i18n-validation-20260529.md`

## Follow-up：后续事项

- Validate DeepSeek provider request path when a real missing translation appears.
- Review `/smsLogin`, `/emailLogin`, and `/xcxLogin` before exposing or reusing them under tenant-aware login flows.
