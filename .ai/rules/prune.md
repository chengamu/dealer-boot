## 11. /prune

`/prune` 是 Memory Tiering。

它不只是删除 MEMORY，而是把记忆分层，按价值和活跃度从 Hot 降到 Warm，再降到 Cold。

目标：

- `.ai/MEMORY.md` 长期保持轻量。
- `.ai/MEMORY.md` 不成为历史日志仓库。
- 有参考价值但不活跃的历史进入 archive。
- 更老历史默认不读取。

### Hot Memory

`.ai/MEMORY.md` 只保留：

- 最近 3～5 个任务摘要
- 长期规则
- 长期风险
- 项目模式
- 子 Agent 使用经验
- 长期决策

### Warm Memory

`.ai/archive/YYYY-MM.md` 保存：

- 月度任务归档
- 较老任务
- 不再活跃但可能以后有参考价值的内容

### Cold Memory

更老历史：

- 允许继续压缩
- 允许只保留索引
- 默认不读取

执行时：

1. 不修改业务代码。
2. 不清空 `.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/MEMORY.md`。
3. 不删除 `.ai/archive/`。
4. 检查 `.ai/MEMORY.md` 是否存在重复、过期、低价值内容。
5. 将记忆按 Hot -> Warm -> Cold 降温，而不是简单删除。
6. 保留关键决策、项目模式、仍未解决风险、重复踩坑经验、最近 3～5 个任务短归档。
7. 更早的任务历史移动到 `.ai/archive/YYYY-MM.md`。
8. 如果某条 `.ai/MEMORY.md` 经验已经变成永久规则，建议升级到 `.ai/RULES.md` 或 `AGENTS.md`，但不要自动升级，除非用户确认。
9. 清理后确保 `.ai/MEMORY.md` 仍包含 Decisions、Lessons、Known Risks、Project Patterns、Sub-Agent Notes、Archive。

### Memory Hygiene

禁止在 `.ai/MEMORY.md` 保存：

- 长过程
- 完整日志
- 完整 browser trace
- 完整 network dump
- 完整 build output

允许保存：

- 摘要
- 风险
- Lessons
- Patterns
- Decisions

输出：

1. 降温了什么。
2. 移动到 `.ai/archive/` 的内容。
3. 保留了什么。
4. 是否有建议升级为规则的经验。
5. 当前 `.ai/MEMORY.md` 是否仍偏大。
