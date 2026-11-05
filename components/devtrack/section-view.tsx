'use client'

import { useCallback, useEffect, useState, type FormEvent } from 'react'
import {  ArrowLeft,  CircleHelp,  Code2,  Database,  FolderKanban,  Layers3,  Monitor,  Moon,  MoreHorizontal,  Plus,  RefreshCw,  ShieldAlert,  Sun,  Trash2,  Wrench,} from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { createProject, deleteProject, getProjects, type ProjectResponse, type TechnologyType } from '@/lib/api/projects'
import { sectionDetails, type AppSection } from './navigation'
import { IssuesSection } from './issues-section'
import { TeamSection } from './team-section'
import { useTheme } from 'next-themes'
import { apiClient, getApiErrorMessage } from '@/lib/api/client'

type SectionViewProps = {
  section: Exclude<AppSection, 'overview'>
  onNavigate: (section: AppSection) => void
}

const backendCoverage: Record<Exclude<AppSection, 'overview'>, string> = {
  projects: 'Projects are loaded from your authenticated Spring Boot API.',
  issues: 'The supplied API lists, creates, and patches project issues. Search, assignment, labels, and priority controls must match the actual DTOs before they are enabled.',
  technologies: 'Technologies are collected from your projects and grouped by type.',
  team: 'Project members can be listed, added, and removed. Only project owners can add or remove members; role changes are not supported.',
  profile: 'Your authenticated account email is loaded from the backend.',
  settings: 'Appearance preferences are managed locally in the browser.',
}

const missingApiSections = new Set<Exclude<AppSection, 'overview'>>()
const technologyTypes: TechnologyType[] = ['LANGUAGE', 'FRAMEWORK', 'DATABASE', 'TOOL', 'OTHER']

