package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDateTime;
/**
 * DTO for body metrics data
 */
public class BodyMetricsDTO {
    private Double weight;
    private Double height;
    private Double bmi;
    private Double bmr;
    private Double weightChange;
    private String weightTrend; // "up", "down", "stable", "no_data"
    private LocalDateTime lastUpdated;
    
    // Constructors
    public BodyMetricsDTO() {}
    
    public BodyMetricsDTO(Double weight, Double height, Double bmi, Double bmr, 
                         Double weightChange, String weightTrend, LocalDateTime lastUpdated) {
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.bmr = bmr;
        this.weightChange = weightChange;
        this.weightTrend = weightTrend;
        this.lastUpdated = lastUpdated;
    }
    
    // Getters and Setters
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
    
    public Double getBmr() { return bmr; }
    public void setBmr(Double bmr) { this.bmr = bmr; }
    
    public Double getWeightChange() { return weightChange; }
    public void setWeightChange(Double weightChange) { this.weightChange = weightChange; }
    
    public String getWeightTrend() { return weightTrend; }
    public void setWeightTrend(String weightTrend) { this.weightTrend = weightTrend; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
