# Frontend Function Alignment And A11y Notes

Date: 2026-05-21

## Scope

- Mainline: `admin-ui/src/main.ts` TS application.
- Functional baseline: legacy `admin-ui/src/views`, legacy JS API wrappers, legacy layout/store behavior.
- Backend contract: current Java controllers. Backend errors are recorded here only; no backend implementation was changed.
- Skipped by request: tenant application approval/reject flow.

## Changes Completed

- Added a global legacy UI i18n bridge in the TS entry path:
  - keeps backend i18n as the source for menus and dictionaries.
  - translates only legacy hard-coded UI chrome: labels, placeholders, table headers, buttons, dialog text, tooltip/title/aria labels, pagination, upload tips.
  - does not translate table body business data or tree node business data.
- Made dictionary cache language-aware so dict refs are reloaded after language switching.
- Reconnected the TS router to the legacy `layout` shell:
  - sidebar expand/collapse
  - mobile drawer behavior
  - breadcrumb
  - TagsView
  - settings drawer
  - user profile dropdown
- Bridged legacy `store/modules/permission.js` to the TS permission store so old layout components and new route generation use the same Pinia store.
- Kept dynamic `/getRouters` compatibility for `Layout`, `ParentView`, `InnerLink`, `meta.title/icon/noCache/link`, and legacy `src/views/**/*.vue`.
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

Old runtime code still has a reason to exist right now:

- `admin-ui/src/views/**`: current dynamic routes still load these pages as the functional baseline.
- `admin-ui/src/api/system|monitor|tool/*.js`: those pages still call the old API wrappers.
- `admin-ui/src/store/modules/*.js`, `admin-ui/src/utils/*.js`, `admin-ui/src/layout/**`: the restored legacy shell and page runtime still depend on them.

Engineering status:

- The app entry, route generation, stores bridge, request path, locale bridge, and new auth pages are TS-based.
- The admin feature pages are not yet fully TS-rewritten or fully componentized. They are currently aligned through a TS shell plus legacy functional pages.
- Full cleanup of legacy views/APIs/stores should happen module by module after each page is rewritten to TS components and verified against the old feature checklist.

## Latest Verification

- `npm run build` passed after cleanup and i18n additions.
- Remaining build warnings are existing Sass `@import`/global `mix()` deprecation warnings and a Vite large chunk warning.
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
  - Extend `legacy-ui-i18n.ts` with any newly discovered old UI phrases during route-by-route testing.
  - Prefer backend i18n for menus and dictionaries; frontend bridge is only for legacy hard-coded interface chrome.
  - If a backend dict item lacks `i18nKey` or a translated message, document that as backend data/config, not a frontend UI translation issue.
