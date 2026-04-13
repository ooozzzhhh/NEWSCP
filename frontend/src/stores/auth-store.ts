import { create } from 'zustand'
import { createJSONStorage, persist } from 'zustand/middleware'
import type { AuthSession } from '@/types/auth'

interface AuthState {
  session: AuthSession | null
  setSession: (session: AuthSession) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      session: null,
      setSession: (session) => set({ session }),
      clearSession: () => set({ session: null }),
    }),
    {
      name: 'newscp-auth',
      storage: createJSONStorage(() => localStorage),
    },
  ),
)

export const getAccessToken = () => useAuthStore.getState().session?.token ?? ''
