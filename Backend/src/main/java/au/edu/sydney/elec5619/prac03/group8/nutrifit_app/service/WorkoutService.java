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
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final CalorieEstimationService calorieEstimationService;


    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository, CalorieEstimationService calorieEstimationService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.calorieEstimationService = calorieEstimationService;
    }

    @Transactional
    public WorkoutOperationResult saveWorkout(WorkoutRequestDto request) {
        log.debug("Saving workout for user ID: {}, type: {}, duration: {} mins",
                request.getUserId(), request.getWorkoutType(), request.getDurationMinutes());
        try {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                log.warn("User not found with ID: {}", request.getUserId());
                return new WorkoutOperationResult(HttpStatus.BAD_REQUEST,
                        new WorkoutResponseDto(false, "User with ID " + request.getUserId() + " does not exist."));
            }

            Workout workout = new Workout();
            workout.setUser(user);
            workout.setWorkoutType(request.getWorkoutType());
            workout.setWorkoutDate(request.getWorkoutDate());
            workout.setDurationMinutes(request.getDurationMinutes());
            // workout.setCaloriesBurned(request.getCaloriesBurned());
            BigDecimal calories = calorieEstimationService.estimateCalories(
                    request.getWorkoutType(),
                    request.getDurationMinutes()
                );
            workout.setCaloriesBurned(calories);
            workout.setNotes(request.getNotes());

            Workout savedWorkout = workoutRepository.save(workout);
            log.info("Workout created successfully with ID: {} for user: {}, calories: {}",
                    savedWorkout.getWorkoutId(), request.getUserId(), calories);
            return new WorkoutOperationResult(HttpStatus.CREATED,
                    new WorkoutResponseDto(true, "Workout created with ID " + savedWorkout.getWorkoutId() + "."));
        } catch (DataAccessException ex) {
            log.error("Database error while creating workout for user {}: {}", request.getUserId(), ex.getMessage(), ex);
            return new WorkoutOperationResult(HttpStatus.INTERNAL_SERVER_ERROR,
                    new WorkoutResponseDto(false, "Database error while creating workout."));
        }
    }

    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkouts(Long userId, String workoutDate, String sortBy) {
        log.debug("Fetching workouts for user ID: {}, date: {}, sortBy: {}", userId, workoutDate, sortBy);
        if (!userRepository.existsById(userId)) {
            log.warn("User not found with ID: {}", userId);
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }

        Sort sort = buildSort(sortBy);

        try {
            if (StringUtils.hasText(workoutDate)) {
                LocalDate parsedDate = parseDate(workoutDate);
                List<WorkoutDto> workouts = workoutRepository.findByUserUserIdAndWorkoutDate(userId, parsedDate, sort)
                        .stream()
                        .map(this::toDto)
                        .toList();
                log.debug("Found {} workouts for user {} on date {}", workouts.size(), userId, parsedDate);
                return workouts;
            }

            List<WorkoutDto> workouts = workoutRepository.findByUserUserId(userId, sort)
                    .stream()
                    .map(this::toDto)
                    .toList();
            log.debug("Found {} total workouts for user {}", workouts.size(), userId);
            return workouts;
        } catch (DataAccessException ex) {
            log.error("Database error while fetching workouts for user {}: {}", userId, ex.getMessage(), ex);
            throw new IllegalStateException("Database error while fetching workouts.", ex);
        }
    }

    @Transactional
    public WorkoutOperationResult updateWorkout(Long workoutId, WorkoutRequestDto request) {
        log.debug("Updating workout ID: {} for user ID: {}", workoutId, request.getUserId());
        try {
            Workout workout = workoutRepository.findById(workoutId).orElse(null);
            if (workout == null) {
                log.warn("Workout not found with ID: {}", workoutId);
                return new WorkoutOperationResult(HttpStatus.NOT_FOUND,
                        new WorkoutResponseDto(false, "Workout with ID " + workoutId + " not found."));
            }

            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                log.warn("User not found with ID: {}", request.getUserId());
                return new WorkoutOperationResult(HttpStatus.BAD_REQUEST,
                        new WorkoutResponseDto(false, "User with ID " + request.getUserId() + " does not exist."));
            }

            workout.setUser(user);
            workout.setWorkoutType(request.getWorkoutType());
            workout.setWorkoutDate(request.getWorkoutDate());
            workout.setDurationMinutes(request.getDurationMinutes());
            // workout.setCaloriesBurned(request.getCaloriesBurned());
            BigDecimal calories = request.getCaloriesBurned();
            if (calories == null) {
                calories = calorieEstimationService.estimateCalories(
                    request.getWorkoutType(),
                    request.getDurationMinutes()
                );
            }
            workout.setCaloriesBurned(calories);
            workout.setNotes(request.getNotes());

            workoutRepository.save(workout);
            log.info("Workout updated successfully, ID: {}, new calories: {}", workoutId, calories);
            return new WorkoutOperationResult(HttpStatus.OK,
                    new WorkoutResponseDto(true, "Workout with ID " + workoutId + " updated."));
        } catch (DataAccessException ex) {
            log.error("Database error while updating workout {}: {}", workoutId, ex.getMessage(), ex);
            return new WorkoutOperationResult(HttpStatus.INTERNAL_SERVER_ERROR,
                    new WorkoutResponseDto(false, "Database error while updating workout."));
        }
    }

    @Transactional
    public WorkoutOperationResult deleteWorkout(Long workoutId) {
        log.debug("Deleting workout ID: {}", workoutId);
        try {
            Workout workout = workoutRepository.findById(workoutId).orElse(null);
            if (workout == null) {
                log.warn("Workout not found with ID: {}", workoutId);
                return new WorkoutOperationResult(HttpStatus.NOT_FOUND,
                        new WorkoutResponseDto(false, "Workout with ID " + workoutId + " not found."));
            }

            workoutRepository.delete(workout);
            log.info("Workout deleted successfully, ID: {}", workoutId);
            return new WorkoutOperationResult(HttpStatus.OK,
                    new WorkoutResponseDto(true, "Workout with ID " + workoutId + " deleted."));
        } catch (DataAccessException ex) {
            log.error("Database error while deleting workout {}: {}", workoutId, ex.getMessage(), ex);
            return new WorkoutOperationResult(HttpStatus.INTERNAL_SERVER_ERROR,
                    new WorkoutResponseDto(false, "Database error while deleting workout."));
        }
    }

    private Sort buildSort(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return Sort.unsorted();
        }

        return switch (sortBy) {
            case "date_asc" -> Sort.by(Sort.Order.asc("workoutDate"));
            case "date_desc" -> Sort.by(Sort.Order.desc("workoutDate"));
            case "type" -> Sort.by(Sort.Order.asc("workoutType"));
            case "calories_desc" -> Sort.by(Sort.Order.desc("caloriesBurned"));
            default -> Sort.unsorted();
        };
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid workout_date format. Use yyyy-MM-dd.", ex);
        }
    }

    private WorkoutDto toDto(Workout workout) {
        return new WorkoutDto(
                workout.getWorkoutId(),
                workout.getUser().getUserId(),
                workout.getWorkoutType(),
                workout.getWorkoutDate(),
                workout.getDurationMinutes(),
                workout.getCaloriesBurned(),
                workout.getNotes(),
                workout.getCreatedAt(),
                workout.getUpdatedAt()
        );
    }

    public record WorkoutOperationResult(HttpStatus status, WorkoutResponseDto body) {
    }
}
