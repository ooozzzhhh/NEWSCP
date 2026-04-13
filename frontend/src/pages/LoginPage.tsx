import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth-store'

export function LoginPage() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('123456')
  const [tenantId, setTenantId] = useState('demo-tenant')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

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
      navigate('/app/dashboard', { replace: true })
    } catch {
      setError('登录失败，请检查后端服务和账号信息')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrap">
      <form className="login-card" onSubmit={onSubmit}>
        <h1>NEWSCP 登录</h1>
        <p className="subtext">默认账号：admin / 123456</p>
        <label>
          租户 ID
          <input value={tenantId} onChange={(e) => setTenantId(e.target.value)} />
        </label>
        <label>
          用户名
          <input value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>
        <label>
          密码
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? '登录中...' : '登录'}
        </button>
      </form>
    </div>
  )
}
