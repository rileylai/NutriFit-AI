import axios, { AxiosError } from 'axios';
import { store } from '../store/store';

const DEFAULT_BASE_URL = 'http://localhost:8080';
const RAW_API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? DEFAULT_BASE_URL;

const normalizeBaseUrl = (url: string): string => {
  if (!url) {
    return DEFAULT_BASE_URL;
  }
  return url.replace(/\/+$/, '');
};

export const API_BASE_URL = normalizeBaseUrl(RAW_API_BASE_URL);

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

apiClient.interceptors.request.use(
    config => {
        const token = store.getState().auth.token;
        if (token) {
            config.headers['Authorization'] = `${token}`;
        }
        return config;
    });

export function extractServerError(error: AxiosError): string | undefined {
  const data = error.response?.data;

  if (data && typeof data === 'object') {
    const maybeMessage = (data as Record<string, unknown>).message;
    if (typeof maybeMessage === 'string' && maybeMessage.trim().length > 0) {
      return maybeMessage;
    }
  }

  if (typeof data === 'string' && data.trim().length > 0) {
    return data;
  }

  return undefined;
}

export function toError(error: unknown): Error {
  if (axios.isAxiosError(error)) {
    const message = extractServerError(error) ?? error.message;
    return new Error(message || 'Request failed.');
  }

  return error instanceof Error ? error : new Error('Request failed.');
}
