# Check: 共享产品能力中心第一阶段落地

创建日期：2026-06-07

本文件在 `/plan` 阶段只记录验收设计；真实结果必须在 `/do` / `/check` 执行后补充，不能预填通过。

## Lane A: Static Review

Owner: `code-reviewer`

状态：`done`

- 权限一致性：`sys_menu.perms`、`v-hasPermi`、`@SaCheckPermission`。
- SQL 安全和幂等：菜单 ID、字典、角色授权、真实库 preflight。
- 后端分层：Controller、Service、Mapper、Engine、DTO/BO/VO。
- 事务和快照：发布包、审核、outbox、快照不可变。
- 前端风险：XSS、重复提交、错误态、loading、空态、组件边界。
- i18n / UTC：新增文案、时间展示和提交。

## Lane B: Runtime Validation

Owner: `browser-debugger`

状态：`done`

已执行：

- 使用内部浏览器访问真实 `192.*` 地址。
- 使用真实登录态和真实开发库。
- 验证三个一级菜单和全部 P0 二级菜单。
- 验证刷新 URL 不 404。
- 验证普通 grid/list：搜索、重置、分页、新增/编辑/详情抽屉、按钮权限。
- 验证 5 个自定义页：视觉复刻、底部操作可见、快捷操作、关键 API。
- 检查 console error、network error、CORS、401/403、错误提示。

结果摘要：

- 首轮 21 个目标页面直接 URL 打开均通过，没有 404、系统异常、无权限页或 console error。
- 最终轮 24 个目标页面直接 URL 打开均通过，没有 404、系统异常、无权限页或 console error；包含 `/log/operlog` 真实菜单路径。
- 移动宽度 390x773 抽查 8 个页面均通过，没有 404、系统异常或横向溢出。
- 覆盖路径包含三个一级菜单下全部 P0 二级菜单、系统参数、商家审核、商家资料、操作日志。
- 交互抽查 5 项均通过：销售只读 Quick Quote 跳转、报价预览调用后端试算、测试发布运行检查、商家审核通过/拒绝按钮存在、系统参数新增抽屉。
- 路由证据：`.ai/changes/20260607-product-capability-phase1/logs/browser-route-summary-final.json`。
- 移动端证据：`.ai/changes/20260607-product-capability-phase1/logs/browser-mobile-summary-final.json`。
- 交互证据：`.ai/changes/20260607-product-capability-phase1/logs/browser-interaction-summary-final.json`。
- 自定义页 DOM/控件证据：`.ai/changes/20260607-product-capability-phase1/logs/browser-custom-pages-final.json`。
- 注意：in-app browser 的 `Page.captureScreenshot` 在最终轮对 5 个自定义页连续超时，最终验收使用 DOM、路由、布局尺寸和关键按钮证据；像素级截图 diff 作为后续 TODO 保留。

### P0 页面路径矩阵

正式验收以入口文档为准，侧边栏只保留两级。每个页面都必须验证：菜单进入、刷新当前 URL 不 404、console/network、搜索、重置、分页、抽屉或关键操作、权限按钮、错误提示。

| 一级菜单 | 二级入口 | 建议 URL | 页面类型 | 最少证据 |
| --- | --- | --- | --- | --- |
| 基础资料 | 工作台 | `/product-master/workbench` | 自定义 dashboard | 首屏、底部、快捷入口 |
| 基础资料 | 产品分类 | `/product-master/categories` | 标准 grid/list | 查询、抽屉、保存回显 |
| 基础资料 | 物料管理 | `/product-master/materials` | 标准 grid/list | 字典类型 filterable、抽屉 |
| 基础资料 | 辅材管理 | `/product-master/components` | 标准 grid/list | 类型 filterable、对象字段 |
| 基础资料 | 资料资产 | `/product-master/media-assets` | 标准 grid/list | 上传/资料字段、抽屉 |
| 基础资料 | 资料绑定 | `/product-master/media-bindings` | 标准 grid/list | 绑定对象搜索、抽屉 |
| 配置与价格 | 产品模型 | `/product-config/models` | 标准 grid/list | 查询、抽屉、保存回显 |
| 配置与价格 | 销售变体 | `/product-config/sales-variants` | 标准 grid/list | 模型选择、抽屉 |
| 配置与价格 | 问题组模板 | `/product-config/question-groups` | 标准 grid/list | 查询、抽屉、保存回显 |
| 配置与价格 | 配置模板 | `/product-config/template` | 自定义录入工作台 | H5 对照、求值、预览/BOM |
| 配置与价格 | 价格中心 | `/product-config/pricing` | 自定义价格编辑与测试 | 矩阵/规则、试算、命中明细 |
| 配置与价格 | 报价预览 | `/product-config/quote-preview` | 独立试算入口，复用价格/销售组件 | Quick Quote、后端试算、跳转 |
| 发布与应用 | 测试发布 | `/product-release/publish` | 自定义发布闸门 | PASS/WARNING/BLOCKER、发布包 |
| 发布与应用 | 审核审批 | `/product-release/approvals` | 标准 grid/list + 审批抽屉 | 通过/拒绝按钮、权限 |
| 发布与应用 | 缺口待办 | `/product-release/gap-tasks` | 标准 grid/list | BLOCKER/WARNING 定位 |
| 发布与应用 | 发布包 | `/product-release/packages` | 标准 grid/list + 详情抽屉 | 不可变快照、详情 |
| 发布与应用 | 同步日志 | `/product-release/sync-outbox` | 标准 grid/list | retry 按钮、失败原因 |
| 发布与应用 | 导入中心 | `/product-release/import` | 标准 grid/list + 导入抽屉/预览 | XLS/XLSX 解析、行级问题 |
| 发布与应用 | 销售只读总览 | `/product-release/sales-view` | 自定义只读总览 | 只读卡片、Quick Quote 入口 |

