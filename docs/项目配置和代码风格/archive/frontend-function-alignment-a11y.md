# Frontend Function Alignment And A11y Notes

Date: 2026-05-21

## Scope

- Mainline: `admin-ui/src/main.ts` TS application.
- Functional baseline: old feature behavior captured from the former `admin-ui/src/views`, old API wrappers, and old layout/store behavior.
- Backend contract: current Java controllers. Backend errors are recorded here only; no backend implementation was changed.
- Skipped by request: tenant application approval/reject flow.

## Changes Completed

- Replaced the temporary legacy UI i18n bridge with explicit locale keys in the active TS shell, pages, and shared components.
  - Superseded i18n direction as of 2026-05-29: single-source JSON under `i18n/locales/` is the target for system UI text and dictionary labels.
  - backend `sys_i18n_message` is deprecated; menus and dictionaries resolve labels through single-source JSON.
  - frontend runtime JSON should cover interface chrome: labels, placeholders, table headers, buttons, dialog text, tooltip/title/aria labels, pagination, upload tips.
  - table body business data and tree node business data remain backend/data concerns.
- Made dictionary cache language-aware so dict refs are reloaded after language switching.
- Reconnected the TS router to the migrated layout shell:
  - sidebar expand/collapse
  - mobile drawer behavior
  - breadcrumb
  - TagsView
  - settings drawer
  - user profile dropdown
- Removed the legacy `store/modules` bridge; global shell code now imports Pinia stores from `src/stores`.
- Kept dynamic `/getRouters` compatibility for `Layout`, `ParentView`, `InnerLink`, and `meta.title/icon/noCache/link`. Backend component paths now resolve through the explicit TS page map.
- Registered old global components and helpers in the TS entry path:
  - `DictTag`, `Pagination`, `RightToolbar`, `SvgIcon`, `TreeSelect`, upload/editor/image helpers.
  - `useDict`, `download`, `parseTime`, `resetForm`, `handleTree`, `addDateRange`, dictionary label helpers.
- Restored right-top message entry using notice list as the first aligned implementation.
- Fixed frontend edit API mismatch in generic module submit: edit uses `PUT {baseUrl}` with id in body.
- Fixed `DictTag` invalid Element Plus tag type warnings.
- Fixed personal profile route and profile basic-info form radio usage.
- Fixed old download plugin error branch using undefined `Message`.
- Added required lightweight legacy dependency: `fuse.js` for old `HeaderSearch`.
- Added accessible names to high-impact old icon-only controls:
  - layout hamburger
  - message bell
  - right toolbar search/refresh/column buttons
  - user and role row action icon buttons
  - code generator row action icon buttons
  - profile avatar cropper zoom/rotate buttons
  - login captcha refresh buttons

## Main Route Coverage

All routes below loaded without 404, without login redirect, and without backend 4xx/5xx during the latest test pass.

| Route | Query/Form Surface | Main Buttons Seen | Legacy Shell |
| --- | --- | --- | --- |
| `/system/user` | user name, phone, status, create time | search/reset/add/edit/delete/import/export | sidebar/tags OK |
| `/system/role` | role name, permission key, status, create time | search/reset/add/edit/delete/export | sidebar/tags OK |
| `/system/menu` | menu name, status | search/reset/add/expand/edit/delete | sidebar/tags OK |
| `/system/dept` | dept name, status | search/reset/add/expand/edit/delete | sidebar/tags OK |
| `/system/post` | post code, post name, status | search/reset/add/edit/delete/export | sidebar/tags OK |
| `/system/dict` | dict name, dict type, status, create time | search/reset/add/edit/delete/export/refresh cache | sidebar/tags OK |
| `/system/config` | config name, config key, built-in flag, create time | search/reset/add/edit/delete/export/refresh cache | sidebar/tags OK |
| `/system/notice` | notice title, operator, type | search/reset/add/edit/delete | sidebar/tags OK |
| `/system/oss` | file name, original name, suffix, create time, uploader, provider | search/reset/upload file/upload image/delete/preview switch/config | sidebar/tags OK |
| `/monitor/online` | login IP, user name | search/reset/force logout | sidebar/tags OK |
| `/monitor/cache` | cache dashboard tables | cache metrics/tables | sidebar/tags OK |
| `/monitor/cache/list` | cache name, cache key, cache content | clear all | sidebar/tags OK |
| `/monitor/operlog` | IP, module, operator, type, status, operation time | search/reset/delete/clear/export/detail | sidebar/tags OK |
| `/monitor/logininfor` | login IP, user name, status, login time | search/reset/delete/clear/unlock/export | sidebar/tags OK |
| `/tool/gen` | datasource, table name, table comment, create time | search/reset/generate/import/edit/delete | sidebar/tags OK |
| `/tool/build` | old form builder page loads | page loads | sidebar/tags OK |
| `/user/profile` | nickname, phone, email, sex, password fields | save/close | sidebar/tags OK |

