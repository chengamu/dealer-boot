import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 产品分类。
业务用途：维护产品分类树，作为物料、销售产品、工程方案和后续配置的基础分类来源。分类不是具体物料，也不是销售产品。
支持意图：说明页面用途、引导新增分类、打开新增分类弹窗、填写新增分类草稿、停用分类、查询分类引用。
新增必填：分类编码、分类名称、上级分类或明确为顶级分类。
默认上级：用户要求新增分类但没有指定上级分类时，默认按顶级分类处理，不要追问；只有用户明确说“子分类/下级/放到某分类下面”但没给父级时才追问。
可建议：分类英文名称可以留空或按名称建议；排序可以按同级末尾建议；状态默认启用。
新增顺序：如果用户已给分类编码和分类名称，可以直接点新增并填写草稿；未指定上级时保持顶级分类；填写分类编码、分类名称、英文名、排序和备注后，展示摘要并停在保存前。
数字字段：排序是数字输入框，输入前先选中或清空旧值，输入后必须确认页面可见值已经变成目标数字。
删除/停用规则：删除前必须引用检查；有引用时建议停用；用户说删除第一条时，应先说明风险并请求确认，不要直接点删除按钮或确认删除。
不要暴露：categoryId、parentId、categoryLevel、categoryPath、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Product Categories.
Business purpose: Maintain the product category tree. It is the base category source for materials, sales products, engineering plans, and later configuration. A category is not a concrete material or sales product.
Supported intents: Explain the page, guide category creation, open the add-category dialog, fill a category draft, disable a category, and check category references.
Required for create: category code, category name, and parent category or an explicit root-category choice.
Default parent: If the user asks to create a category but does not specify a parent, treat it as a root category and do not ask. Ask only when the user explicitly says subcategory/child/under another category but does not provide the parent.
Suggested defaults: English name may be left blank or suggested from the name; sort order may be suggested at the end of the same level; status defaults to enabled.
Create flow: If the user provided category code and category name, click add and fill the draft directly. Keep root category when no parent is specified. Fill category code, category name, English name, sort order, and remarks, then show a summary and stop before save.
Numeric fields: Sort order is a numeric input. Select or clear the old value before typing, then verify that the visible value changed to the target number.
Delete/disable rule: check references before deletion. If referenced, recommend disabling. If the user asks to delete the first row, explain the risk and ask for confirmation before clicking delete or confirming deletion.
Do not expose: categoryId, parentId, categoryLevel, categoryPath, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const productCategoryInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/categories'],
  title: {
    zh_CN: '产品分类',
    en_US: 'Product Categories'
  },
  content
}
