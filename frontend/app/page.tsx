import Link from "next/link";

import { HealthStatus } from "@/components/HealthStatus";

const features = [
  {
    title: "One account",
    description: "A single NextFace identity works across every connected app.",
  },
  {
    title: "Face sign-in",
    description: "Enroll once and authenticate with a selfie instead of a password.",
  },
  {
    title: "Minimal data sharing",
    description: "Apps receive a one-time code, never your face or embeddings.",
  },
];

export default function Home() {
  return (
    <main className="relative flex flex-1 items-center justify-center overflow-hidden px-6 py-16">
      <div
        aria-hidden
        className="pointer-events-none absolute -top-40 left-1/2 size-[36rem] -translate-x-1/2 rounded-full bg-indigo-200/40 blur-3xl dark:bg-indigo-500/10"
      />
      <div
        aria-hidden
        className="pointer-events-none absolute -bottom-52 right-[-10rem] size-[32rem] rounded-full bg-sky-200/40 blur-3xl dark:bg-sky-500/10"
      />

      <section className="relative w-full max-w-2xl rounded-3xl border border-zinc-200/80 bg-white/80 p-8 shadow-xl shadow-zinc-900/5 backdrop-blur sm:p-12 dark:border-zinc-800 dark:bg-zinc-900/70">
        <div className="flex items-center gap-3">
          <span className="flex size-11 items-center justify-center rounded-xl bg-zinc-900 text-sm font-semibold tracking-tight text-white dark:bg-white dark:text-zinc-900">
            NF
          </span>
          <p className="text-sm font-medium text-zinc-500 dark:text-zinc-400">
            Centralized biometric authentication
          </p>
        </div>

        <h1 className="mt-8 text-4xl font-semibold tracking-tight text-zinc-900 sm:text-5xl dark:text-zinc-50">
          NextFace
        </h1>
        <p className="mt-4 max-w-xl text-lg leading-8 text-zinc-600 dark:text-zinc-300">
          Sign in with your face once and continue to any application that integrates
          NextFace — no passwords, no per-site accounts.
        </p>

        <div className="mt-8 flex flex-wrap gap-3">
          <Link
            href="/register"
            className="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-500"
          >
            Create account
          </Link>
          <Link
            href="/login"
            className="rounded-xl border border-zinc-300 px-5 py-2.5 text-sm font-semibold text-zinc-700 transition-colors hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800/60"
          >
            Sign in
          </Link>
        </div>

        <ul className="mt-8 grid gap-3 sm:grid-cols-3">
          {features.map((feature) => (
            <li
              key={feature.title}
              className="rounded-xl border border-zinc-200/80 bg-zinc-50/80 p-4 dark:border-zinc-800 dark:bg-zinc-800/40"
            >
              <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                {feature.title}
              </p>
              <p className="mt-1 text-xs leading-5 text-zinc-500 dark:text-zinc-400">
                {feature.description}
              </p>
            </li>
          ))}
        </ul>

        <div className="mt-8 rounded-2xl border border-zinc-200/80 bg-zinc-50/60 p-5 dark:border-zinc-800 dark:bg-zinc-800/30">
          <HealthStatus />
        </div>
      </section>
    </main>
  );
}
