package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Detailed nutrition summary for AI-estimated meals.
 * Can include breakdown by food items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealNutritionSummaryDTO {

    /**
     * Total estimated calories
     */
    private BigDecimal totalCalories;

    /**
     * Total protein in grams
     */
    private BigDecimal totalProteinG;

    /**
     * Total carbohydrates in grams
     */
    private BigDecimal totalCarbsG;

    /**
     * Total fat in grams
     */
    private BigDecimal totalFatG;

    /**
     * Confidence level of the estimation (0.0 to 1.0)
     */
    private Double confidence;

    /**
     * Individual food items detected (optional)
     */
    private List<FoodItemDTO> detectedItems;

    /**
     * Represents an individual food item detected in the meal
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodItemDTO {
        private String foodName;
        private String portionSize;
        private String portionUnit;
        private BigDecimal calories;
        private BigDecimal proteinG;
        private BigDecimal carbsG;
        private BigDecimal fatG;
    }
}
