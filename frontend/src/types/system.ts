export interface RoleBrief {
  id: number
  roleName: string
}

export interface UserRow {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  userType: string
  status: 'ACTIVE' | 'DISABLED' | 'LOCKED'
  deptId?: number
  deptName?: string
  roles: RoleBrief[]
  createdAt: string
}

export interface UserDetail {
  id: number
  tenantId: string
  username: string
  realName: string
  email?: string
  phone?: string
  avatarUrl?: string
  userType: string
  status: string
  lockedUntil?: string
  pwdChangedAt?: string
  deptId?: number
  deptName?: string
  roleIds: number[]
  createdAt: string
}

export interface UserCreatePayload {
  username: string
  realName: string
  email?: string
  phone?: string
  userType: string
  deptId?: number
  roleIds: number[]
}

export interface UserUpdatePayload {
  realName: string
  email?: string
  phone?: string
  userType: string
  deptId?: number
  roleIds: number[]
}

export interface ResetPasswordResult {
  password: string
}

export interface RoleRow {
  id: number
  roleCode: string
  roleName: string
  remark?: string
  sortOrder: number
  userCount: number
}

export interface RoleDetail {
  id: number
  roleCode: string
  roleName: string
  remark?: string
  sortOrder: number
  permIds: number[]
}

export interface RoleOption {
  id: number
  roleCode: string
  roleName: string
}

export interface PermissionNode {
  id: number
  parentId: number
  permCode: string
  permName: string
  permType: 'MENU' | 'BUTTON'
  routePath?: string
  componentPath?: string
  icon?: string
  sortOrder: number
  isHidden: number
  status: 'ENABLED' | 'DISABLED'
  children: PermissionNode[]
}

export interface RoleCreatePayload {
  roleCode: string
  roleName: string
  remark?: string
  sortOrder: number
  permIds: number[]
}

export interface RoleUpdatePayload {
  roleName: string
  remark?: string
  sortOrder: number
  permIds: number[]
}

export interface DeptNode {
  id: number
  deptName: string
  parentId: number
  leaderId?: number
  leaderName?: string
  sortOrder: number
  remark?: string
  userCount: number
  children: DeptNode[]
}

export interface DeptCreatePayload {
  deptName: string
  parentId: number
  leaderId?: number
  sortOrder: number
  remark?: string
}

export interface DeptUpdatePayload {
  deptName: string
  parentId: number
  leaderId?: number
  sortOrder: number
  remark?: string
}

export interface DeptUser {
  userId: number
  username: string
  realName: string
  status: string
}

export interface TenantRow {
  id: number
  tenantId: string
  tenantName: string
  status: 'ENABLED' | 'DISABLED'
  expireAt?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  remark?: string
  userCount: number
  createdAt: string
}

export interface TenantUser {
  userId: number
  username: string
  realName: string
  userStatus: string
  isDefault: boolean
}

export interface TenantCreatePayload {
  tenantId: string
  tenantName: string
  status: 'ENABLED' | 'DISABLED'
  expireAt?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  remark?: string
  userIds: number[]
  defaultUserId?: number
}

export interface TenantUpdatePayload {
  tenantName: string
  status: 'ENABLED' | 'DISABLED'
  expireAt?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  remark?: string
  userIds: number[]
  defaultUserId?: number
}

export interface PermissionCreatePayload {
  parentId: number
  permType: 'MENU' | 'BUTTON'
  permCode: string
  permName: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortOrder: number
  isHidden: number
  status: 'ENABLED' | 'DISABLED'
}

export interface PermissionUpdatePayload {
  parentId: number
  permName: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortOrder: number
  isHidden: number
  status: 'ENABLED' | 'DISABLED'
}
