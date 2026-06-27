# 老 OFBiz 基础数据导入工具

用途：把老 OFBiz 的基础数据清洗后导入本地/开发库，用作新系统基础信息测试数据。

边界：

- 只读老 OFBiz 数据库。
- 只写目标新系统开发库。
- 不修改 `sql/postgresql/product_capability.sql`。
- 不导配置、价格、库存、订单、报价。
- 不复制老系统交接文档或旧模型文件。

## 导入范围

- 产品分类：老库 `PRODUCT_CATEGORY_TYPE_ID = CATALOG_CATEGORY`，过滤系列、控制方式、罩壳、eBay 等非基础分类。
- 单位：从老库 `UOM` 和实际使用单位抽取。
- 厂家：从 `SUPPLIER_PRODUCT.party_id -> PARTY_GROUP.group_name` 抽取，并保留 `900 通用厂家/暂无厂家`。
- 物料类型：按当前业务口径写入面料、铝材、系统、配件、安装包、包装、印刷品。
- 物料：只导 `PRODUCT.PRODUCT_TYPE_ID = SUBASSEMBLY`。
- 变更流水：导入物料时写 `pc_change_log`，`action_type = IMPORT`。

## 环境变量

工具只读取环境变量，不写死账号密码：

```bash
export OFBIZ_DB_URL='jdbc:mysql://host:port/db?useSSL=false&serverTimezone=Asia/Shanghai'
export OFBIZ_DB_USER='readonly_user'
export OFBIZ_DB_PASSWORD='***'

export TARGET_DB_URL='jdbc:postgresql://host:port/db'
export TARGET_DB_USER='target_user'
export TARGET_DB_PASSWORD='***'
```

可选：

```bash
export IMPORT_OUTPUT_DIR='/tmp/ofbiz-base-import'
export IMPORT_TENANT_ID='1'
```

## 编译

```bash
javac \
  -cp "$HOME/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.6.2/postgresql-42.6.2.jar" \
  scripts/data-import/ofbiz-base/OFBizBaseImporter.java
```

## 运行

先 dry-run：

```bash
java \
  -cp "scripts/data-import/ofbiz-base:$HOME/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.6.2/postgresql-42.6.2.jar" \
  OFBizBaseImporter dry-run
```

确认报告后导入：

```bash
java \
  -cp "scripts/data-import/ofbiz-base:$HOME/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.6.2/postgresql-42.6.2.jar" \
  OFBizBaseImporter apply
```

只备份：

```bash
java \
  -cp "scripts/data-import/ofbiz-base:$HOME/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.6.2/postgresql-42.6.2.jar" \
  OFBizBaseImporter backup
```

按备份还原：

```bash
java \
  -cp "scripts/data-import/ofbiz-base:$HOME/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.6.2/postgresql-42.6.2.jar" \
  OFBizBaseImporter restore --backup-dir /tmp/ofbiz-base-import/backup-YYYYMMDD-HHMMSS
```

## 输出

所有运行报告和备份默认输出到 `/tmp/ofbiz-base-import/`：

- `summary.md`：导入统计。
- `skipped.tsv`：跳过清单。
- `warnings.tsv`：数据兜底或质量提醒。
- `backup-*/restore.sql`：目标库基础信息表还原脚本。
- `backup-*/manifest.tsv`：备份行数。

报告不会输出密码或原始连接串。

## 回滚策略

`apply` 会先自动执行备份，备份成功后才导入。导入结果不满意时，使用 `restore --backup-dir` 还原。

还原范围只包含基础信息相关表：

`pc_category`、`pc_unit`、`pc_manufacturer`、`pc_material_type_group`、`pc_material_type`、`pc_base_attribute`、`pc_material`、`pc_material_attribute`、`pc_media_asset`、`pc_media_binding`、`pc_change_log`。

不会还原或清理系统菜单、权限、用户和其他业务表。
