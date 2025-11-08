package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class CalorieEstimationService {

    private final GeminiClient geminiClient;

    public CalorieEstimationService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public BigDecimal estimateCalories(String workoutType, int durationMinutes) {
        String description = String.format("%s for %d minutes", workoutType, durationMinutes);
        var estimation = geminiClient.estimateWorkout(description);

        if (estimation == null || estimation.getCaloriesBurned() == null) {
            throw new IllegalStateException("Failed to estimate calories.");
        }

        return estimation.getCaloriesBurned();
    }
}