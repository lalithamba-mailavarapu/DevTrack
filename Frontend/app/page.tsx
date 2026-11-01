import { AppShell } from '@/components/devtrack/app-shell'
import AuthGuard from '@/components/auth-guard'

export default function Page() {
  return (
    <AuthGuard>
      <AppShell
        apiBaseUrlConfigured={Boolean(
          process.env.NEXT_PUBLIC_API_BASE_URL?.trim()
        )}
      />
    </AuthGuard>
  )
}