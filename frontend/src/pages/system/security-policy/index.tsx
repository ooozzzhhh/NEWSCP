import { useEffect, useMemo, useState } from 'react'
import { Checkbox } from '@/components/ui/checkbox'
import { fetchPasswordPolicy, updatePasswordPolicy } from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { PasswordPolicyPayload, PasswordPolicySettings } from '@/types/system'

const emptyPolicy: PasswordPolicyPayload = {
  minLength: 8,
  maxLength: 32,
  requireDigit: 1,
  requireLower: 1,
  requireUpper: 0,
  requireSpecial: 0,
  expireEnabled: 0,
  expireDays: 90,
  alertBeforeDays: 7,
  forceChangeDefault: 1,
  forceChangeOnRuleUpdate: 0,
  lockEnabled: 1,
  lockThreshold: 5,
  lockDuration: 30,
  autoUnlock: 1,
  defaultPassword: 'Admin@2026',
}

export default function SecurityPolicyPage() {
  const canView = useHasPermission('sys:security:view')
  const canEdit = useHasPermission('sys:security:edit')
  const [form, setForm] = useState<PasswordPolicyPayload>(emptyPolicy)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!canView) {
      return
    }
    setLoading(true)
    setError('')
    fetchPasswordPolicy()
      .then((res) => {
        if (res.code !== 0) {
          setError(res.msg || '加载策略失败')
          return
        }
        setForm(toPayload(res.data))
      })
      .catch(() => setError('加载策略失败'))
      .finally(() => setLoading(false))
  }, [canView])

  const strength = useMemo(() => calcStrength(form), [form])

  async function onSave() {
    setSuccess('')
    setError('')
    if (form.minLength > form.maxLength) {
      setError('最小长度不能大于最大长度')
      return
    }
    const requiredKinds = [form.requireDigit, form.requireLower, form.requireUpper, form.requireSpecial].filter((v) => v === 1).length
    if (requiredKinds < 1) {
      setError('密码复杂度要求至少开启一项')
      return
    }
    if (!form.defaultPassword.trim()) {
      setError('默认密码不能为空')
      return
    }

    setSaving(true)
    try {
      const res = await updatePasswordPolicy(form)
      if (res.code !== 0) {
        setError(res.msg || '保存失败')
        return
      }
      setSuccess('保存成功')
    } catch {
      setError('保存失败')
    } finally {
      setSaving(false)
    }
  }

  if (!canView) {
    return (
      <section className="panel page-panel">
        <h2>安全策略</h2>
        <p>你没有查看权限</p>
      </section>
    )
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>密码与安全策略</h2>
        {canEdit && (
          <button className="primary-btn" disabled={saving || loading} onClick={() => void onSave()}>
            {saving ? '保存中...' : '保存配置'}
          </button>
        )}
      </header>

      {loading && <p>加载中...</p>}
      {error && <p className="error">{error}</p>}
      {success && <p className="success-msg">{success}</p>}

      <div className="policy-grid">
        <article className="policy-card">
          <h3>密码复杂度</h3>
          <div className="policy-form-grid">
            <NumberField
              label="最小长度"
              value={form.minLength}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, minLength: value }))}
            />
            <NumberField
              label="最大长度"
              value={form.maxLength}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, maxLength: value }))}
            />
            <ToggleField
              label="要求数字"
              value={form.requireDigit}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, requireDigit: value }))}
            />
            <ToggleField
              label="要求小写字母"
              value={form.requireLower}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, requireLower: value }))}
            />
            <ToggleField
              label="要求大写字母"
              value={form.requireUpper}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, requireUpper: value }))}
            />
            <ToggleField
              label="要求特殊符号"
              value={form.requireSpecial}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, requireSpecial: value }))}
            />
          </div>
          <p className="policy-strength">当前强度：{strength}</p>
        </article>

        <article className="policy-card">
          <h3>密码有效期</h3>
          <div className="policy-form-grid">
            <ToggleField
              label="启用有效期"
              value={form.expireEnabled}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, expireEnabled: value }))}
            />
            <NumberField
              label="有效天数"
              value={form.expireDays}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, expireDays: value }))}
            />
            <NumberField
              label="预警天数"
              value={form.alertBeforeDays}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, alertBeforeDays: value }))}
            />
          </div>
        </article>

        <article className="policy-card">
          <h3>强制改密</h3>
          <div className="policy-form-grid">
            <ToggleField
              label="默认密码强制修改"
              value={form.forceChangeDefault}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, forceChangeDefault: value }))}
            />
            <ToggleField
              label="策略更新后强制修改"
              value={form.forceChangeOnRuleUpdate}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, forceChangeOnRuleUpdate: value }))}
            />
          </div>
        </article>

        <article className="policy-card">
          <h3>账户锁定</h3>
          <div className="policy-form-grid">
            <ToggleField
              label="启用失败锁定"
              value={form.lockEnabled}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, lockEnabled: value }))}
            />
            <NumberField
              label="失败阈值（次）"
              value={form.lockThreshold}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, lockThreshold: value }))}
            />
            <NumberField
              label="锁定时长（分钟）"
              value={form.lockDuration}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, lockDuration: value }))}
            />
            <ToggleField
              label="自动解锁"
              value={form.autoUnlock}
              disabled={!canEdit}
              onChange={(value) => setForm((prev) => ({ ...prev, autoUnlock: value }))}
            />
          </div>
        </article>

        <article className="policy-card">
          <h3>默认密码</h3>
          <div className="policy-form-grid policy-form-grid-single">
            <label className="policy-field-name">重置时默认密码</label>
            <input
              className="policy-input"
              type="text"
              disabled={!canEdit}
              value={form.defaultPassword}
              onChange={(e) => setForm((prev) => ({ ...prev, defaultPassword: e.target.value }))}
            />
          </div>
        </article>
      </div>
    </section>
  )
}

