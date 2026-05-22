import type { DirectiveBinding, ObjectDirective } from 'vue'
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

    throw new Error('请设置操作权限标签值')
  }
}

export default hasPermi
