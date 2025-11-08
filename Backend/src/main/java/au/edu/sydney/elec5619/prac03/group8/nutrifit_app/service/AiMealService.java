package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.ImgBBClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.AiMealEstimationRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AiMealEstimationResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Meal;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.MealRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Service for AI-powered meal estimation.
 * Handles image upload, AI nutrition estimation, and optional meal persistence.
 */
@Service
public class AiMealService {

    private static final Logger logger = LoggerFactory.getLogger(AiMealService.class);

    private final ImgBBClient imgBBClient;
    private final GeminiClient geminiClient;
    private final MealRepository mealRepository;
    private final SecurityUtil securityUtil;

    public AiMealService(ImgBBClient imgBBClient,
                         GeminiClient geminiClient,
                         MealRepository mealRepository,
                         SecurityUtil securityUtil) {
        this.imgBBClient = imgBBClient;
        this.geminiClient = geminiClient;
        this.mealRepository = mealRepository;
        this.securityUtil = securityUtil;
    }

    /**
     * Estimates meal nutrition from an uploaded image and/or text description.
     * Optionally saves the meal to the database.
     *
     * @param imageFile Optional image file to upload
     * @param request Request containing description, role, and save preference
     * @return Response with estimated nutrition values
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if external API calls fail
     */
    public AiMealEstimationResponseDTO estimateMeal(MultipartFile imageFile, AiMealEstimationRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        logger.info("Processing AI meal estimation request - role: {}, shouldSave: {}",
                request.getRole(), request.shouldSave());

        // Get current user
        User currentUser = securityUtil.getCurrentUserOrThrow();

        // Step 1: Upload image if provided
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            logger.info("Uploading image file: {}", imageFile.getOriginalFilename());
            imageUrl = imgBBClient.uploadImage(imageFile);
            request.setImageUrl(imageUrl);
            logger.info("Image uploaded successfully: {}", imageUrl);
        }

        // Validate that at least one input is provided
        if (!request.hasValidInput()) {
            throw new IllegalArgumentException("At least one of image or description must be provided.");
        }

        if (!StringUtils.hasText(request.getRole())) {
            throw new IllegalArgumentException("Meal role (breakfast/lunch/dinner/snack) is required.");
        }

        // Step 2: Call AI estimation
        logger.info("Calling AI for meal estimation");
        GeminiClient.MealEstimation estimation = geminiClient.estimateMeal(
                request.getImageUrl(),
                request.getDescription()
        );

        // Use AI description if no user description provided
        String finalDescription = StringUtils.hasText(request.getDescription())
                ? request.getDescription()
                : estimation.getMealDescription();

        logger.info("AI estimation completed: {} calories, description: {}",
                estimation.getTotalCalories(), estimation.getMealDescription());

        // Step 3: If shouldSave is false, return estimation only
        if (!request.shouldSave()) {
            logger.info("Returning estimation without saving");
            return AiMealEstimationResponseDTO.fromEstimation(
                    imageUrl,
                    finalDescription,
                    estimation.getTotalCalories(),
                    estimation.getProteinG(),
                    estimation.getCarbsG(),
                    estimation.getFatG(),
                    request.getRole()
            );
        }

        // Step 4: Save meal to database
        logger.info("Saving AI-estimated meal to database for user: {}", currentUser.getUserId());
        Meal savedMeal = saveMeal(
                currentUser,
                imageUrl,
                finalDescription,
                estimation.getTotalCalories(),
                estimation.getProteinG(),
                estimation.getCarbsG(),
                estimation.getFatG(),
                request.getRole()
        );

        logger.info("Meal saved successfully with ID: {}", savedMeal.getMealId());

        return AiMealEstimationResponseDTO.fromSavedMeal(
                savedMeal.getMealId(),
                savedMeal.getPhotoUrl(),
                savedMeal.getMealDescription(),
                savedMeal.getTotalCalories(),
                savedMeal.getProteinG(),
                savedMeal.getCarbsG(),
                savedMeal.getFatG(),
                savedMeal.getRole()
        );
    }

    /**
     * Saves a meal entity with AI-generated data.
     */
    private Meal saveMeal(User user, String photoUrl, String description,
                          java.math.BigDecimal calories, java.math.BigDecimal protein,
                          java.math.BigDecimal carbs, java.math.BigDecimal fat,
                          String role) {
        Meal meal = new Meal();
        meal.setUser(user);
        meal.setPhotoUrl(photoUrl);
        meal.setMealDescription(description);
        meal.setTotalCalories(calories);
        meal.setProteinG(protein);
        meal.setCarbsG(carbs);
        meal.setFatG(fat);
        meal.setRole(role);
        meal.setMealTime(LocalDateTime.now());
        meal.setIsAiGenerated(true);
        meal.setUserEdited(false);

        return mealRepository.save(meal);
    }
}
