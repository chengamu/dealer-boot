import { defineStore } from 'pinia'
import { getInfo, login, logout } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'
import type { LoginUser } from '@/types/api'
import defaultAvatar from '@/assets/logo/logo.png'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    user: null as LoginUser | null,
    id: '',
    name: '',
    avatar: defaultAvatar,
    roles: [] as string[],
    permissions: [] as string[]
  }),
  getters: {
    displayName: (state) => state.user?.nickName || state.user?.userName || state.name || 'admin'
  },
  actions: {
    async login(usernameOrInfo: string | { username: string; password: string; code?: string; uuid?: string }, password?: string, code?: string, uuid?: string) {
      const payload =
        typeof usernameOrInfo === 'string'
          ? { username: usernameOrInfo, password: password || '', code, uuid }
          : {
              username: usernameOrInfo.username?.trim(),
              password: usernameOrInfo.password,
              code: usernameOrInfo.code,
              uuid: usernameOrInfo.uuid
            }
      const res = await login(payload)
      const token = String(res.token || (typeof res.data === 'string' ? res.data : res.data?.token) || '')
      if (token) {
        this.token = token
        setToken(token)
      }
    },
    async loadUser() {
      const res = await getInfo()
      const data = res.data && 'user' in res.data ? res.data : undefined
      const user = (res.user || data?.user || (res.data && !('user' in res.data) ? res.data : {}) || {}) as LoginUser
      this.user = {
        ...user,
        tenantId: res.tenantId || data?.tenantId || user.tenantId,
        tenantType: res.tenantType || data?.tenantType || user.tenantType,
        merchantId: res.merchantId || data?.merchantId || user.merchantId
      }
      this.roles = res.roles || data?.roles || []
      this.permissions = res.permissions || data?.permissions || []
      this.id = String(this.user?.userId || '')
      this.name = this.user?.userName || ''
      this.avatar = this.user?.avatar || defaultAvatar
      if (!this.roles.length) this.roles = ['ROLE_DEFAULT']
      return res
    },
    getInfo() {
      return this.loadUser()
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.user = null
        this.id = ''
        this.name = ''
        this.avatar = defaultAvatar
        this.roles = []
        this.permissions = []
        removeToken()
      }
    },
    logOut() {
      return this.logout()
    }
  }
})

export default useUserStore
