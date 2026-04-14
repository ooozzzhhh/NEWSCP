import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth-store'
import type { MenuNode } from '@/types/menu'

export function AppLayout() {
  const navigate = useNavigate()
  const session = useAuthStore((s) => s.session)
  const menus = useAuthStore((s) => s.menus)
  const clearSession = useAuthStore((s) => s.clearSession)

  const logout = () => {
    clearSession()
    navigate('/login')
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
            <span className="subtext">Tenant: {session?.tenantId}</span>
          </div>
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

function renderMenuNode(node: MenuNode, level = 0) {
  const hasChildren = node.children && node.children.length > 0
  if (hasChildren) {
    return (
      <div key={node.id} className="menu-group">
        <div className="menu-group-label" style={{ paddingLeft: `${10 + level * 12}px` }}>
          {node.permName}
        </div>
        {node.children.map((child) => renderMenuNode(child, level + 1))}
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
