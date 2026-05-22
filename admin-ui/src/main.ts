import { createApp } from 'vue'
import Cookies from 'js-cookie'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import 'element-plus/dist/index.css'
import 'nprogress/nprogress.css'
import 'virtual:svg-icons-register'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import { i18n } from './i18n'
import './styles/main.scss'
import '@/assets/styles/index.scss'
import plugins from '@/plugins'
import directive from '@/directive'
import { installLocale } from '@/locales'
import { getLocaleCookie } from '@/utils/auth'
import { download } from '@/utils/request'
import { useDict } from '@/utils/dict'
import { getConfigKey, updateConfigByKey } from '@/api/system/config'
import { addDateRange, handleTree, parseTime, resetForm, selectDictLabel, selectDictLabels } from '@/utils/ruoyi'
import SvgIcon from '@/components/SvgIcon/index.vue'
import elementIcons from '@/components/SvgIcon/svgicon'
import DictTag from '@/components/DictTag/index.vue'
import Editor from '@/components/Editor/index.vue'
import FileUpload from '@/components/FileUpload/index.vue'
import ImagePreview from '@/components/ImagePreview/index.vue'
import ImageUpload from '@/components/ImageUpload/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import TreeSelect from '@/components/TreeSelect/index.vue'

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
  locale: getLocaleCookie() === 'en_US' ? en : zhCn,
  size: (Cookies.get('size') || 'default') as 'large' | 'default' | 'small'
})

app.mount('#app')
