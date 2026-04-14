import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="panel">
      <h2>页面不存在</h2>
      <p>你访问的地址无效，或当前账号没有该页面入口。</p>
      <Link className="inline-link" to="/app/dashboard">
        返回仪表盘
      </Link>
    </section>
  )
}
