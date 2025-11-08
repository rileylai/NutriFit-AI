package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDate;
import java.util.List;

public class ProgressMetricsExportDTO {
    private String dateRange;
    private WeightProgressDTO weightProgress;
    private FitnessProgressDTO fitnessProgress;
    private NutritionProgressDTO nutritionProgress;
    private List<WeeklyProgressDTO> weeklyTrends;
    private String overallProgressRating;
    private List<String> milestones;
    private String goalProgressStatus;

    public ProgressMetricsExportDTO() {}

    public ProgressMetricsExportDTO(String dateRange, WeightProgressDTO weightProgress,
                                  FitnessProgressDTO fitnessProgress, NutritionProgressDTO nutritionProgress,
                                  List<WeeklyProgressDTO> weeklyTrends, String overallProgressRating,
                                  List<String> milestones, String goalProgressStatus) {
        this.dateRange = dateRange;
        this.weightProgress = weightProgress;
        this.fitnessProgress = fitnessProgress;
        this.nutritionProgress = nutritionProgress;
        this.weeklyTrends = weeklyTrends;
        this.overallProgressRating = overallProgressRating;
        this.milestones = milestones;
        this.goalProgressStatus = goalProgressStatus;
    }

    // Getters and Setters
    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public WeightProgressDTO getWeightProgress() { return weightProgress; }
    public void setWeightProgress(WeightProgressDTO weightProgress) { this.weightProgress = weightProgress; }

    public FitnessProgressDTO getFitnessProgress() { return fitnessProgress; }
    public void setFitnessProgress(FitnessProgressDTO fitnessProgress) { this.fitnessProgress = fitnessProgress; }

    public NutritionProgressDTO getNutritionProgress() { return nutritionProgress; }
    public void setNutritionProgress(NutritionProgressDTO nutritionProgress) { this.nutritionProgress = nutritionProgress; }

    public List<WeeklyProgressDTO> getWeeklyTrends() { return weeklyTrends; }
    public void setWeeklyTrends(List<WeeklyProgressDTO> weeklyTrends) { this.weeklyTrends = weeklyTrends; }

    public String getOverallProgressRating() { return overallProgressRating; }
    public void setOverallProgressRating(String overallProgressRating) { this.overallProgressRating = overallProgressRating; }

    public List<String> getMilestones() { return milestones; }
    public void setMilestones(List<String> milestones) { this.milestones = milestones; }

    public String getGoalProgressStatus() { return goalProgressStatus; }
    public void setGoalProgressStatus(String goalProgressStatus) { this.goalProgressStatus = goalProgressStatus; }

    public static class WeightProgressDTO {
        private Double startWeight;
        private Double currentWeight;
        private Double weightChange;
        private String trend;
        private Double targetWeight;
        private Double progressToTarget;

        public WeightProgressDTO() {}

        public WeightProgressDTO(Double startWeight, Double currentWeight, Double weightChange,
                               String trend, Double targetWeight, Double progressToTarget) {
            this.startWeight = startWeight;
            this.currentWeight = currentWeight;
            this.weightChange = weightChange;
            this.trend = trend;
            this.targetWeight = targetWeight;
            this.progressToTarget = progressToTarget;
        }

        // Getters and Setters
        public Double getStartWeight() { return startWeight; }
        public void setStartWeight(Double startWeight) { this.startWeight = startWeight; }

        public Double getCurrentWeight() { return currentWeight; }
        public void setCurrentWeight(Double currentWeight) { this.currentWeight = currentWeight; }

        public Double getWeightChange() { return weightChange; }
        public void setWeightChange(Double weightChange) { this.weightChange = weightChange; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }

        public Double getTargetWeight() { return targetWeight; }
        public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }

