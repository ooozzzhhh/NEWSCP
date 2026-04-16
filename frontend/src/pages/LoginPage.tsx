import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '@/api/auth'
import { fetchMenu } from '@/api/menu'
import { client } from '@/api/client'
import { useAuthStore } from '@/stores/auth-store'
import type { ApiResponse } from '@/types/api'

export function LoginPage() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)
  const setMenus = useAuthStore((s) => s.setMenus)
  const setPermissions = useAuthStore((s) => s.setPermissions)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [tenantId, setTenantId] = useState('admin')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [forceChangeVisible, setForceChangeVisible] = useState(false)
  const [forceChangeMessage, setForceChangeMessage] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [forcing, setForcing] = useState(false)
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
        if (res.code === 600) {
          setForceChangeMessage(res.msg || '当前密码需要修改')
          setForceChangeVisible(true)
        } else {
          setError(res.msg || '登录失败')
        }
        return
      }
      setSession(res.data)
      if (res.data.pwdExpireWarning) window.alert(res.data.pwdExpireWarning)
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

  const onForceChange = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    if (!newPassword.trim()) {
      setError('请输入新密码')
      return
    }
    if (newPassword !== confirmPassword) {
      setError('新密码与确认密码不一致')
      return
    }
    setForcing(true)
    try {
      try {
        const response = await client.post<ApiResponse<void>>('/auth/force-change-password', {
          username,
          oldPassword: password,
          newPassword,
          confirmPassword,
          tenantId,
        })
        if (response.data.code !== 0) {
          setError(response.data.msg || '修改密码失败')
          return
        }
      } catch (requestError: unknown) {
        const message =
          typeof requestError === 'object' &&
          requestError &&
          'response' in requestError &&
          typeof (requestError as { response?: { data?: { msg?: string } } }).response?.data?.msg === 'string'
            ? (requestError as { response?: { data?: { msg?: string } } }).response?.data?.msg
            : '修改密码失败'
        setError(message || '修改密码失败')
        return
      }
      setForceChangeVisible(false)
      const retry = await login({ username, password: newPassword, tenantId })
      if (retry.code !== 0) {
        setError(retry.msg || '修改成功但自动登录失败，请手动登录')
        setPassword('')
        return
      }
      setPassword(newPassword)
      setSession(retry.data)
      const menuRes = await fetchMenu()
      if (menuRes.code === 0) {
        setMenus(menuRes.data.menus)
        setPermissions(menuRes.data.permissions)
      }
      navigate('/app/dashboard', { replace: true })
    } catch {
      setError('修改密码失败')
    } finally {
      setForcing(false)
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
              placeholder="例如 admin"
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
      {forceChangeVisible && (
        <div className="force-mask">
          <form className="force-card" onSubmit={onForceChange}>
            <h3>需要修改密码</h3>
            <p>{forceChangeMessage || '当前账号需先修改密码后再登录'}</p>
            <label className="login-field" htmlFor="newPassword">
              新密码
            </label>
            <input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoComplete="new-password"
            />
            <label className="login-field" htmlFor="confirmPassword">
              确认密码
            </label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
            />
            <div className="force-action-row">
              <button
                type="button"
                className="ghost-btn"
                onClick={() => {
                  setForceChangeVisible(false)
                  setNewPassword('')
                  setConfirmPassword('')
                }}
              >
                取消
              </button>
              <button className="login-submit" type="submit" disabled={forcing}>
                {forcing ? '提交中...' : '确认修改并登录'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
