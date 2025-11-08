import axios from 'axios';
import type {
  MetricsHistoryRequest,
  PageResponse,
  UpsertUserProfilePayload,
  UserMetrics,
  UserMetricsPayload,
  UserProfile,
} from '../types/Profile';
import { apiClient, toError } from './apiClient';

type RawMetrics = Record<string, unknown>;

type RawUserProfile = {
  userId?: unknown;
  birthDate?: unknown;
  gender?: unknown;
};

type RawPageResponse = {
  currentPage?: unknown;
  pageSize?: unknown;
  totalItems?: unknown;
  totalPages?: unknown;
  data?: unknown;
  hasPrevious?: unknown;
  hasNext?: unknown;
};

const toNumberOrNull = (value: unknown): number | null => {
  if (value === null || value === undefined) {
    return null;
  }

  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : null;
  }

  if (typeof value === 'string' && value.trim().length > 0) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  return null;
};

const toNumber = (value: unknown, fallback: number): number => {
  const parsed = toNumberOrNull(value);
  return parsed ?? fallback;
};

const toStringOrNull = (value: unknown): string | null => {
  if (value === null || value === undefined) {
    return null;
  }

  if (typeof value === 'string') {
    return value;
  }

  return String(value);
};

const toBoolean = (value: unknown): boolean => {
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    if (normalized === 'true') {
      return true;
    }
    if (normalized === 'false') {
      return false;
    }
  }
  if (typeof value === 'number') {
    return value !== 0;
  }
  return Boolean(value);
};

function transformUserMetrics(raw: RawMetrics): UserMetrics {
  return {
    metricId: toNumber(raw.metricId, 0),
    heightCm: toNumberOrNull(raw.heightCm),
    weightKg: toNumberOrNull(raw.weightKg),
    age: toNumberOrNull(raw.age),
    gender: toStringOrNull(raw.gender),
    bmi: toNumberOrNull(raw.bmi),
    bmr: toNumberOrNull(raw.bmr),
    userGoal: toStringOrNull(raw.userGoal),
    recordAt: toStringOrNull(raw.recordAt),
    createdAt: toStringOrNull(raw.createdAt),
  };
}

function transformUserProfile(raw: RawUserProfile): UserProfile {
  return {
    userId: toNumberOrNull(raw.userId),
    birthDate: toStringOrNull(raw.birthDate),
    gender: toStringOrNull(raw.gender),
  };
}

function transformPageResponse(raw: RawPageResponse): PageResponse<UserMetrics> {
  const data = Array.isArray(raw.data)
    ? raw.data
        .filter((item): item is RawMetrics => Boolean(item && typeof item === 'object'))
        .map((item) => transformUserMetrics(item))
    : [];

  return {
    currentPage: toNumber(raw.currentPage, 1),
    pageSize: toNumber(raw.pageSize, data.length),
    totalItems: toNumber(raw.totalItems, data.length),
    totalPages: toNumber(raw.totalPages, 1),
    data,
    hasPrevious: toBoolean(raw.hasPrevious),
    hasNext: toBoolean(raw.hasNext),
  };
}

const buildMetricsRequestBody = (payload: UserMetricsPayload) => ({
  heightCm: payload.heightCm,
  weightKg: payload.weightKg,
  age: payload.age,
  gender: payload.gender ?? null,
  userGoal: payload.userGoal ?? null,
  recordAt: payload.recordAt ?? null,
});

const buildUserProfileRequestBody = (payload: UpsertUserProfilePayload) => ({
  birthDate: payload.birthDate ?? null,
  gender: payload.gender ?? null,
});

export async function fetchLatestMetrics(): Promise<UserMetrics | null> {
  try {
    const { data } = await apiClient.get<RawMetrics>('/api/homepage/profile');
    return transformUserMetrics(data ?? {});
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw toError(error);
  }
}

export async function fetchMetricsHistory(request: MetricsHistoryRequest = {}): Promise<PageResponse<UserMetrics>> {
  try {
    const { data } = await apiClient.post<RawPageResponse>('/api/homepage/profile/history', {
      page: request.page,
      size: request.size,
      sortBy: request.sortBy,
      sortDirection: request.sortDirection,
      startDate: request.startDate,
      endDate: request.endDate,
    });

    return transformPageResponse(data ?? {});
  } catch (error) {
    throw toError(error);
  }
}

export async function createMetrics(payload: UserMetricsPayload): Promise<UserMetrics> {
  try {
    const { data } = await apiClient.post<RawMetrics>('/api/homepage/profile', buildMetricsRequestBody(payload));
    return transformUserMetrics(data ?? {});
  } catch (error) {
    throw toError(error);
  }
}

export async function updateMetrics(metricId: number, payload: UserMetricsPayload): Promise<UserMetrics> {
  try {
    const { data } = await apiClient.put<RawMetrics>(
      `/api/homepage/profile/${metricId}`,
      buildMetricsRequestBody(payload),
    );
    return transformUserMetrics(data ?? {});
  } catch (error) {
    throw toError(error);
  }
}

export async function fetchUserProfile(): Promise<UserProfile | null> {
  try {
    const { data } = await apiClient.get<RawUserProfile>('/api/users/me/profile');
    return transformUserProfile(data ?? {});
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw toError(error);
  }
}

export async function upsertUserProfile(payload: UpsertUserProfilePayload): Promise<UserProfile> {
  try {
    const { data } = await apiClient.put<RawUserProfile>(
      '/api/users/me/profile',
      buildUserProfileRequestBody(payload),
    );
    return transformUserProfile(data ?? {});
  } catch (error) {
    throw toError(error);
  }
}
