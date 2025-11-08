package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.AiWorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.WorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AiWorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.WorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiWorkoutService {

    private final GeminiClient geminiClient;
    private final WorkoutService workoutService;
    private final SecurityUtil securityUtil;

    public AiWorkoutService(GeminiClient geminiClient,
                            WorkoutService workoutService,
                            SecurityUtil securityUtil) {
        this.geminiClient = geminiClient;
        this.workoutService = workoutService;
        this.securityUtil = securityUtil;
    }

    public AiWorkoutResponseDto estimateWorkout(AiWorkoutRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        User currentUser = securityUtil.getCurrentUserOrThrow();
        Long userId = currentUser.getUserId();
        request.setUserId(userId);

        if (!StringUtils.hasText(request.getDescription())) {
            throw new IllegalArgumentException("Workout description is required.");
        }

        GeminiClient.GeminiEstimation estimation = geminiClient.estimateWorkout(request.getDescription());

        AiWorkoutResponseDto.AiWorkoutSummary summary = AiWorkoutResponseDto.AiWorkoutSummary.builder()
                .durationMinutes(estimation.getDurationMinutes())
                .exerciseType(estimation.getExerciseType())
                .caloriesBurned(estimation.getCaloriesBurned())
                .build();

        if (!request.shouldSave()) {
            return AiWorkoutResponseDto.builder()
                    .success(true)
                    .message("Calorie estimation successful")
                    .data(summary)
                    .build();
        }

        WorkoutRequestDto workoutRequest = new WorkoutRequestDto(
                userId,
                request.getWorkoutDate() != null ? request.getWorkoutDate() : LocalDate.now(),
                summary.getExerciseType(),
                summary.getDurationMinutes(),
                summary.getCaloriesBurned(),
                request.getNotes()
        );

        WorkoutService.WorkoutOperationResult saveResult = workoutService.saveWorkout(workoutRequest);
        WorkoutResponseDto responseDto = saveResult.body();

        if (saveResult.status() != HttpStatus.CREATED || responseDto == null || !responseDto.isSuccess()) {
            String message = responseDto != null && StringUtils.hasText(responseDto.getMessage())
                    ? responseDto.getMessage()
                    : "Failed to save AI generated workout.";
            throw new IllegalStateException(message);
        }

        return AiWorkoutResponseDto.builder()
                .success(true)
                .message("Calorie estimation successful. " + responseDto.getMessage())
                .data(summary)
                .build();
    }
}
