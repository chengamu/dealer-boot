# 04 - 工程与 BOM 模块

## 1. 模块目标

工程与 BOM 模块解决：

```text
能不能做
怎么生产
尺寸如何限制
成品尺寸如何扣减
用什么料
用多少料
怎么包装
```

订单不能自己维护 BOM。

## 2. 页面

一期页面：

```text
工程模板
尺寸能力限制
扣减规则
BOM 规则
组件包
包装规则
工程试算器
BOM 预览
```

## 3. 数据库

优先表：

```text
pc_engineering_template
pc_engineering_template_version
pc_dimension_limit_rule
pc_deduction_rule
pc_bom_rule
pc_packaging_rule
pc_component
pc_component_item
```

## 4. 后端建议

Controller：

```text
ProductEngineeringController
ProductBomController
```

Service：

```text
ProductEngineeringService
ProductBomService
```

Engine：

```text
EngineeringEvaluationEngine
BomPreviewEngine
```

## 5. 先做 CRUD

```text
工程模板
工程模板版本
尺寸能力限制
扣减规则
BOM 规则
包装规则
组件包明细
```

## 6. 必须尽早做试算器

工程模块不能只做 CRUD。

工程试算器最小输入：

```text
engineeringTemplateVersionId
productCode
width
height
selectedOptions
fabricCode
```

最小输出：

```text
dimensionResult
deductionResult
bomPreviewLines
packagingPreview
warnings
blockers
```

BOM 预览行至少包含：

```text
itemCode
itemName
quantity
unit
sourceRuleCode
sourceOptionCode
remark
```

## 7. 一期验收

```text
销售产品能绑定工程模板版本
输入宽高能判断是否超尺寸
选择答案后能带出组件
BOM 预览能输出物料行
BOM 结果来自后端，不由前端拼
```
