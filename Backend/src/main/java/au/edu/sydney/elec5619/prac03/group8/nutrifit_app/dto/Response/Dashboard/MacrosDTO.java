package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

/**
 * DTO for macronutrient data
 */
public class MacrosDTO {
    private Double protein;
    private Double carbs;
    private Double fats;
    
    // Constructors
    public MacrosDTO() {}
    
    public MacrosDTO(Double protein, Double carbs, Double fats) {
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }
    
    // Getters and Setters
    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }
    
    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }
    
    public Double getFats() { return fats; }
    public void setFats(Double fats) { this.fats = fats; }
}

