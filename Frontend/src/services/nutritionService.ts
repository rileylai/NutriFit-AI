import axios from 'axios';
import { apiClient, toError } from './apiClient';
import type { Meal as FrontMeal } from '../types/HomePage';
import type {
  CreateMealRequest,
  MealDetail,
  MealEstimationRequest,
  MealEstimationResponse,
  NutrientGaps
} from '../types/Meal';

export interface DailyIntakeSummary {
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
}

interface TodayMealDTO {
  id: number;
  name: string;
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  time: string; // HH:mm
  type: 'breakfast' | 'lunch' | 'dinner' | 'snack';
}

export async function getTodaySummary(): Promise<DailyIntakeSummary> {
  try {
    const { data } = await apiClient.get<DailyIntakeSummary>(
      '/api/homepage/nutrition/today-summary'
    );
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function getTodayMeals(): Promise<FrontMeal[]> {
  try {
    const { data } = await apiClient.get<TodayMealDTO[]>(
      '/api/homepage/nutrition/today-meals'
    );
    // Map backend DTO to frontend Meal shape (id as string)
    return data.map(m => ({
      id: String(m.id),
      name: m.name,
      calories: m.calories,
      protein: m.protein,
      carbs: m.carbs,
      fat: m.fat,
      time: m.time,
      type: m.type,
    }));
  } catch (error) {
    throw toError(error);
  }
}

export async function deleteMeal(mealId: number): Promise<void> {
  try {
    await apiClient.delete('/api/homepage/nutrition/meals/' + mealId);
  } catch (error) {
    throw toError(error);
  }
}

export async function createMeal(request: CreateMealRequest): Promise<MealDetail> {
  try {
    const { data } = await apiClient.post<MealDetail>('/api/homepage/nutrition/meals', request);
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function getAllMeals(): Promise<MealDetail[]> {
  try {
    const { data } = await apiClient.get<MealDetail[]>('/api/homepage/nutrition/meals');
    return data;
  } catch (error) {
    throw toError(error);
  }
}

export async function estimateMeal(request: MealEstimationRequest): Promise<MealEstimationResponse> {
  try {
    const { data } = await apiClient.post<MealEstimationResponse>(
      '/api/homepage/nutrition/estimate',
      request
    );
    return data;
  } catch (error) {
    throw toError(error);
  }
}

/**
 * AI-powered meal estimation with image and/or description
 * @param image Optional image file
 * @param description Optional text description
 * @param role Meal type (breakfast, lunch, dinner, snack)
 * @param save Whether to save the meal (default: true)
 * @returns Estimated meal nutrition data
 */
export async function estimateMealWithAI(
  image: File | null,
  description: string | null,
  role: string,
  save: boolean = true
): Promise<MealEstimationResponse> {
  try {
    console.log('🚀 [AI Meal Estimation] Starting request...');
    console.log('📸 Image:', image ? {
      name: image.name,
      size: `${(image.size / 1024).toFixed(2)} KB`,
      type: image.type
    } : 'No image');
    console.log('📝 Description:', description || 'No description');
    console.log('🍽️ Role:', role);
    console.log('💾 Save:', save);

    const formData = new FormData();

    if (image) {
      formData.append('image', image);
      console.log('✅ Image appended to FormData');
    }

    if (description) {
      formData.append('description', description);
      console.log('✅ Description appended to FormData');
    }

    formData.append('role', role);
    formData.append('save', String(save));

    console.log('📤 Sending POST request to /api/meals/ai/estimate');
    console.log('📦 FormData contents:', Array.from(formData.entries()).map(([key, value]) => {
      if (value instanceof File) {
        return [key, `File(${value.name}, ${value.size} bytes)`];
      }
      return [key, value];
    }));

    const startTime = Date.now();
    const { data } = await apiClient.post<MealEstimationResponse>(
      '/api/meals/ai/estimate',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 60000, // 60 seconds for image upload + AI processing
      }
    );
    const endTime = Date.now();

    console.log(`✅ Request successful! (${endTime - startTime}ms)`);
    console.log('📥 Response:', data);

    return data;
  } catch (error) {
    console.error('❌ [AI Meal Estimation] Request failed:', error);
    if (axios.isAxiosError(error)) {
      console.error('Status:', error.response?.status);
      console.error('Status Text:', error.response?.statusText);
      console.error('Response Data:', error.response?.data);
      console.error('Request URL:', error.config?.url);
      console.error('Request Method:', error.config?.method);
    }
    throw toError(error);
  }
}

export function calculateNutrientGaps(summary: DailyIntakeSummary): NutrientGaps {
  return {
    protein: {
      current: summary.currentIntake.protein,
      target: summary.dailyTargets.protein,
      gap: Math.max(0, summary.dailyTargets.protein - summary.currentIntake.protein),
    },
    carbs: {
      current: summary.currentIntake.carbs,
      target: summary.dailyTargets.carbs,
      gap: Math.max(0, summary.dailyTargets.carbs - summary.currentIntake.carbs),
    },
    fat: {
      current: summary.currentIntake.fat,
      target: summary.dailyTargets.fat,
      gap: Math.max(0, summary.dailyTargets.fat - summary.currentIntake.fat),
    },
  };
}
