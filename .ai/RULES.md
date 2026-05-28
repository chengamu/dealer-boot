# AI Rules

## Basic Rules

- 默认中文回答；代码、变量名、方法名保持英文。
- 先理解需求和现有实现，再修改代码。
- 保持 minimal diff，不做无关重构。
- 不升级依赖、修改数据库结构或 migration，除非用户明确要求。
- 不删除用户代码或已有注释，除非任务明确要求。
- 没有执行过的命令，不能声称执行过。
- 不确定内容写 TODO，不要猜。
- 涉及跨模块、数据库、依赖升级、架构调整、i18n、UTC、时区、日期格式时，先分析影响范围并写入 `.ai/CURRENT.md`。
- 准备运行 build/test/lint 前，先说明命令，并按 `.ai/rules/do.md` 的授权规则执行。
- CodeGraph 和 Sub-Agent 细则见 `.ai/rules/tooling.md`，仅按需读取。

## Default Reading

默认读取：

- `AGENTS.md`
- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/MEMORY.md`

默认不读取 `.ai/archive/` 和 `.ai/rules/`；执行对应命令或明确查历史时再读取。

## Context Size Control

- `.ai/RULES.md` 只保留轻入口、默认读取策略和 Commands Index。
- `.ai/CONTEXT.md` 只放稳定项目事实。
- `.ai/CURRENT.md` 只放当前任务摘要、结果、风险和下一步；过长时执行 `/compact`。
- `.ai/MEMORY.md` 只放长期经验、决策、风险、项目模式和最近短归档；过长时执行 `/prune`。
- 长日志、完整 trace、完整 build/test 输出、详细调用图和子 Agent 长篇过程不进入活跃上下文。

## .ai Artifact Hygiene

`.ai` 根目录只保留核心文件和核心目录：

- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/MEMORY.md`
- `.ai/rules/`
- `.ai/archive/`
- `.ai/tmp/`
- `.ai/artifacts/`

临时过程文件放 `.ai/tmp/`；验证产物、截图、报告、日志摘要放 `.ai/artifacts/`；冷历史放 `.ai/archive/`；规则文件放 `.ai/rules/`。

禁止在 `.ai` 根目录生成 `*.log`、`*.tmp`、`*.json`、`*.txt`、截图、验证报告、browser trace、network dump。`.ai/CURRENT.md` 只记录摘要和相对路径。

## Commands

- /init -> see `.ai/rules/init.md`
- /plan -> see `.ai/rules/plan.md`
- /do -> see `.ai/rules/do.md`
- /check -> see `.ai/rules/check.md`
- /compact -> see `.ai/rules/compact.md`
- /archive -> see `.ai/rules/archive.md`
- /prune -> see `.ai/rules/prune.md`

执行命令前必须读取对应 `.ai/rules/<command>.md`，不能只凭本文件摘要执行。
