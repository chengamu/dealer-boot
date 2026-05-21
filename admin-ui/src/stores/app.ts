import { defineStore } from 'pinia'
import Cookies from 'js-cookie'

export type AppDevice = 'desktop' | 'mobile'
export type ElementSize = 'large' | 'default' | 'small'

export interface SidebarState {
  opened: boolean
  withoutAnimation: boolean
  hide: boolean
}

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: Cookies.get('sidebarStatus') ? Boolean(Number(Cookies.get('sidebarStatus'))) : true,
      withoutAnimation: false,
      hide: false
    } as SidebarState,
    device: 'desktop' as AppDevice,
    size: (Cookies.get('size') || 'default') as ElementSize
  }),
  actions: {
    toggleSideBar(withoutAnimation = false) {
      if (this.sidebar.hide) return false
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = withoutAnimation
      Cookies.set('sidebarStatus', this.sidebar.opened ? '1' : '0')
      return true
    },
    closeSideBar({ withoutAnimation }: { withoutAnimation: boolean }) {
      Cookies.set('sidebarStatus', '0')
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    },
    toggleDevice(device: AppDevice) {
      this.device = device
    },
    setSize(size: ElementSize) {
      this.size = size
      Cookies.set('size', size)
    },
    toggleSideBarHide(status: boolean) {
      this.sidebar.hide = status
    }
  }
})

export default useAppStore
