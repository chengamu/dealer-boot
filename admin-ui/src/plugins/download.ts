import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'
import { saveAs as fileSaveAs } from 'file-saver'
import { getMessage } from '@/locales'
import { getLocale, getToken } from '@/utils/auth'
import { getApiBaseUrl } from '@/utils/config'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'

const t = (key: string) => getMessage(key, getLocale())
const baseURL = getApiBaseUrl()
let downloadLoadingInstance: LoadingInstance | undefined

const download = {
  oss(ossId: string | number) {
    const url = `${baseURL}/system/oss/download/${ossId}`
    downloadLoadingInstance = ElLoading.service({ text: t('download.loading'), background: 'rgba(0, 0, 0, 0.7)' })
    axios({
      method: 'get',
      url,
      responseType: 'blob',
      headers: { Authorization: `Bearer ${getToken()}` }
    })
      .then((res) => {
        const isBlob = blobValidate(res.data)
        if (isBlob) {
          const blob = new Blob([res.data], { type: 'application/octet-stream' })
          this.saveAs(blob, decodeURIComponent(res.headers['download-filename']))
        } else {
          this.printErrMsg(res.data)
        }
        downloadLoadingInstance?.close()
      })
      .catch((error) => {
        console.error(error)
        ElMessage.error(t('download.error'))
        downloadLoadingInstance?.close()
      })
  },
  zip(url: string, name: string) {
    const downloadUrl = baseURL + url
    downloadLoadingInstance = ElLoading.service({ text: t('download.loading'), spinner: 'el-icon-loading', background: 'rgba(0, 0, 0, 0.7)' })
    axios({
      method: 'get',
      url: downloadUrl,
      responseType: 'blob',
      headers: {
        Authorization: `Bearer ${getToken()}`,
        datasource: localStorage.getItem('dataName')
      }
    })
      .then((res) => {
        const isBlob = blobValidate(res.data)
        if (isBlob) {
          const blob = new Blob([res.data], { type: 'application/zip' })
          this.saveAs(blob, name)
        } else {
          this.printErrMsg(res.data)
        }
        downloadLoadingInstance?.close()
      })
      .catch((error) => {
        console.error(error)
        ElMessage.error(t('download.error'))
        downloadLoadingInstance?.close()
      })
  },
  saveAs(data: Blob | string, name?: string, opts?: unknown) {
    fileSaveAs(data, name, opts as Parameters<typeof fileSaveAs>[2])
  },
  async printErrMsg(data: Blob) {
    const resText = await data.text()
    const rspObj = JSON.parse(resText) as { code?: string | number; msg?: string }
    const errMsg = errorCode[String(rspObj.code)] || rspObj.msg || errorCode.default
    ElMessage.error(errMsg)
  }
}

export default download
