# 共享产品能力中心 Batch 7：通用快照实例持久化

## 结论

Batch 7 已完成并验证通过。

本批根据用户确认的边界调整，将快照持久化从“订单私有 `order_product_snapshot`”改为“产品能力中心通用 `pc_product_snapshot_instance`”。这样订单、ERP、MES、报价等来源系统都通过 `source_system/source_biz_*` 字段写入同一套产品能力快照实例，后期 `bocoo-modules-product` 可以整体拆成独立共享服务。

## 完成内容

- 新增 `pc_product_snapshot_instance` 产品能力快照实例表。
- 新增全部字段中文注释、UTC 时间字段说明和来源/包/hash 查询索引。
- 将 `pc_product_snapshot_instance` 加入 `bocoo.tenant.ignore-tables`。
- 新增 `ProductSnapshotInstance` Entity、VO、Mapper。
- 扩展 `OrderSnapshotBuildBo` / `OrderProductSnapshotVo`，增加 `sourceSystem`、`sourceBizType`、`sourceBizNo`、`sourceBizLineNo` 和 `snapshotId`。
- 保留 `/product-capability/order-snapshots/build` 只构建兼容接口。
- 新增 `/product-capability/snapshot-instances/build` 构建并保存快照实例。
- 新增 `/product-capability/snapshot-instances/list` 和 `/{snapshotId}` 查询接口。
- 更新 SQL 菜单按钮权限：`product:snapshot-instance:build/list/query`。
- 更新数据库设计、API 约束、功能拆分清单、AMU contract 和 handoff 文档。

## 验证

- `node i18n/scripts/sync.ts`：passed。
- `ruby -e 'require "yaml"; YAML.load_file(...)'`：passed。
- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- SQL 结构检查：passed，21 张 `pc_*` 表均覆盖租户忽略、表注释和字段注释。
- 一次性 PostgreSQL 16 空库执行：passed，顺序执行 `base.sql` 和 `product_capability.sql` 成功。
- 临时库结果：21 张 `pc_*` 表、`pc_product_snapshot_instance` 存在、39 条产品能力菜单、39 条 `role_id = 1` 产品能力授权。
- `codegraph sync`：passed，already up to date。
- `git diff --check`：passed。

## 残余风险

- `product_capability.sql` 仍是 PostgreSQL 初始化草案，不是生产 migration。
- 真实开发库执行前仍需按 `docs/产品配置中心/共享产品能力中心数据库真实库执行计划.md` 做目标库 preflight。
- 当前快照 JSON 仍是 MVP：包含来源、发布包、模型、销售变体、模板版本、价格版本和用户输入；后续需要随着 BOM、资料、价格明细 read model 完善继续扩展。
- 订单、ERP、MES 业务表还需要各自保存 `snapshot_id/snapshot_hash` 或必要展示冗余字段。

## 下一步

继续 Deferred 队列：

- 导入中心。
- ERP/MES 深度同步。
- ConfigAgent / PricingAgent。
- 客户价、促销价、客户等级价。
- 独立服务拆分执行方案。
