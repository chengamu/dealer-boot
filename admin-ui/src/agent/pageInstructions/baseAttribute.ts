import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 物料属性。
业务用途：维护物料属性模板，例如颜色、纹理、电压、重量等。属性模板属于某个属性分组，后续由物料类型和物料管理使用。
支持意图：说明页面用途、引导新增属性模板、打开新增弹窗、填写属性草稿、停用属性、查询引用。
新增必填：属性分组、属性编码、属性名称、值类型。值类型为数字时可以填写默认单位。
操作规则：不要把“属性值”误当成“属性模板”；用户要维护具体物料的颜色/重量数值时，应引导到物料属性值或物料管理。
删除/停用规则：已被物料类型、物料属性值或物料引用时不建议删除，优先停用；删除、状态切换和保存需要确认。
不要暴露：attributeId、attributeGroupId、tenantId、extraJson、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Material Attributes.
Business purpose: Maintain material attribute templates such as color, texture, voltage, and weight. Attribute templates belong to attribute groups and are used by material types and materials.
Supported intents: Explain the page, guide attribute-template creation, open the add dialog, fill attribute drafts, disable attributes, and check references.
Required for create: attribute group, attribute code, attribute name, and value type. If the value type is number, a default unit may be filled.
Operation rule: Do not confuse an attribute template with a concrete attribute value. If the user wants concrete color or weight values for a material, guide them to material attribute values or material management.
Delete/disable rule: attributes referenced by material types, material attribute values, or materials should not be deleted; disabling is preferred. Delete, status change, and save require confirmation.
Do not expose: attributeId, attributeGroupId, tenantId, extraJson, delFlag, audit fields, or raw payloads.
`.trim()
}

export const baseAttributeInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/base-attributes'],
  title: {
    zh_CN: '物料属性',
    en_US: 'Material Attributes'
  },
  content
}
