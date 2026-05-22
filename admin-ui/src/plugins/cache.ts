type CacheValue = string
type JsonValue = unknown

function createStorageCache(storage: Storage) {
  return {
    set(key: string | null | undefined, value: CacheValue | null | undefined) {
      if (key != null && value != null) {
        storage.setItem(key, value)
      }
    },
    get(key: string | null | undefined) {
      if (key == null) return null
      return storage.getItem(key)
    },
    setJSON(key: string, jsonValue: JsonValue) {
      if (jsonValue != null) {
        this.set(key, JSON.stringify(jsonValue))
      }
    },
    getJSON<T = unknown>(key: string) {
      const value = this.get(key)
      if (value != null) {
        return JSON.parse(value) as T
      }
      return undefined
    },
    remove(key: string) {
      storage.removeItem(key)
    }
  }
}

const cache = {
  session: createStorageCache(sessionStorage),
  local: createStorageCache(localStorage)
}

export default cache
