import { useEffect, useMemo, useState } from 'react'
import { PageSizeSelect } from '@/components/PageSizeSelect'
import { Checkbox } from '@/components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { createTenant, deleteTenant, fetchTenantDetail, fetchTenants, fetchTenantUsers, fetchUsers, updateTenant } from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { TenantRow, TenantUser, UserRow } from '@/types/system'

type TenantForm = {
  id?: number
  tenantId: string
  tenantName: string
  status: 'ENABLED' | 'DISABLED'
  expireAt: string
  contactName: string
  contactPhone: string
  contactEmail: string
  remark: string
  userIds: string[]
  defaultUserId: string
}

const emptyForm: TenantForm = {
  tenantId: '',
  tenantName: '',
  status: 'ENABLED',
  expireAt: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  remark: '',
  userIds: [],
  defaultUserId: '',
}

export default function TenantManagementPage() {
  const canCreate = useHasPermission('sys:tenant:create')
  const canRead = useHasPermission('sys:tenant:read')
  const canUpdate = useHasPermission('sys:tenant:update')
  const canDelete = useHasPermission('sys:tenant:delete')
  const canAssign = useHasPermission('sys:tenant:assign-user')

  const [rows, setRows] = useState<TenantRow[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [queryKeyword, setQueryKeyword] = useState('')
  const [queryStatus, setQueryStatus] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState<TenantForm>(emptyForm)
  const [allUsers, setAllUsers] = useState<UserRow[]>([])

  const isEdit = !!form.id

  useEffect(() => {
    void loadTenants(page)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size])

  useEffect(() => {
    if (!canRead && !canAssign && !canCreate && !canUpdate) {
      return
    }
    fetchUsers({ page: 1, size: 500 })
      .then((res) => {
        if (res.code === 0) {
          setAllUsers(res.data.records)
        }
      })
      .catch(() => {
        setError('用户列表加载失败')
      })
  }, [canAssign, canCreate, canRead, canUpdate])

  const selectedUsers = useMemo(() => {
    const selected = new Set(form.userIds)
    return allUsers.filter((u) => selected.has(String(u.id)))
  }, [allUsers, form.userIds])

  async function loadTenants(targetPage = page) {
    setLoading(true)
    setError('')
    try {
      const res = await fetchTenants({
        page: targetPage,
        size,
        keyword: queryKeyword || undefined,
        status: queryStatus || undefined,
      })
      if (res.code !== 0) {
        setError(res.msg || '租户列表加载失败')
        return
      }
      setRows(res.data.records)
      setTotal(res.data.total)
      setPage(res.data.page)
      setSize(res.data.size)
    } catch {
      setError('租户列表加载失败')
    } finally {
      setLoading(false)
    }
  }

  function openCreate() {
    setForm(emptyForm)
    setDrawerOpen(true)
  }

  async function openEdit(id: number) {
    if (!canRead || !canUpdate) {
      setError('无权限编辑租户')
      return
    }
    try {
      const [detailRes, userRes] = await Promise.all([fetchTenantDetail(id), fetchTenantUsers(id)])
      if (detailRes.code !== 0) {
        setError(detailRes.msg || '租户详情加载失败')
        return
      }
      if (userRes.code !== 0) {
        setError(userRes.msg || '租户用户加载失败')
        return
      }
      const detail = detailRes.data
      const users: TenantUser[] = userRes.data
      setForm({
        id: detail.id,
        tenantId: detail.tenantId,
        tenantName: detail.tenantName,
        status: detail.status,
        expireAt: toDatetimeLocal(detail.expireAt),
        contactName: detail.contactName || '',
        contactPhone: detail.contactPhone || '',
        contactEmail: detail.contactEmail || '',
        remark: detail.remark || '',
        userIds: users.map((u) => String(u.userId)),
        defaultUserId: users.find((u) => u.isDefault)?.userId ? String(users.find((u) => u.isDefault)!.userId) : '',
      })
      setDrawerOpen(true)
    } catch {
      setError('租户详情加载失败')
    }
  }

  async function handleSave() {
    if (!(canCreate || canUpdate || canAssign)) {
      setError('无权限操作')
      return
    }
    if (!form.tenantName.trim()) {
      setError('租户名称不能为空')
      return
    }
    if (!isEdit && !form.tenantId.trim()) {
      setError('租户ID不能为空')
      return
    }
    if (canAssign && form.userIds.length > 0 && !form.defaultUserId) {
      setError('已绑定用户时必须设置默认用户')
      return
    }
    setSaving(true)
    setError('')
    try {
      const payloadBase = {
        tenantName: form.tenantName.trim(),
        status: form.status,
        expireAt: form.expireAt ? fromDatetimeLocal(form.expireAt) : undefined,
        contactName: form.contactName || undefined,
        contactPhone: form.contactPhone || undefined,
        contactEmail: form.contactEmail || undefined,
        remark: form.remark || undefined,
        userIds: canAssign ? form.userIds.map(Number) : [],
        defaultUserId: canAssign && form.defaultUserId ? Number(form.defaultUserId) : undefined,
      }
      if (isEdit && form.id) {
        const res = await updateTenant(form.id, payloadBase)
        if (res.code !== 0) {
          setError(res.msg || '更新租户失败')
          return
        }
      } else {
        const res = await createTenant({
          tenantId: form.tenantId.trim(),
          ...payloadBase,
        })
        if (res.code !== 0) {
          setError(res.msg || '创建租户失败')
          return
        }
      }
      setDrawerOpen(false)
      setForm(emptyForm)
      await loadTenants(page)
    } catch {
      setError('保存失败')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('确认删除该租户？')) {
      return
    }
    try {
      const res = await deleteTenant(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      await loadTenants(page)
    } catch {
      setError('删除失败')
    }
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>租户管理</h2>
        {canCreate && (
          <button className="primary-btn" onClick={openCreate}>
            新增租户
          </button>
        )}
      </header>

      <div className="toolbar-grid">
        <input placeholder="租户ID/名称" value={queryKeyword} onChange={(e) => setQueryKeyword(e.target.value)} />
        <Select value={queryStatus || 'ALL'} onValueChange={(value) => setQueryStatus(value && value !== 'ALL' ? value : '')}>
          <SelectTrigger className="app-select-trigger">
            <SelectValue />
          </SelectTrigger>
          <SelectContent className="app-select-content">
            <SelectItem value="ALL">全部状态</SelectItem>
            <SelectItem value="ENABLED">ENABLED</SelectItem>
            <SelectItem value="DISABLED">DISABLED</SelectItem>
          </SelectContent>
        </Select>
        <button className="ghost-btn" onClick={() => void loadTenants(1)}>
          查询
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>租户ID</th>
              <th>租户名称</th>
              <th>状态</th>
              <th>到期时间</th>
              <th>用户数</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={7}>加载中...</td>
              </tr>
            )}
            {!loading && rows.length === 0 && (
              <tr>
                <td colSpan={7}>暂无数据</td>
              </tr>
            )}
            {!loading &&
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.tenantId}</td>
                  <td>{row.tenantName}</td>
                  <td>{row.status}</td>
                  <td>{row.expireAt ? formatDate(row.expireAt) : '-'}</td>
                  <td>{row.userCount}</td>
                  <td>{formatDate(row.createdAt)}</td>
                  <td className="action-cell">
                    {canRead && canUpdate && (
                      <button className="text-btn" onClick={() => void openEdit(row.id)}>
                        编辑
                      </button>
                    )}
                    {canDelete && (
                      <button className="text-btn danger" onClick={() => void handleDelete(row.id)}>
                        删除
                      </button>
                    )}
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      <footer className="table-footer">
        <span>共 {total} 条</span>
        <div className="pager">
          <button className="ghost-btn" disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>
            上一页
          </button>
          <span className="pager-summary">
            第 {page} 页 / 每页 <PageSizeSelect value={size} onChange={setSize} /> <span className="pager-unit">条</span>
          </span>
          <button className="ghost-btn" disabled={page * size >= total} onClick={() => setPage((p) => p + 1)}>
            下一页
          </button>
        </div>
      </footer>

      {drawerOpen && (
        <div className="drawer-mask">
          <aside className="drawer drawer-wide">
            <header className="drawer-head">
              <h3>{isEdit ? '编辑租户' : '新增租户'}</h3>
              <button className="ghost-btn" onClick={() => setDrawerOpen(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              {!isEdit && (
                <>
                  <label>租户ID</label>
                  <input
                    value={form.tenantId}
                    onChange={(e) => setForm((prev) => ({ ...prev, tenantId: e.target.value }))}
                    placeholder="如：tenant-acme"
                  />
                </>
              )}
              {canUpdate && (
                <>
                  <label>租户名称</label>
                  <input value={form.tenantName} onChange={(e) => setForm((prev) => ({ ...prev, tenantName: e.target.value }))} />
                  <label>状态</label>
                  <Select
                    value={form.status}
                    onValueChange={(value) =>
                      setForm((prev) => ({ ...prev, status: (value || 'ENABLED') as 'ENABLED' | 'DISABLED' }))
                    }
                  >
                    <SelectTrigger className="app-select-trigger">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="app-select-content">
                      <SelectItem value="ENABLED">ENABLED</SelectItem>
                      <SelectItem value="DISABLED">DISABLED</SelectItem>
                    </SelectContent>
                  </Select>
                  <label>到期时间</label>
                  <input
                    type="datetime-local"
                    value={form.expireAt}
                    onChange={(e) => setForm((prev) => ({ ...prev, expireAt: e.target.value }))}
                  />
                  <label>联系人</label>
                  <input value={form.contactName} onChange={(e) => setForm((prev) => ({ ...prev, contactName: e.target.value }))} />
                  <label>手机号</label>
                  <input value={form.contactPhone} onChange={(e) => setForm((prev) => ({ ...prev, contactPhone: e.target.value }))} />
                  <label>邮箱</label>
                  <input value={form.contactEmail} onChange={(e) => setForm((prev) => ({ ...prev, contactEmail: e.target.value }))} />
                  <label>备注</label>
                  <textarea rows={2} value={form.remark} onChange={(e) => setForm((prev) => ({ ...prev, remark: e.target.value }))} />
                </>
              )}
              {canAssign && (
                <>
                  <label>租户成员</label>
                  <div className="checkbox-grid">
                    {allUsers.map((user) => (
                      <label key={user.id} className="app-check-label">
                        <Checkbox
                          checked={form.userIds.includes(String(user.id))}
                          onCheckedChange={(checked) => {
                            setForm((prev) => {
                              const nextUserIds = checked
                                ? Array.from(new Set([...prev.userIds, String(user.id)]))
                                : prev.userIds.filter((id) => id !== String(user.id))
                              const nextDefault = nextUserIds.includes(prev.defaultUserId) ? prev.defaultUserId : ''
                              return { ...prev, userIds: nextUserIds, defaultUserId: nextDefault }
                            })
                          }}
                        />
                        {user.realName} ({user.username})
                      </label>
                    ))}
                  </div>
                  <label>默认用户</label>
                  <Select
                    value={form.defaultUserId || '__NONE__'}
                    onValueChange={(value) =>
                      setForm((prev) => ({ ...prev, defaultUserId: value && value !== '__NONE__' ? value : '' }))
                    }
                    disabled={form.userIds.length === 0}
                  >
                    <SelectTrigger className="app-select-trigger">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="app-select-content">
                      <SelectItem value="__NONE__">未选择</SelectItem>
                      {selectedUsers.map((user) => (
                        <SelectItem key={user.id} value={String(user.id)}>
                          {user.realName} ({user.username})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </>
              )}
              {!canAssign && <p className="subtext">当前账号无 `sys:tenant:assign-user` 权限，仅可查看租户基本信息。</p>}
            </div>
            <footer className="drawer-footer">
              <button className="ghost-btn" onClick={() => setDrawerOpen(false)}>
                取消
              </button>
              <button className="primary-btn" disabled={saving} onClick={() => void handleSave()}>
                {saving ? '保存中...' : '保存'}
              </button>
            </footer>
          </aside>
        </div>
      )}
    </section>
  )
}

function formatDate(value?: string) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

function toDatetimeLocal(value?: string) {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 16)
}

function fromDatetimeLocal(value: string) {
  if (!value) {
    return ''
  }
  return new Date(value).toISOString().slice(0, 19)
}
