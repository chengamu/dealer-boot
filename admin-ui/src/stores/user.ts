import { defineStore } from 'pinia'
import { getInfo, googleLogin, login, logout } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'
import type { LoginUser } from '@/types/api'
import defaultAvatar from '@/assets/logo/logo.png'

type UserInfoPayload = {
  user?: LoginUser
  roles?: string[]
  permissions?: string[]
  tenantId?: number
  tenantType?: string
  merchantId?: number
  defaultMenuId?: number
  defaultRoute?: string
  defaultRouteTitle?: string
  forcePasswordChange?: boolean
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    user: null as LoginUser | null,
    id: '',
    name: '',
    avatar: defaultAvatar,
    defaultMenuId: null as number | null,
    defaultRoute: '',
    defaultRouteTitle: '',
    roles: [] as string[],
    permissions: [] as string[]
  }),
  getters: {
    displayName: (state) => state.user?.nickName || state.user?.userName || state.name || 'admin',
    forcePasswordChange: (state) => state.user?.forcePasswordChange === true || state.user?.forcePasswordChange === '1'
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
    async loginWithGoogle(credential: string) {
      const res = await googleLogin({ credential })
      const data = res.data || {}
      const token = String(res.token || data.token || '')
      if (token) {
        this.token = token
        setToken(token)
      }
      return {
        ...data,
        login: Boolean(data.login ?? token),
        token: token || data.token,
        forcePasswordChange: res.forcePasswordChange ?? data.forcePasswordChange
      }
    },
    async loadUser() {
      const res = await getInfo() as UserInfoPayload & { data?: LoginUser | UserInfoPayload }
      const data = res.data && typeof res.data === 'object' && 'user' in res.data ? res.data as UserInfoPayload : undefined
      const user = (res.user || data?.user || (res.data && !('user' in res.data) ? res.data : {}) || {}) as LoginUser
      this.user = {
        ...user,
        tenantId: res.tenantId || data?.tenantId || user.tenantId,
        tenantType: res.tenantType || data?.tenantType || user.tenantType,
        merchantId: res.merchantId || data?.merchantId || user.merchantId,
        forcePasswordChange: res.forcePasswordChange ?? data?.forcePasswordChange ?? user.forcePasswordChange
      }
      this.roles = res.roles || data?.roles || []
      this.permissions = res.permissions || data?.permissions || []
      this.id = String(this.user?.userId || '')
      this.name = this.user?.userName || ''
      this.avatar = this.user?.avatar || defaultAvatar
      this.defaultMenuId = res.defaultMenuId || data?.defaultMenuId || null
      this.defaultRoute = res.defaultRoute || data?.defaultRoute || ''
      this.defaultRouteTitle = res.defaultRouteTitle || data?.defaultRouteTitle || ''
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
        this.defaultMenuId = null
        this.defaultRoute = ''
        this.defaultRouteTitle = ''
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
