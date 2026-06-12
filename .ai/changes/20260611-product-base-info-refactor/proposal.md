# Proposal: 产品配置中心基础资料模块重构

## Summary

重构产品配置中心基础资料模块，以 `material_code` 为统一主编号，建立面料系列 / 面料资料、通用物料属性、组件包明细、单位管理、配置字典和附件 OSS 绑定能力。

## Why

当前实现只有浅层 CRUD，无法支撑后续配置器、工程规则、BOM、价格、发布和订单快照。用户已明确当前项目未上线、无客户数据，旧一期功能可以改动、删除和重建。

## Scope

- 改造现有基础资料、附件资料 CRUD。
- 新增面料系列、面料资料、单位、物料属性、组件明细、基础属性。
- 复用现有 OSS，不新增附件系统。
- 参考 OFBiz 数据和代码，准备开发 seed。
- 写入至少 5 个产品测试样本。

## Out of Scope

- 完整配置规则引擎。
- BOM / 工程 / 价格试算器。
- 正式历史迁移。
- 订单、客户、付款、地址导入。

## Recommendation

推荐采用“面料独立 profile + 其他物料通用属性”的方案：

- 面料有系列级约束，使用 `pc_fabric_series` 和 `pc_fabric_profile`。
- 电机、遥控、胶条、安装码、配重条等差异属性进入 `pc_material_attribute`。
- `pc_material` 保留高频冗余字段，提升查询和后续分析性能。

## Rejected Options

- 每类物料都建 profile 表：表数量膨胀，后续维护成本高。
- 全部属性塞 JSON：短期快，但筛选、试算、分析和权限校验都差。
- 继续在旧浅 CRUD 上补字段：无法表达组件明细、单位精度和系列约束。

## Requirement Source

- `.ai/requirements/20260611-product-base-info-refactor.md`
- `docs/产品配置中心重构/实施拆分/09-基础资料模块AMU实施计划.md`
