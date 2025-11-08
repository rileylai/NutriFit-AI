package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;


/**
 * Main DTO for the quick-stats endpoint
 */
public class QuickStatsResponseDTO {
    private BodyMetricsDTO bodyMetrics;
    private DailyAveragesDTO weeklyAverages;
    private DailyAveragesDTO monthlyAverages;
    private WorkoutFrequencyDTO workoutFrequency;
    private StreaksDTO streaks;
    
    // Constructors, getters, setters
    public QuickStatsResponseDTO() {}
    
    public QuickStatsResponseDTO(BodyMetricsDTO bodyMetrics, DailyAveragesDTO weeklyAverages, 
                                DailyAveragesDTO monthlyAverages, WorkoutFrequencyDTO workoutFrequency, 
                                StreaksDTO streaks) {
        this.bodyMetrics = bodyMetrics;
        this.weeklyAverages = weeklyAverages;
        this.monthlyAverages = monthlyAverages;
        this.workoutFrequency = workoutFrequency;
        this.streaks = streaks;
    }
    
    // Getters and Setters
    public BodyMetricsDTO getBodyMetrics() { return bodyMetrics; }
    public void setBodyMetrics(BodyMetricsDTO bodyMetrics) { this.bodyMetrics = bodyMetrics; }
    
    public DailyAveragesDTO getWeeklyAverages() { return weeklyAverages; }
    public void setWeeklyAverages(DailyAveragesDTO weeklyAverages) { this.weeklyAverages = weeklyAverages; }
    
    public DailyAveragesDTO getMonthlyAverages() { return monthlyAverages; }
    public void setMonthlyAverages(DailyAveragesDTO monthlyAverages) { this.monthlyAverages = monthlyAverages; }
    
    public WorkoutFrequencyDTO getWorkoutFrequency() { return workoutFrequency; }
    public void setWorkoutFrequency(WorkoutFrequencyDTO workoutFrequency) { this.workoutFrequency = workoutFrequency; }
    
    public StreaksDTO getStreaks() { return streaks; }
    public void setStreaks(StreaksDTO streaks) { this.streaks = streaks; }
}
