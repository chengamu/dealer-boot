## 7. /do

按 `.ai/CURRENT.md` 执行当前需求。执行粒度仍以一个小任务为单位，但默认应连续推进多个未阻塞 Tasks，不因完成单个小任务而人工暂停。

### Preflight Gate

`/do` 开始前必须先检查 `.ai/CURRENT.md`：

1. Status 是否允许执行。
2. Next Step 是否要求用户确认。
3. Open Questions 是否存在阻塞项。
4. Pause Points 是否触发。
5. Tasks 是否有明确顺序。
6. 第一项未完成任务是否被阻塞。

如果存在阻塞项：

- 不允许选择其他任务绕开。
- 不允许自行重排任务。
- 不允许进入代码修改。
- 必须暂停并向用户列出：
  - 阻塞项。
  - 需要用户确认的问题。
  - 推荐的任务顺序调整。
  - 是否需要回到 `/plan` 优化。

### Task Order Rules

- `/do` 默认只能执行 Tasks 中第一个未完成且未阻塞的任务。
- 如果第一个未完成任务被阻塞，不能自动跳到后面的任务。
- 如果后续任务看起来“不依赖阻塞项”，也不能擅自执行，除非 `.ai/CURRENT.md` 明确标记为可并行或用户明确授权。
- 如果需要调整任务顺序，必须先回到 `/plan` 更新 `.ai/CURRENT.md`。
- 子 Agent 也不能绕过任务顺序。

### User Confirmation Rules

以下情况必须问用户确认：

- 用户还没有确认进入 `/do`。
- `.ai/CURRENT.md` 写了“等待用户确认”。
- 改 SQL、baseline、migration、初始化数据前。
- 平台租户 ID、系统租户 ID、默认租户 ID 未确认前。
- 任务顺序需要调整前。
- 要跳过当前阻塞任务、执行后续任务前。

### Correct Behavior Example

如果 `.ai/CURRENT.md` 写：

“等待用户确认是否进入 /do。建议第一步先做平台租户 ID/租户上下文基础改造，并在改 SQL 前确认平台真实租户 ID。”

用户输入：

“按 .ai/RULES.md 的 /do 执行。”

正确行为不是直接执行其他任务。

正确行为是暂停并回复：

“当前 /do 被 Execution Gate 阻止。需要先确认：
1. 是否进入 /do；
2. 平台真实租户 ID 是什么；
3. 是否允许先做平台租户上下文基础改造；
4. 是否需要回到 /plan 调整任务顺序。”

确认前不得修改代码。

执行循环：

1. Pick One Task：选择一个未完成任务，不连续发散。
2. Choose Executor：按 Owner 选择主 Agent 或对应子 Agent；调整 Owner 必须记录原因。
3. Inspect Before Edit：查看相关源文件、类似实现、公共组件/工具、API、i18n、时间工具、权限逻辑、配置；codegraph 只作定位。
4. Plan Micro-change：说明要改什么、为什么改、预期影响文件、如何验证。
5. Modify With Minimal Diff：只做当前任务需要的最小修改。
6. Self-check：检查 Scope、Out of Scope、i18n、UTC、权限、API 兼容、测试需要、minimal diff。
7. Update CURRENT.md：记录状态、完成内容、修改文件、验证、风险、下一步。

暂停条件以 Preflight Gate、User Confirmation Rules 和 Continuous Execution Mode 的 Mandatory Pause Conditions 为准。

输出：选择任务、执行人、改动摘要、修改文件、自检结果、是否更新 CURRENT、下一步。

## Continuous Execution Mode

默认情况下，`/do` 应该连续推进当前需求，而不是每完成一个小任务就暂停。

### Default Behavior

当以下条件已经满足时：

- 用户已确认进入 `/do`。
- 阻塞项已解除。
- 必要业务规则已确认。
- 必要数据库/权限确认已完成。
- `.ai/CURRENT.md` 中没有 active Pause Point。

则：

- `/do` 应连续执行当前需求的 Tasks。
- 每完成一个任务只更新 `.ai/CURRENT.md`。
- 不要每完成一个任务就暂停等待用户。
- 不要频繁输出“Next Step 等待确认”。
- 不要把正常任务切成大量人工确认步骤。

### Allowed Continuous Execution

允许连续执行：

- 同一需求内的小任务。
- 同一模块内的连续修改。
- 已确认方向的 CRUD / 表单 / 列表 / API / DTO / Mapper / i18n / UTC 调整。
- 已确认 SQL baseline 修改。
- 已确认租户上下文改造。
- 已确认代码生成器调整。
- 已确认权限、菜单、字典同步修改。

### Mandatory Pause Conditions

只有以下情况必须暂停：

- 出现新的阻塞项。
- 出现新的业务规则冲突。
- 需要新增或升级依赖。
- 需要数据库结构新增/重构，但用户未确认。
- 需要 migration，但用户未确认。
- 需要大规模架构重构。
- i18n / UTC / 时区影响突然扩大。
- 发现 `.ai/CURRENT.md` 的 Scope 明显不足。
- 需要运行 build/test/lint。
- 子 Agent 意见冲突。
- 发现高风险数据兼容问题。
- 继续执行会明显扩大需求范围。

### No Artificial Pause

禁止：

- 每完成一个小任务就暂停。
- 每更新 `.ai/CURRENT.md` 就暂停。
- 每修改一个文件就暂停。
- 已解除阻塞后仍然重复确认同一问题。
- 已确认租户 ID 后继续阻塞 tenant task。
- 已确认 SQL baseline 后继续阻塞 baseline task。

### Preferred Flow

正确流程：

`/plan` -> 用户确认 -> `/do` 连续推进 -> 完成全部 Tasks -> `/check` -> `/archive`

而不是：

`/do` -> 改一点 -> 暂停 -> 改一点 -> 暂停
