import type { DirectiveBinding, ObjectDirective } from 'vue'
import useUserStore from '@/stores/user'

const hasRole: ObjectDirective<HTMLElement, string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string[]>) {
    const { value } = binding
    const superAdmin = 'admin'
    const roles = useUserStore().roles

    if (Array.isArray(value) && value.length > 0) {
      const matched = roles.some((role) => superAdmin === role || value.includes(role))
      if (!matched) {
        el.parentNode?.removeChild(el)
      }
      return
    }

    throw new Error('请设置角色权限标签值')
  }
}

export default hasRole
