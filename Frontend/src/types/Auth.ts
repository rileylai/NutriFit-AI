export interface AuthUserResponse {
  uuid: string;
  email: string;
  userName: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  token?: string | null;
  tokenType?: string | null;
  expiresIn?: number | null;
  user?: AuthUserResponse | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  userName: string;
  password: string;
}

export interface EmailVerificationRequest {
  token: string;
}

export interface ResendVerificationRequest {
  email: string;
}

export interface EmailVerificationResponse {
  success: boolean;
  message: string;
  emailVerified: boolean | null;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface PasswordResetRequest {
  token: string;
  newPassword: string;
}

export interface PasswordResetResponse {
  success: boolean;
  message: string;
}
