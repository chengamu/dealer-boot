# Frontend TS Migration Playbook

Date: 2026-05-21

## Purpose

This playbook keeps the admin UI cleanup incremental and verifiable. The current application uses a TypeScript shell with legacy Vue pages as the functional baseline. The goal is to make the project clean by replacing legacy modules one group at a time, not by deleting runtime dependencies prematurely.

Project-scoped Codex agents are installed in `.codex/agents/`:

- `frontend-developer`: frontend behavior, component boundaries, accessibility, loading/empty/error states.
- `typescript-pro`: TypeScript contracts, strictness, API types, compiler-driven refactors.

## Migration Rules

- Do not change backend APIs, database schema, migrations, or production config as part of frontend cleanup.
- New or migrated pages must be Vue SFCs using TypeScript, explicit API types, explicit i18n keys, permission directives, and accessible names for icon-only controls.
- `legacy-ui-i18n.ts` is a temporary bridge only. New TS pages must not depend on DOM text patching.
- Legacy `src/views`, `src/api/*.js`, `src/store/modules/*.js`, `src/layout`, and `src/utils/*.js` may be deleted only after the replacing module is implemented, verified, and no longer referenced.
- Keep old JS store modules as compatibility re-export layers until all imports use the TS stores directly.
- Tighten `tsconfig.exclude` only after the corresponding directory has been migrated and passes type checking.

## Module Order

1. Low-risk or placeholder routes: `monitor/admin`, `monitor/xxljob`, `tool/build`. Completed on 2026-05-21.
2. Monitor table pages: `monitor/online`, `monitor/logininfor`, `monitor/operlog`. Completed on 2026-05-21.
3. Cache pages: `monitor/cache`, `monitor/cache/list`. Completed on 2026-05-21.
4. Simple CRUD pages: `system/post`, `system/notice`, and `system/config` completed on 2026-05-21.
5. Tree pages: `system/dept` and `system/menu` completed on 2026-05-21.
6. Grouped hidden-route modules: `system/dict` with `system/dict/data`, then `system/oss` with `system/oss/config`.
7. Permission-heavy modules: `system/role` with authorization pages, then `system/user` with profile and auth role pages.
8. Code generator: `tool/gen/*`.

## Per-Module Checklist

- Capture the old page behavior before rewriting: query fields, table columns, buttons, dialogs, hidden routes, import/export, batch actions, permission codes, and API calls.
- Implement the smallest TS component structure that preserves the old behavior.
- Use backend `/getRouters` and `/getInfo` as the source for routes and permissions.
- Keep export/download behavior as `POST + x-www-form-urlencoded + blob`.
- Verify Chinese and English UI text, including table headers, placeholders, confirmation text, and toast messages.
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

- TS entry/router/stores/request exist, but many admin pages still run from legacy `src/views`.
- `src/api/**/*.js` modules still serve old pages and should be migrated by business domain.
- `src/store/modules/*.js` now act as compatibility exports for TS stores where migrated.
- `user`, `permission`, `locale`, `app`, `settings`, `tagsView`, and `dict` have TS store implementations under `src/stores`.
- `monitor/online`, `monitor/logininfor`, and `monitor/operlog` now use TS pages and TS API wrappers. Their legacy Vue pages and JS API files have been removed after route, typecheck, build, and browser smoke verification.
- `monitor/cache` and `monitor/cache/list` now use TS pages and a TS API wrapper. Their legacy Vue pages and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/post` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/notice` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/config` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `monitor/admin`, `monitor/xxljob`, and `tool/build` now use TS pages. Their legacy Vue pages have been removed after route, typecheck, build, and browser smoke verification.
- `system/dept` now uses a TS page and TS API wrapper. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `system/menu` now uses a TS page and TS API wrapper while preserving the old API response contract for `system/role` and `tool/gen` compatibility. Its legacy Vue page and JS API file have been removed after route, typecheck, build, and browser smoke verification.
- `components/IconSelect` is now a typed Vue component with an explicit placeholder prop and keyboard-accessible icon buttons.
- `layout/components/Sidebar/Logo.vue` now reads `app.title` from the locale bridge instead of hardcoded Chinese text.
- `src/utils/request.ts` is currently the effective request module for extensionless imports; confirm behavior differences from the old JS request before deleting compatibility code.
- Known shell cleanup still pending: backend menu data currently contains a `//gen` route that causes a Vue Router warning, and the existing tags/transition shell still passes `mode` to a fragment-root `TransitionGroup`.
