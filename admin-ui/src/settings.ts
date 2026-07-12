import { getAppTitle } from '@/utils/config'

export type HeaderLayout = 'compact' | 'stacked'

export interface AppSettings {
  title: string
  theme: string
  sideTheme: string
  showSettings: boolean
  topNav: boolean
  tagsView: boolean
  headerLayout: HeaderLayout
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
   * Theme color
   */
  theme: '#1677FF',

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
   * Header layout: compact or stacked
   */
  headerLayout: 'compact',

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
