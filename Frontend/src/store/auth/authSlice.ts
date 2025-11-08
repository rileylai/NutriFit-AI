import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

export interface AuthUser {
  uuid: string;
  email: string;
  userName: string;
  emailVerified: boolean;
}

export interface AuthState {
  token: string | null;
  user: AuthUser | null;
}

const TOKEN_STORAGE_KEY = 'auth.token';
const USER_STORAGE_KEY = 'auth.user';

function readStoredToken(): string | null {
  try {
    const stored = sessionStorage.getItem(TOKEN_STORAGE_KEY);
    return stored && stored.length > 0 ? stored : null;
  } catch {
    return null;
  }
}

function readStoredUser(): AuthUser | null {
  try {
    const raw = sessionStorage.getItem(USER_STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as Partial<AuthUser>;
      if (
        parsed &&
        typeof parsed.uuid === 'string' &&
        typeof parsed.email === 'string' &&
        typeof parsed.userName === 'string' &&
        typeof parsed.emailVerified === 'boolean'
      ) {
        return parsed as AuthUser;
      }
    }
    return null;
  } catch {
    return null;
  }
}

const initialState: AuthState = {
  token: readStoredToken(),
  user: readStoredUser(),
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action: PayloadAction<{ token: string; user: AuthUser }>) => {
      state.token = action.payload.token;
      state.user = action.payload.user;
      try {
        sessionStorage.setItem(TOKEN_STORAGE_KEY, action.payload.token);
        sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(action.payload.user));
      } catch {
        // Ignore storage errors (e.g. private browsing) and keep state in memory.
      }
    },
    clearCredentials: (state) => {
      state.token = null;
      state.user = null;
      try {
        sessionStorage.removeItem(TOKEN_STORAGE_KEY);
        sessionStorage.removeItem(USER_STORAGE_KEY);
      } catch {
        // Ignore storage errors.
      }
    },
  },
});

export const { setCredentials, clearCredentials } = authSlice.actions;
export default authSlice.reducer;
