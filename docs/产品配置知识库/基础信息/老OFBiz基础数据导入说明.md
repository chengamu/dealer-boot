# 老 OFBiz 基础数据导入说明

本工具用于把老 OFBiz 基础数据清洗后导入本地/开发库，作为新系统基础信息测试数据。

当前导入范围：

- 产品分类
- 单位
- 厂家
- 物料类型
- 物料
- 物料导入变更流水

当前不导入：

- 配置规则
- 报价价格
- 库存
- 订单
- 旧系统文档或旧模型文件

导入工具位置：

`scripts/data-import/ofbiz-base/`

使用原则：

- `product_capability.sql` 只保留结构、菜单、权限和必要基础种子，不写老系统业务数据。
- 导入前必须备份目标基础信息表。
- 导入错误或效果不好时，使用备份目录执行还原。
- 旧数据质量不稳定时，宁可跳过并记录到报告，不强行写入。

物料导入口径：

- 第一版只导 `PRODUCT_TYPE_ID = SUBASSEMBLY`。
- `material_code` 保留老 `PRODUCT.product_id`。
- 厂家来自老 `SUPPLIER_PRODUCT.party_id -> PARTY_GROUP.group_name`；没有厂家时使用 `900 通用厂家/暂无厂家`。
- 导入物料默认 `ENABLED`，即已审核，可直接用于本地测试。
