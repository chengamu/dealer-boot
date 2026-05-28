## 6. /plan

`/plan` 根据用户需求制定计划，不直接写代码。

执行时读取默认上下文；必要时用 codegraph 定位影响范围，并读取真实源文件确认；必要时咨询匹配的子 Agent。计划写入 `.ai/CURRENT.md`。

## Clarification Gate

生成详细计划前，先判断需求是否足够明确：

- 目标和 Scope 是否明确。
- 是否存在多个明显实现方向。
- 是否涉及数据库结构、架构调整、权限模型、i18n、UTC / 时间语义。
- 是否涉及异步/同步策略、性能、大数据量、兼容旧行为。
- 是否缺少关键输入条件。

如果需求明确，可以直接进入完整 `/plan`。

如果需求不明确，先说明当前理解、缺失信息、风险点和最多 3～5 个关键问题，等待用户确认。

禁止为了推进流程而猜测关键业务逻辑、数据库结构、权限策略、导出、分页、筛选、异步等规则。

允许对不影响第一步实现的小问题做临时假设，但必须写入 `.ai/CURRENT.md` 的 Assumptions。

## Fast Path

如果用户需求已经包含明确目标、范围、参考实现和约束，可以跳过 Clarification Gate，直接生成计划。

## Plan Revision

执行过程中如出现以下情况，必须回到 `/plan` 并更新 `.ai/CURRENT.md`：

- Scope、需求或任务顺序变化。
- 原方案不可行。
- 出现新的技术约束。
- 出现新的数据库 / i18n / UTC / 权限影响。

必须记录原方案、修改原因、新方案、影响范围，以及 Acceptance Criteria / Risks 是否变化。

禁止不更新 CURRENT 就改变计划、重排 Tasks 或扩大 Scope。

## Current Plan Content

`.ai/CURRENT.md` 的计划只保留足够执行的信息：

- Requirement：目标、Scope、Out of Scope、Assumptions、Open Questions。
- Current Implementation Analysis：相关模块、文件、现有模式、约束。
- Design：推荐方案和关键影响。
- Tasks：小而清晰，包含标题、Owner、Validation；必要时写 Files。
- Acceptance Criteria、Risks、Pause Points、Next Step。

## Tasks

Tasks 必须可被 `/do` 按 Owner 调度。

Owner：

- `frontend-developer`：前端页面、组件、样式、交互。
- `typescript-pro`：TypeScript 类型和复杂逻辑。
- `java-architect`：Java 后端、Service、Mapper、权限、事务、架构影响。
- `code-reviewer`：最终静态审查和验收。
- `main`：跨领域协调、规则维护、简单任务。

规则：

- `/do` 必须按 Owner 调用对应执行人。
- 不允许忽略 Owner；调整 Owner 必须记录原因。
- 非浏览器 Agent 不可用时记录 fallback。
- 浏览器验证使用 Codex 内置 Browser / Chrome；不可用时记录未执行原因和 Remaining Risks。

## Execution Gate

`/plan` 后如 `.ai/CURRENT.md` 的 Next Step、Pause Points、Open Questions 或 Risks 中存在以下情况，必须等待用户确认，不能直接进入 `/do`：

- 写明等待用户确认。
- 需要确认业务规则、任务顺序或第一步策略。
- 涉及 SQL、baseline、migration、初始化数据。
- 涉及数据库结构、租户上下文、权限模型、角色、菜单、按钮权限。
- i18n / UTC / 时区影响不明确。

确认前不得修改代码。
