import type { PageAgentPageInstruction } from './types'

const content = {
  zh_CN: `
当前页面：基础信息 / 物料管理。
业务用途：维护统一物料主档。面料、铝材、电机、配件、包装件等都进入物料管理。
支持意图：说明页面用途、引导新增物料、打开新增物料抽屉、填写物料草稿、查询引用、查看变更记录、审核或取消审核前引导。
新增必填：物料编码、物料名称、物料类型、规格、主单位。
可选字段：英文名、型号、颜色、重量、副单位、单价、厂家、厂家货号、排序、备注、类型属性值。
可推断：新增物料默认未审核；选择物料类型后，页面会加载对应属性分组和类型属性。
不要猜测：单价、采购、库存、生产工艺结果、厂家信息、内部 ID。用户没有明确给出时必须追问或留空。
新增顺序：点新增，填写基础信息，选择物料类型和主单位，按需要补规格/型号/厂家/属性，最后停在保存前给业务摘要让用户检查。
编辑规则：已审核物料普通修改前需要先取消审核；超级修改必须用户明确要求并具备权限。
删除/审核规则：删除前必须引用检查；已审核或已引用物料不要直接删除，建议取消审核让它不再进入新增业务选择。审核、取消审核、删除、保存、超级修改都需要用户确认。
不要暴露：materialId、materialTypeId、attributeGroupId、manufacturerId、tenantId、auditBy、auditTime、specModelText、delFlag、审计字段和原始 payload。
`.trim(),
  en_US: `
Current page: Basic Information / Material Management.
Business purpose: Maintain the unified material master. Fabric, aluminum, motors, accessories, packaging items, and similar records all belong here.
Supported intents: Explain the page, guide material creation, open the add-material drawer, fill a material draft, check references, view change logs, and guide approve or unapprove actions before confirmation.
Required for create: material code, material name, material type, specification, and primary unit.
Optional fields: English name, model, color, weight, secondary unit, unit price, manufacturer, manufacturer item number, sort order, remarks, and type attribute values.
Inferred behavior: new materials default to unapproved. After choosing a material type, the page loads the matching attribute group and type attributes.
Do not guess: unit price, purchasing, inventory, production process results, manufacturer information, or internal IDs. If the user does not provide them, ask a follow-up question or leave them blank.
Create flow: click add, fill basic information, choose material type and primary unit, add specification/model/manufacturer/attributes as needed, then stop before save and give a business summary for user review.
Edit rule: approved materials need to be unapproved before normal editing. Super edit must be explicitly requested by the user and requires permission.
Delete/approve rule: check references before deletion. Do not directly delete approved or referenced materials; recommend unapproving them so they no longer appear in new business selections. Approve, unapprove, delete, save, and super edit all require user confirmation.
Do not expose: materialId, materialTypeId, attributeGroupId, manufacturerId, tenantId, auditBy, auditTime, specModelText, delFlag, audit fields, or raw payloads.
`.trim()
}

export const materialInstruction: PageAgentPageInstruction = {
  routes: ['/product-master/materials'],
  title: {
    zh_CN: '物料管理',
    en_US: 'Material Management'
  },
  content
}
