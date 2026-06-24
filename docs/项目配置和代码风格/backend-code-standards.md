# 后端代码规范

普通 grid 后端以 `bocoo-modules-demo/` 为标准样例；generator 模板按 [code-generator-standards.md](./code-generator-standards.md) 检查。

## 模块边界

- 包名、API 前缀、权限码、mapper XML、i18n key 按同一业务域组织。
- Controller 不跨模块操作 Mapper。
- 跨模块展示聚合使用专用 VO/DTO；复杂规则放入 Engine；强事务写入先确认业务归属和事务边界。
- 通用能力进入 `bocoo-common-*`，不复制到多个业务模块。

## 标准分层

```text
controller/
service/
service/impl/
mapper/
domain/entity/
domain/bo/
domain/vo/
resources/mapper/
```

| 层 | 职责 |
| --- | --- |
| Controller | 入参、权限、日志、校验、调用 Service、返回 `R` / `TableDataInfo` / Excel |
| Service | 查询、详情、新增、修改、删除、状态变更等业务能力 |
| ServiceImpl | 事务、查询条件、转换、保存校验、引用检查、状态变更、调用 Engine |
| Mapper | 继承 `BaseMapperPlus<Entity, Vo>`，复杂查询补 XML |
| Entity | 单表映射，继承 `BaseEntity` |
| BO | 请求对象，继承 `BaseBo`，包含校验注解和查询参数 |
| VO | 返回对象，允许冗余展示字段和 Excel 注解 |

## 接口口径

```text
GET    /xxx/list                         -> 分页 grid
POST   /xxx/export                       -> Excel 导出
GET    /xxx/allList                      -> 无分页完整业务列表
GET    /xxx/options                      -> 选择器选项
GET    /xxx/{id}                         -> 详情
POST   /xxx                              -> 新增
PUT    /xxx                              -> 修改
DELETE /xxx/{ids}                        -> 删除
PUT    /xxx/change-status/{id}/{status}  -> 状态变更
```

- `/options` 只用于下拉、弹出选择器、远程选项场景。
- 分页列表统一接收 `PageQuery.orderByColumn` / `PageQuery.isAsc`，由 `pageQuery.build()` 生成服务端排序。
- 后端可以设置业务默认排序，但不得覆盖前端显式排序。
- 不在 Controller 或 Service 拼接未转义 SQL 排序片段。

## Mapper 和资源

```text
src/main/resources/
  i18n/en_US.json
  i18n/zh_CN.json
  mapper/<module>/XxxMapper.xml
```

- XML namespace 写 Java Mapper 全限定名，statement id 和 Mapper 接口方法名一致。
- 简单 CRUD 使用 MyBatis-Plus 和 `BaseMapperPlus`；复杂统计、聚合、报表、特殊 join 才补 XML。
- SQL 使用 PostgreSQL 语法。
- resources 不放样例数据、其他业务域 XML、无用配置。

## 硬约束

- Controller 不直接操作 Mapper，不写规则求值、价格计算、发布检查、快照 JSON。
- 新增、修改、删除、导出、审核、发布、状态切换必须有 `@SaCheckPermission`。
- 重要操作补 `@Log`。
- 后端错误消息使用 JSON i18n key。
- 创建业务时间使用 `TimeUtils.utcNow()`。
- Export 是普通 grid 默认能力：后端 `/export`、前端导出按钮、数据库 export 按钮权限必须一致。
- 状态变更必须按真实主键字段生成或手写。
- 删除和停用是两个业务动作：有引用通常阻断删除，但不应默认阻断停用或取消审核。
- 如果停用会影响运行中方案、发布版本或订单流程，必须在对应 Service 中写明确阻断规则和 i18n 提示。
- 审核型主表只保留当前状态和最后审核快照；需要追溯“谁改了什么”时接入通用变更流水，不把历史塞进主表。

## 后端测试

- P0/P1 后端业务模块新增或重构时，必须补 ServiceImpl / Engine 单元测试。
- 浏览器自动化只覆盖页面交互、表单联动、权限入口和链路冒烟。
- 测试放在 `bocoo-admin/src/test/java/com/bocoo/<module>/service/`，包名按真实业务模块组织。
- 必测规则包括唯一性、自然键、业务编码冲突、引用删除、系统内置数据删除、状态流转、发布阻断、价格/工程计算、快照生成、导入解析。
- 审核型模块必须测试审核、取消审核、已审核不可普通编辑、取消审核后可编辑。
- 引用规则必须分别测试删除阻断和停用/取消审核行为，不能只测一个 `allowed`。
- 接入通用变更流水的模块必须测试动作类型、操作人、操作时间和差异内容。
- 优先使用 `JUnit 5` + `MockitoExtension`，Mock Mapper，不连接开发库。

## 验证

- 后端改动后执行 `mvn -pl <module> -am -DskipTests compile`。
- 涉及业务规则、引用检查、状态切换、试算器时，补并执行对应单元测试。
- 权限码三方一致：Vue `v-hasPermi`、Java `@SaCheckPermission`、`sys_menu.perms`。
- 新 API 不返回 Entity，不返回裸 Map，不由 Controller 拼复杂 JSON。
