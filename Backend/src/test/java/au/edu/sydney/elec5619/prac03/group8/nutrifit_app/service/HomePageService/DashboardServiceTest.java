package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.AchievementsExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.NutritionSummaryExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.ProgressMetricsExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.QuickStatsResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.UserProfileExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.WorkoutHistoryExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Gender;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Meal;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserMetrics;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserProfile;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Workout;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.MealRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserMetricsRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserProfileRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserMetricsRepository userMetricsRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AIInsightService aiInsightService;

    @InjectMocks
    private DashboardService dashboardService;

    private Long userId;

    @BeforeEach
    void init() {
        userId = 7L;
    }

    @Test
    void getLatestBodyMetrics_returnsComputedValues() {
        UserMetrics latest = new UserMetrics();
        latest.setWeight(80.0);
        latest.setHeight(180.0);
        latest.setAge(30);
        latest.setGender("MALE");
        latest.setCreatedAt(LocalDateTime.now());

        UserMetrics previous = new UserMetrics();
        previous.setWeight(78.0);

        // Mock UserProfile to provide age and gender
        UserProfile userProfile = new UserProfile();
        userProfile.setBirthDate(LocalDate.now().minusYears(30));
        userProfile.setGender(Gender.MALE);

        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(userId))
            .thenReturn(Optional.of(latest));
        when(userMetricsRepository.findSecondLatestByUserId(userId))
            .thenReturn(Optional.of(previous));
        when(userProfileRepository.findByUserUserId(userId))
            .thenReturn(Optional.of(userProfile));

        Map<String, Object> metrics = dashboardService.getLatestBodyMetrics(userId);

        assertThat(metrics.get("weight")).isEqualTo(80.0);
        assertThat(metrics.get("height")).isEqualTo(180.0);
        assertThat(metrics.get("bmi")).isEqualTo(24.69);
        assertThat(metrics.get("bmr")).isEqualTo(1780.0);
        assertThat(metrics.get("weightChange")).isEqualTo(2.0);
        assertThat(metrics.get("weightTrend")).isEqualTo("up");
        assertThat(metrics.get("lastUpdated")).isEqualTo(latest.getCreatedAt());
    }

    @Test
    void getLatestBodyMetrics_returnsDefaultsWhenNoData() {
        Map<String, Object> metrics = dashboardService.getLatestBodyMetrics(userId);

        assertThat(metrics.get("weight")).isEqualTo(0.0);
        assertThat(metrics.get("weightTrend")).isEqualTo("no_data");
        assertThat(metrics.get("lastUpdated")).isNull();
    }

    @Test
    void getDailyAverages_calculatesAggregatedValues() {
        LocalDate today = LocalDate.now();
        Meal breakfast = meal(today, 8, 500, 30, 40, 15);
        Meal lunch = meal(today, 13, 400, 20, 50, 10);
        Meal yesterdayMeal = meal(today.minusDays(1), 12, 600, 25, 60, 20);

        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(userId), any(), any()))
            .thenReturn(Arrays.asList(breakfast, lunch, yesterdayMeal));

        Workout workout1 = workout(today, 45, 300, "cardio");
        Workout workout2 = workout(today.minusDays(1), 30, 200, "strength");
        when(workoutRepository.findByUserUserIdAndWorkoutDateBetween(eq(userId), any(), any()))
            .thenReturn(Arrays.asList(workout1, workout2));

        Map<String, Object> averages = dashboardService.getDailyAverages(userId, 7);

        assertThat(averages.get("avgCaloriesIntake")).isEqualTo(750);
        assertThat(averages.get("totalCaloriesIntake")).isEqualTo(1500);

        @SuppressWarnings("unchecked")
        Map<String, Double> macros = (Map<String, Double>) averages.get("avgMacros");
        assertThat(macros.get("protein")).isEqualTo(37.5);
        assertThat(macros.get("carbs")).isEqualTo(75.0);
        assertThat(macros.get("fats")).isEqualTo(22.5);

        assertThat(averages.get("avgWorkoutDuration")).isEqualTo(38);
        assertThat(averages.get("avgCaloriesBurned")).isEqualTo(250);
        assertThat(averages.get("totalWorkoutDuration")).isEqualTo(75);
        assertThat(averages.get("totalCaloriesBurned")).isEqualTo(500);
        assertThat(averages.get("workoutCount")).isEqualTo(2);
    }

    @Test
    void getWorkoutFrequency_calculatesWorkoutStats() {
        LocalDate today = LocalDate.now();
        Workout workout1 = workout(today, 30, 200, "cardio");
        Workout workout2 = workout(today.minusDays(1), 40, 300, "strength");
        Workout workout3 = workout(today.minusDays(1), 20, 150, "cardio");

        when(workoutRepository.findByUserUserIdAndWorkoutDateBetween(eq(userId), any(), any()))
            .thenReturn(Arrays.asList(workout1, workout2, workout3));

        Map<String, Object> frequency = dashboardService.getWorkoutFrequency(userId, 7);

        assertThat(frequency.get("workoutDays")).isEqualTo(2L);
        assertThat(frequency.get("totalWorkouts")).isEqualTo(3);
        assertThat(frequency.get("frequencyPercentage")).isEqualTo(28.57);

        @SuppressWarnings("unchecked")
        Map<String, Long> types = (Map<String, Long>) frequency.get("workoutTypes");
        assertThat(types.get("cardio")).isEqualTo(2L);
        assertThat(types.get("strength")).isEqualTo(1L);
    }

    @Test
    void getCurrentStreaks_computesStreaksAndStatuses() {
        UserMetrics metrics = new UserMetrics();
        metrics.setWeight(80.0);
        metrics.setHeight(180.0);
        metrics.setAge(30);
        metrics.setGender("MALE");
        metrics.setCreatedAt(LocalDateTime.now());

        // Mock UserProfile to provide age and gender
        UserProfile userProfile = new UserProfile();
        userProfile.setBirthDate(LocalDate.now().minusYears(30));
        userProfile.setGender(Gender.MALE);

        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(userId))
            .thenReturn(Optional.of(metrics));
        when(userMetricsRepository.findSecondLatestByUserId(userId))
            .thenReturn(Optional.empty());
        when(userProfileRepository.findByUserUserId(userId))
            .thenReturn(Optional.of(userProfile));

        LocalDate today = LocalDate.now();
        when(workoutRepository.findByUserUserIdAndWorkoutDate(eq(userId), any(LocalDate.class)))
            .thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(1);
                if (date.equals(today) || date.equals(today.minusDays(1))) {
                    return List.of(workout(date, 30, 200, "cardio"));
                }
                return Collections.emptyList();
            });

        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenAnswer(invocation -> {
                LocalDateTime start = invocation.getArgument(1);
                LocalDate date = start.toLocalDate();
                if (date.equals(today) || date.equals(today.minusDays(1))) {
                    Meal meal = meal(date, 12, 2500, 0, 0, 0);
                    return List.of(meal);
                }
                return Collections.emptyList();
            });

        Map<String, Object> streaks = dashboardService.getCurrentStreaks(userId);

        assertThat(streaks.get("workoutStreak")).isEqualTo(2);
        assertThat(streaks.get("nutritionStreak")).isEqualTo(2);
        assertThat(streaks.get("consistencyStreak")).isEqualTo(2);
        assertThat(streaks.get("workoutStreakStatus")).isEqualTo("building");
        assertThat(streaks.get("nutritionStreakStatus")).isEqualTo("building");
    }

    @Test
    void getAIInsightsForExport_filtersByDateRange() {
        LocalDate today = LocalDate.now();
        List<AIInsightDTO> insights = List.of(
            new AIInsightDTO(1L, "Inside window", "general", true,
                today.plusDays(5).atStartOfDay(),
                today.minusDays(1).atTime(10, 0),
                today.minusDays(1).atTime(12, 0),
                "active", "general", 3),
            new AIInsightDTO(2L, "Outside window", "general", true,
                today.plusDays(10).atStartOfDay(),
                today.minusDays(10).atStartOfDay(),
                today.minusDays(9).atStartOfDay(),
                "active", "general", 3)
        );

        when(aiInsightService.getLatestInsights()).thenReturn(insights);

        String range = today.minusDays(2) + "," + today;
        List<AIInsightDTO> filtered = dashboardService.getAIInsightsForExport(userId, range, aiInsightService);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getInsightId()).isEqualTo(1L);
    }

    @Test
    void getQuickStats_returnsAggregatedResponse() {
        LocalDateTime now = LocalDateTime.now();
        UserMetrics latestMetrics = new UserMetrics();
        latestMetrics.setWeight(75.0);
        latestMetrics.setHeight(175.0);
        latestMetrics.setAge(28);
        latestMetrics.setGender("MALE");
        latestMetrics.setCreatedAt(now);

        UserMetrics previousMetrics = new UserMetrics();
        previousMetrics.setWeight(74.0);

        // Mock UserProfile to provide age and gender
        UserProfile userProfile = new UserProfile();
        userProfile.setBirthDate(LocalDate.now().minusYears(28));
        userProfile.setGender(Gender.MALE);

        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(userId))
            .thenReturn(Optional.of(latestMetrics));
        when(userMetricsRepository.findSecondLatestByUserId(userId))
            .thenReturn(Optional.of(previousMetrics));
        when(userProfileRepository.findByUserUserId(userId))
            .thenReturn(Optional.of(userProfile));

        Meal sampleMeal = meal(LocalDate.now(), 8, 600, 30, 50, 20);
        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(userId), any(), any()))
            .thenReturn(List.of(sampleMeal));
        when(workoutRepository.findByUserUserIdAndWorkoutDateBetween(eq(userId), any(), any()))
            .thenReturn(List.of(workout(LocalDate.now(), 45, 300, "cardio")));
        when(workoutRepository.findByUserUserIdAndWorkoutDate(eq(userId), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(workout(LocalDate.now(), 30, 200, "cardio")));

        QuickStatsResponseDTO response = dashboardService.getQuickStats(userId, "monthly");

        assertThat(response).isNotNull();
        assertThat(response.getBodyMetrics().getWeight()).isEqualTo(75.0);
        assertThat(response.getWorkoutFrequency().getTotalWorkouts()).isGreaterThanOrEqualTo(1);
        assertThat(response.getWeeklyAverages().getPeriodDays()).isEqualTo(7);
        assertThat(response.getMonthlyAverages().getPeriodDays()).isEqualTo(30);
    }

    @Test
    void getUserProfileForExport_returnsUserSnapshot() {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("Taylor");
        user.setEmail("taylor@example.com");

        UserMetrics metrics = new UserMetrics();
        metrics.setWeight(70.0);
        metrics.setHeight(172.0);
        metrics.setAge(32);
        metrics.setGender("FEMALE");
        metrics.setUserGoal("Build muscle");
        metrics.setCreatedAt(LocalDateTime.now());

        // Mock UserProfile to provide age and gender
        UserProfile userProfile = new UserProfile();
        userProfile.setBirthDate(LocalDate.now().minusYears(32));
        userProfile.setGender(Gender.FEMALE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(userId))
            .thenReturn(Optional.of(metrics));
        when(userMetricsRepository.findSecondLatestByUserId(userId))
            .thenReturn(Optional.of(metrics));
        when(userProfileRepository.findByUserUserId(userId))
            .thenReturn(Optional.of(userProfile));

        UserProfileExportDTO dto = dashboardService.getUserProfileForExport(userId);

        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getUsername()).isEqualTo("Taylor");
        assertThat(dto.getGender()).isEqualTo("FEMALE");
        assertThat(dto.getActivityLevel()).isEqualTo("Build muscle");
    }

    @Test
    void getNutritionSummaryForExport_computesTotalsAndAverages() {
        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate end = LocalDate.now();

        Meal meal1 = meal(start, 8, 500, 30, 40, 15);
        Meal meal2 = meal(start.plusDays(1), 12, 600, 35, 55, 18);
        meal1.setMealTime(start.atTime(8, 0));
        meal2.setMealTime(start.plusDays(1).atTime(12, 0));

        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(userId), any(), any()))
            .thenReturn(List.of(meal1, meal2));

        UserMetrics metrics = new UserMetrics();
        metrics.setWeight(70.0);
        metrics.setHeight(170.0);
        metrics.setAge(30);
        metrics.setGender("MALE");
        metrics.setCreatedAt(LocalDateTime.now());

        // Mock UserProfile to provide age and gender
        UserProfile userProfile = new UserProfile();
        userProfile.setBirthDate(LocalDate.now().minusYears(30));
        userProfile.setGender(Gender.MALE);

        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(userId))
            .thenReturn(Optional.of(metrics));
        when(userMetricsRepository.findSecondLatestByUserId(userId))
            .thenReturn(Optional.of(metrics));
        when(userProfileRepository.findByUserUserId(userId))
            .thenReturn(Optional.of(userProfile));

        NutritionSummaryExportDTO dto = dashboardService.getNutritionSummaryForExport(
            userId, start + "," + end);

        assertThat(dto.getTotalCaloriesConsumed()).isPositive();
        assertThat(dto.getAvgDailyCalories()).isGreaterThan(0);
        assertThat(dto.getDailyBreakdown()).hasSize(3);
        assertThat(dto.getNutritionGoalStatus()).isNotBlank();
    }

    @Test
    void getWorkoutHistoryForExport_compilesHistory() {
        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end = LocalDate.now();

        Workout workout1 = workout(start, 30, 200, "cardio");
        workout1.setCaloriesBurned(BigDecimal.valueOf(200));
        workout1.setDurationMinutes(30);
        Workout workout2 = workout(start.plusDays(1), 45, 350, "strength");
        workout2.setCaloriesBurned(BigDecimal.valueOf(350));
        workout2.setDurationMinutes(45);

        when(workoutRepository.findByUserUserIdAndWorkoutDateBetween(eq(userId), any(), any()))
            .thenReturn(List.of(workout1, workout2));

        WorkoutHistoryExportDTO dto = dashboardService.getWorkoutHistoryForExport(
            userId, start + "," + end);

        assertThat(dto.getTotalWorkouts()).isEqualTo(2);
        assertThat(dto.getWeeklyFrequencyPercentage()).isGreaterThanOrEqualTo(0);
        assertThat(dto.getDailyBreakdown()).hasSize(4);
    }

    @Test
    void getProgressMetricsForExport_returnsCompositeDto() {
        ProgressMetricsExportDTO dto = dashboardService.getProgressMetricsForExport(
            userId, "last_4_weeks");

        assertThat(dto.getOverallProgressRating()).isEqualTo("Good Progress");
        assertThat(dto.getNutritionProgress().getAvgDailyCalories()).isEqualTo(2000.0);
        assertThat(dto.getWeeklyTrends()).isNotEmpty();
    }

    @Test
    void getAchievementsForExport_compilesLevelsAndMilestones() {
        when(workoutRepository.findByUserUserIdAndWorkoutDate(eq(userId), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(userId), any(), any()))
            .thenReturn(Collections.emptyList());
        AchievementsExportDTO dto = dashboardService.getAchievementsForExport(userId);

        assertThat(dto.getCompletedAchievements()).isNotEmpty();
        assertThat(dto.getCurrentLevel()).isNotBlank();
        assertThat(dto.getMilestones()).isNotEmpty();
    }

    @Test
    void calculateTotalDataPoints_countsEntries() {
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("userProfile", new Object());
        exportData.put("nutritionSummary", new Object());
        exportData.put("exerciseHistory", null);
        exportData.put("progressMetrics", new Object());
        exportData.put("aiInsights", null);
        exportData.put("achievements", new Object());

        assertThat(dashboardService.calculateTotalDataPoints(exportData)).isEqualTo(4);
    }

    @Test
    void getAIInsightsForExport_returnsEmptyOnError() {
        AIInsightService failingService = new AIInsightService() {
            @Override
            public List<AIInsightDTO> getLatestInsights() {
                throw new RuntimeException("service down");
            }
        };

        List<AIInsightDTO> result = dashboardService.getAIInsightsForExport(
            userId, null, failingService);

        assertThat(result).isEmpty();
    }

    @Test
    void getNutritionSummaryForExport_computesAggregates() {
        UserMetrics metrics = new UserMetrics();
        metrics.setWeight(80.0);
        metrics.setHeight(180.0);
        metrics.setAge(30);
        metrics.setGender("MALE");
        metrics.setCreatedAt(LocalDateTime.now());

        // Mock UserProfile to provide age and gender
        UserProfile userProfile = new UserProfile();
        userProfile.setBirthDate(LocalDate.now().minusYears(30));
        userProfile.setGender(Gender.MALE);

        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(userId))
            .thenReturn(Optional.of(metrics));
        when(userMetricsRepository.findSecondLatestByUserId(userId))
            .thenReturn(Optional.empty());
        when(userProfileRepository.findByUserUserId(userId))
            .thenReturn(Optional.of(userProfile));

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();
        Meal meal1 = meal(start, 9, 600, 30, 50, 20);
        Meal meal2 = meal(end, 13, 700, 40, 60, 25);

        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(meal1, meal2));

        String range = start + "," + end;
        var summary = dashboardService.getNutritionSummaryForExport(userId, range);

        assertThat(summary.getTotalCaloriesConsumed()).isEqualTo(1300.0);
        assertThat(summary.getAvgDailyCalories()).isEqualTo(650.0);
        assertThat(summary.getTotalMeals()).isEqualTo(2);
        assertThat(summary.getAvgDailyMacros().getProtein()).isEqualTo(35.0);
        assertThat(summary.getDateRange()).contains(start.toString());
        assertThat(summary.getCalorieTargetProgress()).isGreaterThan(0.0);
        assertThat(summary.getDailyBreakdown()).hasSize(2);
    }

    private Meal meal(LocalDate date, int hour, double calories, double protein, double carbs, double fat) {
        Meal meal = new Meal();
        meal.setMealTime(date.atTime(hour, 0));
        meal.setTotalCalories(BigDecimal.valueOf(calories));
        meal.setProteinG(BigDecimal.valueOf(protein));
        meal.setCarbsG(BigDecimal.valueOf(carbs));
        meal.setFatG(BigDecimal.valueOf(fat));
        return meal;
    }

    private Workout workout(LocalDate date, int duration, double calories, String type) {
        Workout workout = new Workout();
        workout.setWorkoutDate(date);
        workout.setDurationMinutes(duration);
        workout.setCaloriesBurned(BigDecimal.valueOf(calories));
        workout.setWorkoutType(type);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setUpdatedAt(LocalDateTime.now());
        return workout;
    }

    // Mockito's eq requires static import; provide helper to keep imports compact.
    private <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
