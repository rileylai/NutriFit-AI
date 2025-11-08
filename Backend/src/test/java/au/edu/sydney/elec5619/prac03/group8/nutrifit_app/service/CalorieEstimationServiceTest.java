package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalorieEstimationServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private CalorieEstimationService calorieEstimationService;

    @Test
    void estimateCalories_returnsCaloriesFromGemini() {
        GeminiClient.GeminiEstimation estimation = new GeminiClient.GeminiEstimation();
        ReflectionTestUtils.setField(estimation, "caloriesBurned", BigDecimal.valueOf(420));

        when(geminiClient.estimateWorkout("Cycling for 45 minutes")).thenReturn(estimation);

        BigDecimal calories = calorieEstimationService.estimateCalories("Cycling", 45);

        assertThat(calories).isEqualByComparingTo("420");
    }

    @Test
    void estimateCalories_throwsWhenGeminiReturnsNull() {
        when(geminiClient.estimateWorkout("Rowing for 10 minutes")).thenReturn(null);

        assertThrows(IllegalStateException.class,
            () -> calorieEstimationService.estimateCalories("Rowing", 10));
    }

    @Test
    void estimateCalories_throwsWhenCaloriesMissing() {
        GeminiClient.GeminiEstimation estimation = new GeminiClient.GeminiEstimation();
        when(geminiClient.estimateWorkout("Yoga for 30 minutes")).thenReturn(estimation);

        assertThrows(IllegalStateException.class,
            () -> calorieEstimationService.estimateCalories("Yoga", 30));
    }
}
