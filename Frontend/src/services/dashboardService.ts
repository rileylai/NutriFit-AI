import type { DashboardPeriod, ProgressData } from '../types/HomePage';
import { apiClient, toError } from './apiClient';

export interface BackendQuickStatsResponse {
  bodyMetrics: {
    weight: number;
    height: number;
    bmi: number;
    bmr: number;
    weightChange: number;
    weightTrend: string;
    lastUpdated: string;
  };
  weeklyAverages: {
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
  };
  monthlyAverages: {
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
  };
  workoutFrequency: {
    workoutDays: number;
    totalDays: number;
    frequencyPercentage: number;
    workoutTypes: Record<string, number>;
    totalWorkouts: number;
  };
  streaks: {
    workoutStreak: number;
    nutritionStreak: number;
    consistencyStreak: number;
    workoutStreakStatus: string;
    nutritionStreakStatus: string;
  };
}

export interface QuickStatsResponse {
  userMetrics: {
    weight: number;
    height: number;
    bmi: number;
    bmr: number;
    bodyFat: number;
    weightChange: number;
    weightTrend: string;
    lastUpdated: string;
  };
  currentIntake: {
    calories: number;
    protein: number;
    carbs: number;
    fat: number;
    fiber: number;
    sodium: number;
  };
  dailyTargets: {
    calories: number;
    protein: number;
    carbs: number;
    fat: number;
    fiber: number;
    sodium: number;
  };
  weeklyAverages: {
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
  };
  monthlyAverages: {
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
  };
  workoutFrequency: {
    workoutDays: number;
    totalDays: number;
    frequencyPercentage: number;
    workoutTypes: Record<string, number>;
    totalWorkouts: number;
  };
  streaks: {
    workoutStreak: number;
    nutritionStreak: number;
    consistencyStreak: number;
    workoutStreakStatus: string;
    nutritionStreakStatus: string;
  };
  activeStreak: number;
  weeklyWorkouts: number;
  weeklyWorkoutGoal: number;
}

export interface ErrorResponse {
  error_code: string;
  message: string;
}

// Export Data Interfaces
export interface ExportDataResponse {
  metadata: {
    dateRange: string;
    totalDataPoints: number;
    exportTimestamp: string;
    exportFormat: string;
  };
  achievements: {
    completedAchievements: Achievement[];
    inProgressAchievements: Achievement[];
    currentStreaks: {
      workoutStreak: number;
      nutritionStreak: number;
      consistencyStreak: number;
      workoutStreakStatus: string;
      nutritionStreakStatus: string;
    };
    milestones: Milestone[];
    totalAchievements: number;
    totalPoints: number;
    currentLevel: string;
    motivationalMessage: string;
  };
  progressMetrics: {
    dateRange: string;
    weightProgress: {
      startWeight: number;
      currentWeight: number;
      weightChange: number;
      trend: string;
      targetWeight: number;
      progressToTarget: number;
    };
    fitnessProgress: {
      totalWorkouts: number;
      avgWorkoutDuration: number;
      totalCaloriesBurned: number;
      fitnessLevel: string;
      enduranceImprovement: number;
      strengthImprovement: number;
    };
    nutritionProgress: {
      avgDailyCalories: number;
      avgMacros: {
        protein: number;
        carbs: number;
        fats: number;
      };
      targetAdherence: number;
      nutritionQuality: string;
      consistentDays: number;
    };
    weeklyTrends: WeeklyTrend[];
    overallProgressRating: string;
    milestones: string[];
    goalProgressStatus: string;
  };
  nutritionSummary: {
    dateRange: string;
    totalCaloriesConsumed: number;
    avgDailyCalories: number;
    totalMacros: {
      protein: number;
      carbs: number;
      fats: number;
    };
    avgDailyMacros: {
      protein: number;
      carbs: number;
      fats: number;
    };
    totalMeals: number;
    avgMealsPerDay: number;
    dailyBreakdown: DailyNutritionBreakdown[];
    calorieTargetProgress: number;
    nutritionGoalStatus: string;
  };
  aiInsights: AIInsight[];
  userProfile: {
    username: string;
    email: string;
    currentWeight: number;
    currentHeight: number;
    currentBMI: number;
    currentBMR: number;
    age: number;
    gender: string;
    activityLevel: string;
    profileLastUpdated: string;
  };
  exerciseHistory: {
    dateRange: string;
    totalWorkouts: number;
    totalWorkoutDays: number;
    totalCaloriesBurned: number;
    totalDurationMinutes: number;
    avgWorkoutDuration: number;
    avgCaloriesBurnedPerWorkout: number;
    workoutTypeDistribution: Record<string, number>;
    dailyBreakdown: DailyExerciseBreakdown[];
    consistencyRating: string;
    weeklyFrequencyPercentage: number;
  };
}

interface Achievement {
  achievementId: string;
  title: string;
  description: string;
  category: string;
  points: number;
  status: string;
  progress: number;
  achievedAt: string | null;
  icon: string;
}

interface Milestone {
  milestoneId: string;
  title: string;
  description: string;
  metric: string;
  targetValue: number;
  currentValue: number;
  progress: number;
  achievedAt: string | null;
  status: string;
}

