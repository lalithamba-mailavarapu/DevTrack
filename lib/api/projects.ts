
import { apiClient } from './client'

export type TechnologyType =
  | 'LANGUAGE'
  | 'FRAMEWORK'
  | 'DATABASE'
  | 'TOOL'
  | 'OTHER'

export interface ProjectTechnologyRequest {
  technologyName: string
  technologyType: TechnologyType
}

export interface CreateProjectRequest {
  name: string
  description?: string
  primaryLanguage?: string
  category?: string
  githubRepositoryUrl?: string
  technologies: ProjectTechnologyRequest[]
}

export interface ProjectTechnologyResponse {
  id: number
  technologyName: string
  technologyType: TechnologyType
}

export interface ProjectResponse {
  id: number
  name: string
  description: string | null
  ownerId: number
  createdAt: string
  primaryLanguage: string | null
  category: string | null
  githubRepositoryUrl: string | null
  technologies: ProjectTechnologyResponse[]
}

export async function getProjects(): Promise<ProjectResponse[]> {
  const response = await apiClient.get<ProjectResponse[]>('/api/projects')
  return response.data
}

export async function createProject(
  data: CreateProjectRequest
): Promise<ProjectResponse> {
  const response = await apiClient.post<ProjectResponse>(
    '/api/projects',
    data
  )
  return response.data
}

export async function deleteProject(projectId: number): Promise<void> {
  await apiClient.delete(`/api/projects/${projectId}`)
}
