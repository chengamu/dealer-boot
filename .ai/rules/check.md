## 8. /check

`/check` 是最终验收阶段，用于确认当前需求是否完成、可运行、可验证，并且没有明显回归风险。

## Gate

进入 `/check` 前必须满足：

- `.ai/CURRENT.md` 中当前 Tasks 已完成。
- 当前没有 active blocking issue。
- Open Questions、未确认业务规则、未确认数据库方案已经处理。

否则暂停，回到 `/plan` 或等待用户确认。

## Order

`/check` 按顺序执行：

1. 如果本轮 `/do` 修改了代码，先在项目根目录执行 `codegraph sync`。
2. 调用或咨询 `code-reviewer` 做静态审查。
3. Runtime Validation。
4. API Validation。
5. Browser Validation。
6. Regression Validation。
7. 更新 `.ai/CURRENT.md` 的 Validation Result、Remaining Risks、Next Step。
8. 判断是否允许 `/archive`。

## CodeGraph Sync

如果本轮 `/do` 修改了代码，Runtime / API / Browser Validation 前优先执行：

```bash
codegraph sync
```

规则：

- `codegraph sync` 不等同于 build/test/lint。
- sync 后仍需读取真实源文件确认。
- sync 失败不得伪造成功，必须记录失败和剩余风险。

## Static Validation

必须调用或咨询 `code-reviewer`。它只负责审查，不继续改代码、不扩大 Scope。

必须检查：

- Requirement 和 Acceptance Criteria 是否满足。
- 是否存在无关改动、破坏 minimal diff、明显 bug、边界问题或重复代码。
- 是否破坏 i18n、UTC / 时间规则、权限、API 兼容。
- 是否存在架构风险、回归风险、未验证项。

## Runtime Validation

在条件允许且已获授权时，执行真实 build / lint / test / dev runtime。

必须记录实际结果、失败原因和关键错误位置。不得把未运行的命令写成通过。

验证产物、截图、报告、日志摘要必须放 `.ai/artifacts/`；临时过程文件放 `.ai/tmp/`。不得在 `.ai` 根目录生成 `*.log`、`*.tmp`、`*.json`、`*.txt`、截图、browser trace 或 network dump。

## API Validation

如果接口可运行，尽量验证：

- 状态码和响应结构。
- 权限和错误处理。
- 时间格式、i18n message、分页 / 筛选、UTC 语义。

只记录已验证接口、关键结果、未验证项和风险。

## Browser Validation

涉及页面、表单、列表、弹窗、路由、登录态、权限或前端交互时，必须尽量执行浏览器验证。

优先使用 Codex 内置 Browser；需要用户现有登录态、Cookie、扩展或已有 Chrome 标签页时使用 Codex Chrome。

必须尽量验证：

- 页面、表单、列表、按钮、弹窗、路由、登录态。
- console error、network error。
- i18n、UTC / 时间展示。

如果当前环境不支持浏览器工具，记录 Browser Validation 未执行、原因和 Remaining Risks。

禁止伪造：没打开页面不能写页面正常；没检查 console/network 不能写无错误；没做 Browser Validation 不能写验证通过。

不要保留长 browser trace、长 network dump、长 console output 或录屏日志。

## Regression Validation

检查当前需求是否影响现有页面、接口、权限、i18n、UTC / 时间展示、构建产物、菜单 / 按钮 / 路由、数据库初始化数据、租户上下文和登录态。

无法验证的内容必须记录为未验证项。

## Validation Result

`/check` 后更新 `.ai/CURRENT.md`，只保留：

- Static / Runtime / API / Browser / Regression 的结果摘要。
- 未验证项。
- Remaining Risks。
- 是否允许进入 `/archive`。
- 需要引用产物时只记录 `.ai/artifacts/` 或 `.ai/tmp/` 相对路径。

## Archive Gate

只有同时满足以下条件，才建议 `/archive`：

- Tasks 已完成。
- Acceptance Criteria 已满足。
- 没有 active blocking issue。
- 没有高风险未验证项。
- Runtime / API / Browser / Regression Validation 已完成，或明确说明未完成原因和风险。

否则保留 `.ai/CURRENT.md`，继续修复或验证。
