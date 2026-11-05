import AuthGuard from '@/components/auth-guard'
import { AppShell } from '@/components/devtrack/app-shell'

export default function HomePage() {
  const apiBaseUrlConfigured = Boolean(
    process.env.NEXT_PUBLIC_API_BASE_URL?.trim(),
  )

  return (
    <AuthGuard>
      <AppShell apiBaseUrlConfigured={apiBaseUrlConfigured} />
    </AuthGuard>
  )
}
