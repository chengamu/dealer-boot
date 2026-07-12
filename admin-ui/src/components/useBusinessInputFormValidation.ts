import { onBeforeUnmount, onMounted, reactive } from 'vue'
import { useFormItem, type FormItemContext } from 'element-plus'

let validationFieldSequence = 0

export function useBusinessInputFormValidation(isValid: () => boolean) {
  const { form } = useFormItem()
  const fieldKey = `businessInput${++validationFieldSequence}`
  const field = reactive({
    prop: undefined,
    propString: '',
    fieldValue: undefined,
    validateState: '',
    validateMessage: '',
    error: '',
    validate: async () => {
      if (isValid()) {
        field.validateState = 'success'
        field.validateMessage = ''
        return true
      }
      field.validateState = 'error'
      field.validateMessage = 'Invalid business number'
      return Promise.reject({
        [fieldKey]: [{ field: fieldKey, message: field.validateMessage }]
      })
    },
    resetField: () => undefined,
    clearValidate: () => {
      field.validateState = ''
      field.validateMessage = ''
    },
    setInitialValue: () => undefined,
    getInitialValue: () => undefined
  }) as unknown as FormItemContext

  onMounted(() => form?.addField(field))
  onBeforeUnmount(() => form?.removeField(field))
}
