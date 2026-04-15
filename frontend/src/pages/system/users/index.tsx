import { useEffect, useMemo, useState } from 'react'
import { PageSizeSelect } from '@/components/PageSizeSelect'
import { Checkbox } from '@/components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  createUser,
  deleteUser,
  disableUser,
  enableUser,
  fetchDeptList,
  fetchRoleAll,
  fetchUserDetail,
  fetchUsers,
  resetUserPassword,
  updateUser,
} from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { DeptNode, RoleOption, UserDetail, UserRow } from '@/types/system'

type FormState = {
  id?: number
  username: string
  realName: string
  email: string
  phone: string
  userType: string
  deptId: string
  roleIds: string[]
}

const emptyForm: FormState = {
  username: '',
  realName: '',
  email: '',
  phone: '',
  userType: 'NORMAL',
  deptId: '',
  roleIds: [],
}

export default function UserManagementPage() {
  const canCreate = useHasPermission('sys:user:create')
  const canUpdate = useHasPermission('sys:user:update')
  const canDelete = useHasPermission('sys:user:delete')
  const canReset = useHasPermission('sys:user:reset-pwd')

  const [rows, setRows] = useState<UserRow[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [queryUsername, setQueryUsername] = useState('')
  const [queryRealName, setQueryRealName] = useState('')
  const [queryStatus, setQueryStatus] = useState('')

  const [roles, setRoles] = useState<RoleOption[]>([])
  const [depts, setDepts] = useState<DeptNode[]>([])
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState<FormState>(emptyForm)

  const isEdit = !!form.id

  useEffect(() => {
    loadUsers(page)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size])

  useEffect(() => {
    void Promise.all([fetchRoleAll(), fetchDeptList()])
      .then(([roleRes, deptRes]) => {
        if (roleRes.code === 0) {
          setRoles(roleRes.data)
        }
        if (deptRes.code === 0) {
          setDepts(deptRes.data)
        }
      })
      .catch(() => {
        setError('角色或部门数据加载失败')
      })
  }, [])

  const deptOptions = useMemo(
    () =>
      depts
        .slice()
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map((dept) => ({ id: dept.id, label: buildDeptLabel(dept, depts) })),
    [depts],
  )

  async function loadUsers(targetPage = page) {
    setLoading(true)
    setError('')
    try {
      const res = await fetchUsers({
        page: targetPage,
        size,
        username: queryUsername || undefined,
        realName: queryRealName || undefined,
        status: queryStatus || undefined,
      })
      if (res.code !== 0) {
        setError(res.msg || '加载失败')
        return
      }
      setRows(res.data.records)
      setTotal(res.data.total)
      setPage(res.data.page)
      setSize(res.data.size)
    } catch {
      setError('用户列表加载失败')
    } finally {
      setLoading(false)
    }
  }

  async function handleSearch() {
    setPage(1)
    await loadUsers(1)
  }

  function openCreate() {
    setForm(emptyForm)
    setDrawerOpen(true)
  }

  async function openEdit(id: number) {
    try {
      const res = await fetchUserDetail(id)
      if (res.code !== 0) {
        setError(res.msg || '加载用户详情失败')
        return
      }
      const data: UserDetail = res.data
      setForm({
        id: data.id,
        username: data.username,
        realName: data.realName,
        email: data.email || '',
        phone: data.phone || '',
        userType: data.userType || 'NORMAL',
        deptId: data.deptId ? String(data.deptId) : '',
        roleIds: data.roleIds.map(String),
      })
      setDrawerOpen(true)
    } catch {
      setError('加载用户详情失败')
    }
  }

  async function handleSave() {
    if (!form.realName.trim()) {
      setError('真实姓名不能为空')
      return
    }
    if (!isEdit && !form.username.trim()) {
      setError('用户名不能为空')
      return
    }
    setSaving(true)
    setError('')
    try {
      if (isEdit && form.id) {
        const res = await updateUser(form.id, {
          realName: form.realName.trim(),
          email: form.email || undefined,
          phone: form.phone || undefined,
          userType: form.userType,
          deptId: form.deptId ? Number(form.deptId) : undefined,
          roleIds: form.roleIds.map(Number),
        })
        if (res.code !== 0) {
          setError(res.msg || '保存失败')
          return
        }
      } else {
        const res = await createUser({
          username: form.username.trim(),
          realName: form.realName.trim(),
          email: form.email || undefined,
          phone: form.phone || undefined,
          userType: form.userType,
          deptId: form.deptId ? Number(form.deptId) : undefined,
          roleIds: form.roleIds.map(Number),
        })
        if (res.code !== 0) {
          setError(res.msg || '创建失败')
          return
        }
      }
      setDrawerOpen(false)
      setForm(emptyForm)
      await loadUsers(page)
    } catch {
      setError('保存失败')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('确认删除该用户？')) {
      return
    }
    try {
      const res = await deleteUser(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      await loadUsers(page)
    } catch {
      setError('删除失败')
    }
  }

  async function handleToggleStatus(row: UserRow) {
    try {
      const res = row.status === 'ACTIVE' ? await disableUser(row.id) : await enableUser(row.id)
      if (res.code !== 0) {
        setError(res.msg || '状态更新失败')
        return
      }
      await loadUsers(page)
    } catch {
      setError('状态更新失败')
    }
  }

  async function handleResetPassword(id: number) {
    try {
      const res = await resetUserPassword(id)
      if (res.code !== 0) {
        setError(res.msg || '重置密码失败')
        return
      }
      window.alert(`新密码：${res.data.password}（请尽快通知用户修改）`)
    } catch {
      setError('重置密码失败')
    }
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>用户管理</h2>
        {canCreate && (
          <button className="primary-btn" onClick={openCreate}>
            新增用户
          </button>
        )}
      </header>

      <div className="toolbar-grid">
        <input placeholder="用户名" value={queryUsername} onChange={(e) => setQueryUsername(e.target.value)} />
        <input placeholder="姓名" value={queryRealName} onChange={(e) => setQueryRealName(e.target.value)} />
        <Select value={queryStatus || 'ALL'} onValueChange={(value) => setQueryStatus(value && value !== 'ALL' ? value : '')}>
          <SelectTrigger className="app-select-trigger">
            <SelectValue />
          </SelectTrigger>
          <SelectContent className="app-select-content">
            <SelectItem value="ALL">全部状态</SelectItem>
            <SelectItem value="ACTIVE">ACTIVE</SelectItem>
            <SelectItem value="DISABLED">DISABLED</SelectItem>
            <SelectItem value="LOCKED">LOCKED</SelectItem>
          </SelectContent>
        </Select>
        <button className="ghost-btn" onClick={() => void handleSearch()}>
          查询
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>姓名</th>
              <th>邮箱</th>
              <th>部门</th>
              <th>角色</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={8}>加载中...</td>
              </tr>
            )}
            {!loading && rows.length === 0 && (
              <tr>
                <td colSpan={8}>暂无数据</td>
              </tr>
            )}
            {!loading &&
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.username}</td>
                  <td>{row.realName}</td>
                  <td>{row.email || '-'}</td>
                  <td>{row.deptName || '-'}</td>
                  <td>{row.roles.map((r) => r.roleName).join(' / ') || '-'}</td>
                  <td>
                    <span className={`status-badge status-${row.status.toLowerCase()}`}>{row.status}</span>
                  </td>
                  <td>{formatDate(row.createdAt)}</td>
                  <td className="action-cell">
                    {canUpdate && (
                      <button className="text-btn" onClick={() => void openEdit(row.id)}>
                        编辑
                      </button>
                    )}
                    {canUpdate && (
                      <button className="text-btn" onClick={() => void handleToggleStatus(row)}>
                        {row.status === 'ACTIVE' ? '禁用' : '启用'}
                      </button>
                    )}
                    {canReset && (
                      <button className="text-btn" onClick={() => void handleResetPassword(row.id)}>
                        重置密码
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
          <button className="ghost-btn" disabled={page <= 1} onClick={() => setPage((p) => Math.max(p - 1, 1))}>
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
          <aside className="drawer">
            <header className="drawer-head">
              <h3>{isEdit ? '编辑用户' : '新增用户'}</h3>
              <button className="ghost-btn" onClick={() => setDrawerOpen(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              <label>登录用户名</label>
              <input
                value={form.username}
                disabled={isEdit}
                onChange={(e) => setForm((prev) => ({ ...prev, username: e.target.value }))}
              />
              <label>真实姓名</label>
              <input value={form.realName} onChange={(e) => setForm((prev) => ({ ...prev, realName: e.target.value }))} />
              <label>用户类型</label>
              <Select value={form.userType} onValueChange={(value) => setForm((prev) => ({ ...prev, userType: value || 'NORMAL' }))}>
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="TENANT_ADMIN">TENANT_ADMIN</SelectItem>
                  <SelectItem value="NORMAL">NORMAL</SelectItem>
                </SelectContent>
              </Select>
              <label>部门</label>
              <Select
                value={form.deptId || '__NONE__'}
                onValueChange={(value) => setForm((prev) => ({ ...prev, deptId: value && value !== '__NONE__' ? value : '' }))}
              >
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="__NONE__">未分配</SelectItem>
                  {deptOptions.map((dept) => (
                    <SelectItem key={dept.id} value={String(dept.id)}>
                      {dept.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <label>分配角色</label>
              <div className="checkbox-grid">
                {roles.map((role) => (
                  <label key={role.id} className="app-check-label">
                    <Checkbox
                      checked={form.roleIds.includes(String(role.id))}
                      onCheckedChange={(checked) => {
                        setForm((prev) => ({
                          ...prev,
                          roleIds: checked
                            ? [...prev.roleIds, String(role.id)]
                            : prev.roleIds.filter((item) => item !== String(role.id)),
                        }))
                      }}
                    />
                    {role.roleName}
                  </label>
                ))}
              </div>
              <label>邮箱</label>
              <input value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
              <label>手机号</label>
              <input value={form.phone} onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))} />
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

function buildDeptLabel(target: DeptNode, all: DeptNode[]) {
  const map = new Map<number, DeptNode>()
  all.forEach((item) => map.set(item.id, item))
  const labels: string[] = [target.deptName]
  let cursor = target.parentId
  while (cursor && cursor !== 0 && map.has(cursor)) {
    const parent = map.get(cursor)!
    labels.unshift(parent.deptName)
    cursor = parent.parentId
  }
  return labels.join(' / ')
}

function formatDate(value?: string) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
