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

export interface PasswordPolicySettings {
  id: number
  tenantId: string
  minLength: number
  maxLength: number
  requireDigit: 0 | 1
  requireLower: 0 | 1
  requireUpper: 0 | 1
  requireSpecial: 0 | 1
  expireEnabled: 0 | 1
  expireDays: number
  alertBeforeDays: number
  forceChangeDefault: 0 | 1
  forceChangeOnRuleUpdate: 0 | 1
  lockEnabled: 0 | 1
  lockThreshold: number
  lockDuration: number
  autoUnlock: 0 | 1
  defaultPassword: string
}

export type PasswordPolicyPayload = Omit<PasswordPolicySettings, 'id' | 'tenantId'>

export interface DictTypeRow {
  id: number
  tenantId: string
  typeCode: string
  typeName: string
  source: 'BUILTIN' | 'CUSTOM'
  editable: 0 | 1
  status: 0 | 1
  sortOrder: number
  remark?: string
}

export interface DictTypePayload {
  typeCode: string
  typeName: string
  source: 'BUILTIN' | 'CUSTOM'
  editable: 0 | 1
  status: 0 | 1
  sortOrder: number
  remark?: string
}

export interface DictItemRow {
  id: number
  tenantId: string
  typeCode: string
  value: string
  label: string
  labelEn?: string
  color?: string
  extra?: string
  sortOrder: number
  status: 0 | 1
  isDefault: 0 | 1
  remark?: string
}

export interface DictItemPayload {
  typeCode: string
  value: string
  label: string
  labelEn?: string
  color?: string
  extra?: string
  sortOrder: number
  status: 0 | 1
  isDefault: 0 | 1
  remark?: string
}

export interface DictBatchSortPayload {
  items: Array<{
    id: number
    sortOrder: number
  }>
}

export interface DictOption {
  typeCode?: string
  value: string
  label: string
  color?: string
  isDefault: 0 | 1
  sortOrder?: number
}

export type DictDropdownMap = Record<string, DictOption[]>
