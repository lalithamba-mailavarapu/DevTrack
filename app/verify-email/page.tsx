
"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { verifyEmail } from "@/lib/api/auth";
import { getApiErrorMessage } from "@/lib/api/client";

function VerifyEmailContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const token = searchParams.get("token");
  const email = searchParams.get("email");

  const [status, setStatus] = useState<
    "pending" | "loading" | "success" | "error"
  >(token ? "loading" : "pending");

  const [message, setMessage] = useState("");
  const requestStarted = useRef(false);

  useEffect(() => {
    if (!token || requestStarted.current) return;

    requestStarted.current = true;

    let redirectTimer: ReturnType<typeof setTimeout>;

    async function confirmEmail() {
      try {
        await verifyEmail({ token: token! });

        setStatus("success");
        setMessage("Your email has been verified and your DevTrack account has been created.");

        redirectTimer = setTimeout(() => {
          router.replace("/login");
        }, 2500);
      } catch (error) {
        setStatus("error");
        setMessage(getApiErrorMessage(error));
      }
    }

    confirmEmail();

    return () => {
      if (redirectTimer) clearTimeout(redirectTimer);
    };
  }, [token, router]);

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-50 p-6 text-slate-900 [color-scheme:light]">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-lg">

        {status === "pending" && (
          <>
            <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-indigo-100 text-3xl">
              ✉️
            </div>

            <h1 className="text-2xl font-bold">
              Check your email
            </h1>

            <p className="mt-3 text-slate-600">
              We've sent a verification link to:
            </p>

            {email && (
              <p className="mt-2 font-semibold text-slate-900">
                {email}
              </p>
            )}

            <p className="mt-4 text-sm text-slate-500">
              Click the link in your email to verify your address and create your DevTrack account.
              The link expires in 15 minutes.
            </p>

            <p className="mt-6 text-sm text-slate-500">
              Already verified?{" "}
              <Link href="/login" className="font-semibold text-blue-600">
                Sign in
              </Link>
            </p>
          </>
        )}

        {status === "loading" && (
          <>
            <div className="mx-auto mb-5 h-12 w-12 animate-spin rounded-full border-4 border-indigo-200 border-t-indigo-600" />

            <h1 className="text-2xl font-bold">
              Verifying your email...
            </h1>

            <p className="mt-3 text-slate-500">
              Please wait while we verify your email and create your account.
            </p>
          </>
        )}

        {status === "success" && (
          <>
            <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-3xl">
              ✓
            </div>

            <h1 className="text-2xl font-bold text-green-700">
              Email verified!
            </h1>

            <p className="mt-3 text-slate-600">
              {message}
            </p>

            <p className="mt-5 text-sm text-slate-500">
              Redirecting you to login...
            </p>
          </>
        )}

        {status === "error" && (
          <>
            <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-red-100 text-3xl">
              !
            </div>

            <h1 className="text-2xl font-bold text-red-700">
              Verification failed
            </h1>

            <p className="mt-3 text-slate-600">
              {message}
            </p>

            <div className="mt-6 flex flex-col gap-3">
              <Link
                href="/register"
                className="w-full rounded-lg bg-slate-900 p-3 font-medium text-white"
              >
                Register again
              </Link>

              <Link
                href="/login"
                className="text-sm font-semibold text-blue-600"
              >
                Back to login
              </Link>
            </div>
          </>
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
          <p className="text-slate-600">Loading verification page...</p>
        </main>
      }
    >
      <VerifyEmailContent />
    </Suspense>
  );
}
