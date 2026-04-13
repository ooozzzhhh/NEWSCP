import { useEffect, useState } from 'react'
import { fetchHealth, type HealthPayload } from '@/api/health'

export function DashboardPage() {
  const [health, setHealth] = useState<HealthPayload | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchHealth()
      .then((res) => setHealth(res.data))
      .catch(() => setError('后端健康检查失败'))
  }, [])

  return (
    <section className="panel">
      <h2>Hello NEWSCP</h2>
      <p>第 1 步基础框架已启动，下面是后端健康状态。</p>
      {error && <p className="error">{error}</p>}
      {health && (
        <ul>
          <li>status: {health.status}</li>
          <li>service: {health.service}</li>
          <li>timestamp: {health.timestamp}</li>
        </ul>
      )}
    </section>
  )
}
