# Design: 共享产品能力中心实施边界

## Architecture Boundary

推荐新建 `bocoo-modules-product`。如果暂不新增 Maven module，至少使用独立包名：

```text
com.bocoo.product
  controller
  service
  service.impl
  mapper
  domain.entity
  domain.bo
  domain.vo
  domain.dto
  domain.result
  engine
```

不要放进 `bocoo-modules-system`。

## Backend Pattern

- 普通 CRUD 优先代码生成器风格。
- Controller 返回 `R` / `TableDataInfo`。
- Entity 不直接返回前端。
- BO 接收请求，VO 返回展示，Result/DTO 用于 engine 输出。
- 复杂能力进入 engine：
  - `ConfigEvaluationEngine`
  - `BomBuildEngine`
  - `PriceCalculationEngine`
  - `PublishCheckEngine`
  - `SnapshotBuildEngine`

## Frontend Pattern

- 菜单进入现有 `sys_menu`，一级建议“产品能力”，最多两级。
- 路由可用 `/product-center/*`，API 用 `/product-capability/*`。
- 普通页面用 grid/list + drawer。
- 5 个自定义页面按效果图和 `产品能力界面设计稿.md` 还原：
  - 工作台
  - 配置录入工作台
  - 价格编辑与测试
  - 测试发布
  - 销售只读总览

## Data Boundary

- 表前缀 `pc_`。
- PostgreSQL + `timestamptz` + 中文注释。
- 发布包、订单快照、read model 采用宽表和 JSONB。
- 核心数据按平台共享主数据处理。
- 订单、ERP、MES 不直接写产品能力核心表。

## Batch Breakdown

### Batch 0：实施决策和 Contract

输出模块名、租户方案、migration 节奏、API contract、前后端字段边界。

### Batch 1：基础能力闭环

实现基础信息、产品模型骨架、资料绑定、工作台只读骨架、菜单/i18n/API 基线。

### Batch 2：配置价格发布闭环

实现配置模板编辑、规则求值 MVP、BOM 预览、价格方案与试算、发布检查、发布包、outbox。

### Batch 3：消费和只读闭环

实现销售只读、订单快照内部 Service/API、发布包消费、报价预览。

### Batch 4：后续增强

保留 ConfigAgent、PricingAgent、导入中心、ERP/MES 深度同步、促销价/客户价、完整生产库存流程。

## Validation Strategy

每批 `/do` 后按范围验证：

- Java：`mvn -pl bocoo-admin -am -DskipTests compile`
- Frontend：`pnpm --dir admin-ui typecheck`
- Frontend build：`pnpm --dir admin-ui build`
- Browser：关键自定义页面打开、交互、console、network 检查
- Static Review：权限、tenant、UTC、i18n、Entity/VO/BO、事务、SQL、快照权威

未执行的验证必须记录 Not Run，不得标记 passed。
