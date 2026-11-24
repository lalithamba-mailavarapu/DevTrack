
"use client";

import { useEffect, useState } from "react";
import { checkBackendHealth } from "@/lib/api/client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { loginUser } from "@/lib/api/auth";
import { setAccessToken } from "@/lib/api/client";
import { getApiErrorMessage } from "@/lib/api/client";
import { useAuth } from "@/lib/auth-context";

export default function LoginPage() {
  console.log("LOGIN PAGE LOADED");
  const router = useRouter();
  const { refreshUser } = useAuth();
  const googleLoginUrl = `${
  process.env.NEXT_PUBLIC_API_BASE_URL?.trim().replace(/\/+$/, "")
}/oauth2/authorization/google`;
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
  void checkBackendHealth().catch((error) => {
    console.error("Backend warm-up failed:", error);
  });
}, []);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const result = await loginUser({ email, password });

      setAccessToken(result.accessToken);
        await refreshUser();
        router.push("/");
    } catch {
  setError("Either the email or password is incorrect.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-50 p-6 text-slate-900 [color-scheme:light]">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-3xl font-bold text-slate-900">Welcome back</h1>
        <p className="mt-2 text-slate-500">
          Sign in to your DevTrack account.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <div>
            <label className="mb-2 block text-sm font-medium">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white p-3 text-slate-900 placeholder:text-slate-400 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              placeholder="you@example.com"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white p-3 text-slate-900 placeholder:text-slate-400 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              placeholder="Enter your password"
            />
          </div>
          <div className="flex justify-end">
            <Link
              href="/forgot-password"
              className="text-sm font-medium text-primary hover:underline"
            >
              Forgot password?
            </Link>
          </div>

          {error && (
            <p className="text-sm text-red-600">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-slate-900 p-3 font-medium text-white disabled:opacity-50"
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
          <div className="mt-5">
  <div className="relative flex items-center">
    <div className="flex-grow border-t border-slate-200" />
    <span className="mx-3 text-sm text-slate-400">
      OR
    </span>
    <div className="flex-grow border-t border-slate-200" />
  </div>

  <a
    href={googleLoginUrl}
    className="mt-5 flex w-full items-center justify-center gap-3 rounded-lg border border-slate-300 bg-white p-3 font-medium text-slate-700 transition hover:bg-slate-50"
  >
    <svg
      viewBox="0 0 48 48"
      className="h-5 w-5"
      aria-hidden="true"
    >
      <path
        fill="#EA4335"
        d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5Z"
        transform="translate(0 4)"
      />
      <path
        fill="#4285F4"
        d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.25 5.48-4.73 7.18l7.98 6.19c4.66-4.3 7.34-10.63 7.34-17.84Z"
      />
      <path
        fill="#FBBC05"
        d="M10.53 28.59A14.4 14.4 0 0 1 9.75 24c0-1.59.27-3.12.75-4.59l-7.98-6.19A23.9 23.9 0 0 0 0 24c0 3.87.93 7.52 2.56 10.78l7.97-6.19Z"
        transform="translate(0 0)"
      />
      <path
        fill="#34A853"
        d="M24 48c6.47 0 11.91-2.13 15.88-5.78l-7.98-6.19c-2.22 1.5-5.07 2.39-7.9 2.39-6.26 0-11.57-4.22-13.46-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48Z"
      />
    </svg>

    Continue with Google
  </a>
</div>
        <p className="mt-6 text-center text-sm text-slate-600">
          Don't have an account?{" "}
          <Link href="/register" className="font-semibold text-blue-600">
            Create account
          </Link>
        </p>
      </div>
    </main>
  );
}
