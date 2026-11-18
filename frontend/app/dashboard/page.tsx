"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { useAuth } from "@/components/AuthProvider";
import { errorMessage } from "@/lib/errors";
import { type FaceProfileStatus, getFaceProfile } from "@/services/api";

function formatDate(value: string | null): string {
  if (!value) {
    return "—";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "—";
  }
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export default function DashboardPage() {
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [profile, setProfile] = useState<FaceProfileStatus | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);

  useEffect(() => {
    if (ready && !user) {
      router.replace("/login");
    }
  }, [ready, user, router]);

  useEffect(() => {
    if (!token) {
      return;
    }

    let cancelled = false;

    async function loadProfile() {
      try {
        const status = await getFaceProfile(token as string);
        if (!cancelled) {
          setProfile(status);
        }
      } catch (error) {
        if (!cancelled) {
          setProfileError(errorMessage(error));
        }
      } finally {
        if (!cancelled) {
          setProfileLoading(false);
        }
      }
    }

    void loadProfile();

    return () => {
      cancelled = true;
    };
  }, [token]);

  if (!ready || !user) {
    return (
      <main className="flex flex-1 items-center justify-center px-6 py-16 text-sm text-zinc-500 dark:text-zinc-400">
        Loading your account…
      </main>
    );
  }

  return (
    <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-12">
      <div>
        <p className="text-sm font-medium text-indigo-600 dark:text-indigo-400">
          Dashboard
        </p>
        <h1 className="mt-1 text-3xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
          Welcome back
        </h1>
        <p className="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
          Signed in as {user.email}
        </p>
      </div>

      <div className="mt-8 grid gap-5 md:grid-cols-2">
        <section className="rounded-2xl border border-zinc-200/80 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <h2 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
            Account
          </h2>
          <dl className="mt-4 space-y-3 text-sm">
            <div className="flex items-center justify-between gap-4">
              <dt className="text-zinc-500 dark:text-zinc-400">Email</dt>
              <dd className="font-medium text-zinc-900 dark:text-zinc-100">
                {user.email}
              </dd>
            </div>
            <div className="flex items-center justify-between gap-4">
              <dt className="text-zinc-500 dark:text-zinc-400">Member since</dt>
              <dd className="text-zinc-700 dark:text-zinc-300">
                {formatDate(user.createdAt)}
              </dd>
            </div>
          </dl>
        </section>

        <section className="rounded-2xl border border-zinc-200/80 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <h2 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
            Face profile
          </h2>

          {profileLoading ? (
            <p className="mt-4 text-sm text-zinc-500 dark:text-zinc-400">
              Checking enrollment status…
            </p>
          ) : profileError ? (
            <p className="mt-4 text-sm text-red-600 dark:text-red-400">
              {profileError}
            </p>
          ) : (
            <div className="mt-4 space-y-3 text-sm">
              <span
                className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-medium ${
                  profile?.enrolled
                    ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300"
                    : "bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-300"
                }`}
              >
                <span
                  aria-hidden
                  className={`size-1.5 rounded-full ${
                    profile?.enrolled ? "bg-emerald-500" : "bg-zinc-400"
                  }`}
                />
                {profile?.enrolled ? "Enrolled" : "Not enrolled"}
              </span>
              <p className="text-zinc-500 dark:text-zinc-400">
                {profile?.enrolled
                  ? `Last updated ${formatDate(profile.updatedAt)}`
                  : "Enroll a face profile to sign in without a password."}
              </p>
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
