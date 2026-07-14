import { delOwnedOss } from '@/api/system/oss'

function extractOssIds(html: string) {
  const document = new DOMParser().parseFromString(html, 'text/html')
  return new Set(Array.from(document.querySelectorAll<HTMLImageElement>('img[data-oss-id]'))
    .map((image) => image.dataset.ossId)
    .filter((id): id is string => Boolean(id)))
}

export function usePendingOssImages() {
  const pending = new Set<string>()

  function track(ossId: string | number) {
    pending.add(String(ossId))
  }

  async function cleanupMissing(html: string) {
    const present = extractOssIds(html)
    await remove(Array.from(pending).filter((ossId) => !present.has(ossId)))
  }

  async function cleanup() {
    await remove(Array.from(pending))
  }

  function commit() {
    pending.clear()
  }

  async function remove(ossIds: string[]) {
    if (!ossIds.length) return
    ossIds.forEach((ossId) => pending.delete(ossId))
    try {
      await delOwnedOss(ossIds)
    } catch {
      ossIds.forEach((ossId) => pending.add(ossId))
    }
  }

  return { track, cleanupMissing, cleanup, commit }
}
