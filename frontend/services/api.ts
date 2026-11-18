const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "http://localhost:8080";

export type HealthResponse = {
  status: string;
  service: string;
};

export type User = {
  id: string;
  email: string;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
};

export type FaceProfileStatus = {
  enrolled: boolean;
  updatedAt: string | null;
};

type FieldErrors = Record<string, string>;

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
    readonly details?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }

  get fieldErrors(): FieldErrors | undefined {
    if (
      this.details &&
      typeof this.details === "object" &&
      !Array.isArray(this.details)
    ) {
      return this.details as FieldErrors;
    }
    return undefined;
  }
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  token?: string | null;
  signal?: AbortSignal;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, token, signal } = options;
  const headers: Record<string, string> = { Accept: "application/json" };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
      cache: "no-store",
    });
  } catch {
    throw new ApiError(0, "NETWORK_ERROR", "Cannot reach the NextFace backend.");
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    const code =
      payload && typeof payload.error === "string"
        ? payload.error
        : `HTTP_${response.status}`;
    const message =
      payload && typeof payload.message === "string"
        ? payload.message
        : "The request failed.";
    throw new ApiError(response.status, code, message, payload?.details);
  }

  return payload as T;
}

/**
 * Checks that the NextFace backend is reachable.
 */
export async function getHealth(signal?: AbortSignal): Promise<HealthResponse> {
  return request<HealthResponse>("/api/health", { signal });
}

export async function registerUser(
  email: string,
  password: string,
): Promise<AuthResponse> {
  return request<AuthResponse>("/api/auth/register", {
    method: "POST",
    body: { email, password },
  });
}

export async function loginUser(
  email: string,
  password: string,
): Promise<AuthResponse> {
  return request<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: { email, password },
  });
}

export async function getCurrentUser(token: string): Promise<User> {
  return request<User>("/api/auth/me", { token });
}

export async function getFaceProfile(token: string): Promise<FaceProfileStatus> {
  return request<FaceProfileStatus>("/api/face/profile", { token });
}

export { API_BASE_URL };
