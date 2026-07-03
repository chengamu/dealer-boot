import { createApp } from 'vue'
import Cookies from 'js-cookie'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'nprogress/nprogress.css'
import 'virtual:svg-icons-register'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import { i18n, setupI18n } from './i18n'
import './styles/main.scss'
import '@/assets/styles/index.scss'
import plugins from '@/plugins'
import directive from '@/directive'
import { installLocale } from '@/locales'
import { download } from '@/utils/request'
import { useDict } from '@/utils/dict'
import { getConfigKey, updateConfigByKey } from '@/api/system/config'
import { addDateRange, handleTree, parseTime, resetForm, selectDictLabel, selectDictLabels } from '@/utils/ruoyi'
import SvgIcon from '@/components/SvgIcon/index.vue'
import elementIcons from '@/components/SvgIcon/svgicon'
import AdminDialog from '@/components/AdminDialog/index.vue'
import AdminDialogFooter from '@/components/AdminDialogFooter/index.vue'
import AdminDrawer from '@/components/AdminDrawer/index.vue'
import AdminEmptyState from '@/components/AdminEmptyState/index.vue'
import AdminSection from '@/components/AdminSection/index.vue'
import AdminSkeletonTable from '@/components/AdminSkeletonTable/index.vue'
import AdminTableActions from '@/components/AdminTableActions/index.vue'
import DictTag from '@/components/DictTag/index.vue'
import Editor from '@/components/Editor/index.vue'
import FileUpload from '@/components/FileUpload/index.vue'
import ImagePreview from '@/components/ImagePreview/index.vue'
import ImageUpload from '@/components/ImageUpload/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import TreeSelect from '@/components/TreeSelect/index.vue'

async function bootstrap() {
  await setupI18n()

  const app = createApp(App)

  app.config.globalProperties.useDict = useDict
  app.config.globalProperties.getConfigKey = getConfigKey
  app.config.globalProperties.updateConfigByKey = updateConfigByKey
  app.config.globalProperties.download = download
  app.config.globalProperties.parseTime = parseTime
  app.config.globalProperties.resetForm = resetForm
  app.config.globalProperties.handleTree = handleTree
  app.config.globalProperties.addDateRange = addDateRange
  app.config.globalProperties.selectDictLabel = selectDictLabel
  app.config.globalProperties.selectDictLabels = selectDictLabels

  app.component('AdminDialog', AdminDialog)
  app.component('AdminDialogFooter', AdminDialogFooter)
  app.component('AdminDrawer', AdminDrawer)
  app.component('AdminEmptyState', AdminEmptyState)
  app.component('AdminSection', AdminSection)
  app.component('AdminSkeletonTable', AdminSkeletonTable)
  app.component('AdminTableActions', AdminTableActions)
  app.component('DictTag', DictTag)
  app.component('Editor', Editor)
  app.component('FileUpload', FileUpload)
  app.component('ImagePreview', ImagePreview)
  app.component('ImageUpload', ImageUpload)
  app.component('Pagination', Pagination)
  app.component('RightToolbar', RightToolbar)
  app.component('SvgIcon', SvgIcon)
  app.component('TreeSelect', TreeSelect)

  app.use(pinia)
  app.use(i18n)
  app.use(router)
  app.use(plugins)
  app.use(elementIcons)
  installLocale(app)
  directive(app)
  app.use(ElementPlus, {
    size: (Cookies.get('size') || 'default') as 'large' | 'default' | 'small'
  })

  app.mount('#app')
}

void bootstrap().catch((error) => {
  console.error('Failed to bootstrap application', error)
})
