"use client";

import { useCallback, useEffect, useState } from "react";

import { API_BASE_URL, getHealth, type HealthResponse } from "@/services/api";

type Status = "checking" | "online" | "offline";

const statusLabels: Record<Status, string> = {
  checking: "Checking backend…",
  online: "Backend online",
  offline: "Backend offline",
};

const statusStyles: Record<Status, string> = {
  checking: "bg-amber-50 text-amber-700 ring-amber-200",
  online: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  offline: "bg-rose-50 text-rose-700 ring-rose-200",
};

const dotStyles: Record<Status, string> = {
  checking: "bg-amber-500 animate-pulse",
  online: "bg-emerald-500",
  offline: "bg-rose-500",
};

export function HealthStatus() {
  const [status, setStatus] = useState<Status>("checking");
  const [service, setService] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const applySuccess = useCallback((health: HealthResponse) => {
    setService(health.service);
    setError(null);
    setStatus("online");
  }, []);

  const applyFailure = useCallback((cause: unknown) => {
    setService(null);
    setError(cause instanceof Error ? cause.message : "Request failed");
    setStatus("offline");
  }, []);

  useEffect(() => {
    const controller = new AbortController();

    getHealth(controller.signal).then(applySuccess).catch((cause: unknown) => {
      if (!controller.signal.aborted) {
        applyFailure(cause);
      }
    });

    return () => controller.abort();
  }, [applyFailure, applySuccess]);

  const retry = () => {
    setStatus("checking");
    setError(null);
    getHealth().then(applySuccess).catch(applyFailure);
  };

  return (
    <div className="flex flex-col gap-3">
      <div
        className={`inline-flex w-fit items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ring-1 ring-inset ${statusStyles[status]}`}
      >
        <span className={`size-2 rounded-full ${dotStyles[status]}`} />
        {statusLabels[status]}
      </div>

      <p className="text-sm text-zinc-500">
        {status === "online" && service
          ? `${service} is responding at ${API_BASE_URL}.`
          : `API endpoint: ${API_BASE_URL}`}
      </p>

      {status === "offline" && (
        <div className="flex flex-col items-start gap-2 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">
          <span>{error ?? "The backend is not reachable."}</span>
          <button
            type="button"
            onClick={retry}
            className="rounded-md bg-rose-600 px-3 py-1.5 text-white transition hover:bg-rose-700"
          >
            Retry
          </button>
        </div>
      )}
    </div>
  );
}
