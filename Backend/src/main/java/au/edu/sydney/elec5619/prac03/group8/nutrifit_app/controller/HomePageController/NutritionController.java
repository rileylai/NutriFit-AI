package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller.HomePageController;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.CreateMealRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.UpdateMealRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.MealDetailResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Nutrition.DailyIntakeSummaryDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Nutrition.TodayMealDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.ErrorResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService.NutritionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/homepage/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;
    private final SecurityUtil securityUtil;

    @GetMapping("/today-summary")
    public ResponseEntity<?> getTodaySummary() {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            DailyIntakeSummaryDTO dto = nutritionService.getTodaySummary(userId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO("FORBIDDEN", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to load today's intake: " + e.getMessage()));
        }
    }

    @GetMapping("/today-meals")
    public ResponseEntity<?> getTodayMeals() {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            List<TodayMealDTO> meals = nutritionService.getTodayMeals(userId);
            return ResponseEntity.ok(meals);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO("FORBIDDEN", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to load today's meals: " + e.getMessage()));
        }
    }

    @DeleteMapping("/meals/{mealId}")
    public ResponseEntity<?> deleteMeal(
            @PathVariable Long mealId) {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            nutritionService.deleteMeal(userId, mealId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to delete meal: " + e.getMessage()));
        }
    }

    @PostMapping("/meals")
    public ResponseEntity<?> createMeal(@Valid @RequestBody CreateMealRequestDTO request) {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            MealDetailResponseDTO meal = nutritionService.createMeal(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(meal);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to create meal: " + e.getMessage()));
        }
    }

    @PutMapping("/meals/{mealId}")
    public ResponseEntity<?> updateMeal(
            @PathVariable Long mealId,
            @Valid @RequestBody UpdateMealRequestDTO request) {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            MealDetailResponseDTO meal = nutritionService.updateMeal(userId, mealId, request);
            return ResponseEntity.ok(meal);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to update meal: " + e.getMessage()));
        }
    }

    @GetMapping("/meals/{mealId}")
    public ResponseEntity<?> getMeal(@PathVariable Long mealId) {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            MealDetailResponseDTO meal = nutritionService.getMeal(userId, mealId);
            return ResponseEntity.ok(meal);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to get meal: " + e.getMessage()));
        }
    }

    @GetMapping("/meals")
    public ResponseEntity<?> getAllMeals() {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            List<MealDetailResponseDTO> meals = nutritionService.getAllMeals(userId);
            return ResponseEntity.ok(meals);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to get meals: " + e.getMessage()));
        }
    }
}
