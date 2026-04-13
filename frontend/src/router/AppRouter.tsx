import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AppLayout } from '@/layout/AppLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { LoginPage } from '@/pages/LoginPage'
import { PlaceholderPage } from '@/pages/PlaceholderPage'
import { useAuthStore } from '@/stores/auth-store'

function RequireAuth() {
  const session = useAuthStore((s) => s.session)
  if (!session?.token) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}

function HomeRedirect() {
  const session = useAuthStore((s) => s.session)
  return <Navigate to={session?.token ? '/app/dashboard' : '/login'} replace />
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/app" element={<AppLayout />}>
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="customer" element={<PlaceholderPage title="客户管理" />} />
          <Route path="forecast" element={<PlaceholderPage title="需求预测" />} />
          <Route path="safety-stock" element={<PlaceholderPage title="安全库存" />} />
          <Route path="order" element={<PlaceholderPage title="客户订单" />} />
          <Route path="charts" element={<PlaceholderPage title="分析看板" />} />
        </Route>
      </Route>
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  )
}
