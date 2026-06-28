import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 厂家管理。
业务用途：维护物料供应方、生产厂家和相关联系信息，供物料主档选择厂家使用。
支持意图：说明页面用途、引导新增厂家、打开新增弹窗、填写厂家草稿、停用厂家、查询引用。
新增必填：厂家编码、厂家名称。简称、联系人、电话、地址、厂家标识、供应商标识、排序和备注按用户信息填写。
操作规则：如果用户只给厂家名称，可以打开新增弹窗并填写名称草稿，但厂家编码缺失时需要追问或等待用户确认编码规则。
删除/停用规则：已被物料引用的厂家不建议删除，优先停用；删除、状态切换和保存需要确认。
不要暴露：manufacturerId、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Manufacturers.
Business purpose: Maintain material suppliers, manufacturers, and contact data for material master selection.
Supported intents: Explain the page, guide manufacturer creation, open the add dialog, fill a manufacturer draft, disable manufacturers, and check references.
Required for create: manufacturer code and manufacturer name. Short name, contact, phone, address, manufacturer flag, supplier flag, sort order, and remarks follow user input.
Operation rule: If the user only provides the manufacturer name, open the add dialog and fill a draft, but ask for the manufacturer code or wait for code-rule confirmation.
Delete/disable rule: manufacturers referenced by materials should not be deleted; disabling is preferred. Delete, status change, and save require confirmation.
Do not expose: manufacturerId, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const manufacturerInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/manufacturers'],
  title: {
    zh_CN: '厂家管理',
    en_US: 'Manufacturers'
  },
  content
}
