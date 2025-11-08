package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkoutResponseDto {

    private boolean success;
    private String message;
    private AiWorkoutSummary data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiWorkoutSummary {

        @JsonProperty("duration_minutes")
        private Integer durationMinutes;

        @JsonProperty("exercise_type")
        private String exerciseType;

        @JsonProperty("calories_burned")
        private BigDecimal caloriesBurned;
    }
}
