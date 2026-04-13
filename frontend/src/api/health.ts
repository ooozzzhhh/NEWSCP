import { client } from './client'
import type { ApiResponse } from '@/types/api'

export interface HealthPayload {
  status: string
  service: string
  timestamp: string
}

export async function fetchHealth() {
  const response = await client.get<ApiResponse<HealthPayload>>('/health')
  return response.data
}
