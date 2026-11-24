
"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { registerUser } from "@/lib/api/auth";
import { getApiErrorMessage } from "@/lib/api/client";

export default function RegisterPage() {
  const router = useRouter();

  const googleLoginUrl = `${
  process.env.NEXT_PUBLIC_API_BASE_URL?.trim().replace(/\/+$/, "")
}/oauth2/authorization/google`;

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await registerUser({
        username: username.trim(),
        email: email.trim(),
        password,
      });

      router.push(
        `/verify-email?sent=1&email=${encodeURIComponent(email.trim())}`
      );
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-50 p-6 text-slate-900 [color-scheme:light]">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-3xl font-bold text-slate-900">
          Create your account
        </h1>

        <p className="mt-2 text-slate-500">
          Start managing your projects with DevTrack.
        </p>  

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <div>
            <label className="mb-2 block text-sm font-medium">
              Username
            </label>
            <input
              required
              minLength={3}
              maxLength={50}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white p-3 text-slate-900 placeholder:text-slate-400 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              placeholder="Your username"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium">
              Email
            </label>
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
            <label className="mb-2 block text-sm font-medium">
              Password
            </label>
            <input
              type="password"
              required
              minLength={8}
              maxLength={72}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white p-3 text-slate-900 placeholder:text-slate-400 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              placeholder="Create a password"
            />
          </div>

          {error && (
            <p className="text-sm text-red-600">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-slate-900 p-3 font-medium text-white disabled:opacity-50"
          >
            {loading ? "Sending verification email..." : "Create account"}
          </button>
        </form>

        <div className="mt-6">
  <div className="relative flex items-center">
    <div className="flex-grow border-t border-slate-200" />
    <span className="mx-3 text-sm text-slate-400">OR</span>
    <div className="flex-grow border-t border-slate-200" />
  </div>

  <a
    href={googleLoginUrl}
    className="mt-5 flex w-full items-center justify-center gap-3 rounded-lg border border-slate-300 bg-white p-3 font-medium text-slate-700 transition hover:bg-slate-50"
  >
    <svg viewBox="0 0 48 48" className="h-5 w-5" aria-hidden="true">
      <path
        fill="#4285F4"
        d="M47.5 24.5c0-1.6-.15-3.1-.4-4.5H24v9h13.2c-.6 2.9-2.3 5.4-4.8 7.1v5.9h7.8c4.6-4.3 7.3-10.6 7.3-17.5Z"
      />
      <path
        fill="#34A853"
        d="M24 48c6.6 0 12.1-2.2 16.2-6l-7.8-5.9c-2.2 1.5-5 2.4-8.4 2.4-6.4 0-11.8-4.3-13.8-10.1H2.3v6.1C6.3 42.5 14.5 48 24 48Z"
      />
      <path
        fill="#FBBC05"
        d="M10.2 28.4a14.4 14.4 0 0 1 0-8.8v-6.1H2.3a24 24 0 0 0 0 21l7.9-6.1Z"
      />
      <path
        fill="#EA4335"
        d="M24 9.5c3.6 0 6.8 1.2 9.3 3.6l7-7C36.1 2.3 30.6 0 24 0 14.5 0 6.3 5.5 2.3 13.5l7.9 6.1C12.2 13.8 17.6 9.5 24 9.5Z"
      />
    </svg>

    Continue with Google
  </a>
</div>

        <p className="mt-6 text-center text-sm text-slate-600">
          Already registered?{" "}
          <Link href="/login" className="font-semibold text-blue-600">
            Sign in
          </Link>
        </p>
      </div>
    </main>
  );
}
