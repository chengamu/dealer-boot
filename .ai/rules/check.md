## 8. /check

`/check` 是最终验收阶段。

它不只是静态代码 review，而是：

- Static Validation
- Runtime Validation
- API Validation
- Browser Validation
- Regression Validation

目标：

确认当前需求：

- 真正完成
- 真正可运行
- 真正可验证
- 没有明显回归风险

==================================================
/check 前置条件
==================================================

默认在以下情况后执行：

- `.ai/CURRENT.md` 中所有 Tasks 已完成
- `/do` 已完成 Continuous Execution
- 当前没有 active blocking issue

如果仍存在：

- Open Questions
- 阻塞项
- 未确认业务规则
- 未确认数据库方案

则：

- 不允许进入最终 `/check`
- 必须先暂停并返回 `/plan` 或等待用户确认

==================================================
/check 执行顺序
==================================================

`/check` 必须按以下顺序执行：

1. `code-reviewer` 静态审查
2. Runtime Validation：build / lint / test / dev runtime
3. API Validation
4. Browser Validation：优先使用 Codex 内置 Browser / Chrome 工具
5. 汇总 Validation / Risks / Next Step
6. 判断是否允许 `/archive`

==================================================
1. Static Validation（code-reviewer）
==================================================

必须调用或咨询：

- `code-reviewer`

`code-reviewer` 只负责：

- 静态代码审查
- 风险检查
- 回归风险分析
- Acceptance Criteria 检查

不负责：

- 继续改代码
- 扩大 Scope
- 重构
- 新增需求

`code-reviewer` 必须检查：

1. Requirement 是否完成
2. Acceptance Criteria 是否逐条满足
3. 是否存在无关改动
4. 是否破坏 minimal diff
5. 是否存在明显 bug
6. 是否存在边界问题
7. 是否存在重复代码
8. 是否破坏 i18n
9. 是否破坏 UTC / 时间规则
10. 是否影响权限
11. 是否影响 API 兼容
12. 是否存在明显架构风险
13. 是否存在未验证项

==================================================
2. Runtime Validation
==================================================

在条件允许时，必须执行真实运行验证。

优先级：

1. build
2. lint
3. test
4. dev / runtime

如果项目存在：

- build 命令
- test 命令
- lint 命令
- dev server

则优先执行。

必须明确记录：

- 实际运行了什么
- 没运行什么
- 为什么没运行
- 命令是否成功
- 是否存在 warning / error

==================================================
No Fake Runtime Validation
==================================================

禁止：

- 没 build 却写“build 通过”
- 没 test 却写“测试通过”
- 没 lint 却写“lint 正常”
- 没运行却写“验证完成”

==================================================
3. API Validation
==================================================

如果接口可运行，必须尽量验证：

- 状态码
- 响应结构
- 权限行为
- 错误处理
- 时间格式
- i18n message
- 分页 / 筛选参数
- UTC 时间语义

必须明确记录：

- 验证了哪些接口
- 请求参数
- 预期结果
- 实际结果
- 未验证接口
- 未验证原因

==================================================
4. Browser Validation
==================================================

如果需求涉及：

- 页面
- 表单
- 列表
- 弹窗
- 路由
- 登录态
- 权限
- 前端交互

则必须尽量执行浏览器验证。

==================================================
Browser Validation Tool Selection
==================================================

Browser Validation 使用 Codex 当前环境可用的内置浏览器能力。

优先级：

1. 优先使用 Codex 内置 Browser 工具进行页面验证。
2. 如果需要用户现有登录态、Cookie、扩展或已有 Chrome 标签页，使用 Codex Chrome 工具。
3. 如果当前环境不支持浏览器工具，不允许伪造浏览器验证；必须在 `.ai/CURRENT.md` 的 Validation 中记录：
   - Browser Validation 未执行
   - 原因
   - Remaining Risks

==================================================
Browser Validation 必须尽量验证
==================================================

1. 打开页面
2. 模拟用户操作
3. 点击按钮
4. 填写表单
5. 验证弹窗
6. 验证列表
7. 验证路由
8. 验证权限行为
9. 检查 console error
10. 检查 network error
11. 检查失败请求
12. 验证 i18n
13. 验证 UTC / 时间展示

必须明确记录：

- 已验证页面
- 已验证交互
- console / network 状态
- 未验证项
- Remaining Risks

不要保留长 browser trace、长 network dump、长 console output 或完整操作录屏日志。

==================================================
No Fake Browser Validation
==================================================

禁止：

- 没有打开页面却写“页面正常”
- 没有浏览器工具却写“已完成浏览器验证”
- 没有检查 console 却写“无错误”
- 没有检查 network 却写“接口正常”
- 没有点击按钮却写“功能正常”
- 没做 Browser Validation 却写“验证通过”

==================================================
5. Regression Validation
==================================================

必须检查当前需求是否影响：

- 现有页面
- 现有接口
- 权限行为
- i18n 文案
- UTC / 时间展示
- 构建产物
- 已有菜单 / 按钮 / 路由
- 数据库初始化数据
- 租户上下文
- 用户登录态

如果无法验证，必须记录为未验证项。

==================================================
6. Validation Output
==================================================

`/check` 完成后，必须更新：

- `.ai/CURRENT.md`

`.ai/CURRENT.md` 的 Validation 必须包含：

```md
## Validation

### Static Validation

- code-reviewer 摘要
- 静态风险
- 回归风险

### Runtime Validation

- build / lint / test / dev 实际执行情况
- 成功 / 失败
- warning / error
- 未执行项和原因

### API Validation

- 已验证接口
- 未验证接口
- 风险

### Browser Validation

- 已验证页面
- 已验证交互
- console / network 状态
- 未验证项
- Remaining Risks

### Regression Validation

- 已验证回归点
- 未验证回归点
- 风险

### Remaining Risks

- 当前剩余风险
- 未解决问题
- 未验证项
````

==================================================
7. /check 最终输出
==================================================

`/check` 最终输出：

1. 验收结论
2. code-reviewer 摘要
3. Runtime Validation 结果
4. API Validation 结果
5. Browser Validation 结果
6. Regression Validation 结果
7. 已通过项
8. 未验证项
9. Remaining Risks
10. 是否建议 `/archive`
11. 下一步建议

==================================================
8. /archive Gate
===================================================

只有满足以下条件时，才建议 `/archive`：

* Tasks 已完成
* Acceptance Criteria 已满足
* 没有 active blocking issue
* 没有高风险未验证项
* Runtime Validation 已完成或明确说明未完成原因
* API Validation 已完成或明确说明未完成原因
* Browser Validation 已完成或明确说明未完成原因
* Regression Validation 已完成或明确说明未完成原因

否则：

* 不建议 `/archive`
* 必须保留 `.ai/CURRENT.md`
* 必须继续修复或验证
