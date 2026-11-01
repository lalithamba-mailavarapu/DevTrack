'use client'

import axios from 'axios'

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL?.trim().replace(/\/+$/, '')

let accessToken: string | null = null

export const apiClient = axios.create({
  baseURL: apiBaseUrl,
  timeout: 20_000,
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
    if (error.response) return `The API returned HTTP ${error.response.status}.`
    if (error.code === 'ECONNABORTED') return 'The API request timed out.'
    return 'Could not reach the API server. Check the base URL and network connection.'
  }

  if (error instanceof Error) return error.message
  return 'An unexpected API error occurred.'
}
