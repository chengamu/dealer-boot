# i18n Modernization Archive

## Purpose

This archive preserves the migration history that was removed from active context during AI Context Compaction on 2026-05-25.

## Superseded By

As of 2026-05-29, the active i18n direction is single-source JSON under `i18n/locales/`, build-time AI completion, and frontend/backend runtime static JSON. The TS locale, properties, and `sys_i18n_message` model below is historical context, not the current target architecture.

## Completed Frontend i18n Work

- Frontend i18n was standardized on `vue-i18n`.
- Frontend i18n entry remains `admin-ui/src/i18n/index.ts`.
- Historical locale resources were `admin-ui/src/locales/zh_CN.ts` and `admin-ui/src/locales/en_US.ts`; current source is `i18n/locales/*.json`.
- Language state remains controlled by `admin-ui/src/stores/locale.ts`, with `Content-Language` sent by the request layer.
- Element Plus locale remains controlled by `admin-ui/src/App.vue`.
- Hardcoded frontend language labels were replaced with shared `language.*` keys in locale option paths, auth language selection, error page language controls, and navbar language dropdown.
- Dashboard visible labels, statuses, alert text, timeline text, and toast messages were moved into `dashboard.*` locale keys.
- Permission directive hardcoded error messages were moved into locale keys.
- Weekday hardcoded text in frontend utility code was moved into locale keys.

## Completed Backend i18n Work

- Historical Spring MessageSource used `bocoo-admin/src/main/resources/i18n/messages*.properties`; current runtime reads synced JSON resources.
- Backend locale is resolved from `content-language` through `I18nLocaleResolver`.
- `ServiceException` was extended to support `messageKey + args` through `ofMessageKey(...)` / `setMessageKey(...)` while preserving legacy plain-message constructors.
- `GlobalExceptionHandler` common framework/system handlers now return stable i18n messages rather than exposing raw framework exception text.
- Core user/role/dept user-visible `ServiceException` paths were migrated to backend message keys.
- Low-risk user-visible exceptions were migrated for merchant application audit, sort parameter validation, encrypted API access denial, and fixed OSS upload/download messages.
- System module user-facing Bean Validation annotation messages were migrated from literal Chinese to `{validation.*}` keys.
- Remaining low-risk system `ServiceException` tails in post, dict, config, and OSS config services were migrated to message keys.
- Default `R.ok()` and `R.fail()` messages resolve through `common.success` and `common.failed`.
- Fixed user-visible API response messages in selected handlers/controllers were migrated to MessageSource keys.
- Login, logout, upload, import/export, auth, profile, password, avatar, dept, and menu fixed response keys were added to all backend message bundles.

## Handler And Validation Decisions

- `GlobalExceptionHandler` and `DefaultExcelListener` validation/default-message behavior was kept stable.
- Bean Validation is wired to Spring MessageSource, so migrated `{validation.*}` annotations resolve through existing infrastructure.
- Legacy `ServiceException` plain-message constructors remain for compatibility/internal paths and should not be used for new user-visible messages.
- `BaseException` keeps its key/code model; caller migration should be scoped and deliberate.

## Deferred i18n Areas

- Dynamic response/error messages that include usernames, object names, file-extension arrays, import summaries, or upstream/external messages need a parameterized-message strategy before migration.
- Remaining legacy `ServiceException` / `BaseException` pass-through paths should be migrated only after that strategy is chosen.
- Generator validation annotations, Swagger/OpenAPI text, `@Log(title)`, `@ExcelProperty`, SQL fallback labels, and Java comments need classification before further changes.
- DB i18n editing is no longer the target path because `sys_i18n_message` is deprecated in favor of static JSON resources.

## Historical Verification Notes

- Static searches confirmed migrated keys existed in all three backend message bundles during prior phases.
- Duplicate-key checks were run during previous migration phases.
- Frontend toast sources were audited; request/download/upload helpers surface backend `msg`, so backend API response i18n affects visible toasts.
- Build/test was not run during several scoped i18n cleanup phases unless explicitly noted in the archived handoff history.
