package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkoutRequestDto {

    @JsonProperty("user_id")
    private Long userId;

    private String description;

    private Boolean save;

    @JsonProperty("workout_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workoutDate;

    private String notes;

    public boolean shouldSave() {
        // Default to saving the generated workout unless the client explicitly opts out
        return !Boolean.FALSE.equals(save);
    }
}
