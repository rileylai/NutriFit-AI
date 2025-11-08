export type WorkoutSortOption = 'date_asc' | 'date_desc' | 'type' | 'calories_desc';

export interface Workout {
  workoutId: number;
  workoutType: string;
  workoutDate: string;
  durationMinutes: number;
  caloriesBurned: number;
  notes?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WorkoutRequest {
  // user id is omitted because the backend derives ownership from the bearer token.
  workoutType: string;
  workoutDate: string;
  durationMinutes: number;
  caloriesBurned: number;
  notes?: string | null;
}

export interface WorkoutResponseMessage {
  success: boolean;
  message: string;
  
}
