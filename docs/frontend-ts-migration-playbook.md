# Frontend TS Migration Playbook

Date: 2026-05-21

## Purpose

This playbook keeps the admin UI cleanup incremental and verifiable. The current application now runs through the TypeScript entry, router, stores, API wrappers, and migrated admin pages. The remaining rule is to keep cleanup compiler-driven and verified instead of deleting runtime dependencies prematurely.

Project-scoped Codex agents are installed in `.codex/agents/`:

- `frontend-developer`: frontend behavior, component boundaries, accessibility, loading/empty/error states.
- `typescript-pro`: TypeScript contracts, strictness, API types, compiler-driven refactors.

## Migration Rules

- Do not change backend APIs, database schema, migrations, or production config as part of frontend cleanup.
- New or migrated pages must be Vue SFCs using TypeScript, explicit API types, explicit i18n keys, permission directives, and accessible names for icon-only controls.
- DOM text patching is no longer allowed. Use explicit locale keys in TS pages/components instead of reintroducing `legacy-ui-i18n.ts`-style bridges.
- Legacy files may be deleted only after the replacing module is implemented, verified, and no longer referenced.
- Do not reintroduce `src/store` compatibility shims. Global code must import Pinia stores from `src/stores`.
- Do not add broad `tsconfig.exclude` entries for application source. Source cleanup must pass `vue-tsc`.

## Module Order

1. Low-risk or placeholder routes: `monitor/admin`, `monitor/xxljob`, `tool/build`. Completed on 2026-05-21.
2. Monitor table pages: `monitor/online`, `monitor/logininfor`, `monitor/operlog`. Completed on 2026-05-21.
3. Cache pages: `monitor/cache`, `monitor/cache/list`. Completed on 2026-05-21.
4. Simple CRUD pages: `system/post`, `system/notice`, and `system/config` completed on 2026-05-21.
5. Tree pages: `system/dept` and `system/menu` completed on 2026-05-21.
6. Grouped hidden-route modules: `system/dict` with `system/dict/data` and `system/oss` with `system/oss/config` completed on 2026-05-21.
7. Permission-heavy modules: `system/role` with authorization pages and `system/user` with profile/auth-role pages completed on 2026-05-21.
8. Code generator: `tool/gen/*` completed on 2026-05-21.

## Per-Module Checklist

- Capture the old page behavior before rewriting: query fields, table columns, buttons, dialogs, hidden routes, import/export, batch actions, permission codes, and API calls.
- Implement the smallest TS component structure that preserves the old behavior.
- Use backend `/getRouters` and `/getInfo` as the source for routes and permissions.
- Keep export/download behavior as `POST + x-www-form-urlencoded + blob`.
- Verify Chinese and English UI text, including table headers, placeholders, confirmation text, and toast messages.
- For new UI text, use the single-source JSON flow: add English to `i18n/locales/en_US.json`, then translate/validate/sync.
- Verify UTC rendering in a non-China timezone when the page displays backend date-time values.
- Verify icon-only buttons have `aria-label` or `title`.
- Verify modal focus enters the dialog and returns to the trigger after cancel/save when testing with disposable data.

## Cleanup Gate

Before deleting any legacy file or removing it from `tsconfig.exclude`:

- The new TS module handles the same route and hidden routes.
- Backend dynamic `component` values resolve to the new implementation.
- `rg` shows no remaining imports of the legacy file.
- `npm run typecheck` passes.
- `npm run build` passes.
- Browser smoke test passes for login, refresh-direct route access, dynamic menu loading, locale switch, and the module's core CRUD/query flow.

## Known Current State

- TS entry/router/stores/request, admin pages, and API wrappers are the active runtime.
- Frontend hardening pass completed on 2026-05-22:
  - `admin-ui/src` has no `as any`, `: any`, `Record<string, any>`, `@ts-ignore`, `@ts-expect-error`, or `@ts-nocheck` matches in the current source scan.
  - auth and locale cookies now set `SameSite=Lax`, root path, and `Secure` automatically on HTTPS.
  - Vite manual chunks split chart, editor, and media dependencies out of the main runtime bundle; the latest production build main app chunk is about 292 kB before gzip.
  - high-severity `npm audit` findings are cleared. The remaining audit item is Quill 1.x through `@vueup/vue-quill`, rated moderate with no upstream fix available. Replacing it should be handled as a planned editor migration, not a blind dependency bump.
