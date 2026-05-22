import { getAppTitle } from '@/utils/config'

export interface AppSettings {
  title: string
  sideTheme: string
  showSettings: boolean
  topNav: boolean
  tagsView: boolean
  fixedHeader: boolean
  sidebarLogo: boolean
  dynamicTitle: boolean
  errorLog: string | string[]
}

const settings: AppSettings = {
  /**
   * Page title
   */
  title: getAppTitle(),

  /**
   * Sidebar theme: theme-dark or theme-light
   */
  sideTheme: 'theme-light',

  /**
   * Whether to show system layout settings
   */
  showSettings: true,

  /**
   * Whether to show top navigation
   */
  topNav: false,

  /**
   * Whether to show tags view
   */
  tagsView: true,

  /**
   * Whether to fix the header
   */
  fixedHeader: false,

  /**
   * Whether to show sidebar logo
   */
  sidebarLogo: true,

  /**
   * Whether to enable dynamic document title
   */
  dynamicTitle: false,

  /**
   * Need show err logs component.
   */
  errorLog: 'production'
}

export default settings
