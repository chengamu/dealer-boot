## 6. /plan

根据用户需求制定计划，不直接写代码。

执行时：

1. 读取默认上下文文件。
2. 必要时使用 codegraph 分析影响范围，并读取真实源文件确认。
3. 必要时咨询匹配的子 Agent；不确定时先由主 Agent 分析。
4. 把计划写入 `.ai/CURRENT.md`。

### Clarification Gate

`/plan` 不是总是直接输出计划。在生成详细计划前，必须先判断需求是否已经足够明确。

优先判断：

- 用户目标是否明确。
- Scope 是否明确。
- 是否存在多个明显实现方向。
- 是否涉及数据库结构。
- 是否涉及架构调整。
- 是否涉及权限模型。
- 是否涉及 i18n / UTC / 时间语义。
- 是否涉及异步/同步策略。
- 是否涉及性能或大数据量。
- 是否涉及兼容旧行为。
- 是否缺少关键输入条件。

如果需求已经足够明确，可以直接进入完整 `/plan`。

如果需求不明确，不要直接生成完整计划，先输出：

- 当前理解。
- 缺失信息。
- 风险点。
- 最多 3～5 个关键问题。

等用户确认后再继续完整 `/plan`。

禁止：不要为了推进流程而猜测关键业务逻辑；不要默认数据库结构、权限策略、导出、分页、筛选、异步等业务规则；不要因为存在类似功能就直接复制方案。

允许：对不影响第一步实现的小问题做临时假设，但必须在 `.ai/CURRENT.md` 的 Assumptions 中明确写出。

### Fast Path

如果用户需求已经包含明确目标、明确范围、明确参考实现和明确约束，则允许跳过 Clarification Gate，直接生成完整计划。

例如：“在订单列表增加导出 Excel 按钮，导出当前筛选结果，格式参考用户列表导出，不需要异步任务。” 这种需求可以直接 `/plan`。

### Plan Revision

如果执行过程中出现以下任一情况：

- Scope 变化。
- 需求变化。
- 任务顺序变化。
- 发现原方案不可行。
- 出现新的技术约束。
- 出现新的数据库 / i18n / UTC / 权限影响。

则必须回到 `/plan`，更新 `.ai/CURRENT.md`，并记录：

- 原方案。
- 修改原因。
- 新方案。
- 影响范围。
- Acceptance Criteria 是否变化。
- Risks 是否变化。

禁止：

- 不更新 `.ai/CURRENT.md` 就擅自改变计划。
- 不记录原因就重排 Tasks。
- 不更新 Acceptance 就扩大 Scope。

`.ai/CURRENT.md` 计划至少包含：

- Status：Planning
- Requirement：用户请求、背景、目标、Scope、Out of Scope、假设、开放问题
- Current Implementation Analysis：相关模块、文件、现有模式、可复用工具、约束、子 Agent 结论
- Design：推荐方案、备选方案、Data/API/Frontend/Backend/i18n/UTC 影响
- Tasks：小而清晰的任务，包含 Owner、Files、Validation
- Acceptance Criteria：功能、UI、API、权限、i18n、UTC、回归
- Risks 和 Pause Points

如果开放问题会影响实现，必须暂停问用户；如果不影响第一步，可以记录假设后继续。

输出：需求摘要、影响范围、推荐方案、任务拆分、验收标准、风险和暂停点、是否可进入 `/do`。

### Plan Lock / Execution Gate

`/plan` 完成后，如果 `.ai/CURRENT.md` 的 Next Step、Pause Points、Open Questions、Risks 中出现以下任一情况，必须等待用户确认，不能直接进入 `/do`：

- 写明“等待用户确认”。
- 需要确认业务规则。
- 需要确认平台租户 ID、默认租户、系统租户、初始化数据。
- 涉及 SQL、baseline、migration、初始化数据。
- 涉及数据库结构或租户上下文。
- 涉及权限模型、角色、菜单、按钮权限。
- 涉及 i18n / UTC / 时区影响不明确。
- 涉及任务顺序存在争议。
- 涉及“第一步建议先做 XXX”。

如果这些门禁存在，`/plan` 输出必须明确列出阻塞项和需要用户确认的问题；确认前不得修改代码。
