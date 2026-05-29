# 记忆清理规则

`/prune` 用于 `.ai/MEMORY.md` 和归档索引的 memory tiering（记忆分层）。

它不是日常主命令。只有 `.ai/MEMORY.md` 明显变大时才使用。

## 记忆分层

- Hot：`.ai/MEMORY.md`，保留最近 3 到 5 个任务摘要、长期决策、长期风险、项目模式和子 Agent 使用经验。
- Warm：`.ai/archive/YYYY-MM.md`，保留较旧任务归档。
- Cold：更早历史可以压缩或只保留索引，默认不读取。

## 规则

- 不修改业务代码。
- 不删除活跃 requirements。
- 不删除 DECISIONS。
- 不删除仍被引用的 artifacts。
- 不在 MEMORY 保存完整日志、trace、build output 或长验证输出。
- 将较旧任务历史移动到 archive，避免 MEMORY 变成历史日志库。
- 如果某条记忆已经变成永久规则，只建议升级到 RULES 或 AGENTS；未经确认不自动升级。
