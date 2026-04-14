import { useEffect, useState } from 'react'
import { fetchHealth, type HealthPayload } from '@/api/health'
import { useAuthStore } from '@/stores/auth-store'

export function DashboardPage() {
  const [health, setHealth] = useState<HealthPayload | null>(null)
  const [error, setError] = useState('')
  const session = useAuthStore((s) => s.session)
  const permissions = useAuthStore((s) => s.permissions)

  useEffect(() => {
    fetchHealth()
      .then((res) => setHealth(res.data))
      .catch(() => setError('后端健康检查失败'))
  }, [])

  return (
    <section className="panel">
      <h2>欢迎，{session?.realName || session?.username}</h2>
      <p>阶段1基础能力已接入：数据库用户认证、RBAC、组织架构与动态菜单。</p>
      {error && <p className="error">{error}</p>}
      {health && (
        <ul>
          <li>status: {health.status}</li>
          <li>service: {health.service}</li>
          <li>timestamp: {health.timestamp}</li>
          <li>permissionCount: {permissions.length}</li>
        </ul>
      )}
    </section>
  )
}
