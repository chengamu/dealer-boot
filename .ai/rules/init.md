# 初始化规则

`/init` 用于初始化项目上下文。它是内部维护流程，不是日常主命令。

## 范围

- 扫描项目结构。
- 从真实项目文件确认稳定技术栈和命令。
- 创建或确认轻量 `.ai` 结构。
- 只更新稳定事实和 TODO。

## 需要确认的文件 / 目录

- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/HANDOFF.md`
- `.ai/DECISIONS.md`
- `.ai/MEMORY.md`
- `.ai/requirements/`
- `.ai/rules/`
- `.ai/playbooks/`
- `.ai/archive/`
- `.ai/tmp/`
- `.ai/artifacts/`

## 规则

- 不修改业务代码。
- 不新增或升级依赖。
- 不运行会修改环境、数据库或生成大量文件的命令。
- 需要时用 CodeGraph 辅助理解结构。
- 最终事实必须从真实项目文件确认。
- 不确定内容写 TODO。
- 不把详细调用图或完整文件树复制进 `.ai`。
