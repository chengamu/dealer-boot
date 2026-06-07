# Proposal: 共享产品能力中心适配当前项目

## Requirement Source

- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `.ai/requirements/20260606-product-capability-center.md`

## Suitability：是否适合本项目

适合。

原因：

- 当前项目已有登录、角色、菜单、按钮权限、Sa-Token、OSS、字典、i18n、UTC、审计、代码生成器和管理端布局，能承接这类后台业务能力。
- 产品能力中心本质是平台共享主数据和发布能力，不是订单私有数据。当前先寄居在订单系统代码库中，但按独立模块、独立 API、独立表前缀设计，后续可以拆服务。
- 需求文档已经把 H5 原型降级为业务数据参考，正式页面回归现有 `admin-ui`，方向正确。
- 数据库草案已经强调发布包、快照、read model、outbox 和中文注释，符合性能、追溯和后续 AI 读取要求。

## Needed Optimization：需要优化的地方

这些优化不一定要回改需求源文档，但必须进入实施计划：

1. 第一批不能全量实现所有 P0。P0 要拆成可验收批次，否则会卡在数据库、前后端 contract 和规则引擎上。
2. 编码前必须先确认模块名、租户技术方案和 migration 评审节奏。
3. 第一批建议先打通“基础信息 + 产品模型 + 工作台骨架 + API/菜单/i18n 基线”，不要一开始就把价格、发布、订单快照全部压进去。
4. 第二批再接“配置编辑器 + 规则求值 + 价格中心 + 发布检查 + 发布包”。
5. 销售只读、订单快照、ERP/MES 同步、AI、导入中心保留在后续队列，不删除。
6. Redis 暂不进入 P0 实现；缓存只作为后续性能优化。
7. `配置中心原型.html` 和 `config-center-prototype-data.js` 只做数据参考，不作为 UI 还原目标。

## Recommended Implementation Strategy

采用分批实施：

- Batch 0：Contract / 技术实施决策。
- Batch 1：基础能力闭环。
- Batch 2：配置、价格、发布闭环。
- Batch 3：销售只读和订单快照消费。
- Batch 4：AI、导入、ERP/MES 等后续增强。

## Pause Points

进入 `/do` 后，遇到以下事项必须暂停或只产出草案等待确认：

- 需要新增 Maven module。
- 需要生成或修改 migration。
- 需要修改租户忽略表配置。
- 需要新增依赖。
- 需要改订单模块保存逻辑。
- 需要确认规则 JSON schema。

## Decision

本次 `/plan` 生成可调度计划，不开始业务代码实现。等待用户确认 `/do` 后再执行 Batch 0。
