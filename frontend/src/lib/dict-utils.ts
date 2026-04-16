import type { DictOption } from '@/types/system'

export function getDictLabel(options: DictOption[] = [], value?: string) {
  if (!value) {
    return ''
  }
  const target = options.find((item) => item.value === value)
  return target?.label || value
}

export function getDictLabelAndColor(options: DictOption[] = [], value?: string) {
  if (!value) {
    return { label: '', color: undefined as string | undefined }
  }
  const target = options.find((item) => item.value === value)
  return {
    label: target?.label || value,
    color: target?.color,
  }
}

export function getDictDefault(options: DictOption[] = []) {
  return options.find((item) => item.isDefault === 1)?.value
}

