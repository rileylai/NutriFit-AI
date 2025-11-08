export interface UserMetrics {
  metricId: number;
  heightCm: number | null;
  weightKg: number | null;
  age: number | null;
  gender: string | null;
  bmi: number | null;
  bmr: number | null;
  userGoal: string | null;
  recordAt: string | null;
  createdAt: string | null;
}

export interface UserProfile {
  userId: number | null;
  birthDate: string | null;
  gender: string | null;
}

export interface UpsertUserProfilePayload {
  birthDate: string | null;
  gender: string | null;
}

export interface UserMetricsPayload {
  heightCm: number | null;
  weightKg: number | null;
  age?: number | null;
  gender?: string | null;
  userGoal: string | null;
  recordAt: string | null;
}

export interface MetricsHistoryRequest {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC' | string;
  startDate?: string;
  endDate?: string;
}

export interface PageResponse<T> {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  data: T[];
  hasPrevious: boolean;
  hasNext: boolean;
}
