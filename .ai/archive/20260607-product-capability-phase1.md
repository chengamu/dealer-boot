# Archive: 共享产品能力中心第一阶段落地

## Feature：功能

共享产品能力中心第一阶段落地：历史页面标准化、三组产品能力菜单、产品/配置/价格/发布/报价/审核/缺口/导入/同步基础闭环，以及真实开发库和内部浏览器验收。

## Requirement Source：需求来源

- `docs/产品配置中心/共享产品能力中心开发入口.md`
- `docs/产品配置中心/配置中心功能拆分清单.md`
- `docs/产品配置中心/共享产品能力中心数据库设计草案.md`
- `docs/产品配置中心/共享产品能力中心API与后端实现约束.md`
- `docs/产品配置中心/产品能力界面设计稿.md`
- `docs/项目配置和代码风格/fullstack-code-standards.md`
- `.ai/requirements/20260606-product-capability-center.md`
- `.ai/changes/20260607-product-capability-phase1/wave-plan.md`

## Final Status：最终状态

Accepted with Risks。

验收门已通过；剩余风险均已记录为后续 Wave TODO，不作为第一阶段 blocker。

## Scope：范围

- 历史页面和代码生成器标准化为标准 grid/list + 右侧抽屉。
- `sys_menu` 落三组一级菜单：基础资料、配置与价格、发布与应用。
- 产品能力二级菜单和按钮权限落库。
- `bocoo-modules-product` 补齐产品资料、配置、价格、发布、审核、缺口、导入、同步基础 API。
- 前端产品能力标准页面独立菜单化。
- 5 个主自定义页按 H5/PNG 主结构落地。
- 真实开发库执行 SQL，内部浏览器用真实地址、真实登录态验证。

## Out of Scope：不做范围

- 客户价、促销价、复杂价格体系。
- 真实 ERP/MES 外部推送和回调确认。
- AI 智能识别、ConfigAgent、PricingAgent。
- 共享产品能力中心独立服务拆分。
- Redis 作为发布包、审核、订单快照、价格快照、BOM 快照的权威存储。
- 生产环境配置和生产库变更。

## Completed：已完成

- P0 任务 `PCC-P1-C01` 到 `PCC-P1-V02` 全部完成。
- 发布链路补齐 `submit -> approve/reject -> approved snapshotHash -> publish`。
- `gap-tasks`、引用检查、工作台读模型、Sales Quick Quote、报价预览调用价格引擎已补齐。
- `media-binding` 引用检查已从空壳改为真实绑定/资料/对象摘要。
- 旧 `24000/24001-24199` 草案菜单已隐藏失活。
- 文档已明确报价预览是独立轻量试算入口，不计入 5 个主自定义页。

## Not Completed：未完成

- 5 个主自定义页尚未完成像素级截图 diff。
- 发布检查结果表尚未加入批次号或去重策略。
- 引用检查记录不存在时当前返回摘要但 `allowed=true`，后续可按业务口径改为不可操作。

## Validation Summary：验证摘要

- Java：`mvn -pl bocoo-modules-product,bocoo-admin -am -DskipTests compile` 通过。
- Frontend：`./node_modules/.bin/vue-tsc --noEmit` 通过。
- Frontend build：`./node_modules/.bin/vite build` 通过。
- i18n：`node i18n/scripts/check.ts`、`validate.ts`、`sync.ts` 通过，1996 keys。
- CodeGraph：`codegraph sync` 通过。
- Diff：`git diff --check` 通过。
- SQL：真实开发库 post-check 通过，正式菜单 86、二级页面 19、按钮 64、admin 授权 86、字典类型 4、字典数据 36，旧草案菜单活跃子项 0。
- Browser：24 个路由 0 失败；移动端 8 个抽查 0 失败；关键交互 5 项 0 失败。
- Static Review：一审 blocker 已修复，复审确认 blocker 清零。

## Remaining Risks：剩余风险

- in-app browser 最终轮截图接口超时，未产出像素级截图 diff；已用 DOM、布局尺寸、按钮和关键交互证据替代第一阶段验收。
- 部分销售只读摘要仍依赖可用发布包数据，真实业务数据量上来后需要继续优化可视化和空态。
- 发布检查反复运行会产生历史结果噪音，后续应做批次或幂等收敛。

## Lessons from Learn：经验提炼

- `done` 必须绑定真实验收门，不能只看编译、空库、静态页面或骨架。
- 菜单路径问题应优先通过真实 `sys_menu`、动态路由和权限三方一致解决，不长期依赖前端 alias 兜底。
- 共享配置中心类能力应放在可拆分的产品能力模块中，不放订单模块，方便后续独立服务化。
- 发布包、审核、快照等权威数据应走数据库和不可变快照，不以 Redis 作为权威缓存。

## Key Decisions：关键决策

- PostgreSQL 是唯一新增 SQL 目标。
- 侧边栏保持两级菜单。
- 普通页面按标准 grid/list + right-toolbar + drawer。
- 5 个主自定义页允许自定义内容区；报价预览是独立轻量试算入口。
- 字典下拉必须 `filterable`，对象选择必须支持模糊过滤、远程搜索或选择器。

## Files Modified：修改文件

- `sql/postgresql/product_capability.sql`
- `bocoo-modules-product/**`
- `admin-ui/src/api/product-capability/**`
- `admin-ui/src/pages/product-center/**`
- `admin-ui/src/pages/system/**`
- `admin-ui/src/pages/merchant/**`
- `admin-ui/src/pages/monitor/**`
- `admin-ui/src/stores/permission.ts`
- `bocoo-modules-generator/src/main/resources/vm/**`
- `i18n/locales/*.json`
- `admin-ui/public/i18n/*.json`
- `bocoo-admin/src/main/resources/i18n/*.json`
- `docs/产品配置中心/**`
- `docs/项目配置和代码风格/fullstack-code-standards.md`
- `.ai/changes/20260607-product-capability-phase1/**`

## Artifacts：产物

- `.ai/changes/20260607-product-capability-phase1/check.md`
- `.ai/changes/20260607-product-capability-phase1/logs/db-postcheck-summary-final.txt`
- `.ai/changes/20260607-product-capability-phase1/logs/browser-route-summary-final.json`
- `.ai/changes/20260607-product-capability-phase1/logs/browser-mobile-summary-final.json`
- `.ai/changes/20260607-product-capability-phase1/logs/browser-interaction-summary-final.json`
- `.ai/changes/20260607-product-capability-phase1/logs/browser-custom-pages-final.json`

## Follow-up：后续事项

- 下一 Wave 聚焦上线可用体验：操作路径、空态/异常态、可视化舒适性、真实数据录入效率、复杂页面组件化和像素级细节。
- 继续执行代码审计、内部浏览器真实测试、真实开发库验证；未通过项不得标记 `done`。
