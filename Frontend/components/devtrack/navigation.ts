import type { LucideIcon } from 'lucide-react'
import {
  Activity,
  CircleDot,
  FolderKanban,
  LayoutDashboard,
  Settings2,
  UserRound,
  UsersRound,
  Workflow,
} from 'lucide-react'

export type AppSection =
  | 'overview'
  | 'projects'
  | 'issues'
  | 'technologies'
  | 'team'
  | 'profile'
  | 'settings'

export type NavigationEntry = {
  id: AppSection
  label: string
  description: string
  icon: LucideIcon
}

export const navigationGroups: { label: string; items: NavigationEntry[] }[] = [
  {
    label: 'WORKSPACE',
    items: [
      {
        id: 'overview',
        label: 'Overview',
        description: 'Workspace metrics and recent activity',
        icon: LayoutDashboard,
      },
      {
        id: 'projects',
        label: 'Projects',
        description: 'Browse and manage your projects',
        icon: FolderKanban,
      },
    ],
  },
  {
    label: 'DELIVERY',
    items: [
      {
        id: 'issues',
        label: 'Issues',
        description: 'Track work, priorities, and status',
        icon: CircleDot,
      },
      {
        id: 'technologies',
        label: 'Technologies',
        description: 'Explore languages, frameworks, and tools',
        icon: Workflow,
      },
    ],
  },
  {
    label: 'ADMINISTRATION',
    items: [
      {
        id: 'team',
        label: 'Team',
        description: 'View project members and access',
        icon: UsersRound,
      },
      {
        id: 'profile',
        label: 'Profile',
        description: 'Review your account details',
        icon: UserRound,
      },
      {
        id: 'settings',
        label: 'Settings',
        description: 'Configure workspace preferences',
        icon: Settings2,
      },
    ],
  },
]

export const sectionDetails: Record<AppSection, { title: string; description: string }> = {
  overview: {
    title: 'Overview',
    description: 'A clear view of projects, issues, and the tools behind your work.',
  },
  projects: {
    title: 'Projects',
    description: 'Keep project ownership, technology, and delivery details together.',
  },
  issues: {
    title: 'Issues',
    description: 'Track work from the first report through completion.',
  },
  technologies: {
    title: 'Technologies',
    description: 'See the languages, frameworks, databases, and tools your projects use.',
  },
  team: {
    title: 'Team',
    description: 'Understand who is connected to each project and what they can do.',
  },
  profile: {
    title: 'Profile',
    description: 'Manage your DevTrack account details.',
  },
  settings: {
    title: 'Settings',
    description: 'Manage workspace preferences and account configuration.',
  },
}

export const allNavigationItems = navigationGroups.flatMap((group) => group.items)
