import type { DirectiveBinding, ObjectDirective } from 'vue'
import { i18n } from '@/i18n'
import useUserStore from '@/stores/user'

const hasPermi: ObjectDirective<HTMLElement, string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string[]>) {
    const { value } = binding
    const allPermission = '*:*:*'
    const permissions = useUserStore().permissions

    if (Array.isArray(value) && value.length > 0) {
      const hasPermissions = permissions.some((permission) => allPermission === permission || value.includes(permission))
      if (!hasPermissions) {
        el.parentNode?.removeChild(el)
      }
      return
    }

    throw new Error(i18n.global.t('permission.missingPermissionValue'))
  }
}

export default hasPermi
