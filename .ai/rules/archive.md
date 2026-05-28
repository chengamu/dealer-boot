## 10. /archive

`/archive` 是 Spec Finalization，用于当前任务完成、取消、阻塞或延期后归档当前任务。

目标是形成可追溯的最终工程结论、验收状态、风险记录、Follow-up 和 Lessons；不是简单清理 `.ai/CURRENT.md`。

## Final Status

归档前必须生成 Final Status：

- Accepted
- Accepted with Risks
- Partial
- Blocked
- Cancelled
- Deferred

禁止未验证却写 Accepted；有高风险未验证项时不能写 Accepted。

## Closure Gate

只有以下条件全部满足，才允许 `Final Status = Accepted`：

- Acceptance Criteria 已满足。
- Runtime / API / Browser / Regression Validation 已完成，或明确说明不适用。
- 没有高风险 blocker。

否则使用 Accepted with Risks、Partial、Blocked、Cancelled 或 Deferred。

## Archive Summary

归档摘要必须包含：

- Feature
- Final Status
- Scope / Out of Scope
- Completed / Not Completed
- Validation Summary
- Remaining Risks
- Follow-up
- Lessons
- Key Decisions
- Files Modified

如果存在 Deferred 项、未完成验证、技术债、后续优化、性能问题或浏览器兼容问题，必须写入 Follow-up。

## Hygiene

- 不修改业务代码。
- 不清空 `.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/MEMORY.md`。
- 从 `.ai/CURRENT.md` 提取摘要追加到 `.ai/MEMORY.md` 的 Archive。
- `.ai/MEMORY.md` 只保留摘要，不写长日志、browser trace、network dump、console output、build output 或子 Agent 长篇过程。
- 可复用经验写入 Lessons 或 Project Patterns。
- 未解决风险写入 Known Risks。
- MEMORY 较大时保留最近 3～5 个任务短摘要，旧归档移动到 `.ai/archive/YYYY-MM.md`。
- 不把未验证事项写成已验证。
- 不删除 `.ai/archive/` 冷历史。
- archive 后 `.ai/CURRENT.md` 恢复最小状态：

```md
# Current

## Status

No active task.
```
