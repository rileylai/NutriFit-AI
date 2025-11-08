package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.WorkoutDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.WorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.WorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.WorkoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<WorkoutResponseDto> createWorkout(@RequestBody WorkoutRequestDto workoutRequestDto) {
        Long userId = resolveAuthenticatedUserId();
        workoutRequestDto.setUserId(userId);
        log.info("Create workout request received for authenticated user ID: {}, type: {}",
                userId, workoutRequestDto.getWorkoutType());
        WorkoutService.WorkoutOperationResult result = workoutService.saveWorkout(workoutRequestDto);
        log.info("Workout creation result for user {}: {} - {}", userId, result.status(), result.body().getMessage());
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @GetMapping
    public ResponseEntity<?> getWorkouts(@RequestParam(value = "workout_date", required = false) String workoutDate,
                                         @RequestParam(value = "sort_by", required = false) String sortBy) {
        Long userId = resolveAuthenticatedUserId();
        log.info("Get workouts request for user ID: {}, date: {}, sortBy: {}", userId, workoutDate, sortBy);
        try {
            List<WorkoutDto> workouts = workoutService.getWorkouts(userId, workoutDate, sortBy);
            log.debug("Retrieved {} workouts for user ID: {}", workouts.size(), userId);
            return ResponseEntity.ok(workouts);
        } catch (IllegalArgumentException validationError) {
            log.warn("Validation error while fetching workouts for user {}: {}", userId, validationError.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new WorkoutResponseDto(false, validationError.getMessage()));
        } catch (IllegalStateException databaseError) {
            log.error("Database error while fetching workouts for user {}: {}", userId, databaseError.getMessage(), databaseError);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WorkoutResponseDto(false, databaseError.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponseDto> updateWorkout(@PathVariable("id") Long workoutId,
                                                            @RequestBody WorkoutRequestDto workoutRequestDto) {
        Long userId = resolveAuthenticatedUserId();
        workoutRequestDto.setUserId(userId);
        log.info("Update workout request for ID: {}, user ID: {}", workoutId, userId);
        WorkoutService.WorkoutOperationResult result = workoutService.updateWorkout(workoutId, workoutRequestDto);
        log.info("Workout update result for ID {}: {} - {}", workoutId, result.status(), result.body().getMessage());
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkoutResponseDto> deleteWorkout(@PathVariable("id") Long workoutId) {
        log.info("Delete workout request for ID: {}", workoutId);
        WorkoutService.WorkoutOperationResult result = workoutService.deleteWorkout(workoutId);
        log.info("Workout deletion result for ID {}: {} - {}", workoutId, result.status(), result.body().getMessage());
        return ResponseEntity.status(result.status()).body(result.body());
    }

    private Long resolveAuthenticatedUserId() {
        if (securityUtil == null) {
            throw new IllegalStateException("SecurityUtil not configured");
        }
        return securityUtil.getCurrentUserOrThrow().getUserId();
    }
}
