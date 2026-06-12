# Tasks: 产品配置中心基础资料模块重构

任务源：`.ai/changes/20260611-product-base-info-refactor/wave-plan.md`

## Active Tasks

- `BASE-C1`：定义数据库与后端 contract，Status: `done`
- `BASE-C2`：定义前端页面、API 类型和 i18n contract，Status: `done`
- `BASE-C3`：定义 OFBiz seed 映射与 5 个样本 contract，Status: `done`
- `BASE-D1`：实现数据库脚本和基础 seed 字典，Status: `done`
- `BASE-B1`：实现后端基础资料 API 与服务，Status: `done`
- `BASE-F1`：实现前端基础资料页面和 API 类型，Status: `done`
- `BASE-S1`：实现 OFBiz 开发 seed 工具或 SQL，Status: `done`
- `BASE-I1`：对齐前后端 API、VO、类型和权限，Status: `done`
- `BASE-I2`：对齐 seed 数据和业务验收样本，Status: `done`
- `BASE-R1`：静态审查基础资料重构风险，Status: `done`
- `BASE-V1`：后端与 SQL 验证，Status: `done`
- `BASE-V2`：前端和浏览器验证，Status: `partial`

## Notes

- `/plan` 阶段不实现代码。
- `/do` 阶段按 Wave Scheduler 执行。
- 修改数据库脚本前必须以 Wave 0 contract 为准。
- OFBiz seed 读取不允许输出明文连接串、密码、Token。
