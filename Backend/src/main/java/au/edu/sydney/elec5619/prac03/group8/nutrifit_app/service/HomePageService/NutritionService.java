package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.CreateMealRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.UpdateMealRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.MealDetailResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Nutrition.DailyIntakeSummaryDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Nutrition.TodayMealDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Meal;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.NutritionTarget;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.MealRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.NutritionTargetRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserMetricsRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final MealRepository mealRepository;
    private final NutritionTargetRepository nutritionTargetRepository;
    private final UserRepository userRepository;
    private final UserMetricsRepository userMetricsRepository;
    private final SecurityUtil securityUtil;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public DailyIntakeSummaryDTO getTodaySummary() {
        return getTodaySummary(null);
    }

    public DailyIntakeSummaryDTO getTodaySummary(Long userId) {
        Long resolvedUserId = resolveUserId(userId);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        List<Meal> meals = mealRepository.findByUserUserIdAndMealTimeBetween(resolvedUserId, start, end);

        int calories = (int) Math.round(meals.stream()
            .map(m -> m.getTotalCalories() == null ? 0.0 : m.getTotalCalories().doubleValue())
            .mapToDouble(Double::doubleValue)
            .sum());

        double protein = meals.stream().map(m -> m.getProteinG() == null ? 0.0 : m.getProteinG().doubleValue()).mapToDouble(Double::doubleValue).sum();
        double carbs   = meals.stream().map(m -> m.getCarbsG()   == null ? 0.0 : m.getCarbsG().doubleValue()).mapToDouble(Double::doubleValue).sum();
        double fat     = meals.stream().map(m -> m.getFatG()     == null ? 0.0 : m.getFatG().doubleValue()).mapToDouble(Double::doubleValue).sum();

        DailyIntakeSummaryDTO.Intake intake = new DailyIntakeSummaryDTO.Intake(
            calories,
            round1(protein),
            round1(carbs),
            round1(fat),
            0, // fiber not tracked in schema
            0  // sodium not tracked in schema
        );

        DailyIntakeSummaryDTO.Targets targets = resolveTargets(resolvedUserId);
        return new DailyIntakeSummaryDTO(intake, targets);
    }

    public List<TodayMealDTO> getTodayMeals() {
        return getTodayMeals(null);
    }

    public List<TodayMealDTO> getTodayMeals(Long userId) {
        Long resolvedUserId = resolveUserId(userId);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        return mealRepository
            .findByUserUserIdAndMealTimeBetweenOrderByMealTimeDesc(resolvedUserId, start, end)
            .stream()
            .sorted(Comparator.comparing(Meal::getMealTime))
            .map(this::toTodayMeal)
            .collect(Collectors.toList());
    }

    private TodayMealDTO toTodayMeal(Meal m) {
        String type = (m.getRole() != null && !m.getRole().isBlank())
            ? m.getRole()
            : inferTypeFromTime(m);
        return new TodayMealDTO(
            m.getMealId(),
            m.getMealDescription(),
            safeInt(m.getTotalCalories()),
            safeDouble(m.getProteinG()),
            safeDouble(m.getCarbsG()),
            safeDouble(m.getFatG()),
            m.getMealTime() != null ? m.getMealTime().format(TIME_FMT) : "",
            type
        );
    }

    private String inferTypeFromTime(Meal m) {
        if (m.getMealTime() == null) return "snack";
        int hour = m.getMealTime().getHour();
        if (hour < 11) return "breakfast";
        if (hour < 16) return "lunch";
        if (hour < 18) return "snack";
        return "dinner";
    }

    public void deleteMeal(Long userId, Long mealId) {
        // Ensure the meal exists and belongs to the user
        Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new IllegalArgumentException("Meal not found"));
        if (meal.getUser() == null || !meal.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Meal does not belong to the specified user");
        }

        // Cascade on Meal -> MealItem ensures meal_items are removed
        mealRepository.delete(meal);
    }

    private DailyIntakeSummaryDTO.Targets resolveTargets(Long userId) {
        // First try to get from explicit nutrition target
        Optional<NutritionTarget> current = nutritionTargetRepository.findCurrentTargetByUserId(userId);
        if (current.isPresent()) {
            NutritionTarget nt = current.get();
            return new DailyIntakeSummaryDTO.Targets(
                safeInt(nt.getDailyCalories()),
                safeInt(nt.getDailyProtein()),
                safeInt(nt.getDailyCarbs()),
                safeInt(nt.getDailyFats()),
                safeInt(nt.getDailyFiber()),
                safeInt(nt.getDailySodium())
            );
        }

        // If no explicit target, try to use BMR from latest user metrics
        Optional<au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserMetrics> latestMetrics =
            userMetricsRepository.findTopByUserUserIdOrderByCreatedAtDesc(userId);

        if (latestMetrics.isPresent() && latestMetrics.get().getBmr() != null) {
            int bmrCalories = safeInt(latestMetrics.get().getBmr());
            // Calculate macros based on BMR (40% carbs, 30% protein, 30% fat)
            int protein = (int) Math.round((bmrCalories * 0.30) / 4.0); // 4 cal/g
            int carbs = (int) Math.round((bmrCalories * 0.40) / 4.0); // 4 cal/g
            int fat = (int) Math.round((bmrCalories * 0.30) / 9.0); // 9 cal/g

            return new DailyIntakeSummaryDTO.Targets(
                bmrCalories,
                protein,
                carbs,
                fat,
                25,   // fiber default
                2300  // sodium default
            );
        }

        // Ultimate fallback defaults if no target and no metrics
        return new DailyIntakeSummaryDTO.Targets(2200, 140, 275, 73, 25, 2300);
    }

    private Long resolveUserId(Long requestedUserId) {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        if (requestedUserId != null && !requestedUserId.equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("Authenticated user does not match request payload");
        }
        return currentUser.getUserId();
    }

    public MealDetailResponseDTO createMeal(Long userId, CreateMealRequestDTO request) {
        Long resolvedUserId = resolveUserId(userId);
        User user = userRepository.findById(resolvedUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Meal meal = new Meal();
        meal.setUser(user);
        meal.setMealDescription(request.getMealDescription());
        meal.setPhotoUrl(request.getPhotoUrl());
        meal.setTotalCalories(request.getTotalCalories());
        meal.setProteinG(request.getProteinG());
        meal.setCarbsG(request.getCarbsG());
        meal.setFatG(request.getFatG());
        meal.setRole(request.getRole());
        meal.setIsAiGenerated(request.getIsAiGenerated() != null ? request.getIsAiGenerated() : false);
        meal.setUserEdited(false);

        // Parse mealTime if provided, otherwise use current time
        if (request.getMealTime() != null && !request.getMealTime().isBlank()) {
            try {
                meal.setMealTime(LocalDateTime.parse(request.getMealTime()));
            } catch (Exception e) {
                meal.setMealTime(LocalDateTime.now());
            }
        } else {
            meal.setMealTime(LocalDateTime.now());
        }

        Meal savedMeal = mealRepository.save(meal);
        return toMealDetailDTO(savedMeal);
    }

    public MealDetailResponseDTO updateMeal(Long userId, Long mealId, UpdateMealRequestDTO request) {
        Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new IllegalArgumentException("Meal not found"));

        if (meal.getUser() == null || !meal.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Meal does not belong to the specified user");
        }

        // Update only provided fields
        if (request.getMealDescription() != null) {
            meal.setMealDescription(request.getMealDescription());
        }
        if (request.getPhotoUrl() != null) {
            meal.setPhotoUrl(request.getPhotoUrl());
        }
        if (request.getTotalCalories() != null) {
            meal.setTotalCalories(request.getTotalCalories());
        }
        if (request.getProteinG() != null) {
            meal.setProteinG(request.getProteinG());
        }
        if (request.getCarbsG() != null) {
            meal.setCarbsG(request.getCarbsG());
        }
        if (request.getFatG() != null) {
            meal.setFatG(request.getFatG());
        }
        if (request.getRole() != null) {
            meal.setRole(request.getRole());
        }
        if (request.getUserEdited() != null) {
            meal.setUserEdited(request.getUserEdited());
        }

        Meal savedMeal = mealRepository.save(meal);
        return toMealDetailDTO(savedMeal);
    }

    public MealDetailResponseDTO getMeal(Long userId, Long mealId) {
        Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new IllegalArgumentException("Meal not found"));

        if (meal.getUser() == null || !meal.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Meal does not belong to the specified user");
        }

        return toMealDetailDTO(meal);
    }

    public List<MealDetailResponseDTO> getAllMeals(Long userId) {
        Long resolvedUserId = resolveUserId(userId);
        List<Meal> meals = mealRepository.findByUserUserId(resolvedUserId);
        return meals.stream()
            .map(this::toMealDetailDTO)
            .collect(Collectors.toList());
    }

    private MealDetailResponseDTO toMealDetailDTO(Meal meal) {
        return new MealDetailResponseDTO(
            meal.getMealId(),
            meal.getMealDescription(),
            meal.getPhotoUrl(),
            meal.getTotalCalories(),
            meal.getProteinG(),
            meal.getCarbsG(),
            meal.getFatG(),
            meal.getRole(),
            meal.getMealTime(),
            meal.getIsAiGenerated(),
            meal.getUserEdited()
        );
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    private int safeInt(Number n) { return n == null ? 0 : Math.round(n.floatValue()); }
    private double safeDouble(Number n) { return n == null ? 0.0 : n.doubleValue(); }
}
