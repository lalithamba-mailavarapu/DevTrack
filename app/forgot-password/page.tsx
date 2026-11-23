
'use client'

import { useState, type FormEvent } from 'react'
import Link from 'next/link'
import { forgotPassword } from '@/lib/api/auth'
import { getApiErrorMessage } from '@/lib/api/client'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      await forgotPassword({ email: email.trim() })
      setSuccess(true)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-md space-y-6 rounded-xl border border-border bg-card p-8 shadow-sm">
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-foreground">
            Forgot your password?
          </h1>
          <p className="text-sm text-muted-foreground">
            Enter your registered email address and we&apos;ll send you a
            password reset link.
          </p>
        </div>

        {success ? (
          <div className="space-y-4">
            <p className="text-sm text-green-600">
              If an account exists for that email, a password reset link
              will be sent. Check your inbox.
            </p>
            <Link
              href="/login"
              className="inline-block text-sm font-medium text-primary hover:underline"
            >
              Back to login
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-medium">
                Email address
              </label>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
              />
            </div>

            {error && (
              <p className="text-sm text-destructive">{error}</p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
            >
              {loading ? 'Sending...' : 'Send reset link'}
            </button>

            <Link
              href="/login"
              className="block text-center text-sm text-muted-foreground hover:text-primary"
            >
              Back to login
            </Link>
          </form>
        )}
      </div>
    </main>
  )
}
