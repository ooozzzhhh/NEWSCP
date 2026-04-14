import { client } from '@/api/client'
import type { ApiResponse } from '@/types/api'
import type { MenuResponse } from '@/types/menu'

export async function fetchMenu() {
  const response = await client.get<ApiResponse<MenuResponse>>('/api/sys/menu')
  return response.data
}
