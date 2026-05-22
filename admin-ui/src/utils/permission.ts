import useUserStore from '@/stores/user'

export function checkPermi(value: string[]) {
  if (Array.isArray(value) && value.length > 0) {
    const permissions = useUserStore().permissions
    const allPermission = '*:*:*'
    return permissions.some((permission) => allPermission === permission || value.includes(permission))
  }

  console.error(`need roles! Like checkPermi="['system:user:add','system:user:edit']"`)
  return false
}

export function checkRole(value: string[]) {
  if (Array.isArray(value) && value.length > 0) {
    const roles = useUserStore().roles
    const superAdmin = 'admin'
    return roles.some((role) => superAdmin === role || value.includes(role))
  }

  console.error(`need roles! Like checkRole="['admin','editor']"`)
  return false
}
