import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'
import type {
  DictBatchSortPayload,
  DictDropdownMap,
  DictItemPayload,
  DictItemRow,
  DictTypePayload,
  DictTypeRow,
  DeptCreatePayload,
  DeptNode,
  DeptUpdatePayload,
  PasswordPolicyPayload,
  PasswordPolicySettings,
  DeptUser,
  PermissionCreatePayload,
  PermissionNode,
  PermissionUpdatePayload,
  ResetPasswordResult,
  RoleCreatePayload,
  RoleDetail,
  RoleOption,
  RoleRow,
  TenantCreatePayload,
  TenantRow,
  TenantUpdatePayload,
  TenantUser,
  RoleUpdatePayload,
  UserCreatePayload,
  UserDetail,
  UserRow,
  UserUpdatePayload,
} from '@/types/system'

export async function fetchUsers(params: {
  page: number
  size: number
  username?: string
  realName?: string
  status?: string
}) {
  const response = await client.get<ApiResponse<PageResult<UserRow>>>('/api/sys/users', { params })
  return response.data
}

export async function createUser(payload: UserCreatePayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/users', payload)
  return response.data
}

export async function updateUser(id: number, payload: UserUpdatePayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/users/${id}`, payload)
  return response.data
}

export async function fetchUserDetail(id: number) {
  const response = await client.get<ApiResponse<UserDetail>>(`/api/sys/users/${id}`)
  return response.data
}

export async function deleteUser(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/users/${id}`)
  return response.data
}

export async function enableUser(id: number) {
  const response = await client.post<ApiResponse<void>>(`/api/sys/users/${id}/enable`)
  return response.data
}

export async function disableUser(id: number) {
  const response = await client.post<ApiResponse<void>>(`/api/sys/users/${id}/disable`)
  return response.data
}

export async function resetUserPassword(id: number) {
  const response = await client.post<ApiResponse<ResetPasswordResult>>(`/api/sys/users/${id}/reset-password`)
  return response.data
}

export async function fetchRoles(params: { page: number; size: number; roleName?: string }) {
  const response = await client.get<ApiResponse<PageResult<RoleRow>>>('/api/sys/roles', { params })
  return response.data
}

export async function fetchRoleAll() {
  const response = await client.get<ApiResponse<RoleOption[]>>('/api/sys/roles/all')
  return response.data
}

export async function fetchRoleDetail(id: number) {
  const response = await client.get<ApiResponse<RoleDetail>>(`/api/sys/roles/${id}`)
  return response.data
}

export async function createRole(payload: RoleCreatePayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/roles', payload)
  return response.data
}

export async function updateRole(id: number, payload: RoleUpdatePayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/roles/${id}`, payload)
  return response.data
}

export async function deleteRole(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/roles/${id}`)
  return response.data
}

export async function assignRolePermissions(id: number, permIds: number[]) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/roles/${id}/permissions`, { permIds })
  return response.data
}

export async function fetchPermissionTree() {
  const response = await client.get<ApiResponse<PermissionNode[]>>('/api/sys/permissions/tree')
  return response.data
}

export async function fetchDeptTree() {
  const response = await client.get<ApiResponse<DeptNode[]>>('/api/sys/depts/tree')
  return response.data
}

export async function fetchDeptList() {
  const response = await client.get<ApiResponse<DeptNode[]>>('/api/sys/depts')
  return response.data
}

export async function createDept(payload: DeptCreatePayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/depts', payload)
  return response.data
}

export async function updateDept(id: number, payload: DeptUpdatePayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/depts/${id}`, payload)
  return response.data
}

export async function deleteDept(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/depts/${id}`)
  return response.data
}

export async function fetchDeptUsers(id: number) {
  const response = await client.get<ApiResponse<DeptUser[]>>(`/api/sys/depts/${id}/users`)
  return response.data
}

export async function fetchTenants(params: {
  page: number
  size: number
  keyword?: string
  status?: string
}) {
  const response = await client.get<ApiResponse<PageResult<TenantRow>>>('/api/sys/tenants', { params })
  return response.data
}

export async function fetchTenantDetail(id: number) {
  const response = await client.get<ApiResponse<TenantRow>>(`/api/sys/tenants/${id}`)
  return response.data
}

export async function createTenant(payload: TenantCreatePayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/tenants', payload)
  return response.data
}

export async function updateTenant(id: number, payload: TenantUpdatePayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/tenants/${id}`, payload)
  return response.data
}

export async function deleteTenant(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/tenants/${id}`)
  return response.data
}

export async function fetchTenantUsers(id: number) {
  const response = await client.get<ApiResponse<TenantUser[]>>(`/api/sys/tenants/${id}/users`)
  return response.data
}

export async function assignTenantUsers(
  id: number,
  payload: {
    userIds: number[]
    defaultUserId?: number
  },
) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/tenants/${id}/users`, payload)
  return response.data
}

export async function fetchPermissionOperateTree() {
  const response = await client.get<ApiResponse<PermissionNode[]>>('/api/sys/permissions/operate-tree')
  return response.data
}

export async function createPermission(payload: PermissionCreatePayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/permissions', payload)
  return response.data
}

export async function updatePermission(id: number, payload: PermissionUpdatePayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/permissions/${id}`, payload)
  return response.data
}

export async function deletePermission(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/permissions/${id}`)
  return response.data
}

export async function fetchPasswordPolicy() {
  const response = await client.get<ApiResponse<PasswordPolicySettings>>('/api/sys/password-policy')
  return response.data
}

export async function updatePasswordPolicy(payload: PasswordPolicyPayload) {
  const response = await client.put<ApiResponse<void>>('/api/sys/password-policy', payload)
  return response.data
}

export async function fetchDictTypes(params: { page: number; size: number; keyword?: string }) {
  const response = await client.get<ApiResponse<PageResult<DictTypeRow>>>('/api/sys/dict-types', { params })
  return response.data
}

export async function createDictType(payload: DictTypePayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/dict-types', payload)
  return response.data
}

export async function updateDictType(id: number, payload: DictTypePayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/dict-types/${id}`, payload)
  return response.data
}

export async function deleteDictType(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/dict-types/${id}`)
  return response.data
}

export async function fetchDictItems(typeCode: string) {
  const response = await client.get<ApiResponse<DictItemRow[]>>('/api/sys/dict-items', { params: { typeCode } })
  return response.data
}

export async function createDictItem(payload: DictItemPayload) {
  const response = await client.post<ApiResponse<number>>('/api/sys/dict-items', payload)
  return response.data
}

export async function updateDictItem(id: number, payload: DictItemPayload) {
  const response = await client.put<ApiResponse<void>>(`/api/sys/dict-items/${id}`, payload)
  return response.data
}

export async function deleteDictItem(id: number) {
  const response = await client.delete<ApiResponse<void>>(`/api/sys/dict-items/${id}`)
  return response.data
}

export async function batchSortDictItems(payload: DictBatchSortPayload) {
  const response = await client.put<ApiResponse<void>>('/api/sys/dict-items/batch-sort', payload)
  return response.data
}

export async function fetchDictDropdown(typeCodes: string[]) {
  const response = await client.get<ApiResponse<DictDropdownMap>>('/api/sys/dict/dropdown', {
    params: {
      typeCodes: typeCodes.join(','),
    },
  })
  return response.data
}
