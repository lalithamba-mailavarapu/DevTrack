
import { apiClient } from "./client";

export interface AiResponse {
  analysis: string;
}

export interface AnalyzeIssueRequest {
  title: string;
  description: string;
}

export interface BreakdownTaskRequest {
  goal: string;
  description: string;
}

/**
 * Analyze an issue using Gemini AI.
 */
export async function analyzeIssue(
  data: AnalyzeIssueRequest
): Promise<AiResponse> {
  const response = await apiClient.post<AiResponse>(
    "/api/ai/analyze-issue",
    data
  );

  return response.data;
}

/**
 * Generate a task breakdown from a goal.
 */
export async function breakdownTask(
  data: BreakdownTaskRequest
): Promise<AiResponse> {
  const response = await apiClient.post<AiResponse>(
    "/api/ai/breakdown-task",
    data
  );

  return response.data;
}

/**
 * Analyze a project using its existing project context.
 */
export async function analyzeProject(
  projectId: number
): Promise<AiResponse> {
  const response = await apiClient.post<AiResponse>(
    `/api/ai/projects/${projectId}/analyze`
  );

  return response.data;
}

/**
 * Generate tasks using project context.
 */
export async function breakdownProjectTasks(
  projectId: number
): Promise<AiResponse> {
  const response = await apiClient.post<AiResponse>(
    `/api/ai/projects/${projectId}/breakdown-tasks`
  );

  return response.data;
}
