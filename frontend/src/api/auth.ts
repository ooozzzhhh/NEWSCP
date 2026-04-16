import type { ApiError, ApiResponse } from '@/types/api'
import type { LoginPayload, LoginRequest } from '@/types/auth'
import { client } from './client'
import { isAxiosError } from 'axios'

export async function login(payload: LoginRequest) {
  try {
    const response = await client.post<ApiResponse<LoginPayload>>('/auth/login', payload)
    return response.data
  } catch (error) {
    if (isAxiosError<ApiError>(error) && error.response?.data) {
      return {
        code: error.response.data.code,
        data: null as unknown as LoginPayload,
        msg: error.response.data.msg,
      }
    }
    throw error
  }
}