### 历史标准化抽样矩阵

历史页面是第一阶段 P0，不作为后置项。至少覆盖这些页面：系统用户、商家用户、菜单、角色、参数、字典类型、字典数据、岗位、部门、OSS、OSS 配置、公告、操作日志详情、商家审核。每页检查查询区、toolbar/right-toolbar、表格/树表、分页、右侧抽屉、sticky 底部按钮、移动端不遮挡。

### 证据命名

- 截图：`artifacts/browser/<menu-slug>/<step>.png`。
- network 摘要：`logs/browser-network-summary.md`，记录 method、URL、status、耗时、失败响应摘要。
- console 摘要：`logs/browser-console-summary.md`，记录 error/warn 和页面路径。
- SQL 摘要：`logs/db-postcheck-summary.md`，记录菜单、按钮、角色授权、字典、关键表/索引查询结果。
- 构建摘要：`logs/build-summary.md`，记录 Java/Vue/i18n/SQL 校验命令和结果。

## Build / SQL / i18n

状态：`done`

已执行：

- `codegraph sync`
- Java 编译
- Vue 类型检查/构建
- i18n 同步和 JSON 校验
- PostgreSQL 真实开发库 preflight、执行、post-check

结果摘要：

- `codegraph sync`：通过，`Already up to date`。
- `node i18n/scripts/check.ts`：通过，1996 keys。
- `node i18n/scripts/validate.ts`：通过，1996 keys，10 dict keys covered。
- `node i18n/scripts/sync.ts`：通过，已同步到前后端 runtime i18n。
- `mvn -pl bocoo-modules-product,bocoo-admin -am -DskipTests clean compile`：通过，`BUILD SUCCESS`。
- `mvn -pl bocoo-modules-product,bocoo-admin -am -DskipTests compile`：最终轮通过，`BUILD SUCCESS`。
- `mvn -pl bocoo-modules-product,bocoo-admin -am -DskipTests install`：通过，生成本地开发 jar 用于真实浏览器验收。
- `./node_modules/.bin/vue-tsc --noEmit`：通过。
- `./node_modules/.bin/vite build`：通过。
- `git diff --check`：通过。
- 真实开发库 preflight：正式 `24200-24499` 菜单执行前为 0，旧 `24000` 为 `1/1`。
- 真实开发库执行：`sql/postgresql/product_capability.sql` 执行成功。
- 真实开发库 post-check：正式菜单 86，二级页面 19，按钮 64，管理员授权 86，字典类型 4，字典数据 36，旧 `24000` 为 `0/0`，旧 `24001-24199` 活跃子菜单为 0。
- SQL 证据：`.ai/changes/20260607-product-capability-phase1/logs/db-preflight-summary.txt`、`db-execute-summary.txt`、`db-postcheck-summary.txt`、`db-postcheck-summary-final.txt`。

## Result

当前状态：`done`

已通过：

- 真实开发库 SQL 执行和 post-check 已通过。
- Java/Vue/i18n/codegraph/diff 检查已通过。
- 内部浏览器真实地址、真实登录态、真实开发库的路由和交互抽查已通过。
- 一审 Static Review 的阻塞项已完成代码修复：审核发布闭环、引用检查/缺口待办、Quick Quote/报价、工作台读模型。
- Static Review 复审确认 blocker 清零；`media-binding` 引用检查已改为真实读取绑定和资产并返回摘要。

仍需收口：

- 5 个主自定义页已完成浏览器首屏 DOM、按钮、底部可见和关键动作验证，但 in-app browser 截图采集失败；像素级截图 diff 和细节优化保留 TODO，不宣称已经像素级一致。
- `ProductPublishService.check()` 每次运行会写入检查结果，第一阶段可用；后续建议增加批次号或去重策略，避免历史噪音。
- 引用检查记录不存在时当前返回摘要但 `allowed=true`；后续可按业务口径改为不可操作。
