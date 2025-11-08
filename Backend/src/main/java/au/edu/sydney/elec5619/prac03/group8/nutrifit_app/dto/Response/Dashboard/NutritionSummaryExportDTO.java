package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDate;
import java.util.List;

public class NutritionSummaryExportDTO {
    private String dateRange;
    private Double totalCaloriesConsumed;
    private Double avgDailyCalories;
    private MacrosDTO totalMacros;
    private MacrosDTO avgDailyMacros;
    private Integer totalMeals;
    private Double avgMealsPerDay;
    private List<DailyNutritionDTO> dailyBreakdown;
    private Double calorieTargetProgress;
    private String nutritionGoalStatus;

    public NutritionSummaryExportDTO() {}

    public NutritionSummaryExportDTO(String dateRange, Double totalCaloriesConsumed, Double avgDailyCalories,
                                   MacrosDTO totalMacros, MacrosDTO avgDailyMacros, Integer totalMeals,
                                   Double avgMealsPerDay, List<DailyNutritionDTO> dailyBreakdown,
                                   Double calorieTargetProgress, String nutritionGoalStatus) {
        this.dateRange = dateRange;
        this.totalCaloriesConsumed = totalCaloriesConsumed;
        this.avgDailyCalories = avgDailyCalories;
        this.totalMacros = totalMacros;
        this.avgDailyMacros = avgDailyMacros;
        this.totalMeals = totalMeals;
        this.avgMealsPerDay = avgMealsPerDay;
        this.dailyBreakdown = dailyBreakdown;
        this.calorieTargetProgress = calorieTargetProgress;
        this.nutritionGoalStatus = nutritionGoalStatus;
    }

    // Getters and Setters
    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public Double getTotalCaloriesConsumed() { return totalCaloriesConsumed; }
    public void setTotalCaloriesConsumed(Double totalCaloriesConsumed) { this.totalCaloriesConsumed = totalCaloriesConsumed; }

    public Double getAvgDailyCalories() { return avgDailyCalories; }
    public void setAvgDailyCalories(Double avgDailyCalories) { this.avgDailyCalories = avgDailyCalories; }

    public MacrosDTO getTotalMacros() { return totalMacros; }
    public void setTotalMacros(MacrosDTO totalMacros) { this.totalMacros = totalMacros; }

    public MacrosDTO getAvgDailyMacros() { return avgDailyMacros; }
    public void setAvgDailyMacros(MacrosDTO avgDailyMacros) { this.avgDailyMacros = avgDailyMacros; }

    public Integer getTotalMeals() { return totalMeals; }
    public void setTotalMeals(Integer totalMeals) { this.totalMeals = totalMeals; }

    public Double getAvgMealsPerDay() { return avgMealsPerDay; }
    public void setAvgMealsPerDay(Double avgMealsPerDay) { this.avgMealsPerDay = avgMealsPerDay; }

    public List<DailyNutritionDTO> getDailyBreakdown() { return dailyBreakdown; }
    public void setDailyBreakdown(List<DailyNutritionDTO> dailyBreakdown) { this.dailyBreakdown = dailyBreakdown; }

    public Double getCalorieTargetProgress() { return calorieTargetProgress; }
    public void setCalorieTargetProgress(Double calorieTargetProgress) { this.calorieTargetProgress = calorieTargetProgress; }

    public String getNutritionGoalStatus() { return nutritionGoalStatus; }
    public void setNutritionGoalStatus(String nutritionGoalStatus) { this.nutritionGoalStatus = nutritionGoalStatus; }

    public static class DailyNutritionDTO {
        private LocalDate date;
        private Double dailyCalories;
        private MacrosDTO dailyMacros;
        private Integer mealsCount;

        public DailyNutritionDTO() {}

        public DailyNutritionDTO(LocalDate date, Double dailyCalories, MacrosDTO dailyMacros, Integer mealsCount) {
            this.date = date;
            this.dailyCalories = dailyCalories;
            this.dailyMacros = dailyMacros;
            this.mealsCount = mealsCount;
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public Double getDailyCalories() { return dailyCalories; }
        public void setDailyCalories(Double dailyCalories) { this.dailyCalories = dailyCalories; }

        public MacrosDTO getDailyMacros() { return dailyMacros; }
        public void setDailyMacros(MacrosDTO dailyMacros) { this.dailyMacros = dailyMacros; }

        public Integer getMealsCount() { return mealsCount; }
        public void setMealsCount(Integer mealsCount) { this.mealsCount = mealsCount; }
    }
}