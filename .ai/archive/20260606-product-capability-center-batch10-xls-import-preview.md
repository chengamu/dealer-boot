# 共享产品能力中心 Batch 10：XLS/XLSX 导入解析预览基础层

## 结论

Batch 10 已完成并验证通过。

本批把导入中心从“人工维护批次和问题”推进到“可以上传并解析 XLS/XLSX”。旧 Excel 只作为字段口径参考，不复制旧库结构；组件、物料、产品模型、价格方案统一通过共享主数据编码匹配，缺失时写入行级问题，不自动创建重复基础数据。

## 完成内容

- 新增 `/product-capability/import/batches/parse-excel`，只允许 `xls/xlsx`。
- 复用现有 `bocoo-common-excel` 和 EasyExcel，不新增依赖。
- 解析表头并生成字段映射 `mapping_json`。
- 解析数据行并生成预览 `preview_json`，预览最多保留前 200 行。
- 生成错误汇总 `error_summary_json`。
- 对 Excel 中出现的组件编码、物料编码、产品模型编码、价格方案编码做共享主数据校验。
- 共享主数据缺失时写入 `pc_import_row_issue`，级别为 `ERROR`。
- 新增前端 API `productImportBatchApi.parseExcel(file, data)`。
- 更新 API 约束文档和 AMU 任务台账。

## 当前边界

- 当前只做解析预览和问题清单，不提交正式配置、组件、物料、答案或价格表。
- 当前字段映射是有限别名识别，不做 AI 智能识别。
- 当前不做冲突合并、不做导入撤销、不做按目标对象事务提交。
- 当前没有新增菜单权限，复用 `product:import:add`。

## 验证

- `mvn -pl bocoo-modules-product -am -DskipTests clean compile`：passed。
- `mvn -pl bocoo-admin -am -DskipTests compile`：passed。
- `./node_modules/.bin/vue-tsc --noEmit`：passed。
- `codegraph sync`：passed。
- `git diff --check`：passed。

## 残余风险

- 真实旧 Excel 字段可能比当前别名更复杂，需要拿样本后继续补映射规则。
- 批量提交正式表需要单独设计目标对象事务、审计、冲突合并和回滚策略。
- AI 智能识别仍为后续能力，不能在运行时直接调用 AI 自动改正式数据。

## 下一步

- 补导入字段映射确认 UI。
- 设计导入提交正式表的目标对象事务和冲突合并策略。
- 在拿到真实 XLS 样本后扩充字段别名和校验规则。
- 继续 ConfigAgent / PricingAgent、真实 ERP/MES 推送回调、客户价/促销价和独立服务拆分。
