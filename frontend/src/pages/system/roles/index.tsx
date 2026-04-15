import { useEffect, useMemo, useState } from 'react'
import { PageSizeSelect } from '@/components/PageSizeSelect'
import { Checkbox } from '@/components/ui/checkbox'
import {
  createRole,
  deleteRole,
  fetchPermissionTree,
  fetchRoleDetail,
  fetchRoles,
  updateRole,
} from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { PermissionNode, RoleDetail, RoleRow } from '@/types/system'

type RoleForm = {
  id?: number
  roleCode: string
  roleName: string
  remark: string
  sortOrder: number
  permIds: number[]
}

const emptyRoleForm: RoleForm = {
  roleCode: '',
  roleName: '',
  remark: '',
  sortOrder: 0,
  permIds: [],
}

export default function RoleManagementPage() {
  const canCreate = useHasPermission('sys:role:create')
  const canUpdate = useHasPermission('sys:role:update')
  const canDelete = useHasPermission('sys:role:delete')
  const canAssign = useHasPermission('sys:role:assign-perm')

  const [rows, setRows] = useState<RoleRow[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [queryRoleName, setQueryRoleName] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [permTree, setPermTree] = useState<PermissionNode[]>([])
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState<RoleForm>(emptyRoleForm)

  const isEdit = !!form.id

  useEffect(() => {
    void loadRoles(page)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size])

  useEffect(() => {
    fetchPermissionTree()
      .then((res) => {
        if (res.code === 0) {
          setPermTree(res.data)
        }
      })
      .catch(() => {
        setError('权限树加载失败')
      })
  }, [])

  const permItems = useMemo(() => flattenPermissionTree(permTree), [permTree])

  async function loadRoles(targetPage = page) {
    setLoading(true)
    setError('')
    try {
      const res = await fetchRoles({
        page: targetPage,
        size,
        roleName: queryRoleName || undefined,
      })
      if (res.code !== 0) {
        setError(res.msg || '角色列表加载失败')
        return
      }
      setRows(res.data.records)
      setTotal(res.data.total)
      setPage(res.data.page)
      setSize(res.data.size)
    } catch {
      setError('角色列表加载失败')
    } finally {
      setLoading(false)
    }
  }

  function openCreate() {
    setForm(emptyRoleForm)
    setDrawerOpen(true)
  }

  async function openEdit(id: number) {
    try {
      const res = await fetchRoleDetail(id)
      if (res.code !== 0) {
        setError(res.msg || '角色详情加载失败')
        return
      }
      const role: RoleDetail = res.data
      setForm({
        id: role.id,
        roleCode: role.roleCode,
        roleName: role.roleName,
        remark: role.remark || '',
        sortOrder: role.sortOrder || 0,
        permIds: role.permIds || [],
      })
      setDrawerOpen(true)
    } catch {
      setError('角色详情加载失败')
    }
  }

  async function handleSave() {
    if (!form.roleName.trim()) {
      setError('角色名称不能为空')
      return
    }
    if (!isEdit && !form.roleCode.trim()) {
      setError('角色编码不能为空')
      return
    }
    setSaving(true)
    setError('')
    try {
      if (isEdit && form.id) {
        const res = await updateRole(form.id, {
          roleName: form.roleName.trim(),
          remark: form.remark || undefined,
          sortOrder: Number(form.sortOrder) || 0,
          permIds: form.permIds,
        })
        if (res.code !== 0) {
          setError(res.msg || '更新失败')
          return
        }
      } else {
        const res = await createRole({
          roleCode: form.roleCode.trim(),
          roleName: form.roleName.trim(),
          remark: form.remark || undefined,
          sortOrder: Number(form.sortOrder) || 0,
          permIds: form.permIds,
        })
        if (res.code !== 0) {
          setError(res.msg || '创建失败')
          return
        }
      }
      setDrawerOpen(false)
      setForm(emptyRoleForm)
      await loadRoles(page)
    } catch {
      setError('保存失败')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('确认删除该角色？')) {
      return
    }
    try {
      const res = await deleteRole(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      await loadRoles(page)
    } catch {
      setError('删除失败')
    }
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>角色管理</h2>
        {canCreate && (
          <button className="primary-btn" onClick={openCreate}>
            新增角色
          </button>
        )}
      </header>

      <div className="toolbar-grid">
        <input placeholder="角色名称" value={queryRoleName} onChange={(e) => setQueryRoleName(e.target.value)} />
        <button className="ghost-btn" onClick={() => void loadRoles(1)}>
          查询
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>角色编码</th>
              <th>角色名称</th>
              <th>用户数</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={5}>加载中...</td>
              </tr>
            )}
            {!loading && rows.length === 0 && (
              <tr>
                <td colSpan={5}>暂无数据</td>
              </tr>
            )}
            {!loading &&
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.roleCode}</td>
                  <td>{row.roleName}</td>
                  <td>{row.userCount}</td>
                  <td>{row.remark || '-'}</td>
                  <td className="action-cell">
                    <div className="action-cell-inner">
                      {(canUpdate || canAssign) && (
                        <button className="text-btn" onClick={() => void openEdit(row.id)}>
                          分配权限/编辑
                        </button>
                      )}
                      {canDelete && (
                        <button className="text-btn danger" onClick={() => void handleDelete(row.id)}>
                          删除
                        </button>
                      )}
                    </div>
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
          <aside className="drawer drawer-wide">
            <header className="drawer-head">
              <h3>{isEdit ? '编辑角色' : '新增角色'}</h3>
              <button className="ghost-btn" onClick={() => setDrawerOpen(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              <label>角色编码</label>
              <input
                value={form.roleCode}
                disabled={isEdit}
                onChange={(e) => setForm((prev) => ({ ...prev, roleCode: e.target.value }))}
              />
              <label>角色名称</label>
              <input value={form.roleName} onChange={(e) => setForm((prev) => ({ ...prev, roleName: e.target.value }))} />
              <label>排序</label>
              <input
                type="number"
                value={form.sortOrder}
                onChange={(e) => setForm((prev) => ({ ...prev, sortOrder: Number(e.target.value) }))}
              />
              <label>备注</label>
              <textarea rows={2} value={form.remark} onChange={(e) => setForm((prev) => ({ ...prev, remark: e.target.value }))} />
              <label>权限分配</label>
              <div className="permission-tree">
                {permItems.map((perm) => (
                  <label key={perm.id} className="permission-item app-check-label" style={{ paddingLeft: `${perm.level * 18 + 8}px` }}>
                    <Checkbox
                      checked={form.permIds.includes(perm.id)}
                      onCheckedChange={(checked) => {
                        setForm((prev) => ({
                          ...prev,
                          permIds: checked
                            ? Array.from(new Set([...prev.permIds, perm.id]))
                            : prev.permIds.filter((id) => id !== perm.id),
                        }))
                      }}
                    />
                    <span className="perm-chip">{perm.permType}</span>
                    {perm.permName}
                    <small>{perm.permCode}</small>
                  </label>
                ))}
              </div>
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

function flattenPermissionTree(tree: PermissionNode[]) {
  const items: Array<PermissionNode & { level: number }> = []

  const walk = (nodes: PermissionNode[], level: number) => {
    nodes.forEach((node) => {
      items.push({ ...node, level })
      if (node.children?.length) {
        walk(node.children, level + 1)
      }
    })
  }

  walk(tree, 0)
  return items
}
