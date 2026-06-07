# Context

## Project Summary：项目摘要

`base-boot` 是 Spring Boot 多模块后端 + Vue / Element Plus 管理端项目。当前项目已有登录、角色、菜单、按钮权限、租户、i18n、UTC、OSS、审计、代码生成器和 `admin-ui` 后台布局。

## Important Paths：重要路径

- `/Users/chengmuxuan/Desktop/cmx/base-boot`：项目根目录。
- `/Users/chengmuxuan/Desktop/cmx/base-boot/AGENTS.md`：项目级 Agent 指令。
- `/Users/chengmuxuan/Desktop/cmx/base-boot/.ai`：AMU workflow 状态目录。
- `/Users/chengmuxuan/Desktop/cmx/base-boot/docs/产品配置中心`：共享产品能力中心设计文档。

## Verification：验证方式

- 后端按范围优先执行 `mvn -pl bocoo-admin -am -DskipTests compile`。
- 前端按范围优先执行 `pnpm --dir admin-ui typecheck`，必要时执行 `pnpm --dir admin-ui build`。
- 前端页面实现后使用浏览器验证关键页面、console、network 和主要交互。

## Constraints：约束

- 遵守 `AGENTS.md`。
- 不把密钥、Token、连接串写入 `.ai`。
- 不把完整日志写入 `.ai/CURRENT.md`、`.ai/TASKS.md` 或 `.ai/MEMORY.md`。
