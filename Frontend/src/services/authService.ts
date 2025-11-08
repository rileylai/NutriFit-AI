import type {
  AuthResponse,
  EmailVerificationRequest,
  EmailVerificationResponse,
  ForgotPasswordRequest,
  LoginRequest,
  PasswordResetRequest,
  PasswordResetResponse,
  RegisterRequest,
  ResendVerificationRequest,
} from '../types/Auth';
import { apiClient, toError } from './apiClient';

const AUTH_ROOT = '/api/auth';

export async function registerUser(payload: RegisterRequest): Promise<AuthResponse> {
  try {
    const { data } = await apiClient.post<AuthResponse>(`${AUTH_ROOT}/register`, payload);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function loginUser(payload: LoginRequest): Promise<AuthResponse> {
  try {
    const { data } = await apiClient.post<AuthResponse>(`${AUTH_ROOT}/login`, payload);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function verifyEmail(payload: EmailVerificationRequest): Promise<EmailVerificationResponse> {
  try {
    const { data } = await apiClient.post<EmailVerificationResponse>(`${AUTH_ROOT}/email-verify`, payload);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function resendVerificationEmail(
  payload: ResendVerificationRequest,
): Promise<EmailVerificationResponse> {
  try {
    const { data } = await apiClient.post<EmailVerificationResponse>(`${AUTH_ROOT}/resend-verification`, payload);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function requestPasswordReset(payload: ForgotPasswordRequest): Promise<PasswordResetResponse> {
  try {
    const { data } = await apiClient.post<PasswordResetResponse>(`${AUTH_ROOT}/forgot-password`, payload);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function resetPassword(payload: PasswordResetRequest): Promise<PasswordResetResponse> {
  try {
    const { data } = await apiClient.post<PasswordResetResponse>(`${AUTH_ROOT}/reset-password`, payload);
    return data;
  } catch (error) {
    throw toError(error);
  }
}
