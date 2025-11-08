package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;



/**
 * DTO for daily averages data
 */
public class DailyAveragesDTO {
    private Integer avgCaloriesIntake;
    private MacrosDTO avgMacros;
    private Integer avgWorkoutDuration;
    private Integer avgCaloriesBurned;
    private Integer periodDays;
    private Integer totalCaloriesIntake;
    private Integer totalWorkoutDuration;
    private Integer totalCaloriesBurned;
    private Integer workoutCount;
    
    // Constructors
    public DailyAveragesDTO() {}
    
    public DailyAveragesDTO(Integer avgCaloriesIntake, MacrosDTO avgMacros, 
                           Integer avgWorkoutDuration, Integer avgCaloriesBurned, Integer periodDays,
                           Integer totalCaloriesIntake, Integer totalWorkoutDuration,
                           Integer totalCaloriesBurned, Integer workoutCount) {
        this.avgCaloriesIntake = avgCaloriesIntake;
        this.avgMacros = avgMacros;
        this.avgWorkoutDuration = avgWorkoutDuration;
        this.avgCaloriesBurned = avgCaloriesBurned;
        this.periodDays = periodDays;
        this.totalCaloriesIntake = totalCaloriesIntake;
        this.totalWorkoutDuration = totalWorkoutDuration;
        this.totalCaloriesBurned = totalCaloriesBurned;
        this.workoutCount = workoutCount;
    }
    
    // Getters and Setters
    public Integer getAvgCaloriesIntake() { return avgCaloriesIntake; }
    public void setAvgCaloriesIntake(Integer avgCaloriesIntake) { this.avgCaloriesIntake = avgCaloriesIntake; }
    
    public MacrosDTO getAvgMacros() { return avgMacros; }
    public void setAvgMacros(MacrosDTO avgMacros) { this.avgMacros = avgMacros; }
    
    public Integer getAvgWorkoutDuration() { return avgWorkoutDuration; }
    public void setAvgWorkoutDuration(Integer avgWorkoutDuration) { this.avgWorkoutDuration = avgWorkoutDuration; }
    
    public Integer getAvgCaloriesBurned() { return avgCaloriesBurned; }
    public void setAvgCaloriesBurned(Integer avgCaloriesBurned) { this.avgCaloriesBurned = avgCaloriesBurned; }
    
    public Integer getPeriodDays() { return periodDays; }
    public void setPeriodDays(Integer periodDays) { this.periodDays = periodDays; }

    public Integer getTotalCaloriesIntake() { return totalCaloriesIntake; }
    public void setTotalCaloriesIntake(Integer totalCaloriesIntake) { this.totalCaloriesIntake = totalCaloriesIntake; }

    public Integer getTotalWorkoutDuration() { return totalWorkoutDuration; }
    public void setTotalWorkoutDuration(Integer totalWorkoutDuration) { this.totalWorkoutDuration = totalWorkoutDuration; }

    public Integer getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(Integer totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }
}
