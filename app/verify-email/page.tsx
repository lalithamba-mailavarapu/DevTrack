
"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { verifyEmail } from "@/lib/api/auth";
import { getApiErrorMessage } from "@/lib/api/client";

function VerifyEmailContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const email = searchParams.get("email") ?? "";

  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    if (!email) {
      setError("Email address is missing. Please register again.");
      return;
    }

    if (!/^\d{6}$/.test(otp)) {
      setError("Please enter a valid six-digit OTP.");
      return;
    }

    setLoading(true);

    try {
      await verifyEmail({ email, otp });

      setSuccess(true);

      setTimeout(() => {
        router.replace("/login");
      }, 2000);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-50 p-6 text-slate-900 [color-scheme:light]">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        {!success ? (
          <>
            <div className="mb-5 text-center text-4xl">✉️</div>

            <h1 className="text-3xl font-bold text-center">
              Verify your email
            </h1>

            <p className="mt-3 text-center text-slate-500">
              Enter the six-digit verification code sent to:
            </p>

            <p className="mt-2 text-center font-semibold text-slate-900">
              {email || "Email not provided"}
            </p>

            <form onSubmit={handleSubmit} className="mt-8 space-y-5">
              <div>
                <label
                  htmlFor="otp"
                  className="mb-2 block text-sm font-medium"
                >
                  Verification code
                </label>

                <input
                  id="otp"
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  required
                  value={otp}
                  onChange={(e) =>
                    setOtp(e.target.value.replace(/\D/g, "").slice(0, 6))
                  }
                  placeholder="Enter 6-digit OTP"
                  className="w-full rounded-lg border border-slate-300 bg-white p-3 text-center text-xl tracking-[0.5em] text-slate-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                />
              </div>

              <p className="text-xs text-center text-slate-500">
                Your OTP expires in 10 minutes.
              </p>

              {error && (
                <p className="text-sm text-center text-red-600">
                  {error}
                </p>
              )}

              <button
                type="submit"
                disabled={loading || otp.length !== 6 || !email}
                className="w-full rounded-lg bg-slate-900 p-3 font-medium text-white disabled:opacity-50"
              >
                {loading ? "Verifying..." : "Verify email"}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-slate-600">
              Wrong email?{" "}
              <Link
                href="/register"
                className="font-semibold text-blue-600"
              >
                Register again
              </Link>
            </p>
          </>
        ) : (
          <div className="py-6 text-center">
            <div className="mb-5 text-5xl text-green-600">✓</div>

            <h1 className="text-2xl font-bold text-green-700">
              Email verified!
            </h1>

            <p className="mt-3 text-slate-600">
              Your DevTrack account has been created successfully.
            </p>

            <p className="mt-5 text-sm text-slate-500">
              Redirecting you to login...
            </p>
          </div>
        )}
      </div>
    </main>
  );
}

export default function VerifyEmailPage() {
  return (
    <Suspense
      fallback={
        <main className="flex min-h-screen items-center justify-center bg-slate-50">
          Loading verification page...
        </main>
      }
    >
      <VerifyEmailContent />
    </Suspense>
  );
}