## Dialog And Hidden Route Coverage

Add dialog entry was opened and inspected without saving data:

- `/system/user`: user nickname, department, phone, email, username, password, sex, status, post, role, remark.
- `/system/role`: role name, permission key, role sort, status, menu permissions, remark.
- `/system/menu`: parent menu, menu type, icon, name, i18n key, sort, external link, route, visible status, menu status.
- `/system/dept`: parent dept, dept name, sort, leader, phone, email, status.
- `/system/post`: post name, post code, sort, status, remark.
- `/system/dict`: dict name, dict type, status, remark.
- `/system/config`: config name, key, value, built-in flag, remark.
- `/system/notice`: title, type, status, content.
- `/system/oss-config/index`: config key, endpoint, custom domain, access key, secret key, bucket, prefix, HTTPS, access policy, region, remark.

Hidden old routes loaded and called APIs successfully:

- `/system/user-auth/role/{userId}`
- `/system/role-auth/user/{roleId}`
- `/system/dict-data/index/{dictId}`
- `/system/oss-config/index`

`/tool/gen-edit/index/{tableId}` was not executed because the current code generator list returned no table row in this environment.

## Layout Interaction Coverage

- Sidebar hamburger toggled from `openSidebar` to `hideSidebar`.
- TagsView container is present.
- Right-top message entry opens the notice popover and calls `/system/notice/list`.
- User dropdown shows the personal center entry.
- Personal center route loads profile info and password tabs.

## Backend/API Notes

- Core login/session/menu/list APIs worked after Redis was corrected externally.
- No backend implementation was changed.
- No current backend 4xx/5xx was reproduced in the covered routes above.
- Deeper destructive actions were not submitted automatically: delete, clear, force logout, save, import, export downloads, and status mutations should be manually confirmed before executing against real data.

## Latest Accessibility And Keyboard Check

- Login page keyboard path was checked with Tab order:
  - language selector
  - username
  - password
  - captcha
  - captcha image refresh
  - captcha refresh button
  - remember checkbox
  - merchant apply link
  - login button
- Login page unnamed button count: `0`.
- Sample old admin routes after labeling and the latest legacy i18n bridge pass:
  - `/system/user`: unnamed button count `0`.
  - `/system/role`: unnamed button count `0`.
  - `/system/dict`: unnamed button count `0`.
  - `/system/menu`: unnamed button count `0`.
  - `/monitor/online`: unnamed button count `0`.
  - `/tool/gen`: unnamed button count `0`.
  - `/user/profile`: unnamed button count `0`.
- Dialog keyboard smoke check:
  - focusing the user page add button and pressing Enter opened the add dialog.
  - Escape closed the dialog.
- No backend 4xx/5xx and no console errors were seen in this pass.

## Latest I18n And UTC Check

- Fixed the legacy global `parseTime()` path to parse backend naive date-time strings as UTC and render them in the browser local timezone.
- Confirmed backend i18n behavior with `Content-Language`:
  - `/getRouters` returns English menu titles such as `System`, `Monitor`, `Logs`.
  - `/system/dict/data/type/sys_normal_disable` returns `Normal` and `Disabled`.
- Added a global legacy UI i18n bridge for old hard-coded pages:
  - translates only known UI surfaces such as form labels, table headers, buttons, placeholders, tooltip/title/aria labels, dialogs, upload tips, and pagination text.
  - intentionally does not translate table body data or tree node business data.
- Updated legacy dict caching to be language-aware and to reload dict refs when the locale changes.
- In a browser context emulating `America/New_York` with English locale:
  - `/system/user` loaded in English without backend 4xx/5xx or console errors.
  - user table headers rendered as `User ID`, `User Name`, `Nickname`, `Department`, `Phone`, `Status`, `Created At`, `Actions`.
  - main list buttons rendered as `Search`, `Reset`, `Add`, `Edit`, `Delete`, `Import`, `Export`.
  - right-top message entry rendered as `Messages`; notice popover action rendered as `View all`.
  - the admin row `2026-05-20 12:38:32` UTC value rendered as `2026-05-20 08:38:32` in `America/New_York`.
  - checked Chinese residuals for the user-list surface: no hits for `用户名称`, `手机号码`, `创建时间`, `搜索`, `重置`, `新增`, `修改`, `删除`, `导入`, `导出`, `操作`, `消息通知`, `查看全部`.
