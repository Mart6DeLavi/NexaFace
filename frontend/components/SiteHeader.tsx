"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { useAuth } from "@/components/AuthProvider";

const publicLinks = [
  { href: "/login", label: "Sign in" },
  { href: "/register", label: "Create account" },
];

export function SiteHeader() {
  const { user, ready, logout } = useAuth();
  const pathname = usePathname();

  return (
    <header className="border-b border-zinc-200/80 bg-white/80 backdrop-blur dark:border-zinc-800 dark:bg-zinc-950/70">
      <div className="mx-auto flex w-full max-w-5xl flex-wrap items-center justify-between gap-3 px-6 py-4">
        <Link href="/" className="flex items-center gap-2">
          <span className="flex size-9 items-center justify-center rounded-lg bg-zinc-900 text-xs font-semibold text-white dark:bg-white dark:text-zinc-900">
            NF
          </span>
          <span className="text-sm font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
            NextFace
          </span>
        </Link>

        <nav className="flex items-center gap-1 text-sm">
          {user ? (
            <>
              <Link
                href="/dashboard"
                className={`rounded-lg px-3 py-2 font-medium transition-colors ${
                  pathname === "/dashboard"
                    ? "bg-zinc-100 text-zinc-900 dark:bg-zinc-800 dark:text-zinc-100"
                    : "text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-100"
                }`}
              >
                Dashboard
              </Link>
              <span className="hidden px-3 py-2 text-zinc-400 sm:inline dark:text-zinc-500">
                {user.email}
              </span>
              <button
                type="button"
                onClick={logout}
                className="rounded-lg px-3 py-2 font-medium text-zinc-600 transition-colors hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-100"
              >
                Log out
              </button>
            </>
          ) : (
            ready &&
            publicLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={`rounded-lg px-3 py-2 font-medium transition-colors ${
                  pathname === link.href
                    ? "bg-zinc-100 text-zinc-900 dark:bg-zinc-800 dark:text-zinc-100"
                    : "text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-100"
                }`}
              >
                {link.label}
              </Link>
            ))
          )}
        </nav>
      </div>
    </header>
  );
}
