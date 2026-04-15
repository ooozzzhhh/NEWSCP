import { useEffect, useMemo, useState } from 'react'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  createPermission,
  deletePermission,
  fetchPermissionOperateTree,
  updatePermission,
} from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { PermissionNode } from '@/types/system'

type PermissionForm = {
  id?: number
  parentId: string
  permType: 'MENU' | 'BUTTON'
  permCode: string
  permName: string
  routePath: string
  componentPath: string
  icon: string
  sortOrder: number
  isHidden: number
  status: 'ENABLED' | 'DISABLED'
}

const emptyForm: PermissionForm = {
  parentId: '0',
  permType: 'MENU',
  permCode: '',
  permName: '',
  routePath: '',
  componentPath: '',
  icon: '',
  sortOrder: 0,
  isHidden: 0,
  status: 'ENABLED',
}

export default function PermissionOperatePage() {
  const canCreate = useHasPermission('sys:perm:create')
  const canUpdate = useHasPermission('sys:perm:update')
  const canDelete = useHasPermission('sys:perm:delete')
  const canList = useHasPermission('sys:perm:list')

  const [tree, setTree] = useState<PermissionNode[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState<PermissionForm>(emptyForm)
  const [collapsedIds, setCollapsedIds] = useState<Set<number>>(new Set())

  const flatNodes = useMemo(() => flattenPermissionTree(tree, collapsedIds), [tree, collapsedIds])
  const parentOptions = useMemo(() => flatNodes.filter((n) => n.permType === 'MENU'), [flatNodes])

  const toggleCollapse = (id: number) => {
    setCollapsedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  useEffect(() => {
    if (!canList) {
      return
    }
    void loadTree()
  }, [canList])

  async function loadTree() {
    setLoading(true)
    setError('')
    try {
      const res = await fetchPermissionOperateTree()
      if (res.code !== 0) {
        setError(res.msg || '权限树加载失败')
        return
      }
      setTree(res.data)
    } catch {
      setError('权限树加载失败')
    } finally {
      setLoading(false)
    }
  }

  function openCreate(parentId = 0, permType: 'MENU' | 'BUTTON' = 'MENU') {
    setForm({
      ...emptyForm,
      parentId: String(parentId),
      permType,
    })
    setDrawerOpen(true)
  }

  function openEdit(node: PermissionNode) {
    setForm({
      id: node.id,
      parentId: String(node.parentId ?? 0),
      permType: node.permType,
      permCode: node.permCode,
      permName: node.permName,
      routePath: node.routePath || '',
      componentPath: node.componentPath || '',
      icon: node.icon || '',
      sortOrder: node.sortOrder || 0,
      isHidden: node.isHidden ?? 0,
      status: node.status || 'ENABLED',
    })
    setDrawerOpen(true)
  }

  async function handleSave() {
    if (!form.permName.trim()) {
      setError('权限名称不能为空')
      return
    }
    if (!form.id && !form.permCode.trim()) {
      setError('权限编码不能为空')
      return
    }
    if (form.permType === 'BUTTON' && Number(form.parentId) === 0) {
      setError('按钮权限必须挂在菜单下')
      return
    }
    setSaving(true)
    setError('')
    try {
      if (form.id) {
        const res = await updatePermission(form.id, {
          parentId: Number(form.parentId),
          permName: form.permName.trim(),
          routePath: form.routePath || undefined,
          componentPath: form.componentPath || undefined,
          icon: form.icon || undefined,
          sortOrder: Number(form.sortOrder) || 0,
          isHidden: Number(form.isHidden) || 0,
          status: form.status,
        })
        if (res.code !== 0) {
          setError(res.msg || '更新失败')
          return
        }
      } else {
        const res = await createPermission({
          parentId: Number(form.parentId),
          permType: form.permType,
          permCode: form.permCode.trim(),
          permName: form.permName.trim(),
          routePath: form.routePath || undefined,
          componentPath: form.componentPath || undefined,
          icon: form.icon || undefined,
          sortOrder: Number(form.sortOrder) || 0,
          isHidden: Number(form.isHidden) || 0,
          status: form.status,
        })
        if (res.code !== 0) {
          setError(res.msg || '创建失败')
          return
        }
      }
      setDrawerOpen(false)
      setForm(emptyForm)
      await loadTree()
    } catch {
      setError('保存失败')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('确认删除该权限节点？')) {
      return
    }
    try {
      const res = await deletePermission(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      await loadTree()
    } catch {
      setError('删除失败')
    }
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>菜单权限运营</h2>
        {canCreate && (
          <button className="primary-btn" onClick={() => openCreate(0, 'MENU')}>
            新增顶级菜单
          </button>
        )}
      </header>

      {error && <p className="error">{error}</p>}
      {loading && <p>加载中...</p>}

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>编码</th>
              <th>类型</th>
              <th>路由</th>
              <th>组件</th>
              <th>状态</th>
              <th>可见</th>
              <th>排序</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {flatNodes.length === 0 && (
              <tr>
                <td colSpan={9}>暂无数据</td>
              </tr>
            )}
            {flatNodes.map((node) => (
              <tr key={node.id}>
                <td>
                  <div className="tree-cell" style={{ paddingLeft: `${node.level * 20 + 12}px` }}>
                    {node.hasChildren ? (
                      <button
                        type="button"
                        className={`tree-toggle ${node.collapsed ? 'collapsed' : ''}`}
                        onClick={() => toggleCollapse(node.id)}
                        aria-label={node.collapsed ? '展开' : '折叠'}
                      >
                        {node.collapsed ? '▸' : '▾'}
                      </button>
                    ) : (
                      <span className="tree-toggle-placeholder" />
                    )}
                    <span>{node.permName}</span>
                  </div>
                </td>
                <td>{node.permCode}</td>
                <td>{node.permType}</td>
                <td>{node.routePath || '-'}</td>
                <td>{node.componentPath || '-'}</td>
                <td>{node.status}</td>
                <td>{node.isHidden === 1 ? '隐藏' : '显示'}</td>
                <td>{node.sortOrder}</td>
                <td className="action-cell">
                  {canCreate && node.permType === 'MENU' && (
                    <button className="text-btn" onClick={() => openCreate(node.id, 'BUTTON')}>
                      加按钮
                    </button>
                  )}
                  {canCreate && node.permType === 'MENU' && (
                    <button className="text-btn" onClick={() => openCreate(node.id, 'MENU')}>
                      加子菜单
                    </button>
                  )}
                  {canUpdate && (
                    <button className="text-btn" onClick={() => openEdit(node)}>
                      编辑
                    </button>
                  )}
                  {canDelete && (
                    <button className="text-btn danger" onClick={() => void handleDelete(node.id)}>
                      删除
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {drawerOpen && (
        <div className="drawer-mask">
          <aside className="drawer drawer-wide">
            <header className="drawer-head">
              <h3>{form.id ? '编辑权限节点' : '新增权限节点'}</h3>
              <button className="ghost-btn" onClick={() => setDrawerOpen(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              <label>权限类型</label>
              <Select
                value={form.permType}
                disabled={!!form.id}
                onValueChange={(value) => setForm((prev) => ({ ...prev, permType: (value || 'MENU') as 'MENU' | 'BUTTON' }))}
              >
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="MENU">MENU</SelectItem>
                  <SelectItem value="BUTTON">BUTTON</SelectItem>
                </SelectContent>
              </Select>
              <label>父节点</label>
              <Select value={form.parentId} onValueChange={(value) => setForm((prev) => ({ ...prev, parentId: value || '0' }))}>
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="0">根节点</SelectItem>
                  {parentOptions.map((node) => (
                    <SelectItem key={node.id} value={String(node.id)}>
                      {`${'-'.repeat(node.level)} ${node.permName}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <label>权限编码</label>
              <input
                value={form.permCode}
                disabled={!!form.id}
                onChange={(e) => setForm((prev) => ({ ...prev, permCode: e.target.value }))}
              />
              <label>权限名称</label>
              <input value={form.permName} onChange={(e) => setForm((prev) => ({ ...prev, permName: e.target.value }))} />
              <label>路由路径</label>
              <input value={form.routePath} onChange={(e) => setForm((prev) => ({ ...prev, routePath: e.target.value }))} />
              <label>组件路径</label>
              <input
                value={form.componentPath}
                onChange={(e) => setForm((prev) => ({ ...prev, componentPath: e.target.value }))}
              />
              <label>图标</label>
              <input value={form.icon} onChange={(e) => setForm((prev) => ({ ...prev, icon: e.target.value }))} />
              <label>状态</label>
              <Select
                value={form.status}
                onValueChange={(value) => setForm((prev) => ({ ...prev, status: (value || 'ENABLED') as 'ENABLED' | 'DISABLED' }))}
              >
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="ENABLED">ENABLED</SelectItem>
                  <SelectItem value="DISABLED">DISABLED</SelectItem>
                </SelectContent>
              </Select>
              <label>是否隐藏</label>
              <Select
                value={String(form.isHidden)}
                onValueChange={(value) => setForm((prev) => ({ ...prev, isHidden: Number(value || 0) }))}
              >
                <SelectTrigger className="app-select-trigger">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="app-select-content">
                  <SelectItem value="0">显示</SelectItem>
                  <SelectItem value="1">隐藏</SelectItem>
                </SelectContent>
              </Select>
              <label>排序</label>
              <input
                type="number"
                value={form.sortOrder}
                onChange={(e) => setForm((prev) => ({ ...prev, sortOrder: Number(e.target.value) }))}
              />
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

type VisiblePermissionNode = PermissionNode & {
  level: number
  hasChildren: boolean
  collapsed: boolean
}

function flattenPermissionTree(tree: PermissionNode[], collapsedIds: Set<number>) {
  const items: Array<PermissionNode & { level: number }> = []
  const walk = (nodes: PermissionNode[], level: number) => {
    nodes.forEach((node) => {
      items.push({ ...node, level })
      if (node.children?.length && !collapsedIds.has(node.id)) {
        walk(node.children, level + 1)
      }
    })
  }
  walk(tree, 0)
  return items.map((item): VisiblePermissionNode => ({
    ...item,
    hasChildren: !!item.children?.length,
    collapsed: collapsedIds.has(item.id),
  }))
}
