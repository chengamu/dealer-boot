import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 资料绑定。
业务用途：把资料资产绑定到产品、物料、分类或其他目标对象上，控制资料在业务中的使用位置。
支持意图：说明页面用途、引导新增绑定、打开新增弹窗、填写绑定草稿、解绑资料、查询引用。
新增必填：资料、目标类型、目标编码、用途。可见范围、语言、是否发布必需、排序和备注按用户描述填写。
操作规则：不要让用户填写内部 targetId；优先使用目标编码或页面可选项。用户没有明确绑定对象时必须追问。
删除/解绑规则：解绑可能影响发布或客户可见资料，必须确认；删除、解绑、状态切换和保存需要确认。
不要暴露：bindingId、assetId、targetId、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Media Bindings.
Business purpose: Bind media assets to products, materials, categories, or other target objects and control where assets are used.
Supported intents: Explain the page, guide binding creation, open the add dialog, fill binding drafts, unbind assets, and check references.
Required for create: asset, target type, target code, and usage. Visibility, language, required-for-publish, sort order, and remarks follow user input.
Operation rule: Do not ask users for internal targetId. Prefer target codes or visible page options. If the binding target is unclear, ask a follow-up question.
Delete/unbind rule: unbinding may affect publishing or customer-visible assets and must be confirmed. Delete, unbind, status change, and save require confirmation.
Do not expose: bindingId, assetId, targetId, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const mediaBindingInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/media-bindings'],
  title: {
    zh_CN: '资料绑定',
    en_US: 'Media Bindings'
  },
  content
}
