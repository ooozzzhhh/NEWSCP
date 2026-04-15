import type { ReactNode } from 'react'
import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth-store'
import type { MenuNode } from '@/types/menu'

export function AppLayout() {
  const navigate = useNavigate()
  const session = useAuthStore((s) => s.session)
  const activeTenantId = useAuthStore((s) => s.activeTenantId)
  const setActiveTenantId = useAuthStore((s) => s.setActiveTenantId)
  const menus = useAuthStore((s) => s.menus)
  const clearSession = useAuthStore((s) => s.clearSession)
  const isSuperAdmin = !!session?.roles?.includes('SUPER_ADMIN_ROLE')
  const [collapsedMenuIds, setCollapsedMenuIds] = useState<Set<number>>(new Set())

  const logout = () => {
    clearSession()
    navigate('/login')
  }

  const toggleMenuGroup = (id: number) => {
    setCollapsedMenuIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  const renderMenuNode = (node: MenuNode, level = 0): ReactNode => {
    if (node.status !== 'ENABLED' || node.isHidden === 1) {
      return null
    }
    const children = (node.children || []).filter((child) => child.status === 'ENABLED' && child.isHidden !== 1)
    const hasChildren = children.length > 0
    if (hasChildren) {
      const collapsed = collapsedMenuIds.has(node.id)
      return (
        <div key={node.id} className="menu-group">
          <button
            type="button"
            className={`menu-group-toggle ${collapsed ? 'collapsed' : ''}`}
            style={{ paddingLeft: `${10 + level * 12}px` }}
            onClick={() => toggleMenuGroup(node.id)}
          >
            <span className="menu-caret">{collapsed ? '▸' : '▾'}</span>
            <span>{node.permName}</span>
          </button>
          {!collapsed && children.map((child) => renderMenuNode(child, level + 1))}
        </div>
      )
    }

    if (!node.routePath) {
      return null
    }

    return (
      <NavLink
        key={node.id}
        to={node.routePath}
        className={({ isActive }) => (isActive ? 'menu-item active' : 'menu-item')}
        style={{ paddingLeft: `${10 + level * 12}px` }}
      >
        {node.permName}
      </NavLink>
    )
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <h1 className="brand">NEWSCP</h1>
        <nav>
          <NavLink to="/app/dashboard" className={({ isActive }) => (isActive ? 'menu-item active' : 'menu-item')}>
            仪表盘
          </NavLink>
          {menus.map((menu) => renderMenuNode(menu))}
        </nav>
      </aside>
      <div className="content-area">
        <header className="topbar">
          <div>
            <strong>{session?.realName || session?.username}</strong>
            <span className="subtext">Tenant: {activeTenantId || session?.tenantId}</span>
          </div>
          {isSuperAdmin && (
            <div className="tenant-switch">
              <label htmlFor="active-tenant">管理租户</label>
              <input
                id="active-tenant"
                value={activeTenantId}
                onChange={(e) => setActiveTenantId(e.target.value.trim())}
                placeholder="输入租户ID"
              />
              <button className="ghost-btn" onClick={() => setActiveTenantId(session?.tenantId || 'admin')}>
                重置
              </button>
            </div>
          )}
          <button className="ghost-btn" onClick={logout}>
            退出登录
          </button>
        </header>
        <main className="page">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
