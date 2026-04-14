import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '@/api/auth'
import { fetchMenu } from '@/api/menu'
import { useAuthStore } from '@/stores/auth-store'

export function LoginPage() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)
  const setMenus = useAuthStore((s) => s.setMenus)
  const setPermissions = useAuthStore((s) => s.setPermissions)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [tenantId, setTenantId] = useState('demo-tenant')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [enterReady, setEnterReady] = useState(false)

  useEffect(() => {
    const timer = window.setTimeout(() => setEnterReady(true), 60)
    return () => window.clearTimeout(timer)
  }, [])

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await login({ username, password, tenantId })
      if (res.code !== 0) {
        setError(res.msg || '登录失败')
        return
      }
      setSession(res.data)
      const menuRes = await fetchMenu()
      if (menuRes.code === 0) {
        setMenus(menuRes.data.menus)
        setPermissions(menuRes.data.permissions)
      }
      navigate('/app/dashboard', { replace: true })
    } catch {
      setError('登录失败，请检查后端服务和账号信息')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`login-shell ${enterReady ? 'login-shell-enter' : ''}`}>
      <div className="login-aurora login-aurora-cyan" />
      <div className="login-aurora login-aurora-blue" />
      <div className="login-bg login-bg-cyan" />
      <div className="login-bg login-bg-amber" />
      <div className="login-grid-overlay" />
      <main className="login-layout">
        <section className="login-hero">
          <div className="login-brand-block">
            <p className="login-brand-mark">UHA</p>
            <p className="login-brand-name">supply chain planning</p>
          </div>
          <h1>Plan Faster. Deliver Better.</h1>
          <p className="login-hero-copy">
            统一连接需求预测、库存策略与订单协同，让供应链计划从“被动响应”升级为“主动掌控”。
          </p>
          <div className="login-kpi-list">
            <article>
              <span>Forecast</span>
              <strong>AI-assisted</strong>
            </article>
            <article>
              <span>Visibility</span>
              <strong>End-to-End</strong>
            </article>
            <article>
              <span>Response</span>
              <strong>Real-time</strong>
            </article>
          </div>
        </section>
        <section className="login-panel">
          <form className="login-card" onSubmit={onSubmit}>
            <div className="login-card-head">
              <h2>欢迎回来</h2>
              <p>输入你的租户、用户名和密码登录系统</p>
            </div>
            <label className="login-field" htmlFor="tenantId">
              租户 ID
            </label>
            <input
              id="tenantId"
              value={tenantId}
              onChange={(e) => setTenantId(e.target.value)}
              autoComplete="organization"
              placeholder="例如 demo-tenant"
            />
            <label className="login-field" htmlFor="username">
              用户名
            </label>
            <input
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
            <label className="login-field" htmlFor="password">
              密码
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            {error && <p className="error">{error}</p>}
            <button className="login-submit" type="submit" disabled={loading}>
              {loading ? '登录中...' : '进入 UHA 工作台'}
            </button>
          </form>
          <p className="login-footnote">Secure by design · Multi-tenant ready</p>
        </section>
      </main>
    </div>
  )
}
