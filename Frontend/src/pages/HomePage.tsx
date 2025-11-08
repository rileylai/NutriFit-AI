import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Row, Col, Tabs, message, Spin } from 'antd';
import Header from '../components/HomePageComponents/Header';
import QuickStats from '../components/HomePageComponents/QuickStats';
import DailyIntakeCard from '../components/HomePageComponents/DailyIntakeCard';
import MealsTable from '../components/HomePageComponents/MealsTable';
import AIInsights from '../components/HomePageComponents/AIInsights';
import ProgressCharts from '../components/HomePageComponents/ProgressCharts';
import AIInsightModal from '../components/HomePageComponents/AIInsightModal';
import { getQuickStats, downloadExportData, getDailyProgress, type QuickStatsPeriod } from '../services/dashboardService';
import { getTodayMeals, getTodaySummary, deleteMeal as apiDeleteMeal } from '../services/nutritionService';
import {
  getLatestInsights,
  generateInsight,
  dismissInsight,
  getSuggestion,
  createSuggestion,
  type AIInsightDTO,
  type SuggestionRequestDTO,
  type SuggestionResponse,
  type SuggestionContent,
  deleteSuggestion,
} from '../services/aiinsightService';
import SuggestionFormModal from '../components/HomePageComponents/SuggestionFormModal';
import type {
  UserMetrics,
  DailyIntake,
  DailyTargets,
  Meal,
  ProgressData,
  WeeklyAverages,
  MonthlyAverages,
  WorkoutFrequency,
  Streaks
} from '../types/HomePage';
import { useAppSelector } from '../store/hooks';
import { useNavigate } from 'react-router-dom';

const { TabPane } = Tabs;

type SuggestionOrigin = 'quick' | 'custom' | 'latest';

interface SuggestionDisplayState {
  content: SuggestionContent;
  requestId?: string;
  suggestionId?: string;
  timestamp?: string;
  source: SuggestionOrigin;
}

const buildSuggestionDisplayState = (
  content: SuggestionContent | null | undefined,
  source: SuggestionOrigin,
  meta?: {
    requestId?: string;
    timestamp?: string;
    suggestionId?: string | number | null;
    id?: string | number | null;
  }
): SuggestionDisplayState | null => {
  if (!content) {
    return null;
  }

  const metaRecord = typeof content.meta === 'object' && content.meta !== null
    ? (content.meta as Record<string, unknown>)
    : undefined;

  const suggestionIdRaw =
    meta?.suggestionId ??
    meta?.id ??
    content.suggestionId ??
    content.id ??
    (metaRecord && (metaRecord['suggestionId'] ?? metaRecord['id'])) ??
    null;

  const requestIdRaw =
    meta?.requestId ??
    content.requestId ??
    (typeof metaRecord?.['requestId'] === 'string' ? (metaRecord['requestId'] as string) : undefined);

  const timestampValue = meta?.timestamp ?? content.timestamp ?? content.createdAt ?? undefined;

  const hasMeaningfulContent = Boolean(
    (Array.isArray(content.recommendations) && content.recommendations.length > 0) ||
    (content.specificMetrics && Object.keys(content.specificMetrics).length > 0) ||
    content.rationale ||
    content.summary ||
    content.plan ||
    content.title ||
    content.suggestionType ||
    content.userGoal ||
    content.timeFrame ||
    typeof content.confidenceScore === 'number'
  );

  if (!hasMeaningfulContent) {
    return null;
  }

  return {
    content,
    requestId: typeof requestIdRaw === 'string' ? requestIdRaw : undefined,
    suggestionId: suggestionIdRaw !== null && suggestionIdRaw !== undefined ? String(suggestionIdRaw) : undefined,
    timestamp: timestampValue,
    source,
  };
};