export function SectionView({ section, onNavigate }: SectionViewProps) {
  const details = sectionDetails[section]
  const [projects, setProjects] = useState<ProjectResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [deletingProjectId, setDeletingProjectId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [primaryLanguage, setPrimaryLanguage] = useState('')
  const [category, setCategory] = useState('')
  const [githubRepositoryUrl, setGithubRepositoryUrl] = useState('')
  const [technologyLines, setTechnologyLines] = useState('')
  const { theme, setTheme, systemTheme } = useTheme()
  const [themeMounted, setThemeMounted] = useState(false)
  const [profileEmail, setProfileEmail] = useState('')
  const [profileLoading, setProfileLoading] = useState(false)
  const [profileError, setProfileError] = useState<string | null>(null)

  useEffect(() => {
    setThemeMounted(true)
  }, [])

  const loadProjects = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getProjects()
      setProjects(data)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  async function handleDeleteProject(project: ProjectResponse) {
  const confirmed = window.confirm(
    `Are you sure you want to delete "${project.name}"? This action cannot be undone.`
  )

  if (!confirmed) return

  setDeletingProjectId(project.id)
  setError(null)

  try {
    await deleteProject(project.id)

    setProjects((current) =>
      current.filter((item) => item.id !== project.id)
    )
  } catch (err) {
    setError(getApiErrorMessage(err))
  } finally {
    setDeletingProjectId(null)
  }
}

  const loadProfile = useCallback(async () => {
  setProfileLoading(true)
  setProfileError(null)

  try {
    const response = await apiClient.get<string>('/api/auth/me', {
      responseType: 'text',
      headers: {
        Accept: 'text/plain',
      },
    })

    const email = response.data.trim()

    if (!email) {
      throw new Error('The backend returned an empty user identity.')
    }

    setProfileEmail(email)
  } catch (err) {
    setProfileError(getApiErrorMessage(err))
  } finally {
    setProfileLoading(false)
  }
}, [])


  useEffect(() => {
  if (section === 'projects' || section === 'technologies') {
    void loadProjects()
  }

  if (section === 'profile') {
    void loadProfile()
  }
}, [section, loadProjects, loadProfile])

  async function handleCreateProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const technologies = technologyLines
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
        .map((line) => {
          const separator = line.lastIndexOf('|')
          const technologyName = (separator >= 0 ? line.slice(0, separator) : line).trim()
          const requestedType = separator >= 0 ? line.slice(separator + 1).trim().toUpperCase() : 'OTHER'
          if (!technologyTypes.includes(requestedType as TechnologyType)) {
            throw new Error(`Invalid technology type "${requestedType}". Use LANGUAGE, FRAMEWORK, DATABASE, TOOL or OTHER.`)
          }
          if (!technologyName) throw new Error('Each technology needs a name.')
          return { technologyName, technologyType: requestedType as TechnologyType }
        })

      await createProject({
        name: name.trim(),
        description: description.trim() || undefined,
        primaryLanguage: primaryLanguage.trim() || undefined,
        category: category.trim() || undefined,
        githubRepositoryUrl: githubRepositoryUrl.trim() || undefined,
        technologies,
      })
      setName('')
      setDescription('')
      setPrimaryLanguage('')
      setCategory('')
      setGithubRepositoryUrl('')
      setTechnologyLines('')
      setShowForm(false)
      await loadProjects()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (section === 'projects') {
    return (
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex flex-col gap-2">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace</p>
            <h1 className="text-3xl font-semibold tracking-tight text-foreground">{details.title}</h1>
            <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">{details.description}</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => void loadProjects()} disabled={loading || saving}>
              <RefreshCw data-icon="inline-start" /> Refresh
            </Button>
            <Button onClick={() => setShowForm((current) => !current)}>
              <Plus data-icon="inline-start" /> {showForm ? 'Close form' : 'New project'}
            </Button>
          </div>
        </div>

        {error && <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">{error}</div>}

        {showForm && (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Create a project</CardTitle>
              <CardDescription>Details will be submitted to your Spring Boot backend.</CardDescription>
            </CardHeader>
            <CardContent>
              <form className="grid gap-4 md:grid-cols-2" onSubmit={(event) => { void handleCreateProject(event) }}>
                <label className="grid gap-1.5 text-sm md:col-span-2">Project name *
                  <Input required maxLength={100} value={name} onChange={(event) => setName(event.target.value)} placeholder="e.g. DevTrack" />
                </label>
                <label className="grid gap-1.5 text-sm md:col-span-2">Description
                  <textarea className="min-h-20 rounded-lg border border-input bg-transparent px-3 py-2 text-sm" maxLength={2000} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="What is this project about?" />
                </label>
                <label className="grid gap-1.5 text-sm">Primary language
                  <Input maxLength={50} value={primaryLanguage} onChange={(event) => setPrimaryLanguage(event.target.value)} placeholder="Java, Python, TypeScript..." />
                </label>
                <label className="grid gap-1.5 text-sm">Category
                  <Input maxLength={50} value={category} onChange={(event) => setCategory(event.target.value)} placeholder="Web app, API, mobile..." />
                </label>
                <label className="grid gap-1.5 text-sm md:col-span-2">GitHub repository URL
                  <Input type="url" maxLength={500} value={githubRepositoryUrl} onChange={(event) => setGithubRepositoryUrl(event.target.value)} placeholder="https://github.com/username/repository" />
                </label>
                <label className="grid gap-1.5 text-sm md:col-span-2">Technologies <span className="text-xs text-muted-foreground">One per line, format: technology name | TYPE. Example: Spring Boot | FRAMEWORK</span>
                  <textarea className="min-h-24 rounded-lg border border-input bg-transparent px-3 py-2 font-mono text-xs" value={technologyLines} onChange={(event) => setTechnologyLines(event.target.value)} placeholder={'Java | LANGUAGE\nSpring Boot | FRAMEWORK\nPostgreSQL | DATABASE'} />
                </label>
                <div className="flex justify-end gap-2 md:col-span-2">
                  <Button type="button" variant="outline" onClick={() => setShowForm(false)} disabled={saving}>Cancel</Button>
                  <Button type="submit" disabled={saving || !name.trim()}>{saving ? 'Creating...' : 'Create project'}</Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}

        <div className="flex items-center justify-between">
          <p className="text-sm text-muted-foreground">{loading ? 'Loading projects...' : `${projects.length} project${projects.length === 1 ? '' : 's'}`}</p>
          <Badge variant="outline">Live backend data</Badge>
        </div>

        {loading && projects.length === 0 ? (
          <Card><CardContent className="p-8 text-center text-sm text-muted-foreground">Loading your projects...</CardContent></Card>
        ) : projects.length === 0 ? (
          <Card><CardContent className="flex flex-col items-center gap-3 p-10 text-center">
            <span className="grid size-12 place-items-center rounded-2xl bg-primary/8 text-primary"><FolderKanban /></span>
            <h2 className="font-semibold">No projects yet</h2>
            <p className="max-w-md text-sm text-muted-foreground">Create your first project. It will be saved by your backend and loaded again from the database.</p>
            <Button onClick={() => setShowForm(true)}><Plus data-icon="inline-start" /> Create your first project</Button>
          </CardContent></Card>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {projects.map((project) => (
              <Card key={project.id}>
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between gap-3">
                      <CardTitle className="text-base">{project.name}</CardTitle>

                      <details className="relative shrink-0">
                        <summary
                          aria-label={`Actions for ${project.name}`}
                          className="grid size-8 cursor-pointer list-none place-items-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground"
                        >
                          <MoreHorizontal className="size-5" />
                        </summary>

                        <div className="absolute right-0 top-9 z-50 min-w-40 rounded-lg border border-border bg-popover p-1 shadow-lg">
                          <button
                            type="button"
                            disabled={deletingProjectId === project.id}
                            onClick={(event) => {
                              event.currentTarget.closest('details')?.removeAttribute('open')
                              void handleDeleteProject(project)
                            }}
                            className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm text-destructive hover:bg-destructive/10 disabled:opacity-50"
                          >
                            <Trash2 className="size-4" />
                            {deletingProjectId === project.id
                              ? 'Deleting...'
                              : 'Delete project'}
                          </button>
                        </div>
                      </details>
                    </div>
                  <CardDescription className="line-clamp-3">{project.description || 'No description provided.'}</CardDescription>
                </CardHeader>
                <CardContent className="flex flex-col gap-3">
                  <div className="flex flex-wrap gap-2">
                    {project.primaryLanguage && <Badge variant="outline">{project.primaryLanguage}</Badge>}
                    {project.category && <Badge variant="outline">{project.category}</Badge>}
                  </div>
                  {project.technologies?.length > 0 && <div className="flex flex-wrap gap-1.5">{project.technologies.map((tech) => <Badge key={tech.id} variant="secondary">{tech.technologyName}</Badge>)}</div>}
                  {project.githubRepositoryUrl && <a className="truncate text-xs text-primary underline-offset-4 hover:underline" href={project.githubRepositoryUrl} target="_blank" rel="noreferrer">Open GitHub repository</a>}
                  <p className="text-xs text-muted-foreground">Created {project.createdAt ? new Date(project.createdAt).toLocaleDateString() : 'date unavailable'}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
        <Button variant="ghost" size="sm" onClick={() => onNavigate('overview')} className="self-start"><ArrowLeft data-icon="inline-start" /> Back to overview</Button>
      </div>
    )
  }

  if (section === 'technologies') {
    const technologyMap = new Map<string, { name: string; type: TechnologyType; projects: Set<string> }>()
    for (const project of projects) {
      for (const technology of project.technologies ?? []) {
        const name = technology.technologyName?.trim()
        if (!name) continue
        const key = `${technology.technologyType}:${name.toLocaleLowerCase()}`
        const existing = technologyMap.get(key) ?? { name, type: technology.technologyType, projects: new Set<string>() }
        existing.projects.add(project.name)
        technologyMap.set(key, existing)
      }
    }

    const groups: { type: TechnologyType; label: string; icon: typeof Code2 }[] = [
      { type: 'LANGUAGE', label: 'Languages', icon: Code2 },
      { type: 'FRAMEWORK', label: 'Frameworks', icon: Layers3 },
      { type: 'DATABASE', label: 'Databases', icon: Database },
      { type: 'TOOL', label: 'Tools', icon: Wrench },
      { type: 'OTHER', label: 'Others', icon: CircleHelp },
    ]
    const technologies = Array.from(technologyMap.values()).sort((a, b) => a.name.localeCompare(b.name))

    return (
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex flex-col gap-2">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace</p>
            <h1 className="text-3xl font-semibold tracking-tight text-foreground">{details.title}</h1>
            <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">{details.description}</p>
          </div>
          <Button variant="outline" onClick={() => void loadProjects()} disabled={loading}>
            <RefreshCw data-icon="inline-start" className={loading ? 'animate-spin' : ''} /> Refresh
          </Button>
        </div>

        {error && <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">{error}</div>}

        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
          <Card><CardContent className="p-5"><p className="text-sm text-muted-foreground">Unique technologies</p><p className="mt-2 text-3xl font-semibold">{technologies.length}</p></CardContent></Card>
          <Card><CardContent className="p-5"><p className="text-sm text-muted-foreground">Projects scanned</p><p className="mt-2 text-3xl font-semibold">{projects.length}</p></CardContent></Card>
          <Card><CardContent className="p-5"><p className="text-sm text-muted-foreground">Technology categories</p><p className="mt-2 text-3xl font-semibold">{groups.filter((group) => technologies.some((technology) => technology.type === group.type)).length}</p></CardContent></Card>
        </div>

        {loading && projects.length === 0 ? (
          <Card><CardContent className="p-8 text-center text-sm text-muted-foreground">Loading technologies from your projects...</CardContent></Card>
        ) : technologies.length === 0 ? (
          <Card><CardContent className="flex flex-col items-center gap-3 p-10 text-center">
            <span className="grid size-12 place-items-center rounded-2xl bg-primary/8 text-primary"><Code2 /></span>
            <h2 className="font-semibold">No technologies found</h2>
            <p className="max-w-md text-sm text-muted-foreground">Add technologies when creating a project. They will appear here automatically after the project is saved.</p>
            <Button onClick={() => onNavigate('projects')}><FolderKanban data-icon="inline-start" /> Go to projects</Button>
          </CardContent></Card>
        ) : (
          <div className="grid gap-5 xl:grid-cols-2">
            {groups.map((group) => {
              const items = technologies.filter((technology) => technology.type === group.type)
              if (!items.length) return null
              const GroupIcon = group.icon
              return (
                <Card key={group.type}>
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-2"><span className="grid size-9 place-items-center rounded-xl bg-primary/8 text-primary"><GroupIcon className="size-4" /></span><CardTitle className="text-base">{group.label}</CardTitle></div>
                      <Badge variant="secondary">{items.length}</Badge>
                    </div>
                  </CardHeader>
                  <CardContent className="flex flex-col gap-3">
                    {items.map((technology) => (
                      <div key={`${technology.type}:${technology.name}`} className="flex flex-col gap-2 rounded-xl border border-border/70 p-3 sm:flex-row sm:items-center sm:justify-between">
                        <span className="font-medium">{technology.name}</span>
                        <div className="flex flex-wrap gap-1.5">
                          {Array.from(technology.projects).sort().map((projectName) => <Badge key={projectName} variant="outline">{projectName}</Badge>)}
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>
              )
            })}
          </div>
        )}
        <p className="text-xs text-muted-foreground">Read-only view based on technology records returned by your projects API.</p>
        <Button variant="ghost" size="sm" onClick={() => onNavigate('overview')} className="self-start"><ArrowLeft data-icon="inline-start" /> Back to overview</Button>
      </div>
    )
  }

  if (section === 'issues') {
    return <IssuesSection onNavigate={onNavigate} />
  }

  if (section === 'team') {
    return <TeamSection onNavigate={onNavigate} />
  }

  if (section === 'profile') {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">
          Account
        </p>
        <h1 className="text-3xl font-semibold tracking-tight text-foreground">
          Profile
        </h1>
        <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">
          Information retrieved from your authenticated backend session.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Account information</CardTitle>
          <CardDescription>
            Details associated with your signed-in account.
          </CardDescription>
        </CardHeader>

        <CardContent className="flex flex-col gap-3">
          <div className="grid gap-1">
            <p className="text-sm font-medium">Authenticated email</p>

            {profileLoading ? (
              <p className="text-sm text-muted-foreground">
                Loading account information...
              </p>
            ) : profileError ? (
              <p role="alert" className="text-sm text-destructive">
                {profileError}
              </p>
            ) : (
              <p className="text-sm text-foreground">{profileEmail}</p>
            )}
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={() => void loadProfile()}
            disabled={profileLoading}
            className="self-start"
          >
            <RefreshCw
              data-icon="inline-start"
              className={profileLoading ? 'animate-spin' : ''}
            />
            Refresh profile
          </Button>
        </CardContent>
      </Card>

      <Button
        variant="ghost"
        size="sm"
        onClick={() => onNavigate('overview')}
        className="self-start"
      >
        <ArrowLeft data-icon="inline-start" />
        Back to overview
      </Button>
    </div>
  )
}


  if (section === 'settings') {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">
          Administration
        </p>
        <h1 className="text-3xl font-semibold tracking-tight text-foreground">
          Settings
        </h1>
        <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">
          Customize how DevTrack looks across your application.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Appearance</CardTitle>
          <CardDescription>
            Choose your preferred theme. Your selection is saved in this browser.
          </CardDescription>
        </CardHeader>

        <CardContent className="flex flex-col gap-4">
          <label htmlFor="theme-select" className="text-sm font-medium">
            Application theme
          </label>

          <select
            id="theme-select"
            value={themeMounted ? theme ?? 'system' : 'system'}
            onChange={(event) => setTheme(event.target.value)}
            disabled={!themeMounted}
            className="h-11 w-full max-w-sm rounded-lg border border-input bg-background px-3 text-sm text-foreground outline-none focus:ring-2 focus:ring-ring"
          >
            <option value="light">Light</option>
            <option value="dark">Dark</option>
            <option value="system">System default</option>
          </select>

          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            {theme === 'dark' ? (
              <Moon className="size-4" />
            ) : theme === 'light' ? (
              <Sun className="size-4" />
            ) : (
              <Monitor className="size-4" />
            )}

            <span>
              {!themeMounted
                ? 'Loading appearance preference...'
                : `Current appearance: ${
                    theme === 'system'
                      ? `System (${systemTheme ?? 'detecting'})`
                      : theme === 'dark'
                        ? 'Dark'
                        : 'Light'
                  }`}
            </span>
          </div>
        </CardContent>
      </Card>

      <p className="text-xs text-muted-foreground">
        Appearance preferences are stored locally and do not require a backend API.
      </p>

      <Button
        variant="ghost"
        size="sm"
        onClick={() => onNavigate('overview')}
        className="self-start"
      >
        <ArrowLeft data-icon="inline-start" />
        Back to overview
      </Button>
    </div>
  )
}

  const isMissingApi = missingApiSections.has(section)
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace</p>
        <h1 className="text-3xl font-semibold tracking-tight text-foreground">{details.title}</h1>
        <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">{details.description}</p>
      </div>
      <Card className="overflow-hidden border-border/80 shadow-sm shadow-slate-900/[0.025]">
        <CardHeader className="flex flex-row items-start gap-4 p-5 sm:p-7">
          <span className="grid size-11 shrink-0 place-items-center rounded-2xl bg-primary/8 text-primary">{isMissingApi ? <CircleHelp aria-hidden="true" /> : <ShieldAlert aria-hidden="true" />}</span>
          <div className="flex min-w-0 flex-col gap-2">
            <div className="flex flex-wrap items-center gap-2"><CardTitle className="text-base">Waiting for verified backend data</CardTitle><Badge variant={isMissingApi ? 'outline' : 'secondary'}>{isMissingApi ? 'Additional API required' : 'Contract required'}</Badge></div>
            <CardDescription className="max-w-2xl text-sm leading-relaxed">{backendCoverage[section]}</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="border-t border-border/70 bg-muted/25 px-5 py-5 sm:px-7">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"><p className="max-w-2xl text-xs leading-relaxed text-muted-foreground">DevTrack only displays backend data and does not simulate successful writes.</p><Button variant="outline" size="sm" onClick={() => onNavigate('overview')} className="shrink-0"><ArrowLeft data-icon="inline-start" /> Back to overview</Button></div>
        </CardContent>
      </Card>
    </div>
  )
}
