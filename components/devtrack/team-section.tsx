'use client'

import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ArrowLeft, RefreshCw, UserPlus, UserRound, UserX } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { getApiErrorMessage } from '@/lib/api/client'
import { getProjects, type ProjectResponse } from '@/lib/api/projects'
import { addProjectMember, getProjectMembers, removeProjectMember, type ProjectMember } from '@/lib/api/members'
import type { AppSection } from './navigation'

export function TeamSection({ onNavigate }: { onNavigate: (section: AppSection) => void }) {
  const [projects, setProjects] = useState<ProjectResponse[]>([])
  const [projectId, setProjectId] = useState('')
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [removingId, setRemovingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const loadProjects = useCallback(async () => {
    try {
      const result = await getProjects()
      setProjects(result)
      setProjectId((current) => current || (result[0] ? String(result[0].id) : ''))
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }, [])

  const loadMembers = useCallback(async () => {
    if (!projectId) { setMembers([]); return }
    setLoading(true)
    setError(null)
    try {
      setMembers(await getProjectMembers(Number(projectId)))
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => { void loadProjects() }, [loadProjects])
  useEffect(() => { void loadMembers() }, [loadMembers])

  async function handleAdd(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!projectId || !email.trim()) return
    setSaving(true); setError(null); setNotice(null)
    try {
      const added = await addProjectMember(Number(projectId), email.trim())
      setEmail('')
      setNotice(`${added.username} was added to this project.`)
      await loadMembers()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleRemove(member: ProjectMember) {
    if (!projectId) return
    const confirmed = window.confirm(`Remove ${member.username} from this project?`)
    if (!confirmed) return
    setRemovingId(member.userId); setError(null); setNotice(null)
    try {
      await removeProjectMember(Number(projectId), member.userId)
      setNotice(`${member.username} was removed from this project.`)
      await loadMembers()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="flex flex-col gap-2">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Administration</p>
          <h1 className="text-3xl font-semibold tracking-tight">Team</h1>
          <p className="max-w-2xl text-sm text-muted-foreground">View and manage members connected to each project.</p>
        </div>
        <Button variant="outline" onClick={() => void loadMembers()} disabled={loading || !projectId}>
          <RefreshCw data-icon="inline-start" /> Refresh
        </Button>
      </div>

      {error && <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">{error}</div>}
      {notice && <div role="status" className="rounded-xl border border-border bg-muted/40 p-4 text-sm">{notice}</div>}

      <Card><CardContent className="flex flex-wrap items-end gap-4 p-4">
        <label className="grid gap-1.5 text-xs text-muted-foreground">Select project
          <select className="h-10 min-w-56 rounded-lg border border-input bg-background px-3 text-sm text-foreground" value={projectId} onChange={(e) => setProjectId(e.target.value)}>
            {projects.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}
          </select>
        </label>
        <Badge variant="outline">{members.length} member{members.length === 1 ? '' : 's'}</Badge>
      </CardContent></Card>

      {!projectId ? (
        <Card><CardContent className="p-10 text-center"><h2 className="font-semibold">Create a project first</h2><p className="mt-1 text-sm text-muted-foreground">Team members are managed within individual projects.</p></CardContent></Card>
      ) : (
        <>
          <Card>
            <CardHeader><CardTitle className="text-base">Add a team member</CardTitle><CardDescription>The person must already have a registered DevTrack account. Only the project owner can add members.</CardDescription></CardHeader>
            <CardContent>
              <form className="flex flex-col gap-3 sm:flex-row" onSubmit={(e) => { void handleAdd(e) }}>
                <Input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Registered user's email" className="sm:max-w-md" />
                <Button type="submit" disabled={saving || !email.trim()}><UserPlus data-icon="inline-start" />{saving ? 'Adding...' : 'Add member'}</Button>
              </form>
            </CardContent>
          </Card>

          <div className="flex items-center justify-between"><h2 className="font-semibold">Project members</h2><span className="text-xs text-muted-foreground">Live backend data</span></div>
          {loading ? <Card><CardContent className="p-8 text-center text-sm text-muted-foreground">Loading members...</CardContent></Card>
          : members.length === 0 ? <Card><CardContent className="p-10 text-center"><UserRound className="mx-auto mb-3 size-8 text-muted-foreground" /><h3 className="font-semibold">No members returned</h3><p className="mt-1 text-sm text-muted-foreground">This project may currently have only its owner, or you may not have permission to view members.</p></CardContent></Card>
          : <div className="grid gap-3 md:grid-cols-2">{members.map((member) => <Card key={member.userId}><CardContent className="flex items-center justify-between gap-3 p-4">
              <div className="flex min-w-0 items-center gap-3"><span className="grid size-10 shrink-0 place-items-center rounded-full bg-primary/10 text-primary"><UserRound /></span><div className="min-w-0"><p className="truncate font-medium">{member.username}</p><p className="truncate text-sm text-muted-foreground">{member.email}</p><Badge className="mt-1" variant="secondary">{member.role}</Badge></div></div>
              <Button size="icon" variant="ghost" aria-label={`Remove ${member.username}`} title="Remove member" disabled={removingId === member.userId || member.role.toUpperCase() === 'OWNER'} onClick={() => void handleRemove(member)}><UserX /></Button>
            </CardContent></Card>)}</div>}
        </>
      )}
      <Button variant="ghost" size="sm" onClick={() => onNavigate('overview')} className="self-start"><ArrowLeft data-icon="inline-start" /> Back to overview</Button>
    </div>
  )
}
