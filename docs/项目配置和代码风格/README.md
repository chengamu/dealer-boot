# 项目配置和代码风格

本目录是代码、权限、i18n、部署和文档治理的规范入口。每轮只读本页和任务相关专项规范，避免无关上下文污染。

## 阅读索引

| 任务 | 读取 |
| --- | --- |
| 后端模块、Controller、Service、Mapper | [backend-code-standards.md](./backend-code-standards.md) |
| 前端页面、组件、API 封装 | [frontend-code-standards.md](./frontend-code-standards.md) |
| 代码生成器 | [code-generator-standards.md](./code-generator-standards.md) |
| 菜单、按钮、权限、动态路由 | [permission-menu-standards.md](./permission-menu-standards.md) |
| 文案、错误消息、时间字段 | [i18n-utc-standards.md](./i18n-utc-standards.md) |
| 同源代理部署 | [deployment-nginx-same-origin.md](./deployment-nginx-same-origin.md) |
| 文档放置、删除、整理 | [documentation-governance.md](./documentation-governance.md) |

## 通用规则

- 先理解现有实现，再做 minimal diff。
- 不把业务字段、表结构草案、执行计划、调试日志写进通用规范；业务资料放对应知识库。
- 菜单权限必须三方一致：数据库 `sys_menu.perms`、前端 `v-hasPermi`、后端 `@SaCheckPermission`。
- P0/P1 后端业务规则必须有 ServiceImpl / Engine 测试兜底；浏览器自动化只验证页面交互和链路冒烟。
- 代码生成器产物按当前 demo 和 generator 规范二次检查后再进入业务模块。

## 本地验证

- 后端运行产物在 `bocoo-admin/target/dist/`，这是构建产物和运行目录，不在其中改源码或配置源文件。
- 启停服务前先确认已有进程、端口和用户当前环境，不默认重启或覆盖。
- 前端源码在 `admin-ui/`，启动和代理规则以 `admin-ui/README.md` 为准。
- 浏览器验证优先使用 Codex 内部浏览器；只有依赖真实浏览器状态时才使用外部浏览器。
- 回复验证结果时说明实际执行的命令、访问 URL 和未验证项。
