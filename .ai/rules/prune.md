## 11. /prune

`/prune` 是 Memory Tiering，用于让 `.ai/MEMORY.md` 长期保持轻量。

## Memory Tiers

- Hot Memory：`.ai/MEMORY.md`，只保留最近 3～5 个任务摘要、长期规则、长期风险、项目模式、子 Agent 使用经验、长期决策。
- Warm Memory：`.ai/archive/YYYY-MM.md`，保存较老但仍可能有参考价值的月度任务归档。
- Cold Memory：更老历史，允许继续压缩或只保留索引，默认不读取。

## Rules

- 不修改业务代码。
- 不清空 `.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/MEMORY.md`。
- 不删除 `.ai/archive/`。
- 将记忆按 Hot -> Warm -> Cold 降温，而不是简单删除。
- 保留关键决策、项目模式、仍未解决风险、重复踩坑经验、最近 3～5 个任务短归档。
- 更早任务历史移动到 `.ai/archive/YYYY-MM.md`。
- 经验已变成永久规则时，只建议升级到 `.ai/RULES.md` 或 `AGENTS.md`；未经用户确认不自动升级。

## Memory Hygiene

禁止在 `.ai/MEMORY.md` 保存长过程、完整日志、完整 browser trace、完整 network dump、完整 build output 或完整 validation trace。

允许保存摘要、风险、Lessons、Patterns、Decisions。