function ToggleField({
  label,
  value,
  disabled,
  onChange,
}: {
  label: string
  value: 0 | 1
  disabled?: boolean
  onChange: (value: 0 | 1) => void
}) {
  return (
    <div className="policy-field-row">
      <span className="policy-field-name">{label}</span>
      <label className="app-check-label policy-toggle">
        <Checkbox
          checked={value === 1}
          disabled={disabled}
          onCheckedChange={(checked) => onChange(checked ? 1 : 0)}
        />
        <span>{value === 1 ? '开启' : '关闭'}</span>
      </label>
    </div>
  )
}

function NumberField({
  label,
  value,
  disabled,
  onChange,
}: {
  label: string
  value: number
  disabled?: boolean
  onChange: (value: number) => void
}) {
  return (
    <div className="policy-field-row">
      <label className="policy-field-name">{label}</label>
      <input
        className="policy-input"
        type="number"
        disabled={disabled}
        value={value}
        onChange={(e) => onChange(Number(e.target.value) || 0)}
      />
    </div>
  )
}

function toPayload(data: PasswordPolicySettings): PasswordPolicyPayload {
  return {
    minLength: data.minLength,
    maxLength: data.maxLength,
    requireDigit: data.requireDigit,
    requireLower: data.requireLower,
    requireUpper: data.requireUpper,
    requireSpecial: data.requireSpecial,
    expireEnabled: data.expireEnabled,
    expireDays: data.expireDays,
    alertBeforeDays: data.alertBeforeDays,
    forceChangeDefault: data.forceChangeDefault,
    forceChangeOnRuleUpdate: data.forceChangeOnRuleUpdate,
    lockEnabled: data.lockEnabled,
    lockThreshold: data.lockThreshold,
    lockDuration: data.lockDuration,
    autoUnlock: data.autoUnlock,
    defaultPassword: data.defaultPassword,
  }
}

function calcStrength(policy: PasswordPolicyPayload) {
  let score = 0
  if (policy.minLength > 12) score += 40
  else if (policy.minLength > 8) score += 20
  const typeCount = [policy.requireDigit, policy.requireLower, policy.requireUpper, policy.requireSpecial].filter(Boolean).length
  if (typeCount >= 4) score += 40
  else if (typeCount >= 3) score += 30
  else if (typeCount >= 2) score += 15
  score += typeCount * 5
  if (score >= 80) return '强'
  if (score >= 41) return '中'
  return '弱'
}
