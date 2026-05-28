## 9. /compact

`/compact` 是 Working Context Squash，用于把 `.ai/CURRENT.md` 压缩成“另一个 Agent 明天接手也能继续工作”的最小必要上下文。

## CURRENT Hygiene

`.ai/CURRENT.md` 只保存：

- 当前目标
- 当前状态
- blockers
- 已完成 / 未完成
- Validation Result
- Remaining Risks
- 下一步

禁止保存长日志、长 reasoning、完整 browser trace、完整 network dump、完整 build/test 输出、长 code-reviewer 输出或已废弃过程。

长输出只记录关键错误和文件位置。

## Rules

- 不修改业务代码。
- 不清空 `.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/MEMORY.md`。
- 把可复用经验、重复踩坑、关键决策提炼到 `.ai/MEMORY.md`。
- 不删除仍未解决的问题。
- 压缩后 CURRENT 必须仍能支撑下一步继续工作。
- `/compact` 可以清理 `.ai/tmp/` 中无引用的临时产物。
- `.ai/artifacts/` 中长期无用的产物可移动到 `.ai/archive/` 或删除；删除前确认无引用。
