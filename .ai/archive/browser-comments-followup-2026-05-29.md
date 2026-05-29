# Archive: Browser comments follow-up

## Feature

Resolve browser feedback around legal document links, merchant apply layout, message panel UI, password lock defaults, role edit session behavior, dictionary labels, OSS preview config, and platform tenant visibility.

## Requirement Source

- User supplied browser comments for login footer legal links, merchant apply page, notice/message panel, cache/password lock, role edit, dictionary labels, notice type, OSS preview config, tenant application approval visibility, and merchant profile access.
- User clarified the three desktop TXT legal documents are not needed because backend legal document maintenance already exists.
- User later requested resolving the two remaining risks: browser smoke validation and data/dictionary fallback risk.

## Final Status

Accepted with Risks.

## Scope

- Wire login footer legal links to public frontend pages backed by existing legal document API.
- Improve targeted frontend layout and notification panel presentation.
- Harden password retry lock defaults and unlock cleanup.
- Stop role edit from kicking online users.
- Make OSS list preview behavior honor `sys.oss.previewListResource`.
- Let platform tenant user queries bypass tenant/data-permission filters.
- Prefer i18n labels for known dictionaries and fix known PostgreSQL seed text.

## Out of Scope

- No database structure or migration changes.
- No dependency upgrades.
- No use of the three desktop TXT files.
- No broad cleanup of unrelated `.idea/*`, `logs/*`, or other pre-existing dirty files.
- Deprecated MySQL `sql/base.sql` was not repaired because the project rules state PostgreSQL is the active database.

## Completed

- Added public legal document page:
  - `/legal/privacy`
  - `/legal/terms`
  - `/legal/cookie`
- Login footer legal links now route to those pages instead of doing nothing.
- Merchant apply remark textarea now spans the full account grid width.
- Message bell panel has improved header, count, empty state, and clickable notice rows.
- Role edit no longer calls `cleanOnlineUserByRole`; role membership changes still keep existing cleanup behavior.
- Password lock defaults now fall back to 5 attempts and 10 minutes when config is absent or invalid.
- Login unlock now deletes `pwd_err_cnt:<user>:*`, matching the username + client IP key pattern.
- Platform tenant user list/detail data-scope checks now explicitly bypass tenant/data-permission filters.
- OSS list private URL conversion now checks `sys.oss.previewListResource`.
- Notice type fallback includes System Announcement.
- Known dictionary labels now prefer frontend i18n keys over raw backend labels, fixing `sys_user_sex=2` showing `Unknown` in zh.
- PostgreSQL seed `sys_language.zh_CN.name_native` corrected to `简体中文`.
- `AGENTS.md` was staged at the user's request.

## Not Completed

- No direct update was applied to the live remote/test database. The runnable code and PostgreSQL seed are fixed; if an already-deployed DB contains bad data, it still needs a one-off operational update through the normal DB process.
- Local backend `127.0.0.1:8081` was not running during browser smoke, so legal pages showed frontend fallback content during the smoke test. Route/click/page rendering were verified.

## Validation Summary

- `git diff --check`: passed.
- JSON parse check for source/runtime i18n files: passed.
- `pnpm --dir admin-ui typecheck`: passed.
- `mvn -DskipTests compile -rf :bocoo-modules-system`: passed.
- Local Vite started at `http://127.0.0.1:8083` for smoke validation and was stopped afterward.
- Browser smoke passed:
  - login footer exposes legal buttons for Privacy, Terms, and Cookie Policy.
  - Privacy link navigates to `/legal/privacy`.
  - `/legal/privacy`, `/legal/terms`, and `/legal/cookie` render.
  - merchant apply remark textarea spans the full grid width.

## Remaining Risks

- Live DB data may still need cleanup if it already contains `????` or other corrupted labels outside the seed path.
- Browser smoke used frontend fallback legal content because the local backend was not running.
- `.idea/*`, `logs/*`, and `.ai/CURRENT.md` remain unstaged by design.

## Lessons from Learn

- If the shared browser target at a LAN IP is blocked or not tied to the local workspace process, start the local Vite dev server on `127.0.0.1` and smoke-test the current checkout directly.
- For known frontend dictionary types, prefer i18n keys over raw backend `dictLabel` values so locale-specific UI does not leak English seed labels such as `Unknown`.
- Keep legal document content on the existing backend maintenance API; do not introduce file-based legal content once the admin feature exists.
- Treat PostgreSQL seed data as the active path. Do not spend task scope repairing deprecated MySQL seed files unless explicitly requested.

## Key Decisions

- Legal pages use public frontend routes backed by existing `sys_legal_document` API.
- The three desktop TXT files are not part of runtime or seed behavior.
- Role edit should refresh role/cache state without forcing online users out; role membership changes retain the existing online-session cleanup.
- Platform tenant user-management queries may bypass tenant/data-permission filters only through explicit platform identity checks.

## Files Modified

- `AGENTS.md`
- `admin-ui/public/i18n/en_US.json`
- `admin-ui/public/i18n/zh_CN.json`
- `admin-ui/src/pages/auth/LegalDocumentViewPage.vue`
- `admin-ui/src/pages/auth/LoginPage.vue`
- `admin-ui/src/pages/auth/MerchantApplyPage.vue`
- `admin-ui/src/router/index.ts`
- `admin-ui/src/shell/MessageBell.vue`
- `admin-ui/src/utils/dict.ts`
- `bocoo-admin/src/main/resources/i18n/en_US.json`
- `bocoo-admin/src/main/resources/i18n/zh_CN.json`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/monitor/SysLogininforController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysRoleController.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLegalDocumentService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLoginService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysOssService.java`
- `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysUserService.java`
- `i18n/locales/en_US.json`
- `i18n/locales/zh_CN.json`
- `sql/postgresql/base.sql`

## Artifacts

- Archive record: `.ai/archive/browser-comments-followup-2026-05-29.md`
- Smoke logs from temporary Vite run: `.ai/vite-smoke.log`, `.ai/vite-smoke.err.log`

## Suggested Commit Message

```text
fix: polish legal pages and admin behavior follow-ups

- add public legal document pages and wire login footer links
- improve merchant apply remark layout and message bell panel
- harden password retry defaults and unlock cleanup
- avoid kicking users on role edit
- make OSS preview list honor sys config
- bypass tenant filters for platform user queries
- prefer i18n labels for known dict values and fix zh seed text
```

## Follow-up

- If needed, run a one-off live DB check for corrupted values in dictionary and standard language tables before release.
