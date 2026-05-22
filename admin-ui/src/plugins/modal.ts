import { ElLoading, ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'
import { getMessage } from '@/locales'
import { getLocale } from '@/utils/auth'

const t = (key: string) => getMessage(key, getLocale())

let loadingInstance: LoadingInstance | undefined

const modal = {
  msg(content: string) {
    ElMessage.info(content)
  },
  msgError(content: string) {
    ElMessage.error(content)
  },
  msgSuccess(content: string) {
    ElMessage.success(content)
  },
  msgWarning(content: string) {
    ElMessage.warning(content)
  },
  alert(content: string) {
    return ElMessageBox.alert(content, t('common.systemPrompt'))
  },
  alertError(content: string) {
    return ElMessageBox.alert(content, t('common.systemPrompt'), { type: 'error' })
  },
  alertSuccess(content: string) {
    return ElMessageBox.alert(content, t('common.systemPrompt'), { type: 'success' })
  },
  alertWarning(content: string) {
    return ElMessageBox.alert(content, t('common.systemPrompt'), { type: 'warning' })
  },
  notify(content: string) {
    ElNotification.info(content)
  },
  notifyError(content: string) {
    ElNotification.error(content)
  },
  notifySuccess(content: string) {
    ElNotification.success(content)
  },
  notifyWarning(content: string) {
    ElNotification.warning(content)
  },
  confirm(content: string) {
    return ElMessageBox.confirm(content, t('common.systemPrompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
  },
  prompt(content: string) {
    return ElMessageBox.prompt(content, t('common.systemPrompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
  },
  loading(content: string) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: content,
      background: 'rgba(0, 0, 0, 0.7)'
    })
  },
  closeLoading() {
    loadingInstance?.close()
  }
}

export default modal
