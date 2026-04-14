export interface MenuNode {
  id: number
  permCode: string
  permName: string
  routePath: string
  componentPath?: string
  icon?: string
  sortOrder: number
  children: MenuNode[]
}

export interface MenuResponse {
  menus: MenuNode[]
  permissions: string[]
}
