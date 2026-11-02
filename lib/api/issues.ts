import { apiClient } from './client'

export type IssueStatus = 'OPEN' | 'IN_PROGRESS' | 'CLOSED'
export type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type IssueLabel = 'BUG' | 'FEATURE' | 'ENHANCEMENT' | 'DOCUMENTATION' | 'TASK'

export interface IssueResponse {
  id: number
  title: string
  description: string | null
  status: IssueStatus
  priority: IssuePriority
  label: IssueLabel | null
  projectId: number
  reporterId: number
  assigneeId: number | null
  createdAt: string
  updatedAt: string
}

export interface CreateIssueRequest {
  title: string
  description?: string
  label?: IssueLabel
}

export interface UpdateIssueRequest {
  title?: string
  description?: string
  status?: IssueStatus
  priority?: IssuePriority
}

export interface AssignIssueRequest {
  assigneeId: number
}

export interface IssuePage {
  content: IssueResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function getIssues(
  projectId: number,
  page = 0,
  size = 10,
  filters?: { status?: IssueStatus; priority?: IssuePriority }
): Promise<IssuePage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (filters?.status) params.set('status', filters.status)
  if (filters?.priority) params.set('priority', filters.priority)
  const path = filters?.status || filters?.priority
    ? `/api/projects/${projectId}/issues/filter?${params.toString()}`
    : `/api/projects/${projectId}/issues?${params.toString()}`
  const response = await apiClient.get<IssuePage>(path)
  return response.data
}

export async function createIssue(projectId: number, data: CreateIssueRequest) {
  const response = await apiClient.post<IssueResponse>(
    `/api/projects/${projectId}/issues`, data
  )
  return response.data
}

export async function updateIssue(projectId: number, issueId: number, data: UpdateIssueRequest) {
  const response = await apiClient.patch<IssueResponse>(
    `/api/projects/${projectId}/issues/${issueId}`, data
  )
  return response.data
}

export async function assignIssue(projectId: number, issueId: number, assigneeId: number) {
  const response = await apiClient.patch<IssueResponse>(
    `/api/projects/${projectId}/issues/${issueId}/assignee`, { assigneeId }
  )
  return response.data
}
