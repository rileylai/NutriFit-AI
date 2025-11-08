package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.WorkoutDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.WorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.WorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Workout;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.WorkoutRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalorieEstimationService calorieEstimationService;

    private WorkoutService workoutService;

    @BeforeEach
    void setUp() {
        workoutService = new WorkoutService(workoutRepository, userRepository, calorieEstimationService);
    }

    @Test
    void saveWorkout_returnsBadRequestWhenUserMissing() {
        WorkoutRequestDto request = new WorkoutRequestDto(5L, LocalDate.now(), "Run", 30, null, null);
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        WorkoutService.WorkoutOperationResult result = workoutService.saveWorkout(request);

        assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.body().isSuccess()).isFalse();
        verify(workoutRepository, never()).save(any());
    }

    @Test
    void saveWorkout_persistsWorkoutAndReturnsCreated() {
        WorkoutRequestDto request = new WorkoutRequestDto(5L, LocalDate.now(), "Run", 30, null, "notes");
        User user = buildUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(calorieEstimationService.estimateCalories("Run", 30)).thenReturn(BigDecimal.valueOf(320));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> {
            Workout workout = invocation.getArgument(0, Workout.class);
            workout.setWorkoutId(99L);
            return workout;
        });

        WorkoutService.WorkoutOperationResult result = workoutService.saveWorkout(request);

        assertThat(result.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.body().isSuccess()).isTrue();

        ArgumentCaptor<Workout> captor = ArgumentCaptor.forClass(Workout.class);
        verify(workoutRepository).save(captor.capture());
        Workout saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCaloriesBurned()).isEqualByComparingTo("320");
    }

    @Test
    void saveWorkout_handlesDataAccessException() {
        WorkoutRequestDto request = new WorkoutRequestDto(5L, LocalDate.now(), "Run", 30, null, null);
        User user = buildUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(calorieEstimationService.estimateCalories("Run", 30)).thenReturn(BigDecimal.TEN);
        when(workoutRepository.save(any(Workout.class)))
            .thenThrow(new DataAccessResourceFailureException("db down"));

        WorkoutService.WorkoutOperationResult result = workoutService.saveWorkout(request);

        assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.body().isSuccess()).isFalse();
    }

    @Test
    void getWorkouts_throwsWhenUserMissing() {
        when(userRepository.existsById(42L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
            () -> workoutService.getWorkouts(42L, null, null));
    }

    @Test
    void getWorkouts_returnsFilteredByDate() {
        LocalDate date = LocalDate.of(2025, 2, 1);
        when(userRepository.existsById(5L)).thenReturn(true);

        Workout workout = buildWorkout(1L, date, "Swim", BigDecimal.valueOf(300));
        when(workoutRepository.findByUserUserIdAndWorkoutDate(eq(5L), eq(date), any(Sort.class)))
            .thenReturn(List.of(workout));

        List<WorkoutDto> dtos = workoutService.getWorkouts(5L, "2025-02-01", "calories_desc");

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getCaloriesBurned()).isEqualByComparingTo("300");
        assertThat(dtos.get(0).getWorkoutType()).isEqualTo("Swim");
    }

    @Test
    void getWorkouts_returnsAllWhenNoDateGiven() {
        when(userRepository.existsById(5L)).thenReturn(true);
        Workout workout = buildWorkout(2L, LocalDate.now(), "Cycle", BigDecimal.valueOf(400));
        when(workoutRepository.findByUserUserId(eq(5L), any(Sort.class)))
            .thenReturn(List.of(workout));

        List<WorkoutDto> dtos = workoutService.getWorkouts(5L, "", "date_desc");

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getWorkoutId()).isEqualTo(2L);
    }

    @Test
    void getWorkouts_throwsForInvalidDateFormat() {
        when(userRepository.existsById(5L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> workoutService.getWorkouts(5L, "2025-13-01", null));
    }

    @Test
    void getWorkouts_wrapsDataAccessException() {
        when(userRepository.existsById(5L)).thenReturn(true);
        when(workoutRepository.findByUserUserId(eq(5L), any(Sort.class)))
            .thenThrow(new DataAccessResourceFailureException("db"));

        assertThrows(IllegalStateException.class,
            () -> workoutService.getWorkouts(5L, null, null));
    }

    @Test
    void updateWorkout_returnsNotFoundWhenWorkoutMissing() {
        when(workoutRepository.findById(10L)).thenReturn(Optional.empty());

        WorkoutService.WorkoutOperationResult result = workoutService.updateWorkout(10L,
            new WorkoutRequestDto(5L, LocalDate.now(), "Run", 20, BigDecimal.ONE, null));

        assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateWorkout_returnsBadRequestWhenUserMissing() {
        Workout workout = buildWorkout(10L, LocalDate.now(), "Run", BigDecimal.TEN);
        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        WorkoutService.WorkoutOperationResult result = workoutService.updateWorkout(10L,
            new WorkoutRequestDto(5L, LocalDate.now(), "Run", 20, BigDecimal.ONE, null));

        assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateWorkout_updatesWithProvidedCalories() {
        Workout workout = buildWorkout(10L, LocalDate.now(), "Run", BigDecimal.TEN);
        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        User user = buildUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(workoutRepository.save(workout)).thenReturn(workout);

        WorkoutRequestDto request = new WorkoutRequestDto(5L, LocalDate.now(), "Cycle", 40,
            BigDecimal.valueOf(500), "notes");

        WorkoutService.WorkoutOperationResult result = workoutService.updateWorkout(10L, request);

        assertThat(result.status()).isEqualTo(HttpStatus.OK);
        assertThat(workout.getCaloriesBurned()).isEqualByComparingTo("500");
        verify(calorieEstimationService, never()).estimateCalories(anyString(), anyInt());
    }

    @Test
    void updateWorkout_estimatesCaloriesWhenMissing() {
        Workout workout = buildWorkout(10L, LocalDate.now(), "Run", BigDecimal.TEN);
        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        User user = buildUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(calorieEstimationService.estimateCalories("Cycle", 40)).thenReturn(BigDecimal.valueOf(350));
        when(workoutRepository.save(workout)).thenReturn(workout);

        WorkoutRequestDto request = new WorkoutRequestDto(5L, LocalDate.now(), "Cycle", 40,
            null, null);

        workoutService.updateWorkout(10L, request);

        assertThat(workout.getCaloriesBurned()).isEqualByComparingTo("350");
        verify(calorieEstimationService).estimateCalories("Cycle", 40);
    }

    @Test
    void updateWorkout_handlesDataAccessException() {
        Workout workout = buildWorkout(10L, LocalDate.now(), "Run", BigDecimal.TEN);
        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        User user = buildUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(calorieEstimationService.estimateCalories("Cycle", 40)).thenReturn(BigDecimal.valueOf(350));
        when(workoutRepository.save(workout))
            .thenThrow(new DataAccessResourceFailureException("db"));

        WorkoutService.WorkoutOperationResult result = workoutService.updateWorkout(10L,
            new WorkoutRequestDto(5L, LocalDate.now(), "Cycle", 40, null, null));

        assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteWorkout_returnsNotFoundWhenMissing() {
        when(workoutRepository.findById(10L)).thenReturn(Optional.empty());

        WorkoutService.WorkoutOperationResult result = workoutService.deleteWorkout(10L);

        assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteWorkout_deletesWhenPresent() {
        Workout workout = buildWorkout(10L, LocalDate.now(), "Run", BigDecimal.TEN);
        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        doNothing().when(workoutRepository).delete(workout);

        WorkoutService.WorkoutOperationResult result = workoutService.deleteWorkout(10L);

        assertThat(result.status()).isEqualTo(HttpStatus.OK);
        verify(workoutRepository).delete(workout);
    }

    @Test
    void deleteWorkout_handlesDataAccessException() {
        Workout workout = buildWorkout(10L, LocalDate.now(), "Run", BigDecimal.TEN);
        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        doThrow(new DataAccessResourceFailureException("db"))
            .when(workoutRepository).delete(workout);

        WorkoutService.WorkoutOperationResult result = workoutService.deleteWorkout(10L);

        assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private User buildUser(long id) {
        User user = new User();
        user.setUserId(id);
        user.setUserName("User " + id);
        return user;
    }

    private Workout buildWorkout(long id, LocalDate date, String type, BigDecimal calories) {
        Workout workout = new Workout();
        workout.setWorkoutId(id);
        workout.setUser(buildUser(5L));
        workout.setWorkoutType(type);
        workout.setWorkoutDate(date);
        workout.setDurationMinutes(30);
        workout.setCaloriesBurned(calories);
        workout.setNotes("notes");
        workout.setCreatedAt(LocalDateTime.now());
        workout.setUpdatedAt(LocalDateTime.now());
        return workout;
    }
}
