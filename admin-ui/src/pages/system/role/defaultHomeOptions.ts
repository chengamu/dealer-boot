import type { Menu } from '@/api/system/menu'

export interface DefaultHomeOption {
  value: number
  label: string
}

export function flattenMenus(menus: Menu[]) {
  const flat: Menu[] = []
  const visit = (nodes: Menu[]) => {
    nodes.forEach((node) => {
      flat.push(node)
      if (node.children?.length) visit(node.children)
    })
  }
  visit(menus)
  return flat
}

export function buildDefaultHomeOptions(menus: Menu[], selectedMenuIds: Array<number | string>) {
  const selectedIds = new Set(selectedMenuIds.map((item) => String(item)))
  const menuMap = new Map(menus.map((menu) => [String(menu.menuId), menu]))
  return menus
    .filter((menu) =>
      selectedIds.has(String(menu.menuId))
      && menu.menuType === 'C'
      && menu.status === '1'
      && menu.visible === '1'
      && menu.isFrame === '1'
      && !!menu.component
    )
    .map((menu) => ({
      value: Number(menu.menuId),
      label: buildMenuLabel(menu, menuMap)
    }))
}

function buildMenuLabel(menu: Menu, menuMap: Map<string, Menu>) {
  const names: string[] = []
  let current: Menu | undefined = menu
  const visited = new Set<string>()
  while (current?.menuId != null && !visited.has(String(current.menuId))) {
    visited.add(String(current.menuId))
    if (current.menuName) names.unshift(current.menuName)
    current = current.parentId != null ? menuMap.get(String(current.parentId)) : undefined
  }
  return names.join(' / ') || menu.menuName || String(menu.menuId)
}
