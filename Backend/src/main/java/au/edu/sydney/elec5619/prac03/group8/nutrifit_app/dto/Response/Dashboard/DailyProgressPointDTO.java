package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDate;

public class DailyProgressPointDTO {
    private LocalDate date;
    private Double weight;    // kg
    private Integer calories; // intake kcal
    private Integer workouts; // count

    public DailyProgressPointDTO() {}

    public DailyProgressPointDTO(LocalDate date, Double weight, Integer calories, Integer workouts) {
        this.date = date;
        this.weight = weight;
        this.calories = calories;
        this.workouts = workouts;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    public Integer getWorkouts() { return workouts; }
    public void setWorkouts(Integer workouts) { this.workouts = workouts; }
}

