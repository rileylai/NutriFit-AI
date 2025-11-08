package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for AI meal estimation.
 * At least one of imageUrl or description must be provided.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMealEstimationRequestDTO {

    /**
     * URL of the uploaded image (from picui.cn)
     */
    private String imageUrl;

    /**
     * Optional text description provided by user
     */
    private String description;

    /**
     * Meal type: breakfast, lunch, dinner, or snack
     */
    @NotBlank(message = "Meal type (role) is required")
    private String role;

    /**
     * Whether to save the estimation as a Meal entity.
     * Defaults to true if not specified.
     */
    private Boolean save;

    /**
     * Determines if the estimation should be saved.
     * Defaults to true when save is null.
     *
     * @return true if should save, false otherwise
     */
    public boolean shouldSave() {
        return !Boolean.FALSE.equals(save);
    }

    /**
     * Validates that at least one input (image or description) is provided.
     *
     * @return true if valid, false otherwise
     */
    public boolean hasValidInput() {
        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();
        boolean hasDescription = description != null && !description.trim().isEmpty();
        return hasImage || hasDescription;
    }
}
