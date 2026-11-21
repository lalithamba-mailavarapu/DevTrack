
import AuthGuard from '@/components/auth-guard'
import { AppShell } from '@/components/devtrack/app-shell'

export default function HomePage() {
  return (
    <AuthGuard>
      <AppShell />
    </AuthGuard>
  )
}
