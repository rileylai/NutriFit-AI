package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMealRequestDTO {

    @NotBlank(message = "Meal description is required")
    private String mealDescription;

    private String photoUrl;

    @NotNull(message = "Total calories is required")
    @Positive(message = "Total calories must be positive")
    private BigDecimal totalCalories;

    @NotNull(message = "Protein is required")
    private BigDecimal proteinG;

    @NotNull(message = "Carbs is required")
    private BigDecimal carbsG;

    @NotNull(message = "Fat is required")
    private BigDecimal fatG;

    @NotBlank(message = "Role is required")
    private String role; // breakfast, lunch, dinner, snack

    private String mealTime; // ISO datetime string, optional

    private Boolean isAiGenerated = false;
}
