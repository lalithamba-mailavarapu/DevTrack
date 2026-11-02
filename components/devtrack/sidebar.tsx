'use client'

import { Layers, PanelLeftClose, PanelLeftOpen, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { navigationGroups, type AppSection } from './navigation'

type SidebarProps = {
  activeSection: AppSection
  collapsed: boolean
  apiBaseUrlConfigured: boolean
  onNavigate: (section: AppSection) => void
  onToggleCollapsed?: () => void
  onClose?: () => void
  mobile?: boolean
}

export function Sidebar({
  activeSection,
  collapsed,
  apiBaseUrlConfigured,
  onNavigate,
  onToggleCollapsed,
  onClose,
  mobile = false,
}: SidebarProps) {
  const expanded = mobile || !collapsed

  return (
    <aside
      className={cn(
        'flex h-full flex-col bg-sidebar text-sidebar-foreground',
        mobile
          ? 'w-full'
          : cn(
              'sticky top-0 h-screen shrink-0 border-r border-sidebar-border transition-[width] duration-200',
              collapsed ? 'w-[4.75rem]' : 'w-[16.5rem]',
            ),
      )}
    >
      <div className={cn('flex h-[4.5rem] shrink-0 items-center px-4', expanded ? 'justify-between' : 'justify-center')}>
        <div className={cn('flex min-w-0 items-center gap-3', !expanded && 'justify-center')}>
          <div className="grid size-9 shrink-0 place-items-center rounded-xl bg-gradient-to-br from-violet-500 to-indigo-600 text-white shadow-lg shadow-violet-950/20">
            <Layers aria-hidden="true" />
          </div>
          {expanded && (
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold tracking-tight text-sidebar-foreground">DevTrack</p>
              <p className="truncate text-[11px] text-sidebar-foreground/50">Engineering workspace</p>
            </div>
          )}
        </div>

        {mobile ? (
          <button
            type="button"
            onClick={onClose}
            className="grid size-9 place-items-center rounded-lg text-sidebar-foreground/60 transition-colors hover:bg-sidebar-accent hover:text-sidebar-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
            aria-label="Close navigation"
          >
            <X aria-hidden="true" />
          </button>
        ) : expanded ? (
          <button
            type="button"
            onClick={onToggleCollapsed}
            className="grid size-8 shrink-0 place-items-center rounded-lg text-sidebar-foreground/50 transition-colors hover:bg-sidebar-accent hover:text-sidebar-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
            aria-label="Collapse sidebar"
            title="Collapse sidebar"
          >
            <PanelLeftClose aria-hidden="true" />
          </button>
        ) : null}
      </div>

      {!mobile && collapsed && (
        <div className="shrink-0 px-3 pb-2">
          <button
            type="button"
            onClick={onToggleCollapsed}
            className="mx-auto grid size-9 place-items-center rounded-lg text-sidebar-foreground/55 transition-colors hover:bg-sidebar-accent hover:text-sidebar-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring"
            aria-label="Expand sidebar"
            title="Expand sidebar"
          >
            <PanelLeftOpen aria-hidden="true" />
          </button>
        </div>
      )}

      <div className="flex-1 overflow-y-auto px-3 pb-4 pt-2">
        {navigationGroups.map((group) => (
          <div className="mb-5" key={group.label}>
            {expanded && (
              <p className="mb-2 px-3 text-[10px] font-semibold tracking-[0.16em] text-sidebar-foreground/35">
                {group.label}
              </p>
            )}
            <nav aria-label={group.label.toLowerCase()} className="flex flex-col gap-1">
              {group.items.map((item) => {
                const Icon = item.icon
                const isActive = activeSection === item.id

                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => onNavigate(item.id)}
                    aria-current={isActive ? 'page' : undefined}
                    aria-label={expanded ? undefined : item.label}
                    title={expanded ? undefined : item.label}
                    className={cn(
                      'group flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring',
                      !expanded && 'justify-center px-2',
                      isActive
                        ? 'bg-sidebar-accent font-medium text-sidebar-foreground shadow-sm'
                        : 'text-sidebar-foreground/65 hover:bg-sidebar-accent/70 hover:text-sidebar-foreground',
                    )}
                  >
                    <Icon
                      aria-hidden="true"
                      className={cn(
                        'size-[18px] shrink-0 transition-colors',
                        isActive ? 'text-sidebar-primary' : 'text-sidebar-foreground/50 group-hover:text-sidebar-foreground',
                      )}
                    />
                    {expanded && <span className="truncate">{item.label}</span>}
                  </button>
                )
              })}
            </nav>
          </div>
        ))}
      </div>

      <div className="shrink-0 border-t border-sidebar-border p-3">
        {expanded ? (
          <div className="rounded-xl border border-sidebar-border bg-white/[0.035] p-3">
            <div className="flex items-center gap-2">
              <span
                aria-hidden="true"
                className={cn('size-2 shrink-0 rounded-full', apiBaseUrlConfigured ? 'bg-emerald-400' : 'bg-amber-300')}
              />
              <p className="text-xs font-medium text-sidebar-foreground/85">
                {apiBaseUrlConfigured ? 'API URL configured' : 'API URL missing'}
              </p>
            </div>
            <p className="mt-2 text-[11px] leading-relaxed text-sidebar-foreground/45">
              {apiBaseUrlConfigured
                ? 'Live data is paused until authentication and DTO contracts are verified.'
                : 'Set NEXT_PUBLIC_API_BASE_URL to connect the deployed service.'}
            </p>
          </div>
        ) : (
          <div
            className="mx-auto grid size-9 place-items-center rounded-xl border border-sidebar-border"
            title={apiBaseUrlConfigured ? 'API URL configured' : 'API URL missing'}
            aria-label={apiBaseUrlConfigured ? 'API URL configured' : 'API URL missing'}
          >
            <span
              aria-hidden="true"
              className={cn('size-2 rounded-full', apiBaseUrlConfigured ? 'bg-emerald-400' : 'bg-amber-300')}
            />
          </div>
        )}
      </div>
    </aside>
  )
}
