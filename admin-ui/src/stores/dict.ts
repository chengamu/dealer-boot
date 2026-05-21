import { defineStore } from 'pinia'

export interface DictCacheEntry<T = unknown> {
  key: string
  value: T
}

export const useDictStore = defineStore('dict', {
  state: () => ({
    dict: [] as DictCacheEntry[]
  }),
  actions: {
    getDict<T = unknown>(key?: string | null) {
      if (key == null || key === '') return null
      return (this.dict.find((item) => item.key === key)?.value as T | undefined) ?? null
    },
    setDict<T = unknown>(key: string | null | undefined, value: T) {
      if (key == null || key === '') return
      const target = this.dict.find((item) => item.key === key)
      if (target) {
        target.value = value
        return
      }
      this.dict.push({ key, value })
    },
    removeDict(key?: string | null) {
      if (key == null || key === '') return false
      const index = this.dict.findIndex((item) => item.key === key)
      if (index === -1) return false
      this.dict.splice(index, 1)
      return true
    },
    cleanDict() {
      this.dict = []
    },
    initDict() {}
  }
})

export default useDictStore
