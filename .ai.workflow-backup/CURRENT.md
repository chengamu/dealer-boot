# Current

## Status

Archived task: browser comment follow-up for legal document links, merchant apply layout, message panel UI, password lock cache defaults, role save behavior, dictionary/notice fallbacks, OSS config cache, and platform tenant visibility.

## Last Archive

- `.ai/archive/browser-comments-followup-2026-05-29.md`
- Final status: Accepted with Risks.

## Summary

- Impact scope spanned `admin-ui` auth/system shell pages and `bocoo-modules-system` services/controllers. No database structure or migration changes were made.
- Existing `sys_legal_document` entity/service/page/API was reused; the three desktop TXT files were explicitly not used.
- Implemented:
  - Public `/legal/:type` page for privacy/terms/cookie, with login footer routes wired to it.
  - Merchant apply remark field spans the full account section width.
  - Message bell empty/list UI is more structured.
  - Role edit no longer logs out bound users; auth-user changes still retain existing online-session cleanup.
  - Password retry defaults to 5 attempts / 10 minutes when config is absent/invalid; unlock clears `pwd_err_cnt:<user>:*` keys.
  - Platform tenant user list/detail checks explicitly bypass tenant/data-permission filters.
  - OSS list reads `sys.oss.previewListResource`.
  - Notice type fallback includes System Announcement.
  - Known dictionary labels prefer i18n over raw backend labels; `sys_user_sex` zh now resolves to `男/女/未知`.
  - PostgreSQL seed `sys_language.zh_CN.name_native` corrected to `简体中文`.

## Archive Notes

- Archive file: `.ai/archive/browser-comments-followup-2026-05-29.md`
- `HANDOFF.md` and `MEMORY.md` updated per `.ai/rules/archive.md`.
- Staged for commit: task files plus user-requested `AGENTS.md`.
- Left unstaged intentionally: `.idea/*`, `logs/*`, `.ai/CURRENT.md`, and the new archive file unless the user asks to stage `.ai` records.
- Suggested commit message is recorded in the archive file.

## Verification

- `git diff --check`: passed.
- JSON parse check for source/runtime i18n files: passed.
- `pnpm --dir admin-ui typecheck`: passed.
- `mvn -DskipTests compile -rf :bocoo-modules-system`: passed.
- Local browser smoke on `http://127.0.0.1:8083`: passed for legal pages and merchant apply remark width.
- Local Vite smoke server was stopped after validation.

## Remaining Notes

- Local backend `127.0.0.1:8081` was not running during smoke, so legal pages used frontend fallback content while route/click/render behavior was validated.
- Live DB may still need an operational data cleanup if deployed data already contains corrupted labels outside the PostgreSQL seed path.
