import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 基础字典。
业务用途：维护产品配置使用的基础枚举和下拉选项。左侧是字典类型，右侧是当前类型下的字典项。
支持意图：说明页面用途、引导新增字典类型、引导新增字典项、打开新增弹窗、填写草稿、停用字典类型或字典项、查询引用。
新增字典类型必填：字典类型编码、字典类型名称。
新增字典项必填：先选择字典类型，再填写字典项编码、字典项名称。
操作规则：用户说新增“选项/下拉项/枚举值”时，优先理解为新增右侧字典项；如果没有选中或说明字典类型，先追问或帮助选择。
删除/停用规则：已被页面字段、物料、配方或业务数据引用时不建议删除，优先停用；删除、状态切换和保存都需要确认。
不要暴露：dictTypeId、dictItemId、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Base Dictionaries.
Business purpose: Maintain base enums and dropdown options used by product configuration. The left side is dictionary types; the right side is items of the selected type.
Supported intents: Explain the page, guide dictionary-type creation, guide dictionary-item creation, open add dialogs, fill drafts, disable types or items, and check references.
Required for new dictionary type: dictionary type code and dictionary type name.
Required for new dictionary item: select a dictionary type first, then fill item code and item name.
Operation rule: If the user says add an option/dropdown item/enum value, prefer creating a right-side dictionary item. If the dictionary type is not selected or specified, ask or help select it first.
Delete/disable rule: if referenced by page fields, materials, formulas, or business data, deletion is not recommended; disabling is preferred. Delete, status change, and save require confirmation.
Do not expose: dictTypeId, dictItemId, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const productDictInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/product-dicts'],
  title: {
    zh_CN: '基础字典',
    en_US: 'Base Dictionaries'
  },
  content
}
