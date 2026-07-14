import { genFileId, type UploadInstance, type UploadRawFile } from 'element-plus'

export const DEFAULT_UPLOAD_MAX_SIZE_MB = 10

export type UploadValidationIssue = 'type' | 'size' | 'limit'

interface UploadValidationOptions {
  allowedExtensions: readonly string[]
  maxSizeMb?: number
  limit?: number
  currentCount?: number
}

const EXTENSION_BY_MIME: Record<string, string> = {
  'image/bmp': 'bmp',
  'image/gif': 'gif',
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'application/pdf': 'pdf'
}

export function getFileExtension(file: File) {
  const index = file.name.lastIndexOf('.')
  return index > -1 ? file.name.slice(index + 1).toLowerCase() : ''
}

export function formatUploadAccept(extensions: readonly string[]) {
  return extensions.map((extension) => `.${extension.replace(/^\./, '').toLowerCase()}`).join(',')
}

export function validateUploadFile(file: File, options: UploadValidationOptions): UploadValidationIssue | undefined {
  const allowed = options.allowedExtensions.map((extension) => extension.replace(/^\./, '').toLowerCase())
  if (allowed.length && !allowed.includes(getFileExtension(file))) return 'type'
  if (options.maxSizeMb && file.size > options.maxSizeMb * 1024 * 1024) return 'size'
  return undefined
}

export function selectUploadFiles(files: File[], options: UploadValidationOptions) {
  const accepted: File[] = []
  const available = options.limit === undefined
    ? Number.POSITIVE_INFINITY
    : Math.max(options.limit - (options.currentCount || 0), 0)

  for (const file of files) {
    if (accepted.length >= available) return { files: accepted, issue: 'limit' as const }
    const issue = validateUploadFile(file, options)
    if (issue) return { files: accepted, issue }
    accepted.push(file)
  }
  return { files: accepted, issue: undefined }
}

function normalizeTransferFiles(files: File[], prefix: string) {
  const now = Date.now()
  return files.map((file, index) => {
    if (getFileExtension(file)) return file
    const extension = EXTENSION_BY_MIME[file.type.toLowerCase()]
    if (!extension) return file
    return new File([file], `${prefix}-${now}-${index + 1}.${extension}`, {
      type: file.type,
      lastModified: file.lastModified
    })
  })
}

export function getClipboardFiles(event: ClipboardEvent) {
  const clipboard = event.clipboardData
  if (!clipboard) return []
  const listedFiles = Array.from(clipboard.files)
  const files = listedFiles.length
    ? listedFiles
    : Array.from(clipboard.items).flatMap((item) => {
        const file = item.kind === 'file' ? item.getAsFile() : null
        return file ? [file] : []
      })
  return normalizeTransferFiles(files, 'pasted-file')
}

export function getDroppedFiles(event: DragEvent) {
  return normalizeTransferFiles(Array.from(event.dataTransfer?.files || []), 'dropped-file')
}

export function enqueueUploadFiles(upload: UploadInstance | undefined, files: File[], autoSubmit = true) {
  if (!upload || !files.length) return
  files.forEach((file) => {
    const rawFile = file as UploadRawFile
    rawFile.uid = genFileId()
    upload.handleStart(rawFile)
  })
  if (autoSubmit) upload.submit()
}
