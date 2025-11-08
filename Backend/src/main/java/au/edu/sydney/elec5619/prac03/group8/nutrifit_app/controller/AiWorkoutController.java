package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.AiWorkoutRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AiWorkoutResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.AiWorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts/ai")
public class AiWorkoutController {

    private final AiWorkoutService aiWorkoutService;

    public AiWorkoutController(AiWorkoutService aiWorkoutService) {
        this.aiWorkoutService = aiWorkoutService;
    }

    @PostMapping
    public ResponseEntity<AiWorkoutResponseDto> estimateCalories(@RequestBody AiWorkoutRequestDto request) {
        try {
            AiWorkoutResponseDto response = aiWorkoutService.estimateWorkout(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AiWorkoutResponseDto.builder()
                            .success(false)
                            .message(ex.getMessage())
                            .build());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AiWorkoutResponseDto.builder()
                            .success(false)
                            .message(ex.getMessage())
                            .build());
        }
    }
}
