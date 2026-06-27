<template>
  <div :class="{ 'show': show }" class="header-search">
    <button
      type="button"
      class="header-search-button"
      :aria-label="t('shell.searchMenu')"
      :title="t('shell.searchMenu')"
      @click.stop="click"
    >
      <svg-icon class-name="search-icon" icon-class="search" />
    </button>
    <el-select
      ref="headerSearchSelectRef"
      v-model="search"
      :remote-method="querySearch"
      filterable
      default-first-option
      remote
      :placeholder="t('common.search')"
      class="header-search-select"
      @change="change"
    >
      <el-option v-for="option in options" :key="option.item.path" :value="option.item" :label="option.item.title.join(' > ')" />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import Fuse from 'fuse.js'
import { getNormalPath } from '@/utils/ruoyi'
import { isHttp } from '@/utils/validate'
import usePermissionStore from '@/stores/permission'
import { getMessage } from '@/locales'
import useLocaleStore from '@/stores/locale'

type SearchRoute = {
  path: string
  query?: string
  title: string[]
}

type SearchOption = {
  item: SearchRoute
}

type RouteLike = {
  path: string
  hidden?: boolean
  redirect?: string
  query?: string
  meta?: {
    title?: string
  }
  children?: RouteLike[]
}

type HeaderSearchSelect = {
  focus: () => void
  blur: () => void
}

const search = ref('');
const options = ref<SearchOption[]>([]);
const searchPool = ref<SearchRoute[]>([]);
const show = ref(false);
const fuse = ref<Fuse<SearchRoute>>();
const headerSearchSelectRef = ref<HeaderSearchSelect | null>(null);
const router = useRouter();
const routes = computed(() => usePermissionStore().routes);
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function click() {
  show.value = !show.value
  if (show.value) {
    headerSearchSelectRef.value && headerSearchSelectRef.value.focus()
  }
};
function close() {
  headerSearchSelectRef.value && headerSearchSelectRef.value.blur()
  options.value = []
  show.value = false
}
function change(val: SearchRoute) {
  const path = val.path;
  const query = val.query;
  if (isHttp(path)) {
    // http(s):// 路径新窗口打开
    const pindex = path.indexOf("http");
    window.open(path.substr(pindex, path.length), "_blank");
  } else {
    if (query) {
      router.push({ path: path, query: JSON.parse(query) });
    } else {
      router.push(path)
    }
  }

  search.value = ''
  options.value = []
  nextTick(() => {
    show.value = false
  })
}
function initFuse(list: SearchRoute[]) {
  fuse.value = new Fuse(list, {
    shouldSort: true,
    threshold: 0.4,
    location: 0,
    distance: 100,
    minMatchCharLength: 1,
    keys: [{
      name: 'title',
      weight: 0.7
    }, {
      name: 'path',
      weight: 0.3
    }]
  })
}
// Filter out the routes that can be displayed in the sidebar
// And generate the internationalized title
function generateRoutes(routes: RouteLike[], basePath = '', prefixTitle: string[] = []) {
  let res: SearchRoute[] = []

  for (const r of routes) {
    // skip hidden router
    if (r.hidden) { continue }
    const p = r.path.length > 0 && r.path[0] === '/' ? r.path : '/' + r.path;
    const data: SearchRoute = {
      path: !isHttp(r.path) ? getNormalPath(basePath + p) : r.path,
      title: [...prefixTitle]
    }

    if (r.meta && r.meta.title) {
      data.title = [...data.title, r.meta.title]

      if (r.redirect !== 'noRedirect') {
        // only push the routes with title
        // special case: need to exclude parent router without redirect
        res.push(data)
      }
    }

    if (r.query) {
      data.query = r.query
    }

    // recursive child routes
    if (r.children) {
      const tempRoutes = generateRoutes(r.children, data.path, data.title)
      if (tempRoutes.length >= 1) {
        res = [...res, ...tempRoutes]
      }
    }
  }
  return res
}
function querySearch(query: string) {
  if (query !== '') {
    options.value = fuse.value?.search(query) || []
  } else {
    options.value = []
  }
}

onMounted(() => {
  searchPool.value = generateRoutes(routes.value as RouteLike[]);
})

watchEffect(() => {
  searchPool.value = generateRoutes(routes.value as RouteLike[])
})

watch(show, (value) => {
  if (value) {
    document.body.addEventListener('click', close)
  } else {
    document.body.removeEventListener('click', close)
  }
})

watch(searchPool, (list) => {
  initFuse(list)
})
</script>

<style lang='scss' scoped>
.header-search {
  font-size: 0 !important;

  .search-icon {
    font-size: 18px;
    vertical-align: middle;
  }

  .header-search-button {
    width: 26px;
    height: 50px;
    padding: 0;
    border: 0;
    background: transparent;
    color: inherit;
    cursor: pointer;
    vertical-align: middle;
  }

  .header-search-select {
    font-size: 18px;
    transition: width 0.2s;
    width: 0;
    overflow: hidden;
    background: transparent;
    border-radius: 0;
    display: inline-block;
    vertical-align: middle;

    :deep(.el-input__inner) {
      border-radius: 0;
      border: 0;
      padding-left: 0;
      padding-right: 0;
      box-shadow: none !important;
      border-bottom: 1px solid #d9d9d9;
      vertical-align: middle;
    }
  }

  &.show {
    .header-search-select {
      width: 280px;
      margin-left: 10px;
    }
  }
}
</style>
