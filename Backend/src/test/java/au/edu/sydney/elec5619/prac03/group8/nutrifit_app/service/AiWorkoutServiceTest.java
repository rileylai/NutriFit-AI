package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.AiWorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.WorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AiWorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.WorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiWorkoutServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private WorkoutService workoutService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AiWorkoutService aiWorkoutService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(21L);
    }

    private void mockCurrentUser() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
    }

    @Test
    void estimateWorkout_throwsWhenRequestNull() {
        assertThrows(IllegalArgumentException.class, () -> aiWorkoutService.estimateWorkout(null));
    }

    @Test
    void estimateWorkout_throwsWhenDescriptionMissing() {
        mockCurrentUser();
        AiWorkoutRequestDto request = AiWorkoutRequestDto.builder()
            .description(" ")
            .save(false)
            .build();

        assertThrows(IllegalArgumentException.class, () -> aiWorkoutService.estimateWorkout(request));
    }

    @Test
    void estimateWorkout_returnsSummaryWhenNotSaving() {
        mockCurrentUser();
        AiWorkoutRequestDto request = AiWorkoutRequestDto.builder()
            .description("Light jog for 30 minutes")
            .save(false)
            .build();

        GeminiClient.GeminiEstimation estimation = new GeminiClient.GeminiEstimation();
        ReflectionTestUtils.setField(estimation, "durationMinutes", 30);
        ReflectionTestUtils.setField(estimation, "exerciseType", "Jogging");
        ReflectionTestUtils.setField(estimation, "caloriesBurned", BigDecimal.valueOf(250));

        when(geminiClient.estimateWorkout("Light jog for 30 minutes")).thenReturn(estimation);

        AiWorkoutResponseDto response = aiWorkoutService.estimateWorkout(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Calorie estimation successful");
        assertThat(response.getData().getExerciseType()).isEqualTo("Jogging");

        verify(workoutService, never()).saveWorkout(any(WorkoutRequestDto.class));
    }

    @Test
    void estimateWorkout_savesWorkoutWhenRequested() {
        mockCurrentUser();
        AiWorkoutRequestDto request = AiWorkoutRequestDto.builder()
            .description("Rowing for 20 minutes")
            .save(true)
            .workoutDate(LocalDate.of(2025, 1, 1))
            .notes("Keep up")
            .build();

        GeminiClient.GeminiEstimation estimation = new GeminiClient.GeminiEstimation();
        ReflectionTestUtils.setField(estimation, "durationMinutes", 20);
        ReflectionTestUtils.setField(estimation, "exerciseType", "Rowing");
        ReflectionTestUtils.setField(estimation, "caloriesBurned", BigDecimal.valueOf(180));

        when(geminiClient.estimateWorkout("Rowing for 20 minutes")).thenReturn(estimation);
        WorkoutResponseDto workoutResponse = new WorkoutResponseDto(true, "Created");
        WorkoutService.WorkoutOperationResult saveResult =
            new WorkoutService.WorkoutOperationResult(HttpStatus.CREATED, workoutResponse);
        when(workoutService.saveWorkout(any(WorkoutRequestDto.class))).thenReturn(saveResult);

        AiWorkoutResponseDto response = aiWorkoutService.estimateWorkout(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("Calorie estimation successful");

        ArgumentCaptor<WorkoutRequestDto> captor = ArgumentCaptor.forClass(WorkoutRequestDto.class);
        verify(workoutService).saveWorkout(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(21L);
        assertThat(captor.getValue().getWorkoutDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void estimateWorkout_throwsWhenSaveFails() {
        mockCurrentUser();
        AiWorkoutRequestDto request = AiWorkoutRequestDto.builder()
            .description("Workout failure case")
            .save(true)
            .build();

        GeminiClient.GeminiEstimation estimation = new GeminiClient.GeminiEstimation();
        ReflectionTestUtils.setField(estimation, "durationMinutes", 10);
        ReflectionTestUtils.setField(estimation, "exerciseType", "Yoga");
        ReflectionTestUtils.setField(estimation, "caloriesBurned", BigDecimal.valueOf(50));

        when(geminiClient.estimateWorkout("Workout failure case")).thenReturn(estimation);
        WorkoutResponseDto workoutResponse = new WorkoutResponseDto(false, "persist failed");
        WorkoutService.WorkoutOperationResult saveResult =
            new WorkoutService.WorkoutOperationResult(HttpStatus.BAD_REQUEST, workoutResponse);
        when(workoutService.saveWorkout(any(WorkoutRequestDto.class))).thenReturn(saveResult);

        assertThrows(IllegalStateException.class, () -> aiWorkoutService.estimateWorkout(request));
    }
}
