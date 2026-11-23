'use client'

import axios from 'axios'

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL?.trim().replace(/\/+$/, '')


export async function checkBackendHealth() {
  if (!apiBaseUrl) {
    throw new Error('NEXT_PUBLIC_API_BASE_URL is not configured.')
  }

  const response = await axios.get(
    `${apiBaseUrl}/api/health`,
    {
      timeout: 120_000,
      headers: {
        Accept: 'application/json',
      },
    }
  )

  return response.data
}


let accessToken: string | null = null

export const apiClient = axios.create({
  baseURL: apiBaseUrl,
  timeout: 120_000,
  headers: {
    Accept: 'application/json',
  },
})


apiClient.interceptors.request.use((config) => {
  if (!apiBaseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not configured.")
  }

  if (!accessToken && typeof window !== "undefined") {
    accessToken = sessionStorage.getItem("devtrack_access_token")
  }

  if (accessToken) {
    config.headers.set("Authorization", `Bearer ${accessToken}`)
  }

  return config
})



export function setAccessToken(token: string | null) {
  accessToken = token?.trim() || null

  if (typeof window !== "undefined") {
    if (accessToken) {
      sessionStorage.setItem("devtrack_access_token", accessToken)
    } else {
      sessionStorage.removeItem("devtrack_access_token")
    }
  }
}



export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;

    if (typeof data === "string" && data.trim()) {
      return data;
    }

    if (data && typeof data === "object") {
      const body = data as {
        message?: string;
        detail?: string;
        error?: string;
      };

      if (body.message?.trim()) return body.message;
      if (body.detail?.trim()) return body.detail;
    }

    if (error.code === "ECONNABORTED") {
      return "The request timed out. Please try again.";
    }

    if (!error.response) {
      return "Unable to connect to DevTrack. Please try again.";
    }

    if (error.response.status === 409) {
      return "This username or email is already in use.";
    }

    return "Something went wrong. Please try again.";
  }

  if (error instanceof Error) return error.message;

  return "An unexpected error occurred. Please try again.";
}

