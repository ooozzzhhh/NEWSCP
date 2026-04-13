import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth-store'

const menus = [
  { path: '/app/dashboard', label: '仪表盘' },
  { path: '/app/customer', label: '客户' },
  { path: '/app/forecast', label: '预测' },
  { path: '/app/safety-stock', label: '安全库存' },
  { path: '/app/order', label: '客户订单' },
  { path: '/app/charts', label: '分析看板' },
]

export function AppLayout() {
  const navigate = useNavigate()
  const session = useAuthStore((s) => s.session)
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
          {menus.map((menu) => (
            <NavLink
              key={menu.path}
              to={menu.path}
              className={({ isActive }) => (isActive ? 'menu-item active' : 'menu-item')}
            >
              {menu.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="content-area">
        <header className="topbar">
          <div>
            <strong>{session?.username}</strong>
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
