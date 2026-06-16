# 项目配置和代码风格

本目录只放后续写代码必须遵守的规范和注意事项。历史迁移记录、阶段性检查结论放入 `archive/`，不作为新代码模板。

## 阅读顺序

| 场景 | 先看 |
| --- | --- |
| 不确定该遵守哪份规范 | [fullstack-code-standards.md](./fullstack-code-standards.md) |
| 写前端页面、组件、API 封装 | [frontend-code-standards.md](./frontend-code-standards.md) |
| 写后端模块、Controller、Service、Mapper | [backend-code-standards.md](./backend-code-standards.md) |
| 新增菜单、按钮、权限、动态路由 | [permission-menu-standards.md](./permission-menu-standards.md) |
| 新增文案、错误消息、时间字段 | [i18n-utc-standards.md](./i18n-utc-standards.md) |
| 使用代码生成器 | [code-generator-standards.md](./code-generator-standards.md) |
| 部署前后端同源代理 | [deployment-nginx-same-origin.md](./deployment-nginx-same-origin.md) |

## 目录边界

- 通用代码规范写在本目录。
- 具体业务设计、表结构草案、操作手册写到对应业务知识库。
- 产品配置中心等业务模块可以引用本目录规范，但不要把业务字段口径和阶段性整改记录塞回通用规范。
- `archive/` 只用于保留历史资料，不作为新功能实现依据。
