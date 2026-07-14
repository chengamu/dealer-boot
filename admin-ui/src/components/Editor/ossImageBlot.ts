type ImageValue = string | { url: string; ossId?: string | number }

type ImageBlotConstructor = {
  new (...args: never[]): object
  create(value: ImageValue): HTMLElement
  value(node: HTMLElement): ImageValue
}

type QuillRegistry = {
  import(path: string): unknown
  register(target: unknown, overwrite?: boolean): void
}

export function registerOssImageBlot(quill: QuillRegistry) {
  const BaseImage = quill.import('formats/image') as ImageBlotConstructor

  class OssImageBlot extends BaseImage {
    static create(value: ImageValue) {
      const url = typeof value === 'string' ? value : value.url
      const node = super.create(url)
      if (typeof value !== 'string' && value.ossId !== undefined) {
        node.dataset.ossId = String(value.ossId)
      }
      return node
    }

    static value(node: HTMLElement): ImageValue {
      const value = super.value(node)
      const url = typeof value === 'string' ? value : value.url
      return node.dataset.ossId ? { url, ossId: node.dataset.ossId } : url
    }
  }

  quill.register(OssImageBlot, true)
}
