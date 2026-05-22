import useUserStore from '@/stores/user'

function authPermission(permission: string) {
  const allPermission = '*:*:*'
  const permissions = useUserStore().permissions
  if (!permission) return false
  return permissions.some((item) => allPermission === item || item === permission)
}

function authRole(role: string) {
  const superAdmin = 'admin'
  const roles = useUserStore().roles
  if (!role) return false
  return roles.some((item) => superAdmin === item || item === role)
}

const auth = {
  hasPermi(permission: string) {
    return authPermission(permission)
  },
  hasPermiOr(permissions: string[]) {
    return permissions.some((item) => authPermission(item))
  },
  hasPermiAnd(permissions: string[]) {
    return permissions.every((item) => authPermission(item))
  },
  hasRole(role: string) {
    return authRole(role)
  },
  hasRoleOr(roles: string[]) {
    return roles.some((item) => authRole(item))
  },
  hasRoleAnd(roles: string[]) {
    return roles.every((item) => authRole(item))
  }
}

export default auth
