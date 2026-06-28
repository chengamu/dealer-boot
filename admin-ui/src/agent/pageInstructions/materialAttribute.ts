import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 物料属性值。
业务用途：查看或维护具体物料上的属性值，例如某个面料的颜色、纹理、重量、电压等。
支持意图：说明页面用途、按物料或属性查询、引导查看属性值、填写属性值草稿、停用属性值。
新增/修改必填：物料、属性编码，以及与属性值类型匹配的值字段，例如文本值、数字值、布尔值或单位。
操作规则：当前页面偏数据维护和核对。用户说“新增物料属性模板”时，应引导到物料属性；用户说“给某个物料填颜色/重量”等具体值时，才使用本页。
删除/停用规则：属性值被配置、配方或发布数据使用时不建议删除，优先停用；删除、状态切换和保存需要确认。
不要暴露：attributeValueId、materialId、attributeId、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Material Attribute Values.
Business purpose: View or maintain concrete attribute values on materials, such as color, texture, weight, or voltage for a specific material.
Supported intents: Explain the page, query by material or attribute, guide value inspection, fill attribute-value drafts, and disable values.
Required for create/edit: material, attribute code, and the value field matching the attribute value type, such as text value, number value, boolean value, or unit.
Operation rule: This page is for data maintenance and checking. If the user asks to create a material attribute template, guide them to Material Attributes. Use this page when they want concrete values for a material.
Delete/disable rule: values used by configuration, formulas, or published data should not be deleted; disabling is preferred. Delete, status change, and save require confirmation.
Do not expose: attributeValueId, materialId, attributeId, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const materialAttributeInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/material-attributes'],
  title: {
    zh_CN: '物料属性值',
    en_US: 'Material Attribute Values'
  },
  content
}
