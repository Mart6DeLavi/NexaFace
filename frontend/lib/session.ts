const TOKEN_KEY = "nextface.token";

/**
 * JWT storage for the prototype. Keeping the token in localStorage makes it
 * readable by any script on the page, which is acceptable for this educational
 * project but would be replaced by httpOnly cookies in production.
 */
export function readToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return window.localStorage.getItem(TOKEN_KEY);
}

export function writeToken(token: string): void {
  window.localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  window.localStorage.removeItem(TOKEN_KEY);
}
