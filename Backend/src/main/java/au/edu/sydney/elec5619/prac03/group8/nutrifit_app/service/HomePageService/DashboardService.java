package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.*;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.*;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.*;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private UserMetricsRepository userMetricsRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;


    /**
     * Get complete quick stats response including all required metrics
     * @param period - "weekly" or "monthly" to determine the data range
     */
    public QuickStatsResponseDTO getQuickStats(Long userId, String period) {
        try {
            // Determine the number of days based on period
            int days = period.equalsIgnoreCase("monthly") ? 30 : 7;

            // Get body metrics
            BodyMetricsDTO bodyMetrics = getLatestBodyMetricsDTO(userId);

            // Get averages based on selected period
            DailyAveragesDTO weeklyAverages = getDailyAveragesDTO(userId, 7);
            DailyAveragesDTO monthlyAverages = getDailyAveragesDTO(userId, 30);

            // Get workout frequency for the selected period
            WorkoutFrequencyDTO workoutFrequency = getWorkoutFrequencyDTO(userId, days);

            // Get streak data
            StreaksDTO streaks = getCurrentStreaksDTO(userId);

            return new QuickStatsResponseDTO(bodyMetrics, weeklyAverages, monthlyAverages, workoutFrequency, streaks);

        } catch (Exception e) {
            throw new RuntimeException("Error getting quick stats: " + e.getMessage());
        }
    }

    /**
     * Get complete quick stats response (backward compatibility - defaults to weekly)
     */
    public QuickStatsResponseDTO getQuickStats(Long userId) {
        return getQuickStats(userId, "weekly");
    }

    /**
     * Get latest body metrics as DTO
     */
    public BodyMetricsDTO getLatestBodyMetricsDTO(Long userId) {
        try {
            if (userId == null) {
                return createDefaultBodyMetricsDTO();
            }

            Map<String, Object> metrics = getLatestBodyMetrics(userId);

            return new BodyMetricsDTO(
                (Double) metrics.get("weight"),
                (Double) metrics.get("height"),
                (Double) metrics.get("bmi"),
                (Double) metrics.get("bmr"),
                (Double) metrics.get("weightChange"),
                (String) metrics.get("weightTrend"),
                (LocalDateTime) metrics.get("lastUpdated")
            );
        } catch (Exception e) {
            return createDefaultBodyMetricsDTO();
        }
    }

    private BodyMetricsDTO createDefaultBodyMetricsDTO() {
        return new BodyMetricsDTO(0.0, 0.0, 0.0, 0.0, 0.0, "no_data", null);
    }

    /**
     * Get latest body metrics (weight, BMI, BMR) from most recent entries
     * Note: Age and gender are now retrieved from UserProfile instead of UserMetrics
     */
    public Map<String, Object> getLatestBodyMetrics(Long userId) {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Get age and gender from UserProfile (current user data)
            Map<String, Object> profileData = getAgeAndGenderFromProfile(userId);
            Integer age = (Integer) profileData.get("age");
            String gender = (String) profileData.get("gender");

            // Use the corrected method name
            Optional<UserMetrics> latestMetrics = userMetricsRepository
                .findTopByUserUserIdOrderByRecordAt(userId);

            if (latestMetrics.isPresent()) {
                UserMetrics userMetrics = latestMetrics.get();

                double weight = userMetrics.getWeight();
                double height = userMetrics.getHeight();

                metrics.put("weight", weight);
                metrics.put("height", height);
                metrics.put("bmi", calculateBMI(weight, height));
                metrics.put("bmr", calculateBMR(weight, height, age, gender));
                metrics.put("lastUpdated", userMetrics.getCreatedDate());

                // Calculate weight change from previous entry
                Optional<UserMetrics> previousMetrics = userMetricsRepository
                    .findSecondLatestByUserId(userId);

                if (previousMetrics.isPresent()) {
                    double weightChange = weight - previousMetrics.get().getWeight();
                    metrics.put("weightChange", Math.round(weightChange * 100.0) / 100.0);
                    metrics.put("weightTrend", weightChange > 0 ? "up" : weightChange < 0 ? "down" : "stable");
                } else {
                    metrics.put("weightChange", 0.0);
                    metrics.put("weightTrend", "stable");
                }
            } else {
                // Return default values if no metrics found
                metrics.put("weight", 0.0);
                metrics.put("height", 0.0);
                metrics.put("bmi", 0.0);
                metrics.put("bmr", 0.0);
                metrics.put("weightChange", 0.0);
                metrics.put("weightTrend", "no_data");
                metrics.put("lastUpdated", null);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching latest body metrics: " + e.getMessage());
        }

        return metrics;
    }

    // Helper methods for BMI and BMR calculations
    private double calculateBMI(double weight, double height) {
        if (height <= 0) return 0.0;
        double heightInMeters = height / 100.0;
        return Math.round((weight / (heightInMeters * heightInMeters)) * 100.0) / 100.0;
    }

    /**
     * Calculate BMR using age and gender from UserProfile instead of UserMetrics
     */
    private double calculateBMR(double weight, double height, Integer age, String gender) {
        if (age == null || age <= 0) return 0.0;

        double baseBMR = 10 * weight + 6.25 * height - 5 * age;

        if ("MALE".equalsIgnoreCase(gender) || "M".equalsIgnoreCase(gender)) {
            return Math.round((baseBMR + 5) * 100.0) / 100.0;
        } else if ("FEMALE".equalsIgnoreCase(gender) || "F".equalsIgnoreCase(gender)) {
            return Math.round((baseBMR - 161) * 100.0) / 100.0;
        } else {
            // Default to average if gender not specified
            return Math.round((baseBMR - 78) * 100.0) / 100.0;
        }
    }

    /**
     * Calculate age from birth date
     */
    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) return null;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Get age and gender from UserProfile for current user data
     */
    private Map<String, Object> getAgeAndGenderFromProfile(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<UserProfile> profile = userProfileRepository.findByUserUserId(userId);
            if (profile.isPresent()) {
                UserProfile userProfile = profile.get();
                result.put("age", calculateAge(userProfile.getBirthDate()));
                result.put("gender", userProfile.getGender() != null ? userProfile.getGender().name() : null);
            } else {
                result.put("age", null);
                result.put("gender", null);
            }
        } catch (Exception e) {
            result.put("age", null);
            result.put("gender", null);
        }

        return result;
    }

    /**
     * Get daily averages as DTO
     */
    public DailyAveragesDTO getDailyAveragesDTO(Long userId, int days) {
        try {
            if (userId == null) {
                return createDefaultDailyAveragesDTO(days);
            }

            Map<String, Object> averages = getDailyAverages(userId, days);
            @SuppressWarnings("unchecked")
            Map<String, Double> avgMacrosMap = (Map<String, Double>) averages.get("avgMacros");

            MacrosDTO avgMacros = new MacrosDTO(
                avgMacrosMap.get("protein"),
                avgMacrosMap.get("carbs"),
                avgMacrosMap.get("fats")
            );

            return new DailyAveragesDTO(
                (Integer) averages.get("avgCaloriesIntake"),
                avgMacros,
                (Integer) averages.get("avgWorkoutDuration"),
                (Integer) averages.get("avgCaloriesBurned"),
                (Integer) averages.get("periodDays"),
                (Integer) averages.get("totalCaloriesIntake"),
                (Integer) averages.get("totalWorkoutDuration"),
                (Integer) averages.get("totalCaloriesBurned"),
                (Integer) averages.get("workoutCount")
            );
        } catch (Exception e) {
            return createDefaultDailyAveragesDTO(days);
        }
    }

    private DailyAveragesDTO createDefaultDailyAveragesDTO(int days) {
        MacrosDTO defaultMacros = new MacrosDTO(0.0, 0.0, 0.0);
        return new DailyAveragesDTO(0, defaultMacros, 0, 0, days, 0, 0, 0, 0);
    }

    /**
     * Get workout frequency as DTO
     */
    public WorkoutFrequencyDTO getWorkoutFrequencyDTO(Long userId, int days) {
        try {
            if (userId == null) {
                return createDefaultWorkoutFrequencyDTO(days);
            }

            Map<String, Object> frequency = getWorkoutFrequency(userId, days);
            @SuppressWarnings("unchecked")
            Map<String, Long> workoutTypes = (Map<String, Long>) frequency.get("workoutTypes");

            return new WorkoutFrequencyDTO(
                (Long) frequency.get("workoutDays"),
                (Integer) frequency.get("totalDays"),
                (Double) frequency.get("frequencyPercentage"),
                workoutTypes,
                (Integer) frequency.get("totalWorkouts")
            );
        } catch (Exception e) {
            return createDefaultWorkoutFrequencyDTO(days);
        }
    }

    private WorkoutFrequencyDTO createDefaultWorkoutFrequencyDTO(int days) {
        return new WorkoutFrequencyDTO(0L, days, 0.0, new HashMap<>(), 0);
    }

    /**
     * Get current streaks as DTO
     */
    public StreaksDTO getCurrentStreaksDTO(Long userId) {
        try {
            if (userId == null) {
                return createDefaultStreaksDTO();
            }

            Map<String, Object> streaks = getCurrentStreaks(userId);

            return new StreaksDTO(
                (Integer) streaks.get("workoutStreak"),
                (Integer) streaks.get("nutritionStreak"),
                (Integer) streaks.get("consistencyStreak"),
                (String) streaks.get("workoutStreakStatus"),
                (String) streaks.get("nutritionStreakStatus")
            );
        } catch (Exception e) {
            return createDefaultStreaksDTO();
        }
    }

    private StreaksDTO createDefaultStreaksDTO() {
        return new StreaksDTO(0, 0, 0, "none", "none");
    }

    /**
     * Get workout frequency for specified number of days
     */
    public Map<String, Object> getWorkoutFrequency(Long userId, int days) {
        Map<String, Object> frequency = new HashMap<>();
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            LocalDate endDate = LocalDate.now();
            
            List<Workout> workouts = workoutRepository
                .findByUserUserIdAndWorkoutDateBetween(userId, startDate, endDate);
    
            // Count unique workout days
            long workoutDays = workouts.stream()
                .map(Workout::getWorkoutDate)
                .distinct()
                .count();
            
            // Calculate frequency percentage
            double frequencyPercentage = (double) workoutDays / days * 100;
            
            // Get workout types distribution
            Map<String, Long> workoutTypes = workouts.stream()
                .collect(Collectors.groupingBy(
                    Workout::getWorkoutType,
                    Collectors.counting()
                ));
            
            frequency.put("workoutDays", workoutDays);
            frequency.put("totalDays", days);
            frequency.put("frequencyPercentage", Math.round(frequencyPercentage * 100.0) / 100.0);
            frequency.put("workoutTypes", workoutTypes);
            frequency.put("totalWorkouts", workouts.size());
            
        } catch (Exception e) {
            throw new RuntimeException("Error calculating workout frequency: " + e.getMessage());
        }
        
        return frequency;
    }

    /**
     * Calculate daily averages for key indicators over specified number of days
     */
    public Map<String, Object> getDailyAverages(Long userId, int days) {
        Map<String, Object> averages = new HashMap<>();
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            LocalDate endDate = LocalDate.now();
            
            // Get nutrition data for the period
            List<Meal> meals = mealRepository
                .findByUserUserIdAndMealTimeBetween(userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            List<Meal> mealsWithTimestamps = meals.stream()
                .filter(meal -> meal.getMealTime() != null)
                .collect(Collectors.toList());

            // Calculate calorie intake totals and average
            double totalCalories = mealsWithTimestamps.stream()
                .map(Meal::getTotalCalories)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
            double avgCalories = mealsWithTimestamps.stream()
                .collect(Collectors.groupingBy(meal -> meal.getMealTime().toLocalDate()))
                .entrySet().stream()
                .mapToDouble(entry -> entry.getValue().stream()
                    .mapToDouble(meal -> meal.getTotalCalories() != null
                        ? meal.getTotalCalories().doubleValue()
                        : 0.0)
                    .sum())
                .average()
                .orElse(0.0);
            
            // Calculate macro distribution averages
            Map<String, Double> avgMacros = calculateAverageMacros(mealsWithTimestamps);

            // Get workout data for the period
            List<Workout> workouts = workoutRepository
                .findByUserUserIdAndWorkoutDateBetween(userId, startDate, endDate);
            
            int totalWorkoutDuration = workouts.stream()
                .map(Workout::getDurationMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

            double avgWorkoutDuration = workouts.stream()
                .map(Workout::getDurationMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
            
            double totalCaloriesBurnedValue = workouts.stream()
                .map(Workout::getCaloriesBurned)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
            int totalCaloriesBurned = (int) Math.round(totalCaloriesBurnedValue);

            double avgCaloriesBurnedValue = workouts.stream()
                .map(Workout::getCaloriesBurned)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
            int avgCaloriesBurned = (int) Math.round(avgCaloriesBurnedValue);
            
            averages.put("avgCaloriesIntake", (int) Math.round(avgCalories));
            averages.put("avgMacros", avgMacros);
            averages.put("avgWorkoutDuration", (int) Math.round(avgWorkoutDuration));
            averages.put("avgCaloriesBurned", avgCaloriesBurned);
            averages.put("periodDays", days);
            averages.put("totalCaloriesIntake", (int) Math.round(totalCalories));
            averages.put("totalWorkoutDuration", totalWorkoutDuration);
            averages.put("totalCaloriesBurned", totalCaloriesBurned);
            averages.put("workoutCount", workouts.size());
            
        } catch (Exception e) {
            throw new RuntimeException("Error calculating daily averages: " + e.getMessage());
        }
        
        return averages;
    }

    /**
     * Get current streaks (consecutive workout days, nutrition target hits)
     */
    public Map<String, Object> getCurrentStreaks(Long userId) {
        Map<String, Object> streaks = new HashMap<>();
        
        try {
            // Calculate workout streak
            int workoutStreak = calculateWorkoutStreak(userId);
            
            // Calculate nutrition target streak
            int nutritionStreak = calculateNutritionTargetStreak(userId);
            
            // Calculate overall consistency streak (both workout and nutrition)
            int consistencyStreak = Math.min(workoutStreak, nutritionStreak);
            
            streaks.put("workoutStreak", workoutStreak);
            streaks.put("nutritionStreak", nutritionStreak);
            streaks.put("consistencyStreak", consistencyStreak);
            
            // Add streak status
            streaks.put("workoutStreakStatus", getStreakStatus(workoutStreak));
            streaks.put("nutritionStreakStatus", getStreakStatus(nutritionStreak));
            
        } catch (Exception e) {
            throw new RuntimeException("Error calculating current streaks: " + e.getMessage());
        }
        
        return streaks;
    }

    private Map<String, Double> calculateAverageMacros(List<Meal> meals) {
        Map<String, Double> avgMacros = new HashMap<>();
        
        if (meals.isEmpty()) {
            avgMacros.put("protein", 0.0);
            avgMacros.put("carbs", 0.0);
            avgMacros.put("fats", 0.0);
            return avgMacros;
        }
        
        Map<LocalDate, List<Meal>> dailyMeals = meals.stream()
            .filter(meal -> meal.getMealTime() != null)
            .collect(Collectors.groupingBy(meal -> meal.getMealTime().toLocalDate()));
        
        // Calculate daily totals first, then average
        double avgProtein = dailyMeals.entrySet().stream()
            .mapToDouble(entry -> entry.getValue().stream()
                .map(Meal::getProteinG)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum())
            .average()
            .orElse(0.0);
            
        double avgCarbs = dailyMeals.entrySet().stream()
            .mapToDouble(entry -> entry.getValue().stream()
                .map(Meal::getCarbsG)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum())
            .average()
            .orElse(0.0);
            
        double avgFats = dailyMeals.entrySet().stream()
            .mapToDouble(entry -> entry.getValue().stream()
                .map(Meal::getFatG)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum())
            .average()
            .orElse(0.0);
        
        avgMacros.put("protein", Math.round(avgProtein * 100.0) / 100.0);
        avgMacros.put("carbs", Math.round(avgCarbs * 100.0) / 100.0);
        avgMacros.put("fats", Math.round(avgFats * 100.0) / 100.0);
        
        return avgMacros;
    }
    
    private int calculateWorkoutStreak(Long userId) {
        LocalDate currentDate = LocalDate.now();
        int streak = 0;
        
        // Check each day backwards until we find a day without workout
        while (true) {
            List<Workout> workoutsForDate = workoutRepository
                .findByUserUserIdAndWorkoutDate(userId, currentDate.minusDays(streak));
            
            if (workoutsForDate.isEmpty()) {
                break;
            }
            
            streak++;
            
            // Prevent infinite loop - max reasonable streak check
            if (streak > 365) break;
        }
        
        return streak;
    }
    
    private int calculateNutritionTargetStreak(Long userId) {
        LocalDate currentDate = LocalDate.now();
        int streak = 0;
        
        // You'll need to define what constitutes "hitting nutrition targets"
        // For now, let's assume hitting 80% of calorie target
        while (true) {
            boolean hitTarget = checkNutritionTargetForDate(userId, currentDate.minusDays(streak));
            
            if (!hitTarget) {
                break;
            }
            
            streak++;
            
            // Prevent infinite loop
            if (streak > 365) break;
        }
        
        return streak;
    }
    
    private boolean checkNutritionTargetForDate(Long userId, LocalDate date) {
        // Get daily meals for the date
        List<Meal> dailyMeals = mealRepository
            .findByUserUserIdAndMealTimeBetween(userId, date.atStartOfDay(), date.atTime(23, 59, 59));
        
        if (dailyMeals.isEmpty()) return false;
        
        double totalCalories = dailyMeals.stream()
            .mapToDouble(meal -> meal.getTotalCalories().doubleValue())
            .sum();
        
        // Get user's calorie target (you'll need to implement this)
        double calorieTarget = getUserCalorieTarget(userId);
        
        // Consider target hit if within 80-120% of target
        return totalCalories >= (calorieTarget * 0.8) && totalCalories <= (calorieTarget * 1.2);
    }
    
    private double getUserCalorieTarget(Long userId) {
        // This should fetch from user preferences/goals
        // For now, return a default based on BMR * activity level
        Map<String, Object> bodyMetrics = getLatestBodyMetrics(userId);
        double bmr = (Double) bodyMetrics.get("bmr");
        return bmr * 1.4; // Moderate activity level
    }
    
    private String getStreakStatus(int streak) {
        if (streak == 0) return "none";
        else if (streak < 3) return "building";
        else if (streak < 7) return "good";
        else if (streak < 14) return "great";
        else return "excellent";
    }

    // Export Methods

    /**
     * Get user profile data for export
     * Note: Age and gender are now retrieved from UserProfile instead of UserMetrics
     */
    public UserProfileExportDTO getUserProfileForExport(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> bodyMetrics = getLatestBodyMetrics(userId);

            // Get age and gender from UserProfile (current user data)
            Map<String, Object> profileData = getAgeAndGenderFromProfile(userId);
            Integer age = (Integer) profileData.get("age");
            String gender = (String) profileData.get("gender");

            // Get user goal from latest metrics (historical snapshot)
            String userGoal = null;
            Optional<UserMetrics> latestMetrics = userMetricsRepository
                .findTopByUserUserIdOrderByRecordAt(userId);

            if (latestMetrics.isPresent()) {
                userGoal = latestMetrics.get().getUserGoal();
            }

            return new UserProfileExportDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                (Double) bodyMetrics.get("weight"),
                (Double) bodyMetrics.get("height"),
                (Double) bodyMetrics.get("bmi"),
                (Double) bodyMetrics.get("bmr"),
                age,
                gender,
                userGoal, // Using userGoal as activity level placeholder
                (LocalDateTime) bodyMetrics.get("lastUpdated")
            );
        } catch (Exception e) {
            throw new RuntimeException("Error getting user profile for export: " + e.getMessage());
        }
    }

    /**
     * Get nutrition summary data for export
     */
    public NutritionSummaryExportDTO getNutritionSummaryForExport(Long userId, String dateRange) {
        try {
            LocalDate[] dates = parseDateRange(dateRange);
            LocalDate startDate = dates[0];
            LocalDate endDate = dates[1];

            List<Meal> meals = mealRepository
                .findByUserUserIdAndMealTimeBetween(userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            // Calculate totals
            double totalCalories = meals.stream()
                .mapToDouble(meal -> meal.getTotalCalories().doubleValue())
                .sum();

            int dayCount = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
            double avgDailyCalories = totalCalories / dayCount;

            // Calculate macros
            MacrosDTO totalMacros = calculateTotalMacros(meals);
            MacrosDTO avgDailyMacros = new MacrosDTO(
                totalMacros.getProtein() / dayCount,
                totalMacros.getCarbs() / dayCount,
                totalMacros.getFats() / dayCount
            );

            // Daily breakdown
            List<NutritionSummaryExportDTO.DailyNutritionDTO> dailyBreakdown = calculateDailyNutritionBreakdown(meals, startDate, endDate);

            // Calculate progress metrics
            double calorieTarget = getUserCalorieTarget(userId);
            double calorieTargetProgress = (avgDailyCalories / calorieTarget) * 100;
            String nutritionGoalStatus = determineNutritionGoalStatus(calorieTargetProgress);

            return new NutritionSummaryExportDTO(
                formatDateRange(startDate, endDate),
                totalCalories,
                avgDailyCalories,
                totalMacros,
                avgDailyMacros,
                meals.size(),
                (double) meals.size() / dayCount,
                dailyBreakdown,
                calorieTargetProgress,
                nutritionGoalStatus
            );
        } catch (Exception e) {
            throw new RuntimeException("Error getting nutrition summary for export: " + e.getMessage());
        }
    }

    /**
     * Get workout history data for export
     */
    public WorkoutHistoryExportDTO getWorkoutHistoryForExport(Long userId, String dateRange) {
        try {
            LocalDate[] dates = parseDateRange(dateRange);
            LocalDate startDate = dates[0];
            LocalDate endDate = dates[1];

            List<Workout> workouts = workoutRepository
                .findByUserUserIdAndWorkoutDateBetween(userId, startDate, endDate);

            // Calculate totals
            int totalWorkouts = workouts.size();
            long totalWorkoutDays = workouts.stream()
                .map(Workout::getWorkoutDate)
                .distinct()
                .count();

            double totalCaloriesBurned = workouts.stream()
                .mapToDouble(w -> w.getCaloriesBurned().doubleValue())
                .sum();

            int totalDurationMinutes = workouts.stream()
                .mapToInt(Workout::getDurationMinutes)
                .sum();

            double avgWorkoutDuration = totalWorkouts > 0 ? (double) totalDurationMinutes / totalWorkouts : 0.0;
            double avgCaloriesBurnedPerWorkout = totalWorkouts > 0 ? totalCaloriesBurned / totalWorkouts : 0.0;

            // Workout type distribution
            Map<String, Integer> workoutTypeDistribution = workouts.stream()
                .collect(Collectors.groupingBy(
                    Workout::getWorkoutType,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

            // Daily breakdown
            List<WorkoutHistoryExportDTO.DailyWorkoutDTO> dailyBreakdown = calculateDailyWorkoutBreakdown(workouts, startDate, endDate);

            // Calculate consistency metrics
            int dayCount = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
            double weeklyFrequencyPercentage = ((double) totalWorkoutDays / dayCount) * 100;
            String consistencyRating = determineConsistencyRating(weeklyFrequencyPercentage);

            return new WorkoutHistoryExportDTO(
                formatDateRange(startDate, endDate),
                totalWorkouts,
                (int) totalWorkoutDays,
                totalCaloriesBurned,
                totalDurationMinutes,
                avgWorkoutDuration,
                avgCaloriesBurnedPerWorkout,
                workoutTypeDistribution,
                dailyBreakdown,
                consistencyRating,
                weeklyFrequencyPercentage
            );
        } catch (Exception e) {
            throw new RuntimeException("Error getting workout history for export: " + e.getMessage());
        }
    }

    /**
     * Get progress metrics data for export
     */
    public ProgressMetricsExportDTO getProgressMetricsForExport(Long userId, String dateRange) {
        try {
            LocalDate[] dates = parseDateRange(dateRange);
            LocalDate startDate = dates[0];
            LocalDate endDate = dates[1];

            // Weight progress
            ProgressMetricsExportDTO.WeightProgressDTO weightProgress = calculateWeightProgress(userId, startDate, endDate);

            // Fitness progress
            ProgressMetricsExportDTO.FitnessProgressDTO fitnessProgress = calculateFitnessProgress(userId, startDate, endDate);

            // Nutrition progress
            ProgressMetricsExportDTO.NutritionProgressDTO nutritionProgress = calculateNutritionProgress(userId, startDate, endDate);

            // Weekly trends
            List<ProgressMetricsExportDTO.WeeklyProgressDTO> weeklyTrends = calculateWeeklyTrends(userId, startDate, endDate);

            // Overall ratings
            String overallProgressRating = determineOverallProgressRating(weightProgress, fitnessProgress, nutritionProgress);
            List<String> milestones = calculateMilestones(userId, startDate, endDate);
            String goalProgressStatus = determineGoalProgressStatus(userId);

            return new ProgressMetricsExportDTO(
                formatDateRange(startDate, endDate),
                weightProgress,
                fitnessProgress,
                nutritionProgress,
                weeklyTrends,
                overallProgressRating,
                milestones,
                goalProgressStatus
            );
        } catch (Exception e) {
            throw new RuntimeException("Error getting progress metrics for export: " + e.getMessage());
        }
    }

    /**
     * Get AI insights data for export with date filtering
     */
    public List<AIInsightDTO> getAIInsightsForExport(Long userId, String dateRange, AIInsightService aiInsightService) {
        try {
            // Get latest insights from AI service
            List<AIInsightDTO> insights = aiInsightService.getLatestInsights();

            // Filter by date range if specified
            if (dateRange != null && !dateRange.isEmpty()) {
                LocalDate[] dates = parseDateRange(dateRange);
                LocalDate startDate = dates[0];
                LocalDate endDate = dates[1];

                insights = insights.stream()
                    .filter(insight -> {
                        LocalDate insightDate = insight.getCreatedAt().toLocalDate();
                        return !insightDate.isBefore(startDate) && !insightDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
            }

            return insights;
        } catch (Exception e) {
            // Return empty list if there's an error fetching insights
            return new ArrayList<>();
        }
    }

    /**
     * Get achievements data for export
     */
    public AchievementsExportDTO getAchievementsForExport(Long userId) {
        try {
            // For now, create sample achievements data
            // In a real implementation, you would fetch from an achievements repository

            List<AchievementsExportDTO.AchievementDTO> completedAchievements = createSampleAchievements("completed");
            List<AchievementsExportDTO.AchievementDTO> inProgressAchievements = createSampleAchievements("in_progress");

            StreaksDTO currentStreaks = getCurrentStreaksDTO(userId);
            List<AchievementsExportDTO.MilestoneDTO> milestones = createSampleMilestones(userId);

            int totalAchievements = completedAchievements.size();
            int totalPoints = completedAchievements.stream()
                .mapToInt(AchievementsExportDTO.AchievementDTO::getPoints)
                .sum();

            String currentLevel = determineLevelFromPoints(totalPoints);
            String motivationalMessage = generateMotivationalMessage(currentStreaks);

            return new AchievementsExportDTO(
                completedAchievements,
                inProgressAchievements,
                currentStreaks,
                milestones,
                totalAchievements,
                totalPoints,
                currentLevel,
                motivationalMessage
            );
        } catch (Exception e) {
            throw new RuntimeException("Error getting achievements for export: " + e.getMessage());
        }
    }

    // Helper methods for export functionality

    private LocalDate[] parseDateRange(String dateRange) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        if (dateRange == null || dateRange.isEmpty()) {
            startDate = endDate.minusDays(30); // Default to last 30 days
        } else {
            switch (dateRange.toLowerCase()) {
                case "week":
                case "7d":
                    startDate = endDate.minusDays(7);
                    break;
                case "month":
                case "30d":
                    startDate = endDate.minusDays(30);
                    break;
                case "3months":
                case "90d":
                    startDate = endDate.minusDays(90);
                    break;
                case "year":
                case "365d":
                    startDate = endDate.minusDays(365);
                    break;
                default:
                    // Try to parse as date range format "YYYY-MM-DD,YYYY-MM-DD"
                    if (dateRange.contains(",")) {
                        String[] dates = dateRange.split(",");
                        startDate = LocalDate.parse(dates[0].trim());
                        endDate = LocalDate.parse(dates[1].trim());
                    } else {
                        startDate = endDate.minusDays(30);
                    }
                    break;
            }
        }

        return new LocalDate[]{startDate, endDate};
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.toString() + " to " + endDate.toString();
    }

    /**
     * Build a simple daily progress time series for charts
     * Each point contains: date, calories intake, workout count, and last-known weight
     */
    public List<au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.DailyProgressPointDTO>
    getDailyProgress(Long userId, String dateRange) {
        LocalDate[] dates = parseDateRange(dateRange);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];

        // Nutrition: gather meals in range and sum by day
        List<Meal> meals = mealRepository
            .findByUserUserIdAndMealTimeBetween(userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        Map<LocalDate, Integer> caloriesByDay = meals.stream()
            .collect(Collectors.groupingBy(m -> m.getMealTime().toLocalDate(),
                    Collectors.collectingAndThen(
                        Collectors.summingDouble(m -> m.getTotalCalories() != null ? m.getTotalCalories().doubleValue() : 0.0),
                        d -> (int) Math.round(d)
                    )));

        // Workouts: count by day
        List<Workout> workouts = workoutRepository
            .findByUserUserIdAndWorkoutDateBetween(userId, startDate, endDate);
        Map<LocalDate, Integer> workoutsByDay = workouts.stream()
            .collect(Collectors.groupingBy(Workout::getWorkoutDate,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

        // Weight: carry-forward last-known weight from user_metrics up to each date
        List<UserMetrics> metricsAll = userMetricsRepository
            .findByUserUserIdOrderByCreatedAtDesc(userId);
        // Reverse to ascending by createdAt for carry-forward
        Collections.reverse(metricsAll);

        List<au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.DailyProgressPointDTO> points = new ArrayList<>();
        Double lastWeight = null;
        int metricIdx = 0;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            // advance lastWeight if there is a metric created on/before this day
            while (metricIdx < metricsAll.size()) {
                UserMetrics m = metricsAll.get(metricIdx);
                if (m.getCreatedDate() != null && !m.getCreatedDate().toLocalDate().isAfter(d)) {
                    lastWeight = m.getWeight();
                    metricIdx++;
                } else {
                    break;
                }
            }

            int cals = caloriesByDay.getOrDefault(d, 0);
            int wks = workoutsByDay.getOrDefault(d, 0);
            points.add(new au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.DailyProgressPointDTO(
                d, lastWeight != null ? Math.round(lastWeight * 10.0) / 10.0 : 0.0, cals, wks
            ));
        }

        return points;
    }

    private MacrosDTO calculateTotalMacros(List<Meal> meals) {
        double totalProtein = meals.stream().mapToDouble(m -> m.getProteinG().doubleValue()).sum();
        double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbsG().doubleValue()).sum();
        double totalFats = meals.stream().mapToDouble(m -> m.getFatG().doubleValue()).sum();

        return new MacrosDTO(totalProtein, totalCarbs, totalFats);
    }

    private List<NutritionSummaryExportDTO.DailyNutritionDTO> calculateDailyNutritionBreakdown(List<Meal> meals, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Meal>> dailyMeals = meals.stream()
            .collect(Collectors.groupingBy(meal -> meal.getMealTime().toLocalDate()));

        List<NutritionSummaryExportDTO.DailyNutritionDTO> breakdown = new ArrayList<>();;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Meal> dayMeals = dailyMeals.getOrDefault(date, new ArrayList<>());

            double dailyCalories = dayMeals.stream().mapToDouble(m -> m.getTotalCalories().doubleValue()).sum();
            MacrosDTO dailyMacros = calculateTotalMacros(dayMeals);

            breakdown.add(new NutritionSummaryExportDTO.DailyNutritionDTO(
                date, dailyCalories, dailyMacros, dayMeals.size()
            ));
        }

        return breakdown;
    }

    private List<WorkoutHistoryExportDTO.DailyWorkoutDTO> calculateDailyWorkoutBreakdown(List<Workout> workouts, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Workout>> dailyWorkouts = workouts.stream()
            .collect(Collectors.groupingBy(Workout::getWorkoutDate));

        List<WorkoutHistoryExportDTO.DailyWorkoutDTO> breakdown = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Workout> dayWorkouts = dailyWorkouts.getOrDefault(date, new ArrayList<>());

            int totalDuration = dayWorkouts.stream().mapToInt(Workout::getDurationMinutes).sum();
            double totalCaloriesBurned = dayWorkouts.stream().mapToDouble(w -> w.getCaloriesBurned().doubleValue()).sum();
            List<String> workoutTypes = dayWorkouts.stream().map(Workout::getWorkoutType).distinct().collect(Collectors.toList());

            breakdown.add(new WorkoutHistoryExportDTO.DailyWorkoutDTO(
                date, dayWorkouts.size(), totalDuration, totalCaloriesBurned, workoutTypes
            ));
        }

        return breakdown;
    }

    private String determineNutritionGoalStatus(double calorieTargetProgress) {
        if (calorieTargetProgress >= 90 && calorieTargetProgress <= 110) return "On Track";
        else if (calorieTargetProgress < 80) return "Under Target";
        else if (calorieTargetProgress > 120) return "Over Target";
        else return "Close to Target";
    }

    private String determineConsistencyRating(double frequencyPercentage) {
        if (frequencyPercentage >= 80) return "Excellent";
        else if (frequencyPercentage >= 60) return "Good";
        else if (frequencyPercentage >= 40) return "Fair";
        else return "Needs Improvement";
    }

    // Placeholder methods for complex calculations - implement based on business logic

    private ProgressMetricsExportDTO.WeightProgressDTO calculateWeightProgress(Long userId, LocalDate startDate, LocalDate endDate) {
        // Implementation would calculate weight progress over the date range
        return new ProgressMetricsExportDTO.WeightProgressDTO(70.0, 68.5, -1.5, "down", 65.0, 65.0);
    }

    private ProgressMetricsExportDTO.FitnessProgressDTO calculateFitnessProgress(Long userId, LocalDate startDate, LocalDate endDate) {
        // Implementation would calculate fitness improvements
        return new ProgressMetricsExportDTO.FitnessProgressDTO(20, 45.0, 8000.0, "Intermediate", 15.0, 10.0);
    }

    private ProgressMetricsExportDTO.NutritionProgressDTO calculateNutritionProgress(Long userId, LocalDate startDate, LocalDate endDate) {
        // Implementation would calculate nutrition consistency
        MacrosDTO avgMacros = new MacrosDTO(120.0, 200.0, 80.0);
        return new ProgressMetricsExportDTO.NutritionProgressDTO(2000.0, avgMacros, 85.0, "Good", 25);
    }

    private List<ProgressMetricsExportDTO.WeeklyProgressDTO> calculateWeeklyTrends(Long userId, LocalDate startDate, LocalDate endDate) {
        // Implementation would calculate weekly trends
        List<ProgressMetricsExportDTO.WeeklyProgressDTO> trends = new ArrayList<>();
        for (LocalDate week = startDate; !week.isAfter(endDate); week = week.plusWeeks(1)) {
            trends.add(new ProgressMetricsExportDTO.WeeklyProgressDTO(
                week, -0.5, 4, 2000.0, 2100.0, "Good"
            ));
        }
        return trends;
    }

    private String determineOverallProgressRating(ProgressMetricsExportDTO.WeightProgressDTO weight,
                                                 ProgressMetricsExportDTO.FitnessProgressDTO fitness,
                                                 ProgressMetricsExportDTO.NutritionProgressDTO nutrition) {
        return "Good Progress";
    }

    private List<String> calculateMilestones(Long userId, LocalDate startDate, LocalDate endDate) {
        return Arrays.asList("First 10 workouts completed", "Consistency streak of 7 days", "Weight loss target 50% achieved");
    }

    private String determineGoalProgressStatus(Long userId) {
        return "On Track";
    }

    private List<AchievementsExportDTO.AchievementDTO> createSampleAchievements(String status) {
        List<AchievementsExportDTO.AchievementDTO> achievements = new ArrayList<>();
        if ("completed".equals(status)) {
            achievements.add(new AchievementsExportDTO.AchievementDTO(
                "first_workout", "First Workout", "Complete your first workout", "fitness",
                10, "completed", 100.0, LocalDateTime.now().minusDays(10), "🏃"
            ));
        }
        return achievements;
    }

    private List<AchievementsExportDTO.MilestoneDTO> createSampleMilestones(Long userId) {
        List<AchievementsExportDTO.MilestoneDTO> milestones = new ArrayList<>();
        milestones.add(new AchievementsExportDTO.MilestoneDTO(
            "weight_goal", "Weight Loss Goal", "Reach target weight", "weight",
            65.0, 68.5, 65.0, null, "in_progress"
        ));
        return milestones;
    }

    private String determineLevelFromPoints(int points) {
        if (points >= 1000) return "Expert";
        else if (points >= 500) return "Advanced";
        else if (points >= 200) return "Intermediate";
        else return "Beginner";
    }

    private String generateMotivationalMessage(StreaksDTO streaks) {
        if (streaks.getWorkoutStreak() > 7) {
            return "Amazing! You're on fire with your workout consistency! 🔥";
        } else if (streaks.getNutritionStreak() > 5) {
            return "Great job maintaining your nutrition goals! Keep it up! 💪";
        } else {
            return "Every step forward is progress. You've got this! 🌟";
        }
    }

    /**
     * Calculate total data points for export metadata
     */
    public int calculateTotalDataPoints(Map<String, Object> exportData) {
        int totalPoints = 0;

        // Count data points from each section
        if (exportData.get("userProfile") != null) totalPoints += 1;
        if (exportData.get("nutritionSummary") != null) totalPoints += 1;
        if (exportData.get("exerciseHistory") != null) totalPoints += 1;
        if (exportData.get("progressMetrics") != null) totalPoints += 1;
        if (exportData.get("aiInsights") != null) totalPoints += 1;
        if (exportData.get("achievements") != null) totalPoints += 1;

        return totalPoints;
    }
}
