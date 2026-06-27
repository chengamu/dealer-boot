import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 单位管理。
业务用途：维护物料、属性、工程和后续业务使用的统一单位。
支持意图：说明页面用途、引导新增单位、打开新增单位弹窗、填写单位草稿、停用单位、查询单位引用。
新增必填：单位编码、中文名称。单位类型、英文名称、基准单位、换算率按页面字段和用户提供信息填写。
可建议：排序可建议；状态默认启用。涉及换算率时必须向用户确认换算方向。
新增顺序：点新增，填写单位编码、中文名称、单位类型和必要换算信息，停在保存前让用户检查。
删除/停用规则：被物料或业务引用时不建议删除，优先停用；删除和状态切换需要用户确认。
不要暴露：unitId、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Unit Management.
Business purpose: Maintain unified units used by materials, attributes, engineering, and later business flows.
Supported intents: Explain the page, guide unit creation, open the add-unit dialog, fill a unit draft, disable a unit, and check unit references.
Required for create: unit code and Chinese name. Unit type, English name, base unit, and conversion rate should follow the page fields and user-provided information.
Suggested defaults: sort order may be suggested; status defaults to enabled. If a conversion rate is involved, confirm the conversion direction with the user.
Create flow: click add, fill unit code, Chinese name, unit type, and required conversion information, then stop before save and ask the user to review.
Delete/disable rule: if referenced by materials or business data, deletion is not recommended; disabling is preferred. Deletion and status changes require user confirmation.
Do not expose: unitId, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const unitInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/units'],
  title: {
    zh_CN: '单位管理',
    en_US: 'Unit Management'
  },
  content
}
