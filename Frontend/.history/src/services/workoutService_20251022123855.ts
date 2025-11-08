import type { AiWorkoutEstimateRequest, AiWorkoutResponse, AiWorkoutSummary } from '../types/AiWorkout';
import type { Workout, WorkoutRequest, WorkoutResponseMessage, WorkoutSortOption } from '../types/Workout';
import { apiClient, toError } from './apiClient';

type FetchWorkoutsParams = {
  // user id is no longer required because the API scopes results via the bearer token.
  workoutDate?: string;
  sortBy?: WorkoutSortOption;
};

type AiWorkoutSummaryRaw = {
  duration_minutes?: number | string | null;
  exercise_type?: string | null;
  calories_burned?: number | string | null;
} | null;

type AiWorkoutResponseRaw = {
  success?: boolean;
  message?: string;
  data?: AiWorkoutSummaryRaw;
};

function transformWorkout(apiWorkout: Record<string, unknown>): Workout {
  return {
    workoutId: Number(apiWorkout.workoutId),
    workoutType: String(apiWorkout.workoutType ?? ''),
    workoutDate: String(apiWorkout.workoutDate ?? ''),
    durationMinutes: Number(apiWorkout.durationMinutes ?? 0),
    caloriesBurned: Number(apiWorkout.caloriesBurned ?? 0),
    notes: apiWorkout.notes === null || apiWorkout.notes === undefined ? null : String(apiWorkout.notes),
    createdAt: String(apiWorkout.createdAt ?? ''),
    updatedAt: String(apiWorkout.updatedAt ?? ''),
  };
}

function transformAiSummary(raw: AiWorkoutSummaryRaw): AiWorkoutSummary | null {
  if (!raw) {
    return null;
  }

  const durationValue = raw.duration_minutes;
  const caloriesValue = raw.calories_burned;

  const durationMinutes = typeof durationValue === 'number' ? durationValue : Number(durationValue ?? 0);
  const caloriesBurned = typeof caloriesValue === 'number' ? caloriesValue : Number(caloriesValue ?? 0);

  return {
    durationMinutes: Number.isFinite(durationMinutes) ? durationMinutes : 0,
    exerciseType: raw.exercise_type ?? 'Unknown',
    caloriesBurned: Number.isFinite(caloriesBurned) ? caloriesBurned : 0,
  };
}

function buildAiRequestBody(payload: AiWorkoutEstimateRequest) {
  const body: Record<string, unknown> = {
    description: payload.description,
    save: payload.save ?? true,
  };

  if (payload.workoutDate) {
    body.workout_date = payload.workoutDate;
  }

  if (payload.notes !== undefined) {
    body.notes = payload.notes ?? null;
  }

  return body;
}

export async function fetchWorkouts(params: FetchWorkoutsParams): Promise<Workout[]> {
  try {
    const response = await apiClient.get<unknown>('/api/workouts', {
      params: {
        workout_date: params.workoutDate,
        sort_by: params.sortBy,
      },
    });

    if (!Array.isArray(response.data)) {
      throw new Error('Unexpected workout response format.');
    }

    return response.data.map((item: unknown) => {
      if (item && typeof item === 'object') {
        return transformWorkout(item as Record<string, unknown>);
      }
      throw new Error('Encountered invalid workout entry in response.');
    });
  } catch (error) {
    throw toError(error);
  }
}

export async function createWorkout(payload: WorkoutRequest): Promise<WorkoutResponseMessage> {
  try {
    const { data } = await apiClient.post<WorkoutResponseMessage>('/api/workouts', {
      workoutType: payload.workoutType,
      workoutDate: payload.workoutDate,
      durationMinutes: payload.durationMinutes,
      caloriesBurned: payload.caloriesBurned,
      notes: payload.notes ?? null,
    });

    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function updateWorkout(workoutId: number, payload: WorkoutRequest): Promise<WorkoutResponseMessage> {
  try {
    const { data } = await apiClient.put<WorkoutResponseMessage>(`/api/workouts/${workoutId}`, {
      workoutType: payload.workoutType,
      workoutDate: payload.workoutDate,
      durationMinutes: payload.durationMinutes,
      caloriesBurned: payload.caloriesBurned,
      notes: payload.notes ?? null,
    });

    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function deleteWorkout(workoutId: number): Promise<WorkoutResponseMessage> {
  try {
  const { data } = await apiClient.delete<WorkoutResponseMessage>(`/api/workouts/${workoutId}`);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function estimateWorkoutWithAi(payload: AiWorkoutEstimateRequest): Promise<AiWorkoutResponse> {
  try {
  const { data } = await apiClient.post<AiWorkoutResponseRaw>('/api/workouts/ai', buildAiRequestBody(payload));

    const summary = transformAiSummary(data?.data ?? null);
    const message =
      typeof data?.message === 'string' && data.message.trim().length > 0
        ? data.message
        : summary
          ? 'Calorie estimation successful.'
          : 'Calorie estimation request completed.';

    return {
      success: Boolean(data?.success),
      message,
      data: summary,
    };
  } catch (error) {
    throw toError(error);
  }
}
