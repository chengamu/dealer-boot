# Wave Plan: 共享产品能力中心上线体验收口 Wave

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 3
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only
- DesignContinuityPolicy: ui-execution-contract-first

## UI Continuity Rule

本 Wave 不能把 5 个自定义页的连续体验拆成多个互不知情的局部实现。所有 frontend、review、browser validation 任务必须先读取：

- `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`
- `docs/产品配置中心/产品能力界面设计稿.md`

`PCC-UW-F01` 是 5 个自定义页的设计主线任务，不再拆成多个并行 Agent。其他任务不得重做或覆盖它的主结构。

## Agent Registry

| Agent | Source | Match |
| --- | --- | --- |
| java-architect | `.codex/agents/java-architect.toml` | Java / Spring / SQL / permission / backend contract |
| frontend-developer | `.codex/agents/frontend-developer.toml` | Vue / UI / component / frontend usability |
| typescript-pro | `.codex/agents/typescript-pro.toml` | TypeScript / API typing / frontend contract |
| code-reviewer | `.codex/agents/code-reviewer.toml` | Static Review / quality / maintainability |
| browser-debugger | `.codex/agents/browser-debugger.toml` | Browser / runtime / UI validation |
| main | fallback | orchestration / final checks |

## Wave 0: Contract / Scope / File Boundary

### Task PCC-UW-C01: 冻结上线体验 P0 contract 和 UI 精修验收口径

Owner: frontend-developer
OwnerSource: agent-registry
OwnerReason: 该任务涉及 Vue 页面、组件拆分、设计参考转组件化实现和操作便捷性，匹配 frontend-developer。
AgentRole: frontend-contract
Status: pending
Priority: high
RiskLevel: high，UI 精修是本 Wave P0，验收口径不清会再次退化成 MVP。
Wave: 0

