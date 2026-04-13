interface PlaceholderPageProps {
  title: string
}

export function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      <p>该模块占位页已创建，后续按领域逐步填充。</p>
    </section>
  )
}
