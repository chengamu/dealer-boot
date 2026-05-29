# AI 工作流规则

## 主命令

- `/plan`：自动执行需求沉淀与方案规划。按需加载 `spec.md` 和 `plan.md`。
- `/do`：连续执行任务，并自动进行 do/check 循环。按需加载 `do.md` 和 `check.md`，必要时加载 `compact.md` / `tooling.md`。
- `/archive`：自动提炼经验并归档。按需加载 `archive.md`、`learn.md` 和 `compact.md`。

日常只使用 `/plan`、`/do`、`/archive`。内部规则分层保存，按需加载，不一次性读取全部规则。

## 内部规则

- `spec.md`：`/plan` 的内部前置阶段，负责需求发现、方案探索和 requirements（长期需求）沉淀。
- `plan.md`：方案拆解、Owner（任务负责人 / 子 Agent 角色）分配、任务生成。
- `do.md`：连续执行任务。
- `check.md`：`/do` 的内部验收阶段。
- `compact.md`：`/do`、`/check`、`/archive` 的内部 Compact（上下文压缩）阶段。
- `learn.md`：`/archive` 的内部经验提炼阶段。
- `archive.md`：Archive（历史归档）阶段。
- `tooling.md`：子 Agent、CodeGraph、工具调用规则。

## 命令索引

- `/plan` -> `.ai/rules/spec.md` + `.ai/rules/plan.md`
- `/do` -> `.ai/rules/do.md` + `.ai/rules/check.md`，必要时加载 `.ai/rules/compact.md` / `.ai/rules/tooling.md`
- `/archive` -> `.ai/rules/archive.md` + `.ai/rules/learn.md` + `.ai/rules/compact.md`

## Lazy Loading：按需加载策略

默认只读取：

- `AGENTS.md`
- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/MEMORY.md`

默认不要读取：

- `.ai/rules/*.md` 全量
- `.ai/requirements/*.md` 全量
- `.ai/playbooks/*.md` 全量
- `.ai/archive/*.md` 全量

按命令加载：

- `/plan` 只加载 `.ai/rules/spec.md` 和 `.ai/rules/plan.md`，并读取当前需求相关的 requirements（长期需求）。
- `/do` 只加载 `.ai/rules/do.md` 和 `.ai/rules/check.md`；需要子 Agent 或 CodeGraph 时再加载 `.ai/rules/tooling.md`；上下文超限时再加载 `.ai/rules/compact.md`。
- `/archive` 只加载 `.ai/rules/archive.md`、`.ai/rules/learn.md` 和 `.ai/rules/compact.md`。

按问题加载 Playbook（可复用排错手册）：

- Maven / jar / compile / runtime -> `.ai/playbooks/java-build.md`
- Vue / TS / frontend build -> `.ai/playbooks/frontend-build.md`
- i18n -> `.ai/playbooks/i18n.md`
- UTC / timezone / date -> `.ai/playbooks/utc.md`
- code generator -> `.ai/playbooks/generator.md`
- tenant / permission / scope -> `.ai/playbooks/tenant.md`

Archive（历史归档）默认不读取。只有当前问题明确需要历史经验时，才按关键词读取相关归档摘要。

## 文件职责

- `.ai/requirements/*.md`：长期需求、产品规格、业务规则、验收标准。
- `.ai/CURRENT.md`：当前阶段状态摘要，不放长期需求全文。
- `.ai/TASKS.md`：当前任务队列、Owner（任务负责人 / 子 Agent 角色）、Scope（任务边界）、Files（相关文件）、Validation（验证方式）、Short Notes（简短记录）。
- `.ai/HANDOFF.md`：交接摘要。
- `.ai/DECISIONS.md`：长期设计决策。
- `.ai/MEMORY.md`：高价值、短小、可复用经验。
- `.ai/playbooks/*.md`：某类问题的 Playbook（可复用排错手册）。
- `.ai/archive/*.md`：历史执行过程、验证摘要、失败原因、完整归档。
- `.ai/tmp/`：临时过程文件。
- `.ai/artifacts/`：验证产物、截图、报告、日志摘要。
- `.ai/rules/*.md`：工作流规则，按需加载。

## 核心约束

- 默认中文回答；代码、变量名、方法名保持英文。
- 先理解需求和现有实现，再修改代码。
- 保持 minimal diff（最小必要改动），不做无关重构。
- 不升级依赖、不修改数据库结构或 migration，除非用户明确确认。
- 遵守项目 i18n、UTC / 时间语义、权限、租户和数据库规则。
- build/test/lint 需要授权；未执行不能声称通过。
- 不把长日志、完整 trace、完整 build/test 输出、子 Agent 长篇过程写入 CURRENT / TASKS / MEMORY。
- 不确定内容写 TODO，不猜测不存在的框架能力。

## 产物归类

`.ai` 根目录只保留核心文件和核心目录：

- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/HANDOFF.md`
- `.ai/DECISIONS.md`
- `.ai/MEMORY.md`
- `.ai/rules/`
- `.ai/requirements/`
- `.ai/playbooks/`
- `.ai/archive/`
- `.ai/tmp/`
- `.ai/artifacts/`

归类规则：

- 临时过程文件 -> `.ai/tmp/`
- 验证产物、截图、报告、日志摘要 -> `.ai/artifacts/`
- 长期需求 -> `.ai/requirements/`
- 冷历史归档 -> `.ai/archive/`
- 规则文件 -> `.ai/rules/`
- 排查手册 -> `.ai/playbooks/`

禁止在 `.ai` 根目录生成散乱的 `*.log`、`*.tmp`、`*.json`、`*.txt`、截图、验证报告、browser trace、network dump。
