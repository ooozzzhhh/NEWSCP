import { useAuthStore } from '@/stores/auth-store'

export function useHasPermission(permCode: string): boolean {
  const permissions = useAuthStore((s) => s.permissions)
  return permissions.includes(permCode)
}

export function useHasAllPermissions(permCodes: string[]): boolean {
  const permissions = useAuthStore((s) => s.permissions)
  return permCodes.every((code) => permissions.includes(code))
}
