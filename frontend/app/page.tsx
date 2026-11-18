import { HealthStatus } from "@/components/HealthStatus";

const features = [
  "One account for every integrated app",
  "Face recognition instead of passwords",
  "One-time authorization codes",
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
          <div>
            <p className="text-sm font-medium text-zinc-500 dark:text-zinc-400">
              Biometric authentication
            </p>
            <p className="text-xs text-zinc-400 dark:text-zinc-500">
              Phase 1 · Project setup
            </p>
          </div>
        </div>

        <h1 className="mt-8 text-4xl font-semibold tracking-tight text-zinc-900 sm:text-5xl dark:text-zinc-50">
          NextFace
        </h1>
        <p className="mt-4 max-w-xl text-lg leading-8 text-zinc-600 dark:text-zinc-300">
          Sign in with your face once and continue to any application that integrates
          NextFace — no passwords, no per-site accounts.
        </p>

        <ul className="mt-6 grid gap-2 text-sm text-zinc-600 sm:grid-cols-3 dark:text-zinc-400">
          {features.map((feature) => (
            <li
              key={feature}
              className="rounded-lg border border-zinc-200/80 bg-zinc-50/80 px-3 py-2 dark:border-zinc-800 dark:bg-zinc-800/40"
            >
              {feature}
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
