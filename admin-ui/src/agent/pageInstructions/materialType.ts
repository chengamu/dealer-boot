import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 物料类型。
业务用途：维护属性分组和具体物料类型。属性分组用于归类属性模板，物料类型用于新增物料时带出对应属性分组。
支持意图：说明页面用途、引导新增属性分组、引导新增物料类型、打开新增弹窗、填写草稿、停用分组或类型、查询引用。
新增属性分组必填：分组编码、分组名称。
新增物料类型必填：先选择属性分组，再填写物料类型编码、物料类型名称。
可建议：英文名、排序、是否展示配方摘要可以按用户描述建议；状态默认启用。
操作规则：先在左侧选中属性分组，再维护右侧物料类型；如果未选中分组，先提示用户选择或帮助点击对应分组。
删除/停用规则：已被物料、物料属性或业务引用的分组/类型不能删除，建议停用；删除和状态切换需要用户确认。
不要暴露：groupId、materialTypeId、attributeGroupId、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Material Types.
Business purpose: Maintain attribute groups and concrete material types. Attribute groups organize attribute templates; material types bring the matching attribute group when creating materials.
Supported intents: Explain the page, guide attribute-group creation, guide material-type creation, open add dialogs, fill drafts, disable groups or types, and check references.
Required for new attribute group: group code and group name.
Required for new material type: select an attribute group first, then fill material type code and material type name.
Suggested defaults: English name, sort order, and formula-summary visibility may be suggested from the user description; status defaults to enabled.
Operation rule: select an attribute group on the left first, then maintain material types on the right. If no group is selected, ask the user to choose one or help click the matching group.
Delete/disable rule: groups or types referenced by materials, material attributes, or business data cannot be deleted; disabling is recommended. Deletion and status changes require user confirmation.
Do not expose: groupId, materialTypeId, attributeGroupId, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const materialTypeInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/material-types'],
  title: {
    zh_CN: '物料类型',
    en_US: 'Material Types'
  },
  content
}
