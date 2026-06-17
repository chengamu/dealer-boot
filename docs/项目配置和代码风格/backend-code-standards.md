# 后端代码规范

## 1. 标准参考

普通 grid 后端以 `bocoo-modules-generator/src/main/resources/vm/java/` 为基线。

当前项目后端参考样例为：

```text
bocoo-modules-demo/
```

`bocoo-modules-demo` 只作为后端规范示例模块，不承载真实业务。新后端功能参考它的分层、命名、权限、i18n、UTC 和 mapper 写法。

## 2. 模块单一化

一个 Maven 模块只负责自己的业务边界：

```text
bocoo-modules-demo    -> 只放 demo 示例代码
bocoo-modules-dealer  -> 只放 dealer / 经销商业务代码
bocoo-modules-product -> 只放 product / 产品能力业务代码
bocoo-modules-system  -> 只放 system / 后台系统管理代码
```

新增后端模块前必须确认：

```text
这个模块负责哪个业务域？
这个模块的 Controller 对外暴露哪些 API 前缀？
这个模块的表、mapper、service、i18n key 和权限码是否都属于这个业务域？
```

规范要求：

- 包名按业务域组织，例如 `com.bocoo.demo`、`com.bocoo.product`、`com.bocoo.system`。
- API 前缀按业务域组织，例如 `/demo/*`、`/product-capability/*`、`/system/*`。
- 权限码首段按业务域组织，例如 `demo:*`、`product:*`、`system:*`。
- mapper XML 放在本模块资源目录下。
- i18n key 使用模块前缀，例如 `demo.*`、`product.*`、`system.*`。
- 模块 POM 只声明真实需要的依赖，不为了以后可能用到而引入能力。

禁止事项：

- 在 `dealer` 模块里放 `demo` 包。
- 在 `system` 模块里实现产品业务。
- 在 `product` 模块里直接写订单、支付、经销商门户业务。
- Controller 直接跨模块操作其他模块 Mapper。
- 把多个业务流程塞进一个大 Controller 或一个大 Service。

跨模块协作建议：

- 查询展示类聚合：定义专用 VO/DTO，由本模块 Service 调用其他模块稳定 Service 或只读接口。
- 复杂规则类聚合：放入 Engine，Engine 输入输出使用 DTO/Result，不依赖 Controller BO。
- 强事务写入：先确认业务归属和事务边界，必要时由明确的应用 Service 编排。
- 通用能力：优先进入 `bocoo-common-*`，不要复制到多个业务模块。

## 3. 标准分层

标准结构：

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

标准职责：

| 层 | 职责 |
| --- | --- |
| Controller | 接收入参、权限、日志、校验、调用 Service、返回 `R` / `TableDataInfo` / Excel |
| Service 接口 | 定义查询、详情、新增、修改、删除等业务能力 |
| ServiceImpl | 事务、查询条件、转换、保存校验、状态变更、调用 Engine |
| Mapper | 继承 `BaseMapperPlus<Entity, Vo>`，复杂查询可补 XML |
| Entity | 单表映射，继承 `BaseEntity` |
| BO | 请求对象，继承 `BaseBo`，包含校验注解和查询参数 |
| VO | 返回对象，允许冗余展示字段和 Excel 注解 |

Controller 标准接口：

```text
GET    /xxx/list       -> TableDataInfo<Vo>
POST   /xxx/export     -> ExcelUtil.exportExcel(...)
GET    /xxx/{id}       -> R<Vo>
POST   /xxx            -> R<Void>
PUT    /xxx            -> R<Void>
DELETE /xxx/{ids}      -> R<Void>
PUT    /xxx/changeStatus/{id}/{status} -> R<Void>
```

## 4. Mapper XML 和 resources

模块资源目录也必须遵守单一化原则，参考 `bocoo-modules-demo/src/main/resources`：

```text
src/main/resources/
  i18n/
    en_US.json
    zh_CN.json
  mapper/<module>/
    XxxMapper.xml
```

Mapper XML 规范：

- XML 放在本模块 `src/main/resources/mapper/<module>/` 下，例如 `mapper/demo/DemoTechManagementMapper.xml`。
- 文件名和 Java Mapper 接口保持一致，例如 `DemoTechManagementMapper.xml` 对应 `DemoTechManagementMapper`。
- `<mapper namespace="">` 必须写 Java Mapper 全限定名，例如 `com.bocoo.demo.mapper.DemoTechManagementMapper`。
- `<select id="">` / `<update id="">` / `<delete id="">` 必须和 Mapper 接口方法名一致。
- 查询返回优先使用 VO `resultType`；复杂字段映射再使用 `resultMap`。
- SQL 只访问本模块业务边界内的表；跨模块查询优先走 Service/DTO，不在 XML 里直接拼其他模块表。
- SQL 使用 PostgreSQL 语法，不新增 MySQL 兼容写法。
- 不在 XML 中硬编码租户、语言、时区、权限判断；这些逻辑放到 Service、拦截器或统一工具中。
- 简单 CRUD 优先使用 MyBatis-Plus 和 `BaseMapperPlus`，只有复杂统计、聚合、报表、特殊 join 才补 XML。

