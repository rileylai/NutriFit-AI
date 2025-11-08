import { apiClient, toError } from './apiClient';

// AI Insights Interfaces
export interface AIInsightDTO {
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

export interface LatestInsightsResponse {
  insights: AIInsightDTO[];
  totalCount: number;
  hasNewInsights: boolean;
  latestSuggestion?: SuggestionContent | null;
}

export interface CreateInsightRequestDTO {
  analysisType: 'nutrition' | 'exercise' | 'general';
  forceRegenerate?: boolean;
}

export interface GenerateInsightResponse {
  insight: AIInsightDTO;
  message: string;
}

export interface DismissInsightResponse {
  message: string;
  success: boolean;
}

export interface SuggestionRequestDTO {
  suggestionType: 'exercise' | 'diet';
  userGoal?: string;
  timeFrame?: 'day' | 'week' | 'month';
  intensity?: 'low' | 'moderate' | 'high';
  experienceLevel?: 'beginner' | 'intermediate' | 'advanced';
  focusAreas?: string[];
  availableEquipment?: string[];
  dietaryPreferences?: string[];
  restrictions?: string[];
  schedule?: {
    daysPerWeek?: number;
    minutesPerSession?: number;
    preferredTimes?: string[];
  };
  notes?: string;
}

export interface SuggestionContent {
  requestId?: string | null;
  timestamp?: string | null;
  suggestionType?: string | null;
  userGoal?: string | null;
  timeFrame?: string | null;
  suggestionId?: string | number | null;
  id?: string | number | null;
  recommendations?: string[];
  specificMetrics?: Record<string, string>;
  rationale?: string | null;
  confidenceScore?: number | null;
  title?: string | null;
  summary?: string | null;
  plan?: string | null;
  createdAt?: string | null;
  meta?: Record<string, unknown>;
  [key: string]: unknown;
}

export interface SuggestionResponse {
  requestId?: string;
  timestamp?: string;
  suggestions?: SuggestionContent;
  // Some responses may return the suggestion payload at the root level
  latestSuggestion?: SuggestionContent | null;
  suggestionId?: string | number | null;
  id?: string | number | null;
  suggestionType?: string | null;
  userGoal?: string | null;
  timeFrame?: string | null;
  recommendations?: string[];
  specificMetrics?: Record<string, string>;
  rationale?: string | null;
  confidenceScore?: number | null;
  title?: string | null;
  summary?: string | null;
  plan?: string | null;
  createdAt?: string | null;
  meta?: Record<string, unknown>;
  [key: string]: unknown;
}

export async function getLatestInsights(): Promise<LatestInsightsResponse> {
  try {
    const response = await apiClient.get<LatestInsightsResponse>('/api/homepage/ai-insights/latest');

    return response.data;
  } catch (error) {
    throw toError(error);
  }
}

export async function generateInsight(request: CreateInsightRequestDTO): Promise<GenerateInsightResponse> {
  try {
    const response = await apiClient.post<GenerateInsightResponse>('/api/homepage/ai-insights/generate', request);

    return response.data;
  } catch (error) {
    throw toError(error);
  }
}

export async function dismissInsight(insightId: number): Promise<DismissInsightResponse> {
  try {
    const response = await apiClient.delete<DismissInsightResponse>(`/api/homepage/ai-insights/${insightId}`);
    return response.data;
  } catch (error) {
    throw toError(error);
  }
}

export async function createSuggestion(request: SuggestionRequestDTO): Promise<SuggestionResponse> {
  try {
    const response = await apiClient.post<SuggestionResponse>('/api/homepage/ai-insights/suggestions', request);
    return response.data;
  } catch (error) {
    throw toError(error);
  }
}

export interface SuggestionQueryParams {
  type?: 'exercise' | 'diet';
  goal?: string;
  timeFrame?: 'day' | 'week' | 'month';
}

export async function getSuggestion(params: SuggestionQueryParams): Promise<SuggestionResponse> {
  try {
    const response = await apiClient.get<SuggestionResponse>(
      '/api/homepage/ai-insights/suggestions',
      { params }
    );
    return response.data;
  } catch (error) {
    throw toError(error);
  }
}

export interface DeleteSuggestionResponse {
  message?: string;
  success?: boolean;
  [key: string]: unknown;
}

export async function deleteSuggestion(
  suggestionId: number
): Promise<DeleteSuggestionResponse> {
  try {
    const response = await apiClient.delete<DeleteSuggestionResponse>(
      `/api/homepage/ai-insights/suggestions/${suggestionId}`
    );
    return response.data;
  } catch (error) {
    throw toError(error);
  }
}
