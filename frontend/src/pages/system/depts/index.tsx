import { useEffect, useMemo, useState } from 'react'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  createDept,
  deleteDept,
  fetchDeptList,
  fetchDeptTree,
  fetchDeptUsers,
  fetchUsers,
  updateDept,
} from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { DeptNode, DeptUser } from '@/types/system'

type DeptForm = {
  id?: number
  deptName: string
  parentId: string
  leaderId: string
  sortOrder: number
  remark: string
}

const emptyForm: DeptForm = {
  deptName: '',
  parentId: '0',
  leaderId: '',
  sortOrder: 0,
  remark: '',
}

export default function DeptManagementPage() {
  const canCreate = useHasPermission('sys:dept:create')
  const canUpdate = useHasPermission('sys:dept:update')
  const canDelete = useHasPermission('sys:dept:delete')

  const [tree, setTree] = useState<DeptNode[]>([])
  const [flat, setFlat] = useState<DeptNode[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [deptUsers, setDeptUsers] = useState<DeptUser[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState<DeptForm>(emptyForm)
  const [leaderOptions, setLeaderOptions] = useState<Array<{ id: number; label: string }>>([])

  const selectedNode = useMemo(() => findNodeById(tree, selectedId), [selectedId, tree])

  useEffect(() => {
    void loadData()
  }, [])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const [treeRes, listRes, userRes] = await Promise.all([
        fetchDeptTree(),
        fetchDeptList(),
        fetchUsers({ page: 1, size: 200 }),
      ])
      if (treeRes.code === 0) {
        setTree(treeRes.data)
        const first = firstNode(treeRes.data)
        if (first) {
          setSelectedId((prev) => prev ?? first.id)
        }
      }
      if (listRes.code === 0) {
        setFlat(listRes.data)
      }
      if (userRes.code === 0) {
        setLeaderOptions(userRes.data.records.map((item) => ({ id: item.id, label: `${item.realName}(${item.username})` })))
      }
    } catch {
      setError('部门数据加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!selectedId) {
      setDeptUsers([])
      return
    }
    fetchDeptUsers(selectedId)
      .then((res) => {
        if (res.code === 0) {
          setDeptUsers(res.data)
        }
      })
      .catch(() => {
        setError('部门用户加载失败')
      })
  }, [selectedId])

  function openCreate(parentId?: number) {
    setForm({
      ...emptyForm,
      parentId: String(parentId ?? 0),
    })
    setDrawerOpen(true)
  }

  function openEdit(node: DeptNode) {
    setForm({
      id: node.id,
      deptName: node.deptName,
      parentId: String(node.parentId),
      leaderId: node.leaderId ? String(node.leaderId) : '',
      sortOrder: node.sortOrder || 0,
      remark: node.remark || '',
    })
    setDrawerOpen(true)
  }

  async function handleSave() {
    if (!form.deptName.trim()) {
      setError('部门名称不能为空')
      return
    }
    setSaving(true)
    setError('')
    try {
      if (form.id) {
        const res = await updateDept(form.id, {
          deptName: form.deptName.trim(),
          parentId: Number(form.parentId),
          leaderId: form.leaderId ? Number(form.leaderId) : undefined,
          sortOrder: Number(form.sortOrder) || 0,
          remark: form.remark || undefined,
        })
        if (res.code !== 0) {
          setError(res.msg || '更新失败')
          return
        }
      } else {
        const res = await createDept({
          deptName: form.deptName.trim(),
          parentId: Number(form.parentId),
          leaderId: form.leaderId ? Number(form.leaderId) : undefined,
          sortOrder: Number(form.sortOrder) || 0,
          remark: form.remark || undefined,
        })
        if (res.code !== 0) {
          setError(res.msg || '创建失败')
          return
        }
      }
      setDrawerOpen(false)
      setForm(emptyForm)
      await loadData()
    } catch {
      setError('保存失败')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('确认删除该部门？')) {
      return
    }
    try {
      const res = await deleteDept(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      setSelectedId(null)
      await loadData()
    } catch {
      setError('删除失败')
    }
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>部门管理</h2>
        {canCreate && (
          <button className="primary-btn" onClick={() => openCreate(0)}>
            新增根部门
          </button>
        )}
      </header>

      {error && <p className="error">{error}</p>}
      {loading && <p>加载中...</p>}

      <div className="dept-layout">
        <aside className="dept-tree-pane">
          {tree.length === 0 && <p className="subtext">暂无部门数据</p>}
          {tree.map((node) => (
            <DeptTreeItem key={node.id} node={node} selectedId={selectedId} onSelect={setSelectedId} />
          ))}
        </aside>
        <div className="dept-detail-pane">
          {!selectedNode && <p className="subtext">请选择一个部门查看详情</p>}
          {selectedNode && (
            <>
              <h3>{selectedNode.deptName}</h3>
              <p>负责人：{selectedNode.leaderName || '未设置'}</p>
              <p>用户数：{selectedNode.userCount} 人</p>
              <div className="action-line">
                {canCreate && (
                  <button className="ghost-btn" onClick={() => openCreate(selectedNode.id)}>
                    新增子部门
                  </button>
                )}
                {canUpdate && (
                  <button className="ghost-btn" onClick={() => openEdit(selectedNode)}>
                    编辑
                  </button>
                )}
                {canDelete && (
                  <button className="ghost-btn danger" onClick={() => void handleDelete(selectedNode.id)}>
                    删除
                  </button>
                )}
              </div>
              <h4>部门用户</h4>
              <ul className="dept-user-list">
                {deptUsers.length === 0 && <li>暂无用户</li>}
                {deptUsers.map((user) => (
                  <li key={user.userId}>
                    {user.realName} ({user.username}) - {user.status}
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      </div>

      {drawerOpen && (
        <div className="drawer-mask">
          <aside className="drawer">
            <header className="drawer-head">
              <h3>{form.id ? '编辑部门' : '新增部门'}</h3>
              <button className="ghost-btn" onClick={() => setDrawerOpen(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              <label>部门名称</label>
              <input value={form.deptName} onChange={(e) => setForm((prev) => ({ ...prev, deptName: e.target.value }))} />
              <label>上级部门</label>
              <Select value={form.parentId} onValueChange={(value) => setForm((prev) => ({ ...prev, parentId: value || '0' }))}>
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="0">根节点</SelectItem>
                  {flat.map((dept) => (
                    <SelectItem key={dept.id} value={String(dept.id)}>
                      {dept.deptName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <label>部门负责人</label>
              <Select
                value={form.leaderId || '__NONE__'}
                onValueChange={(value) => setForm((prev) => ({ ...prev, leaderId: value && value !== '__NONE__' ? value : '' }))}
              >
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="__NONE__">未设置</SelectItem>
                  {leaderOptions.map((leader) => (
                    <SelectItem key={leader.id} value={String(leader.id)}>
                      {leader.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <label>排序</label>
              <input
                type="number"
                value={form.sortOrder}
                onChange={(e) => setForm((prev) => ({ ...prev, sortOrder: Number(e.target.value) }))}
              />
              <label>备注</label>
              <textarea rows={2} value={form.remark} onChange={(e) => setForm((prev) => ({ ...prev, remark: e.target.value }))} />
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

function DeptTreeItem({
  node,
  selectedId,
  onSelect,
  level = 0,
}: {
  node: DeptNode
  selectedId: number | null
  onSelect: (id: number) => void
  level?: number
}) {
  return (
    <div>
      <button
        className={`tree-node ${selectedId === node.id ? 'active' : ''}`}
        style={{ paddingLeft: `${8 + level * 14}px` }}
        onClick={() => onSelect(node.id)}
      >
        {node.deptName}
      </button>
      {node.children?.map((child) => (
        <DeptTreeItem key={child.id} node={child} selectedId={selectedId} onSelect={onSelect} level={level + 1} />
      ))}
    </div>
  )
}

function findNodeById(nodes: DeptNode[], id: number | null): DeptNode | null {
  if (!id) {
    return null
  }
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    const child = findNodeById(node.children || [], id)
    if (child) {
      return child
    }
  }
  return null
}

function firstNode(nodes: DeptNode[]): DeptNode | null {
  if (!nodes.length) {
    return null
  }
  return nodes[0]
}
