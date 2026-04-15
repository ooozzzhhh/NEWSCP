import { create } from 'zustand'
import { createJSONStorage, persist } from 'zustand/middleware'
import type { AuthSession } from '@/types/auth'
import type { MenuNode } from '@/types/menu'

interface AuthState {
  session: AuthSession | null
  activeTenantId: string
  menus: MenuNode[]
  permissions: string[]
  setSession: (session: AuthSession) => void
  setActiveTenantId: (tenantId: string) => void
  setMenus: (menus: MenuNode[]) => void
  setPermissions: (permissions: string[]) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      session: null,
      activeTenantId: '',
      menus: [],
      permissions: [],
      setSession: (session) => set({ session, activeTenantId: session.tenantId }),
      setActiveTenantId: (tenantId) => set({ activeTenantId: tenantId }),
      setMenus: (menus) => set({ menus }),
      setPermissions: (permissions) => set({ permissions }),
      clearSession: () => set({ session: null, activeTenantId: '', menus: [], permissions: [] }),
    }),
    {
      name: 'newscp-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ session: state.session, activeTenantId: state.activeTenantId }),
    },
  ),
)

export const getAccessToken = () => useAuthStore.getState().session?.token ?? ''
