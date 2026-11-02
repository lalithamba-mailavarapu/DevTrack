'use client'

import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ArrowLeft, Plus, RefreshCw } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { getApiErrorMessage } from '@/lib/api/client'
import { getProjects, type ProjectResponse } from '@/lib/api/projects'
import {
  createIssue, getIssues, updateIssue,
  type IssueResponse, type IssueStatus, type IssuePriority, type IssueLabel,
} from '@/lib/api/issues'
import type { AppSection } from './navigation'

const statuses: IssueStatus[] = ['OPEN', 'IN_PROGRESS', 'CLOSED']
const priorities: IssuePriority[] = ['LOW', 'MEDIUM', 'HIGH']
const labels: IssueLabel[] = ['BUG', 'FEATURE', 'ENHANCEMENT', 'DOCUMENTATION', 'TASK']

export function IssuesSection({ onNavigate }: { onNavigate: (section: AppSection) => void }) {
  const [projects, setProjects] = useState<ProjectResponse[]>([])
  const [projectId, setProjectId] = useState('')
  const [issues, setIssues] = useState<IssueResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [status, setStatus] = useState('')
  const [priority, setPriority] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [label, setLabel] = useState<IssueLabel>('TASK')
  const [error, setError] = useState<string | null>(null)

  const loadProjects = useCallback(async () => {
    try {
      const data = await getProjects()
      setProjects(data)
      setProjectId((current) => current || (data[0] ? String(data[0].id) : ''))
    } catch (err) { setError(getApiErrorMessage(err)) }
  }, [])

  const loadIssues = useCallback(async () => {
    if (!projectId) { setIssues([]); return }
    setLoading(true); setError(null)
    try {
      const result = await getIssues(Number(projectId), page, 10, {
        status: status ? status as IssueStatus : undefined,
        priority: priority ? priority as IssuePriority : undefined,
      })
      setIssues(result.content)
      setTotalPages(result.totalPages)
    } catch (err) { setError(getApiErrorMessage(err)) }
    finally { setLoading(false) }
  }, [projectId, page, status, priority])

  useEffect(() => { void loadProjects() }, [loadProjects])
  useEffect(() => { void loadIssues() }, [loadIssues])

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!projectId) return
    setSaving(true); setError(null)
    try {
      await createIssue(Number(projectId), { title: title.trim(), description: description.trim() || undefined, label })
      setTitle(''); setDescription(''); setLabel('TASK'); setShowForm(false); setPage(0)
      await loadIssues()
    } catch (err) { setError(getApiErrorMessage(err)) }
    finally { setSaving(false) }
  }

  async function changeIssue(issue: IssueResponse, patch: { status?: IssueStatus; priority?: IssuePriority }) {
    if (!projectId) return
    setError(null)
    try {
      await updateIssue(Number(projectId), issue.id, patch)
      await loadIssues()
    } catch (err) { setError(getApiErrorMessage(err)) }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="flex flex-col gap-2">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace</p>
          <h1 className="text-3xl font-semibold tracking-tight">Issues</h1>
          <p className="max-w-2xl text-sm text-muted-foreground">Create, track, prioritize, and update issues for your projects.</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => void loadIssues()} disabled={loading}><RefreshCw data-icon="inline-start" /> Refresh</Button>
          <Button onClick={() => setShowForm((v) => !v)} disabled={!projectId}><Plus data-icon="inline-start" /> New issue</Button>
        </div>
      </div>

      {error && <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">{error}</div>}

      <Card><CardContent className="flex flex-wrap items-center gap-3 p-4">
        <label className="grid gap-1 text-xs text-muted-foreground">Project
          <select className="h-10 min-w-52 rounded-lg border border-input bg-background px-3 text-sm text-foreground" value={projectId} onChange={(e) => { setProjectId(e.target.value); setPage(0) }}>
            {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </label>
        <label className="grid gap-1 text-xs text-muted-foreground">Status
          <select className="h-10 rounded-lg border border-input bg-background px-3 text-sm text-foreground" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0) }}>
            <option value="">All statuses</option>{statuses.map((v) => <option key={v} value={v}>{v.replace('_', ' ')}</option>)}
          </select>
        </label>
        <label className="grid gap-1 text-xs text-muted-foreground">Priority
          <select className="h-10 rounded-lg border border-input bg-background px-3 text-sm text-foreground" value={priority} onChange={(e) => { setPriority(e.target.value); setPage(0) }}>
            <option value="">All priorities</option>{priorities.map((v) => <option key={v} value={v}>{v}</option>)}
          </select>
        </label>
      </CardContent></Card>

      {showForm && <Card><CardHeader><CardTitle className="text-base">Create issue</CardTitle><CardDescription>Fields follow your backend CreateIssueRequest.</CardDescription></CardHeader><CardContent>
        <form className="grid gap-4 md:grid-cols-2" onSubmit={(e) => { void handleCreate(e) }}>
          <label className="grid gap-1.5 text-sm md:col-span-2">Title *
            <Input required maxLength={200} value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Describe the issue" />
          </label>
          <label className="grid gap-1.5 text-sm md:col-span-2">Description
            <textarea className="min-h-24 rounded-lg border border-input bg-transparent px-3 py-2 text-sm" maxLength={5000} value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>
          <label className="grid gap-1.5 text-sm">Label
            <select className="h-10 rounded-lg border border-input bg-background px-3 text-sm" value={label} onChange={(e) => setLabel(e.target.value as IssueLabel)}>{labels.map((v) => <option key={v} value={v}>{v}</option>)}</select>
          </label>
          <div className="flex items-end justify-end gap-2"><Button type="button" variant="outline" onClick={() => setShowForm(false)}>Cancel</Button><Button type="submit" disabled={saving || !title.trim()}>{saving ? 'Creating...' : 'Create issue'}</Button></div>
        </form>
      </CardContent></Card>}

      {loading ? <Card><CardContent className="p-8 text-center text-sm text-muted-foreground">Loading issues...</CardContent></Card>
      : issues.length === 0 ? <Card><CardContent className="p-10 text-center"><h2 className="font-semibold">No issues found</h2><p className="mt-1 text-sm text-muted-foreground">Choose a project, adjust filters, or create an issue.</p></CardContent></Card>
      : <div className="grid gap-3">{issues.map((issue) => <Card key={issue.id}><CardContent className="flex flex-col gap-3 p-4">
        <div className="flex flex-wrap items-start justify-between gap-2"><div><h3 className="font-semibold">{issue.title}</h3><p className="mt-1 text-sm text-muted-foreground">{issue.description || 'No description'} </p></div><Badge variant="outline">#{issue.id}</Badge></div>
        <div className="flex flex-wrap items-center gap-2"><Badge variant="secondary">{issue.label || 'UNLABELLED'}</Badge><span className="text-xs text-muted-foreground">Reporter #{issue.reporterId}</span>{issue.assigneeId && <span className="text-xs text-muted-foreground">Assignee #{issue.assigneeId}</span>}</div>
        <div className="flex flex-wrap gap-3">
          <label className="grid gap-1 text-xs text-muted-foreground">Status<select className="h-9 rounded-lg border border-input bg-background px-2 text-sm text-foreground" value={issue.status} onChange={(e) => void changeIssue(issue, { status: e.target.value as IssueStatus })}>{statuses.map((v) => <option key={v} value={v}>{v.replace('_', ' ')}</option>)}</select></label>
          <label className="grid gap-1 text-xs text-muted-foreground">Priority<select className="h-9 rounded-lg border border-input bg-background px-2 text-sm text-foreground" value={issue.priority} onChange={(e) => void changeIssue(issue, { priority: e.target.value as IssuePriority })}>{priorities.map((v) => <option key={v} value={v}>{v}</option>)}</select></label>
        </div>
      </CardContent></Card>)}</div>}

      <div className="flex items-center justify-between"><Button variant="ghost" size="sm" onClick={() => onNavigate('overview')}><ArrowLeft data-icon="inline-start" /> Back to overview</Button><div className="flex items-center gap-3 text-sm text-muted-foreground"><span>Page {page + 1} of {Math.max(totalPages, 1)}</span><Button size="sm" variant="outline" disabled={page <= 0 || loading} onClick={() => setPage((v) => v - 1)}>Previous</Button><Button size="sm" variant="outline" disabled={page + 1 >= totalPages || loading} onClick={() => setPage((v) => v + 1)}>Next</Button></div></div>
    </div>
  )
}