interface WeeklyTrend {
  weekStartDate: string;
  weeklyWeightChange: number;
  weeklyWorkouts: number;
  weeklyCaloriesBurned: number;
  weeklyAvgCaloriesIntake: number;
  weeklyProgressRating: string;
}

interface DailyNutritionBreakdown {
  date: string;
  dailyCalories: number;
  dailyMacros: {
    protein: number;
    carbs: number;
    fats: number;
  };
  mealsCount: number;
}

interface AIInsight {
  insightId: number;
  content: string;
  suggestionFormat: string;
  isActive: boolean;
  expiresAt: string;
  createdAt: string;
  updatedAt: string;
  status: string;
  category: string;
  priority: number;
}

interface DailyExerciseBreakdown {
  date: string;
  workoutsCount: number;
  totalDuration: number;
  totalCaloriesBurned: number;
  workoutTypes: string[];
}

function transformBackendResponse(backendData: BackendQuickStatsResponse): QuickStatsResponse {
  return {
    userMetrics: {
      weight: backendData.bodyMetrics.weight,
      height: backendData.bodyMetrics.height,
      bmi: backendData.bodyMetrics.bmi,
      bmr: backendData.bodyMetrics.bmr,
      bodyFat: 0, // Not provided by backend, using default
      weightChange: backendData.bodyMetrics.weightChange,
      weightTrend: backendData.bodyMetrics.weightTrend,
      lastUpdated: backendData.bodyMetrics.lastUpdated,
    },
    currentIntake: {
      calories: backendData.weeklyAverages.avgCaloriesIntake,
      protein: backendData.weeklyAverages.avgMacros.protein,
      carbs: backendData.weeklyAverages.avgMacros.carbs,
      fat: backendData.weeklyAverages.avgMacros.fats,
      fiber: 0, // Not provided by backend
      sodium: 0, // Not provided by backend
    },
    dailyTargets: {
      calories: 2200, // Default values since not provided by backend
      protein: 140,
      carbs: 275,
      fat: 73,
      fiber: 25,
      sodium: 2300,
    },
    weeklyAverages: backendData.weeklyAverages,
    monthlyAverages: backendData.monthlyAverages,
    workoutFrequency: backendData.workoutFrequency,
    streaks: backendData.streaks,
    activeStreak: backendData.streaks.workoutStreak,
    weeklyWorkouts: backendData.workoutFrequency.totalWorkouts,
    weeklyWorkoutGoal: 6, // Default goal since not provided by backend
  };
}

export type QuickStatsPeriod = DashboardPeriod;

export async function getQuickStats(
  period: QuickStatsPeriod = 'weekly'
): Promise<QuickStatsResponse> {
  try {
    const response = await apiClient.get<BackendQuickStatsResponse>(
      '/api/homepage/dashboard/quick-stats',
      {
        params: { period }
      }
    );

    return transformBackendResponse(response.data);
  } catch (error) {
    throw toError(error);
  }
}

export async function exportDashboardData(
  format: 'json' | 'pdf' = 'json',
  dateRange: string = '30d'
): Promise<ExportDataResponse | Blob> {
  try {
    // Determine endpoint based on format
    const endpoint = format === 'pdf'
      ? '/api/homepage/dashboard/export-data-pdf'
      : '/api/homepage/dashboard/export-data-json';

    // For PDF, expect blob response
    if (format === 'pdf') {
      const response = await apiClient.get(endpoint, {
        params: {
          dateRange
        },
        responseType: 'blob'
      });
      return response.data;
    }

    // For JSON, expect ExportDataResponse
    const response = await apiClient.get<ExportDataResponse>(endpoint, {
      params: {
        dateRange
      }
    });

    return response.data;
  } catch (error) {
    throw toError(error);
  }
}

export async function downloadExportData(
  format: 'json' | 'pdf' = 'json',
  dateRange: string = '30d'
): Promise<void> {
  try {
    const data = await exportDashboardData(format, dateRange);

    let blob: Blob;
    let filename: string;

    if (format === 'pdf') {
      // Data is already a Blob for PDF
      blob = data as Blob;
      filename = `nutrifit-export-${new Date().toISOString()}.pdf`;
    } else {
      // For JSON, convert to blob
      const jsonData = data as ExportDataResponse;
      blob = new Blob([JSON.stringify(jsonData, null, 2)], {
        type: 'application/json'
      });
      filename = `nutrifit-export-${jsonData.metadata.exportTimestamp}.json`;
    }

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    throw toError(error);
  }
}

// Daily progress series for charts
export async function getDailyProgress(
  dateRange: string = '30d'
): Promise<ProgressData[]> {
  try {
    const { data } = await apiClient.get<Array<{ date: string; weight: number; calories: number; workouts: number }>>(
      '/api/homepage/dashboard/daily-progress',
      { params: { dateRange } }
    );

    // Backend returns LocalDate strings; keep as-is for charts
    return data.map(p => ({
      date: p.date,
      weight: p.weight ?? 0,
      calories: p.calories ?? 0,
      workouts: p.workouts ?? 0,
    }));
  } catch (error) {
    throw toError(error);
  }
}
