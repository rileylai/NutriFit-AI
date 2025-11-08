package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

/**
 * DTO for streak data
 */
public class StreaksDTO {
    private Integer workoutStreak;
    private Integer nutritionStreak;
    private Integer consistencyStreak;
    private String workoutStreakStatus; // "none", "building", "good", "great", "excellent"
    private String nutritionStreakStatus;
    
    // Constructors
    public StreaksDTO() {}
    
    public StreaksDTO(Integer workoutStreak, Integer nutritionStreak, Integer consistencyStreak, 
                     String workoutStreakStatus, String nutritionStreakStatus) {
        this.workoutStreak = workoutStreak;
        this.nutritionStreak = nutritionStreak;
        this.consistencyStreak = consistencyStreak;
        this.workoutStreakStatus = workoutStreakStatus;
        this.nutritionStreakStatus = nutritionStreakStatus;
    }
    
    // Getters and Setters
    public Integer getWorkoutStreak() { return workoutStreak; }
    public void setWorkoutStreak(Integer workoutStreak) { this.workoutStreak = workoutStreak; }
    
    public Integer getNutritionStreak() { return nutritionStreak; }
    public void setNutritionStreak(Integer nutritionStreak) { this.nutritionStreak = nutritionStreak; }
    
    public Integer getConsistencyStreak() { return consistencyStreak; }
    public void setConsistencyStreak(Integer consistencyStreak) { this.consistencyStreak = consistencyStreak; }
    
    public String getWorkoutStreakStatus() { return workoutStreakStatus; }
    public void setWorkoutStreakStatus(String workoutStreakStatus) { this.workoutStreakStatus = workoutStreakStatus; }
    
    public String getNutritionStreakStatus() { return nutritionStreakStatus; }
    public void setNutritionStreakStatus(String nutritionStreakStatus) { this.nutritionStreakStatus = nutritionStreakStatus; }
}
