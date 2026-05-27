## 10. /archive

`/archive` 是 Spec Finalization。

它用于当前任务已经完成、取消、阻塞或延期后归档；目标不是简单清理 `.ai/CURRENT.md`，而是形成可追溯的最终工程结论、验收状态、风险记录、Follow-up、Lessons 和历史摘要。

`/archive` 只归档当前任务，不清空整个 `.ai/` 目录。

## Archive Status

归档前必须生成 Final Status。

允许状态：

- Accepted
- Accepted with Risks
- Partial
- Blocked
- Cancelled
- Deferred

禁止：

- 没有最终状态就 archive。
- 未验证却写 Accepted。
- 有高风险未验证项却写 Fully Accepted。

## Spec Closure Rules

只有以下条件全部满足时，才允许 `Final Status = Accepted`：

- Acceptance Criteria 已满足。
- Runtime Validation 已完成。
- API Validation 已完成。
- Browser Validation 已完成，或已明确说明未完成原因。
- Regression Validation 已完成，或已明确说明未完成原因。
- 没有高风险 blocker。

否则必须使用以下状态之一：

- Accepted with Risks
- Partial
- Blocked
- Cancelled
- Deferred

## Archive Summary

归档摘要必须包含：

### Feature

需求名称。

### Final Status

最终状态。

### Scope

最终实际完成范围。

### Out of Scope

明确未做内容。

### Completed

已完成项。

### Not Completed

未完成项。

### Validation Summary

- Runtime Validation
- API Validation
- Browser Validation
- Regression Validation

### Remaining Risks

剩余风险。

### Follow-up

后续建议任务。

### Lessons

可复用经验。

### Key Decisions

关键决策。

### Files Modified

关键修改文件。

## Follow-up Tasks

如果 archive 时发现以下内容，必须写入 `### Follow-up`：

- Deferred 项。
- 未完成验证。
- 技术债。
- 后续优化点。
- 性能问题。
- 浏览器兼容问题。

禁止：

- archive 后完全丢失后续事项。
- 只写 Remaining Risks 不写 Follow-up。

## Archive Hygiene

执行时：

1. 不修改业务代码。
2. 不清空 `.ai/RULES.md`。
3. 不清空 `.ai/CONTEXT.md`。
4. 不清空 `.ai/MEMORY.md`。
5. 从 `.ai/CURRENT.md` 提取当前任务最终摘要，先追加到 `.ai/MEMORY.md` 的 Archive 部分。
6. `.ai/MEMORY.md` 只保留摘要，不写长日志、browser trace、network dump、console output、build output 或子 Agent 长篇过程。
7. 可复用经验同步写入 `.ai/MEMORY.md` 的 Lessons 或 Project Patterns。
8. 未解决风险保留到 `.ai/MEMORY.md` 的 Known Risks。
9. 如果 `.ai/MEMORY.md` 已经较大，保留最近 3～5 个任务短摘要，把更早归档移动到 `.ai/archive/YYYY-MM.md`。
10. 不把未验证事项写成已验证。
11. 不删除 `.ai/archive/` 中的冷历史。
12. archive 后 `.ai/CURRENT.md` 必须恢复最小状态。

No active task 模板：

```md
# Current

## Status

No active task.
```

## Output

`/archive` 输出：

1. 已归档的任务。
2. Final Status。
3. 归档写入位置。
4. 是否移动旧归档到 `.ai/archive/YYYY-MM.md`。
5. 已保留的风险。
6. 已沉淀的经验。
7. Follow-up。
8. 当前是否可以开始新任务。
