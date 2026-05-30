# Check Summary

## Validation Results

- Passed: `codegraph sync`
- Passed: `git diff --check`
- Passed: `mvn -DskipTests compile`
- Passed: `admin-ui` `pnpm typecheck`
- Passed: `admin-ui` `pnpm build`
- Passed: Playwright browser validation for login Curtain Reveal:
  - 首次进入 `.curtain-intro` 可见。
  - 约 1.7 秒后 DOM 移除。
  - `sessionStorage` 写入 `skyspf:curtain-intro-seen=1`。
  - 本会话刷新不重复播放。
  - 点击遮罩可跳过并移除 DOM。
  - 390x844 移动端视口首播和自动移除正常。

## Blocked / Not Run

- Passed: DEV backend runtime/API smoke after Redis was available:
  - `/captchaImage`: HTTP 200, business code 200.
  - unauthenticated `/getInfo`: HTTP 200, business code 401.
  - `/login`: HTTP 200, business code 200.
  - authenticated `/getInfo`: HTTP 200, business code 200.
  - authenticated `/getRouters`: HTTP 200, business code 200.
  - authenticated `/system/dict/data/type/sys_oper_type`: HTTP 200, business code 200.
  - authenticated `/monitor/operlog/list?pageNum=1&pageSize=1`: HTTP 200, business code 200.

## Notes

- No i18n files changed, so `pnpm i18n:validate` / `pnpm i18n:sync` were not required.
- Dev server and backend processes started for validation were cleaned up.
- Runtime smoke did not print password, captcha, or Token values.
