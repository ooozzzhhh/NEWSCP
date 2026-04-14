import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'
import type {
  DeptCreatePayload,
  DeptNode,
  DeptUpdatePayload,
  DeptUser,
  PermissionNode,
  ResetPasswordResult,
  RoleCreatePayload,
  RoleDetail,
  RoleOption,
  RoleRow,
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
