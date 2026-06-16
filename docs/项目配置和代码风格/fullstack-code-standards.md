# 前后端代码规范

本文是兼容入口，保留给 `AGENTS.md` 和现有业务文档引用。具体规则已经拆分到本目录的专项规范，新增或修改代码时按阅读路径进入对应文件。

## 1. 阅读路径

| 要做什么 | 看哪份规范 |
| --- | --- |
| 写前端页面、组件、API 封装 | [frontend-code-standards.md](./frontend-code-standards.md) |
| 写后端模块、Controller、Service、Mapper | [backend-code-standards.md](./backend-code-standards.md) |
| 新增菜单、按钮权限、动态路由 | [permission-menu-standards.md](./permission-menu-standards.md) |
| 新增文案、错误消息、时间字段 | [i18n-utc-standards.md](./i18n-utc-standards.md) |
| 使用代码生成器 | [code-generator-standards.md](./code-generator-standards.md) |
| 部署同源代理 | [deployment-nginx-same-origin.md](./deployment-nginx-same-origin.md) |

历史迁移记录在 [archive/](./archive/) 下，只作为背景资料，不作为新功能模板。

## 2. 总原则

- 普通 CRUD/grid 功能优先使用代码生成器风格，不从零手写一套页面和分层。
- 新功能必须接入现有菜单、按钮权限、动态路由、请求封装、i18n、UTC、审计和日志体系。
- 菜单和按钮权限必须先落数据库，再接前端和后端，不能只靠前端路由或临时代码兜底。
- 业务聚合、规则求值、发布、价格、BOM、快照可以手写 Service/Engine，但 Controller 不写复杂业务逻辑。
- 后端模块遵守单一化原则，一个 Maven 模块只负责自己的业务边界。
- 跨模块复杂能力优先通过独立 Service/Engine/DTO 编排；只有确实无法拆分且有明确事务边界时，才允许在一个业务 Service 内做有限聚合。
- 设计稿和效果图只作为内容区布局参考，不重做 Layout、Sidebar、Navbar、TagsView、登录态和权限框架。
- H5 原型和静态 mock 只作为业务数据和复杂度参考，不作为生产视觉和代码结构依据。

## 3. 快速验收清单

前端：

- 页面是标准 `app-container + 查询区 + toolbar + table + pagination`，复杂表单使用右侧抽屉。
- 动态路由组件已补到 `admin-ui/src/stores/permission.ts`。
- 可见文案走 i18n key，没有新增硬编码中文或英文。
- 时间展示和提交使用 UTC 工具。
- 菜单进入、刷新、搜索、重置、分页、按钮权限、控制台错误已做浏览器验证。

后端：

- 代码位于正确业务模块，没有跨模块混放。
- Controller 只处理入参、权限、日志、校验、Service 调用和返回。
- Service/Mapper/Entity/BO/VO 分层清晰。
- 新增、修改、删除、导出、审核、发布、状态切换有 `@SaCheckPermission`。
- 后端错误消息走 JSON i18n，业务时间使用 `TimeUtils.utcNow()`。
- 已执行 `mvn -pl <module> -am -DskipTests compile` 或说明未执行原因。

权限：

- `sys_menu.perms`、前端 `v-hasPermi`、后端 `@SaCheckPermission` 三方一致。
- 页面内动作有 `F` 按钮权限，菜单刷新不 404。
- 角色授权后普通用户按钮显示和接口访问一致。

文档：

- 特殊页面必须说明为什么不走标准 grid，以及仍遵守哪些项目级约束。
- 新规范和注意事项写入本目录对应专项文件，不把历史整改过程写回主规范。
