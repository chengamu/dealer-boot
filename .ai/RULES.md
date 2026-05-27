# AI Rules

## 1. Basic Rules

- 默认中文回答；代码、变量名、方法名保持英文。
- 开始任务前默认读取：`AGENTS.md`、`.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/CURRENT.md`、`.ai/MEMORY.md`。
- 默认不读取 `.ai/archive/` 和 `.ai/rules/` 下的命令详情，除非用户明确要求查历史、恢复旧任务、追溯过去决策，或正在执行对应命令。
- 只有执行对应命令时，才读取 `.ai/rules/<command>.md`。
- 先理解需求和现有实现，再修改代码。
- 保持 minimal diff，不做无关重构。
- 不升级依赖，除非用户明确要求。
- 不修改数据库结构或 migration，除非用户明确要求。
- 不删除用户代码或已有注释，除非任务明确要求。
- 没有执行过的命令，不能声称执行过。
- 不确定内容写 TODO，不要猜。
- 涉及跨模块、数据库、依赖升级、架构调整、i18n、UTC、时区、日期格式时，必须先分析影响范围并写入 `.ai/CURRENT.md`。
- 准备运行 build/test/lint 前，必须先说明准备运行什么，等待用户确认或根据 `AGENTS.md` 规则暂停。

## 2. Context Size Control

`.ai` 目录必须保持轻量，避免每次新对话读取时上下文膨胀。

- `.ai/RULES.md` 只保留长期基础规则、简版上下文策略、简版工具/Agent 规则和命令索引。
- `.ai/rules/*.md` 保存各命令详细规则，只在执行对应命令时读取。
- `.ai/CONTEXT.md` 只放稳定项目事实、关键入口、重要命令，不记录详细调用图、完整文件树、过程日志。
- `.ai/CURRENT.md` 是当前任务工作区；超过约 200 行时建议执行 `/compact`。
- `.ai/MEMORY.md` 只放长期经验、关键决策、未解决风险、项目模式、最近 3～5 个任务短归档；超过约 300 行时建议执行 `/prune`。
- `.ai/archive/` 是冷历史目录，默认不读取，除非用户明确要求查历史、恢复旧任务或追溯过去决策。
- 不把 codegraph 详细调用图、完整文件树、子 Agent 长篇过程写入活跃上下文。
- 当前需求、计划、任务、验收、交接写入 `.ai/CURRENT.md`；稳定事实写入 `.ai/CONTEXT.md`；长期经验和风险写入 `.ai/MEMORY.md`。

- `/compact` 是 Working Context Squash，详细规则见 `.ai/rules/compact.md`。
- `/prune` 是 Memory Tiering，详细规则见 `.ai/rules/prune.md`。

### Default Reading Policy

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

## 3. CodeGraph Usage

- 项目已安装 codegraph。
- 分析代码结构、调用关系、影响范围时，优先使用 codegraph。
- codegraph 结果只作为定位依据，最终修改前必须读取真实源文件确认。
- 如果 codegraph 结果和真实源文件不一致，以真实源文件为准，并在 `.ai/MEMORY.md` 记录该风险。
- 不把 codegraph 能实时查询到的大量调用关系、文件树、符号图复制进 `.ai` 文件。

## 4. Sub-Agent Usage

### Available Sub Agents

- frontend-developer：前端页面、组件、交互、样式、接口接入。
- typescript-pro：TypeScript 类型、复杂 TS 逻辑、类型安全、组合式函数。
- java-architect：Java 后端、Spring、接口、Service、Mapper、架构影响分析。
- code-reviewer：`/check` 阶段通用代码审查、质量检查、回归风险检查、验收确认。
- browser-debugger：可选浏览器验证执行器；只有 `chrome_devtools MCP server` 可用时才使用，不要求为了它启动 MCP 服务，除非用户明确要求。

### Dispatch Rules

- 主 Agent 负责统一流程、上下文、状态更新和最终结果。
- 子 Agent 只负责专业分析或专业执行，不直接扩大任务范围。
- 不要为了调用而调用子 Agent；只有当前任务确实匹配其专业范围时才调用。
- 子 Agent 结论需要压缩后写入 `.ai/CURRENT.md`，不要粘贴长篇过程。
- 子 Agent 不能绕过 `AGENTS.md`、`.ai/RULES.md`、命令详情规则、i18n、UTC、minimal diff、暂停规则。
- 如果多个子 Agent 意见冲突，主 Agent 记录冲突点并暂停，不要强行猜测。
- 子 Agent 不能擅自升级依赖、修改数据库结构、修改 migration 或扩大 Scope。

## Commands

- /init -> see `.ai/rules/init.md`
- /plan -> see `.ai/rules/plan.md`（includes Plan Revision）
- /do -> see `.ai/rules/do.md`
- /check -> see `.ai/rules/check.md`
- /compact -> see `.ai/rules/compact.md`
- /archive -> see `.ai/rules/archive.md`
- /prune -> see `.ai/rules/prune.md`
