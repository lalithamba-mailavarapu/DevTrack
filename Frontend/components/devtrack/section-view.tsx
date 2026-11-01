'use client'

import { ArrowLeft, CircleHelp, ShieldAlert } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { sectionDetails, type AppSection } from './navigation'

type SectionViewProps = {
  section: Exclude<AppSection, 'overview'>
  onNavigate: (section: AppSection) => void
}

const backendCoverage: Record<Exclude<AppSection, 'overview'>, string> = {
  projects:
    'The supplied API lists projects and creates projects. Project detail, edit, and delete routes were not included.',
  issues:
    'The supplied API lists, creates, and patches project issues. Search, assignment, labels, and priority controls must match the actual DTOs before they are enabled.',
  technologies:
    'No standalone technology endpoints were supplied. Technology details can only be shown if they are present in the project response.',
  team:
    'The supplied API lists and adds project members. Role changes and member removal require additional backend routes.',
  profile:
    'No current-user or profile endpoint was supplied, so profile data cannot be loaded or saved yet.',
  settings:
    'No workspace or user settings endpoints were supplied, so settings cannot be persisted yet.',
}

export function SectionView({ section, onNavigate }: SectionViewProps) {
  const details = sectionDetails[section]
  const isMissingApi = section === 'profile' || section === 'settings' || section === 'technologies'

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">Workspace</p>
        <h1 className="text-3xl font-semibold tracking-tight text-foreground">{details.title}</h1>
        <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">{details.description}</p>
      </div>

      <Card className="overflow-hidden border-border/80 shadow-sm shadow-slate-900/[0.025]">
        <CardHeader className="flex flex-row items-start gap-4 p-5 sm:p-7">
          <span className="grid size-11 shrink-0 place-items-center rounded-2xl bg-primary/8 text-primary">
            {isMissingApi ? <CircleHelp aria-hidden="true" /> : <ShieldAlert aria-hidden="true" />}
          </span>
          <div className="flex min-w-0 flex-col gap-2">
            <div className="flex flex-wrap items-center gap-2">
              <CardTitle className="text-base">Waiting for verified backend data</CardTitle>
              <Badge variant={isMissingApi ? 'outline' : 'secondary'}>
                {isMissingApi ? 'Additional API required' : 'Contract required'}
              </Badge>
            </div>
            <CardDescription className="max-w-2xl text-sm leading-relaxed">{backendCoverage[section]}</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="border-t border-border/70 bg-muted/25 px-5 py-5 sm:px-7">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="max-w-2xl text-xs leading-relaxed text-muted-foreground">
              DevTrack will not fabricate records or report a successful write until the Spring Boot request and response shapes are confirmed.
            </p>
            <Button variant="outline" size="sm" onClick={() => onNavigate('overview')} className="shrink-0">
              <ArrowLeft data-icon="inline-start" />
              Back to overview
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
