import axios from 'axios'
import { getAccessToken, useAuthStore } from '@/stores/auth-store'

export const client = axios.create({
  baseURL: 'http://localhost:8090',
  timeout: 5000,
})

client.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearSession()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else if (error.response?.status === 403) {
      if (!window.location.pathname.startsWith('/app/403')) {
        window.location.href = '/app/403'
      }
    }
    return Promise.reject(error)
  },
)
