import { Suspense, useEffect, useMemo, useState } from 'react'
import type { ReactElement } from 'react'
import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { fetchMenu } from '@/api/menu'
import { AppLayout } from '@/layout/AppLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { LoginPage } from '@/pages/LoginPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { PlaceholderPage } from '@/pages/PlaceholderPage'
import { useAuthStore } from '@/stores/auth-store'
import type { MenuNode } from '@/types/menu'
import { routeComponentMap } from './routeComponentMap'

function RequireAuth() {
  const session = useAuthStore((s) => s.session)
  const menus = useAuthStore((s) => s.menus)
  const setMenus = useAuthStore((s) => s.setMenus)
  const setPermissions = useAuthStore((s) => s.setPermissions)
  const [loading, setLoading] = useState(false)
  const [loadedOnce, setLoadedOnce] = useState(false)

  if (!session?.token) {
    return <Navigate to="/login" replace />
  }

  useEffect(() => {
    if (menus.length > 0 || loading || loadedOnce) {
      return
    }
    setLoading(true)
    fetchMenu()
      .then((res) => {
        if (res.code === 0) {
          setMenus(res.data.menus)
          setPermissions(res.data.permissions)
        }
      })
      .finally(() => {
        setLoading(false)
        setLoadedOnce(true)
      })
  }, [menus.length, loading, loadedOnce, setMenus, setPermissions])

  if (loading && menus.length === 0) {
    return <div className="page-loading">正在加载权限...</div>
  }

  return <Outlet />
}

function HomeRedirect() {
  const session = useAuthStore((s) => s.session)
  return <Navigate to={session?.token ? '/app/dashboard' : '/login'} replace />
}

export function AppRouter() {
  const menus = useAuthStore((s) => s.menus)
  const dynamicRoutes = useMemo(() => buildRoutes(menus), [menus])

  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/app" element={<AppLayout />}>
          <Route index element={<Navigate to="/app/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="403" element={<PlaceholderPage title="403 无权访问" />} />
          {dynamicRoutes}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  )
}

function buildRoutes(menus: MenuNode[]) {
  const routes: ReactElement[] = []

  function walk(nodes: MenuNode[]) {
    nodes.forEach((node) => {
      if (node.status === 'ENABLED' && node.isHidden !== 1 && node.componentPath && routeComponentMap[node.componentPath]) {
        const Component = routeComponentMap[node.componentPath]
        const path = normalizeRoutePath(node.routePath)
        if (path) {
          routes.push(
            <Route
              key={node.id}
              path={path}
              element={
                <Suspense fallback={<div className="page-loading">页面加载中...</div>}>
                  <Component />
                </Suspense>
              }
            />,
          )
        }
      }
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }

  walk(menus)
  return routes
}

function normalizeRoutePath(routePath?: string) {
  if (!routePath || !routePath.startsWith('/app/')) {
    return ''
  }
  return routePath.replace('/app/', '')
}
