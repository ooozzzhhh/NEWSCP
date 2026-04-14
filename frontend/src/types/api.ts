export interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}
