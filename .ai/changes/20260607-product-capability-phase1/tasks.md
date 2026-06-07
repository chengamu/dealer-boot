# Tasks: 共享产品能力中心第一阶段落地

创建日期：2026-06-07

## Active Queue

| TaskId | Title | Owner | Status | Wave | Priority |
| --- | --- | --- | --- | --- | --- |
| PCC-P1-C01 | 冻结第一阶段 contract 与文件边界 | java-architect | done | 0 | high |
| PCC-P1-C02 | 三组菜单、按钮、权限和真实库 contract | java-architect | done | 0 | high |
| PCC-P1-C03 | UI 复刻、组件拆分和标准页面 contract | frontend-developer | done | 0 | high |
| PCC-P1-C04 | 验收脚本、浏览器路径和真实库核验 contract | browser-debugger | done | 0 | high |
| PCC-P1-D01 | 修正数据库菜单、按钮、字典和真实库执行脚本 | java-architect | done | 1 | high |
| PCC-P1-B01 | 补齐产品/配置/价格/发布/报价/审核/缺口/同步/导入后端闭环 | java-architect | done | 1 | high |
| PCC-P1-F01 | 历史页面和代码生成器标准化为 grid/list + drawer | frontend-developer | done | 1 | high |
| PCC-P1-F02 | 普通产品能力页面独立菜单化和标准 grid/list 改造 | frontend-developer | done | 1 | high |
| PCC-P1-F03 | 五个自定义页组件化和 H5/PNG 复刻实现 | frontend-developer | done | 1 | high |
| PCC-P1-I01 | 前后端 API、权限、i18n、UTC、字典选择器集成对齐 | typescript-pro | done | 2 | high |
| PCC-P1-I02 | 真实开发库执行和菜单/API 数据核验 | java-architect | done | 2 | high |
| PCC-P1-R01 | Static Review：第一阶段代码审计 | code-reviewer | done | 3 | high |
| PCC-P1-V01 | Runtime Validation：内部浏览器真实流程和视觉验收 | browser-debugger | done | 4 | high |
| PCC-P1-V02 | Build/Test/SQL/i18n 总验收和任务状态收口 | main | done | 4 | high |

## Completion Rule

- 任何任务只通过编译、空库或静态页面，不得标记为 `done`。
- `done` 必须满足任务 Acceptance，并且不违反 `docs/产品配置中心/共享产品能力中心开发入口.md` 第 7 节验收门。
- 未完成项必须保留 `pending`、`blocked` 或 `failed`，不能从任务清单删除。

## Deferred

- 客户价 / 促销价 / 复杂价格体系。
- 真实 ERP/MES 外部推送、回调确认、失败退避和定时重试。
- AI 智能识别、ConfigAgent、PricingAgent。
- 导入字段映射确认后的批量提交正式表、冲突合并和回滚策略。
- 独立服务拆分。
- 五个主自定义页继续做像素级细节优化和截图 diff；第一阶段已满足 H5/PNG 主结构、底部可见和关键操作门。
- 发布检查结果表后续增加批次号或去重策略，避免反复运行检查产生历史噪音。
- 引用检查记录不存在时当前返回摘要但 `allowed=true`；后续可按业务口径改为不可操作。
