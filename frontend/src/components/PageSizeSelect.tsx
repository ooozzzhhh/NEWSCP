import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

const DEFAULT_OPTIONS = [10, 20, 50]

export function PageSizeSelect({
  value,
  onChange,
  options = DEFAULT_OPTIONS,
}: {
  value: number
  onChange: (value: number) => void
  options?: number[]
}) {
  return (
    <Select value={String(value)} onValueChange={(next) => onChange(Number(next))}>
      <SelectTrigger className="page-size-trigger">
        <SelectValue />
      </SelectTrigger>
      <SelectContent className="page-size-content">
        {options.map((item) => (
          <SelectItem key={item} value={String(item)}>
            {item}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