资源文件规范：

- 模块 i18n 放在本模块 `src/main/resources/i18n/`，key 使用模块前缀。
- resources 里不要放临时样例、旧模块 XML、旧业务表查询或无用配置。
- mapper XML、i18n key、权限码和包名必须能看出同一个业务域。

## 5. 后端硬约束

- Controller 不直接操作 Mapper。
- Controller 不拼快照 JSON，不写规则求值、价格计算、发布检查。
- 新增、修改、删除、导出、审核、发布、状态切换必须有 `@SaCheckPermission`。
- 重要操作补 `@Log`。
- 后端错误消息优先使用 message key / JSON i18n，不新增 `messages*.properties`。
- 创建业务时间使用 `TimeUtils.utcNow()`，禁止直接 `LocalDateTime.now()`。
- PostgreSQL 是主数据库；新增 SQL 使用 PG 语法和 `timestamptz`，不补 MySQL 新脚本。
- 导出接口使用 `ExcelUtil.exportExcel(...)`，前端导出按钮必须有对应后端接口和菜单按钮权限。
- 状态变更必须按真实主键字段生成或手写，不允许硬编码 `getId`。

## 6. 后端单元测试规范

后端单元测试优先覆盖 ServiceImpl、Engine 和规则类，不把浏览器自动化当作后端单元测试的一部分。

测试目录约定：

```text
bocoo-admin/src/test/java/com/bocoo/<module>/service/
```

当前项目由 `bocoo-admin` 聚合启动测试；业务模块的轻量单元测试先放在 `bocoo-admin/src/test/java` 下，包名仍按真实业务模块组织，例如 `com.bocoo.product.service`。

单元测试优先级：

- 字典、单位、分类、基础属性等基础资料：唯一校验、系统内置删除拦截、状态切换、options 返回、引用检查。
- 物料、面料、组件、附件绑定：业务编码统一、冗余字段同步、附件绑定引用、删除前引用检查。
- 工程配置、产品配置、价格：规则解析、试算结果、阻断/警告、输出组件/物料/附件。
- 发布、导入、快照：状态流转、解析校验、错误汇总、快照结构。

测试写法：

- 优先使用 `JUnit 5` + `MockitoExtension`，Mock Mapper，不连接开发库。
- ServiceImpl 单元测试只验证业务规则和 Mapper 调用结果，不验证 MyBatis-Plus SQL 生成细节。
- Engine 测试使用内存对象构造输入，断言输出 DTO / Result / Map 的关键字段。
- 重要异常使用 `assertThatThrownBy(...).isInstanceOf(ServiceException.class)`，不要只测空返回。
- 不为了测试访问 private 方法；如确实需要测试，优先把业务规则提取成包内可见 helper 或 Engine。
- 不在单元测试里依赖当前开发库已有数据，不读取生产配置，不输出密钥、连接串。
- 当前不要求浏览器自动化；页面级回归可以后续使用 Browser / Playwright 单独做。

推荐命名：

```text
ProductDictServiceTest
ProductUnitServiceTest
FabricSeriesServiceTest
EngineeringPreviewServiceTest
```

测试命令：

```bash
# 跑指定测试
mvn -pl bocoo-admin -am -Dtest=ProductDictServiceTest,ProductUnitServiceTest -Dsurefire.failIfNoSpecifiedTests=false test

# 跑产品模块相关单元测试
mvn -pl bocoo-admin -am -Dtest='*Product*Test,*Fabric*Test,*Engineering*Test' -Dsurefire.failIfNoSpecifiedTests=false test

# 编译兜底
mvn -pl bocoo-admin -am -DskipTests compile
```

新增或重构产品模块后，至少补以下一类测试：

```text
基础资料 CRUD / options / 引用检查
规则或试算器核心分支
删除、停用、状态切换等容易破坏数据一致性的操作
```

## 7. 后端验证

后端改动后至少检查：

- `mvn -pl <module> -am -DskipTests compile`。
- 涉及业务规则、引用检查、状态切换、试算器时，补并执行对应单元测试。
- 权限码三方一致：Vue `v-hasPermi`、Java `@SaCheckPermission`、`sys_menu.perms`。
- Mapper XML、i18n 资源、权限码都在本模块业务边界内。
- Mapper XML 的 namespace、statement id、resultType / resultMap 和 Java Mapper 对齐。
- 新 API 不返回 Entity，不返回裸 Map，不由 Controller 拼复杂 JSON。