- Additional English UI smoke routes after the global bridge:
  - `/system/user`, `/system/role`, `/system/dict`, `/system/menu`, `/monitor/online`, `/tool/gen`, `/user/profile` had no Chinese hits in checked UI surfaces: form labels, toolbar/buttons, table headers, placeholders, tabs, aria labels, and titles.
  - backend dict labels for `sys_normal_disable` were `Normal`, `Disabled`.

## Old Code Cleanup Audit

Clearly unused scaffold files were removed after reference checks:

- `admin-ui/src/main.js`
- `admin-ui/src/router/index.js`
- `admin-ui/src/permission.js`
- `admin-ui/src/views/login.vue`
- `admin-ui/src/api/login.js`
- `admin-ui/src/api/menu.js`
- `admin-ui/src/shell/AppLayout.vue`
- `admin-ui/src/shell/SideMenuItem.vue`
- `admin-ui/src/shell/TagsBar.vue`
- `admin-ui/src/stores/navigation.ts`
- `admin-ui/src/router/menu.ts`
- old generic scaffold pages under `admin-ui/src/pages/dashboard` and `admin-ui/src/pages/modules`.

Old runtime code cleanup status:

- `admin-ui/src/views/**` has been removed.
- `admin-ui/src/api/**/*.js`, `admin-ui/src/store/modules/*.js`, and `admin-ui/src/utils/*.js` have been removed or migrated to TS.
- `admin-ui/src/layout/**` is included in `vue-tsc` and no longer hidden behind `tsconfig.exclude`.

Engineering status:

- The app entry, route generation, stores, request path, explicit locale messages, framework shell, and migrated admin feature pages are TS-based.
- Tenant application approval/reject UI has been removed by request and replaced with a TS placeholder while keeping the menu route.
- `admin-ui/src` has no current `any` escape or TS suppression matches in the latest source scan.
- Auth and locale cookies now set root path, `SameSite=Lax`, and HTTPS-only `Secure` automatically.
- Vite splits chart, editor, and media dependencies into dedicated chunks so chart/editor code is not pulled into ordinary CRUD pages.
- Future cleanup should focus on a planned rich-text editor replacement because `@vueup/vue-quill` still depends on Quill 1.x, which has a moderate advisory and no upstream fix.

## Latest Verification

- `npm run typecheck` passed after TS strictness cleanup.
- `npm run build` passed after dependency overrides, cookie hardening, and bundle chunking.
- `npm audit --registry=https://registry.npmjs.org --audit-level=high` passed. Remaining audit output is Quill 1.x through `@vueup/vue-quill`, rated moderate with no fix available.
- English/US-timezone route scan passed for:
  - `/system/user`
  - `/system/role`
  - `/system/dict`
  - `/system/menu`
  - `/monitor/online`
  - `/tool/gen`
  - `/user/profile`
- In that scan:
  - checked UI surfaces had no remaining hits for the known legacy Chinese phrases.
  - visible button unnamed count was `0`.
  - console error count was `0`.

## Remaining Work

- Tenant approval/reject flow: intentionally skipped because it is not implemented yet.
- Code generator edit/preview/sync/download:
  - route and list load.
  - current environment returned no generated table rows, so preview/edit/sync/download row actions could not be executed.
  - sync/import/save/delete are data-changing and should only be run after confirming a disposable table row.
- Destructive or data-mutating actions should be tested manually or in a disposable dataset:
  - delete/clear/force logout/status switches/reset password/save/import/export.
- Accessibility follow-up:
  - continue route-by-route labeling for any newly restored icon-only buttons.
  - run a full manual screen reader pass after UI styling is finalized.
  - focus return after save/cancel should be verified against real dialogs during disposable-data CRUD testing.
- I18n follow-up:
  - Add newly discovered system UI phrases to `i18n/locales/en_US.json`, then use the i18n translate/validate/sync flow.
  - Keep menus and dictionaries on single-source JSON keys; do not add new `sys_i18n_message` dependencies.
  - If a dictionary item lacks a `dict.<dictType>.<value>` JSON entry, document it as source JSON coverage debt, not a page-level translation issue.
