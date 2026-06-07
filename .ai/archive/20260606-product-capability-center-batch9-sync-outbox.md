# 共享产品能力中心 Batch 9：ERP/MES 同步Outbox基础层

## 结论

Batch 9 已完成并验证通过。

本批没有直接接 ERP/MES 外部接口，而是先把发布后的同步边界从“只给订单一条 outbox”扩展为“ORDER、ERP、MES 三个目标系统各一条 outbox”。这样产品能力中心仍然是发布包 source of truth，订单、ERP、MES 后续通过 outbox 或独立服务 API 消费，不反向写共享产品能力核心表。

## 完成内容

- `publish/packages/create` 发布成功后，同事务写入 ORDER、ERP、MES 三条 `pc_product_sync_outbox`。
- `PublishExecutionResultVo` 保留旧 `outbox` 字段作为兼容字段，同时新增 `outboxes` 返回完整目标系统列表。
- 新增 `/product-capability/sync-outbox/{id}/retry`，用于将同步事件重新标记为 `PENDING` 并递增 `retryCount`。
- 新增按钮权限 `product:publish:retrySync` 和菜单种子数据。
- 新增前端 API 方法 `syncOutboxApi.retry(id)`。
- 补充 `productCenter.publish.retrySync` i18n key，并同步 runtime i18n。
- 更新 API 约束、数据库设计草案、数据库落库评审、真实库执行计划、AMU contract 和 handoff 文档。
- 将旧文档里的 `pc_event_outbox` 关键位置收敛为当前真实表 `pc_product_sync_outbox`。

## 当前边界

- 当前只落同步事件和重试标记，不在请求线程直接调用 ERP/MES 外部系统。
- 当前没有实现定时扫描、失败退避、回调确认、死信队列和幂等消费。
- ERP/MES 仍应只读发布包、read model 或快照，不直接写产品能力核心编辑表。

## 验证

- `node i18n/scripts/sync.ts`：passed。
- YAML parse：passed。
- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- SQL 结构检查：passed，23 张 `pc_*` 表均覆盖租户忽略、表注释和字段注释。
- 一次性 PostgreSQL 16 空库执行：passed，顺序执行 `base.sql` 和 `product_capability.sql` 成功。
- 临时库结果：23 张 `pc_*` 表、45 条产品能力菜单、45 条 `role_id = 1` 产品能力授权。
- `codegraph sync`：passed。
- `git diff --check`：passed。

## 残余风险

- `product_capability.sql` 仍是 PostgreSQL 初始化草案，不是生产 migration。
- 真实开发库执行前仍需按 `docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md` 做目标库 preflight。
- ERP/MES 真实推送、失败退避、定时扫描、回调确认、幂等消费和死信处理需要后续单独设计。
- 当前前端发布页 outbox grid 还没有挂重试按钮，只预留了 API 和权限，后续可在自定义发布页交互里补。

## 下一步

继续 Deferred 队列：

- 完整 Excel/AI 导入解析和提交。
- ConfigAgent / PricingAgent。
- 客户价、促销价、客户等级价。
- 真实 ERP/MES 推送、回调确认和失败处理。
- 独立服务拆分执行方案。
