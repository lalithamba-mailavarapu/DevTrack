
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

export interface CurrentUserResponse {
  id: number
  username: string
  email: string
  createdAt: string
}

export async function getCurrentUser(): Promise<CurrentUserResponse> {
  const response = await apiClient.get<CurrentUserResponse>('/api/auth/me')
  return response.data
}

export interface UpdateProfileRequest {
  username: string
}

export async function updateCurrentUser(
  data: UpdateProfileRequest
): Promise<CurrentUserResponse> {
  const response = await apiClient.put<CurrentUserResponse>(
    '/api/auth/me',
    data
  )

  return response.data
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export async function changePassword(
  data: ChangePasswordRequest
): Promise<string> {
  const response = await apiClient.post<string>(
    "/api/auth/change-password",
    data
  );

  return response.data;
}


export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export async function forgotPassword(
  data: ForgotPasswordRequest
): Promise<string> {
  const response = await apiClient.post<string>(
    '/api/auth/forgot-password',
    data
  );

  return response.data;
}

export async function resetPassword(
  data: ResetPasswordRequest
): Promise<string> {
  const response = await apiClient.post<string>(
    '/api/auth/reset-password',
    data
  );

  return response.data;
}

export interface VerifyEmailRequest {
  token: string;
}

export async function verifyEmail(
  data: VerifyEmailRequest
): Promise<string> {
  const response = await apiClient.post<string>(
    "/api/auth/verify-email",
    data
  );

  return response.data;
}