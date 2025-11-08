package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.ImgBBClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.AiMealEstimationRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AiMealEstimationResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Meal;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.MealRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMealServiceTest {

    @Mock
    private ImgBBClient imgBBClient;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AiMealService aiMealService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(15L);
    }

    private void mockCurrentUser() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
    }

    @Test
    void estimateMeal_throwsWhenRequestNull() {
        assertThrows(IllegalArgumentException.class, () -> aiMealService.estimateMeal(null, null));
    }

    @Test
    void estimateMeal_throwsWhenNoInputProvided() {
        mockCurrentUser();
        AiMealEstimationRequestDTO request = new AiMealEstimationRequestDTO(null, null, "lunch", true);

        assertThrows(IllegalArgumentException.class, () -> aiMealService.estimateMeal(null, request));
    }

    @Test
    void estimateMeal_throwsWhenRoleMissing() {
        mockCurrentUser();
        AiMealEstimationRequestDTO request = new AiMealEstimationRequestDTO(null, "description", " ", true);

        assertThrows(IllegalArgumentException.class, () -> aiMealService.estimateMeal(null, request));
    }

    @Test
    void estimateMeal_returnsEstimationWhenNotSaving() {
        mockCurrentUser();
        AiMealEstimationRequestDTO request = new AiMealEstimationRequestDTO(null, "description", "dinner", false);

        GeminiClient.MealEstimation estimation = new GeminiClient.MealEstimation();
        estimation.setMealDescription("AI Salad");
        estimation.setTotalCalories(BigDecimal.valueOf(450));
        estimation.setProteinG(BigDecimal.valueOf(30));
        estimation.setCarbsG(BigDecimal.valueOf(40));
        estimation.setFatG(BigDecimal.valueOf(15));

        when(geminiClient.estimateMeal(null, "description")).thenReturn(estimation);

        AiMealEstimationResponseDTO response = aiMealService.estimateMeal(null, request);

        assertThat(response.getMealId()).isNull();
        assertThat(response.getMealDescription()).isEqualTo("description");
        assertThat(response.getTotalCalories()).isEqualTo(BigDecimal.valueOf(450));
        verify(mealRepository, never()).save(any(Meal.class));
    }

    @Test
    void estimateMeal_uploadsImageAndSavesWhenRequested() {
        mockCurrentUser();
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("meal.jpg");

        AiMealEstimationRequestDTO request = new AiMealEstimationRequestDTO(null, null, "breakfast", true);

        GeminiClient.MealEstimation estimation = new GeminiClient.MealEstimation();
        estimation.setMealDescription("AI Oats");
        estimation.setTotalCalories(BigDecimal.valueOf(320));
        estimation.setProteinG(BigDecimal.valueOf(18));
        estimation.setCarbsG(BigDecimal.valueOf(35));
        estimation.setFatG(BigDecimal.valueOf(8));

        when(imgBBClient.uploadImage(file)).thenReturn("https://img.test/meal.jpg");
        when(geminiClient.estimateMeal("https://img.test/meal.jpg", null)).thenReturn(estimation);
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> {
            Meal meal = invocation.getArgument(0, Meal.class);
            meal.setMealId(99L);
            return meal;
        });

        AiMealEstimationResponseDTO response = aiMealService.estimateMeal(file, request);

        assertThat(response.getMealId()).isEqualTo(99L);
        assertThat(response.getMealDescription()).isEqualTo("AI Oats");
        assertThat(response.getPhotoUrl()).isEqualTo("https://img.test/meal.jpg");

        ArgumentCaptor<Meal> mealCaptor = ArgumentCaptor.forClass(Meal.class);
        verify(mealRepository).save(mealCaptor.capture());
        Meal savedMeal = mealCaptor.getValue();
        assertThat(savedMeal.getUser()).isSameAs(user);
        assertThat(savedMeal.getPhotoUrl()).isEqualTo("https://img.test/meal.jpg");
        assertThat(savedMeal.getIsAiGenerated()).isTrue();
    }
}
