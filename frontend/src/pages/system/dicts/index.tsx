import { useEffect, useState } from 'react'
import { PageSizeSelect } from '@/components/PageSizeSelect'
import {
  batchSortDictItems,
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  fetchDictItems,
  fetchDictTypes,
  updateDictItem,
  updateDictType,
} from '@/api/system'
import { useHasPermission } from '@/hooks/usePermission'
import type { DictItemPayload, DictItemRow, DictTypePayload, DictTypeRow } from '@/types/system'

const emptyTypeForm: DictTypePayload = {
  typeCode: '',
  typeName: '',
  source: 'CUSTOM',
  editable: 1,
  status: 1,
  sortOrder: 0,
  remark: '',
}

const emptyItemForm: DictItemPayload = {
  typeCode: '',
  value: '',
  label: '',
  labelEn: '',
  color: '',
  extra: '',
  sortOrder: 0,
  status: 1,
  isDefault: 0,
  remark: '',
}

export default function DictManagementPage() {
  const canList = useHasPermission('sys:dict:list')
  const canCreate = useHasPermission('sys:dict:create')
  const canEdit = useHasPermission('sys:dict:edit')
  const canDelete = useHasPermission('sys:dict:delete')

  const [types, setTypes] = useState<DictTypeRow[]>([])
  const [items, setItems] = useState<DictItemRow[]>([])
  const [selectedType, setSelectedType] = useState<DictTypeRow | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [total, setTotal] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [typeForm, setTypeForm] = useState<DictTypePayload>(emptyTypeForm)
  const [typeFormId, setTypeFormId] = useState<number | null>(null)
  const [itemForm, setItemForm] = useState<DictItemPayload>(emptyItemForm)
  const [itemFormId, setItemFormId] = useState<number | null>(null)
  const [showTypeForm, setShowTypeForm] = useState(false)
  const [showItemForm, setShowItemForm] = useState(false)

  useEffect(() => {
    if (!canList) return
    void loadTypes(1)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [canList])

  useEffect(() => {
    if (!selectedType) {
      setItems([])
      return
    }
    void loadItems(selectedType.typeCode)
  }, [selectedType])

  async function loadTypes(targetPage = page) {
    setLoading(true)
    setError('')
    try {
      const res = await fetchDictTypes({ page: targetPage, size, keyword: keyword || undefined })
      if (res.code !== 0) {
        setError(res.msg || '加载类型失败')
        return
      }
      setTypes(res.data.records)
      setTotal(res.data.total)
      setPage(res.data.page)
      setSize(res.data.size)
      if (!selectedType && res.data.records.length > 0) {
        setSelectedType(res.data.records[0])
      } else if (selectedType) {
        const nextSelected = res.data.records.find((item) => item.id === selectedType.id) || null
        setSelectedType(nextSelected)
      }
    } catch {
      setError('加载类型失败')
    } finally {
      setLoading(false)
    }
  }

  async function loadItems(typeCode: string) {
    try {
      const res = await fetchDictItems(typeCode)
      if (res.code !== 0) {
        setError(res.msg || '加载枚举值失败')
        return
      }
      setItems(res.data)
    } catch {
      setError('加载枚举值失败')
    }
  }

  function openCreateType() {
    setTypeForm(emptyTypeForm)
    setTypeFormId(null)
    setShowTypeForm(true)
  }

  function openEditType(row: DictTypeRow) {
    setTypeForm({
      typeCode: row.typeCode,
      typeName: row.typeName,
      source: row.source,
      editable: row.editable,
      status: row.status,
      sortOrder: row.sortOrder,
      remark: row.remark || '',
    })
    setTypeFormId(row.id)
    setShowTypeForm(true)
  }

  async function onSaveType() {
    if (!typeForm.typeCode.trim() || !typeForm.typeName.trim()) {
      setError('类型编码和类型名称不能为空')
      return
    }
    try {
      const payload: DictTypePayload = {
        ...typeForm,
        typeCode: typeForm.typeCode.trim().toUpperCase(),
        typeName: typeForm.typeName.trim(),
      }
      const res = typeFormId ? await updateDictType(typeFormId, payload) : await createDictType(payload)
      if (res.code !== 0) {
        setError(res.msg || '保存失败')
        return
      }
      setShowTypeForm(false)
      await loadTypes(page)
    } catch {
      setError('保存失败')
    }
  }

  async function onDeleteType(id: number) {
    if (!window.confirm('确认删除该字典类型及其枚举值？')) return
    try {
      const res = await deleteDictType(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      if (selectedType?.id === id) {
        setSelectedType(null)
      }
      await loadTypes(page)
    } catch {
      setError('删除失败')
    }
  }

  function openCreateItem() {
    if (!selectedType) return
    setItemForm({ ...emptyItemForm, typeCode: selectedType.typeCode })
    setItemFormId(null)
    setShowItemForm(true)
  }

  function openEditItem(row: DictItemRow) {
    setItemForm({
      typeCode: row.typeCode,
      value: row.value,
      label: row.label,
      labelEn: row.labelEn || '',
      color: row.color || '',
      extra: row.extra || '',
      sortOrder: row.sortOrder,
      status: row.status,
      isDefault: row.isDefault,
      remark: row.remark || '',
    })
    setItemFormId(row.id)
    setShowItemForm(true)
  }

  async function onSaveItem() {
    if (!selectedType) return
    if (!itemForm.value.trim() || !itemForm.label.trim()) {
      setError('枚举值和值名称不能为空')
      return
    }
    try {
      const payload: DictItemPayload = {
        ...itemForm,
        typeCode: selectedType.typeCode,
        value: itemForm.value.trim().toUpperCase(),
        label: itemForm.label.trim(),
      }
      const res = itemFormId ? await updateDictItem(itemFormId, payload) : await createDictItem(payload)
      if (res.code !== 0) {
        setError(res.msg || '保存失败')
        return
      }
      setShowItemForm(false)
      await loadItems(selectedType.typeCode)
    } catch {
      setError('保存失败')
    }
  }

  async function onDeleteItem(id: number) {
    if (!window.confirm('确认删除该枚举值？')) return
    try {
      const res = await deleteDictItem(id)
      if (res.code !== 0) {
        setError(res.msg || '删除失败')
        return
      }
      if (selectedType) {
        await loadItems(selectedType.typeCode)
      }
    } catch {
      setError('删除失败')
    }
  }

  async function onQuickSort(item: DictItemRow, delta: -1 | 1) {
    const target = items.find((row) => row.id === item.id)
    if (!target) return
    const nextSort = Math.max(0, target.sortOrder + delta)
    try {
      const res = await batchSortDictItems({ items: [{ id: target.id, sortOrder: nextSort }] })
      if (res.code !== 0) {
        setError(res.msg || '排序失败')
        return
      }
      if (selectedType) {
        await loadItems(selectedType.typeCode)
      }
    } catch {
      setError('排序失败')
    }
  }

  if (!canList) {
    return (
      <section className="panel page-panel">
        <h2>枚举字典</h2>
        <p>你没有查看权限</p>
      </section>
    )
  }

  return (
    <section className="panel page-panel">
      <header className="section-head">
        <h2>枚举字典</h2>
        <div className="action-cell-inner">
          <input placeholder="类型关键字" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <button className="ghost-btn" onClick={() => void loadTypes(1)}>
            查询
          </button>
          {canCreate && (
            <button className="primary-btn" onClick={openCreateType}>
              新增类型
            </button>
          )}
          {canEdit && selectedType?.editable === 1 && (
            <button className="primary-btn" onClick={openCreateItem}>
              新增枚举值
            </button>
          )}
        </div>
      </header>

      {loading && <p>加载中...</p>}
      {error && <p className="error">{error}</p>}

      <div className="dict-layout">
        <aside className="dict-type-list">
          <h3>类型列表</h3>
          {types.length === 0 ? (
            <p>暂无数据</p>
          ) : (
            types.map((row) => (
              <div key={row.id} className={`dict-type-item ${selectedType?.id === row.id ? 'active' : ''}`}>
                <button type="button" className="dict-type-main" onClick={() => setSelectedType(row)}>
                  <strong>{row.typeCode}</strong>
                  <span>{row.typeName}</span>
                  <small>{row.source}</small>
                </button>
                <div className="action-cell-inner">
                  {canEdit && (
                    <button className="text-btn" onClick={() => openEditType(row)}>
                      编辑
                    </button>
                  )}
                  {canDelete && (
                    <button className="text-btn danger" onClick={() => void onDeleteType(row.id)}>
                      删除
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
          <footer className="table-footer">
            <span>共 {total} 条</span>
            <div className="pager">
              <button className="ghost-btn" disabled={page <= 1} onClick={() => void loadTypes(page - 1)}>
                上一页
              </button>
              <span className="pager-summary">
                第 {page} 页 / 每页 <PageSizeSelect value={size} onChange={(next) => setSize(next)} />
              </span>
              <button className="ghost-btn" disabled={page * size >= total} onClick={() => void loadTypes(page + 1)}>
                下一页
              </button>
            </div>
          </footer>
        </aside>

        <article className="dict-item-panel">
          <h3>{selectedType ? `${selectedType.typeCode} 枚举值` : '请选择类型'}</h3>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>排序</th>
                  <th>值</th>
                  <th>标签</th>
                  <th>颜色</th>
                  <th>状态</th>
                  <th>默认</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {items.length === 0 && (
                  <tr>
                    <td colSpan={7}>暂无数据</td>
                  </tr>
                )}
                {items.map((row) => (
                  <tr key={row.id}>
                    <td>{row.sortOrder}</td>
                    <td>{row.value}</td>
                    <td>{row.label}</td>
                    <td>{row.color || '-'}</td>
                    <td>{row.status === 1 ? '启用' : '禁用'}</td>
                    <td>{row.isDefault === 1 ? '是' : '否'}</td>
                    <td className="action-cell">
                      <div className="action-cell-inner">
                        {canEdit && selectedType?.editable === 1 && (
                          <>
                            <button className="text-btn" onClick={() => openEditItem(row)}>
                              编辑
                            </button>
                            <button className="text-btn" onClick={() => void onQuickSort(row, -1)}>
                              ↑
                            </button>
                            <button className="text-btn" onClick={() => void onQuickSort(row, 1)}>
                              ↓
                            </button>
                          </>
                        )}
                        {canDelete && selectedType?.editable === 1 && (
                          <button className="text-btn danger" onClick={() => void onDeleteItem(row.id)}>
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
        </article>
      </div>

      {showTypeForm && (
        <div className="drawer-mask">
          <aside className="drawer">
            <header className="drawer-head">
              <h3>{typeFormId ? '编辑类型' : '新增类型'}</h3>
              <button className="ghost-btn" onClick={() => setShowTypeForm(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              <label>类型编码</label>
              <input
                disabled={!!typeFormId}
                value={typeForm.typeCode}
                onChange={(e) => setTypeForm((prev) => ({ ...prev, typeCode: e.target.value }))}
              />
              <label>类型名称</label>
              <input value={typeForm.typeName} onChange={(e) => setTypeForm((prev) => ({ ...prev, typeName: e.target.value }))} />
              <label>来源</label>
              <select
                className="app-select-native"
                disabled={!!typeFormId && typeForm.source === 'BUILTIN'}
                value={typeForm.source}
                onChange={(e) => setTypeForm((prev) => ({ ...prev, source: (e.target.value as 'BUILTIN' | 'CUSTOM') || 'CUSTOM' }))}
              >
                <option value="CUSTOM">CUSTOM</option>
                <option value="BUILTIN">BUILTIN</option>
              </select>
              <label>可编辑</label>
              <select
                className="app-select-native"
                value={String(typeForm.editable)}
                onChange={(e) => setTypeForm((prev) => ({ ...prev, editable: Number(e.target.value) as 0 | 1 }))}
              >
                <option value="1">是</option>
                <option value="0">否</option>
              </select>
              <label>状态</label>
              <select
                className="app-select-native"
                value={String(typeForm.status)}
                onChange={(e) => setTypeForm((prev) => ({ ...prev, status: Number(e.target.value) as 0 | 1 }))}
              >
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
              <label>排序</label>
              <input
                type="number"
                value={typeForm.sortOrder}
                onChange={(e) => setTypeForm((prev) => ({ ...prev, sortOrder: Number(e.target.value) || 0 }))}
              />
              <label>备注</label>
              <textarea rows={3} value={typeForm.remark} onChange={(e) => setTypeForm((prev) => ({ ...prev, remark: e.target.value }))} />
            </div>
            <footer className="drawer-footer">
              <button className="ghost-btn" onClick={() => setShowTypeForm(false)}>
                取消
              </button>
              <button className="primary-btn" onClick={() => void onSaveType()}>
                保存
              </button>
            </footer>
          </aside>
        </div>
      )}

      {showItemForm && (
        <div className="drawer-mask">
          <aside className="drawer">
            <header className="drawer-head">
              <h3>{itemFormId ? '编辑枚举值' : '新增枚举值'}</h3>
              <button className="ghost-btn" onClick={() => setShowItemForm(false)}>
                关闭
              </button>
            </header>
            <div className="form-grid">
              <label>枚举值</label>
              <input value={itemForm.value} onChange={(e) => setItemForm((prev) => ({ ...prev, value: e.target.value }))} />
              <label>显示名称</label>
              <input value={itemForm.label} onChange={(e) => setItemForm((prev) => ({ ...prev, label: e.target.value }))} />
              <label>颜色</label>
              <input value={itemForm.color || ''} onChange={(e) => setItemForm((prev) => ({ ...prev, color: e.target.value }))} />
              <label>状态</label>
              <select
                className="app-select-native"
                value={String(itemForm.status)}
                onChange={(e) => setItemForm((prev) => ({ ...prev, status: Number(e.target.value) as 0 | 1 }))}
              >
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
              <label>默认值</label>
              <select
                className="app-select-native"
                value={String(itemForm.isDefault)}
                onChange={(e) => setItemForm((prev) => ({ ...prev, isDefault: Number(e.target.value) as 0 | 1 }))}
              >
                <option value="0">否</option>
                <option value="1">是</option>
              </select>
              <label>排序</label>
              <input
                type="number"
                value={itemForm.sortOrder}
                onChange={(e) => setItemForm((prev) => ({ ...prev, sortOrder: Number(e.target.value) || 0 }))}
              />
              <label>备注</label>
              <textarea rows={3} value={itemForm.remark || ''} onChange={(e) => setItemForm((prev) => ({ ...prev, remark: e.target.value }))} />
            </div>
            <footer className="drawer-footer">
              <button className="ghost-btn" onClick={() => setShowItemForm(false)}>
                取消
              </button>
              <button className="primary-btn" onClick={() => void onSaveItem()}>
                保存
              </button>
            </footer>
          </aside>
        </div>
      )}
    </section>
  )
}
