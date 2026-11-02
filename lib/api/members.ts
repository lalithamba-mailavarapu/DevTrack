import { apiClient } from './client'

export interface ProjectMember {
  userId: number
  username: string
  email: string
  role: string
}

export interface AddMemberRequest {
  email: string
}

export async function getProjectMembers(projectId: number): Promise<ProjectMember[]> {
  const response = await apiClient.get<ProjectMember[]>(`/api/projects/${projectId}/members`)
  return response.data
}

export async function addProjectMember(projectId: number, email: string): Promise<ProjectMember> {
  const response = await apiClient.post<ProjectMember>(
    `/api/projects/${projectId}/members`,
    { email } satisfies AddMemberRequest
  )
  return response.data
}

export async function removeProjectMember(projectId: number, userId: number): Promise<void> {
  await apiClient.delete(`/api/projects/${projectId}/members/${userId}`)
}
