import type { ApiResponse } from '@/types/api'
import type { LoginPayload, LoginRequest } from '@/types/auth'
import { client } from './client'

export async function login(payload: LoginRequest) {
  const response = await client.post<ApiResponse<LoginPayload>>('/auth/login', payload)
  return response.data
}
