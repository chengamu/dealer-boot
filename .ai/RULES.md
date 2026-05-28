# AI Rules

## Basic Rules

- 默认中文回答；代码、变量名、方法名保持英文。
- 开始任务前默认读取：`AGENTS.md`、`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- 默认不读取 `.ai/archive/` 和 `.ai/rules/` 下的详细规则，除非用户明确要求查历史、恢复旧任务、追溯过去决策，或正在执行对应命令。
- 只有执行对应命令时，才读取 `.ai/rules/<command>.md`。
- 先理解需求和现有实现，再修改代码。
- 保持 minimal diff，不做无关重构。
- 不升级依赖，除非用户明确要求。
- 不修改数据库结构或 migration，除非用户明确要求。
- 不删除用户代码或已有注释，除非任务明确要求。
- 没有执行过的命令，不能声称执行过。
- 不确定内容写 TODO，不要猜。
- 涉及跨模块、数据库、依赖升级、架构调整、i18n、UTC、时区、日期格式时，必须先分析影响范围并写入 `.ai/CURRENT.md`。
- 准备运行 build/test/lint 前，必须先说明准备运行什么，并按 `.ai/rules/do.md` 的授权规则执行。
- CodeGraph 和 Sub-Agent 详细规则见 `.ai/rules/tooling.md`，仅在需要使用对应工具或 Agent 时读取。

## Context Size Control

`.ai` 目录必须保持轻量，避免每次新对话读取时上下文膨胀。

- `.ai/RULES.md` 只保留轻量入口、默认读取策略和 Commands Index。
- `.ai/rules/*.md` 保存详细规则，只在执行对应命令或使用对应工具时读取。
- `.ai/CONTEXT.md` 只放稳定项目事实、关键入口、重要命令。
- `.ai/CURRENT.md` 只放当前任务摘要、结果、风险和下一步；超过约 200 行时建议执行 `/compact`。
- `.ai/MEMORY.md` 只放长期经验、关键决策、未解决风险、项目模式和最近 3～5 个短归档；超过约 300 行时建议执行 `/prune`。
- `.ai/archive/` 是冷历史目录，默认不读取。
- 不把详细调用图、完整文件树、长日志、完整 browser trace、完整 network dump、完整 build/test 输出或子 Agent 长篇过程写入活跃上下文。

默认只读取：

- `AGENTS.md`
- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/MEMORY.md`

只有在执行对应命令时，才读取：

- `.ai/rules/init.md`
- `.ai/rules/plan.md`
- `.ai/rules/do.md`
- `.ai/rules/check.md`
- `.ai/rules/compact.md`
- `.ai/rules/archive.md`
- `.ai/rules/prune.md`

## Commands

- /init -> see `.ai/rules/init.md`
- /plan -> see `.ai/rules/plan.md`
- /do -> see `.ai/rules/do.md`
- /check -> see `.ai/rules/check.md`
- /compact -> see `.ai/rules/compact.md`
- /archive -> see `.ai/rules/archive.md`
- /prune -> see `.ai/rules/prune.md`
- /tooling -> see `.ai/rules/tooling.md`

执行任一命令时，必须先读取对应 `.ai/rules/<command>.md`，不能只凭本文件摘要执行。
