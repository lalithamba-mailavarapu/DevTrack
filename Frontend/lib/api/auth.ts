
import { apiClient } from "./client";

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

export async function registerUser(data: RegisterRequest) {
  const response = await apiClient.post("/api/auth/register", data);
  return response.data;
}

export async function loginUser(data: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>(
    "/api/auth/login",
    data
  );

  return response.data;
}

export async function getCurrentUser() {
  const response = await apiClient.get("/api/auth/me");
  return response.data;
}
