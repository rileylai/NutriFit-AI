export interface UserMetrics {
  weight: number;
  height: number;
  bmi: number;
  bmr: number;
  bodyFat: number;
  weightChange: number;
  weightTrend: string;
  lastUpdated: string;
}

export interface DailyIntake {
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  fiber: number;
  sodium: number;
}

export interface DailyTargets {
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  fiber: number;
  sodium: number;
}

export interface Meal {
  id: string;
  name: string;
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  time: string;
  type: 'breakfast' | 'lunch' | 'dinner' | 'snack';
}

export interface Workout {
  id: string;
  name: string;
  duration: number;
  caloriesBurned: number;
  type: string;
  date: string;
}

export interface AIInsight {
  id: string;
  title: string;
  content: string;
  type: 'nutrition' | 'exercise' | 'recovery';
  priority: 'high' | 'medium' | 'low';
  timestamp: string;
  expired: boolean;
}

export interface ProgressData {
  date: string;
  weight: number;
  calories: number;
  workouts: number;
}

export interface WeeklyAverages {
  avgCaloriesIntake: number;
  avgMacros: {
    protein: number;
    carbs: number;
    fats: number;
  };
  avgWorkoutDuration: number;
  avgCaloriesBurned: number;
  periodDays: number;
  totalCaloriesIntake?: number;
  totalWorkoutDuration?: number;
  totalCaloriesBurned?: number;
  workoutCount?: number;
}

export interface MonthlyAverages {
  avgCaloriesIntake: number;
  avgMacros: {
    protein: number;
    carbs: number;
    fats: number;
  };
  avgWorkoutDuration: number;
  avgCaloriesBurned: number;
  periodDays: number;
  totalCaloriesIntake?: number;
  totalWorkoutDuration?: number;
  totalCaloriesBurned?: number;
  workoutCount?: number;
}

export interface WorkoutFrequency {
  workoutDays: number;
  totalDays: number;
  frequencyPercentage: number;
  workoutTypes: Record<string, number>;
  totalWorkouts: number;
}

export interface Streaks {
  workoutStreak: number;
  nutritionStreak: number;
  consistencyStreak: number;
  workoutStreakStatus: string;
  nutritionStreakStatus: string;
}

export type DashboardPeriod = 'weekly' | 'monthly';
