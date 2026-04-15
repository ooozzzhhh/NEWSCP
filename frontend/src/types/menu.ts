export interface MenuNode {
  id: number
  parentId: number
  permCode: string
  permName: string
  routePath: string
  componentPath?: string
  icon?: string
  sortOrder: number
  isHidden: number
  status: 'ENABLED' | 'DISABLED'
  children: MenuNode[]
}

export interface MenuResponse {
  menus: MenuNode[]
  permissions: string[]
}
