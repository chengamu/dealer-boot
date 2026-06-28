import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 资料资产。
业务用途：维护产品、物料、安装说明、样册、图片等资料资产的基础信息和访问地址。
支持意图：说明页面用途、引导新增资料、打开新增弹窗、填写资料草稿、停用资料、查询引用。
新增必填：资料编码、资料名称。资料类型、用途、语言、可见范围、URL、版本号按用户信息填写。
上传边界：上传附件是高风险动作，必须用户确认；没有文件时只能填写 URL 或资料草稿，不要声称已上传。
删除/停用规则：已绑定或已发布使用的资料不建议删除，优先停用或解绑；删除、上传、状态切换和保存需要确认。
不要暴露：assetId、ossId 内部对象、tenantId、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Media Assets.
Business purpose: Maintain base information and access URLs for product, material, installation guide, brochure, image, and other assets.
Supported intents: Explain the page, guide asset creation, open the add dialog, fill asset drafts, disable assets, and check references.
Required for create: asset code and asset name. Asset type, usage, language, visibility, URL, and version follow user input.
Upload boundary: attachment upload is high risk and requires user confirmation. If no file is provided, only fill URL or asset draft fields; do not claim an upload happened.
Delete/disable rule: assets already bound or published should not be deleted; disabling or unbinding is preferred. Delete, upload, status change, and save require confirmation.
Do not expose: assetId, internal ossId object, tenantId, delFlag, audit fields, or raw payloads.
`.trim()
}

export const mediaAssetInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/media-assets'],
  title: {
    zh_CN: '资料资产',
    en_US: 'Media Assets'
  },
  content
}
