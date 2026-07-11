# 代码生成器规范

普通 grid 标准优先级：

```text
bocoo-modules-demo/ > bocoo-modules-generator 模板 > 业务模块二次校验
```

## 适用场景

适合生成后局部增强：

- 基础资料、字典式主数据、普通列表。
- 导入批次、行级问题、同步日志、发布包列表、缺口待办。

需要手写核心逻辑：

- 工作台、配置编辑器、价格矩阵、价格试算。
- 发布检查、发布包生成、快照构建、销售只读聚合。
- 多行录入、单元格编辑、列拖动、批量粘贴等复杂表格。

## 后端模板口径

- Service 接口命名为 `${ClassName}Service`。
- 详情接口通过 `service.queryById`。
- 新增使用 `AddGroup`，修改使用 `EditGroup`。
- 普通 grid 默认生成 `GET /list`、`POST /export`、`GET /allList`、`GET /{id}`、`POST /`、`PUT /`、`DELETE /{ids}`。
- 有状态字段时生成 `PUT /change-status/{id}/{status}`。
- `/options` 只用于明确的选择器场景，不等同于完整列表。

## 前端和权限口径

```text
app-container
  -> 查询区
  -> toolbar + right-toolbar
  -> el-table
  -> pagination
  -> el-drawer
```

- 普通资料维护页默认使用 `el-table`。
- 导出按钮默认生成，使用 `v-hasPermi="['<module>:<business>:export']"`。
- 导出下载路径对应后端 `POST /export`。
- 菜单 SQL 默认生成 list/query/add/edit/remove/export 权限。
- 前端按钮权限、后端 `@SaCheckPermission`、数据库 `sys_menu.perms` 三方一致。
- 数值字段必须在生成器列配置中明确选择业务控件：`number-count`、`number-quantity`、`number-unit-price`、`number-money`、`number-rate` 或 `inch`。
- `BigDecimal` 默认生成 `number-quantity`，整数默认生成 `number-count`；单价、金额、比例和英寸必须由开发者在生成前改成准确类型。
- 生成页面必须使用 `BusinessNumberInput`、`BusinessNumberText`、`BusinessInchInput` 和公共 formatter，禁止生成 `precision=2` 或 `toFixed(2)`。

## SQL 口径

- SQL 模板只生成当前模块需要的幂等菜单和按钮权限。
- 管理员角色授权使用幂等写法。
- 新增 SQL 使用 PostgreSQL 语法。
- 初始化 SQL 不写业务样例数据或执行记录。

## 生成后检查

- 后端接口、前端按钮、SQL 权限是否同时包含 export。
- `/allList` 和 `/options` 语义是否清晰。
- 状态变更是否使用真实主键字段。
- 前端页面是否符合普通 grid 结构。
- 新增文案是否补 i18n。
- 时间字段是否走 UTC 工具。
- 每个数值字段的生成类型是否与单位、币种、保存精度和业务语义一致。