- `admin-ui/src` no longer contains JavaScript files. The former JS utilities, directives, plugins, DOM locale bridge, API wrappers, and framework helpers have been migrated or removed after reference checks.
- The old `plugins/legacy-ui-i18n.ts` DOM text patcher has been removed after all active pages and shell components moved to explicit locale keys.
- Remaining legacy-style Vue runtime components now use `lang="ts"` so they are no longer plain JS blocks. The temporary local `// @ts-nocheck` escapes have been removed from `admin-ui/src` after typing the dynamic route, upload, editor, tree-select, and instance-proxy contracts.
- `user`, `permission`, `locale`, `app`, `settings`, `tagsView`, and `dict` have TS store implementations under `src/stores`.
- `src/store` compatibility shims have been removed. All global shell/plugin/directive imports now use `src/stores` directly.
- `monitor/online`, `monitor/logininfor`, and `monitor/operlog` now use TS pages and TS API wrappers. Their legacy Vue pages and JS API files have been removed after route, typecheck, build, and browser smoke verification.
- `monitor/cache` and `monitor/cache/list` now use TS pages and a TS API wrapper. Their legacy Vue pages and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/post` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/notice` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/config` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `monitor/admin`, `monitor/xxljob`, and `tool/build` now use TS pages. Their legacy Vue pages have been removed after route, typecheck, build, and browser smoke verification.
- `system/dept` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/menu` now uses a TS page and TS API wrapper while preserving the old API response contract for `system/role` and `tool/gen` compatibility. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/dict` and hidden `system/dict-data/index/:dictId` now use TS pages and TS API wrappers while preserving the old API response contract for the dict helper and `tool/gen` compatibility. Their legacy Vue pages and JS API files have been removed after route, typecheck, build, and browser smoke verification.
- `system/oss` and hidden `system/oss-config/index` now use TS pages and TS API wrappers while preserving the old API response contract for `FileUpload` and `ImageUpload` compatibility. Their legacy Vue pages and JS API files have been removed after route, typecheck, build, and browser smoke verification.
- `components/FileUpload` and `components/ImageUpload` now use typed upload item/response contracts; their visible upload labels, tips, preview title, and validation/loading messages use explicit locale keys instead of hardcoded Chinese text.
- `components/Editor` now uses explicit locale keys for its placeholder, upload validation/loading messages, and Quill tooltip/dropdown labels. `components/SizeSelect` now uses typed TS state, direct TS store imports, localized size labels, and a localized accessible trigger name.
- `system/role` and hidden `system/role-auth/user/:roleId` now use TS pages and a TS API wrapper. The role page preserves query/reset, CRUD, export, status switch, menu permission tree, data scope tree, and authorization user navigation. The authorization user page preserves assigned-user query, batch cancel, single cancel, close, and select-user dialog. Legacy role Vue pages and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/user`, hidden `system/user-auth/role/:userId`, and `/user/profile` now use TS pages and a TS API wrapper. The user page preserves department tree filtering, query/reset, column visibility, pagination, CRUD, status switch, reset password, role assignment navigation, import/template/export, and UTC date rendering. Profile is split into typed avatar, user-info, and password forms. Legacy user Vue pages and JS API file have been removed after route and typecheck verification.
- `layout/components/TagsView/index.vue` now uses explicit i18n keys for the context menu instead of hardcoded Chinese labels.
- Backend dictionary data still returns Chinese labels for `sys_user_sex` in the English profile form (`男`, `女`, `未知`). Under the new i18n target, this should be fixed by adding `dict.<dictType>.<value>` entries to the single-source JSON and migrating dictionary label resolution, not by hardcoding business dictionary labels in the page.
- `tool/gen`, hidden `tool/gen-edit/index/:tableId`, and their import/basic/generation child forms now use TS pages and a TS API wrapper. The generator keeps datasource header behavior, query/reset, import, preview/copy, edit tabs, sync, delete, and zip/custom generation entry points. Legacy generator Vue pages and JS API file have been removed after route, typecheck, and build verification.
- Framework routes `redirect`, dashboard, 404, and public register now resolve to TS pages under `src/pages`. Their old `src/views` files have been removed after route, typecheck, and build verification.
- `src/views` has been removed. The tenant application menu is still present, but it resolves to a TS placeholder page because approval/reject UI is intentionally deferred for a future rewrite.
- `components/IconSelect` is now a typed Vue component with an explicit placeholder prop and keyboard-accessible icon buttons.
- `layout/components/Sidebar/Logo.vue` now reads `app.title` from explicit locale messages instead of hardcoded Chinese text.
- Project settings, runtime config, auth cookie helpers, error-code lookup, global plugins, global directives, dictionary helper, dynamic title helper, permission helper, request helper, icon registration, RSA helper, and Ruoyi global helpers have been migrated from JS to TS and verified with `npm run typecheck`.
- `tsconfig.exclude` now contains only `node_modules` and `dist`; layout and utilities are included in strict type checking.
- Strict Vue component cleanup completed for breadcrumb, dict tag, pagination, right toolbar, image preview, size selector, layout shell, iframe toggle, sidebar, header search, top nav, settings drawer, tags view, scroll pane, upload, editor, and tree-select components.
- Shell warning cleanup completed for the iframe transition wrapper and repeated-slash menu path normalization; browser smoke on `/system/user` no longer reports the previous `TransitionGroup mode` or `//gen` warnings after reload.
- Sass entry styles now use module-style `@use` for global partials, and TreeSelect uses `sass:color` plus public theme color aliases. `npm run build` no longer emits the previous Sass `@import` or global `mix()` deprecation warnings.
- Navbar user menu/logout confirmation, Settings drawer labels, TopNav overflow text, and Hamburger accessible names now use explicit locale messages instead of hardcoded Chinese. Navbar also restores the old global header search, fullscreen, layout size, language, user menu, and settings drawer entries with localized icon labels and semantic trigger buttons. Browser smoke in English verifies the user dropdown renders `Profile` and `Sign out`, and the collapsed sidebar toggle exposes `Expand menu` through `aria-label` and `title`.
