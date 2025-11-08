export interface AiWorkoutEstimateRequest {
  // The API authenticates via token, so we no longer forward the numeric user id.
  description: string;
  save?: boolean;
  workoutDate?: string;
  notes?: string | null;
}

export interface AiWorkoutSummary {
  durationMinutes: number;
  exerciseType: string;
  caloriesBurned: number;
}

export interface AiWorkoutResponse {
  success: boolean;
  message: string;
  data: AiWorkoutSummary | null;
}
