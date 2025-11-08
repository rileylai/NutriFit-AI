/**
 * Type definitions for Meal-related data structures
 */

export interface CreateMealRequest {
  mealDescription: string;
  photoUrl?: string;
  totalCalories: number;
  proteinG: number;
  carbsG: number;
  fatG: number;
  role: string; // breakfast, lunch, dinner, snack
  mealTime?: string; // ISO datetime string, optional
  isAiGenerated?: boolean;
}

export interface MealDetail {
  mealId: number;
  mealDescription: string;
  photoUrl?: string;
  totalCalories: number;
  proteinG: number;
  carbsG: number;
  fatG: number;
  role: string;
  mealTime?: string;
  isAiGenerated?: boolean;
  userEdited?: boolean;
}

export interface MealEstimationRequest {
  description: string;
  mealType: string;
}

export interface MealEstimationResponse {
  mealId?: number;
  photoUrl?: string;
  mealDescription: string;
  totalCalories: number;
  proteinG: number;
  carbsG: number;
  fatG: number;
  role: string;
  saved: boolean;
  aiGenerated: boolean;
}

export interface NutrientGap {
  current: number;
  target: number;
  gap: number;
}

export interface NutrientGaps {
  protein: NutrientGap;
  carbs: NutrientGap;
  fat: NutrientGap;
}
