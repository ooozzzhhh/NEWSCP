import React from 'react'
import { PlaceholderPage } from '@/pages/PlaceholderPage'

export const routeComponentMap: Record<string, React.LazyExoticComponent<React.ComponentType>> = {
  'system/users/index': React.lazy(() => import('@/pages/system/users')),
  'system/roles/index': React.lazy(() => import('@/pages/system/roles')),
  'system/depts/index': React.lazy(() => import('@/pages/system/depts')),
  'master/products/index': React.lazy(async () => ({ default: () => <PlaceholderPage title="产品管理（建设中）" /> })),
  'master/stock-point/index': React.lazy(async () => ({ default: () => <PlaceholderPage title="库存点管理（建设中）" /> })),
  'master/customer/index': React.lazy(async () => ({ default: () => <PlaceholderPage title="客户管理（建设中）" /> })),
}