const normalizeSuggestionResponse = (
  response: SuggestionResponse,
  source: SuggestionOrigin
): SuggestionDisplayState | null => {
  const content: SuggestionContent | undefined = response.suggestions ?? {
    suggestionType: response.suggestionType ?? null,
    userGoal: response.userGoal ?? null,
    timeFrame: response.timeFrame ?? null,
    recommendations: response.recommendations,
    specificMetrics: response.specificMetrics,
    rationale: response.rationale ?? null,
    confidenceScore: response.confidenceScore ?? null,
    title: response.title ?? null,
    summary: response.summary ?? null,
    plan: response.plan ?? null,
    createdAt: response.createdAt ?? null,
    meta: response.meta,
  };

  return buildSuggestionDisplayState(content, source, {
    requestId: response.requestId,
    timestamp: response.timestamp,
    suggestionId: response.suggestionId ?? null,
    id: response.id ?? null,
  });
};

const mapInsightTypeToSuggestionType = (type: 'exercise' | 'nutrition'): 'exercise' | 'diet' => {
  return type === 'exercise' ? 'exercise' : 'diet';
};

const Homepage: React.FC = () => {
  const token = useAppSelector(state => state.auth.token);
  const isAuthenticated = Boolean(token);
  const navigate = useNavigate();

  const [timeRange, setTimeRange] = useState<QuickStatsPeriod>('weekly');
  const [activeTab, setActiveTab] = useState('overview');
  const [isInsightModalVisible, setIsInsightModalVisible] = useState(false);
  const [selectedInsightType, setSelectedInsightType] = useState<'exercise' | 'nutrition'>('nutrition');
  const [isSuggestionModalVisible, setIsSuggestionModalVisible] = useState(false);
  const [isQuickSuggestionLoading, setIsQuickSuggestionLoading] = useState(false);
  const [isCustomSuggestionLoading, setIsCustomSuggestionLoading] = useState(false);
  const [suggestionResult, setSuggestionResult] = useState<SuggestionDisplayState | null>(null);
  const [isClearingSuggestion, setIsClearingSuggestion] = useState(false);

  // Loading states
  const [loading, setLoading] = useState(true);

  // TODO: should be replaced with 0 after integrating real API calls
  // API data states
  const [userMetrics, setUserMetrics] = useState<UserMetrics>({
    weight: 0,
    height: 0,
    bmi: 0,
    bmr: 0,
    bodyFat: 0,
    weightChange: 0,
    weightTrend: '',
    lastUpdated: '',
  });

  const [dailyTargets, setDailyTargets] = useState<DailyTargets>({
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
    fiber: 0,
    sodium: 0,
  });

  const [currentIntake, setCurrentIntake] = useState<DailyIntake>({
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
    fiber: 0,
    sodium: 0,
  });

  const [activeStreak, setActiveStreak] = useState(7);
  const [weeklyWorkouts, setWeeklyWorkouts] = useState(5);
  const [weeklyWorkoutGoal, setWeeklyWorkoutGoal] = useState(6);

  const [weeklyAverages, setWeeklyAverages] = useState<WeeklyAverages>({
    avgCaloriesIntake: 0,
    avgMacros: { protein: 0, carbs: 0, fats: 0 },
    avgWorkoutDuration: 0,
    avgCaloriesBurned: 0,
    periodDays: 7,
  });

  const [monthlyAverages, setMonthlyAverages] = useState<MonthlyAverages>({
    avgCaloriesIntake: 0,
    avgMacros: { protein: 0, carbs: 0, fats: 0 },
    avgWorkoutDuration: 0,
    avgCaloriesBurned: 0,
    periodDays: 30,
  });

  const [workoutFrequency, setWorkoutFrequency] = useState<WorkoutFrequency>({
    workoutDays: 0,
    totalDays: 7,
    frequencyPercentage: 0,
    workoutTypes: {},
    totalWorkouts: 0,
  });

  const [streaks, setStreaks] = useState<Streaks>({
    workoutStreak: 0,
    nutritionStreak: 0,
    consistencyStreak: 0,
    workoutStreakStatus: '',
    nutritionStreakStatus: '',
  });

  const [meals, setMeals] = useState<Meal[]>([]);

  const [aiInsights, setAIInsights] = useState<AIInsightDTO[]>([]);
  const [latestSuggestion, setLatestSuggestion] = useState<SuggestionDisplayState | null>(null);

  const [progressData, setProgressData] = useState<ProgressData[]>([]);

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        const [
          quickStatsResult,
          todaySummaryResult,
          todayMealsResult,
          progressSeriesResult
        ] = await Promise.allSettled([
          getQuickStats(timeRange),
          getTodaySummary(),
          getTodayMeals(),
          getDailyProgress(timeRange === 'weekly' ? '7d' : '30d'),
        ]);

        if (quickStatsResult.status === 'fulfilled') {
          const quickStatsData = quickStatsResult.value;
          setUserMetrics(quickStatsData.userMetrics);
          setActiveStreak(quickStatsData.activeStreak);
          setWeeklyWorkouts(quickStatsData.weeklyWorkouts);
          setWeeklyWorkoutGoal(quickStatsData.weeklyWorkoutGoal);
          setWeeklyAverages(quickStatsData.weeklyAverages);
          setMonthlyAverages(quickStatsData.monthlyAverages);
          setWorkoutFrequency(quickStatsData.workoutFrequency);
          setStreaks(quickStatsData.streaks);
          setCurrentIntake(quickStatsData.currentIntake);
          setDailyTargets(quickStatsData.dailyTargets);
        } else {
          message.error('Failed to load quick stats');
          console.error('Error fetching quick stats:', quickStatsResult.reason);
        }

        if (todaySummaryResult.status === 'fulfilled') {
          setCurrentIntake(todaySummaryResult.value.currentIntake);
          setDailyTargets(todaySummaryResult.value.dailyTargets);
        } else if (todaySummaryResult.status === 'rejected') {
          message.warning("Unable to refresh today's nutrition summary");
          console.warn('Error fetching today summary:', todaySummaryResult.reason);
        }

        if (todayMealsResult.status === 'fulfilled') {
          setMeals(todayMealsResult.value);
        } else if (todayMealsResult.status === 'rejected') {
          message.warning("Unable to load today's meals");
          console.warn('Error fetching today meals:', todayMealsResult.reason);
        }

        if (progressSeriesResult.status === 'fulfilled') {
          setProgressData(progressSeriesResult.value);
        } else if (progressSeriesResult.status === 'rejected') {
          message.warning('Unable to load progress history');
          console.warn('Error fetching progress series:', progressSeriesResult.reason);
        }
      } catch (error) {
        message.error('Unexpected error while loading dashboard data');
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [timeRange, isAuthenticated]);

  const fetchAIInsights = useCallback(async (options?: { suppressMessages?: boolean }) => {
    if (!isAuthenticated) {
      return;
    }

    try {
      const insightsData = await getLatestInsights();
      setAIInsights(insightsData.insights);
      setLatestSuggestion(
        buildSuggestionDisplayState(insightsData.latestSuggestion, 'latest', {
          requestId: insightsData.latestSuggestion?.requestId ?? undefined,
          timestamp: insightsData.latestSuggestion?.timestamp ?? insightsData.latestSuggestion?.createdAt ?? undefined,
          suggestionId: insightsData.latestSuggestion?.suggestionId ?? null,
          id: insightsData.latestSuggestion?.id ?? null,
        })
      );
    } catch (error) {
      if (!options?.suppressMessages) {
        message.warning('Failed to load AI insights');
      }
      console.error('Error fetching AI insights:', error);
      throw error;
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchAIInsights().catch(() => undefined);
  }, [fetchAIInsights]);

  // Event handlers
  const handleTimeRangeChange = (range: QuickStatsPeriod) => {
    setTimeRange(range);
  };

  const handleExportReport = async (format: 'json' | 'pdf') => {
    try {
      if (!isAuthenticated) {
        message.error('You must be logged in to export reports.');
        return;
      }

      message.loading(`Preparing ${format.toUpperCase()} export...`, 0);

      await downloadExportData(format, timeRange === 'weekly' ? '7d' : '30d');

      message.destroy();
      message.success(`Report exported successfully as ${format.toUpperCase()}!`);
    } catch (error) {
      message.destroy();
      message.error('Failed to export report');
      console.error('Export error:', error);
    }
  };

  const handleAddMeal = () => {
    navigate('/meal');
  };

  const handleDeleteMeal = async (mealId: string) => {
    if (!isAuthenticated) {
      message.error('You must be logged in to delete meals.');
      return;
    }

    try {
      const numericId = Number(mealId);
      await apiDeleteMeal(numericId);
      setMeals(prev => prev.filter(m => m.id !== mealId));
      message.success('Meal deleted successfully');
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Failed to delete meal.';
      message.error(msg);
    }
  };

  const handleQuickSuggestion = async () => {
    if (!isAuthenticated) {
      message.error('Please sign in to request AI suggestions.');
      return;
    }

    try {
      setIsQuickSuggestionLoading(true);
      message.loading('Fetching quick suggestion...', 0);
      const response = await getSuggestion({
        type: mapInsightTypeToSuggestionType(selectedInsightType),
        timeFrame: 'week',
      });
      message.destroy();
      const normalized = normalizeSuggestionResponse(response, 'quick');
      if (!normalized) {
        message.warning('No suggestion data received.');
        return;
      }
      const displayState = normalized ?? latestSuggestion;
      if (normalized) {
        setLatestSuggestion(normalized);
      }
      setSuggestionResult(displayState);
      setIsClearingSuggestion(false);
      message.success('Quick suggestion ready!');
    } catch (error) {
      message.destroy();
      const msg = error instanceof Error ? error.message : 'Failed to fetch quick suggestion.';
      message.error(msg);
      console.error('Error fetching quick suggestion:', error);
    } finally {
      setIsQuickSuggestionLoading(false);
    }
  };

  const handleOpenSuggestionModal = () => {
    if (!isAuthenticated) {
      message.error('Please sign in to request AI suggestions.');
      return;
    }

    setIsSuggestionModalVisible(true);
  };

  const handleSubmitSuggestion = async (request: SuggestionRequestDTO) => {
    if (!isAuthenticated) {
      message.error('Please sign in to request AI suggestions.');
      return;
    }

    try {
      setIsCustomSuggestionLoading(true);
      message.loading('Creating custom suggestion...', 0);
      const payload: SuggestionRequestDTO = {
        ...request,
        timeFrame: request.timeFrame || 'week',
      };
      const response = await createSuggestion(payload);
      message.destroy();
      const normalized = normalizeSuggestionResponse(response, 'custom');
      if (!normalized) {
        message.warning('No suggestion data received.');
        return;
      }
      const displayState = normalized ?? latestSuggestion;
      if (normalized) {
        setLatestSuggestion(normalized);
      }
      setSuggestionResult(displayState);
      setIsClearingSuggestion(false);
      setIsSuggestionModalVisible(false);
      message.success('Custom suggestion ready!');
    } catch (error) {
      message.destroy();
      const msg = error instanceof Error ? error.message : 'Failed to create suggestion.';
      message.error(msg);
      console.error('Error creating suggestion:', error);
    } finally {
      setIsCustomSuggestionLoading(false);
    }
  };

  const handleClearSuggestion = async () => {
    const targetSuggestion = suggestionResult ?? latestSuggestion;

    if (!targetSuggestion) {
      return;
    }

    if (!isAuthenticated) {
      message.error('Please sign in to manage AI suggestions.');
      return;
    }

    let suggestionId = targetSuggestion.suggestionId;
    if (!suggestionId) {
      const metaRecord = typeof targetSuggestion.content.meta === 'object' && targetSuggestion.content.meta !== null
        ? (targetSuggestion.content.meta as Record<string, unknown>)
        : undefined;
      const metaCandidate = metaRecord?.['suggestionId'] ?? metaRecord?.['id'];
      if (typeof metaCandidate === 'number' || typeof metaCandidate === 'string') {
        suggestionId = String(metaCandidate);
      }
    }

    if (!suggestionId) {
      message.error('Unable to clear suggestion: missing identifier.');
      return;
    }

    const numericId = Number(suggestionId);

    if (!Number.isFinite(numericId)) {
      message.error('Unable to clear suggestion: invalid identifier.');
      return;
    }

    try {
      setIsClearingSuggestion(true);
      message.loading('Clearing suggestion...', 0);
      await deleteSuggestion(numericId);
      setSuggestionResult(null);
      setLatestSuggestion(null);
      message.destroy();
      message.success('Suggestion cleared.');
      await fetchAIInsights({ suppressMessages: true }).catch(() => undefined);
    } catch (error) {
      message.destroy();
      const msg = error instanceof Error ? error.message : 'Failed to clear suggestion.';
      message.error(msg);
      console.error('Error clearing suggestion:', error);
    } finally {
      setIsClearingSuggestion(false);
    }
  };

  const handleGenerateInsight = async () => {
    try {
      if (!isAuthenticated) {
        message.error('Please sign in to generate AI insights.');
        return;
      }

      message.loading('Generating AI insight...', 0);

      const response = await generateInsight({
        analysisType: selectedInsightType === 'nutrition' ? 'nutrition' : 'exercise',
        forceRegenerate: true
      });

      console.log('Generated insight response:', response);

      // Add the new insight to the beginning of the list
      setAIInsights(prev => [response.insight, ...prev]);
      setIsInsightModalVisible(false);

      message.destroy();
      message.success(response.message || 'AI insight generated successfully!');
    } catch (error) {
      message.destroy();
      message.error('Failed to generate AI insight');
      console.error('Error generating insight:', error);
    }
  };

  const handleDismissInsight = async (insightId: number) => {
    try {
      await dismissInsight(insightId);
      setAIInsights(insights => insights.filter(insight => insight.insightId !== insightId));
      message.success('Insight dismissed');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to dismiss insight.';
      message.error(errorMessage);
      console.error('Error dismissing insight:', error);
    }
  };

  const activeSuggestion = useMemo(() => suggestionResult ?? latestSuggestion, [suggestionResult, latestSuggestion]);

  return (
    <div style={{ padding: '24px', backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      {/* Header */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col span={24}>
          <Header
            timeRange={timeRange}
            onTimeRangeChange={handleTimeRangeChange}
            onExportReport={handleExportReport}
          />
        </Col>
      </Row>

      {/* Quick Stats */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col span={24}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '50px' }}>
              <Spin size="large" />
            </div>
          ) : (
            <QuickStats
              userMetrics={userMetrics}
              currentIntake={currentIntake}
              dailyTargets={dailyTargets}
              selectedPeriod={timeRange}
              activeStreak={activeStreak}
              weeklyWorkouts={weeklyWorkouts}
              weeklyWorkoutGoal={weeklyWorkoutGoal}
              weeklyAverages={weeklyAverages}
              monthlyAverages={monthlyAverages}
              workoutFrequency={workoutFrequency}
              streaks={streaks}
            />
          )}
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col span={24}>
          <AIInsights
            insights={aiInsights}
            suggestion={activeSuggestion}
            onClearSuggestion={handleClearSuggestion}
            onGenerateInsight={() => setIsInsightModalVisible(true)}
            onDismissInsight={handleDismissInsight}
            onQuickSuggestion={handleQuickSuggestion}
            onCustomSuggestion={handleOpenSuggestionModal}
            quickSuggestionLoading={isQuickSuggestionLoading}
            customSuggestionLoading={isCustomSuggestionLoading}
            clearingSuggestion={isClearingSuggestion}
          />
        </Col>
      </Row>

      {/* Main Dashboard Content */}
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab="Daily Overview" key="overview">
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}>
              <DailyIntakeCard
                currentIntake={currentIntake}
                dailyTargets={dailyTargets}
                onAddMeal={handleAddMeal}
              />
            </Col>
            <Col xs={24} lg={12}>
              <MealsTable
                meals={meals}
                onDeleteMeal={handleDeleteMeal}
              />
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="Progress Analytics" key="analytics">
          <ProgressCharts data={progressData} />
        </TabPane>

        {/* Add other tabs as needed */}
      </Tabs>

      {/* AI Insight Generation Modal */}
      <AIInsightModal
        visible={isInsightModalVisible}
        selectedType={selectedInsightType}
        onTypeChange={setSelectedInsightType}
        onGenerate={handleGenerateInsight}
        onCancel={() => setIsInsightModalVisible(false)}
      />

      <SuggestionFormModal
        visible={isSuggestionModalVisible}
        initialType={mapInsightTypeToSuggestionType(selectedInsightType)}
        loading={isCustomSuggestionLoading}
        onSubmit={handleSubmitSuggestion}
        onCancel={() => setIsSuggestionModalVisible(false)}
      />
    </div>
  );
};

export default Homepage;
