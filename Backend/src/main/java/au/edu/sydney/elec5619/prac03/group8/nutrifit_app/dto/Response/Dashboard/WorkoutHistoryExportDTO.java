package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class WorkoutHistoryExportDTO {
    private String dateRange;
    private Integer totalWorkouts;
    private Integer totalWorkoutDays;
    private Double totalCaloriesBurned;
    private Integer totalDurationMinutes;
    private Double avgWorkoutDuration;
    private Double avgCaloriesBurnedPerWorkout;
    private Map<String, Integer> workoutTypeDistribution;
    private List<DailyWorkoutDTO> dailyBreakdown;
    private String consistencyRating;
    private Double weeklyFrequencyPercentage;

    public WorkoutHistoryExportDTO() {}

    public WorkoutHistoryExportDTO(String dateRange, Integer totalWorkouts, Integer totalWorkoutDays,
                                 Double totalCaloriesBurned, Integer totalDurationMinutes, Double avgWorkoutDuration,
                                 Double avgCaloriesBurnedPerWorkout, Map<String, Integer> workoutTypeDistribution,
                                 List<DailyWorkoutDTO> dailyBreakdown, String consistencyRating,
                                 Double weeklyFrequencyPercentage) {
        this.dateRange = dateRange;
        this.totalWorkouts = totalWorkouts;
        this.totalWorkoutDays = totalWorkoutDays;
        this.totalCaloriesBurned = totalCaloriesBurned;
        this.totalDurationMinutes = totalDurationMinutes;
        this.avgWorkoutDuration = avgWorkoutDuration;
        this.avgCaloriesBurnedPerWorkout = avgCaloriesBurnedPerWorkout;
        this.workoutTypeDistribution = workoutTypeDistribution;
        this.dailyBreakdown = dailyBreakdown;
        this.consistencyRating = consistencyRating;
        this.weeklyFrequencyPercentage = weeklyFrequencyPercentage;
    }

    // Getters and Setters
    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public Integer getTotalWorkoutDays() { return totalWorkoutDays; }
    public void setTotalWorkoutDays(Integer totalWorkoutDays) { this.totalWorkoutDays = totalWorkoutDays; }

    public Double getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(Double totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Integer totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }

    public Double getAvgWorkoutDuration() { return avgWorkoutDuration; }
    public void setAvgWorkoutDuration(Double avgWorkoutDuration) { this.avgWorkoutDuration = avgWorkoutDuration; }

    public Double getAvgCaloriesBurnedPerWorkout() { return avgCaloriesBurnedPerWorkout; }
    public void setAvgCaloriesBurnedPerWorkout(Double avgCaloriesBurnedPerWorkout) { this.avgCaloriesBurnedPerWorkout = avgCaloriesBurnedPerWorkout; }

    public Map<String, Integer> getWorkoutTypeDistribution() { return workoutTypeDistribution; }
    public void setWorkoutTypeDistribution(Map<String, Integer> workoutTypeDistribution) { this.workoutTypeDistribution = workoutTypeDistribution; }

    public List<DailyWorkoutDTO> getDailyBreakdown() { return dailyBreakdown; }
    public void setDailyBreakdown(List<DailyWorkoutDTO> dailyBreakdown) { this.dailyBreakdown = dailyBreakdown; }

    public String getConsistencyRating() { return consistencyRating; }
    public void setConsistencyRating(String consistencyRating) { this.consistencyRating = consistencyRating; }

    public Double getWeeklyFrequencyPercentage() { return weeklyFrequencyPercentage; }
    public void setWeeklyFrequencyPercentage(Double weeklyFrequencyPercentage) { this.weeklyFrequencyPercentage = weeklyFrequencyPercentage; }

    public static class DailyWorkoutDTO {
        private LocalDate date;
        private Integer workoutsCount;
        private Integer totalDuration;
        private Double totalCaloriesBurned;
        private List<String> workoutTypes;

        public DailyWorkoutDTO() {}

        public DailyWorkoutDTO(LocalDate date, Integer workoutsCount, Integer totalDuration,
                             Double totalCaloriesBurned, List<String> workoutTypes) {
            this.date = date;
            this.workoutsCount = workoutsCount;
            this.totalDuration = totalDuration;
            this.totalCaloriesBurned = totalCaloriesBurned;
            this.workoutTypes = workoutTypes;
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public Integer getWorkoutsCount() { return workoutsCount; }
        public void setWorkoutsCount(Integer workoutsCount) { this.workoutsCount = workoutsCount; }

        public Integer getTotalDuration() { return totalDuration; }
        public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }

        public Double getTotalCaloriesBurned() { return totalCaloriesBurned; }
        public void setTotalCaloriesBurned(Double totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

        public List<String> getWorkoutTypes() { return workoutTypes; }
        public void setWorkoutTypes(List<String> workoutTypes) { this.workoutTypes = workoutTypes; }
    }
}