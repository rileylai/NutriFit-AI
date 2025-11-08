package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.util.Map;

/**
 * DTO for workout frequency data
 */
public class WorkoutFrequencyDTO {
    private Long workoutDays;
    private Integer totalDays;
    private Double frequencyPercentage;
    private Map<String, Long> workoutTypes;
    private Integer totalWorkouts;
    
    // Constructors
    public WorkoutFrequencyDTO() {}
    
    public WorkoutFrequencyDTO(Long workoutDays, Integer totalDays, Double frequencyPercentage, 
                              Map<String, Long> workoutTypes, Integer totalWorkouts) {
        this.workoutDays = workoutDays;
        this.totalDays = totalDays;
        this.frequencyPercentage = frequencyPercentage;
        this.workoutTypes = workoutTypes;
        this.totalWorkouts = totalWorkouts;
    }
    
    // Getters and Setters
    public Long getWorkoutDays() { return workoutDays; }
    public void setWorkoutDays(Long workoutDays) { this.workoutDays = workoutDays; }
    
    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    
    public Double getFrequencyPercentage() { return frequencyPercentage; }
    public void setFrequencyPercentage(Double frequencyPercentage) { this.frequencyPercentage = frequencyPercentage; }
    
    public Map<String, Long> getWorkoutTypes() { return workoutTypes; }
    public void setWorkoutTypes(Map<String, Long> workoutTypes) { this.workoutTypes = workoutTypes; }
    
    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }
}
