'use client'

import { useCallback, useEffect, useState } from 'react'
import {
  Activity, ArrowRight, BarChart3, CheckCircle2, CircleDot,
  FolderKanban, RefreshCw, ShieldCheck, Timer, type LucideIcon,
} from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { getApiErrorMessage } from '@/lib/api/client'
import { getProjects, type ProjectResponse } from '@/lib/api/projects'
import { getIssues, type IssueResponse, type IssueStatus } from '@/lib/api/issues'
import type { AppSection } from './navigation'

type DashboardViewProps = {
  apiBaseUrlConfigured: boolean
  onNavigate: (section: AppSection) => void
}

type Metrics = { projects: number; open: number; inProgress: number; closed: number }

const metricDefinitions: { key: keyof Metrics; label: string; icon: LucideIcon }[] = [
  { key: 'projects', label: 'Total projects', icon: FolderKanban },
  { key: 'open', label: 'Open issues', icon: CircleDot },
  { key: 'inProgress', label: 'In progress', icon: Timer },
  { key: 'closed', label: 'Closed issues', icon: CheckCircle2 },
]

export function DashboardView({ apiBaseUrlConfigured, onNavigate }: DashboardViewProps) {
  const [projects, setProjects] = useState<ProjectResponse[]>([])
  const [metrics, setMetrics] = useState<Metrics>({ projects: 0, open: 0, inProgress: 0, closed: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [issueDataComplete, setIssueDataComplete] = useState(true)

  const loadDashboard = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const projectData = await getProjects()
      setProjects(projectData)

      const counts: Metrics = {
        projects: projectData.length,
        open: 0,
        inProgress: 0,
        closed: 0,
      }
      let complete = true

      // The API returns paginated issue data. Fetch every page (maximum size: 100)
      // so the dashboard totals are not limited to the first page.
      const perProjectResults = await Promise.all(projectData.map(async (project) => {
        const all: IssueResponse[] = []
        let page = 0
        let totalPages = 1
        try {
          while (page < totalPages) {
            const result = await getIssues(project.id, page, 100)
            all.push(...result.content)
            totalPages = result.totalPages
            page += 1
          }
          return { issues: all, failed: false }
        } catch {
          return { issues: [] as IssueResponse[], failed: true }
        }
      }))

      for (const result of perProjectResults) {
        if (result.failed) {
          complete = false
          continue
        }
        for (const issue of result.issues) {
          if (issue.status === 'OPEN') counts.open += 1
          else if (issue.status === 'IN_PROGRESS') counts.inProgress += 1
          else if (issue.status === 'CLOSED') counts.closed += 1
        }
      }

      setMetrics(counts)
      setIssueDataComplete(complete)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { void loadDashboard() }, [loadDashboard])

  const statusRows: { status: IssueStatus; label: string; count: number; color: string }[] = [
    { status: 'OPEN', label: 'OPEN', count: metrics.open, color: 'bg-chart-1' },
    { status: 'IN_PROGRESS', label: 'IN PROGRESS', count: metrics.inProgress, color: 'bg-chart-2' },
    { status: 'CLOSED', label: 'CLOSED', count: metrics.closed, color: 'bg-chart-3' },
  ]

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-5 rounded-3xl border border-border/80 bg-gradient-to-br from-card via-card to-primary/[0.045] p-6 shadow-sm sm:flex-row sm:items-end sm:justify-between sm:p-8">
        <div className="max-w-2xl">
          <div className="mb-3 flex items-center gap-2">
            <span className="size-2 rounded-full bg-primary" aria-hidden="true" />
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace overview</p>
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">Your engineering workspace</h1>
          <p className="mt-3 max-w-xl text-sm leading-relaxed text-muted-foreground sm:text-base">
            A live summary of your projects and the issues tracked across them.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => void loadDashboard()} disabled={loading}>
            <RefreshCw data-icon="inline-start" /> Refresh
          </Button>
          <Button onClick={() => onNavigate('projects')} className="shrink-0">
            Browse projects <ArrowRight data-icon="inline-end" />
          </Button>
        </div>
      </section>

      <Card className="overflow-hidden border-primary/15 shadow-sm">
        <CardContent className="flex flex-col gap-4 border-l-[3px] border-l-primary bg-gradient-to-r from-primary/[0.035] to-transparent p-5 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <div className="flex items-start gap-4">
            <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-primary/10 text-primary"><ShieldCheck aria-hidden="true" /></span>
            <div className="flex flex-col gap-1">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-sm font-semibold">Backend connection</h2>
                <Badge variant="outline">{apiBaseUrlConfigured ? 'URL configured' : 'Configuration required'}</Badge>
              </div>
              <p className="max-w-3xl text-sm leading-relaxed text-muted-foreground">
                {apiBaseUrlConfigured
                  ? 'Dashboard metrics are calculated from your authenticated project and issue API responses.'
                  : 'Set NEXT_PUBLIC_API_BASE_URL to your deployed Spring Boot service.'}
              </p>
            </div>
          </div>
          <Badge variant="secondary" className="w-fit shrink-0">Live API data</Badge>
        </CardContent>
      </Card>

      {error && <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">{error}</div>}
      {!issueDataComplete && !loading && <div role="status" className="rounded-xl border border-border bg-muted/40 p-4 text-sm text-muted-foreground">Some project issue lists could not be loaded. Issue totals are partial; check project access or refresh.</div>}

      <section aria-label="Workspace metrics" className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {metricDefinitions.map(({ key, label, icon: Icon }) => (
          <Card key={key} className="border-border/80 shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between p-5 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
              <span className="grid size-9 place-items-center rounded-xl bg-primary/8 text-primary"><Icon aria-hidden="true" /></span>
            </CardHeader>
            <CardContent className="px-5 pb-5 pt-0">
              <p className="text-3xl font-semibold tracking-tight">{loading ? '…' : metrics[key]}</p>
              <p className="mt-1 text-xs text-muted-foreground">{loading ? 'Loading authenticated data' : 'From backend responses'}</p>
            </CardContent>
          </Card>
        ))}
      </section>

      <section aria-label="Project and issue summaries" className="grid gap-5 xl:grid-cols-[1.35fr_0.85fr]">
        <Card className="border-border/80 shadow-sm">
          <CardHeader className="flex flex-row items-start justify-between gap-3 p-5 sm:p-6">
            <div><CardTitle className="text-base">Issue summary</CardTitle><CardDescription>Current issue distribution across accessible projects</CardDescription></div>
            <span className="grid size-9 place-items-center rounded-xl bg-muted text-muted-foreground"><Activity aria-hidden="true" /></span>
          </CardHeader>
          <CardContent className="flex flex-col gap-4 px-5 pb-5 sm:px-6 sm:pb-6">
            {statusRows.map((row) => (
              <div className="flex items-center gap-3" key={row.status}>
                <span aria-hidden="true" className={`size-2 rounded-full ${row.color}`} />
                <span className="flex-1 text-xs font-medium tracking-wide text-muted-foreground">{row.label}</span>
                <span className="text-sm font-semibold tabular-nums">{loading ? '—' : row.count}</span>
              </div>
            ))}
            <div className="rounded-xl bg-muted/70 p-3">
              <p className="text-xs leading-relaxed text-muted-foreground">Counts are calculated from paginated issue records. No synthetic activity data is displayed.</p>
            </div>
            <Button variant="outline" size="sm" onClick={() => onNavigate('issues')} className="self-start">Open issues <ArrowRight data-icon="inline-end" /></Button>
          </CardContent>
        </Card>

        <Card className="border-border/80 shadow-sm">
          <CardHeader className="flex flex-row items-start justify-between gap-3 p-5 sm:p-6">
            <div><CardTitle className="text-base">Recent projects</CardTitle><CardDescription>Your latest projects, across any technology stack</CardDescription></div>
            <Button variant="ghost" size="sm" onClick={() => onNavigate('projects')}>View all <ArrowRight data-icon="inline-end" /></Button>
          </CardHeader>
          <CardContent className="flex flex-col gap-3 px-5 pb-5 sm:px-6">
            {loading ? <p className="py-6 text-center text-sm text-muted-foreground">Loading projects...</p>
              : projects.length === 0 ? <p className="py-6 text-center text-sm text-muted-foreground">No projects yet. Create your first project.</p>
              : [...projects].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).slice(0, 5).map((project) => (
                <div key={project.id} className="flex items-start justify-between gap-3 rounded-xl border border-border/70 p-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{project.name}</p>
                    <p className="mt-1 truncate text-xs text-muted-foreground">{project.primaryLanguage || project.category || 'Project'}</p>
                  </div>
                  <Badge variant="outline">#{project.id}</Badge>
                </div>
              ))}
          </CardContent>
        </Card>
      </section>
    </div>
  )
}
