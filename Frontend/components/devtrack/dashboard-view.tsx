'use client'

import {
  Activity,
  ArrowRight,
  BarChart3,
  CheckCircle2,
  CircleDot,
  FolderKanban,
  Layers3,
  ShieldCheck,
  Timer,
  type LucideIcon,
} from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import type { AppSection } from './navigation'

type DashboardViewProps = {
  apiBaseUrlConfigured: boolean
  onNavigate: (section: AppSection) => void
}

type Metric = {
  label: string
  icon: LucideIcon
}

const metrics: Metric[] = [
  { label: 'Total projects', icon: FolderKanban },
  { label: 'Open issues', icon: CircleDot },
  { label: 'In progress', icon: Timer },
  { label: 'Closed issues', icon: CheckCircle2 },
]

const issueStatuses = [
  { label: 'OPEN', color: 'bg-chart-1' },
  { label: 'IN_PROGRESS', color: 'bg-chart-2' },
  { label: 'CLOSED', color: 'bg-chart-3' },
]

function MetricCard({ label, icon: Icon }: Metric) {
  return (
    <Card className="border-border/80 shadow-sm shadow-slate-900/[0.025]">
      <CardHeader className="flex flex-row items-center justify-between p-5 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
        <span className="grid size-9 place-items-center rounded-xl bg-primary/8 text-primary">
          <Icon aria-hidden="true" />
        </span>
      </CardHeader>
      <CardContent className="px-5 pb-5 pt-0">
        <p className="text-3xl font-semibold tracking-tight text-foreground">—</p>
        <p className="mt-1 text-xs text-muted-foreground">Waiting for authenticated API data</p>
      </CardContent>
    </Card>
  )
}

