package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.ErrorResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.AiMealEstimationRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AiMealEstimationResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.AiMealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for AI-powered meal estimation.
 * Handles image upload and nutrition estimation requests.
 */
@RestController
@RequestMapping("/api/meals/ai")
public class AiMealController {

    private static final Logger logger = LoggerFactory.getLogger(AiMealController.class);

    private final AiMealService aiMealService;

    public AiMealController(AiMealService aiMealService) {
        this.aiMealService = aiMealService;
    }

    /**
     * Estimates meal nutrition from an uploaded image and/or text description.
     *
     * POST /api/meals/ai/estimate
     *
     * Request parameters:
     * - image (MultipartFile, optional): The meal image file
     * - description (String, optional): Text description of the meal
     * - role (String, required): Meal type - breakfast, lunch, dinner, or snack
     * - save (Boolean, optional): Whether to save the meal (default: true)
     *
     * At least one of image or description must be provided.
     *
     * @param image Optional image file
     * @param description Optional text description
     * @param role Meal type (required)
     * @param save Whether to save the meal (optional, defaults to true)
     * @return Response with nutrition estimates
     */
    @PostMapping(value = "/estimate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> estimateMeal(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "role") String role,
            @RequestParam(value = "save", required = false) Boolean save) {

        logger.info("Received AI meal estimation request - role: {}, hasImage: {}, hasDescription: {}, save: {}",
                role, image != null && !image.isEmpty(), description != null && !description.isEmpty(), save);

        try {
            // Build request DTO
            AiMealEstimationRequestDTO request = new AiMealEstimationRequestDTO();
            request.setDescription(description);
            request.setRole(role);
            request.setSave(save);

            // Validate request
            if (!request.hasValidInput() && (image == null || image.isEmpty())) {
                logger.warn("Invalid request: no image or description provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponseDTO("INVALID_REQUEST",
                                "At least one of image or description must be provided"));
            }

            // Call service
            AiMealEstimationResponseDTO response = aiMealService.estimateMeal(image, request);

            logger.info("AI meal estimation successful - mealId: {}, calories: {}",
                    response.getMealId(), response.getTotalCalories());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            logger.error("Bad request for AI meal estimation: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("INVALID_REQUEST", ex.getMessage()));

        } catch (IllegalStateException ex) {
            logger.error("Internal error during AI meal estimation: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO("SERVICE_ERROR",
                            "AI meal estimation failed: " + ex.getMessage()));

        } catch (Exception ex) {
            logger.error("Unexpected error during AI meal estimation: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO("SERVICE_ERROR",
                            "An unexpected error occurred: " + ex.getMessage()));
        }
    }
}
