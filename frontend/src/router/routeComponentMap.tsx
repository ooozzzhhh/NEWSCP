import React from 'react'
import { PlaceholderPage } from '@/pages/PlaceholderPage'

export const routeComponentMap: Record<string, React.LazyExoticComponent<React.ComponentType>> = {
  'system/users/index': React.lazy(() => import('@/pages/system/users')),
  'system/roles/index': React.lazy(() => import('@/pages/system/roles')),
  'system/depts/index': React.lazy(() => import('@/pages/system/depts')),
  'system/tenants/index': React.lazy(() => import('@/pages/system/tenants')),
  'system/permissions/index': React.lazy(() => import('@/pages/system/permissions')),
  'system/security-policy/index': React.lazy(() => import('@/pages/system/security-policy')),
  'system/dicts/index': React.lazy(() => import('@/pages/system/dicts')),
  'master/products/index': React.lazy(async () => ({ default: () => <PlaceholderPage title="产品管理（建设中）" /> })),
  'master/stock-point/index': React.lazy(async () => ({ default: () => <PlaceholderPage title="库存点管理（建设中）" /> })),
  'master/customer/index': React.lazy(async () => ({ default: () => <PlaceholderPage title="客户管理（建设中）" /> })),
}
