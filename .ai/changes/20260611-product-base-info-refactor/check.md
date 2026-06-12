# Check: 产品配置中心基础资料模块重构

## 已执行验证

- `codegraph sync`：通过，结果为 `Already up to date` / `Done`。
- `mvn -pl bocoo-modules-product -am -DskipTests compile`：通过。
- `mvn -pl bocoo-admin -am -DskipTests compile`：通过。
- `mvn -pl bocoo-modules-product -DskipTests compile`：通过。
- `pnpm --dir admin-ui build`：通过，包含 `vue-tsc --noEmit` 和 `vite build`。
- `git diff --check`：通过。
- `sql/postgresql/product_capability.sql` 已执行到本地开发 PostgreSQL。
- 旧模型/销售变体清理已执行到本地开发 PostgreSQL：
  - `pc_product_model`：不存在
  - `pc_sales_variant`：不存在
  - 旧 Workbench / Product Models / Sales Variants 菜单：0
  - 对应旧 `sys_role_menu`：0
- Seed 数据计数已验证：
  - `pc_unit`：7
  - `pc_base_attribute`：10
  - `pc_fabric_series`：3
  - `pc_fabric_profile`：3
  - `pc_material_attribute`：13
  - `pc_component`：5
  - `pc_component_item`：16
  - `pc_media_asset`：5
  - `pc_media_binding`：5
- 开发库关键补列已验证：
  - `pc_unit.sort_order`
  - `pc_component.legacy_source`
  - `pc_component.legacy_id`

## 运行态验证

- 后端按真实运行产物启动：`bocoo-admin/target/dist/bocoo-admin.jar`，地址 `http://127.0.0.1:8081`。
- 前端 Vite 启动到 `http://127.0.0.1:8083/`。
- 当前没有直接暴露 `browser.open` 类工具；已使用 Playwright + 本机 Chrome 完成真实页面验证。
- 六个基础资料页面真实冒烟通过：
  - `/product-master/fabrics`
  - `/product-master/materials`
  - `/product-master/components`
  - `/product-master/base-attributes`
  - `/product-master/media-assets`
  - `/product-master/units`
- 冒烟结果：登录成功，六页均有表格、Add 按钮和新增弹窗；`consoleErrorCount=0`、`badResponseCount=0`、`apiFailureCount=0`、`failedPageCount=0`。
- Google 登录按钮 iframe 曾出现 `accounts.google.com` 外部 403 资源错误，已排除为第三方资源，不计入本地产品模块失败。
- 已对 `PlusWebInvokeTimeInterceptor` 增加敏感字段脱敏，覆盖 `password`、验证码、token、secret 等字段。
- 登录日志已确认敏感字段以掩码输出，未记录明文密码或验证码。

## 静态审查结论

- 后端已按模块拆分为 `ProductBaseInfoService`、`ProductMaterialService`、`ProductFabricService`、`ProductComponentService`、`ProductMediaService` 和 `ProductCrudSupport`，不再使用 600 行混合 service。
- `ProductBaseInfoController` 仅保留分类、单位、基础属性；物料、组件、面料、附件分别有独立 controller。
- 前端 API 已拆分为 `base.ts`、`material.ts`、`component.ts`、`fabric.ts`、`asset.ts`。
- 附件资料复用现有 OSS 能力，新表只保存 asset 元数据与业务绑定关系。
- 旧 workbench/model/sales 入口未在本轮迁移；如菜单仍开放，后续应独立拆分或删除，不能再塞回基础资料 service。
- 旧 workbench/model/sales 入口已清理：前端动态路由映射、旧页面/API 文件、后端旧 Entity/BO/VO/Mapper 和开发库旧表/菜单均已移除。
- 真实页面验证期间修正了 SQL 与实体不一致：
  - `pc_unit` 补 `sort_order`，匹配单位列表排序。
  - `pc_component` 补 `legacy_source` / `legacy_id`，匹配组件包 BO/VO/Entity 与旧系统追溯字段。
