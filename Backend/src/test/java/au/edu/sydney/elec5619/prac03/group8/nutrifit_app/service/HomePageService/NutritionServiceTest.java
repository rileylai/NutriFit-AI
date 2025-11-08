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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private MealRepository mealRepository;

    @Mock
    private NutritionTargetRepository nutritionTargetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMetricsRepository userMetricsRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private NutritionService nutritionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(15L);
        lenient().when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
    }

    @Test
    void getTodaySummary_returnsAggregatedIntakeAndTargets() {
        LocalDate today = LocalDate.now();
        LocalDateTime breakfastTime = today.atTime(8, 0);
        LocalDateTime lunchTime = today.atTime(13, 0);

        Meal breakfast = meal(1L, "Breakfast", breakfastTime, 500, 30, 40, 20);
        Meal lunch = meal(2L, "Lunch", lunchTime, 350, 15, 45, 10);

        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(15L), any(), any()))
            .thenReturn(List.of(breakfast, lunch));

        NutritionTarget target = new NutritionTarget();
        target.setDailyCalories(2000);
        target.setDailyProtein(140);
        target.setDailyCarbs(260);
        target.setDailyFats(70);
        target.setDailyFiber(25);
        target.setDailySodium(2300);
        target.setIsActive(true);
        target.setStartDate(today.minusDays(10));

        when(nutritionTargetRepository.findCurrentTargetByUserId(15L)).thenReturn(Optional.of(target));

        DailyIntakeSummaryDTO summary = nutritionService.getTodaySummary();

        assertEquals(850, summary.getCurrentIntake().getCalories());
        assertEquals(45.0, summary.getCurrentIntake().getProtein());
        assertEquals(85.0, summary.getCurrentIntake().getCarbs());
        assertEquals(30.0, summary.getCurrentIntake().getFat());
        assertEquals(2000, summary.getDailyTargets().getCalories());
        assertEquals(2300, summary.getDailyTargets().getSodium());
    }

    @Test
    void getTodayMeals_sortsAscendingAndInfersType() {
        LocalDate today = LocalDate.now();
        Meal dinner = meal(100L, "Dinner", today.atTime(19, 0), 600, 35, 60, 25);
        dinner.setRole("dinner");
        Meal breakfast = meal(101L, "Toast", today.atTime(7, 30), 250, 10, 30, 8);
        breakfast.setRole(null);

        when(mealRepository.findByUserUserIdAndMealTimeBetweenOrderByMealTimeDesc(eq(15L), any(), any()))
            .thenReturn(Arrays.asList(dinner, breakfast));

        List<TodayMealDTO> meals = nutritionService.getTodayMeals();

        assertThat(meals).extracting(TodayMealDTO::getId).containsExactly(101L, 100L);
        assertEquals("breakfast", meals.get(0).getType());
        assertEquals("dinner", meals.get(1).getType());
    }

    @Test
    void createMeal_persistsMealForAuthenticatedUser() {
        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> {
            Meal meal = invocation.getArgument(0);
            meal.setMealId(77L);
            return meal;
        });

        CreateMealRequestDTO request = new CreateMealRequestDTO(
            "Yoghurt bowl",
            null,
            BigDecimal.valueOf(320),
            BigDecimal.valueOf(20),
            BigDecimal.valueOf(40),
            BigDecimal.valueOf(8),
            "breakfast",
            LocalDateTime.now().toString(),
            false
        );

        MealDetailResponseDTO response = nutritionService.createMeal(15L, request);

        assertEquals(77L, response.getMealId());
        verify(mealRepository).save(argThat(saved ->
            saved.getUser().getUserId().equals(15L) &&
                saved.getMealDescription().equals("Yoghurt bowl") &&
                Boolean.FALSE.equals(saved.getIsAiGenerated())
        ));
    }

    @Test
    void updateMeal_updatesProvidedFieldsOnly() {
        Meal meal = meal(9L, "Salad", LocalDateTime.now(), 300, 15, 25, 10);
        meal.setUser(user);
        meal.setUserEdited(false);

        when(mealRepository.findById(9L)).thenReturn(Optional.of(meal));
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMealRequestDTO update = new UpdateMealRequestDTO();
        update.setMealDescription("Updated Salad");
        update.setProteinG(BigDecimal.valueOf(18));
        update.setUserEdited(true);

        MealDetailResponseDTO response = nutritionService.updateMeal(15L, 9L, update);

        assertEquals("Updated Salad", response.getMealDescription());
        assertEquals(BigDecimal.valueOf(18), response.getProteinG());
        assertEquals(Boolean.TRUE, response.getUserEdited());
        verify(mealRepository).save(argThat(saved -> saved.getProteinG().equals(BigDecimal.valueOf(18))));
    }

    @Test
    void deleteMeal_throwsWhenMealBelongsToDifferentUser() {
        User other = new User();
        other.setUserId(99L);

        Meal meal = meal(50L, "Soup", LocalDateTime.now(), 200, 8, 20, 5);
        meal.setUser(other);

        when(mealRepository.findById(50L)).thenReturn(Optional.of(meal));

        assertThrows(IllegalArgumentException.class, () -> nutritionService.deleteMeal(15L, 50L));
        verify(mealRepository, never()).delete(any());
    }

    @Test
    void getTodaySummary_returnsDefaultTargetsWhenNoCurrentTarget() {
        when(mealRepository.findByUserUserIdAndMealTimeBetween(eq(15L), any(), any()))
            .thenReturn(List.of());
        when(nutritionTargetRepository.findCurrentTargetByUserId(15L)).thenReturn(Optional.empty());

        DailyIntakeSummaryDTO summary = nutritionService.getTodaySummary();

        assertEquals(2200, summary.getDailyTargets().getCalories());
        assertEquals(0, summary.getCurrentIntake().getCalories());
    }

    @Test
    void getTodaySummary_throwsWhenRequestUserMismatch() {
        assertThrows(IllegalArgumentException.class, () -> nutritionService.getTodaySummary(999L));
    }

    @Test
    void deleteMeal_removesMealWhenOwnedByUser() {
        Meal meal = meal(60L, "Owned", LocalDateTime.now(), 400, 20, 30, 15);
        meal.setUser(user);

        when(mealRepository.findById(60L)).thenReturn(Optional.of(meal));

        nutritionService.deleteMeal(15L, 60L);

        verify(mealRepository).delete(meal);
    }

    private Meal meal(Long id, String name, LocalDateTime time, double calories, double protein, double carbs, double fat) {
        Meal meal = new Meal();
        meal.setMealId(id);
        meal.setMealDescription(name);
        meal.setMealTime(time);
        meal.setTotalCalories(BigDecimal.valueOf(calories));
        meal.setProteinG(BigDecimal.valueOf(protein));
        meal.setCarbsG(BigDecimal.valueOf(carbs));
        meal.setFatG(BigDecimal.valueOf(fat));
        meal.setUser(user);
        return meal;
    }

    private Meal meal(Long id, String name, LocalDate date, double calories, double protein, double carbs, double fat) {
        return meal(id, name, date.atTime(LocalTime.NOON), calories, protein, carbs, fat);
    }
}
