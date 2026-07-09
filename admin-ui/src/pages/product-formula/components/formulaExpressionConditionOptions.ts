export type BuilderState = {
  field: string
  operator: string
  value: string
  joiner: string
}

export type MaterialBuilderState = BuilderState & {
  role: string
}

export type ConditionValue = {
  valueCode?: string
  valueNameCn?: string
  valueNameEn?: string
  label?: string
  insert?: string
}

export type ConditionField = {
  label: string
  name: string
  insert: string
  optionCode?: string
  attributeCode?: string
  stringValue?: boolean
}

export function distinctOptions(values: ConditionValue[]) {
  const map = new Map<string, ConditionValue>()
  values.forEach((value) => {
    if (value.valueCode && !map.has(value.valueCode)) map.set(value.valueCode, value)
  })
  return Array.from(map.values())
}

export function ensureBuilderField(builder: BuilderState, fields: ConditionField[]) {
  if (!fields.some((field) => field.name === builder.field)) builder.field = fields[0]?.name || ''
}

export function resetBuilderValue(builder: BuilderState, values: ConditionValue[]) {
  builder.value = values[0]?.valueCode || ''
}

export function valueLabel(value: ConditionValue) {
  if (value.label) return value.label
  return value.valueNameCn && value.valueNameCn !== value.valueCode ? `${value.valueNameCn} (${value.valueCode})` : value.valueCode || '-'
}

export function buildConditionText(field: ConditionField | undefined, builder: BuilderState, values: ConditionValue[]) {
  if (!field) return ''
  const rawValue = String(builder.value || '').trim()
  const valueText = conditionValueText(rawValue, values)
  const displayValue = needsQuotedValue(field, valueText) ? `"${valueText.replace(/"/g, '\\"')}"` : valueText
  return `${field.insert} ${builder.operator} ${displayValue}`
}

function conditionValueText(valueCode: string, values: ConditionValue[]) {
  const value = values.find((item) => item.valueCode === valueCode)
  return value?.insert || value?.valueNameCn || valueCode
}

function needsQuotedValue(field: ConditionField, value: string) {
  if (!value || value.startsWith('"') || value.startsWith("'")) return false
  return field.stringValue || Number.isNaN(Number(value))
}
