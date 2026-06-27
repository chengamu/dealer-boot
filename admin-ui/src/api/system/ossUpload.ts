import service from '@/utils/request'

export const OSS_UPLOAD_URL = '/system/oss/upload'

export interface OssUploadData {
  fileName: string
  url: string
  ossId: number | string
}

export interface OssUploadResponse {
  code: number
  msg?: string
  data: OssUploadData
}

export function uploadOssFile(file: Blob, filename = 'file') {
  const formData = new FormData()
  formData.append(filename, file)
  return service.post(OSS_UPLOAD_URL, formData) as unknown as Promise<OssUploadResponse>
}