Files:
- `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`
- `.ai/changes/20260607-product-capability-usability-wave/design.md`
- `.ai/changes/20260607-product-capability-usability-wave/wave-plan.md`
- `.ai/changes/20260607-product-capability-usability-wave/check.md`
- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/产品能力界面设计稿.md`

Forbidden:
- `admin-ui/**`
- `bocoo-modules-product/**`
- `sql/**`
- `.ai/archive/**`
- `pom.xml`
- `package.json`
- 生产环境配置

DependsOn: none
ParallelGroup: contract
ConflictBoundary: frontend-contract-docs

Acceptance:
- 产出并冻结 `ui-execution-contract.md`，作为本 Wave UI 执行和验收的单一真相。
- 明确 5 个自定义页的页面布局、关键组件、快捷操作、Vue 组件化重构注意事项和浏览器证据。
- 明确执行 `产品能力界面设计稿.md` 第 2.3 / 2.4 / 5 章：风格锁定、image2-ui 拆解、frontend-design 实现、design-taste 审查、浏览器验收。
- 明确 5 个页面对应组件边界：工作台任务流/队列/快捷操作，配置问题组/问题答案/检查/BOM，价格矩阵/试算/命中明细，发布状态/发布包/阻断任务，销售头卡/Quick Quote/限制/版本历史。
- 明确普通 grid/list 页面和历史页面标准化复查清单。
- 明确 UI 精修为 P0，不允许延期到 P1/P2。

### Task PCC-UW-C02: 冻结报价/审核/缺口/发布/同步/导入 API 和数据 contract

Owner: java-architect
OwnerSource: agent-registry
OwnerReason: 该任务涉及后端 API、Service、权限、SQL 和数据闭环，匹配 java-architect。
AgentRole: backend-contract
Status: done
Priority: high
RiskLevel: high，报价、审核、发布等流程如果只有前端触发而无真实数据闭环，不能上线。
Wave: 0

Files:
- `.ai/changes/20260607-product-capability-usability-wave/design.md`
- `.ai/changes/20260607-product-capability-usability-wave/wave-plan.md`
- `.ai/changes/20260607-product-capability-usability-wave/check.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/共享产品能力中心开发入口.md`

Forbidden:
- `admin-ui/**`
- `bocoo-modules-product/**`
- `sql/**`
- 生产环境配置
- MySQL SQL
- 无计划的依赖升级

DependsOn: none
ParallelGroup: contract
ConflictBoundary: backend-contract-sql

Acceptance:
- 明确报价预览、审核审批、缺口待办、发布包、同步日志、导入中心的 API contract。
- 明确发布检查结果历史噪音的处理方案：批次号、最新批次过滤、去重或明确暂不改结构的替代方案。
- 明确权限码、DTO/VO、分页、错误消息、UTC 和真实库 post-check 要求。

### Task PCC-UW-C03: 冻结真实浏览器验收矩阵和证据口径

Owner: browser-debugger
OwnerSource: agent-registry
OwnerReason: 该任务涉及内部浏览器、真实路径、viewport、console/network 和证据采集，匹配 browser-debugger。
AgentRole: runtime-contract
Status: done
Priority: high
RiskLevel: high，没有真实浏览器证据就无法证明上线体验完成。
Wave: 0

Files:
- `.ai/changes/20260607-product-capability-usability-wave/check.md`
- `.ai/changes/20260607-product-capability-usability-wave/logs/**`
- `.ai/changes/20260607-product-capability-usability-wave/artifacts/**`

Forbidden:
- 业务代码
- 生产环境配置

DependsOn: none
ParallelGroup: contract
ConflictBoundary: validation-contract-only

Acceptance:
- 建立路由矩阵、移动端矩阵、关键交互矩阵和截图/替代证据规则。
- 明确失败时必须记录 `blocked` 或 `failed`，不能用编译通过替代浏览器验收。

## Barrier 0

Result: passed

Checks:
- `ui-execution-contract.md` 已存在并被 Wave Plan / Check / Tasks 引用。
- 本 Wave P0 和 Deferred 已明确。
- UI 精修明确为 P0。
- API / permission / SQL / frontend contract 已有可执行边界。
- 真实浏览器验收矩阵已明确。
- 任务拆分没有把 5 个自定义页按页面并行拆给多个 Agent。

## Wave 1: Independent Implementation

### Task PCC-UW-F01: 5 个自定义页转 Vue 组件化重构、UI 精修和录入效率优化

Owner: frontend-developer
OwnerSource: agent-registry
OwnerReason: 该任务主要把设计文档和静态参考转为 Vue 组件化页面，并优化样式、组件和交互，匹配 frontend-developer。
AgentRole: frontend
Status: done
Priority: high
RiskLevel: high，5 个自定义页是用户感知最强的上线门面。
Wave: 1

Files:
- `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`
- `admin-ui/src/pages/product-center/workbench/**`
- `admin-ui/src/pages/product-center/template/**`
- `admin-ui/src/pages/product-center/pricing/**`
- `admin-ui/src/pages/product-center/publish/**`
- `admin-ui/src/pages/product-center/sales-view/**`
- `admin-ui/src/pages/product-center/components/**`
- `i18n/locales/en_US.json`

Forbidden:
- `bocoo-modules-product/**`
- `sql/**`
- 生产环境配置

DependsOn: PCC-UW-C01,PCC-UW-C03
ParallelGroup: frontend-custom
ConflictBoundary: product-center-custom-pages

Acceptance:
- 必须完整遵守 `ui-execution-contract.md` 第 2、3、4、6、7 节。
- 工作台、配置模板、价格中心、测试发布、销售只读总览完成 Vue 组件化重构，达到可用视觉和操作效率要求。
- 必须按设计文档第 5.4 的页面结构拆组件，页面 SFC 只负责编排和数据流。
- 必须保留设计文档第 5.5 / 6 章定义的快捷操作和减少录入负担交互。
- 底部操作可见，长页面不遮挡，移动宽度不横向溢出。
- 复杂区块组件化，页面 SFC 不写到底。
- 新增文案走 i18n。

### Task PCC-UW-F02: 普通产品能力 grid/list 与历史页面标准化复查整改

Owner: frontend-developer
OwnerSource: agent-registry
OwnerReason: 该任务涉及普通后台页标准化、生成器风格一致性和历史页面 UI 修复，匹配 frontend-developer。
AgentRole: frontend
Status: done
Priority: high
RiskLevel: high，历史标准不统一会继续污染新增页面。
Wave: 1

Files:
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/pages/system/**`
- `admin-ui/src/pages/merchant/**`
- `admin-ui/src/pages/monitor/**`
- `bocoo-modules-generator/src/main/resources/vm/**`
- `i18n/locales/en_US.json`

Forbidden:
- `bocoo-modules-product/**`
- `sql/**`
- 生产环境配置

DependsOn: PCC-UW-C01
ParallelGroup: frontend-standard
ConflictBoundary: standard-grid-pages

Acceptance:
- 产品能力普通页和列入范围的历史页面符合标准 grid/list + right-toolbar + drawer。
- 搜索、重置、分页、抽屉、sticky footer、空态、错误态、权限按钮可用。
- 代码生成器模板不再生成旧长弹窗风格。
- 不允许修改 5 个自定义页主结构；如发现普通页标准影响自定义页，必须记录 blocked。

### Task PCC-UW-B01: 报价/审核/缺口/发布包/同步/导入后端可用性加固

Owner: java-architect
OwnerSource: agent-registry
OwnerReason: 该任务涉及后端 Service、Engine、Mapper、权限和可能的 PG SQL，匹配 java-architect。
AgentRole: backend
Status: done
Priority: high
RiskLevel: high，关键流程后端不闭环会导致前端体验只是壳。
Wave: 1

Files:
- `bocoo-modules-product/src/main/java/**`
- `bocoo-modules-product/src/main/resources/**`
- `sql/postgresql/product_capability.sql`
- `i18n/locales/en_US.json`

Forbidden:
- `admin-ui/src/pages/**`
- MySQL SQL
- 生产环境配置

DependsOn: PCC-UW-C02
ParallelGroup: backend-flow
ConflictBoundary: product-module-backend

Acceptance:
- 报价预览、审核审批、缺口待办、发布包、同步日志、导入中心后端关键路径可用。
- 发布检查结果历史噪音有收敛方案并实现，若需 SQL 必须有 preflight/post-check。
- 权限、事务、DTO/VO、UTC、错误消息符合项目标准。

### Task PCC-UW-F03: 报价预览、审核审批、缺口、发布包、同步日志、导入中心前端流程收口

Owner: frontend-developer
OwnerSource: agent-registry
OwnerReason: 该任务涉及流程页交互、API 调用、错误态和权限按钮，匹配 frontend-developer。
AgentRole: frontend
Status: done
Priority: high
RiskLevel: high，这些页面是发布和运营闭环，不可停留在能打开。
Wave: 1

Files:
- `admin-ui/src/pages/product-center/quote-preview/**`
- `admin-ui/src/pages/product-center/approval/**`
- `admin-ui/src/pages/product-center/gap/**`
- `admin-ui/src/pages/product-center/package/**`
- `admin-ui/src/pages/product-center/sync/**`
- `admin-ui/src/pages/product-center/import/**`
- `admin-ui/src/api/product-capability/**`
- `i18n/locales/en_US.json`

Forbidden:
- `bocoo-modules-product/**`
- `sql/**`
- 生产环境配置

DependsOn: PCC-UW-C01,PCC-UW-C02
ParallelGroup: frontend-flow
ConflictBoundary: product-flow-pages

Acceptance:
- 报价预览和 Quick Quote 形成可理解的试算流程。
- 审核通过/拒绝、缺口定位、发布包详情、同步重试、导入预览有明确操作、loading、错误和结果状态。
- 权限按钮与后端权限一致。
- 不允许重做 5 个自定义页视觉主结构；流程页只收口关键操作。

## Barrier 1

Checks:
- Wave 1 任务 Files 不存在未处理冲突。
- 前后端关键流程均有实现或明确 `blocked`。
- 新增可见文案只进入 `i18n/locales/en_US.json`。
- 若修改 SQL，必须存在 preflight/post-check 计划。

## Wave 2: Integration Alignment

### Task PCC-UW-I01: 前后端字段、权限、i18n、UTC、选择器和错误态集成对齐

Owner: typescript-pro
OwnerSource: agent-registry
OwnerReason: 该任务涉及 TypeScript API 类型、前端状态、权限码和后端 response contract 对齐，匹配 typescript-pro。
AgentRole: integration
Status: done
Priority: high
RiskLevel: high，字段或权限错位会在真实浏览器验收时暴露。
Wave: 2

Files:
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `i18n/locales/en_US.json`
- `bocoo-modules-product/src/main/java/**/vo/**`
- `bocoo-modules-product/src/main/java/**/bo/**`

Forbidden:
- 生产环境配置
- 无关模块重构

DependsOn: PCC-UW-F01,PCC-UW-F02,PCC-UW-B01,PCC-UW-F03
ParallelGroup: integration
ConflictBoundary: product-capability-contract-alignment

Acceptance:
- 前端 request/response 类型与后端 BO/VO 一致。
- 权限码、字典值、日期展示、错误态和选择器能力对齐。
- 无明显 `any` 扩散或不真实断言绕过。

### Task PCC-UW-I02: 真实开发库菜单、按钮、字典、关键数据和 SQL post-check 收口

Owner: java-architect
OwnerSource: agent-registry
OwnerReason: 该任务涉及真实开发库、菜单权限、字典和 SQL 核验，匹配 java-architect。
AgentRole: db-validation
Status: done
Priority: high
RiskLevel: high，真实库不一致会直接导致菜单、按钮或流程验收失败。
Wave: 2

Files:
- `sql/postgresql/product_capability.sql`
- `.ai/changes/20260607-product-capability-usability-wave/logs/**`
- `docs/产品配置中心/**`

Forbidden:
- 生产库
- 生产环境配置
- MySQL SQL

DependsOn: PCC-UW-B01
ParallelGroup: db-validation
ConflictBoundary: product-capability-dev-db

Acceptance:
- 真实开发库菜单、按钮、角色授权、字典、关键产品能力数据 post-check 通过。
- 如果有 SQL 变更，执行日志、post-check 摘要和风险记录完整。

## Barrier 2

Checks:
- 前后端字段和权限一致。
- 真实开发库核验通过或明确 blocked。
- i18n/UTC/选择器要求无遗漏。

## Wave 3: Code Review / Security Review

### Task PCC-UW-R01: Static Review：上线体验 Wave 代码审计

Owner: code-reviewer
OwnerSource: agent-registry
OwnerReason: 该任务需要静态审查权限、事务、SQL、XSS、重复提交、DTO/VO 和维护性风险，匹配 code-reviewer。
AgentRole: static-review
Status: done
Priority: high
RiskLevel: high，未审计的上线体验改动可能引入权限和数据完整性风险。
Wave: 3

Files:
- `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`
- `bocoo-modules-product/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/api/product-capability/**`
- `bocoo-modules-generator/src/main/resources/vm/**`
- `sql/postgresql/product_capability.sql`

Forbidden:
- 修改业务代码
- 生产环境配置

DependsOn: PCC-UW-I01,PCC-UW-I02
ParallelGroup: review
ConflictBoundary: read-only-review

Acceptance:
- 输出具体文件/行级风险和优先级。
- 检查权限、事务、SQL 注入、XSS、重复提交、错误处理、i18n、UTC、DTO/VO 边界。
- 检查任务拆分是否违反 `ui-execution-contract.md` 第 6 节 Agent 拆分规则。
- Blocker 必须修复后才能进入最终 `done`。

## Barrier 3

Checks:
- Static Review blocker 清零。
- 非阻塞风险已记录到 check 或 TODO。

## Wave 4: Check / Validation

### Task PCC-UW-V01: Runtime Validation：内部浏览器真实录入和视觉验收

Owner: browser-debugger
OwnerSource: agent-registry
OwnerReason: 该任务需要内部浏览器、真实登录态、console/network、viewport 和交互证据，匹配 browser-debugger。
AgentRole: runtime-validation
Status: done
Priority: high
RiskLevel: high，没有真实浏览器验证不能证明上线可用。
Wave: 4

Files:
- `.ai/changes/20260607-product-capability-usability-wave/ui-execution-contract.md`
- `.ai/changes/20260607-product-capability-usability-wave/logs/**`
- `.ai/changes/20260607-product-capability-usability-wave/artifacts/**`
- `.ai/changes/20260607-product-capability-usability-wave/check.md`

Forbidden:
- 业务代码
- 生产环境配置

DependsOn: PCC-UW-R01
ParallelGroup: validation
ConflictBoundary: runtime-evidence-only

Acceptance:
- 19 个产品能力二级页面刷新 URL 不 404。
- 必须按 `ui-execution-contract.md` 第 4 / 5 / 7 节逐项验收。
- 5 个自定义页通过主结构、底部、快捷操作、关键 API、移动端验证。
- 5 个自定义页需要对照设计文档确认主信息架构未丢失：工作台聚合、配置三栏、价格矩阵 + 试算、发布闸门、销售只读总览。
- 报价预览、审核、发布检查、缺口、发布包、同步日志、导入中心关键流程有浏览器证据。
- console/network 无未解释错误。

### Task PCC-UW-V02: Build/Test/i18n/codegraph/diff 总验收和任务状态收口

Owner: main
OwnerSource: main-fallback
OwnerReason: 该任务负责总体验收、状态更新和最终收口，适合 orchestrator 执行。
AgentRole: final-validation
Status: done
Priority: high
RiskLevel: high，最终状态必须准确，不能把失败项写成 done。
Wave: 4

Files:
- `.ai/changes/20260607-product-capability-usability-wave/**`
- `.ai/TASKS.md`
- `.ai/CURRENT.md`
- `.ai/HANDOFF.md`

Forbidden:
- 生产环境配置
- 伪造未执行验证结果

DependsOn: PCC-UW-V01
ParallelGroup: validation
ConflictBoundary: orchestration-final-state

Acceptance:
- Java/Vue/i18n/codegraph/diff 检查结果写入 check。
- 真实开发库和内部浏览器结果写入 check。
- 未完成项保留 `pending`、`blocked` 或 `failed`。
- Next Step 设置为 Ready for `/archive` 或明确 blocked。

## Barrier 4

Checks:
- Build/Test/i18n/codegraph/diff 通过。
- Runtime Validation 通过或明确 blocked/failed。
- Static Review blocker 清零。
- `.ai/TASKS.md` 状态与 change tasks 一致。
