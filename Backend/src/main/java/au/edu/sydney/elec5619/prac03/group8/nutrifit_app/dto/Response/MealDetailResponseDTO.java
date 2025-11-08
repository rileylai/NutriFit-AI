package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealDetailResponseDTO {

    private Long mealId;
    private String mealDescription;
    private String photoUrl;
    private BigDecimal totalCalories;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
    private String role;
    private LocalDateTime mealTime;
    private Boolean isAiGenerated;
    private Boolean userEdited;
}