export function DashboardView({ apiBaseUrlConfigured, onNavigate }: DashboardViewProps) {
  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-5 rounded-3xl border border-border/80 bg-gradient-to-br from-card via-card to-primary/[0.045] p-6 shadow-sm shadow-slate-900/[0.025] sm:flex-row sm:items-end sm:justify-between sm:p-8">
        <div className="max-w-2xl">
          <div className="mb-3 flex items-center gap-2">
            <span className="size-2 rounded-full bg-primary" aria-hidden="true" />
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace overview</p>
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">Your engineering workspace</h1>
          <p className="mt-3 max-w-xl text-sm leading-relaxed text-muted-foreground sm:text-base">
            A focused home for the projects you build, the issues you solve, and the technologies behind them.
          </p>
        </div>
        <Button variant="outline" onClick={() => onNavigate('projects')} className="shrink-0">
          Browse projects
          <ArrowRight data-icon="inline-end" />
        </Button>
      </section>

      <Card className="overflow-hidden border-primary/15 shadow-sm shadow-slate-900/[0.025]">
        <CardContent className="flex flex-col gap-4 border-l-[3px] border-l-primary bg-gradient-to-r from-primary/[0.035] to-transparent p-5 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <div className="flex items-start gap-4">
            <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-primary/10 text-primary">
              <ShieldCheck aria-hidden="true" />
            </span>
            <div className="flex flex-col gap-1">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-sm font-semibold text-foreground">Backend connection</h2>
                <Badge variant="outline">{apiBaseUrlConfigured ? 'URL configured' : 'Configuration required'}</Badge>
              </div>
              <p className="max-w-3xl text-sm leading-relaxed text-muted-foreground">
                {apiBaseUrlConfigured
                  ? 'The API base URL is set. Live requests are intentionally paused until the real authentication and DTO contracts are confirmed.'
                  : 'Set NEXT_PUBLIC_API_BASE_URL to the deployed Spring Boot service. No sample data or simulated API results are shown.'}
              </p>
            </div>
          </div>
          <Badge variant="secondary" className="w-fit shrink-0">Awaiting verified API contract</Badge>
        </CardContent>
      </Card>

      <section aria-label="Workspace metrics" className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {metrics.map((metric) => (
          <MetricCard key={metric.label} {...metric} />
        ))}
      </section>

      <section aria-label="Project and issue summaries" className="grid gap-5 xl:grid-cols-[1.35fr_0.85fr]">
        <Card className="border-border/80 shadow-sm shadow-slate-900/[0.025]">
          <CardHeader className="flex flex-row items-start justify-between gap-3 p-5 sm:p-6">
            <div className="flex flex-col gap-1">
              <CardTitle className="text-base">Issue activity</CardTitle>
              <CardDescription>Daily issue volume across your workspace</CardDescription>
            </div>
            <Badge variant="outline">No data loaded</Badge>
          </CardHeader>
          <CardContent className="px-5 pb-5 sm:px-6 sm:pb-6">
            <div
              className="empty-chart-grid flex h-56 items-center justify-center rounded-2xl border border-dashed border-border/80"
              role="img"
              aria-label="Issue activity chart is waiting for real issue data"
            >
              <div className="flex flex-col items-center gap-2 rounded-xl bg-card/90 px-5 py-4 text-center shadow-sm backdrop-blur-sm">
                <span className="grid size-9 place-items-center rounded-xl bg-primary/8 text-primary">
                  <BarChart3 aria-hidden="true" />
                </span>
                <p className="text-sm font-medium text-foreground">Your activity chart will appear here</p>
                <p className="max-w-xs text-xs leading-relaxed text-muted-foreground">
                  Issue history will be charted after authenticated data is available.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-border/80 shadow-sm shadow-slate-900/[0.025]">
          <CardHeader className="flex flex-row items-start justify-between gap-3 p-5 sm:p-6">
            <div className="flex flex-col gap-1">
              <CardTitle className="text-base">Issue status</CardTitle>
              <CardDescription>Current distribution by workflow state</CardDescription>
            </div>
            <span className="grid size-9 place-items-center rounded-xl bg-muted text-muted-foreground">
              <Activity aria-hidden="true" />
            </span>
          </CardHeader>
          <CardContent className="flex flex-col gap-4 px-5 pb-5 sm:px-6 sm:pb-6">
            {issueStatuses.map((status) => (
              <div className="flex items-center gap-3" key={status.label}>
                <span aria-hidden="true" className={`size-2 rounded-full ${status.color}`} />
                <span className="flex-1 text-xs font-medium tracking-wide text-muted-foreground">{status.label}</span>
                <span className="text-sm font-semibold tabular-nums text-foreground">—</span>
              </div>
            ))}
            <div className="mt-1 rounded-xl bg-muted/70 p-3">
              <p className="text-xs leading-relaxed text-muted-foreground">
                Counts are withheld until they can be derived from the backend, not estimated or hardcoded.
              </p>
            </div>
          </CardContent>
        </Card>
      </section>

      <Card className="border-border/80 shadow-sm shadow-slate-900/[0.025]">
        <CardHeader className="flex flex-row items-start justify-between gap-3 p-5 sm:p-6">
          <div className="flex flex-col gap-1">
            <CardTitle className="text-base">Recent projects</CardTitle>
            <CardDescription>Your latest work across any language or technology stack</CardDescription>
          </div>
          <Button variant="ghost" size="sm" onClick={() => onNavigate('projects')}>
            View all
            <ArrowRight data-icon="inline-end" />
          </Button>
        </CardHeader>
        <CardContent className="px-5 pb-6 sm:px-6">
          <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border/80 bg-muted/25 px-6 py-10 text-center">
            <span className="mb-4 grid size-11 place-items-center rounded-2xl bg-card text-primary shadow-sm">
              <Layers3 aria-hidden="true" />
            </span>
            <h3 className="text-sm font-semibold text-foreground">No project data loaded</h3>
            <p className="mt-1 max-w-md text-xs leading-relaxed text-muted-foreground">
              Once you sign in and the project response contract is verified, your real projects will appear here.
            </p>
            <Button variant="outline" size="sm" onClick={() => onNavigate('projects')} className="mt-4">
              Open projects
              <ArrowRight data-icon="inline-end" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
