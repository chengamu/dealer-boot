# Frontend i18n and UTC Rules

Date: 2026-05-22

## Purpose

This document records the current frontend rules for internationalization and UTC time handling. It is the reference for future admin-ui changes so locale switching, dictionary text, backend messages, and date-time display stay consistent.

Note: if a task or discussion says `TUC`, treat it as `UTC` unless explicitly stated otherwise.

## Current Runtime Files

- Locale store: `admin-ui/src/stores/locale.ts`
- Vue i18n setup: `admin-ui/src/i18n/index.ts`
- App locale wrapper for Element Plus: `admin-ui/src/App.vue`
- Message tables: `admin-ui/src/locales/zh_CN.ts`, `admin-ui/src/locales/en_US.ts`
- Locale lookup helper: `admin-ui/src/locales/index.ts`
- Request locale header: `admin-ui/src/utils/request.ts`
- Dictionary locale bridge: `admin-ui/src/utils/dict.ts`
- UTC helpers: `admin-ui/src/utils/datetime.ts`
- Legacy-compatible global time helper: `admin-ui/src/utils/ruoyi.ts`
- 404 page: `admin-ui/src/pages/error/Error404Page.vue`

## i18n Rules

- All visible shell/page/component text must use explicit locale keys.
- Do not reintroduce DOM text patching or `legacy-ui-i18n.ts` style bridges.
- Do not hardcode Chinese UI text in migrated TS pages, including:
  - table headers
  - query labels
  - placeholders
  - dialogs
  - confirmation messages
  - toast messages
  - icon-only button labels
  - empty/loading/error states
- Locale keys live in both `zh_CN.ts` and `en_US.ts`. A key is not complete until both languages exist.
- Locale switching must be reactive. Do not call `location.reload()` for normal language switching.
- `App.vue` owns the Element Plus locale through `el-config-provider`; components should not set Element Plus locale locally.
- `localeStore.setLanguage()` / `localeStore.setLocale()` is the only standard way to switch app language.
- The request layer must keep sending `Content-Language` so backend APIs can return localized messages where supported.
- 404 must be understandable in both languages. The current 404 page shows the active language plus the alternate language, and includes language buttons.

## Backend i18n Boundary

- Menu and dictionary labels should prefer backend i18n data when available.
- Frontend may bridge known old dictionary labels only through the centralized dictionary helper, not per page.
- Business data stored in the database is not translated by the frontend. Examples: department names, user nicknames, notice titles, log module names.
- If a backend dictionary item lacks an `i18nKey` or translated message, record it as a backend data/config issue instead of hardcoding the page.
- Backend error messages may still arrive in Chinese. The frontend should:
  - display backend messages when they are specific and actionable
  - use frontend locale fallback messages only for generic/network/client-side errors
  - avoid silently replacing meaningful backend validation text

## UTC Rules

- Backend date-time values are treated as UTC unless the string already contains a timezone suffix.
- Frontend display converts UTC to the user's local browser timezone.
- Frontend submissions that represent an absolute date-time should be converted back to UTC payload format.
- Use `formatUtc()` for table/detail display of backend date-time values.
- Use `parseUtc()` when custom parsing is needed.
- Use `toUtcPayload()` before sending date-time values that must be stored as absolute UTC instants.
- `parseTime()` is kept for old Ruoyi-compatible globals, but it delegates parsing to the UTC helper.
- Do not format backend date-time values with raw `new Date(...)`, raw `dayjs(...)`, or ad hoc string slicing in pages.

## Timezone Examples

For a browser in `America/New_York`:

- Backend value `2026-05-22T01:29:07Z`
- Frontend local display: `2026-05-21 21:29:07`
- Local input `2026-05-21 21:29:07`
- UTC payload: `2026-05-22T01:29:07Z`

For a browser in `Asia/Shanghai`:

- Backend value `2026-05-22T01:29:07Z`
- Frontend local display: `2026-05-22 09:29:07`

## Do / Do Not

Do:

- Add new UI text to both locale files in the same change.
- Use `getMessage(key, localeStore.language)` or existing page `t()` helpers for TS pages/components.
- Keep icon-only buttons accessible with localized `aria-label` and `title`.
- Test a changed page in Chinese and English.
- Test date-time display in a non-China timezone when touching UTC code.

Do not:

- Reload the page to apply a language switch.
- Put hardcoded Chinese UI labels in TS pages.
- Translate user/business database content in the frontend.
- Guess backend API paths or response contracts while fixing i18n.
- Add per-page special cases for dictionary translations when a global dictionary rule is appropriate.
- Send browser-local ISO strings with nanoseconds or unexpected timezone formats to backend APIs.

## Verification Checklist

Run after touching i18n or UTC behavior:

- `cd admin-ui && npm run typecheck`
- `cd admin-ui && npm run build`
- Browser smoke:
  - login page language switch
  - `/index` language switch
  - one CRUD page language switch, for example `/system/user`
  - one hidden/detail route if changed
  - `/not-a-real-route-for-404-test` shows bilingual 404 content
- Confirm language switching does not change the current route or send the user to 404.
- Confirm console has no frontend error.
- Confirm no obvious i18n key leaks such as `common.search` in visible UI.
- Confirm date-time values render through `formatUtc()` or `parseTime()` rather than raw formatting.

## Known Exceptions

- Tenant application approval is intentionally a placeholder route until the feature is rewritten.
- External monitor pages only localize the wrapper shell; embedded third-party/admin pages own their own language behavior.
- Database business content remains as stored.
