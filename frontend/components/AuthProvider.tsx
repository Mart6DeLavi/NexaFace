"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  type AuthResponse,
  type User,
  getCurrentUser,
  loginUser,
  registerUser,
} from "@/services/api";
import { clearToken, readToken, writeToken } from "@/lib/session";

type AuthContextValue = {
  user: User | null;
  token: string | null;
  ready: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function restoreSession() {
      const stored = readToken();
      if (!stored) {
        if (!cancelled) {
          setReady(true);
        }
        return;
      }

      try {
        const currentUser = await getCurrentUser(stored);
        if (!cancelled) {
          setToken(stored);
          setUser(currentUser);
        }
      } catch {
        clearToken();
      } finally {
        if (!cancelled) {
          setReady(true);
        }
      }
    }

    void restoreSession();

    return () => {
      cancelled = true;
    };
  }, []);

  const applyAuth = useCallback((response: AuthResponse) => {
    writeToken(response.accessToken);
    setToken(response.accessToken);
    setUser(response.user);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      applyAuth(await loginUser(email, password));
    },
    [applyAuth],
  );

  const register = useCallback(
    async (email: string, password: string) => {
      applyAuth(await registerUser(email, password));
    },
    [applyAuth],
  );

  const logout = useCallback(() => {
    clearToken();
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, token, ready, login, register, logout }),
    [user, token, ready, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
