export interface LoginPayload {
  token: string
  tokenType: string
  expiresIn: number
  userId: string
  username: string
  tenantId: string
  roles: string[]
}

export interface LoginRequest {
  username: string
  password: string
  tenantId?: string
}

export interface AuthSession {
  token: string
  tokenType: string
  expiresIn: number
  userId: string
  username: string
  tenantId: string
  roles: string[]
}
