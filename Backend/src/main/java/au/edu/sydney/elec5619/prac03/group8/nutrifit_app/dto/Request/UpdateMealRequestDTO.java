package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMealRequestDTO {

    private String mealDescription;

    private String photoUrl;

    @Positive(message = "Total calories must be positive")
    private BigDecimal totalCalories;

    private BigDecimal proteinG;

    private BigDecimal carbsG;

    private BigDecimal fatG;

    private String role; // breakfast, lunch, dinner, snack

    private Boolean userEdited = true;
}
