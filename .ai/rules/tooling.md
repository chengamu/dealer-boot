## Tooling Rules

本文件保存 CodeGraph 和 Sub-Agent 详细规则。默认不读取；只有需要使用 codegraph 或子 Agent 时才按需读取。

## CodeGraph Usage

- 项目已安装 codegraph。
- 分析代码结构、调用关系、影响范围时，优先使用 codegraph。
- codegraph 结果只作为定位依据，最终修改前必须读取真实源文件确认。
- 如果 codegraph 结果和真实源文件不一致，以真实源文件为准，并在 `.ai/MEMORY.md` 记录该风险。
- 不把 codegraph 能实时查询到的大量调用关系、文件树、符号图复制进 `.ai` 文件。

## Sub-Agent Usage

### Available Sub Agents

- frontend-developer：前端页面、组件、交互、样式、接口接入。
- typescript-pro：TypeScript 类型、复杂 TS 逻辑、类型安全、组合式函数。
- java-architect：Java 后端、Spring、接口、Service、Mapper、架构影响分析。
- code-reviewer：`/check` 阶段通用代码审查、质量检查、回归风险检查、验收确认。
- browser-debugger：历史可选浏览器验证执行器；Browser Validation 当前以 `.ai/rules/check.md` 为准，优先使用 Codex 内置 Browser / Chrome 工具，不保留 MCP 依赖。

### Dispatch Rules

- 主 Agent 负责统一流程、上下文、状态更新和最终结果。
- 子 Agent 只负责专业分析或专业执行，不直接扩大任务范围。
- 不要为了调用而调用子 Agent；只有当前任务确实匹配其专业范围时才调用。
- 子 Agent 结论需要压缩后写入 `.ai/CURRENT.md`，不要粘贴长篇过程。
- 子 Agent 不能绕过 `AGENTS.md`、`.ai/RULES.md`、命令详情规则、i18n、UTC、minimal diff、暂停规则。
- 如果多个子 Agent 意见冲突，主 Agent 记录冲突点并暂停，不要强行猜测。
- 子 Agent 不能擅自升级依赖、修改数据库结构、修改 migration 或扩大 Scope。
