## 7. /do

按 `.ai/CURRENT.md` 执行当前需求。默认连续推进未阻塞 Tasks，不因完成单个小任务而人工暂停。

## Execution Gate

`/do` 开始前必须检查 `.ai/CURRENT.md`：

- Status / Next Step 是否允许执行。
- Open Questions、Pause Points、Blockers 是否存在。
- Tasks 是否有明确顺序。
- 第一项未完成任务是否被阻塞。

存在阻塞时必须暂停，不允许绕过、重排或进入代码修改。

## Task Order

- 默认执行第一个未完成且未阻塞的 Task。
- 第一个未完成 Task 被阻塞时，不能自动跳到后面的任务。
- 需要调整任务顺序时，必须回到 `/plan` 更新 `.ai/CURRENT.md`。
- 子 Agent 也不能绕过任务顺序。

## Owner Dispatch

- 按 Task Owner 选择主 Agent 或对应子 Agent。
- 不允许忽略 Owner。
- 如需调整 Owner，必须在 `.ai/CURRENT.md` 记录原因。
- 子 Agent 不能扩大 Scope、升级依赖、修改数据库结构或绕过 i18n / UTC / minimal diff。

## Execution Loop

每个 Task 执行时：

1. Inspect：读取相关源文件、类似实现、公共组件/工具、API、i18n、时间工具、权限逻辑和配置；codegraph 只作定位。
2. Micro-plan：说明本次小改动、影响文件和验证方式。
3. Modify：只做当前 Task 所需最小修改。
4. Self-check：检查 Scope、Out of Scope、i18n、UTC、权限、API 兼容、测试需要和 minimal diff。
5. Update CURRENT：记录完成内容、验证结果、风险和下一步。

## Artifact Hygiene

- `/do` 的临时过程文件、codegraph 输出、子 Agent 过程产物放 `.ai/tmp/`。
- 需要保留的验证摘要、截图、报告放 `.ai/artifacts/`。
- 文件名带日期和用途，例如 `.ai/tmp/2026-05-28-codegraph-sync.log`。
- 不在 `.ai` 根目录生成临时文件、日志、截图、JSON、TXT、trace 或 dump。
- `.ai/CURRENT.md` 只记录摘要和相对路径，不保存完整输出。

## Continuous Execution

当以下条件满足时，`/do` 应连续执行当前需求 Tasks：

- 用户已确认进入 `/do`。
- 阻塞项已解除。
- 必要业务规则、数据库和权限确认已完成。
- `.ai/CURRENT.md` 中没有 active Pause Point。

禁止每完成一个小任务、每更新 CURRENT、每修改一个文件就暂停。

## Build/Test/Lint Authorization

默认在准备运行 build/test/lint 前暂停确认。

如果用户本轮已明确授权，例如“准许 build/test/lint”“完成后运行 build/test/lint”“完成整个需求后执行验证并进入 /check”，则：

- 完成全部未阻塞 Tasks 后，可以连续执行已授权命令。
- 执行前仍需简要说明将运行哪些命令。
- 不需要再次暂停确认。
- build/test/lint 完成后继续进入 `/check`。
- 不在 build/test/lint 和 `/check` 之间制造人工暂停。

未明确授权时，完成 Tasks 后暂停，给出建议命令并等待确认。

禁止未授权运行；禁止没运行却声称通过；失败后不得假装成功验收。

## Mandatory Pause Conditions

只有以下情况必须暂停：

- 新阻塞项或业务规则冲突。
- 需要新增或升级依赖。
- 数据库结构新增/重构或 migration 未确认。
- 需要大规模架构重构。
- i18n / UTC / 时区影响突然扩大。
- `.ai/CURRENT.md` Scope 明显不足。
- 需要运行 build/test/lint 且用户尚未明确授权。
- 子 Agent 意见冲突。
- 高风险数据兼容问题。
- 继续执行会明显扩大需求范围。

## Next Stage

全部 Tasks 完成后，根据验证授权进入 build/test/lint，然后进入 `/check`；未授权时暂停等待确认。
