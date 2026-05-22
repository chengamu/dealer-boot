import type { LocationQueryRaw, RouteLocationRaw } from 'vue-router'
import router from '@/router'
import useTagsViewStore, { type TagView } from '@/stores/tagsView'

type RefreshTarget = Pick<TagView, 'name' | 'path' | 'query'> & Partial<TagView>

function currentTagView() {
  return router.currentRoute.value as unknown as TagView
}

const tab = {
  refreshPage(obj?: RefreshTarget) {
    let target = obj
    const { path, query, matched } = router.currentRoute.value
    if (target === undefined) {
      matched.forEach((item) => {
        const componentName = item.components?.default && 'name' in item.components.default ? item.components.default.name : undefined
        if (componentName && !['Layout', 'ParentView'].includes(String(componentName))) {
          target = { name: componentName, path, query }
        }
      })
    }
    if (!target) return Promise.resolve()
    return useTagsViewStore().delCachedView(target as TagView).then(() =>
      router.replace({
        path: `/redirect${target?.path}`,
        query: target?.query as LocationQueryRaw | undefined
      })
    )
  },
  closeOpenPage(obj?: RouteLocationRaw) {
    useTagsViewStore().delView(currentTagView())
    if (obj !== undefined) {
      return router.push(obj)
    }
    return undefined
  },
  closePage(obj?: TagView) {
    if (obj === undefined) {
      return useTagsViewStore().delView(currentTagView()).then(({ visitedViews }) => {
        const latestView = visitedViews.slice(-1)[0]
        if (latestView) {
          return router.push(latestView.fullPath || latestView.path)
        }
        return router.push('/')
      })
    }
    return useTagsViewStore().delView(obj)
  },
  closeAllPage() {
    return useTagsViewStore().delAllViews()
  },
  closeLeftPage(obj?: TagView) {
    return useTagsViewStore().delLeftTags(obj || currentTagView())
  },
  closeRightPage(obj?: TagView) {
    return useTagsViewStore().delRightTags(obj || currentTagView())
  },
  closeOtherPage(obj?: TagView) {
    return useTagsViewStore().delOthersViews(obj || currentTagView())
  },
  openPage(url: RouteLocationRaw) {
    return router.push(url)
  },
  updatePage(obj: TagView) {
    return useTagsViewStore().updateVisitedView(obj)
  }
}

export default tab
