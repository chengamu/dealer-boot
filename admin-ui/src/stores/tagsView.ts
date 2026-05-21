import { defineStore } from 'pinia'

export interface TagView {
  name?: string | symbol | null
  path: string
  fullPath?: string
  query?: Record<string, unknown>
  meta: {
    title?: string
    noCache?: boolean
    affix?: boolean
    link?: string
    [key: string]: unknown
  }
  [key: string]: unknown
}

interface TagsViewResult {
  visitedViews: TagView[]
  cachedViews: string[]
}

function getViewTitle(view: TagView) {
  return view.meta?.title || 'no-name'
}

function getCacheName(view: TagView) {
  return typeof view.name === 'string' ? view.name : ''
}

export const useTagsViewStore = defineStore('tags-view', {
  state: () => ({
    visitedViews: [] as TagView[],
    cachedViews: [] as string[],
    iframeViews: [] as TagView[]
  }),
  actions: {
    addView(view: TagView) {
      this.addVisitedView(view)
      this.addCachedView(view)
    },
    addIframeView(view: TagView) {
      if (this.iframeViews.some((item) => item.path === view.path)) return
      this.iframeViews.push(Object.assign({}, view, { title: getViewTitle(view) }))
    },
    addVisitedView(view: TagView) {
      if (this.visitedViews.some((item) => item.path === view.path)) return
      this.visitedViews.push(Object.assign({}, view, { title: getViewTitle(view) }))
    },
    addCachedView(view: TagView) {
      const name = getCacheName(view)
      if (!name || this.cachedViews.includes(name)) return
      if (!view.meta?.noCache) this.cachedViews.push(name)
    },
    delView(view: TagView) {
      return Promise.all([this.delVisitedView(view), this.delCachedView(view)]).then(
        (): TagsViewResult => ({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews]
        })
      )
    },
    delVisitedView(view: TagView) {
      return new Promise<TagView[]>((resolve) => {
        const index = this.visitedViews.findIndex((item) => item.path === view.path)
        if (index > -1) this.visitedViews.splice(index, 1)
        this.iframeViews = this.iframeViews.filter((item) => item.path !== view.path)
        resolve([...this.visitedViews])
      })
    },
    delIframeView(view: TagView) {
      return new Promise<TagView[]>((resolve) => {
        this.iframeViews = this.iframeViews.filter((item) => item.path !== view.path)
        resolve([...this.iframeViews])
      })
    },
    delCachedView(view: TagView) {
      return new Promise<string[]>((resolve) => {
        const name = getCacheName(view)
        const index = this.cachedViews.indexOf(name)
        if (index > -1) this.cachedViews.splice(index, 1)
        resolve([...this.cachedViews])
      })
    },
    delOthersViews(view: TagView) {
      return Promise.all([this.delOthersVisitedViews(view), this.delOthersCachedViews(view)]).then(
        (): TagsViewResult => ({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews]
        })
      )
    },
    delOthersVisitedViews(view: TagView) {
      return new Promise<TagView[]>((resolve) => {
        this.visitedViews = this.visitedViews.filter((item) => item.meta?.affix || item.path === view.path)
        this.iframeViews = this.iframeViews.filter((item) => item.path === view.path)
        resolve([...this.visitedViews])
      })
    },
    delOthersCachedViews(view: TagView) {
      return new Promise<string[]>((resolve) => {
        const name = getCacheName(view)
        const index = this.cachedViews.indexOf(name)
        this.cachedViews = index > -1 ? this.cachedViews.slice(index, index + 1) : []
        resolve([...this.cachedViews])
      })
    },
    delAllViews(view?: TagView) {
      return Promise.all([this.delAllVisitedViews(view), this.delAllCachedViews(view)]).then(
        (): TagsViewResult => ({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews]
        })
      )
    },
    delAllVisitedViews(_view?: TagView) {
      return new Promise<TagView[]>((resolve) => {
        this.visitedViews = this.visitedViews.filter((tag) => tag.meta?.affix)
        this.iframeViews = []
        resolve([...this.visitedViews])
      })
    },
    delAllCachedViews(_view?: TagView) {
      return new Promise<string[]>((resolve) => {
        this.cachedViews = []
        resolve([...this.cachedViews])
      })
    },
    updateVisitedView(view: TagView) {
      const target = this.visitedViews.find((item) => item.path === view.path)
      if (target) Object.assign(target, view)
    },
    delRightTags(view: TagView) {
      return new Promise<TagView[]>((resolve) => {
        const index = this.visitedViews.findIndex((item) => item.path === view.path)
        if (index === -1) {
          resolve([...this.visitedViews])
          return
        }
        this.visitedViews = this.visitedViews.filter((item, idx) => {
          if (idx <= index || item.meta?.affix) return true
          const cacheIndex = this.cachedViews.indexOf(getCacheName(item))
          if (cacheIndex > -1) this.cachedViews.splice(cacheIndex, 1)
          if (item.meta?.link) this.iframeViews = this.iframeViews.filter((iframe) => iframe.path !== item.path)
          return false
        })
        resolve([...this.visitedViews])
      })
    },
    delLeftTags(view: TagView) {
      return new Promise<TagView[]>((resolve) => {
        const index = this.visitedViews.findIndex((item) => item.path === view.path)
        if (index === -1) {
          resolve([...this.visitedViews])
          return
        }
        this.visitedViews = this.visitedViews.filter((item, idx) => {
          if (idx >= index || item.meta?.affix) return true
          const cacheIndex = this.cachedViews.indexOf(getCacheName(item))
          if (cacheIndex > -1) this.cachedViews.splice(cacheIndex, 1)
          if (item.meta?.link) this.iframeViews = this.iframeViews.filter((iframe) => iframe.path !== item.path)
          return false
        })
        resolve([...this.visitedViews])
      })
    }
  }
})

export default useTagsViewStore