        public Double getProgressToTarget() { return progressToTarget; }
        public void setProgressToTarget(Double progressToTarget) { this.progressToTarget = progressToTarget; }
    }

    public static class FitnessProgressDTO {
        private Integer totalWorkouts;
        private Double avgWorkoutDuration;
        private Double totalCaloriesBurned;
        private String fitnessLevel;
        private Double enduranceImprovement;
        private Double strengthImprovement;

        public FitnessProgressDTO() {}

        public FitnessProgressDTO(Integer totalWorkouts, Double avgWorkoutDuration, Double totalCaloriesBurned,
                                String fitnessLevel, Double enduranceImprovement, Double strengthImprovement) {
            this.totalWorkouts = totalWorkouts;
            this.avgWorkoutDuration = avgWorkoutDuration;
            this.totalCaloriesBurned = totalCaloriesBurned;
            this.fitnessLevel = fitnessLevel;
            this.enduranceImprovement = enduranceImprovement;
            this.strengthImprovement = strengthImprovement;
        }

        // Getters and Setters
        public Integer getTotalWorkouts() { return totalWorkouts; }
        public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

        public Double getAvgWorkoutDuration() { return avgWorkoutDuration; }
        public void setAvgWorkoutDuration(Double avgWorkoutDuration) { this.avgWorkoutDuration = avgWorkoutDuration; }

        public Double getTotalCaloriesBurned() { return totalCaloriesBurned; }
        public void setTotalCaloriesBurned(Double totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

        public String getFitnessLevel() { return fitnessLevel; }
        public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }

        public Double getEnduranceImprovement() { return enduranceImprovement; }
        public void setEnduranceImprovement(Double enduranceImprovement) { this.enduranceImprovement = enduranceImprovement; }

        public Double getStrengthImprovement() { return strengthImprovement; }
        public void setStrengthImprovement(Double strengthImprovement) { this.strengthImprovement = strengthImprovement; }
    }

    public static class NutritionProgressDTO {
        private Double avgDailyCalories;
        private MacrosDTO avgMacros;
        private Double targetAdherence;
        private String nutritionQuality;
        private Integer consistentDays;

        public NutritionProgressDTO() {}

        public NutritionProgressDTO(Double avgDailyCalories, MacrosDTO avgMacros, Double targetAdherence,
                                  String nutritionQuality, Integer consistentDays) {
            this.avgDailyCalories = avgDailyCalories;
            this.avgMacros = avgMacros;
            this.targetAdherence = targetAdherence;
            this.nutritionQuality = nutritionQuality;
            this.consistentDays = consistentDays;
        }

        // Getters and Setters
        public Double getAvgDailyCalories() { return avgDailyCalories; }
        public void setAvgDailyCalories(Double avgDailyCalories) { this.avgDailyCalories = avgDailyCalories; }

        public MacrosDTO getAvgMacros() { return avgMacros; }
        public void setAvgMacros(MacrosDTO avgMacros) { this.avgMacros = avgMacros; }

        public Double getTargetAdherence() { return targetAdherence; }
        public void setTargetAdherence(Double targetAdherence) { this.targetAdherence = targetAdherence; }

        public String getNutritionQuality() { return nutritionQuality; }
        public void setNutritionQuality(String nutritionQuality) { this.nutritionQuality = nutritionQuality; }

        public Integer getConsistentDays() { return consistentDays; }
        public void setConsistentDays(Integer consistentDays) { this.consistentDays = consistentDays; }
    }

    public static class WeeklyProgressDTO {
        private LocalDate weekStartDate;
        private Double weeklyWeightChange;
        private Integer weeklyWorkouts;
        private Double weeklyCaloriesBurned;
        private Double weeklyAvgCaloriesIntake;
        private String weeklyProgressRating;

        public WeeklyProgressDTO() {}

        public WeeklyProgressDTO(LocalDate weekStartDate, Double weeklyWeightChange, Integer weeklyWorkouts,
                               Double weeklyCaloriesBurned, Double weeklyAvgCaloriesIntake, String weeklyProgressRating) {
            this.weekStartDate = weekStartDate;
            this.weeklyWeightChange = weeklyWeightChange;
            this.weeklyWorkouts = weeklyWorkouts;
            this.weeklyCaloriesBurned = weeklyCaloriesBurned;
            this.weeklyAvgCaloriesIntake = weeklyAvgCaloriesIntake;
            this.weeklyProgressRating = weeklyProgressRating;
        }

        // Getters and Setters
        public LocalDate getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

        public Double getWeeklyWeightChange() { return weeklyWeightChange; }
        public void setWeeklyWeightChange(Double weeklyWeightChange) { this.weeklyWeightChange = weeklyWeightChange; }

        public Integer getWeeklyWorkouts() { return weeklyWorkouts; }
        public void setWeeklyWorkouts(Integer weeklyWorkouts) { this.weeklyWorkouts = weeklyWorkouts; }

        public Double getWeeklyCaloriesBurned() { return weeklyCaloriesBurned; }
        public void setWeeklyCaloriesBurned(Double weeklyCaloriesBurned) { this.weeklyCaloriesBurned = weeklyCaloriesBurned; }

        public Double getWeeklyAvgCaloriesIntake() { return weeklyAvgCaloriesIntake; }
        public void setWeeklyAvgCaloriesIntake(Double weeklyAvgCaloriesIntake) { this.weeklyAvgCaloriesIntake = weeklyAvgCaloriesIntake; }

        public String getWeeklyProgressRating() { return weeklyProgressRating; }
        public void setWeeklyProgressRating(String weeklyProgressRating) { this.weeklyProgressRating = weeklyProgressRating; }
    }
}