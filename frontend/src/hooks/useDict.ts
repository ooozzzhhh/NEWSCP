import { useEffect, useMemo, useState } from 'react'
import { fetchDictDropdown } from '@/api/system'
import type { DictDropdownMap, DictOption } from '@/types/system'

const dictCache = new Map<string, DictOption[]>()

export function useDict(...typeCodes: string[]) {
  const normalizedCodes = useMemo(
    () =>
      typeCodes
        .filter((item) => !!item?.trim())
        .map((item) => item.trim().toUpperCase())
        .filter((item, idx, arr) => arr.indexOf(item) === idx)
        .sort(),
    [typeCodes],
  )
  const [dictMap, setDictMap] = useState<DictDropdownMap>({})

  useEffect(() => {
    if (normalizedCodes.length === 0) {
      setDictMap({})
      return
    }

    const cached: DictDropdownMap = {}
    const missing: string[] = []
    normalizedCodes.forEach((code) => {
      if (dictCache.has(code)) {
        cached[code] = dictCache.get(code) || []
      } else {
        missing.push(code)
      }
    })
    setDictMap(cached)

    if (missing.length === 0) {
      return
    }

    fetchDictDropdown(missing)
      .then((res) => {
        if (res.code !== 0 || !res.data) {
          return
        }
        const next: DictDropdownMap = { ...cached }
        missing.forEach((code) => {
          const options = res.data[code] || []
          dictCache.set(code, options)
          next[code] = options
        })
        setDictMap(next)
      })
      .catch(() => {
        // ignore network errors and keep cached values
      })
  }, [normalizedCodes])

  return dictMap
}

