# base-boot

`base-boot` 是基于 RuoYi-Vue-Plus 改造的业务管理系统，当前主数据库为 PostgreSQL，前端为 Vue 3 + TypeScript + Element Plus。

## 快速入口

| 目的 | 入口 |
| --- | --- |
| 项目协作规则 | `AGENTS.md` |
| 文档索引 | `docs/README.md` |
| 代码规范 | `docs/项目配置和代码风格/README.md` |
| 产品配置知识库 | `docs/产品配置知识库/README.md` |
| 管理端前端说明 | `admin-ui/README.md` |

## 文档原则

- 根目录只保留入口和长期有效规则。
- 临时任务计划、执行过程、截图证据和一次性调试记录不进入根目录文档体系。
- 当前可信文档以 `docs/README.md` 为准。
- 开发约束、文档治理、业务知识库规则都从 `docs/README.md` 进入，不在根目录重复维护。

## 部署说明

Docker Compose / 生产部署推荐使用 Nginx 同源代理：前端由 Nginx 提供静态资源，`/dev-api/*` 由同一个 Nginx 反向代理到后端服务。

详细说明见 `docs/项目配置和代码风格/deployment-nginx-same-origin.md`。
