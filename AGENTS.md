# Project Agent Instructions

## 入口

- 默认中文说明；代码、变量名、方法名保持英文。
- 先读 `docs/README.md`，再按任务领域进入对应文档。
- 代码规范入口：`docs/项目配置和代码风格/README.md`。
- 产品配置知识库入口：`docs/产品配置知识库/README.md`。

## 工作方式

- 先理解需求和现有实现，再修改。
- Always plan before implementing。
- 遵循 minimal diff；不做无关重构，不猜测不存在的 API 或框架能力。
- 阅读代码结构、业务流程、调用链和影响面时，必须先使用 CodeGraph；符号定位使用 `codegraph_search`，整体理解使用 `codegraph_explore`，调用关系和重构影响使用 `codegraph_callers`、`codegraph_callees`、`codegraph_impact`。
- CodeGraph 可能作为延迟加载 MCP 工具，不出现在顶层简化工具列表中；必须先检查完整工具清单并尝试调用，禁止仅凭顶层列表判断其未启用。
- `rg` 仅用于精确文本、SQL、配置、生成文件和残留扫描，或补充 CodeGraph 未覆盖的具体细节；不得用反复 `rg` 和逐文件读取替代已有 CodeGraph 索引。
- 临时计划和执行状态留在对话中；长期规则、业务契约、操作手册和体验问题才写入 `docs/`。
- 文档治理规则见 `docs/项目配置和代码风格/documentation-governance.md`。

## 边界

- 不输出密钥、密码、Token、连接串等敏感信息。
- 不修改数据库结构、migration 文件、生产环境配置。
- 不升级依赖，除非用户明确要求且已说明原因。
- 不删除用户代码或已有注释，除非本任务明确要求。
- 涉及跨模块、数据库、依赖、架构、i18n、UTC、时区、日期格式时，先说明影响范围和计划。
- 涉及 i18n 时必须先读 `docs/项目配置和代码风格/i18n-utc-standards.md`。新增或修改翻译只改 `i18n/source/<module>/{en_US,zh_CN}.json`，再运行 `pnpm i18n` 生成产物。
- 禁止手工修改 i18n 生成产物：`i18n/locales/*.json`、`admin-ui/public/i18n/*.json`、`bocoo-admin/src/main/resources/i18n/*.json`、`admin-ui/src/types/i18n-keys.d.ts`。这些文件只能由脚本生成。

## 验证

- 修改前确认文件存在。
- 修改后执行能覆盖本次变更的最小检查；未执行就明确说明。
- 不声称没有执行过的命令已经通过。
- 文案、名称、注释、文档和纯样式修改只检查 diff，禁止运行 test、build、lint、coverage 或 E2E；i18n 修改只运行必要的生成命令。
- 局部代码修改只运行直接相关的最小测试或最小模块检查；局部检查通过后必须停止，禁止自行扩大验证范围。
- 禁止默认运行仓库级全量验证。只有用户明确要求“运行全量验证”时，才允许执行全量测试、构建、lint、coverage 或 E2E。
- 预计超过 120 秒的命令，执行前必须说明命令、原因和预计耗时，并等待用户明确确认；用户未回复不等于授权。
- 验证失败或超时后，禁止自行改跑范围更大的命令；没有合适的局部检查时应如实说明，不得用全量验证代替。

## 输出

- 回复简洁说明关键事实、风险和验证结果。
- 文件引用使用绝对路径。
- 没有把握的结论写 TODO，不要猜。


## Code Size Limit（强约束）

- 单文件最大不超过 300 行
- Vue 单文件组件建议 200~300 行以内
- Java class 不超过 300 行

## Hard Rule

生成代码前必须先进行文件拆分设计，禁止直接进入实现。

## Mandatory Split Rule

当文件预计超过限制时，必须：

1. 先拆分模块结构
2. 再分别生成多个文件
3. 禁止输出超大单文件

## Split Triggers

必须拆分的情况：

- UI + API + 状态管理混在一个文件
- methods / functions > 6 个
- 文件需要频繁滚动阅读

## Forbidden

- 禁止 500+ 行文件
- 禁止“先写大文件后重构”
