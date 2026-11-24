
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { setAccessToken } from "@/lib/api/client";
import { useAuth } from "@/lib/auth-context";

export default function GoogleCallbackPage() {
  const router = useRouter();
  const { refreshUser } = useAuth();

  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function handleGoogleCallback() {
      try {
        const hash = window.location.hash;

        const params = new URLSearchParams(
          hash.startsWith("#") ? hash.substring(1) : hash
        );

        const token = params.get("access_token");

        if (!token) {
          throw new Error("Google authentication failed. Please try again.");
        }

        // Remove the token from the visible browser URL.
        window.history.replaceState(
          null,
          "",
          window.location.pathname
        );

        // Store the DevTrack JWT using the existing mechanism.
        setAccessToken(token);

        // Load the authenticated user's information.
        await refreshUser();

        if (active) {
          router.replace("/");
        }
      } catch (err) {
        setAccessToken(null);

        if (active) {
          setError(
            err instanceof Error
              ? err.message
              : "Google login failed. Please try again."
          );
        }
      }
    }

    void handleGoogleCallback();

    return () => {
      active = false;
    };
  }, [refreshUser, router]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-lg">
        {error ? (
          <>
            <h1 className="text-2xl font-bold text-red-600">
              Authentication failed
            </h1>

            <p className="mt-3 text-sm text-slate-600">
              {error}
            </p>

            <button
              onClick={() => router.replace("/login")}
              className="mt-6 rounded-lg bg-slate-900 px-6 py-3 font-medium text-white"
            >
              Back to login
            </button>
          </>
        ) : (
          <>
            <div className="mb-4 text-4xl">🔐</div>

            <h1 className="text-2xl font-bold text-slate-900">
              Signing you in
            </h1>

            <p className="mt-3 text-slate-500">
              Please wait while we complete your Google authentication.
            </p>
          </>
        )}
      </div>
    </main>
  );
}
