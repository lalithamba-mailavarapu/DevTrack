'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { ArrowRight, ChevronRight, Command, Menu, Search, UserRound, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { DashboardView } from './dashboard-view'
import { navigationGroups, sectionDetails, type AppSection } from './navigation'
import { SectionView } from './section-view'
import { Sidebar } from './sidebar'
import { useAuth } from '@/lib/auth-context'

type AppShellProps = {
  apiBaseUrlConfigured: boolean
}

const searchableNavigation = navigationGroups.flatMap((group) => group.items)

export function AppShell({ apiBaseUrlConfigured }: AppShellProps) {
  const [activeSection, setActiveSection] = useState<AppSection>('overview')
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const { logout, user } = useAuth()
  const searchDialogRef = useRef<HTMLDialogElement>(null)
  const mobileNavigationRef = useRef<HTMLDialogElement>(null)

  const openSearch = useCallback(() => {
    const dialog = searchDialogRef.current
    if (dialog && !dialog.open) dialog.showModal()
  }, [])

  useEffect(() => {
    function handleGlobalKeyDown(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        openSearch()
      }
    }

    window.addEventListener('keydown', handleGlobalKeyDown)
    return () => window.removeEventListener('keydown', handleGlobalKeyDown)
  }, [openSearch])

  const filteredNavigation = useMemo(() => {
    const query = searchTerm.trim().toLowerCase()
    if (!query) return searchableNavigation

    return searchableNavigation.filter(
      (item) => item.label.toLowerCase().includes(query) || item.description.toLowerCase().includes(query),
    )
  }, [searchTerm])

  const navigateTo = (section: AppSection) => {
    setActiveSection(section)
    searchDialogRef.current?.close()
    mobileNavigationRef.current?.close()
  }

  const activeDetails = sectionDetails[activeSection]

  return (
    <div className="flex min-h-screen bg-background">
      <div className="hidden lg:block">
        <Sidebar
          activeSection={activeSection}
          collapsed={sidebarCollapsed}
          apiBaseUrlConfigured={apiBaseUrlConfigured}
          onNavigate={navigateTo}
          onToggleCollapsed={() => setSidebarCollapsed((current) => !current)}
        />
      </div>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-20 flex h-16 shrink-0 items-center justify-between gap-3 border-b border-border/80 bg-background/90 px-4 backdrop-blur-xl sm:px-6 lg:px-8">
          <div className="flex min-w-0 items-center gap-3">
            <Button
              variant="ghost"
              size="icon"
              className="lg:hidden"
              aria-label="Open navigation"
              onClick={() => mobileNavigationRef.current?.showModal()}
            >
              <Menu />
            </Button>
            <div className="flex min-w-0 items-center gap-2 text-sm">
              <span className="hidden text-muted-foreground sm:inline">DevTrack</span>
              <ChevronRight className="hidden text-muted-foreground/60 sm:block" aria-hidden="true" />
              <span className="truncate font-medium text-foreground" aria-current="page">{activeDetails.title}</span>
            </div>
          </div>

          <div className="flex shrink-0 items-center gap-2">
            <Button variant="outline" size="sm" className="hidden w-60 justify-between text-muted-foreground sm:inline-flex" onClick={openSearch}>
              <span className="flex items-center gap-2">
                <Search data-icon="inline-start" />
                Search workspace
              </span>
              <kbd className="flex items-center gap-1 rounded-md border border-border bg-muted/80 px-1.5 py-0.5 text-[10px] font-medium text-muted-foreground">
                <Command aria-hidden="true" />K
              </kbd>
            </Button>
            <Button variant="outline" size="icon" className="sm:hidden" aria-label="Search workspace" onClick={openSearch}>
              <Search />
            </Button>
            <div className="flex items-center gap-2">
              <span className="hidden text-sm text-muted-foreground md:inline">
                {user}
              </span>

                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Open profile"
                      title="Profile"
                      onClick={() => navigateTo('profile')}
                    >
                      <UserRound />
                    </Button>

                    <Button
                      variant="outline"
                      size="sm"
                      onClick={logout}
                    >
                      Logout
                    </Button>
             </div>
          </div>
        </header>

        <main className="mx-auto flex w-full max-w-[1480px] flex-1 flex-col px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
          {activeSection === 'overview' ? (
            <DashboardView apiBaseUrlConfigured={apiBaseUrlConfigured} onNavigate={navigateTo} />
          ) : (
            <SectionView section={activeSection} onNavigate={navigateTo} />
          )}
        </main>

        <footer className="flex min-h-12 items-center justify-between gap-4 border-t border-border/70 px-4 text-[11px] text-muted-foreground sm:px-6 lg:px-8">
          <span>DevTrack · Project and issue tracking</span>
          <span className="hidden sm:inline">Built for any stack</span>
        </footer>
      </div>

      <dialog
        ref={searchDialogRef}
        className="command-dialog"
        aria-labelledby="search-dialog-title"
        onClose={() => setSearchTerm('')}
      >
        <div className="p-4 sm:p-5">
          <div className="mb-4 flex items-start justify-between gap-4">
            <div>
              <h2 id="search-dialog-title" className="text-base font-semibold tracking-tight text-foreground">Search DevTrack</h2>
              <p className="mt-1 text-xs text-muted-foreground">Jump to a workspace section.</p>
            </div>
            <Button variant="ghost" size="icon-sm" aria-label="Close search" onClick={() => searchDialogRef.current?.close()}>
              <X />
            </Button>
          </div>

          <div className="relative">
            <label htmlFor="workspace-search" className="sr-only">Search workspace sections</label>
            <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
            <Input
              id="workspace-search"
              autoFocus
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  if (event.nativeEvent.isComposing || event.keyCode === 229) return
                  event.preventDefault()
                  if (filteredNavigation[0]) navigateTo(filteredNavigation[0].id)
                }
              }}
              placeholder="Search projects, issues, team..."
              className="h-11 pl-10"
            />
          </div>

          <div className="mt-4">
            <p className="mb-2 px-1 text-[10px] font-semibold uppercase tracking-[0.15em] text-muted-foreground">Destinations</p>
            {filteredNavigation.length > 0 ? (
              <ul className="flex max-h-[min(50vh,24rem)] flex-col gap-1 overflow-y-auto" aria-label="Search results">
                {filteredNavigation.map((item) => {
                  const Icon = item.icon
                  return (
                    <li key={item.id}>
                      <button
                        type="button"
                        onClick={() => navigateTo(item.id)}
                        className={cn(
                          'group flex w-full items-center gap-3 rounded-xl p-3 text-left transition-colors hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
                          activeSection === item.id && 'bg-accent/70',
                        )}
                      >
                        <span className="grid size-9 shrink-0 place-items-center rounded-lg border border-border/70 bg-card text-muted-foreground transition-colors group-hover:text-primary">
                          <Icon aria-hidden="true" />
                        </span>
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-sm font-medium text-foreground">{item.label}</span>
                          <span className="mt-0.5 block truncate text-xs text-muted-foreground">{item.description}</span>
                        </span>
                        <ArrowRight className="text-muted-foreground/55" aria-hidden="true" />
                      </button>
                    </li>
                  )
                })}
              </ul>
            ) : (
              <p className="rounded-xl border border-dashed border-border px-4 py-7 text-center text-sm text-muted-foreground" role="status">
                No workspace sections match “{searchTerm}”.
              </p>
            )}
          </div>

          <div className="mt-4 flex items-center justify-between border-t border-border/70 pt-3 text-[11px] text-muted-foreground">
            <span>Navigate with Enter</span>
            <kbd className="rounded-md border border-border bg-muted/70 px-1.5 py-0.5">Esc</kbd>
          </div>
        </div>
      </dialog>

      <dialog ref={mobileNavigationRef} className="mobile-nav-dialog" aria-label="Mobile navigation">
        <Sidebar
          activeSection={activeSection}
          collapsed={false}
          mobile
          apiBaseUrlConfigured={apiBaseUrlConfigured}
          onNavigate={navigateTo}
          onClose={() => mobileNavigationRef.current?.close()}
        />
      </dialog>
    </div>
  )
}
